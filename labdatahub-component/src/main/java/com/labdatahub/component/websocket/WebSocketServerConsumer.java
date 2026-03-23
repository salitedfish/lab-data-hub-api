package com.labdatahub.component.websocket;

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

import static com.labdatahub.component.websocket.WebSocketCache.CLIENT_DEVICE;
import static com.labdatahub.component.websocket.WebSocketCache.DEVICE_CLIENT;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-09-16
 */
@Slf4j
@Component
public class WebSocketServerConsumer {
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private EventBus eventBus;

    public void message(String componentId, Integer wsPort, String wsPath, String message,String channelId) {
        threadPoolTaskExecutor.execute(() -> {
            try {
                handleMessage(componentId, wsPort, wsPath, message,channelId);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 消息处理
     *
     * @param message
     */
    public void handleMessage(String componentId, Integer wsPort, String wsPath, String message,String channelId) throws InvocationTargetException, IllegalAccessException {
    	JSONObject objecotData = new JSONObject();
    	objecotData.put("wsPort", wsPort);
    	objecotData.put("wsPath", wsPath);
    	objecotData.put("message", message);
    	threadPoolTaskExecutor.execute(() -> {
            WebSocketServer.broadcast("component", componentId, message);
        });
        String protocolId = ProtocolManager.PROTOCOL_MAP.getOrDefault(componentId, null);
        if (StringUtils.isNotEmpty(protocolId)) {
            Method method = ProtocolManager.DECODE_METHOD.get(protocolId);
            Object object = ProtocolManager.CLASS_INSTANCE.get(protocolId);
            try {
                //Object data = method.invoke(object,wsPort,wsPath, message);
                Object data = method.invoke(object,objecotData);
                DecodeMessage decodeMessage = MessageUtils.parseMessage(data);
                if (decodeMessage == null) {
                    return;
                }
                if(StringUtils.isNotEmpty(decodeMessage.getDeviceSn())){
                    DEVICE_CLIENT.put(decodeMessage.getDeviceSn(),channelId);
                    WebSocketCache.bindDevice(channelId,decodeMessage.getDeviceSn());
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
