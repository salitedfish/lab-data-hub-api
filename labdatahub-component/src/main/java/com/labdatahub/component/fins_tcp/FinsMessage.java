//由AI修改
package com.labdatahub.component.fins_tcp;

import lombok.Data;

/**
 * FINS消息实体
 */
@Data
public class FinsMessage {
//    // FINS节点地址
//    private Integer finsNodeAddress;
    // 设备SN
    private String deviceSn;
    // 指令编码
    private String code;
    // 存储区代码：字区如 DM=0x82 / CIO=0xB0，位区如 DM位=0x02 / CIO位=0x30
    private Integer areaCode;
    // 起始字地址（位区下这是「字地址」，位号另看 bitAddress）
    private Integer startAddress;
    // 位号（仅位区用，0-15；字区为 null 表示按字访问）
    private Integer bitAddress;
    // 读取数量：字区=字个数，位区=位个数
    private Integer length;
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
