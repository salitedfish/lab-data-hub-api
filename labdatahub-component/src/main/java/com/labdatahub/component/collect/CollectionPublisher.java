package com.labdatahub.component.collect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.alibaba.fastjson2.JSONObject;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.event.EventBus;
import com.labdatahub.component.event.MessageUpEvent;
import com.labdatahub.component.message.DecodeMessage;
import com.labdatahub.component.message.MessageCache;
import com.labdatahub.component.message.MessageUtils;
import com.labdatahub.component.protocol.ProtocolManager;
import com.labdatahub.component.sysws.WebSocketServer;

import lombok.extern.slf4j.Slf4j;

/**
 * 采集结果上报：组件频道广播 → 协议解码 → 设备频道广播 → 消息缓存 → device.up 事件
 *
 * <p>与旧链路各 XxxMessageConsumeService 的后半段逐行等价，保证协议 jar、MQTT 出口、
 * WebSocket 前端都拿到同样的东西。
 */
@Slf4j
public final class CollectionPublisher {

    /** 异步广播用线程池 */
    private static volatile ThreadPoolTaskExecutor executor;

    /** 设备数据事件总线 */
    private static volatile EventBus eventBus;

    private CollectionPublisher() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 上报一个点位的读取结果
     *
     * @param adapter 协议适配器
     * @param task 读取任务
     * @param point 点位
     * @param raw 原始值
     * @throws Exception 协议解码异常
     */
    public static void publish(CollectionAdapter adapter, ReadTask task, CollectionPoint point, Object raw)
            throws Exception {
        String componentId = task.getComponentId();
        JSONObject payload = adapter.buildPayload(task, point, raw);
        if (payload == null) {
            // 适配器判断本次跳过（如点位不支持、行/字段越界）
            return;
        }
        broadcastComponent(componentId, adapter.buildComponentPayload(task, point, raw));
        decodeAndPublish(componentId, payload);
    }

    /**
     * 组件频道广播（前端组件调试页）
     *
     * @param componentId 组件ID
     * @param json 内容
     */
    private static void broadcastComponent(String componentId, String json) {
        ThreadPoolTaskExecutor pool = executor();
        if (pool == null) {
            return;
        }
        pool.execute(() -> {
            try {
                WebSocketServer.broadcast("component", componentId, json);
            } catch (Exception e) {
                log.error("componentId={} 广播组件数据失败", componentId, e);
            }
        });
    }

    /**
     * 调用协议解码器并上报设备数据
     *
     * @param componentId 组件ID
     * @param payload 协议要求的 JSON
     * @throws Exception 解码异常
     */
    private static void decodeAndPublish(String componentId, JSONObject payload) throws Exception {
        String protocolId = ProtocolManager.PROTOCOL_MAP.get(componentId);
        if (StringUtils.isEmpty(protocolId)) {
            log.debug("componentId={} 未配置协议解析器", componentId);
            return;
        }
        Method method = ProtocolManager.DECODE_METHOD.get(protocolId);
        Object object = ProtocolManager.CLASS_INSTANCE.get(protocolId);
        if (method == null || object == null) {
            log.error("componentId={} 协议解析器未初始化，protocolId={}", componentId, protocolId);
            return;
        }
        try {
            Object data = method.invoke(object, payload);
            DecodeMessage decodeMessage = MessageUtils.parseMessage(data);
            if (decodeMessage == null) {
                return;
            }
            broadcastDevice(decodeMessage);
            MessageCache.setDeviceLastData(decodeMessage.getDeviceSn(), decodeMessage);
            publishUpEvent(decodeMessage);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("componentId={} 调用协议解析器失败，protocolId={}", componentId, protocolId, e);
            throw new RuntimeException("协议解析失败", e);
        }
    }

    /**
     * 设备频道广播（前端实时数据/物模型）
     *
     * @param decodeMessage 解码结果
     */
    private static void broadcastDevice(DecodeMessage decodeMessage) {
        ThreadPoolTaskExecutor pool = executor();
        if (pool == null) {
            return;
        }
        pool.execute(() -> {
            try {
                WebSocketServer.broadcast("device", decodeMessage.getDeviceSn(),
                        JSONObject.toJSONString(decodeMessage));
            } catch (Exception e) {
                log.error("deviceSn={} 广播设备数据失败", decodeMessage.getDeviceSn(), e);
            }
        });
    }

    /**
     * 发布 device.up 事件（规则引擎/设备联动/落库都挂在这条事件上）
     *
     * @param decodeMessage 解码结果
     */
    private static void publishUpEvent(DecodeMessage decodeMessage) {
        EventBus bus = eventBus();
        if (bus == null) {
            return;
        }
        try {
            bus.publish("device.up", new MessageUpEvent(CollectionPublisher.class, "device.up",
                    decodeMessage.getDeviceSn(), decodeMessage));
        } catch (Exception e) {
            log.error("deviceSn={} 发布设备事件失败", decodeMessage.getDeviceSn(), e);
        }
    }

    private static ThreadPoolTaskExecutor executor() {
        ThreadPoolTaskExecutor cached = executor;
        if (cached != null) {
            return cached;
        }
        try {
            executor = SpringUtils.getBean(ThreadPoolTaskExecutor.class);
            return executor;
        } catch (Exception e) {
            log.warn("[统一采集] 未取到线程池，组件频道广播跳过：{}", e.getMessage());
            return null;
        }
    }

    private static EventBus eventBus() {
        EventBus cached = eventBus;
        if (cached != null) {
            return cached;
        }
        try {
            eventBus = SpringUtils.getBean(EventBus.class);
            return eventBus;
        } catch (Exception e) {
            log.warn("[统一采集] 未取到事件总线，device.up 事件跳过：{}", e.getMessage());
            return null;
        }
    }
}
