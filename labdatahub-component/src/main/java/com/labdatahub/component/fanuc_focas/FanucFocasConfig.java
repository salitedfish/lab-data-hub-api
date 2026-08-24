//由AI修改
package com.labdatahub.component.fanuc_focas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FANUC FOCAS2 连接配置，从网络组件 otherConfig JSON 反序列化
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FanucFocasConfig {
    // IP地址
    private String ipAddr;
    // 端口，FOCAS2 协议固定8193
    private Integer port = 8193;
    // 连接超时时间
    private Integer timeout = 3000;
    // fwlib32 库路径（可选，为空时自动搜索）
    private String libPath;
}
