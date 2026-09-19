package com.labdatahub.component.db;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
* @ClassName: DatabaseLoopConsumer  
* @Description: 循环消费者   
* @author xwb  
* @date 2026年4月1日
 */
@Slf4j
public class DatabaseLoopConsumer {

    // 存储已启动的消费线程：key = datasourceId
    private static final Map<String, ConsumeThread> consumeThreadMap = new ConcurrentHashMap<>();
    private static final String THREAD_NAME_PREFIX = "db-loop-consumer-";
    private static final long STOP_TIMEOUT_MS = 5000L;
    // 消息队列不存在时的重试间隔（毫秒）。
    // 队列是在读配置的定时任务里懒创建的：数据源先连上、读配置后到时队列还不存在，
    // 这种情况必须退避等待 —— 直接取消息会「抛异常→立刻重试」空转，实测能把日志刷到 100MB/分钟
    private static final long QUEUE_MISSING_RETRY_MS = 1000L;
    // 连续异常时：前 N 次打全量日志（带堆栈），之后每分钟汇总一条，避免刷屏把磁盘打满
    private static final int FULL_LOG_LIMIT = 3;
    private static final long SUMMARY_LOG_INTERVAL_MS = 60000L;
    // 一条消息都没取到就抛异常时的退避（handler 自己抛的异常已消费掉一条消息，按消息节奏走，不额外等待）
    private static final long NO_MESSAGE_RETRY_MS = 200L;

    private static class ConsumeThread extends Thread {
        private final String datasourceId;
        private final DatabaseMessageConsumeHandler handler;
        private final AtomicBoolean isRunning = new AtomicBoolean(true);

        public ConsumeThread(String datasourceId, DatabaseMessageConsumeHandler handler) {
            super(THREAD_NAME_PREFIX + datasourceId);
            this.datasourceId = datasourceId;
            this.handler = handler;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            log.info("启动数据库消费线程 datasourceId={}", datasourceId);
            // 连续异常/等待计数 + 上次汇总日志时间（只在本线程内读写，无需同步）
            int consecutiveErrors = 0;
            long lastSummaryAt = 0L;
            while (isRunning.get()) {
                boolean gotMessage = false;
                try {
                    // 队列由读配置的定时任务懒创建，可能尚未存在：不存在就退避等待，不能空转
                    BlockingQueue<DatabaseMessage> queue = DatabaseMessageScheduler.messageQueueMap.get(datasourceId);
                    if (queue == null) {
                        consecutiveErrors++;
                        long now = System.currentTimeMillis();
                        if (consecutiveErrors <= FULL_LOG_LIMIT) {
                            log.warn("datasourceId={} 消息队列尚未创建（读配置未启动），{}ms 后重试",
                                    datasourceId, QUEUE_MISSING_RETRY_MS);
                        } else if (now - lastSummaryAt >= SUMMARY_LOG_INTERVAL_MS) {
                            lastSummaryAt = now;
                            log.warn("datasourceId={} 消息队列仍不存在，已连续等待 {} 次（日志按分钟汇总）",
                                    datasourceId, consecutiveErrors);
                        }
                        if (!sleepQuietly(QUEUE_MISSING_RETRY_MS)) {
                            break; // 等待中被中断 = 要求停止
                        }
                        continue;
                    }
                    DatabaseMessage message = queue.take();
                    gotMessage = true;
                    handler.handle(datasourceId, message);
                    if (consecutiveErrors > 0) {
                        log.info("datasourceId={} 消费已恢复正常（此前连续异常/等待 {} 次）", datasourceId, consecutiveErrors);
                        consecutiveErrors = 0;
                        lastSummaryAt = 0L;
                    }
                } catch (InterruptedException e) {
                    log.info("datasourceId={} 消费线程被中断，准备停止", datasourceId);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // 消费单条消息异常：打印日志，不中断整个消费循环
                    consecutiveErrors++;
                    long now = System.currentTimeMillis();
                    if (consecutiveErrors <= FULL_LOG_LIMIT) {
                        log.error("datasourceId={} 消费消息异常（连续第{}次）", datasourceId, consecutiveErrors, e);
                    } else if (now - lastSummaryAt >= SUMMARY_LOG_INTERVAL_MS) {
                        lastSummaryAt = now;
                        log.error("datasourceId={} 消费消息已连续异常 {} 次，日志按分钟汇总，最近一次：{}",
                                datasourceId, consecutiveErrors, e.toString());
                    }
                    // 没取到消息就抛异常（如队列刚好被移除）必须退避，否则又是空转刷日志
                    if (!gotMessage && !sleepQuietly(NO_MESSAGE_RETRY_MS)) {
                        break;
                    }
                }
            }
            consumeThreadMap.remove(datasourceId);
            log.info("数据库消费线程已停止 datasourceId={}", datasourceId);
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
                log.info("datasourceId={} 消费线程在等待中被中断，准备停止", datasourceId);
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
     * 启动指定数据源的循环消费
     */
    public static void startConsume(String datasourceId, DatabaseMessageConsumeHandler handler) {
        if (StringUtils.isBlank(datasourceId) || handler == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (consumeThreadMap.containsKey(datasourceId)) {
            ConsumeThread exist = consumeThreadMap.get(datasourceId);
            if (exist.isConsuming()) {
                throw new IllegalStateException("datasourceId=" + datasourceId + " 已启动消费");
            } else {
                consumeThreadMap.remove(datasourceId);
            }
        }
        ConsumeThread thread = new ConsumeThread(datasourceId, handler);
        consumeThreadMap.put(datasourceId, thread);
        thread.start();
    }

    /**
     * 停止指定数据源的循环消费
     */
    public static void stopConsume(String datasourceId) {
        if (StringUtils.isBlank(datasourceId)) {
            return;
        }
        ConsumeThread thread = consumeThreadMap.get(datasourceId);
        if (thread != null && thread.isConsuming()) {
            thread.stopConsume();
            try {
                thread.join(STOP_TIMEOUT_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("等待 datasourceId={} 消费线程停止时被中断", datasourceId, e);
            }
        }
    }

    /**
     * 检查是否正在消费
     */
    public static boolean isConsuming(String datasourceId) {
        if (StringUtils.isBlank(datasourceId)) {
            return false;
        }
        ConsumeThread thread = consumeThreadMap.get(datasourceId);
        return thread != null && thread.isConsuming();
    }

    /**
     * 停止所有消费线程
     */
    public static void stopAllConsume() {
        List<String> ids = new ArrayList<>(consumeThreadMap.keySet());
        for (String id : ids) {
            stopConsume(id);
        }
        consumeThreadMap.clear();
        log.info("所有数据库消费线程已停止");
    }
}