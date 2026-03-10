package com.labdatahub.business.service.impl;

import java.util.*;

import com.labdatahub.business.warn.link.*;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.utils.DateUtils;
import com.labdatahub.common.utils.StringUtils;
import org.apache.commons.collections.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubWarnLinkageMapper;
import com.labdatahub.business.domain.LabdatahubWarnLinkage;
import com.labdatahub.business.service.ILabdatahubWarnLinkageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

/**
 * 设备联动告警Service业务层处理
 *
 * @author ruoyi
 * @date 2025-11-03
 */
@Service
public class LabdatahubWarnLinkageServiceImpl extends ServiceImpl<LabdatahubWarnLinkageMapper, LabdatahubWarnLinkage> implements ILabdatahubWarnLinkageService
{
    @Autowired
    private LabdatahubWarnLinkageMapper labdatahubWarnLinkageMapper;

    /**
     * 查询设备联动告警
     *
     * @param id 设备联动告警主键
     * @return 设备联动告警
     */
    @Override
    public LabdatahubWarnLinkage selectLabdatahubWarnLinkageById(String id)
    {
        return labdatahubWarnLinkageMapper.selectLabdatahubWarnLinkageById(id);
    }

    /**
     * 查询设备联动告警列表
     *
     * @param labdatahubWarnLinkage 设备联动告警
     * @return 设备联动告警
     */
    @Override
    public List<LabdatahubWarnLinkage> selectLabdatahubWarnLinkageList(LabdatahubWarnLinkage labdatahubWarnLinkage)
    {
        return labdatahubWarnLinkageMapper.selectLabdatahubWarnLinkageList(labdatahubWarnLinkage);
    }

    /**
     * 新增设备联动告警
     *
     * @param labdatahubWarnLinkage 设备联动告警
     * @return 结果
     */
    @Override
    public int insertLabdatahubWarnLinkage(LabdatahubWarnLinkage labdatahubWarnLinkage)
    {
        labdatahubWarnLinkage.setCreateTime(DateUtils.getNowDate());
        return labdatahubWarnLinkageMapper.insertLabdatahubWarnLinkage(labdatahubWarnLinkage);
    }

    /**
     * 修改设备联动告警
     *
     * @param labdatahubWarnLinkage 设备联动告警
     * @return 结果
     */
    @Override
    public int updateLabdatahubWarnLinkage(LabdatahubWarnLinkage labdatahubWarnLinkage)
    {
        return labdatahubWarnLinkageMapper.updateLabdatahubWarnLinkage(labdatahubWarnLinkage);
    }

    /**
     * 批量删除设备联动告警
     *
     * @param ids 需要删除的设备联动告警主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubWarnLinkageByIds(String[] ids)
    {
        return labdatahubWarnLinkageMapper.deleteLabdatahubWarnLinkageByIds(ids);
    }

    /**
     * 删除设备联动告警信息
     *
     * @param id 设备联动告警主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubWarnLinkageById(String id)
    {
        return labdatahubWarnLinkageMapper.deleteLabdatahubWarnLinkageById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult configLinkage(LabdatahubWarnLinkage labdatahubWarnLinkage) throws Exception {
        if(StringUtils.isEmpty(labdatahubWarnLinkage.getId())||StringUtils.isEmpty(labdatahubWarnLinkage.getRuleJson())){
            return AjaxResult.error("ID或配置为空");
        }
        RuleConfigValidator.ValidationResult validationResult = RuleConfigValidator.validateRuleConfig(labdatahubWarnLinkage.getRuleJson());
        if(!validationResult.isValid()){
            return AjaxResult.error(String.join(",",validationResult.getErrors()));
        }
        WarnLinkRule warnLinkRule = RuleConfigConverter.convertToRule(labdatahubWarnLinkage,labdatahubWarnLinkage.getName());
        List<CommandNode> commandTree = CommandTreeParser.parseCommandTree(labdatahubWarnLinkage.getRuleJson());
        warnLinkRule.setActions(commandTree);
        labdatahubWarnLinkage.setExecuteSnList(String.join(",",warnLinkRule.getExecuteDeviceSns()));
        labdatahubWarnLinkage.setExecuteNameList(String.join(",",warnLinkRule.getExecuteDeviceNames()));
        labdatahubWarnLinkage.setTriggerSnList(String.join(",",new HashSet<>(warnLinkRule.getInvolvedDeviceSns())));
        labdatahubWarnLinkage.setTriggerNameList(String.join(",",new HashSet<>(warnLinkRule.getInvolvedDeviceNames())));
        labdatahubWarnLinkageMapper.updateById(labdatahubWarnLinkage);
        if("1".equals(labdatahubWarnLinkage.getIsEnable())){
            control(labdatahubWarnLinkage.getId(),"1");
        }else {
            control(labdatahubWarnLinkage.getId(),"0");
        }
        return AjaxResult.success();
    }

    @Override
    public AjaxResult control(String id,String isEnable) throws Exception {
        LabdatahubWarnLinkage labdatahubWarnLinkage = labdatahubWarnLinkageMapper.selectById(id);
        if("1".equals(isEnable)){
            WarnLinkUtils.removeAllCacheByRuleId(id);
            WarnLinkRule warnLinkRule = RuleConfigConverter.convertToRule(labdatahubWarnLinkage,labdatahubWarnLinkage.getName());
            List<CommandNode> commandTree = CommandTreeParser.parseCommandTree(labdatahubWarnLinkage.getRuleJson());
            warnLinkRule.setActions(commandTree);
            WarnLinkUtils.addRuleToCache(labdatahubWarnLinkage.getId(),warnLinkRule);
            List<ConditionNode> conditionNodes = RuleConfigConverter.getAllConditionNodes(warnLinkRule);
            Set<String> deviceSnList = new HashSet<>();
            conditionNodes.forEach(node->{
                if("deviceProperty".equals(node.getAttributeType())){
                    WarnLinkUtils.addPropertyRule(node.getDeviceSn(),labdatahubWarnLinkage.getId());
                }
                if("changeStatus".equals(node.getAttributeType())){
                    WarnLinkUtils.addChangeStatusRule(node.getDeviceSn(),labdatahubWarnLinkage.getId());
                }
                if("currentStatus".equals(node.getAttributeType())){
                    WarnLinkUtils.addCurrentStatusRule(node.getDeviceSn(),labdatahubWarnLinkage.getId());
                }
                WarnLinkUtils.addDeviceRuleRelation(node.getDeviceSn(),labdatahubWarnLinkage.getId());
                deviceSnList.add(node.getDeviceSn());
            });
            WarnLinkUtils.addDeviceListToCache(labdatahubWarnLinkage.getId(),new ArrayList<>(deviceSnList));
            labdatahubWarnLinkage.setIsEnable("1");
            labdatahubWarnLinkageMapper.updateById(labdatahubWarnLinkage);
        }else {
            WarnLinkUtils.removeAllCacheByRuleId(id);
            labdatahubWarnLinkage.setIsEnable("0");
            labdatahubWarnLinkageMapper.updateById(labdatahubWarnLinkage);
        }
        return AjaxResult.success();
    }
}
