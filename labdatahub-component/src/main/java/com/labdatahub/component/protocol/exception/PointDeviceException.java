//由AI修改
package com.labdatahub.component.protocol.exception;

/**
 * 点位写入 —— 设备侧异常（映射为 HTTP 504）
 *
 * <p><b>语义包含「结果不确定」，这是本异常最重要的一点。</b> MODBUS 是请求-响应协议：
 * 请求发出去后等不到响应（超时），值<b>可能已经写进设备了</b>——只是响应包丢了。
 * 所以 504 <b>不等于「没写」</b>。
 *
 * <p>调用方看到 504 该怎么做：写点位是赋值操作，<b>重试是安全的</b>（再写一遍同一个值结果一致）；
 * 但<b>不能据此判定「写入失败」并改走人工处理</b>——那会把一次成功的写入当成失败。
 *
 * <p>覆盖的情形：
 * <ul>
 *   <li>请求超时 / 响应包丢失（结果不确定）</li>
 *   <li>设备返回异常响应（ExceptionResponse，功能码最高位为 1）—— 设备明确拒绝，
 *       如非法地址 / 非法值，此时<b>值为未写入</b></li>
 *   <li>回显地址或回显数量与请求不符 —— 设备确实处理了这次请求，但回显对不上，
 *       <b>值大概率已写入</b>，按「结果不确定」处理，不要当成「没写」</li>
 * </ul>
 */
public class PointDeviceException extends RuntimeException {

    /** 序列化标识 */
    private static final long serialVersionUID = 1L;

    public PointDeviceException(String message) {
        super(message);
    }

    public PointDeviceException(String message, Throwable cause) {
        super(message, cause);
    }
}
