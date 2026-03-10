package com.labdatahub.business.service;

import java.util.List;
import com.labdatahub.business.domain.LabdatahubDeviceLogs;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 设备日志Service接口
 *
 * @author labdatahub
 * @date 2025-09-22
 */
public interface ILabdatahubDeviceLogsService extends IService<LabdatahubDeviceLogs>
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
     * 批量删除设备日志
     *
     * @param ids 需要删除的设备日志主键集合
     * @return 结果
     */
    public int deleteLabdatahubDeviceLogsByIds(Long[] ids);

    /**
     * 删除设备日志信息
     *
     * @param id 设备日志主键
     * @return 结果
     */
    public int deleteLabdatahubDeviceLogsById(Long id);
}
