package com.labdatahub.component.collect.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.labdatahub.component.collect.CollectionAdapter;
import com.labdatahub.component.collect.CollectionFamily;
import com.labdatahub.component.collect.CollectionPoint;
import com.labdatahub.component.collect.ReadTask;
import com.labdatahub.component.mitsubishi_mc3e_tcp.MitsubishiMc3eConnectionManager;
import com.labdatahub.component.mitsubishi_mc3e_tcp.MitsubishiMc3eDataReader;
import com.labdatahub.component.mitsubishi_mc3e_tcp.MitsubishiMc3eReadConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * 三菱 MC 3E（MITSUBISHI_MC3E_TCP / MITSUBISHI_MC3E_UDP）采集适配器
 *
 * <p>字设备（D/W/R/ZR/SD）按「同设备 + 同软元件 + 连续地址」合并成一次批量读（单次 ≤960 点）；
 * 位设备（M/L/B/X/Y/S/SM/F）逐点读。字内位点位（D150.0）仍按整字读、再取位，与旧链路一致。
 */
@Slf4j
public class Mc3eCollectionAdapter implements CollectionAdapter {

    @Override
    public String family() {
        return CollectionFamily.MC3E;
    }

    @Override
    public String unitKey(CollectionPoint point) {
        MitsubishiMc3eReadConfig config = config(point);
        return point.getDeviceSn() + "|" + config.getAreaCode() + "|" + (isWordDevice(config) ? "W" : "B");
    }

    @Override
    public List<ReadTask> split(String componentId, String unitKey, int intervalSeconds,
                                List<CollectionPoint> points) {
        List<CollectionPoint> valid = new ArrayList<>();
        for (CollectionPoint point : points) {
            MitsubishiMc3eReadConfig config = config(point);
            if (config.getAreaCode() == null || config.getStartAddress() == null
                    || config.getLength() == null) {
                log.warn("[统一采集] componentId={} 点位{}配置不完整（areaCode/startAddress/length 为空），跳过",
                        componentId, point.getCode());
                continue;
            }
            valid.add(point);
        }
        if (valid.isEmpty()) {
            return Collections.emptyList();
        }
        List<ReadTask> tasks = new ArrayList<>();
        if (!isWordDevice(config(valid.get(0)))) {
            // 位设备：逐点读（位设备按位访问，各点位地址独立）
            for (CollectionPoint point : valid) {
                MitsubishiMc3eReadConfig config = config(point);
                McPlan plan = new McPlan(config.getAreaCode(), config.getStartAddress(),
                        config.getLength(), point.getCode());
                tasks.add(new ReadTask(componentId, family(),
                        unitKey + "@" + intervalSeconds + "@" + point.getCode(), unitKey,
                        intervalSeconds, Collections.singletonList(point), plan));
            }
            return tasks;
        }
        List<RangeMerge.Span> spans = new ArrayList<>();
        for (CollectionPoint point : valid) {
            MitsubishiMc3eReadConfig config = config(point);
            int start = config.getStartAddress();
            spans.add(new RangeMerge.Span(start, start + config.getLength() - 1, point));
        }
        List<RangeMerge.Cluster> clusters = RangeMerge.cluster(spans,
                MitsubishiMc3eDataReader.MAX_WORD_COUNT, CollectionFamily.mergeGap(CollectionFamily.MC3E));
        MitsubishiMc3eReadConfig first = config(valid.get(0));
        for (RangeMerge.Cluster cluster : clusters) {
            List<CollectionPoint> taskPoints = new ArrayList<>();
            for (RangeMerge.Span span : cluster.spans) {
                taskPoints.add(span.point);
            }
            McPlan plan = new McPlan(first.getAreaCode(), cluster.start, cluster.size(), null);
            // 任务键必须带间隔：同一采集单元在多个间隔下各有一份定时任务，键相同会互相覆盖
            tasks.add(new ReadTask(componentId, family(),
                    unitKey + "@" + intervalSeconds + "@" + cluster.start, unitKey,
                    intervalSeconds, taskPoints, plan));
        }
        return tasks;
    }

