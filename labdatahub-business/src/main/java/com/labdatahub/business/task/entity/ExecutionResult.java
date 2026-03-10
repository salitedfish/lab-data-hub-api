package com.labdatahub.business.task.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 执行结果实体
@Data
public class ExecutionResult {
    /**
     * 任务组ID
     */
    private String groupId;

    /**
     * 定时节点名称
     */
    private String timerNodeName;

    /**
     * 执行是否成功
     */
    private boolean success;

    /**
     * 执行消息
     */
    private String message;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行时长（毫秒）
     */
    private long duration;

    /**
     * 节点执行结果列表
     */
    private List<NodeExecutionResult> results = new ArrayList<>();

    /**
     * 错误信息
     */
    private String error;
}
