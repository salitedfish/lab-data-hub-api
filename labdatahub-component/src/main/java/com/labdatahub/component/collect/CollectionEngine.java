package com.labdatahub.component.collect;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一采集引擎门面（所有协议共用一套调度/队列/消费/上报）
 *
 * <p>旧链路是「每个协议一套 Scheduler + Queue + LoopConsumer + ConsumeService」，
 * 八个协议八份近似的管道，每个点位一条定时任务、每条消息 sleep(delayTime) 后才发下一条。
 * 本引擎把它收敛成三层：
 * <ol>
 *   <li>点位 → 按「采集单元 + 间隔」分组；</li>
 *   <li>每个分组交给协议适配器拆成读取任务（一次读取覆盖多个点位）；</li>
 *   <li>每组件一条消费线程：取任务 → 协议读取 → 逐点上报（不再有固定延迟）。</li>
 * </ol>
 *
 * <p>各协议旧 Scheduler/LoopConsumer 的静态方法在开启统一引擎时直接委托到这里，
 * 因此 139 处 addReadConfig/removeReadConfig 调用点、各连接管理器的启停调用点都不用改。
 */
@Slf4j
public final class CollectionEngine {

    /** 组件采集状态：key = 组件ID */
    private static final Map<String, ComponentState> STATES = new ConcurrentHashMap<>();

    /** 组件任务队列：key = 组件ID */
    private static final Map<String, CollectionQueue> QUEUES = new ConcurrentHashMap<>();

    /** 待重建的组件（配置变动后合并成一次重建，避免启动期几百个点位逐个重建） */
    private static final Set<String> REBUILD_PENDING = ConcurrentHashMap.newKeySet();

