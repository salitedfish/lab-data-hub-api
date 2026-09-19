-- ============================================
-- 点位外部写入（写值）审计表
-- 独立增量脚本，勿合入 init.sql；已同步进 protocol_config_all.sql。
-- 在 PostgreSQL 主库执行本脚本即可建表：
--   psql -U <user> -d <database> -f openapi_point_write.sql
-- 表结构对齐 domain/LabdatahubPointWriteRecord。
--
-- 说明：
--   1. 用 CREATE TABLE IF NOT EXISTS 而非 DROP + CREATE —— 这是审计表，
--      重复执行脚本绝不能把已有记录清掉（其它配置表可以 DROP，本表不行）。
--   2. 凡是走到 service 的请求都落一条，含 404/409/422 这些失败请求，
--      否则「点了写值但没反应」这种问题事后无法复盘。
--   3. 不做幂等：同一请求重复提交会写多条记录，这是有意的 —— 审计要如实反映
--      用户点了几次，幂等是接口层的事，不是审计层的事。
--   4. net_type 这一列留着不是冗余：address / write_count / payload 三列的
--      单位与形态全由协议决定（MODBUS 按 16 位寄存器），少了它，将来翻审计记录
--      只会看到一串无法解释的数字。
-- ============================================

CREATE TABLE IF NOT EXISTS "public"."labdatahub_point_write_record" (
  "id" bigserial NOT NULL,
  "request_id" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "device_sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "device_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "code" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "point_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "raw_value" text COLLATE "pg_catalog"."default",
  "source" varchar(16) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "net_type" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "data_type" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "address" int4,
  "write_count" int4,
  "payload" text COLLATE "pg_catalog"."default",
  "is_success" varchar(2) COLLATE "pg_catalog"."default" DEFAULT '1'::character varying,
  "error_code" int4,
  "error_msg" varchar(1024) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "cost_ms" int4,
  "create_time" timestamp(6)
)
;
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."request_id" IS '请求id（服务端生成的追踪标识，不做幂等，见方案 4.6）';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."device_sn" IS '设备sn';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."device_name" IS '设备名称（冗余，设备改名后仍能看清当时写的是哪台）';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."code" IS '点位标识（对应协议点位配置 code / 物模型 identifier）';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."point_name" IS '点位名称（冗余，取协议点位的 name）';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."raw_value" IS '请求写入值（原始字符串，原样落库不转换）';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."source" IS '写入来源 api-接口写入 manual-页面手动写值（由入口标明，不取请求体，见方案 4.8.2）';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."net_type" IS '协议类型（本期恒为 MODBUS_TCP），决定下面 address/write_count/payload 三列的口径';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."data_type" IS '数据类型（物模型 dataType）';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."address" IS '起始地址——MODBUS 寄存器号';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."write_count" IS '写入寄存器个数（16位字）';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."payload" IS '编码后的载荷 JSON（即 EncodeMessage.modbusWriteJson 的内容，如 [{"start":5,"count":1,"registerList":[26729]}]）';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."is_success" IS '0-失败 1-成功';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."error_code" IS '失败时的错误码（400/404/409/422/500/503/504）';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."error_msg" IS '失败原因';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."cost_ms" IS '耗时毫秒';
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."create_time" IS '创建时间';
COMMENT ON TABLE "public"."labdatahub_point_write_record" IS '点位外部写入审计表';

-- ----------------------------
-- Primary Key structure for table labdatahub_point_write_record
-- ----------------------------
ALTER TABLE "public"."labdatahub_point_write_record" DROP CONSTRAINT IF EXISTS "labdatahub_point_write_record_pkey";
ALTER TABLE "public"."labdatahub_point_write_record" ADD CONSTRAINT "labdatahub_point_write_record_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes：按设备倒查、按点位倒查是最常用的两种姿势
-- ----------------------------
CREATE INDEX IF NOT EXISTS "idx_point_write_record_device" ON "public"."labdatahub_point_write_record" ("device_sn", "create_time");
CREATE INDEX IF NOT EXISTS "idx_point_write_record_code" ON "public"."labdatahub_point_write_record" ("code", "create_time");

-- ----------------------------
-- 加列（表已存在的环境补列）
-- source：写入来源 api/manual，方案 4.8 追加时新增。
-- 上面的 CREATE TABLE IF NOT EXISTS 对已建好的表是空操作，所以这里必须再显式补一次。
-- ----------------------------
ALTER TABLE "public"."labdatahub_point_write_record"
  ADD COLUMN IF NOT EXISTS "source" varchar(16) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying;
COMMENT ON COLUMN "public"."labdatahub_point_write_record"."source" IS '写入来源 api-接口写入 manual-页面手动写值（由入口标明，不取请求体，见方案 4.8.2）';
