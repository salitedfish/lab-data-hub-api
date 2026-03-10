package com.labdatahub.component.rule_engine.mq.rocketmq;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RocketMQ异步日志推送服务
 */
public class RocketMQLogPushService {
    private static final Logger logger = LoggerFactory.getLogger(RocketMQLogPushService.class);

    private final RocketMQProducerManager producerManager;
    private final BlockingQueue<LogMessage> logQueue;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread workerThread;
    private String producerId;
    private String defaultTopic;

    public RocketMQLogPushService() {
        this.producerManager = RocketMQProducerManager.getInstance();
        this.logQueue = new LinkedBlockingQueue<>(10000);
    }

    /**
     * 初始化服务
     */
    public boolean initialize(String producerId, String nameServer, String group, String defaultTopic) {
        this.producerId = producerId;
        this.defaultTopic = defaultTopic;

        boolean success = producerManager.createProducer(producerId, nameServer, group);
        if (success) {
            startWorkerThread();
            logger.info("RocketMQ log push service initialized: {}", producerId);
        } else {
            logger.error("Failed to initialize RocketMQ log push service");
        }
        return success;
    }

    /**
     * 推送日志
     */
    public void pushLog(String message) {
        pushLog(defaultTopic, "", message);
    }

    public void pushLog(String topic, String tags, String message) {
        if (!running.get()) {
            logger.warn("Log push service not running");
            return;
        }

        LogMessage logMessage = new LogMessage(topic, tags, message, System.currentTimeMillis());
        if (!logQueue.offer(logMessage)) {
            logger.warn("Log queue full, message dropped: {}",
                    message.substring(0, Math.min(message.length(), 100)));
        }
    }

    /**
     * 启动工作线程
     */
    private void startWorkerThread() {
        running.set(true);
        workerThread = new Thread(() -> {
            logger.info("RocketMQ log push worker started: {}", producerId);

            while (running.get() || !logQueue.isEmpty()) {
                try {
                    LogMessage logMessage = logQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                    if (logMessage != null) {
                        // 使用单向消息，不关心发送结果
                        producerManager.sendOneway(producerId, logMessage.topic,
                                logMessage.tags, logMessage.message);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error in log push worker", e);
                }
            }

            logger.info("RocketMQ log push worker stopped: {}", producerId);
        }, "RocketMQLogPush-" + producerId);

        workerThread.start();
    }

    /**
     * 关闭服务
     */
    public void shutdown() {
        running.set(false);
        if (workerThread != null) {
            workerThread.interrupt();
            try {
                workerThread.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 清空队列
        int remaining = logQueue.size();
        if (remaining > 0) {
            logger.warn("Discarding {} unsent messages", remaining);
            logQueue.clear();
        }

        logger.info("RocketMQ log push service shutdown");
    }

    /**
     * 获取队列大小
     */
    public int getQueueSize() {
        return logQueue.size();
    }

    private static class LogMessage {
        final String topic;
        final String tags;
        final String message;
        final long timestamp;

        LogMessage(String topic, String tags, String message, long timestamp) {
            this.topic = topic;
            this.tags = tags;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
}
