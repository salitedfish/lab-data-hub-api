package com.labdatahub.business.engine.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 规则引擎缓存（支持多类型数据转发）
 * @Author: ruoyi
 * @CreateTime: 2025-10-30
 */
public class RuleEngineCache {

    /**
     * 数据类型枚举
     */
    public enum DataType {
        DEVICE_LOG("device_log", "设备日志"),
        DEVICE_ALARM("device_alarm", "设备告警"),
        DEVICE_METRICS("device_metrics", "设备指标"),
        DEVICE_STATUS("device_status", "设备状态"),
        DEVICE_EVENT("device_event", "设备事件");

        private final String code;
        private final String desc;

        DataType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static DataType getByCode(String code) {
            for (DataType type : values()) {
                if (type.getCode().equals(code)) {
                    return type;
                }
            }
            return DEVICE_LOG; // 默认返回设备日志
        }
    }

    /**
     * 缓存结构：数据类型 -> 目标类型 -> 设备编号 -> 目标ID列表
     * 例如：DEVICE_LOG -> RABBITMQ -> "device001" -> ["rabbit1", "rabbit2"]
     */
    private static final Map<DataType, Map<TargetType, Map<String, List<String>>>> CACHE = new ConcurrentHashMap<>();
    /**
     * JSON配置缓存
     */
    public static final Map<String, String> JSON_CONFIG_CACHE = new ConcurrentHashMap<>();
    /**
     * 目标类型枚举
     */
    public enum TargetType {
        RABBITMQ("rabbitmq"),
        KAFKA("kafka"),
        ROCKETMQ("rocketmq"),
        HTTP("http"),
        MQTT("MQTT");

        private final String code;

        TargetType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    // 初始化缓存结构
    static {
        for (DataType dataType : DataType.values()) {
            CACHE.put(dataType, new ConcurrentHashMap<>());
            for (TargetType targetType : TargetType.values()) {
                CACHE.get(dataType).put(targetType, new ConcurrentHashMap<>());
            }
        }
    }

    /**
     * 添加转发配置
     * @param dataType 数据类型
     * @param targetType 目标类型
     * @param deviceSn 设备编号
     * @param targetId 目标ID
     */
    public static void addForwardConfig(DataType dataType, TargetType targetType, String deviceSn, String targetId) {
        Map<String, List<String>> deviceTargetMap = CACHE.get(dataType).get(targetType);
        List<String> targetList = deviceTargetMap.computeIfAbsent(deviceSn, k -> new ArrayList<>());

        if (!targetList.contains(targetId)) {
            targetList.add(targetId);
        }
    }

    /**
     * 移除转发配置
     * @param dataType 数据类型
     * @param targetType 目标类型
     * @param targetId 目标ID
     */
    public static void removeForwardConfig(DataType dataType, TargetType targetType, String targetId) {
        Map<String, List<String>> deviceTargetMap = CACHE.get(dataType).get(targetType);
        List<String> removeKeys = new ArrayList<>();

        deviceTargetMap.forEach((deviceSn, targetList) -> {
            targetList.removeIf(o -> o.equals(targetId));
            if (targetList.isEmpty()) {
                removeKeys.add(deviceSn);
            }
        });

        removeKeys.forEach(deviceTargetMap::remove);
    }

    /**
     * 移除设备的某个目标类型的所有转发配置
     * @param dataType 数据类型
     * @param targetType 目标类型
     * @param deviceSn 设备编号
     */
    public static void removeDeviceForwardConfig(DataType dataType, TargetType targetType, String deviceSn) {
        Map<String, List<String>> deviceTargetMap = CACHE.get(dataType).get(targetType);
        deviceTargetMap.remove(deviceSn);
    }

    /**
     * 获取设备的所有转发目标
     * @param dataType 数据类型
     * @param deviceSn 设备编号
     * @return 目标类型到目标ID列表的映射
     */
    public static Map<TargetType, List<String>> getDeviceForwardTargets(DataType dataType, String deviceSn) {
        Map<TargetType, List<String>> result = new HashMap<>();

        for (TargetType targetType : TargetType.values()) {
            Map<String, List<String>> deviceTargetMap = CACHE.get(dataType).get(targetType);
            List<String> targetList = deviceTargetMap.get(deviceSn);
            if (targetList != null && !targetList.isEmpty()) {
                result.put(targetType, new ArrayList<>(targetList));
            }
        }

        return result;
    }

    /**
     * 获取特定数据类型和目标的设备列表
     * @param dataType 数据类型
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 设备编号列表
     */
    public static List<String> getDevicesByTarget(DataType dataType, TargetType targetType, String targetId) {
        List<String> devices = new ArrayList<>();
        Map<String, List<String>> deviceTargetMap = CACHE.get(dataType).get(targetType);

        deviceTargetMap.forEach((deviceSn, targetList) -> {
            if (targetList.contains(targetId)) {
                devices.add(deviceSn);
            }
        });

        return devices;
    }

