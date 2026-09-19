-- ============================================================================
-- lab-data-hub 平台初始化脚本（全量结构 + 框架初始数据）
-- ----------------------------------------------------------------------------
-- 生成方式：从本机在跑的 sc_1.0.0 库 pg_dump 导出（PostgreSQL 15.19），非手写，
--           结构与运行库完全一致。生成日期 2026-09-19。
--
-- 【执行顺序】全新部署只跑前两个：
--   1. init.sql   —— 本文件：全量结构 + 框架初始数据
--   2. db.sql     —— TimescaleDB 扩展安装 + labdatahub_device_logs 转超表
--
--   ⚠️ others/ 下的增量脚本 【全新部署不要跑】
--      它们建的表与加的列已全部包含在本文件里，只用于【老库增量升级】。
--      部分脚本（如 mitsubishi_mc3e_config.sql）内部带 DROP TABLE IF EXISTS，
--      新装库跑它会把刚建好的 brother / fanuc / mitsubishi_cnc /
--      mitsubishi_mc3e 四张配置表删掉重建。
--      （原 protocol_config_all.sql 整合脚本已于 2026-09-19 删除 —— 它的内容
--        100% 被本文件覆盖，且同样带 DROP TABLE，留着容易被误当成部署步骤。）
--
-- 【数据结构】全量，57 张表，与运行库一致。含 TimescaleDB 超表
--   labdatahub_device_logs 的建表语句（此处是普通表，由 db.sql 转超表）。
--
-- 【数据范围】
--   有数据：RuoYi 框架自身 —— 菜单 / 字典 / 参数 / 部门 / 岗位 / 角色 / 用户 /
--           角色菜单关联 / 定时任务 / 通知公告，以及代码生成器元数据
--           （gen_table / gen_table_column）。
--   只建表、无数据：
--     · 全部 labdatahub_* 业务表 —— 设备 / 组件 / 产品 / 协议 / 各协议配置 /
--       物模型属性等，由平台自行配置。
--     · 全部日志与记录表 —— labdatahub_device_logs、labdatahub_point_write_record、
--       labdatahub_function_record、labdatahub_linkage_action_record、
--       labdatahub_linkage_warn_record、labdatahub_warn_record、sys_oper_log、
--       sys_logininfor、sys_job_log。
--     · qrtz_* 调度运行时表（quartz 自行管理）。
--
--   ⚠️ 这些日志表刻意保留建表语句、只清数据：db.sql 的 create_hypertable 与
--      运行期写入都依赖表存在，连表删掉会让部署脚本直接失败。
--
-- 【依赖】本文件含 CREATE EXTENSION IF NOT EXISTS timescaledb，
--         目标 PostgreSQL 需已安装 timescaledb（本机版本 2.27.2）。
-- ============================================================================

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: timescaledb; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS timescaledb WITH SCHEMA public;


--
-- Name: EXTENSION timescaledb; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION timescaledb IS 'Enables scalable inserts and complex queries for time-series data (Community Edition)';


