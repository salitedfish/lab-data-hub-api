-- ============================================
-- 三菱 CNC TCP（MOCHA 协议）读取配置表
-- 独立增量脚本，勿合入 init.sql。
-- 在 PostgreSQL 主库执行本脚本即可建表：
--   psql -U <user> -d <database> -f mitsubishi_cnc_config.sql
-- 表结构对齐 domain/LabdatahubMitsubishiCncConfig。
-- 地址约定：readType 为树根点位键，轴点位配 axis_no（轴序 1-6）
--   al/fre/pn/spn/cc/sl1/ss1/tn/stn/po/opt/cut/ct/sv/fv/st/pst/opm 无需轴号
--   mechpos/currpos/remapos/cu/sp 需 axis_no（机械坐标/当前坐标/相对坐标/轴电流/轴速度）
-- ============================================

DROP TABLE IF EXISTS "public"."labdatahub_mitsubishi_cnc_config";
CREATE TABLE "public"."labdatahub_mitsubishi_cnc_config" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "belong_sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "belong_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "code" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamp(6),
  "read_type" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "axis_no" numeric(10,0),
  "interval_time" int4 DEFAULT 1,
  "delay_time" int4 DEFAULT 0
)
;
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_cnc_config"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_cnc_config"."belong_sn" IS '归属sn';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_cnc_config"."belong_type" IS '归属类型 0-产品 1-设备';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_cnc_config"."code" IS '读取编码';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_cnc_config"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_cnc_config"."read_type" IS '采集项类型（树根点位键：al/fre/pn/spn/cc/sl1/ss1/tn/stn/po/opt/cut/ct/sv/fv/st/pst/opm/axc；轴点 mechpos/currpos/remapos/cu/sp）';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_cnc_config"."axis_no" IS '轴号（1-6，仅轴类点位有效）';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_cnc_config"."interval_time" IS '多少秒读取一次';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_cnc_config"."delay_time" IS '同一网络组件读取属性延迟时间';
COMMENT ON TABLE "public"."labdatahub_mitsubishi_cnc_config" IS '三菱CNC TCP(MOCHA)协议读取配置表';

-- ----------------------------
-- Primary Key structure for table labdatahub_mitsubishi_cnc_config
-- ----------------------------
ALTER TABLE "public"."labdatahub_mitsubishi_cnc_config" ADD CONSTRAINT "labdatahub_mitsubishi_cnc_config_pkey" PRIMARY KEY ("id");
