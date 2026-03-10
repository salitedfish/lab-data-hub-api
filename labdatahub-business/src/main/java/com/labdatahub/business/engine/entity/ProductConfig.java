package com.labdatahub.business.engine.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 规则数据源配置
 * @Author: ruoyi
 * @CreateTime: 2025-10-30
 */
@Data
public class ProductConfig {
    private List<String> productScope;
    private List<String> deviceSnList;
    private String deviceScope;
}
