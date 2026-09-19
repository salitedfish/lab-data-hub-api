//由AI修改
package com.labdatahub.component.modbus_tcp;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

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
    // 已放弃重连的 componentId：失败次数达上限后连接是"死"的，但健康检查每 10 秒仍会走到上限分支，
    // 只在这个集合里第一次加入时提示一条，避免同一条错误无限刷（重连/关连接时清掉）
    private static final Set<String> reconnectStoppedSet = ConcurrentHashMap.newKeySet();

    /**
     * 每个 componentId 一把读写串行锁。
     *
     * <p>一台设备只有一条 TCP 连接（TCPMasterConnection），而 {@code ModbusTCPTransaction} 不是线程安全的：
     * 读跑在消费线程、写从 HTTP 线程进来、重连跑在 modbus-reconnect-worker 上，三方同时操作同一个 socket
     * 会<b>响应错配</b>——读到的实时数据串位、写的结果判断错误。所以读、写、重连三方都必须过这把锁。
     *
     * <p>取锁策略有意的<b>不对称</b>（见方案 5.4）：
     * 读侧用无界 {@code lock()}——读是采集链路的命脉，抢锁时不能因为「等写」而失败；
     * 写侧用有界 {@code tryLock(waitMs)}——写是外部请求，宁可返回 503 让调用方重试，
     * 也不能让 HTTP 线程无限挂着。
     */
    private static final Map<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();
    
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
            // 组件重新连接，清掉"已放弃重连"标记（下次再断连时仍会提示一条）
            reconnectStoppedSet.remove(componentId);

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
        reconnectStoppedSet.remove(componentId);
        // 2.1 清锁表：组件已关，锁不再有意义；留着会随组件反复开关慢慢涨
        lockMap.remove(componentId);

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
        reconnectStoppedSet.clear();
        lockMap.clear();

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
                // 只在刚到达上限时提示一次：健康检查每 10 秒会再走到这里，不加这个判断会无限刷同一条错误
                if (reconnectStoppedSet.add(componentId)) {
                    log.error("[Modbus 重连] componentId={} 重连失败次数已达上限（{}次），停止重连（需手动重启组件）",
                            componentId, MAX_RECONNECT_ATTEMPTS);
                }
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
        // 重连会 close() 旧连接再换掉 connections 里的对象 —— 不持锁的话：
        //   写线程持锁 → 拿到 conn A → 正在 write/read，重连线程把 conn A.close() 了
        // 写结果从此无法判定。读失败只是丢一次数据，写失败是「不知道写没写进去」，量级不同。
        ReentrantLock lock = getLock(componentId);
        lock.lock();
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
        } finally {
            lock.unlock();
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

    /**
     * 取该 componentId 的读写串行锁（惰性创建）
     *
     * <p>读、写、重连三方共用同一把锁。{@link ReentrantLock} 可重入，所以同一线程
     * 「持锁 → 调 getValidConnection → 内部同步 reconnect」不会死锁。
     *
     * @param componentId 组件唯一标识
     * @return 该组件的锁，永不为 null（componentId 为 null 时返回一把独立锁，调用方应自行保证非空）
     */
    public static ReentrantLock getLock(String componentId) {
        return lockMap.computeIfAbsent(String.valueOf(componentId), k -> new ReentrantLock());
    }

    /**
     * 取该 componentId 的连接配置（供上层拼「连不上 127.0.0.1:502」这类提示语用）
     *
     * @return 配置；组件未开启或开启时连接失败时为 null
     */
    public static ModbusTcpConfig getConfig(String componentId) {
        return configMap.get(componentId);
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

    /**
     * 强制重连（丢弃当前连接，重建一条）
     *
     * <p>给写值链路在「结果不确定」之后用：回显对不上或读响应超时，说明这条 socket 上
     * 的请求/响应已经错位，而 {@link #isConnectionValid} 只看本地 isConnected()，
     * 错位但没断的 socket 会被判成「有效」从而一直复用下去。
     *
     * <p>⚠️ 调用方必须在 {@code unlock()} <b>之后</b>调用 —— 这里会真的建 TCP 连接，
     * 持着锁做会把整条读链路卡住一个 connect 超时。
     */
    public static void forceReconnect(String componentId) {
        ModbusTcpConfig config = configMap.get(componentId);
        if (config == null) {
            return;
        }
        AtomicInteger failCount = reconnectFailCountMap.computeIfAbsent(componentId, k -> new AtomicInteger(0));
        // 先摘掉旧连接：保留着的话 getValidConnection 会认为它「有效」而不再重建
        connections.remove(componentId);
        reconnect(componentId, config, failCount);
    }

    // 强制重连并返回新连接
    public static TCPMasterConnection renewConnection(String componentId) {
        ModbusTcpConfig config = configMap.get(componentId);
        if (config == null) return null;
        // 同样要持锁：这里也会 connections.put 换掉连接对象，理由同 reconnect
        ReentrantLock lock = getLock(componentId);
        lock.lock();
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
        } finally {
            lock.unlock();
        }
    }
}
