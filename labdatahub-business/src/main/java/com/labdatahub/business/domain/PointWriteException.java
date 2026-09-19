//由AI修改
package com.labdatahub.business.domain;

/**
 * 写值业务异常：匹配 / 编码阶段的可预期失败（400 / 404 / 409 / 422 / 500）
 *
 * <p>为什么带一份 {@link PointWriteMeta}：失败响应也要回显设备、地址、编码结果
 * （方案 4.1），而失败可能发生在匹配链路的中途——此时 meta 里已经填好的部分
 * 恰恰是排查最有用的信息。所以异常把「当时的那一份 meta」一起带出去，
 * service 拿到后直接用它拼失败响应、落审计。
 *
 * <p>连接类（503）与设备类（504）失败<b>不走这个异常</b>——那是协议层
 * {@code PointConnectionException} / {@code PointDeviceException} 的职责，
 * service 只做一次映射（方案 5.3）。
 */
public class PointWriteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务错误码（400/404/409/422/500） */
    private final int code;

    /** 失败时已经匹配出来的元数据（可能只填了一部分，不会为 null） */
    private final PointWriteMeta meta;

    public PointWriteException(int code, String message) {
        this(code, message, new PointWriteMeta());
    }

    public PointWriteException(int code, String message, PointWriteMeta meta) {
        super(message);
        this.code = code;
        this.meta = meta == null ? new PointWriteMeta() : meta;
    }

    public int getCode() {
        return code;
    }

    public PointWriteMeta getMeta() {
        return meta;
    }
}
