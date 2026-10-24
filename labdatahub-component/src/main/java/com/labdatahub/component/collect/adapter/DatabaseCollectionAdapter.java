package com.labdatahub.component.collect.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSONObject;
import com.labdatahub.component.collect.CollectionAdapter;
import com.labdatahub.component.collect.CollectionFamily;
import com.labdatahub.component.collect.CollectionPoint;
import com.labdatahub.component.collect.ReadTask;
import com.labdatahub.component.db.DatabaseReader;

/**
 * 数据库采集（DATABASE_TCP）适配器
 *
 * <p>数据库采集不按点位 SQL 读，而是「取该数据源最新一条记录」，整条记录交给协议解码；
 * 设备下的点位只是映射到这条记录的不同字段。所以整条任务只上报一次。
 */
public class DatabaseCollectionAdapter implements CollectionAdapter {

    @Override
    public String family() {
        return CollectionFamily.DATABASE;
    }

    @Override
    public String unitKey(CollectionPoint point) {
        // 同设备的点位共用「取最新一条记录」这次查询
        return point.getDeviceSn();
    }

    @Override
    public List<ReadTask> split(String componentId, String unitKey, int intervalSeconds,
                                List<CollectionPoint> points) {
        if (points.isEmpty()) {
            return Collections.emptyList();
        }
        // 任务键必须带间隔：同一设备的点位可能配了不同间隔，键相同会互相覆盖
        ReadTask task = new ReadTask(componentId, family(), unitKey + "@" + intervalSeconds, unitKey,
                intervalSeconds, new ArrayList<>(points), null);
        return Collections.singletonList(task);
    }

    @Override
    public Map<String, Object> execute(String componentId, ReadTask task) throws Exception {
        Map<String, Object> record = DatabaseReader.getLatestRecord(componentId);
        Map<String, Object> values = new HashMap<>();
        values.put(task.getPoints().get(0).key(), record);
        return values;
    }

    @Override
    public JSONObject buildPayload(ReadTask task, CollectionPoint point, Object raw) {
        JSONObject payload = new JSONObject();
        payload.put("deviceSn", point.getDeviceSn());
        payload.put("data", raw);
        return payload;
    }

    /**
     * 一条任务 = 该数据源的一份最新记录，只上报一次（与旧链路每条消息一次上报一致）
     *
     * @return true
     */
    @Override
    public boolean publishOncePerTask() {
        return true;
    }
}
