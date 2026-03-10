package com.labdatahub.business.warn.link;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

// 逻辑节点（非叶子节点）
@Data
@EqualsAndHashCode(callSuper = true)
public class LogicNode extends RuleNode {
    // 逻辑关系: and-与, or-或
    private String relation;
    // 子节点
    private List<RuleNode> children;

    public LogicNode() {
        super.setType(NodeType.LOGIC);
    }
}
