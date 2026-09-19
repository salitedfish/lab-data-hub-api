//由AI修改
package com.labdatahub.component.mitsubishi_mc3e_tcp;

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
import java.util.concurrent.locks.ReentrantLock;

/**
 * 三菱 MC 消息消费服务
 */
@Slf4j
@Component
public class MitsubishiMc3eMessageConsumeService implements MitsubishiMc3eMessageConsumeHandler{
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private EventBus eventBus;
    @Override
    public void handle(String componentId, MitsubishiMc3eMessage message) throws Exception {
        Socket connection = MitsubishiMc3eConnectionManager.connections.get(componentId);
        if(connection==null||!connection.isConnected()||connection.isClosed()){
            return;
        }
        List<Integer> dataList = new ArrayList<>();
        // 配置字段空值兜底：areaCode/startAddress/length 任一为空直接跳过，避免自动拆箱 NPE 触发无谓重连
        if (message.getAreaCode() == null || message.getStartAddress() == null || message.getLength() == null) {
            log.warn("componentId={} 读取code={}配置不完整（areaCode/startAddress/length 为空），跳过本次", componentId, message.getCode());
            return;
        }
        // 读与写共用同一个 socket，且 MC 3E 是「发一帧收一帧」的同步问答：不加锁就会和写值/重连
        // 交错，把对方的响应当自己的帧解析。读侧用无界 lock()（采集命脉，不因等写而失败）
        ReentrantLock lock = MitsubishiMc3eConnectionManager.getLock(componentId);
        boolean needReconnect = false;
        lock.lock();
        try {
        	dataList = MitsubishiMc3eDataReader.readMemoryArea(componentId, message.getAreaCode(), message.getStartAddress(), message.getLength());
        }catch (Exception e){
            //处理恢复后避免脏数据过多
            BlockingQueue<MitsubishiMc3eMessage> queue = MitsubishiMc3eMessageScheduler.messageQueueMap.get(componentId);
            if(queue!=null){
                queue.removeIf(o->o.getCode().equals(message.getCode()));
            }
            //读失败强制重连：半开连接本地状态检测不出来，靠读超时触发自愈（与S7的forceReconnect对齐）
            log.warn("componentId={} 读取code={}失败，触发强制重连", componentId, message.getCode());
            needReconnect = true;
            throw e;
        } finally {
            lock.unlock();
            // forceReconnect 必须在 unlock 之后：它内部真建 TCP 连接 + 指数退避 sleep，
            // 持着锁做会把整条采集链路卡住 1+2 秒
            if (needReconnect) {
                MitsubishiMc3eConnectionManager.forceReconnect(componentId);
            }
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