--
-- Name: find_in_set(bigint, text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.find_in_set(str bigint, strlist text) RETURNS boolean
    LANGUAGE plpgsql IMMUTABLE
    AS $$
BEGIN
    RETURN str::text = ANY(string_to_array(strlist, ','));
END;
$$;


--
-- Name: find_in_set(text, text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.find_in_set(str text, strlist text) RETURNS boolean
    LANGUAGE plpgsql IMMUTABLE
    AS $$
BEGIN
    RETURN str = ANY(string_to_array(strlist, ','));
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: labdatahub_device_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_device_logs (
    id bigint NOT NULL,
    device_sn character varying(255) DEFAULT NULL::character varying NOT NULL,
    report_time timestamp(6) without time zone,
    properties text,
    create_time timestamp(6) without time zone NOT NULL,
    log_type character varying(255) DEFAULT NULL::character varying
);


--
-- Name: TABLE labdatahub_device_logs; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_device_logs IS '设备日志表';


--
-- Name: COLUMN labdatahub_device_logs.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device_logs.id IS 'id';


--
-- Name: COLUMN labdatahub_device_logs.device_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device_logs.device_sn IS '设备sn';


--
-- Name: COLUMN labdatahub_device_logs.report_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device_logs.report_time IS '上报时间';


--
-- Name: COLUMN labdatahub_device_logs.properties; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device_logs.properties IS '属性json';


--
-- Name: COLUMN labdatahub_device_logs.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device_logs.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_device_logs.log_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device_logs.log_type IS '日志类型  PROPERTY-上行消息 OFFLINE-设备离线 ONLINE-设备上线';


--
-- Name: gen_table; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gen_table (
    table_id bigint NOT NULL,
    table_name character varying(200) DEFAULT ''::character varying,
    table_comment character varying(500) DEFAULT ''::character varying,
    sub_table_name character varying(64) DEFAULT NULL::character varying,
    sub_table_fk_name character varying(64) DEFAULT NULL::character varying,
    class_name character varying(100) DEFAULT ''::character varying,
    tpl_category character varying(200) DEFAULT 'crud'::character varying,
    tpl_web_type character varying(30) DEFAULT ''::character varying,
    package_name character varying(100) DEFAULT NULL::character varying,
    module_name character varying(30) DEFAULT NULL::character varying,
    business_name character varying(30) DEFAULT NULL::character varying,
    function_name character varying(50) DEFAULT NULL::character varying,
    function_author character varying(50) DEFAULT NULL::character varying,
    gen_type character(1) DEFAULT '0'::bpchar,
    gen_path character varying(200) DEFAULT '/'::character varying,
    options character varying(1000) DEFAULT NULL::character varying,
    create_by character varying(64) DEFAULT ''::character varying,
    create_time timestamp(6) without time zone,
    update_by character varying(64) DEFAULT ''::character varying,
    update_time timestamp(6) without time zone,
    remark character varying(500) DEFAULT NULL::character varying
);


--
-- Name: TABLE gen_table; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.gen_table IS '代码生成业务表';


--
-- Name: COLUMN gen_table.table_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.table_id IS '编号';


--
-- Name: COLUMN gen_table.table_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.table_name IS '表名称';


--
-- Name: COLUMN gen_table.table_comment; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.table_comment IS '表描述';


--
-- Name: COLUMN gen_table.sub_table_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.sub_table_name IS '关联子表的表名';


--
-- Name: COLUMN gen_table.sub_table_fk_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.sub_table_fk_name IS '子表关联的外键名';


--
-- Name: COLUMN gen_table.class_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.class_name IS '实体类名称';


--
-- Name: COLUMN gen_table.tpl_category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.tpl_category IS '使用的模板（crud单表操作 tree树表操作）';


--
-- Name: COLUMN gen_table.tpl_web_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.tpl_web_type IS '前端模板类型（element-ui模版 element-plus模版）';


--
-- Name: COLUMN gen_table.package_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.package_name IS '生成包路径';


--
-- Name: COLUMN gen_table.module_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.module_name IS '生成模块名';


--
-- Name: COLUMN gen_table.business_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.business_name IS '生成业务名';


--
-- Name: COLUMN gen_table.function_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.function_name IS '生成功能名';


--
-- Name: COLUMN gen_table.function_author; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.function_author IS '生成功能作者';


--
-- Name: COLUMN gen_table.gen_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.gen_type IS '生成代码方式（0zip压缩包 1自定义路径）';


--
-- Name: COLUMN gen_table.gen_path; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.gen_path IS '生成路径（不填默认项目路径）';


--
-- Name: COLUMN gen_table.options; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.options IS '其它生成选项';


--
-- Name: COLUMN gen_table.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.create_by IS '创建者';


--
-- Name: COLUMN gen_table.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.create_time IS '创建时间';


--
-- Name: COLUMN gen_table.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.update_by IS '更新者';


--
-- Name: COLUMN gen_table.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.update_time IS '更新时间';


--
-- Name: COLUMN gen_table.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table.remark IS '备注';


--
-- Name: gen_table_column; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gen_table_column (
    column_id bigint NOT NULL,
    table_id bigint,
    column_name character varying(200) DEFAULT NULL::character varying,
    column_comment character varying(500) DEFAULT NULL::character varying,
    column_type character varying(100) DEFAULT NULL::character varying,
    java_type character varying(500) DEFAULT NULL::character varying,
    java_field character varying(200) DEFAULT NULL::character varying,
    is_pk character(1) DEFAULT NULL::bpchar,
    is_increment character(1) DEFAULT NULL::bpchar,
    is_required character(1) DEFAULT NULL::bpchar,
    is_insert character(1) DEFAULT NULL::bpchar,
    is_edit character(1) DEFAULT NULL::bpchar,
    is_list character(1) DEFAULT NULL::bpchar,
    is_query character(1) DEFAULT NULL::bpchar,
    query_type character varying(200) DEFAULT 'EQ'::character varying,
    html_type character varying(200) DEFAULT NULL::character varying,
    dict_type character varying(200) DEFAULT ''::character varying,
    sort integer,
    create_by character varying(64) DEFAULT ''::character varying,
    create_time timestamp(6) without time zone,
    update_by character varying(64) DEFAULT ''::character varying,
    update_time timestamp(6) without time zone
);


--
-- Name: TABLE gen_table_column; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.gen_table_column IS '代码生成业务表字段';


--
-- Name: COLUMN gen_table_column.column_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.column_id IS '编号';


--
-- Name: COLUMN gen_table_column.table_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.table_id IS '归属表编号';


--
-- Name: COLUMN gen_table_column.column_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.column_name IS '列名称';


--
-- Name: COLUMN gen_table_column.column_comment; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.column_comment IS '列描述';


--
-- Name: COLUMN gen_table_column.column_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.column_type IS '列类型';


--
-- Name: COLUMN gen_table_column.java_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.java_type IS 'JAVA类型';


--
-- Name: COLUMN gen_table_column.java_field; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.java_field IS 'JAVA字段名';


--
-- Name: COLUMN gen_table_column.is_pk; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.is_pk IS '是否主键（1是）';


--
-- Name: COLUMN gen_table_column.is_increment; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.is_increment IS '是否自增（1是）';


--
-- Name: COLUMN gen_table_column.is_required; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.is_required IS '是否必填（1是）';


--
-- Name: COLUMN gen_table_column.is_insert; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.is_insert IS '是否为插入字段（1是）';


--
-- Name: COLUMN gen_table_column.is_edit; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.is_edit IS '是否编辑字段（1是）';


--
-- Name: COLUMN gen_table_column.is_list; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.is_list IS '是否列表字段（1是）';


--
-- Name: COLUMN gen_table_column.is_query; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.is_query IS '是否查询字段（1是）';


--
-- Name: COLUMN gen_table_column.query_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.query_type IS '查询方式（等于、不等于、大于、小于、范围）';


--
-- Name: COLUMN gen_table_column.html_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.html_type IS '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）';


--
-- Name: COLUMN gen_table_column.dict_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.dict_type IS '字典类型';


--
-- Name: COLUMN gen_table_column.sort; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.sort IS '排序';


--
-- Name: COLUMN gen_table_column.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.create_by IS '创建者';


--
-- Name: COLUMN gen_table_column.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.create_time IS '创建时间';


--
-- Name: COLUMN gen_table_column.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.update_by IS '更新者';


--
-- Name: COLUMN gen_table_column.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gen_table_column.update_time IS '更新时间';


--
-- Name: gen_table_column_column_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gen_table_column_column_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gen_table_column_column_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gen_table_column_column_id_seq OWNED BY public.gen_table_column.column_id;


--
-- Name: gen_table_table_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gen_table_table_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gen_table_table_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gen_table_table_id_seq OWNED BY public.gen_table.table_id;


--
-- Name: labdatahub_brother_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_brother_config (
    id character varying(255) NOT NULL,
    belong_sn character varying(255) DEFAULT NULL::character varying,
    belong_type character varying(255) DEFAULT '0'::character varying,
    code character varying(255) DEFAULT NULL::character varying,
    create_time timestamp(6) without time zone,
    data_area character varying(64) DEFAULT NULL::character varying,
    row_number numeric(10,0),
    field_index numeric(10,0),
    interval_time integer DEFAULT 1,
    delay_time integer DEFAULT 0,
    name character varying(100)
);


--
-- Name: TABLE labdatahub_brother_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_brother_config IS 'Brother NC协议读取配置表';


--
-- Name: COLUMN labdatahub_brother_config.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_brother_config.id IS 'id';


--
-- Name: COLUMN labdatahub_brother_config.belong_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_brother_config.belong_sn IS '归属sn';


--
-- Name: COLUMN labdatahub_brother_config.belong_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_brother_config.belong_type IS '归属类型 0-产品 1-设备';


--
-- Name: COLUMN labdatahub_brother_config.code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_brother_config.code IS '读取编码';


--
-- Name: COLUMN labdatahub_brother_config.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_brother_config.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_brother_config.data_area; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_brother_config.data_area IS '数据区名（PDSP/ALARM/PRD3/WKCNTR）';


--
-- Name: COLUMN labdatahub_brother_config.row_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_brother_config.row_number IS '行号（1起，对应数据区点表行顺序）';


--
-- Name: COLUMN labdatahub_brother_config.field_index; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_brother_config.field_index IS '字段序号（1起，第1个字段=行Symbol后第一个值）';


--
-- Name: COLUMN labdatahub_brother_config.interval_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_brother_config.interval_time IS '多少毫秒读取一次';


--
-- Name: COLUMN labdatahub_brother_config.delay_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_brother_config.delay_time IS '同一网络组件读取属性延迟时间';


--
-- Name: COLUMN labdatahub_brother_config.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_brother_config.name IS '点位名称（物模型属性名用，null 用标识 code）';


--
-- Name: labdatahub_component; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_component (
    id character varying(255) NOT NULL,
    name character varying(255) DEFAULT NULL::character varying,
    net_type character varying(255) DEFAULT NULL::character varying,
    ip_addr character varying(255) DEFAULT NULL::character varying,
    port character varying(255) DEFAULT NULL::character varying,
    open_tls character varying(255) DEFAULT NULL::character varying,
    remark text,
    create_time timestamp(6) without time zone,
    create_by character varying(255) DEFAULT NULL::character varying,
    update_time timestamp(6) without time zone,
    update_by character varying(255) DEFAULT NULL::character varying,
    status character varying(255) DEFAULT NULL::character varying,
    other_config text,
    protocol_id text,
    protocol_name text
);


--
-- Name: TABLE labdatahub_component; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_component IS '网络组件';


--
-- Name: COLUMN labdatahub_component.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_component.id IS 'id';


--
-- Name: COLUMN labdatahub_component.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_component.name IS '组件名称';


--
-- Name: COLUMN labdatahub_component.net_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_component.net_type IS '网络类型';


--
-- Name: COLUMN labdatahub_component.ip_addr; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_component.ip_addr IS 'IP地址';


--
-- Name: COLUMN labdatahub_component.port; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_component.port IS '端口';


--
-- Name: COLUMN labdatahub_component.open_tls; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_component.open_tls IS '是否开启TLS (0-否 1-是)';


--
-- Name: COLUMN labdatahub_component.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_component.remark IS '备注';


--
-- Name: COLUMN labdatahub_component.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_component.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_component.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_component.create_by IS '创建人';


--
-- Name: COLUMN labdatahub_component.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_component.update_time IS '修改时间';


--
-- Name: COLUMN labdatahub_component.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_component.update_by IS '修改人';


--
-- Name: COLUMN labdatahub_component.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_component.status IS '0-停用 1-启用';


--
-- Name: COLUMN labdatahub_component.other_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_component.other_config IS '其余配置(如账号密码等)';


--
-- Name: COLUMN labdatahub_component.protocol_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_component.protocol_id IS '协议id';


--
-- Name: COLUMN labdatahub_component.protocol_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_component.protocol_name IS '协议名称';


--
-- Name: labdatahub_db_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_db_config (
    id character varying(255) NOT NULL,
    belong_sn character varying(255) DEFAULT NULL::character varying,
    belong_type character varying(255) DEFAULT '0'::character varying,
    code character varying(255) DEFAULT NULL::character varying,
    create_time timestamp(6) without time zone,
    interval_time integer DEFAULT 1,
    delay_time integer DEFAULT 0,
    field_type character varying(255),
    field_comment character varying(255),
    field_length character varying(255)
);


--
-- Name: TABLE labdatahub_db_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_db_config IS 'modbus协议读取配置表';


--
-- Name: COLUMN labdatahub_db_config.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_db_config.id IS 'id';


--
-- Name: COLUMN labdatahub_db_config.belong_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_db_config.belong_sn IS '归属sn';


--
-- Name: COLUMN labdatahub_db_config.belong_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_db_config.belong_type IS '归属类型 0-产品 1-设备';


--
-- Name: COLUMN labdatahub_db_config.code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_db_config.code IS '读取编码(字段名)';


--
-- Name: COLUMN labdatahub_db_config.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_db_config.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_db_config.interval_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_db_config.interval_time IS '多少毫秒读取一次';


--
-- Name: COLUMN labdatahub_db_config.delay_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_db_config.delay_time IS '同一网络组件读取属性延迟时间';


--
-- Name: COLUMN labdatahub_db_config.field_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_db_config.field_type IS '字段类型';


--
-- Name: COLUMN labdatahub_db_config.field_comment; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_db_config.field_comment IS '字段备注';


--
-- Name: COLUMN labdatahub_db_config.field_length; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_db_config.field_length IS '字段长度';


--
-- Name: labdatahub_device; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_device (
    id character varying(255) DEFAULT ''::character varying NOT NULL,
    device_sn character varying(255) DEFAULT NULL::character varying,
    device_name character varying(255) DEFAULT NULL::character varying,
    product_id character varying(255) DEFAULT NULL::character varying,
    product_name character varying(255) DEFAULT NULL::character varying,
    product_sn character varying(255) DEFAULT NULL::character varying,
    create_time timestamp(6) without time zone,
    create_by character varying(255) DEFAULT NULL::character varying,
    update_time timestamp(6) without time zone,
    update_by character varying(255) DEFAULT NULL::character varying,
    link_method_id character varying(255) DEFAULT NULL::character varying,
    link_method_name character varying(255) DEFAULT NULL::character varying,
    protocol_id character varying(255) DEFAULT NULL::character varying,
    protocol_name character varying(255) DEFAULT NULL::character varying,
    status character varying(255) DEFAULT '0'::character varying,
    remark text,
    timeout_seconds integer DEFAULT 10,
    device_type character varying(255) DEFAULT '0'::character varying,
    regular_cleaning character varying(255) DEFAULT '0'::character varying,
    retention_time bigint DEFAULT 1,
    retention_unit character varying(255) DEFAULT NULL::character varying,
    component_id character varying(255) DEFAULT NULL::character varying,
    component_name character varying(255) DEFAULT NULL::character varying,
    custom_config text,
    "position" character varying(255) DEFAULT NULL::character varying,
    position_name text,
    slave_id integer,
    modbus_read character varying(255) DEFAULT '0'::character varying,
    group_code character varying(255) DEFAULT NULL::character varying,
    group_name character varying(255) DEFAULT NULL::character varying
);


--
-- Name: TABLE labdatahub_device; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_device IS '设备表';


--
-- Name: COLUMN labdatahub_device.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.id IS 'id';


--
-- Name: COLUMN labdatahub_device.device_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.device_sn IS '设备编码';


--
-- Name: COLUMN labdatahub_device.device_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.device_name IS '设备名称';


--
-- Name: COLUMN labdatahub_device.product_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.product_id IS '关联产品id';


--
-- Name: COLUMN labdatahub_device.product_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.product_name IS '关联产品名称';


--
-- Name: COLUMN labdatahub_device.product_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.product_sn IS '关联产品编码';


--
-- Name: COLUMN labdatahub_device.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_device.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.create_by IS '创建人';


--
-- Name: COLUMN labdatahub_device.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.update_time IS '修改时间';


--
-- Name: COLUMN labdatahub_device.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.update_by IS '修改人';


--
-- Name: COLUMN labdatahub_device.link_method_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.link_method_id IS '接入方式id';


--
-- Name: COLUMN labdatahub_device.link_method_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.link_method_name IS '接入方式名称';


--
-- Name: COLUMN labdatahub_device.protocol_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.protocol_id IS '协议id';


--
-- Name: COLUMN labdatahub_device.protocol_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.protocol_name IS '协议名称';


--
-- Name: COLUMN labdatahub_device.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.status IS '0-离线 1-在线';


--
-- Name: COLUMN labdatahub_device.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.remark IS '备注';


--
-- Name: COLUMN labdatahub_device.timeout_seconds; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.timeout_seconds IS '心跳超时时间(秒)';


--
-- Name: COLUMN labdatahub_device.device_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.device_type IS '设备类型 0-直连设备 1-网关设备 2-无状态设备';


--
-- Name: COLUMN labdatahub_device.regular_cleaning; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.regular_cleaning IS '数据是否定期清理 0-否 1-是';


--
-- Name: COLUMN labdatahub_device.retention_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.retention_time IS '数据保存时间';


--
-- Name: COLUMN labdatahub_device.retention_unit; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.retention_unit IS '数据保存时间单位 hour-时 day-天 week-周 month-月 year-年';


--
-- Name: COLUMN labdatahub_device.component_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.component_id IS '组件id';


--
-- Name: COLUMN labdatahub_device.component_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.component_name IS '组件名称';


--
-- Name: COLUMN labdatahub_device.custom_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.custom_config IS '自定义配置';


--
-- Name: COLUMN labdatahub_device."position"; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device."position" IS '经纬度';


--
-- Name: COLUMN labdatahub_device.position_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.position_name IS '定位地点名称';


--
-- Name: COLUMN labdatahub_device.slave_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.slave_id IS 'modbus从站id';


--
-- Name: COLUMN labdatahub_device.modbus_read; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.modbus_read IS '是否开启modbus数据读取 0-关闭 1-开启';


--
-- Name: COLUMN labdatahub_device.group_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.group_code IS '分组CODE';


--
-- Name: COLUMN labdatahub_device.group_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device.group_name IS '分组名称';


--
-- Name: labdatahub_device_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_device_group (
    id character varying(255) NOT NULL,
    group_name character varying(255) DEFAULT NULL::character varying,
    group_code character varying(255) DEFAULT '0'::character varying,
    type character varying(255) DEFAULT '0'::character varying,
    sort_num integer DEFAULT 0,
    remark text,
    create_time timestamp(6) without time zone
);


--
-- Name: TABLE labdatahub_device_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_device_group IS '设备分组';


--
-- Name: COLUMN labdatahub_device_group.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device_group.id IS 'id';


--
-- Name: COLUMN labdatahub_device_group.group_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device_group.group_name IS '分组名称';


--
-- Name: COLUMN labdatahub_device_group.group_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device_group.group_code IS '唯一标识';


--
-- Name: COLUMN labdatahub_device_group.type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device_group.type IS '0-产品 1-设备';


--
-- Name: COLUMN labdatahub_device_group.sort_num; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device_group.sort_num IS '排序';


--
-- Name: COLUMN labdatahub_device_group.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device_group.remark IS '备注';


--
-- Name: COLUMN labdatahub_device_group.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_device_group.create_time IS '创建时间';


--
-- Name: labdatahub_fanuc_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_fanuc_config (
    id character varying(255) NOT NULL,
    belong_sn character varying(255) DEFAULT NULL::character varying,
    belong_type character varying(255) DEFAULT '0'::character varying,
    code character varying(255) DEFAULT NULL::character varying,
    create_time timestamp(6) without time zone,
    read_type character varying(32) DEFAULT NULL::character varying,
    param1 numeric(10,0),
    param2 numeric(10,0),
    interval_time integer DEFAULT 1,
    delay_time integer DEFAULT 0,
    name character varying(100)
);


--
-- Name: TABLE labdatahub_fanuc_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_fanuc_config IS 'FANUC FOCAS2协议读取配置表';


--
-- Name: COLUMN labdatahub_fanuc_config.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_fanuc_config.id IS 'id';


--
-- Name: COLUMN labdatahub_fanuc_config.belong_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_fanuc_config.belong_sn IS '归属sn';


--
-- Name: COLUMN labdatahub_fanuc_config.belong_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_fanuc_config.belong_type IS '归属类型 0-产品 1-设备';


--
-- Name: COLUMN labdatahub_fanuc_config.code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_fanuc_config.code IS '读取编码';


--
-- Name: COLUMN labdatahub_fanuc_config.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_fanuc_config.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_fanuc_config.read_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_fanuc_config.read_type IS '采集项类型（axis/spindle/feed/mode/status/prgnum/alarm/tcode/macro/timer/pmc）';


--
-- Name: COLUMN labdatahub_fanuc_config.param1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_fanuc_config.param1 IS '参数1（轴号/子项/宏变量号等，各readType含义不同）';


--
-- Name: COLUMN labdatahub_fanuc_config.param2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_fanuc_config.param2 IS '参数2（坐标类型/地址号等，各readType含义不同）';


--
-- Name: COLUMN labdatahub_fanuc_config.interval_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_fanuc_config.interval_time IS '多少秒读取一次';


--
-- Name: COLUMN labdatahub_fanuc_config.delay_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_fanuc_config.delay_time IS '同一网络组件读取属性延迟时间';


--
-- Name: COLUMN labdatahub_fanuc_config.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_fanuc_config.name IS '点位名称（物模型属性名用，null 用标识 code）';


--
-- Name: labdatahub_function; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_function (
    id character varying(255) NOT NULL,
    function_name character varying(255) DEFAULT NULL::character varying,
    function_code character varying(255) DEFAULT NULL::character varying,
    function_params text,
    belong_sn character varying(255) DEFAULT NULL::character varying,
    protocol_id text,
    create_time timestamp(6) without time zone,
    create_by character varying(255) DEFAULT NULL::character varying,
    belong_type character varying(255) DEFAULT '0'::character varying
);


--
-- Name: TABLE labdatahub_function; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_function IS '设备指令下发表';


--
-- Name: COLUMN labdatahub_function.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function.id IS 'id';


--
-- Name: COLUMN labdatahub_function.function_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function.function_name IS '功能名称';


--
-- Name: COLUMN labdatahub_function.function_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function.function_code IS '功能编码';


--
-- Name: COLUMN labdatahub_function.function_params; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function.function_params IS '自定义参数';


--
-- Name: COLUMN labdatahub_function.belong_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function.belong_sn IS '设备/产品sn';


--
-- Name: COLUMN labdatahub_function.protocol_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function.protocol_id IS '协议id';


--
-- Name: COLUMN labdatahub_function.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_function.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function.create_by IS '创建人';


--
-- Name: COLUMN labdatahub_function.belong_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function.belong_type IS '归属类型 0-产品 1-设备';


--
-- Name: labdatahub_function_record; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_function_record (
    id character varying(255) NOT NULL,
    function_id character varying(255) DEFAULT NULL::character varying,
    function_code character varying(255) DEFAULT NULL::character varying,
    function_name character varying(255) DEFAULT NULL::character varying,
    function_params text,
    is_success character varying(255) DEFAULT '1'::character varying,
    create_time timestamp(6) without time zone,
    device_sn character varying(255) DEFAULT NULL::character varying,
    device_name character varying(255) DEFAULT NULL::character varying,
    trigger_type character varying(255) DEFAULT '0'::character varying
);


--
-- Name: TABLE labdatahub_function_record; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_function_record IS '指令下发记录';


--
-- Name: COLUMN labdatahub_function_record.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function_record.id IS 'id';


--
-- Name: COLUMN labdatahub_function_record.function_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function_record.function_id IS '功能id';


--
-- Name: COLUMN labdatahub_function_record.function_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function_record.function_code IS '功能code';


--
-- Name: COLUMN labdatahub_function_record.function_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function_record.function_name IS '功能名称';


--
-- Name: COLUMN labdatahub_function_record.function_params; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function_record.function_params IS '参数';


--
-- Name: COLUMN labdatahub_function_record.is_success; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function_record.is_success IS '0-失败 1-成功';


--
-- Name: COLUMN labdatahub_function_record.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function_record.create_time IS '下发时间';


--
-- Name: COLUMN labdatahub_function_record.device_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function_record.device_sn IS '设备sn';


--
-- Name: COLUMN labdatahub_function_record.device_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function_record.device_name IS '设备名称';


--
-- Name: COLUMN labdatahub_function_record.trigger_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_function_record.trigger_type IS '0-手动触发 1-告警触发 2-定时触发';


--
-- Name: labdatahub_linkage_action_record; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_linkage_action_record (
    id character varying(255) NOT NULL,
    config_id character varying(255) DEFAULT NULL::character varying,
    config_name character varying(255) DEFAULT NULL::character varying,
    execute_sn text,
    execute_name text,
    function_code character varying(255) DEFAULT NULL::character varying,
    function_name character varying(255) DEFAULT NULL::character varying,
    function_param text,
    create_time timestamp(6) without time zone
);


--
-- Name: TABLE labdatahub_linkage_action_record; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_linkage_action_record IS '设备联动告警动作执行记录';


--
-- Name: COLUMN labdatahub_linkage_action_record.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_action_record.id IS 'id';


--
-- Name: COLUMN labdatahub_linkage_action_record.config_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_action_record.config_id IS '告警配置id';


--
-- Name: COLUMN labdatahub_linkage_action_record.config_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_action_record.config_name IS '告警配置名称';


--
-- Name: COLUMN labdatahub_linkage_action_record.execute_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_action_record.execute_sn IS '执行动作设备SN';


--
-- Name: COLUMN labdatahub_linkage_action_record.execute_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_action_record.execute_name IS '执行动作设备名称';


--
-- Name: COLUMN labdatahub_linkage_action_record.function_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_action_record.function_code IS '动作CODE';


--
-- Name: COLUMN labdatahub_linkage_action_record.function_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_action_record.function_name IS '动作名称';


--
-- Name: COLUMN labdatahub_linkage_action_record.function_param; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_action_record.function_param IS '参数';


--
-- Name: COLUMN labdatahub_linkage_action_record.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_action_record.create_time IS '创建时间';


--
-- Name: labdatahub_linkage_warn_record; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_linkage_warn_record (
    id character varying(255) NOT NULL,
    config_id character varying(255) DEFAULT NULL::character varying,
    config_name character varying(255) DEFAULT NULL::character varying,
    warn_message text,
    warn_data text,
    trigger_sn_list text,
    trigger_name_list text,
    create_time timestamp(6) without time zone,
    warn_level character varying(255) DEFAULT '1'::character varying,
    status character varying(255) DEFAULT '0'::character varying
);


--
-- Name: TABLE labdatahub_linkage_warn_record; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_linkage_warn_record IS '设备联动告警记录';


--
-- Name: COLUMN labdatahub_linkage_warn_record.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_warn_record.id IS 'id';


--
-- Name: COLUMN labdatahub_linkage_warn_record.config_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_warn_record.config_id IS '告警配置id';


--
-- Name: COLUMN labdatahub_linkage_warn_record.config_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_warn_record.config_name IS '告警配置名称';


--
-- Name: COLUMN labdatahub_linkage_warn_record.warn_message; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_warn_record.warn_message IS '告警内容';


--
-- Name: COLUMN labdatahub_linkage_warn_record.warn_data; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_warn_record.warn_data IS '告警时相关设备数据';


--
-- Name: COLUMN labdatahub_linkage_warn_record.trigger_sn_list; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_warn_record.trigger_sn_list IS '告警设备SN列表';


--
-- Name: COLUMN labdatahub_linkage_warn_record.trigger_name_list; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_warn_record.trigger_name_list IS '告警设备名称列表';


--
-- Name: COLUMN labdatahub_linkage_warn_record.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_warn_record.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_linkage_warn_record.warn_level; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_warn_record.warn_level IS '告警等级 1-紧急 2-严重 3-一般 4-警告 5-正常';


--
-- Name: COLUMN labdatahub_linkage_warn_record.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_linkage_warn_record.status IS '0-未处理 1-已处理';


--
-- Name: labdatahub_media_device; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_media_device (
    id character varying(255) NOT NULL,
    device_sn character varying(255) DEFAULT NULL::character varying,
    device_name character varying(255) DEFAULT NULL::character varying,
    play_url text,
    platform_id character varying(255) DEFAULT NULL::character varying,
    platform_name character varying(255) DEFAULT NULL::character varying,
    platform_type character varying(255) DEFAULT '3'::character varying,
    create_time timestamp(6) without time zone,
    device_type character varying(255) DEFAULT '0'::character varying,
    use_model character varying(255) DEFAULT '0'::character varying
);


--
-- Name: TABLE labdatahub_media_device; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_media_device IS '视频设备表';


--
-- Name: COLUMN labdatahub_media_device.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_device.id IS 'id';


--
-- Name: COLUMN labdatahub_media_device.device_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_device.device_sn IS '设备编码';


--
-- Name: COLUMN labdatahub_media_device.device_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_device.device_name IS '设备名称';


--
-- Name: COLUMN labdatahub_media_device.play_url; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_device.play_url IS '播放地址';


--
-- Name: COLUMN labdatahub_media_device.platform_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_device.platform_id IS '平台id';


--
-- Name: COLUMN labdatahub_media_device.platform_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_device.platform_name IS '平台名称';


--
-- Name: COLUMN labdatahub_media_device.platform_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_device.platform_type IS '平台类型 1-海康 2-大华 3-自建';


--
-- Name: COLUMN labdatahub_media_device.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_device.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_media_device.device_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_device.device_type IS '0-枪机 1-球机';


--
-- Name: COLUMN labdatahub_media_device.use_model; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_device.use_model IS '模式  0-绑定平台 1-手动添加';


--
-- Name: labdatahub_media_server; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_media_server (
    id character varying(255) NOT NULL,
    platform_type character varying(255) DEFAULT '3'::character varying,
    platform_name character varying(255) DEFAULT NULL::character varying,
    server_ip character varying(255) DEFAULT NULL::character varying,
    server_port character varying(255) DEFAULT NULL::character varying,
    config_json text,
    create_time timestamp(6) without time zone
);


--
-- Name: TABLE labdatahub_media_server; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_media_server IS '流媒体服务器配置';


--
-- Name: COLUMN labdatahub_media_server.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_server.id IS 'id';


--
-- Name: COLUMN labdatahub_media_server.platform_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_server.platform_type IS '平台类型 1-海康 2-大华 3-自建';


--
-- Name: COLUMN labdatahub_media_server.platform_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_server.platform_name IS '平台名称';


--
-- Name: COLUMN labdatahub_media_server.server_ip; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_server.server_ip IS '流媒体服务器地址';


--
-- Name: COLUMN labdatahub_media_server.server_port; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_server.server_port IS '端口';


--
-- Name: COLUMN labdatahub_media_server.config_json; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_server.config_json IS 'json配置';


--
-- Name: COLUMN labdatahub_media_server.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_media_server.create_time IS '创建时间';


--
-- Name: labdatahub_mitsubishi_cnc_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_mitsubishi_cnc_config (
    id character varying(255) NOT NULL,
    belong_sn character varying(255) DEFAULT NULL::character varying,
    belong_type character varying(255) DEFAULT '0'::character varying,
    code character varying(255) DEFAULT NULL::character varying,
    create_time timestamp(6) without time zone,
    read_type character varying(32) DEFAULT NULL::character varying,
    axis_no numeric(10,0),
    interval_time integer DEFAULT 1,
    delay_time integer DEFAULT 0,
    name character varying(100)
);


--
-- Name: TABLE labdatahub_mitsubishi_cnc_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_mitsubishi_cnc_config IS '三菱CNC TCP(MOCHA)协议读取配置表';


--
-- Name: COLUMN labdatahub_mitsubishi_cnc_config.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_cnc_config.id IS 'id';


--
-- Name: COLUMN labdatahub_mitsubishi_cnc_config.belong_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_cnc_config.belong_sn IS '归属sn';


--
-- Name: COLUMN labdatahub_mitsubishi_cnc_config.belong_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_cnc_config.belong_type IS '归属类型 0-产品 1-设备';


--
-- Name: COLUMN labdatahub_mitsubishi_cnc_config.code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_cnc_config.code IS '读取编码';


--
-- Name: COLUMN labdatahub_mitsubishi_cnc_config.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_cnc_config.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_mitsubishi_cnc_config.read_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_cnc_config.read_type IS '采集项类型（树根点位键：al/fre/pn/spn/cc/sl1/ss1/tn/stn/po/opt/cut/ct/sv/fv/st/pst/opm/axc；轴点 mechpos/currpos/remapos/cu/sp）';


--
-- Name: COLUMN labdatahub_mitsubishi_cnc_config.axis_no; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_cnc_config.axis_no IS '轴号（1-6，仅轴类点位有效）';


--
-- Name: COLUMN labdatahub_mitsubishi_cnc_config.interval_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_cnc_config.interval_time IS '多少秒读取一次';


--
-- Name: COLUMN labdatahub_mitsubishi_cnc_config.delay_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_cnc_config.delay_time IS '同一网络组件读取属性延迟时间';


--
-- Name: COLUMN labdatahub_mitsubishi_cnc_config.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_cnc_config.name IS '点位名称（物模型属性名用，null 用标识 code）';


--
-- Name: labdatahub_mitsubishi_mc3e_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_mitsubishi_mc3e_config (
    id character varying(255) NOT NULL,
    belong_sn character varying(255) DEFAULT NULL::character varying,
    belong_type character varying(255) DEFAULT '0'::character varying,
    code character varying(255) DEFAULT NULL::character varying,
    create_time timestamp(6) without time zone,
    area_code numeric(10,0),
    start_address numeric(10,0),
    length numeric(10,0),
    interval_time integer DEFAULT 1,
    delay_time integer DEFAULT 0,
    name character varying(100)
);


--
-- Name: TABLE labdatahub_mitsubishi_mc3e_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_mitsubishi_mc3e_config IS '三菱MC协议（QnA兼容3E二进制帧）读取配置表';


--
-- Name: COLUMN labdatahub_mitsubishi_mc3e_config.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_mc3e_config.id IS 'id';


--
-- Name: COLUMN labdatahub_mitsubishi_mc3e_config.belong_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_mc3e_config.belong_sn IS '归属sn';


--
-- Name: COLUMN labdatahub_mitsubishi_mc3e_config.belong_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_mc3e_config.belong_type IS '归属类型 0-产品 1-设备';


--
-- Name: COLUMN labdatahub_mitsubishi_mc3e_config.code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_mc3e_config.code IS '读取编码';


--
-- Name: COLUMN labdatahub_mitsubishi_mc3e_config.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_mc3e_config.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_mitsubishi_mc3e_config.area_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_mc3e_config.area_code IS '软元件代码（字设备 D=0xA8 W=0xB4 R=0xAF ZR=0xB0 SD=0xA9；位设备 M=0x90 L=0x92 B=0xA0 X=0x9C Y=0x9D S=0x98 SM=0x91 F=0x93）';


--
-- Name: COLUMN labdatahub_mitsubishi_mc3e_config.start_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_mc3e_config.start_address IS '起始地址（X/Y 为八进制地址）';


--
-- Name: COLUMN labdatahub_mitsubishi_mc3e_config.length; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_mc3e_config.length IS '读取数量（字设备为字数，位设备为点数）';


--
-- Name: COLUMN labdatahub_mitsubishi_mc3e_config.interval_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_mc3e_config.interval_time IS '多少秒读取一次';


--
-- Name: COLUMN labdatahub_mitsubishi_mc3e_config.delay_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_mc3e_config.delay_time IS '同一网络组件读取属性延迟时间（毫秒）';


--
-- Name: COLUMN labdatahub_mitsubishi_mc3e_config.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_mitsubishi_mc3e_config.name IS '点位名称（物模型属性名用，null 用标识 code）';


--
-- Name: labdatahub_modbus_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_modbus_config (
    id character varying(255) NOT NULL,
    belong_sn character varying(255) DEFAULT NULL::character varying,
    belong_type character varying(255) DEFAULT '0'::character varying,
    code character varying(255) DEFAULT NULL::character varying,
    create_time timestamp(6) without time zone,
    register_range text,
    interval_time integer DEFAULT 1,
    delay_time integer DEFAULT 0,
    function_code character varying(32) DEFAULT '03'::character varying,
    name character varying(100)
);


--
-- Name: TABLE labdatahub_modbus_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_modbus_config IS 'modbus协议读取配置表';


--
-- Name: COLUMN labdatahub_modbus_config.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_modbus_config.id IS 'id';


--
-- Name: COLUMN labdatahub_modbus_config.belong_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_modbus_config.belong_sn IS '归属sn';


--
-- Name: COLUMN labdatahub_modbus_config.belong_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_modbus_config.belong_type IS '归属类型 0-产品 1-设备';


--
-- Name: COLUMN labdatahub_modbus_config.code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_modbus_config.code IS '读取编码';


--
-- Name: COLUMN labdatahub_modbus_config.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_modbus_config.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_modbus_config.register_range; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_modbus_config.register_range IS '范围,逗号分隔如（1,2-5,7）';


--
-- Name: COLUMN labdatahub_modbus_config.interval_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_modbus_config.interval_time IS '多少毫秒读取一次';


--
-- Name: COLUMN labdatahub_modbus_config.delay_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_modbus_config.delay_time IS '同一网络组件读取属性延迟时间';


--
-- Name: COLUMN labdatahub_modbus_config.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_modbus_config.name IS '点位名称（物模型属性名用，null 用标识 code）';


--
-- Name: labdatahub_omronfins_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_omronfins_config (
    id character varying(255) NOT NULL,
    belong_sn character varying(255) DEFAULT NULL::character varying,
    belong_type character varying(255) DEFAULT '0'::character varying,
    code character varying(255) DEFAULT NULL::character varying,
    create_time timestamp(6) without time zone,
    area_code numeric(10,0),
    interval_time integer DEFAULT 1,
    delay_time integer DEFAULT 0,
    start_address numeric(10,0),
    length numeric(10,0),
    name character varying(100),
    bit_address numeric(10,0)
);


--
-- Name: TABLE labdatahub_omronfins_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_omronfins_config IS 'modbus协议读取配置表';


--
-- Name: COLUMN labdatahub_omronfins_config.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_omronfins_config.id IS 'id';


--
-- Name: COLUMN labdatahub_omronfins_config.belong_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_omronfins_config.belong_sn IS '归属sn';


--
-- Name: COLUMN labdatahub_omronfins_config.belong_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_omronfins_config.belong_type IS '归属类型 0-产品 1-设备';


--
-- Name: COLUMN labdatahub_omronfins_config.code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_omronfins_config.code IS '读取编码';


--
-- Name: COLUMN labdatahub_omronfins_config.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_omronfins_config.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_omronfins_config.area_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_omronfins_config.area_code IS '存储区代码';


--
-- Name: COLUMN labdatahub_omronfins_config.interval_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_omronfins_config.interval_time IS '多少毫秒读取一次';


--
-- Name: COLUMN labdatahub_omronfins_config.delay_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_omronfins_config.delay_time IS '同一网络组件读取属性延迟时间';


--
-- Name: COLUMN labdatahub_omronfins_config.start_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_omronfins_config.start_address IS '起始字地址（位区下这是字地址，位号另看 bit_address，0-65535）';


--
-- Name: COLUMN labdatahub_omronfins_config.length; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_omronfins_config.length IS '读取长度：字区=字个数，位区=位个数（位点位恒为 1）';


--
-- Name: COLUMN labdatahub_omronfins_config.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_omronfins_config.name IS '点位名称（物模型属性名用，null 用标识 code）';


--
-- Name: COLUMN labdatahub_omronfins_config.bit_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_omronfins_config.bit_address IS '位号（仅位区码点位使用，0-15；字区为 NULL 表示按字访问）';


--
-- Name: labdatahub_point_write_record; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_point_write_record (
    id bigint NOT NULL,
    request_id character varying(64) DEFAULT NULL::character varying,
    device_sn character varying(255) DEFAULT NULL::character varying,
    device_name character varying(255) DEFAULT NULL::character varying,
    code character varying(255) DEFAULT NULL::character varying,
    point_name character varying(255) DEFAULT NULL::character varying,
    raw_value text,
    net_type character varying(32) DEFAULT NULL::character varying,
    data_type character varying(64) DEFAULT NULL::character varying,
    address integer,
    write_count integer,
    payload text,
    is_success character varying(2) DEFAULT '1'::character varying,
    error_code integer,
    error_msg character varying(1024) DEFAULT NULL::character varying,
    cost_ms integer,
    create_time timestamp(6) without time zone,
    source character varying(16) DEFAULT NULL::character varying
);


--
-- Name: TABLE labdatahub_point_write_record; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_point_write_record IS '点位外部写入审计表';


--
-- Name: COLUMN labdatahub_point_write_record.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.id IS 'id';


--
-- Name: COLUMN labdatahub_point_write_record.request_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.request_id IS '请求id（服务端生成的追踪标识，不做幂等，见方案 4.6）';


--
-- Name: COLUMN labdatahub_point_write_record.device_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.device_sn IS '设备sn';


--
-- Name: COLUMN labdatahub_point_write_record.device_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.device_name IS '设备名称（冗余，设备改名后仍能看清当时写的是哪台）';


--
-- Name: COLUMN labdatahub_point_write_record.code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.code IS '点位标识（对应协议点位配置 code / 物模型 identifier）';


--
-- Name: COLUMN labdatahub_point_write_record.point_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.point_name IS '点位名称（冗余，取协议点位的 name）';


--
-- Name: COLUMN labdatahub_point_write_record.raw_value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.raw_value IS '请求写入值（原始字符串，原样落库不转换）';


--
-- Name: COLUMN labdatahub_point_write_record.net_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.net_type IS '协议类型（本期恒为 MODBUS_TCP），决定下面 address/write_count/payload 三列的口径';


--
-- Name: COLUMN labdatahub_point_write_record.data_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.data_type IS '数据类型（物模型 dataType）';


--
-- Name: COLUMN labdatahub_point_write_record.address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.address IS '起始地址——MODBUS 寄存器号';


--
-- Name: COLUMN labdatahub_point_write_record.write_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.write_count IS '写入寄存器个数（16位字）';


--
-- Name: COLUMN labdatahub_point_write_record.payload; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.payload IS '编码后的载荷 JSON（即 EncodeMessage.modbusWriteJson 的内容，如 [{"start":5,"count":1,"registerList":[26729]}]）';


--
-- Name: COLUMN labdatahub_point_write_record.is_success; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.is_success IS '0-失败 1-成功';


--
-- Name: COLUMN labdatahub_point_write_record.error_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.error_code IS '失败时的错误码（400/404/409/422/500/503/504）';


--
-- Name: COLUMN labdatahub_point_write_record.error_msg; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.error_msg IS '失败原因';


--
-- Name: COLUMN labdatahub_point_write_record.cost_ms; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.cost_ms IS '耗时毫秒';


--
-- Name: COLUMN labdatahub_point_write_record.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_point_write_record.source; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_point_write_record.source IS '写入来源 api-接口写入 manual-页面手动写值（由入口标明，不取请求体，见方案 4.8.2）';


--
-- Name: labdatahub_point_write_record_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.labdatahub_point_write_record_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: labdatahub_point_write_record_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.labdatahub_point_write_record_id_seq OWNED BY public.labdatahub_point_write_record.id;


--
-- Name: labdatahub_product; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_product (
    id character varying(255) NOT NULL,
    product_sn character varying(255) DEFAULT NULL::character varying,
    product_name character varying(255) DEFAULT NULL::character varying,
    link_method_id character varying(255) DEFAULT NULL::character varying,
    link_method_name character varying(255) DEFAULT NULL::character varying,
    protocol_id character varying(255) DEFAULT NULL::character varying,
    protocol_name character varying(255) DEFAULT NULL::character varying,
    device_count integer DEFAULT 0,
    device_type character varying(255) DEFAULT '0'::character varying,
    remark text,
    create_time timestamp(6) without time zone,
    update_time timestamp(6) without time zone,
    create_by character varying(255) DEFAULT NULL::character varying,
    update_by character varying(255) DEFAULT NULL::character varying,
    status character varying(255) DEFAULT '0'::character varying,
    timeout_seconds integer DEFAULT 10,
    regular_cleaning character varying(255) DEFAULT '0'::character varying,
    retention_time bigint DEFAULT 1,
    retention_unit character varying(255) DEFAULT NULL::character varying,
    component_id character varying(255) DEFAULT NULL::character varying,
    component_name character varying(255) DEFAULT NULL::character varying,
    custom_config text,
    group_code character varying(255) DEFAULT NULL::character varying,
    group_name character varying(255) DEFAULT NULL::character varying
);


--
-- Name: TABLE labdatahub_product; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_product IS '产品表';


--
-- Name: COLUMN labdatahub_product.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.id IS 'id';


--
-- Name: COLUMN labdatahub_product.product_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.product_sn IS '产品编码';


--
-- Name: COLUMN labdatahub_product.product_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.product_name IS '产品名称';


--
-- Name: COLUMN labdatahub_product.link_method_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.link_method_id IS '接入方式id';


--
-- Name: COLUMN labdatahub_product.link_method_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.link_method_name IS '接入方式名称';


--
-- Name: COLUMN labdatahub_product.protocol_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.protocol_id IS '协议id';


--
-- Name: COLUMN labdatahub_product.protocol_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.protocol_name IS '协议名称';


--
-- Name: COLUMN labdatahub_product.device_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.device_count IS '设备数量';


--
-- Name: COLUMN labdatahub_product.device_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.device_type IS '设备类型 0-直连设备 1-网关设备 2-无状态设备';


--
-- Name: COLUMN labdatahub_product.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.remark IS '备注';


--
-- Name: COLUMN labdatahub_product.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_product.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.update_time IS '修改时间';


--
-- Name: COLUMN labdatahub_product.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.create_by IS '创建人';


--
-- Name: COLUMN labdatahub_product.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.update_by IS '修改人';


--
-- Name: COLUMN labdatahub_product.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.status IS '0-停用 1-启用';


--
-- Name: COLUMN labdatahub_product.timeout_seconds; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.timeout_seconds IS '心跳时间(秒)';


--
-- Name: COLUMN labdatahub_product.regular_cleaning; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.regular_cleaning IS '数据是否定期清理 0-否 1-是';


--
-- Name: COLUMN labdatahub_product.retention_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.retention_time IS '数据保存时间';


--
-- Name: COLUMN labdatahub_product.retention_unit; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.retention_unit IS '数据保存时间单位 hour-时 day-天 week-周 month-月 year-年';


--
-- Name: COLUMN labdatahub_product.component_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.component_id IS '组件id';


--
-- Name: COLUMN labdatahub_product.component_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.component_name IS '组件名称';


--
-- Name: COLUMN labdatahub_product.custom_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.custom_config IS '自定义配置';


--
-- Name: COLUMN labdatahub_product.group_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.group_code IS '分组CODE';


--
-- Name: COLUMN labdatahub_product.group_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_product.group_name IS '分组名称';


--
-- Name: labdatahub_properties; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_properties (
    id character varying(255) NOT NULL,
    belong_sn character varying(50) DEFAULT NULL::character varying,
    belong_type character varying(255) DEFAULT NULL::character varying,
    identifier character varying(100) DEFAULT NULL::character varying,
    name character varying(100) DEFAULT NULL::character varying,
    parent_id character varying(255) DEFAULT '0'::character varying,
    data_type character varying(50) DEFAULT NULL::character varying,
    sort_num integer DEFAULT 0,
    from_type character varying(255) DEFAULT NULL::character varying,
    remark text,
    unit character varying(255) DEFAULT NULL::character varying,
    byte_order character varying(20) DEFAULT 'big'::character varying,
    is_signed character varying(10) DEFAULT '1'::character varying,
    scale numeric(20,6) DEFAULT 1,
    offset_value numeric(20,6) DEFAULT 0
);


--
-- Name: TABLE labdatahub_properties; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_properties IS '物模型属性定义表';


--
-- Name: COLUMN labdatahub_properties.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_properties.id IS 'id';


--
-- Name: COLUMN labdatahub_properties.belong_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_properties.belong_sn IS '归属id 产品/设备';


--
-- Name: COLUMN labdatahub_properties.belong_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_properties.belong_type IS '归属类型 0-产品 1-设备';


--
-- Name: COLUMN labdatahub_properties.identifier; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_properties.identifier IS '属性标识符，如 temperature, status';


--
-- Name: COLUMN labdatahub_properties.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_properties.name IS '属性名称';


--
-- Name: COLUMN labdatahub_properties.parent_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_properties.parent_id IS '父属性ID，用于构建嵌套结构。0表示根级属性';


--
-- Name: COLUMN labdatahub_properties.data_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_properties.data_type IS '数据类型: int, double, bool, string, struct, array...';


--
-- Name: COLUMN labdatahub_properties.sort_num; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_properties.sort_num IS '排序';


--
-- Name: COLUMN labdatahub_properties.from_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_properties.from_type IS '来源 0-产品继承 1-设备自定义(继承不可修改)';


--
-- Name: COLUMN labdatahub_properties.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_properties.remark IS '备注';


--
-- Name: COLUMN labdatahub_properties.unit; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_properties.unit IS '单位';


--
-- Name: COLUMN labdatahub_properties.byte_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_properties.byte_order IS '字节序: big-大端 little-小端（多寄存器/多字节数值解析用）';


--
-- Name: COLUMN labdatahub_properties.is_signed; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_properties.is_signed IS '是否有符号: 1-有符号 0-无符号（整型解析用）';


--
-- Name: COLUMN labdatahub_properties.scale; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_properties.scale IS '缩放系数（value = 原始值*scale + offset）';


--
-- Name: COLUMN labdatahub_properties.offset_value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_properties.offset_value IS '偏移量（value = 原始值*scale + offset）';


--
-- Name: labdatahub_protocol; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_protocol (
    id character varying(255) DEFAULT ''::character varying NOT NULL,
    protocol_name character varying(255) DEFAULT NULL::character varying,
    local_url text,
    main_class_path text,
    origin_name text,
    type character varying(255) DEFAULT '0'::character varying,
    new_name text,
    component_id character varying(255) DEFAULT NULL::character varying,
    component_name character varying(255) DEFAULT NULL::character varying,
    create_time timestamp(6) without time zone,
    create_by character varying(255) DEFAULT NULL::character varying,
    update_time timestamp(6) without time zone,
    update_by character varying(255) DEFAULT NULL::character varying,
    status character varying(255) DEFAULT '1'::character varying,
    remark text,
    protocol_type character varying(255) DEFAULT NULL::character varying
);


--
-- Name: TABLE labdatahub_protocol; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_protocol IS '协议管理';


--
-- Name: COLUMN labdatahub_protocol.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.id IS 'id';


--
-- Name: COLUMN labdatahub_protocol.protocol_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.protocol_name IS '协议名称';


--
-- Name: COLUMN labdatahub_protocol.local_url; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.local_url IS '本地存储路径';


--
-- Name: COLUMN labdatahub_protocol.main_class_path; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.main_class_path IS '解析类入口';


--
-- Name: COLUMN labdatahub_protocol.origin_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.origin_name IS '文件原始名字';


--
-- Name: COLUMN labdatahub_protocol.type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.type IS '0-jar包';


--
-- Name: COLUMN labdatahub_protocol.new_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.new_name IS '文件重命名';


--
-- Name: COLUMN labdatahub_protocol.component_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.component_id IS '网络组件id';


--
-- Name: COLUMN labdatahub_protocol.component_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.component_name IS '网络组件名称';


--
-- Name: COLUMN labdatahub_protocol.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_protocol.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.create_by IS '创建人';


--
-- Name: COLUMN labdatahub_protocol.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.update_time IS '修改时间';


--
-- Name: COLUMN labdatahub_protocol.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.update_by IS '修改人';


--
-- Name: COLUMN labdatahub_protocol.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.status IS '0-停用 1-启用';


--
-- Name: COLUMN labdatahub_protocol.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.remark IS '备注';


--
-- Name: COLUMN labdatahub_protocol.protocol_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_protocol.protocol_type IS '协议类型';


--
-- Name: labdatahub_rule_engine; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_rule_engine (
    id character varying(255) NOT NULL,
    engine_name character varying(255) DEFAULT NULL::character varying,
    config_json text,
    create_time timestamp(6) without time zone,
    remark character varying(255) DEFAULT NULL::character varying,
    is_enable character varying(255) DEFAULT '0'::character varying
);


--
-- Name: TABLE labdatahub_rule_engine; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_rule_engine IS '规则引擎配置';


--
-- Name: COLUMN labdatahub_rule_engine.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_rule_engine.id IS 'id';


--
-- Name: COLUMN labdatahub_rule_engine.engine_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_rule_engine.engine_name IS '引擎名称';


--
-- Name: COLUMN labdatahub_rule_engine.config_json; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_rule_engine.config_json IS 'json配置';


--
-- Name: COLUMN labdatahub_rule_engine.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_rule_engine.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_rule_engine.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_rule_engine.remark IS '备注';


--
-- Name: COLUMN labdatahub_rule_engine.is_enable; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_rule_engine.is_enable IS '0-停止 1-启用';


--
-- Name: labdatahub_s71200_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_s71200_config (
    id character varying(255) NOT NULL,
    belong_sn character varying(255) DEFAULT NULL::character varying,
    belong_type character varying(255) DEFAULT '0'::character varying,
    code character varying(255) DEFAULT NULL::character varying,
    create_time timestamp(6) without time zone,
    db_number numeric(10,0),
    interval_time integer DEFAULT 1,
    delay_time integer DEFAULT 0,
    start_address numeric(10,0),
    length numeric(10,0),
    block_type character varying(255),
    bit_offset numeric(10,0),
    area_type character varying(32) DEFAULT 'DB'::character varying,
    name character varying(100)
);


--
-- Name: TABLE labdatahub_s71200_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_s71200_config IS 'modbus协议读取配置表';


--
-- Name: COLUMN labdatahub_s71200_config.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_s71200_config.id IS 'id';


--
-- Name: COLUMN labdatahub_s71200_config.belong_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_s71200_config.belong_sn IS '归属sn';


--
-- Name: COLUMN labdatahub_s71200_config.belong_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_s71200_config.belong_type IS '归属类型 0-产品 1-设备';


--
-- Name: COLUMN labdatahub_s71200_config.code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_s71200_config.code IS '读取编码';


--
-- Name: COLUMN labdatahub_s71200_config.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_s71200_config.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_s71200_config.db_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_s71200_config.db_number IS 'DB块号';


--
-- Name: COLUMN labdatahub_s71200_config.interval_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_s71200_config.interval_time IS '多少毫秒读取一次';


--
-- Name: COLUMN labdatahub_s71200_config.delay_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_s71200_config.delay_time IS '同一网络组件读取属性延迟时间';


--
-- Name: COLUMN labdatahub_s71200_config.start_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_s71200_config.start_address IS '起始字节偏移';


--
-- Name: COLUMN labdatahub_s71200_config.length; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_s71200_config.length IS '读取长度（字节）';


--
-- Name: COLUMN labdatahub_s71200_config.block_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_s71200_config.block_type IS '块类型';


--
-- Name: COLUMN labdatahub_s71200_config.bit_offset; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_s71200_config.bit_offset IS '偏移量';


--
-- Name: COLUMN labdatahub_s71200_config.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_s71200_config.name IS '点位名称（物模型属性名用，null 用标识 code）';


--
-- Name: labdatahub_scheduled_task; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_scheduled_task (
    id character varying(255) NOT NULL,
    name character varying(255) DEFAULT NULL::character varying,
    rule_json text,
    is_enable character varying(255) DEFAULT '0'::character varying,
    execute_sn_list text,
    execute_name_list text,
    create_time timestamp(6) without time zone,
    remark text
);


--
-- Name: TABLE labdatahub_scheduled_task; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_scheduled_task IS '定时引擎配置表';


--
-- Name: COLUMN labdatahub_scheduled_task.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_scheduled_task.id IS 'id';


--
-- Name: COLUMN labdatahub_scheduled_task.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_scheduled_task.name IS '配置名称';


--
-- Name: COLUMN labdatahub_scheduled_task.rule_json; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_scheduled_task.rule_json IS '规则json';


--
-- Name: COLUMN labdatahub_scheduled_task.is_enable; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_scheduled_task.is_enable IS '是否启用 0-否 1-是';


--
-- Name: COLUMN labdatahub_scheduled_task.execute_sn_list; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_scheduled_task.execute_sn_list IS '执行动作设备列表';


--
-- Name: COLUMN labdatahub_scheduled_task.execute_name_list; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_scheduled_task.execute_name_list IS '执行动作设备名称';


--
-- Name: COLUMN labdatahub_scheduled_task.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_scheduled_task.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_scheduled_task.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_scheduled_task.remark IS '备注';


--
-- Name: labdatahub_warn_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_warn_config (
    id character varying(255) NOT NULL,
    name character varying(255) DEFAULT NULL::character varying,
    belong_sn character varying(255) DEFAULT NULL::character varying,
    belong_type character varying(255) DEFAULT '1'::character varying,
    rule_json text,
    warn_message text,
    warn_level character varying(255) DEFAULT '1'::character varying,
    create_time timestamp(6) without time zone,
    create_by character varying(255) DEFAULT NULL::character varying,
    update_time timestamp(6) without time zone,
    update_by character varying(255) DEFAULT NULL::character varying,
    execute_action text,
    is_enable character varying(255) DEFAULT '0'::character varying,
    warn_type character varying(255) DEFAULT '0'::character varying
);


--
-- Name: TABLE labdatahub_warn_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_warn_config IS '告警配置表';


--
-- Name: COLUMN labdatahub_warn_config.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_config.id IS 'id';


--
-- Name: COLUMN labdatahub_warn_config.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_config.name IS '告警名称';


--
-- Name: COLUMN labdatahub_warn_config.belong_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_config.belong_sn IS '产品/设备sn';


--
-- Name: COLUMN labdatahub_warn_config.belong_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_config.belong_type IS '来源 0-产品 1-设备';


--
-- Name: COLUMN labdatahub_warn_config.rule_json; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_config.rule_json IS '规则json';


--
-- Name: COLUMN labdatahub_warn_config.warn_message; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_config.warn_message IS '告警消息模板';


--
-- Name: COLUMN labdatahub_warn_config.warn_level; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_config.warn_level IS '告警等级 1-紧急 2-严重 3-警告 4-正常';


--
-- Name: COLUMN labdatahub_warn_config.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_config.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_warn_config.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_config.create_by IS '创建人';


--
-- Name: COLUMN labdatahub_warn_config.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_config.update_time IS '修改时间';


--
-- Name: COLUMN labdatahub_warn_config.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_config.update_by IS '修改人';


--
-- Name: COLUMN labdatahub_warn_config.execute_action; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_config.execute_action IS '执行动作json';


--
-- Name: COLUMN labdatahub_warn_config.is_enable; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_config.is_enable IS '是否启用 0-否 1-是';


--
-- Name: COLUMN labdatahub_warn_config.warn_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_config.warn_type IS '类型 0-属性 1-上线 2-下线';


--
-- Name: labdatahub_warn_linkage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_warn_linkage (
    id character varying(255) NOT NULL,
    name character varying(255) DEFAULT NULL::character varying,
    rule_json text,
    warn_message text,
    warn_level character varying(255) DEFAULT '1'::character varying,
    is_enable character varying(255) DEFAULT '0'::character varying,
    trigger_sn_list text,
    trigger_name_list text,
    execute_sn_list text,
    execute_name_list text,
    create_time timestamp(6) without time zone,
    remark text
);


--
-- Name: TABLE labdatahub_warn_linkage; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_warn_linkage IS '设备联动告警';


--
-- Name: COLUMN labdatahub_warn_linkage.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_linkage.id IS 'id';


--
-- Name: COLUMN labdatahub_warn_linkage.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_linkage.name IS '配置名称';


--
-- Name: COLUMN labdatahub_warn_linkage.rule_json; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_linkage.rule_json IS '规则json';


--
-- Name: COLUMN labdatahub_warn_linkage.warn_message; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_linkage.warn_message IS '告警消息模板';


--
-- Name: COLUMN labdatahub_warn_linkage.warn_level; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_linkage.warn_level IS '告警等级 1-紧急 2-严重 3-警告 4-正常';


--
-- Name: COLUMN labdatahub_warn_linkage.is_enable; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_linkage.is_enable IS '是否启用 0-否 1-是';


--
-- Name: COLUMN labdatahub_warn_linkage.trigger_sn_list; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_linkage.trigger_sn_list IS '触发设备列表';


--
-- Name: COLUMN labdatahub_warn_linkage.trigger_name_list; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_linkage.trigger_name_list IS '触发设备名称';


--
-- Name: COLUMN labdatahub_warn_linkage.execute_sn_list; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_linkage.execute_sn_list IS '执行动作设备列表';


--
-- Name: COLUMN labdatahub_warn_linkage.execute_name_list; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_linkage.execute_name_list IS '执行动作设备名称';


--
-- Name: COLUMN labdatahub_warn_linkage.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_linkage.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_warn_linkage.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_linkage.remark IS '备注';


--
-- Name: labdatahub_warn_record; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.labdatahub_warn_record (
    id character varying(255) NOT NULL,
    config_id character varying(255) DEFAULT NULL::character varying,
    config_name character varying(255) DEFAULT NULL::character varying,
    warn_message text,
    warn_data text,
    belong_sn character varying(255) DEFAULT NULL::character varying,
    create_time timestamp(6) without time zone,
    warn_level character varying(255) DEFAULT '1'::character varying,
    status character varying(255) DEFAULT '0'::character varying,
    warn_type character varying(255) DEFAULT '0'::character varying
);


--
-- Name: TABLE labdatahub_warn_record; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.labdatahub_warn_record IS '告警记录表';


--
-- Name: COLUMN labdatahub_warn_record.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_record.id IS 'id';


--
-- Name: COLUMN labdatahub_warn_record.config_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_record.config_id IS '告警配置id';


--
-- Name: COLUMN labdatahub_warn_record.config_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_record.config_name IS '告警配置名称';


--
-- Name: COLUMN labdatahub_warn_record.warn_message; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_record.warn_message IS '告警内容';


--
-- Name: COLUMN labdatahub_warn_record.warn_data; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_record.warn_data IS '告警时全属性数据';


--
-- Name: COLUMN labdatahub_warn_record.belong_sn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_record.belong_sn IS '设备/产品sn';


--
-- Name: COLUMN labdatahub_warn_record.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_record.create_time IS '创建时间';


--
-- Name: COLUMN labdatahub_warn_record.warn_level; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_record.warn_level IS '告警等级 1-紧急 2-严重 3-一般 4-警告 5-正常';


--
-- Name: COLUMN labdatahub_warn_record.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_record.status IS '0-未处理 1-已处理';


--
-- Name: COLUMN labdatahub_warn_record.warn_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.labdatahub_warn_record.warn_type IS '类型 0-属性 1-上线 2-下线';


--
-- Name: qrtz_blob_triggers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_blob_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    blob_data bytea
);


--
-- Name: TABLE qrtz_blob_triggers; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.qrtz_blob_triggers IS 'Blob类型的触发器表';


--
-- Name: COLUMN qrtz_blob_triggers.sched_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_blob_triggers.sched_name IS '调度名称';


--
-- Name: COLUMN qrtz_blob_triggers.trigger_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_blob_triggers.trigger_name IS 'qrtz_triggers表trigger_name的外键';


--
-- Name: COLUMN qrtz_blob_triggers.trigger_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_blob_triggers.trigger_group IS 'qrtz_triggers表trigger_group的外键';


--
-- Name: COLUMN qrtz_blob_triggers.blob_data; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_blob_triggers.blob_data IS '存放持久化Trigger对象';


--
-- Name: qrtz_calendars; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_calendars (
    sched_name character varying(120) NOT NULL,
    calendar_name character varying(200) NOT NULL,
    calendar bytea NOT NULL
);


--
-- Name: TABLE qrtz_calendars; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.qrtz_calendars IS '日历信息表';


--
-- Name: COLUMN qrtz_calendars.sched_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_calendars.sched_name IS '调度名称';


--
-- Name: COLUMN qrtz_calendars.calendar_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_calendars.calendar_name IS '日历名称';


--
-- Name: COLUMN qrtz_calendars.calendar; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_calendars.calendar IS '存放持久化calendar对象';


--
-- Name: qrtz_cron_triggers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_cron_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    cron_expression character varying(200) NOT NULL,
    time_zone_id character varying(80) DEFAULT NULL::character varying
);


