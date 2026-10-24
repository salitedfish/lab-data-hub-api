package com.labdatahub.component.collect;

import java.util.Collections;
import java.util.List;

/**
 * 一次读取任务：同一单元（同一从站/同一数据区/同一条 FOCAS 调用）里、同间隔的一批点位
 *
 * <p>{@link #plan} 是适配器自己的拆块结果（如 Modbus 的「起始地址+数量」），
 * 引擎不解析，只原样交回给同一个适配器。
 */
public class ReadTask {

    /** 所属网络组件ID */
    private final String componentId;

    /** 协议族 */
    private final String family;

    /** 任务唯一键：同一组件内去重/防重复排队用 */
    private final String taskKey;

    /** 采集单元键（由适配器定义） */
    private final String unitKey;

    /** 采集间隔（秒） */
    private final int intervalSeconds;

    /** 本任务覆盖的点位 */
    private final List<CollectionPoint> points;

    /** 适配器自己的拆块描述 */
    private final Object plan;

    public ReadTask(String componentId, String family, String taskKey, String unitKey,
                    int intervalSeconds, List<CollectionPoint> points, Object plan) {
        this.componentId = componentId;
        this.family = family;
        this.taskKey = taskKey;
        this.unitKey = unitKey;
        this.intervalSeconds = intervalSeconds;
        this.points = Collections.unmodifiableList(points);
        this.plan = plan;
    }

    public String getComponentId() {
        return componentId;
    }

    public String getFamily() {
        return family;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public String getUnitKey() {
        return unitKey;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public List<CollectionPoint> getPoints() {
        return points;
    }

    public Object getPlan() {
        return plan;
    }
}
