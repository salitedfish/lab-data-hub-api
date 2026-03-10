package com.labdatahub.business.warn.link;

import com.labdatahub.business.down.DeviceDownUtils;
import com.labdatahub.common.utils.spring.SpringUtils;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CommandTreeExecutor {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 执行指令树
     */
    public static void executeCommandTree(List<CommandNode> commandTree) {
        for (CommandNode node : commandTree) {
            executeNode(node);
        }
    }

    /**
     * 递归执行节点
     */
    private static void executeNode(CommandNode node) {
        if (node == null) {
            return;
        }

        // 执行当前节点
        executeSingleCommand(node);

        // 执行子节点
        List<CommandNode> children = node.getChildren();
        if (!children.isEmpty()) {
            if (children.size() == 1) {
                // 单个子节点：顺序执行
                executeNode(children.get(0));
            } else {
                // 多个子节点：并行执行
                executeNodesParallel(children);
            }
        }
    }

    /**
     * 并行执行多个节点
     */
    private static void executeNodesParallel(List<CommandNode> nodes) {
        List<CompletableFuture<Void>> futures = nodes.stream()
                .map(node -> CompletableFuture.runAsync(() -> executeNode(node), SpringUtils.getBean(ThreadPoolTaskExecutor.class)))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * 执行单个指令
     */
    private static void executeSingleCommand(CommandNode command) {
        try {
            DeviceDownUtils.functionDown(command.getDeviceSn(),command.getFunctionCode(),command.getFunctionParams(),"1");
        } catch (MqttException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭执行器
     */
    public static void shutdown() {
        executor.shutdown();
    }

}

@Data
class FlowData {
    private List<Node> nodeList;
    private List<Line> lineList;
}

@Data
class Node {
    private String id;
    private String name;
    private String type;
    private ConfigData configData;
}

@Data
class ConfigData {
    private List<DeviceInfo> deviceList;
    private String deviceSn;
    private String functionCode;
    private String functionParams;
}

@Data
class DeviceInfo {
    private String deviceSn;
    private String deviceName;
}

@Data
class Line {
    private String from;
    private String to;
}