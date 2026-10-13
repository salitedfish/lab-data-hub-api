package com.labdatahub.business.process.log;
import com.labdatahub.business.domain.LabdatahubDeviceLogs;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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

    /** 攒批等待上限（毫秒）：不足一批时，最多再等这么久就把已到的先写掉 */
    private static final long FLUSH_WAIT_MS = 200;

    /** 队列满丢弃的汇总日志间隔（毫秒） */
    private static final long DROP_LOG_INTERVAL_MS = 60000;

    /** 累计丢弃条数（供汇总日志使用） */
    private final AtomicLong droppedCount = new AtomicLong(0);

    /** 上一次打印丢弃汇总的时刻（毫秒） */
    private final AtomicLong lastDropLogMs = new AtomicLong(0);

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
        if (!result) {
            // 队列满即丢弃。原先每丢一条打一行日志 —— 设备规模上来后（100 台）队列一旦压满，
            // 就是每秒上百行的日志洪水，反而把磁盘 IO 一起拖垮。改成按分钟汇总一条。
            long dropped = droppedCount.incrementAndGet();
            long now = System.currentTimeMillis();
            if (now - lastDropLogMs.get() >= DROP_LOG_INTERVAL_MS) {
                lastDropLogMs.set(now);
                log.error("设备日志入库队列已满（上限 {}），累计丢弃 {} 条。消费跟不上生产，需调大批量落库或降低采集频率",
                        maxQueueSize, dropped);
            }
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
                    consumeAccumulated(batch);
                    continue;
                }

                // 没有数据时，等待第一条（避免空转）
                LabdatahubDeviceLogs first = queue.poll(100, TimeUnit.MILLISECONDS);
                if (first != null) {
                    batch.add(first);
                    // 立即取剩余数据
                    queue.drainTo(batch, batchSize - 1);
                    consumeAccumulated(batch);
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
     * 攒够再落库：不足一批时再等一小段，把这段时间到达的补进来，一次性写。
     *
     * <p>{@code drainTo} 是<b>非阻塞</b>的 —— 稳态下（比如 100 台设备 × 5 点位 ÷ 5 秒 = 100 条/秒）
     * 每次只拿到零星几条，直接 {@code consume} 等于「批量开关开着、实际一条一插」，
     * 那时开关的唯一收益只剩「把 INSERT 收敛到一条线程」。
     * 这里最多再等 {@link #FLUSH_WAIT_MS}，攒到一批或到点就写 —— 用 200 毫秒的入库延迟换真正的批量。
     *
     * @param batch 已取到的日志；方法内补齐并消费，返回后 batch 的内容不再有效
     */
    private void consumeAccumulated(List<LabdatahubDeviceLogs> batch) {
        long deadline = System.currentTimeMillis() + FLUSH_WAIT_MS;
        while (batch.size() < batchSize) {
            long remainMs = deadline - System.currentTimeMillis();
            if (remainMs <= 0) {
                break;
            }
            LabdatahubDeviceLogs more;
            try {
                more = queue.poll(remainMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            if (more == null) {
                break;
            }
            batch.add(more);
            queue.drainTo(batch, batchSize - batch.size());
        }
        consumer.consume(batch);
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