//由AI修改
package com.labdatahub.business.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.domain.LabdatahubOmronFinsConfig;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubOmronFinsConfigService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.business.utils.ParseMetaUtils;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.core.page.TableDataInfo;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.component.fins_tcp.FinsMessageScheduler;
import com.labdatahub.component.fins_tcp.FinsReadConfig;

/**
 * 
* @ClassName: LabdatahubOmronFinsConfigController  
* @Description: omronFins协议读取配置Controller
* @author xwb  
* @date 2026年4月1日
 */
@RestController
@RequestMapping("/business/omronFins")
public class LabdatahubOmronFinsConfigController extends BaseController
{
    @Autowired
    private ILabdatahubOmronFinsConfigService labdatahubOmronFinsConfigService;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    /**
     * 查询omronFins协议读取配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubOmronFinsConfig labdatahubOmronFinsConfig)
    {
        LambdaQueryWrapper<LabdatahubOmronFinsConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(LabdatahubOmronFinsConfig::getCreateTime);
        if (StringUtils.isNotEmpty(labdatahubOmronFinsConfig.getBelongSn())) {
            queryWrapper.eq(LabdatahubOmronFinsConfig::getBelongSn, labdatahubOmronFinsConfig.getBelongSn());
        }
        if (StringUtils.isNotEmpty(labdatahubOmronFinsConfig.getCode())) {
            queryWrapper.like(LabdatahubOmronFinsConfig::getCode, labdatahubOmronFinsConfig.getCode());
        }
        Page<LabdatahubOmronFinsConfig> page = new Page<>(PageUtils.getPageNum(), PageUtils.getPageSize());
        Page<LabdatahubOmronFinsConfig> pageList = labdatahubOmronFinsConfigService.page(page, queryWrapper);
        return getDataTable(pageList);
    }

//    /**
//     * 导出modbus协议读取配置列表
//     */
//    @Log(title = "modbus协议读取配置", businessType = BusinessType.EXPORT)
//    @PostMapping("/export")
//    public void export(HttpServletResponse response, LabdatahubOmronFinsConfig labdatahubOmronFinsConfig)
//    {
//        List<LabdatahubOmronFinsConfig> list = labdatahubOmronFinsConfigService.selectLabdatahubOmronFinsConfigList(labdatahubOmronFinsConfig);
//        ExcelUtil<LabdatahubOmronFinsConfig> util = new ExcelUtil<LabdatahubOmronFinsConfig>(LabdatahubOmronFinsConfig.class);
//        util.exportExcel(response, list, "modbus协议读取配置数据");
//    }

