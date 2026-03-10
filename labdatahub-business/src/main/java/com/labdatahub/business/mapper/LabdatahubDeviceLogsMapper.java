package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubDeviceLogs;

/**
 * 设备日志Mapper接口
 *
 * @author labdatahub
 * @date 2025-09-22
 */
public interface LabdatahubDeviceLogsMapper extends BaseMapper<LabdatahubDeviceLogs>
{
    /**
     * 查询设备日志
     *
     * @param id 设备日志主键
     * @return 设备日志
     */
    public LabdatahubDeviceLogs selectLabdatahubDeviceLogsById(Long id);

    /**
     * 查询设备日志列表
     *
     * @param labdatahubDeviceLogs 设备日志
     * @return 设备日志集合
     */
    public List<LabdatahubDeviceLogs> selectLabdatahubDeviceLogsList(LabdatahubDeviceLogs labdatahubDeviceLogs);

    /**
     * 新增设备日志
     *
     * @param labdatahubDeviceLogs 设备日志
     * @return 结果
     */
    public int insertLabdatahubDeviceLogs(LabdatahubDeviceLogs labdatahubDeviceLogs);

    /**
     * 修改设备日志
     *
     * @param labdatahubDeviceLogs 设备日志
     * @return 结果
     */
    public int updateLabdatahubDeviceLogs(LabdatahubDeviceLogs labdatahubDeviceLogs);

    /**
     * 删除设备日志
     *
     * @param id 设备日志主键
     * @return 结果
     */
    public int deleteLabdatahubDeviceLogsById(Long id);

    /**
     * 批量删除设备日志
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubDeviceLogsByIds(Long[] ids);
}
