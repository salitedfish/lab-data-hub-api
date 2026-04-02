package com.labdatahub.business.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.labdatahub.business.domain.LabdatahubComponent;
import com.labdatahub.business.domain.LabdatahubProduct;
import com.labdatahub.business.domain.LabdatahubProtocol;
import com.labdatahub.business.service.ILabdatahubComponentService;
import com.labdatahub.business.service.ILabdatahubProductService;
import com.labdatahub.business.service.ILabdatahubProtocolService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.core.page.TableDataInfo;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.utils.uuid.IdUtils;
import com.labdatahub.component.db.DatabaseConfig;
import com.labdatahub.component.db.DatabaseConnectionManager;
import com.labdatahub.component.db.DatabaseReader;
import com.labdatahub.component.utils.PortChecker;

/**
 * 网络组件Controller
 *
 * @author labdatahub
 * @date 2025-09-18
 */
@RestController
@RequestMapping("/business/component")
public class LabdatahubComponentController extends BaseController
{
    @Autowired
    private ILabdatahubComponentService labdatahubComponentService;
    @Autowired
    private ILabdatahubProtocolService labdatahubProtocolService;
    @Autowired
    private ILabdatahubProductService labdatahubProductService;
    /**
     * 查询网络组件列表
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('business:component:list')")
    public TableDataInfo list(LabdatahubComponent labdatahubComponent)
    {
    	LambdaQueryWrapper<LabdatahubComponent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(LabdatahubComponent::getCreateTime);
        Page<LabdatahubComponent> page = new Page<LabdatahubComponent>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubComponent> pageList = labdatahubComponentService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出网络组件列表
     */
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubComponent labdatahubComponent)
    {
        List<LabdatahubComponent> list = labdatahubComponentService.selectLabdatahubComponentList(labdatahubComponent);
        ExcelUtil<LabdatahubComponent> util = new ExcelUtil<LabdatahubComponent>(LabdatahubComponent.class);
        util.exportExcel(response, list, "网络组件数据");
    }

    /**
     * 获取网络组件详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubComponentService.getById(id));
    }

    /**
     * 新增网络组件
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult add(@RequestBody LabdatahubComponent labdatahubComponent) throws Exception {
    	labdatahubComponent.setCreateTime(new Date());
        labdatahubComponentService.save(labdatahubComponent);
        CacheUtils.setComponentCache(labdatahubComponent.getId(),labdatahubComponent);
        if("1".equals(labdatahubComponent.getStatus())) {
            boolean isOk = labdatahubComponentService.openComponent(labdatahubComponent.getId());
            if (!isOk) {
                return AjaxResult.error("开启失败，请检查参数是否错误，端口是否占用");
            }
            if (StringUtils.isNotEmpty(labdatahubComponent.getProtocolId())) {
                bindProtocol(labdatahubComponent.getId(), labdatahubComponent.getProtocolId());
            }
        }
        return success();
    }

    /**
     * 修改网络组件
     */
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult edit(@RequestBody LabdatahubComponent labdatahubComponent) throws Exception {
        labdatahubComponentService.updateById(labdatahubComponent);
        if("1".equals(labdatahubComponent.getStatus())) {
            labdatahubComponentService.closeComponent(labdatahubComponent.getId());
            boolean isOk = labdatahubComponentService.openComponent(labdatahubComponent.getId());
            if (!isOk) {
                return AjaxResult.error("修改失败，请检查参数是否错误，端口是否占用");
            }
            if(StringUtils.isNotEmpty(labdatahubComponent.getProtocolId())){
                bindProtocol(labdatahubComponent.getId(),labdatahubComponent.getProtocolId());
            }
        }else {
            labdatahubComponentService.closeComponent(labdatahubComponent.getId());
        }
        return AjaxResult.success();
    }

    /**
     * 删除网络组件
     */
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        //先检查是否已经关闭
        List<String> idList = Arrays.asList(ids);
        long openCount = labdatahubComponentService.count(new LambdaQueryWrapper<LabdatahubComponent>()
                .in(LabdatahubComponent::getId,idList)
                .eq(LabdatahubComponent::getStatus,"1"));
        if(openCount>0){
            return AjaxResult.error("请先停用网络组件再删除");
        }
        for (int i = 0; i < idList.size(); i++) {
            long count = labdatahubProductService.count(new LambdaQueryWrapper<LabdatahubProduct>()
                    .eq(LabdatahubProduct::getComponentId,idList.get(i)));
            if(count>0){
                return AjaxResult.error("网络组件已经被使用");
            }
        }
        labdatahubComponentService.removeBatchByIds(idList);
        idList.forEach(CacheUtils::removeComponentCache);
        return AjaxResult.success("删除成功");
    }

    /**
     * 开关网络组件
     * @param status 0-关 1-开
     */
    @PutMapping("/control")
    public AjaxResult controlComponent(@RequestParam String id,
                                       @RequestParam String status) throws Exception {
        if("0".equals(status)){
            labdatahubComponentService.closeComponent(id);
        }else {
            labdatahubComponentService.openComponent(id);
        }
        return AjaxResult.success();
    }

    /**
     * 绑定协议
     * @param componentId 组件id
     * @param protocolId 协议id
     */
    @PutMapping("/bindProtocol")
    public AjaxResult bindProtocol(String componentId,String protocolId){
        if(StringUtils.isNotEmpty(componentId)&&StringUtils.isNotEmpty(protocolId)){
            LabdatahubComponent component = labdatahubComponentService.getById(componentId);
            if(component==null){
                return AjaxResult.error("网络组件不存在");
            }
            labdatahubComponentService.update(new LambdaUpdateWrapper<LabdatahubComponent>()
                    .eq(LabdatahubComponent::getId,componentId)
                    .set(LabdatahubComponent::getProtocolId,protocolId));
            component.setProtocolId(protocolId);
            CacheUtils.setComponentCache(component.getId(),component);
            labdatahubProtocolService.update(new LambdaUpdateWrapper<LabdatahubProtocol>()
                    .eq(LabdatahubProtocol::getId,protocolId)
                    .set(LabdatahubProtocol::getComponentId,componentId)
                    .set(LabdatahubProtocol::getComponentName,component.getName()));
            return AjaxResult.success("绑定成功");
        }else {
            return AjaxResult.error("请选择正确的网络组件和协议");
        }
    }

    /**
     * 检查端口占用
     */
    @PostMapping("/checkPort")
    public AjaxResult checkPort(@RequestParam Integer port){
        if(PortChecker.isLocalPortAvailable(port)){
            return AjaxResult.success("端口处于空闲状态");
        }else {
            return AjaxResult.error("端口已被占用，请选择其他端口");
        }
    }
    
    /**
     * 获取表名
     */
    @PostMapping("/listAllTables")
    public AjaxResult listAllTables(@RequestBody String otherConfig) throws Exception {
    	DatabaseConfig config = JSONObject.parseObject(otherConfig).toJavaObject(DatabaseConfig.class);
    	String tempId = IdUtils.simpleUUID();
    	boolean isOk = DatabaseConnectionManager.addConnection(tempId, config);
        if(!isOk){
            return AjaxResult.error("数据库连接失败");
        }
        List<String> tables = DatabaseReader.getAllTables(tempId);
        DatabaseConnectionManager.closeConnection(tempId);
        return AjaxResult.success(tables);
    }

}
