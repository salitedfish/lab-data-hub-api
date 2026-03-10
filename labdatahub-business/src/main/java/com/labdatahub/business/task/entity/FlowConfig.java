package com.labdatahub.business.task.entity;

import lombok.Data;

import java.util.List;

// 流程配置实体
@Data
public class FlowConfig {
    private List<FlowNode> nodeList;
    private List<FlowLine> lineList;
}
