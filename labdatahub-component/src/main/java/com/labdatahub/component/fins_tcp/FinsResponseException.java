//由AI修改
package com.labdatahub.component.fins_tcp;

/**
 * FINS 协议层错误异常
 * 表示 PLC 已正常响应但返回了错误（结束码非 0、响应格式/长度异常等），
 * 说明连接本身是好的，只是命令被拒绝（区码/地址/数量配置错误）。
 * 消费端捕获到本异常只记日志、不触发强制重连，避免连接抖动；
 * 传输层异常（超时/断流/EOF）仍走原 Exception 触发重连自愈。
 */
public class FinsResponseException extends Exception {

    private static final long serialVersionUID = 1L;

    public FinsResponseException(String message) {
        super(message);
    }
}
