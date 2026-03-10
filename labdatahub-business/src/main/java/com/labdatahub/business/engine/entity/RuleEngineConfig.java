package com.labdatahub.business.engine.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 规则引擎配置
 * @Author: ruoyi
 * @CreateTime: 2025-10-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleEngineConfig {
    private List<RuleLine> lineList;
    private List<RuleNode> nodeList;
}
