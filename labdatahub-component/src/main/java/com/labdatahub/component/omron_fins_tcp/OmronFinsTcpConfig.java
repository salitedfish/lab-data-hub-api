package com.labdatahub.component.omron_fins_tcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OmronFinsTcpConfig {
    private String ipAddr;       // PLC IP地址
    private Integer port = 9600; // FINS TCP默认端口9600
    private Integer timeout = 3000; // 连接超时/读写超时（毫秒）
}