    /** 重建线程（单线程：重建只是取消/重排定时任务，很快） */
    private static final ScheduledExecutorService REBUILDER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "collect-rebuilder");
        t.setDaemon(true);
        return t;
    });

    /** 配置变动后延迟多久重建（毫秒）：TimerTask/控制器会连续增删点位，合并成一次 */
    private static final long REBUILD_DELAY_MS = 150L;

    private CollectionEngine() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 注册一个点位（等价旧链路的 addReadConfig）
     *
     * @param componentId 组件ID
     * @param family 协议族
     * @param point 点位
     */
    public static void addPoint(String componentId, String family, CollectionPoint point) {
        if (StringUtils.isBlank(componentId) || point == null) {
            return;
        }
        ComponentState state = STATES.computeIfAbsent(componentId, id -> new ComponentState(id, family));
        state.points.put(point.key(), point);
        QUEUES.computeIfAbsent(componentId, id -> new CollectionQueue(CollectionFamily.queueCapacity()));
        scheduleRebuild(componentId);
    }

    /**
     * 移除点位（等价旧链路的 removeReadConfig）
     *
     * <p>code 为空表示移除该设备的全部点位：数据库采集按设备整体调度（旧链路一个设备一条任务，
     * 不区分点位编码），调用方传 null 时不能只删一个 key。
     *
     * @param componentId 组件ID
     * @param deviceSn 设备SN
     * @param code 点位编码；为空表示移除该设备的全部点位
     */
    public static void removePoint(String componentId, String deviceSn, String code) {
        if (StringUtils.isBlank(componentId) || StringUtils.isBlank(deviceSn)) {
            return;
        }
        ComponentState state = STATES.get(componentId);
        if (state == null) {
            return;
        }
        if (StringUtils.isBlank(code)) {
            String prefix = deviceSn + "#";
            state.points.keySet().removeIf(key -> key.startsWith(prefix));
        } else {
            state.points.remove(deviceSn + "#" + code);
        }
        scheduleRebuild(componentId);
    }

    /**
     * 点位是否已注册（等价旧链路的 isReadConfigRunning）
     *
     * @param componentId 组件ID
     * @param deviceSn 设备SN
     * @param code 点位编码
     * @return true=已注册
     */
    public static boolean hasPoint(String componentId, String deviceSn, String code) {
        ComponentState state = STATES.get(componentId);
        return state != null && state.points.containsKey(deviceSn + "#" + code);
    }

    /**
     * 移除组件的全部调度与队列（等价旧链路的 removeMessageQueue）
     *
     * @param componentId 组件ID
     */
    public static void removeComponent(String componentId) {
        if (StringUtils.isBlank(componentId)) {
            return;
        }
        ComponentState state = STATES.remove(componentId);
        if (state != null) {
            state.cancelAll();
        }
        CollectionQueue queue = QUEUES.remove(componentId);
        if (queue != null) {
            queue.clear();
        }
        log.info("[统一采集] 已移除组件调度与队列：componentId={}", componentId);
    }

    /**
     * 启动组件消费线程（等价旧链路的 startConsume）
     *
     * @param componentId 组件ID
     * @param family 协议族
     */
    public static void startConsumer(String componentId, String family) {
        if (StringUtils.isBlank(componentId)) {
            throw new IllegalArgumentException("componentId 不能为空");
        }
        ComponentState state = STATES.computeIfAbsent(componentId, id -> new ComponentState(id, family));
        QUEUES.computeIfAbsent(componentId, id -> new CollectionQueue(CollectionFamily.queueCapacity()));
        CollectionLoopConsumer.start(componentId, state, CollectionRegistry.get(state.family));
    }

    /**
     * 停止组件消费线程（等价旧链路的 stopConsume）
     *
     * @param componentId 组件ID
     */
    public static void stopConsumer(String componentId) {
        CollectionLoopConsumer.stop(componentId);
    }

    /**
     * 组件是否正在消费
     *
     * @param componentId 组件ID
     * @return true=消费线程存活
     */
    public static boolean isConsuming(String componentId) {
        return CollectionLoopConsumer.isConsuming(componentId);
    }

    /**
     * 组件队列里的任务数
     *
     * @param componentId 组件ID
     * @return 任务数
     */
    public static int queueSize(String componentId) {
        CollectionQueue queue = QUEUES.get(componentId);
        return queue == null ? 0 : queue.size();
    }

    /**
     * 关闭指定协议族的全部调度与消费线程（应用关闭时调用）
     *
     * @param family 协议族
     */
    public static void shutdownFamily(String family) {
        for (ComponentState state : STATES.values()) {
            if (family.equals(state.family)) {
                state.cancelAll();
                CollectionLoopConsumer.stop(state.componentId);
            }
        }
    }

    /**
     * 取组件状态（调度器重建用）
     *
     * @param componentId 组件ID
     * @return 状态；不存在返回 null
     */
    static ComponentState state(String componentId) {
        return STATES.get(componentId);
    }

    /**
     * 取组件队列（消费线程用）
     *
     * @param componentId 组件ID
     * @return 队列；不存在返回 null
     */
    static CollectionQueue queue(String componentId) {
        return QUEUES.get(componentId);
    }

    /**
     * 安排一次延迟重建（同一组件多次变动只重建一次）
     *
     * @param componentId 组件ID
     */
    private static void scheduleRebuild(String componentId) {
        if (!REBUILD_PENDING.add(componentId)) {
            return;
        }
        REBUILDER.schedule(() -> {
            REBUILD_PENDING.remove(componentId);
            try {
                CollectionScheduler.rebuild(state(componentId));
            } catch (Throwable t) {
                log.error("[统一采集] 重建组件采集计划失败：componentId={}", componentId, t);
            }
        }, REBUILD_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 单个组件的采集状态
     */
    static final class ComponentState {

        /** 组件ID */
        final String componentId;

        /** 协议族（一个组件只属于一个协议族） */
        final String family;

        /** 已注册点位：key = 设备SN#点位编码 */
        final Map<String, CollectionPoint> points = new ConcurrentHashMap<>();

        /** 已排的定时任务：key = 任务键 */
        final Map<String, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

        ComponentState(String componentId, String family) {
            this.componentId = componentId;
            this.family = family;
        }

        /**
         * 取消本组件的全部定时任务
         */
        void cancelAll() {
            for (ScheduledFuture<?> future : futures.values()) {
                future.cancel(false);
            }
            futures.clear();
        }
    }
}
