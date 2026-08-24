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
//        TCPMasterConnection connection = ModbusConnectionManager.connections.get(componentId);
//        if(connection==null||!connection.isConnected()){
//            log.warn("componentId={} 连接不存在或未连接", componentId);
//            return;
//        }
        List<RangeParserUtil.RangeItem> list = RangeParserUtil.parse(message.getRegisterRange());
        if (list.isEmpty()) {
            log.warn("componentId={} 寄存器范围为空：{}", componentId, message.getRegisterRange());
            return;
        }
        //功能码: 01线圈 02离散输入 03保持寄存器 04输入寄存器（null/非法按 03）
        Integer functionCode = 3;
        if (StringUtils.isNotBlank(message.getFunctionCode())) {
            try {
                functionCode = Integer.parseInt(message.getFunctionCode());
            } catch (NumberFormatException e) {
                log.warn("componentId={} 功能码非法，按03保持寄存器处理：{}", componentId, message.getFunctionCode());
            }
        }
        //合并多个子区间为 [minStart, maxEnd] 一个连续块：保证一个 code 对应一块寄存器，解码(同 code 覆盖)不再互相覆盖
        int minStart = Integer.MAX_VALUE;
        int maxEnd = Integer.MIN_VALUE;
        for (RangeParserUtil.RangeItem item : list) {
            minStart = Math.min(minStart, item.getStart());
            maxEnd = Math.max(maxEnd, item.getStart() + item.getCount() - 1);
        }
        int count = maxEnd - minStart + 1;
        //读取（失败清理脏数据并抛出，让调度层感知读异常）
        List<Integer> dataList;
        try {
            dataList = ModbusDataReader.readByFunction(componentId, message.getSlaveId(), functionCode, minStart, count);
        } catch (Exception e) {
            log.error("componentId={} 读取寄存器失败，slaveId={}, range={}",
                    componentId, message.getSlaveId(), minStart + "-" + count, e);
            //处理恢复后避免脏数据过多
            BlockingQueue<ModbusMessage> queue = ModbusMessageScheduler.messageQueueMap.get(componentId);
            if (queue != null) {
                queue.removeIf(o -> o.getCode().equals(message.getCode()) && o.getSlaveId().equals(message.getSlaveId()));
                log.info("componentId={} 清理脏数据", componentId);
            }
            throw e;
        }
        //合并成单个连续块，供协议解码（一个 code 对应一块寄存器，不再互覆盖）
        RangeParserUtil.RangeItem merged = new RangeParserUtil.RangeItem(minStart, count);
        merged.setRegisterList(dataList);
        List<RangeParserUtil.RangeItem> mergedList = new ArrayList<>();
        mergedList.add(merged);
        //读取完成，按配置延迟再继续下次读取（单消费者读节奏限制）
        if (message.getDelayTime() != null && message.getDelayTime() > 0) {
            Thread.sleep(message.getDelayTime());
        }
        JSONObject objectData = new JSONObject();
        objectData.put("deviceSn", message.getDeviceSn());
        objectData.put("slaveId", message.getSlaveId());
        objectData.put("code", message.getCode());
        objectData.put("registerRange", message.getRegisterRange());
        objectData.put("jsonArray", JSONArray.from(mergedList));
        objectData.put("dataType", message.getDataType());
        objectData.put("byteOrder", message.getByteOrder());
        objectData.put("isSigned", message.getIsSigned());
        objectData.put("scale", message.getScale());
        objectData.put("offset", message.getOffset());
        threadPoolTaskExecutor.execute(() -> {
            try {
                WebSocketServer.broadcast("component", componentId, JSONArray.toJSONString(mergedList));
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
