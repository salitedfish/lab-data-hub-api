package com.labdatahub.component.fins_tcp;

import lombok.Data;

/**
 * FINS读取配置
 */
@Data
public class FinsReadConfig {
    // FINS节点地址
    private Integer finsNodeAddress;
    // 设备id
    private String deviceSn;
    // 指令编码
    private String code;
    // 地址范围
    private String addressRange;
    // 间隔时间
    private Integer intervalTime;
    // 读取完暂停时间
    private Integer delayTime;
}
