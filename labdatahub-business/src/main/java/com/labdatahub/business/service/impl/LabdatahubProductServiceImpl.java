package com.labdatahub.business.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubProductMapper;
import com.labdatahub.business.domain.LabdatahubProduct;
import com.labdatahub.business.service.ILabdatahubProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 产品Service业务层处理
 *
 * @author labdatahub
 * @date 2025-09-18
 */
@Service
public class LabdatahubProductServiceImpl extends ServiceImpl<LabdatahubProductMapper, LabdatahubProduct> implements ILabdatahubProductService
{
    @Autowired
    private LabdatahubProductMapper labdatahubProductMapper;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    /**
     * 查询产品
     *
     * @param id 产品主键
     * @return 产品
     */
    @Override
    public LabdatahubProduct selectLabdatahubProductById(String id)
    {
        return labdatahubProductMapper.selectLabdatahubProductById(id);
    }

    /**
     * 查询产品列表
     *
     * @param labdatahubProduct 产品
     * @return 产品
     */
    @Override
    public List<LabdatahubProduct> selectLabdatahubProductList(LabdatahubProduct labdatahubProduct)
    {
        return labdatahubProductMapper.selectLabdatahubProductList(labdatahubProduct);
    }

    /**
     * 新增产品
     *
     * @param labdatahubProduct 产品
     * @return 结果
     */
    @Override
    public int insertLabdatahubProduct(LabdatahubProduct labdatahubProduct)
    {
            return labdatahubProductMapper.insertLabdatahubProduct(labdatahubProduct);
    }

    /**
     * 修改产品
     *
     * @param labdatahubProduct 产品
     * @return 结果
     */
    @Override
    public int updateLabdatahubProduct(LabdatahubProduct labdatahubProduct)
    {
        return labdatahubProductMapper.updateLabdatahubProduct(labdatahubProduct);
    }

    /**
     * 批量删除产品
     *
     * @param ids 需要删除的产品主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubProductByIds(String[] ids)
    {
        return labdatahubProductMapper.deleteLabdatahubProductByIds(ids);
    }

    /**
     * 删除产品信息
     *
     * @param id 产品主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubProductById(String id)
    {
        return labdatahubProductMapper.deleteLabdatahubProductById(id);
    }

    @Override
    public void syncDeviceCount(String productSn) {
        long countDevice = labdatahubDeviceService.count(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        LabdatahubProduct labdatahubProduct = new LabdatahubProduct();
        labdatahubProduct.setProductSn(productSn);
        labdatahubProduct.setDeviceCount(countDevice);
        labdatahubProductMapper.update(labdatahubProduct,new LambdaUpdateWrapper<LabdatahubProduct>()
                .eq(LabdatahubProduct::getProductSn,labdatahubProduct.getProductSn())
                .set(LabdatahubProduct::getDeviceCount,labdatahubProduct.getDeviceCount()));
    }
}
