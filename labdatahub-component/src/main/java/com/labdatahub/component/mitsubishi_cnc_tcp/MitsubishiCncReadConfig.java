//由AI修改
package com.labdatahub.component.mitsubishi_cnc_tcp;

import lombok.Data;

/**
 * 三菱 CNC TCP（MOCHA）读取配置（点位级）
 */
@Data
public class MitsubishiCncReadConfig {
    // 设备id
    private String deviceSn;
    // 指令编码（物模型 identifier）
    private String code;
    // 采集项类型：树根点位键（al/fre/pn/spn/cc/sl1/ss1/tn/stn/po/opt/cut/ct/sv/fv/st/pst/opm/axc；轴点 mechpos/currpos/remapos/cu/sp）
    private String readType;
    // 轴号（1-6，仅轴类点位有效）
    private Integer axisNo;
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
