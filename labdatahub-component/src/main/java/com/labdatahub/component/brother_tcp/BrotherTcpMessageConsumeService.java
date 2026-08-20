//由AI修改
package com.labdatahub.component.brother_tcp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

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

/**
 * Brother NC 消息消费服务
 */
@Slf4j
@Component
public class BrotherTcpMessageConsumeService implements BrotherTcpMessageConsumeHandler{
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private EventBus eventBus;
    @Override
    public void handle(String componentId, BrotherTcpMessage message) throws Exception {
        Socket connection = BrotherTcpConnectionManager.connections.get(componentId);
        if(connection==null||!connection.isConnected()||connection.isClosed()){
            return;
        }
        String value = null;
        try {
            // 一次 LOD 读整块数据区，按点位地址（行号.字段序号）取出原始字符串
            List<String[]> rows = BrotherTcpDataReader.readDataArea(componentId, message.getDataArea());
            value = BrotherTcpDataReader.extractValue(rows, message.getRowNumber(), message.getFieldIndex());
        }catch (Exception e){
            //处理恢复后避免脏数据过多
            BlockingQueue<BrotherTcpMessage> queue = BrotherTcpMessageScheduler.messageQueueMap.get(componentId);
            if(queue!=null){
                queue.removeIf(o->o.getCode().equals(message.getCode()));
            }
            throw e;
        }
        if(value == null){
            // 行或字段越界（如机型无该点位），跳过本次
            return;
        }
        JSONObject objecotData = new JSONObject();
        objecotData.put("deviceSn", message.getDeviceSn());
        objecotData.put("code", message.getCode());
        objecotData.put("value", value);
        objecotData.put("dataType", message.getDataType());
        objecotData.put("byteOrder", message.getByteOrder());
        objecotData.put("isSigned", message.getIsSigned());
        objecotData.put("scale", message.getScale());
        objecotData.put("offset", message.getOffset());
        threadPoolTaskExecutor.execute(() -> {
            WebSocketServer.broadcast("component", componentId, objecotData.toJSONString());
        });
        //调用协议
        String protocolId = ProtocolManager.PROTOCOL_MAP.getOrDefault(componentId, null);
        if (StringUtils.isNotEmpty(protocolId)) {
            Method method = ProtocolManager.DECODE_METHOD.get(protocolId);
            Object object = ProtocolManager.CLASS_INSTANCE.get(protocolId);
            try {
                Object data = method.invoke(object,objecotData);
                DecodeMessage decodeMessage = MessageUtils.parseMessage(data);
                if (decodeMessage == null) {
                    return;
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
