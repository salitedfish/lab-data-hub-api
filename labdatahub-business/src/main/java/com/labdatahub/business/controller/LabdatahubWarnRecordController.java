package com.labdatahub.business.controller;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.labdatahub.business.domain.LabdatahubLinkageWarnRecord;
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
import com.labdatahub.business.domain.LabdatahubWarnRecord;
import com.labdatahub.business.service.ILabdatahubWarnRecordService;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.core.page.TableDataInfo;

/**
 * 告警记录Controller
 * 
 * @author labdatahub
 * @date 2025-10-05
 */
@RestController
@RequestMapping("/business/warnRecord")
public class LabdatahubWarnRecordController extends BaseController
{
    @Autowired
    private ILabdatahubWarnRecordService labdatahubWarnRecordService;

    /**
     * 查询告警记录列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubWarnRecord labdatahubWarnRecord)
    {
        QueryWrapper<LabdatahubWarnRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("status");
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubWarnRecord.getWarnLevel()),"warn_level",labdatahubWarnRecord.getWarnLevel());
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubWarnRecord.getBelongSn()),"belong_sn",labdatahubWarnRecord.getBelongSn());
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubWarnRecord.getStatus()),"status",labdatahubWarnRecord.getStatus());
        queryWrapper.like(StringUtils.isNotEmpty(labdatahubWarnRecord.getConfigName()),"config_name",labdatahubWarnRecord.getConfigName());
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubWarnRecord.getWarnType()),"warn_type",labdatahubWarnRecord.getWarnType());
        Page<LabdatahubWarnRecord> page = new Page<LabdatahubWarnRecord>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubWarnRecord> pageList = labdatahubWarnRecordService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出告警记录列表
     */
    @Log(title = "告警记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubWarnRecord labdatahubWarnRecord)
    {
        List<LabdatahubWarnRecord> list = labdatahubWarnRecordService.selectLabdatahubWarnRecordList(labdatahubWarnRecord);
        ExcelUtil<LabdatahubWarnRecord> util = new ExcelUtil<LabdatahubWarnRecord>(LabdatahubWarnRecord.class);
        util.exportExcel(response, list, "告警记录数据");
    }

    /**
     * 获取告警记录详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubWarnRecordService.getById(id));
    }

    /**
     * 新增告警记录
     */
    @Log(title = "告警记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubWarnRecord labdatahubWarnRecord)
    {
        return toAjax(labdatahubWarnRecordService.insertLabdatahubWarnRecord(labdatahubWarnRecord));
    }

    /**
     * 修改告警记录
     */
    @Log(title = "告警记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubWarnRecord labdatahubWarnRecord)
    {
        return toAjax(labdatahubWarnRecordService.updateLabdatahubWarnRecord(labdatahubWarnRecord));
    }

    /**
     * 删除告警记录
     */
    @Log(title = "告警记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(labdatahubWarnRecordService.deleteLabdatahubWarnRecordByIds(ids));
    }

    /**
     * 标记处理状态
     */
    @Log(title = "标记处理状态", businessType = BusinessType.UPDATE)
    @PutMapping("/deal/{id}")
    public AjaxResult deal(@PathVariable String id)
    {
        labdatahubWarnRecordService.update(new LambdaUpdateWrapper<LabdatahubWarnRecord>()
                .eq(LabdatahubWarnRecord::getId,id)
                .set(LabdatahubWarnRecord::getStatus,"1")
        );
        return AjaxResult.success();
    }
}
