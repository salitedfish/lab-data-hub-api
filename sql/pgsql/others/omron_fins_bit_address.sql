-- ============================================
-- 欧姆龙 FINS 配置表新增位号列（位访问支持）
-- 表：labdatahub_omronfins_config
-- 独立增量脚本，勿合入 init.sql，部署时单独执行。
--
-- 背景：FINS 的地址是 3 字节 —— 前 2 字节为「字地址」、第 3 字节为「位号」。
--   字访问时第 3 字节恒为 0，位访问时填 0-15。原来只有 start_address 一列，
--   位访问无法表达，故新增 bit_address。
--
-- 语义约定（后端 LabdatahubOmronFinsConfigController#checkConfig 强校验）：
--   bit_address IS NULL → 字访问：区码必须是字区码，length 为字个数（1-1000）
--   bit_address NOT NULL → 位访问：区码必须是位区码，bit_address 0-15，length 必须为 1
--   区码决定第 3 字节的含义，配错不是报错而是写/读到别的地址，所以两边必须一致。
-- ============================================

ALTER TABLE "public"."labdatahub_omronfins_config"
  ADD COLUMN IF NOT EXISTS "bit_address" numeric(10,0);

COMMENT ON COLUMN "public"."labdatahub_omronfins_config"."bit_address" IS '位号（仅位区码点位使用，0-15；字区为 NULL 表示按字访问）';

-- 顺带纠正两处旧注释（列本身不动，只改注释文字）：
--   start_address 原注释写「起始字节」，FINS 的寻址单位是「字」，不是字节；
--   length 原注释写「读取长度（字节）」，实际字区=字个数、位区=位个数。
COMMENT ON COLUMN "public"."labdatahub_omronfins_config"."start_address" IS '起始字地址（位区下这是字地址，位号另看 bit_address，0-65535）';
COMMENT ON COLUMN "public"."labdatahub_omronfins_config"."length" IS '读取长度：字区=字个数，位区=位个数（位点位恒为 1）';
