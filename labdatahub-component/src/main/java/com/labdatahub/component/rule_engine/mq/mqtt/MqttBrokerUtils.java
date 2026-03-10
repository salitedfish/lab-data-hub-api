package com.labdatahub.component.rule_engine.mq.mqtt;

import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * MQTT Broker 管理器 - 只下发数据版本
 * 用于规则引擎消息队列，仅支持消息发布，不支持订阅和消息接收处理
 */
public class MqttBrokerUtils {
    private static final Logger log = LoggerFactory.getLogger(MqttBrokerUtils.class);

    // 存储 Broker 实例
    public static Map<String, Server> serverMap = new HashMap<>();

    public static Map<String, String> serverTopic = new HashMap<>();

    // 基础数据存储路径
    private static final String BASE_DATA_DIR = System.getProperty("user.dir") + File.separator + "data" + File.separator + "mqtt";

    static {
        File baseDir = new File(BASE_DATA_DIR);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    /**
     * 清理 H2 数据库锁文件
     */
    private static void cleanupLockFiles(File dataDir) {
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            return;
        }

        File[] lockFiles = dataDir.listFiles((dir, name) -> 
            name.endsWith(".lock.db") || name.endsWith(".trace.db"));
        
        if (lockFiles != null) {
            for (File lockFile : lockFiles) {
                try {
                    if (lockFile.delete()) {
                        log.debug("删除锁文件：{}", lockFile.getAbsolutePath());
                    } else {
                        log.warn("无法删除锁文件：{}", lockFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    log.warn("删除锁文件失败：{}", lockFile.getAbsolutePath(), e);
                }
            }
        }
    }

    /**
     * 新增 broker（只下发数据版本）
     * @param id Broker ID
     * @param tcpPort TCP 端口
     * @param topic 主题
     * @param allowAnonymous 是否允许匿名连接
     * @param userMap 用户账号密码映射表
     */
    public static boolean addBroker(String id, String tcpPort,String topic, boolean allowAnonymous, Map<String, String> userMap) throws IOException {
        String dataPath = BASE_DATA_DIR + File.separator + id;
        File path = new File(dataPath);
        if (!path.exists()) {
            path.mkdirs();
        }

        // 清理可能存在的锁文件
        cleanupLockFiles(path);

        Server mqttServer = new Server();
        MemoryConfig config = new MemoryConfig(new Properties());
        config.setProperty("host", "0.0.0.0");
        config.setProperty("port", tcpPort);
        if(allowAnonymous){
            config.setProperty("allow_anonymous", "true");
        }else {
            config.setProperty("allow_anonymous", "false");
        }
        config.setProperty("persistence_enabled", "true");
        config.setProperty("data_path", path.getAbsolutePath());
        config.setProperty("authenticator_class", "");
        config.setProperty("telemetry_enabled", "false");
        mqttServer.startServer(config);
        // 添加拦截器用于账号密码认证（但不处理设备上报消息）
        mqttServer.addInterceptHandler(new MessageHandler(id, userMap, allowAnonymous));

        serverMap.put(id, mqttServer);
        serverTopic.put(id,topic);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopBroker(id);
        }));

        return true;
    }

    /**
     * 向指定主题发布消息
     */
    public static boolean publishMessage(String brokerId, String message, int qos, boolean retain) {
        Server server = serverMap.get(brokerId);
        String topic = serverTopic.get(brokerId);
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

            byte[] payloadBytes = message.getBytes(StandardCharsets.UTF_8);
            MqttPublishMessage publishMessage = new MqttPublishMessage(
                    fixedHeader,
                    variableHeader,
                    Unpooled.copiedBuffer(payloadBytes)
            );

            server.internalPublish(publishMessage, "internal");
            return true;

        } catch (Exception e) {
            log.error("Broker[{}] 向主题 [{}] 发布消息失败：{}", brokerId, topic, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 向指定主题发布消息（带消息 ID）
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
            log.error("Broker[{}] 向主题 [{}] 发布消息失败：{}", brokerId, topic, e.getMessage(), e);
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
            log.info("Broker[{}] 向主题 [{}] 发布二进制消息成功，长度：{} bytes",
                    brokerId, topic, payload.length);
            return true;

        } catch (Exception e) {
            log.error("Broker[{}] 向主题 [{}] 发布二进制消息失败：{}", brokerId, topic, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 简化版发布消息 (默认 QoS 0, 不保留)
     */
    public static boolean publishMessage(String brokerId, String message) {
        return publishMessage(brokerId, message, 0, false);
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
                log.error("批量发布消息失败，主题：{}, 错误：{}", msg.getTopic(), e.getMessage());
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    /**
     * 获取 Broker 状态信息
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
     * 获取所有活跃的 Broker 列表
     */
    public static List<String> getActiveBrokers() {
        return new ArrayList<>(serverMap.keySet());
    }

    /**
     * 新增 broker（简化版）
     */
    public static boolean addBroker(String id, String tcpPort,String topic, boolean allowAnonymous, String username, String password) throws IOException {
        Map<String, String> userMap = new HashMap<>();
        if(!allowAnonymous){
            userMap.put(username, password);
        }
        return addBroker(id, tcpPort, topic, allowAnonymous, userMap);
    }

    /**
     * 停止指定的 broker
     */
    public static void stopBroker(String id) {
        Server server = serverMap.get(id);
        if (server != null) {
            server.stopServer();
            serverMap.remove(id);
            serverTopic.remove(id);
            log.info("Broker[{}] 已关闭", id);
            
            // 关闭后清理锁文件
            String dataPath = BASE_DATA_DIR + File.separator + id;
            cleanupLockFiles(new File(dataPath));
        }
    }

    /**
     * 停止所有 Broker
     */
    public static synchronized void stopAllBrokers() {
        List<String> brokerIds = new ArrayList<>(serverMap.keySet());
        for (String brokerId : brokerIds) {
            stopBroker(brokerId);
        }
        log.info("所有 Broker 已停止");
    }

    /**
     * MQTT 消息封装类
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