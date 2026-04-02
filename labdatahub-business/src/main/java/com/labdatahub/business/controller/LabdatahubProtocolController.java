package com.labdatahub.business.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.labdatahub.business.domain.LabdatahubComponent;
import com.labdatahub.business.domain.LabdatahubProtocol;
import com.labdatahub.business.service.ILabdatahubComponentService;
import com.labdatahub.business.service.ILabdatahubProtocolService;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.core.page.TableDataInfo;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.poi.ExcelUtil;

/**
 * 协议管理Controller
 *
 * @author labdatahub
 * @date 2025-09-18
 */
@RestController
@RequestMapping("/business/protocol")
public class LabdatahubProtocolController extends BaseController
{
    @Autowired
    private ILabdatahubProtocolService labdatahubProtocolService;
    @Autowired
    private ILabdatahubComponentService labdatahubComponentService;
    /**
     * 查询协议管理列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubProtocol labdatahubProtocol)
    {
        QueryWrapper<LabdatahubProtocol> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("create_time");
        Page<LabdatahubProtocol> page = new Page<LabdatahubProtocol>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubProtocol> pageList = labdatahubProtocolService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出协议管理列表
     */
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubProtocol labdatahubProtocol)
    {
        List<LabdatahubProtocol> list = labdatahubProtocolService.selectLabdatahubProtocolList(labdatahubProtocol);
        ExcelUtil<LabdatahubProtocol> util = new ExcelUtil<LabdatahubProtocol>(LabdatahubProtocol.class);
        util.exportExcel(response, list, "协议管理数据");
    }

    /**
     * 获取协议管理详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubProtocolService.getById(id));
    }

    /**
     * 新增协议管理
     */
    @PostMapping
    public AjaxResult add(LabdatahubProtocol labdatahubProtocol, MultipartFile protocolFile) throws Exception {
        return AjaxResult.success(labdatahubProtocolService.insertLabdatahubProtocol(labdatahubProtocol,protocolFile));
    }

    /**
     * 修改协议管理
     */
    @PutMapping
    public AjaxResult edit(LabdatahubProtocol labdatahubProtocol, MultipartFile protocolFile) throws Exception {
        return toAjax(labdatahubProtocolService.updateLabdatahubProtocol(labdatahubProtocol,protocolFile));
    }

    /**
     * 删除协议管理
     */
	@DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable String id)
    {
        List<LabdatahubComponent> list = labdatahubComponentService.list(new LambdaQueryWrapper<LabdatahubComponent>()
                .eq(LabdatahubComponent::getProtocolId,id));
        if(list!=null&&!list.isEmpty()){
            List<String> names = list.stream().map(LabdatahubComponent::getName).collect(Collectors.toList());
            return AjaxResult.warn("删除失败，以下网络组件使用了该协议："+String.join(",",names));
        }
        return toAjax(labdatahubProtocolService.removeById(id));
    }
}
