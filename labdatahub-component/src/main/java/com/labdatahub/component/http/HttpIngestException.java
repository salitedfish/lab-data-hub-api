package com.labdatahub.component.http;

/**
 * HTTP 上报接入异常
 *
 * <p>报文不合格（请求体不是合法 JSON、缺 deviceSn、组件未绑协议……）时抛出，
 * 由 {@link HttpServerManager} 在请求线程里接住，转成 HTTP 状态码 + JSON 说明回给设备端。
 *
 * <p>之所以要单独一个异常：接入校验必须发生在**请求线程**上（此时响应还没提交），
 * 才能把失败原因回给调用方；一旦进了线程池异步处理，响应早就发出去了，异常只能进日志。
 *
 * @Author: labdatahub
 * @CreateTime: 2026-10-16
 */
public class HttpIngestException extends RuntimeException {

    /** 要回给设备端的 HTTP 状态码（400 报文问题 / 500 平台侧问题） */
    private final int status;

    public HttpIngestException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
