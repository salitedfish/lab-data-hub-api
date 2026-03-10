package com.labdatahub.business.warn.link;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.*;

/**
 * @Description: 规则配置验证器 - 修复版
 * @Author: ruoyi
 * @CreateTime: 2025-11-13
 */
public class RuleConfigValidator {

    /**
     * 验证规则配置是否正确
     */
    public static ValidationResult validateRuleConfig(String configJson) {
        try {
            JSONObject config = JSON.parseObject(configJson);
            JSONArray nodeList = config.getJSONArray("nodeList");
            JSONArray lineList = config.getJSONArray("lineList");

            // 构建节点映射
            Map<String, JSONObject> nodeMap = new HashMap<>();
            for (int i = 0; i < nodeList.size(); i++) {
                JSONObject node = nodeList.getJSONObject(i);
                nodeMap.put(node.getString("id"), node);
            }

            // 构建连接关系
            Map<String, List<String>> inConnections = buildInConnections(lineList);
            Map<String, List<String>> outConnections = buildOutConnections(lineList);

            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // 打印连接关系用于调试
            System.out.println("=== 连接关系调试信息 ===");
            System.out.println("入边连接: " + inConnections);
            System.out.println("出边连接: " + outConnections);

            // 1. 基础检查
            basicChecks(nodeMap, lineList, errors, warnings);

            // 2. 连接关系检查
            connectionChecks(nodeMap, inConnections, outConnections, errors, warnings);

            // 3. 规则结构检查
            ruleStructureChecks(nodeMap, inConnections, outConnections, errors, warnings);

            // 4. 配置完整性检查
            configIntegrityChecks(nodeMap, errors, warnings);

            return new ValidationResult(errors.isEmpty(), errors, warnings);

        } catch (Exception e) {
            List<String> errors = new ArrayList<>();
            errors.add("配置解析失败: " + e.getMessage());
            e.printStackTrace();
            return new ValidationResult(false, errors, new ArrayList<>());
        }
    }

    /**
     * 基础检查
     */
    private static void basicChecks(Map<String, JSONObject> nodeMap, JSONArray lineList,
                                    List<String> errors, List<String> warnings) {
        // 检查节点数量
        if (nodeMap.isEmpty()) {
            errors.add("配置中没有节点");
            return;
        }

        // 检查连线数量
        if (lineList == null || lineList.isEmpty()) {
            errors.add("配置中没有连线");
            return;
        }

        // 检查是否有条件节点
        boolean hasCondition = nodeMap.values().stream()
                .anyMatch(node -> {
                    String type = node.getString("type");
                    return "deviceProperty".equals(type) || "currentStatus".equals(type) || "changeStatus".equals(type);
                });
        if (!hasCondition) {
            errors.add("配置中缺少条件节点（设备属性/设备状态/状态变化）");
        }

//        // 检查是否有逻辑节点
//        boolean hasLogic = nodeMap.values().stream()
//                .anyMatch(node -> {
//                    String type = node.getString("type");
//                    return "and".equals(type) || "or".equals(type);
//                });
//        if (!hasLogic) {
//            errors.add("配置中缺少逻辑节点（并且/或者）");
//        }
//
//        // 检查是否有动作节点
//        boolean hasAction = nodeMap.values().stream()
//                .anyMatch(node -> {
//                    String type = node.getString("type");
//                    return "warn".equals(type) || "function".equals(type);
//                });
//        if (!hasAction) {
//            errors.add("配置中缺少动作节点（告警/指令下发）");
//        }
    }

    /**
     * 连接关系检查
     */
    private static void connectionChecks(Map<String, JSONObject> nodeMap,
                                         Map<String, List<String>> inConnections,
                                         Map<String, List<String>> outConnections,
                                         List<String> errors, List<String> warnings) {
        // 检查所有节点是否都有连接
        Set<String> connectedNodes = new HashSet<>();
        connectedNodes.addAll(inConnections.keySet());
        for (List<String> targets : outConnections.values()) {
            connectedNodes.addAll(targets);
        }

        for (String nodeId : nodeMap.keySet()) {
            JSONObject node = nodeMap.get(nodeId);
            String type = node.getString("type");

            // 动作节点可以有入边但没有出边
            if ("warn".equals(type) || "function".equals(type)) {
                if (!inConnections.containsKey(nodeId)) {
                    warnings.add("动作节点 '" + node.getString("name") + "' 没有输入连接");
                }
                continue;
            }

            // 条件节点应该有出边
            if ("deviceProperty".equals(type) || "currentStatus".equals(type) || "changeStatus".equals(type)) {
                if (!outConnections.containsKey(nodeId)) {
                    errors.add("条件节点 '" + node.getString("name") + "' 没有输出连接");
                }
                continue;
            }

            // 逻辑节点应该有入边和出边
            if ("and".equals(type) || "or".equals(type)) {
                if (!inConnections.containsKey(nodeId)) {
                    errors.add("逻辑节点 '" + node.getString("name") + "' 没有输入连接");
                }
                if (!outConnections.containsKey(nodeId)) {
                    errors.add("逻辑节点 '" + node.getString("name") + "' 没有输出连接");
                }
            }
        }

        // 检查循环引用
        checkForCycles(nodeMap, outConnections, errors);
    }

