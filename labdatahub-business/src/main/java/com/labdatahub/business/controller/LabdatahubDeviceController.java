package com.labdatahub.business.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.labdatahub.business.domain.*;
import com.labdatahub.business.event.DeviceHeartbeatManager;
import com.labdatahub.business.service.ILabdatahubComponentService;
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
    @Autowired
    private ILabdatahubComponentService labdatahubComponentService;
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
        applyComponentFilter(queryWrapper, labdatahubDevice);
        Page<LabdatahubDevice> page = new Page<LabdatahubDevice>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubDevice> pageList = labdatahubDeviceService.page(page,queryWrapper);
        fillComponentInfo(pageList.getRecords());
        return getDataTable(pageList);
    }

    /**
     * 按网络组件属性过滤设备：名称 / IP / 备注。
     *
     * <p>设备表只有 component_id、component_name 两个派生列，IP、端口、备注都在 labdatahub_component 上，
     * 所以先按条件把命中的组件 ID 查出来，再用 component_id IN (...) 过滤设备。
     * 一个组件都没命中时直接让设备查询恒假 —— 不能用空 IN，MP 对空集合的处理是忽略该条件，
     * 那样就会把全部设备返回，搜索形同失效。
     */
    private void applyComponentFilter(QueryWrapper<LabdatahubDevice> queryWrapper, LabdatahubDevice query) {
        String componentName = StringUtils.trim(query.getComponentName());
        String componentIp = StringUtils.trim(query.getComponentIp());
        String componentRemark = StringUtils.trim(query.getComponentRemark());
        if (StringUtils.isEmpty(componentName) && StringUtils.isEmpty(componentIp) && StringUtils.isEmpty(componentRemark)) {
            return;
        }
        LambdaQueryWrapper<LabdatahubComponent> componentWrapper = new LambdaQueryWrapper<>();
        componentWrapper.like(StringUtils.isNotEmpty(componentName), LabdatahubComponent::getName, componentName);
        componentWrapper.like(StringUtils.isNotEmpty(componentIp), LabdatahubComponent::getIpAddr, componentIp);
        componentWrapper.like(StringUtils.isNotEmpty(componentRemark), LabdatahubComponent::getRemark, componentRemark);
        List<LabdatahubComponent> components = labdatahubComponentService.list(componentWrapper);
        if (components.isEmpty()) {
            queryWrapper.apply("1 = 0");
            return;
        }
        Set<String> componentIds = new HashSet<>();
        for (LabdatahubComponent component : components) {
            componentIds.add(component.getId());
        }
        queryWrapper.in("component_id", componentIds);
    }

    /**
     * 回填设备对应的网络组件 IP / 端口 / 备注（仅本页数据，一次批量查询）。
     *
     * <p>ip_addr / port 是 labdatahub_component 上的派生展示列，正常保存时都会同步；
     * 老数据若为空，再从 other_config 里取一次，避免卡片上明明有组件却显示 "--"。
     */
    private void fillComponentInfo(List<LabdatahubDevice> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<String> componentIds = new HashSet<>();
        for (LabdatahubDevice device : records) {
            if (StringUtils.isNotEmpty(device.getComponentId())) {
                componentIds.add(device.getComponentId());
            }
        }
        if (componentIds.isEmpty()) {
            return;
        }
        Map<String, LabdatahubComponent> componentMap = new HashMap<>();
        for (LabdatahubComponent component : labdatahubComponentService.listByIds(componentIds)) {
            componentMap.put(component.getId(), component);
        }
        for (LabdatahubDevice device : records) {
            LabdatahubComponent component = componentMap.get(device.getComponentId());
            if (component == null) {
                continue;
            }
            device.setComponentIp(resolveComponentIp(component));
            device.setComponentPort(resolveComponentPort(component));
            device.setComponentRemark(component.getRemark());
        }
    }

    /**
     * 组件 IP：优先取派生列，老数据没同步过则回退 other_config.ipAddr / brokerUrl
     */
    private String resolveComponentIp(LabdatahubComponent component) {
        if (StringUtils.isNotEmpty(component.getIpAddr())) {
            return component.getIpAddr();
        }
        JSONObject config = parseOtherConfig(component.getOtherConfig());
        if (config == null) {
            return null;
        }
        String ipAddr = config.getString("ipAddr");
        return StringUtils.isNotEmpty(ipAddr) ? ipAddr : config.getString("brokerUrl");
    }

    /**
     * 组件端口：优先取派生列，老数据回退 other_config，键名与组件保存时的同步逻辑一致
     */
    private String resolveComponentPort(LabdatahubComponent component) {
        if (StringUtils.isNotEmpty(component.getPort())) {
            return component.getPort();
        }
        JSONObject config = parseOtherConfig(component.getOtherConfig());
        if (config == null) {
            return null;
        }
        String port = config.getString("port");
        if (StringUtils.isEmpty(port)) {
            port = config.getString("serverPort");
        }
        if (StringUtils.isEmpty(port)) {
            String tcpPort = config.getString("tcpPort");
            if (StringUtils.isNotEmpty(tcpPort)) {
                String wsPort = config.getString("wsPort");
                port = StringUtils.isNotEmpty(wsPort) ? tcpPort + "," + wsPort : tcpPort;
            }
        }
        return port;
    }

    /**
     * 解析组件的动态配置，非法 JSON 返回 null
     */
    private JSONObject parseOtherConfig(String otherConfig) {
        if (StringUtils.isEmpty(otherConfig)) {
            return null;
        }
        try {
            return JSONObject.parseObject(otherConfig);
        } catch (Exception e) {
            return null;
        }
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
        // 设备状态缓存与库对齐（新增/编辑是同一 SN 重新开始的入口，缓存里可能有上一次生命周期的脏状态）
        CacheUtils.updateDeviceStatusCache(newDevice);
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
            // ⚠️ 设备状态缓存必须一起删，否则同 SN 重建后残留的 true 会让心跳设备永远不写 status=1
            //    （2026-10-21：catlwjc0003 实时数据能收到、页面一直离线）。
            CacheUtils.DEVICE_STATUS.remove(device.getDeviceSn());
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
