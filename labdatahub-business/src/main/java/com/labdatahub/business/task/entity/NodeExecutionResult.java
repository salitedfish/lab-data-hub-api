package com.labdatahub.business.task.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

// 节点执行结果实体
@Data
public class NodeExecutionResult {
    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点类型
     */
    private String nodeType;

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
     * 指令参数（针对function类型）
     */
    private Map<String, Object> commandParams;

    /**
     * 执行结果数据
     */
    private Object resultData;
}
