-- ============================================
-- Brother NC（BROTHER_TCP）协议读取配置表
-- 独立增量脚本，勿合入 init.sql。
-- 在 PostgreSQL 主库执行本脚本即可建表：
--   psql -U <user> -d <database> -f brother_config.sql
-- 表结构对齐 domain/LabdatahubBrotherConfig。
-- ============================================

DROP TABLE IF EXISTS "public"."labdatahub_brother_config";
CREATE TABLE "public"."labdatahub_brother_config" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "belong_sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "belong_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "code" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamp(6),
  "data_area" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "row_number" numeric(10,0),
  "field_index" numeric(10,0),
  "interval_time" int4 DEFAULT 1,
  "delay_time" int4 DEFAULT 0
)
;
COMMENT ON COLUMN "public"."labdatahub_brother_config"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_brother_config"."belong_sn" IS '归属sn';
COMMENT ON COLUMN "public"."labdatahub_brother_config"."belong_type" IS '归属类型 0-产品 1-设备';
COMMENT ON COLUMN "public"."labdatahub_brother_config"."code" IS '读取编码';
COMMENT ON COLUMN "public"."labdatahub_brother_config"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_brother_config"."data_area" IS '数据区名（PDSP/ALARM/PRD3/WKCNTR）';
COMMENT ON COLUMN "public"."labdatahub_brother_config"."row_number" IS '行号（1起，对应数据区点表行顺序）';
COMMENT ON COLUMN "public"."labdatahub_brother_config"."field_index" IS '字段序号（1起，第1个字段=行Symbol后第一个值）';
COMMENT ON COLUMN "public"."labdatahub_brother_config"."interval_time" IS '多少秒读取一次';
COMMENT ON COLUMN "public"."labdatahub_brother_config"."delay_time" IS '同一网络组件读取属性延迟时间';
COMMENT ON TABLE "public"."labdatahub_brother_config" IS 'Brother NC协议读取配置表';

-- ----------------------------
-- Primary Key structure for table labdatahub_brother_config
-- ----------------------------
ALTER TABLE "public"."labdatahub_brother_config" ADD CONSTRAINT "labdatahub_brother_config_pkey" PRIMARY KEY ("id");
