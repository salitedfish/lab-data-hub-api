package com.labdatahub.component.modbus_tcp;

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
import net.wimpi.modbus.net.TCPMasterConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-12-25
 */
@Slf4j
@Component
public class ModbusMessageConsumeService implements ModbusMessageConsumeHandler{
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private EventBus eventBus;
    @Override
    public void handle(String componentId,ModbusMessage message) throws Exception {
        TCPMasterConnection connection = ModbusConnectionManager.connections.get(componentId);
        if(connection==null||!connection.isConnected()){
            return;
        }
        List<RangeParserUtil.RangeItem> list = RangeParserUtil.parse(message.getRegisterRange());
        if(list.size()>0){
            for (int i = 0; i < list.size(); i++) {
                RangeParserUtil.RangeItem item = list.get(i);
                try {
                    List<Integer> dataList = ModbusDataReader.readHoldingRegisters(connection,message.getSlaveId(),item.getStart(),item.getCount());
                    item.setRegisterList(dataList);
                }catch (Exception e){
                    //处理恢复后避免脏数据过多
                    BlockingQueue<ModbusMessage> queue = ModbusMessageScheduler.messageQueueMap.get(componentId);
                    if(queue!=null){
                        queue.removeIf(o->o.getCode().equals(message.getCode())&& o.getSlaveId().equals(message.getSlaveId()));
                    }
                    throw e;
                }
            }
        }
        JSONObject objecotData = new JSONObject();
    	objecotData.put("deviceSn", message.getDeviceSn());
    	objecotData.put("slaveId", message.getSlaveId());
    	objecotData.put("code", message.getCode());
    	objecotData.put("registerRange", message.getRegisterRange());
    	objecotData.put("jsonArray", JSONArray.from(list));
        threadPoolTaskExecutor.execute(() -> {
            WebSocketServer.broadcast("component", componentId, JSONObject.toJSONString(list));
        });
        //调用协议
        String protocolId = ProtocolManager.PROTOCOL_MAP.getOrDefault(componentId, null);
        if (StringUtils.isNotEmpty(protocolId)) {
            Method method = ProtocolManager.DECODE_METHOD.get(protocolId);
            Object object = ProtocolManager.CLASS_INSTANCE.get(protocolId);
            try {
//                Object data = method.invoke(object,message.getDeviceSn(),message.getSlaveId(),message.getCode(),message.getRegisterRange(), JSONArray.from(list));
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
