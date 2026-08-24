//由AI修改
package com.labdatahub.component.fanuc_focas;

import lombok.Data;

/**
 * FANUC FOCAS2 消息实体
 */
@Data
public class FanucFocasMessage {
    // 设备SN
    private String deviceSn;
    // 指令编码（物模型 identifier）
    private String code;
    // 采集项类型：axis/spindle/feed/mode/status/prgnum/alarm/tcode/macro/timer/pmc
    private String readType;
    // 参数1（轴号/子项/宏变量号等，各 readType 含义不同）
    private Integer param1;
    // 参数2（坐标类型/地址号等，各 readType 含义不同）
    private Integer param2;
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
