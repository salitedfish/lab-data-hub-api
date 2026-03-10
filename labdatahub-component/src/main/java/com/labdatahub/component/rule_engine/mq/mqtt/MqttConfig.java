package com.labdatahub.component.rule_engine.mq.mqtt;

import lombok.Data;

/**
 * @Description:
 * @Author: ruoyi
 * @CreateTime: 2025-10-31
 */
@Data
public class MqttConfig {
    private String port;
    private String username;
    private String password;
    private Boolean allowAnonymous;
    private String topic;
}
