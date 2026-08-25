//由AI修改
package com.labdatahub.component.event;

import com.labdatahub.common.utils.spring.SpringUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 组件连接状态通知器
 * 主动轮询协议（FANUC/Brother/Modbus/S7/FINS/Mitsubishi/Database）的连接管理器，
 * 在连接从"在线变离线"时发布 component.offline 事件（带节流，只在转变时发一次）；
 * 连接恢复（重连成功/健康检查通过）时清除节流标记，允许下次断连再次通知。
 * business 层 DeviceUpListener 订阅 component.offline 后把该组件下设备批量置离线，
 * 解决"主动轮询协议设备断开后状态卡在线"的问题。
 */
public class ComponentOnlineNotifier {
    // 已通知离线的组件集合（节流：健康检查每 10 秒轮询，避免连接长时间断开时反复发布离线事件）
    private static final Set<String> OFFLINE_NOTIFIED = ConcurrentHashMap.newKeySet();

    /**
     * 标记组件离线并发布 component.offline 事件
     * 仅当组件未通知过离线时发布（在线→离线转变一次），重复调用幂等
     * @param componentId 组件唯一标识
     */
    public static void markOfflineAndNotify(String componentId) {
        if (componentId == null) {
            return;
        }
        if (OFFLINE_NOTIFIED.add(componentId)) {
            try {
                SpringUtils.getBean(EventBus.class).publish("component.offline", componentId);
            } catch (Exception e) {
                System.err.printf("[组件状态] componentId=%s 发布离线事件异常：%s%n", componentId, e.getMessage());
            }
        }
    }

    /**
     * 标记组件在线（连接成功/健康检查通过时调用）
     * 清除离线节流标记，组件再次断连时可再次通知离线
     * @param componentId 组件唯一标识
     */
    public static void markOnline(String componentId) {
        if (componentId == null) {
            return;
        }
        OFFLINE_NOTIFIED.remove(componentId);
    }
}
