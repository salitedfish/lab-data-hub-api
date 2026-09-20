//由AI修改
package com.labdatahub.component.fanuc_focas;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.event.ComponentOnlineNotifier;
import com.labdatahub.component.utils.ParallelHealthCheck;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.ShortByReference;

import lombok.extern.slf4j.Slf4j;

/**
 * FANUC FOCAS2 连接管理器，维护多个组件的连接句柄（新增自动检查+定时重连）
 * FOCAS2 用 fwlib32 库建立连接，句柄（unsigned short）即连接，需保持有效；
 * 每 component 一把读锁，避免并发 fwlib 调用同一句柄冲突
 *
 * 由AI修改（根因修复）：FOCAS2 fwlib 连接"绑定创建线程"——cnc_allclibhndl3 在哪个线程建立连接，
 * 就只有该线程能读（其它线程读同一句柄返回 EW_BUSY -8，实测验证：addConnection 线程读=0，
 * 消费线程/健康检查线程读=-8）。故连接必须由消费线程在其本线程建立（establishConnection），
 * addConnection 只保存配置并启动消费线程，同步等待建连结果。健康检查线程不能读/建共享句柄，
 * 改为建立临时探测连接自查（建连→statinfo→释放都在健康检查线程），重连统一由消费线程自愈。
 */
@Slf4j
public class FanucFocasConnectionManager {
    // 存储设备连接句柄：key为componentId（线程安全）
    public static Map<String, Short> handleMap = new ConcurrentHashMap<>();
    // 存储每个componentId对应的连接配置（用于重连）
    public static final Map<String, FanucFocasConfig> configMap = new ConcurrentHashMap<>();
    // 每 component 的读锁（并发 fwlib 调用同一句柄会冲突，读/健康检查/重连都要持锁）
    public static final Map<String, ReentrantLock> readLockMap = new ConcurrentHashMap<>();
    // 连接健康检查调度器（全局单例，定时检查连接状态）
    private static final ScheduledExecutorService healthCheckScheduler = Executors.newScheduledThreadPool(
            1,
            r -> {
                Thread t = new Thread(r, "fanuc-focas-health-check");
                t.setDaemon(true); // 守护线程，应用退出时自动销毁
                return t;
            }
    );

    private static final int HEALTH_CHECK_INTERVAL = 10;
    // 静态初始化：启动全局连接健康检查（每10秒检查一次）
    // 首次延迟 HEALTH_CHECK_INTERVAL 秒再执行：若延迟0立即执行，会与启动期的 addConnection 并发建连，
    // 产生"健康检查建连 + addConnection建连"的双连接竞态，其中一个句柄未被释放而泄漏
    static {
        healthCheckScheduler.scheduleAtFixedRate(
                FanucFocasConnectionManager::checkAllConnections,
                HEALTH_CHECK_INTERVAL,
                HEALTH_CHECK_INTERVAL,
                TimeUnit.SECONDS
        );
        log.info("FANUC FOCAS2 连接健康检查任务已启动，检查间隔={}秒（首次延迟{}秒，避免与启动期建连竞态）", HEALTH_CHECK_INTERVAL, HEALTH_CHECK_INTERVAL);
    }

