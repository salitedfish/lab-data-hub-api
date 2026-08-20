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
import com.labdatahub.business.domain.LabdatahubBrotherConfig;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.service.ILabdatahubBrotherConfigService;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.business.utils.ParseMetaUtils;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.core.page.TableDataInfo;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.component.brother_tcp.BrotherTcpMessageScheduler;
import com.labdatahub.component.brother_tcp.BrotherTcpReadConfig;

/**
 *
* @ClassName: LabdatahubBrotherConfigController
* @Description: Brother NC协议读取配置Controller
* @author xwb
* @date 2026年8月20日
 */
@RestController
@RequestMapping("/business/brotherTcp")
public class LabdatahubBrotherConfigController extends BaseController
{
    @Autowired
    private ILabdatahubBrotherConfigService labdatahubBrotherConfigService;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    /**
     * 查询Brother NC协议读取配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubBrotherConfig labdatahubBrotherConfig)
    {
        LambdaQueryWrapper<LabdatahubBrotherConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(LabdatahubBrotherConfig::getCreateTime);
        if (StringUtils.isNotEmpty(labdatahubBrotherConfig.getBelongSn())) {
            queryWrapper.eq(LabdatahubBrotherConfig::getBelongSn, labdatahubBrotherConfig.getBelongSn());
        }
        if (StringUtils.isNotEmpty(labdatahubBrotherConfig.getCode())) {
            queryWrapper.like(LabdatahubBrotherConfig::getCode, labdatahubBrotherConfig.getCode());
        }
        Page<LabdatahubBrotherConfig> page = new Page<>(PageUtils.getPageNum(), PageUtils.getPageSize());
        Page<LabdatahubBrotherConfig> pageList = labdatahubBrotherConfigService.page(page, queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 获取Brother NC协议读取配置详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubBrotherConfigService.getById(id));
    }

    /**
     * 新增Brother NC协议读取配置
     */
    @Log(title = "Brother协议读取配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubBrotherConfig labdatahubBrotherConfig)
    {
        labdatahubBrotherConfig.setCreateTime(new Date());
        return toAjax(labdatahubBrotherConfigService.save(labdatahubBrotherConfig));
    }

    /**
     * 修改Brother NC协议读取配置
     */
    @Log(title = "Brother协议读取配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubBrotherConfig labdatahubBrotherConfig)
    {
        return toAjax(labdatahubBrotherConfigService.updateById(labdatahubBrotherConfig));
    }

    /**
     * 删除Brother NC协议读取配置
     */
    @Log(title = "Brother协议读取配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(labdatahubBrotherConfigService.removeBatchByIds(Arrays.asList(ids)));
    }

    /**
     * 同步到所有设备
     */
    @PostMapping("/syncConfigToDevice")
    public AjaxResult syncConfigToDevice(@RequestParam String productSn){
        List<LabdatahubBrotherConfig> configList = labdatahubBrotherConfigService.list(new LambdaQueryWrapper<LabdatahubBrotherConfig>()
                .eq(LabdatahubBrotherConfig::getBelongSn,productSn));
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        deviceList.forEach(device->{
            //删除设备所有规则
            labdatahubBrotherConfigService.remove(new LambdaUpdateWrapper<LabdatahubBrotherConfig>()
                    .eq(LabdatahubBrotherConfig::getBelongSn,device.getDeviceSn())
                    .eq(LabdatahubBrotherConfig::getBelongType,"1"));
            //添加新的规则
            configList.forEach(config->{
                config.setId(IdWorker.getIdStr());
                config.setBelongSn(device.getDeviceSn());
                config.setBelongType("1");
                config.setCreateTime(new Date());
                config.setCode(config.getCode());
                config.setDelayTime(config.getDelayTime());
                config.setIntervalTime(config.getIntervalTime());
                config.setDataArea(config.getDataArea());
                config.setRowNumber(config.getRowNumber());
                config.setFieldIndex(config.getFieldIndex());
            });
            labdatahubBrotherConfigService.saveBatch(configList);
            if("1".equals(device.getModbusRead())){
                configList.forEach(o->{
                    BrotherTcpMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    BrotherTcpReadConfig config = new BrotherTcpReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime().intValue());
                    config.setDataArea(o.getDataArea());
                    config.setRowNumber(o.getRowNumber());
                    config.setFieldIndex(o.getFieldIndex());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    BrotherTcpMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                configList.forEach(o->{
                    BrotherTcpMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
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
        List<LabdatahubBrotherConfig> list = labdatahubBrotherConfigService.list(new LambdaQueryWrapper<LabdatahubBrotherConfig>()
                .eq(LabdatahubBrotherConfig::getBelongSn,device.getDeviceSn()));
        if("1".equals(isOpen)){
            list.forEach(o->{
                BrotherTcpMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                BrotherTcpReadConfig config = new BrotherTcpReadConfig();
                config.setDeviceSn(o.getBelongSn());
                config.setCode(o.getCode());
                config.setDelayTime(o.getDelayTime().intValue());
                config.setIntervalTime(o.getIntervalTime().intValue());
                config.setDataArea(o.getDataArea());
                config.setRowNumber(o.getRowNumber());
                config.setFieldIndex(o.getFieldIndex());
                ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                BrotherTcpMessageScheduler.addReadConfig(device.getComponentId(),config);
            });
        }else {
            list.forEach(o->{
                BrotherTcpMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
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
            List<LabdatahubBrotherConfig> list = labdatahubBrotherConfigService.list(new LambdaQueryWrapper<LabdatahubBrotherConfig>()
                    .eq(LabdatahubBrotherConfig::getBelongSn,device.getDeviceSn()));
            if("1".equals(isOpen)){
                list.forEach(o->{
                    BrotherTcpMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    BrotherTcpReadConfig config = new BrotherTcpReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime().intValue());
                    config.setDataArea(o.getDataArea());
                    config.setRowNumber(o.getRowNumber());
                    config.setFieldIndex(o.getFieldIndex());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    BrotherTcpMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                list.forEach(o->{
                    BrotherTcpMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                });
            }
        });
        return AjaxResult.success("操作成功");
    }
}
