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
import com.labdatahub.component.tcp.TCPClientManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
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

    public void message(String componentId,
                        String requestMethod,
                        String requestPath,
                        String contentType,
                        Map<String, String> headers,
                        Map<String, String> queryParams,
                        String requestBody,
                        Map<String, String> formData,
                        String clientIp,
                        Integer clientPort) {
        threadPoolTaskExecutor.execute(() -> {
            try {
                handleMessage(componentId, requestMethod, requestPath, contentType, headers, queryParams, requestBody, formData,clientIp,clientPort);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 消息处理
     *
     * @param componentId
     */
    public HttpResData handleMessage(String componentId,
                                String requestMethod,
                                String requestPath,
                                String contentType,
                                Map<String, String> headers,
                                Map<String, String> queryParams,
                                String requestBody,
                                Map<String, String> formData,
                                String clientIp,
                                Integer clientPort) throws InvocationTargetException, IllegalAccessException {
    	JSONObject objecotData = new JSONObject();
    	objecotData.put("requestMethod", requestMethod);
    	objecotData.put("requestPath", requestPath);
    	objecotData.put("contentType", contentType);
    	objecotData.put("headers", headers);
    	objecotData.put("queryParams", queryParams);
    	objecotData.put("requestBody", requestBody);
    	objecotData.put("formData", formData);
        threadPoolTaskExecutor.execute(() -> {
//            JSONObject data = new JSONObject();
//            data.put("requestMethod", requestMethod);
//            data.put("requestPath", requestPath);
//            data.put("contentType", contentType);
//            data.put("headers", headers);
//            data.put("queryParams", queryParams);
//            data.put("requestBody", requestBody);
//            data.put("formData", formData);
            WebSocketServer.broadcast("component", componentId, objecotData.toJSONString());
        });
        HttpResData resData = new HttpResData();
        String protocolId = ProtocolManager.PROTOCOL_MAP.getOrDefault(componentId, null);
        if (StringUtils.isNotEmpty(protocolId)) {
            Method method = ProtocolManager.DECODE_METHOD.get(protocolId);
            Object object = ProtocolManager.CLASS_INSTANCE.get(protocolId);
            try {
                //Object data = method.invoke(object, requestMethod, requestPath, contentType, headers, queryParams, requestBody, formData);
                Object data = method.invoke(object, objecotData);
                DecodeMessage decodeMessage = MessageUtils.parseMessage(data);
                if (decodeMessage == null) {
                    resData.setHttNeedReply(false);
                    return resData;
                }
                if(StringUtils.isNotEmpty(decodeMessage.getDeviceSn())){
                    HttpClientManager.DEVICE_CLIENT.put(decodeMessage.getDeviceSn(),clientIp+"_"+clientPort);
                }
                resData.setHttNeedReply(decodeMessage.getHttpNeedReply());
                resData.setData(decodeMessage.getHttpReply());
                threadPoolTaskExecutor.execute(() -> {
                    WebSocketServer.broadcast("device", decodeMessage.getDeviceSn(), JSONObject.toJSONString(decodeMessage));
                });
                MessageCache.setDeviceLastData(decodeMessage.getDeviceSn(), decodeMessage);
                eventBus.publish("device.up", new MessageUpEvent(this, "device.up", decodeMessage.getDeviceSn(), decodeMessage));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return resData;
    }
}
