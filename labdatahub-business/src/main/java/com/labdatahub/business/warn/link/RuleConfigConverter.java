package com.labdatahub.business.warn.link;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.labdatahub.business.domain.LabdatahubWarnLinkage;

import java.util.*;

/**
 * @Description: 规则配置转换器 - 将节点配置转换为规则数据结构
 * @Author: ruoyi
 * @CreateTime: 2025-11-13
 */
public class RuleConfigConverter {

    /**
     * 将节点配置转换为规则数据结构
     */
    public static WarnLinkRule convertToRule(LabdatahubWarnLinkage warnLinkage, String name) {
        JSONObject config = JSON.parseObject(warnLinkage.getRuleJson());
        JSONArray nodeList = config.getJSONArray("nodeList");
        JSONArray lineList = config.getJSONArray("lineList");

        // 构建节点映射
        Map<String, JSONObject> nodeMap = new HashMap<>();
        for (int i = 0; i < nodeList.size(); i++) {
            JSONObject node = nodeList.getJSONObject(i);
            nodeMap.put(node.getString("id"), node);
        }

        // 构建连接关系 - 入边和出边映射
        Map<String, List<String>> inConnections = buildInConnections(lineList);
        Map<String, List<String>> outConnections = buildOutConnections(lineList);

        System.out.println("入边连接关系: " + inConnections);
        System.out.println("出边连接关系: " + outConnections);

        // 找到根节点（连接到动作节点的逻辑节点）
        String rootNodeId = findRootNode(nodeMap, outConnections);
        if (rootNodeId == null) {
            throw new IllegalArgumentException("未找到有效的根节点。请检查节点连接关系。");
        }

        System.out.println("找到根节点: " + rootNodeId + ", 类型: " + nodeMap.get(rootNodeId).getString("type"));

        // 递归构建规则树
        RuleNode rootNode = buildRuleTree(rootNodeId, nodeMap, inConnections, outConnections);

        if (!(rootNode instanceof LogicNode)) {
            throw new IllegalArgumentException("根节点必须是逻辑节点");
        }

        // 创建规则
        WarnLinkRule rule = new WarnLinkRule();
        rule.setId(UUID.randomUUID().toString());
        rule.setName(name);
        rule.setRootNode(rootNode);
        rule.setEnable(true);
        rule.setDelayTime(300); // 5分钟不重复告警
        rule.setLevel(warnLinkage.getWarnLevel());
        rule.setMessage(warnLinkage.getWarnMessage());
        return rule;
    }
    /**
     * 获取规则中的所有条件节点
     */
    public static List<ConditionNode> getAllConditionNodes(WarnLinkRule rule) {
        List<ConditionNode> conditionNodes = new ArrayList<>();
        if (rule != null && rule.getRootNode() != null) {
            collectConditionNodes(rule.getRootNode(), conditionNodes);
        }
        return conditionNodes;
    }

    /**
     * 递归收集条件节点
     */
    private static void collectConditionNodes(RuleNode node, List<ConditionNode> conditionNodes) {
        if (node instanceof ConditionNode) {
            conditionNodes.add((ConditionNode) node);
        } else if (node instanceof LogicNode) {
            LogicNode logicNode = (LogicNode) node;
            if (logicNode.getChildren() != null) {
                for (RuleNode child : logicNode.getChildren()) {
                    collectConditionNodes(child, conditionNodes);
                }
            }
        }
    }
    /**
     * 获取规则中的所有逻辑节点
     */
    public static List<LogicNode> getAllLogicNodes(WarnLinkRule rule) {
        List<LogicNode> logicNodes = new ArrayList<>();
        if (rule != null && rule.getRootNode() != null) {
            collectLogicNodes(rule.getRootNode(), logicNodes);
        }
        return logicNodes;
    }

    /**
     * 递归收集逻辑节点
     */
    private static void collectLogicNodes(RuleNode node, List<LogicNode> logicNodes) {
        if (node instanceof LogicNode) {
            logicNodes.add((LogicNode) node);

            // 递归处理子节点
            LogicNode logicNode = (LogicNode) node;
            if (logicNode.getChildren() != null) {
                for (RuleNode child : logicNode.getChildren()) {
                    collectLogicNodes(child, logicNodes);
                }
            }
        }
    }


    /**
     * 构建入边连接关系映射 (to -> from)
     */
    private static Map<String, List<String>> buildInConnections(JSONArray lineList) {
        Map<String, List<String>> inConnections = new HashMap<>();

        for (int i = 0; i < lineList.size(); i++) {
            JSONObject line = lineList.getJSONObject(i);
            String from = line.getString("from");
            String to = line.getString("to");

            inConnections.computeIfAbsent(to, k -> new ArrayList<>()).add(from);
        }

        return inConnections;
    }

