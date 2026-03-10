package com.labdatahub.component.rule_engine.mq.rocketmq;


import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RocketMQ生产者管理工具
 */
public class RocketMQProducerManager {
    private static final Logger logger = LoggerFactory.getLogger(RocketMQProducerManager.class);

    private final Map<String, DefaultMQProducer> producers = new ConcurrentHashMap<>();
    private static final Map<String,RocketConfig> producerConfig = new HashMap<>();
    private static volatile RocketMQProducerManager instance;

    private RocketMQProducerManager() {}

    public static RocketMQProducerManager getInstance() {
        if (instance == null) {
            synchronized (RocketMQProducerManager.class) {
                if (instance == null) {
                    instance = new RocketMQProducerManager();
                }
            }
        }
        return instance;
    }

    /**
     * 创建生产者
     */
    public boolean createProducer(String producerId, String nameServer, String group) {
        return createProducer(producerId, nameServer, group, null);
    }

    /**
     * 创建生产者
     */
    public boolean createProducer(String producerId, RocketConfig rocketConfig) {
        if(createProducer(producerId, rocketConfig.getHost()+":"+rocketConfig.getPort(), rocketConfig.getGroup(), null)){
            producerConfig.put(producerId,rocketConfig);
            return true;
        }else {
            return false;
        }
    }

    public boolean createProducer(String producerId, String nameServer, String group,
                                  Map<String, String> customConfig) {
        try {
            if (producers.containsKey(producerId)) {
                logger.warn("Producer {} already exists", producerId);
                return true;
            }

            DefaultMQProducer producer = new DefaultMQProducer(group);
            producer.setNamesrvAddr(nameServer);

            // 设置默认配置
            producer.setSendMsgTimeout(3000); // 发送超时3秒
            producer.setRetryTimesWhenSendFailed(2); // 失败重试2次

            // 应用自定义配置
            if (customConfig != null) {
                applyCustomConfig(producer, customConfig);
            }

            producer.start();
            producers.put(producerId, producer);

            logger.info("RocketMQ producer created: {}, nameServer: {}, group: {}",
                    producerId, nameServer, group);
            return true;

        } catch (MQClientException e) {
            logger.error("Failed to create RocketMQ producer {}", producerId, e);
            return false;
        }
    }

    /**
     * 发送同步消息
     */
    public SendResult sendSync(String producerId, String topic, String message) {
        return sendSync(producerId, topic, "", message);
    }

    public SendResult sendSync(String producerId, String topic, String tags, String message) {
        return sendSync(producerId, topic, tags, "", message);
    }

    public SendResult sendSync(String producerId, String topic, String tags, String keys, String message) {
        DefaultMQProducer producer = producers.get(producerId);
        if (producer == null) {
            logger.error("Producer {} not found", producerId);
            return null;
        }

        try {
            Message msg = new Message(topic, tags, keys, message.getBytes(StandardCharsets.UTF_8));
            SendResult sendResult = producer.send(msg);

            logger.debug("Message sent successfully: {}", sendResult.getMsgId());
            return sendResult;

        } catch (Exception e) {
            logger.error("Failed to send message to topic {}", topic, e);
            return null;
        }
    }

    /**
     * 发送异步消息
     */
    public boolean sendAsync(String producerId, String topic, String message) {
        return sendAsync(producerId, topic, "", message, null);
    }

    /**
     * 发送异步消息
     */
    public boolean sendAsync(String producerId, String message) {
        RocketConfig rocketConfig = producerConfig.get(producerId);
        return sendAsync(producerId, rocketConfig.getTopic(), rocketConfig.getTags(), message, null);
    }

