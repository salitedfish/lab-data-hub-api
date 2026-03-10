package com.labdatahub.component.coap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 配置类
 * @Author: labdatahub
 * @CreateTime: 2025-10-15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoapServerConfig {
    private Integer port;
    private Boolean isAuth;
    private String tokenConfig;
}