    /**
     * 规则结构检查
     */
    private static void ruleStructureChecks(Map<String, JSONObject> nodeMap,
                                            Map<String, List<String>> inConnections,
                                            Map<String, List<String>> outConnections,
                                            List<String> errors, List<String> warnings) {
        // 找到所有动作节点
        Set<String> actionNodes = new HashSet<>();
        for (String nodeId : nodeMap.keySet()) {
            JSONObject node = nodeMap.get(nodeId);
            String type = node.getString("type");
            if ("warn".equals(type) || "function".equals(type)) {
                actionNodes.add(nodeId);
            }
        }

        // 检查动作节点是否有正确的输入
        for (String actionNodeId : actionNodes) {
            List<String> inputs = inConnections.get(actionNodeId);
            if (inputs == null || inputs.isEmpty()) {
                errors.add("动作节点 '" + nodeMap.get(actionNodeId).getString("name") + "' 没有输入条件");
                continue;
            }

            // 检查动作节点的输入是否是逻辑节点
            for (String inputId : inputs) {
                JSONObject inputNode = nodeMap.get(inputId);
                if (inputNode != null) {
                    String inputType = inputNode.getString("type");
                    if (!"and".equals(inputType) && !"or".equals(inputType)) {
                        warnings.add("动作节点 '" + nodeMap.get(actionNodeId).getString("name") +
                                "' 的输入 '" + inputNode.getString("name") + "' 不是逻辑节点");
                    }
                }
            }
        }

        // 检查是否有有效的规则路径
//        checkValidRulePaths(nodeMap, inConnections, outConnections, actionNodes, errors);
    }

    /**
     * 配置完整性检查
     */
    private static void configIntegrityChecks(Map<String, JSONObject> nodeMap,
                                              List<String> errors, List<String> warnings) {
        for (JSONObject node : nodeMap.values()) {
            String type = node.getString("type");
            JSONObject configData = node.getJSONObject("configData");

            if (configData == null) {
                continue; // 有些节点可能没有configData
            }

            switch (type) {
                case "deviceProperty":
                    checkDevicePropertyConfig(node, configData, errors);
                    break;
                case "currentStatus":
                    checkCurrentStatusConfig(node, configData, errors);
                    break;
                case "changeStatus":
                    checkChangeStatusConfig(node, configData, errors);
                    break;
                case "warn":
                    checkWarnConfig(node, configData, errors, warnings);
                    break;
                case "function":
                    checkFunctionConfig(node, configData, errors, warnings);
                    break;
            }
        }
    }

    /**
     * 检查设备属性配置
     */
    private static void checkDevicePropertyConfig(JSONObject node, JSONObject configData, List<String> errors) {
        String deviceSn = configData.getString("deviceSn");
        String attribute = configData.getString("attribute");
        String operator = configData.getString("operator");
        String value = configData.getString("value");

        if (deviceSn == null || deviceSn.trim().isEmpty()) {
            errors.add("设备属性节点 '" + node.getString("name") + "' 未配置设备");
        }
        if (attribute == null || attribute.trim().isEmpty()) {
            errors.add("设备属性节点 '" + node.getString("name") + "' 未配置属性");
        }
        if (operator == null || operator.trim().isEmpty()) {
            errors.add("设备属性节点 '" + node.getString("name") + "' 未配置比较操作符");
        }
        if (value == null || value.trim().isEmpty()) {
            errors.add("设备属性节点 '" + node.getString("name") + "' 未配置比较值");
        }
    }

    /**
     * 检查当前状态配置
     */
    private static void checkCurrentStatusConfig(JSONObject node, JSONObject configData, List<String> errors) {
        String deviceSn = configData.getString("deviceSn");
        String currentStatus = configData.getString("currentStatus");

        if (deviceSn == null || deviceSn.trim().isEmpty()) {
            errors.add("设备当前状态节点 '" + node.getString("name") + "' 未配置设备");
        }
        if (currentStatus == null || currentStatus.trim().isEmpty()) {
            errors.add("设备当前状态节点 '" + node.getString("name") + "' 未配置状态值");
        }
    }

