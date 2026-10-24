package com.labdatahub.component.collect.adapter;

import java.net.Socket;
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
import com.labdatahub.component.mitsubishi_cnc_tcp.MitsubishiCncConnectionManager;
import com.labdatahub.component.mitsubishi_cnc_tcp.MitsubishiCncDataReader;
import com.labdatahub.component.mitsubishi_cnc_tcp.MitsubishiCncReadConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * MITSUBISHI_CNC_TCP 采集适配器
 *
 * <p>MOCHA 协议一次读一个采集项，逐点读。读失败即强制重连（与旧链路一致：半开连接本地状态
 * 检测不出来，只能靠读超时触发自愈）。
 */
@Slf4j
public class CncCollectionAdapter implements CollectionAdapter {

    @Override
    public String family() {
        return CollectionFamily.CNC;
    }

    @Override
    public String unitKey(CollectionPoint point) {
        return point.getDeviceSn() + "|" + point.getCode();
    }

    @Override
    public List<ReadTask> split(String componentId, String unitKey, int intervalSeconds,
                                List<CollectionPoint> points) {
        List<ReadTask> tasks = new ArrayList<>();
        for (CollectionPoint point : points) {
            // 任务键带间隔：与其它适配器保持同一口径，避免将来支持同点位多间隔时互相覆盖
            tasks.add(new ReadTask(componentId, family(), unitKey + "@" + intervalSeconds, unitKey,
                    intervalSeconds, Collections.singletonList(point), null));
        }
        return tasks;
    }

    @Override
    public Map<String, Object> execute(String componentId, ReadTask task) throws Exception {
        Socket connection = MitsubishiCncConnectionManager.connections.get(componentId);
        if (connection == null || connection.isClosed() || !connection.isConnected()) {
            return Collections.emptyMap();
        }
        Map<String, Object> values = new HashMap<>();
        for (CollectionPoint point : task.getPoints()) {
            MitsubishiCncReadConfig config = config(point);
            try {
                String value = MitsubishiCncDataReader.readPoint(componentId, config.getReadType(),
                        config.getAxisNo());
                if (value == null) {
                    // 点位不支持或读取失败，跳过本次
                    continue;
                }
                values.put(point.key(), value);
            } catch (Exception e) {
                log.warn("componentId={} 读取code={}失败，触发强制重连", componentId, point.getCode());
                MitsubishiCncConnectionManager.forceReconnect(componentId);
                throw e;
            }
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

    private static MitsubishiCncReadConfig config(CollectionPoint point) {
        return (MitsubishiCncReadConfig) point.getSource();
    }
}
