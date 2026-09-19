-- ============================================
-- 三菱MC协议（MITSUBISHI_MC3E_TCP）读取配置表
-- 独立增量脚本，勿合入 init.sql。
-- 在 PostgreSQL 主库执行本脚本即可建表：
--   psql -U <user> -d <database> -f mitsubishi_mc3e_config.sql
-- 表结构对齐 domain/LabdatahubMitsubishiMc3eConfig。
-- 软元件：字设备 D/W/R/ZR/SD；位设备 M/L/B/X/Y/S/SM/F（X/Y 地址为八进制）。
-- 字读单次 ≤960 点，位读单次 ≤2000 点。
-- ============================================

DROP TABLE IF EXISTS "public"."labdatahub_mitsubishi_mc3e_config";
CREATE TABLE "public"."labdatahub_mitsubishi_mc3e_config" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "belong_sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "belong_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "code" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamp(6),
  "area_code" numeric(10,0),
  "start_address" numeric(10,0),
  "length" numeric(10,0),
  "interval_time" int4 DEFAULT 1,
  "delay_time" int4 DEFAULT 0
)
;
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_mc3e_config"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_mc3e_config"."belong_sn" IS '归属sn';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_mc3e_config"."belong_type" IS '归属类型 0-产品 1-设备';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_mc3e_config"."code" IS '读取编码';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_mc3e_config"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_mc3e_config"."area_code" IS '软元件代码（字设备 D=0xA8 W=0xB4 R=0xAF ZR=0xB0 SD=0xA9；位设备 M=0x90 L=0x92 B=0xA0 X=0x9C Y=0x9D S=0x98 SM=0x91 F=0x93）';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_mc3e_config"."start_address" IS '起始地址（X/Y 为八进制地址）';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_mc3e_config"."length" IS '读取数量（字设备为字数，位设备为点数）';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_mc3e_config"."interval_time" IS '多少秒读取一次';
COMMENT ON COLUMN "public"."labdatahub_mitsubishi_mc3e_config"."delay_time" IS '同一网络组件读取属性延迟时间（毫秒）';
COMMENT ON TABLE "public"."labdatahub_mitsubishi_mc3e_config" IS '三菱MC协议读取配置表';

-- ----------------------------
-- Primary Key structure for table labdatahub_mitsubishi_mc3e_config
-- ----------------------------
ALTER TABLE "public"."labdatahub_mitsubishi_mc3e_config" ADD CONSTRAINT "labdatahub_mitsubishi_mc3e_config_pkey" PRIMARY KEY ("id");
