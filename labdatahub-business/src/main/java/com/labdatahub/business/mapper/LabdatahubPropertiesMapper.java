package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubProperties;

/**
 * 物模型属性定义Mapper接口
 *
 * @author labdatahub
 * @date 2025-09-18
 */
public interface LabdatahubPropertiesMapper extends BaseMapper<LabdatahubProperties>
{
    /**
     * 查询物模型属性定义
     *
     * @param id 物模型属性定义主键
     * @return 物模型属性定义
     */
    public LabdatahubProperties selectLabdatahubPropertiesById(String id);

    /**
     * 查询物模型属性定义列表
     *
     * @param labdatahubProperties 物模型属性定义
     * @return 物模型属性定义集合
     */
    public List<LabdatahubProperties> selectLabdatahubPropertiesList(LabdatahubProperties labdatahubProperties);

    /**
     * 新增物模型属性定义
     *
     * @param labdatahubProperties 物模型属性定义
     * @return 结果
     */
    public int insertLabdatahubProperties(LabdatahubProperties labdatahubProperties);

    /**
     * 修改物模型属性定义
     *
     * @param labdatahubProperties 物模型属性定义
     * @return 结果
     */
    public int updateLabdatahubProperties(LabdatahubProperties labdatahubProperties);

    /**
     * 删除物模型属性定义
     *
     * @param id 物模型属性定义主键
     * @return 结果
     */
    public int deleteLabdatahubPropertiesById(String id);

    /**
     * 批量删除物模型属性定义
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubPropertiesByIds(String[] ids);
}
