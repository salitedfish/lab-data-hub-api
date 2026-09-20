//由AI修改
package com.labdatahub.component.s7_tcp;


import com.github.s7connector.api.S7Connector;
import com.github.s7connector.api.factory.S7ConnectorFactory;
import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.event.ComponentOnlineNotifier;
import com.labdatahub.component.utils.ParallelHealthCheck;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class S7ConnectionManager {
    // 存储连接：key = componentId
    public static Map<String, S7Connector> connections = new ConcurrentHashMap<>();
    // 存储配置（用于重连）
    private static final Map<String, S7TcpConfig> configMap = new ConcurrentHashMap<>();

    /**
     * 每个 componentId 一把读写串行锁。
     *
     * <p>一台设备只有一条 TCP 连接，而读跑在消费线程、写从 HTTP 线程进来、重连跑在健康检查线程上，
     * 三方共用同一个 socket。s7connector 2.1 的 {@code S7BaseConnection.read/write} 都是
     * {@code synchronized}（锁的是连接对象自身），但 <b>{@code S7TCPConnection.close()} 不是 synchronized</b>
     * —— 关连接不会被在途的读写挡住，重连能直接把一条正在写的连接掐掉，于是「值到底写进去没有」
     * 就成了未知数（写失败的严重性远高于读丢一帧）。所以库自己的锁不够，必须在<b>这一层</b>
     * 把「取连接 → 读写 → 换连接」整段串起来。
     *
     * <p>取锁策略有意<b>不对称</b>（与 Modbus 一致，见方案 5.4）：
     * 读侧用无界 {@code lock()}（采集命脉，不能因为等写而失败）；
     * 写侧用有界 {@code tryLock(waitMs)}（外部请求，宁可返 503 让调用方重试，也不把 HTTP 线程挂住）。
     */
    private static final Map<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    /**
     * 连续慢读计数：key = componentId，值 = 连续多少次读取耗时超过 {@link #SLOW_READ_THRESHOLD_MS}。
     * 读到一次「快」的就清零。
     *
     * <p>存在的理由见 {@link #SLOW_READ_THRESHOLD_MS} 的注释：s7connector 把连接错误吞了，
     * 「读得慢」是平台侧唯一能观测到连接已死的信号。
     */
    private static final Map<String, AtomicInteger> slowReadCountMap = new ConcurrentHashMap<>();
    // 健康检查调度器
    private static final ScheduledExecutorService healthCheckScheduler = Executors.newScheduledThreadPool(1,
            r -> {
                Thread t = new Thread(r, "s7-connection-health-check");
                t.setDaemon(true);
                return t;
            });
    // 重连配置
    // 健康检查间隔（秒）
    private static final int HEALTH_CHECK_INTERVAL = 10;
    /**
     * 裸 socket 探活超时（毫秒）。
     *
     * <p>这是<b>独立于库之外</b>的判据：只做 TCP 三次握手，成功即认为设备侧还在监听。
     * S7-1200 默认允许 8 条并发 ISO-TCP 连接（可配），探测连接握手完立刻关闭，
     * 不会挤掉正在采集的那条正式连接，所以这里敢建第二条连接（机床类协议如 Brother/三菱 CNC
     * 只服务单条活动连接，不能这么探 —— 它们的健康检查因此只敢看本地状态）。
     */
    private static final int PROBE_TIMEOUT_MS = 1500;
    /**
     * 「读取超时」判定阈值（毫秒）。
     *
     * <p>这是修复「设备停了但平台一直显示在线、还在上报假数据」的<b>核心判据</b>。
     *
     * <p>设备停了（或 Windows 休眠唤醒了）之后，s7connector 2.1 底层 nodave 的
     * {@code PLCinterface.read(byte[],int,int)} 会把 {@code IOException} 吞掉
     * （catch 里只 {@code printStackTrace()}，然后 {@code return 0}），
     * 于是读<b>不抛异常</b>，只把 {@code new byte[n]} 的空缓冲区原样返回，
     * 平台侧看到的是 {@code cpu_load=256 / counter=16777216 / temperature=2.35e-38} 这类
     * 由 {@code 01 00 00 00} 拼出来的假值，一路当正常采集写进 device_logs。
     *
     * <p>唯一还留在平台侧可观测的信号就是<b>耗时</b>：nodave 取响应是
     * {@code while (in.available() <= 0 && timeout < 500) { Thread.sleep(1); timeout++; }} 的等待循环 ——
     * 设备正常时响应在 RTT 内到达，循环几轮就出数据（本机实测 10ms 量级）；
     * 连接死了则 {@code available()} 永远是 0，每次都把 500 轮等满（本机实测约 1 秒）。
     * 450ms 卡在两者中间，留了近 20 倍余量。
     */
    private static final long SLOW_READ_THRESHOLD_MS = 450;
    /**
     * 连续多少次「慢读」才判定连接已死。
     *
     * <p>单次慢读可能只是 PLC 偶发卡顿，不足以判死；连接真断了则每一次都慢，
     * 3 次在本机约 3~4 秒内就能凑齐，远小于健康检查 10 秒的节奏 ——
     * 也就是说最坏情况下假数据最多再流 10 来秒就被掐断。
     */
    private static final int SUSPECT_SLOW_READS = 3;
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000;  // 1秒
    private static final long MAX_RETRY_DELAY_MS = 30000;     // 30秒
    
    // 静态初始化：启动全局连接健康检查（每10秒检查一次，可根据业务调整）
    static {
    	// 首次延迟0秒执行，之后每10秒执行一次健康检查
        healthCheckScheduler.scheduleAtFixedRate(
                S7ConnectionManager::checkAllConnections,
                0,
                HEALTH_CHECK_INTERVAL,
                TimeUnit.SECONDS
        );
        log.info("S7 连接健康检查任务已启动，检查间隔={}秒", HEALTH_CHECK_INTERVAL);
    }

    /**
     * 创建连接
     * @param componentId 组件ID
     * @param config 连接配置
     * @return 是否成功
     */
    public static boolean addConnection(String componentId, S7TcpConfig config) {
        if (componentId == null || config == null || config.getIpAddr() == null) {
        	log.warn("[S7连接] componentId={} 参数非法", componentId);
            return false;
        }
        // 先把消费线程拉起来，与「连接是否成功」解耦：
        //   调用方（LabdatahubComponentServiceImpl 的 S71200_TCP 分支）是「先 addConnection、再
        //   timerTask.initS71200TcpRead()」，也就是无论连接成败，读配置的定时任务都已经开始产消息了；
        //   消费线程不起来，队列只会被灌满到上限、恢复后还要回放一堆过期快照。
        //   连接由健康检查在设备回来后自动重建（见 checkAndReconnect），届时数据即可正常被消费。
        try {
            S7LoopConsumer.startConsume(componentId, SpringUtils.getBean(S7MessageConsumeService.class));
        } catch (IllegalStateException e) {
            log.warn("[S7连接] componentId={} 已存在消费线程，跳过重复启动", componentId);
        }
        S7Connector connection = null;
        // 换连接必须持锁：close() 不受在途读写阻塞（见 lockMap 注释），不串起来会把正在写的连接掐掉
        ReentrantLock lock = getLock(componentId);
        lock.lock();
        try {
        	configMap.put(componentId, config);
            // 关闭旧连接
        	closeOldConnection(componentId);
            // 创建新连接（使用 s7connector）
            connection = buildConnector(config);
            connections.put(componentId, connection);
            slowReadCountMap.remove(componentId);
            log.info("[S7连接] componentId={} 首次连接成功 ({}:{})", componentId, config.getIpAddr(), config.getPort());
            // 连接成功，清除离线节流标记（允许后续断连再次通知离线）
            ComponentOnlineNotifier.markOnline(componentId);
        } catch (Exception e) {
        	log.error("[S7连接] componentId={} 首次连接失败: {}", componentId, e.getMessage(), e);
            connections.remove(componentId);
            // ⚠️ 这里<b>不能</b>把 configMap 里的配置一起删掉：configMap 是健康检查的监控名单
            //    （checkAllConnections 遍历的就是它的 keySet），删掉等于判定「不再重连」。
            //    而「首次连接失败」只是说设备此刻不在线（平台重启时设备正好是停的、或用户点开启时设备没开），
            //    不代表组件不该被监控 —— 删掉之后健康检查再也不会看这个组件，设备回来了也永远接不回去，
            //    只能由管理员在页面上重新点一次开启。配置留在 map 里，健康检查每 10 秒会自动重试、自动恢复。
            //    （组件被真正关闭时走 closeConnection，那里才该清 configMap。）
            // 关闭刚创建的连接，避免失败时连接泄漏
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception closeEx) {
                    log.warn("[S7连接] componentId={} 关闭失败连接异常：{}", componentId, closeEx.getMessage());
                }
            }
            // 首次连接失败视为组件离线，通知设备下线（已离线设备幂等跳过）
            ComponentOnlineNotifier.markOfflineAndNotify(componentId);
            return false;
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * 关闭单个连接
     */
    public static void closeConnection(String componentId) {
        // 关连接要持锁：否则会把在途的写掐掉（见 lockMap 注释）。
        // 只包住「摘连接 + close」这一小段，消费线程的停止放到锁外，避免与消费线程抢锁时互相等
        ReentrantLock lock = getLock(componentId);
        lock.lock();
        try {
            S7Connector connection = connections.remove(componentId);
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    log.error("[S7连接] componentId={} 关闭连接异常", componentId, e);
                }
            }
        } finally {
            lock.unlock();
        }
        configMap.remove(componentId);
        // 组件已关，锁不再有意义；留着会随组件反复开关慢慢涨
        lockMap.remove(componentId);
        slowReadCountMap.remove(componentId);
        S7MessageScheduler.removeMessageQueue(componentId);
        S7LoopConsumer.stopConsume(componentId);
        log.info("[S7连接] componentId={} 连接已关闭，配置已清理", componentId);
    }

    /**
     * 关闭所有连接
     */
    public static void closeAllConnections() {
    	log.info("[S7 连接] 开始关闭所有连接，当前连接数={}", connections.size());
        connections.forEach((id, conn) -> {
            try {
            	conn.close();
            } catch (Exception e) {
            	log.error("[S7连接] componentId={} 关闭连接异常", id, e);
            }
        });
        connections.clear();
        configMap.clear();
        lockMap.clear();
        slowReadCountMap.clear();
        healthCheckScheduler.shutdown();
        try {
            if (!healthCheckScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
            	log.warn("[S7 连接] 健康检查调度器未能在 5 秒内终止，强制关闭");
                healthCheckScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
        	log.error("[S7 连接] 等待健康检查调度器终止时被中断", e);
            healthCheckScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("[S7连接] 所有连接已关闭，健康检查调度器已停止");
    }

    private static void checkAllConnections() {
        // 并发检查：原先单线程串行遍历，设备离线时每台都要等满一次建连超时，
        // 100 台同协议设备同时离线要几分钟才轮完一遍，断线/恢复感知随之失效
        // （详见 ParallelHealthCheck 的类注释）
        ParallelHealthCheck.run("S7", configMap.keySet(), S7ConnectionManager::checkAndReconnect, HEALTH_CHECK_INTERVAL);
    }

    /**
     * 检查单个组件的连接是否还活着，死了就摘掉并重连。
     *
     * <p>判据有三条，任一命中即判死 —— 原实现只判「{@code connection != null}」（纯查表），
     * 等于从来不看设备，所以设备停了平台也能一直显示在线：
     * <ol>
     *   <li>连接对象不存在 —— 首次连接失败或上一轮重连失败；</li>
     *   <li>{@link #isTcpReachable 端口探活不通} —— 设备/模拟器已停，10 秒内就能发现；</li>
     *   <li>{@link #isReadSuspect 读取连续超时} —— 端口还通但会话已半开（休眠唤醒/对端单方面断开），
     *       这条是针对 s7connector 吞异常专门加的。</li>
     * </ol>
     */
    private static void checkAndReconnect(String componentId) {
        S7TcpConfig config = configMap.get(componentId);
        if (config == null) return;

        S7Connector connection = connections.get(componentId);
        String deadReason = null;
        if (connection == null) {
            deadReason = "连接对象不存在";
        } else if (!isTcpReachable(config.getIpAddr(), config.getPort())) {
            deadReason = "端口探活不通";
        } else if (isReadSuspect(componentId)) {
            deadReason = "读取连续超时（会话已半开）";
        }
        if (deadReason == null) {
            // 连接有效，清除离线节流标记（连接已恢复，允许再次断连时通知离线）
            ComponentOnlineNotifier.markOnline(componentId);
            return;
        }

        log.warn("[S7重连] componentId={} 连接失效（{}），摘除连接并重连 ({}:{})",
                componentId, deadReason, config.getIpAddr(), config.getPort());
        // ⚠️ 先摘连接再重连，这一步是「读侧兜底」的关键：消费线程 getConnection 拿到 null 就直接跳过本次，
        //    假数据（01 00 00 00 那类残包）不会再被写进 device_logs。
        //    换成「只重连不摘」的话，重连那几秒（1+2 秒退避）里消费线程仍会读到残包并上报。
        ReentrantLock lock = getLock(componentId);
        lock.lock();
        try {
            closeOldConnection(componentId);
        } finally {
            lock.unlock();
        }
        // 判定依据已用完，清零，让重连后的新连接重新计数
        slowReadCountMap.remove(componentId);
        reconnectWithRetry(componentId, config);
    }

    /**
     * 记录一次读取耗时（由消费线程每次读完调用）。
     *
     * <p>连续 {@link #SUSPECT_SLOW_READS} 次超过 {@link #SLOW_READ_THRESHOLD_MS} 即视为连接已死，
     * 由健康检查线程摘连接并重连；中间只要读到一次快的就清零。
     *
     * @param componentId 组件ID
     * @param costMs 本次读取耗时（毫秒）
     */
    public static void recordReadCost(String componentId, long costMs) {
        AtomicInteger counter = slowReadCountMap.computeIfAbsent(String.valueOf(componentId), k -> new AtomicInteger(0));
        if (costMs >= SLOW_READ_THRESHOLD_MS) {
            counter.incrementAndGet();
        } else {
            counter.set(0);
        }
    }

    /**
     * 读取是否已「连续超时」到可疑程度（见 {@link #SLOW_READ_THRESHOLD_MS}）
     */
    private static boolean isReadSuspect(String componentId) {
        AtomicInteger counter = slowReadCountMap.get(componentId);
        return counter != null && counter.get() >= SUSPECT_SLOW_READS;
    }

    /**
     * 读侧兜底：本次读取若是「慢读」，就替健康检查<b>立刻</b>探活判死。
     *
     * <p>为什么必须由读侧做：健康检查 10 秒才跑一轮，从设备停掉到「摘连接」之间最长有十几秒，
     * 那段时间消费线程手里拿的是一条「还在」的连接，s7connector 吞掉 IOException 后返回空缓冲区，
     * 01 00 00 00 拼出来的假值（{@code cpu_load=256 / counter=16777216}）照常被上报写库。
     * 实测：设备 09:40:07 停，健康检查 09:40:25 才摘连接，中间就多写了一条 {@code cpu_load=256}。
     * 只判「连接对象是否为 null」是堵不住的 —— 那个 null 本身就是健康检查给的。
     *
     * <p>判据只挂在「慢读」上（快读直接返回 false，不花探活的代价）：慢读 → 裸 socket 探一下端口。
     * 探不通就地摘连接 + 通知离线，本次读数作废；探得通说明只是 PLC 偶发卡顿 —— 原样返回 false，
     * 读数照常上报，接真机时不会因为一次慢读丢数据。
     *
     * <p>重连<b>不在这里做</b>：本站跑在消费线程上，退避重连（1+2 秒 sleep）会把整条读链路堵住数秒；
     * 而连接已经摘掉、假数据已经不再产生，重连交给健康检查下一轮（最迟 10 秒）即可。
     *
     * @param componentId 组件ID
     * @param costMs 本次读取耗时（毫秒）
     * @return true = 设备已不在监听，调用方必须丢弃本次读数、不要上报；false = 设备在，读数正常
     */
    public static boolean discardIfDeviceGone(String componentId, long costMs) {
        if (costMs < SLOW_READ_THRESHOLD_MS) {
            return false;
        }
        S7TcpConfig config = configMap.get(componentId);
        if (config == null) {
            // 组件已关（closeConnection 会清 configMap），连接也不在，走既有的「连接不存在」分支
            return false;
        }
        if (isTcpReachable(config.getIpAddr(), config.getPort())) {
            return false;
        }
        log.warn("[S7连接] componentId={} 慢读({}ms)且端口探活不通（{}:{}），判定设备已停，本次读数作废",
                componentId, costMs, config.getIpAddr(), config.getPort());
        // 调用方（消费线程）已持有该组件的读写锁，ReentrantLock 可重入，这里再取一次不会自锁
        ReentrantLock lock = getLock(componentId);
        lock.lock();
        try {
            closeOldConnection(componentId);
        } finally {
            lock.unlock();
        }
        slowReadCountMap.remove(componentId);
        // 不等健康检查那一轮：设备确实没了，离线通知立刻发（节流保证只发一次）
        ComponentOnlineNotifier.markOfflineAndNotify(componentId);
        return true;
    }

    /**
     * 裸 socket 探活：在健康检查线程里对 {@code ip:port} 建一条短超时连接，建完立刻关。
     *
     * <p>不碰 connections 里那条正式连接 —— 探测只验「设备侧还在不在监听」，
     * 所以对采集中的会话零干扰（与 FANUC 健康检查建临时探测连接的做法一致）。
     *
     * @param ip 设备地址
     * @param port 设备端口
     * @return true=能建连（设备在监听）；false=连不上（设备/模拟器已停或网络不通）
     */
    private static boolean isTcpReachable(String ip, int port) {
        if (ip == null || port <= 0) {
            return false;
        }
        Socket probe = new Socket();
        try {
            probe.connect(new InetSocketAddress(ip, port), PROBE_TIMEOUT_MS);
            return true;
        } catch (Exception e) {
            log.warn("[S7探活] {}:{} 探测不通：{}", ip, port, e.getMessage());
            return false;
        } finally {
            try {
                probe.close();
            } catch (Exception ignore) {
                // 探测连接关闭失败没有影响，忽略
            }
        }
    }

    /**
     * 强制重连（读/写异常时由消费端与写值链路调用，修复 PLC 重启/断网后失败不自愈的问题）
     * 先关闭旧连接再走指数退避重连
     *
     * <p>⚠️ 调用方必须在 {@code unlock()} <b>之后</b>调用本站（读写链路都遵守）——建连接是阻塞 IO，
     * 持着锁做会把整条采集链路卡住一个 connect 超时。本站自身只对「摘掉旧连接」这一小段持锁。
     *
     * @param componentId 组件ID
     */
    public static void forceReconnect(String componentId) {
        S7TcpConfig config = configMap.get(componentId);
        if (config == null) {
            log.warn("[S7重连] componentId={} 无连接配置，无法重连", componentId);
            return;
        }
        ReentrantLock lock = getLock(componentId);
        lock.lock();
        try {
            S7Connector connection = connections.get(componentId);
            if (connection != null) {
                try {
                    connection.close();
                    log.info("[S7重连] componentId={} 旧连接已关闭", componentId);
                } catch (Exception e) {
                    log.warn("[S7重连] componentId={} 关闭旧连接异常：{}", componentId, e.getMessage());
                } finally {
                    connections.remove(componentId);
                }
            }
        } finally {
            lock.unlock();
        }
        reconnectWithRetry(componentId, config);
    }

    /**
     * 指数退避重连：每次尝试各自成对持锁（见 {@link #doReconnect}），
     * 退避 sleep 放在锁外 —— 否则一次重连会把读链路卡满 1+2+4 秒。
     */
    private static void reconnectWithRetry(String componentId, S7TcpConfig config) {
        int attempt = 0;
        long delayMs = INITIAL_RETRY_DELAY_MS;
        while (attempt < MAX_RECONNECT_ATTEMPTS) {
            attempt++;
            log.info("[S7重连] componentId={} 第{}次尝试重连", componentId, attempt);
            if (doReconnect(componentId, config)) {
                log.info("[S7重连] componentId={} 重连成功", componentId);
                // 重连成功，清除离线节流标记
                ComponentOnlineNotifier.markOnline(componentId);
                return;
            }
            if (attempt < MAX_RECONNECT_ATTEMPTS) {
                log.warn("[S7重连] componentId={} 第{}次重连失败，{}ms后重试", componentId, attempt, delayMs);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("[S7重连] componentId={} 重连等待被中断", componentId);
                    break;
                }
                delayMs = Math.min(delayMs * 2, MAX_RETRY_DELAY_MS); // 指数退避，上限30秒
            }
        }
        log.error("[S7重连] componentId={} 重连失败，已达到最大重试次数 {}", componentId, MAX_RECONNECT_ATTEMPTS);
        connections.remove(componentId);
        // 重连失败视为组件离线，通知设备下线（节流，仅在在线→离线转变时发一次）
        ComponentOnlineNotifier.markOfflineAndNotify(componentId);
    }

    /**
     * 单次重建：换掉 connections 里的连接对象，必须持锁 ——
     * 不持锁的话「写线程刚取到旧连接 → 重连把旧连接 close 掉」，这次写的结果就无从判定。
     */
    private static boolean doReconnect(String componentId, S7TcpConfig config) {
        ReentrantLock lock = getLock(componentId);
        lock.lock();
        try {
            closeOldConnection(componentId);
            S7Connector newConn = buildConnector(config);
            connections.put(componentId, newConn);
            // 新连接从零开始计慢读次数，否则上一轮攒下的可疑计数会立刻把新连接也判死
            slowReadCountMap.remove(componentId);
            log.info("[S7 重连] componentId={} 重建连接成功", componentId);
            return true;
        } catch (Exception e) {
            log.error("[S7重连] componentId={} 重连尝试失败: {}", componentId, e.getMessage());
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
    public static S7Connector getConnection(String componentId) {
        return connections.get(componentId);
    }

    /**
     * 取该 componentId 的连接配置（供上层拼「连不上 127.0.0.1:102」这类提示语用）
     *
     * @return 配置；组件未开启或开启时连接失败时为 null
     */
    public static S7TcpConfig getConfig(String componentId) {
        return configMap.get(componentId);
    }
    
    private static void closeOldConnection(String componentId) {
    	S7Connector oldConn = connections.get(componentId);
        if (oldConn != null) {
            try {
                oldConn.close();
                log.debug("[S7 连接] componentId={} 旧连接已关闭", componentId);
            } catch (Exception e) {
                log.warn("[S7 连接] componentId={} 关闭旧连接异常：{}", componentId, e.getMessage());
            } finally {
                connections.remove(componentId);
            }
        }
    }
    private static S7Connector buildConnector(S7TcpConfig config) {
        return S7ConnectorFactory.buildTCPConnector()
            	.withHost(config.getIpAddr())
            	.withPort(config.getPort())
            	.withRack(config.getRack())       // rack 机架号，通常为 0,根据实际调整
            	.withSlot(config.getSlot())       // slot 插槽号，S7-1200 通常为 1,根据实际调整
            	.withTimeout(config.getTimeout()) // 连接/读写超时（毫秒），默认5000
            	.build();
    }
}