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
 * 而成功响应给十二个。用同一个类承载两种形态，靠的就是「没算出来的字段是 null 就不输出」。
 *
 * <p>注意 {@code registers} 的唯一来源是协议层回传的载荷（{@code modbusWriteJson}），
 * <b>不是「写入的结果」</b>——503（连接不可用）时写根本没发生，响应照样要给出
 * address / count / registers，调用方据此区分「平台把地址算错了」和「地址对但连不上」。
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

    /** 物模型数据类型 */
    private String dataType;

    /** 起始地址：MODBUS=寄存器地址；S7=起始字节偏移 */
    private Integer address;

    /** 写入长度：MODBUS=寄存器个数（16 位字）；S7=字节数（DBX 1 / DBW 2 / DBD 4 / DBB=length） */
    private Integer count;

    /** 写功能码：MODBUS 保持寄存器为 16；S7 无功能码概念，不输出 */
    private Integer writeFunctionCode;

    /** 编码后的写入单元：MODBUS=寄存器字列表（如 "hi" → [26729]）；S7=字节值列表（无符号 0-255） */
    private List<Integer> registers;
}
