//由AI修改
package com.labdatahub.business.domain;

/**
 * 写值来源常量（方案 4.8.2）
 *
 * <p>落进 {@code labdatahub_point_write_record.source}，供「写值记录」页区分
 * <b>接口写入</b>与<b>页面手动写值</b>。
 *
 * <p>⚠️ <b>由入口标明，不从请求体取。</b> 请求体带的话调用方可自选，
 * 手动写上 {@link #API} 就伪装成外部系统写的 —— 来源是服务端对自己入口的认知，
 * 不是调用方的声明。所以两个 controller 各自传常量：
 * <ul>
 *   <li>{@code OpenApiPointValueController#pointValue} → {@link #API}</li>
 *   <li>{@code LabdatahubDeviceController#pointValue} → {@link #MANUAL}</li>
 * </ul>
 *
 * <p>⚠️ 这<b>不违反「两条入口共用同一个 service，行为不分叉」</b>（方案 4.7.1）：
 * {@code source} 是记账元数据，不参与任何分支判断 —— 走哪个来源都不影响
 * 匹配、编码、下发与错误码。分叉的是「行为」，这里只多记一个字段。
 */
public class PointWriteSource {

    /** 对外接口写入（{@code /openapi/v1/device/pointValue}） */
    public static final String API = "api";

    /** 页面手动写值（{@code /business/device/pointValue}，物模型列表「写值」按钮） */
    public static final String MANUAL = "manual";

    private PointWriteSource() {
        throw new UnsupportedOperationException("该类为常量类，禁止实例化");
    }
}
