package com.labdatahub.component.collect.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.fastjson2.JSONObject;
import com.github.s7connector.api.S7Connector;
import com.labdatahub.component.collect.CollectionAdapter;
import com.labdatahub.component.collect.CollectionFamily;
import com.labdatahub.component.collect.CollectionPoint;
import com.labdatahub.component.collect.ReadTask;
import com.labdatahub.component.s7_tcp.S7ConnectionManager;
import com.labdatahub.component.s7_tcp.S7DataReader;
import com.labdatahub.component.s7_tcp.S7ReadConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * S71200_TCP 采集适配器
 *
 * <p>旧链路每个点位一次 {@code connector.read}；这里把「同设备 + 同区类型 + 同 DB 号」的点位按字节区间
 * 合并成一次读（单次 ≤200 字节），再按各点位自己的区间切片解析。
 *
 * <p>读、写、重连共用一把锁、读耗时交给连接管理器判活、慢读就地丢弃 —— 这三条与旧链路逐行一致，
 * 不能因为合并读取而省掉，否则 s7connector 吞掉 IOException 后返回的空缓冲区会被当成正常数据。
 */
@Slf4j
public class S7CollectionAdapter implements CollectionAdapter {

    /** 单次合并读取的字节数上限（S7 单 PDU 的保守值） */
    private static final int MAX_BYTES = 200;

    /** 「连接不存在」告警节流间隔（毫秒） */
    private static final long CONN_MISSING_LOG_INTERVAL_MS = 30000L;

    /** 各组件「连接不存在」告警的上次打印时间 */
    private static final Map<String, Long> CONN_MISSING_LOG_TS = new ConcurrentHashMap<>();

    @Override
    public String family() {
        return CollectionFamily.S7;
    }

    @Override
    public String unitKey(CollectionPoint point) {
        S7ReadConfig config = config(point);
        return point.getDeviceSn() + "|" + areaType(config) + "|" + config.getDbNumber();
    }

    @Override
    public List<ReadTask> split(String componentId, String unitKey, int intervalSeconds,
                                List<CollectionPoint> points) {
        List<RangeMerge.Span> spans = new ArrayList<>();
        for (CollectionPoint point : points) {
            S7ReadConfig config = config(point);
            if (config.getDbNumber() == null || config.getStartAddress() == null) {
                log.warn("[统一采集] componentId={} 点位{}配置不完整（dbNumber/startAddress 为空），跳过",
                        componentId, point.getCode());
                continue;
            }
            int size = S7DataReader.blockSize(config.getBlockType(), config.getLength());
            if (size <= 0) {
                log.warn("[统一采集] componentId={} 点位{}块类型非法（{}），跳过",
                        componentId, point.getCode(), config.getBlockType());
                continue;
            }
            spans.add(new RangeMerge.Span(config.getStartAddress(),
                    config.getStartAddress() + size - 1, point));
        }
        if (spans.isEmpty()) {
            return Collections.emptyList();
        }
        List<RangeMerge.Cluster> clusters = RangeMerge.cluster(spans, MAX_BYTES,
                CollectionFamily.mergeGap(CollectionFamily.S7));
        S7ReadConfig first = config(points.get(0));
        List<ReadTask> tasks = new ArrayList<>();
        for (RangeMerge.Cluster cluster : clusters) {
            List<CollectionPoint> taskPoints = new ArrayList<>();
            for (RangeMerge.Span span : cluster.spans) {
                taskPoints.add(span.point);
            }
            S7Plan plan = new S7Plan(areaType(first), first.getDbNumber(), cluster.start, cluster.size());
            // 任务键必须带间隔：同一采集单元在多个间隔下各有一份定时任务，键相同会互相覆盖
            tasks.add(new ReadTask(componentId, family(),
                    unitKey + "@" + intervalSeconds + "@" + cluster.start, unitKey,
                    intervalSeconds, taskPoints, plan));
        }
        return tasks;
    }

