//由AI修改
package com.labdatahub.component.mitsubishi_tcp;

import com.alibaba.fastjson2.JSONArray;
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
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * 三菱 MC 消息消费服务
 */
@Slf4j
@Component
public class MitsubishiMessageConsumeService implements MitsubishiMessageConsumeHandler{
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private EventBus eventBus;
    @Override
    public void handle(String componentId, MitsubishiMessage message) throws Exception {
        Socket connection = MitsubishiConnectionManager.connections.get(componentId);
        if(connection==null||!connection.isConnected()||connection.isClosed()){
            return;
        }
        List<Integer> dataList = new ArrayList<>();
        try {
        	dataList = MitsubishiDataReader.readMemoryArea(componentId, message.getAreaCode(), message.getStartAddress(), message.getLength());
        }catch (Exception e){
            //处理恢复后避免脏数据过多
            BlockingQueue<MitsubishiMessage> queue = MitsubishiMessageScheduler.messageQueueMap.get(componentId);
            if(queue!=null){
                queue.removeIf(o->o.getCode().equals(message.getCode()));
            }
            //读失败强制重连：半开连接本地状态检测不出来，靠读超时触发自愈（与S7的forceReconnect对齐）
            log.warn("componentId={} 读取code={}失败，触发强制重连", componentId, message.getCode());
            MitsubishiConnectionManager.forceReconnect(componentId);
            throw e;
        }
        //读取完成，按配置延迟再继续下次读取（单消费者读节奏限制）
        if (message.getDelayTime() != null && message.getDelayTime() > 0) {
            Thread.sleep(message.getDelayTime());
        }
        JSONObject objecotData = new JSONObject();
    	objecotData.put("deviceSn", message.getDeviceSn());
    	objecotData.put("code", message.getCode());
    	objecotData.put("jsonArray", JSONArray.from(dataList));
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
