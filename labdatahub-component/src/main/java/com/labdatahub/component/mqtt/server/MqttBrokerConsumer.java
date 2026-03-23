package com.labdatahub.component.mqtt.server;

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-09-16
 */
@Slf4j
@Component
public class MqttBrokerConsumer {
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private EventBus eventBus;

    public void message(String brokerId, String clientId, String topic, String message) {
        threadPoolTaskExecutor.execute(() -> {
            try {
                handleMessage(brokerId, clientId, topic, message);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 消息处理
     *
     * @param clientId
     * @param topic
     * @param message
     */
    public void handleMessage(String brokerId, String clientId, String topic, String message) throws InvocationTargetException, IllegalAccessException {
    	JSONObject objecotData = new JSONObject();
    	objecotData.put("topic", topic);
    	objecotData.put("message", message);
    	threadPoolTaskExecutor.execute(() -> {
            WebSocketServer.broadcast("component", brokerId, message);
        });
        String protocolId = ProtocolManager.PROTOCOL_MAP.getOrDefault(brokerId, null);
        if (StringUtils.isNotEmpty(protocolId)) {
            Method method = ProtocolManager.DECODE_METHOD.get(protocolId);
            Object object = ProtocolManager.CLASS_INSTANCE.get(protocolId);
            try {
//                Object data = method.invoke(object,topic, message);
                Object data = method.invoke(object, objecotData);
                DecodeMessage decodeMessage = MessageUtils.parseMessage(data);
                if (decodeMessage == null) {
                    return;
                }
                if(StringUtils.isNotEmpty(decodeMessage.getDeviceSn())){
                    MqttBrokerManager.bindDevice(clientId,decodeMessage.getDeviceSn());
                }
                threadPoolTaskExecutor.execute(() -> {
                    WebSocketServer.broadcast("device", decodeMessage.getDeviceSn(), JSONObject.toJSONString(decodeMessage));
                });
                MessageCache.setDeviceLastData(decodeMessage.getDeviceSn(), decodeMessage);
                eventBus.publish("device.up", new MessageUpEvent(this, "device.up", decodeMessage.getDeviceSn(), decodeMessage));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
