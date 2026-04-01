//package com.labdatahub.component.fins_tcp;
//
///**
// * FINS-TCP设备配置信息
// */
//public class FinsDeviceConfig {
//    private String deviceId;       // 设备唯一标识
//    private String ipAddress;      // 设备IP地址
//    private int port = 9600;       // 端口，FINS/TCP默认9600
//    private int finsNodeAddress = 1; // FINS节点地址，默认1
//    private int timeout = 3000;    // 超时时间(毫秒)
//    private int retryCount = 3;    // 重试次数
//    // 客户端FINS节点地址
//    private int clientNodeAddress = 0;
//
//    // 构造函数和getter/setter
//    public FinsDeviceConfig(String deviceId, String ipAddress) {
//        this.deviceId = deviceId;
//        this.ipAddress = ipAddress;
//    }
//    public String getDeviceId() { return deviceId; }
//    public String getIpAddress() { return ipAddress; }
//    public int getPort() { return port; }
//    public void setPort(int port) { this.port = port; }
//    public int getFinsNodeAddress() { return finsNodeAddress; }
//    public void setFinsNodeAddress(int finsNodeAddress) { this.finsNodeAddress = finsNodeAddress; }
//    public int getTimeout() { return timeout; }
//    public void setTimeout(int timeout) { this.timeout = timeout; }
//    public int getRetryCount() { return retryCount; }
//    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
//    public int getClientNodeAddress() { return clientNodeAddress; }
//    public void setClientNodeAddress(int clientNodeAddress) { this.clientNodeAddress = clientNodeAddress; }
//}
