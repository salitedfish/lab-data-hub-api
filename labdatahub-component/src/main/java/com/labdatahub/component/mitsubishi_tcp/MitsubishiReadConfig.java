//由AI修改
package com.labdatahub.component.mitsubishi_tcp;

import lombok.Data;

/**
 * 三菱 MC 读取配置
 */
@Data
public class MitsubishiReadConfig {
    // 设备id
    private String deviceSn;
    // 指令编码
    private String code;
    // 软元件代码（D/W/R/ZR/SD 字设备；M/L/B/X/Y/S/SM/F 位设备）
    private Integer areaCode;
    // 起始地址
    private Integer startAddress;
    // 读取数量
    private Integer length;
    // 间隔时间
    private Integer intervalTime;
    // 读取完暂停时间
    private Integer delayTime;
    // 数据类型: int/double/bool/string（来自物模型）
    private String dataType;
    // 字节序: big-大端 little-小端
    private String byteOrder;
    // 是否有符号: 1-有符号 0-无符号
    private String isSigned;
    // 缩放系数
    private Double scale;
    // 偏移量
    private Double offset;
}
