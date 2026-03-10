package com.labdatahub.business.controller;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.labdatahub.business.domain.LabdatahubLinkageActionRecord;
import com.labdatahub.business.service.ILabdatahubLinkageActionRecordService;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.core.page.TableDataInfo;

/**
 * 设备联动告警动作执行记录Controller
 * 
 * @author ruoyi
 * @date 2025-11-20
 */
@RestController
@RequestMapping("/business/linkageAction")
public class LabdatahubLinkageActionRecordController extends BaseController
{
    @Autowired
    private ILabdatahubLinkageActionRecordService labdatahubLinkageActionRecordService;

    /**
     * 查询设备联动告警动作执行记录列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubLinkageActionRecord labdatahubLinkageActionRecord)
    {
        QueryWrapper<LabdatahubLinkageActionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        Page<LabdatahubLinkageActionRecord> page = new Page<LabdatahubLinkageActionRecord>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubLinkageActionRecord> pageList = labdatahubLinkageActionRecordService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出设备联动告警动作执行记录列表
     */
    @Log(title = "设备联动告警动作执行记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubLinkageActionRecord labdatahubLinkageActionRecord)
    {
        List<LabdatahubLinkageActionRecord> list = labdatahubLinkageActionRecordService.selectLabdatahubLinkageActionRecordList(labdatahubLinkageActionRecord);
        ExcelUtil<LabdatahubLinkageActionRecord> util = new ExcelUtil<LabdatahubLinkageActionRecord>(LabdatahubLinkageActionRecord.class);
        util.exportExcel(response, list, "设备联动告警动作执行记录数据");
    }

    /**
     * 获取设备联动告警动作执行记录详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubLinkageActionRecordService.getById(id));
    }

    /**
     * 新增设备联动告警动作执行记录
     */
    @Log(title = "设备联动告警动作执行记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubLinkageActionRecord labdatahubLinkageActionRecord)
    {
        return toAjax(labdatahubLinkageActionRecordService.save(labdatahubLinkageActionRecord));
    }

    /**
     * 修改设备联动告警动作执行记录
     */
    @Log(title = "设备联动告警动作执行记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubLinkageActionRecord labdatahubLinkageActionRecord)
    {
        return toAjax(labdatahubLinkageActionRecordService.updateById(labdatahubLinkageActionRecord));
    }

    /**
     * 删除设备联动告警动作执行记录
     */
    @Log(title = "设备联动告警动作执行记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(labdatahubLinkageActionRecordService.removeBatchByIds(Arrays.asList(ids)));
    }
}