--
-- Name: TABLE qrtz_cron_triggers; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.qrtz_cron_triggers IS 'Cron类型的触发器表';


--
-- Name: COLUMN qrtz_cron_triggers.sched_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_cron_triggers.sched_name IS '调度名称';


--
-- Name: COLUMN qrtz_cron_triggers.trigger_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_cron_triggers.trigger_name IS 'qrtz_triggers表trigger_name的外键';


--
-- Name: COLUMN qrtz_cron_triggers.trigger_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_cron_triggers.trigger_group IS 'qrtz_triggers表trigger_group的外键';


--
-- Name: COLUMN qrtz_cron_triggers.cron_expression; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_cron_triggers.cron_expression IS 'cron表达式';


--
-- Name: COLUMN qrtz_cron_triggers.time_zone_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_cron_triggers.time_zone_id IS '时区';


--
-- Name: qrtz_fired_triggers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_fired_triggers (
    sched_name character varying(120) NOT NULL,
    entry_id character varying(95) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    instance_name character varying(200) NOT NULL,
    fired_time bigint NOT NULL,
    sched_time bigint NOT NULL,
    priority integer NOT NULL,
    state character varying(16) NOT NULL,
    job_name character varying(200) DEFAULT NULL::character varying,
    job_group character varying(200) DEFAULT NULL::character varying,
    is_nonconcurrent character varying(1) DEFAULT NULL::character varying,
    requests_recovery character varying(1) DEFAULT NULL::character varying
);


