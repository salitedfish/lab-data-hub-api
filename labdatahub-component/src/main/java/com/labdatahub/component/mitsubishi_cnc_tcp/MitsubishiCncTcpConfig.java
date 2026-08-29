//由AI修改
package com.labdatahub.component.mitsubishi_cnc_tcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 三菱 CNC TCP（MELDAS MOCHA）连接配置（存组件表 other_config JSON）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MitsubishiCncTcpConfig {
    // IP地址
    private String ipAddr;
    // 端口，MOCHA 协议默认683
    private Integer port = 683;
    // 连接超时时间
    private Integer timeout = 3000;
}
