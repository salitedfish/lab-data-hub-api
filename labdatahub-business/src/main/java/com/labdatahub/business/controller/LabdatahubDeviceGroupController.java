package com.labdatahub.business.controller;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.labdatahub.business.domain.LabdatahubDeviceGroup;
import com.labdatahub.business.service.ILabdatahubDeviceGroupService;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.core.page.TableDataInfo;

/**
 * 设备分组Controller
 * 
 * @author ruoyi
 * @date 2026-01-18
 */
@RestController
@RequestMapping("/business/deviceGroup")
public class LabdatahubDeviceGroupController extends BaseController
{
    @Autowired
    private ILabdatahubDeviceGroupService labdatahubDeviceGroupService;

    /**
     * 查询设备分组列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubDeviceGroup labdatahubDeviceGroup)
    {
        QueryWrapper<LabdatahubDeviceGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubDeviceGroup.getGroupCode()),"group_code",labdatahubDeviceGroup.getGroupCode());
        queryWrapper.like(StringUtils.isNotEmpty(labdatahubDeviceGroup.getGroupName()),"group_name",labdatahubDeviceGroup.getGroupName());
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubDeviceGroup.getType()),"type",labdatahubDeviceGroup.getType());
        queryWrapper.orderByAsc("sort_num");
        queryWrapper.orderByDesc("create_time");
        Page<LabdatahubDeviceGroup> page = new Page<LabdatahubDeviceGroup>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubDeviceGroup> pageList = labdatahubDeviceGroupService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出设备分组列表
     */
    @Log(title = "设备分组", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubDeviceGroup labdatahubDeviceGroup)
    {
        List<LabdatahubDeviceGroup> list = labdatahubDeviceGroupService.selectLabdatahubDeviceGroupList(labdatahubDeviceGroup);
        ExcelUtil<LabdatahubDeviceGroup> util = new ExcelUtil<LabdatahubDeviceGroup>(LabdatahubDeviceGroup.class);
        util.exportExcel(response, list, "设备分组数据");
    }

    /**
     * 获取设备分组详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubDeviceGroupService.getById(id));
    }

    /**
     * 新增设备分组
     */
    @Log(title = "设备分组", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubDeviceGroup labdatahubDeviceGroup)
    {
        if(StringUtils.isEmpty(labdatahubDeviceGroup.getGroupCode())||StringUtils.isEmpty(labdatahubDeviceGroup.getGroupName())){
            return AjaxResult.warn("请填写完整信息");
        }
        LabdatahubDeviceGroup group = labdatahubDeviceGroupService.getOne(new LambdaQueryWrapper<LabdatahubDeviceGroup>()
                .eq(LabdatahubDeviceGroup::getGroupCode,labdatahubDeviceGroup.getGroupCode()),false);
        if(group!=null){
            return AjaxResult.warn("CODE已经存在，请更换");
        }
        return toAjax(labdatahubDeviceGroupService.save(labdatahubDeviceGroup));
    }

    /**
     * 修改设备分组
     */
    @Log(title = "设备分组", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubDeviceGroup labdatahubDeviceGroup)
    {
        LabdatahubDeviceGroup group = labdatahubDeviceGroupService.getOne(new LambdaQueryWrapper<LabdatahubDeviceGroup>()
                .eq(LabdatahubDeviceGroup::getGroupCode,labdatahubDeviceGroup.getGroupCode()),false);
        if(group!=null && !group.getId().equals(labdatahubDeviceGroup.getId())){
            return AjaxResult.warn("CODE已经存在，请更换");
        }
        return toAjax(labdatahubDeviceGroupService.updateById(labdatahubDeviceGroup));
    }

    /**
     * 删除设备分组
     */
    @Log(title = "设备分组", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(labdatahubDeviceGroupService.removeBatchByIds(Arrays.asList(ids)));
    }
}
