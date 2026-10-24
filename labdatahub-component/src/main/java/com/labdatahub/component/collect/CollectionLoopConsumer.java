package com.labdatahub.component.collect;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一采集消费线程：每个组件一条，串行执行该组件的读取任务
 *
 * <p>为什么串行：一条 TCP 连接（或一个 FOCAS2 句柄）上「发一帧收一帧」，
 * 并发读只会互相取错响应；旧链路也是每组件一条消费线程，这里保持一致。
 * 吞吐靠的是「一次读一批点位」和「读完立刻读下一条（不再 sleep delayTime）」。
 */
@Slf4j
public final class CollectionLoopConsumer {

    /** 已启动的消费线程：key = 组件ID */
    private static final Map<String, ConsumeThread> THREADS = new ConcurrentHashMap<>();

    /** 等任务的最长时间（毫秒）：到点回来重新检查连接，避免长期阻塞后无法自愈 */
    private static final long POLL_TIMEOUT_MS = 1000L;

    /** 停止时等待线程退出（毫秒） */
    private static final long STOP_TIMEOUT_MS = 5000L;

    /** 连续异常时前 N 次打完整日志，之后按分钟汇总 */
    private static final int FULL_LOG_LIMIT = 3;
    private static final long SUMMARY_LOG_INTERVAL_MS = 60000L;

    private CollectionLoopConsumer() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 启动组件消费线程
     *
     * @param componentId 组件ID
     * @param state 组件状态
     * @param adapter 协议适配器
     * @throws IllegalStateException 该组件已在消费
     */
    static void start(String componentId, CollectionEngine.ComponentState state, CollectionAdapter adapter) {
        if (adapter == null) {
            log.error("[统一采集] componentId={} 没有协议适配器，消费线程不启动", componentId);
            return;
        }
        ConsumeThread existing = THREADS.get(componentId);
        if (existing != null) {
            if (existing.isConsuming()) {
                throw new IllegalStateException("componentId=" + componentId + " 已启动循环消费，请勿重复启动");
            }
            THREADS.remove(componentId);
        }
        ConsumeThread thread = new ConsumeThread(componentId, state, adapter);
        THREADS.put(componentId, thread);
        thread.start();
    }

    /**
     * 停止组件消费线程
     *
     * @param componentId 组件ID
     */
    static void stop(String componentId) {
        ConsumeThread thread = THREADS.get(componentId);
        if (thread == null || !thread.isConsuming()) {
            return;
        }
        thread.stopConsume();
        try {
            thread.join(STOP_TIMEOUT_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("等待 componentId={} 采集消费线程停止时被中断", componentId);
        }
    }

    /**
     * 组件是否正在消费
     *
     * @param componentId 组件ID
     * @return true=消费线程存活
     */
    static boolean isConsuming(String componentId) {
        ConsumeThread thread = componentId == null ? null : THREADS.get(componentId);
        return thread != null && thread.isConsuming();
    }

    /**
     * 消费线程
     */
    private static final class ConsumeThread extends Thread {

        /** 组件ID */
        private final String componentId;

        /** 组件状态（点位/协议族） */
        private final CollectionEngine.ComponentState state;

        /** 协议适配器 */
        private final CollectionAdapter adapter;

        /** 运行标志 */
        private volatile boolean running = true;

        ConsumeThread(String componentId, CollectionEngine.ComponentState state, CollectionAdapter adapter) {
            super(state.family.toLowerCase(Locale.ROOT) + "-loop-consumer-" + componentId);
            this.componentId = componentId;
            this.state = state;
            this.adapter = adapter;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            log.info("[统一采集] 启动消费线程 componentId={}, family={}", componentId, state.family);
            int consecutiveErrors = 0;
            long lastSummaryAt = 0L;
            while (running) {
                // 连接保障（FOCAS2 句柄必须在消费线程上建立），失败不影响循环
                try {
                    adapter.ensureConnection(componentId);
                } catch (Throwable t) {
                    log.warn("[统一采集] componentId={} 连接保障失败：{}", componentId, t.toString());
                }
                ReadTask task;
                try {
                    CollectionQueue queue = CollectionEngine.queue(componentId);
                    if (queue == null) {
                        if (!sleepQuietly(POLL_TIMEOUT_MS)) {
                            break;
                        }
                        continue;
                    }
                    task = queue.poll(POLL_TIMEOUT_MS);
                } catch (InterruptedException e) {
                    log.info("[统一采集] componentId={} 消费线程被中断，准备停止", componentId);
                    Thread.currentThread().interrupt();
                    break;
                }
                if (task == null) {
                    continue;
                }
                try {
                    Map<String, Object> values = adapter.execute(componentId, task);
                    if (values != null && !values.isEmpty()) {
                        publish(adapter, task, values);
                    }
                    if (consecutiveErrors > 0) {
                        log.info("[统一采集] componentId={} 消费已恢复正常（此前连续异常 {} 次）",
                                componentId, consecutiveErrors);
                        consecutiveErrors = 0;
                        lastSummaryAt = 0L;
                    }
                } catch (Exception e) {
                    consecutiveErrors++;
                    long now = System.currentTimeMillis();
                    if (consecutiveErrors <= FULL_LOG_LIMIT) {
                        log.error("[统一采集] componentId={} 读取失败（连续第{}次），task={}",
                                componentId, consecutiveErrors, task.getTaskKey(), e);
                    } else if (now - lastSummaryAt >= SUMMARY_LOG_INTERVAL_MS) {
                        lastSummaryAt = now;
                        log.error("[统一采集] componentId={} 读取已连续失败 {} 次（日志按分钟汇总），最近一次：{}",
                                componentId, consecutiveErrors, e.toString());
                    }
                    try {
                        adapter.onFailure(componentId, task, e);
                    } catch (Throwable ignored) {
                        // 失败清理本身出错不额外处理，不能把消费线程带崩
                    }
                }
            }
            THREADS.remove(componentId, this);
            log.info("[统一采集] 消费线程已停止 componentId={}", componentId);
        }

        /**
         * 上报一条任务的读取结果
         */
        private void publish(CollectionAdapter adapter, ReadTask task, Map<String, Object> values) {
            if (adapter.publishOncePerTask()) {
                // 数据库采集：一条任务就是一份最新记录，只上报一次
                CollectionPoint first = task.getPoints().get(0);
                Object raw = values.values().iterator().next();
                try {
                    CollectionPublisher.publish(adapter, task, first, raw);
                } catch (Exception e) {
                    log.error("[统一采集] componentId={} 上报失败，task={}", componentId, task.getTaskKey(), e);
                }
                return;
            }
            for (CollectionPoint point : task.getPoints()) {
                if (!values.containsKey(point.key())) {
                    // 该点位本次没读到（不支持/越界/读失败），与旧链路的「跳过本次」一致
                    continue;
                }
                try {
                    CollectionPublisher.publish(adapter, task, point, values.get(point.key()));
                } catch (Exception e) {
                    log.error("[统一采集] componentId={} 点位{}上报失败", componentId, point.getCode(), e);
                }
            }
        }

        /**
         * 可中断休眠
         *
         * @param ms 毫秒
         * @return true=正常睡完；false=被中断
         */
        private boolean sleepQuietly(long ms) {
            try {
                Thread.sleep(ms);
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        /**
         * 优雅停止
         */
        void stopConsume() {
            running = false;
            interrupt();
        }

        /**
         * 是否在运行
         *
         * @return true=在运行
         */
        boolean isConsuming() {
            return running && isAlive();
        }
    }
}
