package com.labdatahub.component.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description:
 * @Author: ruoyi
 * @CreateTime: 2025-10-27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebsocketServerConfig {
    private Integer port;
    private String path;
}
