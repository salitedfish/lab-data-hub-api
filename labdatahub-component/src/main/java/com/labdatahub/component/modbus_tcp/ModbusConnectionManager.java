//由AI修改
package com.labdatahub.component.modbus_tcp;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.event.ComponentOnlineNotifier;

import lombok.extern.slf4j.Slf4j;
import net.wimpi.modbus.net.TCPMasterConnection;

/**
 * 连接管理器，维护多个设备的连接（支持自动重连+健康检查）
 */
@Slf4j
public class ModbusConnectionManager {
    // 存储串口服务器连接：key 为 componentId（线程安全）
	public static Map<String, TCPMasterConnection> connections = new ConcurrentHashMap<>();
    // 存储每个 componentId 对应的连接配置（用于重连）
    private static final Map<String, ModbusTcpConfig> configMap = new ConcurrentHashMap<>();
    // 存储每个 componentId 的重连失败次数
    private static final Map<String, AtomicInteger> reconnectFailCountMap = new ConcurrentHashMap<>();
    
    // 连接健康检查调度器（全局单例）
    private static final ScheduledExecutorService healthCheckScheduler = Executors.newScheduledThreadPool(
            1,
            r -> {
                Thread t = new Thread(r, "modbus-connection-health-check");
                t.setDaemon(true);
                return t;
            }
    );
    
    // 重连调度器（独立线程池，避免阻塞健康检查）
    private static final ScheduledExecutorService reconnectScheduler =  Executors.newScheduledThreadPool(
    		1,
            r -> {
                Thread t = new Thread(r, "modbus-reconnect-worker");
                t.setDaemon(true);
                return t;
            }
    );
    
    // ========== 可配置参数（可根据业务调整） ==========
    // 健康检查间隔（秒）
    private static final int HEALTH_CHECK_INTERVAL = 10;
    // 最大重连次数（超过后放弃，需手动重启）
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    // 重连基础延迟（毫秒），采用指数退避
    private static final int RECONNECT_BASE_DELAY_MS = 1000;
    // 重连最大延迟（毫秒）
    private static final int RECONNECT_MAX_DELAY_MS = 60000;

   
    // 静态初始化：启动全局连接健康检查（每10秒检查一次，可根据业务调整）
    // 首次延迟0秒执行，之后每10秒执行一次健康检查
    static {
        healthCheckScheduler.scheduleAtFixedRate(
                ModbusConnectionManager::checkAllConnections,
                0,
                HEALTH_CHECK_INTERVAL,
                TimeUnit.SECONDS
        );
        log.info("Modbus 连接健康检查任务已启动，检查间隔={}秒", HEALTH_CHECK_INTERVAL);
    }

