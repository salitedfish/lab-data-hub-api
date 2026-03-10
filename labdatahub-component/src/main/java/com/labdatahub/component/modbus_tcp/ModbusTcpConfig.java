package com.labdatahub.component.modbus_tcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModbusTcpConfig {
    //IP
    private String ipAddr;
    //端口
    private Integer port;
    //连接超时时间
    private Integer timeout;
}
