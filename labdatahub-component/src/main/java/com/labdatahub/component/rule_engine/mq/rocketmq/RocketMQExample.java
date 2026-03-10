package com.labdatahub.component.rule_engine.mq.rocketmq;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-10-22
 */
public class RocketMQExample {
    public static void main(String[] args) {
        RocketMQProducerManager manager = RocketMQProducerManager.getInstance();
        RocketMQLogPushService logService = new RocketMQLogPushService();

        try {
            // 1. 初始化日志推送服务
            boolean initialized = logService.initialize(
                    "log-producer",
                    "47.109.145.72:9876",  // NameServer地址
                    "log-group",       // 生产者组
                    "app-logs"         // 默认topic
            );

            if (!initialized) {
                System.out.println("初始化失败");
                return;
            }

            // 2. 推送日志
            for (int i = 0; i < 10; i++) {
                String log = String.format("应用日志 %d - %s", i, new java.util.Date());
                logService.pushLog(log);
            }

            // 3. 直接使用管理器发送重要消息
            manager.createProducer("important-producer", "localhost:9876", "important-group");

            // 同步发送（等待结果）
            SendResult result = manager.sendSync("important-producer", "order-topic",
                    "CREATE", "订单创建消息");
            if (result != null) {
                System.out.println("重要消息发送成功: " + result.getMsgId());
            }

            // 异步发送
            manager.sendAsync("important-producer", "payment-topic", "SUCCESS",
                    "支付成功消息", new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sendResult) {
                            System.out.println("异步消息发送成功: " + sendResult.getMsgId());
                        }

                        @Override
                        public void onException(Throwable e) {
                            System.err.println("异步消息发送失败: " + e.getMessage());
                        }
                    });

            // 等待一段时间
            Thread.sleep(5000);

            // 4. 查看状态
            System.out.println("活跃生产者: " + manager.getActiveProducers());
            System.out.println("日志队列大小: " + logService.getQueueSize());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 5. 关闭资源
            logService.shutdown();
            manager.shutdown();
        }
    }
}
