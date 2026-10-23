package com.labdatahub.business.scheduled;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.labdatahub.business.domain.LabdatahubComponent;
import com.labdatahub.business.service.ILabdatahubComponentService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.common.exception.CommonWarnException;

import lombok.extern.slf4j.Slf4j;

/**
 * 网络组件启动自愈。
 *
 * <p>背景：设备上电往往比平台慢，后端重启时第一次连接被拒，组件就被 {@code openComponentSafely}
 * 置为关闭且再无人重试；而连接管理器自带的健康检查只负责「已开起来的组件重连」，失败发生在
 * {@code PROTOCOL_MAP} 注册与消费线程建立之前，健康检查救不回来。
 *
 * <p>行为：启动时对库里 {@code status=1} 的组件做快照并首轮并发开启；之后每隔
 * {@code interval-seconds}（默认 60）秒对仍失败的组件顺序重试，直到全部开启或窗口
 * {@code window-seconds}（默认 600）秒结束。窗口结束仍未开启的组件保持停用（页面显示停用），
 * 只记一条放弃日志。
 *
 * <p>手动关闭：{@code LabdatahubComponentServiceImpl#closeComponent} 通过静态
 * {@link #onManualClose(String)} 通知本类，立即取消对该组件的重试（静态方法避免循环依赖）。
 */
@Slf4j
@Component
public class ComponentStartupRetryer {

    /** 组件在本类中的阶段 */
    private enum State {
        /** 已纳入快照、尚未开始尝试 */
        PENDING,
        /** 正在开启（单飞：同一组件同一时刻只允许一个线程调 openComponent） */
        IN_FLIGHT,
        /** 已开启 */
        OPEN,
        /** 开启失败，等待下一轮重试 */
        FAILED,
        /** 用户手动关闭，取消重试 */
        CANCELLED
    }

    /** 各组件阶段。静态是为了让 closeComponent 无需注入本类即可取消重试 */
    private static final ConcurrentMap<String, State> STATES = new ConcurrentHashMap<>();

    @Autowired
    private ILabdatahubComponentService labdatahubComponentService;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /** 是否启用启动自愈（关闭后仍保留启动时的首轮开启，只是不再重试） */
    @Value("${labdatahub.component.startup-retry.enabled:true}")
    private boolean enabled;

    /** 重试间隔（秒） */
    @Value("${labdatahub.component.startup-retry.interval-seconds:60}")
    private int intervalSeconds;

    /** 重试窗口（秒）：从启动首轮开始计时，超过后不再重试 */
    @Value("${labdatahub.component.startup-retry.window-seconds:600}")
    private int windowSeconds;

    /** 启动快照：componentId → 组件行（构建后只读） */
    private final Map<String, LabdatahubComponent> snapshot = new LinkedHashMap<>();

    private final AtomicBoolean started = new AtomicBoolean(false);

    private final AtomicInteger round = new AtomicInteger(0);

    private volatile long startTime;

    private volatile boolean finished;

    private ScheduledExecutorService scheduler;

