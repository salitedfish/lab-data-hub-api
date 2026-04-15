package com.labdatahub.component.fins_tcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinsTcpConfig {
    // IP地址
    private String ipAddr;
    // 端口，FINS/TCP默认9600
    private Integer port = 9600;
    // 连接超时时间
    private Integer timeout = 3000;
    // 客户端FINS节点地址，0表示自动获取
    private Integer clientNodeAddress = 1;
    // PLC FINS节点地址，连接后握手会自动获取，也可手动配置
    private Integer plcNodeAddress = 0;
}
