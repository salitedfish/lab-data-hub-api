//由AI修改
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
    /**
     * 队列上限。原值 1000 是按「能吸收多大抖动」拍的，但这是一条<b>实时</b>数据链：
     * 本机 S7 消费速度约 1 条/秒（读一次 0.1~1 秒 + delayTime），1000 条够排十几分钟，
     * 队列里躺着的数据早就不是「实时」了 —— 顶到上限时按最坏情况算，上报的是 15 分钟前的快照。
     * 上限只该用来吸收瞬时抖动：200 条 ≈ 90 秒的量，够用。
     */
    private static final Integer MAX_QUEUE_SIZE = 200;
    /** 队列告警的上次打印时间：key = componentId */
    private static final Map<String, Long> queueFullLogTsMap = new ConcurrentHashMap<>();
    /**
     * 队列告警的节流间隔（毫秒）。
     *
     * <p>原实现每次丢弃都打一条 warn，而队列一旦顶到上限就是<b>每条都丢</b> ——
     * 本机实测刷了 155894 行「消息队列积压」，把 api_run.log 冲得看不出别的问题。
     */
    private static final long QUEUE_FULL_LOG_INTERVAL_MS = 60000;

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
                // 没有消费线程就不产消息。定时任务按「设备点位配置」在 TimerTask 里注册，与组件是否启动
                // 无关；消费线程则由 S7ConnectionManager#addConnection 创建。两者不同步时
                //（组件未启动 / 启动后又停止 / 消费线程已死）消息只进不出：队列按生产速率一路涨到
                // MAX_QUEUE_SIZE，此后永远「丢最旧」，白占内存且每分钟刷一条告警 —— 而那条告警看着像
                // 吞吐不够，实际是压根没人消费。
                // 队列本身仍照常创建：消费线程的启动路径在等它（见 ConsumeThread#run 的「队列尚未创建」分支）。
                if (!S7LoopConsumer.isConsuming(componentId)) {
                    return;
                }
                // 队列满时丢「最旧」的，不是丢「最新」的。
                // 原实现丢最新（size 超限直接 return），于是队列一旦顶到上限就永久丢新消息、
                // 队列里 1000 条老快照一条不动 —— 设备恢复后还要花十几分钟把陈年数据当实时数据上报一遍。
                // 丢最旧则队列始终只保留最近 MAX_QUEUE_SIZE 条，上报的永远是最新的那一段。
                if (queue.size() >= MAX_QUEUE_SIZE) {
                    queue.poll();
                    logQueueFull(componentId);
                }
                boolean offered = queue.offer(message);
                if (!offered) {
                    logQueueFull(componentId);
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

    /**
     * 队列满告警，按组件节流（{@link #QUEUE_FULL_LOG_INTERVAL_MS} 内最多一条）
     *
     * @param componentId 组件ID
     */
    private static void logQueueFull(String componentId) {
        long now = System.currentTimeMillis();
        Long lastLogTs = queueFullLogTsMap.get(componentId);
        if (lastLogTs == null || now - lastLogTs >= QUEUE_FULL_LOG_INTERVAL_MS) {
            queueFullLogTsMap.put(componentId, now);
            log.warn("[S7调度] componentId={} 消息队列已满（上限{}），丢弃最旧消息以保证上报的是最新数据",
                    componentId, MAX_QUEUE_SIZE);
        }
    }

    public static void removeMessageQueue(String componentId) {
        if (StringUtils.isBlank(componentId)) {
            return;
        }
        queueFullLogTsMap.remove(componentId);
        // 取消该组件所有定时生产任务（key 前缀 = componentId_），避免关闭组件后遗留僵尸定时任务
        String prefix = componentId + "_";
        configTaskMap.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(prefix)) {
                ScheduledFuture<?> future = entry.getValue();
                if (future != null) {
                    future.cancel(true);
                }
                return true;
            }
            return false;
        });
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
        queueFullLogTsMap.clear();
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