package com.labdatahub.business.engine.utils;

import com.alibaba.fastjson2.JSONObject;
import com.labdatahub.business.engine.entity.RuleEngineConfig;
import com.labdatahub.business.engine.entity.RuleLine;
import com.labdatahub.business.engine.entity.RuleNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 规则分组提取器
 * @Author: ruoyi
 * @CreateTime: 2025-10-23
 */
public class RuleGroupExtractor {

    /**
     * 提取符合条件的规则分组
     * @param config 规则引擎配置
     * @return 符合条件的规则分组列表
     */
    public static List<List<RuleNode>> extractValidGroups(RuleEngineConfig config) {
        List<List<RuleNode>> result = new ArrayList<>();

        if (config == null || config.getNodeList() == null || config.getLineList() == null) {
            return result;
        }

        // 构建图的邻接表
        Map<String, List<String>> graph = buildGraph(config.getLineList());

        // 找到所有起点（没有入边的节点）
        List<String> startNodes = findStartNodes(config.getNodeList(), config.getLineList());

        // 找到所有终点（没有出边的节点）
        List<String> endNodes = findEndNodes(config.getNodeList(), config.getLineList());

        // 为每个起点到终点的路径进行DFS遍历
        for (String start : startNodes) {
            for (String end : endNodes) {
                if (!start.equals(end)) {
                    List<List<String>> allPaths = findAllPaths(graph, start, end, new HashSet<>(), new ArrayList<>());

                    for (List<String> path : allPaths) {
                        // 检查路径长度是否大于等于3
                        if (path.size() >= 3) {
                            List<RuleNode> rulePath = convertToRuleNodes(path, config.getNodeList());
                            result.add(rulePath);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * 构建图的邻接表
     */
    private static Map<String, List<String>> buildGraph(List<RuleLine> lineList) {
        Map<String, List<String>> graph = new HashMap<>();

        for (RuleLine line : lineList) {
            graph.computeIfAbsent(line.getFrom(), k -> new ArrayList<>())
                    .add(line.getTo());
        }

        return graph;
    }

    /**
     * 找到所有起点（没有入边的节点）
     */
    private static List<String> findStartNodes(List<RuleNode> nodeList, List<RuleLine> lineList) {
        Set<String> hasIncoming = lineList.stream()
                .map(RuleLine::getTo)
                .collect(Collectors.toSet());

        return nodeList.stream()
                .map(RuleNode::getId)
                .filter(id -> !hasIncoming.contains(id))
                .collect(Collectors.toList());
    }

    /**
     * 找到所有终点（没有出边的节点）
     */
    private static List<String> findEndNodes(List<RuleNode> nodeList, List<RuleLine> lineList) {
        Set<String> hasOutgoing = lineList.stream()
                .map(RuleLine::getFrom)
                .collect(Collectors.toSet());

        return nodeList.stream()
                .map(RuleNode::getId)
                .filter(id -> !hasOutgoing.contains(id))
                .collect(Collectors.toList());
    }

    /**
     * 使用DFS找到所有从起点到终点的路径
     */
    private static List<List<String>> findAllPaths(Map<String, List<String>> graph,
                                                   String current,
                                                   String end,
                                                   Set<String> visited,
                                                   List<String> currentPath) {
        List<List<String>> paths = new ArrayList<>();

        // 如果当前节点已经访问过，返回空列表（避免循环）
        if (visited.contains(current)) {
            return paths;
        }

        // 添加当前节点到路径和已访问集合
        currentPath.add(current);
        visited.add(current);

        // 如果到达终点，保存当前路径
        if (current.equals(end)) {
            paths.add(new ArrayList<>(currentPath));
        } else {
            // 继续遍历邻居节点
            List<String> neighbors = graph.get(current);
            if (neighbors != null) {
                for (String neighbor : neighbors) {
                    paths.addAll(findAllPaths(graph, neighbor, end, new HashSet<>(visited), new ArrayList<>(currentPath)));
                }
            }
        }

        return paths;
    }

    /**
     * 将节点ID列表转换为RuleNode列表
     */
    private static List<RuleNode> convertToRuleNodes(List<String> nodeIds, List<RuleNode> allNodes) {
        Map<String, RuleNode> nodeMap = allNodes.stream()
                .collect(Collectors.toMap(RuleNode::getId, node -> node));

        return nodeIds.stream()
                .map(nodeMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 打印分组结果
     */
    public static void printGroups(List<List<RuleNode>> groups) {
        if (groups.isEmpty()) {
            System.out.println("没有找到符合条件的规则分组");
            return;
        }

        System.out.println("找到 " + groups.size() + " 个符合条件的规则分组：");
        for (int i = 0; i < groups.size(); i++) {
            System.out.println("\n分组 " + (i + 1) + ":");
            List<RuleNode> group = groups.get(i);
            for (int j = 0; j < group.size(); j++) {
                RuleNode node = group.get(j);
                System.out.println("  " + (j + 1) + ". " + node.getName() + " (" + node.getType() + ")");
            }
        }
    }

    // 测试方法
    public static void main(String[] args) {
        // 创建测试数据（使用你提供的JSON数据）
        RuleEngineConfig config = createTestData();

        // 提取分组
        List<List<RuleNode>> groups = extractValidGroups(config);

        // 打印结果
        printGroups(groups);
    }

    /**
     * 创建测试数据（根据你提供的JSON）
     */
    private static RuleEngineConfig createTestData() {
        String data = "{\n" +
                "    \"nodeList\": [\n" +
                "        {\n" +
                "            \"id\": \"i5ddje5o0g\",\n" +
                "            \"name\": \"设备日志\",\n" +
                "            \"type\": \"deviceLog\",\n" +
                "            \"left\": \"70px\",\n" +
                "            \"top\": \"100.006px\",\n" +
                "            \"ico\": \"el-icon-time\",\n" +
                "            \"state\": \"success\",\n" +
                "            \"configData\": {\n" +
                "                \"productScope\": [\n" +
                "                    \"HTTP_PRODUCT\"\n" +
                "                ],\n" +
                "                \"productList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1974341084835921922\",\n" +
                "                        \"productSn\": \"IOT-0016\",\n" +
                "                        \"productName\": \"物联设备\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"protocolId\": null,\n" +
                "                        \"protocolName\": null,\n" +
                "                        \"deviceCount\": 2,\n" +
                "                        \"deviceType\": null,\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": null,\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 0,\n" +
                "                        \"regularCleaning\": \"0\",\n" +
                "                        \"retentionTime\": 2,\n" +
                "                        \"retentionUnit\": null\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1979459276435140609\",\n" +
                "                        \"productSn\": \"HTTP_PRODUCT\",\n" +
                "                        \"productName\": \"HTTP产品\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"protocolId\": null,\n" +
                "                        \"protocolName\": null,\n" +
                "                        \"deviceCount\": 1,\n" +
                "                        \"deviceType\": \"2\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": null,\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 20,\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1979906304262180866\",\n" +
                "                        \"productSn\": \"dfsadf\",\n" +
                "                        \"productName\": \"2312\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"protocolId\": null,\n" +
                "                        \"protocolName\": null,\n" +
                "                        \"deviceCount\": 0,\n" +
                "                        \"deviceType\": \"2\",\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": null,\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 60,\n" +
                "                        \"regularCleaning\": \"0\",\n" +
                "                        \"retentionTime\": 2,\n" +
                "                        \"retentionUnit\": null\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": \"1\",\n" +
                "                        \"productSn\": \"IOT-0015\",\n" +
                "                        \"productName\": \"检测仪\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"protocolId\": null,\n" +
                "                        \"protocolName\": null,\n" +
                "                        \"deviceCount\": 15,\n" +
                "                        \"deviceType\": null,\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": null,\n" +
                "                        \"createTime\": \"2025-09-23 14:17:20\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 0,\n" +
                "                        \"regularCleaning\": \"0\",\n" +
                "                        \"retentionTime\": 2,\n" +
                "                        \"retentionUnit\": null\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"deviceScope\": \"部分设备\",\n" +
                "                \"deviceList\": [\n" +
                "                    {\n" +
                "                        \"id\": \"1979459749720403969\",\n" +
                "                        \"deviceSn\": \"HTTP_DEVICE\",\n" +
                "                        \"deviceName\": \"HTTP设备\",\n" +
                "                        \"productId\": \"1979459276435140609\",\n" +
                "                        \"productName\": \"HTTP产品\",\n" +
                "                        \"productSn\": \"HTTP_PRODUCT\",\n" +
                "                        \"linkMethodId\": null,\n" +
                "                        \"linkMethodName\": null,\n" +
                "                        \"protocolId\": null,\n" +
                "                        \"protocolName\": null,\n" +
                "                        \"status\": \"0\",\n" +
                "                        \"createBy\": \"admin\",\n" +
                "                        \"createTime\": \"2025-10-18 16:09:16\",\n" +
                "                        \"updateBy\": null,\n" +
                "                        \"updateTime\": null,\n" +
                "                        \"remark\": null,\n" +
                "                        \"timeoutSeconds\": 5,\n" +
                "                        \"deviceType\": \"2\",\n" +
                "                        \"regularCleaning\": \"1\",\n" +
                "                        \"retentionTime\": 1,\n" +
                "                        \"retentionUnit\": \"day\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"deviceSnList\": [\n" +
                "                    \"HTTP_DEVICE\"\n" +
                "                ]\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"jab0wytb8a\",\n" +
                "            \"name\": \"实时推送\",\n" +
                "            \"type\": \"realTimePush\",\n" +
                "            \"left\": \"81px\",\n" +
                "            \"top\": \"194px\",\n" +
                "            \"ico\": \"el-icon-caret-right\",\n" +
                "            \"state\": \"success\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"hx7q1gsknd\",\n" +
                "            \"name\": \"设备告警\",\n" +
                "            \"type\": \"deviceWarn\",\n" +
                "            \"left\": \"19px\",\n" +
                "            \"top\": \"414px\",\n" +
                "            \"ico\": \"el-icon-odometer\",\n" +
                "            \"state\": \"success\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"mqau3fs8ls\",\n" +
                "            \"name\": \"HTTP接口\",\n" +
                "            \"type\": \"HTTP\",\n" +
                "            \"left\": \"81px\",\n" +
                "            \"top\": \"318.006px\",\n" +
                "            \"ico\": \"el-icon-caret-right\",\n" +
                "            \"state\": \"success\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"p4leay0ngh\",\n" +
                "            \"name\": \"RabbitMQ\",\n" +
                "            \"type\": \"RabbitMQ\",\n" +
                "            \"left\": \"0px\",\n" +
                "            \"top\": \"260px\",\n" +
                "            \"ico\": \"el-icon-shopping-cart-full\",\n" +
                "            \"state\": \"success\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"lineList\": [\n" +
                "        {\n" +
                "            \"from\": \"i5ddje5o0g\",\n" +
                "            \"to\": \"jab0wytb8a\",\n" +
                "            \"label\": \"23123\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"from\": \"jab0wytb8a\",\n" +
                "            \"to\": \"mqau3fs8ls\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"from\": \"jab0wytb8a\",\n" +
                "            \"to\": \"p4leay0ngh\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"from\": \"hx7q1gsknd\",\n" +
                "            \"to\": \"jab0wytb8a\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        RuleEngineConfig config = JSONObject.parseObject(data,RuleEngineConfig.class);
        return config;
    }
}