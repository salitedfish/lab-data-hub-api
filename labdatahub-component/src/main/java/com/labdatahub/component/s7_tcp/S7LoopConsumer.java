package com.labdatahub.component.s7_tcp;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class S7LoopConsumer {
	// 存储已启动的消费线程：key=componentId，value=消费线程（避免重复启动）
    private static final Map<String, ConsumeThread> CONSUME_THREAD_MAP = new ConcurrentHashMap<>();
    // 消费线程前缀（便于日志排查）
    private static final String CONSUME_THREAD_NAME_PREFIX = "s7-loop-consumer-";

    private static class ConsumeThread extends Thread {
        private final String componentId;
        private final S7MessageConsumeHandler handler;
        private final AtomicBoolean isRunning = new AtomicBoolean(true);

        public ConsumeThread(String componentId, S7MessageConsumeHandler handler) {
            super(CONSUME_THREAD_NAME_PREFIX + componentId);
            this.componentId = componentId;
            this.handler = handler;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            log.info("启动 S7 消费线程 componentId={}", componentId);
            while (isRunning.get()) {
                try {
                	// 阻塞获取指定componentId的消息（无消息时等待，不耗CPU）
                    S7Message message = S7MessageScheduler.takeMessage(componentId);
                    // 调用业务侧的消费逻辑
                    handler.handle(componentId, message);
                } catch (InterruptedException e) {
                    log.info("componentId={} 消费线程被中断，准备停止", componentId);
                    break;
                } catch (Exception e) {
                    log.error("componentId={} 消费消息异常", componentId, e);
                }
            }
            CONSUME_THREAD_MAP.remove(componentId);
            log.info("S7 消费线程已停止 componentId={}", componentId);
        }
        
        

        public void stopConsume() {
            isRunning.set(false);
            this.interrupt();
        }

        public boolean isConsuming() {
            return isRunning.get() && this.isAlive();
        }
    }
    
    /**
     * 启动指定componentId的循环消费
     * @param componentId 目标组件ID（不能为空）
     * @param consumeHandler 消费回调（业务侧自定义消息处理逻辑）
     * @throws IllegalArgumentException 参数非法时抛出
     * @throws IllegalStateException 该componentId已启动消费时抛出
     */
    public static void startConsume(String componentId, S7MessageConsumeHandler handler) {
        if (StringUtils.isBlank(componentId) || handler == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (CONSUME_THREAD_MAP.containsKey(componentId)) {
            ConsumeThread exist = CONSUME_THREAD_MAP.get(componentId);
            if (exist.isConsuming()) {
                throw new IllegalStateException("componentId=" + componentId + " 已启动消费");
            } else {
                CONSUME_THREAD_MAP.remove(componentId);
            }
        }
        ConsumeThread thread = new ConsumeThread(componentId, handler);
        CONSUME_THREAD_MAP.put(componentId, thread);
        thread.start();
    }

    /**
     * 停止指定componentId的循环消费
     * @param componentId 目标组件ID（不能为空）
     */
    public static void stopConsume(String componentId) {
        if (StringUtils.isBlank(componentId)) return;
        ConsumeThread thread = CONSUME_THREAD_MAP.get(componentId);
        if (thread != null && thread.isConsuming()) {
            thread.stopConsume();
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
        ConsumeThread thread = CONSUME_THREAD_MAP.get(componentId);
        return thread != null && thread.isConsuming();
    }

    public static void stopAllConsume() {
        CONSUME_THREAD_MAP.keySet().forEach(S7LoopConsumer::stopConsume);
        CONSUME_THREAD_MAP.clear();
        System.out.println("所有S7消费线程已停止");
    }
}