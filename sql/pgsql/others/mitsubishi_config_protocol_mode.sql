-- ============================================
-- 三菱 MC 配置表新增协议帧模式列（MC1E / MC3E）
-- 独立增量脚本，勿合入 init.sql。
-- 在 PostgreSQL 主库执行本脚本即可加列：
--   psql -U <user> -d <database> -f mitsubishi_config_protocol_mode.sql
-- 作用：labdatahub_mitsubishi_config 每个点位可独立指定帧协议
--   '3E' = QnA 兼容 3E 二进制帧（默认，现有已真机验证实现）
--   '1E' = MC1E 标准二进制帧（新增，待真机验证）
-- ============================================

ALTER TABLE "public"."labdatahub_mitsubishi_config"
    ADD COLUMN IF NOT EXISTS "protocol_mode" varchar(8) COLLATE "pg_catalog"."default" DEFAULT '3E'::character varying;

COMMENT ON COLUMN "public"."labdatahub_mitsubishi_config"."protocol_mode" IS '协议帧模式：3E-QnA兼容3E帧（默认） 1E-MC1E标准二进制帧';
