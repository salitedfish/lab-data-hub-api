//由AI修改
package com.labdatahub.component.fanuc_focas;

/**
 * FANUC FOCAS2 消息消费回调接口
 * 业务侧实现该接口，自定义消息处理逻辑
 */
@FunctionalInterface
public interface FanucFocasMessageConsumeHandler {
    /**
     * 处理单条 FANUC FOCAS2 消息
     * @param componentId 组件ID
     * @param message 待消费的消息
     * @throws Exception 消费过程中抛出的异常（由框架捕获并打印，不中断消费循环）
     */
    void handle(String componentId, FanucFocasMessage message) throws Exception;
}
