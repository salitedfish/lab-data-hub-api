package com.labdatahub.component.rule_engine.mq.kafka;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-10-21
 */
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * 简单的Kafka消费者测试
 */
public class SimpleKafkaConsumerTest {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "47.109.145.72:9092");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            // 直接分配分区，不使用消费者组
            org.apache.kafka.common.TopicPartition partition = new org.apache.kafka.common.TopicPartition("app-logs", 0);
            consumer.assign(Collections.singletonList(partition));
            consumer.seekToBeginning(Collections.singletonList(partition));

            System.out.println("开始消费分区 0 的消息...");

            int count = 0;
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(java.time.Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record : records) {
                    count++;
                    System.out.println("消息 " + count + ": " + record.value());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
