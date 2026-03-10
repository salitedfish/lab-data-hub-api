package com.labdatahub.business.service.impl;

import java.util.List;
        import com.labdatahub.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubModbusConfigMapper;
import com.labdatahub.business.domain.LabdatahubModbusConfig;
import com.labdatahub.business.service.ILabdatahubModbusConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * modbus协议读取配置Service业务层处理
 *
 * @author ruoyi
 * @date 2025-12-16
 */
@Service
public class LabdatahubModbusConfigServiceImpl extends ServiceImpl<LabdatahubModbusConfigMapper, LabdatahubModbusConfig> implements ILabdatahubModbusConfigService
{
    @Autowired
    private LabdatahubModbusConfigMapper labdatahubModbusConfigMapper;

    /**
     * 查询modbus协议读取配置
     *
     * @param id modbus协议读取配置主键
     * @return modbus协议读取配置
     */
    @Override
    public LabdatahubModbusConfig selectLabdatahubModbusConfigById(String id)
    {
        return labdatahubModbusConfigMapper.selectLabdatahubModbusConfigById(id);
    }

    /**
     * 查询modbus协议读取配置列表
     *
     * @param labdatahubModbusConfig modbus协议读取配置
     * @return modbus协议读取配置
     */
    @Override
    public List<LabdatahubModbusConfig> selectLabdatahubModbusConfigList(LabdatahubModbusConfig labdatahubModbusConfig)
    {
        return labdatahubModbusConfigMapper.selectLabdatahubModbusConfigList(labdatahubModbusConfig);
    }

    /**
     * 新增modbus协议读取配置
     *
     * @param labdatahubModbusConfig modbus协议读取配置
     * @return 结果
     */
    @Override
    public int insertLabdatahubModbusConfig(LabdatahubModbusConfig labdatahubModbusConfig)
    {
        labdatahubModbusConfig.setCreateTime(DateUtils.getNowDate());
        return labdatahubModbusConfigMapper.insertLabdatahubModbusConfig(labdatahubModbusConfig);
    }

    /**
     * 修改modbus协议读取配置
     *
     * @param labdatahubModbusConfig modbus协议读取配置
     * @return 结果
     */
    @Override
    public int updateLabdatahubModbusConfig(LabdatahubModbusConfig labdatahubModbusConfig)
    {
        return labdatahubModbusConfigMapper.updateLabdatahubModbusConfig(labdatahubModbusConfig);
    }

    /**
     * 批量删除modbus协议读取配置
     *
     * @param ids 需要删除的modbus协议读取配置主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubModbusConfigByIds(String[] ids)
    {
        return labdatahubModbusConfigMapper.deleteLabdatahubModbusConfigByIds(ids);
    }

    /**
     * 删除modbus协议读取配置信息
     *
     * @param id modbus协议读取配置主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubModbusConfigById(String id)
    {
        return labdatahubModbusConfigMapper.deleteLabdatahubModbusConfigById(id);
    }
}
