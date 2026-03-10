package com.labdatahub.business.service.impl;

import java.util.List;

import com.labdatahub.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubDeviceLogsMapper;
import com.labdatahub.business.domain.LabdatahubDeviceLogs;
import com.labdatahub.business.service.ILabdatahubDeviceLogsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 设备日志Service业务层处理
 *
 * @author labdatahub
 * @date 2025-09-22
 */
@Service
public class LabdatahubDeviceLogsServiceImpl extends ServiceImpl<LabdatahubDeviceLogsMapper, LabdatahubDeviceLogs> implements ILabdatahubDeviceLogsService {
    @Autowired
    private LabdatahubDeviceLogsMapper labdatahubDeviceLogsMapper;

    /**
     * 查询设备日志
     *
     * @param id 设备日志主键
     * @return 设备日志
     */
    @Override
    public LabdatahubDeviceLogs selectLabdatahubDeviceLogsById(Long id) {
        return labdatahubDeviceLogsMapper.selectLabdatahubDeviceLogsById(id);
    }

    /**
     * 查询设备日志列表
     *
     * @param labdatahubDeviceLogs 设备日志
     * @return 设备日志
     */
    @Override
    public List<LabdatahubDeviceLogs> selectLabdatahubDeviceLogsList(LabdatahubDeviceLogs labdatahubDeviceLogs) {
        return labdatahubDeviceLogsMapper.selectLabdatahubDeviceLogsList(labdatahubDeviceLogs);
    }

    /**
     * 新增设备日志
     *
     * @param labdatahubDeviceLogs 设备日志
     * @return 结果
     */
    @Override
    public int insertLabdatahubDeviceLogs(LabdatahubDeviceLogs labdatahubDeviceLogs) {
        return labdatahubDeviceLogsMapper.insertLabdatahubDeviceLogs(labdatahubDeviceLogs);
    }

    /**
     * 修改设备日志
     *
     * @param labdatahubDeviceLogs 设备日志
     * @return 结果
     */
    @Override
    public int updateLabdatahubDeviceLogs(LabdatahubDeviceLogs labdatahubDeviceLogs) {
        return labdatahubDeviceLogsMapper.updateLabdatahubDeviceLogs(labdatahubDeviceLogs);
    }

    /**
     * 批量删除设备日志
     *
     * @param ids 需要删除的设备日志主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubDeviceLogsByIds(Long[] ids) {
        return labdatahubDeviceLogsMapper.deleteLabdatahubDeviceLogsByIds(ids);
    }

    /**
     * 删除设备日志信息
     *
     * @param id 设备日志主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubDeviceLogsById(Long id) {
        return labdatahubDeviceLogsMapper.deleteLabdatahubDeviceLogsById(id);
    }
}
