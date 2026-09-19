//由AI修改
package com.labdatahub.business.domain;

import java.io.Serializable;

import lombok.Data;

/**
 * 写值元数据：匹配链路（方案 4.3）走完之后，写一个点位所需的全部信息
 *
 * <p>这个类<b>同时是失败响应的载体</b>。方案 4.1 要求「失败响应同样带这些字段
 * （只要失败发生在匹配之后）」——503（连接不可用）时匹配与编码其实都已经算完了，
 * 响应里照样要给出 address / count / registers，调用方才能据此区分
 * 「平台把地址算错了」和「地址对但连不上」。
 *
 * <p>所以匹配过程中每算出一项就往这里填一项，中途失败时
 * {@link PointWriteException} 会把<b>当时已经填好的这一份</b>带出去。
 */
@Data
public class PointWriteMeta implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== 响应回显用 ==========

    /** 服务端生成的追踪标识（不做幂等，见方案 4.6） */
    private String requestId;

    /** 设备编码 */
    private String deviceSn;

    /** 设备名称 */
    private String deviceName;

    /** 网络组件id */
    private String componentId;

    /** 平台实际会去连的地址（取自 other_config，不是 ip_addr 列，见方案 2.8） */
    private String endpoint;

    /** 从站ID */
    private Integer slaveId;

    /** 点位标识 */
    private String code;

    /** 点位名称（冗余落审计用） */
    private String pointName;

    /** 物模型数据类型 */
    private String dataType;

    /** 起始地址：MODBUS=起始寄存器地址；S7=起始字节偏移 */
    private Integer address;

    /** 写入长度：MODBUS=寄存器个数（16 位字）；S7=字节数（DBX 1 / DBW 2 / DBD 4 / DBB=length） */
    private Integer count;

    /** 写功能码（MODBUS 保持寄存器恒为 16；S7 无功能码概念，恒为 null） */
    private Integer writeFunctionCode;

    // ========== 内部流转用（不进响应） ==========

    /** 协议id，反射调 encode 用 */
    private String protocolId;

    /** 网络类型：MODBUS_TCP / S71200_TCP，决定取哪张协议点位表 */
    private String netType;

    /** 网络组件otherConfig 解析出的 IP */
    private String ipAddr;

    /** 网络组件otherConfig 解析出的端口 */
    private Integer port;

    /** 读功能码（01/02/03/04，为空兜底成 03） */
    private String readFunctionCode;

    /** 协议点位配置的原始寄存器范围串（如 "5" / "6-10"，仅 MODBUS 有） */
    private String registerRange;

    // ===== 以下 5 项只有 S7 用，直接映射 labdatahub_s71200_config 的同名字段 =====

    /** 区类型（S7）：DB / M / I / Q */
    private String areaType;

    /** DB 块号（S7；M/I/Q 区不使用，但配置里仍带） */
    private Integer dbNumber;

    /** 块类型（S7）：DBX / DBW / DBD / DBB */
    private String blockType;

    /** 位偏移（S7 仅 DBX 用，0-7） */
    private Integer bitOffset;

    /** 读取长度（S7 仅 DBB 用，字符串所占字节数，含 2 字节长度头） */
    private Integer length;

    // ===== 以下 2 项只有欧姆龙 FINS 用，直接映射 labdatahub_omronfins_config 的同名字段 =====

    /** 存储区码（FINS）：字区如 DM=0x82 / CIO=0xB0，位区如 DM位=0x02 / CIO位=0x30 */
    private Integer areaCode;

    /** 位号（FINS 仅位区用，0-15；字区为 null 表示按字访问） */
    private Integer bitAddress;

    /** 物模型字节序 */
    private String byteOrder;

    /** 物模型有无符号 */
    private String isSigned;

    /** 物模型缩放系数 */
    private Double scale;

    /** 物模型偏移量 */
    private Double offset;
}
