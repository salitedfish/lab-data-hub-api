package com.labdatahub.component.s7_tcp;

import lombok.Data;

@Data
public class S7ReadConfig {
    private Integer dbNumber;       // DB块号
    private String deviceSn;        // 设备SN
    private String code;            // 指令编码
    private Integer startAddress;   // 起始地址（字节偏移）
    private Integer length;         // 读取长度（字节）
    private Integer intervalTime;   // 间隔时间（秒）
    private Integer delayTime;      // 读取后延迟（毫秒）
}