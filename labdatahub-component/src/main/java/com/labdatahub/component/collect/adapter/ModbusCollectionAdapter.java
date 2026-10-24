package com.labdatahub.component.collect.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.component.collect.CollectionAdapter;
import com.labdatahub.component.collect.CollectionFamily;
import com.labdatahub.component.collect.CollectionPoint;
import com.labdatahub.component.collect.ReadTask;
import com.labdatahub.component.modbus_tcp.ModbusDataReader;
import com.labdatahub.component.modbus_tcp.ModbusReadConfig;
import com.labdatahub.component.modbus_tcp.RangeParserUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * MODBUS_TCP 采集适配器
 *
 * <p>旧链路每个点位一条消息、一次 Modbus 事务；这里把「同设备 + 同从站 + 同功能码」的点位按地址
 * 合并成一次事务（保持寄存器/输入寄存器单次 ≤125 字，线圈/离散输入 ≤2000 位），读回来再按各点位
 * 自己的地址区间切片，上报的 {@code jsonArray} 仍是该点位原来的连续块。
 */
@Slf4j
public class ModbusCollectionAdapter implements CollectionAdapter {

    /** 保持寄存器/输入寄存器单次读取上限（Modbus 协议） */
    private static final int MAX_REGISTERS = 125;

    /** 线圈/离散输入单次读取上限（Modbus 协议） */
    private static final int MAX_BITS = 2000;

    @Override
    public String family() {
        return CollectionFamily.MODBUS;
    }

    @Override
    public String unitKey(CollectionPoint point) {
        ModbusReadConfig config = config(point);
        return point.getDeviceSn() + "|" + config.getSlaveId() + "|" + functionCode(point);
    }

    @Override
    public List<ReadTask> split(String componentId, String unitKey, int intervalSeconds,
                                List<CollectionPoint> points) {
        List<RangeMerge.Span> spans = new ArrayList<>();
        for (CollectionPoint point : points) {
            ModbusReadConfig config = config(point);
            List<RangeParserUtil.RangeItem> items;
            try {
                items = RangeParserUtil.parse(config.getRegisterRange());
            } catch (IllegalArgumentException e) {
                log.warn("[统一采集] componentId={} 点位{}寄存器范围非法，跳过：{}",
                        componentId, point.getCode(), e.getMessage());
                continue;
            }
            if (items.isEmpty()) {
                log.warn("[统一采集] componentId={} 点位{}寄存器范围为空，跳过", componentId, point.getCode());
                continue;
            }
            int start = Integer.MAX_VALUE;
            int end = Integer.MIN_VALUE;
            for (RangeParserUtil.RangeItem item : items) {
                start = Math.min(start, item.getStart());
                end = Math.max(end, item.getStart() + item.getCount() - 1);
            }
            spans.add(new RangeMerge.Span(start, end, point));
        }
        if (spans.isEmpty()) {
            return Collections.emptyList();
        }
        int functionCode = functionCode(points.get(0));
        int cap = isBitFunction(functionCode) ? MAX_BITS : MAX_REGISTERS;
        List<RangeMerge.Cluster> clusters = RangeMerge.cluster(spans, cap,
                CollectionFamily.mergeGap(CollectionFamily.MODBUS));
        ModbusReadConfig first = config(points.get(0));
        List<ReadTask> tasks = new ArrayList<>();
        for (RangeMerge.Cluster cluster : clusters) {
            List<CollectionPoint> taskPoints = new ArrayList<>();
            for (RangeMerge.Span span : cluster.spans) {
                taskPoints.add(span.point);
            }
            ModbusPlan plan = new ModbusPlan(first.getSlaveId(), functionCode, cluster.start, cluster.size());
            // 任务键必须带间隔：同一采集单元在多个间隔下各有一份定时任务，键相同会互相覆盖
            tasks.add(new ReadTask(componentId, family(),
                    unitKey + "@" + intervalSeconds + "@" + cluster.start, unitKey,
                    intervalSeconds, taskPoints, plan));
        }
        return tasks;
    }

