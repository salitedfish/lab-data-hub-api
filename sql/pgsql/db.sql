-- TimescaleDB 扩展安装与初始化脚本
-- 执行顺序：先安装扩展，再创建 hypertable，最后配置策略

-- ==================== 1. 安装 TimescaleDB 扩展 ====================
-- 需要在数据库中先安装 TimescaleDB 插件
-- PostgreSQL 15+ 安装命令 (以 Linux 为例):
-- Ubuntu/Debian: apt-get install timescaledb-2-postgresql-15
-- CentOS/RHEL: yum install timescaledb-2-postgresql-15
-- Windows: 下载安装包 https://packagecloud.io/timescale/timescaledb/install

-- 连接到数据库后执行以下命令
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

-- 验证安装
SELECT * FROM timescaledb_information.installed_extensions;

-- ==================== 2. 创建 Hypertable ====================

-- 2.1 设备日志表转换为 hypertable
-- 该表存储设备上报的实时数据，数据量大，适合时序优化
SELECT create_hypertable(
    'labdatahub_device_logs',
    'create_time',                    -- 时间列
    'device_sn',                      -- 分区列 (可选)
    4,                                -- 分区数量
    chunk_time_interval => INTERVAL '1 day',  -- 每个 chunk 的时间间隔 (1 天=86400000 毫秒)
    if_not_exists => TRUE
);

-- 为设备日志表添加索引
CREATE INDEX IF NOT EXISTS idx_device_logs_device_sn 
ON labdatahub_device_logs (device_sn, create_time DESC);

CREATE INDEX IF NOT EXISTS idx_device_logs_log_type 
ON labdatahub_device_logs (log_type, create_time DESC);

CREATE INDEX IF NOT EXISTS idx_device_logs_report_time 
ON labdatahub_device_logs (report_time DESC);

-- 2.2 告警记录表转换为 hypertable
SELECT create_hypertable(
    'labdatahub_warn_record',
    'create_time',
    'belong_sn',
    2,
    chunk_time_interval => INTERVAL '1 day',
    if_not_exists => TRUE
);

CREATE INDEX IF NOT EXISTS idx_warn_record_belong_sn 
ON labdatahub_warn_record (belong_sn, create_time DESC);

CREATE INDEX IF NOT EXISTS idx_warn_record_config_id 
ON labdatahub_warn_record (config_id, create_time DESC);

-- 2.3 联动动作记录表转换为 hypertable
SELECT create_hypertable(
    'labdatahub_linkage_action_record',
    'create_time',
    'config_id',
    2,
    chunk_time_interval => INTERVAL '1 day',
    if_not_exists => TRUE
);

CREATE INDEX IF NOT EXISTS idx_action_record_config_id 
ON labdatahub_linkage_action_record (config_id, create_time DESC);

-- 2.4 联动告警记录表转换为 hypertable
SELECT create_hypertable(
    'labdatahub_linkage_warn_record',
    'create_time',
    'config_id',
    2,
    chunk_time_interval => INTERVAL '1 day',
    if_not_exists => TRUE
);

CREATE INDEX IF NOT EXISTS idx_linkage_warn_record_config_id 
ON labdatahub_linkage_warn_record (config_id, create_time DESC);

-- ==================== 3. 配置压缩策略 ====================

-- 3.1 启用设备日志表压缩 (7 天前的数据压缩)
ALTER TABLE labdatahub_device_logs SET (
    timescaledb.compress = true,
    timescaledb.compress_segmentby = 'device_sn'
);

SELECT add_compression_policy(
    'labdatahub_device_logs',
    INTERVAL '7 days'
);

-- 3.2 启用告警记录表压缩 (30 天前的数据压缩)
ALTER TABLE labdatahub_warn_record SET (
    timescaledb.compress = true,
    timescaledb.compress_segmentby = 'belong_sn'
);

SELECT add_compression_policy(
    'labdatahub_warn_record',
    INTERVAL '30 days'
);

-- 3.3 启用联动记录表压缩 (30 天前的数据压缩)
ALTER TABLE labdatahub_linkage_action_record SET (
    timescaledb.compress = true
);

SELECT add_compression_policy(
    'labdatahub_linkage_action_record',
    INTERVAL '30 days'
);

ALTER TABLE labdatahub_linkage_warn_record SET (
    timescaledb.compress = true
);

