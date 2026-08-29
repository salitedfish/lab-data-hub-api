//由AI修改
package com.labdatahub.component.mitsubishi_cnc_tcp;

/**
 * 三菱 CNC TCP（MOCHA）消息消费回调接口
 * 业务侧实现该接口，自定义消息处理逻辑
 */
@FunctionalInterface
public interface MitsubishiCncMessageConsumeHandler {
    /**
     * 处理单条三菱 CNC 消息
     * @param componentId 组件ID
     * @param message 待消费的消息
     * @throws Exception 消费过程中抛出的异常（由框架捕获并打印，不中断消费循环）
     */
    void handle(String componentId, MitsubishiCncMessage message) throws Exception;
}
