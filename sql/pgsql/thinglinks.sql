/*
 Navicat Premium Data Transfer

 Source Server         : 攀哥pgsql
 Source Server Type    : PostgreSQL
 Source Server Version : 150006 (150006)
 Source Host           : 47.109.103.154:5432
 Source Catalog        : labdatahub-iot
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 150006 (150006)
 File Encoding         : 65001

 Date: 24/01/2026 17:46:13
*/


-- ----------------------------
-- Sequence structure for gen_table_column_column_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."gen_table_column_column_id_seq";
CREATE SEQUENCE "public"."gen_table_column_column_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 10000
CACHE 1;

-- ----------------------------
-- Sequence structure for gen_table_table_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."gen_table_table_id_seq";
CREATE SEQUENCE "public"."gen_table_table_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 10000
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_config_config_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_config_config_id_seq";
CREATE SEQUENCE "public"."sys_config_config_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 10000
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_dept_dept_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_dept_dept_id_seq";
CREATE SEQUENCE "public"."sys_dept_dept_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 10000
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_dict_data_dict_code_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_dict_data_dict_code_seq";
CREATE SEQUENCE "public"."sys_dict_data_dict_code_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 10000
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_dict_type_dict_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_dict_type_dict_id_seq";
CREATE SEQUENCE "public"."sys_dict_type_dict_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 10000
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_job_job_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_job_job_id_seq";
CREATE SEQUENCE "public"."sys_job_job_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 10000
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_job_log_job_log_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_job_log_job_log_id_seq";
CREATE SEQUENCE "public"."sys_job_log_job_log_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 10000
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_logininfor_info_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_logininfor_info_id_seq";
CREATE SEQUENCE "public"."sys_logininfor_info_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 10000
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_menu_menu_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_menu_menu_id_seq";
CREATE SEQUENCE "public"."sys_menu_menu_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 10000
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_notice_notice_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_notice_notice_id_seq";
CREATE SEQUENCE "public"."sys_notice_notice_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 10000
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_oper_log_oper_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_oper_log_oper_id_seq";
CREATE SEQUENCE "public"."sys_oper_log_oper_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 10000
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_post_post_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_post_post_id_seq";
CREATE SEQUENCE "public"."sys_post_post_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 10000
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_role_role_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_role_role_id_seq";
CREATE SEQUENCE "public"."sys_role_role_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 10000
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_user_user_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_user_user_id_seq";
CREATE SEQUENCE "public"."sys_user_user_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 10000
CACHE 1;

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS "public"."gen_table";
CREATE TABLE "public"."gen_table" (
  "table_id" int8 NOT NULL DEFAULT nextval('gen_table_table_id_seq'::regclass),
  "table_name" varchar(200) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "table_comment" varchar(500) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "sub_table_name" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "sub_table_fk_name" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "class_name" varchar(100) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "tpl_category" varchar(200) COLLATE "pg_catalog"."default" DEFAULT 'crud'::character varying,
  "tpl_web_type" varchar(30) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "package_name" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "module_name" varchar(30) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "business_name" varchar(30) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "function_name" varchar(50) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "function_author" varchar(50) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "gen_type" char(1) COLLATE "pg_catalog"."default" DEFAULT '0'::bpchar,
  "gen_path" varchar(200) COLLATE "pg_catalog"."default" DEFAULT '/'::character varying,
  "options" varchar(1000) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "update_time" timestamp(6),
  "remark" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."gen_table"."table_id" IS '编号';
COMMENT ON COLUMN "public"."gen_table"."table_name" IS '表名称';
COMMENT ON COLUMN "public"."gen_table"."table_comment" IS '表描述';
COMMENT ON COLUMN "public"."gen_table"."sub_table_name" IS '关联子表的表名';
COMMENT ON COLUMN "public"."gen_table"."sub_table_fk_name" IS '子表关联的外键名';
COMMENT ON COLUMN "public"."gen_table"."class_name" IS '实体类名称';
COMMENT ON COLUMN "public"."gen_table"."tpl_category" IS '使用的模板（crud单表操作 tree树表操作）';
COMMENT ON COLUMN "public"."gen_table"."tpl_web_type" IS '前端模板类型（element-ui模版 element-plus模版）';
COMMENT ON COLUMN "public"."gen_table"."package_name" IS '生成包路径';
COMMENT ON COLUMN "public"."gen_table"."module_name" IS '生成模块名';
COMMENT ON COLUMN "public"."gen_table"."business_name" IS '生成业务名';
COMMENT ON COLUMN "public"."gen_table"."function_name" IS '生成功能名';
COMMENT ON COLUMN "public"."gen_table"."function_author" IS '生成功能作者';
COMMENT ON COLUMN "public"."gen_table"."gen_type" IS '生成代码方式（0zip压缩包 1自定义路径）';
COMMENT ON COLUMN "public"."gen_table"."gen_path" IS '生成路径（不填默认项目路径）';
COMMENT ON COLUMN "public"."gen_table"."options" IS '其它生成选项';
COMMENT ON COLUMN "public"."gen_table"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."gen_table"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."gen_table"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."gen_table"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."gen_table"."remark" IS '备注';
COMMENT ON TABLE "public"."gen_table" IS '代码生成业务表';

-- ----------------------------
-- Records of gen_table
-- ----------------------------
INSERT INTO "public"."gen_table" VALUES (1, 'labdatahub_component', '网络组件', NULL, NULL, 'LabdatahubComponent', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'component', '网络组件', 'ruoyi', '0', '/', '{}', 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18', NULL);
INSERT INTO "public"."gen_table" VALUES (2, 'labdatahub_product', '产品表', NULL, NULL, 'LabdatahubProduct', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'product', '产品', 'ruoyi', '0', '/', '{}', 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26', NULL);
INSERT INTO "public"."gen_table" VALUES (3, 'labdatahub_properties', '物模型属性定义表', NULL, NULL, 'LabdatahubProperties', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'properties', '物模型属性定义', 'ruoyi', '0', '/', '{}', 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33', NULL);
INSERT INTO "public"."gen_table" VALUES (4, 'labdatahub_protocol', '协议管理', NULL, NULL, 'LabdatahubProtocol', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'protocol', '协议管理', 'ruoyi', '0', '/', '{}', 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39', NULL);
INSERT INTO "public"."gen_table" VALUES (5, 'labdatahub', '设备表', NULL, NULL, 'ThingllinksDevice', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'device', '设备', 'ruoyi', '0', '/', '{}', 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45', NULL);
INSERT INTO "public"."gen_table" VALUES (6, 'labdatahub_device_logs', '设备日志表', NULL, NULL, 'LabdatahubDeviceLogs', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'logs', '设备日志', 'ruoyi', '0', '/', '{}', 'admin', '2025-09-22 15:42:02', '', '2025-09-22 15:42:16', NULL);
INSERT INTO "public"."gen_table" VALUES (7, 'labdatahub_warn_config', '告警配置表', NULL, NULL, 'LabdatahubWarnConfig', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'warnConfig', '告警配置', 'ruoyi', '0', '/', '{}', 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48', NULL);
INSERT INTO "public"."gen_table" VALUES (9, 'labdatahub_rule_engine', '规则引擎配置', NULL, NULL, 'LabdatahubRuleEngine', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'engine', '规则引擎配置', 'ruoyi', '0', '/', '{}', 'admin', '2025-10-20 10:01:01', '', '2025-10-20 10:01:27', NULL);
INSERT INTO "public"."gen_table" VALUES (10, 'labdatahub_function', '设备指令下发表', NULL, NULL, 'LabdatahubFunction', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'function', '设备指令下发', 'ruoyi', '0', '/', '{}', 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25', NULL);
INSERT INTO "public"."gen_table" VALUES (11, 'warn_function_record', '指令下发记录', NULL, NULL, 'WarnFunctionRecord', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'record', '指令下发记录', 'ruoyi', '0', '/', '{"parentMenuId":0}', 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24', NULL);
INSERT INTO "public"."gen_table" VALUES (13, 'labdatahub_linkage_action_record', '设备联动告警动作执行记录', NULL, NULL, 'LabdatahubLinkageActionRecord', 'crud', 'element-ui', 'com.labdatahub.system', 'business', 'linkageAction', '设备联动告警动作执行记录', 'ruoyi', '0', '/', '{"parentMenuId":0}', 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:03', NULL);
INSERT INTO "public"."gen_table" VALUES (14, 'labdatahub_linkage_warn_record', '设备联动告警记录', NULL, NULL, 'LabdatahubLinkageWarnRecord', 'crud', 'element-ui', 'com.labdatahub.system', 'business', 'linkageRecord', '设备联动告警记录', 'ruoyi', '0', '/', '{}', 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17', NULL);
INSERT INTO "public"."gen_table" VALUES (15, 'labdatahub_scheduled_task', '定时引擎配置表', NULL, NULL, 'LabdatahubScheduledTask', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'scheduledEngine', '定时引擎配置', 'ruoyi', '0', '/', '{}', 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00', NULL);
INSERT INTO "public"."gen_table" VALUES (16, 'labdatahub_modbus_config', 'modbus协议读取配置表', NULL, NULL, 'LabdatahubModbusConfig', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'modbus', 'modbus协议读取配置', 'ruoyi', '0', '/', '{"parentMenuId":0}', 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:47.726', NULL);
INSERT INTO "public"."gen_table" VALUES (12, 'labdatahub_warn_linkage', '设备联动告警', NULL, NULL, 'LabdatahubWarnLinkage', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'linkage', '设备联动告警', 'ruoyi', '0', '/', '{"parentMenuId":0}', 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.876', NULL);

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS "public"."gen_table_column";
CREATE TABLE "public"."gen_table_column" (
  "column_id" int8 NOT NULL DEFAULT nextval('gen_table_column_column_id_seq'::regclass),
  "table_id" int8,
  "column_name" varchar(200) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "column_comment" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "column_type" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "java_type" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "java_field" varchar(200) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "is_pk" char(1) COLLATE "pg_catalog"."default" DEFAULT NULL::bpchar,
  "is_increment" char(1) COLLATE "pg_catalog"."default" DEFAULT NULL::bpchar,
  "is_required" char(1) COLLATE "pg_catalog"."default" DEFAULT NULL::bpchar,
  "is_insert" char(1) COLLATE "pg_catalog"."default" DEFAULT NULL::bpchar,
  "is_edit" char(1) COLLATE "pg_catalog"."default" DEFAULT NULL::bpchar,
  "is_list" char(1) COLLATE "pg_catalog"."default" DEFAULT NULL::bpchar,
  "is_query" char(1) COLLATE "pg_catalog"."default" DEFAULT NULL::bpchar,
  "query_type" varchar(200) COLLATE "pg_catalog"."default" DEFAULT 'EQ'::character varying,
  "html_type" varchar(200) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "dict_type" varchar(200) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "sort" int4,
  "create_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "update_time" timestamp(6)
)
;
COMMENT ON COLUMN "public"."gen_table_column"."column_id" IS '编号';
COMMENT ON COLUMN "public"."gen_table_column"."table_id" IS '归属表编号';
COMMENT ON COLUMN "public"."gen_table_column"."column_name" IS '列名称';
COMMENT ON COLUMN "public"."gen_table_column"."column_comment" IS '列描述';
COMMENT ON COLUMN "public"."gen_table_column"."column_type" IS '列类型';
COMMENT ON COLUMN "public"."gen_table_column"."java_type" IS 'JAVA类型';
COMMENT ON COLUMN "public"."gen_table_column"."java_field" IS 'JAVA字段名';
COMMENT ON COLUMN "public"."gen_table_column"."is_pk" IS '是否主键（1是）';
COMMENT ON COLUMN "public"."gen_table_column"."is_increment" IS '是否自增（1是）';
COMMENT ON COLUMN "public"."gen_table_column"."is_required" IS '是否必填（1是）';
COMMENT ON COLUMN "public"."gen_table_column"."is_insert" IS '是否为插入字段（1是）';
COMMENT ON COLUMN "public"."gen_table_column"."is_edit" IS '是否编辑字段（1是）';
COMMENT ON COLUMN "public"."gen_table_column"."is_list" IS '是否列表字段（1是）';
COMMENT ON COLUMN "public"."gen_table_column"."is_query" IS '是否查询字段（1是）';
COMMENT ON COLUMN "public"."gen_table_column"."query_type" IS '查询方式（等于、不等于、大于、小于、范围）';
COMMENT ON COLUMN "public"."gen_table_column"."html_type" IS '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）';
COMMENT ON COLUMN "public"."gen_table_column"."dict_type" IS '字典类型';
COMMENT ON COLUMN "public"."gen_table_column"."sort" IS '排序';
COMMENT ON COLUMN "public"."gen_table_column"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."gen_table_column"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."gen_table_column"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."gen_table_column"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."gen_table_column" IS '代码生成业务表字段';

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------
INSERT INTO "public"."gen_table_column" VALUES (1, 1, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO "public"."gen_table_column" VALUES (2, 1, 'name', '组件名称', 'varchar(255)', 'String', 'name', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 2, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO "public"."gen_table_column" VALUES (3, 1, 'net_type', '网络类型', 'varchar(255)', 'String', 'netType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 3, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO "public"."gen_table_column" VALUES (4, 1, 'ip_addr', 'IP地址', 'varchar(255)', 'String', 'ipAddr', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO "public"."gen_table_column" VALUES (5, 1, 'port', '端口', 'int', 'Long', 'port', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO "public"."gen_table_column" VALUES (6, 1, 'open_tls', '是否开启TLS (0-否 1-是)', 'varchar(255)', 'String', 'openTls', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO "public"."gen_table_column" VALUES (7, 1, 'remark', '备注', 'text', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'textarea', '', 7, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO "public"."gen_table_column" VALUES (8, 1, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 8, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO "public"."gen_table_column" VALUES (9, 1, 'create_by', '创建人', 'varchar(255)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 9, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO "public"."gen_table_column" VALUES (10, 1, 'update_time', '修改时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'datetime', '', 10, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO "public"."gen_table_column" VALUES (11, 1, 'update_by', '修改人', 'varchar(255)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'input', '', 11, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO "public"."gen_table_column" VALUES (12, 1, 'status', '0-停用 1-启用', 'varchar(255)', 'String', 'status', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'radio', '', 12, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO "public"."gen_table_column" VALUES (13, 1, 'other_config', '其余配置(如账号密码等)', 'text', 'String', 'otherConfig', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 13, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO "public"."gen_table_column" VALUES (14, 1, 'protocol_id', '协议id', 'varchar(255)', 'String', 'protocolId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 14, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO "public"."gen_table_column" VALUES (15, 1, 'protocol_name', '协议名称', 'varchar(255)', 'String', 'protocolName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 15, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO "public"."gen_table_column" VALUES (16, 2, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO "public"."gen_table_column" VALUES (17, 2, 'product_sn', '产品编码', 'varchar(255)', 'String', 'productSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO "public"."gen_table_column" VALUES (18, 2, 'product_name', '产品名称', 'varchar(255)', 'String', 'productName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 3, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO "public"."gen_table_column" VALUES (19, 2, 'link_method_id', '接入方式id', 'varchar(255)', 'String', 'linkMethodId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO "public"."gen_table_column" VALUES (20, 2, 'link_method_name', '接入方式名称', 'varchar(255)', 'String', 'linkMethodName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 5, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO "public"."gen_table_column" VALUES (21, 2, 'protocol_id', '协议id', 'varchar(255)', 'String', 'protocolId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO "public"."gen_table_column" VALUES (22, 2, 'protocol_name', '协议名称', 'varchar(255)', 'String', 'protocolName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 7, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO "public"."gen_table_column" VALUES (23, 2, 'device_count', '设备数量', 'int', 'Long', 'deviceCount', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO "public"."gen_table_column" VALUES (24, 2, 'device_type', '设备类型 0-直连设备 1-网关设备', 'varchar(255)', 'String', 'deviceType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 9, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO "public"."gen_table_column" VALUES (25, 2, 'remark', '备注', 'text', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'textarea', '', 10, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO "public"."gen_table_column" VALUES (26, 2, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 11, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO "public"."gen_table_column" VALUES (27, 2, 'update_time', '修改时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'datetime', '', 12, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO "public"."gen_table_column" VALUES (28, 2, 'create_by', '创建人', 'varchar(255)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 13, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO "public"."gen_table_column" VALUES (29, 2, 'update_by', '修改人', 'varchar(255)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'input', '', 14, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO "public"."gen_table_column" VALUES (30, 2, 'status', '0-停用 1-启用', 'varchar(255)', 'String', 'status', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'radio', '', 15, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:27');
INSERT INTO "public"."gen_table_column" VALUES (31, 3, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO "public"."gen_table_column" VALUES (32, 3, 'belong_id', '归属id 产品/设备', 'varchar(50)', 'String', 'belongId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO "public"."gen_table_column" VALUES (33, 3, 'belong_type', '归属类型 0-产品 1-设备', 'varchar(255)', 'String', 'belongType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 3, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO "public"."gen_table_column" VALUES (34, 3, 'identifier', '属性标识符，如 temperature, status', 'varchar(100)', 'String', 'identifier', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO "public"."gen_table_column" VALUES (35, 3, 'name', '属性名称', 'varchar(100)', 'String', 'name', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 5, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO "public"."gen_table_column" VALUES (36, 3, 'parent_id', '父属性ID，用于构建嵌套结构。0表示根级属性', 'varchar(255)', 'String', 'parentId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO "public"."gen_table_column" VALUES (37, 3, 'data_type', '数据类型: int, double, bool, string, struct, array...', 'varchar(50)', 'String', 'dataType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 7, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO "public"."gen_table_column" VALUES (38, 3, 'sort_num', '排序', 'int', 'Long', 'sortNum', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO "public"."gen_table_column" VALUES (39, 3, 'from_type', '来源 0-产品继承 1-设备自定义(继承不可修改)', 'varchar(255)', 'String', 'fromType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 9, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO "public"."gen_table_column" VALUES (40, 4, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39');
INSERT INTO "public"."gen_table_column" VALUES (41, 4, 'protocol_name', '协议名称', 'varchar(255)', 'String', 'protocolName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 2, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39');
INSERT INTO "public"."gen_table_column" VALUES (42, 4, 'local_url', '本地存储路径', 'text', 'String', 'localUrl', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 3, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39');
INSERT INTO "public"."gen_table_column" VALUES (43, 4, 'main_class_path', '解析类入口', 'text', 'String', 'mainClassPath', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 4, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39');
INSERT INTO "public"."gen_table_column" VALUES (44, 4, 'origin_name', '文件原始名字', 'text', 'String', 'originName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'textarea', '', 5, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39');
INSERT INTO "public"."gen_table_column" VALUES (45, 4, 'type', '0-jar包', 'varchar(255)', 'String', 'type', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 6, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39');
INSERT INTO "public"."gen_table_column" VALUES (46, 4, 'new_name', '文件重命名', 'text', 'String', 'newName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'textarea', '', 7, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39');
INSERT INTO "public"."gen_table_column" VALUES (47, 5, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO "public"."gen_table_column" VALUES (48, 5, 'device_id', '设备id', 'varchar(255)', 'String', 'deviceId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO "public"."gen_table_column" VALUES (49, 5, 'device_sn', '设备编码', 'varchar(255)', 'String', 'deviceSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO "public"."gen_table_column" VALUES (50, 5, 'device_name', '设备名称', 'varchar(255)', 'String', 'deviceName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 4, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO "public"."gen_table_column" VALUES (51, 5, 'product_id', '关联产品id', 'varchar(255)', 'String', 'productId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO "public"."gen_table_column" VALUES (52, 5, 'product_name', '关联产品名称', 'varchar(255)', 'String', 'productName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 6, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO "public"."gen_table_column" VALUES (53, 5, 'product_sn', '关联产品编码', 'varchar(255)', 'String', 'productSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 7, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO "public"."gen_table_column" VALUES (54, 5, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 8, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO "public"."gen_table_column" VALUES (55, 5, 'create_by', '创建人', 'varchar(255)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 9, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO "public"."gen_table_column" VALUES (56, 5, 'update_time', '修改时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'datetime', '', 10, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO "public"."gen_table_column" VALUES (57, 5, 'update_by', '修改人', 'varchar(255)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'input', '', 11, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO "public"."gen_table_column" VALUES (58, 5, 'link_method_id', '接入方式id', 'varchar(255)', 'String', 'linkMethodId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 12, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO "public"."gen_table_column" VALUES (59, 5, 'link_method_name', '接入方式名称', 'varchar(255)', 'String', 'linkMethodName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 13, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO "public"."gen_table_column" VALUES (60, 5, 'protocol_id', '协议id', 'varchar(255)', 'String', 'protocolId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 14, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO "public"."gen_table_column" VALUES (61, 5, 'protocol_name', '协议名称', 'varchar(255)', 'String', 'protocolName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 15, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO "public"."gen_table_column" VALUES (62, 5, 'status', '0-离线 1-在线', 'varchar(255)', 'String', 'status', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'radio', '', 16, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:46');
INSERT INTO "public"."gen_table_column" VALUES (63, 6, 'id', 'id', 'bigint', 'Long', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-09-22 15:42:02', '', '2025-09-22 15:42:16');
INSERT INTO "public"."gen_table_column" VALUES (64, 6, 'device_sn', '设备sn', 'varchar(255)', 'String', 'deviceSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-09-22 15:42:02', '', '2025-09-22 15:42:16');
INSERT INTO "public"."gen_table_column" VALUES (65, 6, 'report_time', '上报时间', 'datetime', 'Date', 'reportTime', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'datetime', '', 3, 'admin', '2025-09-22 15:42:02', '', '2025-09-22 15:42:16');
INSERT INTO "public"."gen_table_column" VALUES (66, 6, 'properties', '属性json', 'text', 'String', 'properties', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 4, 'admin', '2025-09-22 15:42:02', '', '2025-09-22 15:42:16');
INSERT INTO "public"."gen_table_column" VALUES (67, 6, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 5, 'admin', '2025-09-22 15:42:02', '', '2025-09-22 15:42:16');
INSERT INTO "public"."gen_table_column" VALUES (68, 6, 'log_type', '日志类型', 'varchar(255)', 'String', 'logType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 6, 'admin', '2025-09-22 15:42:02', '', '2025-09-22 15:42:16');
INSERT INTO "public"."gen_table_column" VALUES (69, 7, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO "public"."gen_table_column" VALUES (70, 7, 'name', '告警名称', 'varchar(255)', 'String', 'name', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 2, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO "public"."gen_table_column" VALUES (71, 7, 'belong_sn', '产品/设备sn', 'varchar(255)', 'String', 'belongSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO "public"."gen_table_column" VALUES (72, 7, 'belong_type', '来源 0-产品 1-设备', 'varchar(255)', 'String', 'belongType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 4, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO "public"."gen_table_column" VALUES (73, 7, 'rule_json', '规则json', 'text', 'String', 'ruleJson', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 5, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO "public"."gen_table_column" VALUES (74, 7, 'warn_message', '告警消息模板', 'text', 'String', 'warnMessage', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 6, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO "public"."gen_table_column" VALUES (75, 7, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 7, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO "public"."gen_table_column" VALUES (76, 7, 'create_by', '创建人', 'varchar(255)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 8, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO "public"."gen_table_column" VALUES (77, 7, 'update_time', '修改时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'datetime', '', 9, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO "public"."gen_table_column" VALUES (78, 7, 'update_by', '修改人', 'varchar(255)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'input', '', 10, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO "public"."gen_table_column" VALUES (87, 9, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-10-20 10:01:01', '', '2025-10-20 10:01:27');
INSERT INTO "public"."gen_table_column" VALUES (88, 9, 'engine_name', '引擎名称', 'varchar(255)', 'String', 'engineName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 2, 'admin', '2025-10-20 10:01:01', '', '2025-10-20 10:01:27');
INSERT INTO "public"."gen_table_column" VALUES (89, 9, 'config_json', 'json配置', 'text', 'String', 'configJson', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 3, 'admin', '2025-10-20 10:01:01', '', '2025-10-20 10:01:27');
INSERT INTO "public"."gen_table_column" VALUES (90, 9, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 4, 'admin', '2025-10-20 10:01:01', '', '2025-10-20 10:01:27');
INSERT INTO "public"."gen_table_column" VALUES (91, 9, 'remark', '备注', 'varchar(255)', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'input', '', 5, 'admin', '2025-10-20 10:01:01', '', '2025-10-20 10:01:27');
INSERT INTO "public"."gen_table_column" VALUES (92, 10, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO "public"."gen_table_column" VALUES (93, 10, 'function_name', '功能名称', 'varchar(255)', 'String', 'functionName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 2, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO "public"."gen_table_column" VALUES (94, 10, 'function_code', '功能编码', 'varchar(255)', 'String', 'functionCode', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO "public"."gen_table_column" VALUES (95, 10, 'function_params', '自定义参数', 'text', 'String', 'functionParams', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 4, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO "public"."gen_table_column" VALUES (96, 10, 'device_sn', '设备/产品sn', 'varchar(255)', 'String', 'deviceSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO "public"."gen_table_column" VALUES (97, 10, 'protocol_id', '协议id', 'text', 'String', 'protocolId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 6, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO "public"."gen_table_column" VALUES (98, 10, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 7, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO "public"."gen_table_column" VALUES (99, 10, 'create_by', '创建人', 'varchar(255)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 8, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO "public"."gen_table_column" VALUES (100, 11, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO "public"."gen_table_column" VALUES (101, 11, 'function_id', '功能id', 'varchar(255)', 'String', 'functionId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO "public"."gen_table_column" VALUES (102, 11, 'function_code', '功能code', 'varchar(255)', 'String', 'functionCode', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO "public"."gen_table_column" VALUES (103, 11, 'function_name', '功能名称', 'varchar(255)', 'String', 'functionName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 4, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO "public"."gen_table_column" VALUES (104, 11, 'function_params', '参数', 'text', 'String', 'functionParams', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 5, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO "public"."gen_table_column" VALUES (105, 11, 'is_success', '0-失败 1-成功', 'varchar(255)', 'String', 'isSuccess', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO "public"."gen_table_column" VALUES (106, 11, 'create_time', '下发时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 7, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO "public"."gen_table_column" VALUES (107, 11, 'device_sn', '设备sn', 'varchar(255)', 'String', 'deviceSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO "public"."gen_table_column" VALUES (108, 11, 'device_name', '设备名称', 'varchar(255)', 'String', 'deviceName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 9, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO "public"."gen_table_column" VALUES (109, 11, 'trigger_type', '0-手动触发 1-告警触发', 'varchar(255)', 'String', 'triggerType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 10, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO "public"."gen_table_column" VALUES (121, 13, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:03');
INSERT INTO "public"."gen_table_column" VALUES (122, 13, 'config_id', '告警配置id', 'varchar(255)', 'String', 'configId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:03');
INSERT INTO "public"."gen_table_column" VALUES (123, 13, 'config_name', '告警配置名称', 'varchar(255)', 'String', 'configName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 3, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:03');
INSERT INTO "public"."gen_table_column" VALUES (124, 13, 'execute_sn', '执行动作设备SN', 'text', 'String', 'executeSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 4, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:03');
INSERT INTO "public"."gen_table_column" VALUES (125, 13, 'execute_name', '执行动作设备名称', 'text', 'String', 'executeName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'textarea', '', 5, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:04');
INSERT INTO "public"."gen_table_column" VALUES (126, 13, 'function_code', '动作CODE', 'varchar(255)', 'String', 'functionCode', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:04');
INSERT INTO "public"."gen_table_column" VALUES (127, 13, 'function_name', '动作名称', 'varchar(255)', 'String', 'functionName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 7, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:04');
INSERT INTO "public"."gen_table_column" VALUES (128, 13, 'function_param', '参数', 'text', 'String', 'functionParam', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 8, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:04');
INSERT INTO "public"."gen_table_column" VALUES (129, 13, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 9, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:04');
INSERT INTO "public"."gen_table_column" VALUES (130, 14, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO "public"."gen_table_column" VALUES (131, 14, 'config_id', '告警配置id', 'varchar(255)', 'String', 'configId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO "public"."gen_table_column" VALUES (132, 14, 'config_name', '告警配置名称', 'varchar(255)', 'String', 'configName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 3, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO "public"."gen_table_column" VALUES (133, 14, 'warn_message', '告警内容', 'text', 'String', 'warnMessage', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 4, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO "public"."gen_table_column" VALUES (134, 14, 'warn_data', '告警时相关设备数据', 'text', 'String', 'warnData', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 5, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO "public"."gen_table_column" VALUES (135, 14, 'trigger_sn_list', '告警设备SN列表', 'text', 'String', 'triggerSnList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 6, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO "public"."gen_table_column" VALUES (136, 14, 'trigger_name_list', '告警设备名称列表', 'text', 'String', 'triggerNameList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 7, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO "public"."gen_table_column" VALUES (137, 14, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 8, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO "public"."gen_table_column" VALUES (138, 14, 'warn_level', '告警等级 1-紧急 2-严重 3-一般 4-警告 5-正常', 'varchar(255)', 'String', 'warnLevel', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 9, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO "public"."gen_table_column" VALUES (139, 14, 'status', '0-未处理 1-已处理', 'varchar(255)', 'String', 'status', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'radio', '', 10, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO "public"."gen_table_column" VALUES (140, 15, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO "public"."gen_table_column" VALUES (141, 15, 'name', '配置名称', 'varchar(255)', 'String', 'name', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 2, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO "public"."gen_table_column" VALUES (142, 15, 'rule_json', '规则json', 'text', 'String', 'ruleJson', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 3, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO "public"."gen_table_column" VALUES (143, 15, 'is_enable', '是否启用 0-否 1-是', 'varchar(255)', 'String', 'isEnable', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO "public"."gen_table_column" VALUES (144, 15, 'execute_sn_list', '执行动作设备列表', 'text', 'String', 'executeSnList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 5, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO "public"."gen_table_column" VALUES (145, 15, 'execute_name_list', '执行动作设备名称', 'text', 'String', 'executeNameList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 6, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO "public"."gen_table_column" VALUES (146, 15, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 7, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO "public"."gen_table_column" VALUES (147, 15, 'remark', '备注', 'text', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'textarea', '', 8, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO "public"."gen_table_column" VALUES (110, 12, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.892');
INSERT INTO "public"."gen_table_column" VALUES (111, 12, 'rule_json', '规则json', 'text', 'String', 'ruleJson', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 2, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.908');
INSERT INTO "public"."gen_table_column" VALUES (148, 16, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:47.82');
INSERT INTO "public"."gen_table_column" VALUES (149, 16, 'belong_sn', '归属sn', 'varchar(255)', 'String', 'belongSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:47.852');
INSERT INTO "public"."gen_table_column" VALUES (150, 16, 'belong_type', '归属类型 0-产品 1-设备', 'varchar(255)', 'String', 'belongType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 3, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:47.869');
INSERT INTO "public"."gen_table_column" VALUES (151, 16, 'code', '读取编码', 'varchar(255)', 'String', 'code', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:47.888');
INSERT INTO "public"."gen_table_column" VALUES (152, 16, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 5, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:47.917');
INSERT INTO "public"."gen_table_column" VALUES (153, 16, 'register_range', '范围,逗号分隔如（1,2-5,7）', 'text', 'String', 'registerRange', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 6, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:48.019');
INSERT INTO "public"."gen_table_column" VALUES (154, 16, 'interval_time', '多少毫秒读取一次', 'int', 'Long', 'intervalTime', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 7, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:48.044');
INSERT INTO "public"."gen_table_column" VALUES (155, 16, 'delay_time', '同一设备读取属性延迟时间', 'int', 'Long', 'delayTime', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:48.071');
INSERT INTO "public"."gen_table_column" VALUES (112, 12, 'warn_message', '告警消息模板', 'text', 'String', 'warnMessage', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 3, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.925');
INSERT INTO "public"."gen_table_column" VALUES (113, 12, 'warn_level', '告警等级 1-紧急 2-严重 3-警告 4-正常', 'varchar(255)', 'String', 'warnLevel', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.94');
INSERT INTO "public"."gen_table_column" VALUES (114, 12, 'execute_action', '执行动作json', 'text', 'String', 'executeAction', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 5, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.956');
INSERT INTO "public"."gen_table_column" VALUES (115, 12, 'is_enable', '是否启用 0-否 1-是', 'varchar(255)', 'String', 'isEnable', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.974');
INSERT INTO "public"."gen_table_column" VALUES (116, 12, 'trigger_sn_list', '触发设备列表', 'text', 'String', 'triggerSnList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 7, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.99');
INSERT INTO "public"."gen_table_column" VALUES (117, 12, 'trigger_name_list', '触发设备名称', 'text', 'String', 'triggerNameList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 8, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:50:00.005');
INSERT INTO "public"."gen_table_column" VALUES (118, 12, 'execute_sn_list', '执行动作设备列表', 'text', 'String', 'executeSnList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 9, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:50:00.021');
INSERT INTO "public"."gen_table_column" VALUES (119, 12, 'execute_name_list', '执行动作设备名称', 'text', 'String', 'executeNameList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 10, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:50:00.036');
INSERT INTO "public"."gen_table_column" VALUES (120, 12, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 11, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:50:00.051');

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_blob_triggers";
CREATE TABLE "public"."qrtz_blob_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "blob_data" bytea
)
;
COMMENT ON COLUMN "public"."qrtz_blob_triggers"."sched_name" IS '调度名称';
COMMENT ON COLUMN "public"."qrtz_blob_triggers"."trigger_name" IS 'qrtz_triggers表trigger_name的外键';
COMMENT ON COLUMN "public"."qrtz_blob_triggers"."trigger_group" IS 'qrtz_triggers表trigger_group的外键';
COMMENT ON COLUMN "public"."qrtz_blob_triggers"."blob_data" IS '存放持久化Trigger对象';
COMMENT ON TABLE "public"."qrtz_blob_triggers" IS 'Blob类型的触发器表';

-- ----------------------------
-- Records of qrtz_blob_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_calendars";
CREATE TABLE "public"."qrtz_calendars" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
  "calendar_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "calendar" bytea NOT NULL
)
;
COMMENT ON COLUMN "public"."qrtz_calendars"."sched_name" IS '调度名称';
COMMENT ON COLUMN "public"."qrtz_calendars"."calendar_name" IS '日历名称';
COMMENT ON COLUMN "public"."qrtz_calendars"."calendar" IS '存放持久化calendar对象';
COMMENT ON TABLE "public"."qrtz_calendars" IS '日历信息表';

-- ----------------------------
-- Records of qrtz_calendars
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_cron_triggers";
CREATE TABLE "public"."qrtz_cron_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "cron_expression" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "time_zone_id" varchar(80) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."qrtz_cron_triggers"."sched_name" IS '调度名称';
COMMENT ON COLUMN "public"."qrtz_cron_triggers"."trigger_name" IS 'qrtz_triggers表trigger_name的外键';
COMMENT ON COLUMN "public"."qrtz_cron_triggers"."trigger_group" IS 'qrtz_triggers表trigger_group的外键';
COMMENT ON COLUMN "public"."qrtz_cron_triggers"."cron_expression" IS 'cron表达式';
COMMENT ON COLUMN "public"."qrtz_cron_triggers"."time_zone_id" IS '时区';
COMMENT ON TABLE "public"."qrtz_cron_triggers" IS 'Cron类型的触发器表';

-- ----------------------------
-- Records of qrtz_cron_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_fired_triggers";
CREATE TABLE "public"."qrtz_fired_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
  "entry_id" varchar(95) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "instance_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "fired_time" int8 NOT NULL,
  "sched_time" int8 NOT NULL,
  "priority" int4 NOT NULL,
  "state" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
  "job_name" varchar(200) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "job_group" varchar(200) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "is_nonconcurrent" varchar(1) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "requests_recovery" varchar(1) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."sched_name" IS '调度名称';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."entry_id" IS '调度器实例id';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."trigger_name" IS 'qrtz_triggers表trigger_name的外键';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."trigger_group" IS 'qrtz_triggers表trigger_group的外键';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."instance_name" IS '调度器实例名';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."fired_time" IS '触发的时间';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."sched_time" IS '定时器制定的时间';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."priority" IS '优先级';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."state" IS '状态';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."job_name" IS '任务名称';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."job_group" IS '任务组名';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."is_nonconcurrent" IS '是否并发';
COMMENT ON COLUMN "public"."qrtz_fired_triggers"."requests_recovery" IS '是否接受恢复执行';
COMMENT ON TABLE "public"."qrtz_fired_triggers" IS '已触发的触发器表';

-- ----------------------------
-- Records of qrtz_fired_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_job_details";
CREATE TABLE "public"."qrtz_job_details" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
  "job_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "job_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(250) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "job_class_name" varchar(250) COLLATE "pg_catalog"."default" NOT NULL,
  "is_durable" varchar(1) COLLATE "pg_catalog"."default" NOT NULL,
  "is_nonconcurrent" varchar(1) COLLATE "pg_catalog"."default" NOT NULL,
  "is_update_data" varchar(1) COLLATE "pg_catalog"."default" NOT NULL,
  "requests_recovery" varchar(1) COLLATE "pg_catalog"."default" NOT NULL,
  "job_data" bytea
)
;
COMMENT ON COLUMN "public"."qrtz_job_details"."sched_name" IS '调度名称';
COMMENT ON COLUMN "public"."qrtz_job_details"."job_name" IS '任务名称';
COMMENT ON COLUMN "public"."qrtz_job_details"."job_group" IS '任务组名';
COMMENT ON COLUMN "public"."qrtz_job_details"."description" IS '相关介绍';
COMMENT ON COLUMN "public"."qrtz_job_details"."job_class_name" IS '执行任务类名称';
COMMENT ON COLUMN "public"."qrtz_job_details"."is_durable" IS '是否持久化';
COMMENT ON COLUMN "public"."qrtz_job_details"."is_nonconcurrent" IS '是否并发';
COMMENT ON COLUMN "public"."qrtz_job_details"."is_update_data" IS '是否更新数据';
COMMENT ON COLUMN "public"."qrtz_job_details"."requests_recovery" IS '是否接受恢复执行';
COMMENT ON COLUMN "public"."qrtz_job_details"."job_data" IS '存放持久化job对象';
COMMENT ON TABLE "public"."qrtz_job_details" IS '任务详细信息表';

-- ----------------------------
-- Records of qrtz_job_details
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_locks";
CREATE TABLE "public"."qrtz_locks" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
  "lock_name" varchar(40) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."qrtz_locks"."sched_name" IS '调度名称';
COMMENT ON COLUMN "public"."qrtz_locks"."lock_name" IS '悲观锁名称';
COMMENT ON TABLE "public"."qrtz_locks" IS '存储的悲观锁信息表';

-- ----------------------------
-- Records of qrtz_locks
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_paused_trigger_grps";
CREATE TABLE "public"."qrtz_paused_trigger_grps" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."qrtz_paused_trigger_grps"."sched_name" IS '调度名称';
COMMENT ON COLUMN "public"."qrtz_paused_trigger_grps"."trigger_group" IS 'qrtz_triggers表trigger_group的外键';
COMMENT ON TABLE "public"."qrtz_paused_trigger_grps" IS '暂停的触发器表';

-- ----------------------------
-- Records of qrtz_paused_trigger_grps
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_scheduler_state";
CREATE TABLE "public"."qrtz_scheduler_state" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
  "instance_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "last_checkin_time" int8 NOT NULL,
  "checkin_interval" int8 NOT NULL
)
;
COMMENT ON COLUMN "public"."qrtz_scheduler_state"."sched_name" IS '调度名称';
COMMENT ON COLUMN "public"."qrtz_scheduler_state"."instance_name" IS '实例名称';
COMMENT ON COLUMN "public"."qrtz_scheduler_state"."last_checkin_time" IS '上次检查时间';
COMMENT ON COLUMN "public"."qrtz_scheduler_state"."checkin_interval" IS '检查间隔时间';
COMMENT ON TABLE "public"."qrtz_scheduler_state" IS '调度器状态表';

-- ----------------------------
-- Records of qrtz_scheduler_state
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_simple_triggers";
CREATE TABLE "public"."qrtz_simple_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "repeat_count" int8 NOT NULL,
  "repeat_interval" int8 NOT NULL,
  "times_triggered" int8 NOT NULL
)
;
COMMENT ON COLUMN "public"."qrtz_simple_triggers"."sched_name" IS '调度名称';
COMMENT ON COLUMN "public"."qrtz_simple_triggers"."trigger_name" IS 'qrtz_triggers表trigger_name的外键';
COMMENT ON COLUMN "public"."qrtz_simple_triggers"."trigger_group" IS 'qrtz_triggers表trigger_group的外键';
COMMENT ON COLUMN "public"."qrtz_simple_triggers"."repeat_count" IS '重复的次数统计';
COMMENT ON COLUMN "public"."qrtz_simple_triggers"."repeat_interval" IS '重复的间隔时间';
COMMENT ON COLUMN "public"."qrtz_simple_triggers"."times_triggered" IS '已经触发的次数';
COMMENT ON TABLE "public"."qrtz_simple_triggers" IS '简单触发器的信息表';

-- ----------------------------
-- Records of qrtz_simple_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_simprop_triggers";
CREATE TABLE "public"."qrtz_simprop_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "str_prop_1" varchar(512) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "str_prop_2" varchar(512) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "str_prop_3" varchar(512) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "int_prop_1" int4,
  "int_prop_2" int4,
  "long_prop_1" int8,
  "long_prop_2" int8,
  "dec_prop_1" numeric(13,4) DEFAULT NULL::numeric,
  "dec_prop_2" numeric(13,4) DEFAULT NULL::numeric,
  "bool_prop_1" varchar(1) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "bool_prop_2" varchar(1) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."sched_name" IS '调度名称';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."trigger_name" IS 'qrtz_triggers表trigger_name的外键';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."trigger_group" IS 'qrtz_triggers表trigger_group的外键';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."str_prop_1" IS 'String类型的trigger的第一个参数';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."str_prop_2" IS 'String类型的trigger的第二个参数';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."str_prop_3" IS 'String类型的trigger的第三个参数';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."int_prop_1" IS 'int类型的trigger的第一个参数';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."int_prop_2" IS 'int类型的trigger的第二个参数';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."long_prop_1" IS 'long类型的trigger的第一个参数';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."long_prop_2" IS 'long类型的trigger的第二个参数';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."dec_prop_1" IS 'decimal类型的trigger的第一个参数';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."dec_prop_2" IS 'decimal类型的trigger的第二个参数';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."bool_prop_1" IS 'Boolean类型的trigger的第一个参数';
COMMENT ON COLUMN "public"."qrtz_simprop_triggers"."bool_prop_2" IS 'Boolean类型的trigger的第二个参数';
COMMENT ON TABLE "public"."qrtz_simprop_triggers" IS '同步机制的行锁表';

-- ----------------------------
-- Records of qrtz_simprop_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS "public"."qrtz_triggers";
CREATE TABLE "public"."qrtz_triggers" (
  "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "job_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "job_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(250) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "next_fire_time" int8,
  "prev_fire_time" int8,
  "priority" int4,
  "trigger_state" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
  "trigger_type" varchar(8) COLLATE "pg_catalog"."default" NOT NULL,
  "start_time" int8 NOT NULL,
  "end_time" int8,
  "calendar_name" varchar(200) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "misfire_instr" int2,
  "job_data" bytea
)
;
COMMENT ON COLUMN "public"."qrtz_triggers"."sched_name" IS '调度名称';
COMMENT ON COLUMN "public"."qrtz_triggers"."trigger_name" IS '触发器的名字';
COMMENT ON COLUMN "public"."qrtz_triggers"."trigger_group" IS '触发器所属组的名字';
COMMENT ON COLUMN "public"."qrtz_triggers"."job_name" IS 'qrtz_job_details表job_name的外键';
COMMENT ON COLUMN "public"."qrtz_triggers"."job_group" IS 'qrtz_job_details表job_group的外键';
COMMENT ON COLUMN "public"."qrtz_triggers"."description" IS '相关介绍';
COMMENT ON COLUMN "public"."qrtz_triggers"."next_fire_time" IS '上一次触发时间（毫秒）';
COMMENT ON COLUMN "public"."qrtz_triggers"."prev_fire_time" IS '下一次触发时间（默认为-1表示不触发）';
COMMENT ON COLUMN "public"."qrtz_triggers"."priority" IS '优先级';
COMMENT ON COLUMN "public"."qrtz_triggers"."trigger_state" IS '触发器状态';
COMMENT ON COLUMN "public"."qrtz_triggers"."trigger_type" IS '触发器的类型';
COMMENT ON COLUMN "public"."qrtz_triggers"."start_time" IS '开始时间';
COMMENT ON COLUMN "public"."qrtz_triggers"."end_time" IS '结束时间';
COMMENT ON COLUMN "public"."qrtz_triggers"."calendar_name" IS '日程表名称';
COMMENT ON COLUMN "public"."qrtz_triggers"."misfire_instr" IS '补偿执行的策略';
COMMENT ON COLUMN "public"."qrtz_triggers"."job_data" IS '存放持久化job对象';
COMMENT ON TABLE "public"."qrtz_triggers" IS '触发器详细信息表';

-- ----------------------------
-- Records of qrtz_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_config";
CREATE TABLE "public"."sys_config" (
  "config_id" int4 NOT NULL DEFAULT nextval('sys_config_config_id_seq'::regclass),
  "config_name" varchar(100) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "config_key" varchar(100) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "config_value" varchar(500) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "config_type" char(1) COLLATE "pg_catalog"."default" DEFAULT 'N'::bpchar,
  "create_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "update_time" timestamp(6),
  "remark" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."sys_config"."config_id" IS '参数主键';
COMMENT ON COLUMN "public"."sys_config"."config_name" IS '参数名称';
COMMENT ON COLUMN "public"."sys_config"."config_key" IS '参数键名';
COMMENT ON COLUMN "public"."sys_config"."config_value" IS '参数键值';
COMMENT ON COLUMN "public"."sys_config"."config_type" IS '系统内置（Y是 N否）';
COMMENT ON COLUMN "public"."sys_config"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."sys_config"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sys_config"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."sys_config"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."sys_config"."remark" IS '备注';
COMMENT ON TABLE "public"."sys_config" IS '参数配置表';

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO "public"."sys_config" VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2025-09-15 11:09:58', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO "public"."sys_config" VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2025-09-15 11:09:58', '', NULL, '初始化密码 123456');
INSERT INTO "public"."sys_config" VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2025-09-15 11:09:58', '', NULL, '深色主题theme-dark，浅色主题theme-light');
INSERT INTO "public"."sys_config" VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', '2025-09-15 11:09:58', 'admin', '2026-01-15 10:18:44', '是否开启验证码功能（true开启，false关闭）');
INSERT INTO "public"."sys_config" VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2025-09-15 11:09:58', '', NULL, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO "public"."sys_config" VALUES (6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2025-09-15 11:09:58', '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
INSERT INTO "public"."sys_config" VALUES (7, '用户管理-初始密码修改策略', 'sys.account.initPasswordModify', '1', 'Y', 'admin', '2025-09-15 11:09:58', '', NULL, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
INSERT INTO "public"."sys_config" VALUES (8, '用户管理-账号密码更新周期', 'sys.account.passwordValidateDays', '0', 'Y', 'admin', '2025-09-15 11:09:58', '', NULL, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');
INSERT INTO "public"."sys_config" VALUES (100, '设备自注册-开关', 'device.register.switch', 'true', 'Y', 'admin', '2026-01-18 01:08:27', 'admin', '2026-01-18 19:43:55', '用于全局开启/关闭设备自注册，修改之后十秒钟内生效');
INSERT INTO "public"."sys_config" VALUES (101, '优化-设备日志批量保存', 'device.log.batch', 'true,1000,10000', 'Y', 'admin', '2026-01-21 22:54:31', 'admin', '2026-01-22 20:34:22', '第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备日志并发高并且集中时缓解数据库压力，设备日志会先缓存，然后以每秒取固定数量数据批量插入数据库。数据量大时会导致插入数据可能会有一两秒的延迟，但不会影响实时数据以及告警等功能的实时性。酌情开启。');
INSERT INTO "public"."sys_config" VALUES (102, '优化-指令下发日志批量保存', 'device.function.batch', 'true,1000,10000', 'Y', 'admin', '2026-01-22 20:32:34', 'admin', '2026-01-22 20:36:14', '第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备指令下发并发高并且集中时缓解数据库压力，指令日志会先缓存，然后循环取数据批量插入数据库。数据量大时会导致插入数据可能会有一两秒的延迟。酌情开启。');
INSERT INTO "public"."sys_config" VALUES (103, '优化-告警日志批量保存', 'device.warn.batch', 'true,1000,10000', 'Y', 'admin', '2026-01-22 20:35:09', 'admin', '2026-01-23 14:37:55', '第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备告警并发高并且集中时缓解数据库压力，告警日志会先缓存，然后循环取数据批量插入数据库。数据量大时会导致插入数据可能会有一两秒的延迟。酌情开启。');
INSERT INTO "public"."sys_config" VALUES (104, '优化-设备联动告警日志批量保存', 'device.linkage.batch', 'true,1000,10000', 'Y', 'admin', '2026-01-22 20:49:06', 'admin', '2026-01-22 20:49:18', '第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备告警并发高并且集中时缓解数据库压力，告警日志会先缓存，然后循环取数据批量插入数据库。数据量大时会导致插入数据可能会有一两秒的延迟。酌情开启。');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_dept";
CREATE TABLE "public"."sys_dept" (
  "dept_id" int8 NOT NULL DEFAULT nextval('sys_dept_dept_id_seq'::regclass),
  "parent_id" int8 DEFAULT 0,
  "ancestors" varchar(50) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "dept_name" varchar(30) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "order_num" int4 DEFAULT 0,
  "leader" varchar(20) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "phone" varchar(11) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "email" varchar(50) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "status" char(1) COLLATE "pg_catalog"."default" DEFAULT '0'::bpchar,
  "del_flag" char(1) COLLATE "pg_catalog"."default" DEFAULT '0'::bpchar,
  "create_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "update_time" timestamp(6)
)
;
COMMENT ON COLUMN "public"."sys_dept"."dept_id" IS '部门id';
COMMENT ON COLUMN "public"."sys_dept"."parent_id" IS '父部门id';
COMMENT ON COLUMN "public"."sys_dept"."ancestors" IS '祖级列表';
COMMENT ON COLUMN "public"."sys_dept"."dept_name" IS '部门名称';
COMMENT ON COLUMN "public"."sys_dept"."order_num" IS '显示顺序';
COMMENT ON COLUMN "public"."sys_dept"."leader" IS '负责人';
COMMENT ON COLUMN "public"."sys_dept"."phone" IS '联系电话';
COMMENT ON COLUMN "public"."sys_dept"."email" IS '邮箱';
COMMENT ON COLUMN "public"."sys_dept"."status" IS '部门状态（0正常 1停用）';
COMMENT ON COLUMN "public"."sys_dept"."del_flag" IS '删除标志（0代表存在 2代表删除）';
COMMENT ON COLUMN "public"."sys_dept"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."sys_dept"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sys_dept"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."sys_dept"."update_time" IS '更新时间';
COMMENT ON TABLE "public"."sys_dept" IS '部门表';

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO "public"."sys_dept" VALUES (100, 0, '0', '科技', 0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', 'admin', '2026-01-16 23:22:30');
INSERT INTO "public"."sys_dept" VALUES (101, 100, '0,100', '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO "public"."sys_dept" VALUES (102, 100, '0,100', '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO "public"."sys_dept" VALUES (103, 101, '0,100,101', '研发部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO "public"."sys_dept" VALUES (104, 101, '0,100,101', '市场部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO "public"."sys_dept" VALUES (105, 101, '0,100,101', '测试部门', 3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO "public"."sys_dept" VALUES (106, 101, '0,100,101', '财务部门', 4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO "public"."sys_dept" VALUES (107, 101, '0,100,101', '运维部门', 5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO "public"."sys_dept" VALUES (108, 102, '0,100,102', '市场部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO "public"."sys_dept" VALUES (109, 102, '0,100,102', '财务部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO "public"."sys_dept" VALUES (200, 100, '0,100', '厦门市', 3, '张三', '13129405840', NULL, '0', '0', 'admin', '2026-01-16 23:27:08', '', NULL);
INSERT INTO "public"."sys_dept" VALUES (201, 200, '0,100,200', '翔安医院', 1, NULL, NULL, NULL, '0', '0', 'admin', '2026-01-16 23:27:28', '', NULL);
INSERT INTO "public"."sys_dept" VALUES (202, 200, '0,100,200', '思明医院', 2, NULL, NULL, NULL, '0', '0', 'admin', '2026-01-16 23:27:41', '', NULL);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_dict_data";
CREATE TABLE "public"."sys_dict_data" (
  "dict_code" int8 NOT NULL DEFAULT nextval('sys_dict_data_dict_code_seq'::regclass),
  "dict_sort" int4 DEFAULT 0,
  "dict_label" varchar(100) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "dict_value" varchar(100) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "dict_type" varchar(100) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "css_class" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "list_class" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "is_default" char(1) COLLATE "pg_catalog"."default" DEFAULT 'N'::bpchar,
  "status" char(1) COLLATE "pg_catalog"."default" DEFAULT '0'::bpchar,
  "create_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "update_time" timestamp(6),
  "remark" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."sys_dict_data"."dict_code" IS '字典编码';
COMMENT ON COLUMN "public"."sys_dict_data"."dict_sort" IS '字典排序';
COMMENT ON COLUMN "public"."sys_dict_data"."dict_label" IS '字典标签';
COMMENT ON COLUMN "public"."sys_dict_data"."dict_value" IS '字典键值';
COMMENT ON COLUMN "public"."sys_dict_data"."dict_type" IS '字典类型';
COMMENT ON COLUMN "public"."sys_dict_data"."css_class" IS '样式属性（其他样式扩展）';
COMMENT ON COLUMN "public"."sys_dict_data"."list_class" IS '表格回显样式';
COMMENT ON COLUMN "public"."sys_dict_data"."is_default" IS '是否默认（Y是 N否）';
COMMENT ON COLUMN "public"."sys_dict_data"."status" IS '状态（0正常 1停用）';
COMMENT ON COLUMN "public"."sys_dict_data"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."sys_dict_data"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sys_dict_data"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."sys_dict_data"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."sys_dict_data"."remark" IS '备注';
COMMENT ON TABLE "public"."sys_dict_data" IS '字典数据表';

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO "public"."sys_dict_data" VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '性别男');
INSERT INTO "public"."sys_dict_data" VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '性别女');
INSERT INTO "public"."sys_dict_data" VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '性别未知');
INSERT INTO "public"."sys_dict_data" VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '显示菜单');
INSERT INTO "public"."sys_dict_data" VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '隐藏菜单');
INSERT INTO "public"."sys_dict_data" VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '正常状态');
INSERT INTO "public"."sys_dict_data" VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '停用状态');
INSERT INTO "public"."sys_dict_data" VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '正常状态');
INSERT INTO "public"."sys_dict_data" VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '停用状态');
INSERT INTO "public"."sys_dict_data" VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '默认分组');
INSERT INTO "public"."sys_dict_data" VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '系统分组');
INSERT INTO "public"."sys_dict_data" VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '系统默认是');
INSERT INTO "public"."sys_dict_data" VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '系统默认否');
INSERT INTO "public"."sys_dict_data" VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '通知');
INSERT INTO "public"."sys_dict_data" VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '公告');
INSERT INTO "public"."sys_dict_data" VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '正常状态');
INSERT INTO "public"."sys_dict_data" VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '关闭状态');
INSERT INTO "public"."sys_dict_data" VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '其他操作');
INSERT INTO "public"."sys_dict_data" VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '新增操作');
INSERT INTO "public"."sys_dict_data" VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '修改操作');
INSERT INTO "public"."sys_dict_data" VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '删除操作');
INSERT INTO "public"."sys_dict_data" VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '授权操作');
INSERT INTO "public"."sys_dict_data" VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '导出操作');
INSERT INTO "public"."sys_dict_data" VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '导入操作');
INSERT INTO "public"."sys_dict_data" VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '强退操作');
INSERT INTO "public"."sys_dict_data" VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '生成操作');
INSERT INTO "public"."sys_dict_data" VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '清空操作');
INSERT INTO "public"."sys_dict_data" VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '正常状态');
INSERT INTO "public"."sys_dict_data" VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '停用状态');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_dict_type";
CREATE TABLE "public"."sys_dict_type" (
  "dict_id" int8 NOT NULL DEFAULT nextval('sys_dict_type_dict_id_seq'::regclass),
  "dict_name" varchar(100) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "dict_type" varchar(100) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "status" char(1) COLLATE "pg_catalog"."default" DEFAULT '0'::bpchar,
  "create_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "update_time" timestamp(6),
  "remark" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."sys_dict_type"."dict_id" IS '字典主键';
COMMENT ON COLUMN "public"."sys_dict_type"."dict_name" IS '字典名称';
COMMENT ON COLUMN "public"."sys_dict_type"."dict_type" IS '字典类型';
COMMENT ON COLUMN "public"."sys_dict_type"."status" IS '状态（0正常 1停用）';
COMMENT ON COLUMN "public"."sys_dict_type"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."sys_dict_type"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sys_dict_type"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."sys_dict_type"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."sys_dict_type"."remark" IS '备注';
COMMENT ON TABLE "public"."sys_dict_type" IS '字典类型表';

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO "public"."sys_dict_type" VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '用户性别列表');
INSERT INTO "public"."sys_dict_type" VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2025-09-15 11:09:58', 'admin', '2026-01-02 23:06:05', '菜单状态列表');
INSERT INTO "public"."sys_dict_type" VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '系统开关列表');
INSERT INTO "public"."sys_dict_type" VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '任务状态列表');
INSERT INTO "public"."sys_dict_type" VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '任务分组列表');
INSERT INTO "public"."sys_dict_type" VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '系统是否列表');
INSERT INTO "public"."sys_dict_type" VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '通知类型列表');
INSERT INTO "public"."sys_dict_type" VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '通知状态列表');
INSERT INTO "public"."sys_dict_type" VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '操作类型列表');
INSERT INTO "public"."sys_dict_type" VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '登录状态列表');

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_job";
CREATE TABLE "public"."sys_job" (
  "job_id" int8 NOT NULL DEFAULT nextval('sys_job_job_id_seq'::regclass),
  "job_name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "job_group" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'DEFAULT'::character varying,
  "invoke_target" varchar(500) COLLATE "pg_catalog"."default" NOT NULL,
  "cron_expression" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "misfire_policy" varchar(20) COLLATE "pg_catalog"."default" DEFAULT '3'::character varying,
  "concurrent" char(1) COLLATE "pg_catalog"."default" DEFAULT '1'::bpchar,
  "status" char(1) COLLATE "pg_catalog"."default" DEFAULT '0'::bpchar,
  "create_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "update_time" timestamp(6),
  "remark" varchar(500) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."sys_job"."job_id" IS '任务ID';
COMMENT ON COLUMN "public"."sys_job"."job_name" IS '任务名称';
COMMENT ON COLUMN "public"."sys_job"."job_group" IS '任务组名';
COMMENT ON COLUMN "public"."sys_job"."invoke_target" IS '调用目标字符串';
COMMENT ON COLUMN "public"."sys_job"."cron_expression" IS 'cron执行表达式';
COMMENT ON COLUMN "public"."sys_job"."misfire_policy" IS '计划执行错误策略（1立即执行 2执行一次 3放弃执行）';
COMMENT ON COLUMN "public"."sys_job"."concurrent" IS '是否并发执行（0允许 1禁止）';
COMMENT ON COLUMN "public"."sys_job"."status" IS '状态（0正常 1暂停）';
COMMENT ON COLUMN "public"."sys_job"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."sys_job"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sys_job"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."sys_job"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."sys_job"."remark" IS '备注信息';
COMMENT ON TABLE "public"."sys_job" IS '定时任务调度表';

-- ----------------------------
-- Records of sys_job
-- ----------------------------
INSERT INTO "public"."sys_job" VALUES (1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams', '0/10 * * * * ?', '3', '1', '1', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_job" VALUES (2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(''ry'')', '0/15 * * * * ?', '3', '1', '1', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_job" VALUES (3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(''ry'', true, 2000L, 316.50D, 100)', '0/20 * * * * ?', '3', '1', '1', 'admin', '2025-09-15 11:09:58', '', '2026-01-23 15:09:27', '');

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_job_log";
CREATE TABLE "public"."sys_job_log" (
  "job_log_id" int8 NOT NULL DEFAULT nextval('sys_job_log_job_log_id_seq'::regclass),
  "job_name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "job_group" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "invoke_target" varchar(500) COLLATE "pg_catalog"."default" NOT NULL,
  "job_message" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "status" char(1) COLLATE "pg_catalog"."default" DEFAULT '0'::bpchar,
  "exception_info" varchar(2000) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "create_time" timestamp(6)
)
;
COMMENT ON COLUMN "public"."sys_job_log"."job_log_id" IS '任务日志ID';
COMMENT ON COLUMN "public"."sys_job_log"."job_name" IS '任务名称';
COMMENT ON COLUMN "public"."sys_job_log"."job_group" IS '任务组名';
COMMENT ON COLUMN "public"."sys_job_log"."invoke_target" IS '调用目标字符串';
COMMENT ON COLUMN "public"."sys_job_log"."job_message" IS '日志信息';
COMMENT ON COLUMN "public"."sys_job_log"."status" IS '执行状态（0正常 1失败）';
COMMENT ON COLUMN "public"."sys_job_log"."exception_info" IS '异常信息';
COMMENT ON COLUMN "public"."sys_job_log"."create_time" IS '创建时间';
COMMENT ON TABLE "public"."sys_job_log" IS '定时任务调度日志表';

-- ----------------------------
-- Records of sys_job_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_logininfor";
CREATE TABLE "public"."sys_logininfor" (
  "info_id" int8 NOT NULL DEFAULT nextval('sys_logininfor_info_id_seq'::regclass),
  "user_name" varchar(50) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "ipaddr" varchar(128) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "login_location" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "browser" varchar(50) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "os" varchar(50) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "status" char(1) COLLATE "pg_catalog"."default" DEFAULT '0'::bpchar,
  "msg" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "login_time" timestamp(6)
)
;
COMMENT ON COLUMN "public"."sys_logininfor"."info_id" IS '访问ID';
COMMENT ON COLUMN "public"."sys_logininfor"."user_name" IS '用户账号';
COMMENT ON COLUMN "public"."sys_logininfor"."ipaddr" IS '登录IP地址';
COMMENT ON COLUMN "public"."sys_logininfor"."login_location" IS '登录地点';
COMMENT ON COLUMN "public"."sys_logininfor"."browser" IS '浏览器类型';
COMMENT ON COLUMN "public"."sys_logininfor"."os" IS '操作系统';
COMMENT ON COLUMN "public"."sys_logininfor"."status" IS '登录状态（0成功 1失败）';
COMMENT ON COLUMN "public"."sys_logininfor"."msg" IS '提示消息';
COMMENT ON COLUMN "public"."sys_logininfor"."login_time" IS '访问时间';
COMMENT ON TABLE "public"."sys_logininfor" IS '系统访问记录';

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------
INSERT INTO "public"."sys_logininfor" VALUES (100, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-18 11:50:22');
INSERT INTO "public"."sys_logininfor" VALUES (101, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2025-09-18 13:40:20');
INSERT INTO "public"."sys_logininfor" VALUES (102, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-18 13:40:27');
INSERT INTO "public"."sys_logininfor" VALUES (103, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-09-18 13:40:51');
INSERT INTO "public"."sys_logininfor" VALUES (104, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-18 13:40:53');
INSERT INTO "public"."sys_logininfor" VALUES (105, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-09-18 13:59:02');
INSERT INTO "public"."sys_logininfor" VALUES (106, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-18 13:59:05');
INSERT INTO "public"."sys_logininfor" VALUES (107, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-18 17:10:06');
INSERT INTO "public"."sys_logininfor" VALUES (108, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-20 22:37:41');
INSERT INTO "public"."sys_logininfor" VALUES (109, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-20 22:38:06');
INSERT INTO "public"."sys_logininfor" VALUES (110, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-20 22:47:44');
INSERT INTO "public"."sys_logininfor" VALUES (111, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-21 16:43:30');
INSERT INTO "public"."sys_logininfor" VALUES (112, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-21 22:27:24');
INSERT INTO "public"."sys_logininfor" VALUES (113, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-22 10:38:55');
INSERT INTO "public"."sys_logininfor" VALUES (114, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-22 15:41:26');
INSERT INTO "public"."sys_logininfor" VALUES (115, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-22 20:46:45');
INSERT INTO "public"."sys_logininfor" VALUES (116, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-23 09:23:02');
INSERT INTO "public"."sys_logininfor" VALUES (117, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-09-23 10:16:26');
INSERT INTO "public"."sys_logininfor" VALUES (118, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-23 10:16:28');
INSERT INTO "public"."sys_logininfor" VALUES (119, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-23 11:38:52');
INSERT INTO "public"."sys_logininfor" VALUES (120, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-23 13:35:45');
INSERT INTO "public"."sys_logininfor" VALUES (121, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-23 13:40:14');
INSERT INTO "public"."sys_logininfor" VALUES (122, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-09-24 09:07:19');
INSERT INTO "public"."sys_logininfor" VALUES (123, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-03 14:14:14');
INSERT INTO "public"."sys_logininfor" VALUES (124, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-08 23:07:10');
INSERT INTO "public"."sys_logininfor" VALUES (125, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-11 09:31:29');
INSERT INTO "public"."sys_logininfor" VALUES (126, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-13 09:14:01');
INSERT INTO "public"."sys_logininfor" VALUES (127, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-14 09:53:49');
INSERT INTO "public"."sys_logininfor" VALUES (128, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-10-14 09:54:59');
INSERT INTO "public"."sys_logininfor" VALUES (129, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-14 09:55:01');
INSERT INTO "public"."sys_logininfor" VALUES (130, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-15 15:09:36');
INSERT INTO "public"."sys_logininfor" VALUES (131, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-18 09:43:06');
INSERT INTO "public"."sys_logininfor" VALUES (132, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-20 09:47:57');
INSERT INTO "public"."sys_logininfor" VALUES (133, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-21 09:38:40');
INSERT INTO "public"."sys_logininfor" VALUES (134, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-22 09:17:26');
INSERT INTO "public"."sys_logininfor" VALUES (135, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-23 10:23:42');
INSERT INTO "public"."sys_logininfor" VALUES (136, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-24 17:00:53');
INSERT INTO "public"."sys_logininfor" VALUES (137, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-25 10:37:04');
INSERT INTO "public"."sys_logininfor" VALUES (138, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-28 21:15:35');
INSERT INTO "public"."sys_logininfor" VALUES (139, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-10-28 21:55:08');
INSERT INTO "public"."sys_logininfor" VALUES (140, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-28 21:56:41');
INSERT INTO "public"."sys_logininfor" VALUES (141, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-10-28 21:56:41');
INSERT INTO "public"."sys_logininfor" VALUES (142, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-28 21:56:48');
INSERT INTO "public"."sys_logininfor" VALUES (143, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-10-28 21:56:48');
INSERT INTO "public"."sys_logininfor" VALUES (144, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-28 21:57:28');
INSERT INTO "public"."sys_logininfor" VALUES (145, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-10-28 21:57:28');
INSERT INTO "public"."sys_logininfor" VALUES (146, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-28 21:58:50');
INSERT INTO "public"."sys_logininfor" VALUES (147, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-10-28 21:58:50');
INSERT INTO "public"."sys_logininfor" VALUES (148, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-28 22:02:37');
INSERT INTO "public"."sys_logininfor" VALUES (149, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-10-28 22:02:37');
INSERT INTO "public"."sys_logininfor" VALUES (150, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-28 22:03:00');
INSERT INTO "public"."sys_logininfor" VALUES (151, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-10-28 22:03:01');
INSERT INTO "public"."sys_logininfor" VALUES (152, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-28 22:03:15');
INSERT INTO "public"."sys_logininfor" VALUES (153, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-10-28 22:03:15');
INSERT INTO "public"."sys_logininfor" VALUES (154, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-28 22:04:17');
INSERT INTO "public"."sys_logininfor" VALUES (155, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-10-28 22:04:18');
INSERT INTO "public"."sys_logininfor" VALUES (156, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-28 22:04:30');
INSERT INTO "public"."sys_logininfor" VALUES (157, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-29 10:03:23');
INSERT INTO "public"."sys_logininfor" VALUES (158, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-10-29 10:26:15');
INSERT INTO "public"."sys_logininfor" VALUES (159, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-29 10:40:42');
INSERT INTO "public"."sys_logininfor" VALUES (160, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-10-29 10:40:49');
INSERT INTO "public"."sys_logininfor" VALUES (161, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-29 11:02:36');
INSERT INTO "public"."sys_logininfor" VALUES (162, 'admin', '125.80.201.66', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-30 09:13:26');
INSERT INTO "public"."sys_logininfor" VALUES (163, 'admin', '125.80.201.66', 'XX XX', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-10-30 10:31:45');
INSERT INTO "public"."sys_logininfor" VALUES (164, 'admin', '125.80.201.66', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-30 10:32:27');
INSERT INTO "public"."sys_logininfor" VALUES (165, 'admin', '125.80.201.66', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-30 11:06:52');
INSERT INTO "public"."sys_logininfor" VALUES (166, 'admin', '125.80.201.66', 'XX XX', 'Firefox 14', 'Windows 10', '0', '登录成功', '2025-10-30 11:17:58');
INSERT INTO "public"."sys_logininfor" VALUES (167, 'admin', '125.80.201.66', 'XX XX', 'Firefox 14', 'Windows 10', '0', '登录成功', '2025-10-30 14:36:14');
INSERT INTO "public"."sys_logininfor" VALUES (168, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-30 19:56:33');
INSERT INTO "public"."sys_logininfor" VALUES (169, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-10-30 19:56:33');
INSERT INTO "public"."sys_logininfor" VALUES (170, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-30 19:56:39');
INSERT INTO "public"."sys_logininfor" VALUES (171, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-10-30 19:56:39');
INSERT INTO "public"."sys_logininfor" VALUES (172, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-30 19:56:45');
INSERT INTO "public"."sys_logininfor" VALUES (173, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-10-30 19:56:45');
INSERT INTO "public"."sys_logininfor" VALUES (174, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-30 19:57:17');
INSERT INTO "public"."sys_logininfor" VALUES (175, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-10-30 19:57:17');
INSERT INTO "public"."sys_logininfor" VALUES (176, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-30 19:57:34');
INSERT INTO "public"."sys_logininfor" VALUES (177, 'admin', '125.80.201.66', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-10-31 09:05:36');
INSERT INTO "public"."sys_logininfor" VALUES (178, 'admin', '180.136.232.8', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2025-10-31 10:02:10');
INSERT INTO "public"."sys_logininfor" VALUES (179, 'admin', '113.249.34.82', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-31 15:07:13');
INSERT INTO "public"."sys_logininfor" VALUES (180, 'admin', '113.251.69.46', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-11-01 09:12:56');
INSERT INTO "public"."sys_logininfor" VALUES (181, 'admin', '183.226.251.40', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-11-01 15:29:21');
INSERT INTO "public"."sys_logininfor" VALUES (182, 'admin', '106.92.105.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-02 12:53:23');
INSERT INTO "public"."sys_logininfor" VALUES (183, 'admin', '119.84.70.238', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-03 09:00:57');
INSERT INTO "public"."sys_logininfor" VALUES (184, 'admin', '113.248.184.212', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-11-03 10:32:40');
INSERT INTO "public"."sys_logininfor" VALUES (185, 'admin', '120.229.26.138', 'XX XX', 'Chrome 11', 'Windows 10', '0', '登录成功', '2025-11-03 11:22:59');
INSERT INTO "public"."sys_logininfor" VALUES (186, 'admin', '116.22.147.17', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-03 17:09:58');
INSERT INTO "public"."sys_logininfor" VALUES (187, 'admin', '112.97.82.102', 'XX XX', 'Chrome Mobile', 'Android 1.x', '0', '登录成功', '2025-11-03 17:33:12');
INSERT INTO "public"."sys_logininfor" VALUES (188, 'admin', '113.90.234.90', 'XX XX', 'Chrome Mobile', 'Android 1.x', '0', '登录成功', '2025-11-03 17:34:01');
INSERT INTO "public"."sys_logininfor" VALUES (189, 'admin', '113.90.234.90', 'XX XX', 'Chrome Mobile', 'Android 1.x', '0', '登录成功', '2025-11-03 17:38:51');
INSERT INTO "public"."sys_logininfor" VALUES (190, 'admin', '113.248.184.212', 'XX XX', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-11-03 18:11:41');
INSERT INTO "public"."sys_logininfor" VALUES (191, 'admin', '106.92.195.232', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-03 21:03:33');
INSERT INTO "public"."sys_logininfor" VALUES (192, 'admin', '120.229.26.138', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-04 10:12:11');
INSERT INTO "public"."sys_logininfor" VALUES (193, 'admin', '113.251.93.216', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-11-06 10:27:58');
INSERT INTO "public"."sys_logininfor" VALUES (194, 'admin', '219.147.29.114', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-06 11:25:04');
INSERT INTO "public"."sys_logininfor" VALUES (195, 'admin', '180.159.42.92', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-11-06 11:32:13');
INSERT INTO "public"."sys_logininfor" VALUES (196, 'admin', '113.56.181.228', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-06 17:16:59');
INSERT INTO "public"."sys_logininfor" VALUES (197, 'admin', '120.41.149.5', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-06 19:28:52');
INSERT INTO "public"."sys_logininfor" VALUES (198, 'admin', '113.249.9.53', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-07 09:12:58');
INSERT INTO "public"."sys_logininfor" VALUES (199, 'admin', '113.56.181.226', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-07 11:10:00');
INSERT INTO "public"."sys_logininfor" VALUES (200, 'admin', '112.10.138.136', 'XX XX', 'Chrome Mobile', 'Android 1.x', '0', '登录成功', '2025-11-08 14:55:35');
INSERT INTO "public"."sys_logininfor" VALUES (201, 'admin', '112.10.138.136', 'XX XX', 'Chrome Mobile', 'Android 1.x', '0', '登录成功', '2025-11-08 14:56:25');
INSERT INTO "public"."sys_logininfor" VALUES (202, 'admin', '117.129.14.94', 'XX XX', 'Chrome Mobile', 'Android 1.x', '0', '登录成功', '2025-11-08 16:35:36');
INSERT INTO "public"."sys_logininfor" VALUES (203, 'admin', '120.228.96.95', 'XX XX', 'Firefox 14', 'Windows 10', '0', '登录成功', '2025-11-09 14:50:43');
INSERT INTO "public"."sys_logininfor" VALUES (204, 'admin', '113.56.181.226', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-10 09:18:28');
INSERT INTO "public"."sys_logininfor" VALUES (205, 'admin', '125.80.220.211', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-10 10:55:19');
INSERT INTO "public"."sys_logininfor" VALUES (206, 'admin', '117.129.14.94', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-11-10 15:09:17');
INSERT INTO "public"."sys_logininfor" VALUES (207, 'admin', '112.117.231.140', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-10 19:29:23');
INSERT INTO "public"."sys_logininfor" VALUES (208, 'admin', '125.80.205.8', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-12 09:07:32');
INSERT INTO "public"."sys_logininfor" VALUES (209, 'admin', '125.80.205.8', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-12 16:18:33');
INSERT INTO "public"."sys_logininfor" VALUES (210, 'admin', '14.19.74.240', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-11-12 17:24:04');
INSERT INTO "public"."sys_logininfor" VALUES (211, 'admin', '113.251.75.185', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-13 09:56:21');
INSERT INTO "public"."sys_logininfor" VALUES (212, 'admin', '113.251.75.185', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-13 18:02:39');
INSERT INTO "public"."sys_logininfor" VALUES (213, 'admin', '171.221.106.248', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功', '2025-11-14 09:22:24');
INSERT INTO "public"."sys_logininfor" VALUES (214, 'admin', '113.251.75.185', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-14 09:34:33');
INSERT INTO "public"."sys_logininfor" VALUES (215, 'admin', '223.152.219.181', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-14 13:46:16');
INSERT INTO "public"."sys_logininfor" VALUES (216, 'admin', '112.32.71.216', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-14 20:20:32');
INSERT INTO "public"."sys_logininfor" VALUES (217, 'admin', '113.249.25.20', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-15 09:22:17');
INSERT INTO "public"."sys_logininfor" VALUES (218, 'admin', '175.152.52.222', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-16 11:10:45');
INSERT INTO "public"."sys_logininfor" VALUES (219, 'admin', '101.204.144.96', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-16 22:01:46');
INSERT INTO "public"."sys_logininfor" VALUES (220, 'admin', '113.248.168.213', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-17 09:29:22');
INSERT INTO "public"."sys_logininfor" VALUES (221, 'admin', '14.19.5.203', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-11-17 09:44:12');
INSERT INTO "public"."sys_logininfor" VALUES (222, 'admin', '221.193.216.171', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-11-17 14:50:53');
INSERT INTO "public"."sys_logininfor" VALUES (223, 'admin', '113.248.168.213', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-17 16:29:39');
INSERT INTO "public"."sys_logininfor" VALUES (224, 'admin', '182.116.17.61', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-17 23:42:54');
INSERT INTO "public"."sys_logininfor" VALUES (225, 'admin', '222.75.162.82', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-11-18 08:50:10');
INSERT INTO "public"."sys_logininfor" VALUES (226, 'admin', '113.248.168.213', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-18 09:43:56');
INSERT INTO "public"."sys_logininfor" VALUES (227, 'admin', '218.56.36.99', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-11-18 13:43:29');
INSERT INTO "public"."sys_logininfor" VALUES (228, 'admin', '123.139.22.95', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-18 16:51:41');
INSERT INTO "public"."sys_logininfor" VALUES (229, 'admin', '113.251.92.141', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-19 10:20:30');
INSERT INTO "public"."sys_logininfor" VALUES (230, 'admin', '123.150.93.26', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-19 17:53:19');
INSERT INTO "public"."sys_logininfor" VALUES (231, 'admin', '113.251.92.141', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-20 10:59:42');
INSERT INTO "public"."sys_logininfor" VALUES (232, 'admin', '223.88.94.151', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-20 15:04:27');
INSERT INTO "public"."sys_logininfor" VALUES (233, 'admin', '223.88.94.151', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-20 15:20:15');
INSERT INTO "public"."sys_logininfor" VALUES (234, 'admin', '115.57.8.132', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-21 09:04:03');
INSERT INTO "public"."sys_logininfor" VALUES (235, 'admin', '113.248.180.166', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-21 11:05:22');
INSERT INTO "public"."sys_logininfor" VALUES (236, 'admin', '61.52.175.172', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-11-21 13:44:52');
INSERT INTO "public"."sys_logininfor" VALUES (237, 'admin', '111.19.71.215', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-11-21 21:05:50');
INSERT INTO "public"."sys_logininfor" VALUES (238, 'admin', '106.92.142.163', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-22 19:25:28');
INSERT INTO "public"."sys_logininfor" VALUES (239, 'admin', '106.92.142.163', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-23 00:24:13');
INSERT INTO "public"."sys_logininfor" VALUES (240, 'admin', '113.249.18.3', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-24 09:46:21');
INSERT INTO "public"."sys_logininfor" VALUES (241, 'admin', '122.224.179.172', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-11-24 14:55:11');
INSERT INTO "public"."sys_logininfor" VALUES (242, 'admin', '125.80.212.80', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-25 09:44:14');
INSERT INTO "public"."sys_logininfor" VALUES (243, 'admin', '27.216.199.167', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-11-25 13:06:15');
INSERT INTO "public"."sys_logininfor" VALUES (244, 'admin', '113.249.239.242', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-26 17:37:36');
INSERT INTO "public"."sys_logininfor" VALUES (245, 'admin', '113.116.119.0', 'XX XX', 'Chrome 11', 'Windows 10', '0', '登录成功', '2025-11-26 23:23:18');
INSERT INTO "public"."sys_logininfor" VALUES (246, 'admin', '113.120.113.173', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-27 09:14:21');
INSERT INTO "public"."sys_logininfor" VALUES (247, 'admin', '113.249.239.242', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-27 10:58:03');
INSERT INTO "public"."sys_logininfor" VALUES (248, 'admin', '117.10.36.33', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-11-27 16:58:33');
INSERT INTO "public"."sys_logininfor" VALUES (249, 'admin', '113.249.239.242', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-28 10:54:45');
INSERT INTO "public"."sys_logininfor" VALUES (250, 'admin', '124.239.172.189', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-28 13:50:42');
INSERT INTO "public"."sys_logininfor" VALUES (251, 'admin', '58.19.17.131', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-11-28 16:16:50');
INSERT INTO "public"."sys_logininfor" VALUES (252, 'admin', '125.80.153.136', 'XX XX', 'Apple WebKit', 'Mac OS X (iPhone)', '0', '登录成功', '2025-11-28 17:11:55');
INSERT INTO "public"."sys_logininfor" VALUES (253, 'admin', '115.56.30.22', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-11-29 11:38:02');
INSERT INTO "public"."sys_logininfor" VALUES (254, 'admin', '106.92.146.99', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-30 01:28:21');
INSERT INTO "public"."sys_logininfor" VALUES (255, 'admin', '61.160.106.90', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-01 09:51:13');
INSERT INTO "public"."sys_logininfor" VALUES (256, 'admin', '14.145.15.87', 'XX XX', 'Safari', 'Mac OS X', '0', '登录成功', '2025-12-01 09:55:28');
INSERT INTO "public"."sys_logininfor" VALUES (257, 'admin', '36.101.175.154', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-01 10:59:16');
INSERT INTO "public"."sys_logininfor" VALUES (258, 'admin', '112.230.171.126', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-01 11:13:07');
INSERT INTO "public"."sys_logininfor" VALUES (259, 'admin', '113.250.144.137', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-01 13:44:30');
INSERT INTO "public"."sys_logininfor" VALUES (260, 'admin', '222.211.206.124', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-01 14:32:05');
INSERT INTO "public"."sys_logininfor" VALUES (261, 'admin', '113.250.144.137', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-12-01 15:28:02');
INSERT INTO "public"."sys_logininfor" VALUES (262, 'admin', '113.250.144.137', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-01 15:28:30');
INSERT INTO "public"."sys_logininfor" VALUES (263, 'admin', '106.84.61.120', 'XX XX', 'Firefox 14', 'Windows 10', '0', '登录成功', '2025-12-01 16:23:48');
INSERT INTO "public"."sys_logininfor" VALUES (264, 'admin', '14.105.63.163', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-01 16:25:21');
INSERT INTO "public"."sys_logininfor" VALUES (265, 'admin', '106.84.61.120', 'XX XX', 'Firefox 14', 'Windows 10', '0', '登录成功', '2025-12-01 16:25:29');
INSERT INTO "public"."sys_logininfor" VALUES (266, 'admin', '124.72.57.215', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-01 17:29:19');
INSERT INTO "public"."sys_logininfor" VALUES (267, 'admin', '222.211.206.124', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-01 18:30:08');
INSERT INTO "public"."sys_logininfor" VALUES (268, 'admin', '106.92.193.49', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-01 21:11:54');
INSERT INTO "public"."sys_logininfor" VALUES (269, 'admin', '113.250.144.137', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-02 09:09:03');
INSERT INTO "public"."sys_logininfor" VALUES (270, 'admin', '183.56.181.60', 'XX XX', 'Firefox 14', 'Windows 10', '0', '登录成功', '2025-12-02 10:02:38');
INSERT INTO "public"."sys_logininfor" VALUES (271, 'admin', '60.1.93.3', 'XX XX', 'Safari', 'Mac OS X', '0', '登录成功', '2025-12-02 10:14:02');
INSERT INTO "public"."sys_logininfor" VALUES (272, 'admin', '218.88.71.60', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-02 11:14:17');
INSERT INTO "public"."sys_logininfor" VALUES (273, 'admin', '122.4.236.110', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-02 16:37:53');
INSERT INTO "public"."sys_logininfor" VALUES (274, 'admin', '123.14.78.172', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-03 08:35:33');
INSERT INTO "public"."sys_logininfor" VALUES (275, 'admin', '58.63.47.115', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-03 10:02:43');
INSERT INTO "public"."sys_logininfor" VALUES (276, 'admin', '113.248.168.215', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-03 10:35:25');
INSERT INTO "public"."sys_logininfor" VALUES (277, 'admin', '36.249.149.4', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-03 12:13:47');
INSERT INTO "public"."sys_logininfor" VALUES (278, 'admin', '36.248.206.73', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-03 12:40:10');
INSERT INTO "public"."sys_logininfor" VALUES (279, 'admin', '220.188.160.194', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-03 13:47:21');
INSERT INTO "public"."sys_logininfor" VALUES (280, 'admin', '183.17.49.124', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-03 14:02:08');
INSERT INTO "public"."sys_logininfor" VALUES (281, 'admin', '125.37.29.27', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-03 17:43:25');
INSERT INTO "public"."sys_logininfor" VALUES (282, 'admin', '222.82.253.130', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-03 18:46:36');
INSERT INTO "public"."sys_logininfor" VALUES (283, 'admin', '106.92.193.49', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-03 20:30:34');
INSERT INTO "public"."sys_logininfor" VALUES (284, 'admin', '36.248.206.73', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-03 21:19:21');
INSERT INTO "public"."sys_logininfor" VALUES (285, 'admin', '223.64.21.28', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-04 09:01:39');
INSERT INTO "public"."sys_logininfor" VALUES (286, 'admin', '113.248.168.215', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-04 09:26:35');
INSERT INTO "public"."sys_logininfor" VALUES (287, 'admin', '58.247.152.210', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-04 10:10:45');
INSERT INTO "public"."sys_logininfor" VALUES (288, 'admin', '183.17.49.124', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-04 15:40:04');
INSERT INTO "public"."sys_logininfor" VALUES (289, 'admin', '36.45.244.173', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-04 16:50:23');
INSERT INTO "public"."sys_logininfor" VALUES (290, 'admin', '106.92.193.49', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-04 20:46:29');
INSERT INTO "public"."sys_logininfor" VALUES (291, 'admin', '111.183.5.98', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-05 09:55:39');
INSERT INTO "public"."sys_logininfor" VALUES (292, 'admin', '113.248.175.180', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-05 10:02:00');
INSERT INTO "public"."sys_logininfor" VALUES (293, 'admin', '112.48.20.194', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-05 10:34:02');
INSERT INTO "public"."sys_logininfor" VALUES (294, 'admin', '39.82.225.206', 'XX XX', 'Chrome 11', 'Windows 10', '0', '登录成功', '2025-12-05 13:15:44');
INSERT INTO "public"."sys_logininfor" VALUES (295, 'admin', '59.57.238.138', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-05 14:09:46');
INSERT INTO "public"."sys_logininfor" VALUES (296, 'admin', '218.56.105.222', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-05 14:30:18');
INSERT INTO "public"."sys_logininfor" VALUES (297, 'admin', '218.94.38.115', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-05 14:37:04');
INSERT INTO "public"."sys_logininfor" VALUES (298, 'admin', '61.240.225.76', 'XX XX', 'Firefox 14', 'Windows 10', '0', '登录成功', '2025-12-05 15:02:39');
INSERT INTO "public"."sys_logininfor" VALUES (299, 'admin', '183.17.49.124', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-05 16:22:20');
INSERT INTO "public"."sys_logininfor" VALUES (300, 'admin', '183.17.49.124', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-12-05 16:23:34');
INSERT INTO "public"."sys_logininfor" VALUES (301, 'admin', '183.17.49.124', 'XX XX', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-05 16:23:37');
INSERT INTO "public"."sys_logininfor" VALUES (302, 'admin', '125.41.254.25', 'XX XX', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-05 16:39:27');
INSERT INTO "public"."sys_logininfor" VALUES (303, 'admin', '125.41.254.25', 'XX XX', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-05 16:39:34');
INSERT INTO "public"."sys_logininfor" VALUES (304, 'admin', '59.174.81.106', 'XX XX', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-05 17:10:15');
INSERT INTO "public"."sys_logininfor" VALUES (305, 'admin', '59.174.81.106', 'XX XX', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-05 17:10:25');
INSERT INTO "public"."sys_logininfor" VALUES (306, 'admin', '59.174.81.106', 'XX XX', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-05 17:10:39');
INSERT INTO "public"."sys_logininfor" VALUES (307, 'admin', '223.104.77.154', 'XX XX', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-05 19:30:00');
INSERT INTO "public"."sys_logininfor" VALUES (308, 'admin', '223.104.77.154', 'XX XX', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-05 19:30:07');
INSERT INTO "public"."sys_logininfor" VALUES (309, 'admin', '223.104.77.154', 'XX XX', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-05 19:30:12');
INSERT INTO "public"."sys_logininfor" VALUES (310, 'admin', '223.107.64.218', 'XX XX', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-06 00:46:57');
INSERT INTO "public"."sys_logininfor" VALUES (311, 'admin', '223.107.64.218', 'XX XX', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-06 00:47:05');
INSERT INTO "public"."sys_logininfor" VALUES (312, 'admin', '106.92.193.49', 'XX XX', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-06 01:50:45');
INSERT INTO "public"."sys_logininfor" VALUES (313, 'admin', '106.92.193.49', 'XX XX', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-06 01:50:50');
INSERT INTO "public"."sys_logininfor" VALUES (314, 'admin', '106.92.193.49', 'XX XX', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-06 01:50:57');
INSERT INTO "public"."sys_logininfor" VALUES (315, 'admin', '106.92.193.49', 'XX XX', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-06 01:51:24');
INSERT INTO "public"."sys_logininfor" VALUES (316, 'admin', '106.92.193.49', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-06 01:53:02');
INSERT INTO "public"."sys_logininfor" VALUES (317, 'admin', '106.92.193.49', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-12-06 02:00:00');
INSERT INTO "public"."sys_logininfor" VALUES (318, 'admin', '106.92.193.49', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-06 02:00:01');
INSERT INTO "public"."sys_logininfor" VALUES (319, 'admin', '39.151.108.74', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-06 08:52:12');
INSERT INTO "public"."sys_logininfor" VALUES (320, 'admin', '106.92.193.49', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-06 13:32:07');
INSERT INTO "public"."sys_logininfor" VALUES (321, 'admin', '106.92.193.49', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-06 16:07:14');
INSERT INTO "public"."sys_logininfor" VALUES (322, 'admin', '106.92.193.49', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-06 18:20:34');
INSERT INTO "public"."sys_logininfor" VALUES (323, 'admin', '106.92.193.49', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-07 02:44:42');
INSERT INTO "public"."sys_logininfor" VALUES (324, 'admin', '106.92.146.84', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-07 16:33:01');
INSERT INTO "public"."sys_logininfor" VALUES (325, 'admin', '36.248.255.73', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-07 18:12:55');
INSERT INTO "public"."sys_logininfor" VALUES (326, 'admin', '106.92.146.84', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-07 19:26:41');
INSERT INTO "public"."sys_logininfor" VALUES (327, 'admin', '36.248.255.73', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-07 20:17:02');
INSERT INTO "public"."sys_logininfor" VALUES (328, 'admin', '106.92.146.84', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-12-07 20:49:48');
INSERT INTO "public"."sys_logininfor" VALUES (329, 'admin', '106.92.146.84', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-07 21:12:49');
INSERT INTO "public"."sys_logininfor" VALUES (330, 'admin', '36.248.255.73', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-07 22:19:07');
INSERT INTO "public"."sys_logininfor" VALUES (331, 'admin', '113.251.83.187', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-08 09:07:01');
INSERT INTO "public"."sys_logininfor" VALUES (332, 'admin', '219.146.4.75', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-08 10:42:19');
INSERT INTO "public"."sys_logininfor" VALUES (333, 'admin', '60.208.121.195', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-08 11:16:34');
INSERT INTO "public"."sys_logininfor" VALUES (334, 'admin', '183.250.143.42', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-08 11:26:15');
INSERT INTO "public"."sys_logininfor" VALUES (335, 'admin', '218.23.42.105', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-08 11:47:00');
INSERT INTO "public"."sys_logininfor" VALUES (336, 'admin', '58.56.96.30', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-08 13:50:00');
INSERT INTO "public"."sys_logininfor" VALUES (337, 'admin', '218.23.42.105', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-08 14:05:27');
INSERT INTO "public"."sys_logininfor" VALUES (338, 'admin', '117.177.201.175', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-08 14:06:03');
INSERT INTO "public"."sys_logininfor" VALUES (339, 'admin', '27.17.153.51', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-08 15:56:10');
INSERT INTO "public"."sys_logininfor" VALUES (340, 'admin', '221.200.232.10', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-08 16:16:53');
INSERT INTO "public"."sys_logininfor" VALUES (341, 'admin', '58.56.96.29', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-08 16:34:04');
INSERT INTO "public"."sys_logininfor" VALUES (342, 'admin', '183.157.54.229', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-08 17:04:07');
INSERT INTO "public"."sys_logininfor" VALUES (343, 'admin', '58.56.96.29', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-08 17:33:31');
INSERT INTO "public"."sys_logininfor" VALUES (344, 'admin', '120.219.45.188', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-08 19:28:52');
INSERT INTO "public"."sys_logininfor" VALUES (345, 'admin', '106.92.146.84', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-08 22:25:12');
INSERT INTO "public"."sys_logininfor" VALUES (346, 'admin', '218.23.42.105', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-09 08:36:23');
INSERT INTO "public"."sys_logininfor" VALUES (347, 'admin', '113.248.98.239', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-09 09:04:46');
INSERT INTO "public"."sys_logininfor" VALUES (348, 'admin', '58.213.35.162', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-09 09:07:42');
INSERT INTO "public"."sys_logininfor" VALUES (349, 'admin', '60.208.121.195', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-09 09:42:06');
INSERT INTO "public"."sys_logininfor" VALUES (350, 'admin', '111.55.24.31', 'XX XX', 'Chrome Mobile', 'Android 1.x', '0', '登录成功', '2025-12-09 10:15:03');
INSERT INTO "public"."sys_logininfor" VALUES (351, 'admin', '39.151.104.197', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-09 10:38:35');
INSERT INTO "public"."sys_logininfor" VALUES (352, 'admin', '60.208.121.195', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-09 11:15:53');
INSERT INTO "public"."sys_logininfor" VALUES (353, 'admin', '218.4.116.210', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-09 11:26:41');
INSERT INTO "public"."sys_logininfor" VALUES (354, 'admin', '14.127.37.255', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-09 15:23:29');
INSERT INTO "public"."sys_logininfor" VALUES (355, 'admin', '113.248.98.239', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-12-09 16:37:34');
INSERT INTO "public"."sys_logininfor" VALUES (356, 'admin', '113.248.98.239', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-09 16:48:17');
INSERT INTO "public"."sys_logininfor" VALUES (357, 'admin', '219.145.8.20', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-09 18:05:46');
INSERT INTO "public"."sys_logininfor" VALUES (358, 'admin', '219.145.8.20', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-09 18:07:02');
INSERT INTO "public"."sys_logininfor" VALUES (359, 'admin', '113.87.164.197', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-09 18:23:19');
INSERT INTO "public"."sys_logininfor" VALUES (360, 'admin', '106.92.146.84', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-09 20:53:07');
INSERT INTO "public"."sys_logininfor" VALUES (361, 'admin', '106.47.42.38', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-09 21:19:33');
INSERT INTO "public"."sys_logininfor" VALUES (362, 'admin', '113.248.98.239', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-10 09:14:00');
INSERT INTO "public"."sys_logininfor" VALUES (363, 'admin', '220.202.238.0', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-10 11:12:38');
INSERT INTO "public"."sys_logininfor" VALUES (364, 'admin', '180.169.11.114', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-10 14:28:55');
INSERT INTO "public"."sys_logininfor" VALUES (365, 'admin', '112.224.194.39', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-10 15:34:28');
INSERT INTO "public"."sys_logininfor" VALUES (366, 'admin', '117.190.115.194', 'XX XX', 'Firefox 14', 'Windows 10', '0', '登录成功', '2025-12-10 16:23:35');
INSERT INTO "public"."sys_logininfor" VALUES (367, 'admin', '223.80.162.135', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-11 00:10:54');
INSERT INTO "public"."sys_logininfor" VALUES (368, 'admin', '125.80.215.66', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-11 09:12:34');
INSERT INTO "public"."sys_logininfor" VALUES (369, 'admin', '120.229.94.81', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-11 10:59:08');
INSERT INTO "public"."sys_logininfor" VALUES (370, 'admin', '58.213.35.162', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-11 13:45:30');
INSERT INTO "public"."sys_logininfor" VALUES (371, 'admin', '58.213.35.162', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-11 13:45:40');
INSERT INTO "public"."sys_logininfor" VALUES (372, 'admin', '106.92.146.84', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-11 19:22:16');
INSERT INTO "public"."sys_logininfor" VALUES (373, 'admin', '106.92.146.84', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-11 20:00:06');
INSERT INTO "public"."sys_logininfor" VALUES (374, 'admin', '36.248.255.73', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-11 20:58:39');
INSERT INTO "public"."sys_logininfor" VALUES (375, 'admin', '27.153.75.82', 'XX XX', 'Chrome 11', 'Android 1.x', '0', '登录成功', '2025-12-11 22:25:43');
INSERT INTO "public"."sys_logininfor" VALUES (376, 'admin', '219.146.120.3', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-12 08:48:20');
INSERT INTO "public"."sys_logininfor" VALUES (377, 'admin', '125.80.215.66', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-12 09:04:13');
INSERT INTO "public"."sys_logininfor" VALUES (378, 'admin', '39.90.115.250', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-12 09:28:59');
INSERT INTO "public"."sys_logininfor" VALUES (379, 'admin', '183.252.11.17', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-12 09:55:19');
INSERT INTO "public"."sys_logininfor" VALUES (380, 'admin', '125.80.215.66', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-12-12 11:55:58');
INSERT INTO "public"."sys_logininfor" VALUES (381, 'admin', '125.80.215.66', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-12 11:55:59');
INSERT INTO "public"."sys_logininfor" VALUES (382, 'admin', '119.100.22.53', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-12 14:08:56');
INSERT INTO "public"."sys_logininfor" VALUES (383, 'admin', '39.164.100.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-12 16:20:09');
INSERT INTO "public"."sys_logininfor" VALUES (384, 'admin', '122.193.105.34', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-12 16:33:02');
INSERT INTO "public"."sys_logininfor" VALUES (385, 'admin', '101.39.212.232', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-12 17:20:09');
INSERT INTO "public"."sys_logininfor" VALUES (386, 'admin', '39.90.115.250', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-12 17:23:37');
INSERT INTO "public"."sys_logininfor" VALUES (387, 'admin', '113.248.107.232', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-13 09:26:19');
INSERT INTO "public"."sys_logininfor" VALUES (388, 'admin', '219.141.53.178', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-13 09:51:32');
INSERT INTO "public"."sys_logininfor" VALUES (389, 'admin', '115.60.190.109', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-13 11:38:58');
INSERT INTO "public"."sys_logininfor" VALUES (390, 'admin', '39.184.156.26', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-13 15:59:39');
INSERT INTO "public"."sys_logininfor" VALUES (391, 'admin', '61.142.127.218', 'XX XX', 'Chrome 9', 'Windows 10', '0', '登录成功', '2025-12-13 16:55:58');
INSERT INTO "public"."sys_logininfor" VALUES (392, 'admin', '117.133.87.64', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-13 22:16:23');
INSERT INTO "public"."sys_logininfor" VALUES (393, 'admin', '183.227.121.81', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-13 22:46:38');
INSERT INTO "public"."sys_logininfor" VALUES (394, 'admin', '106.92.146.84', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-14 02:27:40');
INSERT INTO "public"."sys_logininfor" VALUES (395, 'admin', '106.92.105.6', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-14 18:45:11');
INSERT INTO "public"."sys_logininfor" VALUES (396, 'admin', '36.161.238.161', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-14 22:25:00');
INSERT INTO "public"."sys_logininfor" VALUES (397, 'admin', '112.32.20.39', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-14 22:42:04');
INSERT INTO "public"."sys_logininfor" VALUES (398, 'admin', '39.90.115.250', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-15 08:52:48');
INSERT INTO "public"."sys_logininfor" VALUES (399, 'admin', '218.23.42.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-15 09:32:37');
INSERT INTO "public"."sys_logininfor" VALUES (400, 'admin', '103.151.173.199', 'XX XX', 'Firefox 14', 'Windows 10', '0', '登录成功', '2025-12-15 09:47:16');
INSERT INTO "public"."sys_logininfor" VALUES (401, 'admin', '113.251.64.67', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-15 09:55:08');
INSERT INTO "public"."sys_logininfor" VALUES (402, 'admin', '218.23.42.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-12-15 10:05:24');
INSERT INTO "public"."sys_logininfor" VALUES (403, 'admin', '60.173.201.77', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-15 10:19:22');
INSERT INTO "public"."sys_logininfor" VALUES (404, 'admin', '218.81.139.156', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-15 14:32:18');
INSERT INTO "public"."sys_logininfor" VALUES (405, 'admin', '113.128.182.118', 'XX XX', 'Firefox 14', 'Windows 10', '0', '登录成功', '2025-12-15 15:30:57');
INSERT INTO "public"."sys_logininfor" VALUES (406, 'admin', '115.205.33.228', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-15 15:38:27');
INSERT INTO "public"."sys_logininfor" VALUES (407, 'admin', '211.22.180.125', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-15 16:24:58');
INSERT INTO "public"."sys_logininfor" VALUES (408, 'admin', '211.97.159.55', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-15 16:53:37');
INSERT INTO "public"."sys_logininfor" VALUES (409, 'admin', '112.123.8.233', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-15 16:56:26');
INSERT INTO "public"."sys_logininfor" VALUES (410, 'admin', '183.230.64.242', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-15 17:00:40');
INSERT INTO "public"."sys_logininfor" VALUES (411, 'admin', '183.227.69.28', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-15 22:48:40');
INSERT INTO "public"."sys_logininfor" VALUES (412, 'admin', '39.164.100.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-16 08:43:40');
INSERT INTO "public"."sys_logininfor" VALUES (413, 'admin', '171.8.61.75', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-16 09:17:42');
INSERT INTO "public"."sys_logininfor" VALUES (414, 'admin', '113.251.64.67', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-16 09:22:46');
INSERT INTO "public"."sys_logininfor" VALUES (415, 'admin', '222.137.174.138', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-16 09:51:29');
INSERT INTO "public"."sys_logininfor" VALUES (416, 'admin', '183.230.64.242', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-16 09:58:31');
INSERT INTO "public"."sys_logininfor" VALUES (417, 'admin', '122.226.153.50', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-16 10:31:40');
INSERT INTO "public"."sys_logininfor" VALUES (418, 'admin', '60.24.208.115', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-16 11:13:00');
INSERT INTO "public"."sys_logininfor" VALUES (419, 'admin', '111.33.4.220', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-16 14:20:51');
INSERT INTO "public"."sys_logininfor" VALUES (420, 'admin', '39.90.115.250', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-16 14:27:34');
INSERT INTO "public"."sys_logininfor" VALUES (421, 'admin', '36.154.196.46', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-16 16:04:12');
INSERT INTO "public"."sys_logininfor" VALUES (422, 'admin', '113.118.241.227', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2025-12-16 16:12:56');
INSERT INTO "public"."sys_logininfor" VALUES (423, 'admin', '117.22.217.147', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-16 16:16:25');
INSERT INTO "public"."sys_logininfor" VALUES (424, 'admin', '211.137.126.181', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-16 16:16:50');
INSERT INTO "public"."sys_logininfor" VALUES (425, 'admin', '124.132.27.117', 'XX XX', 'Internet Explorer 6', 'Windows XP', '0', '登录成功', '2025-12-16 16:17:19');
INSERT INTO "public"."sys_logininfor" VALUES (426, 'admin', '14.145.42.161', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-16 17:05:22');
INSERT INTO "public"."sys_logininfor" VALUES (427, 'admin', '113.248.103.135', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-17 09:07:57');
INSERT INTO "public"."sys_logininfor" VALUES (428, 'admin', '113.248.103.135', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-17 09:36:14');
INSERT INTO "public"."sys_logininfor" VALUES (429, 'admin', '39.90.115.250', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-17 09:43:57');
INSERT INTO "public"."sys_logininfor" VALUES (430, 'admin', '60.24.208.115', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-17 13:56:29');
INSERT INTO "public"."sys_logininfor" VALUES (431, 'admin', '58.19.17.131', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-17 14:05:48');
INSERT INTO "public"."sys_logininfor" VALUES (432, 'admin', '115.152.9.115', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-17 14:08:17');
INSERT INTO "public"."sys_logininfor" VALUES (433, 'admin', '61.128.195.29', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-17 16:30:28');
INSERT INTO "public"."sys_logininfor" VALUES (434, 'admin', '114.103.246.217', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-17 17:06:32');
INSERT INTO "public"."sys_logininfor" VALUES (435, 'admin', '183.247.2.118', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-17 21:40:06');
INSERT INTO "public"."sys_logininfor" VALUES (436, 'admin', '112.32.15.98', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-17 21:42:04');
INSERT INTO "public"."sys_logininfor" VALUES (437, 'admin', '58.19.17.131', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-18 09:29:05');
INSERT INTO "public"."sys_logininfor" VALUES (438, 'admin', '220.202.230.13', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-18 10:12:29');
INSERT INTO "public"."sys_logininfor" VALUES (439, 'admin', '113.248.103.135', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-18 10:22:10');
INSERT INTO "public"."sys_logininfor" VALUES (440, 'admin', '124.79.191.212', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-18 12:31:01');
INSERT INTO "public"."sys_logininfor" VALUES (441, 'admin', '119.123.198.205', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-18 15:07:07');
INSERT INTO "public"."sys_logininfor" VALUES (442, 'admin', '124.114.23.93', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-18 16:07:25');
INSERT INTO "public"."sys_logininfor" VALUES (443, 'admin', '111.4.64.161', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-18 16:32:06');
INSERT INTO "public"."sys_logininfor" VALUES (444, 'admin', '110.183.133.72', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-18 17:37:33');
INSERT INTO "public"."sys_logininfor" VALUES (445, 'admin', '58.44.8.248', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-18 19:20:23');
INSERT INTO "public"."sys_logininfor" VALUES (446, 'admin', '113.128.182.118', 'XX XX', 'Firefox 14', 'Windows 10', '0', '登录成功', '2025-12-18 21:31:54');
INSERT INTO "public"."sys_logininfor" VALUES (447, 'admin', '101.87.92.251', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-18 21:40:55');
INSERT INTO "public"."sys_logininfor" VALUES (448, 'admin', '125.71.94.13', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-18 22:14:46');
INSERT INTO "public"."sys_logininfor" VALUES (449, 'admin', '219.146.120.3', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-19 08:13:32');
INSERT INTO "public"."sys_logininfor" VALUES (450, 'admin', '58.19.17.131', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-19 08:51:25');
INSERT INTO "public"."sys_logininfor" VALUES (451, 'admin', '223.71.142.250', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-19 09:10:38');
INSERT INTO "public"."sys_logininfor" VALUES (452, 'admin', '42.86.99.237', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-19 09:30:27');
INSERT INTO "public"."sys_logininfor" VALUES (453, 'admin', '1.85.56.110', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-19 10:44:03');
INSERT INTO "public"."sys_logininfor" VALUES (454, 'admin', '125.80.207.230', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-19 11:09:04');
INSERT INTO "public"."sys_logininfor" VALUES (455, 'admin', '36.152.159.202', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-19 14:18:18');
INSERT INTO "public"."sys_logininfor" VALUES (456, 'admin', '106.114.184.132', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-19 14:43:16');
INSERT INTO "public"."sys_logininfor" VALUES (457, 'admin', '211.137.126.181', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-19 14:44:33');
INSERT INTO "public"."sys_logininfor" VALUES (458, 'admin', '58.20.55.73', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-19 15:11:52');
INSERT INTO "public"."sys_logininfor" VALUES (459, 'admin', '125.80.207.230', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-19 17:11:15');
INSERT INTO "public"."sys_logininfor" VALUES (460, 'admin', '125.40.219.117', 'XX XX', 'Chrome 11', 'Windows 10', '0', '登录成功', '2025-12-19 21:23:21');
INSERT INTO "public"."sys_logininfor" VALUES (461, 'admin', '36.57.112.144', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-20 12:47:42');
INSERT INTO "public"."sys_logininfor" VALUES (462, 'admin', '58.213.83.90', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-20 15:06:31');
INSERT INTO "public"."sys_logininfor" VALUES (463, 'admin', '106.92.109.49', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-20 16:46:12');
INSERT INTO "public"."sys_logininfor" VALUES (464, 'admin', '36.57.112.144', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-20 18:18:56');
INSERT INTO "public"."sys_logininfor" VALUES (465, 'admin', '106.92.105.6', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-20 18:48:20');
INSERT INTO "public"."sys_logininfor" VALUES (466, 'admin', '183.247.2.118', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-21 07:15:07');
INSERT INTO "public"."sys_logininfor" VALUES (467, 'admin', '27.199.201.28', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-21 12:34:40');
INSERT INTO "public"."sys_logininfor" VALUES (468, 'admin', '220.168.148.10', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-22 08:51:58');
INSERT INTO "public"."sys_logininfor" VALUES (469, 'admin', '125.80.207.206', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-22 10:13:56');
INSERT INTO "public"."sys_logininfor" VALUES (470, 'admin', '117.179.32.30', 'XX XX', 'Chrome 13', 'Mac OS X', '0', '登录成功', '2025-12-22 14:58:25');
INSERT INTO "public"."sys_logininfor" VALUES (471, 'admin', '115.193.46.70', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-22 16:56:04');
INSERT INTO "public"."sys_logininfor" VALUES (472, 'admin', '49.77.17.88', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-22 17:40:19');
INSERT INTO "public"."sys_logininfor" VALUES (473, 'admin', '223.107.68.194', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-22 18:27:57');
INSERT INTO "public"."sys_logininfor" VALUES (474, 'admin', '117.88.217.14', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-22 19:45:52');
INSERT INTO "public"."sys_logininfor" VALUES (475, 'admin', '119.123.52.255', 'XX XX', 'Firefox 14', 'Windows 10', '0', '登录成功', '2025-12-22 20:20:20');
INSERT INTO "public"."sys_logininfor" VALUES (476, 'admin', '171.213.184.121', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-22 20:36:34');
INSERT INTO "public"."sys_logininfor" VALUES (477, 'admin', '106.92.111.86', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-22 21:32:28');
INSERT INTO "public"."sys_logininfor" VALUES (478, 'admin', '219.130.207.17', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-23 08:38:27');
INSERT INTO "public"."sys_logininfor" VALUES (479, 'admin', '220.178.150.3', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-23 08:54:42');
INSERT INTO "public"."sys_logininfor" VALUES (480, 'admin', '113.248.98.209', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-23 09:19:23');
INSERT INTO "public"."sys_logininfor" VALUES (481, 'admin', '222.129.44.101', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-23 10:33:01');
INSERT INTO "public"."sys_logininfor" VALUES (482, 'admin', '119.123.52.255', 'XX XX', 'Firefox 14', 'Windows 10', '0', '登录成功', '2025-12-23 10:44:39');
INSERT INTO "public"."sys_logininfor" VALUES (483, 'admin', '112.120.48.173', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-23 15:30:11');
INSERT INTO "public"."sys_logininfor" VALUES (484, 'admin', '119.119.33.44', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-23 17:06:41');
INSERT INTO "public"."sys_logininfor" VALUES (485, 'admin', '119.32.30.34', 'XX XX', 'Firefox 14', 'Linux', '0', '登录成功', '2025-12-23 19:34:43');
INSERT INTO "public"."sys_logininfor" VALUES (486, 'admin', '220.202.230.49', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-23 21:17:41');
INSERT INTO "public"."sys_logininfor" VALUES (487, 'admin', '111.124.131.95', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-24 11:02:18');
INSERT INTO "public"."sys_logininfor" VALUES (488, 'admin', '120.224.145.96', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-24 14:17:41');
INSERT INTO "public"."sys_logininfor" VALUES (489, 'admin', '125.69.130.115', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-24 14:18:04');
INSERT INTO "public"."sys_logininfor" VALUES (490, 'admin', '58.101.118.225', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-24 15:29:46');
INSERT INTO "public"."sys_logininfor" VALUES (491, 'admin', '125.120.94.62', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-24 18:34:19');
INSERT INTO "public"."sys_logininfor" VALUES (492, 'admin', '27.154.193.206', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-24 22:14:21');
INSERT INTO "public"."sys_logininfor" VALUES (493, 'admin', '180.172.30.200', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-25 09:33:40');
INSERT INTO "public"."sys_logininfor" VALUES (494, 'admin', '58.19.17.131', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-25 09:55:59');
INSERT INTO "public"."sys_logininfor" VALUES (495, 'admin', '125.80.194.122', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-25 10:53:16');
INSERT INTO "public"."sys_logininfor" VALUES (496, 'admin', '111.202.70.101', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-26 16:21:30');
INSERT INTO "public"."sys_logininfor" VALUES (497, 'admin', '222.210.208.174', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-26 16:30:08');
INSERT INTO "public"."sys_logininfor" VALUES (498, 'admin', '219.145.102.210', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-26 16:31:33');
INSERT INTO "public"."sys_logininfor" VALUES (499, 'admin', '111.204.182.99', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-26 16:37:30');
INSERT INTO "public"."sys_logininfor" VALUES (500, 'admin', '111.202.70.99', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-26 17:55:04');
INSERT INTO "public"."sys_logininfor" VALUES (501, 'admin', '122.188.195.38', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-26 18:07:35');
INSERT INTO "public"."sys_logininfor" VALUES (502, 'admin', '112.80.97.66', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-26 18:29:02');
INSERT INTO "public"."sys_logininfor" VALUES (503, 'admin', '101.87.5.202', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-26 19:39:51');
INSERT INTO "public"."sys_logininfor" VALUES (504, 'admin', '117.175.103.60', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-26 20:30:13');
INSERT INTO "public"."sys_logininfor" VALUES (505, 'admin', '223.104.194.79', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-26 22:02:14');
INSERT INTO "public"."sys_logininfor" VALUES (506, 'admin', '111.199.56.105', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-26 22:02:54');
INSERT INTO "public"."sys_logininfor" VALUES (507, 'admin', '120.40.62.242', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2025-12-27 10:55:47');
INSERT INTO "public"."sys_logininfor" VALUES (508, 'admin', '112.22.213.9', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-27 15:12:52');
INSERT INTO "public"."sys_logininfor" VALUES (509, 'admin', '115.227.75.5', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-27 16:17:39');
INSERT INTO "public"."sys_logininfor" VALUES (510, 'admin', '106.92.111.86', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-28 00:19:28');
INSERT INTO "public"."sys_logininfor" VALUES (511, 'admin', '112.49.4.60', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-28 10:05:57');
INSERT INTO "public"."sys_logininfor" VALUES (512, 'admin', '223.91.185.164', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-28 12:20:40');
INSERT INTO "public"."sys_logininfor" VALUES (513, 'admin', '111.199.56.105', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-28 21:38:00');
INSERT INTO "public"."sys_logininfor" VALUES (514, 'admin', '111.199.56.105', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-28 22:00:02');
INSERT INTO "public"."sys_logininfor" VALUES (515, 'admin', '106.92.111.86', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-28 23:08:54');
INSERT INTO "public"."sys_logininfor" VALUES (516, 'admin', '113.249.27.131', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-29 13:45:17');
INSERT INTO "public"."sys_logininfor" VALUES (517, 'admin', '183.134.211.52', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-29 13:58:05');
INSERT INTO "public"."sys_logininfor" VALUES (518, 'admin', '221.12.5.179', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-29 14:38:43');
INSERT INTO "public"."sys_logininfor" VALUES (519, 'admin', '183.134.211.52', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2025-12-29 14:42:49');
INSERT INTO "public"."sys_logininfor" VALUES (520, 'admin', '219.134.150.147', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-29 15:44:54');
INSERT INTO "public"."sys_logininfor" VALUES (521, 'admin', '60.16.6.236', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-29 16:54:06');
INSERT INTO "public"."sys_logininfor" VALUES (522, 'admin', '202.99.199.66', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-29 17:07:46');
INSERT INTO "public"."sys_logininfor" VALUES (523, 'admin', '58.213.35.162', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-29 17:13:14');
INSERT INTO "public"."sys_logininfor" VALUES (524, 'admin', '58.19.17.131', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-30 08:56:06');
INSERT INTO "public"."sys_logininfor" VALUES (525, 'admin', '222.129.32.75', 'XX XX', 'Chrome 11', 'Windows 10', '0', '登录成功', '2025-12-30 10:13:53');
INSERT INTO "public"."sys_logininfor" VALUES (526, 'admin', '222.129.32.75', 'XX XX', 'Chrome 11', 'Windows 10', '0', '退出成功', '2025-12-30 10:18:24');
INSERT INTO "public"."sys_logininfor" VALUES (527, 'admin', '123.13.218.211', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-30 14:46:35');
INSERT INTO "public"."sys_logininfor" VALUES (528, 'admin', '111.79.178.205', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-30 15:41:31');
INSERT INTO "public"."sys_logininfor" VALUES (529, 'admin', '117.107.143.202', 'XX XX', 'Firefox 13', 'Windows 10', '0', '登录成功', '2025-12-30 15:57:44');
INSERT INTO "public"."sys_logininfor" VALUES (530, 'admin', '218.23.42.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-30 16:30:37');
INSERT INTO "public"."sys_logininfor" VALUES (531, 'admin', '117.64.87.246', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-30 22:52:28');
INSERT INTO "public"."sys_logininfor" VALUES (532, 'admin', '106.120.7.208', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-30 23:50:15');
INSERT INTO "public"."sys_logininfor" VALUES (533, 'admin', '117.13.161.8', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-31 08:48:48');
INSERT INTO "public"."sys_logininfor" VALUES (534, 'admin', '58.19.17.131', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-31 09:05:46');
INSERT INTO "public"."sys_logininfor" VALUES (535, 'admin', '218.23.42.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-31 09:09:24');
INSERT INTO "public"."sys_logininfor" VALUES (536, 'admin', '183.185.82.222', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-31 09:16:21');
INSERT INTO "public"."sys_logininfor" VALUES (537, 'admin', '183.185.82.28', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-31 09:18:21');
INSERT INTO "public"."sys_logininfor" VALUES (538, 'admin', '113.249.239.23', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-31 09:25:09');
INSERT INTO "public"."sys_logininfor" VALUES (539, 'admin', '119.164.74.255', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-31 09:53:26');
INSERT INTO "public"."sys_logininfor" VALUES (540, 'admin', '113.78.101.34', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-31 13:46:32');
INSERT INTO "public"."sys_logininfor" VALUES (541, 'admin', '120.236.210.197', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-31 14:02:41');
INSERT INTO "public"."sys_logininfor" VALUES (542, 'admin', '36.6.249.173', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-31 14:15:50');
INSERT INTO "public"."sys_logininfor" VALUES (543, 'admin', '218.61.106.194', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-31 14:24:07');
INSERT INTO "public"."sys_logininfor" VALUES (544, 'admin', '58.17.214.71', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-12-31 15:17:10');
INSERT INTO "public"."sys_logininfor" VALUES (545, 'admin', '222.74.26.130', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-31 15:19:05');
INSERT INTO "public"."sys_logininfor" VALUES (546, 'admin', '60.3.191.13', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-12-31 16:34:16');
INSERT INTO "public"."sys_logininfor" VALUES (547, 'admin', '110.185.168.6', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-31 23:25:46');
INSERT INTO "public"."sys_logininfor" VALUES (548, 'admin', '101.87.5.202', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-01 11:16:30');
INSERT INTO "public"."sys_logininfor" VALUES (549, 'admin', '117.36.1.147', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-01 15:24:17');
INSERT INTO "public"."sys_logininfor" VALUES (550, 'admin', '101.87.5.202', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-02 01:08:15');
INSERT INTO "public"."sys_logininfor" VALUES (551, 'admin', '120.231.202.203', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-02 13:16:33');
INSERT INTO "public"."sys_logininfor" VALUES (552, 'admin', '106.92.111.25', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-02 16:48:07');
INSERT INTO "public"."sys_logininfor" VALUES (553, 'admin', '125.71.92.50', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-02 20:11:45');
INSERT INTO "public"."sys_logininfor" VALUES (554, 'admin', '180.107.171.127', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-02 21:17:24');
INSERT INTO "public"."sys_logininfor" VALUES (555, 'admin', '106.92.111.25', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-02 21:48:34');
INSERT INTO "public"."sys_logininfor" VALUES (556, 'admin', '106.92.111.25', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-03 14:56:45');
INSERT INTO "public"."sys_logininfor" VALUES (557, 'admin', '106.92.111.25', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-03 16:23:50');
INSERT INTO "public"."sys_logininfor" VALUES (558, 'admin', '106.92.111.25', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-03 17:06:13');
INSERT INTO "public"."sys_logininfor" VALUES (559, 'admin', '180.111.29.129', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-03 19:48:00');
INSERT INTO "public"."sys_logininfor" VALUES (560, 'admin', '58.19.17.131', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-04 09:03:46');
INSERT INTO "public"."sys_logininfor" VALUES (561, 'admin', '218.1.140.35', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-04 09:07:32');
INSERT INTO "public"."sys_logininfor" VALUES (562, 'admin', '58.17.214.71', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-04 09:47:15');
INSERT INTO "public"."sys_logininfor" VALUES (563, 'admin', '113.251.88.235', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-04 09:57:13');
INSERT INTO "public"."sys_logininfor" VALUES (564, 'admin', '221.236.103.98', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-04 11:19:00');
INSERT INTO "public"."sys_logininfor" VALUES (565, 'admin', '58.20.55.73', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-04 13:16:22');
INSERT INTO "public"."sys_logininfor" VALUES (566, 'admin', '219.217.246.204', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-04 14:29:22');
INSERT INTO "public"."sys_logininfor" VALUES (567, 'admin', '110.90.12.73', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-04 14:38:21');
INSERT INTO "public"."sys_logininfor" VALUES (568, 'admin', '180.111.38.212', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-04 14:56:55');
INSERT INTO "public"."sys_logininfor" VALUES (569, 'admin', '219.130.206.172', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-04 15:08:48');
INSERT INTO "public"."sys_logininfor" VALUES (570, 'admin', '36.44.111.121', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-04 18:07:05');
INSERT INTO "public"."sys_logininfor" VALUES (571, 'admin', '120.36.110.239', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-04 20:17:01');
INSERT INTO "public"."sys_logininfor" VALUES (572, 'admin', '182.149.80.90', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功', '2026-01-04 21:41:09');
INSERT INTO "public"."sys_logininfor" VALUES (573, 'admin', '106.92.111.25', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-04 21:50:23');
INSERT INTO "public"."sys_logininfor" VALUES (574, 'admin', '123.7.84.78', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-05 08:52:04');
INSERT INTO "public"."sys_logininfor" VALUES (575, 'admin', '113.78.239.41', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 10:03:11');
INSERT INTO "public"."sys_logininfor" VALUES (576, 'admin', '111.204.182.99', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-05 10:04:12');
INSERT INTO "public"."sys_logininfor" VALUES (577, 'admin', '180.98.152.249', 'XX XX', 'Chrome Mobile', 'Android 1.x', '0', '登录成功', '2026-01-05 10:11:51');
INSERT INTO "public"."sys_logininfor" VALUES (578, 'admin', '117.157.92.73', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-05 11:49:23');
INSERT INTO "public"."sys_logininfor" VALUES (579, 'admin', '113.251.88.235', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 11:56:34');
INSERT INTO "public"."sys_logininfor" VALUES (580, 'admin', '218.68.224.34', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 13:51:36');
INSERT INTO "public"."sys_logininfor" VALUES (581, 'admin', '58.20.55.73', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 14:17:29');
INSERT INTO "public"."sys_logininfor" VALUES (582, 'admin', '171.8.225.63', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2026-01-05 14:23:47');
INSERT INTO "public"."sys_logininfor" VALUES (583, 'admin', '61.240.224.186', 'XX XX', 'Firefox 14', 'Windows 10', '0', '登录成功', '2026-01-05 14:36:54');
INSERT INTO "public"."sys_logininfor" VALUES (584, 'admin', '183.6.26.33', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 14:37:55');
INSERT INTO "public"."sys_logininfor" VALUES (585, 'admin', '123.11.108.24', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 15:59:09');
INSERT INTO "public"."sys_logininfor" VALUES (586, 'admin', '111.4.64.161', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 16:15:18');
INSERT INTO "public"."sys_logininfor" VALUES (587, 'admin', '119.126.31.161', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 18:14:19');
INSERT INTO "public"."sys_logininfor" VALUES (588, 'admin', '106.92.193.137', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 20:32:44');
INSERT INTO "public"."sys_logininfor" VALUES (589, 'admin', '183.137.38.171', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2026-01-05 22:22:35');
INSERT INTO "public"."sys_logininfor" VALUES (590, 'admin', '113.248.165.78', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 09:06:01');
INSERT INTO "public"."sys_logininfor" VALUES (591, 'admin', '219.145.102.210', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 09:58:04');
INSERT INTO "public"."sys_logininfor" VALUES (592, 'admin', '36.44.107.104', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 11:53:32');
INSERT INTO "public"."sys_logininfor" VALUES (593, 'admin', '1.86.240.122', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 15:48:52');
INSERT INTO "public"."sys_logininfor" VALUES (594, 'admin', '111.79.187.69', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 17:27:28');
INSERT INTO "public"."sys_logininfor" VALUES (595, 'admin', '112.80.97.66', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 17:55:38');
INSERT INTO "public"."sys_logininfor" VALUES (596, 'admin', '120.40.25.97', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 18:29:32');
INSERT INTO "public"."sys_logininfor" VALUES (597, 'admin', '125.71.92.50', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 22:54:48');
INSERT INTO "public"."sys_logininfor" VALUES (598, 'admin', '106.92.193.137', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 23:11:33');
INSERT INTO "public"."sys_logininfor" VALUES (599, 'admin', '36.21.225.33', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2026-01-07 02:11:51');
INSERT INTO "public"."sys_logininfor" VALUES (600, 'admin', '36.21.225.33', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2026-01-07 02:16:06');
INSERT INTO "public"."sys_logininfor" VALUES (601, 'admin', '36.44.101.173', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-07 09:18:27');
INSERT INTO "public"."sys_logininfor" VALUES (602, 'admin', '113.248.176.130', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-07 09:46:08');
INSERT INTO "public"."sys_logininfor" VALUES (603, 'admin', '222.210.59.4', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-07 11:15:40');
INSERT INTO "public"."sys_logininfor" VALUES (604, 'admin', '113.248.165.78', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-07 11:22:19');
INSERT INTO "public"."sys_logininfor" VALUES (605, 'admin', '58.59.121.126', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-07 11:36:20');
INSERT INTO "public"."sys_logininfor" VALUES (606, 'admin', '14.19.74.43', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-07 14:05:21');
INSERT INTO "public"."sys_logininfor" VALUES (607, 'admin', '112.80.97.66', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-07 15:32:03');
INSERT INTO "public"."sys_logininfor" VALUES (608, 'admin', '183.198.148.182', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-07 15:41:11');
INSERT INTO "public"."sys_logininfor" VALUES (609, 'admin', '39.144.218.250', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-07 22:31:17');
INSERT INTO "public"."sys_logininfor" VALUES (610, 'admin', '106.92.193.137', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-07 23:26:43');
INSERT INTO "public"."sys_logininfor" VALUES (611, 'admin', '123.185.63.162', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 00:06:16');
INSERT INTO "public"."sys_logininfor" VALUES (612, 'admin', '121.229.195.136', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 09:35:47');
INSERT INTO "public"."sys_logininfor" VALUES (613, 'admin', '222.210.8.2', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-08 10:38:40');
INSERT INTO "public"."sys_logininfor" VALUES (614, 'admin', '112.91.74.37', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-08 13:18:02');
INSERT INTO "public"."sys_logininfor" VALUES (615, 'admin', '113.251.83.169', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 13:34:12');
INSERT INTO "public"."sys_logininfor" VALUES (616, 'admin', '121.229.195.136', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-08 14:00:51');
INSERT INTO "public"."sys_logininfor" VALUES (617, 'admin', '121.229.195.136', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 14:00:54');
INSERT INTO "public"."sys_logininfor" VALUES (618, 'admin', '121.229.195.136', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 14:00:58');
INSERT INTO "public"."sys_logininfor" VALUES (619, 'admin', '171.107.154.29', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-08 14:28:06');
INSERT INTO "public"."sys_logininfor" VALUES (620, 'admin', '27.203.249.90', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-08 15:19:25');
INSERT INTO "public"."sys_logininfor" VALUES (621, 'admin', '123.139.184.195', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-08 15:54:18');
INSERT INTO "public"."sys_logininfor" VALUES (622, 'admin', '183.23.48.126', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 17:28:09');
INSERT INTO "public"."sys_logininfor" VALUES (623, 'admin', '52.221.207.186', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-08 17:39:25');
INSERT INTO "public"."sys_logininfor" VALUES (624, 'admin', '183.23.162.152', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 17:46:39');
INSERT INTO "public"."sys_logininfor" VALUES (625, 'admin', '52.221.207.186', 'XX XX', 'Firefox 14', 'Mac OS X', '0', '登录成功', '2026-01-08 19:12:53');
INSERT INTO "public"."sys_logininfor" VALUES (626, 'admin', '183.253.34.201', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 23:13:46');
INSERT INTO "public"."sys_logininfor" VALUES (627, 'admin', '106.92.193.137', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 23:32:47');
INSERT INTO "public"."sys_logininfor" VALUES (628, 'admin', '113.251.83.169', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 09:07:14');
INSERT INTO "public"."sys_logininfor" VALUES (629, 'admin', '58.20.55.73', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 09:43:02');
INSERT INTO "public"."sys_logininfor" VALUES (630, 'admin', '222.210.59.4', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 10:11:04');
INSERT INTO "public"."sys_logininfor" VALUES (631, 'admin', '14.155.212.120', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-09 10:17:07');
INSERT INTO "public"."sys_logininfor" VALUES (632, 'admin', '113.95.135.135', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 10:33:08');
INSERT INTO "public"."sys_logininfor" VALUES (633, 'admin', '183.134.201.140', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-09 14:09:41');
INSERT INTO "public"."sys_logininfor" VALUES (634, 'admin', '220.191.204.64', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-09 14:15:19');
INSERT INTO "public"."sys_logininfor" VALUES (635, 'admin', '39.71.189.100', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 14:48:25');
INSERT INTO "public"."sys_logininfor" VALUES (636, 'admin', '112.48.20.166', 'XX XX', 'Chrome 14', 'Linux', '0', '登录成功', '2026-01-09 15:21:27');
INSERT INTO "public"."sys_logininfor" VALUES (637, 'admin', '112.48.20.33', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-09 15:42:41');
INSERT INTO "public"."sys_logininfor" VALUES (638, 'admin', '112.48.20.25', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 15:51:06');
INSERT INTO "public"."sys_logininfor" VALUES (639, 'admin', '112.48.20.33', 'XX XX', 'Chrome 12', 'Windows 10', '0', '退出成功', '2026-01-09 15:58:37');
INSERT INTO "public"."sys_logininfor" VALUES (640, 'admin', '112.48.20.33', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-09 16:00:53');
INSERT INTO "public"."sys_logininfor" VALUES (641, 'admin', '112.48.20.191', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 16:04:31');
INSERT INTO "public"."sys_logininfor" VALUES (642, 'admin', '113.87.187.170', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 16:05:32');
INSERT INTO "public"."sys_logininfor" VALUES (643, 'admin', '117.67.192.146', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 16:13:27');
INSERT INTO "public"."sys_logininfor" VALUES (644, 'admin', '123.232.237.243', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 16:24:44');
INSERT INTO "public"."sys_logininfor" VALUES (645, 'admin', '112.5.138.213', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 17:28:10');
INSERT INTO "public"."sys_logininfor" VALUES (646, 'admin', '42.84.37.186', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 17:54:22');
INSERT INTO "public"."sys_logininfor" VALUES (647, 'admin', '106.92.193.137', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 20:39:42');
INSERT INTO "public"."sys_logininfor" VALUES (648, 'admin', '120.231.214.110', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-10 10:03:14');
INSERT INTO "public"."sys_logininfor" VALUES (649, 'admin', '1.189.80.240', 'XX XX', 'Chrome 13', 'Mac OS X', '0', '登录成功', '2026-01-10 11:37:13');
INSERT INTO "public"."sys_logininfor" VALUES (650, 'admin', '106.92.193.137', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-10 12:57:22');
INSERT INTO "public"."sys_logininfor" VALUES (651, 'admin', '218.202.77.198', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-10 13:42:00');
INSERT INTO "public"."sys_logininfor" VALUES (652, 'admin', '106.92.193.137', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-10 14:58:21');
INSERT INTO "public"."sys_logininfor" VALUES (653, 'admin', '106.92.193.137', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-10 18:04:09');
INSERT INTO "public"."sys_logininfor" VALUES (654, 'admin', '117.147.119.240', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-11 03:39:56');
INSERT INTO "public"."sys_logininfor" VALUES (655, 'admin', '114.248.183.15', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-11 18:18:46');
INSERT INTO "public"."sys_logininfor" VALUES (656, 'admin', '52.221.207.186', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-11 19:53:49');
INSERT INTO "public"."sys_logininfor" VALUES (657, 'admin', '183.134.201.140', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 09:12:24');
INSERT INTO "public"."sys_logininfor" VALUES (658, 'admin', '116.147.250.7', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2026-01-12 09:49:27');
INSERT INTO "public"."sys_logininfor" VALUES (659, 'admin', '116.147.250.7', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2026-01-12 09:49:33');
INSERT INTO "public"."sys_logininfor" VALUES (660, 'admin', '112.29.108.9', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-12 10:08:58');
INSERT INTO "public"."sys_logininfor" VALUES (661, 'admin', '118.116.15.246', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 10:46:44');
INSERT INTO "public"."sys_logininfor" VALUES (662, 'admin', '118.121.187.81', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 10:58:50');
INSERT INTO "public"."sys_logininfor" VALUES (663, 'admin', '125.80.153.136', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 11:08:08');
INSERT INTO "public"."sys_logininfor" VALUES (664, 'admin', '222.91.199.45', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 11:11:04');
INSERT INTO "public"."sys_logininfor" VALUES (665, 'admin', '125.80.219.156', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 11:11:23');
INSERT INTO "public"."sys_logininfor" VALUES (666, 'admin', '27.216.199.167', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 12:47:08');
INSERT INTO "public"."sys_logininfor" VALUES (667, 'admin', '222.211.234.191', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:40:54');
INSERT INTO "public"."sys_logininfor" VALUES (668, 'admin', '117.70.191.170', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 14:28:59');
INSERT INTO "public"."sys_logininfor" VALUES (669, 'admin', '112.48.20.191', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 15:03:24');
INSERT INTO "public"."sys_logininfor" VALUES (670, 'admin', '223.104.119.114', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 17:02:36');
INSERT INTO "public"."sys_logininfor" VALUES (671, 'admin', '112.32.52.60', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-12 22:53:02');
INSERT INTO "public"."sys_logininfor" VALUES (672, 'admin', '124.73.220.242', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 00:36:53');
INSERT INTO "public"."sys_logininfor" VALUES (673, 'admin', '125.80.219.156', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 09:14:09');
INSERT INTO "public"."sys_logininfor" VALUES (674, 'admin', '14.154.198.132', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 10:20:56');
INSERT INTO "public"."sys_logininfor" VALUES (675, 'admin', '58.19.17.131', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 10:48:57');
INSERT INTO "public"."sys_logininfor" VALUES (676, 'admin', '113.128.44.178', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 11:18:26');
INSERT INTO "public"."sys_logininfor" VALUES (677, 'admin', '1.119.202.82', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 13:24:48');
INSERT INTO "public"."sys_logininfor" VALUES (678, 'admin', '61.133.98.8', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 13:35:57');
INSERT INTO "public"."sys_logininfor" VALUES (679, 'admin', '58.20.55.73', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 13:43:31');
INSERT INTO "public"."sys_logininfor" VALUES (680, 'admin', '113.89.53.228', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 14:08:59');
INSERT INTO "public"."sys_logininfor" VALUES (681, 'admin', '183.134.201.140', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:13:07');
INSERT INTO "public"."sys_logininfor" VALUES (682, 'admin', '183.134.201.140', 'XX XX', 'Chrome 13', 'Windows 10', '0', '退出成功', '2026-01-13 14:15:24');
INSERT INTO "public"."sys_logininfor" VALUES (683, 'admin', '183.134.201.140', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:15:34');
INSERT INTO "public"."sys_logininfor" VALUES (684, 'admin', '39.144.73.155', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-13 14:16:35');
INSERT INTO "public"."sys_logininfor" VALUES (685, 'admin', '183.134.201.140', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:18:46');
INSERT INTO "public"."sys_logininfor" VALUES (686, 'admin', '113.195.132.70', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-13 14:18:57');
INSERT INTO "public"."sys_logininfor" VALUES (687, 'admin', '113.195.132.70', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-13 14:29:19');
INSERT INTO "public"."sys_logininfor" VALUES (688, 'admin', '113.89.53.228', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 14:29:48');
INSERT INTO "public"."sys_logininfor" VALUES (689, 'admin', '183.134.201.140', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:30:47');
INSERT INTO "public"."sys_logininfor" VALUES (690, 'admin', '183.134.201.140', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:35:21');
INSERT INTO "public"."sys_logininfor" VALUES (691, 'admin', '58.20.55.73', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 15:28:49');
INSERT INTO "public"."sys_logininfor" VALUES (692, 'admin', '58.101.67.115', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 16:19:51');
INSERT INTO "public"."sys_logininfor" VALUES (693, 'admin', '27.200.127.148', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 16:54:42');
INSERT INTO "public"."sys_logininfor" VALUES (694, 'admin', '121.237.162.159', 'XX XX', 'Chrome 11', 'Windows 10', '0', '登录成功', '2026-01-13 17:02:32');
INSERT INTO "public"."sys_logininfor" VALUES (695, 'admin', '125.80.219.156', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 17:18:12');
INSERT INTO "public"."sys_logininfor" VALUES (696, 'admin', '112.49.232.66', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 17:21:14');
INSERT INTO "public"."sys_logininfor" VALUES (697, 'admin', '106.92.128.79', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 22:16:42');
INSERT INTO "public"."sys_logininfor" VALUES (698, 'admin', '106.92.128.79', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-14 00:30:16');
INSERT INTO "public"."sys_logininfor" VALUES (699, 'admin', '125.80.203.97', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-14 09:19:39');
INSERT INTO "public"."sys_logininfor" VALUES (700, 'admin', '125.80.203.97', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-14 09:19:43');
INSERT INTO "public"."sys_logininfor" VALUES (701, 'admin', '45.200.125.12', 'XX XX', 'Chrome 13', 'Linux', '0', '登录成功', '2026-01-14 09:19:52');
INSERT INTO "public"."sys_logininfor" VALUES (702, 'admin', '59.61.129.0', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-14 09:20:32');
INSERT INTO "public"."sys_logininfor" VALUES (703, 'admin', '117.62.161.179', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-14 15:12:59');
INSERT INTO "public"."sys_logininfor" VALUES (704, 'admin', '222.91.149.21', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-14 15:17:46');
INSERT INTO "public"."sys_logininfor" VALUES (705, 'admin', '120.234.91.233', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-14 20:16:26');
INSERT INTO "public"."sys_logininfor" VALUES (706, 'admin', '106.92.128.79', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-14 23:21:15');
INSERT INTO "public"."sys_logininfor" VALUES (707, 'admin', '125.80.203.97', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 09:34:04');
INSERT INTO "public"."sys_logininfor" VALUES (708, 'admin', '125.80.203.97', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-15 10:18:47');
INSERT INTO "public"."sys_logininfor" VALUES (709, 'admin', '125.80.203.97', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 10:18:51');
INSERT INTO "public"."sys_logininfor" VALUES (710, 'admin', '104.194.8.36', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 10:29:43');
INSERT INTO "public"."sys_logininfor" VALUES (711, 'admin', '111.198.29.221', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 14:18:13');
INSERT INTO "public"."sys_logininfor" VALUES (712, 'admin', '112.253.25.52', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-15 14:59:38');
INSERT INTO "public"."sys_logininfor" VALUES (713, 'admin', '112.253.25.51', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-15 15:06:27');
INSERT INTO "public"."sys_logininfor" VALUES (714, 'admin', '223.99.59.228', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 15:09:04');
INSERT INTO "public"."sys_logininfor" VALUES (715, 'admin', '117.29.166.14', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 15:19:12');
INSERT INTO "public"."sys_logininfor" VALUES (716, 'admin', '14.127.43.221', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 15:38:57');
INSERT INTO "public"."sys_logininfor" VALUES (717, 'admin', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 16:59:24');
INSERT INTO "public"."sys_logininfor" VALUES (718, 'admin', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-15 17:39:29');
INSERT INTO "public"."sys_logininfor" VALUES (719, 'admin', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 17:39:35');
INSERT INTO "public"."sys_logininfor" VALUES (720, 'admin', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-15 17:45:59');
INSERT INTO "public"."sys_logininfor" VALUES (721, '张三', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 17:46:15');
INSERT INTO "public"."sys_logininfor" VALUES (722, '张三', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-15 17:46:28');
INSERT INTO "public"."sys_logininfor" VALUES (723, 'admin', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '1', '验证码错误', '2026-01-15 17:46:32');
INSERT INTO "public"."sys_logininfor" VALUES (724, 'admin', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 17:46:35');
INSERT INTO "public"."sys_logininfor" VALUES (725, 'admin', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-15 17:53:40');
INSERT INTO "public"."sys_logininfor" VALUES (726, '张三', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 17:53:49');
INSERT INTO "public"."sys_logininfor" VALUES (727, '张三', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-15 17:54:04');
INSERT INTO "public"."sys_logininfor" VALUES (728, 'admin', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 17:54:07');
INSERT INTO "public"."sys_logininfor" VALUES (729, 'admin', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-15 18:32:43');
INSERT INTO "public"."sys_logininfor" VALUES (730, '张三', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 18:32:53');
INSERT INTO "public"."sys_logininfor" VALUES (731, '张三', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-15 18:33:02');
INSERT INTO "public"."sys_logininfor" VALUES (732, 'admin', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 18:33:06');
INSERT INTO "public"."sys_logininfor" VALUES (733, 'admin', '183.175.134.221', 'XX XX', 'Chrome 14', 'Windows 10', '1', '验证码错误', '2026-01-15 19:07:36');
INSERT INTO "public"."sys_logininfor" VALUES (734, 'admin', '183.175.134.221', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 19:07:42');
INSERT INTO "public"."sys_logininfor" VALUES (735, 'admin', '107.167.18.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 19:09:25');
INSERT INTO "public"."sys_logininfor" VALUES (736, 'admin', '112.48.20.191', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 19:28:56');
INSERT INTO "public"."sys_logininfor" VALUES (737, 'admin', '112.24.143.208', 'XX XX', 'Chrome Mobile', 'Android 1.x', '0', '登录成功', '2026-01-15 23:58:32');
INSERT INTO "public"."sys_logininfor" VALUES (738, 'admin', '222.212.8.163', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 09:29:19');
INSERT INTO "public"."sys_logininfor" VALUES (739, 'admin', '36.112.207.224', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-16 09:33:37');
INSERT INTO "public"."sys_logininfor" VALUES (740, 'admin', '1.192.63.18', 'XX XX', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-16 09:57:19');
INSERT INTO "public"."sys_logininfor" VALUES (741, 'admin', '1.192.63.18', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 09:57:24');
INSERT INTO "public"."sys_logininfor" VALUES (742, 'admin', '36.112.207.224', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 10:28:37');
INSERT INTO "public"."sys_logininfor" VALUES (743, 'admin', '36.112.207.224', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-16 10:30:01');
INSERT INTO "public"."sys_logininfor" VALUES (744, 'admin', '107.167.18.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 13:14:20');
INSERT INTO "public"."sys_logininfor" VALUES (745, 'admin', '14.154.16.234', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 13:59:04');
INSERT INTO "public"."sys_logininfor" VALUES (746, 'admin', '120.227.232.121', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:29:41');
INSERT INTO "public"."sys_logininfor" VALUES (747, 'admin', '117.64.80.99', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 15:28:41');
INSERT INTO "public"."sys_logininfor" VALUES (748, 'admin', '113.248.220.251', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 15:58:01');
INSERT INTO "public"."sys_logininfor" VALUES (749, 'admin', '58.210.215.228', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 17:07:08');
INSERT INTO "public"."sys_logininfor" VALUES (750, 'admin', '117.29.166.14', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 17:25:58');
INSERT INTO "public"."sys_logininfor" VALUES (751, 'admin', '111.53.238.226', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-16 18:22:47');
INSERT INTO "public"."sys_logininfor" VALUES (752, 'admin', '106.92.128.79', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 20:27:10');
INSERT INTO "public"."sys_logininfor" VALUES (753, 'admin', '180.233.80.181', 'XX XX', 'Chrome 14', 'Windows 10', '1', '验证码已失效', '2026-01-16 21:47:08');
INSERT INTO "public"."sys_logininfor" VALUES (754, 'admin', '180.233.80.181', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 21:47:13');
INSERT INTO "public"."sys_logininfor" VALUES (755, 'admin', '171.11.16.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 21:47:16');
INSERT INTO "public"."sys_logininfor" VALUES (756, 'admin', '58.35.78.101', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 21:51:43');
INSERT INTO "public"."sys_logininfor" VALUES (757, 'admin', '180.98.132.91', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-17 09:31:54');
INSERT INTO "public"."sys_logininfor" VALUES (758, 'admin', '36.161.160.87', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-17 10:29:29');
INSERT INTO "public"."sys_logininfor" VALUES (759, 'admin', '113.248.220.251', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-17 11:04:53');
INSERT INTO "public"."sys_logininfor" VALUES (760, 'admin', '120.227.232.121', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-17 16:01:57');
INSERT INTO "public"."sys_logininfor" VALUES (761, 'admin', '223.96.112.36', 'XX XX', 'Chrome 14', 'Windows 10', '1', '验证码错误', '2026-01-17 20:32:20');
INSERT INTO "public"."sys_logininfor" VALUES (762, 'admin', '223.96.112.36', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-17 20:32:26');
INSERT INTO "public"."sys_logininfor" VALUES (763, 'admin', '106.92.128.79', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-18 00:20:09');
INSERT INTO "public"."sys_logininfor" VALUES (764, 'admin', '160.22.39.32', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-18 13:23:25');
INSERT INTO "public"."sys_logininfor" VALUES (765, 'admin', '106.92.128.79', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-18 18:49:02');
INSERT INTO "public"."sys_logininfor" VALUES (766, 'admin', '106.92.128.79', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-18 19:02:48');
INSERT INTO "public"."sys_logininfor" VALUES (767, 'admin', '106.92.128.79', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-18 19:02:51');
INSERT INTO "public"."sys_logininfor" VALUES (768, 'admin', '120.227.232.121', 'XX XX', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-18 19:42:41');
INSERT INTO "public"."sys_logininfor" VALUES (769, 'admin', '120.227.232.121', 'XX XX', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-18 19:42:44');
INSERT INTO "public"."sys_logininfor" VALUES (770, 'admin', '120.227.232.121', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-18 19:42:48');
INSERT INTO "public"."sys_logininfor" VALUES (771, 'admin', '114.232.184.229', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-18 20:18:39');
INSERT INTO "public"."sys_logininfor" VALUES (772, 'admin', '114.232.184.229', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-18 20:19:53');
INSERT INTO "public"."sys_logininfor" VALUES (773, 'admin', '114.232.184.229', 'XX XX', 'Chrome 14', 'Windows 10', '1', '验证码已失效', '2026-01-18 20:22:43');
INSERT INTO "public"."sys_logininfor" VALUES (774, 'admin', '114.232.184.229', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-18 20:22:46');
INSERT INTO "public"."sys_logininfor" VALUES (775, 'admin', '39.82.85.183', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-18 21:53:39');
INSERT INTO "public"."sys_logininfor" VALUES (776, 'admin', '106.92.128.79', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-18 22:37:11');
INSERT INTO "public"."sys_logininfor" VALUES (777, 'admin', '106.92.128.79', 'XX XX', 'Unknown', 'Unknown', '0', '登录成功', '2026-01-18 22:38:44');
INSERT INTO "public"."sys_logininfor" VALUES (778, 'admin', '106.92.128.79', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 00:16:39');
INSERT INTO "public"."sys_logininfor" VALUES (779, 'string', '116.62.161.155', 'XX XX', 'Unknown', 'Unknown', '1', '用户不存在/密码错误', '2026-01-19 01:39:48');
INSERT INTO "public"."sys_logininfor" VALUES (780, 'string', '47.98.216.100', 'XX XX', 'Unknown', 'Unknown', '1', '用户不存在/密码错误', '2026-01-19 01:40:36');
INSERT INTO "public"."sys_logininfor" VALUES (781, 'string', '120.26.147.118', 'XX XX', 'Unknown', 'Unknown', '1', '用户不存在/密码错误', '2026-01-19 01:41:57');
INSERT INTO "public"."sys_logininfor" VALUES (782, 'string', '47.96.164.78', 'XX XX', 'Unknown', 'Unknown', '1', '用户不存在/密码错误', '2026-01-19 01:42:26');
INSERT INTO "public"."sys_logininfor" VALUES (783, 'admin', '47.97.76.78', 'XX XX', 'Unknown', 'Unknown', '0', '登录成功', '2026-01-19 01:42:57');
INSERT INTO "public"."sys_logininfor" VALUES (784, 'admin', '114.232.173.177', 'XX XX', 'Chrome 8', 'Windows 10', '0', '登录成功', '2026-01-19 08:56:30');
INSERT INTO "public"."sys_logininfor" VALUES (785, 'admin', '1.192.169.18', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-19 09:07:31');
INSERT INTO "public"."sys_logininfor" VALUES (786, 'admin', '58.20.41.139', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 09:19:47');
INSERT INTO "public"."sys_logininfor" VALUES (787, 'admin', '113.248.96.236', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 09:26:16');
INSERT INTO "public"."sys_logininfor" VALUES (788, 'admin', '117.29.166.14', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 10:25:44');
INSERT INTO "public"."sys_logininfor" VALUES (789, 'admin', '58.101.67.119', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 11:01:36');
INSERT INTO "public"."sys_logininfor" VALUES (790, 'admin', '118.116.121.202', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 11:06:24');
INSERT INTO "public"."sys_logininfor" VALUES (791, 'admin', '223.155.166.21', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-19 11:31:24');
INSERT INTO "public"."sys_logininfor" VALUES (792, 'admin', '107.167.18.105', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 14:06:13');
INSERT INTO "public"."sys_logininfor" VALUES (793, 'admin', '180.98.132.91', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 14:09:46');
INSERT INTO "public"."sys_logininfor" VALUES (794, 'admin', '112.96.209.169', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 14:11:13');
INSERT INTO "public"."sys_logininfor" VALUES (795, 'admin', '27.18.225.166', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 14:13:54');
INSERT INTO "public"."sys_logininfor" VALUES (796, 'admin', '113.218.94.251', 'XX XX', 'Chrome Mobile', 'Android 1.x', '0', '登录成功', '2026-01-19 14:21:10');
INSERT INTO "public"."sys_logininfor" VALUES (797, 'admin', '39.155.237.42', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-19 14:28:59');
INSERT INTO "public"."sys_logininfor" VALUES (798, 'admin', '36.112.207.224', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-19 14:29:33');
INSERT INTO "public"."sys_logininfor" VALUES (799, 'admin', '115.219.156.234', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 14:31:16');
INSERT INTO "public"."sys_logininfor" VALUES (800, 'admin', '211.137.106.151', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-19 14:46:40');
INSERT INTO "public"."sys_logininfor" VALUES (801, 'admin', '39.91.93.84', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 14:52:32');
INSERT INTO "public"."sys_logininfor" VALUES (802, 'admin', '183.134.201.140', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-19 15:22:19');
INSERT INTO "public"."sys_logininfor" VALUES (803, 'admin', '58.20.55.73', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 15:56:35');
INSERT INTO "public"."sys_logininfor" VALUES (804, 'admin', '218.15.132.214', 'XX XX', 'Safari', 'Mac OS X', '0', '登录成功', '2026-01-19 19:31:04');
INSERT INTO "public"."sys_logininfor" VALUES (805, 'admin', '120.227.232.121', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-19 19:50:45');
INSERT INTO "public"."sys_logininfor" VALUES (806, 'admin', '122.190.2.174', 'XX XX', 'Chrome 14', 'Windows 10', '1', '验证码错误', '2026-01-19 20:17:30');
INSERT INTO "public"."sys_logininfor" VALUES (807, 'admin', '122.190.2.174', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 20:17:36');
INSERT INTO "public"."sys_logininfor" VALUES (808, 'admin', '42.93.252.124', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 21:08:49');
INSERT INTO "public"."sys_logininfor" VALUES (809, 'admin', '120.225.5.170', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-19 22:03:01');
INSERT INTO "public"."sys_logininfor" VALUES (810, 'admin', '223.104.54.249', 'XX XX', 'Chrome 12', 'Windows 10', '1', '验证码错误', '2026-01-20 00:00:54');
INSERT INTO "public"."sys_logininfor" VALUES (811, 'admin', '223.104.54.249', 'XX XX', 'Chrome 12', 'Windows 10', '1', '验证码错误', '2026-01-20 00:01:03');
INSERT INTO "public"."sys_logininfor" VALUES (812, 'admin', '223.104.54.249', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-20 00:01:09');
INSERT INTO "public"."sys_logininfor" VALUES (813, 'admin', '36.112.207.224', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-20 09:29:28');
INSERT INTO "public"."sys_logininfor" VALUES (814, 'admin', '113.248.96.236', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-20 09:35:45');
INSERT INTO "public"."sys_logininfor" VALUES (815, 'admin', '58.20.55.73', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-20 09:51:13');
INSERT INTO "public"."sys_logininfor" VALUES (816, 'admin', '113.104.211.29', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-20 10:22:36');
INSERT INTO "public"."sys_logininfor" VALUES (817, 'admin', '27.19.29.177', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-20 10:27:13');
INSERT INTO "public"."sys_logininfor" VALUES (818, 'admin', '223.160.220.50', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-20 10:45:07');
INSERT INTO "public"."sys_logininfor" VALUES (819, 'admin', '1.202.28.98', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-20 10:45:24');
INSERT INTO "public"."sys_logininfor" VALUES (820, 'admin', '36.112.207.224', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-20 10:50:13');
INSERT INTO "public"."sys_logininfor" VALUES (821, 'admin', '36.112.207.224', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-20 15:01:41');
INSERT INTO "public"."sys_logininfor" VALUES (822, 'admin', '112.80.97.66', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-20 15:21:23');
INSERT INTO "public"."sys_logininfor" VALUES (823, 'admin', '211.90.238.30', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-20 15:28:27');
INSERT INTO "public"."sys_logininfor" VALUES (824, 'admin', '123.138.150.176', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-20 16:16:17');
INSERT INTO "public"."sys_logininfor" VALUES (825, 'admin', '119.2.145.69', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-20 16:43:45');
INSERT INTO "public"."sys_logininfor" VALUES (826, 'admin', '36.112.207.224', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-20 16:48:53');
INSERT INTO "public"."sys_logininfor" VALUES (827, 'admin', '117.29.166.14', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-20 16:50:48');
INSERT INTO "public"."sys_logininfor" VALUES (828, 'admin', '118.116.105.162', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-20 17:33:01');
INSERT INTO "public"."sys_logininfor" VALUES (829, 'admin', '222.189.78.226', 'XX XX', 'Firefox 14', 'Ubuntu', '0', '登录成功', '2026-01-20 17:49:25');
INSERT INTO "public"."sys_logininfor" VALUES (830, 'admin', '39.82.85.183', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-20 23:32:04');
INSERT INTO "public"."sys_logininfor" VALUES (831, 'admin', '106.92.128.79', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-21 00:18:06');
INSERT INTO "public"."sys_logininfor" VALUES (832, 'admin', '223.104.128.74', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-21 08:42:47');
INSERT INTO "public"."sys_logininfor" VALUES (833, 'admin', '36.112.207.224', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-21 09:27:34');
INSERT INTO "public"."sys_logininfor" VALUES (834, 'admin', '113.248.96.236', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-21 09:31:40');
INSERT INTO "public"."sys_logininfor" VALUES (835, 'admin', '113.248.96.236', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-21 09:35:23');
INSERT INTO "public"."sys_logininfor" VALUES (836, 'admin', '113.248.96.236', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-21 09:35:26');
INSERT INTO "public"."sys_logininfor" VALUES (837, 'admin', '120.227.232.121', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-21 09:43:29');
INSERT INTO "public"."sys_logininfor" VALUES (838, 'admin', '113.248.96.236', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-21 10:02:29');
INSERT INTO "public"."sys_logininfor" VALUES (839, 'admin', '113.248.96.236', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-21 10:02:31');
INSERT INTO "public"."sys_logininfor" VALUES (840, 'admin', '36.112.207.224', 'XX XX', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-21 10:23:57');
INSERT INTO "public"."sys_logininfor" VALUES (841, 'admin', '36.112.207.224', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-21 10:24:18');
INSERT INTO "public"."sys_logininfor" VALUES (842, 'admin', '171.213.182.230', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-21 10:34:47');
INSERT INTO "public"."sys_logininfor" VALUES (843, 'admin', '58.34.85.66', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-21 10:45:56');
INSERT INTO "public"."sys_logininfor" VALUES (844, 'admin', '58.20.55.73', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-21 10:53:54');
INSERT INTO "public"."sys_logininfor" VALUES (845, 'admin', '124.128.9.243', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-21 13:45:03');
INSERT INTO "public"."sys_logininfor" VALUES (846, 'admin', '113.248.96.236', 'XX XX', 'Unknown', 'Unknown', '0', '登录成功', '2026-01-21 14:17:38');
INSERT INTO "public"."sys_logininfor" VALUES (847, 'admin', '113.248.96.236', 'XX XX', 'Unknown', 'Unknown', '0', '登录成功', '2026-01-21 14:17:50');
INSERT INTO "public"."sys_logininfor" VALUES (848, 'admin', '113.248.96.236', 'XX XX', 'Unknown', 'Unknown', '0', '登录成功', '2026-01-21 14:20:59');
INSERT INTO "public"."sys_logininfor" VALUES (849, 'admin', '113.248.96.236', 'XX XX', 'Unknown', 'Unknown', '0', '登录成功', '2026-01-21 14:21:54');
INSERT INTO "public"."sys_logininfor" VALUES (850, 'admin', '218.26.227.31', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-21 14:41:34');
INSERT INTO "public"."sys_logininfor" VALUES (851, 'admin', '61.178.29.188', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-21 15:18:57');
INSERT INTO "public"."sys_logininfor" VALUES (852, 'admin', '112.232.77.245', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-21 15:33:15');
INSERT INTO "public"."sys_logininfor" VALUES (853, 'admin', '27.19.67.218', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-21 15:44:35');
INSERT INTO "public"."sys_logininfor" VALUES (854, 'admin', '1.192.61.168', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-21 15:49:27');
INSERT INTO "public"."sys_logininfor" VALUES (855, 'admin', '110.189.211.244', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-21 17:04:53');
INSERT INTO "public"."sys_logininfor" VALUES (856, 'admin', '121.11.203.2', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2026-01-21 17:09:29');
INSERT INTO "public"."sys_logininfor" VALUES (857, 'admin', '14.110.78.203', 'XX XX', 'Firefox 14', 'Windows 10', '0', '登录成功', '2026-01-21 19:26:33');
INSERT INTO "public"."sys_logininfor" VALUES (858, 'admin', '171.222.183.233', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-21 22:01:59');
INSERT INTO "public"."sys_logininfor" VALUES (859, 'admin', '106.92.128.79', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-21 22:53:59');
INSERT INTO "public"."sys_logininfor" VALUES (860, 'admin', '223.104.54.114', 'XX XX', 'Chrome 12', 'Windows 10', '1', '验证码错误', '2026-01-22 08:24:15');
INSERT INTO "public"."sys_logininfor" VALUES (861, 'admin', '223.104.54.114', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-22 08:24:21');
INSERT INTO "public"."sys_logininfor" VALUES (862, 'admin', '58.20.55.73', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-22 08:34:13');
INSERT INTO "public"."sys_logininfor" VALUES (863, 'admin', '223.104.54.114', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-22 08:39:39');
INSERT INTO "public"."sys_logininfor" VALUES (864, 'admin', '42.48.47.50', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-22 08:43:42');
INSERT INTO "public"."sys_logininfor" VALUES (865, 'admin', '211.90.238.30', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-22 08:47:32');
INSERT INTO "public"."sys_logininfor" VALUES (866, 'admin', '61.178.29.188', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-22 09:11:34');
INSERT INTO "public"."sys_logininfor" VALUES (867, 'admin', '36.112.207.224', 'XX XX', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-22 09:23:03');
INSERT INTO "public"."sys_logininfor" VALUES (868, 'admin', '36.112.207.224', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-22 09:23:12');
INSERT INTO "public"."sys_logininfor" VALUES (869, 'admin', '124.128.9.243', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-22 09:32:46');
INSERT INTO "public"."sys_logininfor" VALUES (870, 'admin', '36.112.207.224', 'XX XX', 'Chrome 14', 'Mac OS X', '0', '登录成功', '2026-01-22 09:50:32');
INSERT INTO "public"."sys_logininfor" VALUES (871, 'admin', '125.80.202.14', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-22 09:56:34');
INSERT INTO "public"."sys_logininfor" VALUES (872, 'admin', '112.11.185.22', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-22 09:58:53');
INSERT INTO "public"."sys_logininfor" VALUES (873, 'admin', '112.19.176.19', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-22 10:15:34');
INSERT INTO "public"."sys_logininfor" VALUES (874, 'admin', '221.197.232.185', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-22 11:14:57');
INSERT INTO "public"."sys_logininfor" VALUES (875, 'admin', '60.184.32.170', 'XX XX', 'Chrome 14', 'Windows 10', '1', '验证码错误', '2026-01-22 11:58:01');
INSERT INTO "public"."sys_logininfor" VALUES (876, 'admin', '60.184.32.170', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-22 11:58:08');
INSERT INTO "public"."sys_logininfor" VALUES (877, 'admin', '122.245.150.229', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-22 13:19:33');
INSERT INTO "public"."sys_logininfor" VALUES (878, 'admin', '116.7.105.113', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-22 14:13:49');
INSERT INTO "public"."sys_logininfor" VALUES (879, 'admin', '116.30.127.89', 'XX XX', 'Chrome 14', 'Windows 10', '1', '验证码错误', '2026-01-22 14:27:32');
INSERT INTO "public"."sys_logininfor" VALUES (880, 'admin', '116.30.127.89', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-22 14:27:36');
INSERT INTO "public"."sys_logininfor" VALUES (881, 'admin', '183.134.201.140', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-22 15:27:24');
INSERT INTO "public"."sys_logininfor" VALUES (882, 'admin', '218.88.27.233', 'XX XX', 'Chrome 8', 'Windows 10', '1', '验证码错误', '2026-01-22 15:51:33');
INSERT INTO "public"."sys_logininfor" VALUES (883, 'admin', '218.88.27.233', 'XX XX', 'Chrome 8', 'Windows 10', '0', '登录成功', '2026-01-22 15:51:42');
INSERT INTO "public"."sys_logininfor" VALUES (884, 'admin', '222.128.15.33', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-22 17:28:19');
INSERT INTO "public"."sys_logininfor" VALUES (885, 'admin', '222.128.15.33', 'XX XX', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-22 17:32:10');
INSERT INTO "public"."sys_logininfor" VALUES (886, 'admin', '121.11.203.2', 'XX XX', 'Chrome 10', 'Windows 10', '1', '验证码错误', '2026-01-22 17:58:02');
INSERT INTO "public"."sys_logininfor" VALUES (887, 'admin', '121.11.203.2', 'XX XX', 'Chrome 10', 'Windows 10', '1', '验证码错误', '2026-01-22 17:58:07');
INSERT INTO "public"."sys_logininfor" VALUES (888, 'admin', '121.11.203.2', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2026-01-22 17:58:13');
INSERT INTO "public"."sys_logininfor" VALUES (889, 'admin', '106.92.128.79', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-22 20:31:20');
INSERT INTO "public"."sys_logininfor" VALUES (890, 'admin', '117.88.27.87', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-22 22:26:40');
INSERT INTO "public"."sys_logininfor" VALUES (891, 'admin', '61.52.175.172', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-23 09:05:34');
INSERT INTO "public"."sys_logininfor" VALUES (892, 'admin', '124.128.9.243', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-23 09:36:39');
INSERT INTO "public"."sys_logininfor" VALUES (893, 'admin', '36.112.207.224', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-23 09:52:18');
INSERT INTO "public"."sys_logininfor" VALUES (894, 'admin', '175.31.193.226', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-23 10:08:37');
INSERT INTO "public"."sys_logininfor" VALUES (895, 'admin', '180.110.156.109', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-23 10:17:42');
INSERT INTO "public"."sys_logininfor" VALUES (896, 'admin', '125.80.202.14', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-23 10:22:39');
INSERT INTO "public"."sys_logininfor" VALUES (897, 'admin', '27.19.67.218', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-23 10:27:54');
INSERT INTO "public"."sys_logininfor" VALUES (898, 'admin', '124.128.9.243', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-23 10:28:04');
INSERT INTO "public"."sys_logininfor" VALUES (899, 'admin', '223.104.54.12', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-23 10:59:59');
INSERT INTO "public"."sys_logininfor" VALUES (900, 'admin', '61.178.29.188', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-23 11:39:19');
INSERT INTO "public"."sys_logininfor" VALUES (901, 'admin', '124.128.9.243', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-23 13:54:11');
INSERT INTO "public"."sys_logininfor" VALUES (902, 'admin', '120.227.232.141', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-23 14:30:05');
INSERT INTO "public"."sys_logininfor" VALUES (903, 'admin', '219.145.102.210', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-23 15:03:58');
INSERT INTO "public"."sys_logininfor" VALUES (904, 'admin', '58.20.55.73', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-23 16:01:31');
INSERT INTO "public"."sys_logininfor" VALUES (905, 'admin', '39.129.4.177', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-23 17:37:14');
INSERT INTO "public"."sys_logininfor" VALUES (906, 'admin', '112.29.108.9', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-23 18:59:51');
INSERT INTO "public"."sys_logininfor" VALUES (907, 'admin', '112.11.185.22', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-24 08:42:36');
INSERT INTO "public"."sys_logininfor" VALUES (908, 'admin', '113.248.165.12', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-24 10:45:21');
INSERT INTO "public"."sys_logininfor" VALUES (909, 'admin', '36.143.33.145', 'XX XX', 'Chrome 12', 'Windows 10', '1', '验证码错误', '2026-01-24 14:07:19');
INSERT INTO "public"."sys_logininfor" VALUES (910, 'admin', '36.143.33.145', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2026-01-24 14:07:23');
INSERT INTO "public"."sys_logininfor" VALUES (911, 'admin', '120.193.227.118', 'XX XX', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-24 14:08:11');
INSERT INTO "public"."sys_logininfor" VALUES (912, 'admin', '27.19.67.218', 'XX XX', 'Chrome 14', 'Windows 10', '1', '验证码错误', '2026-01-24 15:54:50');
INSERT INTO "public"."sys_logininfor" VALUES (913, 'admin', '27.19.67.218', 'XX XX', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-24 15:54:55');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_menu";
CREATE TABLE "public"."sys_menu" (
  "menu_id" int8 NOT NULL DEFAULT nextval('sys_menu_menu_id_seq'::regclass),
  "menu_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "parent_id" int8 DEFAULT 0,
  "order_num" int4 DEFAULT 0,
  "path" varchar(200) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "component" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "query" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "route_name" varchar(50) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "is_frame" varchar(32) COLLATE "pg_catalog"."default" DEFAULT 1,
  "is_cache" varchar(32) COLLATE "pg_catalog"."default" DEFAULT 0,
  "menu_type" char(1) COLLATE "pg_catalog"."default" DEFAULT ''::bpchar,
  "visible" char(1) COLLATE "pg_catalog"."default" DEFAULT '0'::bpchar,
  "status" char(1) COLLATE "pg_catalog"."default" DEFAULT '0'::bpchar,
  "perms" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "icon" varchar(100) COLLATE "pg_catalog"."default" DEFAULT '#'::character varying,
  "create_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "update_time" timestamp(6),
  "remark" varchar(500) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
COMMENT ON COLUMN "public"."sys_menu"."menu_id" IS '菜单ID';
COMMENT ON COLUMN "public"."sys_menu"."menu_name" IS '菜单名称';
COMMENT ON COLUMN "public"."sys_menu"."parent_id" IS '父菜单ID';
COMMENT ON COLUMN "public"."sys_menu"."order_num" IS '显示顺序';
COMMENT ON COLUMN "public"."sys_menu"."path" IS '路由地址';
COMMENT ON COLUMN "public"."sys_menu"."component" IS '组件路径';
COMMENT ON COLUMN "public"."sys_menu"."query" IS '路由参数';
COMMENT ON COLUMN "public"."sys_menu"."route_name" IS '路由名称';
COMMENT ON COLUMN "public"."sys_menu"."is_frame" IS '是否为外链（0是 1否）';
COMMENT ON COLUMN "public"."sys_menu"."is_cache" IS '是否缓存（0缓存 1不缓存）';
COMMENT ON COLUMN "public"."sys_menu"."menu_type" IS '菜单类型（M目录 C菜单 F按钮）';
COMMENT ON COLUMN "public"."sys_menu"."visible" IS '菜单状态（0显示 1隐藏）';
COMMENT ON COLUMN "public"."sys_menu"."status" IS '菜单状态（0正常 1停用）';
COMMENT ON COLUMN "public"."sys_menu"."perms" IS '权限标识';
COMMENT ON COLUMN "public"."sys_menu"."icon" IS '菜单图标';
COMMENT ON COLUMN "public"."sys_menu"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."sys_menu"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sys_menu"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."sys_menu"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."sys_menu"."remark" IS '备注';
COMMENT ON TABLE "public"."sys_menu" IS '菜单权限表';

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO "public"."sys_menu" VALUES (1, '系统管理', 0, 3, 'system', NULL, '', '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2025-09-15 11:09:57', 'admin', '2026-01-14 09:21:14', '系统管理目录');
INSERT INTO "public"."sys_menu" VALUES (2, '系统监控', 0, 2, 'monitor', NULL, '', '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', '2025-09-15 11:09:57', '', NULL, '系统监控目录');
INSERT INTO "public"."sys_menu" VALUES (3, '系统工具', 0, 4, 'tool', NULL, '', '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2025-09-15 11:09:57', 'admin', '2026-01-14 09:21:21', '系统工具目录');
INSERT INTO "public"."sys_menu" VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user', 'admin', '2025-09-15 11:09:57', '', NULL, '用户管理菜单');
INSERT INTO "public"."sys_menu" VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 'admin', '2025-09-15 11:09:57', '', NULL, '角色管理菜单');
INSERT INTO "public"."sys_menu" VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table', 'admin', '2025-09-15 11:09:57', '', NULL, '菜单管理菜单');
INSERT INTO "public"."sys_menu" VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'tree', 'admin', '2025-09-15 11:09:57', '', NULL, '部门管理菜单');
INSERT INTO "public"."sys_menu" VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 'admin', '2025-09-15 11:09:57', '', NULL, '岗位管理菜单');
INSERT INTO "public"."sys_menu" VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', '', 1, 0, 'C', '0', '0', 'system:dict:list', 'dict', 'admin', '2025-09-15 11:09:57', '', NULL, '字典管理菜单');
INSERT INTO "public"."sys_menu" VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit', 'admin', '2025-09-15 11:09:57', '', NULL, '参数设置菜单');
INSERT INTO "public"."sys_menu" VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', '', 1, 0, 'C', '0', '0', 'system:notice:list', 'message', 'admin', '2025-09-15 11:09:57', '', NULL, '通知公告菜单');
INSERT INTO "public"."sys_menu" VALUES (108, '日志管理', 1, 9, 'log', '', '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', '2025-09-15 11:09:57', '', NULL, '日志管理菜单');
INSERT INTO "public"."sys_menu" VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', '', 1, 0, 'C', '0', '0', 'monitor:online:list', 'online', 'admin', '2025-09-15 11:09:57', '', NULL, '在线用户菜单');
INSERT INTO "public"."sys_menu" VALUES (110, '定时任务', 2, 2, 'job', 'monitor/job/index', '', '', 1, 0, 'C', '0', '0', 'monitor:job:list', 'job', 'admin', '2025-09-15 11:09:57', '', NULL, '定时任务菜单');
INSERT INTO "public"."sys_menu" VALUES (111, '数据监控', 2, 3, 'druid', 'monitor/druid/index', '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list', 'druid', 'admin', '2025-09-15 11:09:57', '', NULL, '数据监控菜单');
INSERT INTO "public"."sys_menu" VALUES (112, '服务监控', 2, 4, 'server', 'monitor/server/index', '', '', 1, 0, 'C', '0', '0', 'monitor:server:list', 'server', 'admin', '2025-09-15 11:09:57', '', NULL, '服务监控菜单');
INSERT INTO "public"."sys_menu" VALUES (113, '缓存监控', 2, 5, 'cache', 'monitor/cache/index', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis', 'admin', '2025-09-15 11:09:57', '', NULL, '缓存监控菜单');
INSERT INTO "public"."sys_menu" VALUES (114, '缓存列表', 2, 6, 'cacheList', 'monitor/cache/list', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis-list', 'admin', '2025-09-15 11:09:57', '', NULL, '缓存列表菜单');
INSERT INTO "public"."sys_menu" VALUES (115, '表单构建', 3, 1, 'build', 'tool/build/index', '', '', 1, 0, 'C', '0', '0', 'tool:build:list', 'build', 'admin', '2025-09-15 11:09:57', '', NULL, '表单构建菜单');
INSERT INTO "public"."sys_menu" VALUES (116, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin', '2025-09-15 11:09:57', '', NULL, '代码生成菜单');
INSERT INTO "public"."sys_menu" VALUES (117, '系统接口', 3, 3, 'swagger', 'tool/swagger/index', '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list', 'swagger', 'admin', '2025-09-15 11:09:57', '', NULL, '系统接口菜单');
INSERT INTO "public"."sys_menu" VALUES (500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list', 'form', 'admin', '2025-09-15 11:09:57', '', NULL, '操作日志菜单');
INSERT INTO "public"."sys_menu" VALUES (501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor', 'admin', '2025-09-15 11:09:57', '', NULL, '登录日志菜单');
INSERT INTO "public"."sys_menu" VALUES (1000, '用户查询', 100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1001, '用户新增', 100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1002, '用户修改', 100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1003, '用户删除', 100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1004, '用户导出', 100, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1005, '用户导入', 100, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1006, '重置密码', 100, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1007, '角色查询', 101, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1008, '角色新增', 101, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1009, '角色修改', 101, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1010, '角色删除', 101, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1011, '角色导出', 101, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1012, '菜单查询', 102, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1013, '菜单新增', 102, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1014, '菜单修改', 102, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1015, '菜单删除', 102, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1016, '部门查询', 103, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1017, '部门新增', 103, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1018, '部门修改', 103, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1019, '部门删除', 103, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1020, '岗位查询', 104, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1021, '岗位新增', 104, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1022, '岗位修改', 104, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1023, '岗位删除', 104, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1024, '岗位导出', 104, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1025, '字典查询', 105, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1026, '字典新增', 105, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1027, '字典修改', 105, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1028, '字典删除', 105, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1029, '字典导出', 105, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1030, '参数查询', 106, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1031, '参数新增', 106, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1032, '参数修改', 106, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1033, '参数删除', 106, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1034, '参数导出', 106, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1035, '公告查询', 107, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1036, '公告新增', 107, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1037, '公告修改', 107, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1038, '公告删除', 107, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1039, '操作查询', 500, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1040, '操作删除', 500, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1041, '日志导出', 500, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1042, '登录查询', 501, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1043, '登录删除', 501, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1044, '日志导出', 501, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1045, '账户解锁', 501, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1046, '在线查询', 109, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1047, '批量强退', 109, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1048, '单条强退', 109, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1049, '任务查询', 110, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1050, '任务新增', 110, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1051, '任务修改', 110, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1052, '任务删除', 110, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1053, '状态修改', 110, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1054, '任务导出', 110, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1055, '生成查询', 116, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1056, '生成修改', 116, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1057, '生成删除', 116, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1058, '导入代码', 116, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1059, '预览代码', 116, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (1060, '生成代码', 116, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2000, '网络组件', 2037, 1, 'component', 'business/component/index', NULL, '', 1, 0, 'C', '0', '0', 'business:component:list', 'netComponent', 'admin', '2025-09-18 13:58:11', 'admin', '2025-10-30 11:19:43', '网络组件菜单');
INSERT INTO "public"."sys_menu" VALUES (2001, '网络组件查询', 2000, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:component:query', '#', 'admin', '2025-09-18 13:58:11', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2002, '网络组件新增', 2000, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:component:add', '#', 'admin', '2025-09-18 13:58:11', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2003, '网络组件修改', 2000, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:component:edit', '#', 'admin', '2025-09-18 13:58:11', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2004, '网络组件删除', 2000, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:component:remove', '#', 'admin', '2025-09-18 13:58:11', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2005, '网络组件导出', 2000, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:component:export', '#', 'admin', '2025-09-18 13:58:11', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2006, '设备管理', 2036, 1, 'device', 'business/device/index', NULL, '', 1, 0, 'C', '0', '0', 'business:device:list', 'device', 'admin', '2025-09-18 13:58:25', 'admin', '2025-12-12 10:46:51', '设备菜单');
INSERT INTO "public"."sys_menu" VALUES (2007, '设备查询', 2006, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:device:query', '#', 'admin', '2025-09-18 13:58:25', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2008, '设备新增', 2006, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:device:add', '#', 'admin', '2025-09-18 13:58:25', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2009, '设备修改', 2006, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:device:edit', '#', 'admin', '2025-09-18 13:58:25', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2010, '设备删除', 2006, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:device:remove', '#', 'admin', '2025-09-18 13:58:25', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2011, '设备导出', 2006, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:device:export', '#', 'admin', '2025-09-18 13:58:25', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2012, '产品管理', 2036, 0, 'product', 'business/product/index', NULL, '', 1, 0, 'C', '0', '0', 'business:product:list', 'product', 'admin', '2025-09-18 13:58:31', 'admin', '2025-12-12 10:46:46', '产品菜单');
INSERT INTO "public"."sys_menu" VALUES (2013, '产品查询', 2012, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:product:query', '#', 'admin', '2025-09-18 13:58:31', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2014, '产品新增', 2012, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:product:add', '#', 'admin', '2025-09-18 13:58:31', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2015, '产品修改', 2012, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:product:edit', '#', 'admin', '2025-09-18 13:58:31', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2016, '产品删除', 2012, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:product:remove', '#', 'admin', '2025-09-18 13:58:31', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2017, '产品导出', 2012, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:product:export', '#', 'admin', '2025-09-18 13:58:31', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2030, '协议管理', 2037, 1, 'protocol', 'business/protocol/index', NULL, '', 1, 0, 'C', '0', '0', 'business:protocol:list', 'protocol', 'admin', '2025-09-18 14:00:37', 'admin', '2025-10-30 11:19:51', '协议管理菜单');
INSERT INTO "public"."sys_menu" VALUES (2031, '协议管理查询', 2030, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:protocol:query', '#', 'admin', '2025-09-18 14:00:37', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2032, '协议管理新增', 2030, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:protocol:add', '#', 'admin', '2025-09-18 14:00:37', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2033, '协议管理修改', 2030, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:protocol:edit', '#', 'admin', '2025-09-18 14:00:37', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2034, '协议管理删除', 2030, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:protocol:remove', '#', 'admin', '2025-09-18 14:00:37', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2035, '协议管理导出', 2030, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:protocol:export', '#', 'admin', '2025-09-18 14:00:37', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2036, '设备管理', 0, 0, '/deviceManage', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'device', 'admin', '2025-09-18 14:03:01', 'admin', '2025-10-30 11:20:19', '');
INSERT INTO "public"."sys_menu" VALUES (2037, '网络组件', 0, 1, '/componentManage', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'netComponent', 'admin', '2025-09-18 14:04:33', 'admin', '2026-01-14 09:20:58', '');
INSERT INTO "public"."sys_menu" VALUES (2041, '规则引擎', 0, 2, 'engine', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'tree', 'admin', '2025-11-13 10:31:11', 'admin', '2026-01-14 09:21:11', '');
INSERT INTO "public"."sys_menu" VALUES (2042, '设备联动', 2041, 2, 'devicelink', 'business/devicelink/index', NULL, 'Devicelink', 1, 1, 'C', '0', '0', '', 'devicelink', 'admin', '2025-11-13 10:36:58', 'admin', '2025-12-23 13:34:54', '');
INSERT INTO "public"."sys_menu" VALUES (2043, '告警记录', 2041, 4, 'linkageRecord', 'business/linkageRecord/index', NULL, 'LinkageRecord', 1, 1, 'C', '0', '0', '', 'build', 'admin', '2025-11-26 17:38:59', 'admin', '2025-12-08 17:52:34', '');
INSERT INTO "public"."sys_menu" VALUES (2044, '定时任务', 2041, 3, 'scheduledEngine', 'business/scheduledEngine/index', NULL, 'ScheduledEngine', 1, 1, 'C', '0', '0', '', 'cascader', 'admin', '2025-12-08 17:54:22', 'admin', '2025-12-08 17:55:30', '');
INSERT INTO "public"."sys_menu" VALUES (2045, '地图服务', 2036, 3, 'map', 'business/map/index', NULL, 'Map', 1, 0, 'C', '0', '0', NULL, 'guide', 'admin', '2025-12-12 10:47:55', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2046, '数据转发', 2041, 1, 'engine', 'business/engine/index', NULL, 'Engine', 1, 1, 'C', '0', '0', NULL, 'engine', 'admin', '2025-12-23 13:35:44', '', NULL, '');
INSERT INTO "public"."sys_menu" VALUES (2047, '官方文档', 0, 5, 'http://47.109.145.72:18000/', NULL, NULL, '', 0, 0, 'M', '0', '0', '', 'documentation', 'admin', '2026-01-13 10:37:37', 'admin', '2026-01-14 09:21:25', '');
INSERT INTO "public"."sys_menu" VALUES (2050, '设备分组', 2036, 4, 'deviceGroup', 'business/deviceGroup/index', NULL, 'DeviceGroup', 1, 1, 'C', '0', '0', NULL, 'list', 'admin', '2026-01-18 18:58:13', '', NULL, '');

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_notice";
CREATE TABLE "public"."sys_notice" (
  "notice_id" int4 NOT NULL DEFAULT nextval('sys_notice_notice_id_seq'::regclass),
  "notice_title" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "notice_type" char(1) COLLATE "pg_catalog"."default" NOT NULL,
  "notice_content" bytea,
  "status" char(1) COLLATE "pg_catalog"."default" DEFAULT '0'::bpchar,
  "create_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "update_time" timestamp(6),
  "remark" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."sys_notice"."notice_id" IS '公告ID';
COMMENT ON COLUMN "public"."sys_notice"."notice_title" IS '公告标题';
COMMENT ON COLUMN "public"."sys_notice"."notice_type" IS '公告类型（1通知 2公告）';
COMMENT ON COLUMN "public"."sys_notice"."notice_content" IS '公告内容';
COMMENT ON COLUMN "public"."sys_notice"."status" IS '公告状态（0正常 1关闭）';
COMMENT ON COLUMN "public"."sys_notice"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."sys_notice"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sys_notice"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."sys_notice"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."sys_notice"."remark" IS '备注';
COMMENT ON TABLE "public"."sys_notice" IS '通知公告表';

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO "public"."sys_notice" VALUES (1, '温馨提醒：2018-07-01 若依新版本发布啦', '2', E'\\346\\226\\260\\347\\211\\210\\346\\234\\254\\345\\206\\205\\345\\256\\271', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '管理员');
INSERT INTO "public"."sys_notice" VALUES (2, '维护通知：2018-07-01 若依系统凌晨维护', '1', E'\\347\\273\\264\\346\\212\\244\\345\\206\\205\\345\\256\\271', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '管理员');

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_oper_log";
CREATE TABLE "public"."sys_oper_log" (
  "oper_id" int8 NOT NULL DEFAULT nextval('sys_oper_log_oper_id_seq'::regclass),
  "title" varchar(50) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "business_type" int4 DEFAULT 0,
  "method" varchar(200) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "request_method" varchar(10) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "operator_type" int4 DEFAULT 0,
  "oper_name" varchar(50) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "dept_name" varchar(50) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "oper_url" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "oper_ip" varchar(128) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "oper_location" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "oper_param" varchar(2000) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "json_result" varchar(2000) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "status" int4 DEFAULT 0,
  "error_msg" varchar(2000) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "oper_time" timestamp(6),
  "cost_time" int8 DEFAULT 0
)
;
COMMENT ON COLUMN "public"."sys_oper_log"."oper_id" IS '日志主键';
COMMENT ON COLUMN "public"."sys_oper_log"."title" IS '模块标题';
COMMENT ON COLUMN "public"."sys_oper_log"."business_type" IS '业务类型（0其它 1新增 2修改 3删除）';
COMMENT ON COLUMN "public"."sys_oper_log"."method" IS '方法名称';
COMMENT ON COLUMN "public"."sys_oper_log"."request_method" IS '请求方式';
COMMENT ON COLUMN "public"."sys_oper_log"."operator_type" IS '操作类别（0其它 1后台用户 2手机端用户）';
COMMENT ON COLUMN "public"."sys_oper_log"."oper_name" IS '操作人员';
COMMENT ON COLUMN "public"."sys_oper_log"."dept_name" IS '部门名称';
COMMENT ON COLUMN "public"."sys_oper_log"."oper_url" IS '请求URL';
COMMENT ON COLUMN "public"."sys_oper_log"."oper_ip" IS '主机地址';
COMMENT ON COLUMN "public"."sys_oper_log"."oper_location" IS '操作地点';
COMMENT ON COLUMN "public"."sys_oper_log"."oper_param" IS '请求参数';
COMMENT ON COLUMN "public"."sys_oper_log"."json_result" IS '返回参数';
COMMENT ON COLUMN "public"."sys_oper_log"."status" IS '操作状态（0正常 1异常）';
COMMENT ON COLUMN "public"."sys_oper_log"."error_msg" IS '错误消息';
COMMENT ON COLUMN "public"."sys_oper_log"."oper_time" IS '操作时间';
COMMENT ON COLUMN "public"."sys_oper_log"."cost_time" IS '消耗时间';
COMMENT ON TABLE "public"."sys_oper_log" IS '操作日志记录';

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO "public"."sys_oper_log" VALUES (100, '参数管理', 2, 'com.labdatahub.web.controller.system.SysConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/config', '127.0.0.1', '内网IP', '{"configId":4,"configKey":"sys.account.captchaEnabled","configName":"账号自助-验证码开关","configType":"Y","configValue":"false","createBy":"admin","createTime":"2025-09-15 11:09:58","params":{},"remark":"是否开启验证码功能（true开启，false关闭）","updateBy":"admin"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 13:40:46', 31);
INSERT INTO "public"."sys_oper_log" VALUES (101, '代码生成', 6, 'com.labdatahub.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{"tables":"labdatahub_protocol,labdatahub_component,labdatahub_properties,labdatahub_product,labdatahub"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 13:41:42', 276);
INSERT INTO "public"."sys_oper_log" VALUES (102, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{"businessName":"component","className":"LabdatahubComponent","columns":[{"capJavaField":"Id","columnComment":"id","columnId":1,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":1,"updateBy":"","usableColumn":false},{"capJavaField":"Name","columnComment":"组件名称","columnId":2,"columnName":"name","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"name","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":2,"superColumn":false,"tableId":1,"updateBy":"","usableColumn":false},{"capJavaField":"NetType","columnComment":"网络类型","columnId":3,"columnName":"net_type","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":true,"htmlType":"select","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"netType","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":1,"updateBy":"","usableColumn":false},{"capJavaField":"IpAddr","columnComment":"IP地址","columnId":4,"columnName":"ip_addr","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","java', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 13:46:18', 87);
INSERT INTO "public"."sys_oper_log" VALUES (103, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{"businessName":"product","className":"LabdatahubProduct","columns":[{"capJavaField":"Id","columnComment":"id","columnId":16,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":2,"updateBy":"","usableColumn":false},{"capJavaField":"ProductSn","columnComment":"产品编码","columnId":17,"columnName":"product_sn","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"productSn","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":2,"updateBy":"","usableColumn":false},{"capJavaField":"ProductName","columnComment":"产品名称","columnId":18,"columnName":"product_name","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"productName","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":3,"superColumn":false,"tableId":2,"updateBy":"","usableColumn":false},{"capJavaField":"LinkMethodId","columnComment":"接入方式id","columnId":19,"columnName":"link_method_id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 13:46:27', 69);
INSERT INTO "public"."sys_oper_log" VALUES (104, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{"businessName":"properties","className":"LabdatahubProperties","columns":[{"capJavaField":"Id","columnComment":"id","columnId":31,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":3,"updateBy":"","usableColumn":false},{"capJavaField":"BelongId","columnComment":"归属id 产品/设备","columnId":32,"columnName":"belong_id","columnType":"varchar(50)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"belongId","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":3,"updateBy":"","usableColumn":false},{"capJavaField":"BelongType","columnComment":"归属类型 0-产品 1-设备","columnId":33,"columnName":"belong_type","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":true,"htmlType":"select","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"belongType","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":3,"updateBy":"","usableColumn":false},{"capJavaField":"Identifier","columnComment":"属性标识符，如 temperature, status","columnId":34,"columnName":"identifier","columnType":"varchar(100)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isI', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 13:46:33', 42);
INSERT INTO "public"."sys_oper_log" VALUES (105, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{"businessName":"protocol","className":"LabdatahubProtocol","columns":[{"capJavaField":"Id","columnComment":"id","columnId":40,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":4,"updateBy":"","usableColumn":false},{"capJavaField":"ProtocolName","columnComment":"协议名称","columnId":41,"columnName":"protocol_name","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"protocolName","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":2,"superColumn":false,"tableId":4,"updateBy":"","usableColumn":false},{"capJavaField":"LocalUrl","columnComment":"本地存储路径","columnId":42,"columnName":"local_url","columnType":"text","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":true,"htmlType":"textarea","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"localUrl","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":4,"updateBy":"","usableColumn":false},{"capJavaField":"MainClassPath","columnComment":"解析类入口","columnId":43,"columnName":"main_class_path","columnType":"text","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":true,"htmlType":"textarea","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 13:46:39', 34);
INSERT INTO "public"."sys_oper_log" VALUES (106, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{"businessName":"device","className":"ThingllinksDevice","columns":[{"capJavaField":"Id","columnComment":"id","columnId":47,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":5,"updateBy":"","usableColumn":false},{"capJavaField":"DeviceId","columnComment":"设备id","columnId":48,"columnName":"device_id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"deviceId","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":5,"updateBy":"","usableColumn":false},{"capJavaField":"DeviceSn","columnComment":"设备编码","columnId":49,"columnName":"device_sn","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"deviceSn","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":5,"updateBy":"","usableColumn":false},{"capJavaField":"DeviceName","columnComment":"设备名称","columnId":50,"columnName":"device_name","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-18 13:41:42","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","i', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 13:46:46', 73);
INSERT INTO "public"."sys_oper_log" VALUES (107, '代码生成', 8, 'com.labdatahub.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"labdatahub_component,labdatahub_product,labdatahub_properties,labdatahub_protocol,labdatahub"}', NULL, 0, NULL, '2025-09-18 13:46:50', 428);
INSERT INTO "public"."sys_oper_log" VALUES (108, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2024', '127.0.0.1', '内网IP', '2024', '{"msg":"存在子菜单,不允许删除","code":601}', 0, NULL, '2025-09-18 14:00:07', 7);
INSERT INTO "public"."sys_oper_log" VALUES (109, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2026', '127.0.0.1', '内网IP', '2026', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:00:12', 18);
INSERT INTO "public"."sys_oper_log" VALUES (110, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2025', '127.0.0.1', '内网IP', '2025', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:00:14', 23);
INSERT INTO "public"."sys_oper_log" VALUES (111, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2027', '127.0.0.1', '内网IP', '2027', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:00:17', 17);
INSERT INTO "public"."sys_oper_log" VALUES (112, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2028', '127.0.0.1', '内网IP', '2028', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:00:19', 19);
INSERT INTO "public"."sys_oper_log" VALUES (113, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2029', '127.0.0.1', '内网IP', '2029', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:00:21', 18);
INSERT INTO "public"."sys_oper_log" VALUES (114, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2024', '127.0.0.1', '内网IP', '2024', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:00:23', 18);
INSERT INTO "public"."sys_oper_log" VALUES (115, '菜单管理', 1, 'com.labdatahub.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"createBy":"admin","icon":"international","isCache":"0","isFrame":"1","menuName":"设备管理","menuType":"M","orderNum":0,"params":{},"parentId":0,"path":"/deviceManage","status":"0","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:03:01', 13);
INSERT INTO "public"."sys_oper_log" VALUES (195, '告警配置', 3, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/warnConfig/1976932019586027521', '127.0.0.1', '内网IP', '["1976932019586027521"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 17:12:07', 21);
INSERT INTO "public"."sys_oper_log" VALUES (116, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"business/device/index","createTime":"2025-09-18 13:58:25","icon":"#","isCache":"0","isFrame":"1","menuId":2006,"menuName":"设备","menuType":"C","orderNum":1,"params":{},"parentId":2036,"path":"device","perms":"business:device:list","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:03:15', 13);
INSERT INTO "public"."sys_oper_log" VALUES (117, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"business/product/index","createTime":"2025-09-18 13:58:31","icon":"#","isCache":"0","isFrame":"1","menuId":2012,"menuName":"产品","menuType":"C","orderNum":1,"params":{},"parentId":2036,"path":"product","perms":"business:product:list","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:03:21', 14);
INSERT INTO "public"."sys_oper_log" VALUES (118, '菜单管理', 1, 'com.labdatahub.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"createBy":"admin","icon":"component","isCache":"0","isFrame":"1","menuName":"网络组件","menuType":"M","orderNum":0,"params":{},"parentId":0,"path":"/componentManage","status":"0","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:04:33', 14);
INSERT INTO "public"."sys_oper_log" VALUES (119, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"business/component/index","createTime":"2025-09-18 13:58:11","icon":"#","isCache":"0","isFrame":"1","menuId":2000,"menuName":"网络组件","menuType":"C","orderNum":1,"params":{},"parentId":2037,"path":"component","perms":"business:component:list","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:05:12', 14);
INSERT INTO "public"."sys_oper_log" VALUES (120, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"business/protocol/index","createTime":"2025-09-18 14:00:37","icon":"#","isCache":"0","isFrame":"1","menuId":2030,"menuName":"协议管理","menuType":"C","orderNum":1,"params":{},"parentId":2037,"path":"protocol","perms":"business:protocol:list","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:05:33', 12);
INSERT INTO "public"."sys_oper_log" VALUES (121, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2018', '127.0.0.1', '内网IP', '2018', '{"msg":"存在子菜单,不允许删除","code":601}', 0, NULL, '2025-09-18 14:05:40', 5);
INSERT INTO "public"."sys_oper_log" VALUES (122, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2019', '127.0.0.1', '内网IP', '2019', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:05:44', 11);
INSERT INTO "public"."sys_oper_log" VALUES (123, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2020', '127.0.0.1', '内网IP', '2020', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:05:46', 11);
INSERT INTO "public"."sys_oper_log" VALUES (124, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2022', '127.0.0.1', '内网IP', '2022', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:05:48', 14);
INSERT INTO "public"."sys_oper_log" VALUES (125, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2021', '127.0.0.1', '内网IP', '2021', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:05:50', 35);
INSERT INTO "public"."sys_oper_log" VALUES (126, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2023', '127.0.0.1', '内网IP', '2023', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:05:53', 16);
INSERT INTO "public"."sys_oper_log" VALUES (127, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2018', '127.0.0.1', '内网IP', '2018', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:05:55', 16);
INSERT INTO "public"."sys_oper_log" VALUES (128, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"business/product/index","createTime":"2025-09-18 13:58:31","icon":"#","isCache":"0","isFrame":"1","menuId":2012,"menuName":"产品","menuType":"C","orderNum":0,"params":{},"parentId":2036,"path":"product","perms":"business:product:list","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-18 14:06:07', 13);
INSERT INTO "public"."sys_oper_log" VALUES (129, '代码生成', 6, 'com.labdatahub.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{"tables":"labdatahub_device_logs"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-22 15:42:02', 58);
INSERT INTO "public"."sys_oper_log" VALUES (130, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{"businessName":"logs","className":"LabdatahubDeviceLogs","columns":[{"capJavaField":"Id","columnComment":"id","columnId":63,"columnName":"id","columnType":"bigint","createBy":"admin","createTime":"2025-09-22 15:42:02","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"Long","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":6,"updateBy":"","usableColumn":false},{"capJavaField":"DeviceSn","columnComment":"设备sn","columnId":64,"columnName":"device_sn","columnType":"varchar(255)","createBy":"admin","createTime":"2025-09-22 15:42:02","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"deviceSn","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":6,"updateBy":"","usableColumn":false},{"capJavaField":"ReportTime","columnComment":"上报时间","columnId":65,"columnName":"report_time","columnType":"datetime","createBy":"admin","createTime":"2025-09-22 15:42:02","dictType":"","edit":true,"htmlType":"datetime","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"reportTime","javaType":"Date","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":6,"updateBy":"","usableColumn":false},{"capJavaField":"Properties","columnComment":"属性json","columnId":66,"columnName":"properties","columnType":"text","createBy":"admin","createTime":"2025-09-22 15:42:02","dictType":"","edit":true,"htmlType":"textarea","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequire', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-22 15:42:16', 37);
INSERT INTO "public"."sys_oper_log" VALUES (131, '代码生成', 8, 'com.labdatahub.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"labdatahub_device_logs"}', NULL, 0, NULL, '2025-09-22 15:42:30', 201);
INSERT INTO "public"."sys_oper_log" VALUES (132, '菜单管理', 1, 'com.labdatahub.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"/business/component/detail","createBy":"admin","icon":"cascader","isCache":"0","isFrame":"1","menuName":"组件详情","menuType":"C","orderNum":0,"params":{},"parentId":2000,"path":"/componentManage/component-detail","routeName":"ComponentDetail","status":"0","visible":"1"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-23 10:13:52', 28);
INSERT INTO "public"."sys_oper_log" VALUES (133, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"/business/component/detail","createTime":"2025-09-23 10:13:52","icon":"cascader","isCache":"0","isFrame":"1","menuId":2038,"menuName":"组件详情","menuType":"C","orderNum":0,"params":{},"parentId":2000,"path":"/componentManage/component-detail","perms":"","routeName":"ComponentDetail","status":"0","updateBy":"admin","visible":"1"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-23 10:16:20', 21);
INSERT INTO "public"."sys_oper_log" VALUES (272, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.251.75.185', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2025/11/13/b84f7defa5604318b21a3465117fb861.jpg","code":200}', 0, NULL, '2025-11-13 10:00:53', 133);
INSERT INTO "public"."sys_oper_log" VALUES (134, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"/business/component/detail","createTime":"2025-09-23 10:13:52","icon":"cascader","isCache":"0","isFrame":"1","menuId":2038,"menuName":"组件详情","menuType":"C","orderNum":0,"params":{},"parentId":2000,"path":"/componentManage/component-detail","perms":"","routeName":"ComponentDetail","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-23 10:17:38', 13);
INSERT INTO "public"."sys_oper_log" VALUES (135, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"/business/component/detail","createTime":"2025-09-23 10:13:52","icon":"cascader","isCache":"0","isFrame":"1","menuId":2038,"menuName":"组件详情","menuType":"C","orderNum":0,"params":{},"parentId":2000,"path":"component-detail","perms":"","routeName":"ComponentDetail","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-23 10:20:18', 13);
INSERT INTO "public"."sys_oper_log" VALUES (136, '菜单管理', 1, 'com.labdatahub.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"/system/dict","createBy":"admin","isCache":"0","isFrame":"1","menuName":"213","menuType":"C","orderNum":1,"params":{},"parentId":2037,"path":"index","routeName":"Index","status":"0","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-23 10:21:30', 9);
INSERT INTO "public"."sys_oper_log" VALUES (137, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2039', '127.0.0.1', '内网IP', '2039', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-23 10:21:59', 22);
INSERT INTO "public"."sys_oper_log" VALUES (138, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2038', '127.0.0.1', '内网IP', '2038', '{"msg":"操作成功","code":200}', 0, NULL, '2025-09-23 10:22:05', 18);
INSERT INTO "public"."sys_oper_log" VALUES (139, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '127.0.0.1', '内网IP', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2025/09/23/efba96d828614e60a253242760159123.png","code":200}', 0, NULL, '2025-09-23 13:54:37', 81);
INSERT INTO "public"."sys_oper_log" VALUES (140, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '127.0.0.1', '内网IP', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2025/09/23/af3c376fcbff4bdc82c1ddbf4f6c3ea0.jpg","code":200}', 0, NULL, '2025-09-23 13:55:21', 21);
INSERT INTO "public"."sys_oper_log" VALUES (141, '代码生成', 6, 'com.labdatahub.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{"tables":"labdatahub_warn_record,labdatahub_warn_config"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-05 00:04:56', 114);
INSERT INTO "public"."sys_oper_log" VALUES (142, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{"businessName":"warnConfig","className":"LabdatahubWarnConfig","columns":[{"capJavaField":"Id","columnComment":"id","columnId":69,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-05 00:04:56","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":7,"updateBy":"","usableColumn":false},{"capJavaField":"Name","columnComment":"告警名称","columnId":70,"columnName":"name","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-05 00:04:56","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"name","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":2,"superColumn":false,"tableId":7,"updateBy":"","usableColumn":false},{"capJavaField":"BelongSn","columnComment":"产品/设备sn","columnId":71,"columnName":"belong_sn","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-05 00:04:56","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"belongSn","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":7,"updateBy":"","usableColumn":false},{"capJavaField":"BelongType","columnComment":"来源 0-产品 1-设备","columnId":72,"columnName":"belong_type","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-05 00:04:56","dictType":"","edit":true,"htmlType":"select","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-05 00:05:48', 69);
INSERT INTO "public"."sys_oper_log" VALUES (143, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{"businessName":"warnRecord","className":"LabdatahubWarnRecord","columns":[{"capJavaField":"Id","columnComment":"id","columnId":79,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-05 00:04:56","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":8,"updateBy":"","usableColumn":false},{"capJavaField":"ConfigId","columnComment":"告警配置id","columnId":80,"columnName":"config_id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-05 00:04:56","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"configId","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":8,"updateBy":"","usableColumn":false},{"capJavaField":"ConfigName","columnComment":"告警配置名称","columnId":81,"columnName":"config_name","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-05 00:04:56","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"configName","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":3,"superColumn":false,"tableId":8,"updateBy":"","usableColumn":false},{"capJavaField":"WarnMessage","columnComment":"告警内容","columnId":82,"columnName":"warn_message","columnType":"text","createBy":"admin","createTime":"2025-10-05 00:04:56","dictType":"","edit":true,"htmlType":"textarea","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0",', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-05 00:06:02', 49);
INSERT INTO "public"."sys_oper_log" VALUES (144, '代码生成', 8, 'com.labdatahub.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"labdatahub_warn_config,labdatahub_warn_record"}', NULL, 0, NULL, '2025-10-05 00:06:06', 380);
INSERT INTO "public"."sys_oper_log" VALUES (145, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","id":"rule_001","name":"温度过高告警规则"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:05:10', 20);
INSERT INTO "public"."sys_oper_log" VALUES (146, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","id":"rule_001","name":"温度过高告警规则"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:05:39', 9);
INSERT INTO "public"."sys_oper_log" VALUES (147, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","id":"rule_001","name":"温度过高告警规则"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:06:10', 20);
INSERT INTO "public"."sys_oper_log" VALUES (148, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","id":"rule_001","name":"温度过高告警规则"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:07:03', 18);
INSERT INTO "public"."sys_oper_log" VALUES (149, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","id":"rule_001","name":"温度过高告警规则"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:07:34', 8473);
INSERT INTO "public"."sys_oper_log" VALUES (150, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","id":"rule_001","name":"温度过高告警规则"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:34:54', 7);
INSERT INTO "public"."sys_oper_log" VALUES (151, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","id":"rule_001","name":"温度过高告警规则"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:35:28', 12);
INSERT INTO "public"."sys_oper_log" VALUES (194, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"10"}],"delayTime":0,"enable":true,"id":"1976932019586027521","level":"1","message":"123","name":"ceshi","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 16:44:57', 16);
INSERT INTO "public"."sys_oper_log" VALUES (152, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[{"actionSign":"","params":""}],"conditions":[{"attribute":"temperature","operator":"gt","value":"123"}],"level":"warning","message":"哈哈哈","name":"测试","relation":"and"}', NULL, 1, 'java.lang.NullPointerException
	at com.labdatahub.business.controller.LabdatahubWarnConfigController.add(LabdatahubWarnConfigController.java:98)
	at com.labdatahub.business.controller.LabdatahubWarnConfigController$$FastClassBySpringCGLIB$$c73aaee9.invoke(<generated>)
	at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:792)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:762)
	at org.springframework.aop.aspectj.AspectJAfterThrowingAdvice.invoke(AspectJAfterThrowingAdvice.java:64)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:175)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:762)
	at org.springframework.aop.framework.adapter.AfterReturningAdviceInterceptor.invoke(AfterReturningAdviceInterceptor.java:57)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:175)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:762)
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:58)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:175)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:762)
	at org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor.invoke(AuthorizationManagerBeforeMethodInterceptor.java:162)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)
	at org.springframework.aop.framewo', '2025-10-11 10:36:32', 6);
INSERT INTO "public"."sys_oper_log" VALUES (153, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[{"actionSign":"","params":""}],"conditions":[{"attribute":"temperature","operator":"gt","value":"123"}],"level":"warning","message":"213213","name":"测试2","relation":"and"}', NULL, 1, 'java.lang.NullPointerException
	at com.labdatahub.business.controller.LabdatahubWarnConfigController.add(LabdatahubWarnConfigController.java:98)
	at com.labdatahub.business.controller.LabdatahubWarnConfigController$$FastClassBySpringCGLIB$$c73aaee9.invoke(<generated>)
	at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:792)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:762)
	at org.springframework.aop.aspectj.AspectJAfterThrowingAdvice.invoke(AspectJAfterThrowingAdvice.java:64)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:175)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:762)
	at org.springframework.aop.framework.adapter.AfterReturningAdviceInterceptor.invoke(AfterReturningAdviceInterceptor.java:57)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:175)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:762)
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:58)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:175)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:762)
	at org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor.invoke(AuthorizationManagerBeforeMethodInterceptor.java:162)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)
	at org.springframework.aop.framewo', '2025-10-11 10:37:59', 8284);
INSERT INTO "public"."sys_oper_log" VALUES (154, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","id":"rule_001","name":"温度过高告警规则"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:38:58', 16);
INSERT INTO "public"."sys_oper_log" VALUES (155, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[{"actionSign":"","params":""}],"conditions":[{"attribute":"temperature","operator":"gt","value":"123"}],"enable":true,"level":"warning","message":"3213","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:39:22', 24);
INSERT INTO "public"."sys_oper_log" VALUES (156, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"name":"测试"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:41:09', 13);
INSERT INTO "public"."sys_oper_log" VALUES (157, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","id":"123456","name":"温度过高告警规则"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:50:08', 23);
INSERT INTO "public"."sys_oper_log" VALUES (158, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","conditions":[{"attribute":"temperature","operator":"gt","value":"35"},{"attribute":"windSpeed","operator":"gt","value":"6"}],"delayTime":10,"enable":true,"id":"123456","level":"critical","message":"设备温度异常，当前温度${temperature}℃，超过阈值35℃213213","name":"温度过高告警规则","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:52:53', 52);
INSERT INTO "public"."sys_oper_log" VALUES (159, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","conditions":[{"attribute":"temperature","operator":"gt","value":"35"},{"attribute":"windSpeed","operator":"gt","value":"6"}],"delayTime":10,"enable":true,"id":"123456","level":"critical","message":"设备温度异常，当前温度${temperature}℃，超过阈值35℃","name":"温度过高告警规则","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:53:13', 16);
INSERT INTO "public"."sys_oper_log" VALUES (160, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[{"actionSign":"","params":""}],"conditions":[{"attribute":"windSpeed","operator":"gt","value":"10"}],"enable":true,"id":"1976843712109854721","level":"critical","message":"风速大于10,当前风速${windSpeed}","name":"测试告警1","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:54:03', 19);
INSERT INTO "public"."sys_oper_log" VALUES (161, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[{"actionSign":"","params":""}],"conditions":[{"attribute":"windSpeed","operator":"gt","value":"10"}],"enable":true,"id":"1976843712109854721","level":"critical","message":"风速大于10,当前风速${windSpeed}","name":"测试告警1","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:55:55', 16);
INSERT INTO "public"."sys_oper_log" VALUES (162, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[{"actionSign":"","params":""}],"conditions":[{"attribute":"windSpeed","operator":"gt","value":"10"}],"enable":true,"id":"1976843712109854721","level":"critical","message":"风速大于10,当前风速${windSpeed}","name":"测试告警1","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:57:37', 23);
INSERT INTO "public"."sys_oper_log" VALUES (163, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[{"actionSign":"","params":""}],"conditions":[{"attribute":"windSpeed","operator":"gt","value":"5"}],"enable":true,"id":"1976844915975766017","level":"warning","message":"风速过高，当前风速${windSpeed}，大于预警值5","name":"风速告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 10:58:50', 14);
INSERT INTO "public"."sys_oper_log" VALUES (164, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[{"actionSign":"","params":""}],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"windSpeed","operator":"gt","value":"5"}],"enable":true,"id":"1976846009544065026","level":"warning","message":"当前风速${windSpeed}，大于5","name":"风速告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 11:03:11', 27);
INSERT INTO "public"."sys_oper_log" VALUES (165, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[{"actionSign":"","params":""}],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"windSpeed","operator":"gt","value":"5"}],"enable":true,"id":"1976846009544065026","level":"warning","message":"当前风速${windSpeed}，大于5，","name":"风速告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 11:03:23', 14);
INSERT INTO "public"."sys_oper_log" VALUES (166, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[{"actionSign":"","params":""}],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"windSpeed","operator":"gt","value":"5"}],"enable":true,"id":"1976846009544065026","level":"warning","message":"当前风速${windSpeed}，大于5","name":"风速告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 11:03:28', 14);
INSERT INTO "public"."sys_oper_log" VALUES (167, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[{"actionSign":"","params":""}],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"123"}],"enable":true,"id":"1976847153871826946","level":"warning","message":"测试","name":"告警2","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 11:07:44', 21);
INSERT INTO "public"."sys_oper_log" VALUES (168, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"111"}],"enable":true,"id":"1976847432528801794","level":"warning","message":"213","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 11:08:50', 15);
INSERT INTO "public"."sys_oper_log" VALUES (169, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"111"}],"delayTime":10,"enable":true,"id":"1976847432528801794","level":"warning","message":"213","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 11:34:58', 29);
INSERT INTO "public"."sys_oper_log" VALUES (170, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","conditions":[{"attribute":"temperature","operator":"gt","value":"35"},{"attribute":"windSpeed","operator":"gt","value":"6"}],"delayTime":10,"enable":false,"id":"123456","level":"critical","message":"设备温度异常，当前温度${temperature}℃，超过阈值35℃","name":"温度过高告警规则","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 11:46:47', 43);
INSERT INTO "public"."sys_oper_log" VALUES (171, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","conditions":[{"attribute":"temperature","operator":"gt","value":"35"},{"attribute":"windSpeed","operator":"gt","value":"6"}],"delayTime":10,"enable":true,"id":"123456","level":"critical","message":"设备温度异常，当前温度${temperature}℃，超过阈值35℃","name":"温度过高告警规则","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 11:46:48', 17);
INSERT INTO "public"."sys_oper_log" VALUES (172, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"111"}],"delayTime":10,"enable":true,"id":"1976847432528801794","level":"warning","message":"213","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 11:46:50', 23);
INSERT INTO "public"."sys_oper_log" VALUES (173, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","conditions":[{"attribute":"temperature","operator":"gt","value":"35"},{"attribute":"windSpeed","operator":"gt","value":"6"}],"delayTime":10,"enable":false,"id":"123456","level":"critical","message":"设备温度异常，当前温度${temperature}℃，超过阈值35℃","name":"温度过高告警规则","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 11:47:08', 13);
INSERT INTO "public"."sys_oper_log" VALUES (174, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","conditions":[{"attribute":"temperature","operator":"gt","value":"35"},{"attribute":"windSpeed","operator":"gt","value":"6"}],"delayTime":10,"enable":false,"id":"123456","level":"critical","message":"设备温度异常，当前温度${temperature}℃，超过阈值35℃","name":"温度过高告警规则","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 11:47:34', 13);
INSERT INTO "public"."sys_oper_log" VALUES (175, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","conditions":[{"attribute":"temperature","operator":"gt","value":"35"},{"attribute":"windSpeed","operator":"gt","value":"6"}],"delayTime":10,"enable":false,"id":"123456","level":"critical","message":"设备温度异常，当前温度${temperature}℃，超过阈值35℃","name":"温度过高告警规则","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 12:00:33', 30);
INSERT INTO "public"."sys_oper_log" VALUES (176, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","conditions":[{"attribute":"temperature","operator":"gt","value":"35"},{"attribute":"windSpeed","operator":"gt","value":"6"}],"delayTime":10,"enable":true,"id":"123456","level":"critical","message":"设备温度异常，当前温度${temperature}℃，超过阈值35℃","name":"温度过高告警规则","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 12:00:38', 30);
INSERT INTO "public"."sys_oper_log" VALUES (177, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"111"}],"delayTime":10,"enable":false,"id":"1976847432528801794","level":"warning","message":"213","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 12:00:40', 22);
INSERT INTO "public"."sys_oper_log" VALUES (178, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","conditions":[{"attribute":"temperature","operator":"gt","value":"35"},{"attribute":"windSpeed","operator":"gt","value":"6"}],"delayTime":10,"enable":false,"id":"123456","level":"critical","message":"设备温度异常，当前温度${temperature}℃，超过阈值35℃","name":"温度过高告警规则","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 12:00:41', 25);
INSERT INTO "public"."sys_oper_log" VALUES (179, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","conditions":[{"attribute":"temperature","operator":"gt","value":"35"},{"attribute":"windSpeed","operator":"gt","value":"6"}],"delayTime":10,"enable":true,"id":"123456","level":"critical","message":"设备温度异常，当前温度${temperature}℃，超过阈值35℃","name":"温度过高告警规则","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 14:47:23', 25);
INSERT INTO "public"."sys_oper_log" VALUES (180, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","conditions":[{"attribute":"temperature","operator":"gt","value":"35"},{"attribute":"windSpeed","operator":"gt","value":"6"}],"delayTime":10,"enable":true,"id":"123456","level":"1","message":"设备温度异常，当前温度${temperature}℃，超过阈值35℃","name":"温度过高告警规则","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 16:08:54', 42);
INSERT INTO "public"."sys_oper_log" VALUES (181, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"111"}],"delayTime":10,"enable":false,"id":"1976847432528801794","level":"1","message":"213","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 16:08:58', 16);
INSERT INTO "public"."sys_oper_log" VALUES (182, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"111"}],"delayTime":10,"enable":false,"id":"1976847432528801794","level":"1","message":"213","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 16:09:01', 15);
INSERT INTO "public"."sys_oper_log" VALUES (183, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","conditions":[{"attribute":"temperature","operator":"gt","value":"35"},{"attribute":"windSpeed","operator":"gt","value":"6"}],"delayTime":10,"enable":true,"id":"123456","level":"1","message":"设备温度异常，当前温度${temperature}℃，超过阈值35℃","name":"温度过高告警规则","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 16:09:19', 16);
INSERT INTO "public"."sys_oper_log" VALUES (184, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"111"}],"delayTime":10,"enable":true,"id":"1976847432528801794","level":"1","message":"213","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 16:09:20', 19);
INSERT INTO "public"."sys_oper_log" VALUES (185, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"111"}],"delayTime":10,"enable":true,"id":"1976847432528801794","level":"1","message":"213","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 16:09:24', 17);
INSERT INTO "public"."sys_oper_log" VALUES (186, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"111"}],"delayTime":10,"enable":true,"id":"1976847432528801794","level":"1","message":"213","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 16:09:25', 16);
INSERT INTO "public"."sys_oper_log" VALUES (187, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","conditions":[{"attribute":"temperature","operator":"gt","value":"35"},{"attribute":"windSpeed","operator":"gt","value":"6"}],"delayTime":10,"enable":true,"id":"123456","level":"1","message":"设备温度异常，当前温度${temperature}℃，超过阈值35℃","name":"温度过高告警规则","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 16:09:30', 14);
INSERT INTO "public"."sys_oper_log" VALUES (188, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"111"}],"delayTime":10,"enable":true,"id":"1976847432528801794","level":"1","message":"213","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 16:09:32', 14);
INSERT INTO "public"."sys_oper_log" VALUES (189, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"belongSn":"IOT-device01","belongType":"0","conditions":[{"attribute":"temperature","operator":"gt","value":"35"},{"attribute":"windSpeed","operator":"gt","value":"6"}],"delayTime":10,"enable":true,"id":"123456","level":"1","message":"设备温度异常，当前温度${temperature}℃，超过阈值35℃","name":"温度过高告警规则","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 16:10:47', 23);
INSERT INTO "public"."sys_oper_log" VALUES (190, '告警配置', 3, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/warnConfig/123456', '127.0.0.1', '内网IP', '["123456"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 16:13:31', 30);
INSERT INTO "public"."sys_oper_log" VALUES (191, '告警配置', 3, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/warnConfig/1976847432528801794', '127.0.0.1', '内网IP', '["1976847432528801794"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 16:13:35', 10);
INSERT INTO "public"."sys_oper_log" VALUES (192, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"38"},{"attribute":"windSpeed","operator":"lt","value":"10"}],"delayTime":10,"enable":true,"id":"1976924520506019841","level":"3","message":"温度过高，当前温度${temperature}，超过预警值38","name":"高温告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 16:15:09', 25);
INSERT INTO "public"."sys_oper_log" VALUES (193, '告警配置', 3, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/warnConfig/1976924520506019841', '127.0.0.1', '内网IP', '["1976924520506019841"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 16:42:57', 35);
INSERT INTO "public"."sys_oper_log" VALUES (196, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"100"}],"delayTime":0,"enable":true,"id":"1976938887867826178","level":"1","message":"213213","name":"312","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 17:12:15', 23);
INSERT INTO "public"."sys_oper_log" VALUES (197, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"100"}],"delayTime":0,"enable":true,"id":"1976938887867826178","level":"3","message":"213213","name":"312","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 17:12:19', 21);
INSERT INTO "public"."sys_oper_log" VALUES (198, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"100"}],"delayTime":0,"enable":true,"id":"1976938887867826178","level":"4","message":"213213","name":"312","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 17:12:21', 25);
INSERT INTO "public"."sys_oper_log" VALUES (199, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"100"}],"delayTime":0,"enable":true,"id":"1976938887867826178","level":"2","message":"213213","name":"312","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 17:12:24', 12);
INSERT INTO "public"."sys_oper_log" VALUES (200, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"100"}],"delayTime":0,"enable":true,"id":"1976938887867826178","level":"3","message":"213213","name":"312","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 17:12:27', 23);
INSERT INTO "public"."sys_oper_log" VALUES (201, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"100"}],"delayTime":0,"enable":true,"id":"1976938887867826178","level":"2","message":"213213","name":"312","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 17:12:42', 18);
INSERT INTO "public"."sys_oper_log" VALUES (202, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"100"}],"delayTime":0,"enable":true,"id":"1976938887867826178","level":"2","message":"213213","name":"312","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-11 17:14:33', 13);
INSERT INTO "public"."sys_oper_log" VALUES (203, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"HTTP_PRODUCT","belongType":"0","conditions":[{"attribute":"windSpeed","operator":"gt","value":"50"}],"delayTime":0,"enable":true,"id":"1979485650587795457","level":"2","message":"告警啦","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-18 17:52:10', 27);
INSERT INTO "public"."sys_oper_log" VALUES (204, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"100"},{"attribute":"windSpeed","operator":"gt","value":"100"}],"delayTime":0,"enable":true,"id":"1976938887867826178","level":"2","message":"213213","name":"312","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-19 14:07:53', 399);
INSERT INTO "public"."sys_oper_log" VALUES (205, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"IOT-device01","belongType":"1","conditions":[{"attribute":"temperature","operator":"gt","value":"100"},{"attribute":"windSpeed","operator":"gt","value":"100"}],"delayTime":0,"enable":true,"id":"1976938887867826178","level":"2","message":"213213","name":"312","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-19 14:07:57', 9);
INSERT INTO "public"."sys_oper_log" VALUES (206, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"HTTP_PRODUCT","belongType":"0","conditions":[{"attribute":"windSpeed","operator":"gt","value":"50"}],"delayTime":0,"enable":false,"id":"1979485650587795457","level":"2","message":"告警啦","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-19 14:13:34', 28);
INSERT INTO "public"."sys_oper_log" VALUES (207, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"HTTP_PRODUCT","belongType":"0","conditions":[{"attribute":"windSpeed","operator":"gt","value":"50"}],"delayTime":0,"enable":true,"id":"1979485650587795457","level":"2","message":"告警啦","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-19 14:13:36', 19);
INSERT INTO "public"."sys_oper_log" VALUES (208, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"HTTP_PRODUCT","belongType":"0","conditions":[{"attribute":"windSpeed","operator":"gt","value":"50"}],"delayTime":0,"enable":false,"id":"1979485650587795457","level":"2","message":"告警啦","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-19 14:13:53', 25);
INSERT INTO "public"."sys_oper_log" VALUES (209, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"HTTP_PRODUCT","belongType":"0","conditions":[{"attribute":"windSpeed","operator":"gt","value":"50"}],"delayTime":0,"enable":true,"id":"1979485650587795457","level":"2","message":"告警啦","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-19 14:13:56', 19);
INSERT INTO "public"."sys_oper_log" VALUES (210, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"HTTP_PRODUCT","belongType":"0","conditions":[{"attribute":"temperature","operator":"ne","value":"100"}],"delayTime":20,"enable":true,"id":"1979799048719896577","level":"2","message":"温度失衡","name":"测试3","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-19 14:37:30', 26);
INSERT INTO "public"."sys_oper_log" VALUES (211, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"HTTP_PRODUCT","belongType":"0","conditions":[{"attribute":"windSpeed","operator":"gt","value":"50"}],"delayTime":0,"enable":true,"id":"1979485650587795457","level":"2","message":"告警啦","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-19 16:12:20', 44);
INSERT INTO "public"."sys_oper_log" VALUES (212, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[],"belongSn":"HTTP_PRODUCT","belongType":"0","conditions":[{"attribute":"temperature","operator":"ne","value":"100"}],"delayTime":20,"enable":true,"id":"1979799048719896577","level":"2","message":"温度失衡","name":"测试3","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-19 16:12:21', 6);
INSERT INTO "public"."sys_oper_log" VALUES (213, '菜单管理', 1, 'com.labdatahub.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"business/engine/index","createBy":"admin","icon":"druid","isCache":"1","isFrame":"1","menuName":"规则引擎","menuType":"C","orderNum":3,"params":{},"parentId":2037,"path":"engine","routeName":"Engine","status":"0","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-19 18:11:02', 32);
INSERT INTO "public"."sys_oper_log" VALUES (214, '代码生成', 6, 'com.labdatahub.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{"tables":"labdatahub_rule_engine"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-20 10:01:01', 53);
INSERT INTO "public"."sys_oper_log" VALUES (215, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{"businessName":"engine","className":"LabdatahubRuleEngine","columns":[{"capJavaField":"Id","columnComment":"id","columnId":87,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-20 10:01:01","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":9,"updateBy":"","usableColumn":false},{"capJavaField":"EngineName","columnComment":"引擎名称","columnId":88,"columnName":"engine_name","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-20 10:01:01","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"engineName","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":2,"superColumn":false,"tableId":9,"updateBy":"","usableColumn":false},{"capJavaField":"ConfigJson","columnComment":"json配置","columnId":89,"columnName":"config_json","columnType":"text","createBy":"admin","createTime":"2025-10-20 10:01:01","dictType":"","edit":true,"htmlType":"textarea","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"configJson","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":9,"updateBy":"","usableColumn":false},{"capJavaField":"CreateTime","columnComment":"创建时间","columnId":90,"columnName":"create_time","columnType":"datetime","createBy":"admin","createTime":"2025-10-20 10:01:01","dictType":"","edit":false,"htmlType":"datetime","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"0","isRequired":"0","javaField"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-20 10:01:27', 30);
INSERT INTO "public"."sys_oper_log" VALUES (216, '代码生成', 8, 'com.labdatahub.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"labdatahub_rule_engine"}', NULL, 0, NULL, '2025-10-20 10:01:31', 232);
INSERT INTO "public"."sys_oper_log" VALUES (217, '代码生成', 6, 'com.labdatahub.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{"tables":"labdatahub_function"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-24 17:01:11', 129);
INSERT INTO "public"."sys_oper_log" VALUES (218, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{"businessName":"function","className":"LabdatahubFunction","columns":[{"capJavaField":"Id","columnComment":"id","columnId":92,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-24 17:01:11","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":10,"updateBy":"","usableColumn":false},{"capJavaField":"FunctionName","columnComment":"功能名称","columnId":93,"columnName":"function_name","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-24 17:01:11","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"functionName","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":2,"superColumn":false,"tableId":10,"updateBy":"","usableColumn":false},{"capJavaField":"FunctionCode","columnComment":"功能编码","columnId":94,"columnName":"function_code","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-24 17:01:11","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"functionCode","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":10,"updateBy":"","usableColumn":false},{"capJavaField":"FunctionParams","columnComment":"自定义参数","columnId":95,"columnName":"function_params","columnType":"text","createBy":"admin","createTime":"2025-10-24 17:01:11","dictType":"","edit":true,"htmlType":"textarea","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isL', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-24 17:01:25', 81);
INSERT INTO "public"."sys_oper_log" VALUES (219, '代码生成', 8, 'com.labdatahub.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"labdatahub_function"}', NULL, 0, NULL, '2025-10-24 17:01:32', 333);
INSERT INTO "public"."sys_oper_log" VALUES (220, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '127.0.0.1', '内网IP', '{"functionCode":"TEST","functionName":"测试指令","functionParams":"123"}', NULL, 1, 'java.lang.NullPointerException
	at com.labdatahub.business.controller.LabdatahubFunctionController.add(LabdatahubFunctionController.java:110)
	at com.labdatahub.business.controller.LabdatahubFunctionController$$FastClassBySpringCGLIB$$2de77e99.invoke(<generated>)
	at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:792)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:762)
	at org.springframework.aop.aspectj.AspectJAfterThrowingAdvice.invoke(AspectJAfterThrowingAdvice.java:64)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:175)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:762)
	at org.springframework.aop.framework.adapter.AfterReturningAdviceInterceptor.invoke(AfterReturningAdviceInterceptor.java:57)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:175)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:762)
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:58)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:175)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:762)
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(Cglib', '2025-10-25 11:39:55', 10);
INSERT INTO "public"."sys_oper_log" VALUES (221, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '127.0.0.1', '内网IP', '{"belongSn":"IOT-0016","belongType":"0","functionCode":"TEST","functionName":"测试指令","functionParams":"123","id":"1981929251066413058"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-25 11:42:10', 20);
INSERT INTO "public"."sys_oper_log" VALUES (222, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '127.0.0.1', '内网IP', '{"belongSn":"123456","belongType":"0","functionCode":"TEST001","functionName":"测试指令","functionParams":"213123","id":"1981933616963584002","protocolId":"1981912979998031873"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-25 11:59:31', 18);
INSERT INTO "public"."sys_oper_log" VALUES (223, '规则引擎配置', 2, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/ruleEngine', '106.92.105.125', 'XX XX', '{"configJson":"213","createTime":"2025-10-20 02:22:05","engineName":"213","id":"1","isEnable":"undefined","remark":"1"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-28 21:17:10', 19);
INSERT INTO "public"."sys_oper_log" VALUES (224, '规则引擎配置', 3, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/ruleEngine/1', '106.92.105.125', 'XX XX', '["1"]', NULL, 1, 'Invalid bound statement (not found): com.labdatahub.business.mapper.LabdatahubRuleEngineMapper.deleteLabdatahubRuleEngineByIds', '2025-10-28 21:17:15', 13);
INSERT INTO "public"."sys_oper_log" VALUES (225, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '106.92.105.125', 'XX XX', '{"belongSn":"WOSHI","belongType":"1","functionCode":"TEST","functionName":"测试指令","id":"1983167664912101378","protocolId":"1983163608265158657"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-28 21:43:11', 34);
INSERT INTO "public"."sys_oper_log" VALUES (226, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '127.0.0.1', '内网IP', '{"belongSn":"cgq2","belongType":"1","functionCode":"Test","functionName":"测试指令","functionParams":"123123","id":"1983419022980677633","protocolId":"1983369352292122626"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-29 14:21:59', 35);
INSERT INTO "public"."sys_oper_log" VALUES (227, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '127.0.0.1', '内网IP', '{"belongSn":"c2","belongType":"0","functionCode":"test","functionName":"测试指令","functionParams":"21312321","id":"1983419242053369858","protocolId":"1983369352292122626"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-29 14:22:52', 13);
INSERT INTO "public"."sys_oper_log" VALUES (228, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[{"functionCode":"test","functionParams":"213123"}],"belongSn":"c2","belongType":"0","conditions":[{"attribute":"inTemperature","operator":"gt","value":"10"}],"delayTime":0,"enable":true,"id":"1983420534217768962","level":"1","message":"告警啦","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-29 14:28:00', 28);
INSERT INTO "public"."sys_oper_log" VALUES (229, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '127.0.0.1', '内网IP', '{"actions":[{"functionCode":"test","functionParams":"213123"}],"belongSn":"cgq2","belongType":"1","conditions":[{"attribute":"inTemperature","operator":"gt","value":"10"}],"delayTime":0,"enable":true,"id":"1983432800048869377","level":"1","message":"告警啦","name":"测试","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-29 15:18:02', 22);
INSERT INTO "public"."sys_oper_log" VALUES (230, '代码生成', 6, 'com.labdatahub.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{"tables":"warn_function_record"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-29 17:16:05', 68);
INSERT INTO "public"."sys_oper_log" VALUES (231, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{"businessName":"record","className":"WarnFunctionRecord","columns":[{"capJavaField":"Id","columnComment":"id","columnId":100,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-29 17:16:05","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":11,"updateBy":"","usableColumn":false},{"capJavaField":"FunctionId","columnComment":"功能id","columnId":101,"columnName":"function_id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-29 17:16:05","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"functionId","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":11,"updateBy":"","usableColumn":false},{"capJavaField":"FunctionCode","columnComment":"功能code","columnId":102,"columnName":"function_code","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-29 17:16:05","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"functionCode","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":11,"updateBy":"","usableColumn":false},{"capJavaField":"FunctionName","columnComment":"功能名称","columnId":103,"columnName":"function_name","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-29 17:16:05","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-29 17:16:16', 52);
INSERT INTO "public"."sys_oper_log" VALUES (232, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{"businessName":"record","className":"WarnFunctionRecord","columns":[{"capJavaField":"Id","columnComment":"id","columnId":100,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-29 17:16:05","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":11,"updateBy":"","updateTime":"2025-10-29 17:16:16","usableColumn":false},{"capJavaField":"FunctionId","columnComment":"功能id","columnId":101,"columnName":"function_id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-29 17:16:05","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"functionId","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":11,"updateBy":"","updateTime":"2025-10-29 17:16:16","usableColumn":false},{"capJavaField":"FunctionCode","columnComment":"功能code","columnId":102,"columnName":"function_code","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-29 17:16:05","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"functionCode","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":11,"updateBy":"","updateTime":"2025-10-29 17:16:16","usableColumn":false},{"capJavaField":"FunctionName","columnComment":"功能名称","columnId":103,"columnName":"function_name","columnType":"varchar(255)","createBy":"admin","createTime":"2025-10-29 17:16:05","dictType":"","edit":true,', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-29 17:16:24', 50);
INSERT INTO "public"."sys_oper_log" VALUES (233, '代码生成', 8, 'com.labdatahub.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"warn_function_record"}', NULL, 0, NULL, '2025-10-29 17:16:29', 317);
INSERT INTO "public"."sys_oper_log" VALUES (234, '规则引擎配置', 1, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.add()', 'POST', 1, 'admin', '研发部门', '/business/ruleEngine', '125.80.201.66', 'XX XX', '{"createTime":"2025-10-30 02:39:04","id":"1983725311088422914"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 10:39:04', 20);
INSERT INTO "public"."sys_oper_log" VALUES (235, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '125.80.201.66', 'XX XX', '{"children":[],"createTime":"2025-09-18 06:04:33","icon":"netComponent","isCache":"0","isFrame":"1","menuId":2037,"menuName":"网络组件","menuType":"M","orderNum":0,"params":{},"parentId":0,"path":"/componentManage","perms":"","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 11:19:36', 40);
INSERT INTO "public"."sys_oper_log" VALUES (236, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '125.80.201.66', 'XX XX', '{"children":[],"component":"business/component/index","createTime":"2025-09-18 05:58:11","icon":"netComponent","isCache":"0","isFrame":"1","menuId":2000,"menuName":"网络组件","menuType":"C","orderNum":1,"params":{},"parentId":2037,"path":"component","perms":"business:component:list","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 11:19:43', 20);
INSERT INTO "public"."sys_oper_log" VALUES (237, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '125.80.201.66', 'XX XX', '{"children":[],"component":"business/protocol/index","createTime":"2025-09-18 06:00:37","icon":"protocol","isCache":"0","isFrame":"1","menuId":2030,"menuName":"协议管理","menuType":"C","orderNum":1,"params":{},"parentId":2037,"path":"protocol","perms":"business:protocol:list","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 11:19:51', 21);
INSERT INTO "public"."sys_oper_log" VALUES (238, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '125.80.201.66', 'XX XX', '{"children":[],"component":"business/engine/index","createTime":"2025-10-19 10:11:02","icon":"engine","isCache":"1","isFrame":"1","menuId":2040,"menuName":"规则引擎","menuType":"C","orderNum":3,"params":{},"parentId":2037,"path":"engine","perms":"","routeName":"Engine","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 11:20:03', 23);
INSERT INTO "public"."sys_oper_log" VALUES (239, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '125.80.201.66', 'XX XX', '{"children":[],"createTime":"2025-09-18 06:03:01","icon":"device","isCache":"0","isFrame":"1","menuId":2036,"menuName":"设备管理","menuType":"M","orderNum":0,"params":{},"parentId":0,"path":"/deviceManage","perms":"","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 11:20:19', 18);
INSERT INTO "public"."sys_oper_log" VALUES (240, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '125.80.201.66', 'XX XX', '{"children":[],"component":"business/product/index","createTime":"2025-09-18 05:58:31","icon":"product","isCache":"0","isFrame":"1","menuId":2012,"menuName":"产品","menuType":"C","orderNum":0,"params":{},"parentId":2036,"path":"product","perms":"business:product:list","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 11:20:26', 17);
INSERT INTO "public"."sys_oper_log" VALUES (241, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '125.80.201.66', 'XX XX', '{"children":[],"component":"business/device/index","createTime":"2025-09-18 05:58:25","icon":"device","isCache":"0","isFrame":"1","menuId":2006,"menuName":"设备","menuType":"C","orderNum":1,"params":{},"parentId":2036,"path":"device","perms":"business:device:list","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 11:20:32', 13);
INSERT INTO "public"."sys_oper_log" VALUES (242, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '125.80.201.66', 'XX XX', '{"belongSn":"product_001","belongType":"0","functionCode":"TEST","functionName":"测试指令","functionParams":"123456","id":"1983737050999844865","protocolId":"1983735910950268930"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 11:25:43', 40);
INSERT INTO "public"."sys_oper_log" VALUES (243, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '125.80.201.66', 'XX XX', '{"actions":[{"functionCode":"TEST","functionParams":"测试"}],"belongSn":"product_001","belongType":"0","conditions":[{"attribute":"outTemperature","operator":"gt","value":"30"},{"attribute":"inTemperature","operator":"gt","value":"20"}],"delayTime":0,"enable":true,"id":"1983737402524463106","level":"2","message":"室外温度和室内温度均达到预警值，请及时查看设备状态。","name":"温湿度告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 11:27:07', 42);
INSERT INTO "public"."sys_oper_log" VALUES (244, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '125.80.201.66', 'XX XX', '{"actions":[{"functionCode":"TEST","functionParams":"测试"}],"belongSn":"WS_DEVICE_001","belongType":"1","conditions":[{"attribute":"outTemperature","operator":"gt","value":"30"},{"attribute":"inTemperature","operator":"gt","value":"20"}],"delayTime":0,"enable":true,"id":"1983737562021261313","level":"2","message":"室外温度和室内温度均达到预警值，请及时查看设备状态。","name":"温湿度告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 11:28:57', 13);
INSERT INTO "public"."sys_oper_log" VALUES (245, '设备指令下发', 3, 'com.labdatahub.business.controller.LabdatahubFunctionController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/function/1983737553343246338', '125.80.201.66', 'XX XX', '["1983737553343246338"]', NULL, 1, 'Invalid bound statement (not found): com.labdatahub.business.mapper.LabdatahubFunctionMapper.deleteLabdatahubFunctionByIds', '2025-10-30 11:30:47', 9);
INSERT INTO "public"."sys_oper_log" VALUES (246, '设备指令下发', 3, 'com.labdatahub.business.controller.LabdatahubFunctionController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/function/1983737461873864705', '125.80.201.66', 'XX XX', '["1983737461873864705"]', NULL, 1, 'Invalid bound statement (not found): com.labdatahub.business.mapper.LabdatahubFunctionMapper.deleteLabdatahubFunctionByIds', '2025-10-30 11:30:53', 1);
INSERT INTO "public"."sys_oper_log" VALUES (247, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '125.80.201.66', 'XX XX', '{"belongSn":"WS_DEVICE_001","belongType":"1","functionCode":"TEST","functionName":"测试指令","functionParams":"123456","id":"1983737461873864705","protocolId":"1983735910950268930"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 11:31:43', 15);
INSERT INTO "public"."sys_oper_log" VALUES (248, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '106.92.105.125', 'XX XX', '{"belongSn":"mqtt_broker_001","belongType":"0","createTime":"2025-10-30 13:21:35","functionCode":"TEST","functionName":"测试指令","functionParams":"123213","id":"1983887004066148354","protocolId":"1983885892432982018"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 21:21:35', 44);
INSERT INTO "public"."sys_oper_log" VALUES (249, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.105.125', 'XX XX', '{"actions":[{"functionCode":"TEST","functionParams":"4564"}],"belongSn":"mqtt_broker_001","belongType":"0","conditions":[{"attribute":"inTemperature","operator":"gt","value":"20"},{"attribute":"outTemperature","operator":"gt","value":"30"}],"delayTime":0,"enable":true,"id":"1983887278881140738","level":"1","message":"温度过高，请及时查看传感器情况。","name":"温度告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 21:22:40', 44);
INSERT INTO "public"."sys_oper_log" VALUES (250, '设备指令下发', 3, 'com.labdatahub.business.controller.LabdatahubFunctionController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/function/1983887619328602113', '106.92.105.125', 'XX XX', '["1983887619328602113"]', NULL, 1, 'Invalid bound statement (not found): com.labdatahub.business.mapper.LabdatahubFunctionMapper.deleteLabdatahubFunctionByIds', '2025-10-30 21:51:10', 18);
INSERT INTO "public"."sys_oper_log" VALUES (251, '设备指令下发', 3, 'com.labdatahub.business.controller.LabdatahubFunctionController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/function/1983887619328602113', '106.92.105.125', 'XX XX', '["1983887619328602113"]', NULL, 1, 'Invalid bound statement (not found): com.labdatahub.business.mapper.LabdatahubFunctionMapper.deleteLabdatahubFunctionByIds', '2025-10-30 21:51:15', 1);
INSERT INTO "public"."sys_oper_log" VALUES (252, '设备指令下发', 3, 'com.labdatahub.business.controller.LabdatahubFunctionController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/function/1983887619328602113', '106.92.105.125', 'XX XX', '["1983887619328602113"]', NULL, 1, 'Invalid bound statement (not found): com.labdatahub.business.mapper.LabdatahubFunctionMapper.deleteLabdatahubFunctionByIds', '2025-10-30 21:58:30', 22);
INSERT INTO "public"."sys_oper_log" VALUES (253, '设备指令下发', 3, 'com.labdatahub.business.controller.LabdatahubFunctionController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/function/1983887619328602113', '106.92.105.125', 'XX XX', '["1983887619328602113"]', NULL, 1, 'Invalid bound statement (not found): com.labdatahub.business.mapper.LabdatahubFunctionMapper.deleteLabdatahubFunctionByIds', '2025-10-30 21:58:46', 2);
INSERT INTO "public"."sys_oper_log" VALUES (254, '设备指令下发', 3, 'com.labdatahub.business.controller.LabdatahubFunctionController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/function/1983887619328602113', '106.92.105.125', 'XX XX', '["1983887619328602113"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 22:02:42', 102);
INSERT INTO "public"."sys_oper_log" VALUES (255, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '106.92.105.125', 'XX XX', '{"belongSn":"mqtt_001","belongType":"1","createTime":"2025-10-30 13:21:35","functionCode":"TEST","functionName":"测试指令","functionParams":"123213","id":"1983887080943546370","protocolId":"1983885892432982018"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-30 22:02:44', 31);
INSERT INTO "public"."sys_oper_log" VALUES (256, '规则引擎配置', 1, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.add()', 'POST', 1, 'admin', '研发部门', '/business/ruleEngine', '113.249.34.82', 'XX XX', '{"createTime":"2025-10-31 07:08:51","id":"1984155593205641217"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-10-31 15:08:51', 27);
INSERT INTO "public"."sys_oper_log" VALUES (257, '规则引擎配置', 2, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/ruleEngine', '183.226.251.40', 'XX XX', '{"createTime":"2025-10-31 07:08:52","id":"1984155593205641217","isEnable":"undefined","remark":"234"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-01 15:31:46', 16);
INSERT INTO "public"."sys_oper_log" VALUES (258, '规则引擎配置', 2, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/ruleEngine', '113.251.69.46', 'XX XX', '{"createTime":"2025-10-31 07:08:52","engineName":"21323","id":"1984155593205641217","isEnable":"undefined","remark":"234"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-01 18:02:24', 60);
INSERT INTO "public"."sys_oper_log" VALUES (259, '规则引擎配置', 3, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/ruleEngine/1984155593205641217', '113.251.69.46', 'XX XX', '["1984155593205641217"]', NULL, 1, 'Invalid bound statement (not found): com.labdatahub.business.mapper.LabdatahubRuleEngineMapper.deleteLabdatahubRuleEngineByIds', '2025-11-01 18:02:33', 15);
INSERT INTO "public"."sys_oper_log" VALUES (273, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.251.75.185', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2025/11/13/7cc2e3cd375e4efdba81a6409e8e9748.jpg","code":200}', 0, NULL, '2025-11-13 10:01:12', 15);
INSERT INTO "public"."sys_oper_log" VALUES (260, '规则引擎配置', 3, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/ruleEngine/1984155593205641217', '113.251.69.46', 'XX XX', '["1984155593205641217"]', NULL, 1, 'Invalid bound statement (not found): com.labdatahub.business.mapper.LabdatahubRuleEngineMapper.deleteLabdatahubRuleEngineByIds', '2025-11-01 18:02:38', 2);
INSERT INTO "public"."sys_oper_log" VALUES (261, '规则引擎配置', 1, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.add()', 'POST', 1, 'admin', '研发部门', '/business/ruleEngine', '113.251.69.46', 'XX XX', '{"createTime":"2025-11-01 10:02:46","engineName":"23123213","id":"1984561745701662722"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-01 18:02:46', 17);
INSERT INTO "public"."sys_oper_log" VALUES (262, '规则引擎配置', 3, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/ruleEngine/1984561745701662722', '113.251.69.46', 'XX XX', '["1984561745701662722"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-01 18:04:46', 75);
INSERT INTO "public"."sys_oper_log" VALUES (263, '规则引擎配置', 1, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.add()', 'POST', 1, 'admin', '研发部门', '/business/ruleEngine', '113.251.69.46', 'XX XX', '{"configJson":"{\"lineList\":[],\"nodeList\":[]}","createTime":"2025-11-01 10:04:48","engineName":"123123213","id":"1984562257922650113"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-01 18:04:48', 50);
INSERT INTO "public"."sys_oper_log" VALUES (264, '规则引擎配置', 3, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/ruleEngine/1984155593205641217', '106.92.105.125', 'XX XX', '["1984155593205641217"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-02 12:53:42', 45);
INSERT INTO "public"."sys_oper_log" VALUES (265, '规则引擎配置', 2, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.configEngine()', 'PUT', 1, 'admin', '研发部门', '/business/ruleEngine/configEngine', '106.92.105.125', 'XX XX', '{"configJson":"{\"lineList\":[{\"from\":\"zrd6v4exjs\",\"to\":\"7jnz0uhaqk\"},{\"from\":\"vyxwpf3tc\",\"to\":\"7jnz0uhaqk\"},{\"from\":\"7jnz0uhaqk\",\"to\":\"xa80k4nt04\"},{\"from\":\"7jnz0uhaqk\",\"to\":\"wl5fksp67\"}],\"nodeList\":[{\"id\":\"zrd6v4exjs\",\"name\":\"设备告警\",\"type\":\"deviceWarn\",\"left\":\"54px\",\"top\":\"122px\",\"ico\":\"el-icon-odometer\",\"state\":\"success\"},{\"id\":\"vyxwpf3tc\",\"name\":\"设备日志\",\"type\":\"deviceLog\",\"left\":\"34px\",\"top\":\"347px\",\"ico\":\"el-icon-time\",\"state\":\"success\"},{\"id\":\"7jnz0uhaqk\",\"name\":\"实时推送\",\"type\":\"realTimePush\",\"left\":\"280px\",\"top\":\"246px\",\"ico\":\"el-icon-caret-right\",\"state\":\"success\",\"configData\":{\"productScope\":[],\"productList\":[{\"id\":\"1983736431073325058\",\"productSn\":\"product_001\",\"productName\":\"WS产品\",\"linkMethodId\":null,\"linkMethodName\":null,\"componentId\":\"1983736295886712834\",\"componentName\":\"WS网络组件_10883\",\"protocolId\":\"1983735910950268930\",\"protocolName\":\"WS协议\",\"deviceCount\":1,\"deviceType\":\"0\",\"status\":\"0\",\"createBy\":null,\"createTime\":\"2025-10-30 03:23:16\",\"updateBy\":null,\"updateTime\":null,\"remark\":null,\"timeoutSeconds\":60,\"regularCleaning\":\"1\",\"retentionTime\":1,\"retentionUnit\":\"day\",\"customConfig\":null},{\"id\":\"1983886502083457026\",\"productSn\":\"mqtt_broker_001\",\"productName\":\"MQTT服务端产品\",\"linkMethodId\":null,\"linkMethodName\":null,\"componentId\":\"1983886026080284674\",\"componentName\":\"MQTT服务端\",\"protocolId\":\"1983885892432982018\",\"protocolName\":\"MQTT服务端协议\",\"deviceCount\":1,\"deviceType\":\"2\",\"status\":\"0\",\"createBy\":null,\"createTime\":\"2025-10-30 13:19:36\",\"updateBy\":null,\"updateTime\":null,\"remark\":null,\"timeoutSeconds\":60,\"regularCleaning\":\"1\",\"retentionTime\":1,\"retentionUnit\":\"day\",\"customConfig\":null}],\"deviceScope\":\"all\",\"deviceList\":[],\"deviceSnList\":[],\"authHeaderSign\":null,\"authToken\":null,\"routingKey\":null,\"exc', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-02 12:54:36', 18);
INSERT INTO "public"."sys_oper_log" VALUES (266, '规则引擎配置', 2, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/ruleEngine', '106.92.105.125', 'XX XX', '{"configJson":"{\"lineList\":[{\"from\":\"zrd6v4exjs\",\"to\":\"7jnz0uhaqk\"},{\"from\":\"vyxwpf3tc\",\"to\":\"7jnz0uhaqk\"},{\"from\":\"7jnz0uhaqk\",\"to\":\"xa80k4nt04\"},{\"from\":\"7jnz0uhaqk\",\"to\":\"wl5fksp67\"}],\"nodeList\":[{\"id\":\"zrd6v4exjs\",\"name\":\"设备告警\",\"type\":\"deviceWarn\",\"left\":\"54px\",\"top\":\"122px\",\"ico\":\"el-icon-odometer\",\"state\":\"success\"},{\"id\":\"vyxwpf3tc\",\"name\":\"设备日志\",\"type\":\"deviceLog\",\"left\":\"34px\",\"top\":\"347px\",\"ico\":\"el-icon-time\",\"state\":\"success\"},{\"id\":\"7jnz0uhaqk\",\"name\":\"实时推送\",\"type\":\"realTimePush\",\"left\":\"280px\",\"top\":\"246px\",\"ico\":\"el-icon-caret-right\",\"state\":\"success\",\"configData\":{\"productScope\":[],\"productList\":[{\"id\":\"1983736431073325058\",\"productSn\":\"product_001\",\"productName\":\"WS产品\",\"linkMethodId\":null,\"linkMethodName\":null,\"componentId\":\"1983736295886712834\",\"componentName\":\"WS网络组件_10883\",\"protocolId\":\"1983735910950268930\",\"protocolName\":\"WS协议\",\"deviceCount\":1,\"deviceType\":\"0\",\"status\":\"0\",\"createBy\":null,\"createTime\":\"2025-10-30 03:23:16\",\"updateBy\":null,\"updateTime\":null,\"remark\":null,\"timeoutSeconds\":60,\"regularCleaning\":\"1\",\"retentionTime\":1,\"retentionUnit\":\"day\",\"customConfig\":null},{\"id\":\"1983886502083457026\",\"productSn\":\"mqtt_broker_001\",\"productName\":\"MQTT服务端产品\",\"linkMethodId\":null,\"linkMethodName\":null,\"componentId\":\"1983886026080284674\",\"componentName\":\"MQTT服务端\",\"protocolId\":\"1983885892432982018\",\"protocolName\":\"MQTT服务端协议\",\"deviceCount\":1,\"deviceType\":\"2\",\"status\":\"0\",\"createBy\":null,\"createTime\":\"2025-10-30 13:19:36\",\"updateBy\":null,\"updateTime\":null,\"remark\":null,\"timeoutSeconds\":60,\"regularCleaning\":\"1\",\"retentionTime\":1,\"retentionUnit\":\"day\",\"customConfig\":null}],\"deviceScope\":\"all\",\"deviceList\":[],\"deviceSnList\":[],\"authHeaderSign\":null,\"authToken\":null,\"routingKey\":null,\"exc', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-02 12:54:56', 14);
INSERT INTO "public"."sys_oper_log" VALUES (267, '规则引擎配置', 2, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.configEngine()', 'PUT', 1, 'admin', '研发部门', '/business/ruleEngine/configEngine', '106.92.105.125', 'XX XX', '{"configJson":"{\"lineList\":[{\"from\":\"vyxwpf3tc\",\"to\":\"7jnz0uhaqk\"},{\"from\":\"7jnz0uhaqk\",\"to\":\"xa80k4nt04\"},{\"from\":\"7jnz0uhaqk\",\"to\":\"wl5fksp67\"}],\"nodeList\":[{\"id\":\"vyxwpf3tc\",\"name\":\"设备日志\",\"type\":\"deviceLog\",\"left\":\"18px\",\"top\":\"242px\",\"ico\":\"el-icon-time\",\"state\":\"success\",\"configData\":{\"productScope\":[\"product_001\",\"mqtt_broker_001\"],\"productList\":[{\"id\":\"1983736431073325058\",\"productSn\":\"product_001\",\"productName\":\"WS产品\",\"linkMethodId\":null,\"linkMethodName\":null,\"componentId\":\"1983736295886712834\",\"componentName\":\"WS网络组件_10883\",\"protocolId\":\"1983735910950268930\",\"protocolName\":\"WS协议\",\"deviceCount\":1,\"deviceType\":\"0\",\"status\":\"0\",\"createBy\":null,\"createTime\":\"2025-10-30 03:23:16\",\"updateBy\":null,\"updateTime\":null,\"remark\":null,\"timeoutSeconds\":60,\"regularCleaning\":\"1\",\"retentionTime\":1,\"retentionUnit\":\"day\",\"customConfig\":null},{\"id\":\"1983886502083457026\",\"productSn\":\"mqtt_broker_001\",\"productName\":\"MQTT服务端产品\",\"linkMethodId\":null,\"linkMethodName\":null,\"componentId\":\"1983886026080284674\",\"componentName\":\"MQTT服务端\",\"protocolId\":\"1983885892432982018\",\"protocolName\":\"MQTT服务端协议\",\"deviceCount\":1,\"deviceType\":\"2\",\"status\":\"0\",\"createBy\":null,\"createTime\":\"2025-10-30 13:19:36\",\"updateBy\":null,\"updateTime\":null,\"remark\":null,\"timeoutSeconds\":60,\"regularCleaning\":\"1\",\"retentionTime\":1,\"retentionUnit\":\"day\",\"customConfig\":null}],\"deviceScope\":\"all\",\"deviceList\":[],\"deviceSnList\":[],\"authHeaderSign\":null,\"authToken\":null,\"routingKey\":null,\"exchange\":\"\",\"topic\":\"\",\"host\":\"\",\"port\":null,\"tags\":null,\"group\":null,\"url\":null,\"key\":null}},{\"id\":\"7jnz0uhaqk\",\"name\":\"实时推送\",\"type\":\"realTimePush\",\"left\":\"280px\",\"top\":\"246px\",\"ico\":\"el-icon-caret-right\",\"state\":\"success\",\"configData\":{\"productScope\":[],\"productList\":[{', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-02 12:55:56', 15);
INSERT INTO "public"."sys_oper_log" VALUES (268, '代码生成', 6, 'com.labdatahub.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '研发部门', '/tool/gen/importTable', '113.248.184.212', 'XX XX', '{"tables":"labdatahub_warn_linkage"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-03 11:13:47', 143);
INSERT INTO "public"."sys_oper_log" VALUES (269, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '113.248.184.212', 'XX XX', '{"businessName":"linkage","className":"LabdatahubWarnLinkage","columns":[{"capJavaField":"Id","columnComment":"id","columnId":110,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-11-03 03:13:47","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":12,"updateBy":"","usableColumn":false},{"capJavaField":"RuleJson","columnComment":"规则json","columnId":111,"columnName":"rule_json","columnType":"text","createBy":"admin","createTime":"2025-11-03 03:13:47","dictType":"","edit":true,"htmlType":"textarea","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"ruleJson","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":12,"updateBy":"","usableColumn":false},{"capJavaField":"WarnMessage","columnComment":"告警消息模板","columnId":112,"columnName":"warn_message","columnType":"text","createBy":"admin","createTime":"2025-11-03 03:13:47","dictType":"","edit":true,"htmlType":"textarea","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"warnMessage","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":12,"updateBy":"","usableColumn":false},{"capJavaField":"WarnLevel","columnComment":"告警等级 1-紧急 2-严重 3-警告 4-正常","columnId":113,"columnName":"warn_level","columnType":"varchar(255)","createBy":"admin","createTime":"2025-11-03 03:13:47","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-03 11:13:57', 175);
INSERT INTO "public"."sys_oper_log" VALUES (270, '代码生成', 8, 'com.labdatahub.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '113.248.184.212', 'XX XX', '{"tables":"labdatahub_warn_linkage"}', NULL, 0, NULL, '2025-11-03 11:14:01', 371);
INSERT INTO "public"."sys_oper_log" VALUES (271, '规则引擎配置', 2, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.configEngine()', 'PUT', 1, 'admin', '研发部门', '/business/ruleEngine/configEngine', '113.248.184.212', 'XX XX', '{"configJson":"{\"lineList\":[{\"from\":\"vyxwpf3tc\",\"to\":\"7jnz0uhaqk\"},{\"from\":\"7jnz0uhaqk\",\"to\":\"xa80k4nt04\"},{\"from\":\"7jnz0uhaqk\",\"to\":\"wl5fksp67\"}],\"nodeList\":[{\"id\":\"vyxwpf3tc\",\"name\":\"设备日志\",\"type\":\"deviceLog\",\"left\":\"18px\",\"top\":\"242px\",\"ico\":\"el-icon-time\",\"state\":\"success\",\"configData\":{\"productScope\":[\"product_001\",\"mqtt_broker_001\"],\"productList\":[{\"id\":\"1983736431073325058\",\"productSn\":\"product_001\",\"productName\":\"WS产品\",\"linkMethodId\":null,\"linkMethodName\":null,\"componentId\":\"1983736295886712834\",\"componentName\":\"WS网络组件_10883\",\"protocolId\":\"1983735910950268930\",\"protocolName\":\"WS协议\",\"deviceCount\":1,\"deviceType\":\"0\",\"status\":\"0\",\"createBy\":null,\"createTime\":\"2025-10-30 03:23:16\",\"updateBy\":null,\"updateTime\":null,\"remark\":null,\"timeoutSeconds\":60,\"regularCleaning\":\"1\",\"retentionTime\":1,\"retentionUnit\":\"day\",\"customConfig\":null},{\"id\":\"1983886502083457026\",\"productSn\":\"mqtt_broker_001\",\"productName\":\"MQTT服务端产品\",\"linkMethodId\":null,\"linkMethodName\":null,\"componentId\":\"1983886026080284674\",\"componentName\":\"MQTT服务端\",\"protocolId\":\"1983885892432982018\",\"protocolName\":\"MQTT服务端协议\",\"deviceCount\":1,\"deviceType\":\"2\",\"status\":\"0\",\"createBy\":null,\"createTime\":\"2025-10-30 13:19:36\",\"updateBy\":null,\"updateTime\":null,\"remark\":null,\"timeoutSeconds\":60,\"regularCleaning\":\"1\",\"retentionTime\":1,\"retentionUnit\":\"day\",\"customConfig\":null}],\"deviceScope\":\"all\",\"deviceList\":[],\"deviceSnList\":[],\"authHeaderSign\":null,\"authToken\":null,\"routingKey\":null,\"exchange\":\"\",\"topic\":\"\",\"host\":\"\",\"port\":null,\"tags\":null,\"group\":null,\"url\":null,\"key\":null}},{\"id\":\"7jnz0uhaqk\",\"name\":\"实时推送\",\"type\":\"realTimePush\",\"left\":\"280px\",\"top\":\"246px\",\"ico\":\"el-icon-caret-right\",\"state\":\"success\",\"configData\":{\"productScope\":[],\"productList\":[{', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-03 18:09:37', 252);
INSERT INTO "public"."sys_oper_log" VALUES (274, '个人信息', 2, 'com.labdatahub.web.controller.system.SysProfileController.updateProfile()', 'PUT', 1, 'admin', '研发部门', '/system/user/profile', '113.251.75.185', 'XX XX', '{"admin":false,"email":"ry@163.com","nickName":"若依","params":{},"phonenumber":"15888888888","sex":"1"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-13 10:01:18', 26);
INSERT INTO "public"."sys_oper_log" VALUES (275, '菜单管理', 1, 'com.labdatahub.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"createBy":"admin","icon":"engine","isCache":"0","isFrame":"1","menuName":"规则编排","menuType":"M","orderNum":2,"params":{},"parentId":0,"path":"engine","status":"0","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-13 10:31:11', 27);
INSERT INTO "public"."sys_oper_log" VALUES (276, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"createTime":"2025-11-13 10:31:11","icon":"engine","isCache":"0","isFrame":"1","menuId":2041,"menuName":"规则编排","menuType":"M","orderNum":0,"params":{},"parentId":0,"path":"engine","perms":"","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-13 10:31:28', 20);
INSERT INTO "public"."sys_oper_log" VALUES (277, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"business/engine/index","createTime":"2025-10-19 18:11:02","icon":"engine","isCache":"1","isFrame":"1","menuId":2040,"menuName":"规则引擎","menuType":"C","orderNum":3,"params":{},"parentId":2041,"path":"engine","perms":"","routeName":"Engine","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-13 10:31:49', 19);
INSERT INTO "public"."sys_oper_log" VALUES (278, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"createTime":"2025-11-13 10:31:11","icon":"tree","isCache":"0","isFrame":"1","menuId":2041,"menuName":"规则编排","menuType":"M","orderNum":0,"params":{},"parentId":0,"path":"engine","perms":"","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-13 10:32:38', 52);
INSERT INTO "public"."sys_oper_log" VALUES (279, '菜单管理', 1, 'com.labdatahub.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"business/engine/index","createBy":"admin","icon":"devicelink","isCache":"1","isFrame":"1","menuName":"设备联动","menuType":"C","orderNum":0,"params":{},"parentId":2041,"path":"devicelink","routeName":"Devicelink","status":"0","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-13 10:36:58', 12);
INSERT INTO "public"."sys_oper_log" VALUES (280, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"business/devicelink/index","createTime":"2025-10-19 18:11:02","icon":"engine","isCache":"1","isFrame":"1","menuId":2040,"menuName":"规则引擎","menuType":"C","orderNum":3,"params":{},"parentId":2041,"path":"engine","perms":"","routeName":"Engine","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-13 10:37:10', 11);
INSERT INTO "public"."sys_oper_log" VALUES (281, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"business/devicelink/index","createTime":"2025-11-13 10:36:58","icon":"devicelink","isCache":"1","isFrame":"1","menuId":2042,"menuName":"设备联动","menuType":"C","orderNum":0,"params":{},"parentId":2041,"path":"devicelink","perms":"","routeName":"Devicelink","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-13 10:37:34', 16);
INSERT INTO "public"."sys_oper_log" VALUES (282, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"business/engine/index","createTime":"2025-10-19 18:11:02","icon":"engine","isCache":"1","isFrame":"1","menuId":2040,"menuName":"规则引擎","menuType":"C","orderNum":3,"params":{},"parentId":2041,"path":"engine","perms":"","routeName":"Engine","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-13 10:37:38', 16);
INSERT INTO "public"."sys_oper_log" VALUES (283, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{"children":[],"component":"business/engine/index","createTime":"2025-10-19 18:11:02","icon":"engine","isCache":"1","isFrame":"1","menuId":2040,"menuName":"规则引擎","menuType":"C","orderNum":0,"params":{},"parentId":2041,"path":"engine","perms":"","routeName":"Engine","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-13 10:37:50', 13);
INSERT INTO "public"."sys_oper_log" VALUES (284, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '113.251.75.185', 'XX XX', '{"children":[],"component":"business/devicelink/index","createTime":"2025-11-13 10:36:58","icon":"devicelink","isCache":"1","isFrame":"1","menuId":2042,"menuName":"设备联动(开发中)","menuType":"C","orderNum":0,"params":{},"parentId":2041,"path":"devicelink","perms":"","routeName":"Devicelink","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-14 09:36:57', 70);
INSERT INTO "public"."sys_oper_log" VALUES (285, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '113.251.75.185', 'XX XX', '{"children":[],"component":"business/devicelink/index","createTime":"2025-11-13 10:36:58","icon":"devicelink","isCache":"1","isFrame":"1","menuId":2042,"menuName":"设备联动","menuType":"C","orderNum":0,"params":{},"parentId":2041,"path":"devicelink","perms":"","routeName":"Devicelink","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-14 09:37:15', 20);
INSERT INTO "public"."sys_oper_log" VALUES (286, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '113.251.75.185', 'XX XX', '{"children":[],"component":"business/engine/index","createTime":"2025-10-19 18:11:02","icon":"engine","isCache":"1","isFrame":"1","menuId":2040,"menuName":"数据转发","menuType":"C","orderNum":0,"params":{},"parentId":2041,"path":"engine","perms":"","routeName":"Engine","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-14 16:14:39', 39);
INSERT INTO "public"."sys_oper_log" VALUES (287, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '113.251.75.185', 'XX XX', '{"children":[],"createTime":"2025-11-13 10:31:11","icon":"tree","isCache":"0","isFrame":"1","menuId":2041,"menuName":"规则引擎","menuType":"M","orderNum":0,"params":{},"parentId":0,"path":"engine","perms":"","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-14 16:14:57', 17);
INSERT INTO "public"."sys_oper_log" VALUES (288, '规则引擎配置', 1, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.add()', 'POST', 1, 'admin', '研发部门', '/business/ruleEngine', '222.75.162.82', 'XX XX', '{"configJson":"{\"lineList\":[],\"nodeList\":[]}","createTime":"2025-11-18 08:50:48","engineName":"01","id":"1990583434028883970"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-18 08:50:48', 17);
INSERT INTO "public"."sys_oper_log" VALUES (289, '代码生成', 6, 'com.labdatahub.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '研发部门', '/tool/gen/importTable', '113.251.92.141', 'XX XX', '{"tables":"labdatahub_linkage_action_record,labdatahub_linkage_warn_record"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-20 13:47:10', 192);
INSERT INTO "public"."sys_oper_log" VALUES (290, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '113.251.92.141', 'XX XX', '{"businessName":"linkageRecord","className":"LabdatahubLinkageActionRecord","columns":[{"capJavaField":"Id","columnComment":"id","columnId":121,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-11-20 13:47:10","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":13,"updateBy":"","usableColumn":false},{"capJavaField":"ConfigId","columnComment":"告警配置id","columnId":122,"columnName":"config_id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-11-20 13:47:10","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"configId","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":13,"updateBy":"","usableColumn":false},{"capJavaField":"ConfigName","columnComment":"告警配置名称","columnId":123,"columnName":"config_name","columnType":"varchar(255)","createBy":"admin","createTime":"2025-11-20 13:47:10","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"configName","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":3,"superColumn":false,"tableId":13,"updateBy":"","usableColumn":false},{"capJavaField":"ExecuteSn","columnComment":"执行动作设备SN","columnId":124,"columnName":"execute_sn","columnType":"text","createBy":"admin","createTime":"2025-11-20 13:47:10","dictType":"","edit":true,"htmlType":"textarea","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isLi', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-20 13:47:40', 201);
INSERT INTO "public"."sys_oper_log" VALUES (291, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '113.251.92.141', 'XX XX', '{"businessName":"linkageAction","className":"LabdatahubLinkageActionRecord","columns":[{"capJavaField":"Id","columnComment":"id","columnId":121,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-11-20 13:47:10","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":13,"updateBy":"","updateTime":"2025-11-20 13:47:40","usableColumn":false},{"capJavaField":"ConfigId","columnComment":"告警配置id","columnId":122,"columnName":"config_id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-11-20 13:47:10","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"configId","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":13,"updateBy":"","updateTime":"2025-11-20 13:47:40","usableColumn":false},{"capJavaField":"ConfigName","columnComment":"告警配置名称","columnId":123,"columnName":"config_name","columnType":"varchar(255)","createBy":"admin","createTime":"2025-11-20 13:47:10","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"configName","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":3,"superColumn":false,"tableId":13,"updateBy":"","updateTime":"2025-11-20 13:47:40","usableColumn":false},{"capJavaField":"ExecuteSn","columnComment":"执行动作设备SN","columnId":124,"columnName":"execute_sn","columnType":"text","createBy":"admin","createTime":"2025-11-20 13:47:10","dictType":"","edit":true,', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-20 13:48:04', 71);
INSERT INTO "public"."sys_oper_log" VALUES (292, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '113.251.92.141', 'XX XX', '{"businessName":"linkageRecord","className":"LabdatahubLinkageWarnRecord","columns":[{"capJavaField":"Id","columnComment":"id","columnId":130,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-11-20 13:47:10","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":14,"updateBy":"","usableColumn":false},{"capJavaField":"ConfigId","columnComment":"告警配置id","columnId":131,"columnName":"config_id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-11-20 13:47:10","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"configId","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":14,"updateBy":"","usableColumn":false},{"capJavaField":"ConfigName","columnComment":"告警配置名称","columnId":132,"columnName":"config_name","columnType":"varchar(255)","createBy":"admin","createTime":"2025-11-20 13:47:10","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"configName","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":3,"superColumn":false,"tableId":14,"updateBy":"","usableColumn":false},{"capJavaField":"WarnMessage","columnComment":"告警内容","columnId":133,"columnName":"warn_message","columnType":"text","createBy":"admin","createTime":"2025-11-20 13:47:10","dictType":"","edit":true,"htmlType":"textarea","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-20 13:48:17', 61);
INSERT INTO "public"."sys_oper_log" VALUES (293, '代码生成', 8, 'com.labdatahub.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '113.251.92.141', 'XX XX', '{"tables":"labdatahub_linkage_action_record,labdatahub_linkage_warn_record"}', NULL, 0, NULL, '2025-11-20 13:48:24', 418);
INSERT INTO "public"."sys_oper_log" VALUES (294, '设备联动告警', 1, 'com.labdatahub.business.controller.LabdatahubWarnLinkageController.add()', 'POST', 1, 'admin', '研发部门', '/business/linkage', '113.249.18.3', 'XX XX', '{"id":"1992771862468337665","name":"设备联动","remark":"设备联动测试"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-24 09:46:50', 34);
INSERT INTO "public"."sys_oper_log" VALUES (295, '菜单管理', 1, 'com.labdatahub.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '113.249.239.242', 'XX XX', '{"children":[],"component":"engine/linkageRecord/index","createBy":"admin","icon":"build","isCache":"1","isFrame":"1","menuName":"告警记录","menuType":"C","orderNum":3,"params":{},"parentId":2041,"path":"linkageRecord","routeName":"LinkageRecord","status":"0","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-26 17:38:59', 64);
INSERT INTO "public"."sys_oper_log" VALUES (296, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '113.249.239.242', 'XX XX', '{"children":[],"component":"business/linkageRecord/index","createTime":"2025-11-26 17:38:59","icon":"build","isCache":"1","isFrame":"1","menuId":2043,"menuName":"告警记录","menuType":"C","orderNum":3,"params":{},"parentId":2041,"path":"linkageRecord","perms":"","routeName":"LinkageRecord","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-26 17:39:16', 36);
INSERT INTO "public"."sys_oper_log" VALUES (297, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '113.249.239.242', 'XX XX', '{"actions":[{"functionCode":"TEST","functionParams":"4564"}],"belongSn":"mqtt_001","belongType":"1","conditions":[{"attribute":"inTemperature","operator":"gt","value":"20"},{"attribute":"outTemperature","operator":"gt","value":"30"}],"delayTime":0,"enable":false,"id":"1983887632913952770","level":"1","message":"温度过高，请及时查看传感器情况。","name":"温度告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-26 17:54:27', 40);
INSERT INTO "public"."sys_oper_log" VALUES (298, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '113.249.239.242', 'XX XX', '{"actions":[{"functionCode":"TEST","functionParams":"测试"}],"belongSn":"WS_DEVICE_001","belongType":"1","conditions":[{"attribute":"outTemperature","operator":"gt","value":"30"},{"attribute":"inTemperature","operator":"gt","value":"20"}],"delayTime":0,"enable":false,"id":"1983737562021261313","level":"2","message":"室外温度和室内温度均达到预警值，请及时查看设备状态。","name":"温湿度告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-26 17:54:33', 30);
INSERT INTO "public"."sys_oper_log" VALUES (299, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1993619586894278658', '113.249.239.242', 'XX XX', '"1993619586894278658"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-26 17:55:41', 13);
INSERT INTO "public"."sys_oper_log" VALUES (300, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1993621459210612737', '113.249.239.242', 'XX XX', '"1993621459210612737"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-26 18:03:35', 27);
INSERT INTO "public"."sys_oper_log" VALUES (301, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1993621462217928707', '113.249.239.242', 'XX XX', '"1993621462217928707"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-26 18:03:36', 15);
INSERT INTO "public"."sys_oper_log" VALUES (302, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1993621609014374402', '113.249.239.242', 'XX XX', '"1993621609014374402"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-26 18:03:38', 13);
INSERT INTO "public"."sys_oper_log" VALUES (303, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1993621602743889922', '113.249.239.242', 'XX XX', '"1993621602743889922"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-26 18:03:38', 10);
INSERT INTO "public"."sys_oper_log" VALUES (304, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1993621596913807362', '113.249.239.242', 'XX XX', '"1993621596913807362"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-26 18:03:40', 12);
INSERT INTO "public"."sys_oper_log" VALUES (305, '设备联动告警', 1, 'com.labdatahub.business.controller.LabdatahubWarnLinkageController.add()', 'POST', 1, 'admin', '研发部门', '/business/linkage', '113.249.239.242', 'XX XX', '{"createTime":"2025-11-27 10:58:14","id":"1993876994224828418","name":"WS设备离线"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-27 10:58:14', 21);
INSERT INTO "public"."sys_oper_log" VALUES (306, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/4', '113.249.239.242', 'XX XX', '4', '{"msg":"菜单已分配,不允许删除","code":601}', 0, NULL, '2025-11-28 11:32:32', 149);
INSERT INTO "public"."sys_oper_log" VALUES (307, '角色管理', 2, 'com.labdatahub.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/role', '113.249.239.242', 'XX XX', '{"admin":false,"createTime":"2025-09-15 11:09:57","dataScope":"2","delFlag":"0","deptCheckStrictly":true,"flag":false,"menuCheckStrictly":true,"menuIds":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,116,1055,1056,1057,1058,1059,1060,117],"params":{},"remark":"普通角色","roleId":2,"roleKey":"common","roleName":"普通角色","roleSort":2,"status":"0","updateBy":"admin"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-28 11:33:08', 138);
INSERT INTO "public"."sys_oper_log" VALUES (308, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/4', '113.249.239.242', 'XX XX', '4', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-28 11:33:14', 28);
INSERT INTO "public"."sys_oper_log" VALUES (309, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/warnRecord/deal/1993618827939164162', '113.249.239.242', 'XX XX', '"1993618827939164162"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-28 11:56:14', 52);
INSERT INTO "public"."sys_oper_log" VALUES (310, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/warnRecord/deal/1983894353912815619', '113.249.239.242', 'XX XX', '"1983894353912815619"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-11-28 11:56:19', 12);
INSERT INTO "public"."sys_oper_log" VALUES (311, '设备联动告警记录', 3, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/linkageRecord/1993877377668100097', '14.145.15.87', 'XX XX', '["1993877377668100097"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-01 10:01:21', 73);
INSERT INTO "public"."sys_oper_log" VALUES (312, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1993877373738037250', '14.145.15.87', 'XX XX', '"1993877373738037250"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-01 10:01:30', 14);
INSERT INTO "public"."sys_oper_log" VALUES (313, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1993621589326311426', '14.145.15.87', 'XX XX', '"1993621589326311426"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-01 10:01:42', 26);
INSERT INTO "public"."sys_oper_log" VALUES (314, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/b81facd1-c75c-4b35-83c9-86f50f44ec18', '106.84.61.120', 'XX XX', '"b81facd1-c75c-4b35-83c9-86f50f44ec18"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-01 16:25:16', 8);
INSERT INTO "public"."sys_oper_log" VALUES (315, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/3443eae4-a033-4438-86ba-f66ffd71188e', '106.84.61.120', 'XX XX', '"3443eae4-a033-4438-86ba-f66ffd71188e"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-01 16:25:18', 1);
INSERT INTO "public"."sys_oper_log" VALUES (316, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/993dd71d-bdf8-460c-b0aa-ea5cd697105d', '106.84.61.120', 'XX XX', '"993dd71d-bdf8-460c-b0aa-ea5cd697105d"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-01 16:25:19', 1);
INSERT INTO "public"."sys_oper_log" VALUES (317, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/71010743-160c-4079-9794-858e41c72acc', '106.84.61.120', 'XX XX', '"71010743-160c-4079-9794-858e41c72acc"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-01 16:25:22', 0);
INSERT INTO "public"."sys_oper_log" VALUES (318, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/659a537a-ff09-4ad1-a5f0-b8ac03a5f672', '106.84.61.120', 'XX XX', '"659a537a-ff09-4ad1-a5f0-b8ac03a5f672"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-01 16:25:52', 7);
INSERT INTO "public"."sys_oper_log" VALUES (319, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/f92550e7-60cb-468a-85de-0e8e5a4e6031', '106.84.61.120', 'XX XX', '"f92550e7-60cb-468a-85de-0e8e5a4e6031"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-01 16:25:54', 1);
INSERT INTO "public"."sys_oper_log" VALUES (320, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/6015b3e7-f699-44e5-ab70-a40022027509', '106.84.61.120', 'XX XX', '"6015b3e7-f699-44e5-ab70-a40022027509"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-01 16:25:59', 1);
INSERT INTO "public"."sys_oper_log" VALUES (321, '代码生成', 6, 'com.labdatahub.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '研发部门', '/tool/gen/importTable', '113.250.144.137', 'XX XX', '{"tables":"labdatahub_scheduled_task"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-02 11:05:23', 123);
INSERT INTO "public"."sys_oper_log" VALUES (322, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '113.250.144.137', 'XX XX', '{"businessName":"scheduledEngine","className":"LabdatahubScheduledTask","columns":[{"capJavaField":"Id","columnComment":"id","columnId":140,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-12-02 11:05:23","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":15,"updateBy":"","usableColumn":false},{"capJavaField":"Name","columnComment":"配置名称","columnId":141,"columnName":"name","columnType":"varchar(255)","createBy":"admin","createTime":"2025-12-02 11:05:23","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"name","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":2,"superColumn":false,"tableId":15,"updateBy":"","usableColumn":false},{"capJavaField":"RuleJson","columnComment":"规则json","columnId":142,"columnName":"rule_json","columnType":"text","createBy":"admin","createTime":"2025-12-02 11:05:23","dictType":"","edit":true,"htmlType":"textarea","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"ruleJson","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":15,"updateBy":"","usableColumn":false},{"capJavaField":"IsEnable","columnComment":"是否启用 0-否 1-是","columnId":143,"columnName":"is_enable","columnType":"varchar(255)","createBy":"admin","createTime":"2025-12-02 11:05:23","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQ', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-02 11:06:00', 319);
INSERT INTO "public"."sys_oper_log" VALUES (323, '代码生成', 8, 'com.labdatahub.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '113.250.144.137', 'XX XX', '{"tables":"labdatahub_scheduled_task"}', NULL, 0, NULL, '2025-12-02 11:06:04', 104);
INSERT INTO "public"."sys_oper_log" VALUES (324, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/warnRecord/deal/1993618847107133442', '123.14.78.172', 'XX XX', '"1993618847107133442"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-03 08:38:41', 14);
INSERT INTO "public"."sys_oper_log" VALUES (325, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1993877199703781377', '36.248.206.73', 'XX XX', '"1993877199703781377"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-03 12:44:36', 580);
INSERT INTO "public"."sys_oper_log" VALUES (326, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1993877199703781377', '36.248.206.73', 'XX XX', '"1993877199703781377"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-03 12:44:36', 1731);
INSERT INTO "public"."sys_oper_log" VALUES (327, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '183.17.49.124', 'XX XX', '{"belongSn":"qqq","belongType":"0","createTime":"2025-12-03 14:06:16","functionCode":"111","functionName":"重复","functionParams":"按订单","id":"1996098642604732417","protocolId":"1983885892432982018"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-03 14:06:16', 1001);
INSERT INTO "public"."sys_oper_log" VALUES (328, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1993621592836943873', '183.17.49.124', 'XX XX', '"1993621592836943873"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-03 14:09:57', 289);
INSERT INTO "public"."sys_oper_log" VALUES (329, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1996417648381407233', '58.247.152.210', 'XX XX', '"1996417648381407233"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-04 15:08:36', 40);
INSERT INTO "public"."sys_oper_log" VALUES (330, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1996414546987200513', '218.94.38.115', 'XX XX', '"1996414546987200513"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-05 14:38:50', 90);
INSERT INTO "public"."sys_oper_log" VALUES (331, '个人信息', 2, 'com.labdatahub.web.controller.system.SysProfileController.updatePwd()', 'PUT', 1, 'admin', '研发部门', '/system/user/profile/updatePwd', '183.17.49.124', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-05 16:23:20', 420);
INSERT INTO "public"."sys_oper_log" VALUES (332, '个人信息', 2, 'com.labdatahub.web.controller.system.SysProfileController.updatePwd()', 'PUT', 1, 'admin', '研发部门', '/system/user/profile/updatePwd', '106.92.193.49', 'XX XX', '{}', '{"msg":"演示模式，不可修改密码","code":500}', 0, NULL, '2025-12-06 02:00:28', 7);
INSERT INTO "public"."sys_oper_log" VALUES (333, '个人信息', 2, 'com.labdatahub.web.controller.system.SysProfileController.updatePwd()', 'PUT', 1, 'admin', '研发部门', '/system/user/profile/updatePwd', '106.92.193.49', 'XX XX', '{}', '{"msg":"演示模式，不可修改密码","code":500}', 0, NULL, '2025-12-06 02:00:29', 0);
INSERT INTO "public"."sys_oper_log" VALUES (334, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1997648516567543809', '106.92.146.84', 'XX XX', '"1997648516567543809"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-07 20:46:35', 19);
INSERT INTO "public"."sys_oper_log" VALUES (335, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '113.248.98.239', 'XX XX', '{"children":[],"component":"business/linkageRecord/index","createTime":"2025-11-26 17:38:59","icon":"build","isCache":"1","isFrame":"1","menuId":2043,"menuName":"告警记录","menuType":"C","orderNum":4,"params":{},"parentId":2041,"path":"linkageRecord","perms":"","routeName":"LinkageRecord","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-08 17:52:34', 117);
INSERT INTO "public"."sys_oper_log" VALUES (336, '菜单管理', 1, 'com.labdatahub.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '113.248.98.239', 'XX XX', '{"children":[],"component":"engine/ScheduledEngine/index","createBy":"admin","icon":"cascader","isCache":"1","isFrame":"1","menuName":"定时任务","menuType":"C","orderNum":3,"params":{},"parentId":2041,"path":"scheduledEngine","routeName":"ScheduledEngine","status":"0","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-08 17:54:22', 212);
INSERT INTO "public"."sys_oper_log" VALUES (337, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '113.248.98.239', 'XX XX', '{"children":[],"component":"business/ScheduledEngine/index","createTime":"2025-12-08 17:54:22","icon":"cascader","isCache":"1","isFrame":"1","menuId":2044,"menuName":"定时任务","menuType":"C","orderNum":3,"params":{},"parentId":2041,"path":"scheduledEngine","perms":"","routeName":"ScheduledEngine","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-08 17:55:08', 46);
INSERT INTO "public"."sys_oper_log" VALUES (338, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '113.248.98.239', 'XX XX', '{"children":[],"component":"business/scheduledEngine/index","createTime":"2025-12-08 17:54:22","icon":"cascader","isCache":"1","isFrame":"1","menuId":2044,"menuName":"定时任务","menuType":"C","orderNum":3,"params":{},"parentId":2041,"path":"scheduledEngine","perms":"","routeName":"ScheduledEngine","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-08 17:55:30', 74);
INSERT INTO "public"."sys_oper_log" VALUES (339, '定时引擎配置', 1, 'com.labdatahub.business.controller.LabdatahubScheduledTaskController.add()', 'POST', 1, 'admin', '研发部门', '/business/scheduledEngine', '113.248.98.239', 'XX XX', '{"id":"1997971901910626305","name":"定时调节温度","remark":"定时调节温度"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-08 18:09:56', 63);
INSERT INTO "public"."sys_oper_log" VALUES (340, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/warnRecord/deal/1993618780304453635', '58.213.35.162', 'XX XX', '"1993618780304453635"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-09 09:10:03', 133);
INSERT INTO "public"."sys_oper_log" VALUES (341, '定时引擎配置', 3, 'com.labdatahub.business.controller.LabdatahubScheduledTaskController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/scheduledEngine/1997971901910626305', '113.248.98.239', 'XX XX', '["1997971901910626305"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-09 16:49:30', 79);
INSERT INTO "public"."sys_oper_log" VALUES (342, '定时引擎配置', 1, 'com.labdatahub.business.controller.LabdatahubScheduledTaskController.add()', 'POST', 1, 'admin', '研发部门', '/business/scheduledEngine', '113.248.98.239', 'XX XX', '{"createTime":"2025-12-09 16:49:40","id":"1998314088708964354","name":"温度定时调节","remark":"温度定时调节"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-09 16:49:40', 12);
INSERT INTO "public"."sys_oper_log" VALUES (343, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998232072494788609', '106.47.42.38', 'XX XX', '"1998232072494788609"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-09 21:24:30', 27);
INSERT INTO "public"."sys_oper_log" VALUES (344, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '113.248.98.239', 'XX XX', '{"belongSn":"HJ_001","belongType":"1","createTime":"2025-12-10 09:21:31","functionCode":"wd_control","functionName":"调节温度","functionParams":"20","id":"1998563698006294530","protocolId":"1998562616433704961"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 09:21:31', 83);
INSERT INTO "public"."sys_oper_log" VALUES (345, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '113.248.98.239', 'XX XX', '{"actions":[{"functionCode":"wd_control","functionParams":"20"}],"belongSn":"HJ_001","belongType":"1","conditions":[{"attribute":"inTemperature","operator":"gt","value":"25"},{"attribute":"outTemperature","operator":"gt","value":"30"}],"delayTime":60,"enable":true,"id":"1998563991662100481","level":"2","message":"温度预警","name":"高温预警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 09:22:41', 44);
INSERT INTO "public"."sys_oper_log" VALUES (346, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '113.248.98.239', 'XX XX', '{"belongSn":"SW_PRODUCT","belongType":"0","createTime":"2025-12-10 09:29:28","functionCode":"water_out","functionName":"放水","functionParams":"0.8","id":"1998565697795915777","protocolId":"1998564778664525825"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 09:29:28', 43);
INSERT INTO "public"."sys_oper_log" VALUES (347, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '113.248.98.239', 'XX XX', '{"actions":[{"functionCode":"water_out","functionParams":"0.8"}],"belongSn":"SW_PRODUCT","belongType":"0","conditions":[{"attribute":"water_high","operator":"gt","value":"1"}],"delayTime":60,"enable":true,"id":"1998565882991214594","level":"1","message":"水位过高","name":"水位告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 09:30:12', 100);
INSERT INTO "public"."sys_oper_log" VALUES (348, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '113.248.98.239', 'XX XX', '{"belongSn":"SW_001","belongType":"1","createTime":"2025-12-10 09:40:00","functionCode":"water_control","functionName":"水位调节","functionParams":"0.8","id":"1998568349292298241","protocolId":"1998567823599206401"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 09:40:00', 12);
INSERT INTO "public"."sys_oper_log" VALUES (349, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '113.248.98.239', 'XX XX', '{"belongSn":"SW_01","belongType":"1","createTime":"2025-12-10 09:45:25","functionCode":"water_control","functionName":"水位调节","functionParams":"0.8","id":"1998569710050344961","protocolId":"1998567823599206401"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 09:45:25', 20);
INSERT INTO "public"."sys_oper_log" VALUES (350, '规则引擎配置', 3, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/ruleEngine/1990583434028883970', '113.248.98.239', 'XX XX', '["1990583434028883970"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 09:46:48', 86);
INSERT INTO "public"."sys_oper_log" VALUES (351, '规则引擎配置', 3, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/ruleEngine/1984562257922650113', '113.248.98.239', 'XX XX', '["1984562257922650113"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 09:46:51', 14);
INSERT INTO "public"."sys_oper_log" VALUES (352, '设备联动告警', 3, 'com.labdatahub.business.controller.LabdatahubWarnLinkageController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/linkage/1993876994224828418', '113.248.98.239', 'XX XX', '["1993876994224828418"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 09:46:56', 9);
INSERT INTO "public"."sys_oper_log" VALUES (353, '设备联动告警', 3, 'com.labdatahub.business.controller.LabdatahubWarnLinkageController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/linkage/1992771862468337665', '113.248.98.239', 'XX XX', '["1992771862468337665"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 09:46:58', 9);
INSERT INTO "public"."sys_oper_log" VALUES (354, '定时引擎配置', 3, 'com.labdatahub.business.controller.LabdatahubScheduledTaskController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/scheduledEngine/1998314088708964354', '113.248.98.239', 'XX XX', '["1998314088708964354"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 09:47:01', 9);
INSERT INTO "public"."sys_oper_log" VALUES (355, '定时引擎配置', 1, 'com.labdatahub.business.controller.LabdatahubScheduledTaskController.add()', 'POST', 1, 'admin', '研发部门', '/business/scheduledEngine', '113.248.98.239', 'XX XX', '{"createTime":"2025-12-10 09:47:14","id":"1998570168554881025","name":"定时温度调节","remark":"定时温度调节"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 09:47:14', 12);
INSERT INTO "public"."sys_oper_log" VALUES (356, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '113.248.98.239', 'XX XX', '{"belongSn":"HJ_001","belongType":"1","createTime":"2025-12-10 09:56:45","functionCode":"213","functionName":"测试","id":"1998572564794310658","protocolId":"1998562616433704961"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 09:56:45', 12);
INSERT INTO "public"."sys_oper_log" VALUES (357, '设备指令下发', 3, 'com.labdatahub.business.controller.LabdatahubFunctionController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/function/1998572564794310658', '113.248.98.239', 'XX XX', '["1998572564794310658"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 09:56:57', 1344);
INSERT INTO "public"."sys_oper_log" VALUES (358, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '113.248.98.239', 'XX XX', '{"belongSn":"HJ_01","belongType":"1","createTime":"2025-12-10 10:06:56","functionCode":"wd_control","functionName":"温度调节","functionParams":"20","id":"1998575126520004609","protocolId":"1998562616433704961"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 10:06:56', 13);
INSERT INTO "public"."sys_oper_log" VALUES (359, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '113.248.98.239', 'XX XX', '{"actions":[{"functionCode":"wd_control","functionParams":"20"}],"belongSn":"HJ_01","belongType":"1","conditions":[{"attribute":"inTemperature","operator":"gt","value":"20"}],"delayTime":0,"enable":true,"id":"1998575604737769473","level":"1","message":"温度告警","name":"温度告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 10:08:50', 255);
INSERT INTO "public"."sys_oper_log" VALUES (360, '设备联动告警', 1, 'com.labdatahub.business.controller.LabdatahubWarnLinkageController.add()', 'POST', 1, 'admin', '研发部门', '/business/linkage', '113.248.98.239', 'XX XX', '{"createTime":"2025-12-10 10:10:53","id":"1998576121320833025","name":"联动告警"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 10:10:53', 222);
INSERT INTO "public"."sys_oper_log" VALUES (361, '规则引擎配置', 1, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.add()', 'POST', 1, 'admin', '研发部门', '/business/ruleEngine', '113.248.98.239', 'XX XX', '{"configJson":"{\"lineList\":[],\"nodeList\":[]}","createTime":"2025-12-10 10:12:37","engineName":"数据转发","id":"1998576555032838146","remark":"数据转发"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 10:12:37', 20);
INSERT INTO "public"."sys_oper_log" VALUES (362, '规则引擎配置', 2, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.configEngine()', 'PUT', 1, 'admin', '研发部门', '/business/ruleEngine/configEngine', '113.248.98.239', 'XX XX', '{"configJson":"{\"lineList\":[{\"from\":\"yf6v8ryif\",\"to\":\"ejfac7z8t\"},{\"from\":\"ejfac7z8t\",\"to\":\"c44mse2osa\"}],\"nodeList\":[{\"id\":\"ejfac7z8t\",\"name\":\"实时推送\",\"type\":\"realTimePush\",\"left\":\"249px\",\"top\":\"167px\",\"ico\":\"el-icon-caret-right\",\"state\":\"success\"},{\"id\":\"yf6v8ryif\",\"name\":\"设备日志\",\"type\":\"deviceLog\",\"left\":\"0px\",\"top\":\"141px\",\"ico\":\"el-icon-time\",\"state\":\"success\",\"configData\":{\"productScope\":[\"HJ_PRODUCT\"],\"productList\":[{\"id\":\"1998562969057230849\",\"productSn\":\"HJ_PRODUCT\",\"productName\":\"环境检测仪\",\"linkMethodId\":null,\"linkMethodName\":null,\"componentId\":\"1998562852543660033\",\"componentName\":\"环境检测仪\",\"protocolId\":\"1998562616433704961\",\"protocolName\":\"环境监测仪协议\",\"deviceCount\":1,\"deviceType\":\"0\",\"status\":\"0\",\"createBy\":null,\"createTime\":\"2025-12-10 09:18:38\",\"updateBy\":null,\"updateTime\":null,\"remark\":\"环境检测仪\",\"timeoutSeconds\":60,\"regularCleaning\":\"0\",\"retentionTime\":1,\"retentionUnit\":null,\"customConfig\":null},{\"id\":\"1998568066453602306\",\"productSn\":\"SW_PRODUCT\",\"productName\":\"水位检测仪\",\"linkMethodId\":null,\"linkMethodName\":null,\"componentId\":\"1998564935472775169\",\"componentName\":\"水位检测仪组件\",\"protocolId\":\"1998567823599206401\",\"protocolName\":\"水位检测仪\",\"deviceCount\":1,\"deviceType\":\"1\",\"status\":\"0\",\"createBy\":null,\"createTime\":\"2025-12-10 09:38:53\",\"updateBy\":null,\"updateTime\":null,\"remark\":\"水位检测仪\",\"timeoutSeconds\":60,\"regularCleaning\":\"0\",\"retentionTime\":1,\"retentionUnit\":null,\"customConfig\":null}],\"deviceScope\":\"all\",\"deviceList\":[],\"deviceSnList\":[],\"authHeaderSign\":null,\"authToken\":null,\"routingKey\":null,\"exchange\":\"\",\"topic\":\"\",\"host\":\"\",\"port\":null,\"tags\":null,\"group\":null,\"url\":null,\"key\":null}},{\"id\":\"c44mse2osa\",\"name\":\"HTTP接口\",\"type\":\"HTTP\",\"left\":\"511px\",\"top\":\"150px\",\"ico\":\"el-icon-caret-right\",\"state\', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 10:13:32', 34);
INSERT INTO "public"."sys_oper_log" VALUES (405, '代码生成', 8, 'com.labdatahub.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '180.107.171.127', 'XX XX', '{"tables":"labdatahub_modbus_config"}', NULL, 0, NULL, '2026-01-02 23:16:56', 148);
INSERT INTO "public"."sys_oper_log" VALUES (363, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998231982996729858', '220.202.238.0', 'XX XX', '"1998231982996729858"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 11:15:03', 70);
INSERT INTO "public"."sys_oper_log" VALUES (364, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1997681604928483330', '220.202.238.0', 'XX XX', '"1997681604928483330"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-10 11:15:06', 9);
INSERT INTO "public"."sys_oper_log" VALUES (365, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '125.80.215.66', 'XX XX', '{"children":[],"component":"business/product/index","createTime":"2025-09-18 13:58:31","icon":"product","isCache":"0","isFrame":"1","menuId":2012,"menuName":"产品管理","menuType":"C","orderNum":0,"params":{},"parentId":2036,"path":"product","perms":"business:product:list","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-12 10:46:46', 150);
INSERT INTO "public"."sys_oper_log" VALUES (366, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '125.80.215.66', 'XX XX', '{"children":[],"component":"business/device/index","createTime":"2025-09-18 13:58:25","icon":"device","isCache":"0","isFrame":"1","menuId":2006,"menuName":"设备管理","menuType":"C","orderNum":1,"params":{},"parentId":2036,"path":"device","perms":"business:device:list","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-12 10:46:51', 20);
INSERT INTO "public"."sys_oper_log" VALUES (367, '菜单管理', 1, 'com.labdatahub.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '125.80.215.66', 'XX XX', '{"children":[],"component":"business/map/index","createBy":"admin","icon":"guide","isCache":"0","isFrame":"1","menuName":"地图服务","menuType":"C","orderNum":3,"params":{},"parentId":2036,"path":"map","routeName":"Map","status":"0","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-12 10:47:55', 23);
INSERT INTO "public"."sys_oper_log" VALUES (368, '设备联动告警', 1, 'com.labdatahub.business.controller.LabdatahubWarnLinkageController.add()', 'POST', 1, 'admin', '研发部门', '/business/linkage', '39.90.115.250', 'XX XX', '{"createTime":"2025-12-12 15:26:29","id":"1999380318793510914","name":"开关1","remark":"开关11"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-12 15:26:29', 67);
INSERT INTO "public"."sys_oper_log" VALUES (369, '设备联动告警', 2, 'com.labdatahub.business.controller.LabdatahubWarnLinkageController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/linkage', '39.90.115.250', 'XX XX', '{"createTime":"2025-12-12 15:26:29","id":"1999380318793510914","isEnable":"0","name":"","remark":"","warnLevel":"1"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-12 15:44:47', 44);
INSERT INTO "public"."sys_oper_log" VALUES (370, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1996413235180875778', '122.193.105.34', 'XX XX', '"1996413235180875778"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-12 16:35:47', 22);
INSERT INTO "public"."sys_oper_log" VALUES (371, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1996414267873046529', '101.39.212.232', 'XX XX', '"1996414267873046529"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-12 17:30:04', 13);
INSERT INTO "public"."sys_oper_log" VALUES (372, '设备联动告警', 3, 'com.labdatahub.business.controller.LabdatahubWarnLinkageController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/linkage/1999380318793510914', '113.251.64.67', 'XX XX', '["1999380318793510914"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-15 17:51:32', 18);
INSERT INTO "public"."sys_oper_log" VALUES (373, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '183.230.64.242', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2025/12/16/807ef4a3ad9e4176a84f9be14ce8bdcf.jpg","code":200}', 0, NULL, '2025-12-16 10:07:20', 324);
INSERT INTO "public"."sys_oper_log" VALUES (374, '个人信息', 2, 'com.labdatahub.web.controller.system.SysProfileController.updateProfile()', 'PUT', 1, 'admin', '研发部门', '/system/user/profile', '183.230.64.242', 'XX XX', '{"admin":false,"email":"ry@163.com","nickName":"若依","params":{},"phonenumber":"15888888888","sex":"1"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-16 10:07:26', 25);
INSERT INTO "public"."sys_oper_log" VALUES (375, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1996413634273095681', '60.24.208.115', 'XX XX', '"1996413634273095681"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-16 11:14:05', 178);
INSERT INTO "public"."sys_oper_log" VALUES (376, '代码生成', 6, 'com.labdatahub.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '研发部门', '/tool/gen/importTable', '113.251.64.67', 'XX XX', '{"tables":"labdatahub_modbus_config"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-16 13:40:59', 133);
INSERT INTO "public"."sys_oper_log" VALUES (377, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '113.251.64.67', 'XX XX', '{"businessName":"modbus","className":"LabdatahubModbusConfig","columns":[{"capJavaField":"Id","columnComment":"id","columnId":148,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-12-16 13:40:59","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":16,"updateBy":"","usableColumn":false},{"capJavaField":"BelongSn","columnComment":"归属sn","columnId":149,"columnName":"belong_sn","columnType":"varchar(255)","createBy":"admin","createTime":"2025-12-16 13:40:59","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"belongSn","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":16,"updateBy":"","usableColumn":false},{"capJavaField":"BelongType","columnComment":"归属类型 0-产品 1-设备","columnId":150,"columnName":"belong_type","columnType":"varchar(255)","createBy":"admin","createTime":"2025-12-16 13:40:59","dictType":"","edit":true,"htmlType":"select","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"belongType","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":16,"updateBy":"","usableColumn":false},{"capJavaField":"Code","columnComment":"读取编码","columnId":151,"columnName":"code","columnType":"varchar(255)","createBy":"admin","createTime":"2025-12-16 13:40:59","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0",', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-16 13:41:21', 253);
INSERT INTO "public"."sys_oper_log" VALUES (378, '代码生成', 8, 'com.labdatahub.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '113.251.64.67', 'XX XX', '{"tables":"labdatahub_modbus_config"}', NULL, 0, NULL, '2025-12-16 13:41:23', 147);
INSERT INTO "public"."sys_oper_log" VALUES (379, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '36.154.196.46', 'XX XX', '{"belongSn":"HJ_PRODUCT","belongType":"0","createTime":"2025-12-16 16:04:46","id":"2000839506379943938","protocolId":"1998562616433704961"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-16 16:04:46', 36);
INSERT INTO "public"."sys_oper_log" VALUES (380, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2040', '117.155.248.37', 'XX XX', '2040', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-18 20:35:51', 146);
INSERT INTO "public"."sys_oper_log" VALUES (381, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1996413584755142658', '1.85.56.110', 'XX XX', '"1996413584755142658"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-19 10:52:35', 131);
INSERT INTO "public"."sys_oper_log" VALUES (382, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '106.114.184.132', 'XX XX', '{"belongSn":"ywy","belongType":"1","createTime":"2025-12-19 14:50:36","functionCode":"deep","functionName":"深度","id":"2001908005021954049","protocolId":"1998562616433704961"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-19 14:50:36', 276);
INSERT INTO "public"."sys_oper_log" VALUES (383, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '106.114.184.132', 'XX XX', '{"belongSn":"ywy","belongType":"1","createTime":"2025-12-19 14:50:37","functionCode":"deep","functionName":"深度","functionParams":"deep","id":"2001908005021954049","protocolId":"1998562616433704961"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-19 14:50:49', 16);
INSERT INTO "public"."sys_oper_log" VALUES (384, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '113.248.98.209', 'XX XX', '{"children":[],"component":"business/devicelink/index","createTime":"2025-11-13 10:36:58","icon":"devicelink","isCache":"1","isFrame":"1","menuId":2042,"menuName":"设备联动","menuType":"C","orderNum":2,"params":{},"parentId":2041,"path":"devicelink","perms":"","routeName":"Devicelink","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-23 13:34:54', 76);
INSERT INTO "public"."sys_oper_log" VALUES (385, '菜单管理', 1, 'com.labdatahub.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '113.248.98.209', 'XX XX', '{"children":[],"component":"business/engine/index","createBy":"admin","icon":"engine","isCache":"1","isFrame":"1","menuName":"数据转发","menuType":"C","orderNum":1,"params":{},"parentId":2041,"path":"engine","routeName":"Engine","status":"0","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-23 13:35:44', 18);
INSERT INTO "public"."sys_oper_log" VALUES (386, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1993621614420832258', '111.124.131.95', 'XX XX', '"1993621614420832258"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-24 11:08:17', 11);
INSERT INTO "public"."sys_oper_log" VALUES (387, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998231982996729858', '111.124.131.95', 'XX XX', '"1998231982996729858"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-24 11:08:19', 4);
INSERT INTO "public"."sys_oper_log" VALUES (388, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998231982996729858', '111.124.131.95', 'XX XX', '"1998231982996729858"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-24 11:08:22', 7);
INSERT INTO "public"."sys_oper_log" VALUES (389, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998231982996729858', '111.124.131.95', 'XX XX', '"1998231982996729858"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-24 11:08:26', 1009);
INSERT INTO "public"."sys_oper_log" VALUES (390, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1996413502647447555', '117.175.103.60', 'XX XX', '"1996413502647447555"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-26 20:31:36', 29);
INSERT INTO "public"."sys_oper_log" VALUES (391, '代码生成', 6, 'com.labdatahub.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '研发部门', '/tool/gen/importTable', '106.92.111.86', 'XX XX', '{"tables":"device_environmental_monitoring"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-28 01:38:07', 200);
INSERT INTO "public"."sys_oper_log" VALUES (392, '代码生成', 8, 'com.labdatahub.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '106.92.111.86', 'XX XX', '{"tables":"device_environmental_monitoring"}', NULL, 0, NULL, '2025-12-28 01:38:12', 191);
INSERT INTO "public"."sys_oper_log" VALUES (393, '代码生成', 3, 'com.labdatahub.generator.controller.GenController.remove()', 'DELETE', 1, 'admin', '研发部门', '/tool/gen/17', '106.92.111.86', 'XX XX', '[17]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-28 02:46:10', 215);
INSERT INTO "public"."sys_oper_log" VALUES (394, '设备联动告警', 1, 'com.labdatahub.business.controller.LabdatahubWarnLinkageController.add()', 'POST', 1, 'admin', '研发部门', '/business/linkage', '221.12.5.179', 'XX XX', '{"createTime":"2025-12-29 14:48:02","id":"2005531235171442690","name":"测试"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-29 14:48:02', 110);
INSERT INTO "public"."sys_oper_log" VALUES (395, '设备联动告警', 3, 'com.labdatahub.business.controller.LabdatahubWarnLinkageController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/linkage/2005531235171442690', '221.12.5.179', 'XX XX', '["2005531235171442690"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-29 15:56:48', 221);
INSERT INTO "public"."sys_oper_log" VALUES (396, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1993621588730720258', '58.19.17.131', 'XX XX', '"1993621588730720258"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-30 10:16:52', 171);
INSERT INTO "public"."sys_oper_log" VALUES (397, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1996413401581498370', '58.19.17.131', 'XX XX', '"1996413401581498370"', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-31 12:39:55', 123);
INSERT INTO "public"."sys_oper_log" VALUES (398, '角色管理', 2, 'com.labdatahub.web.controller.system.SysRoleController.changeStatus()', 'PUT', 1, 'admin', '研发部门', '/system/role/changeStatus', '120.236.210.197', 'XX XX', '{"admin":false,"deptCheckStrictly":false,"flag":false,"menuCheckStrictly":false,"params":{},"roleId":2,"status":"1","updateBy":"admin"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-31 14:18:09', 36);
INSERT INTO "public"."sys_oper_log" VALUES (399, '角色管理', 2, 'com.labdatahub.web.controller.system.SysRoleController.changeStatus()', 'PUT', 1, 'admin', '研发部门', '/system/role/changeStatus', '120.236.210.197', 'XX XX', '{"admin":false,"deptCheckStrictly":false,"flag":false,"menuCheckStrictly":false,"params":{},"roleId":2,"status":"0","updateBy":"admin"}', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-31 14:18:12', 19);
INSERT INTO "public"."sys_oper_log" VALUES (400, '设备联动告警记录', 3, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/linkageRecord/1993621459210612737', '58.19.17.131', 'XX XX', '["1993621459210612737"]', '{"msg":"操作成功","code":200}', 0, NULL, '2025-12-31 16:18:48', 67);
INSERT INTO "public"."sys_oper_log" VALUES (401, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998232072494788609', '106.92.111.25', 'XX XX', '"1998232072494788609"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-02 16:48:44', 6);
INSERT INTO "public"."sys_oper_log" VALUES (402, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '180.107.171.127', 'XX XX', '{"belongSn":"HJ_01","belongType":"1","createTime":"2025-12-16 16:04:47","functionCode":"test","functionName":"test","id":"2006251209896763393","protocolId":"1998562616433704961"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-02 21:33:05', 83);
INSERT INTO "public"."sys_oper_log" VALUES (403, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1996413146525872130', '180.107.171.127', 'XX XX', '"1996413146525872130"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-02 23:00:52', 54);
INSERT INTO "public"."sys_oper_log" VALUES (404, '字典类型', 2, 'com.labdatahub.web.controller.system.SysDictTypeController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/dict/type', '180.107.171.127', 'XX XX', '{"createBy":"admin","createTime":"2025-09-15 11:09:58","dictId":2,"dictName":"菜单状态","dictType":"sys_show_hide","params":{},"remark":"菜单状态列表","status":"0","updateBy":"admin"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-02 23:06:05', 150);
INSERT INTO "public"."sys_oper_log" VALUES (406, 'modbus协议读取配置', 1, 'com.labdatahub.business.controller.LabdatahubModbusConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/modbus', '113.251.88.235', 'XX XX', '{"belongSn":"modbus_001","belongType":"0","code":"time","createTime":"2026-01-04 14:49:47.853","delayTime":100,"id":"2007706005967220738","intervalTime":10,"registerRange":"0-7"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-04 14:49:47', 32);
INSERT INTO "public"."sys_oper_log" VALUES (407, 'modbus协议读取配置', 1, 'com.labdatahub.business.controller.LabdatahubModbusConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/modbus', '113.251.88.235', 'XX XX', '{"belongSn":"modbus_1","belongType":"0","code":"time","createTime":"2026-01-05 16:41:31.076","delayTime":100,"id":"2008096509204205569","intervalTime":10,"registerRange":"0-7"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-05 16:41:31', 71);
INSERT INTO "public"."sys_oper_log" VALUES (408, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998232072494788609', '183.137.38.171', 'XX XX', '"1998232072494788609"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-05 22:24:00', 18);
INSERT INTO "public"."sys_oper_log" VALUES (409, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998232072494788609', '183.137.38.171', 'XX XX', '"1998232072494788609"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-05 22:24:02', 7);
INSERT INTO "public"."sys_oper_log" VALUES (410, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998231982996729858', '183.137.38.171', 'XX XX', '"1998231982996729858"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-05 22:24:04', 5);
INSERT INTO "public"."sys_oper_log" VALUES (411, '规则引擎配置', 1, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.add()', 'POST', 1, 'admin', '研发部门', '/business/ruleEngine', '113.248.176.130', 'XX XX', '{"configJson":"{\"lineList\":[],\"nodeList\":[]}","createTime":"2026-01-07 09:46:22","engineName":"测试","id":"2008716811932110850","remark":"123"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-07 09:46:22', 62);
INSERT INTO "public"."sys_oper_log" VALUES (412, '规则引擎配置', 2, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.configEngine()', 'PUT', 1, 'admin', '研发部门', '/business/ruleEngine/configEngine', '113.248.176.130', 'XX XX', '{"configJson":"{\"lineList\":[{\"from\":\"rjq2xvmp79\",\"to\":\"mhr00786ah\"},{\"from\":\"2b8ncdyuyj\",\"to\":\"rjq2xvmp79\"}],\"nodeList\":[{\"id\":\"2b8ncdyuyj\",\"name\":\"设备告警\",\"type\":\"deviceWarn\",\"left\":\"64px\",\"top\":\"146px\",\"ico\":\"el-icon-odometer\",\"state\":\"success\",\"configData\":{\"productScope\":[],\"productList\":[{\"id\":\"1998562969057230849\",\"productSn\":\"HJ_PRODUCT\",\"productName\":\"环境检测仪\",\"linkMethodId\":null,\"linkMethodName\":null,\"componentId\":\"1998562852543660033\",\"componentName\":\"环境检测仪\",\"protocolId\":\"1998562616433704961\",\"protocolName\":\"环境监测仪协议\",\"deviceCount\":1,\"deviceType\":\"0\",\"status\":\"0\",\"createBy\":null,\"createTime\":\"2025-12-10 09:18:38\",\"updateBy\":null,\"updateTime\":null,\"remark\":\"环境检测仪\",\"timeoutSeconds\":60,\"regularCleaning\":\"0\",\"retentionTime\":1,\"retentionUnit\":null,\"customConfig\":null},{\"id\":\"1998568066453602306\",\"productSn\":\"SW_PRODUCT\",\"productName\":\"水位检测仪\",\"linkMethodId\":null,\"linkMethodName\":null,\"componentId\":\"1998564935472775169\",\"componentName\":\"水位检测仪组件\",\"protocolId\":\"1998567823599206401\",\"protocolName\":\"水位检测仪\",\"deviceCount\":1,\"deviceType\":\"1\",\"status\":\"0\",\"createBy\":null,\"createTime\":\"2025-12-10 09:38:53\",\"updateBy\":null,\"updateTime\":null,\"remark\":\"水位检测仪\",\"timeoutSeconds\":60,\"regularCleaning\":\"0\",\"retentionTime\":1,\"retentionUnit\":null,\"customConfig\":null},{\"id\":\"2000752133059129345\",\"productSn\":\"123456789\",\"productName\":\"环境检测2\",\"linkMethodId\":null,\"linkMethodName\":null,\"componentId\":\"1998562852543660033\",\"componentName\":\"环境检测仪\",\"protocolId\":\"1998562616433704961\",\"protocolName\":\"环境监测仪协议\",\"deviceCount\":0,\"deviceType\":\"0\",\"status\":\"0\",\"createBy\":null,\"createTime\":\"2025-12-16 10:17:35\",\"updateBy\":null,\"updateTime\":null,\"remark\":null,\"timeoutSeconds\":60,\"regularCleaning\":\"0\",\"retentionTime\":1,\"retentionUnit\":null,\"customConfig\":n', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-07 09:49:32', 153);
INSERT INTO "public"."sys_oper_log" VALUES (413, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '58.59.121.126', 'XX XX', '{"belongSn":"HJ_01","belongType":"1","createTime":"2025-12-16 16:04:47","functionCode":"test","functionName":"test","id":"2006251209896763393","protocolId":"1998562616433704961"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-07 11:37:44', 10);
INSERT INTO "public"."sys_oper_log" VALUES (414, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998232072494788609', '39.144.218.250', 'XX XX', '"1998232072494788609"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-07 22:35:50', 6);
INSERT INTO "public"."sys_oper_log" VALUES (415, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '222.210.8.2', 'XX XX', '{"belongSn":"SW_01","belongType":"1","createTime":"2026-01-08 10:44:16","id":"2009093771048882177","protocolId":"1998567823599206401"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-08 10:44:16', 133);
INSERT INTO "public"."sys_oper_log" VALUES (416, '设备指令下发', 3, 'com.labdatahub.business.controller.LabdatahubFunctionController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/function/2009093771048882177', '222.210.8.2', 'XX XX', '["2009093771048882177"]', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-08 10:44:20', 41);
INSERT INTO "public"."sys_oper_log" VALUES (417, '代码生成', 8, 'com.labdatahub.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '27.203.249.90', 'XX XX', '{"tables":"labdatahub_modbus_config"}', NULL, 0, NULL, '2026-01-08 15:25:47', 181);
INSERT INTO "public"."sys_oper_log" VALUES (418, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998232072494788609', '52.221.207.186', 'XX XX', '"1998232072494788609"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-08 17:41:24', 42);
INSERT INTO "public"."sys_oper_log" VALUES (419, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998232072494788609', '113.87.187.170', 'XX XX', '"1998232072494788609"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-09 16:07:43', 117);
INSERT INTO "public"."sys_oper_log" VALUES (420, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998232072494788609', '113.87.187.170', 'XX XX', '"1998232072494788609"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-09 16:07:44', 6);
INSERT INTO "public"."sys_oper_log" VALUES (421, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998231982996729858', '113.87.187.170', 'XX XX', '"1998231982996729858"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-09 16:07:45', 5);
INSERT INTO "public"."sys_oper_log" VALUES (422, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '113.87.187.170', 'XX XX', '{"belongSn":"ywy","belongType":"1","createTime":"2025-12-19 14:50:37","functionCode":"deep","functionName":"深度","functionParams":"deep","id":"2001908005021954049","protocolId":"1998562616433704961"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-09 16:10:20', 143);
INSERT INTO "public"."sys_oper_log" VALUES (423, '规则引擎配置', 1, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.add()', 'POST', 1, 'admin', '研发部门', '/business/ruleEngine', '123.232.237.243', 'XX XX', '{"configJson":"{\"lineList\":[],\"nodeList\":[]}","createTime":"2026-01-09 16:41:06","engineName":"IoT测试","id":"2009545956694003714"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-09 16:41:06', 126);
INSERT INTO "public"."sys_oper_log" VALUES (424, '规则引擎配置', 3, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/ruleEngine/2009545956694003714', '123.232.237.243', 'XX XX', '["2009545956694003714"]', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-09 16:42:19', 21);
INSERT INTO "public"."sys_oper_log" VALUES (425, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '113.251.83.169', 'XX XX', '{"actions":[],"belongSn":"SW_01","belongType":"1","conditions":[{"attribute":"water_temprature","operator":"gt","type":"device_property","value":"10"}],"delayTime":0,"enable":true,"id":"2009553162269204481","level":"1","message":"水温告警","name":"水温告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-09 17:09:44', 505);
INSERT INTO "public"."sys_oper_log" VALUES (426, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '113.251.83.169', 'XX XX', '{"actions":[],"belongSn":"SW_01","belongType":"1","conditions":[{"attribute":"","operator":"","type":"device_online","value":""}],"delayTime":0,"enable":true,"id":"2009553229835247617","level":"1","message":"上线提醒","name":"上线提醒","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-09 17:10:00', 46);
INSERT INTO "public"."sys_oper_log" VALUES (427, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '113.251.83.169', 'XX XX', '{"actions":[],"belongSn":"SW_01","belongType":"1","conditions":[{"attribute":"","operator":"","type":"device_offline","value":""}],"delayTime":0,"enable":true,"id":"2009553301218107394","level":"1","message":"离线告警","name":"离线告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-09 17:10:17', 38);
INSERT INTO "public"."sys_oper_log" VALUES (428, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '113.251.83.169', 'XX XX', '{"actions":[],"belongSn":"SW_01","belongType":"1","conditions":[{"attribute":"","operator":"","type":"device_online","value":""}],"delayTime":0,"enable":true,"id":"2009553229835247617","level":"4","message":"上线提醒","name":"上线提醒","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-09 17:10:25', 49);
INSERT INTO "public"."sys_oper_log" VALUES (429, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998232072494788609', '52.221.207.186', 'XX XX', '"1998232072494788609"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-11 19:55:12', 29);
INSERT INTO "public"."sys_oper_log" VALUES (430, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '124.73.220.242', 'XX XX', '{"belongSn":"HJ_PRODUCT","belongType":"0","createTime":"2026-01-13 00:38:53","id":"2010753361025015810","protocolId":"1998562616433704961"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 00:38:54', 128);
INSERT INTO "public"."sys_oper_log" VALUES (431, '设备指令下发', 3, 'com.labdatahub.business.controller.LabdatahubFunctionController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/function/2000839506379943938', '124.73.220.242', 'XX XX', '["2000839506379943938"]', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 00:38:57', 18);
INSERT INTO "public"."sys_oper_log" VALUES (432, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '124.73.220.242', 'XX XX', '{"belongSn":"HJ_PRODUCT","belongType":"0","createTime":"2026-01-13 00:38:54","functionCode":"S1","functionName":"S1","functionParams":"{}","id":"2010753361025015810","protocolId":"1998562616433704961"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 00:39:06', 9);
INSERT INTO "public"."sys_oper_log" VALUES (433, '菜单管理', 1, 'com.labdatahub.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '125.80.219.156', 'XX XX', '{"children":[],"createBy":"admin","icon":"documentation","isCache":"0","isFrame":"0","menuName":"官方文档","menuType":"M","orderNum":4,"params":{},"parentId":0,"path":"http://47.109.145.72:18000/","status":"0","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 10:37:37', 69);
INSERT INTO "public"."sys_oper_log" VALUES (434, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.195.132.70', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/13/0625ece2bbfb4ebfb53eb998c88ac9c3.jpg","code":200}', 0, NULL, '2026-01-13 14:25:57', 219);
INSERT INTO "public"."sys_oper_log" VALUES (435, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/7e276d23-221d-451c-acc9-515d0ac2de2a', '113.195.132.70', 'XX XX', '"7e276d23-221d-451c-acc9-515d0ac2de2a"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:28:43', 3);
INSERT INTO "public"."sys_oper_log" VALUES (436, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/13285fbb-916f-484c-9d88-c9df0a6dcb4e', '113.195.132.70', 'XX XX', '"13285fbb-916f-484c-9d88-c9df0a6dcb4e"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:28:46', 1);
INSERT INTO "public"."sys_oper_log" VALUES (437, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/b2a45336-8123-4a4d-a6f3-e6a32e48b9c0', '113.195.132.70', 'XX XX', '"b2a45336-8123-4a4d-a6f3-e6a32e48b9c0"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:28:52', 1);
INSERT INTO "public"."sys_oper_log" VALUES (438, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/c9333bd1-3653-4e6a-967e-51b983267e15', '113.195.132.70', 'XX XX', '"c9333bd1-3653-4e6a-967e-51b983267e15"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:28:55', 1);
INSERT INTO "public"."sys_oper_log" VALUES (439, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/d648f5be-d35b-42af-bd16-d320f3dee865', '113.195.132.70', 'XX XX', '"d648f5be-d35b-42af-bd16-d320f3dee865"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:28:57', 1);
INSERT INTO "public"."sys_oper_log" VALUES (440, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/17c48312-9575-4aa9-a773-768f3e1143ba', '113.195.132.70', 'XX XX', '"17c48312-9575-4aa9-a773-768f3e1143ba"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:29:00', 1);
INSERT INTO "public"."sys_oper_log" VALUES (441, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/03b40d9b-6190-40b0-b782-f538e34faaf3', '113.195.132.70', 'XX XX', '"03b40d9b-6190-40b0-b782-f538e34faaf3"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:29:05', 2);
INSERT INTO "public"."sys_oper_log" VALUES (442, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/80f625f2-d743-4151-9798-cdac757caf31', '113.195.132.70', 'XX XX', '"80f625f2-d743-4151-9798-cdac757caf31"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:29:07', 1);
INSERT INTO "public"."sys_oper_log" VALUES (443, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/39a660ca-7b55-439f-806b-aa3d9f3303e6', '113.195.132.70', 'XX XX', '"39a660ca-7b55-439f-806b-aa3d9f3303e6"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:29:09', 1);
INSERT INTO "public"."sys_oper_log" VALUES (444, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/e1daa522-4363-4382-8017-9309d3165b9d', '113.195.132.70', 'XX XX', '"e1daa522-4363-4382-8017-9309d3165b9d"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:29:11', 0);
INSERT INTO "public"."sys_oper_log" VALUES (445, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/d5f7c764-3adf-4e38-8818-faee204d8e3c', '113.195.132.70', 'XX XX', '"d5f7c764-3adf-4e38-8818-faee204d8e3c"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:29:13', 1);
INSERT INTO "public"."sys_oper_log" VALUES (446, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/d679a4b9-3c16-4e26-b39c-b28b82a72c4f', '113.195.132.70', 'XX XX', '"d679a4b9-3c16-4e26-b39c-b28b82a72c4f"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:29:15', 1);
INSERT INTO "public"."sys_oper_log" VALUES (447, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/1d40790b-c066-4d99-a376-beeb8dfb1511', '113.195.132.70', 'XX XX', '"1d40790b-c066-4d99-a376-beeb8dfb1511"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:29:17', 1);
INSERT INTO "public"."sys_oper_log" VALUES (448, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/a748e056-e571-4ba2-a49e-9bd600584b0f', '113.195.132.70', 'XX XX', '"a748e056-e571-4ba2-a49e-9bd600584b0f"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:29:39', 9);
INSERT INTO "public"."sys_oper_log" VALUES (449, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/b9d909cf-888d-4797-93d1-e4bebde06153', '113.195.132.70', 'XX XX', '"b9d909cf-888d-4797-93d1-e4bebde06153"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:29:40', 1);
INSERT INTO "public"."sys_oper_log" VALUES (450, '在线用户', 7, 'com.labdatahub.web.controller.monitor.SysUserOnlineController.forceLogout()', 'DELETE', 1, 'admin', '研发部门', '/monitor/online/30e55eb9-4073-490c-b9d9-344caaa6e190', '113.195.132.70', 'XX XX', '"30e55eb9-4073-490c-b9d9-344caaa6e190"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 14:29:42', 1);
INSERT INTO "public"."sys_oper_log" VALUES (451, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '27.200.127.148', 'XX XX', '{"belongSn":"mqtt_001","belongType":"1","createTime":"2025-10-30 21:21:35","functionCode":"TEST","functionName":"测试指令","functionParams":"123213","id":"1983887080943546370","protocolId":"1983885892432982018"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-13 16:55:44', 37);
INSERT INTO "public"."sys_oper_log" VALUES (452, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '125.80.203.97', 'XX XX', '{"children":[],"createTime":"2025-09-18 14:04:33","icon":"netComponent","isCache":"0","isFrame":"1","menuId":2037,"menuName":"网络组件","menuType":"M","orderNum":1,"params":{},"parentId":0,"path":"/componentManage","perms":"","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-14 09:20:58', 839);
INSERT INTO "public"."sys_oper_log" VALUES (453, '定时引擎配置', 1, 'com.labdatahub.business.controller.LabdatahubScheduledTaskController.add()', 'POST', 1, 'admin', '研发部门', '/business/scheduledEngine', '59.61.129.0', 'XX XX', '{"createTime":"2026-01-14 09:21:01","id":"2011247147991801857","name":"111","remark":"22"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-14 09:21:01', 49);
INSERT INTO "public"."sys_oper_log" VALUES (454, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '125.80.203.97', 'XX XX', '{"children":[],"createTime":"2025-11-13 10:31:11","icon":"tree","isCache":"0","isFrame":"1","menuId":2041,"menuName":"规则引擎","menuType":"M","orderNum":2,"params":{},"parentId":0,"path":"engine","perms":"","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-14 09:21:11', 25);
INSERT INTO "public"."sys_oper_log" VALUES (455, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '125.80.203.97', 'XX XX', '{"children":[],"createTime":"2025-09-15 11:09:57","icon":"system","isCache":"0","isFrame":"1","menuId":1,"menuName":"系统管理","menuType":"M","orderNum":3,"params":{},"parentId":0,"path":"system","perms":"","query":"","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-14 09:21:14', 11);
INSERT INTO "public"."sys_oper_log" VALUES (456, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '125.80.203.97', 'XX XX', '{"children":[],"createTime":"2025-09-15 11:09:57","icon":"tool","isCache":"0","isFrame":"1","menuId":3,"menuName":"系统工具","menuType":"M","orderNum":4,"params":{},"parentId":0,"path":"tool","perms":"","query":"","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-14 09:21:21', 14);
INSERT INTO "public"."sys_oper_log" VALUES (457, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '125.80.203.97', 'XX XX', '{"children":[],"createTime":"2026-01-13 10:37:37","icon":"documentation","isCache":"0","isFrame":"0","menuId":2047,"menuName":"官方文档","menuType":"M","orderNum":5,"params":{},"parentId":0,"path":"http://47.109.145.72:18000/","perms":"","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-14 09:21:25', 12);
INSERT INTO "public"."sys_oper_log" VALUES (458, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1998232072494788609', '117.62.161.179', 'XX XX', '"1998232072494788609"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-14 15:18:33', 23);
INSERT INTO "public"."sys_oper_log" VALUES (459, '参数管理', 2, 'com.labdatahub.web.controller.system.SysConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/config', '125.80.203.97', 'XX XX', '{"configId":4,"configKey":"sys.account.captchaEnabled","configName":"账号自助-验证码开关","configType":"Y","configValue":"true","createBy":"admin","createTime":"2025-09-15 11:09:58","params":{},"remark":"是否开启验证码功能（true开启，false关闭）","updateBy":"admin","updateTime":"2025-09-18 13:40:46"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-15 10:18:44', 84);
INSERT INTO "public"."sys_oper_log" VALUES (460, '用户管理', 1, 'com.labdatahub.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '研发部门', '/system/user', '171.11.16.105', 'XX XX', '{"admin":false,"createBy":"admin","deptId":101,"nickName":"患者","params":{},"postIds":[],"roleIds":[],"status":"0","userId":100,"userName":"张三"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-15 17:38:06', 295);
INSERT INTO "public"."sys_oper_log" VALUES (461, '用户管理', 4, 'com.labdatahub.web.controller.system.SysUserController.insertAuthRole()', 'PUT', 1, 'admin', '研发部门', '/system/user/authRole', '171.11.16.105', 'XX XX', '{"roleIds":"2","userId":"100"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-15 17:38:27', 23);
INSERT INTO "public"."sys_oper_log" VALUES (462, '角色管理', 1, 'com.labdatahub.web.controller.system.SysRoleController.add()', 'POST', 1, 'admin', '研发部门', '/system/role', '171.11.16.105', 'XX XX', '{"admin":false,"createBy":"admin","deptCheckStrictly":true,"deptIds":[],"flag":false,"menuCheckStrictly":true,"menuIds":[2036,2012,2013,2014,2015,2016,2017,2006,2007,2008,2009,2010,2011,2045],"params":{},"roleId":100,"roleKey":"1","roleName":"患者","roleSort":0,"status":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-15 17:38:57', 293);
INSERT INTO "public"."sys_oper_log" VALUES (463, '用户管理', 4, 'com.labdatahub.web.controller.system.SysUserController.insertAuthRole()', 'PUT', 1, 'admin', '研发部门', '/system/user/authRole', '171.11.16.105', 'XX XX', '{"roleIds":"100","userId":"100"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-15 17:39:07', 19);
INSERT INTO "public"."sys_oper_log" VALUES (464, '用户管理', 2, 'com.labdatahub.web.controller.system.SysUserController.resetPwd()', 'PUT', 1, 'admin', '研发部门', '/system/user/resetPwd', '171.11.16.105', 'XX XX', '{"admin":false,"params":{},"updateBy":"admin","userId":100}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-15 17:45:56', 411);
INSERT INTO "public"."sys_oper_log" VALUES (465, '用户管理', 3, 'com.labdatahub.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/user/100', '171.11.16.105', 'XX XX', '[100]', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-15 18:48:09', 346);
INSERT INTO "public"."sys_oper_log" VALUES (466, '设备联动告警', 1, 'com.labdatahub.business.controller.LabdatahubWarnLinkageController.add()', 'POST', 1, 'admin', '研发部门', '/business/linkage', '117.64.80.99', 'XX XX', '{"createTime":"2026-01-16 15:48:41","id":"2012069481459847169","name":"AS"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-16 15:48:41', 67);
INSERT INTO "public"."sys_oper_log" VALUES (467, '代码生成', 8, 'com.labdatahub.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '113.248.220.251', 'XX XX', '{"tables":"labdatahub_rule_engine,labdatahub_scheduled_task,labdatahub_function"}', NULL, 0, NULL, '2026-01-16 16:30:26', 501);
INSERT INTO "public"."sys_oper_log" VALUES (468, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '120.227.232.121', 'XX XX', '{"belongSn":"SW_01","belongType":"1","createTime":"2025-12-10 09:29:29","functionCode":"water_out","functionName":"放水","functionParams":"0.8","id":"2001818290969128961","protocolId":"1998564778664525825"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-16 18:25:18', 27);
INSERT INTO "public"."sys_oper_log" VALUES (469, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '36.151.64.228', 'XX XX', '{"actions":[{"functionCode":"S1","functionParams":"111"}],"belongSn":"HJ_PRODUCT","belongType":"0","conditions":[{"attribute":"","operator":"","value":""}],"delayTime":0,"enable":true,"id":"2012139780335308801","level":"1","message":"1111","name":"111","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-16 20:28:02', 303);
INSERT INTO "public"."sys_oper_log" VALUES (470, '菜单管理', 1, 'com.labdatahub.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '36.151.64.228', 'XX XX', '{"children":[],"createBy":"admin","isCache":"0","isFrame":"1","menuName":"大屏显示","menuType":"M","orderNum":1,"params":{},"parentId":0,"path":"1","status":"0","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-16 20:36:49', 350);
INSERT INTO "public"."sys_oper_log" VALUES (471, '部门管理', 2, 'com.labdatahub.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/dept', '58.210.215.228', 'XX XX', '{"ancestors":"0","children":[],"deptId":100,"deptName":"科技","email":"ry@qq.com","leader":"若依","orderNum":0,"params":{},"parentId":0,"phone":"15888888888","status":"0","updateBy":"admin"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-16 20:40:53', 66);
INSERT INTO "public"."sys_oper_log" VALUES (472, '菜单管理', 1, 'com.labdatahub.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '58.210.215.228', 'XX XX', '{"children":[],"createBy":"admin","isCache":"0","isFrame":"1","menuName":"视频监控","menuType":"M","orderNum":2,"params":{},"parentId":0,"path":"1","status":"0","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-16 20:46:17', 47);
INSERT INTO "public"."sys_oper_log" VALUES (473, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '36.151.64.228', 'XX XX', '{"children":[],"createTime":"2026-01-16 20:36:49","icon":"#","isCache":"0","isFrame":"1","menuId":2048,"menuName":"大屏显示","menuType":"M","orderNum":6,"params":{},"parentId":0,"path":"1","perms":"","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-16 20:48:30', 127);
INSERT INTO "public"."sys_oper_log" VALUES (474, '菜单管理', 2, 'com.labdatahub.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '36.151.64.228', 'XX XX', '{"children":[],"createTime":"2026-01-16 20:46:17","icon":"#","isCache":"0","isFrame":"1","menuId":2049,"menuName":"视频监控","menuType":"M","orderNum":7,"params":{},"parentId":0,"path":"1","perms":"","routeName":"","status":"0","updateBy":"admin","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-16 20:48:37', 12);
INSERT INTO "public"."sys_oper_log" VALUES (475, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '120.227.232.121', 'XX XX', '{"belongSn":"mqttx_cb9df9cf","belongType":"1","createTime":"2026-01-16 21:50:39","functionCode":"test","functionName":"111","functionParams":"233223","id":"2012160572049694721","protocolId":"2009562099471196161"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-16 21:50:39', 27);
INSERT INTO "public"."sys_oper_log" VALUES (476, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1996417648381407233', '58.35.78.101', 'XX XX', '"1996417648381407233"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-16 21:58:35', 6);
INSERT INTO "public"."sys_oper_log" VALUES (477, '部门管理', 2, 'com.labdatahub.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/dept', '171.11.16.105', 'XX XX', '{"ancestors":"0","children":[],"deptId":100,"deptName":"福建省","email":"ry@qq.com","leader":"若依","orderNum":0,"params":{},"parentId":0,"phone":"15888888888","status":"0","updateBy":"admin"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-16 23:21:43', 35);
INSERT INTO "public"."sys_oper_log" VALUES (478, '部门管理', 2, 'com.labdatahub.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/dept', '171.11.16.105', 'XX XX', '{"ancestors":"0","children":[],"deptId":100,"deptName":"科技","email":"ry@qq.com","leader":"若依","orderNum":0,"params":{},"parentId":0,"phone":"15888888888","status":"0","updateBy":"admin"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-16 23:22:30', 17);
INSERT INTO "public"."sys_oper_log" VALUES (479, '部门管理', 1, 'com.labdatahub.web.controller.system.SysDeptController.add()', 'POST', 1, 'admin', '研发部门', '/system/dept', '171.11.16.105', 'XX XX', '{"ancestors":"0,100","children":[],"createBy":"admin","deptName":"厦门市","leader":"张三","orderNum":3,"params":{},"parentId":100,"phone":"13129405840","status":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-16 23:27:08', 21);
INSERT INTO "public"."sys_oper_log" VALUES (480, '部门管理', 1, 'com.labdatahub.web.controller.system.SysDeptController.add()', 'POST', 1, 'admin', '研发部门', '/system/dept', '171.11.16.105', 'XX XX', '{"ancestors":"0,100,200","children":[],"createBy":"admin","deptName":"翔安医院","orderNum":1,"params":{},"parentId":200,"status":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-16 23:27:28', 13);
INSERT INTO "public"."sys_oper_log" VALUES (481, '部门管理', 1, 'com.labdatahub.web.controller.system.SysDeptController.add()', 'POST', 1, 'admin', '研发部门', '/system/dept', '171.11.16.105', 'XX XX', '{"ancestors":"0,100,200","children":[],"createBy":"admin","deptName":"思明医院","orderNum":2,"params":{},"parentId":200,"status":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-16 23:27:41', 19);
INSERT INTO "public"."sys_oper_log" VALUES (482, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '36.112.207.224', 'XX XX', '{"actions":[{"functionCode":"","functionParams":""}],"belongSn":"SWJC002","belongType":"1","conditions":[{"attribute":"water_temprature","operator":"gt","value":"18"}],"delayTime":60,"enable":true,"id":"2012428150663913474","level":"3","message":"Temperature alarm, it''s too hot, please take actions","name":"温度告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-17 15:33:54', 38);
INSERT INTO "public"."sys_oper_log" VALUES (483, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '36.112.207.224', 'XX XX', '{"actions":[{"functionCode":"water_out","functionParams":"10"}],"belongSn":"SWJC002","belongType":"1","conditions":[{"attribute":"water_temprature","operator":"gt","value":"18"}],"delayTime":60,"enable":true,"id":"2012428150663913474","level":"3","message":"Temperature alarm, it''s too hot, please take actions","name":"温度告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-17 15:35:48', 45);
INSERT INTO "public"."sys_oper_log" VALUES (484, '参数管理', 1, 'com.labdatahub.web.controller.system.SysConfigController.add()', 'POST', 1, 'admin', '研发部门', '/system/config', '106.92.128.79', 'XX XX', '{"configKey":"device.register.switch","configName":"设备自注册-开关","configType":"Y","configValue":"true","createBy":"admin","params":{}}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-18 01:08:27', 244);
INSERT INTO "public"."sys_oper_log" VALUES (485, '参数管理', 2, 'com.labdatahub.web.controller.system.SysConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/config', '106.92.128.79', 'XX XX', '{"configId":100,"configKey":"device.register.switch","configName":"设备自注册-开关","configType":"Y","configValue":"true","createBy":"admin","createTime":"2026-01-18 01:08:27","params":{},"remark":"用于全局开启/关闭设备自注册","updateBy":"admin"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-18 01:08:54', 45);
INSERT INTO "public"."sys_oper_log" VALUES (486, '参数管理', 2, 'com.labdatahub.web.controller.system.SysConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/config', '106.92.128.79', 'XX XX', '{"configId":100,"configKey":"device.register.switch","configName":"设备自注册-开关","configType":"Y","configValue":"true","createBy":"admin","createTime":"2026-01-18 01:08:27","params":{},"remark":"用于全局开启/关闭设备自注册，修改之后十秒钟内生效","updateBy":"admin","updateTime":"2026-01-18 01:08:53"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-18 01:11:09', 22);
INSERT INTO "public"."sys_oper_log" VALUES (487, '菜单管理', 1, 'com.labdatahub.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '106.92.128.79', 'XX XX', '{"children":[],"component":"business/deviceGroup/index","createBy":"admin","icon":"list","isCache":"1","isFrame":"1","menuName":"设备分组","menuType":"C","orderNum":4,"params":{},"parentId":2036,"path":"deviceGroup","routeName":"DeviceGroup","status":"0","visible":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-18 18:58:13', 160);
INSERT INTO "public"."sys_oper_log" VALUES (488, '设备分组', 1, 'com.labdatahub.business.controller.LabdatahubDeviceGroupController.add()', 'POST', 1, 'admin', '研发部门', '/business/deviceGroup', '106.92.128.79', 'XX XX', '{"groupCode":"MOREN_PRODUCT","groupName":"默认产品组","id":"2012842057312346113","type":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-18 18:58:37', 32);
INSERT INTO "public"."sys_oper_log" VALUES (489, '设备分组', 1, 'com.labdatahub.business.controller.LabdatahubDeviceGroupController.add()', 'POST', 1, 'admin', '研发部门', '/business/deviceGroup', '106.92.128.79', 'XX XX', '{"groupCode":"MOREN_DEVICE","groupName":"默认设备组","id":"2012842112303865858","type":"1"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-18 18:58:51', 123);
INSERT INTO "public"."sys_oper_log" VALUES (490, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '106.92.128.79', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/18/4a7f78a569e54335a157598b4d1ca4d4.jpg","code":200}', 0, NULL, '2026-01-18 19:02:02', 221);
INSERT INTO "public"."sys_oper_log" VALUES (491, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '106.92.128.79', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/18/0bb91e1ef6c14595b46146c4d9e10b0d.jpg","code":200}', 0, NULL, '2026-01-18 19:02:42', 37);
INSERT INTO "public"."sys_oper_log" VALUES (492, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2048', '106.92.128.79', 'XX XX', '2048', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-18 19:03:22', 149);
INSERT INTO "public"."sys_oper_log" VALUES (493, '菜单管理', 3, 'com.labdatahub.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2049', '106.92.128.79', 'XX XX', '2049', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-18 19:03:24', 29);
INSERT INTO "public"."sys_oper_log" VALUES (494, '参数管理', 2, 'com.labdatahub.web.controller.system.SysConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/config', '106.92.128.79', 'XX XX', '{"configId":100,"configKey":"device.register.switch","configName":"设备自注册-开关","configType":"Y","configValue":"false","createBy":"admin","createTime":"2026-01-18 01:08:27","params":{},"remark":"用于全局开启/关闭设备自注册，修改之后十秒钟内生效","updateBy":"admin","updateTime":"2026-01-18 01:11:09"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-18 19:09:25', 46);
INSERT INTO "public"."sys_oper_log" VALUES (495, '参数管理', 2, 'com.labdatahub.web.controller.system.SysConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/config', '120.227.232.121', 'XX XX', '{"configId":100,"configKey":"device.register.switch","configName":"设备自注册-开关","configType":"Y","configValue":"true","createBy":"admin","createTime":"2026-01-18 01:08:27","params":{},"remark":"用于全局开启/关闭设备自注册，修改之后十秒钟内生效","updateBy":"admin","updateTime":"2026-01-18 19:09:25"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-18 19:43:55', 58);
INSERT INTO "public"."sys_oper_log" VALUES (496, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '120.227.232.121', 'XX XX', '{"belongSn":"mqttx_9ba55401","belongType":"1","createTime":"2026-01-18 23:33:37","functionCode":"time_response","functionName":"同步时间","id":"2012911259851268098","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-18 23:33:37', 47);
INSERT INTO "public"."sys_oper_log" VALUES (497, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '120.227.232.121', 'XX XX', '{"belongSn":"mqttx_9ba55401","belongType":"1","createTime":"2026-01-18 23:33:37","functionCode":"time_response","functionName":"同步时间","functionParams":"time_response","id":"2012911259851268098","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-18 23:46:58', 27);
INSERT INTO "public"."sys_oper_log" VALUES (498, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":""}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"contains","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012918341585444866","level":"4","message":"请求时间同步","name":"time_request","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:01:45', 44);
INSERT INTO "public"."sys_oper_log" VALUES (499, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012918341585444866","level":"3","message":"请求时间同步","name":"time_request","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:07:07', 62);
INSERT INTO "public"."sys_oper_log" VALUES (500, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012918341585444866","level":"3","message":"请求时间同步","name":"time_request","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:20:44', 120);
INSERT INTO "public"."sys_oper_log" VALUES (501, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":false,"id":"2012918341585444866","level":"3","message":"请求时间同步","name":"time_request","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:24:30', 29);
INSERT INTO "public"."sys_oper_log" VALUES (502, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012918341585444866","level":"3","message":"请求时间同步","name":"time_request","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:24:31', 20);
INSERT INTO "public"."sys_oper_log" VALUES (503, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":10,"enable":true,"id":"2012918341585444866","level":"3","message":"请求时间同步","name":"time_request","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:24:37', 12);
INSERT INTO "public"."sys_oper_log" VALUES (504, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"1"}],"delayTime":10,"enable":true,"id":"2012918341585444866","level":"3","message":"请求时间同步","name":"time_request","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:26:12', 49);
INSERT INTO "public"."sys_oper_log" VALUES (505, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":10,"enable":true,"id":"2012918341585444866","level":"3","message":"请求时间同步","name":"time_request","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:28:30', 45);
INSERT INTO "public"."sys_oper_log" VALUES (506, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":10,"enable":true,"id":"2012918341585444866","level":"3","message":"请求时间同步","name":"time_request","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:34:07', 35);
INSERT INTO "public"."sys_oper_log" VALUES (507, '告警配置', 3, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/warnConfig/2012918341585444866', '106.92.128.79', 'XX XX', '["2012918341585444866"]', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:36:53', 50);
INSERT INTO "public"."sys_oper_log" VALUES (508, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"123"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012927272110104578","level":"1","message":"告警","name":"时间同步","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:37:15', 436);
INSERT INTO "public"."sys_oper_log" VALUES (509, '告警配置', 3, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/warnConfig/2012927272110104578', '106.92.128.79', 'XX XX', '["2012927272110104578"]', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:39:14', 592);
INSERT INTO "public"."sys_oper_log" VALUES (510, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"213123"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012927851855192065","level":"1","message":"123123","name":"告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:39:32', 14);
INSERT INTO "public"."sys_oper_log" VALUES (511, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"213123"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"timeRequest"}],"delayTime":0,"enable":true,"id":"2012927851855192065","level":"1","message":"123123","name":"告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:48:30', 26);
INSERT INTO "public"."sys_oper_log" VALUES (512, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"213123"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"timeRequest"}],"delayTime":0,"enable":true,"id":"2012927851855192065","level":"1","message":"123123","name":"告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:52:08', 59);
INSERT INTO "public"."sys_oper_log" VALUES (513, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"213123"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time"}],"delayTime":0,"enable":true,"id":"2012927851855192065","level":"1","message":"123123","name":"告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:54:27', 20);
INSERT INTO "public"."sys_oper_log" VALUES (514, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"23123"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time2"}],"delayTime":0,"enable":true,"id":"2012931700854730753","level":"1","message":"123123","name":"告警2","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:54:50', 18);
INSERT INTO "public"."sys_oper_log" VALUES (515, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '106.92.128.79', 'XX XX', '{"belongSn":"ceshi_009","belongType":"1","createTime":"2026-01-19 00:57:17","functionCode":"test","functionName":"test","functionParams":"test","id":"2012932318524715010","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:57:17', 19);
INSERT INTO "public"."sys_oper_log" VALUES (516, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[],"belongSn":"ceshi_009","belongType":"1","conditions":[{"attribute":"windSpeed","operator":"eq","type":"device_property","value":"213"}],"delayTime":0,"enable":true,"id":"2012932363353436161","level":"1","message":"sdafasdf","name":"dfsdaf","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:57:28', 12);
INSERT INTO "public"."sys_oper_log" VALUES (517, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"test","functionParams":"21312"}],"belongSn":"ceshi_009","belongType":"1","conditions":[{"attribute":"windSpeed","operator":"eq","type":"device_property","value":"213"}],"delayTime":0,"enable":true,"id":"2012932363353436161","level":"1","message":"sdafasdf","name":"dfsdaf","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:57:33', 15);
INSERT INTO "public"."sys_oper_log" VALUES (518, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '106.92.128.79', 'XX XX', '{"belongSn":"ceshi_009","belongType":"1","createTime":"2026-01-19 00:57:18","functionCode":"test213","functionName":"test","functionParams":"test","id":"2012932318524715010","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:59:12', 24);
INSERT INTO "public"."sys_oper_log" VALUES (519, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"test213","functionParams":"21312"}],"belongSn":"ceshi_009","belongType":"1","conditions":[{"attribute":"windSpeed","operator":"eq","type":"device_property","value":"213"}],"delayTime":0,"enable":true,"id":"2012932363353436161","level":"1","message":"sdafasdf","name":"dfsdaf","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 00:59:24', 15);
INSERT INTO "public"."sys_oper_log" VALUES (520, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"213123"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012927851855192065","level":"1","message":"123123","name":"告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:11:20', 16);
INSERT INTO "public"."sys_oper_log" VALUES (521, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"time_request","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012936228693979137","level":"3","message":"工a","name":"时间同步","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:12:50', 17);
INSERT INTO "public"."sys_oper_log" VALUES (522, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '106.92.128.79', 'XX XX', '{"belongSn":"mqttx_00001","belongType":"1","createTime":"2026-01-19 01:13:25","functionCode":"test","functionName":"test","functionParams":"test","id":"2012936378204139521","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:13:25', 15);
INSERT INTO "public"."sys_oper_log" VALUES (523, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[],"belongSn":"mqttx_00001","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012936445447221250","level":"1","message":"erwer","name":"rewrwer","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:13:41', 14);
INSERT INTO "public"."sys_oper_log" VALUES (524, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"test","functionParams":"213123"}],"belongSn":"mqttx_00001","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012936445447221250","level":"1","message":"erwer","name":"rewrwer","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:13:46', 28);
INSERT INTO "public"."sys_oper_log" VALUES (525, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"time_request","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012936228693979137","level":"3","message":"工a","name":"时间同步","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:19:26', 11);
INSERT INTO "public"."sys_oper_log" VALUES (526, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"time_request","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":false,"id":"2012936228693979137","level":"3","message":"工a","name":"时间同步","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:19:49', 38);
INSERT INTO "public"."sys_oper_log" VALUES (527, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"time_request","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012936228693979137","level":"3","message":"工a","name":"时间同步","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:19:50', 19);
INSERT INTO "public"."sys_oper_log" VALUES (528, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"time_request","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012936228693979137","level":"3","message":"工a","name":"时间同步","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:20:36', 303);
INSERT INTO "public"."sys_oper_log" VALUES (529, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '106.92.128.79', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"213"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"time_request2","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012938246275837954","level":"1","message":"213123","name":"2313","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:20:51', 22);
INSERT INTO "public"."sys_oper_log" VALUES (530, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"213123"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":false,"id":"2012927851855192065","level":"1","message":"123123","name":"告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:21:44', 45);
INSERT INTO "public"."sys_oper_log" VALUES (531, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"23123"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time2"}],"delayTime":0,"enable":false,"id":"2012931700854730753","level":"1","message":"123123","name":"告警2","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:21:44', 17);
INSERT INTO "public"."sys_oper_log" VALUES (532, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"213"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"time_request2","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":false,"id":"2012938246275837954","level":"1","message":"213123","name":"2313","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:21:45', 33);
INSERT INTO "public"."sys_oper_log" VALUES (533, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"time_request","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":false,"id":"2012936228693979137","level":"3","message":"工a","name":"时间同步","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:22:51', 103);
INSERT INTO "public"."sys_oper_log" VALUES (534, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"213123"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012927851855192065","level":"1","message":"123123","name":"告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:22:52', 24);
INSERT INTO "public"."sys_oper_log" VALUES (535, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"23123"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time2"}],"delayTime":0,"enable":true,"id":"2012931700854730753","level":"1","message":"123123","name":"告警2","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:22:54', 16);
INSERT INTO "public"."sys_oper_log" VALUES (536, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"23123"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time2"}],"delayTime":0,"enable":false,"id":"2012931700854730753","level":"1","message":"123123","name":"告警2","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:22:59', 34);
INSERT INTO "public"."sys_oper_log" VALUES (537, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"213123"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":false,"id":"2012927851855192065","level":"1","message":"123123","name":"告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:23:30', 17);
INSERT INTO "public"."sys_oper_log" VALUES (538, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":false,"id":"2012936228693979137","level":"1","message":"ssss","name":"时间同步","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:23:45', 300);
INSERT INTO "public"."sys_oper_log" VALUES (539, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012936228693979137","level":"1","message":"ssss","name":"时间同步","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:23:47', 16);
INSERT INTO "public"."sys_oper_log" VALUES (540, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"time_response"}],"belongSn":"mqttx_9ba55401","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2012936228693979137","level":"2","message":"ssss","name":"时间同步","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:24:09', 15);
INSERT INTO "public"."sys_oper_log" VALUES (541, '用户管理', 1, 'com.labdatahub.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '研发部门', '/system/user', '120.227.232.121', 'XX XX', '{"admin":false,"createBy":"admin","nickName":"mqttest","params":{},"postIds":[],"roleIds":[],"status":"0","userId":101,"userName":"123456"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 01:41:40', 150);
INSERT INTO "public"."sys_oper_log" VALUES (542, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/2012448841878577153', '1.192.169.18', 'XX XX', '"2012448841878577153"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 09:45:21', 77);
INSERT INTO "public"."sys_oper_log" VALUES (543, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '1.192.169.18', 'XX XX', '{"actions":[],"belongSn":"SW_01","belongType":"1","conditions":[{"attribute":"water_temprature","operator":"gt","type":"device_property","value":"10"}],"delayTime":0,"enable":false,"id":"2009553162269204481","level":"1","message":"水温告警","name":"水温告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 10:26:59', 81);
INSERT INTO "public"."sys_oper_log" VALUES (544, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '1.192.169.18', 'XX XX', '{"actions":[],"belongSn":"SW_01","belongType":"1","conditions":[{"attribute":"water_temprature","operator":"gt","type":"device_property","value":"10"}],"delayTime":0,"enable":true,"id":"2009553162269204481","level":"1","message":"水温告警","name":"水温告警","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 10:27:02', 586);
INSERT INTO "public"."sys_oper_log" VALUES (545, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '223.155.166.21', 'XX XX', '{"belongSn":"mqttx_9ba55401","belongType":"1","createTime":"2026-01-18 23:33:37","functionCode":"time_response","functionName":"同步时间","functionParams":"{ \"msg_id\": \"550e8400-e29b-41d4-a716-446655440008\", \"timestamp\": time_response, \"device_id\": \"room_101\", \"type\": \"time_responses\", \"data\": { \"server_time\": 1633024800, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false } }","id":"2012911259851268098","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 11:39:39', 792);
INSERT INTO "public"."sys_oper_log" VALUES (546, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '223.155.166.21', 'XX XX', '{"belongSn":"mqttx_9ba55401","belongType":"1","createTime":"2026-01-18 23:33:37","functionCode":"time_response","functionName":"同步时间","functionParams":"{ \"msg_id\": \"550e8400-e29b-41d4-a716-446655440008\", \"timestamp\": time_response, \"device_id\": \"room_101\", \"type\": \"time_responses\", \"data\": { \"server_time\": time_response, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false } }","id":"2012911259851268098","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 11:40:02', 34);
INSERT INTO "public"."sys_oper_log" VALUES (547, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '223.155.166.21', 'XX XX', '{"belongSn":"mqttx_9ba55401","belongType":"1","createTime":"2026-01-18 23:33:37","functionCode":"time_response","functionName":"同步时间","functionParams":" { \"server_time\": time_response, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false } ","id":"2012911259851268098","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 11:43:57', 190);
INSERT INTO "public"."sys_oper_log" VALUES (548, '告警配置', 3, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/warnConfig/2012927851855192065', '223.155.166.21', 'XX XX', '["2012927851855192065"]', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 11:44:49', 41);
INSERT INTO "public"."sys_oper_log" VALUES (549, '告警配置', 3, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/warnConfig/2012931700854730753', '223.155.166.21', 'XX XX', '["2012931700854730753"]', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 11:44:52', 19);
INSERT INTO "public"."sys_oper_log" VALUES (550, '告警配置', 3, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/warnConfig/2012938246275837954', '223.155.166.21', 'XX XX', '["2012938246275837954"]', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 11:44:54', 8);
INSERT INTO "public"."sys_oper_log" VALUES (551, '参数管理', 9, 'com.labdatahub.web.controller.system.SysConfigController.refreshCache()', 'DELETE', 1, 'admin', '研发部门', '/system/config/refreshCache', '107.167.18.105', 'XX XX', '', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 14:11:27', 626);
INSERT INTO "public"."sys_oper_log" VALUES (552, '字典类型', 9, 'com.labdatahub.web.controller.system.SysDictTypeController.refreshCache()', 'DELETE', 1, 'admin', '研发部门', '/system/dict/type/refreshCache', '107.167.18.105', 'XX XX', '', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 14:11:52', 31);
INSERT INTO "public"."sys_oper_log" VALUES (553, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '27.18.225.166', 'XX XX', '{"belongSn":"mqtt_00103","belongType":"1","createTime":"2026-01-19 14:21:15","functionCode":"test","functionName":"test","functionParams":"4.5","id":"2013134641310048258","protocolId":"2009562099471196161"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-19 14:21:15', 17);
INSERT INTO "public"."sys_oper_log" VALUES (554, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '120.227.232.121', 'XX XX', '{"belongSn":"112313","belongType":"0","createTime":"2026-01-20 09:53:39","functionCode":"time_response","functionName":"时间同步","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"room_101\", \"type\": \"time_response\", \"data\": { \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false } }","id":"2013429685250269185","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 09:53:39', 168);
INSERT INTO "public"."sys_oper_log" VALUES (555, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":""}],"belongSn":"112313","belongType":"0","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_response"}],"delayTime":0,"enable":true,"id":"2013429921968398338","level":"2","message":"time","name":"时间","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 09:54:35', 38);
INSERT INTO "public"."sys_oper_log" VALUES (556, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"room_101\", \"type\": \"time_response\", \"data\": { \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false } }"}],"belongSn":"112313","belongType":"0","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_response"}],"delayTime":0,"enable":true,"id":"2013429921968398338","level":"2","message":"time","name":"时间","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 09:54:53', 49);
INSERT INTO "public"."sys_oper_log" VALUES (568, '设备指令下发', 3, 'com.labdatahub.business.controller.LabdatahubFunctionController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/function/2013465864054087682', '120.227.232.121', 'XX XX', '["2013465864054087682"]', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 12:28:56', 106);
INSERT INTO "public"."sys_oper_log" VALUES (557, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"room_101\", \"type\": \"time_response\", \"data\": { \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false } }"}],"belongSn":"mqttx_12df38a5","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_response"}],"delayTime":0,"enable":true,"id":"2013430009570631681","level":"2","message":"time","name":"时间","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 09:55:24', 28);
INSERT INTO "public"."sys_oper_log" VALUES (558, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '120.227.232.121', 'XX XX', '{"belongSn":"mqttx_12df38a5","belongType":"1","createTime":"2026-01-20 09:53:39","functionCode":"time_response","functionName":"时间同步","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"time_response\", \"data\": { \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false } }","id":"2013429747476963331","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 10:21:34', 26);
INSERT INTO "public"."sys_oper_log" VALUES (559, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"time_response\", \"data\": { \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false } }"}],"belongSn":"mqttx_12df38a5","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_response"}],"delayTime":0,"enable":true,"id":"2013430009570631681","level":"2","message":"time","name":"时间","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 10:30:32', 42);
INSERT INTO "public"."sys_oper_log" VALUES (560, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '120.227.232.121', 'XX XX', '{"belongSn":"112313","belongType":"0","createTime":"2026-01-20 09:53:39","functionCode":"time_response","functionName":"时间同步","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"time_response\", \"data\": { \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false } }","id":"2013429685250269185","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 10:42:18', 75);
INSERT INTO "public"."sys_oper_log" VALUES (561, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"time_response\", \"data\": { \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false } }"}],"belongSn":"112313","belongType":"0","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_response"}],"delayTime":0,"enable":true,"id":"2013429921968398338","level":"2","message":"time","name":"时间","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 10:42:31', 14);
INSERT INTO "public"."sys_oper_log" VALUES (562, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"time_response\", \"data\": { \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false } }"}],"belongSn":"mqttx_4c603c26","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2013441997721935874","level":"2","message":"time","name":"时间","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 10:45:50', 23);
INSERT INTO "public"."sys_oper_log" VALUES (563, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"time_response\", \"data\": { \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false } }"}],"belongSn":"112313","belongType":"0","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2013429921968398338","level":"2","message":"time","name":"时间","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 10:46:03', 13);
INSERT INTO "public"."sys_oper_log" VALUES (564, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/warnRecord/deal/2013443247028936706', '120.227.232.121', 'XX XX', '"2013443247028936706"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 10:49:32', 199);
INSERT INTO "public"."sys_oper_log" VALUES (565, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '113.248.96.236', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"time_response\", \"data\": { \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false } }"}],"belongSn":"mqttx_4c603c26","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2013442979520421889","level":"2","message":"time","name":"时间","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 10:56:27', 8);
INSERT INTO "public"."sys_oper_log" VALUES (566, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '120.227.232.121', 'XX XX', '{"belongSn":"112313","belongType":"0","createTime":"2026-01-20 09:53:39","functionCode":"time_response","functionName":"时间同步","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"time_response\", \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false  }","id":"2013429685250269185","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 12:14:32', 47);
INSERT INTO "public"."sys_oper_log" VALUES (567, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"time_response\", \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false }"}],"belongSn":"112313","belongType":"0","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2013429921968398338","level":"2","message":"time","name":"时间","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 12:15:00', 39);
INSERT INTO "public"."sys_oper_log" VALUES (569, '告警配置', 3, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/warnConfig/2013465877383585793', '120.227.232.121', 'XX XX', '["2013465877383585793"]', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 12:29:00', 21);
INSERT INTO "public"."sys_oper_log" VALUES (570, '设备指令下发', 3, 'com.labdatahub.business.controller.LabdatahubFunctionController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/function/2013429685250269185', '120.227.232.121', 'XX XX', '["2013429685250269185"]', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 12:30:21', 17);
INSERT INTO "public"."sys_oper_log" VALUES (571, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '120.227.232.121', 'XX XX', '{"belongSn":"112313","belongType":"0","createTime":"2026-01-20 12:30:46","functionCode":"time_response","functionName":"时间同步","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"time_response\", \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false  }","id":"2013469226732462082","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 12:30:46', 21);
INSERT INTO "public"."sys_oper_log" VALUES (572, '告警配置', 3, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.remove()', 'DELETE', 1, 'admin', '研发部门', '/business/warnConfig/2013429921968398338', '120.227.232.121', 'XX XX', '["2013429921968398338"]', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 12:30:54', 11);
INSERT INTO "public"."sys_oper_log" VALUES (573, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":""}],"belongSn":"112313","belongType":"0","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2013469399978188802","level":"2","message":"时间同步","name":"时间同步s","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 12:31:28', 21);
INSERT INTO "public"."sys_oper_log" VALUES (574, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"time_response\", \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false }"}],"belongSn":"112313","belongType":"0","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2013469399978188802","level":"2","message":"时间同步","name":"时间同步s","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 12:31:35', 30);
INSERT INTO "public"."sys_oper_log" VALUES (575, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '120.227.232.121', 'XX XX', '{"belongSn":"20260120","belongType":"0","createTime":"2026-01-20 13:14:45","functionCode":"time_response","functionName":"时间同步","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"time_response\", \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false }","id":"2013480294340362241","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 13:14:45', 36);
INSERT INTO "public"."sys_oper_log" VALUES (576, '告警配置', 1, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.add()', 'POST', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"time_response\", \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false }"}],"belongSn":"20260120","belongType":"0","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2013480513740210177","level":"2","message":"time","name":"时间s","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 13:15:37', 17);
INSERT INTO "public"."sys_oper_log" VALUES (577, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"time_response\", \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false }"}],"belongSn":"mqttx_7f4f91f2","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":true,"id":"2013480673924874242","level":"2","message":"time","name":"时间s","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 13:17:15', 11);
INSERT INTO "public"."sys_oper_log" VALUES (578, '告警配置', 2, 'com.labdatahub.business.controller.LabdatahubWarnConfigController.toggleRuleStatus()', 'PUT', 1, 'admin', '研发部门', '/business/warnConfig/toggleRuleStatus', '120.227.232.121', 'XX XX', '{"actions":[{"functionCode":"time_response","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"time_response\", \"server_time\": time_auto, \"timezone\": \"Asia/Shanghai\", \"dst_enabled\": false }"}],"belongSn":"mqttx_a59aaf4c","belongType":"1","conditions":[{"attribute":"type","operator":"eq","type":"device_property","value":"time_request"}],"delayTime":0,"enable":false,"id":"2013481092453498882","level":"2","message":"time","name":"时间s","relation":"and"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 15:18:33', 33);
INSERT INTO "public"."sys_oper_log" VALUES (579, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '120.227.232.121', 'XX XX', '{"belongSn":"20260120","belongType":"0","createTime":"2026-01-20 15:55:03","functionCode":"power_off","functionName":"断电","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"power_off\"}","id":"2013520636766846977","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 15:55:03', 195);
INSERT INTO "public"."sys_oper_log" VALUES (580, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '120.227.232.121', 'XX XX', '{"belongSn":"20260120","belongType":"0","createTime":"2026-01-20 15:55:04","functionCode":"power_off","functionName":"断电","functionParams":"{ \"msg_id\": \"uuid\", \"timestamp\": time_auto, \"device_id\": \"deviceid\", \"type\": \"powerstatus\"}","id":"2013520636766846977","protocolId":"2012855145608617985"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-20 16:02:39', 563);
INSERT INTO "public"."sys_oper_log" VALUES (581, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/4094e8690d6341ef9df8990d8b1f40a6.jpg","code":200}', 0, NULL, '2026-01-21 09:32:38', 139);
INSERT INTO "public"."sys_oper_log" VALUES (582, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/4b652a9503d149bcab2ff1698cd1386a.jpg","code":200}', 0, NULL, '2026-01-21 09:36:50', 12);
INSERT INTO "public"."sys_oper_log" VALUES (583, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/0935f19212364027ab21bec391469603.jpg","code":200}', 0, NULL, '2026-01-21 09:38:31', 22);
INSERT INTO "public"."sys_oper_log" VALUES (584, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/b426de82f67d4b309109b7881dfeea82.jpg","code":200}', 0, NULL, '2026-01-21 09:48:24', 118);
INSERT INTO "public"."sys_oper_log" VALUES (585, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/2b3999c5a0eb4b51b48c298e2ad195f3.jpg","code":200}', 0, NULL, '2026-01-21 09:51:30', 28);
INSERT INTO "public"."sys_oper_log" VALUES (586, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/a26459ec8fc8415babd5cb00c87221d7.jpg","code":200}', 0, NULL, '2026-01-21 09:54:09', 229);
INSERT INTO "public"."sys_oper_log" VALUES (587, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/54a57630cd504347b87c14f6add77125.jpg","code":200}', 0, NULL, '2026-01-21 09:54:41', 13);
INSERT INTO "public"."sys_oper_log" VALUES (588, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/d87273bb63d044669761229b1637cddc.jpg","code":200}', 0, NULL, '2026-01-21 09:55:35', 36);
INSERT INTO "public"."sys_oper_log" VALUES (589, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/4cd91fc957794565ac2c1226c4bd816c.jpg","code":200}', 0, NULL, '2026-01-21 09:59:25', 17);
INSERT INTO "public"."sys_oper_log" VALUES (590, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/cb0b3236b845457dbb4ccae25a4cbd37.jpg","code":200}', 0, NULL, '2026-01-21 10:01:13', 30);
INSERT INTO "public"."sys_oper_log" VALUES (591, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/7fd7a65a9c3a45899daf99db9d4cb3d5.jpg","code":200}', 0, NULL, '2026-01-21 10:06:37', 45);
INSERT INTO "public"."sys_oper_log" VALUES (592, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/f68cbed4ea0d4d63bd610e0dd86b743d.jpg","code":200}', 0, NULL, '2026-01-21 10:08:58', 197);
INSERT INTO "public"."sys_oper_log" VALUES (593, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/d3ce13bbc02e4430aa08658cc877b7c8.jpg","code":200}', 0, NULL, '2026-01-21 10:11:51', 253);
INSERT INTO "public"."sys_oper_log" VALUES (594, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/0cd944ee488f43fc8bcf9f33273c06b8.png","code":200}', 0, NULL, '2026-01-21 10:12:11', 17);
INSERT INTO "public"."sys_oper_log" VALUES (595, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/e4fff13509e54a6d8ef5f9bc0cb52ef0.jpg","code":200}', 0, NULL, '2026-01-21 10:15:09', 291);
INSERT INTO "public"."sys_oper_log" VALUES (596, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/8610806b3a564a7aa405d31edfde5256.jpg","code":200}', 0, NULL, '2026-01-21 10:18:25', 264);
INSERT INTO "public"."sys_oper_log" VALUES (597, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/6f95d98549284eec87a3d610ec234425.jpg","code":200}', 0, NULL, '2026-01-21 10:54:53', 248);
INSERT INTO "public"."sys_oper_log" VALUES (598, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/dac4991d072248d883908f674a8a0dfa.jpg","code":200}', 0, NULL, '2026-01-21 10:55:23', 62);
INSERT INTO "public"."sys_oper_log" VALUES (599, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/ca590a52742548a1a31f5220437c3eab.jpg","code":200}', 0, NULL, '2026-01-21 10:56:21', 32);
INSERT INTO "public"."sys_oper_log" VALUES (600, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/598e49a6904941b2888beb01532ac2af.jpg","code":200}', 0, NULL, '2026-01-21 11:10:10', 257);
INSERT INTO "public"."sys_oper_log" VALUES (601, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/e50edf507e23406ab6f9b85e0e9e217a.jpg","code":200}', 0, NULL, '2026-01-21 11:16:59', 37);
INSERT INTO "public"."sys_oper_log" VALUES (602, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/91e840c9e2ec4d8d9639fd1747d0d26f.jpg","code":200}', 0, NULL, '2026-01-21 11:21:15', 19);
INSERT INTO "public"."sys_oper_log" VALUES (603, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/5c5e694d64704d3f9f0b204b17895216.jpg","code":200}', 0, NULL, '2026-01-21 11:21:51', 20);
INSERT INTO "public"."sys_oper_log" VALUES (604, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/5fbf96eb3f7c4cd2812d0d04f9e75efc.png","code":200}', 0, NULL, '2026-01-21 11:25:25', 12);
INSERT INTO "public"."sys_oper_log" VALUES (605, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/72d0ae1523cc4e7ead7ce1e79dd6cd86.jpg","code":200}', 0, NULL, '2026-01-21 11:26:03', 17);
INSERT INTO "public"."sys_oper_log" VALUES (606, '用户头像', 2, 'com.labdatahub.web.controller.system.SysProfileController.avatar()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/avatar', '113.248.96.236', 'XX XX', '', '{"msg":"操作成功","imgUrl":"/profile/avatar/2026/01/21/5ebbe04a1a52488e8d881a8538ce704f.jpg","code":200}', 0, NULL, '2026-01-21 11:27:03', 15);
INSERT INTO "public"."sys_oper_log" VALUES (607, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '124.128.9.243', 'XX XX', '{"belongSn":"温湿度传感器","belongType":"0","createTime":"2026-01-21 13:46:25","id":"2013850651679891458","protocolId":"2009562099471196161"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-21 13:46:25', 40);
INSERT INTO "public"."sys_oper_log" VALUES (608, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1996414546987200513', '61.178.29.188', 'XX XX', '"1996414546987200513"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-21 15:54:56', 40);
INSERT INTO "public"."sys_oper_log" VALUES (609, '参数管理', 1, 'com.labdatahub.web.controller.system.SysConfigController.add()', 'POST', 1, 'admin', '研发部门', '/system/config', '106.92.128.79', 'XX XX', '{"configKey":"device.log.batch","configName":"优化-设备日志批量保存","configType":"Y","configValue":"true,1000,10000","createBy":"admin","params":{},"remark":"第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备日志并发高并且集中时缓解数据库压力，设备日志会先缓存，然后以每秒取固定数量数据批量插入数据库。这会导致插入数据可能会有一两秒的延迟，但不会影响实时数据以及告警等功能的实时性。酌情开启。"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-21 22:54:31', 19);
INSERT INTO "public"."sys_oper_log" VALUES (610, '参数管理', 2, 'com.labdatahub.web.controller.system.SysConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/config', '106.92.128.79', 'XX XX', '{"configId":101,"configKey":"device.log.batch","configName":"优化-设备日志批量保存","configType":"Y","configValue":"true,1000,10000","createBy":"admin","createTime":"2026-01-21 22:54:31","params":{},"remark":"第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备日志并发高并且集中时缓解数据库压力，设备日志会先缓存，然后以每秒取固定数量数据批量插入数据库。这会导致插入数据可能会有一两秒的延迟，但不会影响实时数据以及告警等功能的实时性。酌情开启。","updateBy":"admin"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-22 00:14:32', 51);
INSERT INTO "public"."sys_oper_log" VALUES (611, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '221.197.232.185', 'XX XX', '{"belongSn":"SW_01","belongType":"1","createTime":"2025-12-10 09:29:29","functionCode":"water_out","functionName":"放水","functionParams":"0.8","id":"2012428471637221378","protocolId":"1998564778664525825"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-22 11:15:48', 49);
INSERT INTO "public"."sys_oper_log" VALUES (612, '规则引擎配置', 2, 'com.labdatahub.business.controller.LabdatahubRuleEngineController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/ruleEngine', '60.184.32.170', 'XX XX', '{"configJson":"{\"lineList\":[{\"from\":\"yf6v8ryif\",\"to\":\"ejfac7z8t\"},{\"from\":\"ejfac7z8t\",\"to\":\"c44mse2osa\"}],\"nodeList\":[{\"id\":\"ejfac7z8t\",\"name\":\"实时推送\",\"type\":\"realTimePush\",\"left\":\"249px\",\"top\":\"167px\",\"ico\":\"el-icon-caret-right\",\"state\":\"success\"},{\"id\":\"yf6v8ryif\",\"name\":\"设备日志\",\"type\":\"deviceLog\",\"left\":\"0px\",\"top\":\"141px\",\"ico\":\"el-icon-time\",\"state\":\"success\",\"configData\":{\"productScope\":[\"HJ_PRODUCT\"],\"productList\":[{\"id\":\"1998562969057230849\",\"productSn\":\"HJ_PRODUCT\",\"productName\":\"环境检测仪\",\"linkMethodId\":null,\"linkMethodName\":null,\"componentId\":\"1998562852543660033\",\"componentName\":\"环境检测仪\",\"protocolId\":\"1998562616433704961\",\"protocolName\":\"环境监测仪协议\",\"deviceCount\":1,\"deviceType\":\"0\",\"status\":\"0\",\"createBy\":null,\"createTime\":\"2025-12-10 09:18:38\",\"updateBy\":null,\"updateTime\":null,\"remark\":\"环境检测仪\",\"timeoutSeconds\":60,\"regularCleaning\":\"0\",\"retentionTime\":1,\"retentionUnit\":null,\"customConfig\":null},{\"id\":\"1998568066453602306\",\"productSn\":\"SW_PRODUCT\",\"productName\":\"水位检测仪\",\"linkMethodId\":null,\"linkMethodName\":null,\"componentId\":\"1998564935472775169\",\"componentName\":\"水位检测仪组件\",\"protocolId\":\"1998567823599206401\",\"protocolName\":\"水位检测仪\",\"deviceCount\":1,\"deviceType\":\"1\",\"status\":\"0\",\"createBy\":null,\"createTime\":\"2025-12-10 09:38:53\",\"updateBy\":null,\"updateTime\":null,\"remark\":\"水位检测仪\",\"timeoutSeconds\":60,\"regularCleaning\":\"0\",\"retentionTime\":1,\"retentionUnit\":null,\"customConfig\":null}],\"deviceScope\":\"all\",\"deviceList\":[],\"deviceSnList\":[],\"authHeaderSign\":null,\"authToken\":null,\"routingKey\":null,\"exchange\":\"\",\"topic\":\"\",\"host\":\"\",\"port\":null,\"tags\":null,\"group\":null,\"url\":null,\"key\":null}},{\"id\":\"c44mse2osa\",\"name\":\"HTTP接口\",\"type\":\"HTTP\",\"left\":\"511px\",\"top\":\"150px\",\"ico\":\"el-icon-caret-right\",\"state\', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-22 11:58:19', 12);
INSERT INTO "public"."sys_oper_log" VALUES (613, '参数管理', 1, 'com.labdatahub.web.controller.system.SysConfigController.add()', 'POST', 1, 'admin', '研发部门', '/system/config', '106.92.128.79', 'XX XX', '{"configKey":"device.function.batch","configName":"优化-指令下发日志批量保存","configType":"Y","configValue":"true,1000,10000","createBy":"admin","params":{}}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-22 20:32:34', 78);
INSERT INTO "public"."sys_oper_log" VALUES (614, '参数管理', 2, 'com.labdatahub.web.controller.system.SysConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/config', '106.92.128.79', 'XX XX', '{"configId":102,"configKey":"device.function.batch","configName":"优化-指令下发日志批量保存","configType":"Y","configValue":"true,1000,10000","createBy":"admin","createTime":"2026-01-22 20:32:34","params":{},"remark":"第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备指令下发并发高并且集中时缓解数据库压力，告警日志会先缓存，然后循环取数据批量插入数据库。数据量大时会导致插入数据可能会有一两秒的延迟。酌情开启。","updateBy":"admin"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-22 20:34:08', 199);
INSERT INTO "public"."sys_oper_log" VALUES (615, '参数管理', 2, 'com.labdatahub.web.controller.system.SysConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/config', '106.92.128.79', 'XX XX', '{"configId":101,"configKey":"device.log.batch","configName":"优化-设备日志批量保存","configType":"Y","configValue":"true,1000,10000","createBy":"admin","createTime":"2026-01-21 22:54:31","params":{},"remark":"第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备日志并发高并且集中时缓解数据库压力，设备日志会先缓存，然后以每秒取固定数量数据批量插入数据库。数据量大时会导致插入数据可能会有一两秒的延迟，但不会影响实时数据以及告警等功能的实时性。酌情开启。","updateBy":"admin","updateTime":"2026-01-22 00:14:32"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-22 20:34:22', 19);
INSERT INTO "public"."sys_oper_log" VALUES (616, '参数管理', 1, 'com.labdatahub.web.controller.system.SysConfigController.add()', 'POST', 1, 'admin', '研发部门', '/system/config', '106.92.128.79', 'XX XX', '{"configKey":"device.warn.batch","configName":"优化-告警日志批量保存","configType":"Y","configValue":"true,1000,10000","createBy":"admin","params":{}}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-22 20:35:09', 38);
INSERT INTO "public"."sys_oper_log" VALUES (617, '参数管理', 2, 'com.labdatahub.web.controller.system.SysConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/config', '106.92.128.79', 'XX XX', '{"configId":103,"configKey":"device.warn.batch","configName":"优化-告警日志批量保存","configType":"Y","configValue":"true,1000,10000","createBy":"admin","createTime":"2026-01-22 20:35:09","params":{},"remark":"第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备告警并发高并且集中时缓解数据库压力，告警日志会先缓存，然后循环取数据批量插入数据库。数据量大时会导致插入数据可能会有一两秒的延迟。酌情开启。","updateBy":"admin"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-22 20:35:58', 46);
INSERT INTO "public"."sys_oper_log" VALUES (618, '参数管理', 2, 'com.labdatahub.web.controller.system.SysConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/config', '106.92.128.79', 'XX XX', '{"configId":102,"configKey":"device.function.batch","configName":"优化-指令下发日志批量保存","configType":"Y","configValue":"true,1000,10000","createBy":"admin","createTime":"2026-01-22 20:32:34","params":{},"remark":"第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备指令下发并发高并且集中时缓解数据库压力，指令日志会先缓存，然后循环取数据批量插入数据库。数据量大时会导致插入数据可能会有一两秒的延迟。酌情开启。","updateBy":"admin","updateTime":"2026-01-22 20:34:08"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-22 20:36:14', 26);
INSERT INTO "public"."sys_oper_log" VALUES (619, '参数管理', 1, 'com.labdatahub.web.controller.system.SysConfigController.add()', 'POST', 1, 'admin', '研发部门', '/system/config', '106.92.128.79', 'XX XX', '{"configKey":"device.linkage.batch","configName":"优化-设备联动告警日志批量保存","configType":"Y","configValue":"true,1000,10000","createBy":"admin","params":{}}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-22 20:49:06', 224);
INSERT INTO "public"."sys_oper_log" VALUES (620, '参数管理', 2, 'com.labdatahub.web.controller.system.SysConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/config', '106.92.128.79', 'XX XX', '{"configId":104,"configKey":"device.linkage.batch","configName":"优化-设备联动告警日志批量保存","configType":"Y","configValue":"true,1000,10000","createBy":"admin","createTime":"2026-01-22 20:49:06","params":{},"remark":"第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备告警并发高并且集中时缓解数据库压力，告警日志会先缓存，然后循环取数据批量插入数据库。数据量大时会导致插入数据可能会有一两秒的延迟。酌情开启。","updateBy":"admin"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-22 20:49:18', 41);
INSERT INTO "public"."sys_oper_log" VALUES (621, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/2012448841245237250', '124.128.9.243', 'XX XX', '"2012448841245237250"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-23 09:48:31', 64);
INSERT INTO "public"."sys_oper_log" VALUES (622, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/2012448840620285954', '124.128.9.243', 'XX XX', '"2012448840620285954"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-23 09:48:34', 11);
INSERT INTO "public"."sys_oper_log" VALUES (623, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubLinkageWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/linkageRecord/deal/1997681604928483330', '61.178.29.188', 'XX XX', '"1997681604928483330"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-23 11:59:35', 58);
INSERT INTO "public"."sys_oper_log" VALUES (624, '参数管理', 1, 'com.labdatahub.web.controller.system.SysConfigController.add()', 'POST', 1, 'admin', '研发部门', '/system/config', '125.80.202.14', 'XX XX', '{"configKey":"device.warn.batch","configName":"device.warn.batch","configType":"Y","configValue":"device.warn.batch\n","params":{}}', '{"msg":"新增参数''device.warn.batch''失败，参数键名已存在","code":500}', 0, NULL, '2026-01-23 14:34:44', 133);
INSERT INTO "public"."sys_oper_log" VALUES (625, '参数管理', 2, 'com.labdatahub.web.controller.system.SysConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/config', '125.80.202.14', 'XX XX', '{"configId":103,"configKey":"device.warn.batch","configName":"优化-告警日志批量保存","configType":"Y","configValue":"true,1000,10000","createBy":"admin","createTime":"2026-01-22 20:35:09","params":{},"remark":"第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备告警并发高并且集中时缓解数据库压力，告警日志会先缓存，然后循环取数据批量插入数据库。数据量大时会导致插入数据可能会有一两秒的延迟。酌情开启。","updateBy":"admin","updateTime":"2026-01-22 20:35:58"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-23 14:37:55', 202);
INSERT INTO "public"."sys_oper_log" VALUES (626, '定时任务', 2, 'com.labdatahub.quartz.controller.SysJobController.changeStatus()', 'PUT', 1, 'admin', '研发部门', '/monitor/job/changeStatus', '61.178.29.188', 'XX XX', '{"jobId":3,"misfirePolicy":"0","params":{},"status":"0"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-23 15:09:24', 285);
INSERT INTO "public"."sys_oper_log" VALUES (627, '定时任务', 2, 'com.labdatahub.quartz.controller.SysJobController.changeStatus()', 'PUT', 1, 'admin', '研发部门', '/monitor/job/changeStatus', '61.178.29.188', 'XX XX', '{"jobId":3,"misfirePolicy":"0","params":{},"status":"1"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-23 15:09:27', 510);
INSERT INTO "public"."sys_oper_log" VALUES (628, '标记处理状态', 2, 'com.labdatahub.business.controller.LabdatahubWarnRecordController.deal()', 'PUT', 1, 'admin', '研发部门', '/business/warnRecord/deal/2012448840599314434', '61.178.29.188', 'XX XX', '"2012448840599314434"', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-23 15:41:56', 56);
INSERT INTO "public"."sys_oper_log" VALUES (629, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '36.143.33.145', 'XX XX', '{"belongSn":"SW_01","belongType":"1","createTime":"2025-12-10 09:29:29","functionCode":"water_out","functionName":"放水","functionParams":"0.8","id":"2012428471637221378","protocolId":"1998564778664525825"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-24 14:08:29', 15);
INSERT INTO "public"."sys_oper_log" VALUES (630, '设备指令下发', 1, 'com.labdatahub.business.controller.LabdatahubFunctionController.add()', 'POST', 1, 'admin', '研发部门', '/business/function', '36.143.33.145', 'XX XX', '{"belongSn":"222222222222","belongType":"1","createTime":"2026-01-24 14:20:34","functionCode":"s1","functionName":"111","functionParams":"2","id":"2014946408034050049","protocolId":"1998562616433704961"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-24 14:20:34', 90);
INSERT INTO "public"."sys_oper_log" VALUES (631, '设备指令下发', 2, 'com.labdatahub.business.controller.LabdatahubFunctionController.edit()', 'PUT', 1, 'admin', '研发部门', '/business/function', '36.143.33.145', 'XX XX', '{"belongSn":"222222222222","belongType":"1","createTime":"2026-01-24 14:20:34","functionCode":"s1","functionName":"111","functionParams":"2","id":"2014946408034050049","protocolId":"1998562616433704961"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-24 14:20:37', 9);
INSERT INTO "public"."sys_oper_log" VALUES (3, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{"businessName":"modbus","className":"LabdatahubModbusConfig","columns":[{"capJavaField":"Id","columnComment":"id","columnId":148,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-12-16 13:40:59","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":16,"updateBy":"","updateTime":"2026-01-24 16:49:47","usableColumn":false},{"capJavaField":"BelongSn","columnComment":"归属sn","columnId":149,"columnName":"belong_sn","columnType":"varchar(255)","createBy":"admin","createTime":"2025-12-16 13:40:59","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"belongSn","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":16,"updateBy":"","updateTime":"2026-01-24 16:49:47","usableColumn":false},{"capJavaField":"BelongType","columnComment":"归属类型 0-产品 1-设备","columnId":150,"columnName":"belong_type","columnType":"varchar(255)","createBy":"admin","createTime":"2025-12-16 13:40:59","dictType":"","edit":true,"htmlType":"select","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"belongType","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":16,"updateBy":"","updateTime":"2026-01-24 16:49:47","usableColumn":false},{"capJavaField":"Code","columnComment":"读取编码","columnId":151,"columnName":"code","columnType":"varchar(255)","createBy":"admin","createTime":"2025-12-16 13:40:59","dictType":"","edit":true,"htmlType":"inpu', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-24 16:49:48.15', 424);
INSERT INTO "public"."sys_oper_log" VALUES (4, '代码生成', 3, 'com.labdatahub.generator.controller.GenController.remove()', 'DELETE', 1, 'admin', '研发部门', '/tool/gen/8', '127.0.0.1', '内网IP', '[8]', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-24 16:49:55.69', 56);
INSERT INTO "public"."sys_oper_log" VALUES (5, '代码生成', 2, 'com.labdatahub.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{"businessName":"linkage","className":"LabdatahubWarnLinkage","columns":[{"capJavaField":"Id","columnComment":"id","columnId":110,"columnName":"id","columnType":"varchar(255)","createBy":"admin","createTime":"2025-11-03 11:13:47","dictType":"","edit":false,"htmlType":"input","increment":false,"insert":true,"isIncrement":"0","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"String","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":12,"updateBy":"","updateTime":"2026-01-24 16:49:59","usableColumn":false},{"capJavaField":"RuleJson","columnComment":"规则json","columnId":111,"columnName":"rule_json","columnType":"text","createBy":"admin","createTime":"2025-11-03 11:13:47","dictType":"","edit":true,"htmlType":"textarea","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"ruleJson","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":12,"updateBy":"","updateTime":"2026-01-24 16:49:59","usableColumn":false},{"capJavaField":"WarnMessage","columnComment":"告警消息模板","columnId":112,"columnName":"warn_message","columnType":"text","createBy":"admin","createTime":"2025-11-03 11:13:47","dictType":"","edit":true,"htmlType":"textarea","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"warnMessage","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":12,"updateBy":"","updateTime":"2026-01-24 16:49:59","usableColumn":false},{"capJavaField":"WarnLevel","columnComment":"告警等级 1-紧急 2-严重 3-警告 4-正常","columnId":113,"columnName":"warn_level","columnType":"varchar(255)","createBy":"admin","createTime":"2025-11-03 11:13:47","dictType":"","edit":true', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-24 16:50:00.098', 211);
INSERT INTO "public"."sys_oper_log" VALUES (7, '用户管理', 1, 'com.labdatahub.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '研发部门', '/system/user', '127.0.0.1', '内网IP', '{"admin":false,"createBy":"admin","createTime":"2026-01-24 16:50:32","nickName":"ruoyi","params":{},"postIds":[],"roleIds":[],"status":"0","userId":2,"userName":"ruoyi"}', '{"msg":"操作成功","code":200}', 0, NULL, '2026-01-24 16:50:32.775', 127);
-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_post";
CREATE TABLE "public"."sys_post" (
  "post_id" int8 NOT NULL DEFAULT nextval('sys_post_post_id_seq'::regclass),
  "post_code" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "post_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "post_sort" int4 NOT NULL,
  "status" char(1) COLLATE "pg_catalog"."default" NOT NULL,
  "create_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "update_time" timestamp(6),
  "remark" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."sys_post"."post_id" IS '岗位ID';
COMMENT ON COLUMN "public"."sys_post"."post_code" IS '岗位编码';
COMMENT ON COLUMN "public"."sys_post"."post_name" IS '岗位名称';
COMMENT ON COLUMN "public"."sys_post"."post_sort" IS '显示顺序';
COMMENT ON COLUMN "public"."sys_post"."status" IS '状态（0正常 1停用）';
COMMENT ON COLUMN "public"."sys_post"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."sys_post"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sys_post"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."sys_post"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."sys_post"."remark" IS '备注';
COMMENT ON TABLE "public"."sys_post" IS '岗位信息表';

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO "public"."sys_post" VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_post" VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_post" VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO "public"."sys_post" VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2025-09-15 11:09:57', '', NULL, '');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_role";
CREATE TABLE "public"."sys_role" (
  "role_id" int8 NOT NULL DEFAULT nextval('sys_role_role_id_seq'::regclass),
  "role_name" varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
  "role_key" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "role_sort" int4 NOT NULL,
  "data_scope" char(1) COLLATE "pg_catalog"."default" DEFAULT '1'::bpchar,
  "menu_check_strictly" bool DEFAULT true,
  "dept_check_strictly" bool DEFAULT true,
  "status" char(1) COLLATE "pg_catalog"."default" NOT NULL,
  "del_flag" char(1) COLLATE "pg_catalog"."default" DEFAULT '0'::bpchar,
  "create_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "update_time" timestamp(6),
  "remark" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."sys_role"."role_id" IS '角色ID';
COMMENT ON COLUMN "public"."sys_role"."role_name" IS '角色名称';
COMMENT ON COLUMN "public"."sys_role"."role_key" IS '角色权限字符串';
COMMENT ON COLUMN "public"."sys_role"."role_sort" IS '显示顺序';
COMMENT ON COLUMN "public"."sys_role"."data_scope" IS '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）';
COMMENT ON COLUMN "public"."sys_role"."menu_check_strictly" IS '菜单树选择项是否关联显示';
COMMENT ON COLUMN "public"."sys_role"."dept_check_strictly" IS '部门树选择项是否关联显示';
COMMENT ON COLUMN "public"."sys_role"."status" IS '角色状态（0正常 1停用）';
COMMENT ON COLUMN "public"."sys_role"."del_flag" IS '删除标志（0代表存在 2代表删除）';
COMMENT ON COLUMN "public"."sys_role"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."sys_role"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sys_role"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."sys_role"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."sys_role"."remark" IS '备注';
COMMENT ON TABLE "public"."sys_role" IS '角色信息表';

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO "public"."sys_role" VALUES (1, '超级管理员', 'admin', 1, '1', 't', 't', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL, '超级管理员');
INSERT INTO "public"."sys_role" VALUES (2, '测试', 'role', 0, '1', 't', 't', '0', '0', 'admin', '2026-01-24 16:51:03.65', '', NULL, NULL);

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_role_dept";
CREATE TABLE "public"."sys_role_dept" (
  "role_id" int8 NOT NULL,
  "dept_id" int8 NOT NULL
)
;
COMMENT ON COLUMN "public"."sys_role_dept"."role_id" IS '角色ID';
COMMENT ON COLUMN "public"."sys_role_dept"."dept_id" IS '部门ID';
COMMENT ON TABLE "public"."sys_role_dept" IS '角色和部门关联表';

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_role_menu";
CREATE TABLE "public"."sys_role_menu" (
  "role_id" int8 NOT NULL,
  "menu_id" int8 NOT NULL
)
;
COMMENT ON COLUMN "public"."sys_role_menu"."role_id" IS '角色ID';
COMMENT ON COLUMN "public"."sys_role_menu"."menu_id" IS '菜单ID';
COMMENT ON TABLE "public"."sys_role_menu" IS '角色和菜单关联表';

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO "public"."sys_role_menu" VALUES (2, 2036);
INSERT INTO "public"."sys_role_menu" VALUES (2, 2012);
INSERT INTO "public"."sys_role_menu" VALUES (2, 2013);
INSERT INTO "public"."sys_role_menu" VALUES (2, 2014);
INSERT INTO "public"."sys_role_menu" VALUES (2, 2015);
INSERT INTO "public"."sys_role_menu" VALUES (2, 2016);
INSERT INTO "public"."sys_role_menu" VALUES (2, 2017);
INSERT INTO "public"."sys_role_menu" VALUES (2, 2006);
INSERT INTO "public"."sys_role_menu" VALUES (2, 2007);
INSERT INTO "public"."sys_role_menu" VALUES (2, 2008);
INSERT INTO "public"."sys_role_menu" VALUES (2, 2009);
INSERT INTO "public"."sys_role_menu" VALUES (2, 2010);
INSERT INTO "public"."sys_role_menu" VALUES (2, 2011);
INSERT INTO "public"."sys_role_menu" VALUES (2, 2045);
INSERT INTO "public"."sys_role_menu" VALUES (2, 2050);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_user";
CREATE TABLE "public"."sys_user" (
  "user_id" int8 NOT NULL DEFAULT nextval('sys_user_user_id_seq'::regclass),
  "dept_id" int8,
  "user_name" varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
  "nick_name" varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
  "user_type" varchar(2) COLLATE "pg_catalog"."default" DEFAULT '00'::character varying,
  "email" varchar(50) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "phonenumber" varchar(11) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "sex" char(1) COLLATE "pg_catalog"."default" DEFAULT '0'::bpchar,
  "avatar" varchar(100) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "password" varchar(100) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "status" char(1) COLLATE "pg_catalog"."default" DEFAULT '0'::bpchar,
  "del_flag" char(1) COLLATE "pg_catalog"."default" DEFAULT '0'::bpchar,
  "login_ip" varchar(128) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "login_date" timestamp(6),
  "pwd_update_date" timestamp(6),
  "create_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "create_time" timestamp(6),
  "update_by" varchar(64) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "update_time" timestamp(6),
  "remark" varchar(500) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."sys_user"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."sys_user"."dept_id" IS '部门ID';
COMMENT ON COLUMN "public"."sys_user"."user_name" IS '用户账号';
COMMENT ON COLUMN "public"."sys_user"."nick_name" IS '用户昵称';
COMMENT ON COLUMN "public"."sys_user"."user_type" IS '用户类型（00系统用户）';
COMMENT ON COLUMN "public"."sys_user"."email" IS '用户邮箱';
COMMENT ON COLUMN "public"."sys_user"."phonenumber" IS '手机号码';
COMMENT ON COLUMN "public"."sys_user"."sex" IS '用户性别（0男 1女 2未知）';
COMMENT ON COLUMN "public"."sys_user"."avatar" IS '头像地址';
COMMENT ON COLUMN "public"."sys_user"."password" IS '密码';
COMMENT ON COLUMN "public"."sys_user"."status" IS '账号状态（0正常 1停用）';
COMMENT ON COLUMN "public"."sys_user"."del_flag" IS '删除标志（0代表存在 2代表删除）';
COMMENT ON COLUMN "public"."sys_user"."login_ip" IS '最后登录IP';
COMMENT ON COLUMN "public"."sys_user"."login_date" IS '最后登录时间';
COMMENT ON COLUMN "public"."sys_user"."pwd_update_date" IS '密码最后更新时间';
COMMENT ON COLUMN "public"."sys_user"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."sys_user"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sys_user"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."sys_user"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."sys_user"."remark" IS '备注';
COMMENT ON TABLE "public"."sys_user" IS '用户信息表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO "public"."sys_user" VALUES (1, 103, 'admin', '若依', '00', 'ry@163.com', '15888888888', '1', '/profile/avatar/2026/01/21/5ebbe04a1a52488e8d881a8538ce704f.jpg', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2026-01-24 16:16:20.545', '2025-12-05 16:23:20', 'admin', '2025-09-15 11:09:57', '', '2026-01-21 11:27:03', '管理员');
INSERT INTO "public"."sys_user" VALUES (2, NULL, 'ruoyi', 'ruoyi', '00', '', '', '0', '', '$2a$10$sej0dh0AJhcbFljQU1PDg.klJJ5kzijsIiigtF4gnOn7/sNt1yc2C', '0', '0', '', NULL, NULL, 'admin', '2026-01-24 16:50:32.728', '', NULL, NULL);

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_user_post";
CREATE TABLE "public"."sys_user_post" (
  "user_id" int8 NOT NULL,
  "post_id" int8 NOT NULL
)
;
COMMENT ON COLUMN "public"."sys_user_post"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."sys_user_post"."post_id" IS '岗位ID';
COMMENT ON TABLE "public"."sys_user_post" IS '用户与岗位关联表';

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_user_role";
CREATE TABLE "public"."sys_user_role" (
  "user_id" int8 NOT NULL,
  "role_id" int8 NOT NULL
)
;
COMMENT ON COLUMN "public"."sys_user_role"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."sys_user_role"."role_id" IS '角色ID';
COMMENT ON TABLE "public"."sys_user_role" IS '用户和角色关联表';

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO "public"."sys_user_role" VALUES (1, 1);

-- ----------------------------
-- Table structure for labdatahub_component
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_component";
CREATE TABLE "public"."labdatahub_component" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "net_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "ip_addr" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "port" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "open_tls" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "remark" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "create_by" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "update_time" timestamp(6),
  "update_by" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "status" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "other_config" text COLLATE "pg_catalog"."default",
  "protocol_id" text COLLATE "pg_catalog"."default",
  "protocol_name" text COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."labdatahub_component"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_component"."name" IS '组件名称';
COMMENT ON COLUMN "public"."labdatahub_component"."net_type" IS '网络类型';
COMMENT ON COLUMN "public"."labdatahub_component"."ip_addr" IS 'IP地址';
COMMENT ON COLUMN "public"."labdatahub_component"."port" IS '端口';
COMMENT ON COLUMN "public"."labdatahub_component"."open_tls" IS '是否开启TLS (0-否 1-是)';
COMMENT ON COLUMN "public"."labdatahub_component"."remark" IS '备注';
COMMENT ON COLUMN "public"."labdatahub_component"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_component"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."labdatahub_component"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."labdatahub_component"."update_by" IS '修改人';
COMMENT ON COLUMN "public"."labdatahub_component"."status" IS '0-停用 1-启用';
COMMENT ON COLUMN "public"."labdatahub_component"."other_config" IS '其余配置(如账号密码等)';
COMMENT ON COLUMN "public"."labdatahub_component"."protocol_id" IS '协议id';
COMMENT ON COLUMN "public"."labdatahub_component"."protocol_name" IS '协议名称';
COMMENT ON TABLE "public"."labdatahub_component" IS '网络组件';

-- ----------------------------
-- Records of labdatahub_component
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_device
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_device";
CREATE TABLE "public"."labdatahub_device" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "device_sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "device_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "product_id" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "product_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "product_sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamp(6),
  "create_by" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "update_time" timestamp(6),
  "update_by" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "link_method_id" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "link_method_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "protocol_id" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "protocol_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "status" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "remark" text COLLATE "pg_catalog"."default",
  "timeout_seconds" int4 DEFAULT 10,
  "device_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "regular_cleaning" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "retention_time" int8 DEFAULT 1,
  "retention_unit" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "component_id" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "component_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "custom_config" text COLLATE "pg_catalog"."default",
  "position" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "position_name" text COLLATE "pg_catalog"."default",
  "slave_id" int4,
  "modbus_read" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "group_code" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "group_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."labdatahub_device"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_device"."device_sn" IS '设备编码';
COMMENT ON COLUMN "public"."labdatahub_device"."device_name" IS '设备名称';
COMMENT ON COLUMN "public"."labdatahub_device"."product_id" IS '关联产品id';
COMMENT ON COLUMN "public"."labdatahub_device"."product_name" IS '关联产品名称';
COMMENT ON COLUMN "public"."labdatahub_device"."product_sn" IS '关联产品编码';
COMMENT ON COLUMN "public"."labdatahub_device"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_device"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."labdatahub_device"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."labdatahub_device"."update_by" IS '修改人';
COMMENT ON COLUMN "public"."labdatahub_device"."link_method_id" IS '接入方式id';
COMMENT ON COLUMN "public"."labdatahub_device"."link_method_name" IS '接入方式名称';
COMMENT ON COLUMN "public"."labdatahub_device"."protocol_id" IS '协议id';
COMMENT ON COLUMN "public"."labdatahub_device"."protocol_name" IS '协议名称';
COMMENT ON COLUMN "public"."labdatahub_device"."status" IS '0-离线 1-在线';
COMMENT ON COLUMN "public"."labdatahub_device"."remark" IS '备注';
COMMENT ON COLUMN "public"."labdatahub_device"."timeout_seconds" IS '心跳超时时间(秒)';
COMMENT ON COLUMN "public"."labdatahub_device"."device_type" IS '设备类型 0-直连设备 1-网关设备 2-无状态设备';
COMMENT ON COLUMN "public"."labdatahub_device"."regular_cleaning" IS '数据是否定期清理 0-否 1-是';
COMMENT ON COLUMN "public"."labdatahub_device"."retention_time" IS '数据保存时间';
COMMENT ON COLUMN "public"."labdatahub_device"."retention_unit" IS '数据保存时间单位 hour-时 day-天 week-周 month-月 year-年';
COMMENT ON COLUMN "public"."labdatahub_device"."component_id" IS '组件id';
COMMENT ON COLUMN "public"."labdatahub_device"."component_name" IS '组件名称';
COMMENT ON COLUMN "public"."labdatahub_device"."custom_config" IS '自定义配置';
COMMENT ON COLUMN "public"."labdatahub_device"."position" IS '经纬度';
COMMENT ON COLUMN "public"."labdatahub_device"."position_name" IS '定位地点名称';
COMMENT ON COLUMN "public"."labdatahub_device"."slave_id" IS 'modbus从站id';
COMMENT ON COLUMN "public"."labdatahub_device"."modbus_read" IS '是否开启modbus数据读取 0-关闭 1-开启';
COMMENT ON COLUMN "public"."labdatahub_device"."group_code" IS '分组CODE';
COMMENT ON COLUMN "public"."labdatahub_device"."group_name" IS '分组名称';
COMMENT ON TABLE "public"."labdatahub_device" IS '设备表';

-- ----------------------------
-- Records of labdatahub_device
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_device_group
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_device_group";
CREATE TABLE "public"."labdatahub_device_group" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "group_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "group_code" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "sort_num" int4 DEFAULT 0,
  "remark" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6)
)
;
COMMENT ON COLUMN "public"."labdatahub_device_group"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_device_group"."group_name" IS '分组名称';
COMMENT ON COLUMN "public"."labdatahub_device_group"."group_code" IS '唯一标识';
COMMENT ON COLUMN "public"."labdatahub_device_group"."type" IS '0-产品 1-设备';
COMMENT ON COLUMN "public"."labdatahub_device_group"."sort_num" IS '排序';
COMMENT ON COLUMN "public"."labdatahub_device_group"."remark" IS '备注';
COMMENT ON COLUMN "public"."labdatahub_device_group"."create_time" IS '创建时间';
COMMENT ON TABLE "public"."labdatahub_device_group" IS '设备分组';

-- ----------------------------
-- Records of labdatahub_device_group
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_device_logs
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_device_logs";
CREATE TABLE "public"."labdatahub_device_logs" (
  "id" int8 NOT NULL,
  "device_sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "report_time" timestamp(6),
  "properties" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "log_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."labdatahub_device_logs"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_device_logs"."device_sn" IS '设备sn';
COMMENT ON COLUMN "public"."labdatahub_device_logs"."report_time" IS '上报时间';
COMMENT ON COLUMN "public"."labdatahub_device_logs"."properties" IS '属性json';
COMMENT ON COLUMN "public"."labdatahub_device_logs"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_device_logs"."log_type" IS '日志类型  PROPERTY-上行消息 OFFLINE-设备离线 ONLINE-设备上线';
COMMENT ON TABLE "public"."labdatahub_device_logs" IS '设备日志表';

-- ----------------------------
-- Records of labdatahub_device_logs
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_function
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_function";
CREATE TABLE "public"."labdatahub_function" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "function_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "function_code" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "function_params" text COLLATE "pg_catalog"."default",
  "belong_sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "protocol_id" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "create_by" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "belong_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying
)
;
COMMENT ON COLUMN "public"."labdatahub_function"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_function"."function_name" IS '功能名称';
COMMENT ON COLUMN "public"."labdatahub_function"."function_code" IS '功能编码';
COMMENT ON COLUMN "public"."labdatahub_function"."function_params" IS '自定义参数';
COMMENT ON COLUMN "public"."labdatahub_function"."belong_sn" IS '设备/产品sn';
COMMENT ON COLUMN "public"."labdatahub_function"."protocol_id" IS '协议id';
COMMENT ON COLUMN "public"."labdatahub_function"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_function"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."labdatahub_function"."belong_type" IS '归属类型 0-产品 1-设备';
COMMENT ON TABLE "public"."labdatahub_function" IS '设备指令下发表';

-- ----------------------------
-- Records of labdatahub_function
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_function_record
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_function_record";
CREATE TABLE "public"."labdatahub_function_record" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "function_id" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "function_code" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "function_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "function_params" text COLLATE "pg_catalog"."default",
  "is_success" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '1'::character varying,
  "create_time" timestamp(6),
  "device_sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "device_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "trigger_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying
)
;
COMMENT ON COLUMN "public"."labdatahub_function_record"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_function_record"."function_id" IS '功能id';
COMMENT ON COLUMN "public"."labdatahub_function_record"."function_code" IS '功能code';
COMMENT ON COLUMN "public"."labdatahub_function_record"."function_name" IS '功能名称';
COMMENT ON COLUMN "public"."labdatahub_function_record"."function_params" IS '参数';
COMMENT ON COLUMN "public"."labdatahub_function_record"."is_success" IS '0-失败 1-成功';
COMMENT ON COLUMN "public"."labdatahub_function_record"."create_time" IS '下发时间';
COMMENT ON COLUMN "public"."labdatahub_function_record"."device_sn" IS '设备sn';
COMMENT ON COLUMN "public"."labdatahub_function_record"."device_name" IS '设备名称';
COMMENT ON COLUMN "public"."labdatahub_function_record"."trigger_type" IS '0-手动触发 1-告警触发 2-定时触发';
COMMENT ON TABLE "public"."labdatahub_function_record" IS '指令下发记录';

-- ----------------------------
-- Records of labdatahub_function_record
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_linkage_action_record
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_linkage_action_record";
CREATE TABLE "public"."labdatahub_linkage_action_record" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "config_id" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "config_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "execute_sn" text COLLATE "pg_catalog"."default",
  "execute_name" text COLLATE "pg_catalog"."default",
  "function_code" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "function_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "function_param" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6)
)
;
COMMENT ON COLUMN "public"."labdatahub_linkage_action_record"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_linkage_action_record"."config_id" IS '告警配置id';
COMMENT ON COLUMN "public"."labdatahub_linkage_action_record"."config_name" IS '告警配置名称';
COMMENT ON COLUMN "public"."labdatahub_linkage_action_record"."execute_sn" IS '执行动作设备SN';
COMMENT ON COLUMN "public"."labdatahub_linkage_action_record"."execute_name" IS '执行动作设备名称';
COMMENT ON COLUMN "public"."labdatahub_linkage_action_record"."function_code" IS '动作CODE';
COMMENT ON COLUMN "public"."labdatahub_linkage_action_record"."function_name" IS '动作名称';
COMMENT ON COLUMN "public"."labdatahub_linkage_action_record"."function_param" IS '参数';
COMMENT ON COLUMN "public"."labdatahub_linkage_action_record"."create_time" IS '创建时间';
COMMENT ON TABLE "public"."labdatahub_linkage_action_record" IS '设备联动告警动作执行记录';

-- ----------------------------
-- Records of labdatahub_linkage_action_record
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_linkage_warn_record
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_linkage_warn_record";
CREATE TABLE "public"."labdatahub_linkage_warn_record" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "config_id" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "config_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "warn_message" text COLLATE "pg_catalog"."default",
  "warn_data" text COLLATE "pg_catalog"."default",
  "trigger_sn_list" text COLLATE "pg_catalog"."default",
  "trigger_name_list" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "warn_level" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '1'::character varying,
  "status" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying
)
;
COMMENT ON COLUMN "public"."labdatahub_linkage_warn_record"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_linkage_warn_record"."config_id" IS '告警配置id';
COMMENT ON COLUMN "public"."labdatahub_linkage_warn_record"."config_name" IS '告警配置名称';
COMMENT ON COLUMN "public"."labdatahub_linkage_warn_record"."warn_message" IS '告警内容';
COMMENT ON COLUMN "public"."labdatahub_linkage_warn_record"."warn_data" IS '告警时相关设备数据';
COMMENT ON COLUMN "public"."labdatahub_linkage_warn_record"."trigger_sn_list" IS '告警设备SN列表';
COMMENT ON COLUMN "public"."labdatahub_linkage_warn_record"."trigger_name_list" IS '告警设备名称列表';
COMMENT ON COLUMN "public"."labdatahub_linkage_warn_record"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_linkage_warn_record"."warn_level" IS '告警等级 1-紧急 2-严重 3-一般 4-警告 5-正常';
COMMENT ON COLUMN "public"."labdatahub_linkage_warn_record"."status" IS '0-未处理 1-已处理';
COMMENT ON TABLE "public"."labdatahub_linkage_warn_record" IS '设备联动告警记录';

-- ----------------------------
-- Records of labdatahub_linkage_warn_record
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_media_device
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_media_device";
CREATE TABLE "public"."labdatahub_media_device" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "device_sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "device_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "play_url" text COLLATE "pg_catalog"."default",
  "platform_id" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "platform_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "platform_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '3'::character varying,
  "create_time" timestamp(6),
  "device_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "use_model" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying
)
;
COMMENT ON COLUMN "public"."labdatahub_media_device"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_media_device"."device_sn" IS '设备编码';
COMMENT ON COLUMN "public"."labdatahub_media_device"."device_name" IS '设备名称';
COMMENT ON COLUMN "public"."labdatahub_media_device"."play_url" IS '播放地址';
COMMENT ON COLUMN "public"."labdatahub_media_device"."platform_id" IS '平台id';
COMMENT ON COLUMN "public"."labdatahub_media_device"."platform_name" IS '平台名称';
COMMENT ON COLUMN "public"."labdatahub_media_device"."platform_type" IS '平台类型 1-海康 2-大华 3-自建';
COMMENT ON COLUMN "public"."labdatahub_media_device"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_media_device"."device_type" IS '0-枪机 1-球机';
COMMENT ON COLUMN "public"."labdatahub_media_device"."use_model" IS '模式  0-绑定平台 1-手动添加';
COMMENT ON TABLE "public"."labdatahub_media_device" IS '视频设备表';

-- ----------------------------
-- Records of labdatahub_media_device
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_media_server
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_media_server";
CREATE TABLE "public"."labdatahub_media_server" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "platform_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '3'::character varying,
  "platform_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "server_ip" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "server_port" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "config_json" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6)
)
;
COMMENT ON COLUMN "public"."labdatahub_media_server"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_media_server"."platform_type" IS '平台类型 1-海康 2-大华 3-自建';
COMMENT ON COLUMN "public"."labdatahub_media_server"."platform_name" IS '平台名称';
COMMENT ON COLUMN "public"."labdatahub_media_server"."server_ip" IS '流媒体服务器地址';
COMMENT ON COLUMN "public"."labdatahub_media_server"."server_port" IS '端口';
COMMENT ON COLUMN "public"."labdatahub_media_server"."config_json" IS 'json配置';
COMMENT ON COLUMN "public"."labdatahub_media_server"."create_time" IS '创建时间';
COMMENT ON TABLE "public"."labdatahub_media_server" IS '流媒体服务器配置';

-- ----------------------------
-- Records of labdatahub_media_server
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_modbus_config
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_modbus_config";
CREATE TABLE "public"."labdatahub_modbus_config" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "belong_sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "belong_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "code" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamp(6),
  "register_range" text COLLATE "pg_catalog"."default",
  "interval_time" int4 DEFAULT 1,
  "delay_time" int4 DEFAULT 0
)
;
COMMENT ON COLUMN "public"."labdatahub_modbus_config"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_modbus_config"."belong_sn" IS '归属sn';
COMMENT ON COLUMN "public"."labdatahub_modbus_config"."belong_type" IS '归属类型 0-产品 1-设备';
COMMENT ON COLUMN "public"."labdatahub_modbus_config"."code" IS '读取编码';
COMMENT ON COLUMN "public"."labdatahub_modbus_config"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_modbus_config"."register_range" IS '范围,逗号分隔如（1,2-5,7）';
COMMENT ON COLUMN "public"."labdatahub_modbus_config"."interval_time" IS '多少毫秒读取一次';
COMMENT ON COLUMN "public"."labdatahub_modbus_config"."delay_time" IS '同一网络组件读取属性延迟时间';
COMMENT ON TABLE "public"."labdatahub_modbus_config" IS 'modbus协议读取配置表';

-- ----------------------------
-- Records of labdatahub_modbus_config
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_product
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_product";
CREATE TABLE "public"."labdatahub_product" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "product_sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "product_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "link_method_id" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "link_method_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "protocol_id" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "protocol_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "device_count" int4 DEFAULT 0,
  "device_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "remark" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "create_by" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "update_by" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "status" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "timeout_seconds" int4 DEFAULT 10,
  "regular_cleaning" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "retention_time" int8 DEFAULT 1,
  "retention_unit" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "component_id" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "component_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "custom_config" text COLLATE "pg_catalog"."default",
  "group_code" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "group_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."labdatahub_product"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_product"."product_sn" IS '产品编码';
COMMENT ON COLUMN "public"."labdatahub_product"."product_name" IS '产品名称';
COMMENT ON COLUMN "public"."labdatahub_product"."link_method_id" IS '接入方式id';
COMMENT ON COLUMN "public"."labdatahub_product"."link_method_name" IS '接入方式名称';
COMMENT ON COLUMN "public"."labdatahub_product"."protocol_id" IS '协议id';
COMMENT ON COLUMN "public"."labdatahub_product"."protocol_name" IS '协议名称';
COMMENT ON COLUMN "public"."labdatahub_product"."device_count" IS '设备数量';
COMMENT ON COLUMN "public"."labdatahub_product"."device_type" IS '设备类型 0-直连设备 1-网关设备 2-无状态设备';
COMMENT ON COLUMN "public"."labdatahub_product"."remark" IS '备注';
COMMENT ON COLUMN "public"."labdatahub_product"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_product"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."labdatahub_product"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."labdatahub_product"."update_by" IS '修改人';
COMMENT ON COLUMN "public"."labdatahub_product"."status" IS '0-停用 1-启用';
COMMENT ON COLUMN "public"."labdatahub_product"."timeout_seconds" IS '心跳时间(秒)';
COMMENT ON COLUMN "public"."labdatahub_product"."regular_cleaning" IS '数据是否定期清理 0-否 1-是';
COMMENT ON COLUMN "public"."labdatahub_product"."retention_time" IS '数据保存时间';
COMMENT ON COLUMN "public"."labdatahub_product"."retention_unit" IS '数据保存时间单位 hour-时 day-天 week-周 month-月 year-年';
COMMENT ON COLUMN "public"."labdatahub_product"."component_id" IS '组件id';
COMMENT ON COLUMN "public"."labdatahub_product"."component_name" IS '组件名称';
COMMENT ON COLUMN "public"."labdatahub_product"."custom_config" IS '自定义配置';
COMMENT ON COLUMN "public"."labdatahub_product"."group_code" IS '分组CODE';
COMMENT ON COLUMN "public"."labdatahub_product"."group_name" IS '分组名称';
COMMENT ON TABLE "public"."labdatahub_product" IS '产品表';

-- ----------------------------
-- Records of labdatahub_product
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_properties
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_properties";
CREATE TABLE "public"."labdatahub_properties" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "belong_sn" varchar(50) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "belong_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "identifier" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "name" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "parent_id" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "data_type" varchar(50) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "sort_num" int4 DEFAULT 0,
  "from_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "remark" text COLLATE "pg_catalog"."default",
  "unit" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."labdatahub_properties"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_properties"."belong_sn" IS '归属id 产品/设备';
COMMENT ON COLUMN "public"."labdatahub_properties"."belong_type" IS '归属类型 0-产品 1-设备';
COMMENT ON COLUMN "public"."labdatahub_properties"."identifier" IS '属性标识符，如 temperature, status';
COMMENT ON COLUMN "public"."labdatahub_properties"."name" IS '属性名称';
COMMENT ON COLUMN "public"."labdatahub_properties"."parent_id" IS '父属性ID，用于构建嵌套结构。0表示根级属性';
COMMENT ON COLUMN "public"."labdatahub_properties"."data_type" IS '数据类型: int, double, bool, string, struct, array...';
COMMENT ON COLUMN "public"."labdatahub_properties"."sort_num" IS '排序';
COMMENT ON COLUMN "public"."labdatahub_properties"."from_type" IS '来源 0-产品继承 1-设备自定义(继承不可修改)';
COMMENT ON COLUMN "public"."labdatahub_properties"."remark" IS '备注';
COMMENT ON COLUMN "public"."labdatahub_properties"."unit" IS '单位';
COMMENT ON TABLE "public"."labdatahub_properties" IS '物模型属性定义表';

-- ----------------------------
-- Records of labdatahub_properties
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_protocol
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_protocol";
CREATE TABLE "public"."labdatahub_protocol" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "protocol_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "local_url" text COLLATE "pg_catalog"."default",
  "main_class_path" text COLLATE "pg_catalog"."default",
  "origin_name" text COLLATE "pg_catalog"."default",
  "type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "new_name" text COLLATE "pg_catalog"."default",
  "component_id" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "component_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamp(6),
  "create_by" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "update_time" timestamp(6),
  "update_by" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "status" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '1'::character varying,
  "remark" text COLLATE "pg_catalog"."default",
  "protocol_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."labdatahub_protocol"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_protocol"."protocol_name" IS '协议名称';
COMMENT ON COLUMN "public"."labdatahub_protocol"."local_url" IS '本地存储路径';
COMMENT ON COLUMN "public"."labdatahub_protocol"."main_class_path" IS '解析类入口';
COMMENT ON COLUMN "public"."labdatahub_protocol"."origin_name" IS '文件原始名字';
COMMENT ON COLUMN "public"."labdatahub_protocol"."type" IS '0-jar包';
COMMENT ON COLUMN "public"."labdatahub_protocol"."new_name" IS '文件重命名';
COMMENT ON COLUMN "public"."labdatahub_protocol"."component_id" IS '网络组件id';
COMMENT ON COLUMN "public"."labdatahub_protocol"."component_name" IS '网络组件名称';
COMMENT ON COLUMN "public"."labdatahub_protocol"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_protocol"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."labdatahub_protocol"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."labdatahub_protocol"."update_by" IS '修改人';
COMMENT ON COLUMN "public"."labdatahub_protocol"."status" IS '0-停用 1-启用';
COMMENT ON COLUMN "public"."labdatahub_protocol"."remark" IS '备注';
COMMENT ON COLUMN "public"."labdatahub_protocol"."protocol_type" IS '协议类型';
COMMENT ON TABLE "public"."labdatahub_protocol" IS '协议管理';

-- ----------------------------
-- Records of labdatahub_protocol
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_rule_engine
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_rule_engine";
CREATE TABLE "public"."labdatahub_rule_engine" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "engine_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "config_json" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "remark" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "is_enable" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying
)
;
COMMENT ON COLUMN "public"."labdatahub_rule_engine"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_rule_engine"."engine_name" IS '引擎名称';
COMMENT ON COLUMN "public"."labdatahub_rule_engine"."config_json" IS 'json配置';
COMMENT ON COLUMN "public"."labdatahub_rule_engine"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_rule_engine"."remark" IS '备注';
COMMENT ON COLUMN "public"."labdatahub_rule_engine"."is_enable" IS '0-停止 1-启用';
COMMENT ON TABLE "public"."labdatahub_rule_engine" IS '规则引擎配置';

-- ----------------------------
-- Records of labdatahub_rule_engine
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_scheduled_task
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_scheduled_task";
CREATE TABLE "public"."labdatahub_scheduled_task" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "rule_json" text COLLATE "pg_catalog"."default",
  "is_enable" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "execute_sn_list" text COLLATE "pg_catalog"."default",
  "execute_name_list" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "remark" text COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."labdatahub_scheduled_task"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_scheduled_task"."name" IS '配置名称';
COMMENT ON COLUMN "public"."labdatahub_scheduled_task"."rule_json" IS '规则json';
COMMENT ON COLUMN "public"."labdatahub_scheduled_task"."is_enable" IS '是否启用 0-否 1-是';
COMMENT ON COLUMN "public"."labdatahub_scheduled_task"."execute_sn_list" IS '执行动作设备列表';
COMMENT ON COLUMN "public"."labdatahub_scheduled_task"."execute_name_list" IS '执行动作设备名称';
COMMENT ON COLUMN "public"."labdatahub_scheduled_task"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_scheduled_task"."remark" IS '备注';
COMMENT ON TABLE "public"."labdatahub_scheduled_task" IS '定时引擎配置表';

-- ----------------------------
-- Records of labdatahub_scheduled_task
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_warn_config
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_warn_config";
CREATE TABLE "public"."labdatahub_warn_config" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "belong_sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "belong_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '1'::character varying,
  "rule_json" text COLLATE "pg_catalog"."default",
  "warn_message" text COLLATE "pg_catalog"."default",
  "warn_level" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '1'::character varying,
  "create_time" timestamp(6),
  "create_by" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "update_time" timestamp(6),
  "update_by" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "execute_action" text COLLATE "pg_catalog"."default",
  "is_enable" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "warn_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying
)
;
COMMENT ON COLUMN "public"."labdatahub_warn_config"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_warn_config"."name" IS '告警名称';
COMMENT ON COLUMN "public"."labdatahub_warn_config"."belong_sn" IS '产品/设备sn';
COMMENT ON COLUMN "public"."labdatahub_warn_config"."belong_type" IS '来源 0-产品 1-设备';
COMMENT ON COLUMN "public"."labdatahub_warn_config"."rule_json" IS '规则json';
COMMENT ON COLUMN "public"."labdatahub_warn_config"."warn_message" IS '告警消息模板';
COMMENT ON COLUMN "public"."labdatahub_warn_config"."warn_level" IS '告警等级 1-紧急 2-严重 3-警告 4-正常';
COMMENT ON COLUMN "public"."labdatahub_warn_config"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_warn_config"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."labdatahub_warn_config"."update_time" IS '修改时间';
COMMENT ON COLUMN "public"."labdatahub_warn_config"."update_by" IS '修改人';
COMMENT ON COLUMN "public"."labdatahub_warn_config"."execute_action" IS '执行动作json';
COMMENT ON COLUMN "public"."labdatahub_warn_config"."is_enable" IS '是否启用 0-否 1-是';
COMMENT ON COLUMN "public"."labdatahub_warn_config"."warn_type" IS '类型 0-属性 1-上线 2-下线';
COMMENT ON TABLE "public"."labdatahub_warn_config" IS '告警配置表';

-- ----------------------------
-- Records of labdatahub_warn_config
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_warn_linkage
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_warn_linkage";
CREATE TABLE "public"."labdatahub_warn_linkage" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "rule_json" text COLLATE "pg_catalog"."default",
  "warn_message" text COLLATE "pg_catalog"."default",
  "warn_level" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '1'::character varying,
  "is_enable" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "trigger_sn_list" text COLLATE "pg_catalog"."default",
  "trigger_name_list" text COLLATE "pg_catalog"."default",
  "execute_sn_list" text COLLATE "pg_catalog"."default",
  "execute_name_list" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "remark" text COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."labdatahub_warn_linkage"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_warn_linkage"."name" IS '配置名称';
COMMENT ON COLUMN "public"."labdatahub_warn_linkage"."rule_json" IS '规则json';
COMMENT ON COLUMN "public"."labdatahub_warn_linkage"."warn_message" IS '告警消息模板';
COMMENT ON COLUMN "public"."labdatahub_warn_linkage"."warn_level" IS '告警等级 1-紧急 2-严重 3-警告 4-正常';
COMMENT ON COLUMN "public"."labdatahub_warn_linkage"."is_enable" IS '是否启用 0-否 1-是';
COMMENT ON COLUMN "public"."labdatahub_warn_linkage"."trigger_sn_list" IS '触发设备列表';
COMMENT ON COLUMN "public"."labdatahub_warn_linkage"."trigger_name_list" IS '触发设备名称';
COMMENT ON COLUMN "public"."labdatahub_warn_linkage"."execute_sn_list" IS '执行动作设备列表';
COMMENT ON COLUMN "public"."labdatahub_warn_linkage"."execute_name_list" IS '执行动作设备名称';
COMMENT ON COLUMN "public"."labdatahub_warn_linkage"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_warn_linkage"."remark" IS '备注';
COMMENT ON TABLE "public"."labdatahub_warn_linkage" IS '设备联动告警';

-- ----------------------------
-- Records of labdatahub_warn_linkage
-- ----------------------------

-- ----------------------------
-- Table structure for labdatahub_warn_record
-- ----------------------------
DROP TABLE IF EXISTS "public"."labdatahub_warn_record";
CREATE TABLE "public"."labdatahub_warn_record" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "config_id" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "config_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "warn_message" text COLLATE "pg_catalog"."default",
  "warn_data" text COLLATE "pg_catalog"."default",
  "belong_sn" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_time" timestamp(6),
  "warn_level" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '1'::character varying,
  "status" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying,
  "warn_type" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '0'::character varying
)
;
COMMENT ON COLUMN "public"."labdatahub_warn_record"."id" IS 'id';
COMMENT ON COLUMN "public"."labdatahub_warn_record"."config_id" IS '告警配置id';
COMMENT ON COLUMN "public"."labdatahub_warn_record"."config_name" IS '告警配置名称';
COMMENT ON COLUMN "public"."labdatahub_warn_record"."warn_message" IS '告警内容';
COMMENT ON COLUMN "public"."labdatahub_warn_record"."warn_data" IS '告警时全属性数据';
COMMENT ON COLUMN "public"."labdatahub_warn_record"."belong_sn" IS '设备/产品sn';
COMMENT ON COLUMN "public"."labdatahub_warn_record"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."labdatahub_warn_record"."warn_level" IS '告警等级 1-紧急 2-严重 3-一般 4-警告 5-正常';
COMMENT ON COLUMN "public"."labdatahub_warn_record"."status" IS '0-未处理 1-已处理';
COMMENT ON COLUMN "public"."labdatahub_warn_record"."warn_type" IS '类型 0-属性 1-上线 2-下线';
COMMENT ON TABLE "public"."labdatahub_warn_record" IS '告警记录表';

-- ----------------------------
-- Records of labdatahub_warn_record
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."gen_table_column_column_id_seq"
OWNED BY "public"."gen_table_column"."column_id";
SELECT setval('"public"."gen_table_column_column_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."gen_table_table_id_seq"
OWNED BY "public"."gen_table"."table_id";
SELECT setval('"public"."gen_table_table_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_config_config_id_seq"
OWNED BY "public"."sys_config"."config_id";
SELECT setval('"public"."sys_config_config_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_dept_dept_id_seq"
OWNED BY "public"."sys_dept"."dept_id";
SELECT setval('"public"."sys_dept_dept_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_dict_data_dict_code_seq"
OWNED BY "public"."sys_dict_data"."dict_code";
SELECT setval('"public"."sys_dict_data_dict_code_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_dict_type_dict_id_seq"
OWNED BY "public"."sys_dict_type"."dict_id";
SELECT setval('"public"."sys_dict_type_dict_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_job_job_id_seq"
OWNED BY "public"."sys_job"."job_id";
SELECT setval('"public"."sys_job_job_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_job_log_job_log_id_seq"
OWNED BY "public"."sys_job_log"."job_log_id";
SELECT setval('"public"."sys_job_log_job_log_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_logininfor_info_id_seq"
OWNED BY "public"."sys_logininfor"."info_id";
SELECT setval('"public"."sys_logininfor_info_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_menu_menu_id_seq"
OWNED BY "public"."sys_menu"."menu_id";
SELECT setval('"public"."sys_menu_menu_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_notice_notice_id_seq"
OWNED BY "public"."sys_notice"."notice_id";
SELECT setval('"public"."sys_notice_notice_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_oper_log_oper_id_seq"
OWNED BY "public"."sys_oper_log"."oper_id";
SELECT setval('"public"."sys_oper_log_oper_id_seq"', 9, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_post_post_id_seq"
OWNED BY "public"."sys_post"."post_id";
SELECT setval('"public"."sys_post_post_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_role_role_id_seq"
OWNED BY "public"."sys_role"."role_id";
SELECT setval('"public"."sys_role_role_id_seq"', 2, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_user_user_id_seq"
OWNED BY "public"."sys_user"."user_id";
SELECT setval('"public"."sys_user_user_id_seq"', 2, true);

-- ----------------------------
-- Primary Key structure for table gen_table
-- ----------------------------
ALTER TABLE "public"."gen_table" ADD CONSTRAINT "gen_table_pkey" PRIMARY KEY ("table_id");

-- ----------------------------
-- Primary Key structure for table gen_table_column
-- ----------------------------
ALTER TABLE "public"."gen_table_column" ADD CONSTRAINT "gen_table_column_pkey" PRIMARY KEY ("column_id");

-- ----------------------------
-- Primary Key structure for table qrtz_blob_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_blob_triggers" ADD CONSTRAINT "qrtz_blob_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Primary Key structure for table qrtz_calendars
-- ----------------------------
ALTER TABLE "public"."qrtz_calendars" ADD CONSTRAINT "qrtz_calendars_pkey" PRIMARY KEY ("sched_name", "calendar_name");

-- ----------------------------
-- Primary Key structure for table qrtz_cron_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_cron_triggers" ADD CONSTRAINT "qrtz_cron_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Primary Key structure for table qrtz_fired_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_fired_triggers" ADD CONSTRAINT "qrtz_fired_triggers_pkey" PRIMARY KEY ("sched_name", "entry_id");

-- ----------------------------
-- Primary Key structure for table qrtz_job_details
-- ----------------------------
ALTER TABLE "public"."qrtz_job_details" ADD CONSTRAINT "qrtz_job_details_pkey" PRIMARY KEY ("sched_name", "job_name", "job_group");

-- ----------------------------
-- Primary Key structure for table qrtz_locks
-- ----------------------------
ALTER TABLE "public"."qrtz_locks" ADD CONSTRAINT "qrtz_locks_pkey" PRIMARY KEY ("sched_name", "lock_name");

-- ----------------------------
-- Primary Key structure for table qrtz_paused_trigger_grps
-- ----------------------------
ALTER TABLE "public"."qrtz_paused_trigger_grps" ADD CONSTRAINT "qrtz_paused_trigger_grps_pkey" PRIMARY KEY ("sched_name", "trigger_group");

-- ----------------------------
-- Primary Key structure for table qrtz_scheduler_state
-- ----------------------------
ALTER TABLE "public"."qrtz_scheduler_state" ADD CONSTRAINT "qrtz_scheduler_state_pkey" PRIMARY KEY ("sched_name", "instance_name");

-- ----------------------------
-- Primary Key structure for table qrtz_simple_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_simple_triggers" ADD CONSTRAINT "qrtz_simple_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Primary Key structure for table qrtz_simprop_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_simprop_triggers" ADD CONSTRAINT "qrtz_simprop_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Indexes structure for table qrtz_triggers
-- ----------------------------
CREATE INDEX "idx_qrtz_triggers_job" ON "public"."qrtz_triggers" USING btree (
  "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "job_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "job_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table qrtz_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_triggers" ADD CONSTRAINT "qrtz_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Primary Key structure for table sys_config
-- ----------------------------
ALTER TABLE "public"."sys_config" ADD CONSTRAINT "sys_config_pkey" PRIMARY KEY ("config_id");

-- ----------------------------
-- Primary Key structure for table sys_dept
-- ----------------------------
ALTER TABLE "public"."sys_dept" ADD CONSTRAINT "sys_dept_pkey" PRIMARY KEY ("dept_id");

-- ----------------------------
-- Primary Key structure for table sys_dict_data
-- ----------------------------
ALTER TABLE "public"."sys_dict_data" ADD CONSTRAINT "sys_dict_data_pkey" PRIMARY KEY ("dict_code");

-- ----------------------------
-- Indexes structure for table sys_dict_type
-- ----------------------------
CREATE UNIQUE INDEX "uniq_dict_type" ON "public"."sys_dict_type" USING btree (
  "dict_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table sys_dict_type
-- ----------------------------
ALTER TABLE "public"."sys_dict_type" ADD CONSTRAINT "sys_dict_type_pkey" PRIMARY KEY ("dict_id");

-- ----------------------------
-- Primary Key structure for table sys_job
-- ----------------------------
ALTER TABLE "public"."sys_job" ADD CONSTRAINT "sys_job_pkey" PRIMARY KEY ("job_id");

-- ----------------------------
-- Primary Key structure for table sys_job_log
-- ----------------------------
ALTER TABLE "public"."sys_job_log" ADD CONSTRAINT "sys_job_log_pkey" PRIMARY KEY ("job_log_id");

-- ----------------------------
-- Indexes structure for table sys_logininfor
-- ----------------------------
CREATE INDEX "idx_sys_logininfor_lt" ON "public"."sys_logininfor" USING btree (
  "login_time" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_sys_logininfor_s" ON "public"."sys_logininfor" USING btree (
  "status" COLLATE "pg_catalog"."default" "pg_catalog"."bpchar_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table sys_logininfor
-- ----------------------------
ALTER TABLE "public"."sys_logininfor" ADD CONSTRAINT "sys_logininfor_pkey" PRIMARY KEY ("info_id");

-- ----------------------------
-- Primary Key structure for table sys_menu
-- ----------------------------
ALTER TABLE "public"."sys_menu" ADD CONSTRAINT "sys_menu_pkey" PRIMARY KEY ("menu_id");

-- ----------------------------
-- Primary Key structure for table sys_notice
-- ----------------------------
ALTER TABLE "public"."sys_notice" ADD CONSTRAINT "sys_notice_pkey" PRIMARY KEY ("notice_id");

-- ----------------------------
-- Indexes structure for table sys_oper_log
-- ----------------------------
CREATE INDEX "idx_sys_oper_log_bt" ON "public"."sys_oper_log" USING btree (
  "business_type" "pg_catalog"."int4_ops" ASC NULLS LAST
);
CREATE INDEX "idx_sys_oper_log_ot" ON "public"."sys_oper_log" USING btree (
  "oper_time" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_sys_oper_log_s" ON "public"."sys_oper_log" USING btree (
  "status" "pg_catalog"."int4_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table sys_oper_log
-- ----------------------------
ALTER TABLE "public"."sys_oper_log" ADD CONSTRAINT "sys_oper_log_pkey" PRIMARY KEY ("oper_id");

-- ----------------------------
-- Primary Key structure for table sys_post
-- ----------------------------
ALTER TABLE "public"."sys_post" ADD CONSTRAINT "sys_post_pkey" PRIMARY KEY ("post_id");

-- ----------------------------
-- Primary Key structure for table sys_role
-- ----------------------------
ALTER TABLE "public"."sys_role" ADD CONSTRAINT "sys_role_pkey" PRIMARY KEY ("role_id");

-- ----------------------------
-- Primary Key structure for table sys_role_dept
-- ----------------------------
ALTER TABLE "public"."sys_role_dept" ADD CONSTRAINT "sys_role_dept_pkey" PRIMARY KEY ("role_id", "dept_id");

-- ----------------------------
-- Primary Key structure for table sys_role_menu
-- ----------------------------
ALTER TABLE "public"."sys_role_menu" ADD CONSTRAINT "sys_role_menu_pkey" PRIMARY KEY ("role_id", "menu_id");

-- ----------------------------
-- Primary Key structure for table sys_user
-- ----------------------------
ALTER TABLE "public"."sys_user" ADD CONSTRAINT "sys_user_pkey" PRIMARY KEY ("user_id");

-- ----------------------------
-- Primary Key structure for table sys_user_post
-- ----------------------------
ALTER TABLE "public"."sys_user_post" ADD CONSTRAINT "sys_user_post_pkey" PRIMARY KEY ("user_id", "post_id");

-- ----------------------------
-- Primary Key structure for table sys_user_role
-- ----------------------------
ALTER TABLE "public"."sys_user_role" ADD CONSTRAINT "sys_user_role_pkey" PRIMARY KEY ("user_id", "role_id");

-- ----------------------------
-- Primary Key structure for table labdatahub_component
-- ----------------------------
ALTER TABLE "public"."labdatahub_component" ADD CONSTRAINT "labdatahub_component_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table labdatahub_device
-- ----------------------------
CREATE UNIQUE INDEX "uniq_device_sn" ON "public"."labdatahub_device" USING btree (
  "device_sn" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table labdatahub_device
-- ----------------------------
ALTER TABLE "public"."labdatahub_device" ADD CONSTRAINT "labdatahub_device_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table labdatahub_device_group
-- ----------------------------
CREATE UNIQUE INDEX "uniq_group_code" ON "public"."labdatahub_device_group" USING btree (
  "group_code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table labdatahub_device_group
-- ----------------------------
ALTER TABLE "public"."labdatahub_device_group" ADD CONSTRAINT "labdatahub_device_group_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table labdatahub_device_logs
-- ----------------------------
CREATE INDEX "idx_device_sn" ON "public"."labdatahub_device_logs" USING btree (
  "device_sn" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table labdatahub_device_logs
-- ----------------------------
ALTER TABLE "public"."labdatahub_device_logs" ADD CONSTRAINT "labdatahub_device_logs_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table labdatahub_function
-- ----------------------------
CREATE INDEX "idx_belong_sn" ON "public"."labdatahub_function" USING btree (
  "belong_sn" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table labdatahub_function
-- ----------------------------
ALTER TABLE "public"."labdatahub_function" ADD CONSTRAINT "labdatahub_function_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table labdatahub_function_record
-- ----------------------------
CREATE INDEX "idx_func_record_device_sn" ON "public"."labdatahub_function_record" USING btree (
  "device_sn" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table labdatahub_function_record
-- ----------------------------
ALTER TABLE "public"."labdatahub_function_record" ADD CONSTRAINT "labdatahub_function_record_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table labdatahub_linkage_action_record
-- ----------------------------
ALTER TABLE "public"."labdatahub_linkage_action_record" ADD CONSTRAINT "labdatahub_linkage_action_record_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table labdatahub_linkage_warn_record
-- ----------------------------
ALTER TABLE "public"."labdatahub_linkage_warn_record" ADD CONSTRAINT "labdatahub_linkage_warn_record_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table labdatahub_media_device
-- ----------------------------
ALTER TABLE "public"."labdatahub_media_device" ADD CONSTRAINT "labdatahub_media_device_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table labdatahub_media_server
-- ----------------------------
ALTER TABLE "public"."labdatahub_media_server" ADD CONSTRAINT "labdatahub_media_server_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table labdatahub_modbus_config
-- ----------------------------
ALTER TABLE "public"."labdatahub_modbus_config" ADD CONSTRAINT "labdatahub_modbus_config_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table labdatahub_product
-- ----------------------------
CREATE UNIQUE INDEX "uniq_product_sn" ON "public"."labdatahub_product" USING btree (
  "product_sn" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table labdatahub_product
-- ----------------------------
ALTER TABLE "public"."labdatahub_product" ADD CONSTRAINT "labdatahub_product_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table labdatahub_properties
-- ----------------------------
CREATE INDEX "idx_belong_sn_properties" ON "public"."labdatahub_properties" USING btree (
  "belong_sn" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_identifier" ON "public"."labdatahub_properties" USING btree (
  "identifier" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table labdatahub_properties
-- ----------------------------
ALTER TABLE "public"."labdatahub_properties" ADD CONSTRAINT "labdatahub_properties_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table labdatahub_protocol
-- ----------------------------
ALTER TABLE "public"."labdatahub_protocol" ADD CONSTRAINT "labdatahub_protocol_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table labdatahub_rule_engine
-- ----------------------------
ALTER TABLE "public"."labdatahub_rule_engine" ADD CONSTRAINT "labdatahub_rule_engine_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table labdatahub_scheduled_task
-- ----------------------------
ALTER TABLE "public"."labdatahub_scheduled_task" ADD CONSTRAINT "labdatahub_scheduled_task_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table labdatahub_warn_config
-- ----------------------------
ALTER TABLE "public"."labdatahub_warn_config" ADD CONSTRAINT "labdatahub_warn_config_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table labdatahub_warn_linkage
-- ----------------------------
ALTER TABLE "public"."labdatahub_warn_linkage" ADD CONSTRAINT "labdatahub_warn_linkage_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table labdatahub_warn_record
-- ----------------------------
CREATE INDEX "idx_warn_record_belong_sn" ON "public"."labdatahub_warn_record" USING btree (
  "belong_sn" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table labdatahub_warn_record
-- ----------------------------
ALTER TABLE "public"."labdatahub_warn_record" ADD CONSTRAINT "labdatahub_warn_record_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table qrtz_blob_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_blob_triggers" ADD CONSTRAINT "qrtz_blob_triggers_fk" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "public"."qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_cron_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_cron_triggers" ADD CONSTRAINT "qrtz_cron_triggers_fk" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "public"."qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_simple_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_simple_triggers" ADD CONSTRAINT "qrtz_simple_triggers_fk" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "public"."qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_simprop_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_simprop_triggers" ADD CONSTRAINT "qrtz_simprop_triggers_fk" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "public"."qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_triggers
-- ----------------------------
ALTER TABLE "public"."qrtz_triggers" ADD CONSTRAINT "qrtz_triggers_job_details_fk" FOREIGN KEY ("sched_name", "job_name", "job_group") REFERENCES "public"."qrtz_job_details" ("sched_name", "job_name", "job_group") ON DELETE NO ACTION ON UPDATE NO ACTION;


SELECT setval('gen_table_column_column_id_seq', GREATEST(10000, COALESCE((SELECT MAX(column_id) FROM gen_table_column), 10000)));
SELECT setval('gen_table_table_id_seq', GREATEST(10000, COALESCE((SELECT MAX(table_id) FROM gen_table), 10000)));
SELECT setval('sys_config_config_id_seq', GREATEST(10000, COALESCE((SELECT MAX(config_id) FROM sys_config), 10000)));
SELECT setval('sys_dept_dept_id_seq', GREATEST(10000, COALESCE((SELECT MAX(dept_id) FROM sys_dept), 10000)));
SELECT setval('sys_dict_data_dict_code_seq', GREATEST(10000, COALESCE((SELECT MAX(dict_code) FROM sys_dict_data), 10000)));
SELECT setval('sys_dict_type_dict_id_seq', GREATEST(10000, COALESCE((SELECT MAX(dict_id) FROM sys_dict_type), 10000)));
SELECT setval('sys_job_job_id_seq', GREATEST(10000, COALESCE((SELECT MAX(job_id) FROM sys_job), 10000)));
SELECT setval('sys_job_log_job_log_id_seq', GREATEST(10000, COALESCE((SELECT MAX(job_log_id) FROM sys_job_log), 10000)));
SELECT setval('sys_logininfor_info_id_seq', GREATEST(10000, COALESCE((SELECT MAX(info_id) FROM sys_logininfor), 10000)));
SELECT setval('sys_menu_menu_id_seq', GREATEST(10000, COALESCE((SELECT MAX(menu_id) FROM sys_menu), 10000)));
SELECT setval('sys_notice_notice_id_seq', GREATEST(10000, COALESCE((SELECT MAX(notice_id) FROM sys_notice), 10000)));
SELECT setval('sys_oper_log_oper_id_seq', GREATEST(10000, COALESCE((SELECT MAX(oper_id) FROM sys_oper_log), 10000)));
SELECT setval('sys_post_post_id_seq', GREATEST(10000, COALESCE((SELECT MAX(post_id) FROM sys_post), 10000)));
SELECT setval('sys_role_role_id_seq', GREATEST(10000, COALESCE((SELECT MAX(role_id) FROM sys_role), 10000)));
SELECT setval('sys_user_user_id_seq', GREATEST(10000, COALESCE((SELECT MAX(user_id) FROM sys_user), 10000)));