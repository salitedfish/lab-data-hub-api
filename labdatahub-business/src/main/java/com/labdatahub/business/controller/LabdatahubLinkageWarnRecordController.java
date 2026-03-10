package com.labdatahub.business.controller;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.labdatahub.common.utils.StringUtils;
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
import com.labdatahub.business.domain.LabdatahubLinkageWarnRecord;
import com.labdatahub.business.service.ILabdatahubLinkageWarnRecordService;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.core.page.TableDataInfo;

/**
 * 设备联动告警记录Controller
 * 
 * @author ruoyi
 * @date 2025-11-20
 */
@RestController
@RequestMapping("/business/linkageRecord")
public class LabdatahubLinkageWarnRecordController extends BaseController
{
    @Autowired
    private ILabdatahubLinkageWarnRecordService labdatahubLinkageWarnRecordService;

    /**
     * 查询设备联动告警记录列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubLinkageWarnRecord labdatahubLinkageWarnRecord)
    {
        QueryWrapper<LabdatahubLinkageWarnRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("status");
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubLinkageWarnRecord.getWarnLevel()),"warn_level",labdatahubLinkageWarnRecord.getWarnLevel());
        queryWrapper.like(StringUtils.isNotEmpty(labdatahubLinkageWarnRecord.getConfigName()),"config_name",labdatahubLinkageWarnRecord.getConfigName());
        queryWrapper.like(StringUtils.isNotEmpty(labdatahubLinkageWarnRecord.getTriggerSnList()),"trigger_sn_list",labdatahubLinkageWarnRecord.getTriggerSnList());
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubLinkageWarnRecord.getConfigId()),"config_id",labdatahubLinkageWarnRecord.getConfigId());
        Page<LabdatahubLinkageWarnRecord> page = new Page<LabdatahubLinkageWarnRecord>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubLinkageWarnRecord> pageList = labdatahubLinkageWarnRecordService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出设备联动告警记录列表
     */
    @Log(title = "设备联动告警记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubLinkageWarnRecord labdatahubLinkageWarnRecord)
    {
        List<LabdatahubLinkageWarnRecord> list = labdatahubLinkageWarnRecordService.selectLabdatahubLinkageWarnRecordList(labdatahubLinkageWarnRecord);
        ExcelUtil<LabdatahubLinkageWarnRecord> util = new ExcelUtil<LabdatahubLinkageWarnRecord>(LabdatahubLinkageWarnRecord.class);
        util.exportExcel(response, list, "设备联动告警记录数据");
    }

    /**
     * 获取设备联动告警记录详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubLinkageWarnRecordService.getById(id));
    }

    /**
     * 新增设备联动告警记录
     */
    @Log(title = "设备联动告警记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubLinkageWarnRecord labdatahubLinkageWarnRecord)
    {
        return toAjax(labdatahubLinkageWarnRecordService.save(labdatahubLinkageWarnRecord));
    }

    /**
     * 修改设备联动告警记录
     */
    @Log(title = "设备联动告警记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubLinkageWarnRecord labdatahubLinkageWarnRecord)
    {
        return toAjax(labdatahubLinkageWarnRecordService.updateById(labdatahubLinkageWarnRecord));
    }

    /**
     * 删除设备联动告警记录
     */
    @Log(title = "设备联动告警记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(labdatahubLinkageWarnRecordService.removeBatchByIds(Arrays.asList(ids)));
    }

    /**
     * 标记处理状态
     */
    @Log(title = "标记处理状态", businessType = BusinessType.UPDATE)
    @PutMapping("/deal/{id}")
    public AjaxResult deal(@PathVariable String id)
    {
        labdatahubLinkageWarnRecordService.update(new LambdaUpdateWrapper<LabdatahubLinkageWarnRecord>()
                .eq(LabdatahubLinkageWarnRecord::getId,id)
                .set(LabdatahubLinkageWarnRecord::getStatus,"1")
        );
        return AjaxResult.success();
    }
}
