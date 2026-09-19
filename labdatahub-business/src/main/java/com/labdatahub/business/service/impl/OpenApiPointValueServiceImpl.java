//由AI修改
package com.labdatahub.business.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.labdatahub.business.domain.LabdatahubPointWriteRecord;
import com.labdatahub.business.domain.PointWriteException;
import com.labdatahub.business.domain.PointWriteMeta;
import com.labdatahub.business.domain.PointWriteRequest;
import com.labdatahub.business.domain.PointWriteResult;
import com.labdatahub.business.service.ILabdatahubPointWriteRecordService;
import com.labdatahub.business.service.IOpenApiPointValueService;
import com.labdatahub.business.utils.PointWriteMetaResolver;
import com.labdatahub.business.utils.PointWriter;
import com.labdatahub.business.utils.ProtocolEncodeInvoker;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.component.modbus_tcp.RangeParserUtil;
import com.labdatahub.component.protocol.exception.PointConnectionException;
import com.labdatahub.component.protocol.exception.PointDeviceException;

import lombok.extern.slf4j.Slf4j;

/**
 * 点位写值Service业务层处理
 *
 * <p>编排四步（方案 5.1）：匹配 → 编码 → 下发 → 审计。
 * 每一步的失败原因都不同，错误码必须分开报，所以这里不抛异常、直接返回 AjaxResult。
 */
@Slf4j
@Service
public class OpenApiPointValueServiceImpl implements IOpenApiPointValueService {

    @Autowired
    private ILabdatahubPointWriteRecordService labdatahubPointWriteRecordService;

    @Override
    public AjaxResult write(PointWriteRequest request, String source) {
        long start = System.currentTimeMillis();
        // 追踪标识先于匹配生成：失败响应里也要给
        String requestId = IdWorker.get32UUID();
        PointWriteMeta meta = null;
        String payload = null;
        Object value = request == null ? null : request.getValue();

        try {
            if (request == null) {
                PointWriteMeta emptyMeta = new PointWriteMeta();
                emptyMeta.setRequestId(requestId);
                throw new PointWriteException(400, "请求体不能为空", emptyMeta);
            }

            // 1. 匹配链路（方案 4.3）
            meta = PointWriteMetaResolver.resolve(request.getDeviceSn(), request.getCode(), requestId);

            // 2. 编码：组 otherConfig → 反射调协议库 encode → 载荷 JSON
            payload = ProtocolEncodeInvoker.encode(meta, value);

            // 3. 用载荷回填地址与个数。响应里的 address/count/registers 只能从载荷来，
            //    不能从「写入的结果」来 —— 503 时写根本没发生，但响应照样要给这几个字段。
            applyRangeFromPayload(meta, payload);

            // 4. 下发（协议层负责连接自愈、读写串行锁、响应逐项校验）
            PointWriter.write(meta, payload);

            saveRecord(meta, source, value, payload, true, null, null, start);
            return AjaxResult.success("写入成功", buildResult(meta, payload));

        } catch (PointWriteException e) {
            // 400/404/409/422/500：匹配或编码阶段的可预期失败，异常里带着当时那份 meta
            log.warn("[写值] 失败 requestId={} deviceSn={} code={} code={} msg={}",
                    requestId, request == null ? null : request.getDeviceSn(),
                    request == null ? null : request.getCode(), e.getCode(), e.getMessage());
            return fail(e.getMeta(), source, value, payload, e.getCode(), e.getMessage(), start);

        } catch (PointConnectionException e) {
            // 503：平台侧没连上，请求压根没发出去，重试是安全的
            log.warn("[写值] 连接不可用 requestId={} msg={}", requestId, e.getMessage());
            return fail(meta, source, value, payload, 503, e.getMessage(), start);

        } catch (PointDeviceException e) {
            // 504：设备侧异常，语义含「结果不确定」——值可能已经写进去了
            log.warn("[写值] 设备侧异常 requestId={} msg={}", requestId, e.getMessage());
            return fail(meta, source, value, payload, 504, e.getMessage(), start);

        } catch (Exception e) {
            log.error("[写值] 未预期异常 requestId={}", requestId, e);
            return fail(meta, source, value, payload, 500, "写值异常：" + e.getMessage(), start);
        }
    }

