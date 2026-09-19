-- 协议点位表加 name 列（点位名称）
-- 用途：物模型属性名用点位名称（前端点位表语义名，如 FANUC"机械坐标 X"），null 时物模型 name 用 code（标识）
-- 仅 7 张协议点位表（含 code 关联物模型）；Database 协议 code=null 不关联物模型，不加
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