--
-- Name: TABLE qrtz_fired_triggers; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.qrtz_fired_triggers IS '已触发的触发器表';


--
-- Name: COLUMN qrtz_fired_triggers.sched_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_fired_triggers.sched_name IS '调度名称';


--
-- Name: COLUMN qrtz_fired_triggers.entry_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_fired_triggers.entry_id IS '调度器实例id';


--
-- Name: COLUMN qrtz_fired_triggers.trigger_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_fired_triggers.trigger_name IS 'qrtz_triggers表trigger_name的外键';


--
-- Name: COLUMN qrtz_fired_triggers.trigger_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_fired_triggers.trigger_group IS 'qrtz_triggers表trigger_group的外键';


--
-- Name: COLUMN qrtz_fired_triggers.instance_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_fired_triggers.instance_name IS '调度器实例名';


--
-- Name: COLUMN qrtz_fired_triggers.fired_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_fired_triggers.fired_time IS '触发的时间';


--
-- Name: COLUMN qrtz_fired_triggers.sched_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_fired_triggers.sched_time IS '定时器制定的时间';


--
-- Name: COLUMN qrtz_fired_triggers.priority; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_fired_triggers.priority IS '优先级';


--
-- Name: COLUMN qrtz_fired_triggers.state; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_fired_triggers.state IS '状态';


--
-- Name: COLUMN qrtz_fired_triggers.job_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_fired_triggers.job_name IS '任务名称';


--
-- Name: COLUMN qrtz_fired_triggers.job_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_fired_triggers.job_group IS '任务组名';


--
-- Name: COLUMN qrtz_fired_triggers.is_nonconcurrent; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_fired_triggers.is_nonconcurrent IS '是否并发';


--
-- Name: COLUMN qrtz_fired_triggers.requests_recovery; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_fired_triggers.requests_recovery IS '是否接受恢复执行';


--
-- Name: qrtz_job_details; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_job_details (
    sched_name character varying(120) NOT NULL,
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    description character varying(250) DEFAULT NULL::character varying,
    job_class_name character varying(250) NOT NULL,
    is_durable character varying(1) NOT NULL,
    is_nonconcurrent character varying(1) NOT NULL,
    is_update_data character varying(1) NOT NULL,
    requests_recovery character varying(1) NOT NULL,
    job_data bytea
);


--
-- Name: TABLE qrtz_job_details; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.qrtz_job_details IS '任务详细信息表';


--
-- Name: COLUMN qrtz_job_details.sched_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_job_details.sched_name IS '调度名称';


--
-- Name: COLUMN qrtz_job_details.job_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_job_details.job_name IS '任务名称';


--
-- Name: COLUMN qrtz_job_details.job_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_job_details.job_group IS '任务组名';


--
-- Name: COLUMN qrtz_job_details.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_job_details.description IS '相关介绍';


--
-- Name: COLUMN qrtz_job_details.job_class_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_job_details.job_class_name IS '执行任务类名称';


--
-- Name: COLUMN qrtz_job_details.is_durable; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_job_details.is_durable IS '是否持久化';


--
-- Name: COLUMN qrtz_job_details.is_nonconcurrent; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_job_details.is_nonconcurrent IS '是否并发';


--
-- Name: COLUMN qrtz_job_details.is_update_data; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_job_details.is_update_data IS '是否更新数据';


--
-- Name: COLUMN qrtz_job_details.requests_recovery; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_job_details.requests_recovery IS '是否接受恢复执行';


--
-- Name: COLUMN qrtz_job_details.job_data; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_job_details.job_data IS '存放持久化job对象';


--
-- Name: qrtz_locks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_locks (
    sched_name character varying(120) NOT NULL,
    lock_name character varying(40) NOT NULL
);


--
-- Name: TABLE qrtz_locks; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.qrtz_locks IS '存储的悲观锁信息表';


--
-- Name: COLUMN qrtz_locks.sched_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_locks.sched_name IS '调度名称';


--
-- Name: COLUMN qrtz_locks.lock_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_locks.lock_name IS '悲观锁名称';


--
-- Name: qrtz_paused_trigger_grps; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_paused_trigger_grps (
    sched_name character varying(120) NOT NULL,
    trigger_group character varying(200) NOT NULL
);


--
-- Name: TABLE qrtz_paused_trigger_grps; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.qrtz_paused_trigger_grps IS '暂停的触发器表';


--
-- Name: COLUMN qrtz_paused_trigger_grps.sched_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_paused_trigger_grps.sched_name IS '调度名称';


--
-- Name: COLUMN qrtz_paused_trigger_grps.trigger_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_paused_trigger_grps.trigger_group IS 'qrtz_triggers表trigger_group的外键';


--
-- Name: qrtz_scheduler_state; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_scheduler_state (
    sched_name character varying(120) NOT NULL,
    instance_name character varying(200) NOT NULL,
    last_checkin_time bigint NOT NULL,
    checkin_interval bigint NOT NULL
);


--
-- Name: TABLE qrtz_scheduler_state; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.qrtz_scheduler_state IS '调度器状态表';


--
-- Name: COLUMN qrtz_scheduler_state.sched_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_scheduler_state.sched_name IS '调度名称';


--
-- Name: COLUMN qrtz_scheduler_state.instance_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_scheduler_state.instance_name IS '实例名称';


--
-- Name: COLUMN qrtz_scheduler_state.last_checkin_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_scheduler_state.last_checkin_time IS '上次检查时间';


--
-- Name: COLUMN qrtz_scheduler_state.checkin_interval; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_scheduler_state.checkin_interval IS '检查间隔时间';


--
-- Name: qrtz_simple_triggers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_simple_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    repeat_count bigint NOT NULL,
    repeat_interval bigint NOT NULL,
    times_triggered bigint NOT NULL
);


--
-- Name: TABLE qrtz_simple_triggers; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.qrtz_simple_triggers IS '简单触发器的信息表';


--
-- Name: COLUMN qrtz_simple_triggers.sched_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simple_triggers.sched_name IS '调度名称';


--
-- Name: COLUMN qrtz_simple_triggers.trigger_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simple_triggers.trigger_name IS 'qrtz_triggers表trigger_name的外键';


--
-- Name: COLUMN qrtz_simple_triggers.trigger_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simple_triggers.trigger_group IS 'qrtz_triggers表trigger_group的外键';


--
-- Name: COLUMN qrtz_simple_triggers.repeat_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simple_triggers.repeat_count IS '重复的次数统计';


--
-- Name: COLUMN qrtz_simple_triggers.repeat_interval; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simple_triggers.repeat_interval IS '重复的间隔时间';


--
-- Name: COLUMN qrtz_simple_triggers.times_triggered; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simple_triggers.times_triggered IS '已经触发的次数';


--
-- Name: qrtz_simprop_triggers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_simprop_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    str_prop_1 character varying(512) DEFAULT NULL::character varying,
    str_prop_2 character varying(512) DEFAULT NULL::character varying,
    str_prop_3 character varying(512) DEFAULT NULL::character varying,
    int_prop_1 integer,
    int_prop_2 integer,
    long_prop_1 bigint,
    long_prop_2 bigint,
    dec_prop_1 numeric(13,4) DEFAULT NULL::numeric,
    dec_prop_2 numeric(13,4) DEFAULT NULL::numeric,
    bool_prop_1 character varying(1) DEFAULT NULL::character varying,
    bool_prop_2 character varying(1) DEFAULT NULL::character varying
);


--
-- Name: TABLE qrtz_simprop_triggers; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.qrtz_simprop_triggers IS '同步机制的行锁表';


--
-- Name: COLUMN qrtz_simprop_triggers.sched_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simprop_triggers.sched_name IS '调度名称';


--
-- Name: COLUMN qrtz_simprop_triggers.trigger_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simprop_triggers.trigger_name IS 'qrtz_triggers表trigger_name的外键';


--
-- Name: COLUMN qrtz_simprop_triggers.trigger_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simprop_triggers.trigger_group IS 'qrtz_triggers表trigger_group的外键';


--
-- Name: COLUMN qrtz_simprop_triggers.str_prop_1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simprop_triggers.str_prop_1 IS 'String类型的trigger的第一个参数';


--
-- Name: COLUMN qrtz_simprop_triggers.str_prop_2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simprop_triggers.str_prop_2 IS 'String类型的trigger的第二个参数';


--
-- Name: COLUMN qrtz_simprop_triggers.str_prop_3; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simprop_triggers.str_prop_3 IS 'String类型的trigger的第三个参数';


--
-- Name: COLUMN qrtz_simprop_triggers.int_prop_1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simprop_triggers.int_prop_1 IS 'int类型的trigger的第一个参数';


--
-- Name: COLUMN qrtz_simprop_triggers.int_prop_2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simprop_triggers.int_prop_2 IS 'int类型的trigger的第二个参数';


--
-- Name: COLUMN qrtz_simprop_triggers.long_prop_1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simprop_triggers.long_prop_1 IS 'long类型的trigger的第一个参数';


--
-- Name: COLUMN qrtz_simprop_triggers.long_prop_2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simprop_triggers.long_prop_2 IS 'long类型的trigger的第二个参数';


--
-- Name: COLUMN qrtz_simprop_triggers.dec_prop_1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simprop_triggers.dec_prop_1 IS 'decimal类型的trigger的第一个参数';


--
-- Name: COLUMN qrtz_simprop_triggers.dec_prop_2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simprop_triggers.dec_prop_2 IS 'decimal类型的trigger的第二个参数';


--
-- Name: COLUMN qrtz_simprop_triggers.bool_prop_1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simprop_triggers.bool_prop_1 IS 'Boolean类型的trigger的第一个参数';


--
-- Name: COLUMN qrtz_simprop_triggers.bool_prop_2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_simprop_triggers.bool_prop_2 IS 'Boolean类型的trigger的第二个参数';


--
-- Name: qrtz_triggers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qrtz_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    description character varying(250) DEFAULT NULL::character varying,
    next_fire_time bigint,
    prev_fire_time bigint,
    priority integer,
    trigger_state character varying(16) NOT NULL,
    trigger_type character varying(8) NOT NULL,
    start_time bigint NOT NULL,
    end_time bigint,
    calendar_name character varying(200) DEFAULT NULL::character varying,
    misfire_instr smallint,
    job_data bytea
);


--
-- Name: TABLE qrtz_triggers; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.qrtz_triggers IS '触发器详细信息表';


--
-- Name: COLUMN qrtz_triggers.sched_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.sched_name IS '调度名称';


--
-- Name: COLUMN qrtz_triggers.trigger_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.trigger_name IS '触发器的名字';


--
-- Name: COLUMN qrtz_triggers.trigger_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.trigger_group IS '触发器所属组的名字';


--
-- Name: COLUMN qrtz_triggers.job_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.job_name IS 'qrtz_job_details表job_name的外键';


--
-- Name: COLUMN qrtz_triggers.job_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.job_group IS 'qrtz_job_details表job_group的外键';


--
-- Name: COLUMN qrtz_triggers.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.description IS '相关介绍';


--
-- Name: COLUMN qrtz_triggers.next_fire_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.next_fire_time IS '上一次触发时间（毫秒）';


--
-- Name: COLUMN qrtz_triggers.prev_fire_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.prev_fire_time IS '下一次触发时间（默认为-1表示不触发）';


--
-- Name: COLUMN qrtz_triggers.priority; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.priority IS '优先级';


--
-- Name: COLUMN qrtz_triggers.trigger_state; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.trigger_state IS '触发器状态';


--
-- Name: COLUMN qrtz_triggers.trigger_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.trigger_type IS '触发器的类型';


--
-- Name: COLUMN qrtz_triggers.start_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.start_time IS '开始时间';


--
-- Name: COLUMN qrtz_triggers.end_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.end_time IS '结束时间';


--
-- Name: COLUMN qrtz_triggers.calendar_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.calendar_name IS '日程表名称';


--
-- Name: COLUMN qrtz_triggers.misfire_instr; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.misfire_instr IS '补偿执行的策略';


--
-- Name: COLUMN qrtz_triggers.job_data; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.qrtz_triggers.job_data IS '存放持久化job对象';


--
-- Name: sys_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_config (
    config_id integer NOT NULL,
    config_name character varying(100) DEFAULT ''::character varying,
    config_key character varying(100) DEFAULT ''::character varying,
    config_value character varying(500) DEFAULT ''::character varying,
    config_type character(1) DEFAULT 'N'::bpchar,
    create_by character varying(64) DEFAULT ''::character varying,
    create_time timestamp(6) without time zone,
    update_by character varying(64) DEFAULT ''::character varying,
    update_time timestamp(6) without time zone,
    remark character varying(500) DEFAULT NULL::character varying
);


--
-- Name: TABLE sys_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_config IS '参数配置表';


--
-- Name: COLUMN sys_config.config_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_config.config_id IS '参数主键';


--
-- Name: COLUMN sys_config.config_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_config.config_name IS '参数名称';


--
-- Name: COLUMN sys_config.config_key; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_config.config_key IS '参数键名';


--
-- Name: COLUMN sys_config.config_value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_config.config_value IS '参数键值';


--
-- Name: COLUMN sys_config.config_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_config.config_type IS '系统内置（Y是 N否）';


--
-- Name: COLUMN sys_config.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_config.create_by IS '创建者';


--
-- Name: COLUMN sys_config.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_config.create_time IS '创建时间';


--
-- Name: COLUMN sys_config.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_config.update_by IS '更新者';


--
-- Name: COLUMN sys_config.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_config.update_time IS '更新时间';


--
-- Name: COLUMN sys_config.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_config.remark IS '备注';


--
-- Name: sys_config_config_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_config_config_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;


--
-- Name: sys_config_config_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_config_config_id_seq OWNED BY public.sys_config.config_id;


--
-- Name: sys_dept; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_dept (
    dept_id bigint NOT NULL,
    parent_id bigint DEFAULT 0,
    ancestors character varying(50) DEFAULT ''::character varying,
    dept_name character varying(30) DEFAULT ''::character varying,
    order_num integer DEFAULT 0,
    leader character varying(20) DEFAULT NULL::character varying,
    phone character varying(11) DEFAULT NULL::character varying,
    email character varying(50) DEFAULT NULL::character varying,
    status character(1) DEFAULT '0'::bpchar,
    del_flag character(1) DEFAULT '0'::bpchar,
    create_by character varying(64) DEFAULT ''::character varying,
    create_time timestamp(6) without time zone,
    update_by character varying(64) DEFAULT ''::character varying,
    update_time timestamp(6) without time zone
);


--
-- Name: TABLE sys_dept; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_dept IS '部门表';


--
-- Name: COLUMN sys_dept.dept_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dept.dept_id IS '部门id';


--
-- Name: COLUMN sys_dept.parent_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dept.parent_id IS '父部门id';


--
-- Name: COLUMN sys_dept.ancestors; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dept.ancestors IS '祖级列表';


--
-- Name: COLUMN sys_dept.dept_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dept.dept_name IS '部门名称';


--
-- Name: COLUMN sys_dept.order_num; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dept.order_num IS '显示顺序';


--
-- Name: COLUMN sys_dept.leader; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dept.leader IS '负责人';


--
-- Name: COLUMN sys_dept.phone; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dept.phone IS '联系电话';


--
-- Name: COLUMN sys_dept.email; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dept.email IS '邮箱';


--
-- Name: COLUMN sys_dept.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dept.status IS '部门状态（0正常 1停用）';


--
-- Name: COLUMN sys_dept.del_flag; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dept.del_flag IS '删除标志（0代表存在 2代表删除）';


--
-- Name: COLUMN sys_dept.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dept.create_by IS '创建者';


--
-- Name: COLUMN sys_dept.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dept.create_time IS '创建时间';


--
-- Name: COLUMN sys_dept.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dept.update_by IS '更新者';


--
-- Name: COLUMN sys_dept.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dept.update_time IS '更新时间';


--
-- Name: sys_dept_dept_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_dept_dept_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_dept_dept_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_dept_dept_id_seq OWNED BY public.sys_dept.dept_id;


--
-- Name: sys_dict_data; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_dict_data (
    dict_code bigint NOT NULL,
    dict_sort integer DEFAULT 0,
    dict_label character varying(100) DEFAULT ''::character varying,
    dict_value character varying(100) DEFAULT ''::character varying,
    dict_type character varying(100) DEFAULT ''::character varying,
    css_class character varying(100) DEFAULT NULL::character varying,
    list_class character varying(100) DEFAULT NULL::character varying,
    is_default character(1) DEFAULT 'N'::bpchar,
    status character(1) DEFAULT '0'::bpchar,
    create_by character varying(64) DEFAULT ''::character varying,
    create_time timestamp(6) without time zone,
    update_by character varying(64) DEFAULT ''::character varying,
    update_time timestamp(6) without time zone,
    remark character varying(500) DEFAULT NULL::character varying
);


--
-- Name: TABLE sys_dict_data; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_dict_data IS '字典数据表';


--
-- Name: COLUMN sys_dict_data.dict_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_data.dict_code IS '字典编码';


--
-- Name: COLUMN sys_dict_data.dict_sort; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_data.dict_sort IS '字典排序';


--
-- Name: COLUMN sys_dict_data.dict_label; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_data.dict_label IS '字典标签';


--
-- Name: COLUMN sys_dict_data.dict_value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_data.dict_value IS '字典键值';


--
-- Name: COLUMN sys_dict_data.dict_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_data.dict_type IS '字典类型';


--
-- Name: COLUMN sys_dict_data.css_class; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_data.css_class IS '样式属性（其他样式扩展）';


--
-- Name: COLUMN sys_dict_data.list_class; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_data.list_class IS '表格回显样式';


--
-- Name: COLUMN sys_dict_data.is_default; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_data.is_default IS '是否默认（Y是 N否）';


--
-- Name: COLUMN sys_dict_data.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_data.status IS '状态（0正常 1停用）';


--
-- Name: COLUMN sys_dict_data.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_data.create_by IS '创建者';


--
-- Name: COLUMN sys_dict_data.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_data.create_time IS '创建时间';


--
-- Name: COLUMN sys_dict_data.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_data.update_by IS '更新者';


--
-- Name: COLUMN sys_dict_data.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_data.update_time IS '更新时间';


--
-- Name: COLUMN sys_dict_data.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_data.remark IS '备注';


--
-- Name: sys_dict_data_dict_code_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_dict_data_dict_code_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_dict_data_dict_code_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_dict_data_dict_code_seq OWNED BY public.sys_dict_data.dict_code;


--
-- Name: sys_dict_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_dict_type (
    dict_id bigint NOT NULL,
    dict_name character varying(100) DEFAULT ''::character varying,
    dict_type character varying(100) DEFAULT ''::character varying,
    status character(1) DEFAULT '0'::bpchar,
    create_by character varying(64) DEFAULT ''::character varying,
    create_time timestamp(6) without time zone,
    update_by character varying(64) DEFAULT ''::character varying,
    update_time timestamp(6) without time zone,
    remark character varying(500) DEFAULT NULL::character varying
);


--
-- Name: TABLE sys_dict_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_dict_type IS '字典类型表';


--
-- Name: COLUMN sys_dict_type.dict_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_type.dict_id IS '字典主键';


--
-- Name: COLUMN sys_dict_type.dict_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_type.dict_name IS '字典名称';


--
-- Name: COLUMN sys_dict_type.dict_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_type.dict_type IS '字典类型';


--
-- Name: COLUMN sys_dict_type.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_type.status IS '状态（0正常 1停用）';


--
-- Name: COLUMN sys_dict_type.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_type.create_by IS '创建者';


--
-- Name: COLUMN sys_dict_type.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_type.create_time IS '创建时间';


--
-- Name: COLUMN sys_dict_type.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_type.update_by IS '更新者';


--
-- Name: COLUMN sys_dict_type.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_type.update_time IS '更新时间';


--
-- Name: COLUMN sys_dict_type.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_dict_type.remark IS '备注';


--
-- Name: sys_dict_type_dict_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_dict_type_dict_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_dict_type_dict_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_dict_type_dict_id_seq OWNED BY public.sys_dict_type.dict_id;


