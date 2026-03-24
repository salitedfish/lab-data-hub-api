package com.labdatahub.component.s7_tcp;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class S7MessageScheduler {
    public static final Map<String, BlockingQueue<S7Message>> messageQueueMap = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private static final Map<String, ScheduledFuture<?>> configTaskMap = new ConcurrentHashMap<>();
    private static final String CONFIG_KEY_FORMAT = "%s_%s_%s";
    private static final Integer MAX_QUEUE_SIZE = 1000;

    private S7MessageScheduler() {}

    public static void addReadConfig(String componentId, S7ReadConfig readConfig) {
        if (StringUtils.isBlank(componentId) || readConfig == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (readConfig.getDbNumber() == null || readConfig.getDbNumber() <= 0) {
            throw new IllegalArgumentException("dbNumber 必须为正整数");
        }
        if (readConfig.getIntervalTime() == null || readConfig.getIntervalTime() <= 0) {
            throw new IllegalArgumentException("intervalTime 必须为正整数");
        }

        String configKey = String.format(CONFIG_KEY_FORMAT, componentId, readConfig.getDeviceSn(), readConfig.getCode());
        if (configTaskMap.containsKey(configKey)) {
            removeReadConfig(componentId, readConfig.getDeviceSn(), readConfig.getCode());
        }

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
        	try {
        		S7Message message = new S7Message();
                message.setDeviceSn(readConfig.getDeviceSn());
                message.setCode(readConfig.getCode());
                message.setDbNumber(readConfig.getDbNumber());
                message.setStartAddress(readConfig.getStartAddress());
                message.setLength(readConfig.getLength());
                message.setDelayTime(readConfig.getDelayTime());

                BlockingQueue<S7Message> queue = messageQueueMap.computeIfAbsent(componentId, k -> new LinkedBlockingQueue<>());
                int currentSize = queue.size();
                if (currentSize > MAX_QUEUE_SIZE) {
                    log.warn("[S7调度] componentId={} 消息队列积压，当前大小={}, 超过限制{}，丢弃消息", componentId, currentSize, MAX_QUEUE_SIZE);
                    return; // 丢弃本次消息
                }
                if (currentSize > MAX_QUEUE_SIZE * 0.8) {
                    log.warn("[S7调度] componentId={} 消息队列接近满载，当前大小={}", componentId, currentSize);
                }
                boolean offered = queue.offer(message);
                if (!offered) {
                    log.warn("[S7调度] componentId={} 消息入队失败（队列已满），丢弃消息", componentId);
                }
        	} catch (Exception e) {
                log.error("[S7调度] componentId={} 定时生成消息异常", componentId, e);
            }
            
        }, 0, readConfig.getIntervalTime(), TimeUnit.SECONDS);

        configTaskMap.put(configKey, future);
        log.info("已添加S7定时配置：componentId={}, deviceSn={}, code={}, 间隔={}", componentId,
        		readConfig.getDeviceSn(), readConfig.getCode(), readConfig.getIntervalTime());
        //System.out.printf("已添加S7定时配置：componentId=%s, deviceSn=%s, code=%s, 间隔=%d秒%n",
                //componentId, readConfig.getDeviceSn(), readConfig.getCode(), readConfig.getIntervalTime());
    }

    public static void removeReadConfig(String componentId, String deviceSn, String code) {
        String configKey = String.format(CONFIG_KEY_FORMAT, componentId, deviceSn, code);
        ScheduledFuture<?> future = configTaskMap.get(configKey);
        if (future != null) {
            future.cancel(true);
            configTaskMap.remove(configKey);
            log.info("已移除S7定时配置：componentId={}, deviceSn={}, code={}", componentId, deviceSn, code);
            //System.out.printf("已移除S7定时配置：componentId=%s, deviceSn=%s, code=%s%n", componentId, deviceSn, code);
        }
    }

    /**
     * 【静态方法】消费者获取指定组件的消息（阻塞式，无消息时等待）
     * @param componentId 组件ID（标识要读取的队列）
     * @return S7Message
     * @throws InterruptedException 线程中断异常
     * @throws IllegalArgumentException componentId为空或无对应队列时抛出
     */
    public static S7Message takeMessage(String componentId) throws InterruptedException {
    	// 参数校验
        if (StringUtils.isBlank(componentId)) {
            throw new IllegalArgumentException("componentId 不能为空");
        }
        BlockingQueue<S7Message> queue = messageQueueMap.get(componentId);
        if (queue == null) {
            throw new IllegalArgumentException("componentId=" + componentId + " 无对应消息队列");
        }
        return queue.take();
    }

    public static S7Message pollMessage(String componentId) {
    	// 参数校验
        if (StringUtils.isBlank(componentId)) {
            throw new IllegalArgumentException("componentId 不能为空");
        }
        BlockingQueue<S7Message> queue = messageQueueMap.get(componentId);
        return queue == null ? null : queue.poll();
    }

    public static void removeMessageQueue(String componentId) {
        BlockingQueue<S7Message> queue = messageQueueMap.remove(componentId);
        if (queue != null) queue.clear();
    }

    public static void shutdown() {
        configTaskMap.values().forEach(f -> f.cancel(true));
        configTaskMap.clear();
        messageQueueMap.clear();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        log.info("S7消息调度器已关闭");
        //System.out.println("S7消息调度器已关闭");
    }
}