    /**
     * 检查状态变化配置
     */
    private static void checkChangeStatusConfig(JSONObject node, JSONObject configData, List<String> errors) {
        String deviceSn = configData.getString("deviceSn");
        String changeStatus = configData.getString("changeStatus");

        if (deviceSn == null || deviceSn.trim().isEmpty()) {
            errors.add("设备状态变化节点 '" + node.getString("name") + "' 未配置设备");
        }
        if (changeStatus == null || changeStatus.trim().isEmpty()) {
            errors.add("设备状态变化节点 '" + node.getString("name") + "' 未配置状态值");
        }
    }

    /**
     * 检查告警配置
     */
    private static void checkWarnConfig(JSONObject node, JSONObject configData, List<String> errors, List<String> warnings) {
        String warnMessage = configData.getString("warnMessage");
        if (warnMessage == null || warnMessage.trim().isEmpty()) {
            warnings.add("告警节点 '" + node.getString("name") + "' 未配置告警消息");
        }
    }

    /**
     * 检查指令下发配置
     */
    private static void checkFunctionConfig(JSONObject node, JSONObject configData, List<String> errors, List<String> warnings) {
        String deviceSn = configData.getString("deviceSn");
        String functionCode = configData.getString("functionCode");

        if (deviceSn == null || deviceSn.trim().isEmpty()) {
            errors.add("指令下发节点 '" + node.getString("name") + "' 未配置设备");
        }
        if (functionCode == null || functionCode.trim().isEmpty()) {
            errors.add("指令下发节点 '" + node.getString("name") + "' 未配置功能代码");
        }
    }

