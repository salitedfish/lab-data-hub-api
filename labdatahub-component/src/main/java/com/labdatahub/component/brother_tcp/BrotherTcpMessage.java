//由AI修改
package com.labdatahub.component.brother_tcp;

import lombok.Data;

/**
 * Brother NC 消息实体
 */
@Data
public class BrotherTcpMessage {
    // 设备SN
    private String deviceSn;
    // 指令编码（物模型 identifier）
    private String code;
    // 数据区名（如 PDSP/ALARM/PRD3）
    private String dataArea;
    // 行号（1起，对应数据区点表的行顺序）
    private Integer rowNumber;
    // 字段序号（1起，第1个字段=响应行 Symbol 后的第一个值）
    private Integer fieldIndex;
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
