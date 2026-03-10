package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubComponent;

/**
 * 网络组件Mapper接口
 *
 * @author labdatahub
 * @date 2025-09-18
 */
public interface LabdatahubComponentMapper extends BaseMapper<LabdatahubComponent>
{
    /**
     * 查询网络组件
     *
     * @param id 网络组件主键
     * @return 网络组件
     */
    public LabdatahubComponent selectLabdatahubComponentById(String id);

    /**
     * 查询网络组件列表
     *
     * @param labdatahubComponent 网络组件
     * @return 网络组件集合
     */
    public List<LabdatahubComponent> selectLabdatahubComponentList(LabdatahubComponent labdatahubComponent);

    /**
     * 新增网络组件
     *
     * @param labdatahubComponent 网络组件
     * @return 结果
     */
    public int insertLabdatahubComponent(LabdatahubComponent labdatahubComponent);

    /**
     * 修改网络组件
     *
     * @param labdatahubComponent 网络组件
     * @return 结果
     */
    public int updateLabdatahubComponent(LabdatahubComponent labdatahubComponent);

    /**
     * 删除网络组件
     *
     * @param id 网络组件主键
     * @return 结果
     */
    public int deleteLabdatahubComponentById(String id);

    /**
     * 批量删除网络组件
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubComponentByIds(String[] ids);
}
