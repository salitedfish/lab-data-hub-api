package com.labdatahub.component.rule_engine.mq.rocketmq;

import lombok.Data;

/**
 * @Description:
 * @Author: ruoyi
 * @CreateTime: 2025-10-30
 */
@Data
public class RocketConfig {
    private String host;
    private String port;
    private String topic;
    private String tags;
    private String username;
    private String password;
    private String group;
}
