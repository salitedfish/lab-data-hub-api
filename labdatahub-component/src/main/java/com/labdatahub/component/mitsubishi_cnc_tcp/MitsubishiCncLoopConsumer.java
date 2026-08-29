//由AI修改
package com.labdatahub.component.mitsubishi_cnc_tcp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

/**
 * 三菱 CNC TCP 循环消费工具类
 * 功能：单独管理指定componentId的循环消费，支持启动/停止消费，线程安全
 */
public class MitsubishiCncLoopConsumer {
    // 存储已启动的消费线程：key=componentId，value=消费线程（避免重复启动）
    private static final Map<String, ConsumeThread> CONSUME_THREAD_MAP = new ConcurrentHashMap<>();
    // 消费线程前缀（便于日志排查）
    private static final String CONSUME_THREAD_NAME_PREFIX = "mitsubishi-cnc-loop-consumer-";
    // ========== 内部消费线程类（封装循环逻辑+停止标志） ==========
    private static class ConsumeThread extends Thread {
        private final String componentId; // 目标组件ID
        private final MitsubishiCncMessageConsumeHandler consumeHandler; // 消费回调
        private final AtomicBoolean isRunning = new AtomicBoolean(true); // 运行标志
        public ConsumeThread(String componentId, MitsubishiCncMessageConsumeHandler consumeHandler) {
            super(CONSUME_THREAD_NAME_PREFIX + componentId);
            this.componentId = componentId;
            this.consumeHandler = consumeHandler;
            this.setDaemon(true); // 守护线程，应用退出时自动销毁
        }
        @Override
        public void run() {
            System.out.printf("启动componentId=%s的循环消费线程%n", componentId);
            while (isRunning.get()) {
                try {
                    // 阻塞获取指定componentId的消息（无消息时等待，不耗CPU）
                    MitsubishiCncMessage message = MitsubishiCncMessageScheduler.takeMessage(componentId);
                    // 调用业务侧的消费逻辑
                    consumeHandler.handle(componentId,message);
                } catch (InterruptedException e) {
                    // 线程被中断，终止循环（优雅停止）
                    System.out.printf("componentId=%s的消费线程被中断，准备停止%n", componentId);
                    break;
                } catch (Exception e) {
                    // 消费单条消息异常：打印一行摘要日志，不打印完整堆栈（断连期间消息积压会反复触发，避免刷屏）
                    System.err.printf("componentId=%s消费消息失败：%s：%s%n", componentId, e.getClass().getSimpleName(), e.getMessage());
                }
            }
            // 线程退出，清理映射（按值移除，避免误删并发重新启动的新线程映射）
            CONSUME_THREAD_MAP.remove(componentId, this);
            System.out.printf("componentId=%s的循环消费线程已停止%n", componentId);
        }
        /**
         * 优雅停止消费线程
         */
        public void stopConsume() {
            isRunning.set(false);
            this.interrupt(); // 中断阻塞的takeMessage()方法
        }
        /**
         * 检查消费线程是否在运行
         */
        public boolean isConsuming() {
            return isRunning.get() && this.isAlive();
        }
    }
    // ========== 公开方法 ==========
    /**
     * 启动指定componentId的循环消费
     * @param componentId 目标组件ID（不能为空）
     * @param consumeHandler 消费回调（业务侧自定义消息处理逻辑）
     * @throws IllegalArgumentException 参数非法时抛出
     * @throws IllegalStateException 该componentId已启动消费时抛出
     */
    public static void startConsume(String componentId, MitsubishiCncMessageConsumeHandler consumeHandler) {
        // 1. 参数校验
        if (StringUtils.isBlank(componentId)) {
            throw new IllegalArgumentException("componentId 不能为空");
        }
        if (consumeHandler == null) {
            throw new IllegalArgumentException("消费回调handler 不能为空");
        }
        // 2. 检查是否已启动消费（避免重复）
        if (CONSUME_THREAD_MAP.containsKey(componentId)) {
            ConsumeThread existThread = CONSUME_THREAD_MAP.get(componentId);
            if (existThread.isConsuming()) {
                throw new IllegalStateException("componentId=" + componentId + " 已启动循环消费，请勿重复启动");
            } else {
                CONSUME_THREAD_MAP.remove(componentId); // 清理已停止的无效线程
            }
        }
        // 3. 创建并启动消费线程
        ConsumeThread consumeThread = new ConsumeThread(componentId, consumeHandler);
        CONSUME_THREAD_MAP.put(componentId, consumeThread);
        consumeThread.start();
    }
    /**
     * 停止指定componentId的循环消费
     * @param componentId 目标组件ID（不能为空）
     */
    public static void stopConsume(String componentId) {
        if (StringUtils.isBlank(componentId)) {
            return;
        }
        ConsumeThread consumeThread = CONSUME_THREAD_MAP.get(componentId);
        if (consumeThread != null && consumeThread.isConsuming()) {
            consumeThread.stopConsume();
        }
    }
    /**
     * 检查指定componentId是否正在消费
     * @param componentId 目标组件ID
     * @return true=正在消费，false=未消费/已停止
     */
    public static boolean isConsuming(String componentId) {
        if (StringUtils.isBlank(componentId)) {
            return false;
        }
        ConsumeThread consumeThread = CONSUME_THREAD_MAP.get(componentId);
        return consumeThread != null && consumeThread.isConsuming();
    }
    /**
     * 停止所有componentId的循环消费（应用关闭时调用）
     */
    public static void stopAllConsume() {
        for (String componentId : CONSUME_THREAD_MAP.keySet()) {
            stopConsume(componentId);
        }
        CONSUME_THREAD_MAP.clear();
        System.out.println("所有MC-CNC循环消费线程已停止");
    }
}
