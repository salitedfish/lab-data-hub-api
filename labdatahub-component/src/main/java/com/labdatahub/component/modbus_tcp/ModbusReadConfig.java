//由AI修改
package com.labdatahub.component.modbus_tcp;

import lombok.Data;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-12-18
 */
@Data
public class ModbusReadConfig {
    //从机ID
    private Integer slaveId;
    //设备id
    private String deviceSn;
    //指令编码
    private String code;
    //寄存器区间
    private String registerRange;
    //间隔时间
    private Integer intervalTime;
    //读取完暂停时间
    private Integer delayTime;
    //数据类型: int/double/bool/string（来自物模型）
    private String dataType;
    //字节序: big-大端 little-小端
    private String byteOrder;
    //是否有符号: 1-有符号 0-无符号
    private String isSigned;
    //缩放系数
    private Double scale;
    //偏移量
    private Double offset;
}
