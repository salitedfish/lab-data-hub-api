package com.labdatahub.component.rule_engine.mq.kafka;

import lombok.Data;

/**
 * @Description:
 * @Author: ruoyi
 * @CreateTime: 2025-10-30
 */
@Data
public class KafkaConfig {
    private String ipAddress;
    private Integer port;
    private String topic;
}
