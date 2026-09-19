//由AI修改
package com.labdatahub.component.s7_tcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S7LoopConsumer {
	// 存储已启动的消费线程：key=componentId，value=消费线程（避免重复启动）
    private static final Map<String, ConsumeThread> CONSUME_THREAD_MAP = new ConcurrentHashMap<>();
    // 消费线程前缀（便于日志排查）
    private static final String CONSUME_THREAD_NAME_PREFIX = "s7-loop-consumer-";
    private static final long STOP_TIMEOUT_MS = 5000L;
    // 消息队列不存在时的重试间隔（毫秒）。
    // 队列是在读配置的定时任务里懒创建的：组件先连上、读配置后到时队列还不存在，
    // 这种情况必须退避等待 —— 直接取消息会「抛异常→立刻重试」空转，实测能把日志刷到 100MB/分钟
    private static final long QUEUE_MISSING_RETRY_MS = 1000L;
    // 连续异常时：前 N 次打全量日志（带堆栈），之后每分钟汇总一条，避免刷屏把磁盘打满
    private static final int FULL_LOG_LIMIT = 3;
    private static final long SUMMARY_LOG_INTERVAL_MS = 60000L;
    // 一条消息都没取到就抛异常时的退避（handler 自己抛的异常已消费掉一条消息，按消息节奏走，不额外等待）
    private static final long NO_MESSAGE_RETRY_MS = 200L;

    private static class ConsumeThread extends Thread {
        private final String componentId;
        private final S7MessageConsumeHandler handler;
        private final AtomicBoolean isRunning = new AtomicBoolean(true);

        public ConsumeThread(String componentId, S7MessageConsumeHandler handler) {
            super(CONSUME_THREAD_NAME_PREFIX + componentId);
            this.componentId = componentId;
            this.handler = handler;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            log.info("启动 S7 消费线程 componentId={}", componentId);
            // 连续异常/等待计数 + 上次汇总日志时间（只在本线程内读写，无需同步）
            int consecutiveErrors = 0;
            long lastSummaryAt = 0L;
            while (isRunning.get()) {
                boolean gotMessage = false;
                try {
                    // 队列由读配置的定时任务懒创建，可能尚未存在：不存在就退避等待，不能空转
                    BlockingQueue<S7Message> queue = S7MessageScheduler.messageQueueMap.get(componentId);
                    if (queue == null) {
                        consecutiveErrors++;
                        long now = System.currentTimeMillis();
                        if (consecutiveErrors <= FULL_LOG_LIMIT) {
                            log.warn("componentId={} 消息队列尚未创建（读配置未启动），{}ms 后重试",
                                    componentId, QUEUE_MISSING_RETRY_MS);
                        } else if (now - lastSummaryAt >= SUMMARY_LOG_INTERVAL_MS) {
                            lastSummaryAt = now;
                            log.warn("componentId={} 消息队列仍不存在，已连续等待 {} 次（日志按分钟汇总）",
                                    componentId, consecutiveErrors);
                        }
                        if (!sleepQuietly(QUEUE_MISSING_RETRY_MS)) {
                            break; // 等待中被中断 = 要求停止
                        }
                        continue;
                    }
                	// 阻塞获取指定componentId的消息（无消息时等待，不耗CPU）
                    S7Message message = queue.take();
                    gotMessage = true;
                    // 调用业务侧的消费逻辑
                    handler.handle(componentId, message);
                    if (consecutiveErrors > 0) {
                        log.info("componentId={} 消费已恢复正常（此前连续异常/等待 {} 次）", componentId, consecutiveErrors);
                        consecutiveErrors = 0;
                        lastSummaryAt = 0L;
                    }
                } catch (InterruptedException e) {
                    log.info("componentId={} 消费线程被中断，准备停止", componentId);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // 消费单条消息异常：打印日志，不中断整个消费循环
                    consecutiveErrors++;
                    long now = System.currentTimeMillis();
                    if (consecutiveErrors <= FULL_LOG_LIMIT) {
                        log.error("componentId={} 消费消息异常（连续第{}次）", componentId, consecutiveErrors, e);
                    } else if (now - lastSummaryAt >= SUMMARY_LOG_INTERVAL_MS) {
                        lastSummaryAt = now;
                        log.error("componentId={} 消费消息已连续异常 {} 次，日志按分钟汇总，最近一次：{}",
                                componentId, consecutiveErrors, e.toString());
                    }
                    // 没取到消息就抛异常（如队列刚好被移除）必须退避，否则又是空转刷日志
                    if (!gotMessage && !sleepQuietly(NO_MESSAGE_RETRY_MS)) {
                        break;
                    }
                }
            }
            // 线程退出，清理映射（仅当映射值仍是本线程时才删除，避免旧线程退出误删新注册的消费线程）
            CONSUME_THREAD_MAP.remove(componentId, this);
            log.info("S7 消费线程已停止 componentId={}", componentId);
        }

        /**
         * 可中断的休眠
         * @param ms 休眠毫秒数
         * @return true=正常睡完；false=被中断（调用方应结束循环）
         */
        private boolean sleepQuietly(long ms) {
            try {
                Thread.sleep(ms);
                return true;
            } catch (InterruptedException e) {
                log.info("componentId={} 消费线程在等待中被中断，准备停止", componentId);
                Thread.currentThread().interrupt();
                return false;
            }
        }

        public void stopConsume() {
            isRunning.set(false);
            this.interrupt();
        }

        public boolean isConsuming() {
            return isRunning.get() && this.isAlive();
        }
    }
    
    /**
     * 启动指定componentId的循环消费
     * @param componentId 目标组件ID（不能为空）
     * @param consumeHandler 消费回调（业务侧自定义消息处理逻辑）
     * @throws IllegalArgumentException 参数非法时抛出
     * @throws IllegalStateException 该componentId已启动消费时抛出
     */
    public static void startConsume(String componentId, S7MessageConsumeHandler handler) {
        if (StringUtils.isBlank(componentId) || handler == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (CONSUME_THREAD_MAP.containsKey(componentId)) {
            ConsumeThread exist = CONSUME_THREAD_MAP.get(componentId);
            if (exist.isConsuming()) {
                throw new IllegalStateException("componentId=" + componentId + " 已启动消费");
            } else {
                CONSUME_THREAD_MAP.remove(componentId);
            }
        }
        ConsumeThread thread = new ConsumeThread(componentId, handler);
        CONSUME_THREAD_MAP.put(componentId, thread);
        thread.start();
    }

    /**
     * 停止指定componentId的循环消费
     * @param componentId 目标组件ID（不能为空）
     */
    public static void stopConsume(String componentId) {
        if (StringUtils.isBlank(componentId)) {
            return;
        }
        ConsumeThread consumeThread = CONSUME_THREAD_MAP.get(componentId);
        if (consumeThread != null && consumeThread.isConsuming()) {
            consumeThread.stopConsume();
            try {
                consumeThread.join(STOP_TIMEOUT_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("等待 componentId={} 消费线程停止时被中断", componentId, e);
            }
        }
    }
    
    /**
     * 检查指定componentId是否正在消费
     * @param componentId 目标组件ID
     * @return true=正在消费，false=未消费/已停止
     */
    public static boolean isConsuming(String componentId) {
    	if (StringUtils.isBlank(componentId)) {
            return false;
        }
        ConsumeThread thread = CONSUME_THREAD_MAP.get(componentId);
        return thread != null && thread.isConsuming();
    }

    public static void stopAllConsume() {
    	List<String> componentIds = new ArrayList<>(CONSUME_THREAD_MAP.keySet());
        for (String componentId : componentIds) {
            stopConsume(componentId);
        }
        CONSUME_THREAD_MAP.clear();
        log.info("所有S7消费线程已停止");
    }
}