    @Override
    public Map<String, Object> execute(String componentId, ReadTask task) throws Exception {
        // TCP 看长连接、UDP 看数据报 socket，统一由连接管理器判定
        if (!MitsubishiMc3eConnectionManager.hasConnection(componentId)) {
            return Collections.emptyMap();
        }
        McPlan plan = (McPlan) task.getPlan();
        List<Integer> data = readWithLock(componentId, plan);
        Map<String, Object> values = new HashMap<>();
        if (plan.pointCode != null) {
            values.put(task.getPoints().get(0).key(), data);
            return values;
        }
        for (CollectionPoint point : task.getPoints()) {
            MitsubishiMc3eReadConfig config = config(point);
            int offset = config.getStartAddress() - plan.start;
            int size = config.getLength();
            if (offset < 0 || offset + size > data.size()) {
                log.warn("[统一采集] componentId={} 点位{}地址区间超出本次读取范围，跳过",
                        componentId, point.getCode());
                continue;
            }
            List<Integer> slice = new ArrayList<>(data.subList(offset, offset + size));
            Integer bitIndex = config.getBitIndex();
            if (bitIndex != null) {
                // 字内位点位（D150.0）：三菱对字设备不支持按位读，读整字后取位
                if (bitIndex < 0 || bitIndex > 15) {
                    log.warn("[统一采集] componentId={} 点位{}的bitIndex={}越界（只允许0-15），跳过",
                            componentId, point.getCode(), bitIndex);
                    continue;
                }
                if (slice.isEmpty()) {
                    continue;
                }
                values.put(point.key(), Collections.singletonList((slice.get(0) >> bitIndex) & 0x01));
                continue;
            }
            values.put(point.key(), slice);
        }
        return values;
    }

    @Override
    public JSONObject buildPayload(ReadTask task, CollectionPoint point, Object raw) {
        JSONObject payload = new JSONObject();
        payload.put("deviceSn", point.getDeviceSn());
        payload.put("code", point.getCode());
        payload.put("jsonArray", JSONArray.from((List<?>) raw));
        payload.put("dataType", point.getDataType());
        payload.put("byteOrder", point.getByteOrder());
        payload.put("isSigned", point.getIsSigned());
        payload.put("scale", point.getScale());
        payload.put("offset", point.getOffset());
        return payload;
    }

    /**
     * 持锁读取；任何异常都视为连接可疑并强制重连（与旧链路一致）
     */
    private List<Integer> readWithLock(String componentId, McPlan plan) throws Exception {
        ReentrantLock lock = MitsubishiMc3eConnectionManager.getLock(componentId);
        boolean needReconnect = false;
        lock.lock();
        try {
            return MitsubishiMc3eDataReader.readMemoryArea(componentId, plan.areaCode, plan.start,
                    plan.count);
        } catch (Exception e) {
            log.warn("componentId={} 读取失败，触发强制重连", componentId);
            needReconnect = true;
            throw e;
        } finally {
            lock.unlock();
            // forceReconnect 内部会真建 TCP + 指数退避 sleep，必须在 unlock 之后
            if (needReconnect) {
                MitsubishiMc3eConnectionManager.forceReconnect(componentId);
            }
        }
    }

    /**
     * 是否字设备
     *
     * @param config 读取配置
     * @return true=字设备
     */
    private static boolean isWordDevice(MitsubishiMc3eReadConfig config) {
        return config.getAreaCode() != null && MitsubishiMc3eDataReader.isWordDevice(config.getAreaCode());
    }

    private static MitsubishiMc3eReadConfig config(CollectionPoint point) {
        return (MitsubishiMc3eReadConfig) point.getSource();
    }

    /**
     * 读取块描述
     */
    private static final class McPlan {

        /** 软元件代码 */
        private final int areaCode;

        /** 起始地址 */
        private final int start;

        /** 读取点数 */
        private final int count;

        /** 位设备逐点任务对应的点位编码（字设备任务为 null） */
        private final String pointCode;

        McPlan(int areaCode, int start, int count, String pointCode) {
            this.areaCode = areaCode;
            this.start = start;
            this.count = count;
            this.pointCode = pointCode;
        }
    }
}
