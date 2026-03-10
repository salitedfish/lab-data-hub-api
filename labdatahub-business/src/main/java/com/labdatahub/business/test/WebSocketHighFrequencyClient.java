package com.labdatahub.business.test;

import com.alibaba.fastjson.JSONObject;

import javax.websocket.*;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * WS压力测试工具
 */
public class WebSocketHighFrequencyClient {

    // 统计计数器
    private static final AtomicInteger sendCounter = new AtomicInteger(0);
    private static final AtomicLong startTime = new AtomicLong(0);
    private static final AtomicLong totalSendTime = new AtomicLong(0);
    private static final AtomicInteger deviceSnCounter = new AtomicInteger(0);  // 在这里定义
    public static void main(String[] args) throws Exception {
        // WebSocket服务器地址（这里使用公共测试服务器）
        String wsUrl = "ws://localhost:10883/ws";  // 替换为你的WS地址
        // 或者使用测试服务器：wss://ws.postman-echo.com/raw

        try {
            // 创建WebSocket容器
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            // 每秒发送消息数量
            int messagesPerSecond = 2000;

            // 创建会话
            Session session = container.connectToServer(
                    new Endpoint(),
                    URI.create(wsUrl)
            );

            System.out.println("WebSocket连接成功，开始发送数据...");
            System.out.println("目标频率: " + messagesPerSecond + " 条/秒");

            // 启动发送任务
            startSending(session, messagesPerSecond);

            // 保持主线程运行
            keepRunning();

        } catch (Exception e) {
            System.err.println("连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 启动异步发送任务
     */
    private static void startSending(Session session, int messagesPerSecond) {
        // 使用ScheduledExecutorService进行精确控制
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        // 计算每批发送的数量和间隔
        int batchSize = 1000;  // 每批发送100条
        int batchesPerSecond = messagesPerSecond / batchSize;  // 每秒批次

        if (batchesPerSecond == 0) {
            batchesPerSecond = 1;
            batchSize = messagesPerSecond;
        }

        final int finalBatchSize = batchSize;
        long intervalMillis = 1000L / batchesPerSecond;

        System.out.println("配置: batchSize=" + finalBatchSize +
                ", batchesPerSecond=" + batchesPerSecond +
                ", interval=" + intervalMillis + "ms");

        startTime.set(System.currentTimeMillis());

        // 定时发送任务
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (session.isOpen()) {
                    // 批量发送
                    long batchStart = System.currentTimeMillis();
                    for (int i = 0; i < finalBatchSize; i++) {
                        String message = generateMessage();
                        session.getAsyncRemote().sendText(message);
                        sendCounter.incrementAndGet();
                    }
                    long batchEnd = System.currentTimeMillis();
                    totalSendTime.addAndGet(batchEnd - batchStart);

                    // 每秒输出一次统计信息
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime.get() >= 1000) {
                        long elapsedSeconds = (currentTime - startTime.get()) / 1000;
                        if (elapsedSeconds > 0) {
                            int totalSent = sendCounter.get();
                            double avgSendTime = (double) totalSendTime.get() / totalSent;

                            System.out.printf("[%ds] 已发送: %d 条, 平均发送时间: %.2fms, 当前频率: %.1f 条/秒%n",
                                    elapsedSeconds, totalSent, avgSendTime,
                                    (double) totalSent / elapsedSeconds);
                        }
                    }
                } else {
                    System.out.println("WebSocket连接已关闭，停止发送");
                    scheduler.shutdown();
                }
            } catch (Exception e) {
                System.err.println("发送数据异常: " + e.getMessage());
                e.printStackTrace();
                scheduler.shutdown();
            }
        }, 0, intervalMillis, TimeUnit.MILLISECONDS);

        // 监控线程
        scheduler.scheduleWithFixedDelay(() -> {
            System.out.println("[监控] 连接状态: " + (session.isOpen() ? "已连接" : "已断开"));
            System.out.println("[监控] 活动线程数: " + Thread.activeCount());
        }, 5, 5, TimeUnit.SECONDS);
    }

    /**
     * 生成测试消息
     */
    private static String generateMessage() {
        JSONObject message = new JSONObject();
        message.put("timestamp", System.currentTimeMillis());
        message.put("id", sendCounter.get() + 1);
        message.put("data", "test_data_" + System.nanoTime());
        message.put("sequence", sendCounter.get());
        message.put("temperature", 20 + Math.random() * 10);
        message.put("windSpeed", 20 + Math.random() * 10);
        message.put("humidity", 40 + Math.random() * 20);
        message.put("pressure", 980 + Math.random() * 40);
        // 生成 deviceSn (WS_001 到 WS_100 循环)
        int deviceNumber = deviceSnCounter.incrementAndGet() % 5000 + 1;
        String deviceSn = String.format("WS_%03d", deviceNumber);
        message.put("deviceSn", deviceSn);
        return message.toJSONString();
    }

    /**
     * WebSocket客户端端点
     */
    @ClientEndpoint
    public static class Endpoint {

        @OnOpen
        public void onOpen(Session session) {
            System.out.println("WebSocket连接已建立");
            System.out.println("Session ID: " + session.getId());
            System.out.println("协议版本: " + session.getProtocolVersion());
        }

        @OnMessage
        public void onMessage(String message) {
            // 这里处理服务器返回的消息
            // 如果不需要处理返回消息，可以留空或简单记录
            if (sendCounter.get() % 1000 == 0) {
                System.out.println("收到服务器响应: " +
                        (message.length() > 50 ? message.substring(0, 50) + "..." : message));
            }
        }

        @OnClose
        public void onClose(Session session, CloseReason reason) {
            System.out.println("连接关闭: " + reason.getReasonPhrase());
            System.out.println("关闭代码: " + reason.getCloseCode());
            printFinalStatistics();
        }

        @OnError
        public void onError(Session session, Throwable throwable) {
            System.err.println("WebSocket错误: " + throwable.getMessage());
            throwable.printStackTrace();
        }
    }

    /**
     * 保持主线程运行
     */
    private static void keepRunning() {
        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            printFinalStatistics();
            System.out.println("程序退出");
        }));

        // 保持运行，直到按Ctrl+C
        try {
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            printFinalStatistics();
        }
    }

    /**
     * 打印最终统计信息
     */
    private static void printFinalStatistics() {
        long totalTime = System.currentTimeMillis() - startTime.get();
        if (totalTime > 0) {
            int totalSent = sendCounter.get();
            System.out.println("\n========== 最终统计 ==========");
            System.out.println("总运行时间: " + totalTime + "ms");
            System.out.println("总发送消息数: " + totalSent);
            System.out.println("平均频率: " +
                    String.format("%.2f", (double) totalSent / (totalTime / 1000.0)) + " 条/秒");
            System.out.println("平均发送时间: " +
                    String.format("%.2f", (double) totalSendTime.get() / totalSent) + "ms");
            System.out.println("=============================");
        }
    }
}
