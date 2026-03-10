package com.labdatahub.business.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.labdatahub.business.domain.LabdatahubComponent;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.domain.LabdatahubProduct;
import com.labdatahub.business.down.DeviceDownUtils;
import com.labdatahub.business.service.ILabdatahubComponentService;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubProductService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.common.annotation.Anonymous;
import com.labdatahub.common.utils.StringUtils;
import io.netty.util.internal.StringUtil;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.BeanUtils;
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
import com.labdatahub.business.domain.LabdatahubFunction;
import com.labdatahub.business.service.ILabdatahubFunctionService;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.core.page.TableDataInfo;

/**
 * 设备指令下发Controller
 * 
 * @author ruoyi
 * @date 2025-10-24
 */
@RestController
@RequestMapping("/business/function")
public class LabdatahubFunctionController extends BaseController
{
    @Autowired
    private ILabdatahubFunctionService labdatahubFunctionService;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    @Autowired
    private ILabdatahubProductService labdatahubProductService;
    @Autowired
    private ILabdatahubComponentService labdatahubComponentService;
    /**
     * 查询设备指令下发列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubFunction labdatahubFunction)
    {
        QueryWrapper<LabdatahubFunction> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubFunction.getBelongSn()),"belong_sn",labdatahubFunction.getBelongSn());
        Page<LabdatahubFunction> page = new Page<LabdatahubFunction>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubFunction> pageList = labdatahubFunctionService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出设备指令下发列表
     */
    @Log(title = "设备指令下发", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubFunction labdatahubFunction)
    {
        List<LabdatahubFunction> list = labdatahubFunctionService.selectLabdatahubFunctionList(labdatahubFunction);
        ExcelUtil<LabdatahubFunction> util = new ExcelUtil<LabdatahubFunction>(LabdatahubFunction.class);
        util.exportExcel(response, list, "设备指令下发数据");
    }

    /**
     * 获取设备指令下发详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubFunctionService.getById(id));
    }

    /**
     * 新增设备指令下发
     */
    @Log(title = "设备指令下发", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubFunction labdatahubFunction)
    {
        if("1".equals(labdatahubFunction.getBelongType())){
            LabdatahubDevice device = labdatahubDeviceService.getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                    .eq(LabdatahubDevice::getDeviceSn,labdatahubFunction.getBelongSn()));
            labdatahubFunction.setProtocolId(device.getProtocolId());
        }else {
            LabdatahubProduct product = labdatahubProductService.getOne(new LambdaQueryWrapper<LabdatahubProduct>()
                    .eq(LabdatahubProduct::getProductSn,labdatahubFunction.getBelongSn()));
            labdatahubFunction.setProtocolId(product.getProtocolId());
        }
        labdatahubFunction.setCreateTime(new Date());
        labdatahubFunctionService.save(labdatahubFunction);
        CacheUtils.updateDeviceFunctionCache(labdatahubFunction.getBelongSn());
        return AjaxResult.success();
    }

    /**
     * 修改设备指令下发
     */
    @Log(title = "设备指令下发", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubFunction labdatahubFunction)
    {
        labdatahubFunctionService.updateById(labdatahubFunction);
        CacheUtils.updateDeviceFunctionCache(labdatahubFunction.getBelongSn());
        return AjaxResult.success();
    }

    /**
     * 删除设备指令下发
     */
    @Log(title = "设备指令下发", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        List<LabdatahubFunction> list = labdatahubFunctionService.list(new LambdaQueryWrapper<LabdatahubFunction>()
                .in(LabdatahubFunction::getId,Arrays.asList(ids)));
        List<String> belongSns = list.stream()
                .map(LabdatahubFunction::getBelongSn)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        belongSns.forEach(CacheUtils::updateDeviceFunctionCache);
        return toAjax(labdatahubFunctionService.removeBatchByIds(Arrays.asList(ids)));
    }

    /**
     * 同步产品数据到设备
     */
    @PostMapping("/syncProductToDevice")
    public AjaxResult syncProductToDevice(@RequestBody LabdatahubFunction labdatahubFunction){
        if(StringUtils.isEmpty(labdatahubFunction.getBelongSn())){
            return AjaxResult.warn("请选择产品");
        }
        List<LabdatahubFunction> functionList = labdatahubFunctionService.list(new LambdaQueryWrapper<LabdatahubFunction>()
                .eq(LabdatahubFunction::getBelongSn,labdatahubFunction.getBelongSn()));
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,labdatahubFunction.getBelongSn()));
        if(deviceList.size()==0){
            return AjaxResult.error("当前产品下没有设备");
        }
        labdatahubFunctionService.remove(new LambdaUpdateWrapper<LabdatahubFunction>()
                .in(LabdatahubFunction::getBelongSn, deviceList.stream().map(LabdatahubDevice::getDeviceSn).collect(Collectors.toList())));
        List<LabdatahubFunction> list = new ArrayList<>();
        deviceList.forEach(device->{
            functionList.forEach(function ->{
                LabdatahubFunction func = new LabdatahubFunction();
                BeanUtils.copyProperties(function,func);
                func.setBelongSn(device.getDeviceSn());
                func.setBelongType("1");
                func.setId(null);
                list.add(func);
            });
        });
        if(list.size()>0){
            labdatahubFunctionService.saveBatch(list);
        }
        return AjaxResult.success();
    }

    /**
     * 指令下发
     */
    @PostMapping("/downFunction")
    public AjaxResult downFunction(@RequestBody LabdatahubFunction labdatahubFunction) throws MqttException, InvocationTargetException, IllegalAccessException {
        LabdatahubDevice device = CacheUtils.getDeviceBySn(labdatahubFunction.getBelongSn());
        LabdatahubComponent component = CacheUtils.getComponentCache(device.getComponentId());
        if(component==null){
            return AjaxResult.error("未绑定或未启动网络组件，无法下发指令");
        }
        boolean isOk = DeviceDownUtils.functionDown(labdatahubFunction.getBelongSn(),
                labdatahubFunction.getFunctionCode(),
                labdatahubFunction.getFunctionParams(),
                device.getComponentId(),
                component.getNetType(),
                device.getProtocolId(),
                device.getCustomConfig(),
                "0");
        if(isOk){
            return AjaxResult.success("下发成功");
        }else {
            return AjaxResult.warn("下发失败");
        }
    }
}
