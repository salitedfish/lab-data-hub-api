//由AI修改
package com.labdatahub.business.domain;

import java.io.Serializable;

import lombok.Data;

/**
 * 点位写值请求
 *
 * <p>请求体<b>只有这三个字段</b>，没有鉴权字段（方案 4.2：本接口不做鉴权，
 * 靠 {@code @Anonymous} 绕开 RuoYi 自己的 JWT 拦截）。
 *
 * <p>{@code value} 类型是 {@code Object} 而不是 {@code String}：它按物模型
 * {@code data_type} 解释，可能是字符串、数字或布尔。取用时统一走
 * {@code String.valueOf(value)} 归一成字符串再交给协议层编码（见方案 4.4.1），
 * 所以 {@code value=123} 与 {@code value="123"} 等价。
 */
@Data
public class PointWriteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 设备编码 */
    private String deviceSn;

    /** 点位标识（既是协议点位 code，也是物模型 identifier） */
    private String code;

    /** 写入值，按物模型 dataType 解释；只能是 JDK 标量 */
    private Object value;
}
