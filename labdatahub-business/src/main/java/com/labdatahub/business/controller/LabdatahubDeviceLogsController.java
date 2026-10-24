package com.labdatahub.business.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.labdatahub.business.domain.LabdatahubDeviceLogs;
import com.labdatahub.business.service.ILabdatahubDeviceLogsService;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.core.page.TableDataInfo;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.common.exception.ServiceException;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.poi.ExcelUtil;

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

    /** 未传时间范围时默认回看的小时数：设备日志按天百万行级增长，不设兜底范围会走全表 */
    private static final long DEFAULT_LOOKBACK_HOURS = 24;

    /** 单次导出上限（条）：Excel 导出会整批读进内存，不设上限遇到百万行直接把堆打满 */
    private static final int MAX_EXPORT_ROWS = 50000;

    /** 时间参数格式，与前端 daterange 传参一致 */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 查询设备日志列表
     * @param propertyName 属性标识符（物模型 identifier），匹配 properties 字段 JSON 中的属性 key
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubDeviceLogs labdatahubDeviceLogs,String startTime,String endTime,String propertyName)
    {
        LambdaQueryWrapper<LabdatahubDeviceLogs> queryWrapper = buildQueryWrapper(labdatahubDeviceLogs, startTime, endTime, propertyName);
        queryWrapper.orderByDesc(LabdatahubDeviceLogs::getCreateTime);
        Page<LabdatahubDeviceLogs> page = new Page<LabdatahubDeviceLogs>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubDeviceLogs> pageList = labdatahubDeviceLogsService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出设备日志列表
     */
    @Log(title = "设备日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubDeviceLogs labdatahubDeviceLogs,String startTime,String endTime,String propertyName)
    {
        LambdaQueryWrapper<LabdatahubDeviceLogs> queryWrapper = buildQueryWrapper(labdatahubDeviceLogs, startTime, endTime, propertyName);
        queryWrapper.orderByDesc(LabdatahubDeviceLogs::getCreateTime);
        // 多取一条用于判断是否超限；超限直接拒绝，不写响应，避免导出半个文件
        queryWrapper.last("limit " + (MAX_EXPORT_ROWS + 1));
        List<LabdatahubDeviceLogs> list = labdatahubDeviceLogsService.list(queryWrapper);
        if (list.size() > MAX_EXPORT_ROWS) {
            throw new ServiceException("导出数据超过 " + MAX_EXPORT_ROWS + " 条，请缩小时间范围或增加筛选条件");
        }
        ExcelUtil<LabdatahubDeviceLogs> util = new ExcelUtil<LabdatahubDeviceLogs>(LabdatahubDeviceLogs.class);
        util.exportExcel(response, list, "设备日志数据");
    }

    /**
     * 组装设备日志查询条件
     *
     * <p>未传时间范围时默认取最近 {@link #DEFAULT_LOOKBACK_HOURS} 小时 —— 设备日志是超表，
     * 全表扫描在百万行级别就是数秒到数十秒，必须给兜底范围。
     */
    private LambdaQueryWrapper<LabdatahubDeviceLogs> buildQueryWrapper(LabdatahubDeviceLogs labdatahubDeviceLogs,
                                                                       String startTime, String endTime, String propertyName)
    {
        LambdaQueryWrapper<LabdatahubDeviceLogs> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubDeviceLogs.getLogType()),LabdatahubDeviceLogs::getLogType,labdatahubDeviceLogs.getLogType());
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubDeviceLogs.getDeviceSn()),LabdatahubDeviceLogs::getDeviceSn,labdatahubDeviceLogs.getDeviceSn());
        // 属性名称过滤：properties 字段存 DecodeMessage JSON（{"properties":{identifier:value},...}），
        // 用引号包裹的标识符匹配属性 key，避免误命中数值
        queryWrapper.like(StringUtils.isNotEmpty(propertyName), LabdatahubDeviceLogs::getProperties, "\"" + propertyName + "\"");
        // 处理时间范围查询
        // ⚠️ 必须写成显式 if：Java 会先把 LocalDateTime.parse(startTime) 求值再传进 ge，
        // 条件为 false 也照样解析，页面「重置」清空时间范围后 startTime 为 null → NPE
        if (StringUtils.isEmpty(startTime) && StringUtils.isEmpty(endTime)) {
            startTime = LocalDateTime.now().minusHours(DEFAULT_LOOKBACK_HOURS).format(TIME_FORMATTER);
        }
        if (StringUtils.isNotEmpty(startTime)) {
            queryWrapper.ge(LabdatahubDeviceLogs::getCreateTime, LocalDateTime.parse(startTime, TIME_FORMATTER));
        }
        if (StringUtils.isNotEmpty(endTime)) {
            queryWrapper.le(LabdatahubDeviceLogs::getCreateTime, LocalDateTime.parse(endTime, TIME_FORMATTER));
        }
        return queryWrapper;
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
