package com.labdatahub.component.rule_engine.mq.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @Description: RabbitMQ连接包装类
 * @Author: labdatahub
 * @CreateTime: 2025-10-20
 */
public class RabbitMQConnection {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConnection.class);

    private final String connectionName;
    private final RabbitMQConfig config;
    private Connection connection;
    private ConnectionStatus status;
    private ConnectionFactory factory;

    public RabbitMQConnection(String connectionName, RabbitMQConfig config) {
        this.connectionName = connectionName;
        this.config = config;
        this.status = new ConnectionStatus(connectionName);
        initializeFactory();
    }

    private void initializeFactory() {
        this.factory = new ConnectionFactory();
        factory.setHost(config.getHost());
        factory.setPort(config.getPort());
        factory.setUsername(config.getUsername());
        factory.setPassword(config.getPassword());
        factory.setVirtualHost(config.getVirtualHost());
        factory.setConnectionTimeout(config.getConnectionTimeout());
        factory.setRequestedHeartbeat(config.getRequestedHeartbeat());
        factory.setNetworkRecoveryInterval(config.getNetworkRecoveryInterval());
        factory.setAutomaticRecoveryEnabled(config.isAutomaticRecoveryEnabled());
    }

    /**
     * 建立连接
     */
    public synchronized boolean connect() {
        try {
            if (connection != null && connection.isOpen()) {
                logger.debug("连接 {} 已经存在且处于打开状态", connectionName);
                return true;
            }

            connection = factory.newConnection();
            status.setConnected(true);
            status.setLastConnectTime(System.currentTimeMillis());
            status.setLastError(null);

            // 添加连接关闭监听器
            connection.addShutdownListener(cause -> {
                status.setConnected(false);
                status.setLastDisconnectTime(System.currentTimeMillis());
                logger.warn("RabbitMQ连接 {} 已关闭: {}", connectionName, cause.getMessage());
            });

            logger.info("RabbitMQ连接 {} 建立成功", connectionName);
            return true;

        } catch (Exception e) {
            status.setConnected(false);
            status.setLastError(e.getMessage());
            status.setLastDisconnectTime(System.currentTimeMillis());
            logger.error("建立RabbitMQ连接 {} 失败", connectionName, e);
            return false;
        }
    }

    /**
     * 重新连接
     */
    public synchronized boolean reconnect() {
        close();
        status.setReconnectCount(status.getReconnectCount() + 1);
        logger.info("尝试第 {} 次重连连接 {}", status.getReconnectCount(), connectionName);
        return connect();
    }

    /**
     * 关闭连接
     */
    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                logger.warn("关闭RabbitMQ连接 {} 时发生异常", connectionName, e);
            } finally {
                connection = null;
                status.setConnected(false);
            }
        }
    }

    /**
     * 创建通道
     */
    public Channel createChannel() throws IOException {
        if (connection != null && connection.isOpen()) {
            return connection.createChannel();
        }
        throw new IOException("连接未就绪，无法创建通道");
    }

    // Getter方法
    public Connection getConnection() {
        return connection;
    }

    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }

    public String getConnectionName() {
        return connectionName;
    }

    public ConnectionStatus getStatus() {
        return status;
    }
}
