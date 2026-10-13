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

            // 耗时只算一次，响应里的 costMs 与审计表的 cost_ms 是同一个值
            int costMs = (int) (System.currentTimeMillis() - start);
            saveRecord(meta, source, value, payload, true, null, null, costMs);
            return AjaxResult.success("写入成功", buildResult(meta, payload, costMs));

        } catch (PointWriteException e) {
            // 400/404/409/422/500：匹配或编码阶段的可预期失败，异常里带着当时那份 meta
            log.warn("[写值] 失败 requestId={} deviceSn={} code={} code={} msg={}",
                    requestId, request == null ? null : request.getDeviceSn(),
                    request == null ? null : request.getCode(), e.getCode(), e.getMessage());
            return fail(e.getMeta(), source, value, payload, e.getCode(), e.getMessage(), start);

        } catch (PointConnectionException e) {
            // 503：平台侧没连上，请求压根没发出去，重试是安全的
            // 走 connectionFailMessage：组件停用时补一句确定的平台侧判定，其余保留协议层原文
            String msg = connectionFailMessage(meta, e.getMessage());
            log.warn("[写值] 连接不可用 requestId={} componentEnabled={} msg={}",
                    requestId, meta == null ? null : meta.getComponentEnabled(), msg);
            return fail(meta, source, value, payload, 503, msg, start);

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
     * 503 的文案细化：把「组件停用」从协议层那句含糊话里择出来
     *
     * <p>协议层（四个 {@code XxxPointWriter} 的 {@code noConnectionMessage}）拿不到
     * {@code labdatahub_component.status}，也不该拿，所以它的兜底文案只能写成
     * 「网络组件未开启，<b>或</b>开启时连接失败」——两种情况混在一句里，调用方只能猜。
     * 而匹配阶段已经把 {@code component.status} 取到了（{@code meta.componentEnabled}），
     * 组件<b>明确停用</b>时就能给一句确定的话。
     *
     * <p>⚠️ 只在明确为 {@code false} 时改写：为 null 或 true 时一律保留协议层原文，
     * 否则「平台繁忙：等待读周期结束超过 5000ms」这类<b>与连接无关</b>的 503
     * 会被套上「组件停用」的错误解释。
     *
     * <p>「连接已断」与「探测超时」的区分<b>做不到</b>：连接层的
     * {@code getValidConnection()} 只返回 null / 连接对象，失败原因在内部就丢了，
     * 要补得改 4~7 个连接管理器。响应里的 {@code componentEnabled} 加上原文里的
     * {@code ip:port} 已够调用方定位。
     */
    private static String connectionFailMessage(PointWriteMeta meta, String rawMsg) {
        if (meta != null && Boolean.FALSE.equals(meta.getComponentEnabled())) {
            return "网络组件当前为停用状态，平台未与设备建立连接，请先在「网络组件」页面开启该组件后重试。协议层详情："
                    + rawMsg;
        }
        return rawMsg;
    }

    /**
     * 失败收口：落审计 + 拼失败响应
     *
     * <p>失败响应同样带 data（只要 data 里有东西），调用方据此区分
     * 「平台把地址算错了」和「地址对但连不上」（方案 4.1）。
     */
    private AjaxResult fail(PointWriteMeta meta, String source, Object value, String payload,
                            int code, String msg, long start) {
        // 耗时只算一次，响应里的 costMs 与审计表的 cost_ms 是同一个值
        int costMs = (int) (System.currentTimeMillis() - start);
        saveRecord(meta, source, value, payload, false, code, msg, costMs);
        AjaxResult ajax = AjaxResult.error(code, msg);
        PointWriteResult result = buildResult(meta, payload, costMs);
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
     * 没算出来的字段自然不输出——这正是同一种结构能同时表达「成功给二十个字段」
     * 和「匹配前失败只给 requestId」的原因。
     *
     * <p>回显分三类：① 写入目标（{@code netType}/{@code address}/{@code count}/
     * {@code dbNumber}/{@code areaCode}/{@code registers}）——其中 address/count/registers
     * 以协议层实际编码出来的载荷为准；② 点位元信息（{@code code}/{@code pointName}/
     * {@code dataType}/{@code unit}）；③ 平台侧状态快照（{@code deviceOnline}/
     * {@code componentEnabled}/{@code costMs}），用来说明失败原因。
     */
    private PointWriteResult buildResult(PointWriteMeta meta, String payload, int costMs) {
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
        result.setPointName(meta.getPointName());
        result.setNetType(meta.getNetType());
        result.setDataType(meta.getDataType());
        result.setUnit(meta.getUnit());
        result.setAddress(meta.getAddress());
        result.setCount(meta.getCount());
        result.setDbNumber(meta.getDbNumber());
        result.setAreaCode(meta.getAreaCode());
        result.setWriteFunctionCode(meta.getWriteFunctionCode());
        // 平台侧状态快照：回显「当时」是什么状态，不是「现在」
        result.setDeviceOnline(meta.getDeviceOnline());
        result.setComponentEnabled(meta.getComponentEnabled());
        result.setCostMs(costMs);

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
                            boolean success, Integer errorCode, String errorMsg, int costMs) {
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
        record.setCostMs(costMs);
        record.setCreateTime(new Date());
        labdatahubPointWriteRecordService.saveRecord(record);
    }
}
