package com.labdatahub.component.rule_engine.mq.rabbitmq;

import lombok.Data;

/**
 * @Description:
 * @Author: ruoyi
 * @CreateTime: 2025-10-31
 */
@Data
public class ExchangeConfig {
    private String exchange;
    private String routeKey;
}
