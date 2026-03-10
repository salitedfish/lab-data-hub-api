package com.labdatahub.business.warn.link;

import java.util.Arrays;
import java.util.Map;

/**
 * @Description:
 * @Author: ruoyi
 * @CreateTime: 2025-11-13
 */
public class RuleEngine {

    /**
     * 评估规则是否触发
     */
    public static boolean evaluate(RuleNode node, Map<String, Map<String, Object>> deviceData) {
        if (node instanceof ConditionNode) {
            return evaluateCondition((ConditionNode) node, deviceData);
        } else if (node instanceof LogicNode) {
            return evaluateLogic((LogicNode) node, deviceData);
        }
        return false;
    }

    public static boolean evaluateCondition(ConditionNode condition,
                                      Map<String, Map<String, Object>> deviceData) {
        String deviceSn = condition.getDeviceSn();
        String attribute = condition.getAttribute();

        if (!deviceData.containsKey(deviceSn)) {
            return false;
        }

        Object actualValue = deviceData.get(deviceSn).get(attribute);
        if (actualValue == null) {
            return false;
        }

        return compareValues(actualValue, condition.getOperator(), condition.getValue());
    }

    public static boolean evaluateLogic(LogicNode logic, Map<String, Map<String, Object>> deviceData) {
        if (logic.getChildren() == null || logic.getChildren().isEmpty()) {
            return false;
        }

        if ("and".equals(logic.getRelation())) {
            return logic.getChildren().stream()
                    .allMatch(child -> evaluate(child, deviceData));
        } else if ("or".equals(logic.getRelation())) {
            return logic.getChildren().stream()
                    .anyMatch(child -> evaluate(child, deviceData));
        }

        return false;
    }

    public static boolean compareValues(Object actual, String operator, String expected) {
        // 实现具体的比较逻辑
        try {
            switch (operator) {
                case "gt":
                    return Double.parseDouble(actual.toString()) > Double.parseDouble(expected);
                case "ge":
                    return Double.parseDouble(actual.toString()) >= Double.parseDouble(expected);
                case "eq":
                    return actual.toString().equals(expected);
                case "ne":
                    return !actual.toString().equals(expected);
                case "lt":
                    return Double.parseDouble(actual.toString()) < Double.parseDouble(expected);
                case "le":
                    return Double.parseDouble(actual.toString()) <= Double.parseDouble(expected);
                case "like":
                    return actual.toString().contains(expected);
                case "notLike":
                    return !actual.toString().contains(expected);
                case "in":
                    return Arrays.asList(expected.split(",")).contains(actual.toString());
                case "notIn":
                    return !Arrays.asList(expected.split(",")).contains(actual.toString());
                case "contains":
                    return expected.contains(actual.toString());
                case "notContains":
                    return !expected.contains(actual.toString());
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
