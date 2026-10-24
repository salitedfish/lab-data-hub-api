-- ============================================================================
-- 2026-10-24 把 labdatahub_device_logs 的 TimescaleDB 策略对齐到 db.sql（老库升级用）
--
-- 目标：压缩窗口 1 天、保留窗口 7 天 —— 与 db.sql 里 add_compression_policy / add_retention_policy 一致。
--
-- ⚠️ 注意：改 add_retention_policy 的 SQL 对已存在的 job 是空操作（if_not_exists => TRUE），
--    改 db.sql / 重跑建表脚本都不会生效，必须 alter_job。下面用 DO 块按 proc_name 找 job，
--    config 用「原 config || 新值」合并写回 —— alter_job 的 config 是整体替换，漏了 hypertable_id 会报错。
--
-- ⚠️ 缩短保留期不可逆：下次策略执行即按 7 天丢弃更早的历史 chunk。
-- ⚠️ 压缩窗口必须小于保留窗口：若两者都是 7 天，chunk 到 7 天直接被删、压缩策略永远不会命中 ——
--    这就是本次把压缩从 7 天改回 1 天的原因（也与 db.sql 一致）。
-- 幂等：可重复执行。
-- ============================================================================

\set ON_ERROR_STOP on

DO $$
DECLARE
    r record;
BEGIN
    FOR r IN
        SELECT job_id, proc_name, config
        FROM timescaledb_information.jobs
        WHERE hypertable_name = 'labdatahub_device_logs'
          AND proc_name IN ('policy_compression', 'policy_retention')
    LOOP
        IF r.proc_name = 'policy_compression' THEN
            PERFORM alter_job(r.job_id, config => r.config || jsonb_build_object('compress_after', '1 day'));
        ELSE
            PERFORM alter_job(r.job_id, config => r.config || jsonb_build_object('drop_after', '7 days'));
        END IF;
        RAISE NOTICE 'altered job % (%) -> %', r.job_id, r.proc_name, r.config;
    END LOOP;
END $$;

-- 核对
SELECT job_id, proc_name, config
FROM timescaledb_information.jobs
WHERE hypertable_name = 'labdatahub_device_logs'
ORDER BY job_id;
