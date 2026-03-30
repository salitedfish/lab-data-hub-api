package com.labdatahub.component.fins_tcp;

/**
 * FINS消息消费回调接口
 * 业务侧实现该接口，自定义消息处理逻辑
 */
@FunctionalInterface
public interface FinsMessageConsumeHandler {
    /**
     * 处理单条FINS消息
     * @param componentId 组件ID
     * @param message 待消费的消息
     * @throws Exception 消费过程中抛出的异常（由框架捕获并打印，不中断消费循环）
     */
    void handle(String componentId, FinsMessage message) throws Exception;
}
