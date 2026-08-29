//由AI修改
package com.labdatahub.component.mitsubishi_cnc_tcp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
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
 * 三菱 CNC TCP（MOCHA）消息消费服务
 */
@Slf4j
@Component
public class MitsubishiCncMessageConsumeService implements MitsubishiCncMessageConsumeHandler{
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private EventBus eventBus;
    @Override
    public void handle(String componentId, MitsubishiCncMessage message) throws Exception {
        Socket connection = MitsubishiCncConnectionManager.connections.get(componentId);
        if(connection==null||!connection.isConnected()||connection.isClosed()){
            return;
        }
        String value = null;
        try {
            // 按点位地址（readType + 轴号）读单项原始值
            value = MitsubishiCncDataReader.readPoint(componentId, message.getReadType(), message.getAxisNo());
        }catch (Exception e){
            //处理恢复后避免脏数据过多
            BlockingQueue<MitsubishiCncMessage> queue = MitsubishiCncMessageScheduler.messageQueueMap.get(componentId);
            if(queue!=null){
                queue.removeIf(o->o.getCode().equals(message.getCode()));
            }
            //读失败强制重连：半开连接本地状态检测不出来，靠读超时触发自愈（与S7/MC的forceReconnect对齐）
            log.warn("componentId={} 读取code={}失败，触发强制重连", componentId, message.getCode());
            MitsubishiCncConnectionManager.forceReconnect(componentId);
            throw e;
        }
        if(value == null){
            // 点位不支持或读取失败，跳过本次
            return;
        }
        //读取完成，按配置延迟再继续下次读取（单消费者读节奏限制）
        if (message.getDelayTime() != null && message.getDelayTime() > 0) {
            Thread.sleep(message.getDelayTime());
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
