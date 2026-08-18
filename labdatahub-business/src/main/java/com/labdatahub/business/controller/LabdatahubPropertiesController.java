//由AI修改
package com.labdatahub.business.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.domain.LabdatahubDeviceLogs;
import com.labdatahub.business.domain.LabdatahubProduct;
import com.labdatahub.business.domain.dto.PropertyListDTO;
import com.labdatahub.business.service.ILabdatahubDeviceLogsService;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubProductService;
import com.labdatahub.business.utils.PropertyConverter;
import com.labdatahub.business.utils.ProtocolReadConfigRebuilder;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.component.message.DecodeMessage;
import com.labdatahub.component.message.MessageCache;
import com.labdatahub.component.message.PropertyNode;
import com.labdatahub.component.utils.PropertyToJson;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.business.domain.LabdatahubProperties;
import com.labdatahub.business.service.ILabdatahubPropertiesService;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.core.page.TableDataInfo;

/**
 * 物模型属性定义Controller
 * 
 * @author labdatahub
 * @date 2025-09-18
 */
@RestController
@RequestMapping("/business/properties")
public class LabdatahubPropertiesController extends BaseController
{
    @Autowired
    private ILabdatahubPropertiesService labdatahubPropertiesService;
    @Autowired
    private ILabdatahubProductService productService;
    @Autowired
    private ILabdatahubDeviceLogsService labdatahubDeviceLogsService;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;

    public static final List<String> KEYWORD = Arrays.asList("currentStatus","changeStatus","deviceProperty","device_online","device_offline");
    /**
     * 查询物模型属性定义列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubProperties labdatahubProperties)
    {
        QueryWrapper<LabdatahubProperties> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_num");
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubProperties.getBelongSn()),"belong_sn",labdatahubProperties.getBelongSn());
        Page<LabdatahubProperties> page = new Page<LabdatahubProperties>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubProperties> pageList = labdatahubPropertiesService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出物模型属性定义列表
     */
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubProperties labdatahubProperties)
    {
        List<LabdatahubProperties> list = labdatahubPropertiesService.selectLabdatahubPropertiesList(labdatahubProperties);
        ExcelUtil<LabdatahubProperties> util = new ExcelUtil<LabdatahubProperties>(LabdatahubProperties.class);
        util.exportExcel(response, list, "物模型属性定义数据");
    }

    /**
     * 获取物模型属性定义详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubPropertiesService.getById(id));
    }

    /**
     * 新增物模型属性定义
     */
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubProperties labdatahubProperties)
    {
        return toAjax(labdatahubPropertiesService.insertLabdatahubProperties(labdatahubProperties));
    }

    /**
     * 修改物模型属性定义
     */
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubProperties labdatahubProperties)
    {
        return toAjax(labdatahubPropertiesService.updateLabdatahubProperties(labdatahubProperties));
    }

    /**
     * 删除物模型属性定义
     */
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(labdatahubPropertiesService.deleteLabdatahubPropertiesByIds(ids));
    }

    /**
     * 批量新增属性
     */
    @PostMapping("/saveBatch")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult saveBatch(@RequestBody PropertyListDTO propertyListDTO){
        List<LabdatahubProperties> list = propertyListDTO.getPropertyList();
        if(list!=null&&list.size()>0){
            for (int i = 0; i < list.size(); i++) {
                if(KEYWORD.contains(list.get(i).getIdentifier())){
                    return AjaxResult.error("不能使用关键字作为属性值");
                }
            }
            labdatahubPropertiesService.remove(new LambdaUpdateWrapper<LabdatahubProperties>()
                    .eq(LabdatahubProperties::getBelongSn,propertyListDTO.getBelongSn()));
            list.forEach(property->{
                property.setBelongSn(propertyListDTO.getBelongSn());
                property.setBelongType(propertyListDTO.getBelongType());
                property.setFromType(propertyListDTO.getFromType());
                property.setParentId("0");
                property.setSortNum(0L);
            });
            if(list.size()>0){
                labdatahubPropertiesService.saveBatch(list);
            }
            LabdatahubDevice device = labdatahubDeviceService.getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                    .eq(LabdatahubDevice::getDeviceSn,propertyListDTO.getBelongSn()),false);
            if(device!=null){
                List<PropertyNode> nodeList = PropertyConverter.buildPropertyTree(list);
                PropertyToJson.PROPERTY_TREE.put(propertyListDTO.getBelongSn(),nodeList);
                // 物模型解析参数变更，重建该设备正在轮询的读取配置（dataType/字节序/缩放/偏移即时生效）
                ProtocolReadConfigRebuilder.rebuildDevice(propertyListDTO.getBelongSn());
            } else {
                // 产品物模型解析参数变更，重建该产品下所有设备的轮询读取配置
                ProtocolReadConfigRebuilder.rebuildByProduct(propertyListDTO.getBelongSn());
            }
            return AjaxResult.success("更新属性成功");
        }else {
            return AjaxResult.error("请至少添加一条属性");
        }
    }

    /**
     * 获取设备最新json数据
     */
    @GetMapping("/getDeviceLastData")
    public AjaxResult getDeviceLastData(@RequestParam String deviceSn){
        DecodeMessage decodeMessage = MessageCache.getDeviceLastData(deviceSn);
        if(decodeMessage==null){
            LabdatahubDeviceLogs log = labdatahubDeviceLogsService.getOne(new LambdaQueryWrapper<LabdatahubDeviceLogs>()
                    .eq(LabdatahubDeviceLogs::getDeviceSn,deviceSn)
                    .orderByDesc(LabdatahubDeviceLogs::getReportTime)
                    .eq(LabdatahubDeviceLogs::getLogType,"PROPERTY")
                    .last(" limit 1 "));
            if(log!=null){
                decodeMessage = JSONObject.parseObject(log.getProperties(),DecodeMessage.class);
                MessageCache.setDeviceLastData(deviceSn,decodeMessage);
            }else {
                decodeMessage = new DecodeMessage();
                MessageCache.setDeviceLastData(deviceSn,new DecodeMessage());
            }
        }
        return AjaxResult.success(decodeMessage);
    }

    /**
     * 获取设备/产品所有可配置属性
     */
    @GetMapping("/getPropertiesBySn")
    public AjaxResult getPropertiesBySn(@RequestParam String sn){
        List<LabdatahubProperties> labdatahubProperties = labdatahubPropertiesService.list(new LambdaQueryWrapper<LabdatahubProperties>()
                .eq(LabdatahubProperties::getBelongSn,sn));
        return AjaxResult.success(labdatahubProperties);
    }
}
