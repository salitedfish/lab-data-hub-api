-- ============================================
-- 规则引擎（数据转发）启停日志表
-- 老库增量脚本；同一改动已整合进 init.sql（2026-10-23 手工同步），全新部署无需执行本脚本。
--   psql -U <user> -d <database> -f rule_engine_control_log.sql
--
-- 背景（2026-10-23）：
--   规则引擎列表页的「启用/停用」会把 MQTT broker 整个停掉再起来，外部接收方在停用期间判设备离线。
--   /business/ruleEngine/control 接口没有 @Log，sys_oper_log 查不到谁在什么时候启停，
--   只能去翻 nginx access.log。本表把每次启停（含启动时自动恢复、失败原因）落库，页面可查。
--
--   记录点：LabdatahubRuleEngineServiceImpl#startRuleEngine / #stopRuleEngine
--   幂等：重复执行安全。
-- ============================================

CREATE TABLE IF NOT EXISTS "public"."labdatahub_rule_engine_control_log" (
    "id" character varying(64) NOT NULL,
    "engine_id" character varying(64) NOT NULL,
    "engine_name" character varying(255) DEFAULT NULL::character varying,
    "action" character varying(16) NOT NULL,
    "oper_name" character varying(64) DEFAULT NULL::character varying,
    "oper_time" timestamp(6) without time zone,
    "status" character(1) DEFAULT '0'::bpchar,
    "message" character varying(500) DEFAULT NULL::character varying
);

COMMENT ON TABLE "public"."labdatahub_rule_engine_control_log" IS '规则引擎启停日志';
COMMENT ON COLUMN "public"."labdatahub_rule_engine_control_log"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_rule_engine_control_log"."engine_id" IS '规则引擎id';
COMMENT ON COLUMN "public"."labdatahub_rule_engine_control_log"."engine_name" IS '引擎名称';
COMMENT ON COLUMN "public"."labdatahub_rule_engine_control_log"."action" IS '操作 start-启用 stop-停用';
COMMENT ON COLUMN "public"."labdatahub_rule_engine_control_log"."oper_name" IS '操作人（无登录态时为 system）';
COMMENT ON COLUMN "public"."labdatahub_rule_engine_control_log"."oper_time" IS '操作时间';
COMMENT ON COLUMN "public"."labdatahub_rule_engine_control_log"."status" IS '结果 0-成功 1-失败';
COMMENT ON COLUMN "public"."labdatahub_rule_engine_control_log"."message" IS '说明（失败原因）';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'labdatahub_rule_engine_control_log_pkey'
    ) THEN
        ALTER TABLE "public"."labdatahub_rule_engine_control_log"
            ADD CONSTRAINT "labdatahub_rule_engine_control_log_pkey" PRIMARY KEY ("id");
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS "idx_rule_engine_control_log_engine"
    ON "public"."labdatahub_rule_engine_control_log" USING btree ("engine_id", "oper_time" DESC);
