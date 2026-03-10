package com.labdatahub.component.modbus_tcp;

import lombok.Data;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-12-18
 */
@Data
public class ModbusMessage {
    //从机ID
    private Integer slaveId;
    //设备SN
    private String deviceSn;
    //指令编码
    private String code;
    //寄存器区间
    private String registerRange;
    //读取完暂停时间
    private Integer delayTime;
}
