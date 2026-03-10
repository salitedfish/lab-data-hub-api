package com.labdatahub.component.http;

import lombok.Data;

/**
 * @Description: 回复响应体
 * @Author: labdatahub
 * @CreateTime: 2026-01-06
 */
@Data
public class HttpResData {
    private Boolean httNeedReply;
    private Object data;
}
