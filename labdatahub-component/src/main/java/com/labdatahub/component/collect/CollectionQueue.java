package com.labdatahub.component.collect;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 单个组件的任务队列
 *
 * <p>与旧链路的关键差别：<b>同一个任务不会在队列里重复排队</b>。
 * 一条任务已经排着还没执行时，下一次定时触发直接跳过 —— 设备离线、单次读超时（数秒）时，
 * 队列不会按「点位 × 超时时间」那样堆成上千条陈旧任务，队列里始终只有一批最新任务。
 */
public class CollectionQueue {

    /** 任务队列 */
    private final LinkedBlockingQueue<ReadTask> queue = new LinkedBlockingQueue<>();

    /** 已入队未取走的任务键 */
    private final Set<String> pendingKeys = ConcurrentHashMap.newKeySet();

    /** 队列容量上限 */
    private final int capacity;

    public CollectionQueue(int capacity) {
        this.capacity = capacity;
    }

    /**
     * 投递任务
     *
     * @param task 读取任务
     * @return true=已入队；false=同一任务已在队列中（本次跳过）
     */
    public boolean offer(ReadTask task) {
        if (!pendingKeys.add(task.getTaskKey())) {
            return false;
        }
        // 同一任务不会重复入队，队列长度上限实际由「任务数」决定，这里只兜底异常情况
        while (queue.size() >= capacity) {
            ReadTask dropped = queue.poll();
            if (dropped == null) {
                break;
            }
            pendingKeys.remove(dropped.getTaskKey());
        }
        queue.offer(task);
        return true;
    }

    /**
     * 取任务（等待指定时间）
     *
     * @param timeoutMs 等待毫秒数
     * @return 任务；超时返回 null
     * @throws InterruptedException 线程中断
     */
    public ReadTask poll(long timeoutMs) throws InterruptedException {
        ReadTask task = queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        if (task != null) {
            pendingKeys.remove(task.getTaskKey());
        }
        return task;
    }

    /**
     * 清空队列
     */
    public void clear() {
        queue.clear();
        pendingKeys.clear();
    }

    /**
     * 当前排队任务数
     *
     * @return 任务数
     */
    public int size() {
        return queue.size();
    }
}
