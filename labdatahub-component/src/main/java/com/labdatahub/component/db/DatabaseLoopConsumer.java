package com.labdatahub.component.db;

import org.apache.commons.lang3.StringUtils;

import com.labdatahub.component.collect.CollectionEngine;
import com.labdatahub.component.collect.CollectionFamily;

/**
 * 数据库采集循环消费门面（全部委托统一采集引擎）
 *
 * <p>消费线程由统一采集引擎按组件创建：一条线程串行执行该组件的全部读取任务。
 * 本类只保留既有调用点使用的方法名。
 */
public class DatabaseLoopConsumer {

    private DatabaseLoopConsumer() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 启动指定数据源的循环消费
     *
     * @param datasourceId 数据源标识（不能为空）
     * @throws IllegalArgumentException datasourceId 为空时抛出
     */
    public static void startConsume(String datasourceId) {
        CollectionEngine.startConsumer(datasourceId, CollectionFamily.DATABASE);
    }

    /**
     * 停止指定数据源的循环消费
     *
     * @param datasourceId 数据源标识
     */
    public static void stopConsume(String datasourceId) {
        if (StringUtils.isBlank(datasourceId)) {
            return;
        }
        CollectionEngine.stopConsumer(datasourceId);
    }

    /**
     * 检查指定数据源是否正在消费
     *
     * @param datasourceId 数据源标识
     * @return true=消费线程存活
     */
    public static boolean isConsuming(String datasourceId) {
        return !StringUtils.isBlank(datasourceId) && CollectionEngine.isConsuming(datasourceId);
    }

    /**
     * 停止本协议族的全部消费线程（应用关闭时调用）
     */
    public static void stopAllConsume() {
        CollectionEngine.shutdownFamily(CollectionFamily.DATABASE);
    }
}
