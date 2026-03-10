package com.labdatahub.business.service;

import java.util.List;
import com.labdatahub.business.domain.LabdatahubRuleEngine;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 规则引擎配置Service接口
 *
 * @author labdatahub
 * @date 2025-10-20
 */
public interface ILabdatahubRuleEngineService extends IService<LabdatahubRuleEngine>
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
     * 批量删除规则引擎配置
     *
     * @param ids 需要删除的规则引擎配置主键集合
     * @return 结果
     */
    public int deleteLabdatahubRuleEngineByIds(String[] ids);

    /**
     * 删除规则引擎配置信息
     *
     * @param id 规则引擎配置主键
     * @return 结果
     */
    public int deleteLabdatahubRuleEngineById(String id);

    /**
     * 启动规则引擎
     */
    public boolean startRuleEngine(String id);

    /**
     * 启动规则引擎
     */
    public boolean stopRuleEngine(String id);
}
