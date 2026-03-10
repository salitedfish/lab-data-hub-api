package com.labdatahub.component.event;

import com.labdatahub.component.event.EventFilter;
import com.labdatahub.component.event.EventListener;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-09-25
 */
// 修正后的 EventBus 接口
// 修正后的 EventBus 接口
public interface EventBus {
    void publish(String topic,Object event);  // 改为 Object 类型
    void subscribe(String topic, EventListener listener);
    void unsubscribe(String topic, EventListener listener);
    void subscribe(String topic, EventListener listener, EventFilter filter);
}