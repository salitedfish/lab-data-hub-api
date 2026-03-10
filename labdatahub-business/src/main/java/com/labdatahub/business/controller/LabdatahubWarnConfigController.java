package com.labdatahub.business.controller;

import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.domain.LabdatahubProduct;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.business.warn.WarnRule;
import com.labdatahub.common.utils.SecurityUtils;
import com.labdatahub.common.utils.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.business.domain.LabdatahubWarnConfig;
import com.labdatahub.business.service.ILabdatahubWarnConfigService;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.core.page.TableDataInfo;

/**
 * 告警配置Controller
 * 
 * @author labdatahub
 * @date 2025-10-05
 */
@RestController
@RequestMapping("/business/warnConfig")
public class LabdatahubWarnConfigController extends BaseController
{
    @Autowired
    private ILabdatahubWarnConfigService labdatahubWarnConfigService;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    /**
     * 查询告警配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubWarnConfig labdatahubWarnConfig)
    {
        QueryWrapper<LabdatahubWarnConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("create_time");
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubWarnConfig.getBelongSn()),"belong_sn",labdatahubWarnConfig.getBelongSn());
        Page<LabdatahubWarnConfig> page = new Page<LabdatahubWarnConfig>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubWarnConfig> pageList = labdatahubWarnConfigService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出告警配置列表
     */
    @Log(title = "告警配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubWarnConfig labdatahubWarnConfig)
    {
        List<LabdatahubWarnConfig> list = labdatahubWarnConfigService.selectLabdatahubWarnConfigList(labdatahubWarnConfig);
        ExcelUtil<LabdatahubWarnConfig> util = new ExcelUtil<LabdatahubWarnConfig>(LabdatahubWarnConfig.class);
        util.exportExcel(response, list, "告警配置数据");
    }

    /**
     * 获取告警配置详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubWarnConfigService.getById(id));
    }

    /**
     * 新增告警配置
     */
    @Log(title = "告警配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WarnRule warnRule)
    {
        warnRule.setId(IdWorker.getIdStr());
        LabdatahubWarnConfig labdatahubWarnConfig = new LabdatahubWarnConfig();
        labdatahubWarnConfig.setCreateTime(new Date());
        labdatahubWarnConfig.setBelongSn(warnRule.getBelongSn());
        labdatahubWarnConfig.setBelongType(warnRule.getBelongType());
        labdatahubWarnConfig.setWarnLevel(warnRule.getLevel());
        labdatahubWarnConfig.setWarnMessage(warnRule.getMessage());
        labdatahubWarnConfig.setCreateBy(SecurityUtils.getUsername());
        labdatahubWarnConfig.setIsEnable(warnRule.getEnable()?"1":"0");
        labdatahubWarnConfig.setExecuteAction(JSONArray.toJSONString(warnRule.getActions()));
        labdatahubWarnConfig.setName(warnRule.getName());
        labdatahubWarnConfig.setRuleJson(JSONObject.toJSONString(warnRule));
        labdatahubWarnConfig.setId(warnRule.getId());
        labdatahubWarnConfigService.save(labdatahubWarnConfig);
        CacheUtils.updateDeviceWarnRule(labdatahubWarnConfig.getBelongSn());
        return AjaxResult.success();
    }

    /**
     * 修改告警配置
     */
    @Log(title = "告警配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WarnRule warnRule)
    {
        LabdatahubWarnConfig labdatahubWarnConfig = new LabdatahubWarnConfig();
        labdatahubWarnConfig.setBelongSn(warnRule.getBelongSn());
        labdatahubWarnConfig.setBelongType(warnRule.getBelongType());
        labdatahubWarnConfig.setWarnMessage(warnRule.getMessage());
        labdatahubWarnConfig.setWarnLevel(warnRule.getLevel());
        labdatahubWarnConfig.setIsEnable(warnRule.getEnable()?"1":"0");
        labdatahubWarnConfig.setExecuteAction(JSONArray.toJSONString(warnRule.getActions()));
        labdatahubWarnConfig.setName(warnRule.getName());
        labdatahubWarnConfig.setRuleJson(JSONObject.toJSONString(warnRule));
        labdatahubWarnConfig.setId(warnRule.getId());
        labdatahubWarnConfigService.updateById(labdatahubWarnConfig);
        CacheUtils.updateDeviceWarnRule(labdatahubWarnConfig.getBelongSn());
        return AjaxResult.success();
    }

    /**
     * 删除告警配置
     */
    @Log(title = "告警配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(labdatahubWarnConfigService.deleteLabdatahubWarnConfigByIds(ids));
    }

    /**
     * 修改告警配置
     */
    @Log(title = "告警配置", businessType = BusinessType.UPDATE)
    @PutMapping("/toggleRuleStatus")
    public AjaxResult toggleRuleStatus(@RequestBody WarnRule warnRule)
    {
        LabdatahubWarnConfig old = labdatahubWarnConfigService.getById(warnRule.getId());
        WarnRule oldRule = JSONObject.parseObject(old.getRuleJson(),WarnRule.class);
        oldRule.setEnable(warnRule.getEnable());
        LabdatahubWarnConfig labdatahubWarnConfig = new LabdatahubWarnConfig();
        labdatahubWarnConfig.setId(warnRule.getId());
        labdatahubWarnConfig.setIsEnable(warnRule.getEnable()?"1":"0");
        labdatahubWarnConfig.setRuleJson(JSONObject.toJSONString(oldRule));
        labdatahubWarnConfigService.updateById(labdatahubWarnConfig);
        CacheUtils.updateDeviceWarnRule(old.getBelongSn());
        return AjaxResult.success();
    }

    /**
     * 同步到所有设备
     */
    @PostMapping("/syncWarnConfigToDevice")
    public AjaxResult syncWarnConfigToDevice(@RequestParam String productSn){
        List<LabdatahubWarnConfig> warnConfigList = labdatahubWarnConfigService.list(new LambdaQueryWrapper<LabdatahubWarnConfig>()
                .eq(LabdatahubWarnConfig::getBelongSn,productSn));
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        deviceList.forEach(device->{
            //删除设备所有规则
            labdatahubWarnConfigService.remove(new LambdaUpdateWrapper<LabdatahubWarnConfig>()
                    .eq(LabdatahubWarnConfig::getBelongSn,device.getDeviceSn())
                    .eq(LabdatahubWarnConfig::getBelongType,"1"));
            //添加新的规则
            warnConfigList.forEach(config->{
                config.setId(IdWorker.getIdStr());
                config.setBelongSn(device.getDeviceSn());
                config.setBelongType("1");
                config.setCreateTime(new Date());
                config.setCreateBy(SecurityUtils.getUsername());
                WarnRule rule =  JSONObject.parseObject(config.getRuleJson(),WarnRule.class);
                rule.setBelongSn(config.getBelongSn());
                rule.setBelongType(config.getBelongType());
                rule.setId(config.getId());
                config.setRuleJson(JSONObject.toJSONString(rule));
            });
            if(warnConfigList.size()>0){
                labdatahubWarnConfigService.saveBatch(warnConfigList);
            }
            CacheUtils.updateDeviceWarnRule(device.getDeviceSn());
        });
        return AjaxResult.success();
    }
}
