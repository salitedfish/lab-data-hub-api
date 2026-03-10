package com.labdatahub.business.service;

import java.util.List;
import com.labdatahub.business.domain.LabdatahubDeviceGroup;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 设备分组Service接口
 *
 * @author ruoyi
 * @date 2026-01-18
 */
public interface ILabdatahubDeviceGroupService extends IService<LabdatahubDeviceGroup>
{
    /**
     * 查询设备分组
     *
     * @param id 设备分组主键
     * @return 设备分组
     */
    public LabdatahubDeviceGroup selectLabdatahubDeviceGroupById(String id);

    /**
     * 查询设备分组列表
     *
     * @param labdatahubDeviceGroup 设备分组
     * @return 设备分组集合
     */
    public List<LabdatahubDeviceGroup> selectLabdatahubDeviceGroupList(LabdatahubDeviceGroup labdatahubDeviceGroup);

    /**
     * 新增设备分组
     *
     * @param labdatahubDeviceGroup 设备分组
     * @return 结果
     */
    public int insertLabdatahubDeviceGroup(LabdatahubDeviceGroup labdatahubDeviceGroup);

    /**
     * 修改设备分组
     *
     * @param labdatahubDeviceGroup 设备分组
     * @return 结果
     */
    public int updateLabdatahubDeviceGroup(LabdatahubDeviceGroup labdatahubDeviceGroup);

    /**
     * 批量删除设备分组
     *
     * @param ids 需要删除的设备分组主键集合
     * @return 结果
     */
    public int deleteLabdatahubDeviceGroupByIds(String[] ids);

    /**
     * 删除设备分组信息
     *
     * @param id 设备分组主键
     * @return 结果
     */
    public int deleteLabdatahubDeviceGroupById(String id);
}
