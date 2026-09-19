//由AI修改
package com.labdatahub.component.s7_tcp;


import com.github.s7connector.api.S7Connector;
import com.github.s7connector.api.factory.S7ConnectorFactory;
import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.event.ComponentOnlineNotifier;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
            log.info("[S7连接] componentId={} 首次连接成功 ({}:{})", componentId, config.getIpAddr(), config.getPort());
            // 连接成功，清除离线节流标记（允许后续断连再次通知离线）
            ComponentOnlineNotifier.markOnline(componentId);
        } catch (Exception e) {
        	log.error("[S7连接] componentId={} 首次连接失败: {}", componentId, e.getMessage(), e);
            connections.remove(componentId);
            configMap.remove(componentId);
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
        // 启动消费线程（放 try 外：重复开启组件时 startConsume 抛 IllegalStateException，
        //    应复用已有消费线程，而不是回滚刚建立的有效连接）
        try {
            S7LoopConsumer.startConsume(componentId, SpringUtils.getBean(S7MessageConsumeService.class));
        } catch (IllegalStateException e) {
            log.warn("[S7连接] componentId={} 已存在消费线程，跳过重复启动", componentId);
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
        for (String componentId : configMap.keySet()) {
            checkAndReconnect(componentId);
        }
    }

    private static void checkAndReconnect(String componentId) {
        S7TcpConfig config = configMap.get(componentId);
        if (config == null) return;

        S7Connector connection = connections.get(componentId);
        boolean isValid = connection != null;
        if (!isValid) {
        	log.warn("[S7重连] componentId={} 连接失效，启动重连流程 ({}:{})", componentId, config.getIpAddr(), config.getPort());
        	reconnectWithRetry(componentId, config);
        } else {
            // 连接有效，清除离线节流标记（连接已恢复，允许再次断连时通知离线）
            ComponentOnlineNotifier.markOnline(componentId);
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