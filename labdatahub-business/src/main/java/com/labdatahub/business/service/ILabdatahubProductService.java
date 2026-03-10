package com.labdatahub.business.service;

import java.util.List;
import com.labdatahub.business.domain.LabdatahubProduct;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 产品Service接口
 *
 * @author labdatahub
 * @date 2025-09-18
 */
public interface ILabdatahubProductService extends IService<LabdatahubProduct>
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
     * 批量删除产品
     *
     * @param ids 需要删除的产品主键集合
     * @return 结果
     */
    public int deleteLabdatahubProductByIds(String[] ids);

    /**
     * 删除产品信息
     *
     * @param id 产品主键
     * @return 结果
     */
    public int deleteLabdatahubProductById(String id);

    /**
     * 同步产品总设备数量
     */
    public void syncDeviceCount(String productSn);
}
