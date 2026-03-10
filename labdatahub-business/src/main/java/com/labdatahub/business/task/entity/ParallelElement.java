package com.labdatahub.business.task.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// 并行分支元素
@Data
public class ParallelElement implements ChainElement {
    private List<Branch> branches = new ArrayList<>();

    @Data
    @AllArgsConstructor
    public static class Branch {
        private List<ChainElement> chain;
        private String startNodeId;
    }
}
