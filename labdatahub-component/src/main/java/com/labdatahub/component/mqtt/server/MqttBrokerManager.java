package com.labdatahub.component.mqtt.server;

import com.labdatahub.common.utils.StringUtils;
import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import org.apache.commons.compress.archivers.sevenz.CLI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * MQTT Broker管理器，每个Broker绑定独立认证器实例
 */
public class MqttBrokerManager {
    private static final Logger log = LoggerFactory.getLogger(MqttBrokerManager.class);

    // 存储Broker实例
    public static Map<String, Server> serverMap = new HashMap<>();
    //客户端-关联设备列表
    public static Map<String, Set<String>> CLIENT_DEVICE = new HashMap<>();

    // 基础数据存储路径
    private static final String BASE_DATA_DIR = System.getProperty("user.dir") + File.separator + "data" + File.separator + "mqtt";

    static {
        // 确保基础目录存在
        File baseDir = new File(BASE_DATA_DIR);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    /**
     * 新增broker并绑定独立认证器
     */
    public static boolean addBroker(String id, String tcpPort, String wsPort, boolean allowAnonymous, Map<String, String> userMap) throws IOException {
        String dataPath = BASE_DATA_DIR + File.separator + id;
        // 确保基础目录存在
        File path = new File(dataPath);
        if (!path.exists()) {
            path.mkdirs();
        }

        // 配置Broker
        Server mqttServer = new Server();
        MemoryConfig config = new MemoryConfig(new Properties());
        config.setProperty("host", "0.0.0.0");
        config.setProperty("port", tcpPort);
        if(StringUtils.isNotEmpty(wsPort)){
            config.setProperty("websocket_port", wsPort);
        }
        if(allowAnonymous){
            config.setProperty("allow_anonymous", "true");
        }else {
            config.setProperty("allow_anonymous", "false");
        }
        config.setProperty("persistence_enabled", "true");
        config.setProperty("data_path", path.getAbsolutePath());
        config.setProperty("authenticator_class", "");
        config.setProperty("telemetry_enabled", "false");

        // 启动服务器
        mqttServer.startServer(config);
        mqttServer.addInterceptHandler(new MessageInterceptHandler(new MqttBrokerConsumer(), id, userMap, allowAnonymous));
        serverMap.put(id, mqttServer);
        log.info("Broker[{}] 已启动 - TCP: {}, WebSocket: {}", id, tcpPort, wsPort);

        // 关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopBroker(id);
        }));

        return true;
    }

    /**
     * 向指定主题发布消息
     * @param brokerId Broker ID
     * @param topic 主题
     * @param message 消息内容
     * @param qos 服务质量等级 (0, 1, 2)
     * @param retain 是否保留消息
     * @return 是否发布成功
     */
    public static boolean publishMessage(String brokerId, String topic, String message, int qos, boolean retain) {
        Server server = serverMap.get(brokerId);
        if (server == null) {
            log.error("Broker[{}] 不存在", brokerId);
            return false;
        }

        try {
            // 创建 MqttPublishMessage
            MqttFixedHeader fixedHeader = new MqttFixedHeader(
                    MqttMessageType.PUBLISH,
                    false, // dup
                    MqttQoS.valueOf(qos),
                    retain,
                    0
            );

            MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(topic, 0); // packetId 设为 0

            // 创建消息负载
            byte[] payloadBytes = message.getBytes(StandardCharsets.UTF_8);
            MqttPublishMessage publishMessage = new MqttPublishMessage(
                    fixedHeader,
                    variableHeader,
                    Unpooled.copiedBuffer(payloadBytes)
            );

            // 发布消息，"internal" 作为 clientId 表示内部发布
            server.internalPublish(publishMessage, "internal");

            return true;

        } catch (Exception e) {
            log.error("Broker[{}] 向主题 [{}] 发布消息失败: {}", brokerId, topic, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 向指定主题发布消息（带消息ID）
     */
    public static boolean publishMessageWithId(String brokerId, String topic, String message, int qos, boolean retain, int messageId) {
        Server server = serverMap.get(brokerId);
        if (server == null) {
            log.error("Broker[{}] 不存在", brokerId);
            return false;
        }

        try {
            MqttFixedHeader fixedHeader = new MqttFixedHeader(
                    MqttMessageType.PUBLISH,
                    false,
                    MqttQoS.valueOf(qos),
                    retain,
                    0
            );

            MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(topic, messageId);

            byte[] payloadBytes = message.getBytes(StandardCharsets.UTF_8);
            MqttPublishMessage publishMessage = new MqttPublishMessage(
                    fixedHeader,
                    variableHeader,
                    Unpooled.copiedBuffer(payloadBytes)
            );

            server.internalPublish(publishMessage, "server_" + messageId);

            return true;

        } catch (Exception e) {
            log.error("Broker[{}] 向主题 [{}] 发布消息失败: {}", brokerId, topic, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发布二进制消息
     */
    public static boolean publishBinaryMessage(String brokerId, String topic, byte[] payload, int qos, boolean retain) {
        Server server = serverMap.get(brokerId);
        if (server == null) {
            log.error("Broker[{}] 不存在", brokerId);
            return false;
        }

        try {
            MqttFixedHeader fixedHeader = new MqttFixedHeader(
                    MqttMessageType.PUBLISH,
                    false,
                    MqttQoS.valueOf(qos),
                    retain,
                    0
            );

            MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(topic, 0);

            MqttPublishMessage publishMessage = new MqttPublishMessage(
                    fixedHeader,
                    variableHeader,
                    Unpooled.copiedBuffer(payload)
            );

            server.internalPublish(publishMessage, "internal_binary");

            return true;

        } catch (Exception e) {
            log.error("Broker[{}] 向主题 [{}] 发布二进制消息失败: {}", brokerId, topic, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 简化版发布消息 (默认 QoS 0, 不保留)
     */
    public static boolean publishMessage(String brokerId, String topic, String message) {
        return publishMessage(brokerId, topic, message, 0, false);
    }

    /**
     * 批量发布消息
     */
    public static boolean publishBatchMessages(String brokerId, List<MqttMessage> messages) {
        Server server = serverMap.get(brokerId);
        if (server == null) {
            log.error("Broker[{}] 不存在", brokerId);
            return false;
        }

        boolean allSuccess = true;
        for (MqttMessage msg : messages) {
            try {
                MqttFixedHeader fixedHeader = new MqttFixedHeader(
                        MqttMessageType.PUBLISH,
                        false,
                        MqttQoS.valueOf(msg.getQos()),
                        msg.isRetain(),
                        0
                );

                MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(msg.getTopic(), 0);

                byte[] payloadBytes = msg.getMessage().getBytes(StandardCharsets.UTF_8);
                MqttPublishMessage publishMessage = new MqttPublishMessage(
                        fixedHeader,
                        variableHeader,
                        Unpooled.copiedBuffer(payloadBytes)
                );

                server.internalPublish(publishMessage, "batch_" + System.currentTimeMillis());

            } catch (Exception e) {
                log.error("批量发布消息失败, 主题: {}, 错误: {}", msg.getTopic(), e.getMessage());
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    /**
     * 获取Broker状态信息
     */
    public static Map<String, Object> getBrokerStatus(String brokerId) {
        Server server = serverMap.get(brokerId);
        Map<String, Object> status = new HashMap<>();

        if (server != null) {
            status.put("running", true);
            status.put("active", true);
            status.put("brokerId", brokerId);
        } else {
            status.put("running", false);
            status.put("active", false);
            status.put("brokerId", brokerId);
        }

        return status;
    }

    /**
     * 获取所有活跃的Broker列表
     */
    public static List<String> getActiveBrokers() {
        return new ArrayList<>(serverMap.keySet());
    }

    /**
     * 新增broker并绑定独立认证器（简化版）
     */
    public static boolean addBroker(String id, String tcpPort, String wsPort, boolean allowAnonymous, String username, String password) throws IOException {
        Map<String, String> userMap = new HashMap<>();
        userMap.put(username, password);
        return addBroker(id, tcpPort, wsPort, allowAnonymous, userMap);
    }

    /**
     * 停止指定的broker
     */
    public static void stopBroker(String id) {
        Server server = serverMap.get(id);
        if (server != null) {
            server.stopServer();
            serverMap.remove(id);
            log.info("Broker[{}] 已关闭", id);
        }
    }

    /**
     * 停止所有Broker
     */
    public static void stopAllBrokers() {
        List<String> brokerIds = new ArrayList<>(serverMap.keySet());
        for (String brokerId : brokerIds) {
            stopBroker(brokerId);
        }
        log.info("所有Broker已关闭");
    }

    /**
     * 绑定设备
     */
    public static void bindDevice(String clientId,String deviceSn){
        Set<String> set = CLIENT_DEVICE.getOrDefault(clientId,null);
        if(set!=null){
            set.add(deviceSn);
        }else {
            Set<String> deviceSet = new HashSet<>();
            deviceSet.add(deviceSn);
            CLIENT_DEVICE.put(clientId,deviceSet);
        }
    }

    /**
     * MQTT消息封装类
     */
    public static class MqttMessage {
        private String topic;
        private String message;
        private int qos;
        private boolean retain;

        public MqttMessage(String topic, String message, int qos, boolean retain) {
            this.topic = topic;
            this.message = message;
            this.qos = qos;
            this.retain = retain;
        }

        // getters and setters
        public String getTopic() { return topic; }
        public String getMessage() { return message; }
        public int getQos() { return qos; }
        public boolean isRetain() { return retain; }

        public void setTopic(String topic) { this.topic = topic; }
        public void setMessage(String message) { this.message = message; }
        public void setQos(int qos) { this.qos = qos; }
        public void setRetain(boolean retain) { this.retain = retain; }
    }
}