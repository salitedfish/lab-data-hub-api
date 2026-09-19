//由AI修改
package com.labdatahub.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.labdatahub.business.domain.PointWriteRequest;
import com.labdatahub.business.domain.PointWriteSource;
import com.labdatahub.business.service.IOpenApiPointValueService;
import com.labdatahub.common.annotation.Anonymous;
import com.labdatahub.common.core.domain.AjaxResult;

/**
 * 点位写值（对外开放入口）
 *
 * <p>与本项目其它 controller 不同，这个类<b>不 extends BaseController</b>：
 * 它不返回分页、不返回操作日志，只做参数搬运，行为完全由 service 决定
 * （与 {@code LabdatahubDeviceController#pointValue} 共用同一个 service，不分叉）。
 *
 * <p>⚠️ <b>本接口不做鉴权</b>（2026-09-18 明确）：没有 appKey/appSecret、不签名、
 * 不做 nonce 去重、不做时间戳窗口。调用方在内网，接口直接开放。
 *
 * <p>⚠️ {@code @Anonymous} <b>不是鉴权设计，是绕开 RuoYi 自己的 JWT 拦截</b>——
 * {@code SecurityConfig} 是 {@code anyRequest().authenticated()}，不挂这个注解的话
 * 请求会被 Spring Security 拦在外面跳登录页，根本进不到这里。其它服务没有平台账号，
 * 走不了登录，所以只能这么进。
 *
 * <p>⚠️ 后果要拎清楚：<b>接口一经暴露，任何能访问到 8085 端口的服务都能写任意设备的
 * 任意点位</b>，没有调用方身份、没有设备白名单。将来要收紧，加一层凭据校验即可，
 * 接口契约不用改。
 */
@RestController
@RequestMapping("/openapi/v1/device")
public class OpenApiPointValueController {

    @Autowired
    private IOpenApiPointValueService openApiPointValueService;

    /**
     * 写入一个点位的值
     *
     * <p>⚠️ 没有预演开关，<b>每一次调用都真的写设备</b>。
     *
     * @param request 请求体只有 deviceSn / code / value 三个字段
     * @return code=200 写入成功；其余按方案 4.5 的错误码表（400/404/409/422/500/503/504）
     */
    @Anonymous
    @PostMapping("/pointValue")
    public AjaxResult pointValue(@RequestBody(required = false) PointWriteRequest request) {
        // 来源由入口标明（方案 4.8.2），不取请求体——否则调用方可以自称是页面手动写的
        return openApiPointValueService.write(request, PointWriteSource.API);
    }
}
