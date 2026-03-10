package com.labdatahub.business.warn.link;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 规则触发测试 (JDK8兼容版本)
 * @Author: ruoyi
 * @CreateTime: 2025-11-13
 */
public class TranslateRule {
    public static void main(String[] args) {
        // 1. 构建规则
        WarnLinkRule complexRule = buildComplexRule();

        // 2. 创建规则引擎
        RuleEngine ruleEngine = new RuleEngine();

        // 3. 生成规则白话文描述
        String ruleDescription = generateRuleDescription(complexRule.getRootNode());
        System.out.println("=== 规则白话文描述 ===");
        System.out.println(ruleDescription);
        System.out.println();

        // 4. 准备测试数据
        Map<String, Map<String, Object>> deviceData = new HashMap<>();

        // 测试场景1: 满足 A&B (应该触发)
        System.out.println("=== 测试场景1: 满足 A&B ===");
        deviceData.clear();

        Map<String, Object> device1Data = new HashMap<>();
        device1Data.put("temp", 85);      // A: temp > 80 -> true
        device1Data.put("humidity", 15);  // B: humidity < 20 -> true
        deviceData.put("device-001", device1Data);

        Map<String, Object> device2Data = new HashMap<>();
        device2Data.put("pressure", 90);  // C: pressure > 100 -> false
        device2Data.put("status", "normal"); // D: status = "error" -> false
        deviceData.put("device-002", device2Data);

        Map<String, Object> device3Data = new HashMap<>();
        device3Data.put("voltage", 250);  // E: voltage < 200 -> false
        device3Data.put("current", 5);    // F: current > 10 -> false
        deviceData.put("device-003", device3Data);

        boolean result1 = ruleEngine.evaluate(complexRule.getRootNode(), deviceData);
        System.out.println("规则触发结果: " + result1 + " (预期: true)");

        // 测试场景2: 满足 D&C (应该触发)
        System.out.println("\n=== 测试场景2: 满足 D&C ===");
        deviceData.clear();

        device1Data = new HashMap<>();
        device1Data.put("temp", 75);      // A: false
        device1Data.put("humidity", 25);  // B: false
        deviceData.put("device-001", device1Data);

        device2Data = new HashMap<>();
        device2Data.put("pressure", 120); // C: true
        device2Data.put("status", "error"); // D: true
        deviceData.put("device-002", device2Data);

        device3Data = new HashMap<>();
        device3Data.put("voltage", 250);  // E: false
        device3Data.put("current", 5);    // F: false
        deviceData.put("device-003", device3Data);

        boolean result2 = ruleEngine.evaluate(complexRule.getRootNode(), deviceData);
        System.out.println("规则触发结果: " + result2 + " (预期: true)");

        // 测试场景3: 满足 E&F (应该触发)
        System.out.println("\n=== 测试场景3: 满足 E&F ===");
        deviceData.clear();

        device1Data = new HashMap<>();
        device1Data.put("temp", 75);      // A: false
        device1Data.put("humidity", 25);  // B: false
        deviceData.put("device-001", device1Data);

        device2Data = new HashMap<>();
        device2Data.put("pressure", 90);  // C: false
        device2Data.put("status", "normal"); // D: false
        deviceData.put("device-002", device2Data);

        device3Data = new HashMap<>();
        device3Data.put("voltage", 180);  // E: true
        device3Data.put("current", 15);   // F: true
        deviceData.put("device-003", device3Data);

        boolean result3 = ruleEngine.evaluate(complexRule.getRootNode(), deviceData);
        System.out.println("规则触发结果: " + result3 + " (预期: true)");

        // 测试场景4: 都不满足 (不应该触发)
        System.out.println("\n=== 测试场景4: 都不满足 ===");
        deviceData.clear();

        device1Data = new HashMap<>();
        device1Data.put("temp", 75);      // A: false
        device1Data.put("humidity", 25);  // B: false
        deviceData.put("device-001", device1Data);

        device2Data = new HashMap<>();
        device2Data.put("pressure", 90);  // C: false
        device2Data.put("status", "normal"); // D: false
        deviceData.put("device-002", device2Data);

        device3Data = new HashMap<>();
        device3Data.put("voltage", 250);  // E: false
        device3Data.put("current", 5);    // F: false
        deviceData.put("device-003", device3Data);

        boolean result4 = ruleEngine.evaluate(complexRule.getRootNode(), deviceData);
        System.out.println("规则触发结果: " + result4 + " (预期: false)");

        // 测试场景5: 部分满足但不完整 (不应该触发)
        System.out.println("\n=== 测试场景5: 部分满足但不完整 ===");
        deviceData.clear();

        device1Data = new HashMap<>();
        device1Data.put("temp", 85);      // A: true
        device1Data.put("humidity", 25);  // B: false (A&B 不成立)
        deviceData.put("device-001", device1Data);

        device2Data = new HashMap<>();
        device2Data.put("pressure", 120); // C: true
        device2Data.put("status", "normal"); // D: false (D&C 不成立)
        deviceData.put("device-002", device2Data);

        device3Data = new HashMap<>();
        device3Data.put("voltage", 180);  // E: true
        device3Data.put("current", 5);    // F: false (E&F 不成立)
        deviceData.put("device-003", device3Data);

        boolean result5 = ruleEngine.evaluate(complexRule.getRootNode(), deviceData);
        System.out.println("规则触发结果: " + result5 + " (预期: false)");

        // 测试场景6: 多个条件组同时满足 (应该触发)
        System.out.println("\n=== 测试场景6: 多个条件组同时满足 ===");
        deviceData.clear();

        device1Data = new HashMap<>();
        device1Data.put("temp", 85);      // A: true
        device1Data.put("humidity", 15);  // B: true (A&B 成立)
        deviceData.put("device-001", device1Data);

        device2Data = new HashMap<>();
        device2Data.put("pressure", 120); // C: true
        device2Data.put("status", "error"); // D: true (D&C 成立)
        deviceData.put("device-002", device2Data);

        device3Data = new HashMap<>();
        device3Data.put("voltage", 180);  // E: true
        device3Data.put("current", 15);   // F: true (E&F 成立)
        deviceData.put("device-003", device3Data);

        boolean result6 = ruleEngine.evaluate(complexRule.getRootNode(), deviceData);
        System.out.println("规则触发结果: " + result6 + " (预期: true)");
    }

