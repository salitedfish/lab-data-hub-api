package com.labdatahub.component.omron_fins_tcp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

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

@Slf4j
@Component
public class OmronFinsMessageConsumeService implements OmronFinsMessageConsumeHandler {

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private EventBus eventBus;

    @Override
    public void handle(String componentId, OmronFinsMessage message) throws Exception {
        OmronFinsClient client = OmronFinsConnectionManager.connections.get(componentId);
        if (client == null || !client.isConnected()) {
            return;
        }

        // 解析寄存器范围（格式如 "1,2-5" 需要转换为FINS地址，此处假设范围解析后为起始地址+数量）
        List<RangeParserUtil.RangeItem> list = RangeParserUtil.parse(message.getRegisterRange());
        if (list.isEmpty()) {
            return;
        }

        // 注意：FINS需要区域代码，此处假设配置中提供了区域代码，或者从其他方式获取
        // 为简化，我们使用D区（0x82）作为示例，实际可能需要根据消息或配置确定
        int areaCode = 0x82; // 应根据实际情况扩展

        for (int i = 0; i < list.size(); i++) {
            RangeParserUtil.RangeItem item = list.get(i);
            try {
                List<Integer> dataList = OmronFinsDataReader.readWords(client, areaCode, item.getStart(), item.getCount());
                item.setRegisterList(dataList);
            } catch (Exception e) {
                // 清理该消息对应的队列项（避免重复消费）
                BlockingQueue<OmronFinsMessage> queue = OmronFinsMessageScheduler.messageQueueMap.get(componentId);
                if (queue != null) {
                    queue.removeIf(o -> o.getCode().equals(message.getCode()) && o.getSlaveId().equals(message.getSlaveId()));
                }
                throw e;
            }
        }

        // 构建返回数据（与Modbus逻辑一致）
        JSONObject objectData = new JSONObject();
        objectData.put("deviceSn", message.getDeviceSn());
        objectData.put("slaveId", message.getSlaveId());
        objectData.put("code", message.getCode());
        objectData.put("registerRange", message.getRegisterRange());
        objectData.put("jsonArray", JSONArray.from(list));

        threadPoolTaskExecutor.execute(() -> {
            WebSocketServer.broadcast("component", componentId, JSONObject.toJSONString(list));
        });

        // 调用协议解析（与Modbus保持一致）
        String protocolId = ProtocolManager.PROTOCOL_MAP.getOrDefault(componentId, null);
        if (StringUtils.isNotEmpty(protocolId)) {
            Method method = ProtocolManager.DECODE_METHOD.get(protocolId);
            Object object = ProtocolManager.CLASS_INSTANCE.get(protocolId);
            try {
                Object data = method.invoke(object, objectData);
                DecodeMessage decodeMessage = MessageUtils.parseMessage(data);
                if (decodeMessage != null) {
                    threadPoolTaskExecutor.execute(() -> {
                        WebSocketServer.broadcast("device", decodeMessage.getDeviceSn(), JSONObject.toJSONString(decodeMessage));
                    });
                    MessageCache.setDeviceLastData(decodeMessage.getDeviceSn(), decodeMessage);
                    eventBus.publish("device.up", new MessageUpEvent(this, "device.up", decodeMessage.getDeviceSn(), decodeMessage));
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}