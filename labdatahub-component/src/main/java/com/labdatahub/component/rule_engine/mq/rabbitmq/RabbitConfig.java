package com.labdatahub.component.rule_engine.mq.rabbitmq;

import lombok.Data;

/**
 * @Description:
 * @Author: ruoyi
 * @CreateTime: 2025-10-30
 */
@Data
public class RabbitConfig {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String exchange;
    private String routingKey;
}
