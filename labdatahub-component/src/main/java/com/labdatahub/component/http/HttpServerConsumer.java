package com.labdatahub.component.http;

import com.alibaba.fastjson2.JSONObject;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.component.event.EventBus;
import com.labdatahub.component.event.MessageUpEvent;
import com.labdatahub.component.message.DecodeMessage;
import com.labdatahub.component.message.MessageCache;
import com.labdatahub.component.message.MessageUtils;
import com.labdatahub.component.protocol.ProtocolManager;
import com.labdatahub.component.sysws.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * HTTP 上报消费者
 *
 * <p>拆成两段，为的是把「报文不合格」回给设备端：
 * <ul>
 *   <li>{@link #decodeAndValidate} —— 解码 + 校验，**在请求线程同步执行**，不通过就抛
 *       {@link HttpIngestException}（此时响应尚未提交，调用方还能写状态码）；</li>
 *   <li>{@link #ingest} / {@link #ingestAsync} —— 入库、进设备缓存、广播 WebSocket、发
 *       {@code device.up} 事件触发规则转发。</li>
 * </ul>
 * 组件配的 {@code needReply=false} 只决定**下游**是否异步，校验一律同步 —— 否则异步分支里
 * 响应早就发出去了，格式错误只能进日志，设备端永远收不到。
 *
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-09-16
 */
@Slf4j
@Component
public class HttpServerConsumer {
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private EventBus eventBus;

    /**
     * 解码 + 校验上报报文（请求线程同步执行）
     *
     * <p>校验内容：组件是否绑了协议、请求体能否被协议 decode 成消息、deviceSn 是否存在。
     * 任何一项不通过都抛 {@link HttpIngestException}，由调用方转成响应回给设备端。
     *
     * @param componentId 组件 ID
     * @param requestBody 请求体原文
     * @return 解析好的上报消息，可直接交给 {@link #ingest}
     * @throws HttpIngestException 报文不合格（400）或平台侧未就绪（500）
     */
    public DecodeMessage decodeAndValidate(String componentId,
                                           String requestMethod,
                                           String requestPath,
                                           String contentType,
                                           Map<String, String> headers,
                                           Map<String, String> queryParams,
                                           String requestBody,
                                           Map<String, String> formData,
                                           String clientIp,
                                           Integer clientPort) {
        JSONObject objecotData = new JSONObject();
        objecotData.put("requestMethod", requestMethod);
        objecotData.put("requestPath", requestPath);
        objecotData.put("contentType", contentType);
        objecotData.put("headers", headers);
        objecotData.put("queryParams", queryParams);
        objecotData.put("requestBody", requestBody);
        objecotData.put("formData", formData);

        // 请求原文广播给组件调试页。放在校验之前 —— 格式不对的上报在调试页里也要能看到，
        // 否则「设备说发了、平台说没收到」时无从对证。
        threadPoolTaskExecutor.execute(() ->
                WebSocketServer.broadcast("component", componentId, objecotData.toJSONString()));

        try {
            String protocolId = ProtocolManager.PROTOCOL_MAP.getOrDefault(componentId, null);
            if (StringUtils.isEmpty(protocolId)) {
                throw new HttpIngestException(500, "组件未绑定协议或协议未注册，平台无法解析上报数据");
            }
            Method method = ProtocolManager.DECODE_METHOD.get(protocolId);
            Object object = ProtocolManager.CLASS_INSTANCE.get(protocolId);
            if (method == null || object == null) {
                throw new HttpIngestException(500, "协议未加载完成，请稍后重试");
            }

            Object data = method.invoke(object, objecotData);
            DecodeMessage decodeMessage = MessageUtils.parseMessage(data);
            if (decodeMessage == null) {
                throw new HttpIngestException(400, "请求体未解析出有效数据");
            }

            // 设备编码缺失（没发 deviceSn / 发空串 / 纯空格）一律拒绝：没有它无法判断数据归属，
            // 放过去只会落到一个空 SN 下面 —— 页面上永远看不到，事后也无从排查。
            if (StringUtils.isEmpty(decodeMessage.getDeviceSn())) {
                throw new HttpIngestException(400, "缺少 deviceSn（设备编码），平台无法判断数据归属");
            }
            return decodeMessage;
        } catch (HttpIngestException e) {
            log.warn("[HTTP上报] 报文被拒绝（{}）：{}，componentId={}，body={}",
                    e.getStatus(), e.getMessage(), componentId, requestBody);
            throw e;
        } catch (Exception e) {
            // 协议 decode 抛出来的（最常见是请求体不是合法 JSON），或反射调用失败
            log.warn("[HTTP上报] 报文解析失败，componentId={}，body={}", componentId, requestBody, e);
            throw new HttpIngestException(400, "请求体解析失败，请检查是否为合法的扁平 JSON 对象");
        }
    }

    /**
     * 入库 + 转发（同步执行）
     *
     * <p>调用方在请求线程上调它就是同步，在 {@link #ingestAsync} 里调就是异步，逻辑同一份。
     *
     * @param decodeMessage 已校验通过的上报消息
     * @param clientIp 调用方 IP（回写连接信息用）
     * @param clientPort 调用方端口（回写连接信息用）
     */
    public void ingest(DecodeMessage decodeMessage, String clientIp, Integer clientPort) {
        HttpClientManager.DEVICE_CLIENT.put(decodeMessage.getDeviceSn(), clientIp + "_" + clientPort);
        threadPoolTaskExecutor.execute(() ->
                WebSocketServer.broadcast("device", decodeMessage.getDeviceSn(), JSONObject.toJSONString(decodeMessage)));
        MessageCache.setDeviceLastData(decodeMessage.getDeviceSn(), decodeMessage);
        eventBus.publish("device.up", new MessageUpEvent(this, "device.up", decodeMessage.getDeviceSn(), decodeMessage));
    }

    /**
     * 入库 + 转发（丢线程池异步执行）
     *
     * <p>组件配 {@code needReply=false} 时走这条：设备端立即拿到响应，不等入库与转发。
     */
    public void ingestAsync(DecodeMessage decodeMessage, String clientIp, Integer clientPort) {
        threadPoolTaskExecutor.execute(() -> ingest(decodeMessage, clientIp, clientPort));
    }
}
