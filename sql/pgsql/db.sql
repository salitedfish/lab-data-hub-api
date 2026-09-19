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
SELECT * FROM pg_extension WHERE extname = 'timescaledb';
-- ==================== 2. 创建 Hypertable ====================

-- 2.1 设备日志表转换为 hypertable
-- 该表存储设备上报的实时数据，数据量大，适合时序优化
SELECT create_hypertable(
    'labdatahub_device_logs',
    'create_time',                    -- 时间列
    'device_sn',                      -- 分区列 (可选)
    4,                                -- 分区数量
    chunk_time_interval => INTERVAL '1 day',  -- 每个 chunk 的时间间隔 (1 天=86400000 毫秒)
    if_not_exists => TRUE,
    migrate_data => TRUE   -- 添加这一行以迁移现有数据
);


-- ==================== 3. 配置压缩策略 ====================

-- 3.1 启用设备日志表压缩 (7 天前的数据压缩)
ALTER TABLE labdatahub_device_logs SET (
    timescaledb.compress = true,
    timescaledb.compress_segmentby = 'device_sn'
);

SELECT add_compression_policy(
    'labdatahub_device_logs',
    INTERVAL '7 days',
    if_not_exists => TRUE
);



-- ==================== 4. 配置数据保留策略 ====================

-- 4.1 设备日志保留 90 天
SELECT add_retention_policy(
    'labdatahub_device_logs',
    INTERVAL '90 days',
    if_not_exists => TRUE
);

-- ==================== 5. 创建连续聚合视图 (可选) ====================

-- 5.1 设备日志小时级聚合视图
CREATE MATERIALIZED VIEW IF NOT EXISTS device_logs_hourly_mat
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 hour', create_time) AS bucket,
    device_sn,
    COUNT(*) as log_count,
    MAX(create_time) as last_log_time
FROM labdatahub_device_logs
GROUP BY bucket, device_sn
WITH NO DATA;

-- 5.2 设备日志天级聚合视图
CREATE MATERIALIZED VIEW IF NOT EXISTS device_logs_daily_mat
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 day', create_time) AS bucket,
    device_sn,
    COUNT(*) as log_count,
    MAX(create_time) as last_log_time
FROM labdatahub_device_logs
GROUP BY bucket, device_sn
WITH NO DATA;

-- 为聚合视图添加刷新策略 (每 10 分钟刷新一次)
SELECT add_continuous_aggregate_policy(
    'device_logs_hourly_mat',
    start_offset => INTERVAL '3 hours',
    end_offset => INTERVAL '1 minute',
    schedule_interval => INTERVAL '10 minutes',
    if_not_exists => TRUE
);

SELECT add_continuous_aggregate_policy(
    'device_logs_daily_mat',
    start_offset => INTERVAL '3 days',   
    end_offset => INTERVAL '1 hour',
    schedule_interval => INTERVAL '30 minutes',
    if_not_exists => TRUE
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
-- 注意：timescaledb 2.27 已删除 timescaledb_information.compressed_hypertables 视图，
-- 引用它会报「关系不存在」。改用 hypertables 的 compression_enabled 列判断。
SELECT * FROM timescaledb_information.hypertables WHERE compression_enabled;

-- 手动压缩某个 chunk (如果需要)
-- SELECT compress_chunk('_timescaledb_internal._hyper_1_1_chunk');

-- 查看数据保留策略
SELECT * FROM timescaledb_information.jobs 
WHERE proc_name = 'policy_retention';

-- 查看压缩策略
SELECT * FROM timescaledb_information.jobs 
WHERE proc_name = 'policy_compression';

-- 查看连续聚合
SELECT * FROM timescaledb_information.continuous_aggregates;

SELECT 
  job_id,
  hypertable_name as 视图名,
  schedule_interval as 刷新间隔,
  next_start as 下次执行时间
FROM timescaledb_information.jobs
WHERE proc_name = 'policy_refresh_continuous_aggregate';
