package com.labdatahub.component.s7_tcp;

import com.alibaba.fastjson2.JSONObject;
import com.github.s7connector.api.S7Connector;
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

@Slf4j
@Component
public class S7MessageConsumeService implements S7MessageConsumeHandler {

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private EventBus eventBus;

    @Override
    public void handle(String componentId, S7Message message) throws Exception {
        S7Connector connector = S7ConnectionManager.connections.get(componentId);
        if (connector == null) {
            return;
        }

        // 读取 DB 数据
        byte[] rawData = S7DataReader.readDB(connector, message.getDbNumber(), message.getStartAddress(), message.getLength());
        
        //String rawData2 = S7DataReader.readDB(connector, message.getDbNumber(), message.getStartAddress(), message.getLength(),String.class);

        // 构建结果 JSON
        JSONObject result = new JSONObject();
        result.put("deviceSn", message.getDeviceSn());
        result.put("code", message.getCode());
        result.put("dbNumber", message.getDbNumber());
        result.put("startAddress", message.getStartAddress());
        result.put("length", message.getLength());
        result.put("data", rawData); // 可按需转换为 hex 或数值列表

        // 广播到 WebSocket
        threadPoolTaskExecutor.execute(() -> {
            WebSocketServer.broadcast("component", componentId, result.toJSONString());
        });

        // 协议解析（若需要）
        String protocolId = ProtocolManager.PROTOCOL_MAP.getOrDefault(componentId, null);
        if (StringUtils.isNotEmpty(protocolId)) {
            Method method = ProtocolManager.DECODE_METHOD.get(protocolId);
            Object instance = ProtocolManager.CLASS_INSTANCE.get(protocolId);
            try {
                Object data = method.invoke(instance, result); // 传递整个 result
                DecodeMessage decodeMessage = MessageUtils.parseMessage(data);
                if (decodeMessage != null) {
                    threadPoolTaskExecutor.execute(() -> {
                        WebSocketServer.broadcast("device", decodeMessage.getDeviceSn(), JSONObject.toJSONString(decodeMessage));
                    });
                    MessageCache.setDeviceLastData(decodeMessage.getDeviceSn(), decodeMessage);
                    eventBus.publish("device.up", new MessageUpEvent(this, "device.up", decodeMessage.getDeviceSn(), decodeMessage));
                }
            } catch (Exception e) {
                log.error("协议解析失败", e);
            }
        }
    }
}