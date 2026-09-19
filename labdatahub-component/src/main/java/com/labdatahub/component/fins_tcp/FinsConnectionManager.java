//由AI修改
package com.labdatahub.component.fins_tcp;

import java.io.InputStream;
import java.io.OutputStream;
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
 * FINS连接管理器，维护多个设备的连接（新增自动检查+定时重连）
 */
@Slf4j
public class FinsConnectionManager {
    // 存储设备连接：key为componentId（线程安全）
    public static Map<String, Socket> connections = new ConcurrentHashMap<>();
    // 存储每个componentId对应的连接配置（用于重连）
    public static final Map<String, FinsTcpConfig> configMap = new ConcurrentHashMap<>();

    /**
     * 每个 componentId 一把读写串行锁。
     *
     * <p>FINS 是「一个 socket 上发一帧、收一帧」的同步问答协议，一台设备只有一条 TCP 连接，
     * 而读跑在消费线程、写从 HTTP 线程进来、重连跑在健康检查线程上，三方共用同一个 socket。
     * 不加锁就会出现最糟的一种：<b>写的响应被读线程取走、读的数据被写线程当结束码解析</b>，
     * 两边都拿到错帧还不自知。所以「取连接 → 收发 → 换连接」必须整段串起来。
     *
     * <p>取锁策略有意<b>不对称</b>（与 S7/Modbus 一致，见方案 5.4）：
     * 读侧用无界 {@code lock()}（采集命脉，不能因为等写而失败）；
     * 写侧用有界 {@code tryLock(waitMs)}（外部请求，宁可返 503 让调用方重试，也不把 HTTP 线程挂住）。
     */
    private static final Map<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    /** 单次握手响应长度：header 8 + Command 4 + Error 4 + Srce节点 4 + Dest节点 4 */
    private static final int HANDSHAKE_RESP_LEN = 24;
    /** 握手响应里「Srce 节点（PLC 自己的节点号）」的起始偏移 */
    private static final int HANDSHAKE_SRCE_OFFSET = 16;

    // 连接健康检查调度器（全局单例，定时检查连接状态）
    private static final ScheduledExecutorService healthCheckScheduler = Executors.newScheduledThreadPool(
            1,
            r -> {
                Thread t = new Thread(r, "fins-connection-health-check");
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
                FinsConnectionManager::checkAllConnections,
                0,
                HEALTH_CHECK_INTERVAL,
                TimeUnit.SECONDS
        );
        log.info("FINS 连接健康检查任务已启动，检查间隔={}秒", HEALTH_CHECK_INTERVAL);
    }

    /**
     * 从输入流中读取指定长度的数据，直到读满
     */
    private static void readFully(InputStream in, byte[] buffer) throws Exception {
        int totalRead = 0;
        int len;
        while (totalRead < buffer.length) {
            len = in.read(buffer, totalRead, buffer.length - totalRead);
            if (len == -1) {
                throw new Exception("流已结束，无法读取足够的数据，预期" + buffer.length + "字节，已读取" + totalRead + "字节");
            }
            totalRead += len;
        }
    }

    /**
     * FINS/TCP握手，获取PLC节点地址
     *
     * <p>响应 24 字节 = header 8 + Command 4 + Error 4 + <b>Srce 节点 4（PLC 自己的节点号）</b>
     * + Dest 节点 4（客户端节点号回显）。要取的是 <b>Srce</b>：Dest 是自己发过去的那个号，
     * 拿它填 DA1 等于让 PLC 把应答发给自己，换个不是 1 的节点号立刻读不通。
     */
    private static int doHandshake(Socket socket, int clientNodeAddr) throws Exception {
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();

        // 构建握手请求报文
        byte[] handshakeReq = new byte[] {
            'F', 'I', 'N', 'S', // FINS头
            0x00, 0x00, 0x00, 0x0C, // 后续数据长度：12字节
            0x00, 0x00, 0x00, 0x00, // 命令：节点地址发送
            0x00, 0x00, 0x00, 0x00, // 错误码
            0x00, 0x00, 0x00, (byte) clientNodeAddr // 客户端节点地址
        };

        out.write(handshakeReq);
        out.flush();

        // 读取握手响应，共24字节（使用readFully确保读满）
        byte[] handshakeResp = new byte[HANDSHAKE_RESP_LEN];
        readFully(in, handshakeResp);

        // 解析PLC节点地址（Srce 节点），4字节大端
        int plcNodeAddr = ((handshakeResp[HANDSHAKE_SRCE_OFFSET] & 0xFF) << 24) |
                          ((handshakeResp[HANDSHAKE_SRCE_OFFSET + 1] & 0xFF) << 16) |
                          ((handshakeResp[HANDSHAKE_SRCE_OFFSET + 2] & 0xFF) << 8) |
                          (handshakeResp[HANDSHAKE_SRCE_OFFSET + 3] & 0xFF);

        return plcNodeAddr;
    }