    /**
     * 检查循环引用
     */
    private static void checkForCycles(Map<String, JSONObject> nodeMap,
                                       Map<String, List<String>> outConnections,
                                       List<String> errors) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String nodeId : nodeMap.keySet()) {
            if (!visited.contains(nodeId)) {
                if (hasCycle(nodeId, nodeMap, outConnections, visited, recursionStack, new ArrayList<>())) {
                    errors.add("检测到循环引用，请检查节点连接关系");
                    return;
                }
            }
        }
    }

    /**
     * 检查是否有循环引用
     */
    private static boolean hasCycle(String nodeId, Map<String, JSONObject> nodeMap,
                                    Map<String, List<String>> outConnections,
                                    Set<String> visited, Set<String> recursionStack,
                                    List<String> path) {
        visited.add(nodeId);
        recursionStack.add(nodeId);
        path.add(nodeId);

        List<String> neighbors = outConnections.get(nodeId);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                if (!recursionStack.contains(neighbor)) {
                    if (!visited.contains(neighbor)) {
                        if (hasCycle(neighbor, nodeMap, outConnections, visited, recursionStack, path)) {
                            return true;
                        }
                    }
                } else {
                    // 发现循环
                    System.out.println("发现循环引用路径: " + path + " -> " + neighbor);
                    return true;
                }
            }
        }

        recursionStack.remove(nodeId);
        path.remove(path.size() - 1);
        return false;
    }

    /**
     * 检查有效的规则路径
     */
    private static void checkValidRulePaths(Map<String, JSONObject> nodeMap,
                                            Map<String, List<String>> inConnections,
                                            Map<String, List<String>> outConnections,
                                            Set<String> actionNodes,
                                            List<String> errors) {
        // 从每个动作节点开始，反向遍历到条件节点，检查是否有完整路径
        for (String actionNodeId : actionNodes) {
            if (!hasValidPathToConditions(actionNodeId, nodeMap, inConnections, new HashSet<>())) {
                errors.add("动作节点 '" + nodeMap.get(actionNodeId).getString("name") +
                        "' 没有有效的条件路径");
            }
        }
    }

    /**
     * 检查是否有有效路径到条件节点
     */
    private static boolean hasValidPathToConditions(String nodeId, Map<String, JSONObject> nodeMap,
                                                    Map<String, List<String>> inConnections,
                                                    Set<String> visited) {
        if (visited.contains(nodeId)) {
            return false; // 避免循环
        }
        visited.add(nodeId);

        JSONObject node = nodeMap.get(nodeId);
        String type = node.getString("type");

        // 如果是条件节点，返回true
        if ("deviceProperty".equals(type) || "currentStatus".equals(type) || "changeStatus".equals(type)) {
            return true;
        }

        // 如果是逻辑节点，检查所有输入是否有有效路径
        if ("and".equals(type) || "or".equals(type)) {
            List<String> inputs = inConnections.get(nodeId);
            if (inputs != null && !inputs.isEmpty()) {
                for (String inputId : inputs) {
                    if (hasValidPathToConditions(inputId, nodeMap, inConnections, new HashSet<>(visited))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 构建入边连接关系映射 (to -> from)
     */
    private static Map<String, List<String>> buildInConnections(JSONArray lineList) {
        Map<String, List<String>> inConnections = new HashMap<>();

        if (lineList != null) {
            for (int i = 0; i < lineList.size(); i++) {
                JSONObject line = lineList.getJSONObject(i);
                String from = line.getString("from");
                String to = line.getString("to");

                inConnections.computeIfAbsent(to, k -> new ArrayList<>()).add(from);
            }
        }

        return inConnections;
    }

    /**
     * 构建出边连接关系映射 (from -> to)
     */
    private static Map<String, List<String>> buildOutConnections(JSONArray lineList) {
        Map<String, List<String>> outConnections = new HashMap<>();

        if (lineList != null) {
            for (int i = 0; i < lineList.size(); i++) {
                JSONObject line = lineList.getJSONObject(i);
                String from = line.getString("from");
                String to = line.getString("to");

                outConnections.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
            }
        }

        return outConnections;
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;

        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("配置验证结果: ").append(valid ? "✅ 通过" : "❌ 失败").append("\n");

            if (!errors.isEmpty()) {
                sb.append("\n错误信息 (").append(errors.size()).append("):\n");
                for (int i = 0; i < errors.size(); i++) {
                    sb.append("  ").append(i + 1).append(". ").append(errors.get(i)).append("\n");
                }
            }

            if (!warnings.isEmpty()) {
                sb.append("\n警告信息 (").append(warnings.size()).append("):\n");
                for (int i = 0; i < warnings.size(); i++) {
                    sb.append("  ").append(i + 1).append(". ").append(warnings.get(i)).append("\n");
                }
            }

            return sb.toString();
        }
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 这里放入您的JSON配置数据进行测试
        String jsonConfig = "{\n" +
                "    \"nodeList\": [\n" +
                "        {\n" +
                "            \"id\": \"0hdg25tugu\",\n" +
                "            \"name\": \"设备属性\",\n" +
                "            \"type\": \"deviceProperty\",\n" +
                "            \"left\": \"33px\",\n" +
                "            \"top\": \"103px\",\n" +
                "            \"ico\": \"el-icon-time\",\n" +
                "            \"state\": \"success\",\n" +
                "            \"configData\": {\n" +
                "                \"productList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736431073325058\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983886502083457026\",\n" +
                "                        \"productSn\": \"mqtt_broker_001\",\n" +
                "                        \"productName\": \"MQTT服务端产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983886026080284674\",\n" +
                "                        \"componentName\": \"MQTT服务端\",\n" +
                "                        \"protocolId\": \"1983885892432982018\",\n" +
                "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"2\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"deviceList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736532730671105\",\n" +
                "                        \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                        \"deviceName\": \"WS设备001\",\n" +
                "                        \"productId\": \"1983736431073325058\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": \"admin\",\n" +
                "                        \"createTime\": \"2025-10-30 11:23:40\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"month\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"propertyList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983737513321197570\",\n" +
                "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                "                        \"belongType\": \"1\",\n" +
                "                        \"identifier\": \"voice\",\n" +
                "                        \"name\": \"音量\",\n" +
                "                        \"parentId\": \"0\",\n" +
                "                        \"dataType\": \"float\",\n" +
                "                        \"sortNum\": 0,\n" +
                "                        \"fromType\": \"0\",\n" +
                "                        \"remark\": null,\n" +
                "                        \"unit\": \"分贝\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983737513321197571\",\n" +
                "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                "                        \"belongType\": \"1\",\n" +
                "                        \"identifier\": \"windSpeed\",\n" +
                "                        \"name\": \"风速\",\n" +
                "                        \"parentId\": \"0\",\n" +
                "                        \"dataType\": \"float\",\n" +
                "                        \"sortNum\": 0,\n" +
                "                        \"fromType\": \"0\",\n" +
                "                        \"remark\": null,\n" +
                "                        \"unit\": \"m/s\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983737513321197572\",\n" +
                "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                "                        \"belongType\": \"1\",\n" +
                "                        \"identifier\": \"humidity\",\n" +
                "                        \"name\": \"湿度\",\n" +
                "                        \"parentId\": \"0\",\n" +
                "                        \"dataType\": \"float\",\n" +
                "                        \"sortNum\": 0,\n" +
                "                        \"fromType\": \"0\",\n" +
                "                        \"remark\": null,\n" +
                "                        \"unit\": \"%rh\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983737513321197573\",\n" +
                "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                "                        \"belongType\": \"1\",\n" +
                "                        \"identifier\": \"outTemperature\",\n" +
                "                        \"name\": \"室外温度\",\n" +
                "                        \"parentId\": \"0\",\n" +
                "                        \"dataType\": \"float\",\n" +
                "                        \"sortNum\": 0,\n" +
                "                        \"fromType\": \"0\",\n" +
                "                        \"remark\": null,\n" +
                "                        \"unit\": \"℃\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983737513321197574\",\n" +
                "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                "                        \"belongType\": \"1\",\n" +
                "                        \"identifier\": \"inTemperature\",\n" +
                "                        \"name\": \"室内温度\",\n" +
                "                        \"parentId\": \"0\",\n" +
                "                        \"dataType\": \"float\",\n" +
                "                        \"sortNum\": 0,\n" +
                "                        \"fromType\": \"0\",\n" +
                "                        \"remark\": null,\n" +
                "                        \"unit\": \"℃\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                \"productSn\": \"product_001\",\n" +
                "                \"currentStatus\": null,\n" +
                "                \"changeStatus\": null,\n" +
                "                \"warnMessage\": null,\n" +
                "                \"functionList\": [],\n" +
                "                \"functionParams\": null,\n" +
                "                \"attribute\": \"voice\",\n" +
                "                \"operator\": \"gt\",\n" +
                "                \"value\": \"5\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"36qp7zklk2\",\n" +
                "            \"name\": \"设备当前状态\",\n" +
                "            \"type\": \"currentStatus\",\n" +
                "            \"left\": \"32px\",\n" +
                "            \"top\": \"217px\",\n" +
                "            \"ico\": \"el-icon-odometer\",\n" +
                "            \"state\": \"success\",\n" +
                "            \"configData\": {\n" +
                "                \"productList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736431073325058\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983886502083457026\",\n" +
                "                        \"productSn\": \"mqtt_broker_001\",\n" +
                "                        \"productName\": \"MQTT服务端产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983886026080284674\",\n" +
                "                        \"componentName\": \"MQTT服务端\",\n" +
                "                        \"protocolId\": \"1983885892432982018\",\n" +
                "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"2\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"deviceList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736532730671105\",\n" +
                "                        \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                        \"deviceName\": \"WS设备001\",\n" +
                "                        \"productId\": \"1983736431073325058\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": \"admin\",\n" +
                "                        \"createTime\": \"2025-10-30 11:23:40\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"month\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"propertyList\": [],\n" +
                "                \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                \"productSn\": \"product_001\",\n" +
                "                \"currentStatus\": \"1\",\n" +
                "                \"changeStatus\": null,\n" +
                "                \"warnMessage\": null,\n" +
                "                \"functionList\": [],\n" +
                "                \"functionParams\": null\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"a4mnoswmnm\",\n" +
                "            \"name\": \"设备属性1\",\n" +
                "            \"type\": \"deviceProperty\",\n" +
                "            \"left\": \"30px\",\n" +
                "            \"top\": \"311px\",\n" +
                "            \"ico\": \"el-icon-time\",\n" +
                "            \"state\": \"success\",\n" +
                "            \"configData\": {\n" +
                "                \"productList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736431073325058\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983886502083457026\",\n" +
                "                        \"productSn\": \"mqtt_broker_001\",\n" +
                "                        \"productName\": \"MQTT服务端产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983886026080284674\",\n" +
                "                        \"componentName\": \"MQTT服务端\",\n" +
                "                        \"protocolId\": \"1983885892432982018\",\n" +
                "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"2\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"deviceList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736532730671105\",\n" +
                "                        \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                        \"deviceName\": \"WS设备001\",\n" +
                "                        \"productId\": \"1983736431073325058\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": \"admin\",\n" +
                "                        \"createTime\": \"2025-10-30 11:23:40\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"month\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"propertyList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983737513321197570\",\n" +
                "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                "                        \"belongType\": \"1\",\n" +
                "                        \"identifier\": \"voice\",\n" +
                "                        \"name\": \"音量\",\n" +
                "                        \"parentId\": \"0\",\n" +
                "                        \"dataType\": \"float\",\n" +
                "                        \"sortNum\": 0,\n" +
                "                        \"fromType\": \"0\",\n" +
                "                        \"remark\": null,\n" +
                "                        \"unit\": \"分贝\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983737513321197571\",\n" +
                "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                "                        \"belongType\": \"1\",\n" +
                "                        \"identifier\": \"windSpeed\",\n" +
                "                        \"name\": \"风速\",\n" +
                "                        \"parentId\": \"0\",\n" +
                "                        \"dataType\": \"float\",\n" +
                "                        \"sortNum\": 0,\n" +
                "                        \"fromType\": \"0\",\n" +
                "                        \"remark\": null,\n" +
                "                        \"unit\": \"m/s\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983737513321197572\",\n" +
                "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                "                        \"belongType\": \"1\",\n" +
                "                        \"identifier\": \"humidity\",\n" +
                "                        \"name\": \"湿度\",\n" +
                "                        \"parentId\": \"0\",\n" +
                "                        \"dataType\": \"float\",\n" +
                "                        \"sortNum\": 0,\n" +
                "                        \"fromType\": \"0\",\n" +
                "                        \"remark\": null,\n" +
                "                        \"unit\": \"%rh\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983737513321197573\",\n" +
                "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                "                        \"belongType\": \"1\",\n" +
                "                        \"identifier\": \"outTemperature\",\n" +
                "                        \"name\": \"室外温度\",\n" +
                "                        \"parentId\": \"0\",\n" +
                "                        \"dataType\": \"float\",\n" +
                "                        \"sortNum\": 0,\n" +
                "                        \"fromType\": \"0\",\n" +
                "                        \"remark\": null,\n" +
                "                        \"unit\": \"℃\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983737513321197574\",\n" +
                "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                "                        \"belongType\": \"1\",\n" +
                "                        \"identifier\": \"inTemperature\",\n" +
                "                        \"name\": \"室内温度\",\n" +
                "                        \"parentId\": \"0\",\n" +
                "                        \"dataType\": \"float\",\n" +
                "                        \"sortNum\": 0,\n" +
                "                        \"fromType\": \"0\",\n" +
                "                        \"remark\": null,\n" +
                "                        \"unit\": \"℃\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                \"productSn\": \"product_001\",\n" +
                "                \"currentStatus\": null,\n" +
                "                \"changeStatus\": null,\n" +
                "                \"warnMessage\": null,\n" +
                "                \"functionList\": [],\n" +
                "                \"functionParams\": null,\n" +
                "                \"attribute\": \"humidity\",\n" +
                "                \"operator\": \"eq\",\n" +
                "                \"value\": \"50\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"2fzxp9ijsj\",\n" +
                "            \"name\": \"并且\",\n" +
                "            \"type\": \"and\",\n" +
                "            \"left\": \"224px\",\n" +
                "            \"top\": \"158px\",\n" +
                "            \"ico\": \"el-icon-caret-right\",\n" +
                "            \"state\": \"success\",\n" +
                "            \"configData\": {\n" +
                "                \"productList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736431073325058\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983886502083457026\",\n" +
                "                        \"productSn\": \"mqtt_broker_001\",\n" +
                "                        \"productName\": \"MQTT服务端产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983886026080284674\",\n" +
                "                        \"componentName\": \"MQTT服务端\",\n" +
                "                        \"protocolId\": \"1983885892432982018\",\n" +
                "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"2\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"deviceList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736532730671105\",\n" +
                "                        \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                        \"deviceName\": \"WS设备001\",\n" +
                "                        \"productId\": \"1983736431073325058\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": \"admin\",\n" +
                "                        \"createTime\": \"2025-10-30 11:23:40\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"month\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"propertyList\": [],\n" +
                "                \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                \"productSn\": \"product_001\",\n" +
                "                \"currentStatus\": null,\n" +
                "                \"changeStatus\": null,\n" +
                "                \"warnMessage\": null,\n" +
                "                \"functionList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983737461873864705\",\n" +
                "                        \"functionName\": \"测试指令\",\n" +
                "                        \"functionCode\": \"TEST\",\n" +
                "                        \"functionParams\": \"123456\",\n" +
                "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                "                        \"belongType\": \"1\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"createTime\": \"2025-10-30 20:15:03\",\n" +
                "                        \"createBy\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"functionParams\": \"123456\",\n" +
                "                \"functionCode\": \"TEST\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"cvq926zif\",\n" +
                "            \"name\": \"或者\",\n" +
                "            \"type\": \"or\",\n" +
                "            \"left\": \"227px\",\n" +
                "            \"top\": \"358px\",\n" +
                "            \"ico\": \"el-icon-shopping-cart-full\",\n" +
                "            \"state\": \"success\",\n" +
                "            \"configData\": {\n" +
                "                \"productList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736431073325058\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983886502083457026\",\n" +
                "                        \"productSn\": \"mqtt_broker_001\",\n" +
                "                        \"productName\": \"MQTT服务端产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983886026080284674\",\n" +
                "                        \"componentName\": \"MQTT服务端\",\n" +
                "                        \"protocolId\": \"1983885892432982018\",\n" +
                "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"2\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"deviceList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736532730671105\",\n" +
                "                        \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                        \"deviceName\": \"WS设备001\",\n" +
                "                        \"productId\": \"1983736431073325058\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": \"admin\",\n" +
                "                        \"createTime\": \"2025-10-30 11:23:40\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"month\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"propertyList\": [],\n" +
                "                \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                \"productSn\": \"product_001\",\n" +
                "                \"currentStatus\": null,\n" +
                "                \"changeStatus\": null,\n" +
                "                \"warnMessage\": null,\n" +
                "                \"functionList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983737461873864705\",\n" +
                "                        \"functionName\": \"测试指令\",\n" +
                "                        \"functionCode\": \"TEST\",\n" +
                "                        \"functionParams\": \"123456\",\n" +
                "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                "                        \"belongType\": \"1\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"createTime\": \"2025-10-30 20:15:03\",\n" +
                "                        \"createBy\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"functionParams\": \"123456\",\n" +
                "                \"functionCode\": \"TEST\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"zh7qdt3mke\",\n" +
                "            \"name\": \"设备状态变化\",\n" +
                "            \"type\": \"changeStatus\",\n" +
                "            \"left\": \"36px\",\n" +
                "            \"top\": \"427px\",\n" +
                "            \"ico\": \"el-icon-odometer\",\n" +
                "            \"state\": \"success\",\n" +
                "            \"configData\": {\n" +
                "                \"productList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736431073325058\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983886502083457026\",\n" +
                "                        \"productSn\": \"mqtt_broker_001\",\n" +
                "                        \"productName\": \"MQTT服务端产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983886026080284674\",\n" +
                "                        \"componentName\": \"MQTT服务端\",\n" +
                "                        \"protocolId\": \"1983885892432982018\",\n" +
                "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"2\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"deviceList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736532730671105\",\n" +
                "                        \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                        \"deviceName\": \"WS设备001\",\n" +
                "                        \"productId\": \"1983736431073325058\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": \"admin\",\n" +
                "                        \"createTime\": \"2025-10-30 11:23:40\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"month\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"propertyList\": [],\n" +
                "                \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                \"productSn\": \"product_001\",\n" +
                "                \"currentStatus\": null,\n" +
                "                \"changeStatus\": \"0\",\n" +
                "                \"warnMessage\": null,\n" +
                "                \"functionList\": [],\n" +
                "                \"functionParams\": null\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"wc9mtjpq\",\n" +
                "            \"name\": \"并且1\",\n" +
                "            \"type\": \"and\",\n" +
                "            \"left\": \"465px\",\n" +
                "            \"top\": \"259px\",\n" +
                "            \"ico\": \"el-icon-caret-right\",\n" +
                "            \"state\": \"success\",\n" +
                "            \"configData\": {\n" +
                "                \"productList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736431073325058\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983886502083457026\",\n" +
                "                        \"productSn\": \"mqtt_broker_001\",\n" +
                "                        \"productName\": \"MQTT服务端产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983886026080284674\",\n" +
                "                        \"componentName\": \"MQTT服务端\",\n" +
                "                        \"protocolId\": \"1983885892432982018\",\n" +
                "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"2\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"deviceList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736532730671105\",\n" +
                "                        \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                        \"deviceName\": \"WS设备001\",\n" +
                "                        \"productId\": \"1983736431073325058\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": \"admin\",\n" +
                "                        \"createTime\": \"2025-10-30 11:23:40\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"month\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"propertyList\": [],\n" +
                "                \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                \"productSn\": \"product_001\",\n" +
                "                \"currentStatus\": null,\n" +
                "                \"changeStatus\": null,\n" +
                "                \"warnMessage\": null,\n" +
                "                \"functionList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983737461873864705\",\n" +
                "                        \"functionName\": \"测试指令\",\n" +
                "                        \"functionCode\": \"TEST\",\n" +
                "                        \"functionParams\": \"123456\",\n" +
                "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                "                        \"belongType\": \"1\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"createTime\": \"2025-10-30 20:15:03\",\n" +
                "                        \"createBy\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"functionParams\": \"123456\",\n" +
                "                \"functionCode\": \"TEST\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"k8ivi54e8\",\n" +
                "            \"name\": \"触发告警\",\n" +
                "            \"type\": \"warn\",\n" +
                "            \"left\": \"787px\",\n" +
                "            \"top\": \"185px\",\n" +
                "            \"ico\": \"el-icon-caret-right\",\n" +
                "            \"state\": \"success\",\n" +
                "            \"configData\": {\n" +
                "                \"productList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736431073325058\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983886502083457026\",\n" +
                "                        \"productSn\": \"mqtt_broker_001\",\n" +
                "                        \"productName\": \"MQTT服务端产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983886026080284674\",\n" +
                "                        \"componentName\": \"MQTT服务端\",\n" +
                "                        \"protocolId\": \"1983885892432982018\",\n" +
                "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"2\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"deviceList\": [],\n" +
                "                \"propertyList\": [],\n" +
                "                \"deviceSn\": null,\n" +
                "                \"productSn\": null,\n" +
                "                \"currentStatus\": null,\n" +
                "                \"changeStatus\": null,\n" +
                "                \"warnMessage\": \"123456\",\n" +
                "                \"functionList\": [],\n" +
                "                \"functionParams\": null,\n" +
                "                \"warnLevel\": \"1\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"lz09oa69bq\",\n" +
                "            \"name\": \"指令下发\",\n" +
                "            \"type\": \"function\",\n" +
                "            \"left\": \"791px\",\n" +
                "            \"top\": \"342px\",\n" +
                "            \"ico\": \"el-icon-shopping-cart-full\",\n" +
                "            \"state\": \"success\",\n" +
                "            \"configData\": {\n" +
                "                \"productList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736431073325058\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1983886502083457026\",\n" +
                "                        \"productSn\": \"mqtt_broker_001\",\n" +
                "                        \"productName\": \"MQTT服务端产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983886026080284674\",\n" +
                "                        \"componentName\": \"MQTT服务端\",\n" +
                "                        \"protocolId\": \"1983885892432982018\",\n" +
                "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"2\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"deviceList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983736532730671105\",\n" +
                "                        \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                        \"deviceName\": \"WS设备001\",\n" +
                "                        \"productId\": \"1983736431073325058\",\n" +
                "                        \"productName\": \"WS产品\",\n" +
                "                        \"productSn\": \"product_001\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"componentId\": \"1983736295886712834\",\n" +
                "                        \"componentName\": \"WS网络组件_10883\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"protocolName\": \"WS协议\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": \"admin\",\n" +
                "                        \"createTime\": \"2025-10-30 11:23:40\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"deviceType\": \"0\",\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"month\",\n" +
                "                        \"customConfig\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"propertyList\": [],\n" +
                "                \"deviceSn\": \"WS_DEVICE_001\",\n" +
                "                \"productSn\": \"product_001\",\n" +
                "                \"currentStatus\": null,\n" +
                "                \"changeStatus\": null,\n" +
                "                \"warnMessage\": null,\n" +
                "                \"functionList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1983737461873864705\",\n" +
                "                        \"functionName\": \"测试指令\",\n" +
                "                        \"functionCode\": \"TEST\",\n" +
                "                        \"functionParams\": \"123456\",\n" +
                "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                "                        \"belongType\": \"1\",\n" +
                "                        \"protocolId\": \"1983735910950268930\",\n" +
                "                        \"createTime\": \"2025-10-30 20:15:03\",\n" +
                "                        \"createBy\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"functionParams\": \"123456\",\n" +
                "                \"functionCode\": \"TEST\"\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"lineList\": [\n" +
                "        {\n" +
                "            \"from\": \"a4mnoswmnm\",\n" +
                "            \"to\": \"cvq926zif\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"from\": \"zh7qdt3mke\",\n" +
                "            \"to\": \"cvq926zif\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"from\": \"36qp7zklk2\",\n" +
                "            \"to\": \"2fzxp9ijsj\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"from\": \"0hdg25tugu\",\n" +
                "            \"to\": \"2fzxp9ijsj\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"from\": \"2fzxp9ijsj\",\n" +
                "            \"to\": \"wc9mtjpq\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"from\": \"wc9mtjpq\",\n" +
                "            \"to\": \"k8ivi54e8\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"from\": \"wc9mtjpq\",\n" +
                "            \"to\": \"lz09oa69bq\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        ValidationResult result = validateRuleConfig(jsonConfig);
        System.out.println(result.toString());

        if (result.isValid()) {
            System.out.println("\n✅ 配置验证通过，可以正常使用");
        } else {
            System.out.println("\n❌ 配置验证失败，请修复错误后重试");
        }
    }
}