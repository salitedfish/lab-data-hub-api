package com.labdatahub.business.warn.link;

import java.util.Arrays;
import java.util.UUID;

/**
 * @Description:
 * @Author: ruoyi
 * @CreateTime: 2025-11-13
 */
public class RuleBuilder {

    public static ConditionNode createCondition(String name, String deviceSn,
                                                String attribute, String operator, String value) {
        ConditionNode node = new ConditionNode();
        node.setId(UUID.randomUUID().toString());
        node.setName(name);
        node.setDeviceSn(deviceSn);
        node.setAttribute(attribute);
        node.setOperator(operator);
        node.setValue(value);
        return node;
    }

    public static LogicNode createLogicNode(String name, String relation, RuleNode... children) {
        LogicNode node = new LogicNode();
        node.setId(UUID.randomUUID().toString());
        node.setName(name);
        node.setRelation(relation);
        node.setChildren(Arrays.asList(children));
        return node;
    }
}
