package com.labdatahub.component.s7_tcp;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S7MessageScheduler {
    public static final Map<String, BlockingQueue<S7Message>> messageQueueMap = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private static final Map<String, ScheduledFuture<?>> configTaskMap = new ConcurrentHashMap<>();
    private static final String CONFIG_KEY_FORMAT = "%s_%s_%s";
    private static final Integer MAX_QUEUE_SIZE = 1000;

    private S7MessageScheduler() {
    	throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    public static void addReadConfig(String componentId, S7ReadConfig readConfig) {
        if (StringUtils.isBlank(componentId)) {
            // componentId为空表示设备未绑定网络组件，无需调度，直接跳过（避免readSwitch等路径抛异常）
            System.err.println("componentId为空，跳过定时配置处理");
            return;
        }
        if (readConfig == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(readConfig.getDeviceSn())) {
            throw new IllegalArgumentException("deviceSn 不能为空");
        }
        if (StringUtils.isBlank(readConfig.getCode())) {
            throw new IllegalArgumentException("code 不能为空");
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
        		BeanUtil.copyProperties(readConfig, message);

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
     * 【静态方法】判断指定读取配置当前是否在定时轮询（用于物模型改动后重建轮询时区分开/关状态）
     * @param componentId 组件ID
     * @param deviceSn 设备SN
     * @param code 指令编码
     * @return true-当前正在轮询
     */
    public static boolean isReadConfigRunning(String componentId, String deviceSn, String code) {
        if (StringUtils.isBlank(componentId) || StringUtils.isBlank(deviceSn) || StringUtils.isBlank(code)) {
            return false;
        }
        String configKey = String.format(CONFIG_KEY_FORMAT, componentId, deviceSn, code);
        return configTaskMap.containsKey(configKey);
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
        if (StringUtils.isBlank(componentId)) {
            return;
        }
        BlockingQueue<S7Message> targetQueue = messageQueueMap.remove(componentId);
        if (targetQueue != null) {
            System.out.printf("已移除componentId=%s 的消息队列，清空消息数：%d%n", componentId, targetQueue.size());
            targetQueue.clear();
        }
    }

    public static void shutdown() {
    	log.info("=== 开始关闭 S7 消息调度器 ===");

        int taskCount = configTaskMap.size();
        log.info("正在取消 {} 个定时任务", taskCount);
        configTaskMap.values().forEach(f -> f.cancel(true));
        configTaskMap.clear();
        // 2. 清空所有队列
        int queueCount = messageQueueMap.size();
        log.info("正在清空 {} 个消息队列", queueCount);
        messageQueueMap.clear();
        // 3. 关闭调度器
        log.info("正在关闭调度器线程池");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
            	log.warn("调度器未能在 5 秒内正常关闭，强制关闭");
                scheduler.shutdownNow();
            } else {
                log.info("调度器线程池已正常关闭");
            }
        } catch (InterruptedException e) {
        	log.error("等待调度器关闭时被中断，强制关闭", e);
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("=== S7 消息调度器已关闭，共取消{}个任务，清空{}个队列 ===", taskCount, queueCount);
    }
}