package com.labdatahub.component.collect;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSONObject;

/**
 * 协议适配器：统一采集引擎与各协议读法的分界
 *
 * <p>引擎负责「什么时候读、读失败怎么办、读完怎么上报」，适配器负责
 * 「怎么把点位拆成一次读取、怎么读、读到的原始值怎么组装成协议包要求的 JSON」。
 *
 * <p>⚠️ {@link #execute} <b>必须运行在消费线程上</b>：FOCAS2 的连接句柄绑定建连线程，
 * 换线程读会返回 EW_BUSY(-8)。
 */
public interface CollectionAdapter {

    /**
     * 协议族名（见 {@link CollectionFamily}）
     *
     * @return 协议族名
     */
    String family();

    /**
     * 采集单元键：相同键的点位在一次读取里可以合并
     *
     * @param point 点位
     * @return 单元键（适配器自定义，必须包含设备SN）
     */
    String unitKey(CollectionPoint point);

    /**
     * 把一个单元（同间隔）内的点位拆成若干读取任务
     *
     * @param componentId 组件ID
     * @param unitKey 采集单元键
     * @param intervalSeconds 采集间隔（秒）
     * @param points 本单元本间隔的全部点位
     * @return 读取任务列表（至少一个）
     */
    List<ReadTask> split(String componentId, String unitKey, int intervalSeconds, List<CollectionPoint> points);

    /**
     * 执行一次读取（在消费线程上）
     *
     * @param componentId 组件ID
     * @param task 读取任务
     * @return 点位唯一键（{@link CollectionPoint#key()}）→ 原始值；读不到的点位不放进结果
     * @throws Exception 读取失败（连接级异常由适配器自行重连）
     */
    Map<String, Object> execute(String componentId, ReadTask task) throws Exception;

    /**
     * 组装协议解码所需的 JSON（与旧链路同样字段，协议 jar 无需改动）
     *
     * @param task 读取任务
     * @param point 点位
     * @param raw {@link #execute} 给出的原始值
     * @return 送去协议解码的 JSON；返回 null 表示本次跳过该点位
     */
    JSONObject buildPayload(ReadTask task, CollectionPoint point, Object raw);

    /**
     * 组件调试页（WebSocket "component" 频道）展示的内容
     *
     * <p>默认与解码 JSON 一致；Modbus 旧链路展示的是寄存器块数组，见实现类。
     *
     * @param task 读取任务
     * @param point 点位
     * @param raw 原始值
     * @return 组件频道文本
     */
    default String buildComponentPayload(ReadTask task, CollectionPoint point, Object raw) {
        return buildPayload(task, point, raw).toJSONString();
    }

    /**
     * 是否整条任务只上报一次（数据库采集：一个数据源一份最新记录，点位只是映射到同一条记录上）
     *
     * @return true=每个任务上报一次；false=每个点位各上报一次
     */
    default boolean publishOncePerTask() {
        return false;
    }

    /**
     * 消费线程空闲时的连接保障钩子（目前只有 FOCAS2 需要：句柄必须在消费线程上建立）
     *
     * @param componentId 组件ID
     */
    default void ensureConnection(String componentId) {
    }

    /**
     * 读取失败后的协议侧清理（如把该点位的待读任务从旧队列里摘掉）
     *
     * @param componentId 组件ID
     * @param task 失败的任务
     * @param e 异常
     */
    default void onFailure(String componentId, ReadTask task, Exception e) {
    }
}
