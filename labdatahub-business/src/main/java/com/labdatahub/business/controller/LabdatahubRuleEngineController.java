package com.labdatahub.business.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.labdatahub.business.engine.entity.RuleEngineConfig;
import com.labdatahub.business.engine.entity.RuleLine;
import com.labdatahub.business.engine.entity.RuleNode;
import com.labdatahub.business.engine.utils.RuleGroupExtractor;
import com.labdatahub.common.exception.CommonWarnException;
import com.labdatahub.common.utils.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.business.domain.LabdatahubRuleEngine;
import com.labdatahub.business.service.ILabdatahubRuleEngineService;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.core.page.TableDataInfo;

/**
 * 规则引擎配置Controller
 * 
 * @author labdatahub
 * @date 2025-10-20
 */
@RestController
@RequestMapping("/business/ruleEngine")
public class LabdatahubRuleEngineController extends BaseController
{
    @Autowired
    private ILabdatahubRuleEngineService labdatahubRuleEngineService;

    /**
     * 查询规则引擎配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubRuleEngine labdatahubRuleEngine)
    {
        QueryWrapper<LabdatahubRuleEngine> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        queryWrapper.like(StringUtils.isNotEmpty(labdatahubRuleEngine.getEngineName()),"engine_name",labdatahubRuleEngine.getEngineName());
        Page<LabdatahubRuleEngine> page = new Page<LabdatahubRuleEngine>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubRuleEngine> pageList = labdatahubRuleEngineService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出规则引擎配置列表
     */
    @Log(title = "规则引擎配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubRuleEngine labdatahubRuleEngine)
    {
        List<LabdatahubRuleEngine> list = labdatahubRuleEngineService.selectLabdatahubRuleEngineList(labdatahubRuleEngine);
        ExcelUtil<LabdatahubRuleEngine> util = new ExcelUtil<LabdatahubRuleEngine>(LabdatahubRuleEngine.class);
        util.exportExcel(response, list, "规则引擎配置数据");
    }

    /**
     * 获取规则引擎配置详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubRuleEngineService.getById(id));
    }

    /**
     * 新增规则引擎配置
     */
    @Log(title = "规则引擎配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubRuleEngine labdatahubRuleEngine)
    {
        labdatahubRuleEngine.setCreateTime(new Date());
        List<RuleLine> lineList = new ArrayList<>();
        List<RuleNode> nodeList = new ArrayList<>();
        RuleEngineConfig ruleEngineConfig = new RuleEngineConfig();
        ruleEngineConfig.setLineList(lineList);
        ruleEngineConfig.setNodeList(nodeList);
        labdatahubRuleEngine.setConfigJson(JSONObject.toJSONString(ruleEngineConfig));
        return toAjax(labdatahubRuleEngineService.save(labdatahubRuleEngine));
    }

    /**
     * 修改规则引擎配置
     */
    @Log(title = "规则引擎配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubRuleEngine labdatahubRuleEngine)
    {
        return toAjax(labdatahubRuleEngineService.updateById(labdatahubRuleEngine));
    }


    /**
     * 修改规则引擎配置
     */
    @Log(title = "规则引擎配置", businessType = BusinessType.UPDATE)
    @PutMapping("/configEngine")
    public AjaxResult configEngine(@RequestBody LabdatahubRuleEngine labdatahubRuleEngine)
    {
        labdatahubRuleEngineService.update(new LambdaUpdateWrapper<LabdatahubRuleEngine>()
                .set(LabdatahubRuleEngine::getConfigJson,labdatahubRuleEngine.getConfigJson())
                .eq(LabdatahubRuleEngine::getId,labdatahubRuleEngine.getId()));
        return AjaxResult.success();
    }

    /**
     * 删除规则引擎配置
     */
    @Log(title = "规则引擎配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        for (String id : ids) {
            labdatahubRuleEngineService.stopRuleEngine(id);
        }
        return toAjax(labdatahubRuleEngineService.removeBatchByIds(Arrays.asList(ids)));
    }

    /**
     * 开关规则引擎
     * @param isEnable 0-关 1-开
     */
    @PutMapping("/control")
    public AjaxResult controlEngine(@RequestParam String id,
                                       @RequestParam String isEnable) throws MqttException, IOException, CommonWarnException {
        if("0".equals(isEnable)){
            labdatahubRuleEngineService.stopRuleEngine(id);
        }else {
            labdatahubRuleEngineService.startRuleEngine(id);
        }
        return AjaxResult.success();
    }

}
