//由AI修改
package com.labdatahub.component.modbus_tcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Modbus循环消费工具类
 * 功能：单独管理指定componentId的循环消费，支持启动/停止消费，线程安全
 */
@Slf4j
public class ModbusLoopConsumer {
    // 存储已启动的消费线程：key=componentId，value=消费线程（避免重复启动）
    private static final Map<String, ConsumeThread> CONSUME_THREAD_MAP = new ConcurrentHashMap<>();
    // 消费线程前缀（便于日志排查）
    private static final String CONSUME_THREAD_NAME_PREFIX = "modbus-loop-consumer-";
    private static final long STOP_TIMEOUT_MS = 5000L;
    // 消息队列不存在时的重试间隔（毫秒）。
    // 队列是在读配置的定时任务里懒创建的：组件先连上、读配置后到时队列还不存在，
    // 这种情况必须退避等待 —— 直接取消息会「抛异常→立刻重试」空转，实测能把日志刷到 100MB/分钟
    private static final long QUEUE_MISSING_RETRY_MS = 1000L;
    // 连续异常时：前 N 次打全量日志（带堆栈），之后每分钟汇总一条，避免刷屏把磁盘打满
    private static final int FULL_LOG_LIMIT = 3;
    private static final long SUMMARY_LOG_INTERVAL_MS = 60000L;
    // 一条消息都没取到就抛异常时的退避（handler 自己抛的异常已消费掉一条消息，按消息节奏走，不额外等待）
    private static final long NO_MESSAGE_RETRY_MS = 200L;
    // ========== 内部消费线程类（封装循环逻辑+停止标志） ==========
    private static class ConsumeThread extends Thread {
        private final String componentId; // 目标组件ID
        private final ModbusMessageConsumeHandler consumeHandler; // 消费回调
        private final AtomicBoolean isRunning = new AtomicBoolean(true); // 运行标志

        public ConsumeThread(String componentId, ModbusMessageConsumeHandler consumeHandler) {
            super(CONSUME_THREAD_NAME_PREFIX + componentId);
            this.componentId = componentId;
            this.consumeHandler = consumeHandler;
            this.setDaemon(true); // 守护线程：应用退出时自动销毁
        }

        @Override
        public void run() {
            log.info("启动Modbus 循环消费线程 componentId={}", componentId);
            // 连续异常/等待计数 + 上次汇总日志时间（只在本线程内读写，无需同步）
            int consecutiveErrors = 0;
            long lastSummaryAt = 0L;
            while (isRunning.get()) {
                boolean gotMessage = false;
                try {
                    // 队列由读配置的定时任务懒创建，可能尚未存在：不存在就退避等待，不能空转
                    BlockingQueue<ModbusMessage> queue = ModbusMessageScheduler.messageQueueMap.get(componentId);
                    if (queue == null) {
                        consecutiveErrors++;
                        long now = System.currentTimeMillis();
                        if (consecutiveErrors <= FULL_LOG_LIMIT) {
                            log.warn("componentId={} 消息队列尚未创建（读配置未启动），{}ms 后重试",
                                    componentId, QUEUE_MISSING_RETRY_MS);
                        } else if (now - lastSummaryAt >= SUMMARY_LOG_INTERVAL_MS) {
                            lastSummaryAt = now;
                            log.warn("componentId={} 消息队列仍不存在，已连续等待 {} 次（日志按分钟汇总）",
                                    componentId, consecutiveErrors);
                        }
                        if (!sleepQuietly(QUEUE_MISSING_RETRY_MS)) {
                            break; // 等待中被中断 = 要求停止
                        }
                        continue;
                    }
                    // 阻塞获取指定componentId的消息（无消息时等待，不耗CPU）
                    ModbusMessage message = queue.take();
                    gotMessage = true;
                    // 调用业务侧的消费逻辑
                    consumeHandler.handle(componentId,message);
                    if (consecutiveErrors > 0) {
                        log.info("componentId={} 消费已恢复正常（此前连续异常/等待 {} 次）", componentId, consecutiveErrors);
                        consecutiveErrors = 0;
                        lastSummaryAt = 0L;
                    }
                } catch (InterruptedException e) {
                    // 线程被中断，终止循环（优雅停止）
                    log.info("componentId={} 消费线程被中断，准备停止", componentId);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // 消费单条消息异常：打印日志，不中断整个消费循环
                    consecutiveErrors++;
                    long now = System.currentTimeMillis();
                    if (consecutiveErrors <= FULL_LOG_LIMIT) {
                        log.error("componentId={} 消费消息异常（连续第{}次）", componentId, consecutiveErrors, e);
                    } else if (now - lastSummaryAt >= SUMMARY_LOG_INTERVAL_MS) {
                        lastSummaryAt = now;
                        log.error("componentId={} 消费消息已连续异常 {} 次，日志按分钟汇总，最近一次：{}",
                                componentId, consecutiveErrors, e.toString());
                    }
                    // 没取到消息就抛异常（如队列刚好被移除）必须退避，否则又是空转刷日志
                    if (!gotMessage && !sleepQuietly(NO_MESSAGE_RETRY_MS)) {
                        break;
                    }
                }
            }
            // 线程退出，清理映射（仅当映射值仍是本线程时才删除，避免旧线程退出误删新注册的消费线程）
            CONSUME_THREAD_MAP.remove(componentId, this);
            log.info("Modbus 消费线程已停止 componentId={}", componentId);
        }

        /**
         * 可中断的休眠
         * @param ms 休眠毫秒数
         * @return true=正常睡完；false=被中断（调用方应结束循环）
         */
        private boolean sleepQuietly(long ms) {
            try {
                Thread.sleep(ms);
                return true;
            } catch (InterruptedException e) {
                log.info("componentId={} 消费线程在等待中被中断，准备停止", componentId);
                Thread.currentThread().interrupt();
                return false;
            }
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
    public static void startConsume(String componentId, ModbusMessageConsumeHandler consumeHandler) {
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
//    public static void stopConsume(String componentId) {
//        if (StringUtils.isBlank(componentId)) {
//            return;
//        }
//        ConsumeThread consumeThread = CONSUME_THREAD_MAP.get(componentId);
//        if (consumeThread != null && consumeThread.isConsuming()) {
//            consumeThread.stopConsume();
//        }
//    }
    public static void stopConsume(String componentId) {
        if (StringUtils.isBlank(componentId)) {
            return;
        }
        ConsumeThread consumeThread = CONSUME_THREAD_MAP.get(componentId);
        if (consumeThread != null && consumeThread.isConsuming()) {
            consumeThread.stopConsume();
            try {
                consumeThread.join(STOP_TIMEOUT_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("等待 componentId={} 消费线程停止时被中断", componentId, e);
            }
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
        List<String> componentIds = new ArrayList<>(CONSUME_THREAD_MAP.keySet());
        for (String componentId : componentIds) {
            stopConsume(componentId);
        }
        CONSUME_THREAD_MAP.clear();
        log.info("所有 Modbus 循环消费线程已停止，共停止 {} 个线程", componentIds.size());
    }
}