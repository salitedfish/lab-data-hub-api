package com.labdatahub.component.omron_fins_tcp;

import lombok.Data;

@Data
public class OmronFinsMessage {
    private Integer slaveId;      // 从站ID（实际上FINS使用节点地址，可复用此字段）
    private String deviceSn;      // 设备SN
    private String code;          // 指令编码
    private String registerRange; // 寄存器区间（如 "1,2-5" 但需映射为FINS地址）
    private Integer delayTime;    // 读取后暂停时间（毫秒）
}