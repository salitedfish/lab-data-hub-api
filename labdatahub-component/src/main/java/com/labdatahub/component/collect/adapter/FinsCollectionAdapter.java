package com.labdatahub.component.collect.adapter;

import java.net.Socket;
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
import com.labdatahub.component.fins_tcp.FinsConnectionManager;
import com.labdatahub.component.fins_tcp.FinsDataReader;
import com.labdatahub.component.fins_tcp.FinsReadConfig;
import com.labdatahub.component.fins_tcp.FinsResponseException;

import lombok.extern.slf4j.Slf4j;

/**
 * OMRONFINS_TCP 采集适配器
 *
 * <p>字区点位按「同设备 + 同区码 + 连续地址」合并成一次内存区读（单次 ≤1000 项）；
 * 位区点位每点地址各带一个位号（地址第 3 字节），无法合并，保持逐点读。
 *
 * <p>FINS 是同步问答，读与写共用一条 socket，必须持组件锁；PLC 明确拒绝（结束码非 0）不重连，
 * 传输层异常（超时/断流）才重连 —— 这条区分不能丢，否则一个配置错误会让连接反复抖动。
 */
@Slf4j
public class FinsCollectionAdapter implements CollectionAdapter {

    @Override
    public String family() {
        return CollectionFamily.FINS;
    }

    @Override
    public String unitKey(CollectionPoint point) {
        FinsReadConfig config = config(point);
        return point.getDeviceSn() + "|" + config.getAreaCode() + "|" + (isBitArea(config) ? "B" : "W");
    }

    @Override
    public List<ReadTask> split(String componentId, String unitKey, int intervalSeconds,
                                List<CollectionPoint> points) {
        List<CollectionPoint> valid = new ArrayList<>();
        for (CollectionPoint point : points) {
            FinsReadConfig config = config(point);
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
        if (isBitArea(config(valid.get(0)))) {
            // 位区：每个点位有自己的位号，逐点读
            for (CollectionPoint point : valid) {
                FinsReadConfig config = config(point);
                FinsPlan plan = new FinsPlan(config.getAreaCode(), config.getStartAddress(),
                        config.getBitAddress(), config.getLength(), point.getCode());
                tasks.add(new ReadTask(componentId, family(),
                        unitKey + "@" + intervalSeconds + "@" + point.getCode(), unitKey,
                        intervalSeconds, Collections.singletonList(point), plan));
            }
            return tasks;
        }
        // 字区：按起始地址合并
        List<RangeMerge.Span> spans = new ArrayList<>();
        for (CollectionPoint point : valid) {
            FinsReadConfig config = config(point);
            int start = config.getStartAddress();
            spans.add(new RangeMerge.Span(start, start + config.getLength() - 1, point));
        }
        List<RangeMerge.Cluster> clusters = RangeMerge.cluster(spans, FinsDataReader.MAX_ITEMS,
                CollectionFamily.mergeGap(CollectionFamily.FINS));
        FinsReadConfig first = config(valid.get(0));
        for (RangeMerge.Cluster cluster : clusters) {
            List<CollectionPoint> taskPoints = new ArrayList<>();
            for (RangeMerge.Span span : cluster.spans) {
                taskPoints.add(span.point);
            }
            FinsPlan plan = new FinsPlan(first.getAreaCode(), cluster.start, null, cluster.size(), null);
            // 任务键必须带间隔：同一采集单元在多个间隔下各有一份定时任务，键相同会互相覆盖
            tasks.add(new ReadTask(componentId, family(),
                    unitKey + "@" + intervalSeconds + "@" + cluster.start, unitKey,
                    intervalSeconds, taskPoints, plan));
        }
        return tasks;
    }

    @Override
    public Map<String, Object> execute(String componentId, ReadTask task) throws Exception {
        Socket connection = FinsConnectionManager.getConnection(componentId);
        if (connection == null || connection.isClosed() || !connection.isConnected()) {
            return Collections.emptyMap();
        }
        FinsPlan plan = (FinsPlan) task.getPlan();
        List<Integer> data = readWithLock(componentId, plan);
        Map<String, Object> values = new HashMap<>();
        if (plan.pointCode != null) {
            // 位区逐点任务：读回来就是该点位自己的值
            values.put(task.getPoints().get(0).key(), data);
            return values;
        }
        for (CollectionPoint point : task.getPoints()) {
            FinsReadConfig config = config(point);
            int offset = config.getStartAddress() - plan.start;
            int size = config.getLength();
            if (offset < 0 || offset + size > data.size()) {
                log.warn("[统一采集] componentId={} 点位{}地址区间超出本次读取范围，跳过",
                        componentId, point.getCode());
                continue;
            }
            values.put(point.key(), new ArrayList<>(data.subList(offset, offset + size)));
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
     * 持锁读取（读、写、重连共用一把锁）；传输层异常在解锁后重连，协议级拒绝只记日志
     */
    private List<Integer> readWithLock(String componentId, FinsPlan plan) throws Exception {
        ReentrantLock lock = FinsConnectionManager.getLock(componentId);
        boolean needReconnect = false;
        lock.lock();
        try {
            return FinsDataReader.readMemoryArea(componentId, plan.areaCode, plan.start,
                    plan.bitAddress, plan.count);
        } catch (Exception e) {
            if (e instanceof FinsResponseException) {
                log.warn("componentId={} 读取被PLC拒绝（FINS错误）：{}", componentId, e.getMessage());
            } else {
                log.warn("componentId={} 读取失败，触发强制重连", componentId);
                needReconnect = true;
            }
            throw e;
        } finally {
            lock.unlock();
            // forceReconnect 内部会真建 TCP + 指数退避 sleep，必须在 unlock 之后
            if (needReconnect) {
                FinsConnectionManager.forceReconnect(componentId);
            }
        }
    }

    /**
     * 是否位区（位区点位每点带位号，不能按字合并）
     *
     * @param config 读取配置
     * @return true=位区
     */
    private static boolean isBitArea(FinsReadConfig config) {
        return config.getAreaCode() != null && FinsDataReader.isBitArea(config.getAreaCode());
    }

    private static FinsReadConfig config(CollectionPoint point) {
        return (FinsReadConfig) point.getSource();
    }

    /**
     * 读取块描述
     */
    private static final class FinsPlan {

        /** 存储区代码 */
        private final int areaCode;

        /** 起始字地址 */
        private final int start;

        /** 位号（字区为 null） */
        private final Integer bitAddress;

        /** 读取数量（字区=字个数，位区=位个数） */
        private final int count;

        /** 位区逐点任务对应的点位编码（字区任务为 null） */
        private final String pointCode;

        FinsPlan(int areaCode, int start, Integer bitAddress, int count, String pointCode) {
            this.areaCode = areaCode;
            this.start = start;
            this.bitAddress = bitAddress;
            this.count = count;
            this.pointCode = pointCode;
        }
    }
}