SELECT add_compression_policy(
    'labdatahub_linkage_warn_record',
    INTERVAL '30 days'
);

-- ==================== 4. 配置数据保留策略 ====================

-- 4.1 设备日志保留 90 天
SELECT add_retention_policy(
    'labdatahub_device_logs',
    INTERVAL '90 days'
);

-- 4.2 告警记录保留 1 年
SELECT add_retention_policy(
    'labdatahub_warn_record',
    INTERVAL '365 days'
);

-- 4.3 联动记录保留 1 年
SELECT add_retention_policy(
    'labdatahub_linkage_action_record',
    INTERVAL '365 days'
);

SELECT add_retention_policy(
    'labdatahub_linkage_warn_record',
    INTERVAL '365 days'
);

-- ==================== 5. 创建连续聚合视图 (可选) ====================

-- 5.1 设备日志小时级聚合视图
CREATE MATERIALIZED VIEW IF NOT EXISTS device_logs_hourly_mat
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 hour', create_time) AS bucket,
    device_sn,
    log_type,
    COUNT(*) as log_count,
    MAX(create_time) as last_log_time
FROM labdatahub_device_logs
GROUP BY bucket, device_sn, log_type
WITH NO DATA;

-- 5.2 设备日志天级聚合视图
CREATE MATERIALIZED VIEW IF NOT EXISTS device_logs_daily_mat
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 day', create_time) AS bucket,
    device_sn,
    log_type,
    COUNT(*) as log_count,
    MAX(create_time) as last_log_time
FROM labdatahub_device_logs
GROUP BY bucket, device_sn, log_type
WITH NO DATA;

-- 为聚合视图添加刷新策略 (每 10 分钟刷新一次)
SELECT add_continuous_aggregate_policy(
    'device_logs_hourly_mat',
    start_offset => INTERVAL '1 hour',
    end_offset => INTERVAL '1 minute',
    schedule_interval => INTERVAL '10 minutes'
);

SELECT add_continuous_aggregate_policy(
    'device_logs_daily_mat',
    start_offset => INTERVAL '1 day',
    end_offset => INTERVAL '1 hour',
    schedule_interval => INTERVAL '30 minutes'
);

-- ==================== 6. 查询优化示例 ====================

-- 6.1 查询最近 1 小时的设备日志 (自动利用 hypertable 优化)
-- SELECT * FROM labdatahub_device_logs 
-- WHERE create_time >= NOW() - INTERVAL '1 hour'
-- ORDER BY create_time DESC;

-- 6.2 查询特定设备最近的日志
-- SELECT * FROM labdatahub_device_logs 
-- WHERE device_sn = 'YOUR_DEVICE_SN'
-- AND create_time >= NOW() - INTERVAL '24 hours'
-- ORDER BY create_time DESC
-- LIMIT 100;

-- 6.3 使用聚合视图查询每日日志统计
-- SELECT * FROM device_logs_daily_mat
-- WHERE bucket >= NOW() - INTERVAL '30 days'
-- ORDER BY bucket DESC;

-- ==================== 7. 监控与维护 ====================

-- 查看 hypertable 信息
SELECT * FROM timescaledb_information.hypertables;

-- 查看 chunk 信息
SELECT * FROM timescaledb_information.chunks 
WHERE hypertable_name = 'labdatahub_device_logs';

-- 查看压缩状态
SELECT * FROM timescaledb_information.compressed_hypertables;

-- 手动压缩某个 chunk (如果需要)
-- SELECT compress_chunk('_timescaledb_internal._hyper_1_1_chunk');

-- 查看数据保留策略
SELECT * FROM timescaledb_information.retention_policies;

-- 查看压缩策略
SELECT * FROM timescaledb_information.compression_policies;

-- ==================== 8. 性能调优建议 ====================

-- 调整 TimescaleDB 内存配置 (在 postgresql.conf 中)
-- shared_preload_libraries = 'timescaledb'
-- timescaledb.max_background_workers = 4
-- timescaledb.cache_size = 100MB
-- timescaledb.max_open_chunks_per_insert = 100

-- 对于高并发写入场景，可以调整以下参数:
-- SET timescaledb.enable_chunk_skipping = on;
-- SET timescaledb.enable_optimizations = on;
