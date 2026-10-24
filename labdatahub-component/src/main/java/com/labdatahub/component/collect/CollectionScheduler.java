package com.labdatahub.component.collect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一采集调度：按「间隔 → 采集单元 → 读取任务」排定时任务
 *
 * <p>与旧链路的一对一关系不同，这里一个定时任务覆盖一批点位，触发时只把任务投进组件队列，
 * 真正的协议读在组件唯一的消费线程上按队列顺序执行（串行，不抢同一条 socket）。
 *
 * <p>定时任务体内任何异常都必须吞掉：{@code scheduleAtFixedRate} 的任务一旦抛出异常，
 * 后续触发会被永久取消 —— 采集会静默停摆。
 */
@Slf4j
public final class CollectionScheduler {

    /** 生产线程池：只做「投递任务」，不阻塞 */
    private static final ScheduledExecutorService PRODUCER = Executors.newScheduledThreadPool(4, r -> {
        Thread t = new Thread(r, "collect-producer");
        t.setDaemon(true);
        return t;
    });

    private CollectionScheduler() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 重建组件的采集计划（配置变动后调用）
     *
     * @param state 组件状态
     */
    static void rebuild(CollectionEngine.ComponentState state) {
        if (state == null) {
            return;
        }
        CollectionAdapter adapter = CollectionRegistry.get(state.family);
        if (adapter == null) {
            log.error("[统一采集] 没有注册协议适配器：componentId={}, family={}", state.componentId, state.family);
            return;
        }
        // 先停旧计划：点位/间隔可能都变了，逐个重排比增量维护可靠
        state.cancelAll();
        if (state.points.isEmpty()) {
            return;
        }
        // 间隔分组：不同间隔的点位不能合并（合并成一次读等于把慢点位的频率拉高）
        Map<Integer, List<CollectionPoint>> byInterval = new TreeMap<>();
        for (CollectionPoint point : state.points.values()) {
            byInterval.computeIfAbsent(point.getIntervalSeconds(), k -> new ArrayList<>()).add(point);
        }
        int taskCount = 0;
        for (Map.Entry<Integer, List<CollectionPoint>> entry : byInterval.entrySet()) {
            int interval = entry.getKey();
            if (interval <= 0) {
                log.warn("[统一采集] componentId={} 存在非法采集间隔{}，相关点位跳过", state.componentId, interval);
                continue;
            }
            // 单元分组：适配器定义什么能合并（同从站/同数据区/同一条 FOCAS 调用）
            Map<String, List<CollectionPoint>> byUnit = new LinkedHashMap<>();
            for (CollectionPoint point : entry.getValue()) {
                String unitKey = adapter.unitKey(point);
                byUnit.computeIfAbsent(unitKey, k -> new ArrayList<>()).add(point);
            }
            for (Map.Entry<String, List<CollectionPoint>> unit : byUnit.entrySet()) {
                List<ReadTask> tasks;
                try {
                    tasks = adapter.split(state.componentId, unit.getKey(), interval, unit.getValue());
                } catch (Exception e) {
                    log.error("[统一采集] 拆读取任务失败：componentId={}, family={}, unit={}",
                            state.componentId, state.family, unit.getKey(), e);
                    continue;
                }
                if (tasks == null || tasks.isEmpty()) {
                    continue;
                }
                for (ReadTask task : tasks) {
                    ScheduledFuture<?> future = PRODUCER.scheduleAtFixedRate(
                            () -> produce(task), 0, interval, TimeUnit.SECONDS);
                    state.futures.put(task.getTaskKey(), future);
                    taskCount++;
                }
            }
        }
        log.info("[统一采集] 组件计划已重建：componentId={}, family={}, 点位={}, 定时任务={}",
                state.componentId, state.family, state.points.size(), taskCount);
    }

    /**
     * 定时触发：把任务投进组件队列
     *
     * @param task 读取任务
     */
    private static void produce(ReadTask task) {
        try {
            // 消费线程没起来就不产任务（与旧链路一致：组件未开启时队列不积压）
            if (!CollectionEngine.isConsuming(task.getComponentId())) {
                return;
            }
            CollectionQueue queue = CollectionEngine.queue(task.getComponentId());
            if (queue != null) {
                queue.offer(task);
            }
        } catch (Throwable t) {
            // 这里绝不能抛出：抛出会让该任务的后续触发被永久取消
            log.error("[统一采集] 任务投递失败：componentId={}, task={}", task.getComponentId(), task.getTaskKey(), t);
        }
    }
}
