//由AI修改
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
        // 配置不完整（areaCode/startAddress/length 任一为空）时跳过本次，避免 Integer 拆箱 NPE 被误判为传输层异常触发无谓重连
        if (message.getAreaCode() == null || message.getStartAddress() == null || message.getLength() == null) {
            log.warn("componentId={} 读取code={}配置不完整（areaCode/startAddress/length 为空），跳过本次", componentId, message.getCode());
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
            if (e instanceof FinsResponseException) {
                // FINS 协议层错误（结束码非0/响应格式异常）：PLC已正常响应，说明连接是好的，
                // 只是命令被拒绝（区码/地址/数量配置错误），只记日志、不触发强制重连，避免连接抖动
                log.warn("componentId={} 读取code={}被PLC拒绝（FINS错误）：{}", componentId, message.getCode(), e.getMessage());
            } else {
                //传输层异常（超时/断流/EOF）：半开连接本地状态检测不出来，靠读超时触发自愈（与S7的forceReconnect对齐）
                log.warn("componentId={} 读取code={}失败，触发强制重连", componentId, message.getCode());
                FinsConnectionManager.forceReconnect(componentId);
            }
            throw e;
        }
        //读取完成，按配置延迟再继续下次读取（单消费者读节奏限制）
        if (message.getDelayTime() != null && message.getDelayTime() > 0) {
            Thread.sleep(message.getDelayTime());
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