--
-- Name: sys_job; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_job (
    job_id bigint NOT NULL,
    job_name character varying(64) DEFAULT ''::character varying NOT NULL,
    job_group character varying(64) DEFAULT 'DEFAULT'::character varying NOT NULL,
    invoke_target character varying(500) NOT NULL,
    cron_expression character varying(255) DEFAULT ''::character varying,
    misfire_policy character varying(20) DEFAULT '3'::character varying,
    concurrent character(1) DEFAULT '1'::bpchar,
    status character(1) DEFAULT '0'::bpchar,
    create_by character varying(64) DEFAULT ''::character varying,
    create_time timestamp(6) without time zone,
    update_by character varying(64) DEFAULT ''::character varying,
    update_time timestamp(6) without time zone,
    remark character varying(500) DEFAULT ''::character varying
);


--
-- Name: TABLE sys_job; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_job IS '定时任务调度表';


--
-- Name: COLUMN sys_job.job_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job.job_id IS '任务ID';


--
-- Name: COLUMN sys_job.job_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job.job_name IS '任务名称';


--
-- Name: COLUMN sys_job.job_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job.job_group IS '任务组名';


--
-- Name: COLUMN sys_job.invoke_target; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job.invoke_target IS '调用目标字符串';


--
-- Name: COLUMN sys_job.cron_expression; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job.cron_expression IS 'cron执行表达式';


--
-- Name: COLUMN sys_job.misfire_policy; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job.misfire_policy IS '计划执行错误策略（1立即执行 2执行一次 3放弃执行）';


--
-- Name: COLUMN sys_job.concurrent; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job.concurrent IS '是否并发执行（0允许 1禁止）';


--
-- Name: COLUMN sys_job.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job.status IS '状态（0正常 1暂停）';


--
-- Name: COLUMN sys_job.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job.create_by IS '创建者';


--
-- Name: COLUMN sys_job.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job.create_time IS '创建时间';


--
-- Name: COLUMN sys_job.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job.update_by IS '更新者';


--
-- Name: COLUMN sys_job.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job.update_time IS '更新时间';


--
-- Name: COLUMN sys_job.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job.remark IS '备注信息';


--
-- Name: sys_job_job_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_job_job_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_job_job_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_job_job_id_seq OWNED BY public.sys_job.job_id;


--
-- Name: sys_job_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_job_log (
    job_log_id bigint NOT NULL,
    job_name character varying(64) NOT NULL,
    job_group character varying(64) NOT NULL,
    invoke_target character varying(500) NOT NULL,
    job_message character varying(500) DEFAULT NULL::character varying,
    status character(1) DEFAULT '0'::bpchar,
    exception_info character varying(2000) DEFAULT ''::character varying,
    create_time timestamp(6) without time zone
);


--
-- Name: TABLE sys_job_log; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_job_log IS '定时任务调度日志表';


--
-- Name: COLUMN sys_job_log.job_log_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job_log.job_log_id IS '任务日志ID';


--
-- Name: COLUMN sys_job_log.job_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job_log.job_name IS '任务名称';


--
-- Name: COLUMN sys_job_log.job_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job_log.job_group IS '任务组名';


--
-- Name: COLUMN sys_job_log.invoke_target; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job_log.invoke_target IS '调用目标字符串';


--
-- Name: COLUMN sys_job_log.job_message; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job_log.job_message IS '日志信息';


--
-- Name: COLUMN sys_job_log.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job_log.status IS '执行状态（0正常 1失败）';


--
-- Name: COLUMN sys_job_log.exception_info; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job_log.exception_info IS '异常信息';


--
-- Name: COLUMN sys_job_log.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_job_log.create_time IS '创建时间';


--
-- Name: sys_job_log_job_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_job_log_job_log_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_job_log_job_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_job_log_job_log_id_seq OWNED BY public.sys_job_log.job_log_id;


--
-- Name: sys_logininfor; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_logininfor (
    info_id bigint NOT NULL,
    user_name character varying(50) DEFAULT ''::character varying,
    ipaddr character varying(128) DEFAULT ''::character varying,
    login_location character varying(255) DEFAULT ''::character varying,
    browser character varying(50) DEFAULT ''::character varying,
    os character varying(50) DEFAULT ''::character varying,
    status character(1) DEFAULT '0'::bpchar,
    msg character varying(255) DEFAULT ''::character varying,
    login_time timestamp(6) without time zone
);


--
-- Name: TABLE sys_logininfor; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_logininfor IS '系统访问记录';


--
-- Name: COLUMN sys_logininfor.info_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_logininfor.info_id IS '访问ID';


--
-- Name: COLUMN sys_logininfor.user_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_logininfor.user_name IS '用户账号';


--
-- Name: COLUMN sys_logininfor.ipaddr; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_logininfor.ipaddr IS '登录IP地址';


--
-- Name: COLUMN sys_logininfor.login_location; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_logininfor.login_location IS '登录地点';


--
-- Name: COLUMN sys_logininfor.browser; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_logininfor.browser IS '浏览器类型';


--
-- Name: COLUMN sys_logininfor.os; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_logininfor.os IS '操作系统';


--
-- Name: COLUMN sys_logininfor.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_logininfor.status IS '登录状态（0成功 1失败）';


--
-- Name: COLUMN sys_logininfor.msg; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_logininfor.msg IS '提示消息';


--
-- Name: COLUMN sys_logininfor.login_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_logininfor.login_time IS '访问时间';


--
-- Name: sys_logininfor_info_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_logininfor_info_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_logininfor_info_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_logininfor_info_id_seq OWNED BY public.sys_logininfor.info_id;


--
-- Name: sys_menu; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_menu (
    menu_id bigint NOT NULL,
    menu_name character varying(50) NOT NULL,
    parent_id bigint DEFAULT 0,
    order_num integer DEFAULT 0,
    path character varying(200) DEFAULT ''::character varying,
    component character varying(255) DEFAULT NULL::character varying,
    query character varying(255) DEFAULT NULL::character varying,
    route_name character varying(50) DEFAULT ''::character varying,
    is_frame character varying(32) DEFAULT 1,
    is_cache character varying(32) DEFAULT 0,
    menu_type character(1) DEFAULT ''::bpchar,
    visible character(1) DEFAULT '0'::bpchar,
    status character(1) DEFAULT '0'::bpchar,
    perms character varying(100) DEFAULT NULL::character varying,
    icon character varying(100) DEFAULT '#'::character varying,
    create_by character varying(64) DEFAULT ''::character varying,
    create_time timestamp(6) without time zone,
    update_by character varying(64) DEFAULT ''::character varying,
    update_time timestamp(6) without time zone,
    remark character varying(500) DEFAULT ''::character varying
);


--
-- Name: TABLE sys_menu; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_menu IS '菜单权限表';


--
-- Name: COLUMN sys_menu.menu_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.menu_id IS '菜单ID';


--
-- Name: COLUMN sys_menu.menu_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.menu_name IS '菜单名称';


--
-- Name: COLUMN sys_menu.parent_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.parent_id IS '父菜单ID';


--
-- Name: COLUMN sys_menu.order_num; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.order_num IS '显示顺序';


--
-- Name: COLUMN sys_menu.path; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.path IS '路由地址';


--
-- Name: COLUMN sys_menu.component; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.component IS '组件路径';


--
-- Name: COLUMN sys_menu.query; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.query IS '路由参数';


--
-- Name: COLUMN sys_menu.route_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.route_name IS '路由名称';


--
-- Name: COLUMN sys_menu.is_frame; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.is_frame IS '是否为外链（0是 1否）';


--
-- Name: COLUMN sys_menu.is_cache; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.is_cache IS '是否缓存（0缓存 1不缓存）';


--
-- Name: COLUMN sys_menu.menu_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.menu_type IS '菜单类型（M目录 C菜单 F按钮）';


--
-- Name: COLUMN sys_menu.visible; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.visible IS '菜单状态（0显示 1隐藏）';


--
-- Name: COLUMN sys_menu.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.status IS '菜单状态（0正常 1停用）';


--
-- Name: COLUMN sys_menu.perms; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.perms IS '权限标识';


--
-- Name: COLUMN sys_menu.icon; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.icon IS '菜单图标';


--
-- Name: COLUMN sys_menu.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.create_by IS '创建者';


--
-- Name: COLUMN sys_menu.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.create_time IS '创建时间';


--
-- Name: COLUMN sys_menu.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.update_by IS '更新者';


--
-- Name: COLUMN sys_menu.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.update_time IS '更新时间';


--
-- Name: COLUMN sys_menu.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_menu.remark IS '备注';


--
-- Name: sys_menu_menu_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_menu_menu_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_menu_menu_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_menu_menu_id_seq OWNED BY public.sys_menu.menu_id;


--
-- Name: sys_notice; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_notice (
    notice_id integer NOT NULL,
    notice_title character varying(50) NOT NULL,
    notice_type character(1) NOT NULL,
    notice_content bytea,
    status character(1) DEFAULT '0'::bpchar,
    create_by character varying(64) DEFAULT ''::character varying,
    create_time timestamp(6) without time zone,
    update_by character varying(64) DEFAULT ''::character varying,
    update_time timestamp(6) without time zone,
    remark character varying(255) DEFAULT NULL::character varying
);


--
-- Name: TABLE sys_notice; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_notice IS '通知公告表';


--
-- Name: COLUMN sys_notice.notice_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_notice.notice_id IS '公告ID';


--
-- Name: COLUMN sys_notice.notice_title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_notice.notice_title IS '公告标题';


--
-- Name: COLUMN sys_notice.notice_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_notice.notice_type IS '公告类型（1通知 2公告）';


--
-- Name: COLUMN sys_notice.notice_content; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_notice.notice_content IS '公告内容';


--
-- Name: COLUMN sys_notice.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_notice.status IS '公告状态（0正常 1关闭）';


--
-- Name: COLUMN sys_notice.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_notice.create_by IS '创建者';


--
-- Name: COLUMN sys_notice.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_notice.create_time IS '创建时间';


--
-- Name: COLUMN sys_notice.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_notice.update_by IS '更新者';


--
-- Name: COLUMN sys_notice.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_notice.update_time IS '更新时间';


--
-- Name: COLUMN sys_notice.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_notice.remark IS '备注';


--
-- Name: sys_notice_notice_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_notice_notice_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;


--
-- Name: sys_notice_notice_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_notice_notice_id_seq OWNED BY public.sys_notice.notice_id;


--
-- Name: sys_oper_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_oper_log (
    oper_id bigint NOT NULL,
    title character varying(50) DEFAULT ''::character varying,
    business_type integer DEFAULT 0,
    method character varying(200) DEFAULT ''::character varying,
    request_method character varying(10) DEFAULT ''::character varying,
    operator_type integer DEFAULT 0,
    oper_name character varying(50) DEFAULT ''::character varying,
    dept_name character varying(50) DEFAULT ''::character varying,
    oper_url character varying(255) DEFAULT ''::character varying,
    oper_ip character varying(128) DEFAULT ''::character varying,
    oper_location character varying(255) DEFAULT ''::character varying,
    oper_param character varying(2000) DEFAULT ''::character varying,
    json_result character varying(2000) DEFAULT ''::character varying,
    status integer DEFAULT 0,
    error_msg character varying(2000) DEFAULT ''::character varying,
    oper_time timestamp(6) without time zone,
    cost_time bigint DEFAULT 0
);


--
-- Name: TABLE sys_oper_log; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_oper_log IS '操作日志记录';


--
-- Name: COLUMN sys_oper_log.oper_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.oper_id IS '日志主键';


--
-- Name: COLUMN sys_oper_log.title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.title IS '模块标题';


--
-- Name: COLUMN sys_oper_log.business_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.business_type IS '业务类型（0其它 1新增 2修改 3删除）';


--
-- Name: COLUMN sys_oper_log.method; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.method IS '方法名称';


--
-- Name: COLUMN sys_oper_log.request_method; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.request_method IS '请求方式';


--
-- Name: COLUMN sys_oper_log.operator_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.operator_type IS '操作类别（0其它 1后台用户 2手机端用户）';


--
-- Name: COLUMN sys_oper_log.oper_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.oper_name IS '操作人员';


--
-- Name: COLUMN sys_oper_log.dept_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.dept_name IS '部门名称';


--
-- Name: COLUMN sys_oper_log.oper_url; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.oper_url IS '请求URL';


--
-- Name: COLUMN sys_oper_log.oper_ip; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.oper_ip IS '主机地址';


--
-- Name: COLUMN sys_oper_log.oper_location; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.oper_location IS '操作地点';


--
-- Name: COLUMN sys_oper_log.oper_param; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.oper_param IS '请求参数';


--
-- Name: COLUMN sys_oper_log.json_result; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.json_result IS '返回参数';


--
-- Name: COLUMN sys_oper_log.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.status IS '操作状态（0正常 1异常）';


--
-- Name: COLUMN sys_oper_log.error_msg; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.error_msg IS '错误消息';


--
-- Name: COLUMN sys_oper_log.oper_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.oper_time IS '操作时间';


--
-- Name: COLUMN sys_oper_log.cost_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_oper_log.cost_time IS '消耗时间';


--
-- Name: sys_oper_log_oper_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_oper_log_oper_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_oper_log_oper_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_oper_log_oper_id_seq OWNED BY public.sys_oper_log.oper_id;


--
-- Name: sys_post; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_post (
    post_id bigint NOT NULL,
    post_code character varying(64) NOT NULL,
    post_name character varying(50) NOT NULL,
    post_sort integer NOT NULL,
    status character(1) NOT NULL,
    create_by character varying(64) DEFAULT ''::character varying,
    create_time timestamp(6) without time zone,
    update_by character varying(64) DEFAULT ''::character varying,
    update_time timestamp(6) without time zone,
    remark character varying(500) DEFAULT NULL::character varying
);


--
-- Name: TABLE sys_post; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_post IS '岗位信息表';


--
-- Name: COLUMN sys_post.post_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_post.post_id IS '岗位ID';


--
-- Name: COLUMN sys_post.post_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_post.post_code IS '岗位编码';


--
-- Name: COLUMN sys_post.post_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_post.post_name IS '岗位名称';


--
-- Name: COLUMN sys_post.post_sort; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_post.post_sort IS '显示顺序';


--
-- Name: COLUMN sys_post.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_post.status IS '状态（0正常 1停用）';


--
-- Name: COLUMN sys_post.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_post.create_by IS '创建者';


--
-- Name: COLUMN sys_post.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_post.create_time IS '创建时间';


--
-- Name: COLUMN sys_post.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_post.update_by IS '更新者';


--
-- Name: COLUMN sys_post.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_post.update_time IS '更新时间';


--
-- Name: COLUMN sys_post.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_post.remark IS '备注';


--
-- Name: sys_post_post_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_post_post_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_post_post_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_post_post_id_seq OWNED BY public.sys_post.post_id;


--
-- Name: sys_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_role (
    role_id bigint NOT NULL,
    role_name character varying(30) NOT NULL,
    role_key character varying(100) NOT NULL,
    role_sort integer NOT NULL,
    data_scope character(1) DEFAULT '1'::bpchar,
    menu_check_strictly boolean DEFAULT true,
    dept_check_strictly boolean DEFAULT true,
    status character(1) NOT NULL,
    del_flag character(1) DEFAULT '0'::bpchar,
    create_by character varying(64) DEFAULT ''::character varying,
    create_time timestamp(6) without time zone,
    update_by character varying(64) DEFAULT ''::character varying,
    update_time timestamp(6) without time zone,
    remark character varying(500) DEFAULT NULL::character varying
);


--
-- Name: TABLE sys_role; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_role IS '角色信息表';


--
-- Name: COLUMN sys_role.role_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role.role_id IS '角色ID';


--
-- Name: COLUMN sys_role.role_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role.role_name IS '角色名称';


--
-- Name: COLUMN sys_role.role_key; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role.role_key IS '角色权限字符串';


--
-- Name: COLUMN sys_role.role_sort; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role.role_sort IS '显示顺序';


--
-- Name: COLUMN sys_role.data_scope; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role.data_scope IS '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）';


--
-- Name: COLUMN sys_role.menu_check_strictly; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role.menu_check_strictly IS '菜单树选择项是否关联显示';


--
-- Name: COLUMN sys_role.dept_check_strictly; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role.dept_check_strictly IS '部门树选择项是否关联显示';


--
-- Name: COLUMN sys_role.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role.status IS '角色状态（0正常 1停用）';


--
-- Name: COLUMN sys_role.del_flag; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role.del_flag IS '删除标志（0代表存在 2代表删除）';


--
-- Name: COLUMN sys_role.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role.create_by IS '创建者';


--
-- Name: COLUMN sys_role.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role.create_time IS '创建时间';


--
-- Name: COLUMN sys_role.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role.update_by IS '更新者';


--
-- Name: COLUMN sys_role.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role.update_time IS '更新时间';


--
-- Name: COLUMN sys_role.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role.remark IS '备注';


--
-- Name: sys_role_dept; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_role_dept (
    role_id bigint NOT NULL,
    dept_id bigint NOT NULL
);


--
-- Name: TABLE sys_role_dept; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_role_dept IS '角色和部门关联表';


--
-- Name: COLUMN sys_role_dept.role_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role_dept.role_id IS '角色ID';


--
-- Name: COLUMN sys_role_dept.dept_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role_dept.dept_id IS '部门ID';


--
-- Name: sys_role_menu; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_role_menu (
    role_id bigint NOT NULL,
    menu_id bigint NOT NULL
);


--
-- Name: TABLE sys_role_menu; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_role_menu IS '角色和菜单关联表';


--
-- Name: COLUMN sys_role_menu.role_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role_menu.role_id IS '角色ID';


--
-- Name: COLUMN sys_role_menu.menu_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_role_menu.menu_id IS '菜单ID';


--
-- Name: sys_role_role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_role_role_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_role_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_role_role_id_seq OWNED BY public.sys_role.role_id;


--
-- Name: sys_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_user (
    user_id bigint NOT NULL,
    dept_id bigint,
    user_name character varying(30) NOT NULL,
    nick_name character varying(30) NOT NULL,
    user_type character varying(2) DEFAULT '00'::character varying,
    email character varying(50) DEFAULT ''::character varying,
    phonenumber character varying(11) DEFAULT ''::character varying,
    sex character(1) DEFAULT '0'::bpchar,
    avatar character varying(100) DEFAULT ''::character varying,
    password character varying(100) DEFAULT ''::character varying,
    status character(1) DEFAULT '0'::bpchar,
    del_flag character(1) DEFAULT '0'::bpchar,
    login_ip character varying(128) DEFAULT ''::character varying,
    login_date timestamp(6) without time zone,
    pwd_update_date timestamp(6) without time zone,
    create_by character varying(64) DEFAULT ''::character varying,
    create_time timestamp(6) without time zone,
    update_by character varying(64) DEFAULT ''::character varying,
    update_time timestamp(6) without time zone,
    remark character varying(500) DEFAULT NULL::character varying
);


--
-- Name: TABLE sys_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_user IS '用户信息表';


--
-- Name: COLUMN sys_user.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.user_id IS '用户ID';


--
-- Name: COLUMN sys_user.dept_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.dept_id IS '部门ID';


--
-- Name: COLUMN sys_user.user_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.user_name IS '用户账号';


--
-- Name: COLUMN sys_user.nick_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.nick_name IS '用户昵称';


--
-- Name: COLUMN sys_user.user_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.user_type IS '用户类型（00系统用户）';


--
-- Name: COLUMN sys_user.email; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.email IS '用户邮箱';


--
-- Name: COLUMN sys_user.phonenumber; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.phonenumber IS '手机号码';


--
-- Name: COLUMN sys_user.sex; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.sex IS '用户性别（0男 1女 2未知）';


--
-- Name: COLUMN sys_user.avatar; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.avatar IS '头像地址';


--
-- Name: COLUMN sys_user.password; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.password IS '密码';


--
-- Name: COLUMN sys_user.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.status IS '账号状态（0正常 1停用）';


--
-- Name: COLUMN sys_user.del_flag; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.del_flag IS '删除标志（0代表存在 2代表删除）';


--
-- Name: COLUMN sys_user.login_ip; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.login_ip IS '最后登录IP';


--
-- Name: COLUMN sys_user.login_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.login_date IS '最后登录时间';


--
-- Name: COLUMN sys_user.pwd_update_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.pwd_update_date IS '密码最后更新时间';


--
-- Name: COLUMN sys_user.create_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.create_by IS '创建者';


--
-- Name: COLUMN sys_user.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.create_time IS '创建时间';


--
-- Name: COLUMN sys_user.update_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.update_by IS '更新者';


--
-- Name: COLUMN sys_user.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.update_time IS '更新时间';


--
-- Name: COLUMN sys_user.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.remark IS '备注';


--
-- Name: sys_user_post; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_user_post (
    user_id bigint NOT NULL,
    post_id bigint NOT NULL
);


--
-- Name: TABLE sys_user_post; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_user_post IS '用户与岗位关联表';


--
-- Name: COLUMN sys_user_post.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user_post.user_id IS '用户ID';


--
-- Name: COLUMN sys_user_post.post_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user_post.post_id IS '岗位ID';


--
-- Name: sys_user_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_user_role (
    user_id bigint NOT NULL,
    role_id bigint NOT NULL
);


--
-- Name: TABLE sys_user_role; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_user_role IS '用户和角色关联表';


--
-- Name: COLUMN sys_user_role.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user_role.user_id IS '用户ID';


--
-- Name: COLUMN sys_user_role.role_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user_role.role_id IS '角色ID';


--
-- Name: sys_user_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_user_user_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_user_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_user_user_id_seq OWNED BY public.sys_user.user_id;


--
-- Name: gen_table table_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gen_table ALTER COLUMN table_id SET DEFAULT nextval('public.gen_table_table_id_seq'::regclass);


--
-- Name: gen_table_column column_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gen_table_column ALTER COLUMN column_id SET DEFAULT nextval('public.gen_table_column_column_id_seq'::regclass);


--
-- Name: labdatahub_point_write_record id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_point_write_record ALTER COLUMN id SET DEFAULT nextval('public.labdatahub_point_write_record_id_seq'::regclass);


--
-- Name: sys_config config_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_config ALTER COLUMN config_id SET DEFAULT nextval('public.sys_config_config_id_seq'::regclass);


--
-- Name: sys_dept dept_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_dept ALTER COLUMN dept_id SET DEFAULT nextval('public.sys_dept_dept_id_seq'::regclass);


--
-- Name: sys_dict_data dict_code; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_dict_data ALTER COLUMN dict_code SET DEFAULT nextval('public.sys_dict_data_dict_code_seq'::regclass);


--
-- Name: sys_dict_type dict_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_dict_type ALTER COLUMN dict_id SET DEFAULT nextval('public.sys_dict_type_dict_id_seq'::regclass);


--
-- Name: sys_job job_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_job ALTER COLUMN job_id SET DEFAULT nextval('public.sys_job_job_id_seq'::regclass);


