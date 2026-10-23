package com.labdatahub.component.mitsubishi_mc3e_tcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 三菱 MC 连接配置（存组件表 other_config JSON）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MitsubishiMc3eTcpConfig {
    // IP地址
    private String ipAddr;
    // 端口，MC协议 TCP 默认5007，UDP 以现场网关为准（如 5040）
    private Integer port = 5007;
    // 连接超时时间（TCP 建连超时 / UDP 单次收发超时）
    private Integer timeout = 3000;
    // 通信方式：tcp-长连接（默认）/ udp-数据报；历史组件没有这个键，反序列化为 null 时按 TCP 处理
    private String transport = "tcp";

    /**
     * 是否走 UDP 通信（transport=udp，大小写不敏感）
     *
     * <p>方法名刻意不用 is 前缀：fastjson 会把 {@code isXxx()} 当成 getter，
     * 序列化 other_config 时多吐一个 "udp" 键。
     */
    public boolean usesUdp() {
        return "udp".equalsIgnoreCase(transport);
    }
}
