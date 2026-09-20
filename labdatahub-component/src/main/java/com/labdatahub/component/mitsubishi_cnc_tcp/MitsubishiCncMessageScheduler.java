//由AI修改
package com.labdatahub.component.mitsubishi_cnc_tcp;

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

/**
 * 三菱 CNC TCP 消息定时生产-消费工具类（静态版+多组件隔离队列）
 * 功能：根据MitsubishiCncReadConfig的intervalTime定时生成MitsubishiCncMessage，按componentId存入不同队列供消费
 */
@Slf4j
public class MitsubishiCncMessageScheduler {
    // ========== 静态变量（全局唯一） ==========
    // 多组件隔离的消息队列：key=componentId，value=对应组件的阻塞队列
    public static final Map<String, BlockingQueue<MitsubishiCncMessage>> messageQueueMap = new ConcurrentHashMap<>();
    // 调度器：用于执行定时生成消息的任务（静态初始化，全局唯一）
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    // 存储配置与对应定时任务的映射（避免重复调度，支持动态移除）
    // 配置唯一标识：componentId + "_" + deviceSn + "_" + code
    private static final Map<String, ScheduledFuture<?>> configTaskMap = new ConcurrentHashMap<>();
    // 配置唯一标识生成规则：componentId + "_" + deviceSn + "_" + code
    private static final String CONFIG_KEY_FORMAT = "%s_%s_%s";
    //单个网络组件最大的消息堆积值
    private static final Integer MAX_QUEUE_SIZE = 1000;
    /** 队列告警的上次打印时间：key = componentId（队列一满就是每条都丢，不节流会刷爆日志） */
    private static final Map<String, Long> queueFullLogTsMap = new ConcurrentHashMap<>();
    /** 队列告警的节流间隔（毫秒） */
    private static final long QUEUE_FULL_LOG_INTERVAL_MS = 60000;
    // ========== 私有构造器（禁止实例化） ==========
    private MitsubishiCncMessageScheduler() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }
    /**
     * 【静态方法】添加三菱 CNC 读取配置，启动定时生成消息任务
     * @param componentId 组件ID（标识消息队列所属类型，不能为空）
     * @param readConfig 读取配置（必须包含deviceSn、code、intervalTime）
     */
    public static void addReadConfig(String componentId, MitsubishiCncReadConfig readConfig) {
        // 1. 核心参数校验（componentId+基础配置）
        if (StringUtils.isBlank(componentId)) {
            // componentId为空表示设备未绑定网络组件，无需调度，直接跳过（避免readSwitch等路径抛异常）
            System.err.println("componentId为空，跳过定时配置处理");
            return;
        }
        if (readConfig == null) {
            throw new IllegalArgumentException("MitsubishiCncReadConfig 不能为null");
        }
        if (StringUtils.isBlank(readConfig.getDeviceSn())) {
            throw new IllegalArgumentException("deviceSn 不能为空");
        }
        if (StringUtils.isBlank(readConfig.getCode())) {
            throw new IllegalArgumentException("code 不能为空");
        }
        if (readConfig.getIntervalTime() == null || readConfig.getIntervalTime() <= 0) {
            throw new IllegalArgumentException("intervalTime 必须为正整数（单位：秒）");
        }
        // 2. 生成配置唯一标识（包含componentId，避免跨组件冲突）
        String configKey = String.format(CONFIG_KEY_FORMAT, componentId, readConfig.getDeviceSn(), readConfig.getCode());
        // 若已存在该配置，先取消旧任务
        if (configTaskMap.containsKey(configKey)) {
            removeReadConfig(componentId, readConfig.getDeviceSn(), readConfig.getCode());
        }
        // 3. 创建定时任务：按intervalTime（秒）循环生成MitsubishiCncMessage
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> {
                    // 配置转消息（核心映射逻辑）
                    MitsubishiCncMessage message = convertToMitsubishiCncMessage(readConfig);
                    // 根据componentId获取/创建对应的队列（原子操作，线程安全）
                    BlockingQueue<MitsubishiCncMessage> targetQueue = messageQueueMap.computeIfAbsent(
                            componentId,
                            k -> new LinkedBlockingQueue<>() // 每个componentId对应一个独立队列
                    );
                    // 没有消费线程就不产消息。定时任务按「设备点位配置」在 TimerTask 里注册，与组件是否启动
                    // 无关；消费线程则由 MitsubishiCncConnectionManager#addConnection 创建。两者不同步时
                    //（组件未启动 / 启动后又停止 / 消费线程已死）消息只进不出：队列按生产速率一路涨到
                    // MAX_QUEUE_SIZE，此后永远「丢最旧」，白占内存且每分钟刷一条告警 —— 而那条告警看着像
                    // 吞吐不够，实际是压根没人消费。
                    // 队列本身仍照常创建：消费线程的启动路径在等它（见 ConsumeThread#run 的「队列尚未创建」分支）。
                    if (!MitsubishiCncLoopConsumer.isConsuming(componentId)) {
                        return;
                    }
                    // 将消息放入对应组件的队列
                    try {
                        // 队列满时丢「最旧」的，不是丢「最新」的：队列顶到上限说明生产已快于消费，
                        // 丢新会让那批陈旧快照永远排不空、此后每条新消息都被静默丢弃（原实现连日志都没有）；
                        // 丢最旧则队列始终只保留最近 MAX_QUEUE_SIZE 条，上报的永远是最新的那一段。
                        if (targetQueue.size() >= MAX_QUEUE_SIZE) {
                            targetQueue.poll();
                            logQueueFull(componentId);
                        }
                        targetQueue.put(message);
                    } catch (InterruptedException e) {
                        // 中断时恢复线程中断状态，不影响任务
                        Thread.currentThread().interrupt();
                        System.err.printf("componentId=%s 消息入队被中断：%s%n", componentId, e.getMessage());
                    }
                },
                0, // 初始延迟：立即执行第一次
                readConfig.getIntervalTime(), // 间隔时间（秒）
                TimeUnit.SECONDS
        );
        // 4. 记录配置与任务的映射
        configTaskMap.put(configKey, future);
        System.out.printf("已添加MC-CNC定时配置：componentId=%s, deviceSn=%s, code=%s, 间隔=%d秒%n",
                componentId, readConfig.getDeviceSn(), readConfig.getCode(), readConfig.getIntervalTime());
    }
    /**
     * 【静态方法】移除指定的三菱 CNC 读取配置，停止定时生成消息
     * @param componentId 组件ID（不能为空）
     * @param deviceSn 设备SN
     * @param code 指令编码
     */
    public static void removeReadConfig(String componentId, String deviceSn, String code) {
        if (StringUtils.isBlank(componentId)) {
            // componentId为空表示设备未绑定网络组件，无需调度，直接跳过（避免readSwitch等路径抛异常）
            System.err.println("componentId为空，跳过定时配置处理");
            return;
        }
        if (StringUtils.isBlank(deviceSn) || StringUtils.isBlank(code)) {
            throw new IllegalArgumentException("deviceSn和code不能为空");
        }
        String configKey = String.format(CONFIG_KEY_FORMAT, componentId, deviceSn, code);
        ScheduledFuture<?> future = configTaskMap.get(configKey);
        if (future != null) {
            future.cancel(true); // 取消定时任务
            configTaskMap.remove(configKey);
            System.out.printf("已移除MC-CNC定时配置：componentId=%s, deviceSn=%s, code=%s%n",
                    componentId, deviceSn, code);
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
     * @return MitsubishiCncMessage
     * @throws InterruptedException 线程中断异常
     * @throws IllegalArgumentException componentId为空或无对应队列时抛出
     */
    public static MitsubishiCncMessage takeMessage(String componentId) throws InterruptedException {
        // 参数校验
        if (StringUtils.isBlank(componentId)) {
            throw new IllegalArgumentException("componentId 不能为空");
        }
        // 获取对应组件的队列（不存在则抛异常）
        BlockingQueue<MitsubishiCncMessage> targetQueue = messageQueueMap.get(componentId);
        if (targetQueue == null) {
            throw new IllegalArgumentException("componentId=" + componentId + " 无对应的消息队列");
        }
        // 阻塞获取该队列的消息
        return targetQueue.take();
    }
    /**
     * 【静态方法】消费者获取指定组件的消息（非阻塞式，无消息时返回null）
     * @param componentId 组件ID（标识要读取的队列）
     * @return MitsubishiCncMessage 或 null
     * @throws IllegalArgumentException componentId为空时抛出
     */
    public static MitsubishiCncMessage pollMessage(String componentId) {
        // 参数校验
        if (StringUtils.isBlank(componentId)) {
            throw new IllegalArgumentException("componentId 不能为空");
        }
        // 获取对应组件的队列（不存在则返回null）
        BlockingQueue<MitsubishiCncMessage> targetQueue = messageQueueMap.get(componentId);
        if (targetQueue == null) {
            return null;
        }
        // 非阻塞获取该队列的消息
        return targetQueue.poll();
    }
    /**
     * 【静态方法】关闭工具类，释放所有资源（应用关闭时调用）
     */
    public static void shutdown() {
        // 1. 取消所有定时任务
        configTaskMap.values().forEach(future -> future.cancel(true));
        configTaskMap.clear();
        // 2. 清空所有队列
        messageQueueMap.clear();
        // 3. 关闭调度器
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow(); // 强制关闭
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        System.out.println("MC-CNC消息调度器已关闭，所有组件队列已清空");
    }
    /**
     * 【静态方法】MitsubishiCncReadConfig 转 MitsubishiCncMessage（字段映射）
     */
    private static MitsubishiCncMessage convertToMitsubishiCncMessage(MitsubishiCncReadConfig readConfig) {
        MitsubishiCncMessage message = new MitsubishiCncMessage();
        BeanUtil.copyProperties(readConfig, message);
        return message;
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
        BlockingQueue<MitsubishiCncMessage> targetQueue = messageQueueMap.get(componentId);
        return targetQueue == null ? 0 : targetQueue.size();
    }
    /**
     * 【静态方法】移除指定组件的队列（清空消息+删除队列）
     * @param componentId 组件ID
     */
    /**
     * 队列满告警，按组件节流（{@link #QUEUE_FULL_LOG_INTERVAL_MS} 内最多一条）。
     *
     * <p>原实现队列满时直接丢弃新消息、不留任何痕迹。而队列一旦顶到上限就说明<b>生产已经快于消费</b>，
     * 那批陈旧快照永远排不空，于是<b>之后每条新消息都被静默丢弃</b> —— 平台照常跑、页面照常显示、
     * 转发照常发生，只是数据越来越旧，且没有任何线索可查。至少要让它能被发现。
     */
    private static void logQueueFull(String componentId) {
        long now = System.currentTimeMillis();
        Long lastLogTs = queueFullLogTsMap.get(componentId);
        if (lastLogTs == null || now - lastLogTs >= QUEUE_FULL_LOG_INTERVAL_MS) {
            queueFullLogTsMap.put(componentId, now);
            log.warn("[CNC调度] componentId={} 消息队列已满（上限{}），丢弃最旧消息以保证上报的是最新数据",
                    componentId, MAX_QUEUE_SIZE);
        }
    }

    public static void removeMessageQueue(String componentId) {
        if (StringUtils.isBlank(componentId)) {
            return;
        }
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
        BlockingQueue<MitsubishiCncMessage> targetQueue = messageQueueMap.remove(componentId);
        if (targetQueue != null) {
            System.out.printf("已移除componentId=%s 的消息队列，清空消息数：%d%n", componentId, targetQueue.size());
            targetQueue.clear();
        }
    }
}
