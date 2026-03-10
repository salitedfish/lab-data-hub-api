package com.labdatahub.component.rule_engine.mq.rabbitmq;

/**
 * @Description: 连接状态信息
 * @Author: labdatahub
 * @CreateTime: 2025-10-20
 */
public class ConnectionStatus {
    private String connectionName;
    private boolean isConnected;
    private long lastConnectTime;
    private long lastDisconnectTime;
    private int reconnectCount;
    private String lastError;

    // 构造器、getter和setter方法
    public ConnectionStatus(String connectionName) {
        this.connectionName = connectionName;
        this.isConnected = false;
        this.reconnectCount = 0;
    }

    // 省略getter和setter方法...
    public String getConnectionName() { return connectionName; }
    public boolean isConnected() { return isConnected; }
    public void setConnected(boolean connected) { isConnected = connected; }
    public long getLastConnectTime() { return lastConnectTime; }
    public void setLastConnectTime(long lastConnectTime) { this.lastConnectTime = lastConnectTime; }
    public long getLastDisconnectTime() { return lastDisconnectTime; }
    public void setLastDisconnectTime(long lastDisconnectTime) { this.lastDisconnectTime = lastDisconnectTime; }
    public int getReconnectCount() { return reconnectCount; }
    public void setReconnectCount(int reconnectCount) { this.reconnectCount = reconnectCount; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    @Override
    public String toString() {
        return String.format("ConnectionStatus{name=%s, connected=%s, reconnectCount=%d}",
                connectionName, isConnected, reconnectCount);
    }
}

