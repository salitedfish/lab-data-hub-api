//由AI修改
package com.labdatahub.component.mitsubishi_mc3e_tcp;

/**
 * 三菱 MC 协议「设备已响应、但结束码非 0」异常
 *
 * <p>语义上要和传输层异常分清楚：<b>连接是好的，是 PLC 明确拒绝了这条命令</b>（点数超限、
 * 软元件不存在、软元件号越界等）。写值链路上据此回 504 并说明「本次未写入」——
 * 绝不能因为它触发重连，那是把它当成断线了，性质完全不同。
 */
public class MitsubishiMc3eResponseException extends Exception {

    private static final long serialVersionUID = 1L;

    public MitsubishiMc3eResponseException(String message) {
        super(message);
    }
}
