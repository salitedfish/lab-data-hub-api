package com.labdatahub.component.rule_engine.mq.rabbitmq;

/**
 * @Description: RabbitMQ连接配置类
 * @Author: labdatahub
 * @CreateTime: 2025-10-20
 */
public class RabbitMQConfig {
    private String host = "localhost";
    private int port = 5672;
    private String username = "guest";
    private String password = "guest";
    private String virtualHost = "/";
    private int connectionTimeout = 30000;
    private int requestedHeartbeat = 60;
    private int networkRecoveryInterval = 5000;
    private boolean automaticRecoveryEnabled = true;

    // 构造器、getter和setter方法
    public RabbitMQConfig() {}

    public RabbitMQConfig(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    // 省略getter和setter方法...
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getVirtualHost() { return virtualHost; }
    public void setVirtualHost(String virtualHost) { this.virtualHost = virtualHost; }

    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }

    public int getRequestedHeartbeat() { return requestedHeartbeat; }
    public void setRequestedHeartbeat(int requestedHeartbeat) { this.requestedHeartbeat = requestedHeartbeat; }

    public int getNetworkRecoveryInterval() { return networkRecoveryInterval; }
    public void setNetworkRecoveryInterval(int networkRecoveryInterval) { this.networkRecoveryInterval = networkRecoveryInterval; }

    public boolean isAutomaticRecoveryEnabled() { return automaticRecoveryEnabled; }
    public void setAutomaticRecoveryEnabled(boolean automaticRecoveryEnabled) { this.automaticRecoveryEnabled = automaticRecoveryEnabled; }
}

