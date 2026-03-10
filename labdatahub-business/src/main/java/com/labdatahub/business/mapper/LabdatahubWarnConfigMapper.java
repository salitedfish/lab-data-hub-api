package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubWarnConfig;

/**
 * 告警配置Mapper接口
 *
 * @author labdatahub
 * @date 2025-10-05
 */
public interface LabdatahubWarnConfigMapper extends BaseMapper<LabdatahubWarnConfig>
{
    /**
     * 查询告警配置
     *
     * @param id 告警配置主键
     * @return 告警配置
     */
    public LabdatahubWarnConfig selectLabdatahubWarnConfigById(String id);

    /**
     * 查询告警配置列表
     *
     * @param labdatahubWarnConfig 告警配置
     * @return 告警配置集合
     */
    public List<LabdatahubWarnConfig> selectLabdatahubWarnConfigList(LabdatahubWarnConfig labdatahubWarnConfig);

    /**
     * 新增告警配置
     *
     * @param labdatahubWarnConfig 告警配置
     * @return 结果
     */
    public int insertLabdatahubWarnConfig(LabdatahubWarnConfig labdatahubWarnConfig);

    /**
     * 修改告警配置
     *
     * @param labdatahubWarnConfig 告警配置
     * @return 结果
     */
    public int updateLabdatahubWarnConfig(LabdatahubWarnConfig labdatahubWarnConfig);

    /**
     * 删除告警配置
     *
     * @param id 告警配置主键
     * @return 结果
     */
    public int deleteLabdatahubWarnConfigById(String id);

    /**
     * 批量删除告警配置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubWarnConfigByIds(String[] ids);
}
