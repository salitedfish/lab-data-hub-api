package com.labdatahub.component.collect.adapter;

import java.net.Socket;
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

    @Override
    public String family() {
        return CollectionFamily.BROTHER;
    }

    @Override
    public String unitKey(CollectionPoint point) {
        String dataArea = config(point).getDataArea();
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
        String dataArea = config(task.getPoints().get(0)).getDataArea();
        // 一次 LOD 读整块数据区，本任务里同数据区的点位共用
        List<String[]> rows = BrotherTcpDataReader.readDataArea(componentId, dataArea);
        Map<String, Object> values = new HashMap<>();
        for (CollectionPoint point : task.getPoints()) {
            BrotherTcpReadConfig config = config(point);
            String value = BrotherTcpDataReader.extractValue(rows, config.getRowNumber(),
                    config.getFieldIndex());
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
}
