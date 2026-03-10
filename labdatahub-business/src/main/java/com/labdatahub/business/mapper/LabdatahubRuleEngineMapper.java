package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubRuleEngine;

/**
 * 规则引擎配置Mapper接口
 *
 * @author labdatahub
 * @date 2025-10-20
 */
public interface LabdatahubRuleEngineMapper extends BaseMapper<LabdatahubRuleEngine>
{
    /**
     * 查询规则引擎配置
     *
     * @param id 规则引擎配置主键
     * @return 规则引擎配置
     */
    public LabdatahubRuleEngine selectLabdatahubRuleEngineById(String id);

    /**
     * 查询规则引擎配置列表
     *
     * @param labdatahubRuleEngine 规则引擎配置
     * @return 规则引擎配置集合
     */
    public List<LabdatahubRuleEngine> selectLabdatahubRuleEngineList(LabdatahubRuleEngine labdatahubRuleEngine);

    /**
     * 新增规则引擎配置
     *
     * @param labdatahubRuleEngine 规则引擎配置
     * @return 结果
     */
    public int insertLabdatahubRuleEngine(LabdatahubRuleEngine labdatahubRuleEngine);

    /**
     * 修改规则引擎配置
     *
     * @param labdatahubRuleEngine 规则引擎配置
     * @return 结果
     */
    public int updateLabdatahubRuleEngine(LabdatahubRuleEngine labdatahubRuleEngine);

    /**
     * 删除规则引擎配置
     *
     * @param id 规则引擎配置主键
     * @return 结果
     */
    public int deleteLabdatahubRuleEngineById(String id);

    /**
     * 批量删除规则引擎配置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubRuleEngineByIds(String[] ids);
}
