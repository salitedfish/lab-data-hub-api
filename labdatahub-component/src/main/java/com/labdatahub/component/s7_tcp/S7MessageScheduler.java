//由AI修改
package com.labdatahub.component.s7_tcp;

import org.apache.commons.lang3.StringUtils;

import com.labdatahub.component.collect.CollectionEngine;
import com.labdatahub.component.collect.CollectionFamily;
import com.labdatahub.component.collect.CollectionPoint;

import lombok.extern.slf4j.Slf4j;

/**
 * S7 采集调度门面（全部委托统一采集引擎）
 *
 * <p>所有协议统一走「点位按采集单元分组 → 合并读取 → 每组件单消费线程」的统一采集引擎，
 * 不再保留旧链路的定时消息队列。本类只保留既有调用点使用的方法名，内部一律委托
 * {@link CollectionEngine}。
 */
@Slf4j
public class S7MessageScheduler {

    private S7MessageScheduler() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 注册一个 S7 采集点位
     *
     * @param componentId 组件ID（为空表示设备未绑定网络组件，直接跳过）
     * @param readConfig 读取配置（必须包含 deviceSn、code、dbNumber、intervalTime）
     */
    public static void addReadConfig(String componentId, S7ReadConfig readConfig) {
        if (StringUtils.isBlank(componentId)) {
            // componentId为空表示设备未绑定网络组件，无需调度，直接跳过（避免readSwitch等路径抛异常）
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
        CollectionEngine.addPoint(componentId, CollectionFamily.S7, new CollectionPoint(
                componentId, readConfig.getDeviceSn(), readConfig.getCode(),
                readConfig.getIntervalTime(), readConfig.getDataType(), readConfig.getByteOrder(),
                readConfig.getIsSigned(), readConfig.getScale(), readConfig.getOffset(), readConfig));
    }

    /**
     * 移除一个 S7 采集点位
     *
     * @param componentId 组件ID
     * @param deviceSn 设备SN
     * @param code 点位编码
     */
    public static void removeReadConfig(String componentId, String deviceSn, String code) {
        CollectionEngine.removePoint(componentId, deviceSn, code);
    }

    /**
     * 判断指定点位当前是否在轮询（物模型改动后重建轮询时用于区分开/关状态）
     *
     * @param componentId 组件ID
     * @param deviceSn 设备SN
     * @param code 点位编码
     * @return true=已注册
     */
    public static boolean isReadConfigRunning(String componentId, String deviceSn, String code) {
        if (StringUtils.isBlank(componentId) || StringUtils.isBlank(deviceSn) || StringUtils.isBlank(code)) {
            return false;
        }
        return CollectionEngine.hasPoint(componentId, deviceSn, code);
    }

    /**
     * 移除组件的全部采集调度与队列
     *
     * @param componentId 组件ID
     */
    public static void removeMessageQueue(String componentId) {
        if (StringUtils.isBlank(componentId)) {
            return;
        }
        CollectionEngine.removeComponent(componentId);
    }

    /**
     * 关闭本协议族的全部采集调度与消费线程（应用关闭时调用）
     */
    public static void shutdown() {
        CollectionEngine.shutdownFamily(CollectionFamily.S7);
        log.info("S7 采集调度已关闭");
    }
}
