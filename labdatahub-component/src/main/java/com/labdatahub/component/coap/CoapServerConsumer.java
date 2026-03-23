package com.labdatahub.component.coap;

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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-09-16
 */
@Slf4j
@Component
public class CoapServerConsumer {
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private EventBus eventBus;

    /**
     * @param componentId 组件id
     * @param requestMethod 请求方式
     * @param requestPath 请求路径
     * @param payload 负载
     * @param pathParams 路径参数
     */
    public DecodeMessage message(String componentId, String requestMethod,String requestPath, String payload, List<String> pathParams) throws InvocationTargetException, IllegalAccessException {
        return handleMessage(componentId,requestMethod,requestPath,payload,pathParams);
    }

    /**
     * 消息处理
     * @param componentId
     */
    public DecodeMessage handleMessage(String componentId, String requestMethod,String requestPath, String payload, List<String> pathParams) throws InvocationTargetException, IllegalAccessException {
    	JSONObject objecotData = new JSONObject();
    	objecotData.put("requestMethod",requestMethod);
    	objecotData.put("requestPath",requestPath);
    	objecotData.put("payload",payload);
    	objecotData.put("pathParams",pathParams);
    	threadPoolTaskExecutor.execute(()->{
//            JSONObject data = new JSONObject();
//            data.put("requestMethod",requestMethod);
//            data.put("requestPath",requestPath);
//            data.put("payload",payload);
//            data.put("pathParams",pathParams);
            WebSocketServer.broadcast("component",componentId,objecotData.toJSONString());
        });
        AtomicReference<DecodeMessage> returnMessage = new AtomicReference<>(null);
        String protocolId = ProtocolManager.PROTOCOL_MAP.getOrDefault(componentId,null);
        if(StringUtils.isNotEmpty(protocolId)){
            Method method = ProtocolManager.DECODE_METHOD.get(protocolId);
            Object object = ProtocolManager.CLASS_INSTANCE.get(protocolId);
                try {
//                    Object data = method.invoke(object, requestMethod, requestPath, payload, pathParams);
                	Object data = method.invoke(object, objecotData);
                    DecodeMessage decodeMessage = MessageUtils.parseMessage(data);
                    if (decodeMessage == null) {
                        return returnMessage.get();
                    }
                    threadPoolTaskExecutor.execute(() -> {
                        WebSocketServer.broadcast("device", decodeMessage.getDeviceSn(), JSONObject.toJSONString(decodeMessage));
                    });
                    MessageCache.setDeviceLastData(decodeMessage.getDeviceSn(), decodeMessage);
                    eventBus.publish("device.up", new MessageUpEvent(this, "device.up", decodeMessage.getDeviceSn(), decodeMessage));
                    if (decodeMessage.getCoapIsRecover()) {
                        returnMessage.set(decodeMessage);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
        }
        return returnMessage.get();
    }
}