    private static WarnLinkRule buildComplexRule() {
        WarnLinkRule complexRule = new WarnLinkRule();
        complexRule.setId("rule-2");
        complexRule.setName("复杂嵌套规则");

        // 创建条件节点 - 现在使用RuleBuilder
        ConditionNode A = RuleBuilder.createCondition("A", "device-001", "temp", "gt", "80");
        ConditionNode B = RuleBuilder.createCondition("B", "device-001", "humidity", "lt", "20");
        ConditionNode C = RuleBuilder.createCondition("C", "device-002", "pressure", "gt", "100");
        ConditionNode D = RuleBuilder.createCondition("D", "device-002", "status", "eq", "error");
        ConditionNode E = RuleBuilder.createCondition("E", "device-003", "voltage", "lt", "200");
        ConditionNode F = RuleBuilder.createCondition("F", "device-003", "current", "gt", "10");

        // 设置设备名称和属性名称（在实际应用中应该从数据库或配置中获取）
        setDeviceAndAttributeNames(A, "温度传感器01", "温度");
        setDeviceAndAttributeNames(B, "温度传感器01", "湿度");
        setDeviceAndAttributeNames(C, "压力监测器02", "压力");
        setDeviceAndAttributeNames(D, "压力监测器02", "运行状态");
        setDeviceAndAttributeNames(E, "电力监测器03", "电压");
        setDeviceAndAttributeNames(F, "电力监测器03", "电流");

        // 构建逻辑树: (A&B||D&C)||(E&F) - 现在使用RuleBuilder
        LogicNode ab = RuleBuilder.createLogicNode("A与B", "and", A, B);
        LogicNode dc = RuleBuilder.createLogicNode("D与C", "and", D, C);
        LogicNode group1 = RuleBuilder.createLogicNode("第一组", "or", ab, dc);
        LogicNode group2 = RuleBuilder.createLogicNode("第二组", "and", E, F);
        LogicNode root = RuleBuilder.createLogicNode("根节点", "or", group1, group2);

        complexRule.setRootNode(root);
        return complexRule;
    }

    /**
     * 设置设备名称和属性名称
     */
    private static void setDeviceAndAttributeNames(ConditionNode node, String deviceName, String attributeName) {
        node.setDeviceName(deviceName);
        node.setAttributeName(attributeName);
    }

