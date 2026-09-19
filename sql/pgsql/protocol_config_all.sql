-- ============================================
-- 协议读取配置 全量增量脚本（整合版，勿合入 init.sql）
-- 整合自 lab-data-hub-api/sql/pgsql/others 下的全部增量脚本：
--   1. fanuc_config.sql                    建表 labdatahub_fanuc_config
--   2. brother_config.sql                  建表 labdatahub_brother_config
--   3. mitsubishi_mc3e_config.sql               建表 labdatahub_mitsubishi_mc3e_config
--   4. mitsubishi_cnc_config.sql           建表 labdatahub_mitsubishi_cnc_config
--   5. plc_read_enhance.sql                加列 function_code / area_type
--   6. protocol_config_name.sql            加列 name（7 张协议点位表）
--   7. openapi_point_write.sql             建表 labdatahub_point_write_record（点位写值审计）
--   8. omron_fins_bit_address.sql          加列 bit_address（FINS 位访问）
-- 在 PostgreSQL 主库执行本脚本即可：
--   psql -U <user> -d <database> -f protocol_config_all.sql
-- 依赖：modbus / s71200 / omronfins 等基础表由 init.sql 创建，需先执行 init.sql。
-- 顺序说明：先建表、后加列。
-- ⚠️ 不含 mitsubishi_mc3e_config_rename.sql（老表名迁移用，只对已部署的老库执行，
--    且本脚本的建表语句是 DROP+CREATE，二者同时跑会互相打架）。
-- ============================================

-- ============================================
-- 1. FANUC FOCAS2（FANUC_TCP）协议读取配置表
-- 来源：fanuc_config.sql
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

-- ============================================
-- 2. Brother NC（BROTHER_TCP）协议读取配置表
-- 来源：brother_config.sql
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

-- ============================================
-- 3. 三菱MC协议（MITSUBISHI_MC3E_TCP）读取配置表
-- 来源：mitsubishi_mc3e_config.sql
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

-- ============================================
-- 4. 三菱 CNC TCP（MOCHA 协议）读取配置表
-- 来源：mitsubishi_cnc_config.sql
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

-- ============================================
-- 5. PLC 协议「读」能力增强（Modbus / S7-1200）
-- 来源：plc_read_enhance.sql
-- 新增列均有默认值，老数据自动兼容（function_code 默认 '03' 保持寄存器，area_type 默认 'DB'）
-- ============================================

ALTER TABLE labdatahub_modbus_config ADD COLUMN IF NOT EXISTS function_code varchar(32) DEFAULT '03';
ALTER TABLE labdatahub_s71200_config ADD COLUMN IF NOT EXISTS area_type varchar(32) DEFAULT 'DB';

-- ============================================
-- 6. 协议点位表加 name 列（点位名称）
-- 来源：protocol_config_name.sql
-- 用途：物模型属性名用点位名称（前端点位表语义名，如 FANUC"机械坐标 X"），null 时物模型 name 用 code（标识）
-- 仅 7 张协议点位表（含 code 关联物模型）；Database 协议 code=null 不关联物模型，不加
-- ============================================

ALTER TABLE labdatahub_fanuc_config ADD COLUMN IF NOT EXISTS name varchar(100);
COMMENT ON COLUMN labdatahub_fanuc_config.name IS '点位名称（物模型属性名用，null 用标识 code）';

ALTER TABLE labdatahub_brother_config ADD COLUMN IF NOT EXISTS name varchar(100);
COMMENT ON COLUMN labdatahub_brother_config.name IS '点位名称（物模型属性名用，null 用标识 code）';

ALTER TABLE labdatahub_mitsubishi_cnc_config ADD COLUMN IF NOT EXISTS name varchar(100);
COMMENT ON COLUMN labdatahub_mitsubishi_cnc_config.name IS '点位名称（物模型属性名用，null 用标识 code）';

ALTER TABLE labdatahub_modbus_config ADD COLUMN IF NOT EXISTS name varchar(100);
COMMENT ON COLUMN labdatahub_modbus_config.name IS '点位名称（物模型属性名用，null 用标识 code）';

ALTER TABLE labdatahub_mitsubishi_mc3e_config ADD COLUMN IF NOT EXISTS name varchar(100);
COMMENT ON COLUMN labdatahub_mitsubishi_mc3e_config.name IS '点位名称（物模型属性名用，null 用标识 code）';

ALTER TABLE labdatahub_omronfins_config ADD COLUMN IF NOT EXISTS name varchar(100);
COMMENT ON COLUMN labdatahub_omronfins_config.name IS '点位名称（物模型属性名用，null 用标识 code）';

ALTER TABLE labdatahub_s71200_config ADD COLUMN IF NOT EXISTS name varchar(100);
COMMENT ON COLUMN labdatahub_s71200_config.name IS '点位名称（物模型属性名用，null 用标识 code）';

-- ============================================
-- 7. 点位外部写入（写值）审计表
-- 来源：openapi_point_write.sql
-- 用途：物模型「写值」入口（/business/device/pointValue 与 /openapi/v1/device/pointValue）
--   的写入审计，含 404/409/422 这些被拒绝的请求
-- 建表用 IF NOT EXISTS：审计表重复执行脚本不得清掉既有记录（与其它配置表不同）
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

-- ============================================
-- 8. 欧姆龙 FINS 配置表新增位号列（位访问支持）
-- 来源：omron_fins_bit_address.sql
-- 表结构对齐 domain/LabdatahubOmronFinsConfig。
-- FINS 地址 3 字节 = 前 2 字节字地址 + 第 3 字节位号，位访问必须有位号列才能表达。
--   bit_address IS NULL → 字访问（字区码，length = 字个数）
--   bit_address NOT NULL → 位访问（位区码，0-15，length = 1）
-- ============================================

ALTER TABLE "public"."labdatahub_omronfins_config"
  ADD COLUMN IF NOT EXISTS "bit_address" numeric(10,0);

COMMENT ON COLUMN "public"."labdatahub_omronfins_config"."bit_address" IS '位号（仅位区码点位使用，0-15；字区为 NULL 表示按字访问）';

-- 顺带纠正两处旧注释（列本身不动，只改注释文字）：
--   start_address 原注释写「起始字节」，FINS 的寻址单位是「字」，不是字节；
--   length 原注释写「读取长度（字节）」，实际字区=字个数、位区=位个数。
COMMENT ON COLUMN "public"."labdatahub_omronfins_config"."start_address" IS '起始字地址（位区下这是字地址，位号另看 bit_address，0-65535）';
COMMENT ON COLUMN "public"."labdatahub_omronfins_config"."length" IS '读取长度：字区=字个数，位区=位个数（位点位恒为 1）';
