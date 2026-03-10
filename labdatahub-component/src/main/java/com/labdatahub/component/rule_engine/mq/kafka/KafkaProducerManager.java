package com.labdatahub.component.rule_engine.mq.kafka;

import com.labdatahub.common.utils.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Kafka生产者连接管理工具
 * 专门用于动态管理Kafka生产者连接，推送日志消息
 */
public class KafkaProducerManager {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerManager.class);

    private final Map<String, Producer<String, String>> producers = new ConcurrentHashMap<>();

    private final Map<String, String> producerTopic = new ConcurrentHashMap<>();

    private static volatile KafkaProducerManager instance;

    private KafkaProducerManager() {}

    public static KafkaProducerManager getInstance() {
        if (instance == null) {
            synchronized (KafkaProducerManager.class) {
                if (instance == null) {
                    instance = new KafkaProducerManager();
                }
            }
        }
        return instance;
    }

    /**
     * 创建生产者连接
     */
    public boolean createProducer(String producerId, String bootstrapServers) {
        return createProducer(producerId, bootstrapServers, null,null);
    }

    public boolean createProducer(String producerId, String bootstrapServers, Properties customProperties,String topic) {
        try {
            if (producers.containsKey(producerId)) {
                logger.warn("Producer {} already exists", producerId);
                return true; // 已经存在，返回true
            }

            Properties props = new Properties();
            // 设置默认配置
            props.putAll(getDefaultProducerProperties());
            // 设置bootstrap servers
            props.put("bootstrap.servers", bootstrapServers);
            // 添加自定义配置
            if (customProperties != null) {
                props.putAll(customProperties);
            }

            Producer<String, String> producer = new KafkaProducer<>(props);
            producers.put(producerId, producer);
            if(StringUtils.isNotEmpty(topic)){
                producerBindTopic(producerId,topic);
            }
            logger.info("Producer {} created successfully, bootstrap.servers: {}",
                    producerId, bootstrapServers);
            return true;

        } catch (Exception e) {
            logger.error("Failed to create producer {} with servers {}",
                    producerId, bootstrapServers, e);
            return false;
        }
    }

    public boolean producerBindTopic(String producerId,String topic){
        producerTopic.put(producerId,topic);
        return true;
    }

    /**
     * 异步发送日志消息
     */
    public boolean sendLogAsync(String producerId, String topic, String logMessage) {
        return sendLogAsync(producerId, topic, null, logMessage);
    }

    /**
     * 异步发送日志消息
     */
    public boolean sendLogAsync(String producerId, String logMessage) {
        String topic = producerTopic.get(producerId);
        if(StringUtils.isNotEmpty(topic)){
            return sendLogAsync(producerId, topic, null, logMessage);
        }else{
            return false;
        }
    }

    public boolean sendLogAsync(String producerId, String topic, String key, String logMessage) {
        Producer<String, String> producer = producers.get(producerId);
        if (producer == null) {
            logger.error("Producer {} not found", producerId);
            return false;
        }

        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, logMessage);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    logger.error("Failed to send message to topic {}: {}", topic, exception.getMessage());
                } else {
                    logger.debug("Message sent successfully to topic {} partition {} offset {}",
                            topic, metadata.partition(), metadata.offset());
                }
            });
            return true;
        } catch (Exception e) {
            logger.error("Error sending message to Kafka topic {}", topic, e);
            return false;
        }
    }

    /**
     * 同步发送日志消息
     */
    public boolean sendLogSync(String producerId, String topic, String logMessage) {
        return sendLogSync(producerId, topic, null, logMessage, 5000);
    }

    /**
     * 批量发送日志消息
     */
    public boolean sendLogsBatch(String producerId, String topic, List<String> logMessages) {
        if (logMessages == null || logMessages.isEmpty()) {
            return true;
        }

        Producer<String, String> producer = producers.get(producerId);
        if (producer == null) {
            logger.error("Producer {} not found", producerId);
            return false;
        }

        int successCount = 0;
        for (String message : logMessages) {
            if (sendLogAsync(producerId, topic, message)) {
                successCount++;
            }
        }

        logger.info("Batch send completed: {}/{} messages sent successfully",
                successCount, logMessages.size());
        return successCount == logMessages.size();
    }

    /**
     * 关闭生产者连接
     */
    public boolean closeProducer(String producerId) {
        Producer<String, String> producer = producers.remove(producerId);
        producerTopic.remove(producerId);
        if (producer != null) {
            try {
                producer.close();
                logger.info("Producer {} closed successfully", producerId);
                return true;
            } catch (Exception e) {
                logger.error("Error closing producer {}", producerId, e);
            }
        } else {
            logger.warn("Producer {} not found", producerId);
        }
        return false;
    }

    /**
     * 关闭所有生产者连接
     */
    public void shutdown() {
        logger.info("Shutting down all Kafka producers...");

        List<String> producerIds = new ArrayList<>(producers.keySet());
        for (String producerId : producerIds) {
            closeProducer(producerId);
        }

        logger.info("All Kafka producers closed");
    }

    /**
     * 获取所有活跃的生产者
     */
    public List<String> getActiveProducers() {
        return new ArrayList<>(producers.keySet());
    }

    /**
     * 检查生产者是否存在
     */
    public boolean containsProducer(String producerId) {
        return producers.containsKey(producerId);
    }

    /**
     * 获取默认的生产者配置
     */
    private Properties getDefaultProducerProperties() {
        Properties props = new Properties();
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("acks", "1"); // 等待leader确认
        props.put("retries", 3); // 重试次数
        props.put("batch.size", 16384); // 批量大小
        props.put("linger.ms", 1); // 等待时间
        props.put("buffer.memory", 33554432); // 缓冲区大小
        props.put("max.block.ms", 3000); // 最大阻塞时间
        return props;
    }

    /**
     * 确保topic存在
     */
    public boolean ensureTopicExists(String producerId, String topic) {
        Producer<String, String> producer = producers.get(producerId);
        if (producer == null) {
            logger.error("Producer {} not found for topic check", producerId);
            return false;
        }

        try {
            // 尝试获取topic的元数据
            producer.partitionsFor(topic);
            logger.debug("Topic {} is available", topic);
            return true;
        } catch (Exception e) {
            logger.warn("Topic {} may not exist or is not accessible: {}", topic, e.getMessage());
            return false;
        }
    }

    /**
     * 同步发送消息（带重试）
     */
    public boolean sendLogSync(String producerId, String topic, String key, String logMessage, long timeoutMs) {
        Producer<String, String> producer = producers.get(producerId);
        if (producer == null) {
            logger.error("Producer {} not found", producerId);
            return false;
        }

        // 首先确保topic存在
        ensureTopicExists(producerId, topic);

        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, logMessage);
            Future<RecordMetadata> future = producer.send(record);

            // 等待发送完成
            RecordMetadata metadata = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            logger.debug("Message sent successfully to topic {} partition {}",
                    topic, metadata.partition());
            return true;

        } catch (java.util.concurrent.TimeoutException e) {
            logger.error("Timeout sending message to topic {} after {} ms", topic, timeoutMs);
            return false;
        } catch (Exception e) {
            logger.error("Error sending message to Kafka topic {}", topic, e);
            return false;
        }
    }
}