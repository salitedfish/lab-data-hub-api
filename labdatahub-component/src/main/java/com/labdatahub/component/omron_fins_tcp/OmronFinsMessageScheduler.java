package com.labdatahub.component.omron_fins_tcp;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

public class OmronFinsMessageScheduler {
    // 消息队列映射：key=componentId，value=阻塞队列
    public static final Map<String, BlockingQueue<OmronFinsMessage>> messageQueueMap = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private static final Map<String, ScheduledFuture<?>> configTaskMap = new ConcurrentHashMap<>();
    private static final String CONFIG_KEY_FORMAT = "%s_%s_%s";
    private static final Integer MAX_QUEUE_SIZE = 1000;

    private OmronFinsMessageScheduler() {}

    public static void addReadConfig(String componentId, OmronFinsReadConfig readConfig) {
        if (StringUtils.isBlank(componentId) || readConfig == null) {
            throw new IllegalArgumentException("参数无效");
        }
        if (StringUtils.isBlank(readConfig.getDeviceSn()) ||
                StringUtils.isBlank(readConfig.getCode()) ||
                readConfig.getIntervalTime() == null || readConfig.getIntervalTime() <= 0) {
            throw new IllegalArgumentException("配置项缺失或非法");
        }

        String configKey = String.format(CONFIG_KEY_FORMAT, componentId, readConfig.getDeviceSn(), readConfig.getCode());
        if (configTaskMap.containsKey(configKey)) {
            removeReadConfig(componentId, readConfig.getDeviceSn(), readConfig.getCode());
        }

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            OmronFinsMessage message = convertToMessage(readConfig);
            BlockingQueue<OmronFinsMessage> queue = messageQueueMap.computeIfAbsent(componentId, k -> new LinkedBlockingQueue<>());
            try {
                if (queue.size() <= MAX_QUEUE_SIZE) {
                    queue.put(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.printf("componentId=%s 消息入队被中断%n", componentId);
            }
        }, 0, readConfig.getIntervalTime(), TimeUnit.SECONDS);

        configTaskMap.put(configKey, future);
        System.out.printf("已添加FINS定时配置：componentId=%s, deviceSn=%s, code=%s, 间隔=%d秒%n",
                componentId, readConfig.getDeviceSn(), readConfig.getCode(), readConfig.getIntervalTime());
    }

    public static void removeReadConfig(String componentId, String deviceSn, String code) {
        String configKey = String.format(CONFIG_KEY_FORMAT, componentId, deviceSn, code);
        ScheduledFuture<?> future = configTaskMap.remove(configKey);
        if (future != null) {
            future.cancel(true);
        }
    }

    public static OmronFinsMessage takeMessage(String componentId) throws InterruptedException {
        BlockingQueue<OmronFinsMessage> queue = messageQueueMap.get(componentId);
        if (queue == null) {
            throw new IllegalArgumentException("未找到componentId对应的消息队列");
        }
        return queue.take();
    }

    public static OmronFinsMessage pollMessage(String componentId) {
        BlockingQueue<OmronFinsMessage> queue = messageQueueMap.get(componentId);
        return queue == null ? null : queue.poll();
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
    }

    private static OmronFinsMessage convertToMessage(OmronFinsReadConfig config) {
        OmronFinsMessage msg = new OmronFinsMessage();
        msg.setSlaveId(config.getSlaveId());
        msg.setDeviceSn(config.getDeviceSn());
        msg.setCode(config.getCode());
        msg.setRegisterRange(config.getRegisterRange());
        msg.setDelayTime(config.getDelayTime());
        return msg;
    }
    
    /**
     * 【静态方法】获取指定组件队列的当前消息数
     * @param componentId 组件ID
     * @return 队列消息数（无对应队列返回0）
     */
    public static int getMessageQueueSize(String componentId) {
        if (StringUtils.isBlank(componentId)) {
            return 0;
        }
        BlockingQueue<OmronFinsMessage> targetQueue = messageQueueMap.get(componentId);
        return targetQueue == null ? 0 : targetQueue.size();
    }

    /**
     * 【静态方法】移除指定组件的队列（清空消息+删除队列）
     * @param componentId 组件ID
     */
    public static void removeMessageQueue(String componentId) {
        if (StringUtils.isBlank(componentId)) {
            return;
        }
        BlockingQueue<OmronFinsMessage> targetQueue = messageQueueMap.remove(componentId);
        if (targetQueue != null) {
            System.out.printf("已移除componentId=%s 的消息队列，清空消息数：%d%n", componentId, targetQueue.size());
            targetQueue.clear();
        }
    }
}