    /**
     * 失败收口：落审计 + 拼失败响应
     *
     * <p>失败响应同样带 data（只要 data 里有东西），调用方据此区分
     * 「平台把地址算错了」和「地址对但连不上」（方案 4.1）。
     */
    private AjaxResult fail(PointWriteMeta meta, String source, Object value, String payload,
                            int code, String msg, long start) {
        saveRecord(meta, source, value, payload, false, code, msg, start);
        AjaxResult ajax = AjaxResult.error(code, msg);
        PointWriteResult result = buildResult(meta, payload);
        boolean empty = result.getRequestId() == null && result.getDeviceSn() == null
                && result.getCode() == null && result.getAddress() == null;
        if (!empty) {
            ajax.put(AjaxResult.DATA_TAG, result);
        }
        return ajax;
    }

    /**
     * 从载荷里回填 address / count 到 meta
     *
     * <p>正常情况下这两个值在匹配阶段就从点位配置里算出来了（MODBUS 来自 register_range，
     * S7 来自块类型 + start_address），这里回填的是<b>协议层实际编码出来的</b>那一份——
     * 两者不一致说明配置与编码结果对不上，以实际编码为准才对得起「回显平台实际会写哪儿」这个目的。
     */
    private void applyRangeFromPayload(PointWriteMeta meta, String payload) {
        if (meta == null) {
            return;
        }
        RangeParserUtil.RangeItem rangeItem = ProtocolEncodeInvoker.parseWriteEcho(payload);
        if (rangeItem != null) {
            meta.setAddress(rangeItem.getStart());
            meta.setCount(rangeItem.getCount());
        }
    }

    /**
     * 拼响应体里的 data（成功与失败共用）
     *
     * <p>{@link PointWriteResult} 挂了 {@code @JsonInclude(NON_NULL)}，
     * 没算出来的字段自然不输出——这正是同一种结构能同时表达「成功给十二个字段」
     * 和「匹配前失败只给 requestId」的原因。
     */
    private PointWriteResult buildResult(PointWriteMeta meta, String payload) {
        PointWriteResult result = new PointWriteResult();
        if (meta == null) {
            return result;
        }
        result.setRequestId(meta.getRequestId());
        result.setDeviceSn(meta.getDeviceSn());
        result.setDeviceName(meta.getDeviceName());
        result.setComponentId(meta.getComponentId());
        result.setEndpoint(meta.getEndpoint());
        result.setSlaveId(meta.getSlaveId());
        result.setCode(meta.getCode());
        result.setDataType(meta.getDataType());
        result.setAddress(meta.getAddress());
        result.setCount(meta.getCount());
        result.setWriteFunctionCode(meta.getWriteFunctionCode());

        RangeParserUtil.RangeItem rangeItem = ProtocolEncodeInvoker.parseWriteEcho(payload);
        if (rangeItem != null) {
            result.setAddress(rangeItem.getStart());
            result.setCount(rangeItem.getCount());
            result.setRegisters(rangeItem.getRegisterList());
        }
        return result;
    }

    /**
     * 落审计
     *
     * <p>凡是进到 service 的请求全部记一条，含 404/409/422 这类拒绝——
     * 被拒绝的请求恰恰是排查时最想看的。写入失败不影响接口返回结果
     * （见 {@code LabdatahubPointWriteRecordServiceImpl#saveRecord}）。
     *
     * <p>{@code source} 单独传参而不是从 meta 里取：它是<b>请求级</b>属性，
     * 不是点位属性。匹配失败（如 404）时 meta 只填了一半，从 meta 取会丢来源；
     * 单独传则无论走到哪一步失败都记得住。
     */
    private void saveRecord(PointWriteMeta meta, String source, Object value, String payload,
                            boolean success, Integer errorCode, String errorMsg, long start) {
        LabdatahubPointWriteRecord record = new LabdatahubPointWriteRecord();
        record.setRequestId(meta == null ? null : meta.getRequestId());
        record.setDeviceSn(meta == null ? null : meta.getDeviceSn());
        record.setDeviceName(meta == null ? null : meta.getDeviceName());
        record.setCode(meta == null ? null : meta.getCode());
        record.setPointName(meta == null ? null : meta.getPointName());
        record.setRawValue(value == null ? null : String.valueOf(value));
        record.setSource(source);
        record.setNetType(meta == null ? null : meta.getNetType());
        record.setDataType(meta == null ? null : meta.getDataType());
        record.setAddress(meta == null ? null : meta.getAddress());
        record.setWriteCount(meta == null ? null : meta.getCount());
        record.setPayload(payload);
        record.setIsSuccess(success ? "1" : "0");
        record.setErrorCode(errorCode);
        record.setErrorMsg(StringUtils.substring(errorMsg, 0, 1000));
        record.setCostMs((int) (System.currentTimeMillis() - start));
        record.setCreateTime(new Date());
        labdatahubPointWriteRecordService.saveRecord(record);
    }
}
