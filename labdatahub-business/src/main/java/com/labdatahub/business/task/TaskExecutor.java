package com.labdatahub.business.task;

import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.down.DeviceDownUtils;
import com.labdatahub.business.task.entity.*;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TaskExecutor {
    @Autowired
    public ScheduledExecutorService executorService;
    public static Map<String,List<String>> TASK_GROUP = new HashMap<>();
    private final ConcurrentMap<String, CompletableFuture<ExecutionResult>> taskFutures = new ConcurrentHashMap<>();
    /**
     * 执行任务组
     */
    public String executeTaskGroup(String id,TaskGroup taskGroup) {
        String scheduledId = CronUtil.schedule(taskGroup.getCronExpression(), new Task() {
            @Override
            public void execute() {
                executeChain(taskGroup.getTaskChain());
            }
        });
        addGroup(id,scheduledId);
        return scheduledId;
    }

    public void stopTaskGroup(String componentId) {
        TASK_GROUP.getOrDefault(componentId,new ArrayList<>()).forEach(CronUtil::remove);
        TASK_GROUP.remove(componentId);
    }


    /**
     * 执行任务链
     */
    private List<NodeExecutionResult> executeChain(List<ChainElement> chain) {
        List<NodeExecutionResult> results = new ArrayList<>();

        for (ChainElement element : chain) {
            if (element instanceof NodeElement) {
                // 执行单个节点
                NodeExecutionResult nodeResult = executeNode((NodeElement) element);
                results.add(nodeResult);

            } else if (element instanceof ParallelElement) {
                // 并行执行分支
                ParallelElement parallelElement = (ParallelElement) element;

                List<CompletableFuture<List<NodeExecutionResult>>> branchFutures =
                        parallelElement.getBranches().stream()
                                .map(branch -> CompletableFuture.supplyAsync(
                                        () -> executeChain(branch.getChain()),
                                        executorService
                                ))
                                .collect(Collectors.toList());

                // 等待所有分支完成
                CompletableFuture.allOf(branchFutures.toArray(new CompletableFuture[0]))
                        .join();

                // 收集结果
                branchFutures.forEach(future -> {
                    try {
                        results.addAll(future.get());
                    } catch (Exception e) {
                        log.error("分支执行异常", e);
                    }
                });
            }
        }

        return results;
    }

    /**
     * 执行单个节点
     */
    private NodeExecutionResult executeNode(NodeElement nodeElement) {
        FlowNode node = nodeElement.getNode();
        NodeExecutionResult result = new NodeExecutionResult();
        result.setNodeId(node.getId());
        result.setNodeName(node.getName());
        result.setNodeType(node.getType());
        result.setStartTime(LocalDateTime.now());

        try {
            if ("function".equals(node.getType())) {
                // 执行指令下发
                result.setSuccess(executeFunction(node));
                result.setMessage("指令下发执行成功");

            } else {
                // 定时组件节点，只记录日志
                log.info("定时组件节点: {}", node.getName());
                result.setSuccess(true);
                result.setMessage("定时组件节点");
            }

        } catch (Exception e) {
            log.error("节点执行失败: {}", node.getName(), e);
            result.setSuccess(false);
            result.setMessage("执行失败: " + e.getMessage());
        }

        result.setEndTime(LocalDateTime.now());
        result.setDuration(Duration.between(result.getStartTime(), result.getEndTime()).toMillis());

        return result;
    }

    /**
     * 执行指令下发
     */
    private boolean executeFunction(FlowNode node) {
        try {
            ConfigData configData = node.getConfigData();
            if (configData == null) {
                log.warn("节点 {} 无配置数据", node.getName());
                return false;
            }

            String functionCode = configData.getFunctionCode();
            String functionParams = configData.getFunctionParams();
            String deviceSn = configData.getDeviceSn();

            // 这里调用实际的指令下发服务
            if(StringUtils.isNotEmpty(configData.getProductSn())){
                //如果选择了设备
                if(StringUtils.isNotEmpty(deviceSn)){
                    List<String> deviceSnList = Arrays.asList(deviceSn.split(","));
                    deviceSnList.forEach(sn->{
                        try {
                            DeviceDownUtils.functionDown(sn,functionCode,functionParams,"2");
                        } catch (InvocationTargetException | IllegalAccessException | MqttException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }else {
                    //如果只选择了产品
                    List<LabdatahubDevice> list = CacheUtils.getDeviceByProductSn(configData.getProductSn());
                    list.forEach(device->{
                        try {
                            DeviceDownUtils.functionDown(device.getDeviceSn(),functionCode,functionParams,"2");
                        } catch (InvocationTargetException | IllegalAccessException | MqttException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
            return true;

        } catch (Exception e) {
            log.error("指令下发失败");
            return false;
        }
    }

    /**
     * 停止执行器
     */
    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void addGroup(String id,String scheduledId){
        List<String> groupIds = TASK_GROUP.getOrDefault(id,new ArrayList<>());
        groupIds.add(scheduledId);
        TASK_GROUP.put(id,groupIds);
    }
}