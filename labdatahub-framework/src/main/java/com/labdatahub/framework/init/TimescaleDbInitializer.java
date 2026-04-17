package com.labdatahub.framework.init;

import java.sql.Connection;
import java.sql.Statement;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import com.labdatahub.framework.config.properties.TimescaleDbProperties;

import lombok.RequiredArgsConstructor;
import lombok.var;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimescaleDbInitializer {

    private final TimescaleDbProperties timescaleDbProperties;
    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initTimescaleDb() {
        if (!Boolean.TRUE.equals(timescaleDbProperties.getEnabled())) {
            log.info("TimescaleDB 自动配置已关闭");
            return;
        }

        log.info("===== 开始自动初始化 TimescaleDB =====");
        try {
            createHypertables();
            createRetentionPolicies();
            createContinuousAggregates();
            log.info("===== TimescaleDB 初始化完成 =====");
        } catch (Exception e) {
            log.error("TimescaleDB 初始化失败", e);
        }
    }

    /**
     * 修复：chunk_time_interval 强制转换为 interval 类型
     */
    private void createHypertables() {
        var hypertables = timescaleDbProperties.getHypertables();
        if (hypertables == null || hypertables.isEmpty()) return;

        Connection conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
        for (var table : hypertables) {
            try (Statement stmt = conn.createStatement()) {
                // 🔥 核心修复：添加 ::interval 强制类型转换
                String createHypertableSql = String.format(
                        "SELECT create_hypertable(" +
                                "'%s', " +
                                "'%s', " +
                                "partitioning_column => '%s', " +
                                "number_partitions => %s, " +
                                "chunk_time_interval => '%s'::interval, " +
                                "if_not_exists => TRUE,migrate_data => TRUE)",
                        table.getTableName(),
                        table.getTimeColumn(),
                        table.getPartitioningColumn(),
                        table.getNumberPartitions(),
                        table.getChunkTimeInterval()
                );
                stmt.execute(createHypertableSql);
                log.info("超表创建/已存在：{}", table.getTableName());

                // 开启压缩
                if (Boolean.TRUE.equals(table.getCompressionEnabled())) {
                    String compressSql = String.format(
                            "ALTER TABLE %s SET (timescaledb.compress = true, timescaledb.compress_segmentby = '%s')",
                            table.getTableName(),
                            table.getPartitioningColumn()
                    );
                    stmt.execute(compressSql);

                    // 压缩策略
                    String addCompressPolicySql = String.format(
                            "SELECT add_compression_policy('%s', '%s'::interval, if_not_exists => TRUE)",
                            table.getTableName(),
                            table.getCompressionInterval()
                    );
                    stmt.execute(addCompressPolicySql);
                    log.info("压缩策略已配置：{}", table.getTableName());
                }
            } catch (Exception e) {
                log.warn("超表 {} 初始化失败：{}", table.getTableName(), e.getMessage());
            }
        }
        DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
    }

    /**
     * 数据保留策略
     */
    private void createRetentionPolicies() {
        var policies = timescaleDbProperties.getRetentionPolicies();
        if (policies == null || policies.isEmpty()) return;

        Connection conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
        for (var policy : policies) {
            try (Statement stmt = conn.createStatement()) {
                // 强制类型转换
                String sql = String.format(
                        "SELECT add_retention_policy('%s', '%s'::interval, if_not_exists => TRUE)",
                        policy.getTableName(),
                        policy.getRetentionInterval()
                );
                stmt.execute(sql);
                log.info("保留策略已配置：{} -> {}", policy.getTableName(), policy.getRetentionInterval());
            } catch (Exception e) {
                log.warn("保留策略配置失败：{}", policy.getTableName(), e.getMessage());
            }
        }
        DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
    }

    /**
     * 连续聚合视图
     */
    private void createContinuousAggregates() {
        var aggregates = timescaleDbProperties.getContinuousAggregates();
        if (aggregates == null || aggregates.isEmpty()) return;

        Connection conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
        for (var agg : aggregates) {
            try (Statement stmt = conn.createStatement()) {
                String createViewSql = String.format(
                        "CREATE MATERIALIZED VIEW IF NOT EXISTS %s " +
                        "WITH (timescaledb.continuous) AS " +
                        "SELECT time_bucket('%s'::interval, %s) AS bucket, %s, COUNT(*) AS log_count,MAX(create_time) as last_log_time " +
                        "FROM %s " +
                        "GROUP BY time_bucket, %s " +
                        "WITH NO DATA",
                        agg.getViewName(),
                        agg.getBucketInterval(),
                        agg.getTimeColumn(),
                        agg.getPartitioningColumn(),
                        agg.getSourceTable(),
                        agg.getPartitioningColumn()
                );
                stmt.execute(createViewSql);
                
                String addRefreshPolicySql = String.format(
                        "SELECT add_continuous_aggregate_policy(" +
                                "'%s', " +
                                "start_offset => '%s'::interval, " +
                                "end_offset => '%s'::interval, " +
                                "schedule_interval => '%s'::interval, " +
                                "if_not_exists => TRUE)",
                        agg.getViewName(),
                        agg.getRefreshStartOffset(),
                        agg.getRefreshEndOffset(),
                        agg.getRefreshInterval()
                );
                stmt.execute(addRefreshPolicySql);
                log.info("连续聚合视图+刷新策略已创建：{}", agg.getViewName());
            } catch (Exception e) {
                log.warn("连续聚合视图创建失败：{}", agg.getViewName(), e.getMessage());
            }
        }
        DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
    }
}