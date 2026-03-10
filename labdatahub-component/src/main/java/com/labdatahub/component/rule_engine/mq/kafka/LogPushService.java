package com.labdatahub.component.rule_engine.mq.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 异步日志推送服务
 */
public class LogPushService {
    private static final Logger logger = LoggerFactory.getLogger(LogPushService.class);

    private final KafkaProducerManager producerManager;
    private final BlockingQueue<LogMessage> logQueue;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread workerThread;
    private String producerId;
    private volatile boolean initialized = false;

    public LogPushService() {
        this.producerManager = KafkaProducerManager.getInstance();
        this.logQueue = new LinkedBlockingQueue<>(10000); // 队列容量
    }

    /**
     * 初始化日志推送服务 - 基础初始化方法
     */
    public boolean initialize(String producerId, String bootstrapServers) {
        return initialize(producerId, bootstrapServers, (Properties) null);
    }

    /**
     * 初始化日志推送服务 - 带自定义配置
     */
    public boolean initialize(String producerId, String bootstrapServers, Properties customProperties) {
        // 防止重复初始化
        if (initialized) {
            logger.warn("LogPushService already initialized");
            return true;
        }

        this.producerId = producerId;

        // 创建生产者配置
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("acks", "1");
        props.put("retries", 3);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("max.block.ms", "10000");
        props.put("request.timeout.ms", "15000");

        // 合并自定义配置
        if (customProperties != null) {
            props.putAll(customProperties);
        }

        // 创建生产者
        boolean success = producerManager.createProducer(producerId, bootstrapServers, props,null);
        if (success) {
            startWorkerThread();
            initialized = true;
            logger.info("Log push service initialized successfully with producer: {}", producerId);
        } else {
            logger.error("Failed to initialize log push service with producer: {}", producerId);
        }
        return success;
    }

    /**
     * 初始化时预创建topic
     */
    public boolean initializeWithTopics(String producerId, String bootstrapServers, List<String> preCreateTopics) {
        return initializeWithTopics(producerId, bootstrapServers, preCreateTopics, null);
    }

    public boolean initializeWithTopics(String producerId, String bootstrapServers,
                                        List<String> preCreateTopics, Properties customProperties) {
        boolean success = initialize(producerId, bootstrapServers, customProperties);
        if (success && preCreateTopics != null) {
            for (String topic : preCreateTopics) {
                producerManager.ensureTopicExists(producerId, topic);
            }
        }
        return success;
    }

    /**
     * 推送日志，确保topic存在
     */
    public void pushLogWithTopicCheck(String topic, String logMessage) {
        pushLogWithTopicCheck(topic, null, logMessage);
    }

    public void pushLogWithTopicCheck(String topic, String key, String logMessage) {
        if (!running.get()) {
            logger.warn("Log push service is not running");
            return;
        }

        // 确保topic存在
        producerManager.ensureTopicExists(producerId, topic);
        pushLog(topic, key, logMessage);
    }

    /**
     * 推送日志（异步）
     */
    public void pushLog(String topic, String logMessage) {
        pushLog(topic, null, logMessage);
    }

    public void pushLog(String topic, String key, String logMessage) {
        if (!running.get()) {
            logger.warn("Log push service is not running");
            return;
        }

        if (!initialized) {
            logger.warn("Log push service not initialized, message dropped");
            return;
        }

        LogMessage message = new LogMessage(topic, key, logMessage, System.currentTimeMillis());
        if (!logQueue.offer(message)) {
            logger.warn("Log queue is full, message dropped: {}",
                    logMessage.substring(0, Math.min(logMessage.length(), 100)));
        }
    }

    /**
     * 批量推送日志
     */
    public void pushLogsBatch(String topic, List<String> logMessages) {
        if (logMessages == null || logMessages.isEmpty()) {
            return;
        }

        for (String message : logMessages) {
            pushLog(topic, message);
        }
    }

