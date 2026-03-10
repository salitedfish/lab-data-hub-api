package com.labdatahub.business.task.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

// 节点元素
@Data
@AllArgsConstructor
public class NodeElement implements ChainElement {
    private FlowNode node;
    private int depth;
}