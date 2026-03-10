package com.labdatahub.business.warn.link;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

public class CommandTreeParser {

    /**
     * 解析JSON，构建指令执行树
     */
    public static List<CommandNode> parseCommandTree(String json) throws Exception {
        FlowData flowData = JSONObject.parseObject(json, FlowData.class);

        // 找到最后一个逻辑节点
        Node rootNode = findLastLogicNode(flowData);
        if (rootNode == null) {
            throw new RuntimeException("未找到逻辑节点");
        }

        // 构建执行树
        return buildCommandTree(flowData, rootNode.getId());
    }

    private static Node findLastLogicNode(FlowData flowData) {
        return flowData.getNodeList().stream()
                .filter(node -> "and".equals(node.getType()) || "or".equals(node.getType()))
                .findFirst()
                .orElse(null);
    }

    private static List<CommandNode> buildCommandTree(FlowData flowData, String nodeId) {
        List<CommandNode> result = new ArrayList<>();

        // 获取当前节点的所有子节点
        List<String> childIds = getChildNodeIds(flowData, nodeId);

        for (String childId : childIds) {
            Node childNode = findNodeById(flowData, childId);
            if (childNode == null) {
                continue;
            }

            if ("function".equals(childNode.getType())) {
                // 指令节点
                CommandNode commandNode = convertToCommandNode(childNode);
                // 递归构建子节点的指令树
                commandNode.setChildren(buildCommandTree(flowData, childId));
                result.add(commandNode);
            } else {
                // 逻辑节点，继续递归
                result.addAll(buildCommandTree(flowData, childId));
            }
        }

        return result;
    }

    private static List<String> getChildNodeIds(FlowData flowData, String nodeId) {
        return flowData.getLineList().stream()
                .filter(line -> nodeId.equals(line.getFrom()))
                .map(Line::getTo)
                .collect(Collectors.toList());
    }

    private static Node findNodeById(FlowData flowData, String nodeId) {
        return flowData.getNodeList().stream()
                .filter(node -> nodeId.equals(node.getId()))
                .findFirst()
                .orElse(null);
    }

    private static CommandNode convertToCommandNode(Node node) {
        CommandNode command = new CommandNode();
        command.setId(node.getId());
        command.setName(node.getName());

        ConfigData config = node.getConfigData();
        if (config != null) {
            command.setDeviceSn(config.getDeviceSn());
            command.setFunctionCode(config.getFunctionCode());
            command.setFunctionParams(config.getFunctionParams());

            // 获取设备名称
            if (config.getDeviceList() != null) {
                command.setDeviceName(config.getDeviceList().stream()
                        .filter(device -> config.getDeviceSn().equals(device.getDeviceSn()))
                        .findFirst()
                        .map(DeviceInfo::getDeviceName)
                        .orElse(null));
            }
        }

        return command;
    }
}

