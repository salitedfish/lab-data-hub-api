package com.labdatahub.business.task.entity;

import lombok.Data;

import java.util.List;

// 任务组实体
@Data
public class TaskGroup {
    private FlowNode timerNode;
    private String cronExpression;
    private List<ChainElement> taskChain;
    private String groupId;
}