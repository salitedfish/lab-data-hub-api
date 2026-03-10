package com.labdatahub.business.service.impl;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.business.warn.WarnRule;
import com.labdatahub.common.utils.DateUtils;
import com.labdatahub.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubWarnConfigMapper;
import com.labdatahub.business.domain.LabdatahubWarnConfig;
import com.labdatahub.business.service.ILabdatahubWarnConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 告警配置Service业务层处理
 *
 * @author labdatahub
 * @date 2025-10-05
 */
@Service
public class LabdatahubWarnConfigServiceImpl extends ServiceImpl<LabdatahubWarnConfigMapper, LabdatahubWarnConfig> implements ILabdatahubWarnConfigService
{
    @Autowired
    private LabdatahubWarnConfigMapper labdatahubWarnConfigMapper;

    /**
     * 查询告警配置
     *
     * @param id 告警配置主键
     * @return 告警配置
     */
    @Override
    public LabdatahubWarnConfig selectLabdatahubWarnConfigById(String id)
    {
        return labdatahubWarnConfigMapper.selectLabdatahubWarnConfigById(id);
    }

    /**
     * 查询告警配置列表
     *
     * @param labdatahubWarnConfig 告警配置
     * @return 告警配置
     */
    @Override
    public List<LabdatahubWarnConfig> selectLabdatahubWarnConfigList(LabdatahubWarnConfig labdatahubWarnConfig)
    {
        return labdatahubWarnConfigMapper.selectLabdatahubWarnConfigList(labdatahubWarnConfig);
    }

    /**
     * 新增告警配置
     *
     * @param labdatahubWarnConfig 告警配置
     * @return 结果
     */
    @Override
    public int insertLabdatahubWarnConfig(LabdatahubWarnConfig labdatahubWarnConfig)
    {
        labdatahubWarnConfig.setCreateTime(DateUtils.getNowDate());
        return labdatahubWarnConfigMapper.insertLabdatahubWarnConfig(labdatahubWarnConfig);
    }

    /**
     * 修改告警配置
     *
     * @param labdatahubWarnConfig 告警配置
     * @return 结果
     */
    @Override
    public int updateLabdatahubWarnConfig(LabdatahubWarnConfig labdatahubWarnConfig)
    {
                labdatahubWarnConfig.setUpdateTime(DateUtils.getNowDate());
        return labdatahubWarnConfigMapper.updateLabdatahubWarnConfig(labdatahubWarnConfig);
    }

    /**
     * 批量删除告警配置
     *
     * @param ids 需要删除的告警配置主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubWarnConfigByIds(String[] ids)
    {
        return labdatahubWarnConfigMapper.deleteLabdatahubWarnConfigByIds(ids);
    }

    /**
     * 删除告警配置信息
     *
     * @param id 告警配置主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubWarnConfigById(String id)
    {
        return labdatahubWarnConfigMapper.deleteLabdatahubWarnConfigById(id);
    }
    /**
     * 从产品同步告警配置到指定设备
     * @param productSn
     * @return
     */
    @Override
    public void syncWarnConfigToDevice(String productSn, String deviceSn) {
        List<LabdatahubWarnConfig> configList = labdatahubWarnConfigMapper.selectList(new LambdaQueryWrapper<LabdatahubWarnConfig>()
                .eq(LabdatahubWarnConfig::getBelongSn,productSn));
        configList.forEach(config->{
            config.setId(IdWorker.getIdStr());
            config.setBelongSn(deviceSn);
            config.setBelongType("1");
            config.setCreateTime(new Date());
            try {
                config.setCreateBy(SecurityUtils.getUsername());
            }catch (Exception e){
                config.setCreateBy("自注册");
            }
            WarnRule rule =  JSONObject.parseObject(config.getRuleJson(),WarnRule.class);
            rule.setBelongSn(config.getBelongSn());
            rule.setBelongType(config.getBelongType());
            rule.setId(config.getId());
            config.setRuleJson(JSONObject.toJSONString(rule));
            labdatahubWarnConfigMapper.insert(config);
        });
        CacheUtils.updateDeviceWarnRule(deviceSn);
    }
}
