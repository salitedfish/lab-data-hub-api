package com.labdatahub.component.fins_tcp;

import lombok.Data;

/**
 * FINS消息实体
 */
@Data
public class FinsMessage {
    // FINS节点地址
    private Integer finsNodeAddress;
    // 设备SN
    private String deviceSn;
    // 指令编码
    private String code;
    // FINS地址范围
    private String addressRange;
    // 读取完暂停时间
    private Integer delayTime;
}