    /**
     * 创建连接（兼容原有逻辑，新增配置存储）
     * @param componentId 组件唯一标识
     * @param config 连接配置
     * @return 首次连接是否成功
     */
    public static boolean addConnection(String componentId, FinsTcpConfig config) {
        if (componentId == null || config == null || config.getIpAddr() == null) {
            log.warn("[FINS连接] componentId={} 参数非法", componentId);
            return false;
        }
        Socket socket = null;
        // 换连接必须持锁：否则会把在途的读写掐掉（见 lockMap 注释）
        ReentrantLock lock = getLock(componentId);
        lock.lock();
        try {
            configMap.put(componentId, config);
            // 先关闭旧连接（避免资源泄漏）
            closeOldConnection(componentId);
            // 执行连接逻辑（带连接超时，避免对不可达地址长时间阻塞）
            socket = buildSocket(config);

            // 建立连接后执行握手
            int plcNodeAddr = doHandshake(socket, config.getClientNodeAddress());
            config.setPlcNodeAddress(plcNodeAddr);
            log.info("[FINS握手] componentId={} 获取PLC节点地址：{}", componentId, plcNodeAddr);

            // 更新连接映射
            connections.put(componentId, socket);
            log.info("[FINS连接] componentId={} 首次连接成功（{}:{}）",
                    componentId, config.getIpAddr(), config.getPort());
            // 连接成功，清除离线节流标记（允许后续断连再次通知离线）
            ComponentOnlineNotifier.markOnline(componentId);
        } catch (Exception e) {
            log.error("[FINS连接] componentId={} 首次连接失败：{}", componentId, e.getMessage());
            // 连接失败时关闭刚创建的 socket（含握手失败场景），避免 socket 泄漏
            closeQuietly(socket, componentId);
            // 连接失败时移除无效配置/连接，避免空转
            connections.remove(componentId);
            configMap.remove(componentId);
            // 首次连接失败视为组件离线，通知设备下线（已离线设备幂等跳过）
            ComponentOnlineNotifier.markOfflineAndNotify(componentId);
            return false;
        } finally {
            lock.unlock();
        }
        // 启动消费线程（放 try 外：重复开启组件时 startConsume 抛 IllegalStateException，
        //    应复用已有消费线程，而不是回滚刚建立的有效连接）
        try {
            FinsLoopConsumer.startConsume(componentId, SpringUtils.getBean(FinsMessageConsumeService.class));
        } catch (IllegalStateException e) {
            log.warn("[FINS连接] componentId={} 已存在消费线程，跳过重复启动", componentId);
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
        FinsMessageScheduler.removeMessageQueue(componentId);
        FinsLoopConsumer.stopConsume(componentId);
        log.info("[FINS连接] componentId={} 连接已关闭，配置已清理", componentId);
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
            Thread.currentThread().interrupt();
        }
        log.info("[FINS连接] 所有连接已关闭，健康检查调度器已停止");
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
     *
     * <p>这里<b>只尝试一次</b>，不走 {@link #reconnectWithRetry} 的指数退避：健康检查是每 10 秒一轮的
     * 全局单线程任务，一次退避重连要占住 1+2 秒 sleep 加最多 3 个 connect 超时，会把<b>其它组件</b>的
     * 检查一起饿死。设备真的长时间不在线，下一轮 10 秒后自然会再试一次，节奏本就是 10 秒一次。
     * 需要「立刻重连成功」的是读写失败的现场（{@link #forceReconnect}），那里才用退避重试。
     *
     * @param componentId 组件唯一标识
     */
    private static void checkAndReconnect(String componentId) {
        FinsTcpConfig config = configMap.get(componentId);
        if (config == null) {
            return;
        }
        Socket socket = connections.get(componentId);
        // 校验连接是否有效
        boolean isConnectionValid = isConnectionValid(socket);
        if (!isConnectionValid) {
            log.warn("[FINS重连] componentId={} 连接失效，开始重连（{}:{}）",
                    componentId, config.getIpAddr(), config.getPort());
            // 执行重连逻辑（单次）
            if (doReconnect(componentId, config)) {
                ComponentOnlineNotifier.markOnline(componentId);
            } else {
                connections.remove(componentId);
                // 重连失败视为组件离线，通知设备下线（节流，仅在在线→离线转变时发一次）
                ComponentOnlineNotifier.markOfflineAndNotify(componentId);
            }
        } else {
            // 连接有效，清除离线节流标记（连接已恢复，允许再次断连时通知离线）
            ComponentOnlineNotifier.markOnline(componentId);
        }
    }

    /**
     * 精准校验连接是否有效
     * @param socket FINS TCP连接
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
     * 强制重连（读/写异常时由消费端与写值链路调用：半开连接本地状态检测不出来，靠读超时触发重连，
     * 与 S7 的 forceReconnect 对齐）
     *
     * <p>⚠️ 调用方必须在 {@code unlock()} <b>之后</b>调用本站（读写链路都遵守）——建连接是阻塞 IO，
     * 持着锁做会把整条采集链路卡住一个 connect 超时。本站自身只对「摘掉旧连接」这一小段持锁。
     *
     * @param componentId 组件唯一标识
     */
    public static void forceReconnect(String componentId) {
        FinsTcpConfig config = configMap.get(componentId);
        if (config == null) {
            log.warn("[FINS重连] componentId={} 无连接配置，无法重连", componentId);
            return;
        }
        ReentrantLock lock = getLock(componentId);
        lock.lock();
        try {
            Socket oldConn = connections.remove(componentId);
            closeQuietly(oldConn, componentId);
        } finally {
            lock.unlock();
        }
        reconnectWithRetry(componentId, config);
    }

    /**
     * 指数退避重连：每次尝试各自成对持锁（见 {@link #doReconnect}），
     * 退避 sleep 放在锁外 —— 否则一次重连会把读链路卡满 1+2+4 秒。
     */
    private static void reconnectWithRetry(String componentId, FinsTcpConfig config) {
        int attempt = 0;
        long delayMs = INITIAL_RETRY_DELAY_MS;
        while (attempt < MAX_RECONNECT_ATTEMPTS) {
            attempt++;
            log.info("[FINS重连] componentId={} 第{}次尝试重连", componentId, attempt);
            if (doReconnect(componentId, config)) {
                log.info("[FINS重连] componentId={} 重连成功", componentId);
                // 重连成功，清除离线节流标记
                ComponentOnlineNotifier.markOnline(componentId);
                return;
            }
            if (attempt < MAX_RECONNECT_ATTEMPTS) {
                log.warn("[FINS重连] componentId={} 第{}次重连失败，{}ms后重试", componentId, attempt, delayMs);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("[FINS重连] componentId={} 重连等待被中断", componentId);
                    break;
                }
                delayMs = Math.min(delayMs * 2, MAX_RETRY_DELAY_MS); // 指数退避，上限30秒
            }
        }
        log.error("[FINS重连] componentId={} 重连失败，已达到最大重试次数 {}", componentId, MAX_RECONNECT_ATTEMPTS);
        connections.remove(componentId);
        // 重连失败视为组件离线，通知设备下线（节流，仅在在线→离线转变时发一次）
        ComponentOnlineNotifier.markOfflineAndNotify(componentId);
    }

    /**
     * 单次重建：换掉 connections 里的 socket，必须持锁 ——
     * 不持锁的话「读线程刚取到旧 socket → 重连把旧 socket close 掉」，这一帧收发就落空。
     *
     * @return 是否重建成功
     */
    private static boolean doReconnect(String componentId, FinsTcpConfig config) {
        ReentrantLock lock = getLock(componentId);
        lock.lock();
        try {
            closeOldConnection(componentId);
            Socket newSocket = buildSocket(config);
            // 握手拿 PLC 节点地址（重连后节点号可能不变，但不能假设 —— 重新问一次）
            int plcNodeAddr = doHandshake(newSocket, config.getClientNodeAddress());
            config.setPlcNodeAddress(plcNodeAddr);
            connections.put(componentId, newSocket);
            log.info("[FINS重连] componentId={} 重建连接成功（{}:{}，PLC节点地址={}）",
                    componentId, config.getIpAddr(), config.getPort(), plcNodeAddr);
            return true;
        } catch (Exception e) {
            log.error("[FINS重连] componentId={} 重连尝试失败: {}", componentId, e.getMessage());
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 取该 componentId 的读写串行锁（惰性创建）
     *
     * <p>读、写、重连三方共用同一把锁。{@link ReentrantLock} 可重入，所以同一线程
     * 「持锁 → 换连接」不会自锁。
     *
     * @param componentId 组件唯一标识
     * @return 该组件的锁，永不为 null
     */
    public static ReentrantLock getLock(String componentId) {
        return lockMap.computeIfAbsent(String.valueOf(componentId), k -> new ReentrantLock());
    }

    /**
     * 取该 componentId 的连接（按需取，不要缓存到局部变量长期持有 —— 重连会换对象）
     */
    public static Socket getConnection(String componentId) {
        return connections.get(componentId);
    }

    /**
     * 取该 componentId 的连接配置（供上层拼「连不上 127.0.0.1:9600」这类提示语用）
     *
     * @return 配置；组件未开启或开启时连接失败时为 null
     */
    public static FinsTcpConfig getConfig(String componentId) {
        return configMap.get(componentId);
    }

    /** 建一条已连上、设好超时的 socket（连接带超时，避免对不可达地址长时间阻塞） */
    private static Socket buildSocket(FinsTcpConfig config) throws Exception {
        InetAddress address = InetAddress.getByName(config.getIpAddr());
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(address, config.getPort()), config.getTimeout());
        socket.setSoTimeout(config.getTimeout());
        socket.setTcpNoDelay(true); // 禁用Nagle算法，降低延迟
        return socket;
    }

    /** 关掉并摘掉旧连接（调用方须持锁） */
    private static void closeOldConnection(String componentId) {
        Socket oldConn = connections.remove(componentId);
        closeQuietly(oldConn, componentId);
    }

    /** 静默关闭 socket：失败只记日志，不影响主流程（socket 为 null 时什么都不做） */
    private static void closeQuietly(Socket socket, String componentId) {
        if (socket == null) {
            return;
        }
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            log.warn("[FINS连接] componentId={} 关闭连接异常：{}", componentId, e.getMessage());
        }
    }
}
