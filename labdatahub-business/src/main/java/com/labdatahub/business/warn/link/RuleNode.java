package com.labdatahub.business.warn.link;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 规则节点基类
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class RuleNode {
    // 节点ID
    private String id;
    // 节点名称
    private String name;
    // 节点类型
    private NodeType type;
    //属性类型 deviceProperty-属性 currentStatus-当前状态 changeStatus-状态变化
    private String attributeType;
}
