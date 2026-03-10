package com.labdatahub.component.rule_engine.mq.rabbitmq;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ工具类 - 提供便捷的静态方法
 */
public class RabbitMQUtils {
    private static final RabbitMQManager manager = RabbitMQManager.getInstance();
    public static final Map<String,ExchangeConfig> connectionConfig = new HashMap<>();
    /**
     * 初始化连接配置
     */
    public static void initConnection(String connectionName, RabbitMQConfig config) {
        manager.addConnection(connectionName, config);
    }

    /**
     * 初始化连接配置
     */
    public static void createConnection(String connectionName, RabbitMQConfig config,String exchange,String routeKey) {
        manager.addFullConnection(connectionName, config,exchange,routeKey);
    }

    /**
     * 发送消息
     */
    public static boolean sendMessage(String connectionName, String exchange, String routingKey, String message) {
        try {
            Channel channel = manager.getChannel(connectionName);
            if (channel != null) {
                channel.basicPublish(exchange, routingKey, null, message.getBytes());
                return true;
            }
        } catch (Exception e) {
            logger.error("发送消息失败", e);
        }
        return false;
    }

    /**
     * 发送消息
     */
    public static boolean sendMessage(String connectionName, String message) {
        try {
            Channel channel = manager.getChannel(connectionName);
            if (channel != null) {
                ExchangeConfig exchangeConfig = RabbitMQUtils.connectionConfig.get(connectionName);
                channel.basicPublish(exchangeConfig.getExchange(), exchangeConfig.getRouteKey(), null, message.getBytes());
                return true;
            }
        } catch (Exception e) {
            logger.error("发送消息失败", e);
        }
        return false;
    }
    /**
     * 检查连接健康状态
     */
    public static boolean isHealthy(String connectionName) {
        return manager.isConnected(connectionName);
    }

    /**
     * 获取连接状态报告
     */
    public static void printConnectionStatus() {
        Map<String, ConnectionStatus> statusMap = manager.getAllConnectionStatus();
        statusMap.forEach((name, status) -> {
            logger.info("连接 {}: {}", name, status);
        });
    }

    /**
     * 关闭所有连接
     */
    public static void shutdown() {
        manager.shutdown();
    }

    /**
     * 关闭指定连接
     */
    public static void removeConnection(String connectionName){
        manager.removeConnection(connectionName);
    }

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQUtils.class);
}
