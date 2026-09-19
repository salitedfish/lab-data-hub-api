//由AI修改
package com.labdatahub.component.mitsubishi_mc3e_tcp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.event.ComponentOnlineNotifier;

import lombok.extern.slf4j.Slf4j;

/**
 * 三菱 MC 连接管理器，维护多个设备的连接（新增自动检查+定时重连，MC 3E 帧无需握手）
 */
@Slf4j
public class MitsubishiMc3eConnectionManager {
    // 存储设备连接：key为componentId（线程安全）
    public static Map<String, Socket> connections = new ConcurrentHashMap<>();
    // 存储每个componentId对应的连接配置（用于重连）
    public static final Map<String, MitsubishiMc3eTcpConfig> configMap = new ConcurrentHashMap<>();

    /**
     * 每个 componentId 一把读写串行锁。
     *
     * <p>MC 3E 是「一个 socket 上发一帧、收一帧」的同步问答协议，一台设备只有一条 TCP 连接，
     * 而读跑在消费线程、写从 HTTP 线程进来、重连跑在健康检查线程上，三方共用同一个 socket。
     * 不加锁就会出现最糟的一种：<b>写的响应被读线程取走、读的数据被写线程当结束码解析</b>，
     * 两边都拿到错帧还不自知（位写尤其致命 —— 错位的结束码会直接判成「设备拒绝」）。
     *
     * <p>取锁策略有意<b>不对称</b>（与 S7/FINS/Modbus 一致，见方案 5.4）：
     * 读侧用无界 {@code lock()}（采集命脉，不能因为等写而失败）；
     * 写侧用有界 {@code tryLock(waitMs)}（外部请求，宁可返 503 让调用方重试，也不把 HTTP 线程挂住）。
     */
    private static final Map<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    // 连接健康检查调度器（全局单例，定时检查连接状态）
    private static final ScheduledExecutorService healthCheckScheduler = Executors.newScheduledThreadPool(
            1,
            r -> {
                Thread t = new Thread(r, "mitsubishi-connection-health-check");
                t.setDaemon(true); // 守护线程，应用退出时自动销毁
                return t;
            }
    );

    private static final int HEALTH_CHECK_INTERVAL = 10;
    /** 重连最大尝试次数（仅 forceReconnect 的指数退避会用到） */
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    /** 首次重试退避 */
    private static final long INITIAL_RETRY_DELAY_MS = 1000;
    /** 退避上限 */
    private static final long MAX_RETRY_DELAY_MS = 30000;

    // 静态初始化：启动全局连接健康检查（每10秒检查一次）
    static {
        healthCheckScheduler.scheduleAtFixedRate(
                MitsubishiMc3eConnectionManager::checkAllConnections,
                0,
                HEALTH_CHECK_INTERVAL,
                TimeUnit.SECONDS
        );
        log.info("MITSUBISHI 连接健康检查任务已启动，检查间隔={}秒", HEALTH_CHECK_INTERVAL);
    }

    /**
     * 创建连接（MC 3E 帧无需握手，建连即用）
     * @param componentId 组件唯一标识
     * @param config 连接配置
     * @return 首次连接是否成功
     */
    public static boolean addConnection(String componentId, MitsubishiMc3eTcpConfig config) {
        // 1. 校验参数
        if (componentId == null || config == null || config.getIpAddr() == null) {
            return false;
        }
        Socket socket = null;
        // 换连接必须持锁：否则会把在途的读写掐掉（见 lockMap 注释）
        ReentrantLock lock = getLock(componentId);
        lock.lock();
        try {
            configMap.put(componentId, config);
            // 先关闭旧连接（避免资源泄漏）
            Socket oldConn = connections.get(componentId);
            if (oldConn != null && !oldConn.isClosed()) {
                oldConn.close();
            }
            // 2. 执行连接逻辑（带连接超时，避免对不可达地址长时间阻塞）
            InetAddress address = InetAddress.getByName(config.getIpAddr());
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, config.getPort()), config.getTimeout());
            socket.setSoTimeout(config.getTimeout());
            socket.setTcpNoDelay(true); // 禁用Nagle算法，降低延迟

