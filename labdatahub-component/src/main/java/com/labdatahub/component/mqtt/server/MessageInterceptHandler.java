package com.labdatahub.component.mqtt.server;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-09-16
 */
import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.event.EventBus;
import io.moquette.broker.ClientDescriptor;
import io.moquette.broker.Server;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.labdatahub.component.mqtt.server.MqttBrokerManager.CLIENT_DEVICE;


/**
 * MQTT 消息拦截处理器
 */
public class MessageInterceptHandler extends AbstractInterceptHandler {

    private static final Logger log = LoggerFactory.getLogger(MessageInterceptHandler.class);
    private final MqttBrokerConsumer mqttBrokerConsumer;

    private final String brokerId;
    private final Map<String, String> users;
    private boolean allowAnonymous = false;

    // 通过构造器注入
    public MessageInterceptHandler(MqttBrokerConsumer mqttBrokerConsumer, String brokerId,Map<String, String> users,boolean allowAnonymous) {
        this.mqttBrokerConsumer = mqttBrokerConsumer;
        this.brokerId = brokerId;
        this.users = new ConcurrentHashMap<>(users);
        this.allowAnonymous = allowAnonymous;
    }
    @Override
    public String getID() {
        return "LabdatahubMessageInterceptor";
    }

    @Override
    public void onDisconnect(InterceptDisconnectMessage msg) {
        if(CLIENT_DEVICE.getOrDefault(msg.getClientID(),null)!=null){
            SpringUtils.getBean(EventBus.class).publish("device.offline",CLIENT_DEVICE.get(msg.getClientID()));
        }
        super.onDisconnect(msg);
    }

    @Override
    public void onConnect(InterceptConnectMessage msg) {
        if(!allowAnonymous) {
            String username = msg.getUsername();
            String password = msg.getPassword() != null ? new String(msg.getPassword()) : null;

            if (username == null || password == null) {
                Server server = MqttBrokerManager.serverMap.get(brokerId);
                server.disconnectClient(msg.getClientID());
                log.warn("Broker[{}] 连接拒绝: 用户名或密码为空", brokerId);
            }

            String storedPassword = users.get(username);
            if (storedPassword == null || !storedPassword.equals(password)) {
                Server server = MqttBrokerManager.serverMap.get(brokerId);
                server.disconnectClient(msg.getClientID());
                log.warn("Broker[{}] 认证失败: 用户 {} 密码错误", brokerId, username);
            }
            log.debug("Broker[{}] 用户认证成功: {}", brokerId, username);
        }
        super.onConnect(msg);
    }

    @Override
    public void onPublish(InterceptPublishMessage msg) {
        try {
            String clientId = msg.getClientID();
            String topic = msg.getTopicName();
            ByteBuf payload = msg.getPayload();

            // 将 ByteBuf 转换为字符串
            String messageContent = byteBufToString(payload);

            // 获取 QoS 等级
            int qos = msg.getQos().value();

            // 获取是否保留消息
            boolean retained = msg.isRetainFlag();

            // 处理消息 - 这里你可以实现自己的业务逻辑
            processMessage(clientId, topic, messageContent, qos, retained);

        } catch (Exception e) {
            log.error("处理MQTT消息时发生错误", e);
        }
    }

    @Override
    public void onSessionLoopError(Throwable throwable) {

    }

    /**
     * 将 ByteBuf 转换为字符串
     */
    public String byteBufToString(ByteBuf byteBuf) {
        if (byteBuf == null) {
            return "";
        }
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), bytes);
        return new String(bytes);
    }

    /**
     * 处理接收到的消息
     */
    public void processMessage(String clientId, String topic, String message, int qos, boolean retained) {
        SpringUtils.getBean(MqttBrokerConsumer.class).message(brokerId,clientId,topic,message);
    }

}
