package com.labdatahub.component.db;

import org.apache.commons.lang3.StringUtils;

import com.labdatahub.component.collect.CollectionEngine;
import com.labdatahub.component.collect.CollectionFamily;
import com.labdatahub.component.collect.CollectionPoint;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据库采集调度门面（全部委托统一采集引擎）
 *
 * <p>数据库采集不按点位 SQL 读，而是「取该数据源最新一条记录」，设备下的点位只是映射到这条
 * 记录的不同字段，所以同一数据源的点位合并成一次查询。本类只保留既有调用点使用的方法名，
 * 内部一律委托 {@link CollectionEngine}。
 */
@Slf4j
public class DatabaseMessageScheduler {

    private DatabaseMessageScheduler() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 注册一个数据库采集点位
     *
     * @param datasourceId 数据源标识（为空表示设备未绑定网络组件，直接跳过）
     * @param readConfig 读取配置（必须包含 deviceSn、intervalTime）
     */
    public static void addReadConfig(String datasourceId, DatabaseReadConfig readConfig) {
        if (StringUtils.isBlank(datasourceId)) {
            // datasourceId为空表示设备未绑定网络组件，无需调度，直接跳过（避免readSwitch等路径抛异常）
            return;
        }
        if (readConfig == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (readConfig.getIntervalTime() == null || readConfig.getIntervalTime() <= 0) {
            throw new IllegalArgumentException("intervalTime 必须为正整数");
        }
        CollectionEngine.addPoint(datasourceId, CollectionFamily.DATABASE, new CollectionPoint(
                datasourceId, readConfig.getDeviceSn(), readConfig.getCode(),
                readConfig.getIntervalTime(), null, null, null, null, null, readConfig));
    }

    /**
     * 移除数据库采集点位
     *
     * <p>code 为空表示移除该设备下的全部点位（数据库采集按设备整体调度，调用方常传 null）。
     *
     * @param datasourceId 数据源标识
     * @param deviceSn 设备SN
     * @param code 点位编码；为空表示移除该设备的全部点位
     */
    public static void removeReadConfig(String datasourceId, String deviceSn, String code) {
        CollectionEngine.removePoint(datasourceId, deviceSn, code);
    }

    /**
     * 移除组件的全部采集调度与队列
     *
     * @param datasourceId 数据源标识
     */
    public static void removeMessageQueue(String datasourceId) {
        if (StringUtils.isBlank(datasourceId)) {
            return;
        }
        CollectionEngine.removeComponent(datasourceId);
    }

    /**
     * 关闭本协议族的全部采集调度与消费线程（应用关闭时调用）
     */
    public static void shutdown() {
        CollectionEngine.shutdownFamily(CollectionFamily.DATABASE);
        log.info("数据库采集调度已关闭");
    }
}
