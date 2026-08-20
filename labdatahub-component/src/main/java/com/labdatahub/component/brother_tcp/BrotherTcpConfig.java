//由AI修改
package com.labdatahub.component.brother_tcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Brother NC（兄弟数控）连接配置，从网络组件 otherConfig JSON 反序列化
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrotherTcpConfig {
    // IP地址
    private String ipAddr;
    // 端口，Brother NC 协议固定10000
    private Integer port = 10000;
    // 连接超时时间
    private Integer timeout = 3000;
}
