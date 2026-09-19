package com.labdatahub.component.mitsubishi_mc3e_tcp;

/**
 * 三菱 MC 消息消费回调接口
 * 业务侧实现该接口，自定义消息处理逻辑
 */
@FunctionalInterface
public interface MitsubishiMc3eMessageConsumeHandler {
    /**
     * 处理单条 MC 消息
     * @param componentId 组件ID
     * @param message 待消费的消息
     * @throws Exception 消费过程中抛出的异常（由框架捕获并打印，不中断消费循环）
     */
    void handle(String componentId, MitsubishiMc3eMessage message) throws Exception;
}
