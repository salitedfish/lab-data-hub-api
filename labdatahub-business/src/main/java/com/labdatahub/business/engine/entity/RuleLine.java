package com.labdatahub.business.engine.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 规则连线
 * @Author: ruoyi
 * @CreateTime: 2025-10-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleLine {
    private String from;
    private String to;
    private String label;
}
