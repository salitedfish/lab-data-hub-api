package com.labdatahub.component.omron_fins_tcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OmronFinsDeviceConfig {
    private String deviceId;       // 设备唯一标识
    private String ipAddress;      // PLC IP地址
    private int port = 9600;       // FINS TCP端口
    private int nodeAddress = 0;   // 本地节点地址（0~254）
    private int unitAddress = 0;   // PLC单元地址（0~254）
    private int timeout = 3000;    // 超时时间(毫秒)
    private int retryCount = 3;    // 重试次数
}