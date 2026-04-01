package com.labdatahub.component.fins_tcp;

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
 * FINS消息消费服务
 */
@Slf4j
@Component
public class FinsMessageConsumeService implements FinsMessageConsumeHandler{
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private EventBus eventBus;
    @Override
    public void handle(String componentId, FinsMessage message) throws Exception {
        Socket connection = FinsConnectionManager.connections.get(componentId);
        if(connection==null||!connection.isConnected()||connection.isClosed()){
            return;
        }
        List<Integer> dataList = new ArrayList<>();
        try {
        	dataList = FinsDataReader.readMemoryArea(componentId, message.getAreaCode(), message.getStartAddress(), message.getLength());          
        }catch (Exception e){
            //处理恢复后避免脏数据过多
            BlockingQueue<FinsMessage> queue = FinsMessageScheduler.messageQueueMap.get(componentId);
            if(queue!=null){
                queue.removeIf(o->o.getCode().equals(message.getCode()));
            }
            throw e;
        }
//        List<FinsRangeParserUtil.RangeItem> list = FinsRangeParserUtil.parse(message.getAddressRange());
//        if(list.size()>0){
//            for (int i = 0; i < list.size(); i++) {
//                FinsRangeParserUtil.RangeItem item = list.get(i);
//                try {
//                    List<Integer> dataList = FinsDataReader.readMemoryArea(componentId, item.getAreaCode(), item.getStart(), item.getCount());
//                    item.setRegisterList(dataList);
//                }catch (Exception e){
//                    //处理恢复后避免脏数据过多
//                    BlockingQueue<FinsMessage> queue = FinsMessageScheduler.messageQueueMap.get(componentId);
//                    if(queue!=null){
//                        queue.removeIf(o->o.getCode().equals(message.getCode())&& o.getFinsNodeAddress().equals(message.getFinsNodeAddress()));
//                    }
//                    throw e;
//                }
//            }
//        }
        JSONObject objecotData = new JSONObject();
    	objecotData.put("deviceSn", message.getDeviceSn());
    	//objecotData.put("finsNodeAddress", message.getFinsNodeAddress());
    	objecotData.put("code", message.getCode());
    	//objecotData.put("addressRange", message.getAddressRange());
    	objecotData.put("jsonArray", JSONArray.from(dataList));
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
