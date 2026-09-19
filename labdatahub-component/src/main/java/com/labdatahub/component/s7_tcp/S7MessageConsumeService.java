//由AI修改
package com.labdatahub.component.s7_tcp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONObject;
import com.github.s7connector.api.S7Connector;
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
public class S7MessageConsumeService implements S7MessageConsumeHandler {

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private EventBus eventBus;

    @Override
    public void handle(String componentId, S7Message message) throws Exception {
        S7Connector connector = S7ConnectionManager.getConnection(componentId);
        if (connector == null) {
        	log.warn("componentId={} 连接不存在或未连接", componentId);
            return;
        }
        // 配置不完整（dbNumber/startAddress 任一为空）时跳过本次，避免 Integer 拆箱 NPE 被误判为读失败触发无谓重连
        if (message.getDbNumber() == null || message.getStartAddress() == null) {
            log.warn("componentId={} 读取code={}配置不完整（dbNumber/startAddress 为空），跳过本次", componentId, message.getCode());
            return;
        }

        // 读取数据（按 区类型+块类型+数据类型 解析，读异常强制重连修复自愈）
        //
        // 读必须与写、重连共用同一把锁（见 S7ConnectionManager.lockMap 注释）：一台设备一条 TCP，
        // 写从 HTTP 线程进来、重连会把连接对象换掉，不串行化的话读到的实时数据会串位。
        // 读侧用无界 lock()：采集是命脉，不能因为抢不到写锁而失败。
        Object rawData;
        boolean needReconnect = false;
        ReentrantLock lock = S7ConnectionManager.getLock(componentId);
        lock.lock();
        try {
            rawData = S7DataReader.readDB(connector, message.getDbNumber(), message.getBlockType(), message.getAreaType(), message.getDataType(), message.getStartAddress(), message.getLength(), message.getBitOffset(), message.getIsSigned());
        } catch (Exception e) {
            log.error("componentId={} 读取S7数据失败", componentId, e);
            needReconnect = true;
            throw e;
        } finally {
            lock.unlock();
            // ⚠️ 重连放在 unlock 之后：doReconnect 内部会真建 TCP 连接并退避 sleep，
            //    持着锁做会把整条链路卡满数秒（方案 5.4）
            if (needReconnect) {
                S7ConnectionManager.forceReconnect(componentId);
            }
        }
        // 读取完成，按配置延迟再继续下次读取（单消费者读节奏限制）
        if (message.getDelayTime() != null && message.getDelayTime() > 0) {
            Thread.sleep(message.getDelayTime());
        }
        // 构建结果 JSON
        JSONObject result = new JSONObject();
        result.put("deviceSn", message.getDeviceSn());
        result.put("code", message.getCode());
        result.put("dbNumber", message.getDbNumber());
        result.put("blockType", message.getBlockType());
        result.put("startAddress", message.getStartAddress());
        result.put("length", message.getLength());
        result.put("data", rawData); // 可按需转换为 hex 或数值列表
        result.put("dataType", message.getDataType());
        result.put("byteOrder", message.getByteOrder());
        result.put("isSigned", message.getIsSigned());
        result.put("scale", message.getScale());
        result.put("offset", message.getOffset());

        // 广播到 WebSocket
        threadPoolTaskExecutor.execute(() -> {
            try {
                WebSocketServer.broadcast("component", componentId, result.toJSONString());
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