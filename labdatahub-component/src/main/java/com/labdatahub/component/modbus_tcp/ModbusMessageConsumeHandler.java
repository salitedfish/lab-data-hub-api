package com.labdatahub.component.modbus_tcp;

/**
 * Modbus消息消费回调接口
 * 业务侧实现该接口，自定义消息处理逻辑
 */
@FunctionalInterface
public interface ModbusMessageConsumeHandler {
    /**
     * 处理单条Modbus消息
     * @param message 待消费的消息
     * @throws Exception 消费过程中抛出的异常（由框架捕获并打印，不中断消费循环）
     */
    void handle(String componentId,ModbusMessage message) throws Exception;
}