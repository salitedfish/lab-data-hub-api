package com.labdatahub.component.s7_tcp;


import com.github.s7connector.api.S7Connector;
import com.github.s7connector.api.factory.S7ConnectorFactory;
import com.labdatahub.common.utils.spring.SpringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class S7ConnectionManager {
    // 存储连接：key = componentId
    public static Map<String, S7Connector> connections = new ConcurrentHashMap<>();
    // 存储配置（用于重连）
    private static final Map<String, S7TcpConfig> configMap = new ConcurrentHashMap<>();
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
        try {
        	configMap.put(componentId, config);
            // 关闭旧连接
        	closeOldConnection(componentId);
            // 创建新连接（使用 s7connector）
            S7Connector connection = buildConnector(config);
            connections.put(componentId, connection);
            log.info("[S7连接] componentId={} 首次连接成功 ({}:{})", componentId, config.getIpAddr(), config.getPort());
            // 启动消费线程
            S7LoopConsumer.startConsume(componentId, SpringUtils.getBean(S7MessageConsumeService.class));
            return true;
        } catch (Exception e) {
        	log.error("[S7连接] componentId={} 首次连接失败: {}", componentId, e.getMessage(), e);
            connections.remove(componentId);
            configMap.remove(componentId);
            return false;
        }
    }

    /**
     * 关闭单个连接
     */
    public static void closeConnection(String componentId) {
        S7Connector connection = connections.remove(componentId);
        if (connection != null) {
            try {
            	connection.close();
            } catch (Exception e) {
                log.error("[S7连接] componentId={} 关闭连接异常", componentId, e);
            }
        }
        configMap.remove(componentId);
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
        }
    }

    private static void reconnectWithRetry(String componentId, S7TcpConfig config) {
        int attempt = 0;
        long delayMs = INITIAL_RETRY_DELAY_MS;
        while (attempt < MAX_RECONNECT_ATTEMPTS) {
            attempt++;
            log.info("[S7重连] componentId={} 第{}次尝试重连", componentId, attempt);
            if (doReconnect(componentId, config)) {
                log.info("[S7重连] componentId={} 重连成功", componentId);
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
    }
    
    private static boolean doReconnect(String componentId, S7TcpConfig config) {
        try {
            closeOldConnection(componentId);
            S7Connector newConn = buildConnector(config);
            connections.put(componentId, newConn);
            log.info("[S7 重连] componentId={} 重建连接成功", componentId);
            return true;
        } catch (Exception e) {
            log.error("[S7重连] componentId={} 重连尝试失败: {}", componentId, e.getMessage());
            return false;
        }
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
            	.build();
    }
    public static void main(String[] args) throws Exception {
    	S7TcpConfig config = new S7TcpConfig();
    	config.setIpAddr("192.168.0.6");
    	config.setPort(102);
    	S7Connector connector = buildConnector(config);
//    	byte[] data = connector.read(DaveArea.DB, 1, 2, 0);
//    	int value = new IntegerConverter().extract(Integer.class, data, 0, 0);
    	System.out.println(S7DataReader.readDB(connector, 1,"DBW", 0, null,null));
    	System.out.println(S7DataReader.readDB(connector, 1,"DBX", 2, null,0));
    	System.out.println(S7DataReader.readDB(connector, 1,"DBD", 260, null,null));
    	System.out.println(S7DataReader.readDB(connector, 1,"DBD", 264, null,null));
    	System.out.println(S7DataReader.readDB(connector, 1,"DBB", 6, 8,null));
	}
}