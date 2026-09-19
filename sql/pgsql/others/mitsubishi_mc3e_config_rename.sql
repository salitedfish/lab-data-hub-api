-- ============================================
-- 三菱 MC 协议配置表迁移：老表名 → 新表名，并去掉已废弃的帧模式列
-- 独立增量脚本，勿合入 init.sql。
--   psql -U <user> -d <database> -f mitsubishi_mc3e_config_rename.sql
--
-- 背景（2026-09-19 协议拆分）：
--   原来的 MITSUBISHI_TCP 一个 netType 底下混着 3E / 1E 两种帧，帧模式由点位上的
--   protocol_mode 列逐条指定。现在按帧拆成三个独立协议（MITSUBISHI_MC3E_TCP /
--   MITSUBISHI_MC1E_TCP / MITSUBISHI_A1E_TCP），一个 netType 一种帧，
--   帧模式不再是「配置项」，protocol_mode 列随之废弃。
--   表名同步改成自描述的 labdatahub_mitsubishi_mc3e_config，与 domain 的
--   @TableName 对齐（不改名的话 Java 会去读一张不存在的表）。
--
-- 数据保留：本脚本用 RENAME 而非 DROP+CREATE，老库里的点位配置原样迁移，不丢数据。
-- 全新部署不需要本脚本，直接用 mitsubishi_mc3e_config.sql 建新表。
-- 二者不要同时执行。
-- ============================================

-- 老表存在、新表尚未建立时才改名（重复执行安全）
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname = 'public' AND tablename = 'labdatahub_mitsubishi_config')
       AND NOT EXISTS (SELECT 1 FROM pg_tables WHERE schemaname = 'public' AND tablename = 'labdatahub_mitsubishi_mc3e_config') THEN
        ALTER TABLE "public"."labdatahub_mitsubishi_config" RENAME TO "labdatahub_mitsubishi_mc3e_config";
        RAISE NOTICE '已改名：labdatahub_mitsubishi_config -> labdatahub_mitsubishi_mc3e_config';
    ELSE
        RAISE NOTICE '跳过改名：老表不存在，或新表已建立';
    END IF;
END $$;

-- 主键约束名同步（老约束名还挂着旧表名）
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'labdatahub_mitsubishi_config_pkey') THEN
        ALTER TABLE "public"."labdatahub_mitsubishi_mc3e_config"
            RENAME CONSTRAINT "labdatahub_mitsubishi_config_pkey" TO "labdatahub_mitsubishi_mc3e_config_pkey";
    END IF;
END $$;

-- 去掉废弃的帧模式列（3E 独立成协议后不再需要）
ALTER TABLE "public"."labdatahub_mitsubishi_mc3e_config"
    DROP COLUMN IF EXISTS "protocol_mode";

COMMENT ON TABLE "public"."labdatahub_mitsubishi_mc3e_config" IS '三菱MC协议（QnA兼容3E二进制帧）读取配置表';
