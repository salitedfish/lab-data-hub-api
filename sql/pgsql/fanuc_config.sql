-- ============================================
-- FANUC FOCAS2（FANUC_TCP）协议读取配置表
-- 独立增量脚本，勿合入 init.sql。
-- 在 PostgreSQL 主库执行本脚本即可建表：
--   psql -U <user> -d <database> -f fanuc_config.sql
-- 表结构对齐 domain/LabdatahubFanucConfig。
-- 地址约定：readType.param1.param2（如 axis.1.1 = X轴机械坐标）
-- ============================================

DROP TABLE IF EXISTS "public"."labdatahub_fanuc_config";
CREATE TABLE "public"."labdatahub_fanuc_config" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "belong_sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "belong_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "code" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamp(6),
  "read_type" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "param1" numeric(10,0),
  "param2" numeric(10,0),
  "interval_time" int4 DEFAULT 1,
  "delay_time" int4 DEFAULT 0
)
;
COMMENT ON COLUMN "public"."labdatahub_fanuc_config"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_fanuc_config"."belong_sn" IS '归属sn';
COMMENT ON COLUMN "public"."labdatahub_fanuc_config"."belong_type" IS '归属类型 0-产品 1-设备';
COMMENT ON COLUMN "public"."labdatahub_fanuc_config"."code" IS '读取编码';
COMMENT ON COLUMN "public"."labdatahub_fanuc_config"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_fanuc_config"."read_type" IS '采集项类型（axis/spindle/feed/mode/status/prgnum/exeprgname/alarm/tcode/macro/timer/count/diag/override/pmc）';
COMMENT ON COLUMN "public"."labdatahub_fanuc_config"."param1" IS '参数1（轴号/子项/宏变量号等，各readType含义不同）';
COMMENT ON COLUMN "public"."labdatahub_fanuc_config"."param2" IS '参数2（坐标类型/地址号等，各readType含义不同）';
COMMENT ON COLUMN "public"."labdatahub_fanuc_config"."interval_time" IS '多少秒读取一次';
COMMENT ON COLUMN "public"."labdatahub_fanuc_config"."delay_time" IS '同一网络组件读取属性延迟时间';
COMMENT ON TABLE "public"."labdatahub_fanuc_config" IS 'FANUC FOCAS2协议读取配置表';

-- ----------------------------
-- Primary Key structure for table labdatahub_fanuc_config
-- ----------------------------
ALTER TABLE "public"."labdatahub_fanuc_config" ADD CONSTRAINT "labdatahub_fanuc_config_pkey" PRIMARY KEY ("id");
