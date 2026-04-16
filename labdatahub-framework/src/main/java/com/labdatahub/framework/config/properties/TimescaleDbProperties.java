package com.labdatahub.framework.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TimescaleDB 配置属性映射类
 * 对应 YAML 中 timescaledb: 开头的配置块
 */
@Data
@Component
@ConfigurationProperties(prefix = "timescaledb")
public class TimescaleDbProperties {

    /**
     * 是否启用 TimescaleDB 自动化配置
     */
    private Boolean enabled;

    /**
     * Hypertable (超表) 配置列表
     */
    private List<Hypertable> hypertables;

    /**
     * 连续聚合视图配置列表
     */
    private List<ContinuousAggregate> continuousAggregates;

    /**
     * 数据保留策略配置列表
     */
    private List<RetentionPolicy> retentionPolicies;

    /**
     * 内部类：Hypertable 配置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Hypertable {
        /**
         * 表名
         */
        private String tableName;

        /**
         * 时间列名
         */
        private String timeColumn;

        /**
         * 分区列名 (空间分区)
         */
        private String partitioningColumn;

        /**
         * 分区数量
         */
        private Integer numberPartitions;

        /**
         * Chunk 时间间隔 (自然语言格式，如 "1 day")
         */
        private String chunkTimeInterval;

        /**
         * 是否启用压缩
         */
        private Boolean compressionEnabled;

        /**
         * 压缩间隔 (自然语言格式，如 "7 days")
         */
        private String compressionInterval;
    }

    /**
     * 内部类：连续聚合视图配置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContinuousAggregate {
        /**
         * 视图名称
         */
        private String viewName;

        /**
         * 源数据表名
         */
        private String sourceTable;

        /**
         * 时间列名
         */
        private String timeColumn;

        /**
         * 聚合时间窗口 (如 "1 hour", "1 day")
         */
        private String bucketInterval;

        /**
         * 物化视图表名 (mat-table)
         */
        private String matTable;
        
        /**
         * 列名 (如device_sn)
         */
        private String partitioningColumn;
        
        // 👇 新增刷新策略字段
        private String refreshStartOffset;
        private String refreshEndOffset;
        private String refreshInterval;
    }

    /**
     * 内部类：数据保留策略配置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetentionPolicy {
        /**
         * 表名
         */
        private String tableName;

        /**
         * 保留时长 (简写格式，如 "90d", "365d")
         */
        private String retentionInterval;
    }
}
