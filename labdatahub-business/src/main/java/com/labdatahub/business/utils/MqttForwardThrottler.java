//由AI修改
package com.labdatahub.business.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labdatahub.component.rule_engine.mq.mqtt.MqttBrokerUtils;

/**
 * MQTT 规则转发节流器
 * 同一设备一个节流窗口内只向 MQTT 目标发布一次该设备全量最新数据，避免"每个点位每次采集就发一条消息"。
 * 用于 DeviceUpListener 规则转发的 MQTT 出口（HTTP/RabbitMQ/Kafka/RocketMQ 等其它出口不走节流，保持原频率）。
 * 节流窗口由 application.yml 的 mqtt.forward.flush-interval-seconds 配置（默认 3 秒）。
 * 数据源：DeviceUpListener.buildFullDataPayload 已把该设备全部点位最新值合并成一条（MessageCache 累积缓存），
 * 这里只是把"每点位一条"压成"每设备每窗口一条"。
 */
public class MqttForwardThrottler {
    private static final Logger log = LoggerFactory.getLogger(MqttForwardThrottler.class);

    /** 节流窗口（秒）：同一设备一个窗口内只发布一次，默认 3 秒 */
    private static volatile int flushIntervalSeconds = 3;

    /** 设备节流缓存：key=deviceSn */
    private static final Map<String, Entry> buffer = new ConcurrentHashMap<>();

    /** 周期扫描线程（守护线程，不阻碍应用退出） */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "mqtt-forward-throttle");
        t.setDaemon(true);
        return t;
    });

    /** 长期无新数据的设备清理阈值（毫秒）：疑似已删除/离线不再上报，避免缓存无限膨胀 */
    private static final long IDLE_CLEANUP_MS = 60 * 1000L;

    private static volatile boolean started = false;

    /** 设备待转发数据 */
    private static final class Entry {
        /** 该设备最新全量数据（JSON 字符串，offer 时用最新覆盖） */
        volatile String payload;
        /** 该设备 MQTT 转发目标（brokerId 列表，offer 时快照） */
        volatile List<String> targets;
        /** 自上次发布后是否有新数据（有才发布，无新数据不重复发旧值） */
        volatile boolean changed;
        /** 最近一次数据更新时间（毫秒） */
        volatile long lastUpdate;
    }

    private MqttForwardThrottler() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 设置节流窗口并启动周期扫描（应用启动时调用一次）
     * @param intervalSeconds 节流窗口秒数（application.yml 的 mqtt.forward.flush-interval-seconds，非法值回退 3 秒）
     */
    public static synchronized void start(int intervalSeconds) {
        flushIntervalSeconds = intervalSeconds > 0 ? intervalSeconds : 3;
        if (started) {
            return;
        }
        started = true;
        scheduler.scheduleAtFixedRate(MqttForwardThrottler::sweep, flushIntervalSeconds, flushIntervalSeconds, TimeUnit.SECONDS);
        log.info("MQTT 规则转发节流已启动，节流窗口={}秒（同一设备一个窗口内只发一次全量数据）", flushIntervalSeconds);
    }

    /**
     * 记录设备最新全量数据，供节流窗口内的下一次发布使用
     * @param deviceSn 设备SN
     * @param payload  该设备最新全量数据 JSON（buildFullDataPayload 产物，含全部点位最新值）
     * @param targets  MQTT 转发目标 brokerId 列表（非空才入缓存）
     */
    public static void offer(String deviceSn, String payload, List<String> targets) {
        if (deviceSn == null || payload == null || targets == null || targets.isEmpty()) {
            return;
        }
        Entry entry = buffer.computeIfAbsent(deviceSn, k -> new Entry());
        entry.payload = payload;
        entry.targets = targets;
        entry.changed = true;
        entry.lastUpdate = System.currentTimeMillis();
    }

    /**
     * 周期扫描：每个节流窗口内，有新数据的设备各发布一次全量数据。
     * 无新数据的设备不重复发布旧值；长期无数据的设备清理出缓存。
     */
    private static void sweep() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Entry> mapEntry : buffer.entrySet()) {
            String deviceSn = mapEntry.getKey();
            Entry entry = mapEntry.getValue();
            if (!entry.changed) {
                // 长期无新数据的设备（疑似已删除/离线不再上报），清理避免缓存膨胀
                if (now - entry.lastUpdate > IDLE_CLEANUP_MS) {
                    buffer.remove(deviceSn, entry);
                }
                continue;
            }
            entry.changed = false;
            String payload = entry.payload;
            List<String> targets = entry.targets;
            boolean allSent = true;
            for (String o : targets) {
                try {
                    MqttBrokerUtils.publishMessage(o, payload);
                } catch (Exception e) {
                    allSent = false;
                    log.error("MQTT 规则转发失败，brokerId={}, deviceSn={}: {}", o, deviceSn, e.getMessage());
                }
            }
            if (!allSent) {
                // ⚠️ 原实现把 changed 置 false 后就发布，publish 抛异常时这条数据<b>永远不会再发</b>
                //    （只留下上面那行日志）—— broker 短暂不可用就会静默丢一窗数据。
                //    有目标没发成功就把标记恢复，下个节流窗口拿同一份最新值重试；
                //    期间该设备若有新数据进来，offer 会用更新的 payload 覆盖（本就是「最新值」语义）。
                entry.changed = true;
            }
        }
    }
}
