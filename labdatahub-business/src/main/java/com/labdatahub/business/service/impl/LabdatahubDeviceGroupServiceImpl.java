package com.labdatahub.business.service.impl;

import java.util.List;
import com.labdatahub.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubDeviceGroupMapper;
import com.labdatahub.business.domain.LabdatahubDeviceGroup;
import com.labdatahub.business.service.ILabdatahubDeviceGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 设备分组Service业务层处理
 *
 * @author ruoyi
 * @date 2026-01-18
 */
@Service
public class LabdatahubDeviceGroupServiceImpl extends ServiceImpl<LabdatahubDeviceGroupMapper, LabdatahubDeviceGroup> implements ILabdatahubDeviceGroupService
{
    @Autowired
    private LabdatahubDeviceGroupMapper labdatahubDeviceGroupMapper;

    /**
     * 查询设备分组
     *
     * @param id 设备分组主键
     * @return 设备分组
     */
    @Override
    public LabdatahubDeviceGroup selectLabdatahubDeviceGroupById(String id)
    {
        return labdatahubDeviceGroupMapper.selectLabdatahubDeviceGroupById(id);
    }

    /**
     * 查询设备分组列表
     *
     * @param labdatahubDeviceGroup 设备分组
     * @return 设备分组
     */
    @Override
    public List<LabdatahubDeviceGroup> selectLabdatahubDeviceGroupList(LabdatahubDeviceGroup labdatahubDeviceGroup)
    {
        return labdatahubDeviceGroupMapper.selectLabdatahubDeviceGroupList(labdatahubDeviceGroup);
    }

    /**
     * 新增设备分组
     *
     * @param labdatahubDeviceGroup 设备分组
     * @return 结果
     */
    @Override
    public int insertLabdatahubDeviceGroup(LabdatahubDeviceGroup labdatahubDeviceGroup)
    {
        labdatahubDeviceGroup.setCreateTime(DateUtils.getNowDate());
        return labdatahubDeviceGroupMapper.insertLabdatahubDeviceGroup(labdatahubDeviceGroup);
    }

    /**
     * 修改设备分组
     *
     * @param labdatahubDeviceGroup 设备分组
     * @return 结果
     */
    @Override
    public int updateLabdatahubDeviceGroup(LabdatahubDeviceGroup labdatahubDeviceGroup)
    {
        return labdatahubDeviceGroupMapper.updateLabdatahubDeviceGroup(labdatahubDeviceGroup);
    }

    /**
     * 批量删除设备分组
     *
     * @param ids 需要删除的设备分组主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubDeviceGroupByIds(String[] ids)
    {
        return labdatahubDeviceGroupMapper.deleteLabdatahubDeviceGroupByIds(ids);
    }

    /**
     * 删除设备分组信息
     *
     * @param id 设备分组主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubDeviceGroupById(String id)
    {
        return labdatahubDeviceGroupMapper.deleteLabdatahubDeviceGroupById(id);
    }
}