--
-- Name: sys_job_log job_log_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_job_log ALTER COLUMN job_log_id SET DEFAULT nextval('public.sys_job_log_job_log_id_seq'::regclass);


--
-- Name: sys_logininfor info_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_logininfor ALTER COLUMN info_id SET DEFAULT nextval('public.sys_logininfor_info_id_seq'::regclass);


--
-- Name: sys_menu menu_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_menu ALTER COLUMN menu_id SET DEFAULT nextval('public.sys_menu_menu_id_seq'::regclass);


--
-- Name: sys_notice notice_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_notice ALTER COLUMN notice_id SET DEFAULT nextval('public.sys_notice_notice_id_seq'::regclass);


--
-- Name: sys_oper_log oper_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_oper_log ALTER COLUMN oper_id SET DEFAULT nextval('public.sys_oper_log_oper_id_seq'::regclass);


--
-- Name: sys_post post_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_post ALTER COLUMN post_id SET DEFAULT nextval('public.sys_post_post_id_seq'::regclass);


--
-- Name: sys_role role_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_role ALTER COLUMN role_id SET DEFAULT nextval('public.sys_role_role_id_seq'::regclass);


--
-- Name: sys_user user_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_user ALTER COLUMN user_id SET DEFAULT nextval('public.sys_user_user_id_seq'::regclass);


