package com.labdatahub.component.collect.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson2.JSONObject;
import com.labdatahub.component.collect.CollectionAdapter;
import com.labdatahub.component.collect.CollectionFamily;
import com.labdatahub.component.collect.CollectionPoint;
import com.labdatahub.component.collect.ReadTask;
import com.labdatahub.component.fanuc_focas.FanucFocasConnectionManager;
import com.labdatahub.component.fanuc_focas.FanucFocasDataReader;
import com.labdatahub.component.fanuc_focas.FanucFocasReadConfig;

/**
 * FANUC_TCP（FOCAS2）采集适配器
 *
 * <p>FOCAS2 一个函数读一个数据项，没有可合并的批量接口，本适配器逐点读；收益来自去掉旧链路的
 * {@code delayTime} 停顿与固定节奏，读多快算多快。
 *
 * <p>⚠️ <b>句柄绑定建连线程</b>：{@code cnc_allclibhndl3} 在哪个线程建连，就只有那个线程能读，
 * 换线程读返回 EW_BUSY(-8)。所以建连只能由本适配器的 {@link #ensureConnection}（跑在统一采集的
 * 消费线程上）发起，绝不能在 HTTP 线程或健康检查线程建共享句柄。
 */
public class FanucCollectionAdapter implements CollectionAdapter {

    /** 建连失败后的重试间隔（毫秒）：cnc_allclibhndl3 本身有 3 秒超时，不能每轮都撞 */
    private static final long CONNECT_RETRY_INTERVAL_MS = 5000L;

    /** 各组件上次尝试建连的时刻 */
    private static final Map<String, Long> LAST_CONNECT_ATTEMPT = new ConcurrentHashMap<>();

    @Override
    public String family() {
        return CollectionFamily.FANUC;
    }

    @Override
    public String unitKey(CollectionPoint point) {
        // 一个点位一次 FOCAS 调用，逐点成任务
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
    public void ensureConnection(String componentId) {
        if (FanucFocasConnectionManager.handleMap.get(componentId) != null) {
            return;
        }
        if (!FanucFocasConnectionManager.configMap.containsKey(componentId)) {
            // 组件未开启（无连接配置），等开启后再说
            return;
        }
        long now = System.currentTimeMillis();
        Long last = LAST_CONNECT_ATTEMPT.get(componentId);
        if (last != null && now - last < CONNECT_RETRY_INTERVAL_MS) {
            return;
        }
        LAST_CONNECT_ATTEMPT.put(componentId, now);
        FanucFocasConnectionManager.establishConnection(componentId);
    }

    @Override
    public Map<String, Object> execute(String componentId, ReadTask task) throws Exception {
        if (FanucFocasConnectionManager.handleMap.get(componentId) == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> values = new HashMap<>();
        for (CollectionPoint point : task.getPoints()) {
            FanucFocasReadConfig config = config(point);
            String value = FanucFocasDataReader.readPoint(componentId, config.getReadType(),
                    config.getParam1(), config.getParam2());
            if (value == null) {
                // 点位不支持或读取失败，跳过本次
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

    private static FanucFocasReadConfig config(CollectionPoint point) {
        return (FanucFocasReadConfig) point.getSource();
    }
}