    /**
     * 构建出边连接关系映射 (from -> to)
     */
    private static Map<String, List<String>> buildOutConnections(JSONArray lineList) {
        Map<String, List<String>> outConnections = new HashMap<>();

        for (int i = 0; i < lineList.size(); i++) {
            JSONObject line = lineList.getJSONObject(i);
            String from = line.getString("from");
            String to = line.getString("to");

            outConnections.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        }

        return outConnections;
    }

    /**
     * 找到根节点（连接到动作节点的逻辑节点）
     */
    private static String findRootNode(Map<String, JSONObject> nodeMap,
                                       Map<String, List<String>> outConnections) {
        // 找到所有动作节点（warn和function）
        Set<String> actionNodes = new HashSet<>();
        for (String nodeId : nodeMap.keySet()) {
            JSONObject node = nodeMap.get(nodeId);
            String type = node.getString("type");
            if ("warn".equals(type) || "function".equals(type)) {
                actionNodes.add(nodeId);
            }
        }

        System.out.println("找到动作节点: " + actionNodes);

        // 找到连接到动作节点的逻辑节点
        for (Map.Entry<String, List<String>> entry : outConnections.entrySet()) {
            String fromNodeId = entry.getKey();
            List<String> toNodeIds = entry.getValue();

            // 检查这个节点是否连接到任何动作节点
            for (String toNodeId : toNodeIds) {
                if (actionNodes.contains(toNodeId)) {
                    JSONObject fromNode = nodeMap.get(fromNodeId);
                    if (fromNode != null) {
                        String fromNodeType = fromNode.getString("type");
                        if ("and".equals(fromNodeType) || "or".equals(fromNodeType)) {
                            System.out.println("找到连接到动作节点的逻辑节点: " + fromNodeId + " -> " + toNodeId);
                            return fromNodeId;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * 递归构建规则树
     */
    private static RuleNode buildRuleTree(String nodeId,
                                          Map<String, JSONObject> nodeMap,
                                          Map<String, List<String>> inConnections,
                                          Map<String, List<String>> outConnections) {
        JSONObject nodeConfig = nodeMap.get(nodeId);
        if (nodeConfig == null) {
            throw new IllegalArgumentException("节点不存在: " + nodeId);
        }

        String type = nodeConfig.getString("type");
        String name = nodeConfig.getString("name");

        System.out.println("构建节点: " + nodeId + ", 类型: " + type + ", 名称: " + name);

        switch (type) {
            case "and":
            case "or":
                return buildLogicNode(nodeId, nodeConfig, nodeMap, inConnections, outConnections);

            case "deviceProperty":
            case "currentStatus":
            case "changeStatus":
                return buildConditionNode(nodeConfig);

            default:
                throw new IllegalArgumentException("不支持的节点类型: " + type);
        }
    }

    /**
     * 构建逻辑节点
     */
    private static LogicNode buildLogicNode(String nodeId,
                                            JSONObject nodeConfig,
                                            Map<String, JSONObject> nodeMap,
                                            Map<String, List<String>> inConnections,
                                            Map<String, List<String>> outConnections) {
        LogicNode logicNode = new LogicNode();
        logicNode.setId(nodeId);
        logicNode.setName(nodeConfig.getString("name"));
        logicNode.setRelation(nodeConfig.getString("type")); // "and" or "or"

        // 获取子节点（连接到这个逻辑节点的输入节点）
        List<String> inputNodeIds = inConnections.get(nodeId);
        if (inputNodeIds != null && !inputNodeIds.isEmpty()) {
            List<RuleNode> children = new ArrayList<>();
            for (String inputNodeId : inputNodeIds) {
                JSONObject inputNode = nodeMap.get(inputNodeId);
                if (inputNode != null) {
                    String inputType = inputNode.getString("type");
                    // 只处理条件节点和逻辑节点
                    if ("deviceProperty".equals(inputType) || "currentStatus".equals(inputType) ||
                            "changeStatus".equals(inputType) || "and".equals(inputType) || "or".equals(inputType)) {
                        RuleNode childRuleNode = buildRuleTree(inputNodeId, nodeMap, inConnections, outConnections);
                        if (childRuleNode != null) {
                            children.add(childRuleNode);
                        }
                    }
                }
            }
            logicNode.setChildren(children);
            System.out.println("逻辑节点 " + nodeId + " 有 " + children.size() + " 个子节点: " +
                    children.stream().map(RuleNode::getName).reduce((a, b) -> a + ", " + b).orElse(""));
        } else {
            System.out.println("逻辑节点 " + nodeId + " 没有输入节点");
        }

        return logicNode;
    }

    /**
     * 构建条件节点
     */
    private static ConditionNode buildConditionNode(JSONObject nodeConfig) {
        ConditionNode conditionNode = new ConditionNode();
        conditionNode.setId(nodeConfig.getString("id"));
        conditionNode.setName(nodeConfig.getString("name"));

        JSONObject configData = nodeConfig.getJSONObject("configData");
        if (configData != null) {
            String deviceSn = configData.getString("deviceSn");
            conditionNode.setDeviceSn(deviceSn);

            // 设置设备名称
            String deviceName = getDeviceName(configData, deviceSn);
            conditionNode.setDeviceName(deviceName);

            String type = nodeConfig.getString("type");
            switch (type) {
                case "deviceProperty":
                    // 设备属性条件
                    String attribute = configData.getString("attribute");
                    conditionNode.setAttribute(attribute);
                    conditionNode.setOperator(configData.getString("operator"));
                    conditionNode.setValue(configData.getString("value"));
                    conditionNode.setAttributeType("deviceProperty");
                    // 设置属性名称
                    String attributeName = getAttributeName(configData, attribute);
                    conditionNode.setAttributeName(attributeName);
                    break;

                case "currentStatus":
                    // 设备当前状态条件
                    conditionNode.setAttribute("currentStatus");
                    conditionNode.setOperator("eq");
                    conditionNode.setValue(configData.getString("currentStatus"));
                    conditionNode.setAttributeName("设备状态");
                    conditionNode.setAttributeType("currentStatus");
                    break;

                case "changeStatus":
                    // 设备状态变化条件
                    conditionNode.setAttribute("changeStatus");
                    conditionNode.setOperator("eq");
                    conditionNode.setValue(configData.getString("changeStatus"));
                    conditionNode.setAttributeName("设备状态变化");
                    conditionNode.setAttributeType("changeStatus");
                    break;
            }

            System.out.println("构建条件节点: " + conditionNode.getName() +
                    ", 设备: " + conditionNode.getDeviceName() +
                    ", 属性: " + conditionNode.getAttributeName() +
                    ", 操作: " + conditionNode.getOperator() +
                    ", 值: " + conditionNode.getValue());
        }

        return conditionNode;
    }

    /**
     * 获取设备名称
     */
    private static String getDeviceName(JSONObject configData, String deviceSn) {
        JSONArray deviceList = configData.getJSONArray("deviceList");
        if (deviceList != null) {
            for (int i = 0; i < deviceList.size(); i++) {
                JSONObject device = deviceList.getJSONObject(i);
                if (deviceSn.equals(device.getString("deviceSn"))) {
                    return device.getString("deviceName");
                }
            }
        }
        return deviceSn; // 如果找不到，返回设备SN
    }

    /**
     * 获取属性名称
     */
    private static String getAttributeName(JSONObject configData, String attribute) {
        JSONArray propertyList = configData.getJSONArray("propertyList");
        if (propertyList != null) {
            for (int i = 0; i < propertyList.size(); i++) {
                JSONObject property = propertyList.getJSONObject(i);
                if (attribute.equals(property.getString("identifier"))) {
                    return property.getString("name");
                }
            }
        }
        return attribute; // 如果找不到，返回属性标识
    }


    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 这里放入您的JSON配置数据
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
                "            \"from\": \"cvq926zif\",\n" +
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

        try {
            // 转换配置为规则
            WarnLinkRule rule = convertToRule(null,"test");
            List<ConditionNode> list = getAllConditionNodes(rule);


            // 生成白话文描述
            String description = TranslateRule.generateRuleDescription(rule.getRootNode());
            System.out.println(description);

            // 准备测试数据
            Map<String, Map<String, Object>> deviceData = new HashMap<>();
            Map<String, Object> wsDeviceData = new HashMap<>();
            wsDeviceData.put("voice", 10); // 音量大于5
            wsDeviceData.put("humidity", 50); // 湿度等于50
            wsDeviceData.put("status", "1"); // 设备在线
            deviceData.put("WS_DEVICE_001", wsDeviceData);

            // 评估规则
            boolean result = RuleEngine.evaluate(rule.getRootNode(), deviceData);
            System.out.println("\n规则触发结果: " + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}