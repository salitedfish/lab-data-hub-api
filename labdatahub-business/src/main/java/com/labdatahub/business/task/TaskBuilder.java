package com.labdatahub.business.task;

import com.labdatahub.business.task.entity.*;
import com.labdatahub.common.exception.CommonWarnException;
import com.labdatahub.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TaskBuilder {

    /**
     * 构建邻接表
     */
    public static Map<String, List<String>> buildAdjacencyMap(List<FlowLine> lineList) {
        Map<String, List<String>> adjacencyMap = new HashMap<>();

        for (FlowLine line : lineList) {
            adjacencyMap.computeIfAbsent(line.getFrom(), k -> new ArrayList<>())
                    .add(line.getTo());
        }

        return adjacencyMap;
    }

    /**
     * 分离定时节点和指令节点
     */
    public static Map<String, List<FlowNode>> classifyNodes(List<FlowNode> nodeList) {
        Map<String, List<FlowNode>> result = new HashMap<>();
        List<FlowNode> timerNodes = new ArrayList<>();
        List<FlowNode> functionNodes = new ArrayList<>();

        for (FlowNode node : nodeList) {
            if ("super".equals(node.getType()) || "common".equals(node.getType())) {
                timerNodes.add(node);
            } else if ("function".equals(node.getType())) {
                functionNodes.add(node);
            }
        }

        result.put("timer", timerNodes);
        result.put("function", functionNodes);
        return result;
    }

    /**
     * 构建任务组
     */
    public static List<TaskGroup> buildTaskGroups(FlowConfig flowConfig) throws CommonWarnException {
        List<TaskGroup> taskGroups = new ArrayList<>();

        // 分离节点
        Map<String, List<FlowNode>> classifiedNodes = classifyNodes(flowConfig.getNodeList());
        List<FlowNode> timerNodes = classifiedNodes.get("timer");

        // 构建邻接表
        Map<String, List<String>> adjacencyMap = buildAdjacencyMap(flowConfig.getLineList());

        // 节点ID到节点的映射
        Map<String, FlowNode> nodeMap = flowConfig.getNodeList().stream()
                .collect(Collectors.toMap(FlowNode::getId, Function.identity()));

        // 为每个定时节点构建任务组
        for (FlowNode timerNode : timerNodes) {
            TaskGroup taskGroup = new TaskGroup();
            taskGroup.setTimerNode(timerNode);
            if(timerNode.getConfigData() == null || StringUtils.isEmpty(timerNode.getConfigData().getCronValue())){
                throw new CommonWarnException("Cron表达式不能为空");
            }
            taskGroup.setCronExpression(timerNode.getConfigData() != null ?
                    timerNode.getConfigData().getCronValue() : "0/30 * * * * ?");
            taskGroup.setGroupId("GROUP_" + timerNode.getId());

            // 构建执行链
            List<ChainElement> chain = buildTaskChain(
                    timerNode.getId(),
                    adjacencyMap,
                    nodeMap,
                    new HashSet<>()
            );
            taskGroup.setTaskChain(chain);

            taskGroups.add(taskGroup);
        }

        return taskGroups;
    }

    /**
     * 递归构建任务链
     */
    private static List<ChainElement> buildTaskChain(String currentNodeId,
                                                     Map<String, List<String>> adjacencyMap,
                                                     Map<String, FlowNode> nodeMap,
                                                     Set<String> visited) {
        List<ChainElement> chain = new ArrayList<>();

        // 防止循环引用
        if (visited.contains(currentNodeId)) {
            return chain;
        }
        visited.add(currentNodeId);

        // 添加当前节点
        FlowNode currentNode = nodeMap.get(currentNodeId);
        if (currentNode != null) {
            chain.add(new NodeElement(currentNode, visited.size() - 1));
        }

        // 获取后继节点
        List<String> successors = adjacencyMap.getOrDefault(currentNodeId, new ArrayList<>());

        // 如果有多个后继节点，创建并行分支
        if (successors.size() > 1) {
            ParallelElement parallelElement = new ParallelElement();

            for (String successorId : successors) {
                // 为每个分支创建新的访问集合
                Set<String> branchVisited = new HashSet<>(visited);
                List<ChainElement> branchChain = buildTaskChain(
                        successorId,
                        adjacencyMap,
                        nodeMap,
                        branchVisited
                );

                parallelElement.getBranches().add(
                        new ParallelElement.Branch(branchChain, successorId)
                );
            }

            chain.add(parallelElement);

        } else if (successors.size() == 1) {
            // 单后继节点，继续构建
            String nextNodeId = successors.get(0);
            if (!visited.contains(nextNodeId)) {
                chain.addAll(buildTaskChain(nextNodeId, adjacencyMap, nodeMap, visited));
            }
        }

        return chain;
    }
}
