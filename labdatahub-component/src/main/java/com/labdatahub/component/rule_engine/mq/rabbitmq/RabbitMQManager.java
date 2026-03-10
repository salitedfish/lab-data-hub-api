package com.labdatahub.component.rule_engine.mq.rabbitmq;

/**
 * @Description: RabbitMQ
 * @Author: labdatahub
 * @CreateTime: 2025-10-20
 */
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.spring.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RabbitMQ连接管理器
 * 支持管理多个RabbitMQ连接，提供连接状态监控和自动重连机制
 */
public class RabbitMQManager {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQManager.class);

    private static volatile RabbitMQManager instance;
    private final Map<String, RabbitMQConnection> connections;
    private final ExecutorService monitorExecutor;
    private final AtomicBoolean isRunning;

    private RabbitMQManager() {
        this.connections = new ConcurrentHashMap<>();
        this.monitorExecutor = Executors.newCachedThreadPool();
        this.isRunning = new AtomicBoolean(true);
        startConnectionMonitor();
    }

    public static RabbitMQManager getInstance() {
        if (instance == null) {
            synchronized (RabbitMQManager.class) {
                if (instance == null) {
                    instance = new RabbitMQManager();
                }
            }
        }
        return instance;
    }

    /**
     * 添加RabbitMQ连接配置
     */
    public void addConnection(String connectionName, RabbitMQConfig config) {
        if (connections.containsKey(connectionName)) {
            logger.warn("连接 {} 已存在，将被替换", connectionName);
        }

        RabbitMQConnection rabbitMQConnection = new RabbitMQConnection(connectionName, config);
        connections.put(connectionName, rabbitMQConnection);

        // 尝试初始连接
        try {
            rabbitMQConnection.connect();
            logger.info("RabbitMQ连接 {} 初始化成功", connectionName);
        } catch (Exception e) {
            logger.error("RabbitMQ连接 {} 初始化失败", connectionName, e);
        }
    }

    /**
     * 添加RabbitMQ连接配置
     */
    public void addFullConnection(String connectionName, RabbitMQConfig config,String exchange,String routeKey) {
        if (connections.containsKey(connectionName)) {
            logger.warn("连接 {} 已存在，将被替换", connectionName);
        }

        RabbitMQConnection rabbitMQConnection = new RabbitMQConnection(connectionName, config);
        connections.put(connectionName, rabbitMQConnection);

        // 尝试初始连接
        try {
            rabbitMQConnection.connect();
            ExchangeConfig exchangeConfig = new ExchangeConfig();
            exchangeConfig.setExchange(StringUtils.isEmpty(exchange)?"":exchange);
            exchangeConfig.setRouteKey(StringUtils.isEmpty(routeKey)?"":routeKey);
            RabbitMQUtils.connectionConfig.put(connectionName,exchangeConfig);
            logger.info("RabbitMQ连接 {} 初始化成功", connectionName);
        } catch (Exception e) {
            logger.error("RabbitMQ连接 {} 初始化失败", connectionName, e);
        }
    }

    /**
     * 获取连接
     */
    public Connection getConnection(String connectionName) {
        RabbitMQConnection rabbitMQConnection = connections.get(connectionName);
        return rabbitMQConnection != null ? rabbitMQConnection.getConnection() : null;
    }

    /**
     * 获取通道
     */
    public Channel getChannel(String connectionName) throws IOException {
        RabbitMQConnection rabbitMQConnection = connections.get(connectionName);
        return rabbitMQConnection != null ? rabbitMQConnection.createChannel() : null;
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected(String connectionName) {
        RabbitMQConnection rabbitMQConnection = connections.get(connectionName);
        return rabbitMQConnection != null && rabbitMQConnection.isConnected();
    }

    /**
     * 获取所有连接状态
     */
    public Map<String, ConnectionStatus> getAllConnectionStatus() {
        Map<String, ConnectionStatus> statusMap = new ConcurrentHashMap<>();
        connections.forEach((name, conn) -> {
            statusMap.put(name, conn.getStatus());
        });
        return statusMap;
    }

    /**
     * 手动重连
     */
    public boolean reconnect(String connectionName) {
        RabbitMQConnection rabbitMQConnection = connections.get(connectionName);
        if (rabbitMQConnection != null) {
            return rabbitMQConnection.reconnect();
        }
        return false;
    }

    /**
     * 移除连接
     */
    public void removeConnection(String connectionName) {
        RabbitMQConnection rabbitMQConnection = connections.remove(connectionName);
        if (rabbitMQConnection != null) {
            rabbitMQConnection.close();
            RabbitMQUtils.connectionConfig.remove(connectionName);
            logger.info("RabbitMQ连接 {} 已移除", connectionName);
        }
    }

    /**
     * 启动连接监控
     */
    private void startConnectionMonitor() {
        monitorExecutor.submit(() -> {
            while (isRunning.get()) {
                try {
                    Thread.sleep(30000); // 30秒检查一次

                    for (RabbitMQConnection connection : connections.values()) {
                        if (!connection.isConnected()) {
                            logger.warn("连接 {} 断开，尝试重连...", connection.getConnectionName());
                            connection.reconnect();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("连接监控线程被中断");
                    break;
                } catch (Exception e) {
                    logger.error("连接监控异常", e);
                }
            }
        });
    }

    /**
     * 关闭所有连接
     */
    public void shutdown() {
        isRunning.set(false);
        monitorExecutor.shutdown();

        connections.values().forEach(RabbitMQConnection::close);
        connections.clear();

        logger.info("RabbitMQ管理器已关闭");
    }
}


