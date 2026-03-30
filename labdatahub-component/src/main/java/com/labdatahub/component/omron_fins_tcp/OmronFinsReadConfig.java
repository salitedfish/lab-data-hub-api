package com.labdatahub.component.omron_fins_tcp;

import lombok.Data;

@Data
public class OmronFinsReadConfig {
    private Integer slaveId;        // 从站ID（节点地址）
    private String deviceSn;        // 设备SN
    private String code;            // 指令编码
    private String registerRange;   // 寄存器区间（如 "1,2-5" 实际表示内存区域+地址）
    private Integer intervalTime;   // 间隔时间（秒）
    private Integer delayTime;      // 读取后暂停时间（毫秒）
    private Integer areaCode;       // 内存区域代码（如0x82=D区，0x80=CIO区）
}