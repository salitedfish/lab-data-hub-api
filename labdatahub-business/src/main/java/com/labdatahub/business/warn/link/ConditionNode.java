package com.labdatahub.business.warn.link;

import lombok.Data;
import lombok.EqualsAndHashCode;

// 条件节点（叶子节点）
@Data
@EqualsAndHashCode(callSuper = true)
public class ConditionNode extends RuleNode {
    // 设备SN
    private String deviceSn;
    // 设备名称
    private String deviceName;
    // 属性标识
    private String attribute;
    // 属性标识名称
    private String attributeName;
    // 比较符
    private String operator;
    // 比较值
    private String value;

    public ConditionNode() {
        super.setType(NodeType.CONDITION);
    }
}
