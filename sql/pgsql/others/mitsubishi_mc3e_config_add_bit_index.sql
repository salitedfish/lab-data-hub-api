-- ============================================
-- 三菱 MC 点位表增量：新增字内位号 bit_index（支持 D150.0 / D150.1 这种点位）
-- 老库增量脚本；同一改动已整合进 init.sql（2026-10-22 手工同步），全新部署无需执行本脚本。
--   psql -U <user> -d <database> -f mitsubishi_mc3e_config_add_bit_index.sql
--
-- 背景（2026-10-22）：
--   现场供应商点位写成 D150.0 / D150.1，即「D150 这个字里的第 0 / 1 位」。
--   三菱对字设备（D/W/R/ZR/SD）不支持按位读（实测位单位读 D150 返回结束码 0xC05C），
--   所以平台按「读整字 + 取位」实现：bit_index 存位号 0-15，轮询读 1 个字，
--   消费侧 (word >> bit_index) & 1 后作为 0/1 上报（物模型 dataType 用 bool）。
--
--   bit_index 为空 = 原来的整字（字设备）/整位（位设备）点位，行为不变。
--   X/Y 等位设备不使用该列（直接按位编号读）。
--
-- 幂等：重复执行安全。
-- ============================================

ALTER TABLE "public"."labdatahub_mitsubishi_mc3e_config"
    ADD COLUMN IF NOT EXISTS "bit_index" int4;

COMMENT ON COLUMN "public"."labdatahub_mitsubishi_mc3e_config"."bit_index" IS '字内位号 0-15（D150.0 存 0）；NULL=整字/整位点位';

-- 同一设备下 bit_index 只约束取值范围，点位唯一性仍由 code 保证
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'labdatahub_mitsubishi_mc3e_config_bit_index_check'
    ) THEN
        ALTER TABLE "public"."labdatahub_mitsubishi_mc3e_config"
            ADD CONSTRAINT "labdatahub_mitsubishi_mc3e_config_bit_index_check"
            CHECK ("bit_index" IS NULL OR ("bit_index" >= 0 AND "bit_index" <= 15));
    END IF;
END $$;
