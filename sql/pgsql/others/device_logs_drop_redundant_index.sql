-- ============================================================================
-- 2026-10-24 删除 labdatahub_device_logs 上的两条冗余索引（老库升级用）
--
-- 背景（银申卡特生产实测）：超表 13,852,263 行，索引合计 3,523MB，其中
--   idx_device_logs_report_time (report_time DESC)  527MB
--   idx_device_sn            (device_sn)             86MB
-- 两条都没有查询在用：
--   · report_time —— 「取设备最新一条」的 3 处查询（getDeviceLastData、DeviceDownUtils 两处）
--     已统一改为按 create_time 排序，走 idx_device_logs_device_sn (device_sn, create_time DESC)；
--     report_time 无索引前缀可用，原查询实测 5,892ms、扫 4,293,755 条索引项。
--   · device_sn —— 被 idx_device_logs_device_sn (device_sn, create_time DESC) 完全覆盖（最左前缀）。
--
-- ⚠️ 超表上的索引会传播到每个 chunk：DROP INDEX 打的是父表索引名，TimescaleDB 会连带删除
--    各 chunk 上的对应索引。CONCURRENTLY 在超表上不被支持，用普通 DROP（会短暂持有 ACCESS EXCLUSIVE，
--    每条几秒，建议在业务低峰执行）。
-- ⚠️ 幂等：IF EXISTS，可重复执行。
-- 全新部署无需执行本脚本：init.sql 已不含这两条索引。
-- ============================================================================

\set ON_ERROR_STOP on

DROP INDEX IF EXISTS public.idx_device_logs_report_time;
DROP INDEX IF EXISTS public.idx_device_sn;

-- 执行后核对（应只剩 idx_device_logs_device_sn / idx_device_logs_log_type /
-- labdatahub_device_logs_create_time_idx / labdatahub_device_logs_pkey）
SELECT indexname, pg_size_pretty(pg_relation_size(indexname::regclass)) AS size
FROM pg_indexes
WHERE tablename = 'labdatahub_device_logs'
ORDER BY pg_relation_size(indexname::regclass) DESC;