    /**
     * 启动工作线程
     */
    private void startWorkerThread() {
        if (running.get()) {
            logger.warn("Worker thread already running");
            return;
        }

        running.set(true);
        workerThread = new Thread(() -> {
            logger.info("Log push worker thread started for producer: {}", producerId);

            while (running.get() || !logQueue.isEmpty()) {
                try {
                    LogMessage logMessage = logQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (logMessage != null) {
                        boolean sent = producerManager.sendLogAsync(
                                producerId,
                                logMessage.topic,
                                logMessage.key,
                                logMessage.message
                        );

                        if (!sent) {
                            logger.error("Failed to send log message to topic: {}", logMessage.topic);
                            // 可以在这里添加重试逻辑或者落盘处理
                            retrySend(logMessage);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error in log push worker", e);
                }
            }

            logger.info("Log push worker thread stopped for producer: {}", producerId);
        }, "LogPushWorker-" + producerId);

        workerThread.start();
    }

    /**
     * 重试发送失败的消息
     */
    private void retrySend(LogMessage logMessage) {
        // 简单的重试逻辑，可以扩展为更复杂的重试策略
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                Thread.sleep(1000 * (i + 1)); // 递增延迟
                boolean sent = producerManager.sendLogSync(producerId,
                        logMessage.topic, logMessage.key, logMessage.message, 5000);
                if (sent) {
                    logger.info("Retry successful for message to topic: {}", logMessage.topic);
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.warn("Retry {} failed for topic {}: {}", i + 1, logMessage.topic, e.getMessage());
            }
        }

        logger.error("All retries failed for message to topic: {}", logMessage.topic);
        // 这里可以添加消息落盘逻辑
    }

    /**
     * 优雅关闭服务
     */
    public void shutdown() {
        shutdown(false);
    }

    /**
     * 关闭服务
     * @param processRemaining 是否处理剩余消息
     */
    public void shutdown(boolean processRemaining) {
        if (!initialized) {
            logger.info("Log push service not initialized, shutdown skipped");
            return;
        }

        logger.info("Starting log push service shutdown...");
        running.set(false);

        // 停止工作线程
        if (workerThread != null) {
            workerThread.interrupt();
            try {
                workerThread.join(3000); // 等待3秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 处理剩余消息
        if (processRemaining) {
            processRemainingLogs();
        } else {
            int remainingCount = logQueue.size();
            if (remainingCount > 0) {
                logger.warn("Discarding {} unsent log messages during shutdown", remainingCount);
                logQueue.clear();
            }
        }

        initialized = false;
        logger.info("Log push service shutdown completed");
    }

    /**
     * 处理队列中剩余的消息
     */
    private void processRemainingLogs() {
        int remainingCount = 0;
        int successCount = 0;

        while (!logQueue.isEmpty()) {
            LogMessage logMessage = logQueue.poll();
            if (logMessage != null) {
                remainingCount++;
                boolean sent = producerManager.sendLogSync(producerId,
                        logMessage.topic, logMessage.key, logMessage.message, 5000);
                if (sent) {
                    successCount++;
                } else {
                    logger.warn("Failed to send remaining log: {}",
                            logMessage.message.substring(0, Math.min(logMessage.message.length(), 100)));
                    // 这里可以添加落盘逻辑
                }
            }
        }

        if (remainingCount > 0) {
            logger.info("Processed {} remaining log messages, {} successful",
                    remainingCount, successCount);
        }
    }

    /**
     * 获取队列状态
     */
    public int getQueueSize() {
        return logQueue.size();
    }

    /**
     * 检查服务是否运行
     */
    public boolean isRunning() {
        return running.get() && initialized;
    }

    /**
     * 检查服务是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 获取生产者ID
     */
    public String getProducerId() {
        return producerId;
    }

    /**
     * 日志消息封装类
     */
    private static class LogMessage {
        private final String topic;
        private final String key;
        private final String message;
        private final long timestamp;

        public LogMessage(String topic, String key, String message, long timestamp) {
            this.topic = topic;
            this.key = key;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
}