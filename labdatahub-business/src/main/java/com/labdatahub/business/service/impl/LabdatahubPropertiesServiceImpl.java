package com.labdatahub.business.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubPropertiesMapper;
import com.labdatahub.business.domain.LabdatahubProperties;
import com.labdatahub.business.service.ILabdatahubPropertiesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 物模型属性定义Service业务层处理
 *
 * @author labdatahub
 * @date 2025-09-18
 */
@Service
public class LabdatahubPropertiesServiceImpl extends ServiceImpl<LabdatahubPropertiesMapper, LabdatahubProperties> implements ILabdatahubPropertiesService {
    @Autowired
    private LabdatahubPropertiesMapper labdatahubPropertiesMapper;

    /**
     * 查询物模型属性定义
     *
     * @param id 物模型属性定义主键
     * @return 物模型属性定义
     */
    @Override
    public LabdatahubProperties selectLabdatahubPropertiesById(String id) {
        return labdatahubPropertiesMapper.selectLabdatahubPropertiesById(id);
    }

    /**
     * 查询物模型属性定义列表
     *
     * @param labdatahubProperties 物模型属性定义
     * @return 物模型属性定义
     */
    @Override
    public List<LabdatahubProperties> selectLabdatahubPropertiesList(LabdatahubProperties labdatahubProperties) {
        return labdatahubPropertiesMapper.selectLabdatahubPropertiesList(labdatahubProperties);
    }

    /**
     * 新增物模型属性定义
     *
     * @param labdatahubProperties 物模型属性定义
     * @return 结果
     */
    @Override
    public int insertLabdatahubProperties(LabdatahubProperties labdatahubProperties) {
        return labdatahubPropertiesMapper.insertLabdatahubProperties(labdatahubProperties);
    }

    /**
     * 修改物模型属性定义
     *
     * @param labdatahubProperties 物模型属性定义
     * @return 结果
     */
    @Override
    public int updateLabdatahubProperties(LabdatahubProperties labdatahubProperties) {
        return labdatahubPropertiesMapper.updateLabdatahubProperties(labdatahubProperties);
    }

    /**
     * 批量删除物模型属性定义
     *
     * @param ids 需要删除的物模型属性定义主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubPropertiesByIds(String[] ids) {
        return labdatahubPropertiesMapper.deleteLabdatahubPropertiesByIds(ids);
    }

    /**
     * 删除物模型属性定义信息
     *
     * @param id 物模型属性定义主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubPropertiesById(String id) {
        return labdatahubPropertiesMapper.deleteLabdatahubPropertiesById(id);
    }
}
