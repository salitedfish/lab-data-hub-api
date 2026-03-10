package com.labdatahub.business.warn.link;

import com.labdatahub.business.domain.LabdatahubFunction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Description:
 * @Author: ruoyi
 * @CreateTime: 2025-11-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarnLinkRule {
    // 规则ID
    private String id;
    // 规则名称
    private String name;
    // 规则根节点
    private RuleNode rootNode;
    // 告警等级 warn/critical
    private String level;
    // 告警消息模板
    private String message;
    // 执行动作
    private List<CommandNode> actions;
    // 是否启用
    private Boolean enable;
    // 多少秒内不重复告警
    private Integer delayTime;

    // 自动提取涉及的设备列表
    public List<String> getInvolvedDeviceSns() {
        Set<String> deviceSns = new HashSet<>();
        extractDeviceSns(rootNode, deviceSns);
        return new ArrayList<>(deviceSns);
    }
    public List<String> getExecuteDeviceSns() {
        Set<String> deviceSns = new HashSet<>();
        // 遍历 actions 列表中的每个 CommandNode
        if (actions != null) {
            for (CommandNode action : actions) {
                collectDeviceSns(action, deviceSns);
            }
        }
        return new ArrayList<>(deviceSns);
    }

    private void collectDeviceSns(CommandNode node, Set<String> deviceSns) {
        if (node == null) {
            return;
        }

        // 添加当前节点的 deviceSn（如果存在）
        if (node.getDeviceSn() != null && !node.getDeviceSn().trim().isEmpty()) {
            deviceSns.add(node.getDeviceSn());
        }

        // 递归处理所有子节点
        if (node.getChildren() != null) {
            for (CommandNode child : node.getChildren()) {
                collectDeviceSns(child, deviceSns);
            }
        }
    }
    public List<String> getExecuteDeviceNames() {
        Set<String> deviceNames = new HashSet<>();
        // 遍历 actions 列表中的每个 CommandNode
        if (actions != null) {
            for (CommandNode action : actions) {
                collectDeviceNames(action, deviceNames);
            }
        }
        return new ArrayList<>(deviceNames);
    }

    private void collectDeviceNames(CommandNode node, Set<String> deviceNames) {
        if (node == null) {
            return;
        }

        // 添加当前节点的 deviceSn（如果存在）
        if (node.getDeviceName() != null && !node.getDeviceName().trim().isEmpty()) {
            deviceNames.add(node.getDeviceName());
        }

        // 递归处理所有子节点
        if (node.getChildren() != null) {
            for (CommandNode child : node.getChildren()) {
                collectDeviceSns(child, deviceNames);
            }
        }
    }

    private void extractDeviceSns(RuleNode node, Set<String> deviceSns) {
        if (node instanceof ConditionNode) {
            deviceSns.add(((ConditionNode) node).getDeviceSn());
        } else if (node instanceof LogicNode) {
            for (RuleNode child : ((LogicNode) node).getChildren()) {
                extractDeviceSns(child, deviceSns);
            }
        }
    }

    // 自动提取涉及的设备名称列表
    public List<String> getInvolvedDeviceNames() {
        Set<String> deviceSns = new HashSet<>();
        extractDeviceNames(rootNode, deviceSns);
        return new ArrayList<>(deviceSns);
    }

    private void extractDeviceNames(RuleNode node, Set<String> deviceSns) {
        if (node instanceof ConditionNode) {
            deviceSns.add(((ConditionNode) node).getDeviceName());
        } else if (node instanceof LogicNode) {
            for (RuleNode child : ((LogicNode) node).getChildren()) {
                extractDeviceNames(child, deviceSns);
            }
        }
    }
}

