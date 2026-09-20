//由AI修改
package com.labdatahub.component.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;

/**
 * 各协议连接健康检查的<b>并发执行器</b>。
 *
 * <p>原先每个协议的 {@code checkAllConnections()} 都是一句
 * <pre>
 * for (String componentId : configMap.keySet()) {
 *     checkAndReconnect(componentId);
 * }
 * </pre>
 * 在<b>单个</b>线程上串行遍历所有组件。设备在线时单台检查只要几十微秒（本地 socket 状态判断），
 * 串行无所谓；但设备<b>离线</b>时每台都要走一次建连尝试、等满一次连接超时 ——
 * 本机 S7 探活超时 1.5 秒，100 台同协议设备同时离线（车间断电、交换机重启）就是
 * <b>150 秒才轮完一遍</b>，而健康检查的调度周期只有 10 秒。
 *
 * <p>{@code scheduleAtFixedRate} 的语义是「上一轮没跑完就立刻补跑下一轮」，于是那一根线程被彻底占满、
 * 任务持续追赶，结果是：<b>设备断线后要等几分钟才被标记离线，设备恢复了也要等几分钟才被重连</b> ——
 * 断线重连与离线判定这两个最基本的能力，在设备规模上来之后直接失效。
 *
 * <p>所以把「遍历」这一步并发化：固定线程池分发，单台失败不影响其他台，
 * 并用 {@link #inflight} 在途计数保证<b>同一时刻最多只有一轮任务在排队</b>（跑不完就跳过本轮，不堆积任务）。
 *
 * @author labdatahub
 */
@Slf4j
public final class ParallelHealthCheck {

    /** 各协议的默认检查并发度：100 台设备同时离线时，一轮 100×1.5s/16 ≈ 10 秒，能追平 10 秒的调度周期 */
    private static final int DEFAULT_THREADS = 16;

    /** 按协议名缓存的执行器实例：各协议只需调一次静态 {@link #run}，无需自己维护字段 */
    private static final Map<String, ParallelHealthCheck> INSTANCES = new ConcurrentHashMap<>();

    /**
     * 并发检查一批组件（各协议 {@code checkAllConnections()} 的直接替代）。
     *
     * @param protocolName   协议名（如 "S7"），同时作为执行器实例的缓存键，需保持稳定
     * @param componentIds   待检查的组件ID集合
     * @param checker        单个组件的检查动作，通常是各协议的 {@code checkAndReconnect}
     * @param timeoutSeconds 本轮整体等待上限，一般传健康检查周期
     */
    public static void run(String protocolName, Collection<String> componentIds,
                           Consumer<String> checker, long timeoutSeconds) {
        INSTANCES.computeIfAbsent(protocolName, k -> new ParallelHealthCheck(k, DEFAULT_THREADS))
                .doRun(componentIds, checker, timeoutSeconds);
    }

    /** 协议名，用于日志与线程命名 */
    private final String protocolName;

    /** 健康检查工作线程池（守护线程，不阻碍应用退出） */
    private final ExecutorService pool;

    /**
     * 在途待检查的组件数：上一轮派发出去、尚未跑完的任务数。
     * <p>用它而不是「本轮是否在跑」的布尔标志 —— 布尔标志只能在整轮等满超时后清零，
     * 那时仍有任务在后台跑，下一轮照样往里塞；一旦「提交速度 &gt; 完成速度」
     * （100 台设备 × 3 秒建连超时 / 16 线程 = 5.3 台/秒，而调度是 10 台/秒提交），
     * 线程池队列就会无限增长。用计数守卫则<b>任何时刻最多只有一轮任务在排队</b>。
     */
    private final AtomicInteger inflight = new AtomicInteger(0);

    /** 本轮任务派发时刻（毫秒），配合 {@link #inflight} 判断上一轮是否卡死 */
    private final AtomicLong roundStartMs = new AtomicLong(0);

    /** 因上一轮未结束而跳过本轮的累计次数（用于日志，说明并发度仍然不够） */
    private final AtomicLong skippedRounds = new AtomicLong(0);

    /**
     * @param protocolName 协议名（如 "S7"），只用于日志和线程命名
     * @param threads      并发检查线程数，建议 16：100 台设备同时离线时一轮约 10 秒，能追平 10 秒的调度周期
     */
    public ParallelHealthCheck(String protocolName, int threads) {
        this.protocolName = protocolName;
        int n = threads > 0 ? threads : 16;
        this.pool = Executors.newFixedThreadPool(n, r -> {
            Thread t = new Thread(r, protocolName + "-health-check-worker");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 并发检查一批组件。
     *
     * <p>调用方通常是各协议的 {@code checkAllConnections()}，由 {@code scheduleAtFixedRate} 周期触发。
     *
     * @param componentIds   待检查的组件ID集合（方法内会先做快照，允许传入活视图 like {@code map.keySet()}）
     * @param checker        单个组件的检查动作，通常是各协议的 {@code checkAndReconnect}
     * @param timeoutSeconds 本轮整体等待上限，一般传健康检查周期；超时不中断子任务，只记一条告警
     */
    private void doRun(Collection<String> componentIds, Consumer<String> checker, long timeoutSeconds) {
        if (componentIds == null || componentIds.isEmpty() || checker == null) {
            return;
        }
        // 先快照：configMap 是并发的，边遍历边提交时若中途增删会看到不一致的视图
        List<String> ids = new ArrayList<>(componentIds);

        int pending = inflight.get();
        if (pending > 0) {
            long elapsedMs = System.currentTimeMillis() - roundStartMs.get();
            // 安全阀：正常情况每个任务都被建连超时兜住（秒级），一轮最多十几秒。
            // 万一某个 checkAndReconnect 卡在不可中断的调用上，死等下去等于健康检查永久哑掉，
            // 所以超过 3 倍调度周期就放行新一轮 —— 宁可多几个任务，也不能让断线感知停摆。
            if (elapsedMs < timeoutSeconds * 3000L) {
                long n = skippedRounds.incrementAndGet();
                log.warn("[{}健康检查] 上一轮尚有 {} 台未检查完（已历时 {} ms），跳过本轮（累计跳过 {} 轮）。" +
                        "若该告警持续出现，说明设备规模已超出检查并发度，需调大线程数",
                        protocolName, pending, elapsedMs, n);
                return;
            }
            log.error("[{}健康检查] 上一轮已历时 {} ms 仍未结束（剩余 {} 台），疑似卡死，本轮强制放行",
                    protocolName, elapsedMs, pending);
        }

        roundStartMs.set(System.currentTimeMillis());
        for (String componentId : ids) {
            inflight.incrementAndGet();
            try {
                pool.execute(() -> {
                    try {
                        checker.accept(componentId);
                    } catch (Exception e) {
                        // 单台检查失败不能影响其他台，更不能让计数永远减不到 0
                        log.error("[{}健康检查] componentId={} 检查异常：{}", protocolName, componentId, e.getMessage(), e);
                    } finally {
                        inflight.decrementAndGet();
                    }
                });
            } catch (RejectedExecutionException ree) {
                // 线程池已关闭（组件全关/应用退出）：把预加的计数还回去，否则守卫会一直以为有任务在跑
                inflight.decrementAndGet();
                log.warn("[{}健康检查] componentId={} 的检查任务被拒绝（线程池已关闭）", protocolName, componentId);
            }
        }
    }

    /** 主动关闭线程池（组件全关/应用退出时调用） */
    public void shutdown() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