    @Override
    public Map<String, Object> execute(String componentId, ReadTask task) throws Exception {
        S7Connector connector = S7ConnectionManager.getConnection(componentId);
        if (connector == null) {
            // 连接已被健康检查摘除：跳过本次，不拿死连接去读
            logConnMissing(componentId);
            return Collections.emptyMap();
        }
        S7Plan plan = (S7Plan) task.getPlan();
        Object[] raw = readWithLock(componentId, connector, plan);
        if (raw == null) {
            // 慢读判死：本次读的是空缓冲区拼出的假值，整批丢弃
            return Collections.emptyMap();
        }
        byte[] data = (byte[]) raw[0];
        Map<String, Object> values = new HashMap<>();
        for (CollectionPoint point : task.getPoints()) {
            S7ReadConfig config = config(point);
            int size = S7DataReader.blockSize(config.getBlockType(), config.getLength());
            int offset = config.getStartAddress() - plan.start;
            if (offset < 0 || offset + size > data.length) {
                log.warn("[统一采集] componentId={} 点位{}字节区间超出本次读取范围，跳过",
                        componentId, point.getCode());
                continue;
            }
            byte[] block = Arrays.copyOfRange(data, offset, offset + size);
            values.put(point.key(), S7DataReader.decodeBlock(block, config.getBlockType(),
                    config.getDataType(), config.getLength(), config.getBitOffset(), config.getIsSigned()));
        }
        return values;
    }

    @Override
    public JSONObject buildPayload(ReadTask task, CollectionPoint point, Object raw) {
        S7ReadConfig config = config(point);
        JSONObject payload = new JSONObject();
        payload.put("deviceSn", point.getDeviceSn());
        payload.put("code", point.getCode());
        payload.put("dbNumber", config.getDbNumber());
        payload.put("blockType", config.getBlockType());
        payload.put("startAddress", config.getStartAddress());
        payload.put("length", config.getLength());
        payload.put("data", raw);
        payload.put("dataType", point.getDataType());
        payload.put("byteOrder", point.getByteOrder());
        payload.put("isSigned", point.getIsSigned());
        payload.put("scale", point.getScale());
        payload.put("offset", point.getOffset());
        return payload;
    }

    /**
     * 持锁读取并记录读耗时（锁、耗时判活、慢读丢弃与旧链路一致）
     *
     * @return 读取结果（只有一项：字节数组）；慢读判死返回 null
     */
    private Object[] readWithLock(String componentId, S7Connector connector, S7Plan plan) throws Exception {
        ReentrantLock lock = S7ConnectionManager.getLock(componentId);
        boolean needReconnect = false;
        boolean deviceGone = false;
        byte[] data;
        lock.lock();
        long readStart = System.currentTimeMillis();
        try {
            data = S7DataReader.readArea(connector, plan.areaType, plan.dbNumber, plan.start, plan.count);
            long costMs = System.currentTimeMillis() - readStart;
            S7ConnectionManager.recordReadCost(componentId, costMs);
            deviceGone = S7ConnectionManager.discardIfDeviceGone(componentId, costMs);
        } catch (Exception e) {
            needReconnect = true;
            throw e;
        } finally {
            lock.unlock();
            // 重连必须在 unlock 之后：doReconnect 内部会真建 TCP 并退避 sleep，持锁会卡住整条采集链路
            if (needReconnect) {
                S7ConnectionManager.forceReconnect(componentId);
            }
        }
        return deviceGone ? null : new Object[] {data};
    }

    /**
     * 「连接不存在」告警，按组件节流
     *
     * @param componentId 组件ID
     */
    private static void logConnMissing(String componentId) {
        long now = System.currentTimeMillis();
        Long lastLogTs = CONN_MISSING_LOG_TS.get(componentId);
        if (lastLogTs == null || now - lastLogTs >= CONN_MISSING_LOG_INTERVAL_MS) {
            CONN_MISSING_LOG_TS.put(componentId, now);
            log.warn("componentId={} 连接不存在或未连接，已跳过本次上报（连接恢复前不再重复告警）", componentId);
        }
    }

    /**
     * 区类型归一：DB/M/I/Q（其它按 DB）
     *
     * @param config 读取配置
     * @return 归一后的区类型
     */
    private static String areaType(S7ReadConfig config) {
        String areaType = config.getAreaType();
        if (areaType == null) {
            return "DB";
        }
        String trimmed = areaType.trim().toUpperCase();
        switch (trimmed) {
            case "M":
            case "I":
            case "Q":
                return trimmed;
            case "DB":
            default:
                return "DB";
        }
    }

    private static S7ReadConfig config(CollectionPoint point) {
        return (S7ReadConfig) point.getSource();
    }

    /**
     * 读取块描述
     */
    private static final class S7Plan {

        /** 区类型 */
        private final String areaType;

        /** DB号 */
        private final Integer dbNumber;

        /** 起始字节偏移 */
        private final int start;

        /** 读取字节数 */
        private final int count;

        S7Plan(String areaType, Integer dbNumber, int start, int count) {
            this.areaType = areaType;
            this.dbNumber = dbNumber;
            this.start = start;
            this.count = count;
        }
    }
}
