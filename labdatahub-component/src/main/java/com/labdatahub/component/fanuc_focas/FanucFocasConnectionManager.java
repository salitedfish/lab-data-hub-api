//由AI修改
package com.labdatahub.component.fanuc_focas;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.labdatahub.common.utils.spring.SpringUtils;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.ShortByReference;

import lombok.extern.slf4j.Slf4j;

/**
 * FANUC FOCAS2 连接管理器，维护多个组件的连接句柄（新增自动检查+定时重连）
 * FOCAS2 用 fwlib32 库建立连接，句柄（unsigned short）即连接，需保持有效；
 * 每 component 一把读锁，避免并发 fwlib 调用同一句柄冲突
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
    static {
        healthCheckScheduler.scheduleAtFixedRate(
                FanucFocasConnectionManager::checkAllConnections,
                0,
                HEALTH_CHECK_INTERVAL,
                TimeUnit.SECONDS
        );
        log.info("FANUC FOCAS2 连接健康检查任务已启动，检查间隔={}秒", HEALTH_CHECK_INTERVAL);
    }

    /**
     * 创建连接（FOCAS2 无握手，cnc_allclibhndl3 返回句柄即建连成功）
     * @param componentId 组件唯一标识
     * @param config 连接配置
     * @return 首次连接是否成功
     */
    public static boolean addConnection(String componentId, FanucFocasConfig config) {
        try {
            // 1. 校验参数
            if (componentId == null || config == null || config.getIpAddr() == null) {
                return false;
            }
            configMap.put(componentId, config);
            // 2. 先关闭旧连接（避免资源泄漏）
            closeHandle(componentId);
            // 3. 加载 fwlib32 库并建立连接
            Fwlib32 lib = Fwlib32Loader.get(config.getLibPath());
            ShortByReference handleRef = new ShortByReference();
            short ret = lib.cnc_allclibhndl3(config.getIpAddr(), config.getPort().shortValue(),
                    new NativeLong(config.getTimeout() == null ? 3000 : config.getTimeout()), handleRef);
            if (ret != Fwlib32.EW_OK) {
                System.err.printf("[FOCAS2连接] componentId=%s 建连失败（返回码=%d），请确认机床 FOCAS2 选项已开启（端口8193）%n",
                        componentId, ret);
                configMap.remove(componentId);
                return false;
            }
            // 4. 存储句柄与读锁
            handleMap.put(componentId, handleRef.getValue());
            readLockMap.put(componentId, new ReentrantLock());
            System.out.printf("[FOCAS2连接] componentId=%s 首次连接成功（%s:%d, 句柄=%d）%n",
                    componentId, config.getIpAddr(), config.getPort(), handleRef.getValue());
            FanucFocasLoopConsumer.startConsume(componentId, SpringUtils.getBean(FanucFocasMessageConsumeService.class));
            return true;
        } catch (Exception e) {
            System.err.printf("[FOCAS2连接] componentId=%s 首次连接失败：%s%n", componentId, e.getMessage());
            // 连接失败时移除无效配置/句柄，避免空转
            handleMap.remove(componentId);
            configMap.remove(componentId);
            readLockMap.remove(componentId);
            return false;
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
        // 遍历所有已配置的componentId（避免遗漏待重连的组件）
        for (String componentId : configMap.keySet()) {
            checkAndReconnect(componentId);
        }
    }

    /**
     * 检查单个componentId的连接状态，失效则重连
     * FOCAS2 无本地 socket 可查，用 cnc_statinfo 探测句柄有效性（EW_OK=存活，其它返回码=失效）
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
            Short handle = handleMap.get(componentId);
            if (handle == null) {
                // 句柄缺失，直接重连
                reconnect(componentId, config);
                return;
            }
            Fwlib32 lib = Fwlib32Loader.get(config.getLibPath());
            short ret = lib.cnc_statinfo(handle, new Fwlib32.ODBST());
            if (ret != Fwlib32.EW_OK) {
                System.out.printf("[FOCAS2重连] componentId=%s 连接失效（返回码=%d），开始重连（%s:%d）%n",
                        componentId, ret, config.getIpAddr(), config.getPort());
                // 执行重连逻辑（先关旧句柄再建新）
                reconnect(componentId, config);
            }
        } catch (Exception e) {
            System.err.printf("[FOCAS2重连] componentId=%s 健康检查异常：%s%n", componentId, e.getMessage());
            // 健康检查异常（如库未加载）时，尝试清理死句柄，避免残留
            handleMap.remove(componentId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 执行重连逻辑
     * @param componentId 组件唯一标识
     * @param config 连接配置
     * @return 重连是否成功
     */
    private static boolean reconnect(String componentId, FanucFocasConfig config) {
        // 先关闭旧句柄（FOCAS2 同一连接对象句柄会互相占用，旧句柄必须先 close）
        closeHandle(componentId);
        try {
            Fwlib32 lib = Fwlib32Loader.get(config.getLibPath());
            ShortByReference handleRef = new ShortByReference();
            short ret = lib.cnc_allclibhndl3(config.getIpAddr(), config.getPort().shortValue(),
                    new NativeLong(config.getTimeout() == null ? 3000 : config.getTimeout()), handleRef);
            if (ret != Fwlib32.EW_OK) {
                System.err.printf("[FOCAS2重连] componentId=%s 重连失败（返回码=%d）%n", componentId, ret);
                handleMap.remove(componentId);
                return false;
            }
            handleMap.put(componentId, handleRef.getValue());
            System.out.printf("[FOCAS2重连] componentId=%s 重连成功（%s:%d, 句柄=%d）%n",
                    componentId, config.getIpAddr(), config.getPort(), handleRef.getValue());
            return true;
        } catch (Exception e) {
            System.err.printf("[FOCAS2重连] componentId=%s 重连失败：%s%n", componentId, e.getMessage());
            // 重连失败时移除无效句柄（避免下次检查重复处理）
            handleMap.remove(componentId);
            return false;
        }
    }

    /**
     * 主动强制重连（读取失败时由读取器触发）：关闭死句柄并建立新句柄
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
            reconnect(componentId, config);
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
                lib.cnc_close(handle);
            } catch (Exception e) {
                // 句柄关闭失败不影响后续
                System.err.printf("[FOCAS2连接] componentId=%s 句柄关闭失败：%s%n", componentId, e.getMessage());
            }
        }
    }
}
