package com.labdatahub.business.task.entity;

import lombok.Data;

import java.util.List;

// 配置数据实体
@Data
public class ConfigData {
    private String cronValue;
    private String productSn;
    private String deviceSn;
    private String functionCode;
    private String functionParams;
    private List<FunctionItem> functionList;
}