    /**
     * 创建连接
     * @param componentId 组件唯一标识
     * @param config 连接配置
     * @return 首次连接是否成功
     */
    public static boolean addConnection(String componentId, ModbusTcpConfig config) {
        TCPMasterConnection connection = null;
        try {
            if (componentId == null || config == null || config.getIpAddr() == null) {
                log.error("[Modbus 连接] componentId={} 参数非法", componentId);
                return false;
            }

            configMap.put(componentId, config);
            reconnectFailCountMap.put(componentId, new AtomicInteger(0));

            InetAddress address = InetAddress.getByName(config.getIpAddr());
            connection = new TCPMasterConnection(address);
            connection.setPort(config.getPort());
            connection.setTimeout(config.getTimeout());

            TCPMasterConnection oldConn = connections.get(componentId);
            if (oldConn != null && oldConn.isConnected()) {
                try {
                    oldConn.close();
                    log.debug("[Modbus 连接] componentId={} 旧连接已关闭", componentId);
                } catch (Exception e) {
                    log.warn("[Modbus 连接] componentId={} 关闭旧连接失败：{}", componentId, e.getMessage());
                }
            }

            if (!connection.isConnected()) {
                connection.connect();
            }

            connections.put(componentId, connection);
            log.info("[Modbus 连接] componentId={} 首次连接成功（{}:{}）",
                    componentId, config.getIpAddr(), config.getPort());
            // 连接成功，清除离线节流标记（允许后续断连再次通知离线）
            ComponentOnlineNotifier.markOnline(componentId);
        } catch (Exception e) {
            log.error("[Modbus 连接] componentId={} 首次连接失败：{}", componentId, e.getMessage(), e);
            connections.remove(componentId);
            configMap.remove(componentId);
            reconnectFailCountMap.remove(componentId);
            // 关闭刚创建的连接，避免失败时 socket 泄漏
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception closeEx) {
                    log.warn("[Modbus 连接] componentId={} 关闭失败连接异常：{}", componentId, closeEx.getMessage());
                }
            }
            // 首次连接失败视为组件离线，通知设备下线（已离线设备幂等跳过）
            ComponentOnlineNotifier.markOfflineAndNotify(componentId);
            return false;
        }
        // 启动消费线程（放 try 外：重复开启组件时 startConsume 抛 IllegalStateException，
        //    应复用已有消费线程，而不是回滚刚建立的有效连接）
        try {
            ModbusLoopConsumer.startConsume(componentId, SpringUtils.getBean(ModbusMessageConsumeService.class));
        } catch (IllegalStateException e) {
            log.warn("[Modbus 连接] componentId={} 已存在消费线程，跳过重复启动", componentId);
        }
        return true;
    }

    /**
     * 关闭单个设备连接
     */
    public static void closeConnection(String componentId) {
    	// 1. 关闭连接
        TCPMasterConnection connection = connections.remove(componentId);
        if (connection != null) {
            try {
                if (connection.isConnected()) {
                    connection.close();
                }
                log.debug("[Modbus 连接] componentId={} 连接已关闭", componentId);
            } catch (Exception e) {
                log.warn("[Modbus 连接] componentId={} 关闭连接失败：{}", componentId, e.getMessage());
            }
        }
        // 2. 清理配置（停止该组件的重连检查）
        configMap.remove(componentId);
        reconnectFailCountMap.remove(componentId);
        
        // 3. 清理消息队列和消费线程（原有逻辑保留）
        ModbusMessageScheduler.removeMessageQueue(componentId);
        ModbusLoopConsumer.stopConsume(componentId);

        log.info("[Modbus 连接] componentId={} 连接已关闭，配置已清理", componentId);
    }

    /**
     * 关闭所有连接（应用关闭时调用）
     */
    public static void closeAllConnections() {
        connections.forEach((id, conn) -> {
            try {
                if (conn.isConnected()) {
                    conn.close();
                }
            } catch (Exception e) {
                log.warn("[Modbus 连接] componentId={} 关闭失败：{}", id, e.getMessage());
            }
        });

        connections.clear();
        configMap.clear();
        reconnectFailCountMap.clear();

        shutdownExecutor(healthCheckScheduler, "健康检查调度器");
        shutdownExecutor(reconnectScheduler, "重连调度器");

        log.info("[Modbus 连接] 所有连接已关闭，调度器已停止");
    }

    /**
     * 检查所有连接状态，失效则自动重连
     */
    private static void checkAllConnections() {
        for (String componentId : configMap.keySet()) {
            try {
                checkAndReconnect(componentId);
            } catch (Exception e) {
                log.error("[Modbus 健康检查] componentId={} 检查失败：{}", componentId, e.getMessage(), e);
            }
        }
    }

    /**
     * 检查单个 componentId 的连接状态，失效则重连
     */
    private static void checkAndReconnect(String componentId) {
        ModbusTcpConfig config = configMap.get(componentId);
        if (config == null) {
            return;
        }

        TCPMasterConnection connection = connections.get(componentId);
        if (!isConnectionValid(connection)) {
            AtomicInteger failCount = reconnectFailCountMap.computeIfAbsent(componentId, k -> new AtomicInteger(0));
            int currentFailCount = failCount.get();
            
            if (currentFailCount >= MAX_RECONNECT_ATTEMPTS) {
                log.error("[Modbus 重连] componentId={} 重连失败次数已达上限（{}次），停止重连", 
                        componentId, MAX_RECONNECT_ATTEMPTS);
                connections.remove(componentId);
                return;
            }

            log.warn("[Modbus 重连] componentId={} 连接失效（失败{}/{}次），开始重连（{}:{}）", 
                    componentId, currentFailCount + 1, MAX_RECONNECT_ATTEMPTS, 
                    config.getIpAddr(), config.getPort());
            
            reconnectScheduler.submit(() -> reconnect(componentId, config, failCount));
        } else {
            // 连接有效，清除离线节流标记（连接已恢复，允许再次断连时通知离线）
            ComponentOnlineNotifier.markOnline(componentId);
        }
    }

    /**
     * 精准校验连接是否有效
     */
