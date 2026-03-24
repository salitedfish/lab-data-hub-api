package com.labdatahub.component.s7_tcp;

import lombok.Data;

@Data
public class S7DeviceConfig {
    private String deviceId;    // 设备唯一标识
    private String ipAddress;   // PLC IP
    private int port = 102;     // 端口
    private int rack = 0;       // 机架号（S7-1200 通常为0）
    private int slot = 1;       // 槽位号（S7-1200 通常为1）
    private int timeout = 5000; // 超时（毫秒）
    private int retryCount = 3; // 重试次数

}