    @Override
    public Map<String, Object> execute(String componentId, ReadTask task) throws Exception {
        ModbusPlan plan = (ModbusPlan) task.getPlan();
        List<Integer> registers = ModbusDataReader.readByFunction(componentId, plan.slaveId,
                plan.functionCode, plan.start, plan.count);
        Map<String, Object> values = new HashMap<>();
        for (CollectionPoint point : task.getPoints()) {
            // 按点位自己的地址区间切片：一个 code 对应一块寄存器，与旧链路的读法完全一致
            int[] span = ownSpan(point);
            int offset = span[0] - plan.start;
            int size = span[1] - span[0] + 1;
            if (offset < 0 || offset + size > registers.size()) {
                log.warn("[统一采集] componentId={} 点位{}地址区间超出本次读取范围，跳过",
                        componentId, point.getCode());
                continue;
            }
            RangeParserUtil.RangeItem merged = new RangeParserUtil.RangeItem(span[0], size);
            merged.setRegisterList(new ArrayList<>(registers.subList(offset, offset + size)));
            values.put(point.key(), merged);
        }
        return values;
    }

    @Override
    public JSONObject buildPayload(ReadTask task, CollectionPoint point, Object raw) {
        ModbusReadConfig config = config(point);
        RangeParserUtil.RangeItem merged = (RangeParserUtil.RangeItem) raw;
        List<RangeParserUtil.RangeItem> mergedList = new ArrayList<>();
        mergedList.add(merged);
        JSONObject payload = new JSONObject();
        payload.put("deviceSn", point.getDeviceSn());
        payload.put("slaveId", config.getSlaveId());
        payload.put("code", point.getCode());
        payload.put("registerRange", config.getRegisterRange());
        payload.put("jsonArray", JSONArray.from(mergedList));
        payload.put("dataType", point.getDataType());
        payload.put("byteOrder", point.getByteOrder());
        payload.put("isSigned", point.getIsSigned());
        payload.put("scale", point.getScale());
        payload.put("offset", point.getOffset());
        return payload;
    }

    /**
     * Modbus 的组件调试频道展示的是寄存器块数组（与旧链路一致，不是解码 JSON）
     */
    @Override
    public String buildComponentPayload(ReadTask task, CollectionPoint point, Object raw) {
        List<RangeParserUtil.RangeItem> mergedList = new ArrayList<>();
        mergedList.add((RangeParserUtil.RangeItem) raw);
        return JSONArray.toJSONString(mergedList);
    }

    /**
     * 点位自己的地址区间 [起始, 结束]
     *
     * @param point 点位
     * @return 两元素数组
     */
    private static int[] ownSpan(CollectionPoint point) {
        List<RangeParserUtil.RangeItem> items = RangeParserUtil.parse(config(point).getRegisterRange());
        int start = Integer.MAX_VALUE;
        int end = Integer.MIN_VALUE;
        for (RangeParserUtil.RangeItem item : items) {
            start = Math.min(start, item.getStart());
            end = Math.max(end, item.getStart() + item.getCount() - 1);
        }
        return new int[] {start, end};
    }

    /**
     * 功能码：01线圈 02离散输入 03保持寄存器 04输入寄存器（空/非法按 03）
     *
     * @param point 点位
     * @return 功能码
     */
    private static int functionCode(CollectionPoint point) {
        String functionCode = config(point).getFunctionCode();
        if (StringUtils.isBlank(functionCode)) {
            return 3;
        }
        try {
            int code = Integer.parseInt(functionCode.trim());
            return code >= 1 && code <= 4 ? code : 3;
        } catch (NumberFormatException e) {
            return 3;
        }
    }

    /**
     * 是否位功能码（线圈/离散输入）
     *
     * @param functionCode 功能码
     * @return true=按位读
     */
    private static boolean isBitFunction(int functionCode) {
        return functionCode == 1 || functionCode == 2;
    }

    private static ModbusReadConfig config(CollectionPoint point) {
        return (ModbusReadConfig) point.getSource();
    }

    /**
     * 读取块描述
     */
    private static final class ModbusPlan {

        /** 从站ID */
        private final Integer slaveId;

        /** 功能码 */
        private final int functionCode;

        /** 起始地址 */
        private final int start;

        /** 读取数量（寄存器/位） */
        private final int count;

        ModbusPlan(Integer slaveId, int functionCode, int start, int count) {
            this.slaveId = slaveId;
            this.functionCode = functionCode;
            this.start = start;
            this.count = count;
        }
    }
}
