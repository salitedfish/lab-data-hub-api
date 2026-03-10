package com.labdatahub.business.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

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
import com.labdatahub.business.domain.LabdatahubFunctionRecord;
import com.labdatahub.business.service.ILabdatahubFunctionRecordService;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.core.page.TableDataInfo;

/**
 * 指令下发记录Controller
 * 
 * @author ruoyi
 * @date 2025-10-29
 */
@RestController
@RequestMapping("/business/functionRecord")
public class LabdatahubFunctionRecordController extends BaseController
{
    @Autowired
    private ILabdatahubFunctionRecordService LabdatahubFunctionRecordService;

    /**
     * 查询指令下发记录列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubFunctionRecord labdatahubFunctionRecord)
    {
        QueryWrapper<LabdatahubFunctionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubFunctionRecord.getFunctionCode()),"function_code", labdatahubFunctionRecord.getFunctionCode());
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubFunctionRecord.getDeviceSn()),"device_sn", labdatahubFunctionRecord.getDeviceSn());
        Page<LabdatahubFunctionRecord> page = new Page<LabdatahubFunctionRecord>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubFunctionRecord> pageList = LabdatahubFunctionRecordService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出指令下发记录列表
     */
    @Log(title = "指令下发记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubFunctionRecord labdatahubFunctionRecord)
    {
        List<LabdatahubFunctionRecord> list = LabdatahubFunctionRecordService.selectLabdatahubFunctionRecordList(labdatahubFunctionRecord);
        ExcelUtil<LabdatahubFunctionRecord> util = new ExcelUtil<LabdatahubFunctionRecord>(LabdatahubFunctionRecord.class);
        util.exportExcel(response, list, "指令下发记录数据");
    }

    /**
     * 获取指令下发记录详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(LabdatahubFunctionRecordService.getById(id));
    }

    /**
     * 新增指令下发记录
     */
    @Log(title = "指令下发记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubFunctionRecord labdatahubFunctionRecord)
    {
        return toAjax(LabdatahubFunctionRecordService.save(labdatahubFunctionRecord));
    }

    /**
     * 修改指令下发记录
     */
    @Log(title = "指令下发记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubFunctionRecord labdatahubFunctionRecord)
    {
        return toAjax(LabdatahubFunctionRecordService.updateById(labdatahubFunctionRecord));
    }

    /**
     * 删除指令下发记录
     */
    @Log(title = "指令下发记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(LabdatahubFunctionRecordService.deleteLabdatahubFunctionRecordByIds(ids));
    }
}