    /**
     * 创建连接（FOCAS2 无握手，cnc_allclibhndl3 返回句柄即建连成功）
     * 由AI修改：fwlib 连接绑定创建线程，连接必须由消费线程在其本线程建立（establishConnection），
     * 本方法只保存配置并启动消费线程，然后同步等待消费线程建连完成，保持"开启成功/失败"的返回语义。
     * @param componentId 组件唯一标识
     * @param config 连接配置
     * @return 首次连接是否成功
     */
    public static boolean addConnection(String componentId, FanucFocasConfig config) {
        if (componentId == null || config == null || config.getIpAddr() == null) {
            return false;
        }
        try {
            configMap.put(componentId, config);
            // 已有消费线程且句柄有效（该句柄由消费线程建立，绑定其线程）：复用现有连接，仅更新配置
            if (FanucFocasLoopConsumer.isConsuming(componentId) && handleMap.get(componentId) != null) {
                System.out.printf("[FOCAS2连接] componentId=%s 已连接且消费线程运行中，复用现有连接（仅更新配置）%n", componentId);
                return true;
            }
            // 否则：关闭旧连接，启动消费线程（线程内部先 establishConnection 建连）
            closeHandle(componentId);
            try {
                FanucFocasLoopConsumer.startConsume(componentId, SpringUtils.getBean(FanucFocasMessageConsumeService.class));
            } catch (IllegalStateException e) {
                // 已存在消费线程（但句柄缺失）：其循环顶部检测到句柄缺失会自行在本线程重建连接
                System.out.printf("[FOCAS2连接] componentId=%s 已存在消费线程，跳过重复启动，等待其重建连接%n", componentId);
            }
            // 同步等待消费线程建连完成（最多 8 秒，覆盖 cnc_allclibhndl3 的 3 秒超时）
            for (int i = 0; i < 80; i++) {
                if (handleMap.get(componentId) != null) {
                    return true;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            System.err.printf("[FOCAS2连接] componentId=%s 等待消费线程建连超时（8秒），连接可能失败%n", componentId);
            return handleMap.get(componentId) != null;
        } catch (Exception e) {
            System.err.printf("[FOCAS2连接] componentId=%s 首次连接失败：%s%n", componentId, e.getMessage());
            // 连接失败时释放已建立句柄并移除无效配置，避免空转/泄漏
            closeHandle(componentId);
            // ⚠️ 但 configMap 不能删：它是健康检查的监控名单（checkAllConnections 遍历的就是它的 keySet），
            //    删掉等于判定「不再重连」。而「首次连接失败」只说明机床此刻不在线（平台启动时机床正好没开），
            //    不代表组件不该被监控 —— 删掉之后机床开机了也永远接不回去，只能由管理员在页面上重新点开启。
            //    配置留在 map 里，健康检查每 10 秒会自动重试（组件被真正关闭时走 closeConnection，那里才清 configMap）
            readLockMap.remove(componentId);
            // 首次连接失败视为组件离线，通知设备下线（已离线设备幂等跳过）
            ComponentOnlineNotifier.markOfflineAndNotify(componentId);
            return false;
        }
    }

    /**
     * 由AI修改：在调用线程上建立 FOCAS2 连接并存入句柄。
     * fwlib 连接绑定创建线程，本方法必须由消费线程在其本线程调用（循环顶部、重连自愈场景）。
     * 已有旧句柄时先关闭（旧句柄也是本线程建立的，关闭安全）。
     * @param componentId 组件唯一标识
     */
    public static void establishConnection(String componentId) {
        FanucFocasConfig config = configMap.get(componentId);
        if (config == null) {
            return;
        }
        ReentrantLock lock = getReadLock(componentId);
        lock.lock();
        try {
            // 已有句柄（重连/重开场景）：先关闭旧句柄，避免泄漏
            closeHandle(componentId);
            Fwlib32 lib = Fwlib32Loader.get(config.getLibPath());
            ShortByReference handleRef = new ShortByReference();
            short ret = lib.cnc_allclibhndl3(config.getIpAddr(), config.getPort().shortValue(),
                    new NativeLong(toTimeoutSeconds(config.getTimeout())), handleRef);
            if (ret != Fwlib32.EW_OK) {
                System.err.printf("[FOCAS2连接] componentId=%s 建连失败（返回码=%d），请确认机床 FOCAS2 选项已开启（端口8193）%n",
                        componentId, ret);
                handleMap.remove(componentId);
                // 建连失败视为组件离线，通知设备下线（节流，仅在在线→离线转变时发一次）
                ComponentOnlineNotifier.markOfflineAndNotify(componentId);
                return;
            }
            handleMap.put(componentId, handleRef.getValue());
            System.out.printf("[FOCAS2连接] componentId=%s 连接成功（%s:%d, 句柄=%d, 线程=%s）%n",
                    componentId, config.getIpAddr(), config.getPort(), handleRef.getValue(), Thread.currentThread().getName());
            // 连接成功，清除离线节流标记
            ComponentOnlineNotifier.markOnline(componentId);
        } catch (Exception e) {
            System.err.printf("[FOCAS2连接] componentId=%s 建连失败：%s%n", componentId, e.getMessage());
            closeHandle(componentId);
            ComponentOnlineNotifier.markOfflineAndNotify(componentId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 关闭单个组件连接（兼容原有逻辑，新增配置清理）
     */
    public static void closeConnection(String componentId) {
        // 1. 关闭句柄
        closeHandle(componentId);
        // 2. 清理配置（停止该组件的重连检查）
        configMap.remove(componentId);
        readLockMap.remove(componentId);
        // 3. 清理消息队列和消费线程
        FanucFocasMessageScheduler.removeMessageQueue(componentId);
        FanucFocasLoopConsumer.stopConsume(componentId);
        System.out.printf("[FOCAS2连接] componentId=%s 连接已关闭，配置已清理%n", componentId);
    }

    /**
     * 关闭所有连接（兼容原有逻辑，新增调度器停止+配置清理）
     */
    public static void closeAllConnections() {
        // 1. 关闭所有句柄
        for (String componentId : handleMap.keySet()) {
            closeHandle(componentId);
        }
        // 2. 清理所有映射
        handleMap.clear();
        configMap.clear();
        readLockMap.clear();
        // 3. 停止健康检查调度器（避免资源泄漏）
        healthCheckScheduler.shutdown();
        try {
            if (!healthCheckScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                healthCheckScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            healthCheckScheduler.shutdownNow();
        }
        System.out.println("[FOCAS2连接] 所有连接已关闭，健康检查调度器已停止");
    }

    // ========== 核心新增：连接健康检查与重连逻辑 ==========
    /**
     * 检查所有连接状态，失效则自动重连
     */
    private static void checkAllConnections() {
        // 并发检查：原先单线程串行遍历，设备离线时每台都要等满一次建连超时，
        // 100 台同协议设备同时离线要几分钟才轮完一遍，断线/恢复感知随之失效
        // （详见 ParallelHealthCheck 的类注释）
        ParallelHealthCheck.run("FANUC", configMap.keySet(), FanucFocasConnectionManager::checkAndReconnect, HEALTH_CHECK_INTERVAL);
    }

    /**
     * 检查单个componentId的连接状态，失效则标记离线（由消费线程自愈重连）。
     * 由AI修改：fwlib 连接绑定创建线程，健康检查线程不能读消费线程建立的共享句柄（跨线程读返回EW_BUSY -8），
     * 也不能在健康检查线程重连（否则新句柄绑定健康检查线程，消费线程永远读不了）。
     * 故本方法改为在本线程建立临时探测连接：建连→statinfo→释放（都在健康检查线程），
     * 只用于判断机床可达性并刷新在线/离线状态；真正的重连统一由消费线程自愈
     * （读失败触发 forceReconnect / 句柄缺失时 establishConnection，均在消费线程执行）。
     * @param componentId 组件唯一标识
     */
    private static void checkAndReconnect(String componentId) {
        FanucFocasConfig config = configMap.get(componentId);
        if (config == null) {
            return;
        }
        ReentrantLock lock = getReadLock(componentId);
        lock.lock();
        try {
            Fwlib32 lib = Fwlib32Loader.get(config.getLibPath());
            ShortByReference probeHandleRef = new ShortByReference();
            short ret = lib.cnc_allclibhndl3(config.getIpAddr(), config.getPort().shortValue(),
                    new NativeLong(toTimeoutSeconds(config.getTimeout())), probeHandleRef);
            if (ret != Fwlib32.EW_OK) {
                // 探测建连失败：机床不可达，标记离线（消费线程会在下次读取时自愈重连）
                System.out.printf("[FOCAS2重连] componentId=%s 探测建连失败（返回码=%d），机床可能不可达，标记离线（由消费线程自愈）%n",
                        componentId, ret);
                ComponentOnlineNotifier.markOfflineAndNotify(componentId);
                return;
            }
            // 探测连接建立成功：读一次状态确认机床可用，随后立即释放（都在本线程，符合线程绑定）
            short sret = lib.cnc_statinfo(probeHandleRef.getValue(), new Fwlib32.ODBST());
            lib.cnc_freelibhndl(probeHandleRef.getValue());
            if (sret == Fwlib32.EW_OK || sret == Fwlib32.EW_BUSY) {
                // 机床可达（EW_BUSY 表示机床忙但连接有效，不视为故障）
                ComponentOnlineNotifier.markOnline(componentId);
            } else {
                System.out.printf("[FOCAS2重连] componentId=%s 探测 statinfo 异常（返回码=%d），机床可能不可达，标记离线（由消费线程自愈）%n",
                        componentId, sret);
                ComponentOnlineNotifier.markOfflineAndNotify(componentId);
            }
        } catch (Exception e) {
            System.err.printf("[FOCAS2重连] componentId=%s 健康检查异常：%s%n", componentId, e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 主动强制重连（读取失败时由读取器触发，在消费线程执行）：关闭死句柄并建立新句柄
     * @param componentId 组件唯一标识
     */
    public static void forceReconnect(String componentId) {
        FanucFocasConfig config = configMap.get(componentId);
        if (config == null) {
            // 无连接配置时仅清理死句柄，避免残留悬挂
            closeHandle(componentId);
            return;
        }
        ReentrantLock lock = getReadLock(componentId);
        lock.lock();
        try {
            establishConnection(componentId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取指定组件的读锁（不存在时创建）
     * @param componentId 组件唯一标识
     * @return 该组件的读锁
     */
    public static ReentrantLock getReadLock(String componentId) {
        return readLockMap.computeIfAbsent(componentId, k -> new ReentrantLock());
    }

    /**
     * 关闭并移除指定组件的句柄
     */
    private static void closeHandle(String componentId) {
        Short handle = handleMap.remove(componentId);
        if (handle != null) {
            try {
                Fwlib32 lib = Fwlib32Loader.get();
                // FOCAS2 断开连接用 cnc_freelibhndl（fwlib 无 cnc_close 函数）
                lib.cnc_freelibhndl(handle);
            } catch (Exception e) {
                // 句柄关闭失败不影响后续
                System.err.printf("[FOCAS2连接] componentId=%s 句柄关闭失败：%s%n", componentId, e.getMessage());
            }
        }
    }

    /**
     * 由AI修改：FOCAS2 cnc_allclibhndl3 的 timeout 参数单位为秒（官方 Fwlib64.h 注释），
     * 而前端 otherConfig 的 timeout 按毫秒填写（默认3000），需换算成秒（最小1秒）。
     * 此前直接传 3000 会被 fwlib 当作 3000 秒，机床不响应时建连挂起约50分钟，导致开启接口超时。
     */
    private static int toTimeoutSeconds(Integer timeoutMs) {
        if (timeoutMs == null || timeoutMs <= 0) {
            return 3;
        }
        return Math.max(1, timeoutMs / 1000);
    }
}
