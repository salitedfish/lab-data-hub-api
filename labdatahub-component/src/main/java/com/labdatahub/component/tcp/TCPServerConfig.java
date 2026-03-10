package com.labdatahub.component.tcp;

import lombok.Data;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-10-14
 */
@Data
public class TCPServerConfig {
    private Integer serverPort;
    private String delimiter;
    private Integer cacheSize;
}