//    private static boolean isConnectionValid(TCPMasterConnection connection) {
//        if (connection == null||!connection.isConnected()) {
//            return false;
//        }else {
//            return true;
//        }
//    }
    
    private static boolean isConnectionValid(TCPMasterConnection connection) {
        if (connection == null || !connection.isConnected()) {
            return false;
        }
        else {
        	return true;
        }
    }

    /**
     * 执行重连逻辑（带指数退避）
     */
    private static boolean reconnect(String componentId, ModbusTcpConfig config, AtomicInteger failCount) {
        try {
            int retryCount = failCount.incrementAndGet();
            
            InetAddress address = InetAddress.getByName(config.getIpAddr());
            TCPMasterConnection newConn = new TCPMasterConnection(address);
            newConn.setPort(config.getPort());
            newConn.setTimeout(config.getTimeout());

            TCPMasterConnection oldConn = connections.get(componentId);
            if (oldConn != null) {
                try {
                    oldConn.close();
                } catch (Exception e) {
                    log.warn("[Modbus 重连] componentId={} 旧连接关闭失败：{}", componentId, e.getMessage());
                }
            }

            newConn.connect();
            connections.put(componentId, newConn);

            failCount.set(0);
            log.info("[Modbus 重连] componentId={} 重连成功（第{}次尝试，{}:{}）",
                    componentId, retryCount, config.getIpAddr(), config.getPort());
            // 重连成功，清除离线节流标记
            ComponentOnlineNotifier.markOnline(componentId);
            return true;

        } catch (Exception e) {
            int currentFailCount = failCount.get();
            long delayMs = calculateReconnectDelay(currentFailCount);

            log.error("[Modbus 重连] componentId={} 重连失败（第{}次），{}ms 后重试：{}",
                    componentId, currentFailCount, delayMs, e.getMessage());

            connections.remove(componentId);
            // 重连失败视为组件离线，通知设备下线（节流，仅在在线→离线转变时发一次）
            ComponentOnlineNotifier.markOfflineAndNotify(componentId);

            if (currentFailCount < MAX_RECONNECT_ATTEMPTS) {
                reconnectScheduler.schedule(
                        () -> reconnect(componentId, config, failCount),
                        delayMs,
                        TimeUnit.MILLISECONDS
                );
            } else {
                log.error("[Modbus 重连] componentId={} 重连失败次数已达上限，停止重连", componentId);
            }

            return false;
        }
    }

    /**
     * 计算重连延迟（指数退避算法）
     */
    private static long calculateReconnectDelay(int attemptCount) {
        long delay = (long) (RECONNECT_BASE_DELAY_MS * Math.pow(2, attemptCount - 1));
        delay = Math.min(delay, RECONNECT_MAX_DELAY_MS);
        delay = delay + (long) (Math.random() * 1000);
        return delay;
    }

    /**
     * 优雅关闭线程池
     */
    private static void shutdownExecutor(ExecutorService executor, String name) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                log.warn("[{}] 强制关闭", name);
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            log.warn("[{}] 关闭被中断", name, e);
        }
    }

    /**
     * 获取连接状态（供外部查询）
     */
    public static boolean isConnected(String componentId) {
        TCPMasterConnection connection = connections.get(componentId);
        return isConnectionValid(connection);
    }

    /**
     * 获取重连失败次数（供监控使用）
     */
    public static int getReconnectFailCount(String componentId) {
        AtomicInteger count = reconnectFailCountMap.get(componentId);
        return count != null ? count.get() : 0;
    }
    
 // 获取有效连接（如果当前连接无效则触发重连）
//    public static TCPMasterConnection getValidConnection(String componentId) {
//        TCPMasterConnection conn = connections.get(componentId);
//        if (!isConnectionValid(conn)) {
//            // 异步触发重连（或者同步等待重连结果）
//            checkAndReconnect(componentId);
//            // 等待一小段时间让重连完成（简单起见，可以同步重连）
//            conn = connections.get(componentId);
//        }
//        return conn;
//    }
    
    public static TCPMasterConnection getValidConnection(String componentId) {
        TCPMasterConnection conn = connections.get(componentId);
        if (!isConnectionValid(conn)) {
            log.warn("[Modbus 连接] componentId={} 连接无效，尝试同步重连", componentId);
            ModbusTcpConfig config = configMap.get(componentId);
            if (config != null) {
                // 同步调用一次重连逻辑，避免异步等待
                AtomicInteger failCount = reconnectFailCountMap.computeIfAbsent(componentId, k -> new AtomicInteger(0));
                reconnect(componentId, config, failCount);
            }
            conn = connections.get(componentId);
        }
        return conn;
    }

    // 强制重连并返回新连接
    public static TCPMasterConnection renewConnection(String componentId) {
        ModbusTcpConfig config = configMap.get(componentId);
        if (config == null) return null;
        try {
            InetAddress address = InetAddress.getByName(config.getIpAddr());
            TCPMasterConnection newConn = new TCPMasterConnection(address);
            newConn.setPort(config.getPort());
            newConn.setTimeout(config.getTimeout());
            newConn.connect();
            connections.put(componentId, newConn);
            return newConn;
        } catch (Exception e) {
            log.error("重连失败", e);
            return null;
        }
    }
}
