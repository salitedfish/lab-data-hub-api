package com.labdatahub.component.mqtt.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: MQTT服务端配置
 * @Author: labdatahub
 * @CreateTime: 2025-09-18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MqttBrokerConfig {
    private String id;
    private String username;
    private String password;
    private String tcpPort;
    private String wsPort;
    private Boolean allowAnonymous;
}
