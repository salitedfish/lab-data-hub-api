-- 设备数量冗余列回填：labdatahub_product.device_count = labdatahub_device 按 product_sn 的真实统计
-- 背景：产品列表/详情接口已改为动态统计 device_count（不直接信该列），删除产品守卫亦用真实数；
--       本列仅剩导出等直接读列的方使用，回填保证存量数据一致。可重复执行（幂等）。
with c as (
    select p.id, coalesce(t.cnt, 0) as device_count
    from labdatahub_product p
    left join (
        select product_sn, count(*) as cnt
        from labdatahub_device
        group by product_sn
    ) t on t.product_sn = p.product_sn
)
update labdatahub_product p
set device_count = c.device_count
from c
where p.id = c.id;
