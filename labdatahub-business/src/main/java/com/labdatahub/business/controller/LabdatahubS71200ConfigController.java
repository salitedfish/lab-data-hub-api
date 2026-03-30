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
import com.labdatahub.business.domain.LabdatahubS71200Config;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubS71200ConfigService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.core.page.TableDataInfo;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.component.s7_tcp.S7MessageScheduler;
import com.labdatahub.component.s7_tcp.S7ReadConfig;

/**
 * 
* @ClassName: LabdatahubS71200ConfigController  
* @Description: s71200协议读取配置Controller
* @author xwb  
* @date 2026年3月24日
 */
@RestController
@RequestMapping("/business/s71200")
public class LabdatahubS71200ConfigController extends BaseController
{
    @Autowired
    private ILabdatahubS71200ConfigService labdatahubS71200ConfigService;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    /**
     * 查询modbus协议读取配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubS71200Config labdatahubS71200Config)
    {
        LambdaQueryWrapper<LabdatahubS71200Config> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(LabdatahubS71200Config::getCreateTime);
        if (StringUtils.isNotEmpty(labdatahubS71200Config.getBelongSn())) {
            queryWrapper.eq(LabdatahubS71200Config::getBelongSn, labdatahubS71200Config.getBelongSn());
        }
        Page<LabdatahubS71200Config> page = new Page<>(PageUtils.getPageNum(), PageUtils.getPageSize());
        Page<LabdatahubS71200Config> pageList = labdatahubS71200ConfigService.page(page, queryWrapper);
        return getDataTable(pageList);
    }

//    /**
//     * 导出modbus协议读取配置列表
//     */
//    @Log(title = "modbus协议读取配置", businessType = BusinessType.EXPORT)
//    @PostMapping("/export")
//    public void export(HttpServletResponse response, LabdatahubS71200Config labdatahubS71200Config)
//    {
//        List<LabdatahubS71200Config> list = labdatahubS71200ConfigService.selectLabdatahubS71200ConfigList(labdatahubS71200Config);
//        ExcelUtil<LabdatahubS71200Config> util = new ExcelUtil<LabdatahubS71200Config>(LabdatahubS71200Config.class);
//        util.exportExcel(response, list, "modbus协议读取配置数据");
//    }

    /**
     * 获取modbus协议读取配置详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubS71200ConfigService.getById(id));
    }

    /**
     * 新增modbus协议读取配置
     */
    @Log(title = "modbus协议读取配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubS71200Config labdatahubS71200Config)
    {
        labdatahubS71200Config.setCreateTime(new Date());
        return toAjax(labdatahubS71200ConfigService.save(labdatahubS71200Config));
    }

    /**
     * 修改modbus协议读取配置
     */
    @Log(title = "modbus协议读取配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubS71200Config labdatahubS71200Config)
    {
        return toAjax(labdatahubS71200ConfigService.updateById(labdatahubS71200Config));
    }

    /**
     * 删除modbus协议读取配置
     */
    @Log(title = "modbus协议读取配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(labdatahubS71200ConfigService.removeBatchByIds(Arrays.asList(ids)));
    }

    /**
     * 同步到所有设备
     */
    @PostMapping("/syncConfigToDevice")
    public AjaxResult syncConfigToDevice(@RequestParam String productSn){
        List<LabdatahubS71200Config> modbusConfigList = labdatahubS71200ConfigService.list(new LambdaQueryWrapper<LabdatahubS71200Config>()
                .eq(LabdatahubS71200Config::getBelongSn,productSn));
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        deviceList.forEach(device->{
            //删除设备所有规则
            labdatahubS71200ConfigService.remove(new LambdaUpdateWrapper<LabdatahubS71200Config>()
                    .eq(LabdatahubS71200Config::getBelongSn,device.getDeviceSn())
                    .eq(LabdatahubS71200Config::getBelongType,"1"));
            //添加新的规则
            modbusConfigList.forEach(config->{
                config.setId(IdWorker.getIdStr());
                config.setBelongSn(device.getDeviceSn());
                config.setBelongType("1");
                config.setCreateTime(new Date());
                config.setCode(config.getCode());
                config.setDelayTime(config.getDelayTime());
                config.setIntervalTime(config.getIntervalTime());
                config.setDbNumber(config.getDbNumber());
                config.setStartAddress(config.getStartAddress());
                config.setLength(config.getLength());
            });
            labdatahubS71200ConfigService.saveBatch(modbusConfigList);
            if("1".equals(device.getModbusRead())){
                modbusConfigList.forEach(o->{
                    S7MessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    S7ReadConfig config = new S7ReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime().intValue());
                    config.setDbNumber(o.getDbNumber());
                    config.setBlockType(o.getBlockType());
                    config.setBitOffset(o.getBitOffset());
                    config.setStartAddress(o.getStartAddress());
                    config.setLength(o.getLength());
                    S7MessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                modbusConfigList.forEach(o->{
                    S7MessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
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
        List<LabdatahubS71200Config> list = labdatahubS71200ConfigService.list(new LambdaQueryWrapper<LabdatahubS71200Config>()
                .eq(LabdatahubS71200Config::getBelongSn,device.getDeviceSn()));
        if("1".equals(isOpen)){
            list.forEach(o->{
                S7MessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                S7ReadConfig config = new S7ReadConfig();
                config.setDeviceSn(o.getBelongSn());
                config.setCode(o.getCode());
                config.setDelayTime(o.getDelayTime().intValue());
                config.setIntervalTime(o.getIntervalTime().intValue());
                config.setDbNumber(o.getDbNumber());
                config.setBlockType(o.getBlockType());
                config.setBitOffset(o.getBitOffset());
                config.setStartAddress(o.getStartAddress());
                config.setLength(o.getLength());
                S7MessageScheduler.addReadConfig(device.getComponentId(),config);
            });
        }else {
            list.forEach(o->{
                S7MessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
            });
        }
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
            List<LabdatahubS71200Config> list = labdatahubS71200ConfigService.list(new LambdaQueryWrapper<LabdatahubS71200Config>()
                    .eq(LabdatahubS71200Config::getBelongSn,device.getDeviceSn()));
            if("1".equals(isOpen)){
                list.forEach(o->{
                    S7MessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    S7ReadConfig config = new S7ReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime().intValue());
                    config.setDbNumber(o.getDbNumber());
                    config.setBlockType(o.getBlockType());
                    config.setBitOffset(o.getBitOffset());
                    config.setStartAddress(o.getStartAddress());
                    config.setLength(o.getLength());
                    S7MessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                list.forEach(o->{
                    S7MessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                });
            }
        });
        return AjaxResult.success("操作成功");
    }
}
