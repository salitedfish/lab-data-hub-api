package com.labdatahub.business.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;

import com.alibaba.fastjson2.JSONObject;
import com.labdatahub.business.warn.link.*;
import com.labdatahub.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.business.domain.LabdatahubWarnLinkage;
import com.labdatahub.business.service.ILabdatahubWarnLinkageService;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.core.page.TableDataInfo;

/**
 * 设备联动告警Controller
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
@RestController
@RequestMapping("/business/linkage")
public class LabdatahubWarnLinkageController extends BaseController
{
    @Autowired
    private ILabdatahubWarnLinkageService labdatahubWarnLinkageService;

    /**
     * 查询设备联动告警列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubWarnLinkage labdatahubWarnLinkage)
    {
        QueryWrapper<LabdatahubWarnLinkage> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        queryWrapper.like(StringUtils.isNotEmpty(labdatahubWarnLinkage.getName()),"name",labdatahubWarnLinkage.getName());
        Page<LabdatahubWarnLinkage> page = new Page<LabdatahubWarnLinkage>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubWarnLinkage> pageList = labdatahubWarnLinkageService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出设备联动告警列表
     */
    @Log(title = "设备联动告警", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubWarnLinkage labdatahubWarnLinkage)
    {
        List<LabdatahubWarnLinkage> list = labdatahubWarnLinkageService.selectLabdatahubWarnLinkageList(labdatahubWarnLinkage);
        ExcelUtil<LabdatahubWarnLinkage> util = new ExcelUtil<LabdatahubWarnLinkage>(LabdatahubWarnLinkage.class);
        util.exportExcel(response, list, "设备联动告警数据");
    }

    /**
     * 获取设备联动告警详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubWarnLinkageService.getById(id));
    }

    /**
     * 新增设备联动告警
     */
    @Log(title = "设备联动告警", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubWarnLinkage labdatahubWarnLinkage)
    {
        labdatahubWarnLinkage.setCreateTime(new Date());
        return toAjax(labdatahubWarnLinkageService.save(labdatahubWarnLinkage));
    }

    /**
     * 修改设备联动告警
     */
    @Log(title = "设备联动告警", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubWarnLinkage labdatahubWarnLinkage)
    {
        return toAjax(labdatahubWarnLinkageService.updateById(labdatahubWarnLinkage));
    }

    /**
     * 删除设备联动告警
     */
    @Log(title = "设备联动告警", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(labdatahubWarnLinkageService.removeBatchByIds(Arrays.asList(ids)));
    }

    /**
     * 配置联动告警
     */
    @PutMapping("/configLinkage")
    public AjaxResult configLinkage(@RequestBody LabdatahubWarnLinkage labdatahubWarnLinkage) throws Exception {
        return labdatahubWarnLinkageService.configLinkage(labdatahubWarnLinkage);
    }

    /**
     * 开关控制
     */
    @PutMapping("/control")
    public AjaxResult control(@RequestParam String id, @RequestParam String isEnable) throws Exception {
        return labdatahubWarnLinkageService.control(id,isEnable);
    }
}
