package com.labdatahub.business.engine.entity;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 规则节点
 * @Author: ruoyi
 * @CreateTime: 2025-10-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleNode {
    private String id;
    private String name;
    private String type;
    private String left;
    private String top;
    private String ico;
    private String state;
    private JSONObject configData;
}
