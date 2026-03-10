package com.labdatahub.business.warn.link;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// 指令节点（包含父子关系）
@Data
public class CommandNode {
    private String id;
    private String name;
    private String deviceSn;
    private String deviceName;
    private String functionCode;
    private String functionParams;
    private List<CommandNode> children = new ArrayList<>();
}
