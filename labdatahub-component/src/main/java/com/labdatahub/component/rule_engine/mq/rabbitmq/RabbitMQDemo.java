package com.labdatahub.component.rule_engine.mq.rabbitmq;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-10-20
 */
public class RabbitMQDemo {
    public static void main(String[] args) throws InterruptedException {
        // 配置第一个RabbitMQ连接
        RabbitMQConfig config1 = new RabbitMQConfig();
        config1.setHost("47.109.145.72");
        config1.setPort(5672);
        config1.setUsername("rabbitmq");
        config1.setPassword("rabbitmq");

        // 初始化连接
        RabbitMQUtils.initConnection("primary-mq", config1);

        // 配置第一个RabbitMQ连接
        RabbitMQConfig config2 = new RabbitMQConfig();
        config2.setHost("47.109.145.72");
        config2.setPort(5672);
        config2.setUsername("rabbitmq");
        config2.setPassword("rabbitmq");

        // 初始化连接
        RabbitMQUtils.initConnection("second-mq", config2);

        // 发送消息
        for (int i = 0; i < 50; i++) {
            Thread.sleep(1000);
            RabbitMQUtils.sendMessage("primary-mq", "", "hhh", "Hello RabbitMQ");
//            RabbitMQUtils.sendMessage("second-mq", "test", "testKey", "Hello RabbitMQ2");
        }
        // 检查连接状态
        RabbitMQUtils.printConnectionStatus();

        // 程序退出时关闭连接
        Runtime.getRuntime().addShutdownHook(new Thread(RabbitMQUtils::shutdown));
    }
}
