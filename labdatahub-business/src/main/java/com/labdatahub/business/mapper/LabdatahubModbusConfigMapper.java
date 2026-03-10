package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubModbusConfig;

/**
 * modbus协议读取配置Mapper接口
 *
 * @author ruoyi
 * @date 2025-12-16
 */
public interface LabdatahubModbusConfigMapper extends BaseMapper<LabdatahubModbusConfig>
{
    /**
     * 查询modbus协议读取配置
     *
     * @param id modbus协议读取配置主键
     * @return modbus协议读取配置
     */
    public LabdatahubModbusConfig selectLabdatahubModbusConfigById(String id);

    /**
     * 查询modbus协议读取配置列表
     *
     * @param labdatahubModbusConfig modbus协议读取配置
     * @return modbus协议读取配置集合
     */
    public List<LabdatahubModbusConfig> selectLabdatahubModbusConfigList(LabdatahubModbusConfig labdatahubModbusConfig);

    /**
     * 新增modbus协议读取配置
     *
     * @param labdatahubModbusConfig modbus协议读取配置
     * @return 结果
     */
    public int insertLabdatahubModbusConfig(LabdatahubModbusConfig labdatahubModbusConfig);

    /**
     * 修改modbus协议读取配置
     *
     * @param labdatahubModbusConfig modbus协议读取配置
     * @return 结果
     */
    public int updateLabdatahubModbusConfig(LabdatahubModbusConfig labdatahubModbusConfig);

    /**
     * 删除modbus协议读取配置
     *
     * @param id modbus协议读取配置主键
     * @return 结果
     */
    public int deleteLabdatahubModbusConfigById(String id);

    /**
     * 批量删除modbus协议读取配置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubModbusConfigByIds(String[] ids);
}
