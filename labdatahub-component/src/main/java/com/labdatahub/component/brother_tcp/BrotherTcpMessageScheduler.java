//由AI修改
package com.labdatahub.component.brother_tcp;

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

/**
 * Brother NC 消息定时生产-消费工具类（静态版+多组件隔离队列）
 * 功能：根据BrotherTcpReadConfig的intervalTime定时生成BrotherTcpMessage，按componentId存入不同队列供消费
 */
public class BrotherTcpMessageScheduler {
    // ========== 静态变量（全局唯一） ==========
    // 多组件隔离的消息队列：key=componentId，value=对应组件的阻塞队列
    public static final Map<String, BlockingQueue<BrotherTcpMessage>> messageQueueMap = new ConcurrentHashMap<>();
    // 调度器：用于执行定时生成消息的任务（静态初始化，全局唯一）
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    // 存储配置与对应定时任务的映射（避免重复调度，支持动态移除）
    // 配置唯一标识：componentId + "_" + deviceSn + "_" + code
    private static final Map<String, ScheduledFuture<?>> configTaskMap = new ConcurrentHashMap<>();
    // 配置唯一标识生成规则：componentId + "_" + deviceSn + "_" + code
    private static final String CONFIG_KEY_FORMAT = "%s_%s_%s";
    //单个网络组件最大的消息堆积值
    private static final Integer MAX_QUEUE_SIZE = 1000;
    // ========== 私有构造器（禁止实例化） ==========
    private BrotherTcpMessageScheduler() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }
    /**
     * 【静态方法】添加Brother读取配置，启动定时生成消息任务
     * @param componentId 组件ID（标识消息队列所属类型，不能为空）
     * @param readConfig 读取配置（必须包含deviceSn、code、intervalTime）
     */
    public static void addReadConfig(String componentId, BrotherTcpReadConfig readConfig) {
        // 1. 核心参数校验（componentId+基础配置）
        if (StringUtils.isBlank(componentId)) {
            // componentId为空表示设备未绑定网络组件，无需调度，直接跳过（避免readSwitch等路径抛异常）
            System.err.println("componentId为空，跳过定时配置处理");
            return;
        }
        if (readConfig == null) {
            throw new IllegalArgumentException("BrotherTcpReadConfig 不能为null");
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
        // 3. 创建定时任务：按intervalTime（秒）循环生成BrotherTcpMessage
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> {
                    // 配置转消息（核心映射逻辑）
                    BrotherTcpMessage message = convertToBrotherTcpMessage(readConfig);
                    // 根据componentId获取/创建对应的队列（原子操作，线程安全）
                    BlockingQueue<BrotherTcpMessage> targetQueue = messageQueueMap.computeIfAbsent(
                            componentId,
                            k -> new LinkedBlockingQueue<>() // 每个componentId对应一个独立队列
                    );
                    // 将消息放入对应组件的队列
                    try {
                        if(targetQueue.size()<=MAX_QUEUE_SIZE){
                            targetQueue.put(message);
                        }
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
        System.out.printf("已添加Brother定时配置：componentId=%s, deviceSn=%s, code=%s, 间隔=%d秒%n",
                componentId, readConfig.getDeviceSn(), readConfig.getCode(), readConfig.getIntervalTime());
    }
    /**
     * 【静态方法】移除指定的Brother读取配置，停止定时生成消息
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
            System.out.printf("已移除Brother定时配置：componentId=%s, deviceSn=%s, code=%s%n",
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
     * @return BrotherTcpMessage
     * @throws InterruptedException 线程中断异常
     * @throws IllegalArgumentException componentId为空或无对应队列时抛出
     */
    public static BrotherTcpMessage takeMessage(String componentId) throws InterruptedException {
        // 参数校验
        if (StringUtils.isBlank(componentId)) {
            throw new IllegalArgumentException("componentId 不能为空");
        }
        // 获取对应组件的队列（不存在则抛异常）
        BlockingQueue<BrotherTcpMessage> targetQueue = messageQueueMap.get(componentId);
        if (targetQueue == null) {
            throw new IllegalArgumentException("componentId=" + componentId + " 无对应的消息队列");
        }
        // 阻塞获取该队列的消息
        return targetQueue.take();
    }
    /**
     * 【静态方法】消费者获取指定组件的消息（非阻塞式，无消息时返回null）
     * @param componentId 组件ID（标识要读取的队列）
     * @return BrotherTcpMessage 或 null
     * @throws IllegalArgumentException componentId为空时抛出
     */
    public static BrotherTcpMessage pollMessage(String componentId) {
        // 参数校验
        if (StringUtils.isBlank(componentId)) {
            throw new IllegalArgumentException("componentId 不能为空");
        }
        // 获取对应组件的队列（不存在则返回null）
        BlockingQueue<BrotherTcpMessage> targetQueue = messageQueueMap.get(componentId);
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
        System.out.println("Brother消息调度器已关闭，所有组件队列已清空");
    }
    /**
     * 【静态方法】BrotherTcpReadConfig 转 BrotherTcpMessage（字段映射）
     */
    private static BrotherTcpMessage convertToBrotherTcpMessage(BrotherTcpReadConfig readConfig) {
        BrotherTcpMessage message = new BrotherTcpMessage();
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
        BlockingQueue<BrotherTcpMessage> targetQueue = messageQueueMap.get(componentId);
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
        BlockingQueue<BrotherTcpMessage> targetQueue = messageQueueMap.remove(componentId);
        if (targetQueue != null) {
            System.out.printf("已移除componentId=%s 的消息队列，清空消息数：%d%n", componentId, targetQueue.size());
            targetQueue.clear();
        }
    }
}