            // 3. 更新连接映射
            connections.put(componentId, socket);
            System.out.printf("[MITSUBISHI连接] componentId=%s 首次连接成功（%s:%d）%n",
                    componentId, config.getIpAddr(), config.getPort());
            // 连接成功，清除离线节流标记（允许后续断连再次通知离线）
            ComponentOnlineNotifier.markOnline(componentId);
        } catch (Exception e) {
            System.err.printf("[MITSUBISHI连接] componentId=%s 首次连接失败：%s%n", componentId, e.getMessage());
            // 连接失败时关闭刚创建的 socket，避免 socket 泄漏
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception closeEx) {
                    System.err.printf("[MITSUBISHI连接] componentId=%s 关闭失败连接异常：%s%n", componentId, closeEx.getMessage());
                }
            }
            // 连接失败时移除无效配置/连接，避免空转
            connections.remove(componentId);
            configMap.remove(componentId);
            // 首次连接失败视为组件离线，通知设备下线（已离线设备幂等跳过）
            ComponentOnlineNotifier.markOfflineAndNotify(componentId);
            return false;
        } finally {
            lock.unlock();
        }
        // 4. 启动消费线程（放 try 外：重复开启组件时 startConsume 抛 IllegalStateException，
        //    应复用已有消费线程，而不是回滚刚建立的有效连接）
        try {
            MitsubishiMc3eLoopConsumer.startConsume(componentId, SpringUtils.getBean(MitsubishiMc3eMessageConsumeService.class));
        } catch (IllegalStateException e) {
            System.out.printf("[MITSUBISHI连接] componentId=%s 已存在消费线程，跳过重复启动%n", componentId);
        }
        return true;
    }

    /**
     * 关闭单个设备连接（兼容原有逻辑，新增配置清理）
     */
    public static void closeConnection(String componentId) {
        // 关连接要持锁：否则会把在途的读写掐掉（见 lockMap 注释）。
        // 只包住「摘连接 + close」这一小段，消费线程的停止放到锁外，避免与消费线程抢锁时互相等
        ReentrantLock lock = getLock(componentId);
        lock.lock();
        try {
            // 1. 关闭连接
            Socket socket = connections.remove(componentId);
            closeQuietly(socket, componentId);
        } finally {
            lock.unlock();
        }
        // 2. 清理配置（停止该组件的重连检查）
        configMap.remove(componentId);
        // 组件已关，锁不再有意义；留着会随组件反复开关慢慢涨
        lockMap.remove(componentId);
        // 3. 清理消息队列和消费线程（原有逻辑保留）
        MitsubishiMc3eMessageScheduler.removeMessageQueue(componentId);
        MitsubishiMc3eLoopConsumer.stopConsume(componentId);
        System.out.printf("[MITSUBISHI连接] componentId=%s 连接已关闭，配置已清理%n", componentId);
    }

    /**
     * 关闭所有连接（兼容原有逻辑，新增调度器停止+配置清理）
     */
    public static void closeAllConnections() {
        // 1. 关闭所有连接
        connections.forEach((id, conn) -> closeQuietly(conn, id));
        // 2. 清理所有映射
        connections.clear();
        configMap.clear();
        lockMap.clear();
        // 3. 停止健康检查调度器（避免资源泄漏）
        healthCheckScheduler.shutdown();
        try {
            if (!healthCheckScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                healthCheckScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            healthCheckScheduler.shutdownNow();
        }
        System.out.println("[MITSUBISHI连接] 所有连接已关闭，健康检查调度器已停止");
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
     * @param componentId 组件唯一标识
     */
    private static void checkAndReconnect(String componentId) {
        MitsubishiMc3eTcpConfig config = configMap.get(componentId);
        if (config == null) {
            return;
        }
        Socket socket = connections.get(componentId);
        // 校验连接是否有效
        boolean isConnectionValid = isConnectionValid(socket);
        if (!isConnectionValid) {
            System.out.printf("[MITSUBISHI重连] componentId=%s 连接失效，开始重连（%s:%d）%n",
                    componentId, config.getIpAddr(), config.getPort());
            // 执行重连逻辑
            reconnect(componentId, config);
        } else {
            // 连接有效，清除离线节流标记（连接已恢复，允许再次断连时通知离线）
            ComponentOnlineNotifier.markOnline(componentId);
        }
    }

    /**
     * 精准校验连接是否有效
     * @param socket MC TCP连接
     * @return true=有效，false=失效
     */
    private static boolean isConnectionValid(Socket socket) {
        if (socket == null) {
            return false;
        }
        try {
            // 仅检查Socket本地状态；不再发送OOB紧急字节（sendUrgentData 0xFF）探测半开连接——
            // 半开连接内核缓冲仍可写入，探测无意义；且0xFF可能被PLC协议栈当垃圾数据打乱帧对齐（与Brother一致）。
            // 半开连接由读超时触发 forceReconnect 自愈
            if (!socket.isConnected() || socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 单次重建：换掉 connections 里的 socket，必须持锁 ——
     * 不持锁的话「读线程刚取到旧 socket → 重连把旧 socket close 掉」，这一帧收发就落空。
     *
     * <p>健康检查线程只走这一次尝试（见 {@link #checkAndReconnect} 注释），
     * 需要「立刻重连成功」的是读写失败的现场（{@link #forceReconnect}），那里才带头退避重试。
     *
     * @return 重连是否成功
     */
    private static boolean reconnect(String componentId, MitsubishiMc3eTcpConfig config) {
        ReentrantLock lock = getLock(componentId);
        lock.lock();
        try {
            // 1. 创建新连接
            InetAddress address = InetAddress.getByName(config.getIpAddr());
            Socket newSocket = new Socket(address, config.getPort());
            newSocket.setSoTimeout(config.getTimeout());
            newSocket.setTcpNoDelay(true);

            // 2. 关闭旧连接（释放资源）
            closeQuietly(connections.get(componentId), componentId);

            // 3. 更新连接映射
            connections.put(componentId, newSocket);
            System.out.printf("[MITSUBISHI重连] componentId=%s 重连成功（%s:%d）%n",
                    componentId, config.getIpAddr(), config.getPort());
            // 重连成功，清除离线节流标记
            ComponentOnlineNotifier.markOnline(componentId);
            return true;
        } catch (Exception e) {
            System.err.printf("[MITSUBISHI重连] componentId=%s 重连失败：%s%n", componentId, e.getMessage());
            // 重连失败时移除无效连接（避免下次检查重复处理）
            connections.remove(componentId);
            // 重连失败视为组件离线，通知设备下线（节流，仅在在线→离线转变时发一次）
            ComponentOnlineNotifier.markOfflineAndNotify(componentId);
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 强制重连（读/写异常时由消费端与写值链路调用：半开连接本地状态检测不出来，靠读超时触发重连，
     * 与 S7/FINS 的 forceReconnect 对齐）
     *
     * <p>⚠️ 调用方必须在 {@code unlock()} <b>之后</b>调用本站（读写链路都遵守）——建连接是阻塞 IO，
     * 持着锁做会把整条采集链路卡住一个 connect 超时。本站自身只对「摘掉旧连接」这一小段持锁，
     * 退避 sleep 全程在锁外。
     *
     * @param componentId 组件唯一标识
     */
    public static void forceReconnect(String componentId) {
        MitsubishiMc3eTcpConfig config = configMap.get(componentId);
        if (config == null) {
            System.err.printf("[MITSUBISHI重连] componentId=%s 无连接配置，无法重连%n", componentId);
            return;
        }
        // 先摘掉旧连接（持锁），让等在锁上的读线程立刻看到「没连接」而不是复用那个坏 socket
        ReentrantLock lock = getLock(componentId);
        lock.lock();
        try {
            closeQuietly(connections.remove(componentId), componentId);
        } finally {
            lock.unlock();
        }
        // 再带指数退避重连：每次尝试各自成对持锁，sleep 在锁外
        long delayMs = INITIAL_RETRY_DELAY_MS;
        for (int attempt = 1; attempt <= MAX_RECONNECT_ATTEMPTS; attempt++) {
            if (reconnect(componentId, config)) {
                return;
            }
            if (attempt < MAX_RECONNECT_ATTEMPTS) {
                System.err.printf("[MITSUBISHI重连] componentId=%s 第%d次重连失败，%dms后重试%n",
                        componentId, attempt, delayMs);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.printf("[MITSUBISHI重连] componentId=%s 重连等待被中断%n", componentId);
                    return;
                }
                delayMs = Math.min(delayMs * 2, MAX_RETRY_DELAY_MS); // 指数退避，上限30秒
            }
        }
        System.err.printf("[MITSUBISHI重连] componentId=%s 重连失败，已达到最大重试次数 %d%n",
                componentId, MAX_RECONNECT_ATTEMPTS);
    }

    /** 安静地关掉一个 socket（记录异常但不外抛），null 安全 */
    private static void closeQuietly(Socket socket, String componentId) {
        if (socket == null) {
            return;
        }
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            System.err.printf("[MITSUBISHI连接] componentId=%s 关闭失败：%s%n", componentId, e.getMessage());
        }
    }

    /**
     * 取该 componentId 的读写串行锁（惰性创建）
     *
     * <p>读、写、重连三方共用同一把锁。{@link ReentrantLock} 可重入，所以同一线程
     * 「持锁 → 换连接」不会自锁。
     */
    public static ReentrantLock getLock(String componentId) {
        return lockMap.computeIfAbsent(String.valueOf(componentId), k -> new ReentrantLock());
    }
}
