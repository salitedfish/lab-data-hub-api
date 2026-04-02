package com.labdatahub.component.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

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
 * 
* @ClassName: DatabaseMessageConsumeService  
* @Description: 消费处理实现 
* @author xwb  
* @date 2026年4月1日
 */
@Slf4j
@Component
public class DatabaseMessageConsumeService implements DatabaseMessageConsumeHandler {

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    
    @Autowired
    private EventBus eventBus;
    
    @Override
    public void handle(String componentId, DatabaseMessage message) throws Exception {
        // 执行 SQL 查询
//        List<Map<String, Object>> resultList;
//        try {
//            resultList = DatabaseReader.queryForList(componentId, message.getSql(), message.getParameters());
//        } catch (Exception e) {
//            log.error("执行SQL失败 componentId={}, sql={}", componentId, message.getSql(), e);
//            return;
//        }
    	Map<String, Object> resultList;
        try {
            resultList = DatabaseReader.getLatestRecord(componentId);
        } catch (Exception e) {
            log.error("执行查询失败 componentId={}}", componentId, e);
            return;
        }
        // 构建结果 JSON
        JSONObject result = new JSONObject();
        result.put("deviceSn", message.getDeviceSn());
//        result.put("code", message.getCode());
//        result.put("sql", message.getSql());
        result.put("data", resultList);

        // 延迟（如果配置了）
        if (message.getDelayTime() != null && message.getDelayTime() > 0) {
            try {
                Thread.sleep(message.getDelayTime());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("延迟等待被中断");
            }
        }

        // 异步广播结果（例如 WebSocket）
        threadPoolTaskExecutor.execute(() -> {
            try {
                WebSocketServer.broadcast("component", componentId, result.toJSONString());
                log.debug("数据库查询结果广播: componentId={}, result={}", componentId, result);
            } catch (Exception e) {
                log.error("广播数据库结果失败 componentId={}", componentId, e);
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
            Object data = method.invoke(object, result); // 传递整个 result
            DecodeMessage decodeMessage = MessageUtils.parseMessage(data);
            if (decodeMessage != null) {
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