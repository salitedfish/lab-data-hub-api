//由AI修改
package com.labdatahub.component.s7_tcp;

import org.apache.commons.lang3.StringUtils;

import com.labdatahub.component.collect.CollectionEngine;
import com.labdatahub.component.collect.CollectionFamily;

/**
 * S7 循环消费门面（全部委托统一采集引擎）
 *
 * <p>消费线程由统一采集引擎按组件创建：一条线程串行执行该组件的全部读取任务，
 * 一次读取覆盖一批点位（合并读取）。本类只保留既有调用点使用的方法名。
 */
public class S7LoopConsumer {

    private S7LoopConsumer() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 启动指定组件的循环消费
     *
     * @param componentId 组件ID（不能为空）
     * @throws IllegalArgumentException componentId 为空时抛出
     */
    public static void startConsume(String componentId) {
        CollectionEngine.startConsumer(componentId, CollectionFamily.S7);
    }

    /**
     * 停止指定组件的循环消费
     *
     * @param componentId 组件ID
     */
    public static void stopConsume(String componentId) {
        if (StringUtils.isBlank(componentId)) {
            return;
        }
        CollectionEngine.stopConsumer(componentId);
    }

    /**
     * 检查指定组件是否正在消费
     *
     * @param componentId 组件ID
     * @return true=消费线程存活
     */
    public static boolean isConsuming(String componentId) {
        return !StringUtils.isBlank(componentId) && CollectionEngine.isConsuming(componentId);
    }

    /**
     * 停止本协议族的全部消费线程（应用关闭时调用）
     */
    public static void stopAllConsume() {
        CollectionEngine.shutdownFamily(CollectionFamily.S7);
    }
}
