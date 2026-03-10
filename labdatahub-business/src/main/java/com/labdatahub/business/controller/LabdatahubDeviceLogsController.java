package com.labdatahub.business.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.labdatahub.common.utils.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.business.domain.LabdatahubDeviceLogs;
import com.labdatahub.business.service.ILabdatahubDeviceLogsService;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.core.page.TableDataInfo;

/**
 * 设备日志Controller
 * 
 * @author labdatahub
 * @date 2025-09-22
 */
@RestController
@RequestMapping("/business/deviceLogs")
public class LabdatahubDeviceLogsController extends BaseController
{
    @Autowired
    private ILabdatahubDeviceLogsService labdatahubDeviceLogsService;

    /**
     * 查询设备日志列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubDeviceLogs labdatahubDeviceLogs,String startTime,String endTime)
    {
        QueryWrapper<LabdatahubDeviceLogs> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubDeviceLogs.getLogType()),"log_type",labdatahubDeviceLogs.getLogType());
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubDeviceLogs.getDeviceSn()),"device_sn",labdatahubDeviceLogs.getDeviceSn());
//        queryWrapper.ge(StringUtils.isNotEmpty(startTime),"create_time",startTime);
//        queryWrapper.le(StringUtils.isNotEmpty(endTime),"create_time",endTime);
     // 处理时间范围查询
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (StringUtils.isNotEmpty(startTime)) {
            LocalDateTime start = LocalDateTime.parse(startTime, formatter);
            queryWrapper.ge("create_time", start);
        }
        if (StringUtils.isNotEmpty(endTime)) {
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);
            queryWrapper.le("create_time", end);
        }
        Page<LabdatahubDeviceLogs> page = new Page<LabdatahubDeviceLogs>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubDeviceLogs> pageList = labdatahubDeviceLogsService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出设备日志列表
     */
    @Log(title = "设备日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubDeviceLogs labdatahubDeviceLogs)
    {
        List<LabdatahubDeviceLogs> list = labdatahubDeviceLogsService.selectLabdatahubDeviceLogsList(labdatahubDeviceLogs);
        ExcelUtil<LabdatahubDeviceLogs> util = new ExcelUtil<LabdatahubDeviceLogs>(LabdatahubDeviceLogs.class);
        util.exportExcel(response, list, "设备日志数据");
    }

    /**
     * 获取设备日志详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(labdatahubDeviceLogsService.getById(id));
    }

    /**
     * 新增设备日志
     */
    @Log(title = "设备日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubDeviceLogs labdatahubDeviceLogs)
    {
        return toAjax(labdatahubDeviceLogsService.insertLabdatahubDeviceLogs(labdatahubDeviceLogs));
    }

    /**
     * 修改设备日志
     */
    @Log(title = "设备日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubDeviceLogs labdatahubDeviceLogs)
    {
        return toAjax(labdatahubDeviceLogsService.updateLabdatahubDeviceLogs(labdatahubDeviceLogs));
    }

    /**
     * 删除设备日志
     */
    @Log(title = "设备日志", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(labdatahubDeviceLogsService.deleteLabdatahubDeviceLogsByIds(ids));
    }
}
