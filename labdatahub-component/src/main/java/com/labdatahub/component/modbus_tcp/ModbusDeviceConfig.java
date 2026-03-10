package com.labdatahub.component.modbus_tcp;

import java.util.concurrent.TimeUnit;

/**
 * MODBUS-TCP设备配置信息
 */
public class ModbusDeviceConfig {
    private String deviceId;       // 设备唯一标识
    private String ipAddress;      // 设备IP地址
    private int port = 502;        // 端口，默认502
    private int slaveId = 1;       // 从站地址，默认1
    private int timeout = 3000;    // 超时时间(毫秒)
    private int retryCount = 3;    // 重试次数

    // 构造函数和getter/setter省略
    public ModbusDeviceConfig(String deviceId, String ipAddress) {
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
    }

    public String getDeviceId() { return deviceId; }
    public String getIpAddress() { return ipAddress; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public int getSlaveId() { return slaveId; }
    public void setSlaveId(int slaveId) { this.slaveId = slaveId; }
    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
}
