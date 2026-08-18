//由AI修改
package com.labdatahub.component.s7_tcp;

import lombok.Data;

@Data
public class S7ReadConfig {
    private Integer dbNumber;       // DB块号
    private String deviceSn;        // 设备SN
    private String code;            // 指令编码
    private String blockType;    	//块类型
    private Integer startAddress;   // 起始地址（字节偏移）
    private Integer bitOffset;   		// 偏移量
    private Integer length;         // 读取长度（字节）
    private Integer intervalTime;   // 间隔时间（秒）
    private Integer delayTime;      // 读取后延迟（毫秒）
    private String dataType;        // 数据类型: int/double/bool/string（来自物模型）
    private String byteOrder;       // 字节序: big-大端 little-小端
    private String isSigned;        // 是否有符号: 1-有符号 0-无符号
    private Double scale;           // 缩放系数
    private Double offset;          // 偏移量
}