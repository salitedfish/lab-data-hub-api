package com.labdatahub.component.rule_engine.mq.rabbitmq;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-10-21
 */

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * 简单RabbitMQ消费者
 */
public class SimpleRabbitMQConsumer {

    public static void main(String[] args) {
        // RabbitMQ连接参数
        String host = "47.109.145.72"; // 你的RabbitMQ地址
        int port = 5672;
        String username = "rabbitmq";
        String password = "rabbitmq";
        String queueName = "test123456"; // 要消费的队列名

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        try {
            // 建立连接
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            System.out.println("等待接收消息，队列: " + queueName);
            System.out.println("按Ctrl+C停止消费...");

            // 创建消费者
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("收到消息: " + message);
            };

            // 开始消费
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
//            channel.basicConsume("test2", true, deliverCallback, consumerTag -> {});
            // 保持程序运行
//            Thread.sleep(30000); // 运行30秒后自动停止
//            System.out.println("消费结束");

//            channel.close();
//            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
