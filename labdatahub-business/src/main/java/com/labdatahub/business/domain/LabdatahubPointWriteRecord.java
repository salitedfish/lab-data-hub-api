//由AI修改
package com.labdatahub.business.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点位外部写入审计对象 labdatahub_point_write_record
 *
 * <p>主键是 int8 自增（对齐 sys_oper_log 这类日志表的做法），所以这里<b>不加
 * {@code @TableId}</b>——与 {@code LabdatahubDeviceLogs} 一致。
 *
 * <p>落库粒度：<b>凡是走到 service 的请求全部记一条</b>，含 404 / 409 / 422 这类拒绝。
 * 被拒绝的请求恰恰是排查时最想看的，接口又没鉴权，不存在「未通过身份校验不该落库」的那类。
 *
 * <p>不做幂等：{@code requestId} 由服务端生成，服务端生成的 ID 天然无法用于幂等
 * （同一请求重发会拿到两个不同 ID，去重不了）。写寄存器是赋值操作，重试写同一个值
 * 结果一致，不需要服务端去重。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_point_write_record")
public class LabdatahubPointWriteRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    private Long id;

    /** 请求id（服务端生成的追踪标识，不做幂等） */
    private String requestId;

    /** 设备sn */
    private String deviceSn;

    /** 设备名称（冗余，设备改名后仍能看清当时写的是哪台） */
    private String deviceName;

    /** 点位标识（对应协议点位配置 code / 物模型 identifier） */
    private String code;

    /** 点位名称（冗余，取协议点位的 name） */
    private String pointName;

    /** 请求写入值（原始字符串，原样落库不转换） */
    private String rawValue;

    /** 写入来源 api-接口写入 manual-页面手动写值（取值见 {@link PointWriteSource}，由入口标明） */
    private String source;

    /** 协议类型（本期恒为 MODBUS_TCP），决定 address/writeCount/payload 三列的口径 */
    private String netType;

    /** 数据类型（物模型 dataType） */
    private String dataType;

    /** 起始地址——MODBUS 寄存器号 */
    private Integer address;

    /** 写入寄存器个数（16位字） */
    private Integer writeCount;

    /** 编码后的载荷 JSON（即 EncodeMessage.modbusWriteJson 的内容） */
    private String payload;

    /** 是否成功 0-失败 1-成功 */
    private String isSuccess;

    /** 失败时的错误码（400/404/409/422/500/503/504） */
    private Integer errorCode;

    /** 失败原因 */
    private String errorMsg;

    /** 耗时毫秒 */
    private Integer costMs;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