--
-- Data for Name: gen_table; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.gen_table VALUES (1, 'labdatahub_component', '网络组件', NULL, NULL, 'LabdatahubComponent', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'component', '网络组件', 'ruoyi', '0', '/', '{}', 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18', NULL);
INSERT INTO public.gen_table VALUES (2, 'labdatahub_product', '产品表', NULL, NULL, 'LabdatahubProduct', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'product', '产品', 'ruoyi', '0', '/', '{}', 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26', NULL);
INSERT INTO public.gen_table VALUES (3, 'labdatahub_properties', '物模型属性定义表', NULL, NULL, 'LabdatahubProperties', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'properties', '物模型属性定义', 'ruoyi', '0', '/', '{}', 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33', NULL);
INSERT INTO public.gen_table VALUES (4, 'labdatahub_protocol', '协议管理', NULL, NULL, 'LabdatahubProtocol', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'protocol', '协议管理', 'ruoyi', '0', '/', '{}', 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39', NULL);
INSERT INTO public.gen_table VALUES (5, 'labdatahub', '设备表', NULL, NULL, 'ThingllinksDevice', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'device', '设备', 'ruoyi', '0', '/', '{}', 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45', NULL);
INSERT INTO public.gen_table VALUES (6, 'labdatahub_device_logs', '设备日志表', NULL, NULL, 'LabdatahubDeviceLogs', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'logs', '设备日志', 'ruoyi', '0', '/', '{}', 'admin', '2025-09-22 15:42:02', '', '2025-09-22 15:42:16', NULL);
INSERT INTO public.gen_table VALUES (7, 'labdatahub_warn_config', '告警配置表', NULL, NULL, 'LabdatahubWarnConfig', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'warnConfig', '告警配置', 'ruoyi', '0', '/', '{}', 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48', NULL);
INSERT INTO public.gen_table VALUES (9, 'labdatahub_rule_engine', '规则引擎配置', NULL, NULL, 'LabdatahubRuleEngine', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'engine', '规则引擎配置', 'ruoyi', '0', '/', '{}', 'admin', '2025-10-20 10:01:01', '', '2025-10-20 10:01:27', NULL);
INSERT INTO public.gen_table VALUES (10, 'labdatahub_function', '设备指令下发表', NULL, NULL, 'LabdatahubFunction', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'function', '设备指令下发', 'ruoyi', '0', '/', '{}', 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25', NULL);
INSERT INTO public.gen_table VALUES (11, 'warn_function_record', '指令下发记录', NULL, NULL, 'WarnFunctionRecord', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'record', '指令下发记录', 'ruoyi', '0', '/', '{"parentMenuId":0}', 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24', NULL);
INSERT INTO public.gen_table VALUES (13, 'labdatahub_linkage_action_record', '设备联动告警动作执行记录', NULL, NULL, 'LabdatahubLinkageActionRecord', 'crud', 'element-ui', 'com.labdatahub.system', 'business', 'linkageAction', '设备联动告警动作执行记录', 'ruoyi', '0', '/', '{"parentMenuId":0}', 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:03', NULL);
INSERT INTO public.gen_table VALUES (14, 'labdatahub_linkage_warn_record', '设备联动告警记录', NULL, NULL, 'LabdatahubLinkageWarnRecord', 'crud', 'element-ui', 'com.labdatahub.system', 'business', 'linkageRecord', '设备联动告警记录', 'ruoyi', '0', '/', '{}', 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17', NULL);
INSERT INTO public.gen_table VALUES (15, 'labdatahub_scheduled_task', '定时引擎配置表', NULL, NULL, 'LabdatahubScheduledTask', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'scheduledEngine', '定时引擎配置', 'ruoyi', '0', '/', '{}', 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00', NULL);
INSERT INTO public.gen_table VALUES (16, 'labdatahub_modbus_config', 'modbus协议读取配置表', NULL, NULL, 'LabdatahubModbusConfig', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'modbus', 'modbus协议读取配置', 'ruoyi', '0', '/', '{"parentMenuId":0}', 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:47.726', NULL);
INSERT INTO public.gen_table VALUES (12, 'labdatahub_warn_linkage', '设备联动告警', NULL, NULL, 'LabdatahubWarnLinkage', 'crud', 'element-ui', 'com.labdatahub.business', 'business', 'linkage', '设备联动告警', 'ruoyi', '0', '/', '{"parentMenuId":0}', 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.876', NULL);


--
-- Data for Name: gen_table_column; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.gen_table_column VALUES (1, 1, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO public.gen_table_column VALUES (2, 1, 'name', '组件名称', 'varchar(255)', 'String', 'name', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 2, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO public.gen_table_column VALUES (3, 1, 'net_type', '网络类型', 'varchar(255)', 'String', 'netType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 3, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO public.gen_table_column VALUES (4, 1, 'ip_addr', 'IP地址', 'varchar(255)', 'String', 'ipAddr', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO public.gen_table_column VALUES (5, 1, 'port', '端口', 'int', 'Long', 'port', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO public.gen_table_column VALUES (6, 1, 'open_tls', '是否开启TLS (0-否 1-是)', 'varchar(255)', 'String', 'openTls', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO public.gen_table_column VALUES (7, 1, 'remark', '备注', 'text', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'textarea', '', 7, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO public.gen_table_column VALUES (8, 1, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 8, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO public.gen_table_column VALUES (9, 1, 'create_by', '创建人', 'varchar(255)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 9, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO public.gen_table_column VALUES (10, 1, 'update_time', '修改时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'datetime', '', 10, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO public.gen_table_column VALUES (11, 1, 'update_by', '修改人', 'varchar(255)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'input', '', 11, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO public.gen_table_column VALUES (12, 1, 'status', '0-停用 1-启用', 'varchar(255)', 'String', 'status', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'radio', '', 12, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO public.gen_table_column VALUES (13, 1, 'other_config', '其余配置(如账号密码等)', 'text', 'String', 'otherConfig', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 13, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO public.gen_table_column VALUES (14, 1, 'protocol_id', '协议id', 'varchar(255)', 'String', 'protocolId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 14, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO public.gen_table_column VALUES (15, 1, 'protocol_name', '协议名称', 'varchar(255)', 'String', 'protocolName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 15, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:18');
INSERT INTO public.gen_table_column VALUES (16, 2, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO public.gen_table_column VALUES (17, 2, 'product_sn', '产品编码', 'varchar(255)', 'String', 'productSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO public.gen_table_column VALUES (18, 2, 'product_name', '产品名称', 'varchar(255)', 'String', 'productName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 3, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO public.gen_table_column VALUES (19, 2, 'link_method_id', '接入方式id', 'varchar(255)', 'String', 'linkMethodId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO public.gen_table_column VALUES (20, 2, 'link_method_name', '接入方式名称', 'varchar(255)', 'String', 'linkMethodName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 5, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO public.gen_table_column VALUES (21, 2, 'protocol_id', '协议id', 'varchar(255)', 'String', 'protocolId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO public.gen_table_column VALUES (22, 2, 'protocol_name', '协议名称', 'varchar(255)', 'String', 'protocolName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 7, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO public.gen_table_column VALUES (23, 2, 'device_count', '设备数量', 'int', 'Long', 'deviceCount', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO public.gen_table_column VALUES (24, 2, 'device_type', '设备类型 0-直连设备 1-网关设备', 'varchar(255)', 'String', 'deviceType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 9, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO public.gen_table_column VALUES (25, 2, 'remark', '备注', 'text', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'textarea', '', 10, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO public.gen_table_column VALUES (26, 2, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 11, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO public.gen_table_column VALUES (27, 2, 'update_time', '修改时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'datetime', '', 12, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO public.gen_table_column VALUES (28, 2, 'create_by', '创建人', 'varchar(255)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 13, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO public.gen_table_column VALUES (29, 2, 'update_by', '修改人', 'varchar(255)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'input', '', 14, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:26');
INSERT INTO public.gen_table_column VALUES (30, 2, 'status', '0-停用 1-启用', 'varchar(255)', 'String', 'status', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'radio', '', 15, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:27');
INSERT INTO public.gen_table_column VALUES (31, 3, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO public.gen_table_column VALUES (32, 3, 'belong_id', '归属id 产品/设备', 'varchar(50)', 'String', 'belongId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO public.gen_table_column VALUES (33, 3, 'belong_type', '归属类型 0-产品 1-设备', 'varchar(255)', 'String', 'belongType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 3, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO public.gen_table_column VALUES (34, 3, 'identifier', '属性标识符，如 temperature, status', 'varchar(100)', 'String', 'identifier', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO public.gen_table_column VALUES (35, 3, 'name', '属性名称', 'varchar(100)', 'String', 'name', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 5, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO public.gen_table_column VALUES (36, 3, 'parent_id', '父属性ID，用于构建嵌套结构。0表示根级属性', 'varchar(255)', 'String', 'parentId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO public.gen_table_column VALUES (37, 3, 'data_type', '数据类型: int, double, bool, string, struct, array...', 'varchar(50)', 'String', 'dataType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 7, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO public.gen_table_column VALUES (38, 3, 'sort_num', '排序', 'int', 'Long', 'sortNum', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO public.gen_table_column VALUES (39, 3, 'from_type', '来源 0-产品继承 1-设备自定义(继承不可修改)', 'varchar(255)', 'String', 'fromType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 9, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:33');
INSERT INTO public.gen_table_column VALUES (40, 4, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39');
INSERT INTO public.gen_table_column VALUES (41, 4, 'protocol_name', '协议名称', 'varchar(255)', 'String', 'protocolName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 2, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39');
INSERT INTO public.gen_table_column VALUES (42, 4, 'local_url', '本地存储路径', 'text', 'String', 'localUrl', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 3, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39');
INSERT INTO public.gen_table_column VALUES (43, 4, 'main_class_path', '解析类入口', 'text', 'String', 'mainClassPath', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 4, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39');
INSERT INTO public.gen_table_column VALUES (44, 4, 'origin_name', '文件原始名字', 'text', 'String', 'originName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'textarea', '', 5, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39');
INSERT INTO public.gen_table_column VALUES (45, 4, 'type', '0-jar包', 'varchar(255)', 'String', 'type', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 6, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39');
INSERT INTO public.gen_table_column VALUES (46, 4, 'new_name', '文件重命名', 'text', 'String', 'newName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'textarea', '', 7, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:39');
INSERT INTO public.gen_table_column VALUES (47, 5, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO public.gen_table_column VALUES (48, 5, 'device_id', '设备id', 'varchar(255)', 'String', 'deviceId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO public.gen_table_column VALUES (49, 5, 'device_sn', '设备编码', 'varchar(255)', 'String', 'deviceSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO public.gen_table_column VALUES (50, 5, 'device_name', '设备名称', 'varchar(255)', 'String', 'deviceName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 4, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO public.gen_table_column VALUES (51, 5, 'product_id', '关联产品id', 'varchar(255)', 'String', 'productId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO public.gen_table_column VALUES (52, 5, 'product_name', '关联产品名称', 'varchar(255)', 'String', 'productName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 6, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO public.gen_table_column VALUES (53, 5, 'product_sn', '关联产品编码', 'varchar(255)', 'String', 'productSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 7, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO public.gen_table_column VALUES (54, 5, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 8, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO public.gen_table_column VALUES (55, 5, 'create_by', '创建人', 'varchar(255)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 9, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO public.gen_table_column VALUES (56, 5, 'update_time', '修改时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'datetime', '', 10, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO public.gen_table_column VALUES (57, 5, 'update_by', '修改人', 'varchar(255)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'input', '', 11, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO public.gen_table_column VALUES (58, 5, 'link_method_id', '接入方式id', 'varchar(255)', 'String', 'linkMethodId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 12, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO public.gen_table_column VALUES (59, 5, 'link_method_name', '接入方式名称', 'varchar(255)', 'String', 'linkMethodName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 13, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO public.gen_table_column VALUES (60, 5, 'protocol_id', '协议id', 'varchar(255)', 'String', 'protocolId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 14, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO public.gen_table_column VALUES (61, 5, 'protocol_name', '协议名称', 'varchar(255)', 'String', 'protocolName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 15, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:45');
INSERT INTO public.gen_table_column VALUES (62, 5, 'status', '0-离线 1-在线', 'varchar(255)', 'String', 'status', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'radio', '', 16, 'admin', '2025-09-18 13:41:42', '', '2025-09-18 13:46:46');
INSERT INTO public.gen_table_column VALUES (63, 6, 'id', 'id', 'bigint', 'Long', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-09-22 15:42:02', '', '2025-09-22 15:42:16');
INSERT INTO public.gen_table_column VALUES (64, 6, 'device_sn', '设备sn', 'varchar(255)', 'String', 'deviceSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-09-22 15:42:02', '', '2025-09-22 15:42:16');
INSERT INTO public.gen_table_column VALUES (65, 6, 'report_time', '上报时间', 'datetime', 'Date', 'reportTime', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'datetime', '', 3, 'admin', '2025-09-22 15:42:02', '', '2025-09-22 15:42:16');
INSERT INTO public.gen_table_column VALUES (66, 6, 'properties', '属性json', 'text', 'String', 'properties', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 4, 'admin', '2025-09-22 15:42:02', '', '2025-09-22 15:42:16');
INSERT INTO public.gen_table_column VALUES (67, 6, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 5, 'admin', '2025-09-22 15:42:02', '', '2025-09-22 15:42:16');
INSERT INTO public.gen_table_column VALUES (68, 6, 'log_type', '日志类型', 'varchar(255)', 'String', 'logType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 6, 'admin', '2025-09-22 15:42:02', '', '2025-09-22 15:42:16');
INSERT INTO public.gen_table_column VALUES (69, 7, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO public.gen_table_column VALUES (70, 7, 'name', '告警名称', 'varchar(255)', 'String', 'name', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 2, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO public.gen_table_column VALUES (71, 7, 'belong_sn', '产品/设备sn', 'varchar(255)', 'String', 'belongSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO public.gen_table_column VALUES (72, 7, 'belong_type', '来源 0-产品 1-设备', 'varchar(255)', 'String', 'belongType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 4, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO public.gen_table_column VALUES (73, 7, 'rule_json', '规则json', 'text', 'String', 'ruleJson', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 5, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO public.gen_table_column VALUES (74, 7, 'warn_message', '告警消息模板', 'text', 'String', 'warnMessage', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 6, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO public.gen_table_column VALUES (75, 7, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 7, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO public.gen_table_column VALUES (76, 7, 'create_by', '创建人', 'varchar(255)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 8, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO public.gen_table_column VALUES (77, 7, 'update_time', '修改时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'datetime', '', 9, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO public.gen_table_column VALUES (78, 7, 'update_by', '修改人', 'varchar(255)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'input', '', 10, 'admin', '2025-10-05 00:04:56', '', '2025-10-05 00:05:48');
INSERT INTO public.gen_table_column VALUES (87, 9, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-10-20 10:01:01', '', '2025-10-20 10:01:27');
INSERT INTO public.gen_table_column VALUES (88, 9, 'engine_name', '引擎名称', 'varchar(255)', 'String', 'engineName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 2, 'admin', '2025-10-20 10:01:01', '', '2025-10-20 10:01:27');
INSERT INTO public.gen_table_column VALUES (89, 9, 'config_json', 'json配置', 'text', 'String', 'configJson', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 3, 'admin', '2025-10-20 10:01:01', '', '2025-10-20 10:01:27');
INSERT INTO public.gen_table_column VALUES (90, 9, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 4, 'admin', '2025-10-20 10:01:01', '', '2025-10-20 10:01:27');
INSERT INTO public.gen_table_column VALUES (91, 9, 'remark', '备注', 'varchar(255)', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'input', '', 5, 'admin', '2025-10-20 10:01:01', '', '2025-10-20 10:01:27');
INSERT INTO public.gen_table_column VALUES (92, 10, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO public.gen_table_column VALUES (93, 10, 'function_name', '功能名称', 'varchar(255)', 'String', 'functionName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 2, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO public.gen_table_column VALUES (94, 10, 'function_code', '功能编码', 'varchar(255)', 'String', 'functionCode', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO public.gen_table_column VALUES (95, 10, 'function_params', '自定义参数', 'text', 'String', 'functionParams', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 4, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO public.gen_table_column VALUES (96, 10, 'device_sn', '设备/产品sn', 'varchar(255)', 'String', 'deviceSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO public.gen_table_column VALUES (97, 10, 'protocol_id', '协议id', 'text', 'String', 'protocolId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 6, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO public.gen_table_column VALUES (98, 10, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 7, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO public.gen_table_column VALUES (99, 10, 'create_by', '创建人', 'varchar(255)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 8, 'admin', '2025-10-24 17:01:11', '', '2025-10-24 17:01:25');
INSERT INTO public.gen_table_column VALUES (100, 11, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO public.gen_table_column VALUES (101, 11, 'function_id', '功能id', 'varchar(255)', 'String', 'functionId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO public.gen_table_column VALUES (102, 11, 'function_code', '功能code', 'varchar(255)', 'String', 'functionCode', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO public.gen_table_column VALUES (103, 11, 'function_name', '功能名称', 'varchar(255)', 'String', 'functionName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 4, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO public.gen_table_column VALUES (104, 11, 'function_params', '参数', 'text', 'String', 'functionParams', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 5, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO public.gen_table_column VALUES (105, 11, 'is_success', '0-失败 1-成功', 'varchar(255)', 'String', 'isSuccess', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO public.gen_table_column VALUES (106, 11, 'create_time', '下发时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 7, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO public.gen_table_column VALUES (107, 11, 'device_sn', '设备sn', 'varchar(255)', 'String', 'deviceSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO public.gen_table_column VALUES (108, 11, 'device_name', '设备名称', 'varchar(255)', 'String', 'deviceName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 9, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO public.gen_table_column VALUES (109, 11, 'trigger_type', '0-手动触发 1-告警触发', 'varchar(255)', 'String', 'triggerType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 10, 'admin', '2025-10-29 17:16:05', '', '2025-10-29 17:16:24');
INSERT INTO public.gen_table_column VALUES (121, 13, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:03');
INSERT INTO public.gen_table_column VALUES (122, 13, 'config_id', '告警配置id', 'varchar(255)', 'String', 'configId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:03');
INSERT INTO public.gen_table_column VALUES (123, 13, 'config_name', '告警配置名称', 'varchar(255)', 'String', 'configName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 3, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:03');
INSERT INTO public.gen_table_column VALUES (124, 13, 'execute_sn', '执行动作设备SN', 'text', 'String', 'executeSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 4, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:03');
INSERT INTO public.gen_table_column VALUES (125, 13, 'execute_name', '执行动作设备名称', 'text', 'String', 'executeName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'textarea', '', 5, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:04');
INSERT INTO public.gen_table_column VALUES (126, 13, 'function_code', '动作CODE', 'varchar(255)', 'String', 'functionCode', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:04');
INSERT INTO public.gen_table_column VALUES (127, 13, 'function_name', '动作名称', 'varchar(255)', 'String', 'functionName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 7, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:04');
INSERT INTO public.gen_table_column VALUES (128, 13, 'function_param', '参数', 'text', 'String', 'functionParam', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 8, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:04');
INSERT INTO public.gen_table_column VALUES (129, 13, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 9, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:04');
INSERT INTO public.gen_table_column VALUES (130, 14, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO public.gen_table_column VALUES (131, 14, 'config_id', '告警配置id', 'varchar(255)', 'String', 'configId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO public.gen_table_column VALUES (132, 14, 'config_name', '告警配置名称', 'varchar(255)', 'String', 'configName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 3, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO public.gen_table_column VALUES (133, 14, 'warn_message', '告警内容', 'text', 'String', 'warnMessage', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 4, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO public.gen_table_column VALUES (134, 14, 'warn_data', '告警时相关设备数据', 'text', 'String', 'warnData', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 5, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO public.gen_table_column VALUES (135, 14, 'trigger_sn_list', '告警设备SN列表', 'text', 'String', 'triggerSnList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 6, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO public.gen_table_column VALUES (136, 14, 'trigger_name_list', '告警设备名称列表', 'text', 'String', 'triggerNameList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 7, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO public.gen_table_column VALUES (137, 14, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 8, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO public.gen_table_column VALUES (138, 14, 'warn_level', '告警等级 1-紧急 2-严重 3-一般 4-警告 5-正常', 'varchar(255)', 'String', 'warnLevel', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 9, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO public.gen_table_column VALUES (139, 14, 'status', '0-未处理 1-已处理', 'varchar(255)', 'String', 'status', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'radio', '', 10, 'admin', '2025-11-20 13:47:10', '', '2025-11-20 13:48:17');
INSERT INTO public.gen_table_column VALUES (140, 15, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO public.gen_table_column VALUES (141, 15, 'name', '配置名称', 'varchar(255)', 'String', 'name', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 2, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO public.gen_table_column VALUES (142, 15, 'rule_json', '规则json', 'text', 'String', 'ruleJson', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 3, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO public.gen_table_column VALUES (143, 15, 'is_enable', '是否启用 0-否 1-是', 'varchar(255)', 'String', 'isEnable', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO public.gen_table_column VALUES (144, 15, 'execute_sn_list', '执行动作设备列表', 'text', 'String', 'executeSnList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 5, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO public.gen_table_column VALUES (145, 15, 'execute_name_list', '执行动作设备名称', 'text', 'String', 'executeNameList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 6, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO public.gen_table_column VALUES (146, 15, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 7, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO public.gen_table_column VALUES (147, 15, 'remark', '备注', 'text', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'textarea', '', 8, 'admin', '2025-12-02 11:05:23', '', '2025-12-02 11:06:00');
INSERT INTO public.gen_table_column VALUES (110, 12, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.892');
INSERT INTO public.gen_table_column VALUES (111, 12, 'rule_json', '规则json', 'text', 'String', 'ruleJson', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 2, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.908');
INSERT INTO public.gen_table_column VALUES (148, 16, 'id', 'id', 'varchar(255)', 'String', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:47.82');
INSERT INTO public.gen_table_column VALUES (149, 16, 'belong_sn', '归属sn', 'varchar(255)', 'String', 'belongSn', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:47.852');
INSERT INTO public.gen_table_column VALUES (150, 16, 'belong_type', '归属类型 0-产品 1-设备', 'varchar(255)', 'String', 'belongType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 3, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:47.869');
INSERT INTO public.gen_table_column VALUES (151, 16, 'code', '读取编码', 'varchar(255)', 'String', 'code', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:47.888');
INSERT INTO public.gen_table_column VALUES (152, 16, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 5, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:47.917');
INSERT INTO public.gen_table_column VALUES (153, 16, 'register_range', '范围,逗号分隔如（1,2-5,7）', 'text', 'String', 'registerRange', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 6, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:48.019');
INSERT INTO public.gen_table_column VALUES (154, 16, 'interval_time', '多少毫秒读取一次', 'int', 'Long', 'intervalTime', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 7, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:48.044');
INSERT INTO public.gen_table_column VALUES (155, 16, 'delay_time', '同一设备读取属性延迟时间', 'int', 'Long', 'delayTime', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2025-12-16 13:40:59', '', '2026-01-24 16:49:48.071');
INSERT INTO public.gen_table_column VALUES (112, 12, 'warn_message', '告警消息模板', 'text', 'String', 'warnMessage', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 3, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.925');
INSERT INTO public.gen_table_column VALUES (113, 12, 'warn_level', '告警等级 1-紧急 2-严重 3-警告 4-正常', 'varchar(255)', 'String', 'warnLevel', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.94');
INSERT INTO public.gen_table_column VALUES (114, 12, 'execute_action', '执行动作json', 'text', 'String', 'executeAction', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 5, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.956');
INSERT INTO public.gen_table_column VALUES (115, 12, 'is_enable', '是否启用 0-否 1-是', 'varchar(255)', 'String', 'isEnable', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.974');
INSERT INTO public.gen_table_column VALUES (116, 12, 'trigger_sn_list', '触发设备列表', 'text', 'String', 'triggerSnList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 7, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:49:59.99');
INSERT INTO public.gen_table_column VALUES (117, 12, 'trigger_name_list', '触发设备名称', 'text', 'String', 'triggerNameList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 8, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:50:00.005');
INSERT INTO public.gen_table_column VALUES (118, 12, 'execute_sn_list', '执行动作设备列表', 'text', 'String', 'executeSnList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 9, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:50:00.021');
INSERT INTO public.gen_table_column VALUES (119, 12, 'execute_name_list', '执行动作设备名称', 'text', 'String', 'executeNameList', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 10, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:50:00.036');
INSERT INTO public.gen_table_column VALUES (120, 12, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 11, 'admin', '2025-11-03 11:13:47', '', '2026-01-24 16:50:00.051');


--
-- Data for Name: sys_config; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.sys_config VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2025-09-15 11:09:58', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO public.sys_config VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2025-09-15 11:09:58', '', NULL, '初始化密码 123456');
INSERT INTO public.sys_config VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2025-09-15 11:09:58', '', NULL, '深色主题theme-dark，浅色主题theme-light');
INSERT INTO public.sys_config VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2025-09-15 11:09:58', '', NULL, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO public.sys_config VALUES (6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2025-09-15 11:09:58', '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
INSERT INTO public.sys_config VALUES (7, '用户管理-初始密码修改策略', 'sys.account.initPasswordModify', '1', 'Y', 'admin', '2025-09-15 11:09:58', '', NULL, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
INSERT INTO public.sys_config VALUES (8, '用户管理-账号密码更新周期', 'sys.account.passwordValidateDays', '0', 'Y', 'admin', '2025-09-15 11:09:58', '', NULL, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');
INSERT INTO public.sys_config VALUES (100, '设备自注册-开关', 'device.register.switch', 'true', 'Y', 'admin', '2026-01-18 01:08:27', 'admin', '2026-01-18 19:43:55', '用于全局开启/关闭设备自注册，修改之后十秒钟内生效');
INSERT INTO public.sys_config VALUES (101, '优化-设备日志批量保存', 'device.log.batch', 'true,1000,10000', 'Y', 'admin', '2026-01-21 22:54:31', 'admin', '2026-01-22 20:34:22', '第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备日志并发高并且集中时缓解数据库压力，设备日志会先缓存，然后以每秒取固定数量数据批量插入数据库。数据量大时会导致插入数据可能会有一两秒的延迟，但不会影响实时数据以及告警等功能的实时性。酌情开启。');
INSERT INTO public.sys_config VALUES (102, '优化-指令下发日志批量保存', 'device.function.batch', 'true,1000,10000', 'Y', 'admin', '2026-01-22 20:32:34', 'admin', '2026-01-22 20:36:14', '第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备指令下发并发高并且集中时缓解数据库压力，指令日志会先缓存，然后循环取数据批量插入数据库。数据量大时会导致插入数据可能会有一两秒的延迟。酌情开启。');
INSERT INTO public.sys_config VALUES (103, '优化-告警日志批量保存', 'device.warn.batch', 'true,1000,10000', 'Y', 'admin', '2026-01-22 20:35:09', 'admin', '2026-01-23 14:37:55', '第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备告警并发高并且集中时缓解数据库压力，告警日志会先缓存，然后循环取数据批量插入数据库。数据量大时会导致插入数据可能会有一两秒的延迟。酌情开启。');
INSERT INTO public.sys_config VALUES (104, '优化-设备联动告警日志批量保存', 'device.linkage.batch', 'true,1000,10000', 'Y', 'admin', '2026-01-22 20:49:06', 'admin', '2026-01-22 20:49:18', '第一个参数是开关，第二个参数是批量插入条数配置，第三个参数是最大缓存数据条数，超过该值丢弃，即使关闭状态，也请保持三个参数。配置修改后十秒内生效。该配置用于优化设备告警并发高并且集中时缓解数据库压力，告警日志会先缓存，然后循环取数据批量插入数据库。数据量大时会导致插入数据可能会有一两秒的延迟。酌情开启。');
INSERT INTO public.sys_config VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', '2025-09-15 11:09:58', 'admin', '2026-01-15 10:18:44', '是否开启验证码功能（true开启，false关闭）');


--
-- Data for Name: sys_dept; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.sys_dept VALUES (100, 0, '0', '科技', 0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', 'admin', '2026-01-16 23:22:30');
INSERT INTO public.sys_dept VALUES (101, 100, '0,100', '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO public.sys_dept VALUES (102, 100, '0,100', '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO public.sys_dept VALUES (103, 101, '0,100,101', '研发部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO public.sys_dept VALUES (104, 101, '0,100,101', '市场部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO public.sys_dept VALUES (105, 101, '0,100,101', '测试部门', 3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO public.sys_dept VALUES (106, 101, '0,100,101', '财务部门', 4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO public.sys_dept VALUES (107, 101, '0,100,101', '运维部门', 5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO public.sys_dept VALUES (108, 102, '0,100,102', '市场部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO public.sys_dept VALUES (109, 102, '0,100,102', '财务部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL);
INSERT INTO public.sys_dept VALUES (200, 100, '0,100', '厦门市', 3, '张三', '13129405840', NULL, '0', '0', 'admin', '2026-01-16 23:27:08', '', NULL);
INSERT INTO public.sys_dept VALUES (201, 200, '0,100,200', '翔安医院', 1, NULL, NULL, NULL, '0', '0', 'admin', '2026-01-16 23:27:28', '', NULL);
INSERT INTO public.sys_dept VALUES (202, 200, '0,100,200', '思明医院', 2, NULL, NULL, NULL, '0', '0', 'admin', '2026-01-16 23:27:41', '', NULL);


--
-- Data for Name: sys_dict_data; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.sys_dict_data VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '性别男');
INSERT INTO public.sys_dict_data VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '性别女');
INSERT INTO public.sys_dict_data VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '性别未知');
INSERT INTO public.sys_dict_data VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '显示菜单');
INSERT INTO public.sys_dict_data VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '隐藏菜单');
INSERT INTO public.sys_dict_data VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '正常状态');
INSERT INTO public.sys_dict_data VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '停用状态');
INSERT INTO public.sys_dict_data VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '正常状态');
INSERT INTO public.sys_dict_data VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '停用状态');
INSERT INTO public.sys_dict_data VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '默认分组');
INSERT INTO public.sys_dict_data VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '系统分组');
INSERT INTO public.sys_dict_data VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '系统默认是');
INSERT INTO public.sys_dict_data VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '系统默认否');
INSERT INTO public.sys_dict_data VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '通知');
INSERT INTO public.sys_dict_data VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '公告');
INSERT INTO public.sys_dict_data VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '正常状态');
INSERT INTO public.sys_dict_data VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '关闭状态');
INSERT INTO public.sys_dict_data VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '其他操作');
INSERT INTO public.sys_dict_data VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '新增操作');
INSERT INTO public.sys_dict_data VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '修改操作');
INSERT INTO public.sys_dict_data VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '删除操作');
INSERT INTO public.sys_dict_data VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '授权操作');
INSERT INTO public.sys_dict_data VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '导出操作');
INSERT INTO public.sys_dict_data VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '导入操作');
INSERT INTO public.sys_dict_data VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '强退操作');
INSERT INTO public.sys_dict_data VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '生成操作');
INSERT INTO public.sys_dict_data VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '清空操作');
INSERT INTO public.sys_dict_data VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '正常状态');
INSERT INTO public.sys_dict_data VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '停用状态');


--
-- Data for Name: sys_dict_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.sys_dict_type VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '用户性别列表');
INSERT INTO public.sys_dict_type VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2025-09-15 11:09:58', 'admin', '2026-01-02 23:06:05', '菜单状态列表');
INSERT INTO public.sys_dict_type VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '系统开关列表');
INSERT INTO public.sys_dict_type VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '任务状态列表');
INSERT INTO public.sys_dict_type VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '任务分组列表');
INSERT INTO public.sys_dict_type VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '系统是否列表');
INSERT INTO public.sys_dict_type VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '通知类型列表');
INSERT INTO public.sys_dict_type VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '通知状态列表');
INSERT INTO public.sys_dict_type VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '操作类型列表');
INSERT INTO public.sys_dict_type VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '登录状态列表');


--
-- Data for Name: sys_job; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.sys_job VALUES (1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams', '0/10 * * * * ?', '3', '1', '1', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_job VALUES (2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(''ry'')', '0/15 * * * * ?', '3', '1', '1', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_job VALUES (3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(''ry'', true, 2000L, 316.50D, 100)', '0/20 * * * * ?', '3', '1', '1', 'admin', '2025-09-15 11:09:58', '', '2026-01-23 15:09:27', '');


--
-- Data for Name: sys_menu; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.sys_menu VALUES (1, '系统管理', 0, 3, 'system', NULL, '', '', '1', '0', 'M', '0', '0', '', 'system', 'admin', '2025-09-15 11:09:57', 'admin', '2026-01-14 09:21:14', '系统管理目录');
INSERT INTO public.sys_menu VALUES (2, '系统监控', 0, 2, 'monitor', NULL, '', '', '1', '0', 'M', '0', '0', '', 'monitor', 'admin', '2025-09-15 11:09:57', '', NULL, '系统监控目录');
INSERT INTO public.sys_menu VALUES (3, '系统工具', 0, 4, 'tool', NULL, '', '', '1', '0', 'M', '0', '0', '', 'tool', 'admin', '2025-09-15 11:09:57', 'admin', '2026-01-14 09:21:21', '系统工具目录');
INSERT INTO public.sys_menu VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', '', '1', '0', 'C', '0', '0', 'system:user:list', 'user', 'admin', '2025-09-15 11:09:57', '', NULL, '用户管理菜单');
INSERT INTO public.sys_menu VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', '', '1', '0', 'C', '0', '0', 'system:role:list', 'peoples', 'admin', '2025-09-15 11:09:57', '', NULL, '角色管理菜单');
INSERT INTO public.sys_menu VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', '', '1', '0', 'C', '0', '0', 'system:menu:list', 'tree-table', 'admin', '2025-09-15 11:09:57', '', NULL, '菜单管理菜单');
INSERT INTO public.sys_menu VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', '', '1', '0', 'C', '0', '0', 'system:dept:list', 'tree', 'admin', '2025-09-15 11:09:57', '', NULL, '部门管理菜单');
INSERT INTO public.sys_menu VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', '', '1', '0', 'C', '0', '0', 'system:post:list', 'post', 'admin', '2025-09-15 11:09:57', '', NULL, '岗位管理菜单');
INSERT INTO public.sys_menu VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', '', '1', '0', 'C', '0', '0', 'system:dict:list', 'dict', 'admin', '2025-09-15 11:09:57', '', NULL, '字典管理菜单');
INSERT INTO public.sys_menu VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', '', '1', '0', 'C', '0', '0', 'system:config:list', 'edit', 'admin', '2025-09-15 11:09:57', '', NULL, '参数设置菜单');
INSERT INTO public.sys_menu VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', '', '1', '0', 'C', '0', '0', 'system:notice:list', 'message', 'admin', '2025-09-15 11:09:57', '', NULL, '通知公告菜单');
INSERT INTO public.sys_menu VALUES (108, '日志管理', 1, 9, 'log', '', '', '', '1', '0', 'M', '0', '0', '', 'log', 'admin', '2025-09-15 11:09:57', '', NULL, '日志管理菜单');
INSERT INTO public.sys_menu VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', '', '1', '0', 'C', '0', '0', 'monitor:online:list', 'online', 'admin', '2025-09-15 11:09:57', '', NULL, '在线用户菜单');
INSERT INTO public.sys_menu VALUES (110, '定时任务', 2, 2, 'job', 'monitor/job/index', '', '', '1', '0', 'C', '0', '0', 'monitor:job:list', 'job', 'admin', '2025-09-15 11:09:57', '', NULL, '定时任务菜单');
INSERT INTO public.sys_menu VALUES (111, '数据监控', 2, 3, 'druid', 'monitor/druid/index', '', '', '1', '0', 'C', '0', '0', 'monitor:druid:list', 'druid', 'admin', '2025-09-15 11:09:57', '', NULL, '数据监控菜单');
INSERT INTO public.sys_menu VALUES (112, '服务监控', 2, 4, 'server', 'monitor/server/index', '', '', '1', '0', 'C', '0', '0', 'monitor:server:list', 'server', 'admin', '2025-09-15 11:09:57', '', NULL, '服务监控菜单');
INSERT INTO public.sys_menu VALUES (113, '缓存监控', 2, 5, 'cache', 'monitor/cache/index', '', '', '1', '0', 'C', '0', '0', 'monitor:cache:list', 'redis', 'admin', '2025-09-15 11:09:57', '', NULL, '缓存监控菜单');
INSERT INTO public.sys_menu VALUES (114, '缓存列表', 2, 6, 'cacheList', 'monitor/cache/list', '', '', '1', '0', 'C', '0', '0', 'monitor:cache:list', 'redis-list', 'admin', '2025-09-15 11:09:57', '', NULL, '缓存列表菜单');
INSERT INTO public.sys_menu VALUES (115, '表单构建', 3, 1, 'build', 'tool/build/index', '', '', '1', '0', 'C', '0', '0', 'tool:build:list', 'build', 'admin', '2025-09-15 11:09:57', '', NULL, '表单构建菜单');
INSERT INTO public.sys_menu VALUES (116, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', '', '1', '0', 'C', '0', '0', 'tool:gen:list', 'code', 'admin', '2025-09-15 11:09:57', '', NULL, '代码生成菜单');
INSERT INTO public.sys_menu VALUES (117, '系统接口', 3, 3, 'swagger', 'tool/swagger/index', '', '', '1', '0', 'C', '0', '0', 'tool:swagger:list', 'swagger', 'admin', '2025-09-15 11:09:57', '', NULL, '系统接口菜单');
INSERT INTO public.sys_menu VALUES (500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', '', '1', '0', 'C', '0', '0', 'monitor:operlog:list', 'form', 'admin', '2025-09-15 11:09:57', '', NULL, '操作日志菜单');
INSERT INTO public.sys_menu VALUES (501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', '', '1', '0', 'C', '0', '0', 'monitor:logininfor:list', 'logininfor', 'admin', '2025-09-15 11:09:57', '', NULL, '登录日志菜单');
INSERT INTO public.sys_menu VALUES (1000, '用户查询', 100, 1, '', '', '', '', '1', '0', 'F', '0', '0', 'system:user:query', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1001, '用户新增', 100, 2, '', '', '', '', '1', '0', 'F', '0', '0', 'system:user:add', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1002, '用户修改', 100, 3, '', '', '', '', '1', '0', 'F', '0', '0', 'system:user:edit', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1003, '用户删除', 100, 4, '', '', '', '', '1', '0', 'F', '0', '0', 'system:user:remove', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1004, '用户导出', 100, 5, '', '', '', '', '1', '0', 'F', '0', '0', 'system:user:export', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1005, '用户导入', 100, 6, '', '', '', '', '1', '0', 'F', '0', '0', 'system:user:import', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1006, '重置密码', 100, 7, '', '', '', '', '1', '0', 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1007, '角色查询', 101, 1, '', '', '', '', '1', '0', 'F', '0', '0', 'system:role:query', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1008, '角色新增', 101, 2, '', '', '', '', '1', '0', 'F', '0', '0', 'system:role:add', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1009, '角色修改', 101, 3, '', '', '', '', '1', '0', 'F', '0', '0', 'system:role:edit', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1010, '角色删除', 101, 4, '', '', '', '', '1', '0', 'F', '0', '0', 'system:role:remove', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1011, '角色导出', 101, 5, '', '', '', '', '1', '0', 'F', '0', '0', 'system:role:export', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1012, '菜单查询', 102, 1, '', '', '', '', '1', '0', 'F', '0', '0', 'system:menu:query', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1013, '菜单新增', 102, 2, '', '', '', '', '1', '0', 'F', '0', '0', 'system:menu:add', '#', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1014, '菜单修改', 102, 3, '', '', '', '', '1', '0', 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1015, '菜单删除', 102, 4, '', '', '', '', '1', '0', 'F', '0', '0', 'system:menu:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1016, '部门查询', 103, 1, '', '', '', '', '1', '0', 'F', '0', '0', 'system:dept:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1017, '部门新增', 103, 2, '', '', '', '', '1', '0', 'F', '0', '0', 'system:dept:add', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1018, '部门修改', 103, 3, '', '', '', '', '1', '0', 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1019, '部门删除', 103, 4, '', '', '', '', '1', '0', 'F', '0', '0', 'system:dept:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1020, '岗位查询', 104, 1, '', '', '', '', '1', '0', 'F', '0', '0', 'system:post:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1021, '岗位新增', 104, 2, '', '', '', '', '1', '0', 'F', '0', '0', 'system:post:add', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1022, '岗位修改', 104, 3, '', '', '', '', '1', '0', 'F', '0', '0', 'system:post:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1023, '岗位删除', 104, 4, '', '', '', '', '1', '0', 'F', '0', '0', 'system:post:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1024, '岗位导出', 104, 5, '', '', '', '', '1', '0', 'F', '0', '0', 'system:post:export', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1025, '字典查询', 105, 1, '#', '', '', '', '1', '0', 'F', '0', '0', 'system:dict:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1026, '字典新增', 105, 2, '#', '', '', '', '1', '0', 'F', '0', '0', 'system:dict:add', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1027, '字典修改', 105, 3, '#', '', '', '', '1', '0', 'F', '0', '0', 'system:dict:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1028, '字典删除', 105, 4, '#', '', '', '', '1', '0', 'F', '0', '0', 'system:dict:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1029, '字典导出', 105, 5, '#', '', '', '', '1', '0', 'F', '0', '0', 'system:dict:export', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1030, '参数查询', 106, 1, '#', '', '', '', '1', '0', 'F', '0', '0', 'system:config:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1031, '参数新增', 106, 2, '#', '', '', '', '1', '0', 'F', '0', '0', 'system:config:add', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1032, '参数修改', 106, 3, '#', '', '', '', '1', '0', 'F', '0', '0', 'system:config:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1033, '参数删除', 106, 4, '#', '', '', '', '1', '0', 'F', '0', '0', 'system:config:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1034, '参数导出', 106, 5, '#', '', '', '', '1', '0', 'F', '0', '0', 'system:config:export', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1035, '公告查询', 107, 1, '#', '', '', '', '1', '0', 'F', '0', '0', 'system:notice:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1036, '公告新增', 107, 2, '#', '', '', '', '1', '0', 'F', '0', '0', 'system:notice:add', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1037, '公告修改', 107, 3, '#', '', '', '', '1', '0', 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1038, '公告删除', 107, 4, '#', '', '', '', '1', '0', 'F', '0', '0', 'system:notice:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1039, '操作查询', 500, 1, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:operlog:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1040, '操作删除', 500, 2, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1041, '日志导出', 500, 3, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:operlog:export', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1042, '登录查询', 501, 1, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:logininfor:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1043, '登录删除', 501, 2, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1044, '日志导出', 501, 3, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:logininfor:export', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1045, '账户解锁', 501, 4, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:logininfor:unlock', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1046, '在线查询', 109, 1, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:online:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1047, '批量强退', 109, 2, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1048, '单条强退', 109, 3, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1049, '任务查询', 110, 1, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:job:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1050, '任务新增', 110, 2, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:job:add', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1051, '任务修改', 110, 3, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:job:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1052, '任务删除', 110, 4, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:job:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1053, '状态修改', 110, 5, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1054, '任务导出', 110, 6, '#', '', '', '', '1', '0', 'F', '0', '0', 'monitor:job:export', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1055, '生成查询', 116, 1, '#', '', '', '', '1', '0', 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1056, '生成修改', 116, 2, '#', '', '', '', '1', '0', 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1057, '生成删除', 116, 3, '#', '', '', '', '1', '0', 'F', '0', '0', 'tool:gen:remove', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1058, '导入代码', 116, 4, '#', '', '', '', '1', '0', 'F', '0', '0', 'tool:gen:import', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1059, '预览代码', 116, 5, '#', '', '', '', '1', '0', 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (1060, '生成代码', 116, 6, '#', '', '', '', '1', '0', 'F', '0', '0', 'tool:gen:code', '#', 'admin', '2025-09-15 11:09:58', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2000, '网络组件', 2037, 1, 'component', 'business/component/index', NULL, '', '1', '0', 'C', '0', '0', 'business:component:list', 'netComponent', 'admin', '2025-09-18 13:58:11', 'admin', '2025-10-30 11:19:43', '网络组件菜单');
INSERT INTO public.sys_menu VALUES (2001, '网络组件查询', 2000, 1, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:component:query', '#', 'admin', '2025-09-18 13:58:11', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2002, '网络组件新增', 2000, 2, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:component:add', '#', 'admin', '2025-09-18 13:58:11', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2003, '网络组件修改', 2000, 3, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:component:edit', '#', 'admin', '2025-09-18 13:58:11', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2004, '网络组件删除', 2000, 4, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:component:remove', '#', 'admin', '2025-09-18 13:58:11', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2005, '网络组件导出', 2000, 5, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:component:export', '#', 'admin', '2025-09-18 13:58:11', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2006, '设备管理', 2036, 1, 'device', 'business/device/index', NULL, '', '1', '0', 'C', '0', '0', 'business:device:list', 'device', 'admin', '2025-09-18 13:58:25', 'admin', '2025-12-12 10:46:51', '设备菜单');
INSERT INTO public.sys_menu VALUES (2007, '设备查询', 2006, 1, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:device:query', '#', 'admin', '2025-09-18 13:58:25', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2008, '设备新增', 2006, 2, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:device:add', '#', 'admin', '2025-09-18 13:58:25', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2009, '设备修改', 2006, 3, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:device:edit', '#', 'admin', '2025-09-18 13:58:25', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2010, '设备删除', 2006, 4, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:device:remove', '#', 'admin', '2025-09-18 13:58:25', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2011, '设备导出', 2006, 5, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:device:export', '#', 'admin', '2025-09-18 13:58:25', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2012, '产品管理', 2036, 0, 'product', 'business/product/index', NULL, '', '1', '0', 'C', '0', '0', 'business:product:list', 'product', 'admin', '2025-09-18 13:58:31', 'admin', '2025-12-12 10:46:46', '产品菜单');
INSERT INTO public.sys_menu VALUES (2013, '产品查询', 2012, 1, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:product:query', '#', 'admin', '2025-09-18 13:58:31', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2014, '产品新增', 2012, 2, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:product:add', '#', 'admin', '2025-09-18 13:58:31', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2015, '产品修改', 2012, 3, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:product:edit', '#', 'admin', '2025-09-18 13:58:31', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2016, '产品删除', 2012, 4, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:product:remove', '#', 'admin', '2025-09-18 13:58:31', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2017, '产品导出', 2012, 5, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:product:export', '#', 'admin', '2025-09-18 13:58:31', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2030, '协议管理', 2037, 1, 'protocol', 'business/protocol/index', NULL, '', '1', '0', 'C', '0', '0', 'business:protocol:list', 'protocol', 'admin', '2025-09-18 14:00:37', 'admin', '2025-10-30 11:19:51', '协议管理菜单');
INSERT INTO public.sys_menu VALUES (2031, '协议管理查询', 2030, 1, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:protocol:query', '#', 'admin', '2025-09-18 14:00:37', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2032, '协议管理新增', 2030, 2, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:protocol:add', '#', 'admin', '2025-09-18 14:00:37', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2033, '协议管理修改', 2030, 3, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:protocol:edit', '#', 'admin', '2025-09-18 14:00:37', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2034, '协议管理删除', 2030, 4, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:protocol:remove', '#', 'admin', '2025-09-18 14:00:37', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2035, '协议管理导出', 2030, 5, '#', '', NULL, '', '1', '0', 'F', '0', '0', 'business:protocol:export', '#', 'admin', '2025-09-18 14:00:37', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2036, '设备管理', 0, 0, '/deviceManage', NULL, NULL, '', '1', '0', 'M', '0', '0', '', 'device', 'admin', '2025-09-18 14:03:01', 'admin', '2025-10-30 11:20:19', '');
INSERT INTO public.sys_menu VALUES (2037, '网络组件', 0, 1, '/componentManage', NULL, NULL, '', '1', '0', 'M', '0', '0', '', 'netComponent', 'admin', '2025-09-18 14:04:33', 'admin', '2026-01-14 09:20:58', '');
INSERT INTO public.sys_menu VALUES (2041, '规则引擎', 0, 2, 'engine', NULL, NULL, '', '1', '0', 'M', '0', '0', '', 'tree', 'admin', '2025-11-13 10:31:11', 'admin', '2026-01-14 09:21:11', '');
INSERT INTO public.sys_menu VALUES (2042, '设备联动', 2041, 2, 'devicelink', 'business/devicelink/index', NULL, 'Devicelink', '1', '1', 'C', '0', '0', '', 'devicelink', 'admin', '2025-11-13 10:36:58', 'admin', '2025-12-23 13:34:54', '');
INSERT INTO public.sys_menu VALUES (2043, '告警记录', 2041, 4, 'linkageRecord', 'business/linkageRecord/index', NULL, 'LinkageRecord', '1', '1', 'C', '0', '0', '', 'build', 'admin', '2025-11-26 17:38:59', 'admin', '2025-12-08 17:52:34', '');
INSERT INTO public.sys_menu VALUES (2044, '定时任务', 2041, 3, 'scheduledEngine', 'business/scheduledEngine/index', NULL, 'ScheduledEngine', '1', '1', 'C', '0', '0', '', 'cascader', 'admin', '2025-12-08 17:54:22', 'admin', '2025-12-08 17:55:30', '');
INSERT INTO public.sys_menu VALUES (2045, '地图服务', 2036, 3, 'map', 'business/map/index', NULL, 'Map', '1', '0', 'C', '0', '0', NULL, 'guide', 'admin', '2025-12-12 10:47:55', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2046, '数据转发', 2041, 1, 'engine', 'business/engine/index', NULL, 'Engine', '1', '1', 'C', '0', '0', NULL, 'engine', 'admin', '2025-12-23 13:35:44', '', NULL, '');
INSERT INTO public.sys_menu VALUES (2047, '官方文档', 0, 5, 'http://47.109.145.72:18000/', NULL, NULL, '', '0', '0', 'M', '0', '0', '', 'documentation', 'admin', '2026-01-13 10:37:37', 'admin', '2026-01-14 09:21:25', '');
INSERT INTO public.sys_menu VALUES (2050, '设备分组', 2036, 4, 'deviceGroup', 'business/deviceGroup/index', NULL, 'DeviceGroup', '1', '1', 'C', '0', '0', NULL, 'list', 'admin', '2026-01-18 18:58:13', '', NULL, '');


--
-- Data for Name: sys_notice; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.sys_notice VALUES (1, '温馨提醒：2018-07-01 若依新版本发布啦', '2', '\xe696b0e78988e69cace58685e5aeb9', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '管理员');
INSERT INTO public.sys_notice VALUES (2, '维护通知：2018-07-01 若依系统凌晨维护', '1', '\xe7bbb4e68aa4e58685e5aeb9', '0', 'admin', '2025-09-15 11:09:58', '', NULL, '管理员');


--
-- Data for Name: sys_post; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.sys_post VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_post VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_post VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2025-09-15 11:09:57', '', NULL, '');
INSERT INTO public.sys_post VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2025-09-15 11:09:57', '', NULL, '');


--
-- Data for Name: sys_role; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.sys_role VALUES (1, '超级管理员', 'admin', 1, '1', true, true, '0', '0', 'admin', '2025-09-15 11:09:57', '', NULL, '超级管理员');
INSERT INTO public.sys_role VALUES (2, '测试', 'role', 0, '1', true, true, '0', '0', 'admin', '2026-01-24 16:51:03.65', '', NULL, NULL);


--
-- Data for Name: sys_role_dept; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: sys_role_menu; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.sys_role_menu VALUES (2, 2036);
INSERT INTO public.sys_role_menu VALUES (2, 2012);
INSERT INTO public.sys_role_menu VALUES (2, 2013);
INSERT INTO public.sys_role_menu VALUES (2, 2014);
INSERT INTO public.sys_role_menu VALUES (2, 2015);
INSERT INTO public.sys_role_menu VALUES (2, 2016);
INSERT INTO public.sys_role_menu VALUES (2, 2017);
INSERT INTO public.sys_role_menu VALUES (2, 2006);
INSERT INTO public.sys_role_menu VALUES (2, 2007);
INSERT INTO public.sys_role_menu VALUES (2, 2008);
INSERT INTO public.sys_role_menu VALUES (2, 2009);
INSERT INTO public.sys_role_menu VALUES (2, 2010);
INSERT INTO public.sys_role_menu VALUES (2, 2011);
INSERT INTO public.sys_role_menu VALUES (2, 2045);
INSERT INTO public.sys_role_menu VALUES (2, 2050);


--
-- Data for Name: sys_user; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.sys_user VALUES (2, NULL, 'ruoyi', 'ruoyi', '00', '', '', '0', '', '$2a$10$sej0dh0AJhcbFljQU1PDg.klJJ5kzijsIiigtF4gnOn7/sNt1yc2C', '0', '0', '', NULL, NULL, 'admin', '2026-01-24 16:50:32.728', '', NULL, NULL);
INSERT INTO public.sys_user VALUES (1, 103, 'admin', '若依', '00', 'ry@163.com', '15888888888', '1', '/profile/avatar/2026/01/21/5ebbe04a1a52488e8d881a8538ce704f.jpg', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2026-09-19 09:26:40.226', '2025-12-05 16:23:20', 'admin', '2025-09-15 11:09:57', '', '2026-01-21 11:27:03', '管理员');


--
-- Data for Name: sys_user_post; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: sys_user_role; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.sys_user_role VALUES (1, 1);


--
-- Name: gen_table_column_column_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gen_table_column_column_id_seq', 10000, true);


--
-- Name: gen_table_table_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gen_table_table_id_seq', 10000, true);


--
-- Name: sys_config_config_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_config_config_id_seq', 10000, true);


--
-- Name: sys_dept_dept_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_dept_dept_id_seq', 10000, true);


--
-- Name: sys_dict_data_dict_code_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_dict_data_dict_code_seq', 10000, true);


--
-- Name: sys_dict_type_dict_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_dict_type_dict_id_seq', 10000, true);


--
-- Name: sys_job_job_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_job_job_id_seq', 10000, true);


--
-- Name: sys_job_log_job_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_job_log_job_log_id_seq', 10000, true);


--
-- Name: sys_logininfor_info_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_logininfor_info_id_seq', 10024, true);


--
-- Name: sys_menu_menu_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_menu_menu_id_seq', 10000, true);


--
-- Name: sys_notice_notice_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_notice_notice_id_seq', 10000, true);


--
-- Name: sys_oper_log_oper_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_oper_log_oper_id_seq', 10141, true);


--
-- Name: sys_post_post_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_post_post_id_seq', 10000, true);


--
-- Name: sys_role_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_role_role_id_seq', 10000, true);


--
-- Name: sys_user_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_user_user_id_seq', 10000, true);


--
-- Name: gen_table_column gen_table_column_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gen_table_column
    ADD CONSTRAINT gen_table_column_pkey PRIMARY KEY (column_id);


--
-- Name: gen_table gen_table_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gen_table
    ADD CONSTRAINT gen_table_pkey PRIMARY KEY (table_id);


--
-- Name: labdatahub_brother_config labdatahub_brother_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_brother_config
    ADD CONSTRAINT labdatahub_brother_config_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_component labdatahub_component_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_component
    ADD CONSTRAINT labdatahub_component_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_device_group labdatahub_device_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_device_group
    ADD CONSTRAINT labdatahub_device_group_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_device_logs labdatahub_device_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_device_logs
    ADD CONSTRAINT labdatahub_device_logs_pkey PRIMARY KEY (id, create_time, device_sn);


--
-- Name: labdatahub_device labdatahub_device_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_device
    ADD CONSTRAINT labdatahub_device_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_fanuc_config labdatahub_fanuc_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_fanuc_config
    ADD CONSTRAINT labdatahub_fanuc_config_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_function labdatahub_function_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_function
    ADD CONSTRAINT labdatahub_function_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_function_record labdatahub_function_record_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_function_record
    ADD CONSTRAINT labdatahub_function_record_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_linkage_action_record labdatahub_linkage_action_record_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_linkage_action_record
    ADD CONSTRAINT labdatahub_linkage_action_record_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_linkage_warn_record labdatahub_linkage_warn_record_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_linkage_warn_record
    ADD CONSTRAINT labdatahub_linkage_warn_record_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_media_device labdatahub_media_device_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_media_device
    ADD CONSTRAINT labdatahub_media_device_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_media_server labdatahub_media_server_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_media_server
    ADD CONSTRAINT labdatahub_media_server_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_mitsubishi_cnc_config labdatahub_mitsubishi_cnc_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_mitsubishi_cnc_config
    ADD CONSTRAINT labdatahub_mitsubishi_cnc_config_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_mitsubishi_mc3e_config labdatahub_mitsubishi_mc3e_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_mitsubishi_mc3e_config
    ADD CONSTRAINT labdatahub_mitsubishi_mc3e_config_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_s71200_config labdatahub_modbus_config_copy1_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_s71200_config
    ADD CONSTRAINT labdatahub_modbus_config_copy1_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_modbus_config labdatahub_modbus_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_modbus_config
    ADD CONSTRAINT labdatahub_modbus_config_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_point_write_record labdatahub_point_write_record_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_point_write_record
    ADD CONSTRAINT labdatahub_point_write_record_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_product labdatahub_product_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_product
    ADD CONSTRAINT labdatahub_product_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_properties labdatahub_properties_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_properties
    ADD CONSTRAINT labdatahub_properties_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_protocol labdatahub_protocol_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_protocol
    ADD CONSTRAINT labdatahub_protocol_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_rule_engine labdatahub_rule_engine_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_rule_engine
    ADD CONSTRAINT labdatahub_rule_engine_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_omronfins_config labdatahub_s71200_config_copy1_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_omronfins_config
    ADD CONSTRAINT labdatahub_s71200_config_copy1_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_db_config labdatahub_s71200_config_copy1_pkey1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_db_config
    ADD CONSTRAINT labdatahub_s71200_config_copy1_pkey1 PRIMARY KEY (id);


--
-- Name: labdatahub_scheduled_task labdatahub_scheduled_task_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_scheduled_task
    ADD CONSTRAINT labdatahub_scheduled_task_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_warn_config labdatahub_warn_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_warn_config
    ADD CONSTRAINT labdatahub_warn_config_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_warn_linkage labdatahub_warn_linkage_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_warn_linkage
    ADD CONSTRAINT labdatahub_warn_linkage_pkey PRIMARY KEY (id);


--
-- Name: labdatahub_warn_record labdatahub_warn_record_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.labdatahub_warn_record
    ADD CONSTRAINT labdatahub_warn_record_pkey PRIMARY KEY (id);


--
-- Name: qrtz_blob_triggers qrtz_blob_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_calendars qrtz_calendars_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_calendars
    ADD CONSTRAINT qrtz_calendars_pkey PRIMARY KEY (sched_name, calendar_name);


--
-- Name: qrtz_cron_triggers qrtz_cron_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_fired_triggers qrtz_fired_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_fired_triggers
    ADD CONSTRAINT qrtz_fired_triggers_pkey PRIMARY KEY (sched_name, entry_id);


--
-- Name: qrtz_job_details qrtz_job_details_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_job_details
    ADD CONSTRAINT qrtz_job_details_pkey PRIMARY KEY (sched_name, job_name, job_group);


--
-- Name: qrtz_locks qrtz_locks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_locks
    ADD CONSTRAINT qrtz_locks_pkey PRIMARY KEY (sched_name, lock_name);


--
-- Name: qrtz_paused_trigger_grps qrtz_paused_trigger_grps_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_paused_trigger_grps
    ADD CONSTRAINT qrtz_paused_trigger_grps_pkey PRIMARY KEY (sched_name, trigger_group);


--
-- Name: qrtz_scheduler_state qrtz_scheduler_state_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_scheduler_state
    ADD CONSTRAINT qrtz_scheduler_state_pkey PRIMARY KEY (sched_name, instance_name);


--
-- Name: qrtz_simple_triggers qrtz_simple_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_simprop_triggers qrtz_simprop_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_simprop_triggers
    ADD CONSTRAINT qrtz_simprop_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_triggers qrtz_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: sys_config sys_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_config
    ADD CONSTRAINT sys_config_pkey PRIMARY KEY (config_id);


--
-- Name: sys_dept sys_dept_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_dept
    ADD CONSTRAINT sys_dept_pkey PRIMARY KEY (dept_id);


--
-- Name: sys_dict_data sys_dict_data_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_dict_data
    ADD CONSTRAINT sys_dict_data_pkey PRIMARY KEY (dict_code);


--
-- Name: sys_dict_type sys_dict_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_dict_type
    ADD CONSTRAINT sys_dict_type_pkey PRIMARY KEY (dict_id);


--
-- Name: sys_job_log sys_job_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_job_log
    ADD CONSTRAINT sys_job_log_pkey PRIMARY KEY (job_log_id);


--
-- Name: sys_job sys_job_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_job
    ADD CONSTRAINT sys_job_pkey PRIMARY KEY (job_id);


--
-- Name: sys_logininfor sys_logininfor_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_logininfor
    ADD CONSTRAINT sys_logininfor_pkey PRIMARY KEY (info_id);


--
-- Name: sys_menu sys_menu_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_menu
    ADD CONSTRAINT sys_menu_pkey PRIMARY KEY (menu_id);


--
-- Name: sys_notice sys_notice_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_notice
    ADD CONSTRAINT sys_notice_pkey PRIMARY KEY (notice_id);


--
-- Name: sys_oper_log sys_oper_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_oper_log
    ADD CONSTRAINT sys_oper_log_pkey PRIMARY KEY (oper_id);


--
-- Name: sys_post sys_post_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_post
    ADD CONSTRAINT sys_post_pkey PRIMARY KEY (post_id);


--
-- Name: sys_role_dept sys_role_dept_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_role_dept
    ADD CONSTRAINT sys_role_dept_pkey PRIMARY KEY (role_id, dept_id);


--
-- Name: sys_role_menu sys_role_menu_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_role_menu
    ADD CONSTRAINT sys_role_menu_pkey PRIMARY KEY (role_id, menu_id);


--
-- Name: sys_role sys_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_role
    ADD CONSTRAINT sys_role_pkey PRIMARY KEY (role_id);


--
-- Name: sys_user sys_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_user
    ADD CONSTRAINT sys_user_pkey PRIMARY KEY (user_id);


--
-- Name: sys_user_post sys_user_post_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_user_post
    ADD CONSTRAINT sys_user_post_pkey PRIMARY KEY (user_id, post_id);


--
-- Name: sys_user_role sys_user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_user_role
    ADD CONSTRAINT sys_user_role_pkey PRIMARY KEY (user_id, role_id);


--
-- Name: idx_belong_sn; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_belong_sn ON public.labdatahub_function USING btree (belong_sn);


--
-- Name: idx_belong_sn_properties; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_belong_sn_properties ON public.labdatahub_properties USING btree (belong_sn);


--
-- Name: idx_device_logs_device_sn; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_device_logs_device_sn ON public.labdatahub_device_logs USING btree (device_sn, create_time DESC);


--
-- Name: idx_device_logs_log_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_device_logs_log_type ON public.labdatahub_device_logs USING btree (log_type, create_time DESC);


--
-- Name: idx_device_logs_report_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_device_logs_report_time ON public.labdatahub_device_logs USING btree (report_time DESC);


--
-- Name: idx_device_sn; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_device_sn ON public.labdatahub_device_logs USING btree (device_sn);


--
-- Name: idx_func_record_device_sn; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_func_record_device_sn ON public.labdatahub_function_record USING btree (device_sn);


--
-- Name: idx_identifier; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_identifier ON public.labdatahub_properties USING btree (identifier);


--
-- Name: idx_point_write_record_code; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_point_write_record_code ON public.labdatahub_point_write_record USING btree (code, create_time);


--
-- Name: idx_point_write_record_device; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_point_write_record_device ON public.labdatahub_point_write_record USING btree (device_sn, create_time);


--
-- Name: idx_qrtz_triggers_job; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qrtz_triggers_job ON public.qrtz_triggers USING btree (sched_name, job_name, job_group);


--
-- Name: idx_sys_logininfor_lt; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_logininfor_lt ON public.sys_logininfor USING btree (login_time);


--
-- Name: idx_sys_logininfor_s; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_logininfor_s ON public.sys_logininfor USING btree (status);


--
-- Name: idx_sys_oper_log_bt; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_oper_log_bt ON public.sys_oper_log USING btree (business_type);


--
-- Name: idx_sys_oper_log_ot; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_oper_log_ot ON public.sys_oper_log USING btree (oper_time);


--
-- Name: idx_sys_oper_log_s; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_oper_log_s ON public.sys_oper_log USING btree (status);


--
-- Name: idx_warn_record_belong_sn; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_warn_record_belong_sn ON public.labdatahub_warn_record USING btree (belong_sn);


--
-- Name: labdatahub_device_logs_create_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX labdatahub_device_logs_create_time_idx ON public.labdatahub_device_logs USING btree (create_time DESC);


--
-- Name: uniq_device_sn; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uniq_device_sn ON public.labdatahub_device USING btree (device_sn);


--
-- Name: uniq_dict_type; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uniq_dict_type ON public.sys_dict_type USING btree (dict_type);


--
-- Name: uniq_group_code; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uniq_group_code ON public.labdatahub_device_group USING btree (group_code);


--
-- Name: uniq_product_sn; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uniq_product_sn ON public.labdatahub_product USING btree (product_sn);


--
-- Name: qrtz_blob_triggers qrtz_blob_triggers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_fk FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES public.qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_cron_triggers qrtz_cron_triggers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_fk FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES public.qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_simple_triggers qrtz_simple_triggers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_fk FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES public.qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_simprop_triggers qrtz_simprop_triggers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_simprop_triggers
    ADD CONSTRAINT qrtz_simprop_triggers_fk FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES public.qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_triggers qrtz_triggers_job_details_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_job_details_fk FOREIGN KEY (sched_name, job_name, job_group) REFERENCES public.qrtz_job_details(sched_name, job_name, job_group);


--
-- ============================================================================
-- init.sql 结束。接着执行 db.sql（TimescaleDB 扩展 + device_logs 转超表）。
-- ============================================================================
