package com.labdatahub.component.rule_engine.mq.http;

import lombok.Data;

/**
 * @Description:
 * @Author: ruoyi
 * @CreateTime: 2025-10-31
 */
@Data
public class HttpConfig {
    private String url;
    private String method;
    private String authHeaderSign;
    private String authToken;
}
