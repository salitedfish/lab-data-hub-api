package com.labdatahub.component.db;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
* @ClassName: DatabaseMessageScheduler  
* @Description: 消息调度器
* @author xwb  
* @date 2026年4月1日
 */
@Slf4j
public class DatabaseMessageScheduler {

    // 消息队列：key = datasourceId
    public static final Map<String, BlockingQueue<DatabaseMessage>> messageQueueMap = new ConcurrentHashMap<>();
    // 定时任务存储：key = configKey (datasourceId_deviceSn_code)
    private static final Map<String, ScheduledFuture<?>> configTaskMap = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5,
            r -> {
                Thread t = new Thread(r, "db-scheduler-thread");
                t.setDaemon(true);
                return t;
            });
    private static final int MAX_QUEUE_SIZE = 1000;
    //private static final String CONFIG_KEY_FORMAT = "%s_%s_%s";
    private static final String CONFIG_KEY_FORMAT = "%s_%s";

    /**
     * 添加定时读取配置
     * @param datasourceId 数据源标识
     * @param readConfig 读取配置
     */
    public static void addReadConfig(String datasourceId, DatabaseReadConfig readConfig) {
        if (StringUtils.isBlank(datasourceId)) {
            // datasourceId为空表示设备未绑定网络组件，无需调度，直接跳过（避免readSwitch等路径抛异常）
            System.err.println("datasourceId为空，跳过定时配置处理");
            return;
        }
        if (readConfig == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
//        if (readConfig.getSql() == null || readConfig.getSql().trim().isEmpty()) {
//            throw new IllegalArgumentException("SQL 不能为空");
//        }
        if (readConfig.getIntervalTime() == null || readConfig.getIntervalTime() <= 0) {
            throw new IllegalArgumentException("intervalTime 必须为正整数");
        }

        //String configKey = String.format(CONFIG_KEY_FORMAT, datasourceId, readConfig.getDeviceSn(), readConfig.getCode());
        String configKey = String.format(CONFIG_KEY_FORMAT, datasourceId, readConfig.getDeviceSn());
        // 若已存在则先移除
        if (configTaskMap.containsKey(configKey)) {
            removeReadConfig(datasourceId, readConfig.getDeviceSn(), readConfig.getCode());
        }

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                DatabaseMessage message = new DatabaseMessage();
                message.setDeviceSn(readConfig.getDeviceSn());
                message.setCode(readConfig.getCode());
                message.setSql(readConfig.getSql());
                message.setParameters(readConfig.getParameters());
                message.setDelayTime(readConfig.getDelayTime());

                BlockingQueue<DatabaseMessage> queue = messageQueueMap.computeIfAbsent(datasourceId,
                        k -> new LinkedBlockingQueue<>());
                // 没有消费线程就不产消息。定时任务按「设备点位配置」在 TimerTask 里注册，与组件是否启动
                // 无关；消费线程则由 DatabaseConnectionManager#addConnection 创建。两者不同步时
                //（组件未启动 / 启动后又停止 / 消费线程已死）消息只进不出：队列按生产速率一路涨到
                // MAX_QUEUE_SIZE，此后永远「丢最旧」，白占内存且每分钟刷一条告警 —— 而那条告警看着像
                // 吞吐不够，实际是压根没人消费。
                // 队列本身仍照常创建：消费线程的启动路径在等它（见 ConsumeThread#run 的「队列尚未创建」分支）。
                if (!DatabaseLoopConsumer.isConsuming(datasourceId)) {
                    return;
                }
                int currentSize = queue.size();
                if (currentSize > MAX_QUEUE_SIZE) {
                    log.warn("[DB调度] 数据源 {} 消息队列积压，当前大小={}, 超过限制{}，丢弃消息",
                            datasourceId, currentSize, MAX_QUEUE_SIZE);
                    return;
                }
                if (currentSize > MAX_QUEUE_SIZE * 0.8) {
                    log.warn("[DB调度] 数据源 {} 消息队列接近满载，当前大小={}", datasourceId, currentSize);
                }
                boolean offered = queue.offer(message);
                if (!offered) {
                    log.warn("[DB调度] 数据源 {} 消息入队失败（队列已满），丢弃消息", datasourceId);
                }
            } catch (Exception e) {
                log.error("[DB调度] 数据源 {} 定时生成消息异常", datasourceId, e);
            }
        }, 0, readConfig.getIntervalTime(), TimeUnit.SECONDS);

        configTaskMap.put(configKey, future);
        log.info("已添加数据库定时配置：datasourceId={}, deviceSn={}, code={}, 间隔={}秒",
                datasourceId, readConfig.getDeviceSn(), readConfig.getCode(), readConfig.getIntervalTime());
    }

    /**
     * 移除定时配置
     */
    public static void removeReadConfig(String datasourceId, String deviceSn, String code) {
        //String configKey = String.format(CONFIG_KEY_FORMAT, datasourceId, deviceSn, code);
        String configKey = String.format(CONFIG_KEY_FORMAT, datasourceId, deviceSn);
        ScheduledFuture<?> future = configTaskMap.remove(configKey);
        if (future != null) {
            future.cancel(true);
            log.info("已移除数据库定时配置：datasourceId={}, deviceSn={}, code={}",
                    datasourceId, deviceSn, code);
        }
    }

    /**
     * 消费者阻塞获取消息
     */
    public static DatabaseMessage takeMessage(String datasourceId) throws InterruptedException {
        if (StringUtils.isBlank(datasourceId)) {
            throw new IllegalArgumentException("datasourceId 不能为空");
        }
        BlockingQueue<DatabaseMessage> queue = messageQueueMap.get(datasourceId);
        if (queue == null) {
            throw new IllegalArgumentException("数据源 " + datasourceId + " 无对应消息队列");
        }
        return queue.take();
    }

    /**
     * 非阻塞获取消息
     */
    public static DatabaseMessage pollMessage(String datasourceId) {
        if (StringUtils.isBlank(datasourceId)) {
            return null;
        }
        BlockingQueue<DatabaseMessage> queue = messageQueueMap.get(datasourceId);
        return queue == null ? null : queue.poll();
    }

    /**
     * 移除消息队列
     */
    public static void removeMessageQueue(String datasourceId) {
        if (StringUtils.isBlank(datasourceId)) {
            return;
        }
        BlockingQueue<DatabaseMessage> queue = messageQueueMap.remove(datasourceId);
        if (queue != null) {
            log.info("已移除数据源 {} 的消息队列，清空消息数：{}", datasourceId, queue.size());
            queue.clear();
        }
    }

    /**
     * 关闭调度器
     */
    public static void shutdown() {
        log.info("=== 开始关闭数据库消息调度器 ===");
        int taskCount = configTaskMap.size();
        log.info("正在取消 {} 个定时任务", taskCount);
        configTaskMap.values().forEach(f -> f.cancel(true));
        configTaskMap.clear();

        int queueCount = messageQueueMap.size();
        log.info("正在清空 {} 个消息队列", queueCount);
        messageQueueMap.clear();

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("调度器未能在5秒内正常关闭，强制关闭");
                scheduler.shutdownNow();
            } else {
                log.info("调度器线程池已正常关闭");
            }
        } catch (InterruptedException e) {
            log.error("等待调度器关闭时被中断，强制关闭", e);
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("=== 数据库消息调度器已关闭，共取消{}个任务，清空{}个队列 ===", taskCount, queueCount);
    }
}