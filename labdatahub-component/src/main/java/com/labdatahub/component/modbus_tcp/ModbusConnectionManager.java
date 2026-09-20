//由AI修改
package com.labdatahub.component.modbus_tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.event.ComponentOnlineNotifier;
import com.labdatahub.component.utils.ParallelHealthCheck;

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
    /**
     * 正在重连（已派发、尚未跑完）的 componentId。
     *
     * <p>健康检查每 10 秒会为「连接无效」的组件派发一次重连，黑洞设备上一次建连要等满一个建连超时
     * （秒级），没有这个在途标记的话，上一次还没跑完就又被派发一次，任务会在 reconnectScheduler
     * 的队列里越堆越多。有了它，任何时刻每个组件最多只有一个重连任务在排队或执行。
     */
    private static final Set<String> reconnectInFlight = ConcurrentHashMap.newKeySet();
    /**
     * 最近一次「同步重建」的时刻（毫秒），key 为 componentId。
     *
     * <p>见 {@link #getValidConnection}：那条路径跑在消费线程上、每条消息都会走到，
     * 不限流就是热重连循环。
     */
    private static final Map<String, Long> lastSyncReconnectMs = new ConcurrentHashMap<>();
    /**
     * 最近一次「该组件仍在重连」告警的时刻（毫秒），key 为 componentId。
     *
     * <p>见 {@link #shouldLogRetry}：它和 {@link #reconnectInFlight} 配对 ——
     * 前者压日志量，后者压重连任务量。
     */
    private static final Map<String, Long> lastRetryLogMs = new ConcurrentHashMap<>();
    /** 「仍在重连」告警的节流间隔（毫秒）：一次离线只会在开始时立刻报一条，之后每分钟汇总一条 */
    private static final long RETRY_LOG_INTERVAL_MS = 60000;

    // ========== 可配置参数（可根据业务调整） ==========
    // 健康检查间隔（秒）
    private static final int HEALTH_CHECK_INTERVAL = 10;
    /**
     * 重连线程数。
     *
     * <p>原先只有 1 条线程：100+ 台设备同时离线时，一台一次建连要等满一个建连超时
     * （{@link ModbusTcpConfig#getTimeout()}，典型 3 秒），单线程轮完一遍要 300 秒 ——
     * 最后一台设备要等 5 分钟才被重连一次。与 {@code ParallelHealthCheck} 的并发度对齐取 16。
     */
    private static final int RECONNECT_THREADS = 16;
    /**
     * 两次「同步重建」之间的最小间隔（毫秒）。
     *
     * <p>只用于 {@link #getValidConnection} 那条路径 —— 它跑在消费线程上、每条消息都会调用，
     * 不限流的话设备一离线就变成「每条消息建一次 TCP」的热重连循环（实测 60 秒 34~36 次）。
     * 限流之后，重连的节奏交回给健康检查（每 {@link #HEALTH_CHECK_INTERVAL} 秒一次），
     * 这里只保证「写值 / 读失败现场」这类真的需要一条活的连接的调用方还能同步拿到它。
     */
    private static final long SYNC_RECONNECT_MIN_INTERVAL_MS = 10000;

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
    private static final ScheduledExecutorService reconnectScheduler = Executors.newScheduledThreadPool(
            RECONNECT_THREADS,
            r -> {
                Thread t = new Thread(r, "modbus-reconnect-worker");
                t.setDaemon(true);
                return t;
            }
    );

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
        if (componentId == null || config == null || config.getIpAddr() == null) {
            log.error("[Modbus 连接] componentId={} 参数非法", componentId);
            return false;
        }
        // 先把消费线程拉起来，与「连接是否成功」解耦：
        //   调用方（LabdatahubComponentServiceImpl 的 MODBUS_TCP 分支）是「先 addConnection、再
        //   timerTask.initModbusTcpRead()」，也就是无论连接成败，读配置的定时任务都已经开始产消息了；
        //   消费线程不起来，队列只会被灌满到上限、恢复后还要回放一堆过期快照。
        //   连接由健康检查在设备回来后自动重建（见 checkAndReconnect），届时数据即可正常被消费。
        //   （消费线程对「队列不存在」自带退避重试，见 ModbusLoopConsumer#run 的 QUEUE_MISSING_RETRY_MS）
        try {
            ModbusLoopConsumer.startConsume(componentId, SpringUtils.getBean(ModbusMessageConsumeService.class));
        } catch (IllegalStateException e) {
            log.warn("[Modbus 连接] componentId={} 已存在消费线程，跳过重复启动", componentId);
        }
        TCPMasterConnection connection = null;
        try {
            configMap.put(componentId, config);
            reconnectFailCountMap.put(componentId, new AtomicInteger(0));
            // 组件重新连接，清掉同步重建的节流时刻，让它能立刻同步重建一次而不是先等满 10 秒
            lastSyncReconnectMs.remove(componentId);

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
            // ⚠️ 但 configMap 不能删：它是健康检查的监控名单（checkAllConnections 遍历的就是它的 keySet），
            //    删掉等于判定「不再重连」。而「首次连接失败」只说明 PLC 此刻不在线（平台启动时 PLC 正好停着），
            //    不代表组件不该被监控 —— 删掉之后 PLC 上电了也永远接不回去，只能由管理员在页面上重新点开启。
            //    配置留在 map 里，健康检查会自动重试（组件被真正关闭时走 closeConnection，那里才清 configMap）
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
        reconnectInFlight.remove(componentId);
        lastSyncReconnectMs.remove(componentId);
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
        reconnectInFlight.clear();
        lastSyncReconnectMs.clear();
        lockMap.clear();

        shutdownExecutor(healthCheckScheduler, "健康检查调度器");
        shutdownExecutor(reconnectScheduler, "重连调度器");

        log.info("[Modbus 连接] 所有连接已关闭，调度器已停止");
    }

    /**
     * 检查所有连接状态，失效则自动重连
     */
    private static void checkAllConnections() {
        // 并发检查：原先单线程串行遍历，设备离线时每台都要等满一次建连超时，
        // 100 台同协议设备同时离线要几分钟才轮完一遍，断线/恢复感知随之失效
        // （详见 ParallelHealthCheck 的类注释）
        ParallelHealthCheck.run("Modbus", configMap.keySet(), ModbusConnectionManager::checkAndReconnect, HEALTH_CHECK_INTERVAL);
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
        if (isConnectionValid(connection)) {
            // 连接有效，清除离线节流标记（连接已恢复，允许再次断连时通知离线）
            ComponentOnlineNotifier.markOnline(componentId);
            return;
        }

        // ⚠️ 这里<b>没有</b>「试够 N 次就放弃」。设备可能为了检修停机几小时，放弃等于要求人工
        //    重启组件 —— 对无人值守的产线，那就是「设备回来了平台再也接不回去」。
        //    改为无上限地重试下去，靠健康检查每 10 秒一轮的节奏天然限速（一次只是一个 TCP SYN，代价极低）。
        if (!reconnectInFlight.add(componentId)) {
            // 上一次派发的重连还没跑完（黑洞设备要等满一个建连超时），不重复派发，避免任务在队列里堆积
            return;
        }
        AtomicInteger failCount = reconnectFailCountMap.computeIfAbsent(componentId, k -> new AtomicInteger(0));
        if (shouldLogRetry(componentId)) {
            log.warn("[Modbus 重连] componentId={} 连接失效（已连续失败 {} 次），持续重连中（{}:{}，日志按分钟汇总）",
                    componentId, failCount.get(), config.getIpAddr(), config.getPort());
        }
        try {
            reconnectScheduler.submit(() -> {
                try {
                    reconnect(componentId, config, failCount);
                } finally {
                    // 必须在 finally 里清：任务抛异常也要清，否则这个组件从此再也不会被重连
                    reconnectInFlight.remove(componentId);
                }
            });
        } catch (RejectedExecutionException ree) {
            // 线程池已关闭（组件全关 / 应用退出）：把在途标记还回去，否则组件重开后再也不会被重连
            reconnectInFlight.remove(componentId);
            log.warn("[Modbus 重连] componentId={} 重连任务被拒绝（线程池已关闭）", componentId);
        }
    }

    /**
     * 「该组件仍在离线、仍在重连」这类日志的节流判定（{@link #RETRY_LOG_INTERVAL_MS} 内最多一条）
     *
     * <p>100 台设备同时离线时，每 10 秒一轮的健康检查会变成 10 条/秒的刷屏日志 ——
     * 真正该被看见的那条会被淹掉。按分钟汇总，但每次离线的<b>第一条</b>仍然立刻打出来。
     *
     * @param componentId 组件ID
     * @return true=本次允许打日志（并刷新节流时刻）
     */
    private static boolean shouldLogRetry(String componentId) {
        long now = System.currentTimeMillis();
        Long last = lastRetryLogMs.get(componentId);
        if (last == null || now - last >= RETRY_LOG_INTERVAL_MS) {
            lastRetryLogMs.put(componentId, now);
            return true;
        }
        return false;
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

            // 先探一次端口，再交给 jamod 建连 —— jamod 的 connect() 是 `new Socket(addr, port)`，
            // 没有建连超时（setTimeout 设的是 SO_TIMEOUT，读超时，不是建连超时），碰到
            // 「SYN 被丢进黑洞」的设备（防火墙 DROP / 关机但在别的网段）要等满操作系统的建连
            // 超时（Windows 约 21 秒）。重连线程就那么几条，几台这种设备就能把它们全占满。
            // 端口通≠协议通，所以探针只用来「提前判死」，通了照样走下面的正式建连。
            if (!probeReachable(componentId, config)) {
                throw new IOException("端口不可达（" + config.getIpAddr() + ":" + config.getPort()
                        + "），建连探针超时 " + config.getTimeout() + "ms");
            }

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
            // 恢复在线：清掉告警节流时刻，下次再断连时能立刻报出第一条
            lastRetryLogMs.remove(componentId);
            log.info("[Modbus 重连] componentId={} 重连成功（第{}次尝试，{}:{}）",
                    componentId, retryCount, config.getIpAddr(), config.getPort());
            // 重连成功，清除离线节流标记
            ComponentOnlineNotifier.markOnline(componentId);
            return true;

        } catch (Exception e) {
            int currentFailCount = failCount.get();

            // ⚠️ 这里<b>不能</b>用 newConn.close() 兜底关闭半成品连接：jamod 1.2 的
            //    TCPMasterConnection.close() 实现是「if (m_Connected) { ... }」，而 connect() 里
            //    m_Socket 是在 setTimeout()/prepareTransport() 之前赋的值、m_Connected 是最后才置 true
            //    （已反编译核实）。所以「socket 建出来了但 connect 中途抛异常」时 m_Connected 仍是 false，
            //    close() 整个方法体被跳过，关不掉那条 socket —— 这是 jamod 自身的问题，平台侧无 API 可解。
            //    好在触发条件很窄：new Socket(addr,port) 抛异常时 m_Socket 压根没赋值；
            //    能漏的只有 setTimeout/prepareTransport 抛异常这一种（实际极少发生），故不为此上反射。
            connections.remove(componentId);
            // 重连失败视为组件离线，通知设备下线（节流，仅在在线→离线转变时发一次）
            ComponentOnlineNotifier.markOfflineAndNotify(componentId);

            if (shouldLogRetry(componentId)) {
                log.warn("[Modbus 重连] componentId={} 重连失败（已连续 {} 次），仍会持续重试：{}",
                        componentId, currentFailCount, e.getMessage());
            }

            // ⚠️ 不再在这里自排下一次重连：重连节奏统一由健康检查（每 HEALTH_CHECK_INTERVAL 秒一轮）
            //    驱动。两条链同时排的话，一个组件会同时挂着两条重连链，越滚越多。
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 带建连超时的端口可达性探针（只用来「提前判死」：不通才拦，通不代表协议通）
     *
     * <p>存在的唯一理由：jamod 的 {@code TCPMasterConnection.connect()} 内部是
     * {@code new Socket(addr, port)}，没有建连超时（它自己的 {@code setTimeout()} 设的是 SO_TIMEOUT，
     * 是读超时不是建连超时）。碰到 SYN 被丢弃的设备，它会一直等到操作系统的建连超时
     * （Windows 约 21 秒）才返回；而 jamod 把 socket 和 m_Connected 都封成 private 且没有
     * {@code setSocket}，没法把一条带超时的 socket 塞进去，所以只能在它外面先探一次。
     *
     * @param componentId 组件ID（仅用于日志）
     * @param config      连接配置
     * @return true=端口可达（继续走正式建连）；false=不可达（直接判定本次重连失败）
     */
    private static boolean probeReachable(String componentId, ModbusTcpConfig config) {
        int timeout = config.getTimeout();
        if (timeout <= 0) {
            return true; // 没配超时就不探，维持原行为
        }
        try (Socket probe = new Socket()) {
            probe.connect(new InetSocketAddress(config.getIpAddr(), config.getPort()), timeout);
            return true;
        } catch (IOException e) {
            // 含 UnknownHostException / ConnectException / SocketTimeoutException，一律按不可达处理
            log.debug("[Modbus 重连] componentId={} 端口探针未通过：{}", componentId, e.getMessage());
            return false;
        }
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
        if (isConnectionValid(conn)) {
            return conn;
        }
        ModbusTcpConfig config = configMap.get(componentId);
        if (config == null) {
            return null; // 组件已关闭
        }
        // ⚠️ 这里必须限流。本方法跑在消费线程上、<b>每一条消息</b>都会调用它：设备一离线，
        //    不限流就是「每条消息建一次 TCP」的热重连循环 —— 实测 60 秒里重连了 34~36 次，
        //    每次还带 3 行日志；黑洞设备更糟，jamod 没有建连超时，一次能把消费线程按 21 秒。
        //    限流之后这里只服务「确实需要一条活连接的现场」（写值、读失败后自愈），
        //    常规恢复交给健康检查驱动；被限流时立刻返回 null，调用方直接跳过这一条消息。
        long now = System.currentTimeMillis();
        Long last = lastSyncReconnectMs.get(componentId);
        if (last != null && now - last < SYNC_RECONNECT_MIN_INTERVAL_MS) {
            return null;
        }
        // 健康检查已经派发了重连任务在跑，这里不再插一脚 —— 否则同一个组件会有两条重连链在互相拆连接
        if (reconnectInFlight.contains(componentId)) {
            return null;
        }
        lastSyncReconnectMs.put(componentId, now);
        // debug 级：这是采集链路的常规自愈动作（每台设备每 10 秒最多一次），不是需要运维介入的事件 ——
        // 真正的告警由健康检查那条「连接失效…持续重连中」按分钟出，两条都放 warn 就是双份刷屏。
        log.debug("[Modbus 连接] componentId={} 连接无效，同步重建一次（该路径按 {}ms 限流）",
                componentId, SYNC_RECONNECT_MIN_INTERVAL_MS);
        // 同步重建一次，避免调用方拿不到连接（写值链路依赖这个「同步拿到或明确失败」的语义）
        AtomicInteger failCount = reconnectFailCountMap.computeIfAbsent(componentId, k -> new AtomicInteger(0));
        reconnect(componentId, config, failCount);
        return connections.get(componentId);
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

    /**
     * 强制重连并返回新连接（保留的方法签名，当前无任何调用方 —— 唯一那处引用在
     * {@code ModbusDataReader} 里是注释掉的旧代码）。
     *
     * <p>⚠️ 原实现自己 {@code new TCPMasterConnection(...).connect()}，绕开了
     * {@link #reconnect} 做的三件事：建连端口探针（jamod 的 connect() 没有建连超时，
     * 黑洞设备会把调用线程按满一个操作系统建连超时）、失败计数与在线/离线通知、日志节流。
     * 既然没有调用方，就让它直接走 {@link #reconnect}，免得日后有人把那行注释取消掉，
     * 把热重连这套老问题再带回来。
     */
    public static TCPMasterConnection renewConnection(String componentId) {
        ModbusTcpConfig config = configMap.get(componentId);
        if (config == null) {
            return null;
        }
        AtomicInteger failCount = reconnectFailCountMap.computeIfAbsent(componentId, k -> new AtomicInteger(0));
        reconnect(componentId, config, failCount);
        return connections.get(componentId);
    }
}