    public boolean sendAsync(String producerId, String topic, String tags, String message,
                             SendCallback callback) {
        DefaultMQProducer producer = producers.get(producerId);
        if (producer == null) {
            logger.error("Producer {} not found", producerId);
            return false;
        }

        try {
            Message msg = new Message(topic, tags, message.getBytes(StandardCharsets.UTF_8));

            producer.send(msg, (callback != null ? callback : new SendCallback() {
                            @Override
                            public void onSuccess(SendResult sendResult) {
                                logger.debug("Async message sent successfully: {}", sendResult.getMsgId());
                            }

                            @Override
                            public void onException(Throwable e) {
                                logger.error("Async message send failed", e);
                            }
                        }));

            return true;

        } catch (Exception e) {
            logger.error("Failed to send async message to topic {}", topic, e);
            return false;
        }
    }

    /**
     * 发送单向消息（不关心结果）
     */
    public boolean sendOneway(String producerId, String topic, String message) {
        return sendOneway(producerId, topic, "", message);
    }

    public boolean sendOneway(String producerId, String topic, String tags, String message) {
        DefaultMQProducer producer = producers.get(producerId);
        if (producer == null) {
            logger.error("Producer {} not found", producerId);
            return false;
        }

        try {
            Message msg = new Message(topic, tags, message.getBytes(StandardCharsets.UTF_8));
            producer.sendOneway(msg);

            logger.debug("Oneway message sent to topic: {}", topic);
            return true;

        } catch (Exception e) {
            logger.error("Failed to send oneway message to topic {}", topic, e);
            return false;
        }
    }

    /**
     * 批量发送消息
     */
    public boolean sendBatch(String producerId, String topic, java.util.List<String> messages) {
        DefaultMQProducer producer = producers.get(producerId);
        if (producer == null) {
            logger.error("Producer {} not found", producerId);
            return false;
        }

        try {
            java.util.List<Message> messageList = new java.util.ArrayList<>();
            for (String msg : messages) {
                messageList.add(new Message(topic, "", msg.getBytes("UTF-8")));
            }

            SendResult sendResult = producer.send(messageList);
            logger.info("Batch message sent successfully, count: {}", messages.size());
            return true;

        } catch (Exception e) {
            logger.error("Failed to send batch messages to topic {}", topic, e);
            return false;
        }
    }

    /**
     * 关闭生产者
     */
    public boolean closeProducer(String producerId) {
        DefaultMQProducer producer = producers.remove(producerId);
        if (producer != null) {
            try {
                producer.shutdown();
                producerConfig.remove(producerId);
                logger.info("RocketMQ producer {} closed", producerId);
                return true;
            } catch (Exception e) {
                logger.error("Error closing producer {}", producerId, e);
            }
        }
        return false;
    }

    /**
     * 关闭所有生产者
     */
    public void shutdown() {
        logger.info("Shutting down all RocketMQ producers...");

        for (Map.Entry<String, DefaultMQProducer> entry : producers.entrySet()) {
            try {
                entry.getValue().shutdown();
                logger.info("Producer {} closed", entry.getKey());
            } catch (Exception e) {
                logger.error("Error closing producer {}", entry.getKey(), e);
            }
        }

        producers.clear();
        logger.info("All RocketMQ producers closed");
    }

    /**
     * 获取活跃的生产者列表
     */
    public java.util.List<String> getActiveProducers() {
        return new java.util.ArrayList<>(producers.keySet());
    }

    private void applyCustomConfig(DefaultMQProducer producer, Map<String, String> config) {
        try {
            for (Map.Entry<String, String> entry : config.entrySet()) {
                switch (entry.getKey()) {
                    case "sendMsgTimeout":
                        producer.setSendMsgTimeout(Integer.parseInt(entry.getValue()));
                        break;
                    case "retryTimesWhenSendFailed":
                        producer.setRetryTimesWhenSendFailed(Integer.parseInt(entry.getValue()));
                        break;
                    case "maxMessageSize":
                        producer.setMaxMessageSize(Integer.parseInt(entry.getValue()));
                        break;
                    case "compressMsgBodyOverHowmuch":
                        producer.setCompressMsgBodyOverHowmuch(Integer.parseInt(entry.getValue()));
                        break;
                }
            }
        } catch (Exception e) {
            logger.warn("Apply custom config failed", e);
        }
    }

}
