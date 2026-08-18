//由AI修改
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
//        TCPMasterConnection connection = ModbusConnectionManager.connections.get(componentId);
//        if(connection==null||!connection.isConnected()){
//            log.warn("componentId={} 连接不存在或未连接", componentId);
//            return;
//        }
        TCPMasterConnection connection = ModbusConnectionManager.getValidConnection(componentId);
        if(connection == null){
            log.warn("componentId={} 连接不存在或无效", componentId);
            return;
        }
        List<RangeParserUtil.RangeItem> list = RangeParserUtil.parse(message.getRegisterRange());
        if (list.isEmpty()) {
            log.warn("componentId={} 寄存器范围为空：{}", componentId, message.getRegisterRange());
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            RangeParserUtil.RangeItem item = list.get(i);
            try {
                List<Integer> dataList = ModbusDataReader.readHoldingRegisters(connection,message.getSlaveId(),item.getStart(),item.getCount());
                item.setRegisterList(dataList);
            }catch (Exception e){
                log.error("componentId={} 读取寄存器失败，slaveId={}, range={}",
                        componentId, message.getSlaveId(), item.getStart() + "-" + item.getCount(), e);
                //处理恢复后避免脏数据过多
                BlockingQueue<ModbusMessage> queue = ModbusMessageScheduler.messageQueueMap.get(componentId);
                if(queue!=null){
                    queue.removeIf(o->o.getCode().equals(message.getCode())&& o.getSlaveId().equals(message.getSlaveId()));
                    log.info("componentId={} 清理脏数据", componentId);
                }
                throw e;
            }
        }
        JSONObject objectData = new JSONObject();
        objectData.put("deviceSn", message.getDeviceSn());
        objectData.put("slaveId", message.getSlaveId());
        objectData.put("code", message.getCode());
        objectData.put("registerRange", message.getRegisterRange());
        objectData.put("jsonArray", JSONArray.from(list));
        objectData.put("dataType", message.getDataType());
        objectData.put("byteOrder", message.getByteOrder());
        objectData.put("isSigned", message.getIsSigned());
        objectData.put("scale", message.getScale());
        objectData.put("offset", message.getOffset());
        threadPoolTaskExecutor.execute(() -> {
            try {
                WebSocketServer.broadcast("component", componentId, JSONArray.toJSONString(list));
            } catch (Exception e) {
                log.error("componentId={} 广播组件数据失败", componentId, e);
            }
        });
        //调用协议
        String protocolId = ProtocolManager.PROTOCOL_MAP.getOrDefault(componentId, null);
        if (StringUtils.isEmpty(protocolId)) {
            log.debug("componentId={} 未配置协议解析器", componentId);
            return;
        }
        Method method = ProtocolManager.DECODE_METHOD.get(protocolId);
        Object object = ProtocolManager.CLASS_INSTANCE.get(protocolId);
        if (method == null || object == null) {
            log.error("componentId={} 协议解析器未初始化，protocolId={}", componentId, protocolId);
            return;
        }
        try {
//                Object data = method.invoke(object,message.getDeviceSn(),message.getSlaveId(),message.getCode(),message.getRegisterRange(), JSONArray.from(list));
            Object data = method.invoke(object,objectData);
            DecodeMessage decodeMessage = MessageUtils.parseMessage(data);
            if (decodeMessage == null) {
                return;
            }
            threadPoolTaskExecutor.execute(() -> {
                try {
                    WebSocketServer.broadcast("device", decodeMessage.getDeviceSn(),
                            JSONObject.toJSONString(decodeMessage));
                } catch (Exception e) {
                    log.error("deviceSn={} 广播设备数据失败", decodeMessage.getDeviceSn(), e);
                }
            });
            MessageCache.setDeviceLastData(decodeMessage.getDeviceSn(), decodeMessage);
            try {
                eventBus.publish("device.up", new MessageUpEvent(
                        this, "device.up", decodeMessage.getDeviceSn(), decodeMessage));
            } catch (Exception e) {
                log.error("deviceSn={} 发布设备事件失败", decodeMessage.getDeviceSn(), e);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("componentId={} 调用协议解析器失败，protocolId={}", componentId, protocolId, e);
            throw new RuntimeException("协议解析失败", e);
        } catch (Exception e) {
            log.error("componentId={} 协议数据处理异常", componentId, e);
            throw new RuntimeException("协议数据处理失败", e);

        }
    }
}