    /**
     * 生成规则的白话文描述
     */
    public static String generateRuleDescription(RuleNode rootNode) {
        if (!(rootNode instanceof LogicNode)) {
            return "规则格式错误：根节点必须是逻辑节点";
        }

        StringBuilder description = new StringBuilder();
        description.append("=== 规则白话文描述 ===\n\n");
        description.append("当以下任意一种情况发生时，就会触发告警：\n\n");

        generateNodeDescription(rootNode, description, 0, true);

        // 添加设备信息
        description.append("\n涉及的设备：\n");
        if (rootNode instanceof LogicNode) {
            java.util.Set<String> devices = new java.util.HashSet<>();
            collectDeviceInfo((LogicNode) rootNode, devices);
            for (String device : devices) {
                description.append("  - ").append(device).append("\n");
            }
        }

        return description.toString();
    }

    /**
     * 递归生成节点描述
     */
    private static void generateNodeDescription(RuleNode node, StringBuilder description, int level, boolean isRoot) {
        if (node == null) return;

        // JDK 8 兼容的缩进实现
        StringBuilder indentBuilder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indentBuilder.append("  ");
        }
        String indent = indentBuilder.toString();

        if (node instanceof LogicNode) {
            LogicNode logicNode = (LogicNode) node;
            String relation = getRelationChinese(logicNode.getRelation());

            if (!isRoot && logicNode.getChildren() != null && logicNode.getChildren().size() > 1) {
                description.append("(");
            }

            if (logicNode.getChildren() != null) {
                for (int i = 0; i < logicNode.getChildren().size(); i++) {
                    RuleNode child = logicNode.getChildren().get(i);

                    if (i > 0) {
                        description.append("\n").append(indent).append(relation).append(" ");
                    }

                    generateNodeDescription(child, description, level + 1, false);
                }
            }

            if (!isRoot && logicNode.getChildren() != null && logicNode.getChildren().size() > 1) {
                description.append(")");
            }

        } else if (node instanceof ConditionNode) {
            ConditionNode conditionNode = (ConditionNode) node;
            String deviceName = conditionNode.getDeviceName() != null ?
                    conditionNode.getDeviceName() : conditionNode.getDeviceSn();
            String attributeName = conditionNode.getAttributeName() != null ?
                    conditionNode.getAttributeName() : conditionNode.getAttribute();
            String operatorName = getOperatorChinese(conditionNode.getOperator());
            String value = conditionNode.getValue();

            description.append(deviceName)
                    .append("的")
                    .append(attributeName)
                    .append(" ")
                    .append(operatorName)
                    .append(" ")
                    .append(value)
                    .append(getValueUnit(conditionNode.getAttribute(), value));
        }
    }
    /**
     * 收集设备信息
     */
    private static void collectDeviceInfo(LogicNode node, java.util.Set<String> devices) {
        if (node.getChildren() != null) {
            for (RuleNode child : node.getChildren()) {
                if (child instanceof ConditionNode) {
                    ConditionNode condition = (ConditionNode) child;
                    String deviceInfo = condition.getDeviceName() != null ?
                            condition.getDeviceName() + " (" + condition.getDeviceSn() + ")" :
                            condition.getDeviceSn();
                    devices.add(deviceInfo);
                } else if (child instanceof LogicNode) {
                    collectDeviceInfo((LogicNode) child, devices);
                }
            }
        }
    }

    /**
     * 获取操作符的中文描述
     */
    private static String getOperatorChinese(String operator) {
        switch (operator) {
            case "gt": return "大于";
            case "ge": return "大于等于";
            case "eq": return "等于";
            case "ne": return "不等于";
            case "lt": return "小于";
            case "le": return "小于等于";
            case "like": return "包含";
            case "notLike": return "不包含";
            case "in": return "在列表中";
            case "notIn": return "不在列表中";
            case "contains": return "包含";
            case "notContains": return "不包含";
            default: return operator;
        }
    }

    /**
     * 获取逻辑关系的中文描述
     */
    private static String getRelationChinese(String relation) {
        switch (relation) {
            case "and": return "并且";
            case "or": return "或者";
            default: return relation;
        }
    }

    /**
     * 获取数值的单位
     */
    private static String getValueUnit(String attribute, String value) {
        if ("status".equals(attribute)) {
            // 状态值特殊处理
            switch (value) {
                case "0": return " (离线)";
                case "1": return " (在线)";
                default: return "";
            }
        }

        // 根据属性名判断单位
        switch (attribute) {
            case "temp":
            case "outTemperature":
            case "inTemperature": return "°C";
            case "humidity": return "%";
            case "windSpeed": return "m/s";
            case "voice": return "分贝";
            default: return "";
        }
    }
}