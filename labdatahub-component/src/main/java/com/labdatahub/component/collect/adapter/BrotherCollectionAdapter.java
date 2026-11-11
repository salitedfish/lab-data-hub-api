package com.labdatahub.component.collect.adapter;

import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSONObject;
import com.labdatahub.component.brother_tcp.BrotherTcpConnectionManager;
import com.labdatahub.component.brother_tcp.BrotherTcpDataReader;
import com.labdatahub.component.brother_tcp.BrotherTcpReadConfig;
import com.labdatahub.component.collect.CollectionAdapter;
import com.labdatahub.component.collect.CollectionFamily;
import com.labdatahub.component.collect.CollectionPoint;
import com.labdatahub.component.collect.ReadTask;

/**
 * BROTHER_TCP 采集适配器
 *
 * <p>Brother 的一次 LOD 命令返回<b>整个数据区</b>，旧链路却是每个点位发一次 LOD —— 同一数据区
 * 十个点位就十次整块读。这里按「同设备 + 同数据区」只读一次，再按各点位的行号/字段序号取值。
 */
public class BrotherCollectionAdapter implements CollectionAdapter {

    /** 派生数据区：非切削时间 = 循环时间 − 切削时间，机床不直接提供该字段，读同一块 PRDC2 后计算 */
    private static final String DERIVED_NON_CUT_AREA = "PRDC2NC";
    /** 派生数据区实际读取的 LOD 数据区 */
    private static final String PRDC2_AREA = "PRDC2";
    /** PRDC2 L01 行内：字段1=循环时间、字段2=切削时间 */
    private static final int PRDC2_CYCLE_FIELD = 1;
    private static final int PRDC2_CUTTING_FIELD = 2;

    /**
     * 派生数据区：当日操作次数（机床「7511 发送已完成(生产数据3) → 操作时间日志」画面当天行的「操作次数」）
     * 机床不提供该字段，读同一块 OPLOG 操作时间日志后统计
     */
    private static final String DERIVED_OP_COUNT_AREA = "OPLOGCNT";
    /** 派生数据区实际读取的 LOD 数据区：操作时间日志（近 10000 条加工操作记录，新→旧） */
    private static final String OPLOG_AREA = "OPLOG";
    /** OPLOG 条目值 = 257 表示一次操作（程序）结束，按天计数即机床画面的「操作次数」 */
    private static final long OPLOG_OPERATION_DONE_VALUE = 257L;

    @Override
    public String family() {
        return CollectionFamily.BROTHER;
    }

    @Override
    public String unitKey(CollectionPoint point) {
        // 派生区与真实数据区合并成同一个采集单元，共用一次整块读取
        String dataArea = realArea(config(point).getDataArea());
        return point.getDeviceSn() + "|" + (dataArea == null ? "" : dataArea.trim());
    }

    @Override
    public List<ReadTask> split(String componentId, String unitKey, int intervalSeconds,
                                List<CollectionPoint> points) {
        if (points.isEmpty()) {
            return Collections.emptyList();
        }
        // 任务键必须带间隔：同一数据区在多个间隔下各有一份定时任务，键相同会互相覆盖
        ReadTask task = new ReadTask(componentId, family(), unitKey + "@" + intervalSeconds, unitKey,
                intervalSeconds, new ArrayList<>(points), null);
        return Collections.singletonList(task);
    }

    @Override
    public Map<String, Object> execute(String componentId, ReadTask task) throws Exception {
        Socket connection = BrotherTcpConnectionManager.connections.get(componentId);
        if (connection == null || connection.isClosed() || !connection.isConnected()) {
            return Collections.emptyMap();
        }
        String dataArea = realArea(config(task.getPoints().get(0)).getDataArea());
        // 一次 LOD 读整块数据区，本任务里同数据区的点位共用
        List<String[]> rows = BrotherTcpDataReader.readDataArea(componentId, dataArea);
        Map<String, Object> values = new HashMap<>();
        for (CollectionPoint point : task.getPoints()) {
            BrotherTcpReadConfig config = config(point);
            String value;
            if (DERIVED_NON_CUT_AREA.equals(config.getDataArea())) {
                value = readNonCuttingTime(rows, config.getRowNumber());
            } else if (DERIVED_OP_COUNT_AREA.equals(config.getDataArea())) {
                value = readTodayOperationCount(rows);
            } else {
                value = BrotherTcpDataReader.extractValue(rows, config.getRowNumber(),
                        config.getFieldIndex());
            }
            if (value == null) {
                // 行或字段越界（如机型无该点位），跳过本次
                continue;
            }
            values.put(point.key(), value);
        }
        return values;
    }

