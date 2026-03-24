package com.labdatahub.component.s7_tcp;

import lombok.Data;

@Data
public class S7Message {
    private String deviceSn;      // 设备SN
    private String code;          // 指令编码
    private Integer dbNumber;     // DB块号
    private Integer startAddress; // 起始字节偏移
    private Integer length;       // 读取长度（字节）
    private Integer delayTime;    // 读取完成后暂停时间（毫秒）
}