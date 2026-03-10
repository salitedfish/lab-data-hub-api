package com.labdatahub.component.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpServerConfig {
    //端口
    private Integer port;
    //是否需要回复 0-否 1-是
    private Boolean needReply;
}
