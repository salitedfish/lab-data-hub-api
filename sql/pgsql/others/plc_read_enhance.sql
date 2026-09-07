-- PLC 协议「读」能力增强迁移脚本（勿合入 init.sql）
-- 新增列均有默认值，老数据自动兼容（function_code 默认 '03' 保持寄存器，area_type 默认 'DB'）

ALTER TABLE labdatahub_modbus_config ADD COLUMN IF NOT EXISTS function_code varchar(32) DEFAULT '03';
ALTER TABLE labdatahub_s71200_config ADD COLUMN IF NOT EXISTS area_type varchar(32) DEFAULT 'DB';
