//由AI修改
package com.labdatahub.business.service;

import com.labdatahub.business.domain.PointWriteRequest;
import com.labdatahub.business.domain.PointWriteSource;
import com.labdatahub.common.core.domain.AjaxResult;

/**
 * 点位写值Service接口
 *
 * <p><b>两条入口共用这一个 service，行为不分叉</b>（方案 4.7.1）：
 * <ul>
 *   <li>{@code OpenApiPointValueController} —— {@code /openapi/v1/device/pointValue}，{@code @Anonymous}（绕 JWT，不是鉴权）</li>
 *   <li>{@code LabdatahubDeviceController#pointValue} —— {@code /business/device/pointValue}，走平台 JWT</li>
 * </ul>
 * 所以校验、匹配、编码、下发、审计全在这里，controller 只做参数搬运。
 */
public interface IOpenApiPointValueService {

    /**
     * 写入一个点位的值
     *
     * <p>⚠️ <b>没有预演开关，每一次调用都真的写设备。</b>
     *
     * <p>⚠️ 返回 {@link AjaxResult} 而不是抛异常：失败要区分
     * 400/404/409/422/500/503/504 七种码并各自带 msg，用异常穿透到全局异常处理器
     * 会把码值压成一个统一的 500。两条入口拿到后原样返回即可。
     *
     * @param request 写值请求（deviceSn / code / value）
     * @param source  写入来源，取 {@link PointWriteSource} 的常量（{@code api} / {@code manual}），
     *                由入口标明并落进审计表，供「写值记录」页区分。
     *                <b>不参与任何分支判断</b>——两条入口的行为完全一致（方案 4.8.2）
     * @return {@code code=200} 表示写入成功；其余按方案 4.5 的错误码表。
     *         失败时 {@code data} 里会带上「失败前已经匹配出来的」字段（方案 4.1）
     */
    AjaxResult write(PointWriteRequest request, String source);
}