    /**
     * 检查设备是否有某种数据类型的转发配置
     * @param dataType 数据类型
     * @param deviceSn 设备编号
     * @return 是否有转发配置
     */
    public static boolean hasForwardConfig(DataType dataType, String deviceSn) {
        for (TargetType targetType : TargetType.values()) {
            Map<String, List<String>> deviceTargetMap = CACHE.get(dataType).get(targetType);
            List<String> targetList = deviceTargetMap.get(deviceSn);
            if (targetList != null && !targetList.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 清空所有缓存
     */
    public static void clearAll() {
        for (DataType dataType : DataType.values()) {
            for (TargetType targetType : TargetType.values()) {
                CACHE.get(dataType).get(targetType).clear();
            }
        }
    }

    /**
     * 获取缓存统计信息
     * @return 缓存统计信息
     */
    public static Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        for (DataType dataType : DataType.values()) {
            Map<String, Object> typeStats = new HashMap<>();
            int totalDevices = 0;
            int totalTargets = 0;

            for (TargetType targetType : TargetType.values()) {
                Map<String, List<String>> deviceTargetMap = CACHE.get(dataType).get(targetType);
                int deviceCount = deviceTargetMap.size();
                int targetCount = deviceTargetMap.values().stream().mapToInt(List::size).sum();

                typeStats.put(targetType.getCode() + "_devices", deviceCount);
                typeStats.put(targetType.getCode() + "_targets", targetCount);

                totalDevices += deviceCount;
                totalTargets += targetCount;
            }

            typeStats.put("total_devices", totalDevices);
            typeStats.put("total_targets", totalTargets);
            stats.put(dataType.getCode(), typeStats);
        }

        return stats;
    }

    // ========== 向后兼容的方法 ==========

    /**
     * 向后兼容：添加RabbitMQ配置（默认为设备日志）
     */
    public static void addRabbitMq(String deviceSn, String rabbitId) {
        addForwardConfig(DataType.DEVICE_LOG, TargetType.RABBITMQ, deviceSn, rabbitId);
    }

    /**
     * 向后兼容：添加Kafka配置（默认为设备日志）
     */
    public static void addKafkaMq(String deviceSn, String kafkaId) {
        addForwardConfig(DataType.DEVICE_LOG, TargetType.KAFKA, deviceSn, kafkaId);
    }

    /**
     * 向后兼容：添加RocketMQ配置（默认为设备日志）
     */
    public static void addRocketMq(String deviceSn, String rocketId) {
        addForwardConfig(DataType.DEVICE_LOG, TargetType.ROCKETMQ, deviceSn, rocketId);
    }

    /**
     * 向后兼容：添加HTTP配置（默认为设备日志）
     */
    public static void addHttp(String deviceSn, String httpId) {
        addForwardConfig(DataType.DEVICE_LOG, TargetType.HTTP, deviceSn, httpId);
    }

    /**
     * 添加RabbitMQ配置
     */
    public static void addRabbitMq(String deviceSn,String dataType, String rabbitId) {
        addForwardConfig(DataType.getByCode(dataType), TargetType.RABBITMQ, deviceSn, rabbitId);
    }

    /**
     * 添加Kafka配置
     */
    public static void addKafkaMq(String deviceSn,String dataType, String kafkaId) {
        addForwardConfig(DataType.getByCode(dataType), TargetType.KAFKA, deviceSn, kafkaId);
    }

    /**
     * 添加RocketMQ配置
     */
    public static void addRocketMq(String deviceSn,String dataType, String rocketId) {
        addForwardConfig(DataType.getByCode(dataType), TargetType.ROCKETMQ, deviceSn, rocketId);
    }

    /**
     * 添加HTTP配置
     */
    public static void addHttp(String deviceSn,String dataType, String httpId) {
        addForwardConfig(DataType.getByCode(dataType), TargetType.HTTP, deviceSn, httpId);
    }

    /**
     * 添加HTTP配置
     */
    public static void addMqtt(String deviceSn,String dataType, String mqttId) {
        addForwardConfig(DataType.getByCode(dataType), TargetType.MQTT, deviceSn, mqttId);
    }


    /**
     * 向后兼容：移除RabbitMQ配置（所有数据类型）
     */
    public static void removeRabbitMq(String rabbitId) {
        for (DataType dataType : DataType.values()) {
            removeForwardConfig(dataType, TargetType.RABBITMQ, rabbitId);
        }
    }

    /**
     * 向后兼容：移除Kafka配置（所有数据类型）
     */
    public static void removeKafka(String kafkaId) {
        for (DataType dataType : DataType.values()) {
            removeForwardConfig(dataType, TargetType.KAFKA, kafkaId);
        }
    }

    /**
     * 向后兼容：移除RocketMQ配置（所有数据类型）
     */
    public static void removeRocket(String rocketId) {
        for (DataType dataType : DataType.values()) {
            removeForwardConfig(dataType, TargetType.ROCKETMQ, rocketId);
        }
    }

    /**
     * 向后兼容：移除HTTP配置（所有数据类型）
     */
    public static void removeHttp(String httpId) {
        for (DataType dataType : DataType.values()) {
            removeForwardConfig(dataType, TargetType.HTTP, httpId);
        }
    }

    /**
     * 向后兼容：移除HTTP配置（所有数据类型）
     */
    public static void removeMqtt(String mqttId) {
        for (DataType dataType : DataType.values()) {
            removeForwardConfig(dataType, TargetType.MQTT, mqttId);
        }
    }

    /**
     * json缓存获取
     */
    public static String getJsonCache(String key) {
        return JSON_CONFIG_CACHE.get(key);
    }
    /**
     * json缓存更新
     */
    public static void updateJsonCache(String key, String value) {
        JSON_CONFIG_CACHE.put(key, value);
    }
    /**
     * json缓存删除
     */
    public static void deleteJsonCache(String key) {
        JSON_CONFIG_CACHE.remove(key);
    }
}