    @Override
    public JSONObject buildPayload(ReadTask task, CollectionPoint point, Object raw) {
        JSONObject payload = new JSONObject();
        payload.put("deviceSn", point.getDeviceSn());
        payload.put("code", point.getCode());
        payload.put("value", raw);
        payload.put("dataType", point.getDataType());
        payload.put("byteOrder", point.getByteOrder());
        payload.put("isSigned", point.getIsSigned());
        payload.put("scale", point.getScale());
        payload.put("offset", point.getOffset());
        return payload;
    }

    private static BrotherTcpReadConfig config(CollectionPoint point) {
        return (BrotherTcpReadConfig) point.getSource();
    }

    /**
     * 派生数据区映射到实际 LOD 数据区（PRDC2NC → PRDC2、OPLOGCNT → OPLOG）
     *
     * @param dataArea 配置里的数据区名
     * @return 实际要发送给机床的数据区名
     */
    private static String realArea(String dataArea) {
        if (DERIVED_NON_CUT_AREA.equals(dataArea)) {
            return PRDC2_AREA;
        }
        if (DERIVED_OP_COUNT_AREA.equals(dataArea)) {
            return OPLOG_AREA;
        }
        return dataArea;
    }

    /**
     * 统计当天（机床日期）的操作次数：OPLOG 里值 = 257 的条目数
     *
     * <p>OPLOG 每行是若干「时间戳,值」对（B 行 1 对、C 行最多 50 对），共约 10000 条、新→旧。
     * 机床把本地时间直接写成 epoch 数值，所以按 UTC 规则格式化即是机床显示的墙上时间。
     * 日期以「服务器本地日期」为准；机床时钟比服务器快约 10 分钟，跨零点后机床已进入新的一天，
     * 此时以最新一条记录的机床日期为准（机床停机的日子没有记录，仍取服务器日期 → 计为 0）。
     *
     * @param rows OPLOG 整块数据
     * @return 当天操作次数；无任何记录时返回 null（跳过本次）
     */
    private static String readTodayOperationCount(List<String[]> rows) {
        List<long[]> entries = new ArrayList<>();
        long newestEpoch = Long.MIN_VALUE;
        for (String[] tokens : rows) {
            if (tokens == null || tokens.length < 3 || !isOplogRow(tokens[0])) {
                continue;
            }
            // 逐对解析「时间戳,值」；行内出现非数字（空行字段）则丢弃该行，避免错位配对
            List<long[]> rowEntries = new ArrayList<>();
            boolean valid = true;
            for (int i = 1; i + 1 < tokens.length; i += 2) {
                try {
                    rowEntries.add(new long[]{Long.parseLong(tokens[i].trim()),
                            Long.parseLong(tokens[i + 1].trim())});
                } catch (NumberFormatException e) {
                    valid = false;
                    break;
                }
            }
            if (!valid || rowEntries.isEmpty()) {
                continue;
            }
            for (long[] entry : rowEntries) {
                entries.add(entry);
                if (entry[0] > newestEpoch) {
                    newestEpoch = entry[0];
                }
            }
        }
        if (entries.isEmpty()) {
            return null;
        }
        LocalDate today = LocalDate.now();
        LocalDate machineDay = LocalDateTime.ofEpochSecond(newestEpoch, 0, ZoneOffset.UTC).toLocalDate();
        if (machineDay.isAfter(today)) {
            today = machineDay;
        }
        int count = 0;
        for (long[] entry : entries) {
            if (entry[1] != OPLOG_OPERATION_DONE_VALUE) {
                continue;
            }
            LocalDate day = LocalDateTime.ofEpochSecond(entry[0], 0, ZoneOffset.UTC).toLocalDate();
            if (day.equals(today)) {
                count++;
            }
        }
        return String.valueOf(count);
    }

    /**
     * 是否为 OPLOG 的 B/C 数据行（B001 / C001 ...），A 行是汇总头、帧头与校验行不算
     */
    private static boolean isOplogRow(String symbol) {
        if (symbol == null || symbol.length() != 4) {
            return false;
        }
        char prefix = symbol.charAt(0);
        if (prefix != 'B' && prefix != 'C') {
            return false;
        }
        for (int i = 1; i < 4; i++) {
            if (!Character.isDigit(symbol.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算非切削时间（秒）= 循环时间 − 切削时间
     *
     * @param rows PRDC2 整块数据
     * @param rowNumber 行号（1 起，L01 = 1）
     * @return 差值字符串；行/字段缺失或非数字时返回 null（跳过本次）
     */
    private static String readNonCuttingTime(List<String[]> rows, Integer rowNumber) {
        String cycle = BrotherTcpDataReader.extractValue(rows, rowNumber, PRDC2_CYCLE_FIELD);
        String cutting = BrotherTcpDataReader.extractValue(rows, rowNumber, PRDC2_CUTTING_FIELD);
        if (cycle == null || cutting == null) {
            return null;
        }
        try {
            return String.valueOf(Long.parseLong(cycle.trim()) - Long.parseLong(cutting.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