    /**
     * 启动：记录快照、首轮并发开启、并按配置启动重试轮次。
     * 由 {@code TimerTask#initComponentAndProtocol} 在协议注册完成后调用。
     *
     * @param components 启动时库中 {@code status=1} 的组件
     */
    public void start(List<LabdatahubComponent> components) {
        if (components == null || components.isEmpty()) {
            return;
        }
        if (!started.compareAndSet(false, true)) {
            log.warn("[组件自愈] 重复调用 start()，已忽略");
            return;
        }
        startTime = System.currentTimeMillis();
        for (LabdatahubComponent component : components) {
            snapshot.put(component.getId(), component);
            STATES.put(component.getId(), State.PENDING);
        }
        // 首轮与原来的启动行为一致：并发开启（FANUC 的 addConnection 会同步等待消费线程建连）
        for (LabdatahubComponent component : components) {
            threadPoolTaskExecutor.execute(() -> attempt(component, false));
        }
        if (!enabled) {
            log.info("[组件自愈] 未启用（labdatahub.component.startup-retry.enabled=false），启动失败的组件不会自动重试");
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "component-startup-retry");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(this::retryRound, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        log.info("[组件自愈] 已启动：共 {} 个组件，每 {} 秒重试一次，窗口 {} 秒",
                components.size(), intervalSeconds, windowSeconds);
    }

    /**
     * 开启单个组件（单飞）。
     *
     * @param component 组件
     * @param quiet     是否为重试轮次：重试轮次的失败只打 DEBUG，避免多轮刷屏
     * @return 是否已开启成功
     */
    private boolean attempt(LabdatahubComponent component, boolean quiet) {
        String componentId = component.getId();
        AtomicBoolean acquired = new AtomicBoolean(false);
        STATES.compute(componentId, (key, state) -> {
            if (state == State.IN_FLIGHT || state == State.OPEN || state == State.CANCELLED) {
                return state;
            }
            acquired.set(true);
            return State.IN_FLIGHT;
        });
        if (!acquired.get()) {
            return false;
        }
        try {
            boolean isOk = labdatahubComponentService.openComponent(componentId);
            if (!isOk) {
                // openComponent 在 otherConfig 为空等分支只返回 false 不抛异常，必须当作失败处理，
                // 否则组件会停在「库里 status=1、实际没跑」的半开状态
                markFailed(component, "openComponent 返回 false（配置为空或该网络类型不支持）", null, quiet);
                return false;
            }
            // 开启期间用户手动关闭了它：按用户意图再关一次
            State after = STATES.compute(componentId,
                    (key, state) -> state == State.CANCELLED ? State.CANCELLED : State.OPEN);
            if (after == State.CANCELLED) {
                log.info("[组件自愈] componentId={}（{}）开启成功，但期间已被手动关闭，按手动关闭处理",
                        componentId, component.getName());
                try {
                    labdatahubComponentService.closeComponent(componentId);
                } catch (Exception closeException) {
                    log.warn("[组件自愈] componentId={} 补偿关闭失败：{}", componentId, closeException.getMessage());
                }
                return false;
            }
            return true;
        } catch (Exception e) {
            markFailed(component, e.getMessage(), e, quiet);
            return false;
        }
    }

    /**
     * 记录失败：状态置 FAILED、库与缓存置停用、按轮次决定日志级别。
     */
    private void markFailed(LabdatahubComponent component, String message, Exception e, boolean quiet) {
        // 开启过程中被手动关闭：不能把 CANCELLED 覆盖回 FAILED，否则下一轮又会把它拉起来
        AtomicBoolean cancelled = new AtomicBoolean(false);
        STATES.compute(component.getId(), (key, state) -> {
            if (state == State.CANCELLED) {
                cancelled.set(true);
                return state;
            }
            return State.FAILED;
        });
        if (cancelled.get()) {
            log.debug("[组件自愈] componentId={}（{}）本次尝试失败，但期间已被手动关闭，不再重试：{}",
                    component.getId(), component.getName(), message);
            return;
        }
        try {
            LabdatahubComponent closed = new LabdatahubComponent();
            closed.setId(component.getId());
            closed.setStatus("0");
            labdatahubComponentService.updateById(closed);
            // 组件缓存里这一行的 status 同步置 0（启动时注册协议那段已把缓存写成 1）
            LabdatahubComponent cached = CacheUtils.getComponentCache(component.getId());
            if (cached != null) {
                cached.setStatus("0");
            }
        } catch (Exception updateException) {
            log.error("[组件自愈] componentId={} 失败状态落库异常：{}", component.getId(), updateException.getMessage());
        }
        String detail = message == null ? "未知原因" : message;
        if (quiet) {
            log.debug("[组件自愈] componentId={}（{}）重试仍失败：{}", component.getId(), component.getName(), detail);
        } else if (e instanceof CommonWarnException) {
            log.warn("[组件自愈] componentId={}（{}）开启失败：{}", component.getId(), component.getName(), detail);
        } else {
            log.error("[组件自愈] componentId={}（{}）开启失败，将纳入重试：{}",
                    component.getId(), component.getName(), detail, e);
        }
    }

    /**
     * 一轮重试：只处理 FAILED 的组件，顺序执行，全部成功或到窗口即结束。
     */
    private void retryRound() {
        if (finished) {
            return;
        }
        try {
            List<LabdatahubComponent> failed = snapshot.values().stream()
                    .filter(component -> STATES.get(component.getId()) == State.FAILED)
                    .collect(Collectors.toList());
            if (failed.isEmpty()) {
                finish("全部组件已开启");
                return;
            }
            long elapsedMillis = System.currentTimeMillis() - startTime;
            if (elapsedMillis >= windowSeconds * 1000L) {
                giveUp(failed);
                return;
            }
            int roundNo = round.incrementAndGet();
            int succeeded = 0;
            for (LabdatahubComponent component : failed) {
                if (System.currentTimeMillis() - startTime >= windowSeconds * 1000L) {
                    break;
                }
                if (attempt(component, true)) {
                    succeeded++;
                }
            }
            List<String> stillFailed = new ArrayList<>();
            for (LabdatahubComponent component : failed) {
                if (STATES.get(component.getId()) == State.FAILED) {
                    stillFailed.add(component.getId());
                }
            }
            log.info("[组件自愈] 第{}轮（启动后{}s）：尝试{}个，成功{}个，仍失败{}个{}",
                    roundNo, elapsedMillis / 1000, failed.size(), succeeded, stillFailed.size(),
                    stillFailed.isEmpty() ? "" : "（componentId=" + String.join(",", stillFailed) + "）");
            if (stillFailed.isEmpty()) {
                finish("全部组件已开启");
            }
        } catch (Exception e) {
            // 单轮异常不能让重试线程退出
            log.error("[组件自愈] 重试轮次异常，继续下一轮", e);
        }
    }

    /**
     * 窗口结束：保持失败组件停用，记一条汇总后退出。
     */
    private void giveUp(List<LabdatahubComponent> failed) {
        String detail = failed.stream()
                .map(component -> component.getId() + "（" + component.getName() + "）")
                .collect(Collectors.joining("、"));
        log.warn("[组件自愈] 窗口 {} 秒已到，放弃重试，仍停用 {} 个：{}", windowSeconds, failed.size(), detail);
        finish(null);
    }

    /**
     * 结束重试：停掉调度线程（重复调用无副作用）。
     */
    private void finish(String reason) {
        if (finished) {
            return;
        }
        finished = true;
        if (reason != null) {
            log.info("[组件自愈] 结束：{}", reason);
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    /**
     * 用户手动关闭组件时调用：立即取消对该组件的重试。
     *
     * <p>静态方法，供 {@code LabdatahubComponentServiceImpl#closeComponent} 直接调用，
     * 避免本类与组件服务互相注入。不在启动快照里的组件调用本方法无副作用。
     *
     * @param componentId 组件主键
     */
    public static void onManualClose(String componentId) {
        if (componentId == null) {
            return;
        }
        AtomicBoolean cancelled = new AtomicBoolean(false);
        STATES.computeIfPresent(componentId, (key, state) -> {
            if (state == State.FAILED || state == State.IN_FLIGHT || state == State.PENDING) {
                cancelled.set(true);
                return State.CANCELLED;
            }
            return state;
        });
        if (cancelled.get()) {
            log.info("[组件自愈] componentId={} 被手动关闭，已取消其启动重试", componentId);
        }
    }

    @PreDestroy
    public void destroy() {
        finished = true;
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
