//由AI修改
package com.labdatahub.business.domain;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 点位写值结果 —— 接口响应体里的 {@code data} 部分
 *
 * <p>{@code @JsonInclude(NON_NULL)} 是必须的，不是洁癖：方案 4.1 的失败响应只给
 * {@code requestId / deviceSn / code} 三个字段（失败发生在匹配之前时连 address 都没有），
 * 而成功响应给二十个。用同一个类承载两种形态，靠的就是「没算出来的字段是 null 就不输出」。
 *
 * <p>注意 {@code registers} 的唯一来源是协议层回传的载荷（{@code modbusWriteJson}），
 * <b>不是「写入的结果」</b>——503（连接不可用）时写根本没发生，响应照样要给出
 * address / count / registers，调用方据此区分「平台把地址算错了」和「地址对但连不上」。
 *
 * <p><b>自解释</b>（2026-10-13 补）：一次调用的响应应当能独立读懂写入目标 ——
 * {@code netType} 说是哪个协议（也决定 address/count/registers 的<b>单位口径</b>：
 * MODBUS=16 位字、S7=字节、FINS/MC=字或软元件），{@code dbNumber}/{@code areaCode} 说地址
 * 落在哪个区，{@code address}/{@code count}/{@code registers} 说从哪写多少、写成了什么；
 * {@code deviceOnline}/{@code componentEnabled} 是<b>平台侧当时的状态快照</b>，
 * 用来解释 503/504 为什么失败。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PointWriteResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 服务端生成的追踪标识（不是幂等键，见方案 4.6） */
    private String requestId;

    /** 设备编码 */
    private String deviceSn;

    /** 设备名称 */
    private String deviceName;

    /** 网络组件id */
    private String componentId;

    /** 平台实际会去连的地址，形如 127.0.0.1:502 */
    private String endpoint;

    /** 从站ID */
    private Integer slaveId;

    /** 点位标识 */
    private String code;

    /** 点位名称（协议点位配置的 name，物模型没配 name 时为空） */
    private String pointName;

    /** 网络类型（MODBUS_TCP / S71200_TCP / OMRONFINS_TCP / MITSUBISHI_MC3E_TCP）——
     *  同时是 address / count / registers 三个字段的单位口径说明 */
    private String netType;

    /** 物模型数据类型 */
    private String dataType;

    /** 物模型单位（如 ℃ / mm），物模型没配就不输出 */
    private String unit;

    /** 起始地址：MODBUS=寄存器地址；S7=起始字节偏移；FINS=起始字地址；MC=软元件地址（X/Y 为八进制写法） */
    private Integer address;

    /** 写入长度：MODBUS=寄存器个数（16 位字）；S7=字节数（DBX 1 / DBW 2 / DBD 4 / DBB=length）；
     *  FINS=项数（字区=字数、位区恒 1）；MC=点数（字设备=字数、位设备恒 1） */
    private Integer count;

    /** DB 块号（仅 S7）：address 是 DB 几里的字节偏移，缺了它 address 读不懂 */
    private Integer dbNumber;

    /** 存储区码（仅 FINS / 三菱 MC）：地址落在哪个区（FINS 字区/位区码、MC 软元件码） */
    private Integer areaCode;

    /** 写功能码：MODBUS 保持寄存器为 16；S7/FINS/MC 无功能码概念，不输出 */
    private Integer writeFunctionCode;

    /** 编码后的写入单元：MODBUS=寄存器字列表（如 "hi" → [26729]）；S7=字节值列表（无符号 0-255）；
     *  FINS/MC=字值列表（0-65535）或位值列表（0/1） */
    private List<Integer> registers;

    /** 本次写值当时，平台侧记录的设备在线状态（与设备列表页同一口径的平台判定，不是「此刻一定连得上」） */
    private Boolean deviceOnline;

    /** 本次写值当时，网络组件是否启用（0-停用 1-启用）；为 false 时 503 的文案会点明「组件未启用」 */
    private Boolean componentEnabled;

    /** 本次调用耗时（毫秒，含匹配 + 编码 + 下发），与审计表 cost_ms 同一个值 */
    private Integer costMs;
}
