package com.labdatahub.component.coap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 资源配置类
 * @Author: labdatahub
 * @CreateTime: 2025-10-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoapResource {
    //请求方式 GET、POST、PUT、DELETE、FETCH、PATCH
    private String method;
    //请求路径
    private String path;
}
