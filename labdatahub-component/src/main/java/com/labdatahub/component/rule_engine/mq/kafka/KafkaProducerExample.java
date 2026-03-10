package com.labdatahub.component.rule_engine.mq.kafka;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-10-21
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 使用示例
 */
public class KafkaProducerExample {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerExample.class);

    public static void main(String[] args) {
        // 获取生产者管理器实例
        KafkaProducerManager manager = KafkaProducerManager.getInstance();
        LogPushService logService = new LogPushService();

        try {
            // 1. 初始化日志推送服务
            boolean initialized = logService.initialize(
                    "log-producer",
                    "47.109.145.72:9092"  // 请替换为你的Kafka服务器地址
            );

            if (!initialized) {
                logger.error("Failed to initialize log push service");
                return;
            }

            // 2. 推送一些测试日志
            for (int i = 0; i < 10; i++) {
                String logMessage = String.format(
                        "{\"timestamp\": %d, \"level\": \"INFO\", \"message\": \"Application log message %d\", \"service\": \"test-service\"}",
                        System.currentTimeMillis(), i
                );

                logService.pushLog("app-logs", "log-key-" + i, logMessage);
            }

//            // 3. 动态创建另一个生产者
//            Properties customProps = new Properties();
//            customProps.put("acks", "all"); // 等待所有副本确认
//            customProps.put("retries", 5); // 更多重试次数
//
//            manager.createProducer("backup-producer", "47.109.145.72:9092", customProps);
//
//            // 4. 直接使用管理器发送重要日志（同步方式）
//            String importantLog = "{\"level\": \"ERROR\", \"message\": \"Critical error occurred\", \"timestamp\": " + System.currentTimeMillis() + "}";
//            boolean sent = manager.sendLogSync("backup-producer", "error-logs", importantLog);
//            if (sent) {
//                logger.info("Critical log sent successfully");
//            }
//
//            // 5. 批量发送日志
//            List<String> batchLogs = new ArrayList<>();
//            for (int i = 0; i < 5; i++) {
//                batchLogs.add("Batch log message " + i + " at " + System.currentTimeMillis());
//            }
//            manager.sendLogsBatch("log-producer", "batch-logs", batchLogs);

            // 等待一段时间让异步消息处理

            // 6. 查看活跃的生产者
            System.out.println("Active producers: " + manager.getActiveProducers());
            System.out.println("Log queue size: " + logService.getQueueSize());

            // 模拟持续运行

        } catch (Exception e) {
            logger.error("Error in example", e);
        } finally {
            // 7. 清理资源
//            logService.shutdown();
//            manager.shutdown();
        }
    }
}
