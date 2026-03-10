package com.labdatahub.business.task.entity;

import lombok.Data;

// 流程节点实体
@Data
public class FlowNode {
    private String id;
    private String name;
    private String type; // super, common, function
    private String left;
    private String top;
    private String ico;
    private String state;
    private ConfigData configData;
}
