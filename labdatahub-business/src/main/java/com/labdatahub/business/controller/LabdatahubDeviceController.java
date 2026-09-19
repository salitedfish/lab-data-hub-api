package com.labdatahub.business.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.labdatahub.business.domain.*;
import com.labdatahub.business.event.DeviceHeartbeatManager;
import com.labdatahub.business.service.ILabdatahubFunctionService;
import com.labdatahub.business.service.IOpenApiPointValueService;
import com.labdatahub.business.service.ILabdatahubProductService;
import com.labdatahub.business.service.ILabdatahubWarnConfigService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.SecurityUtils;
import com.labdatahub.common.utils.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.core.page.TableDataInfo;

/**
 * 设备Controller
 * 
 * @author labdatahub
 * @date 2025-09-18
 */
@RestController
@RequestMapping("/business/device")
public class LabdatahubDeviceController extends BaseController
{
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    @Autowired
    private ILabdatahubProductService labdatahubProductService;
    @Autowired
    private ILabdatahubWarnConfigService labdatahubWarnConfigService;
    @Autowired
    private ILabdatahubFunctionService labdatahubFunctionService;
    @Autowired
    private IOpenApiPointValueService openApiPointValueService;
    /**
     * 查询设备列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubDevice labdatahubDevice)
    {
        QueryWrapper<LabdatahubDevice> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("create_time");
        queryWrapper.like(StringUtils.isNotEmpty(labdatahubDevice.getDeviceSn()),"device_sn",labdatahubDevice.getDeviceSn());
        queryWrapper.like(StringUtils.isNotEmpty(labdatahubDevice.getDeviceName()),"device_name",labdatahubDevice.getDeviceName());
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubDevice.getProductSn()),"product_sn",labdatahubDevice.getProductSn());
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubDevice.getStatus()),"status",labdatahubDevice.getStatus());
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubDevice.getGroupCode()),"group_code",labdatahubDevice.getGroupCode());
        Page<LabdatahubDevice> page = new Page<LabdatahubDevice>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubDevice> pageList = labdatahubDeviceService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出设备列表
     */
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubDevice labdatahubDevice)
    {
        List<LabdatahubDevice> list = labdatahubDeviceService.selectLabdatahubDeviceList(labdatahubDevice);
        ExcelUtil<LabdatahubDevice> util = new ExcelUtil<LabdatahubDevice>(LabdatahubDevice.class);
        util.exportExcel(response, list, "设备数据");
    }

    /**
     * 获取设备详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubDeviceService.getById(id));
    }

    /**
     * 获取设备详细信息
     */
    @GetMapping("/getDeviceBySn")
    public AjaxResult getDeviceBySn(@RequestParam String deviceSn)
    {
        LabdatahubDevice device = labdatahubDeviceService.getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getDeviceSn,deviceSn),false);
        if(device==null){
            return AjaxResult.error("设备不存在，请检查是否已被删除！");
        }
        return success(device);
    }

    /**
     * 新增设备
     */
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubDevice labdatahubDevice)
    {
        return labdatahubDeviceService.saveDevice(labdatahubDevice);
    }

    /**
     * 修改设备
     */
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubDevice labdatahubDevice)
    {
        labdatahubDeviceService.updateById(labdatahubDevice);
        LabdatahubDevice newDevice = labdatahubDeviceService.getById(labdatahubDevice.getId());
        DeviceHeartbeatManager.updateHeartbeat(newDevice.getDeviceSn(),"1",newDevice.getTimeoutSeconds());
        CacheUtils.updateDeviceCache(newDevice.getDeviceSn());
        return AjaxResult.success();
    }

    /**
     * 修改设备modbus从站ID
     */
    @PutMapping("/updateSlaveId")
    public AjaxResult updateSlaveId(@RequestBody LabdatahubDevice labdatahubDevice)
    {
        labdatahubDeviceService.update(new LambdaUpdateWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getDeviceSn,labdatahubDevice.getDeviceSn())
                .set(LabdatahubDevice::getSlaveId,labdatahubDevice.getSlaveId()));
        LabdatahubDevice newDevice = labdatahubDeviceService.getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getDeviceSn,labdatahubDevice.getDeviceSn()),false);
        CacheUtils.updateDeviceCache(newDevice.getDeviceSn());
        return AjaxResult.success();
    }

    /**
     * 删除设备
     */
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        for (String id : ids) {
            LabdatahubDevice device = labdatahubDeviceService.getById(id);
            labdatahubDeviceService.removeById(id);
            labdatahubProductService.syncDeviceCount(device.getProductSn());
            labdatahubWarnConfigService.remove(new LambdaUpdateWrapper<LabdatahubWarnConfig>()
                    .eq(LabdatahubWarnConfig::getBelongSn,device.getDeviceSn()));
            labdatahubFunctionService.remove(new LambdaUpdateWrapper<LabdatahubFunction>()
                    .eq(LabdatahubFunction::getBelongSn,device.getDeviceSn()));
            CacheUtils.DEVICE_MAP.remove(device.getDeviceSn());
        }
        return AjaxResult.success();
    }

    /**
     * 根据产品sn获取设备列表
     */
    @GetMapping("/getDeviceByProductSn")
    public AjaxResult getDeviceByProductSn(@RequestParam String productSnList){
        List<String> productSn = Arrays.asList(productSnList.split(","));
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .in(LabdatahubDevice::getProductSn,productSn));
        return AjaxResult.success(deviceList);
    }

    /**
     * 同步数据保存时间到设备
     */
    @PostMapping("/editRetentionTime")
    public AjaxResult editRetentionTime(@RequestBody LabdatahubDevice labdatahubDevice) {
        labdatahubDeviceService.update(new LambdaUpdateWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getDeviceSn,labdatahubDevice.getDeviceSn())
                .set(LabdatahubDevice::getRegularCleaning,labdatahubDevice.getRegularCleaning())
                .set(LabdatahubDevice::getRetentionTime,labdatahubDevice.getRetentionTime())
                .set(LabdatahubDevice::getRetentionUnit,labdatahubDevice.getRetentionUnit()));
        CacheUtils.updateDeviceCache(labdatahubDevice.getDeviceSn());
        return AjaxResult.success("修改成功");
    }

    /**
     * 修改自定义配置
     */
    @PostMapping("/editCustomConfig")
    public AjaxResult editCustomConfig(@RequestBody LabdatahubDevice labdatahubDevice){
        labdatahubDeviceService.update(new LambdaUpdateWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getDeviceSn,labdatahubDevice.getDeviceSn())
                .set(LabdatahubDevice::getCustomConfig,labdatahubDevice.getCustomConfig()));
        CacheUtils.updateDeviceCustomConfig(labdatahubDevice.getDeviceSn(),labdatahubDevice.getCustomConfig());
        return AjaxResult.success("更新成功");
    }

    /**
     * 写入一个点位的值（前端「物模型」列表「写值」按钮的入口）
     *
     * <p>与 {@code OpenApiPointValueController#pointValue}
     * （{@code /openapi/v1/device/pointValue}）<b>共用同一个 service，行为不分叉</b>（方案 4.7.2），
     * 区别只在这条走平台 JWT、不需要 {@code @Anonymous}。
     *
     * <p>⚠️ <b>每次调用都真的写设备</b>，没有预演开关。
     *
     * @param request 请求体只有 deviceSn / code / value 三个字段
     * @return code=200 写入成功；其余按方案 4.5 的错误码表（400/404/409/422/500/503/504）
     */
    @PostMapping("/pointValue")
    public AjaxResult pointValue(@RequestBody(required = false) PointWriteRequest request)
    {
        // 来源由入口标明（方案 4.8.2），落进审计表供「写值记录」页区分
        return openApiPointValueService.write(request, PointWriteSource.MANUAL);
    }
}
