package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubProduct;

/**
 * 产品Mapper接口
 *
 * @author labdatahub
 * @date 2025-09-18
 */
public interface LabdatahubProductMapper extends BaseMapper<LabdatahubProduct>
{
    /**
     * 查询产品
     *
     * @param id 产品主键
     * @return 产品
     */
    public LabdatahubProduct selectLabdatahubProductById(String id);

    /**
     * 查询产品列表
     *
     * @param labdatahubProduct 产品
     * @return 产品集合
     */
    public List<LabdatahubProduct> selectLabdatahubProductList(LabdatahubProduct labdatahubProduct);

    /**
     * 新增产品
     *
     * @param labdatahubProduct 产品
     * @return 结果
     */
    public int insertLabdatahubProduct(LabdatahubProduct labdatahubProduct);

    /**
     * 修改产品
     *
     * @param labdatahubProduct 产品
     * @return 结果
     */
    public int updateLabdatahubProduct(LabdatahubProduct labdatahubProduct);

    /**
     * 删除产品
     *
     * @param id 产品主键
     * @return 结果
     */
    public int deleteLabdatahubProductById(String id);

    /**
     * 批量删除产品
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubProductByIds(String[] ids);
}
