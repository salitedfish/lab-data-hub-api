package com.labdatahub.component.s7_tcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class S7TcpConfig {
    private String ipAddr;      // PLC IP 地址
    private Integer port = 102; // 端口（默认102）
    private Integer timeout = 5000; // 连接超时（毫秒）
}