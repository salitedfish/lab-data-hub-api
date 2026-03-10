package com.labdatahub.component.rule_engine.mq.mqtt;

import io.moquette.broker.Server;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MQTT 消息处理器 - 只下发数据版本
 * 仅用于连接认证，不处理任何设备上报的消息
 */
public class MessageHandler extends AbstractInterceptHandler {

    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);

    private final String brokerId;
    private final Map<String, String> users;
    private boolean allowAnonymous = false;

    /**
     * @param brokerId Broker ID
     * @param users 用户账号密码映射表
     * @param allowAnonymous 是否允许匿名连接
     */
    public MessageHandler(String brokerId, Map<String, String> users, boolean allowAnonymous) {
        this.brokerId = brokerId;
        this.users = new ConcurrentHashMap<>(users);
        this.allowAnonymous = allowAnonymous;
    }

    @Override
    public String getID() {
        return "LabdatahubDownlinkMessageHandler";
    }

    /**
     * 处理客户端连接 - 进行账号密码认证
     */
    @Override
    public void onConnect(InterceptConnectMessage msg) {
        if(!allowAnonymous) {
            String username = msg.getUsername();
            String password = msg.getPassword() != null ? new String(msg.getPassword()) : null;

            if (username == null || password == null) {
                Server server = MqttBrokerUtils.serverMap.get(brokerId);
                if (server != null) {
                    server.disconnectClient(msg.getClientID());
                }
                log.warn("Broker[{}] 连接拒绝：用户名或密码为空，clientId={}", brokerId, msg.getClientID());
                return;
            }

            String storedPassword = users.get(username);
            if (storedPassword == null || !storedPassword.equals(password)) {
                Server server = MqttBrokerUtils.serverMap.get(brokerId);
                if (server != null) {
                    server.disconnectClient(msg.getClientID());
                }
                log.warn("Broker[{}] 认证失败：用户 {} 密码错误，clientId={}", brokerId, username, msg.getClientID());
                return;
            }
            log.debug("Broker[{}] 用户认证成功：{}, clientId={}", brokerId, username, msg.getClientID());
        }
        super.onConnect(msg);
    }

    /**
     * 处理客户端断开连接
     */
    @Override
    public void onDisconnect(InterceptDisconnectMessage msg) {
        log.debug("Broker[{}] 客户端断开连接：{}", brokerId, msg.getClientID());
        super.onDisconnect(msg);
    }

    /**
     * 处理发布的消息 - 忽略所有设备上报的消息，不做任何处理
     */
    @Override
    public void onPublish(InterceptPublishMessage msg) {
        // 只记录 debug 日志，不处理任何业务逻辑
        if (log.isDebugEnabled()) {
            log.debug("Broker[{}] 忽略设备上报消息：clientId={}, topic={}, qos={}", 
                    brokerId, msg.getClientID(), msg.getTopicName(), msg.getQos());
        }
    }

    @Override
    public void onSessionLoopError(Throwable throwable) {
        log.error("Broker[{}] Session 循环错误", brokerId, throwable);
    }
}
