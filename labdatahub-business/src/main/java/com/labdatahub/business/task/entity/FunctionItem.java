package com.labdatahub.business.task.entity;

import lombok.Data;

import java.time.LocalDateTime;

// FunctionItem实体（用于配置数据）
@Data
public class FunctionItem {
    private String id;
    private String functionName;
    private String functionCode;
    private String functionParams;
    private String belongSn;
    private String belongType;
    private String protocolId;
    private LocalDateTime createTime;
    private String createBy;
}
