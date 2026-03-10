package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubDevice;

/**
 * 设备Mapper接口
 *
 * @author labdatahub
 * @date 2025-09-18
 */
public interface LabdatahubDeviceMapper extends BaseMapper<LabdatahubDevice>
{
    /**
     * 查询设备
     *
     * @param id 设备主键
     * @return 设备
     */
    public LabdatahubDevice selectLabdatahubDeviceById(String id);

    /**
     * 查询设备列表
     *
     * @param labdatahubDevice 设备
     * @return 设备集合
     */
    public List<LabdatahubDevice> selectLabdatahubDeviceList(LabdatahubDevice labdatahubDevice);

    /**
     * 新增设备
     *
     * @param labdatahubDevice 设备
     * @return 结果
     */
    public int insertLabdatahubDevice(LabdatahubDevice labdatahubDevice);

    /**
     * 修改设备
     *
     * @param labdatahubDevice 设备
     * @return 结果
     */
    public int updateLabdatahubDevice(LabdatahubDevice labdatahubDevice);

    /**
     * 删除设备
     *
     * @param id 设备主键
     * @return 结果
     */
    public int deleteLabdatahubDeviceById(String id);

    /**
     * 批量删除设备
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubDeviceByIds(String[] ids);
}