    /**
     * 获取modbus协议读取配置详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubOmronFinsConfigService.getById(id));
    }

    /**
     * 新增modbus协议读取配置
     */
    @Log(title = "modbus协议读取配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubOmronFinsConfig labdatahubOmronFinsConfig)
    {
        labdatahubOmronFinsConfig.setCreateTime(new Date());
        return toAjax(labdatahubOmronFinsConfigService.save(labdatahubOmronFinsConfig));
    }

    /**
     * 修改modbus协议读取配置
     */
    @Log(title = "modbus协议读取配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubOmronFinsConfig labdatahubOmronFinsConfig)
    {
        return toAjax(labdatahubOmronFinsConfigService.updateById(labdatahubOmronFinsConfig));
    }

    /**
     * 删除modbus协议读取配置
     */
    @Log(title = "modbus协议读取配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(labdatahubOmronFinsConfigService.removeBatchByIds(Arrays.asList(ids)));
    }

    /**
     * 同步到所有设备
     */
    @PostMapping("/syncConfigToDevice")
    public AjaxResult syncConfigToDevice(@RequestParam String productSn){
        List<LabdatahubOmronFinsConfig> configList = labdatahubOmronFinsConfigService.list(new LambdaQueryWrapper<LabdatahubOmronFinsConfig>()
                .eq(LabdatahubOmronFinsConfig::getBelongSn,productSn));
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        deviceList.forEach(device->{
            //删除设备所有规则
            labdatahubOmronFinsConfigService.remove(new LambdaUpdateWrapper<LabdatahubOmronFinsConfig>()
                    .eq(LabdatahubOmronFinsConfig::getBelongSn,device.getDeviceSn())
                    .eq(LabdatahubOmronFinsConfig::getBelongType,"1"));
            //添加新的规则
            configList.forEach(config->{
                config.setId(IdWorker.getIdStr());
                config.setBelongSn(device.getDeviceSn());
                config.setBelongType("1");
                config.setCreateTime(new Date());
                config.setCode(config.getCode());
                config.setDelayTime(config.getDelayTime());
                config.setIntervalTime(config.getIntervalTime());
                config.setAreaCode(config.getAreaCode());
                config.setStartAddress(config.getStartAddress());
                config.setLength(config.getLength());
            });
            labdatahubOmronFinsConfigService.saveBatch(configList);
            if("1".equals(device.getModbusRead())){
                configList.forEach(o->{
                    FinsMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    FinsReadConfig config = new FinsReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime().intValue());
                    config.setAreaCode(o.getAreaCode());
                    config.setStartAddress(o.getStartAddress());
                    config.setLength(o.getLength());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    FinsMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                configList.forEach(o->{
                    FinsMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                });
            }
            CacheUtils.updateDeviceWarnRule(device.getDeviceSn());
        });
        return AjaxResult.success();
    }

    /**
     * 数据读取开关
     * @param deviceSn 设备SN
     * @param isOpen 0-关闭 1-开启
     */
    @PostMapping("/readSwitchByDevice")
    public AjaxResult readSwitchByDevice(@RequestParam String deviceSn,@RequestParam String isOpen){
        LabdatahubDevice device = labdatahubDeviceService.getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getDeviceSn,deviceSn));
        List<LabdatahubOmronFinsConfig> list = labdatahubOmronFinsConfigService.list(new LambdaQueryWrapper<LabdatahubOmronFinsConfig>()
                .eq(LabdatahubOmronFinsConfig::getBelongSn,device.getDeviceSn()));
        if("1".equals(isOpen)){
            list.forEach(o->{
                FinsMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                FinsReadConfig config = new FinsReadConfig();
                config.setDeviceSn(o.getBelongSn());
                config.setCode(o.getCode());
                config.setDelayTime(o.getDelayTime().intValue());
                config.setIntervalTime(o.getIntervalTime().intValue());
                config.setAreaCode(o.getAreaCode());
                config.setStartAddress(o.getStartAddress());
                config.setLength(o.getLength());
                ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                FinsMessageScheduler.addReadConfig(device.getComponentId(),config);
            });
        }else {
            list.forEach(o->{
                FinsMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
            });
        }
        // 持久化读取开关状态，保证刷新页面后开关状态与实际读取一致
        device.setModbusRead(isOpen);
        labdatahubDeviceService.updateById(device);
        return AjaxResult.success("操作成功");
    }

    /**
     * 产品全部设备数据读取开关
     * @param productSn 产品SN
     * @param isOpen 0-关闭 1-开启
     */
    @PostMapping("/readSwitchByProduct")
    public AjaxResult readSwitchByProduct(@RequestParam String productSn,@RequestParam String isOpen){
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        deviceList.forEach(device->{
            List<LabdatahubOmronFinsConfig> list = labdatahubOmronFinsConfigService.list(new LambdaQueryWrapper<LabdatahubOmronFinsConfig>()
                    .eq(LabdatahubOmronFinsConfig::getBelongSn,device.getDeviceSn()));
            if("1".equals(isOpen)){
                list.forEach(o->{
                    FinsMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    FinsReadConfig config = new FinsReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime().intValue());
                    config.setAreaCode(o.getAreaCode());
                    config.setStartAddress(o.getStartAddress());
                    config.setLength(o.getLength());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    FinsMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                list.forEach(o->{
                    FinsMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                });
            }
            // 持久化读取开关状态，保证刷新页面后开关状态与实际读取一致
            device.setModbusRead(isOpen);
            labdatahubDeviceService.updateById(device);
        });
        return AjaxResult.success("操作成功");
    }
}
