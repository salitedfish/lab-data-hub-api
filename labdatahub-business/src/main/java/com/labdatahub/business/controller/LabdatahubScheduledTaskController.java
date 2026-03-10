package com.labdatahub.business.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletResponse;

import com.labdatahub.business.domain.LabdatahubWarnLinkage;
import com.labdatahub.common.exception.CommonWarnException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.business.domain.LabdatahubScheduledTask;
import com.labdatahub.business.service.ILabdatahubScheduledTaskService;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.core.page.TableDataInfo;

/**
 * 定时引擎配置Controller
 * 
 * @author ruoyi
 * @date 2025-12-02
 */
@RestController
@RequestMapping("/business/scheduledEngine")
public class LabdatahubScheduledTaskController extends BaseController
{
    @Autowired
    private ILabdatahubScheduledTaskService labdatahubScheduledTaskService;

    /**
     * 查询定时引擎配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubScheduledTask labdatahubScheduledTask)
    {
        QueryWrapper<LabdatahubScheduledTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        Page<LabdatahubScheduledTask> page = new Page<LabdatahubScheduledTask>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubScheduledTask> pageList = labdatahubScheduledTaskService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出定时引擎配置列表
     */
    @Log(title = "定时引擎配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubScheduledTask labdatahubScheduledTask)
    {
        List<LabdatahubScheduledTask> list = labdatahubScheduledTaskService.selectLabdatahubScheduledTaskList(labdatahubScheduledTask);
        ExcelUtil<LabdatahubScheduledTask> util = new ExcelUtil<LabdatahubScheduledTask>(LabdatahubScheduledTask.class);
        util.exportExcel(response, list, "定时引擎配置数据");
    }

    /**
     * 获取定时引擎配置详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubScheduledTaskService.getById(id));
    }

    /**
     * 新增定时引擎配置
     */
    @Log(title = "定时引擎配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubScheduledTask labdatahubScheduledTask)
    {
        labdatahubScheduledTask.setCreateTime(new Date());
        return toAjax(labdatahubScheduledTaskService.save(labdatahubScheduledTask));
    }

    /**
     * 修改定时引擎配置
     */
    @Log(title = "定时引擎配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubScheduledTask labdatahubScheduledTask)
    {
        return toAjax(labdatahubScheduledTaskService.updateById(labdatahubScheduledTask));
    }

    /**
     * 删除定时引擎配置
     */
    @Log(title = "定时引擎配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(labdatahubScheduledTaskService.removeBatchByIds(Arrays.asList(ids)));
    }

    /**
     * 定时配置
     */
    @PutMapping("/configTask")
    public AjaxResult configTask(@RequestBody LabdatahubScheduledTask labdatahubScheduledTask) throws Exception {
        return labdatahubScheduledTaskService.configTask(labdatahubScheduledTask);
    }

    /**
     * 开关控制
     */
    @PutMapping("/control")
    public AjaxResult control(@RequestParam String id, @RequestParam String isEnable) throws Exception {
        return labdatahubScheduledTaskService.control(id,isEnable);
    }
}
