package com.labdatahub.business.process.log;
import com.labdatahub.business.domain.LabdatahubDeviceLogs;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 极简高性能日志处理器（单例）
 */
@Slf4j
public final class LogProcessor {

    // 单例实例
    private static volatile LogProcessor INSTANCE;

    // 获取单例
    public static LogProcessor getInstance() {
        if (INSTANCE == null) {
            synchronized (LogProcessor.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LogProcessor();
                }
            }
        }
        return INSTANCE;
    }

    // 私有构造
    private LogProcessor() {
        // 注册JVM关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    // 内部状态
    private BlockingQueue<LabdatahubDeviceLogs> queue;
    private volatile Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private LogConsumer consumer;
    private int batchSize = 1000;

    private int maxQueueSize = 10000;

    /**
     * 启动消费
     * @param batchSize 批次大小
     * @param maxQueueSize 最大队列容量（超过丢弃）
     * @param consumer 消费处理器
     */
    public synchronized void start(int batchSize, int maxQueueSize, LogConsumer consumer) {
        if (running.get()) {
            return;
        }

        this.batchSize = Math.max(1, batchSize);
        this.consumer = consumer;
        this.queue = new LinkedBlockingQueue<>(maxQueueSize);
        this.running.set(true);
        this.maxQueueSize = maxQueueSize;
        // 启动消费线程
        worker = new Thread(this::consume);
        worker.setDaemon(true);
        worker.setName("LogProcessor-Thread");
        worker.start();

        System.out.println("LogProcessor started. BatchSize: " + batchSize + ", QueueSize: " + maxQueueSize);
    }


    /**
     * 动态修改队列容量（不重启，丢弃当前队列数据）
     * @param newMaxSize 新的队列最大容量
     */
    public synchronized void resizeQueueAndDiscard(int newMaxSize) {
        if (newMaxSize < 1) {
            throw new IllegalArgumentException("Max queue size must be at least 1");
        }

        this.maxQueueSize = newMaxSize;

        if (!running.get() || queue == null) {
            return;
        }

        try {
            queue = new LinkedBlockingQueue<>(newMaxSize);

        } catch (Exception ignored) {
        }
    }

    /**
     * 修改batchSize
     */
    public synchronized void setBatchSize(int batchSize){
        this.batchSize = batchSize;
    }

    /**
     * 停止消费（优雅关闭）
     */
    public synchronized void stop() {
        if (!running.get()) {
            return;
        }

        running.set(false);
        if (worker != null) {
            worker.interrupt();
            try {
                worker.join(5000); // 等待5秒结束
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 处理剩余数据
        processRemaining();

        System.out.println("LogProcessor stopped");
    }

    /**
     * 添加日志（队列满时自动丢弃）
     * @param logs 日志内容
     * @return 是否成功添加
     */
    public boolean addLog(LabdatahubDeviceLogs logs) {
        if (!running.get() || queue == null) {
            return false;
        }
        boolean result = queue.offer(logs);
        if(!result){
            log.error("丢弃");
        }
        return result; // 队列满时返回false，自动丢弃
    }

    /**
     * 批量添加日志
     * @param logs 日志列表
     * @return 成功添加的数量
     */
    public int addLogs(List<LabdatahubDeviceLogs> logs) {
        if (!running.get() || queue == null || logs == null) {
            return 0;
        }

        int count = 0;
        for (LabdatahubDeviceLogs log : logs) {
            if (queue.offer(log)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 核心消费方法（极致优化）
     */
    private void consume() {
        List<LabdatahubDeviceLogs> batch = new ArrayList<>(batchSize);

        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                // 清空批次重用，避免GC
                batch.clear();

                // 优化：先非阻塞取一批
                int drained = queue.drainTo(batch, batchSize);

                // 如果有数据立即处理
                if (drained > 0) {
                    consumer.consume(batch);
                    continue;
                }

                // 没有数据时，等待第一条（避免空转）
                LabdatahubDeviceLogs first = queue.poll(100, TimeUnit.MILLISECONDS);
                if (first != null) {
                    batch.add(first);
                    // 立即取剩余数据
                    queue.drainTo(batch, batchSize - 1);
                    consumer.consume(batch);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("LogProcessor error: " + e.getMessage());
                try {
                    Thread.sleep(1000); // 出错时休眠1秒
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // 线程退出前清空批次
        batch.clear();
    }

    /**
     * 处理剩余数据
     */
    private void processRemaining() {
        if (queue == null || consumer == null) {
            return;
        }

        List<LabdatahubDeviceLogs> remaining = new ArrayList<>();
        queue.drainTo(remaining);

        if (!remaining.isEmpty()) {
            System.out.println("Processing remaining logs: " + remaining.size());
            consumer.consume(remaining);
        }
    }

    /**
     * 获取队列大小
     */
    public int getQueueSize() {
        return queue != null ? queue.size() : 0;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * 是否运行中
     */
    public boolean isRunning() {
        return running.get();
    }
}