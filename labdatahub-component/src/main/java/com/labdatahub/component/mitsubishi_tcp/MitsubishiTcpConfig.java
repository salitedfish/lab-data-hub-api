package com.labdatahub.component.mitsubishi_tcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 三菱 MC 连接配置（存组件表 other_config JSON）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MitsubishiTcpConfig {
    // IP地址
    private String ipAddr;
    // 端口，MC协议默认5007
    private Integer port = 5007;
    // 连接超时时间
    private Integer timeout = 3000;
}
