//由AI修改
package com.labdatahub.component.protocol.exception;

/**
 * 点位写入 —— 平台侧连接异常（映射为 HTTP 503，可重试）
 *
 * <p>三种情形在 message 里必须说清，否则现场排查只能靠猜（方案 4.5）：
 * <ul>
 *   <li>无可用连接：组件未开启，<b>或</b>开启时连接失败 —— ⚠️ 不要写成「去开启组件」，
 *       {@code addConnection} 失败时会把 connections 与 configMap 一起清掉，
 *       这两种情形在 connections 里长得一模一样</li>
 *   <li>连接存在但已断开：无法连接 {ip}:{port}，请检查设备网络</li>
 *   <li>等锁超时：平台繁忙，等待读周期超时，请稍后重试</li>
 * </ul>
 *
 * <p>记住语义边界：503 是<b>平台侧没连上</b>，请求没有发出去，重试是安全的。
 * 设备侧的问题（含结果不确定）一律走 {@link PointDeviceException}（504）。
 */
public class PointConnectionException extends RuntimeException {

    /** 序列化标识 */
    private static final long serialVersionUID = 1L;

    public PointConnectionException(String message) {
        super(message);
    }

    public PointConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
