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
        String error = validateConfig(labdatahubBrotherConfig);
        if (StringUtils.isNotEmpty(error)) {
            return AjaxResult.error(error);
        }
        labdatahubBrotherConfig.setCreateTime(new Date());
        AjaxResult result = toAjax(labdatahubBrotherConfigService.save(labdatahubBrotherConfig));
        // 由AI修改：保存成功后立即同步调度器（新增点位无需重启服务或重开关读取即生效）
        syncConfigToScheduler(labdatahubBrotherConfig.getBelongSn(), labdatahubBrotherConfig, false);
        return result;
    }

    /**
     * 修改Brother NC协议读取配置
     */
    @Log(title = "Brother协议读取配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubBrotherConfig labdatahubBrotherConfig)
    {
        String error = validateConfig(labdatahubBrotherConfig);
        if (StringUtils.isNotEmpty(error)) {
            return AjaxResult.error(error);
        }
        AjaxResult result = toAjax(labdatahubBrotherConfigService.updateById(labdatahubBrotherConfig));
        // 由AI修改：修改成功后立即同步调度器（改地址/间隔无需重启服务即生效）
        syncConfigToScheduler(labdatahubBrotherConfig.getBelongSn(), labdatahubBrotherConfig, false);
        return result;
    }

    /**
     * 校验Brother配置必填项与地址合法性（避免脏数据导致调度空指针或静默采空）
     * @param config 待校验配置
     * @return 空串表示通过，否则为错误提示
     */
    private String validateConfig(LabdatahubBrotherConfig config)
    {
        if (config == null) {
            return "配置不能为空";
        }
        if (StringUtils.isBlank(config.getCode())) {
            return "标识不能为空";
        }
        if (StringUtils.isBlank(config.getDataArea())) {
            return "数据区不能为空";
        }
        if (config.getRowNumber() == null || config.getRowNumber() <= 0) {
            return "行号必须大于0";
        }
        if (config.getFieldIndex() == null || config.getFieldIndex() <= 0) {
            return "字段序号必须大于0";
        }
        if (config.getIntervalTime() == null || config.getIntervalTime() <= 0) {
            return "读取间隔必须大于0";
        }
        return "";
    }

    /**
     * 删除Brother NC协议读取配置
     */
    @Log(title = "Brother协议读取配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        // 由AI修改：删除前先取出配置，删除后逐个同步移除调度器，避免停服前残留调度
        List<LabdatahubBrotherConfig> configList = labdatahubBrotherConfigService.listByIds(Arrays.asList(ids));
        AjaxResult result = toAjax(labdatahubBrotherConfigService.removeBatchByIds(Arrays.asList(ids)));
        for (LabdatahubBrotherConfig config : configList) {
            syncConfigToScheduler(config.getBelongSn(), config, true);
        }
        return result;
    }

    /**
     * 由AI修改：配置保存/删除后，若设备读取开关已开启且已绑定网络组件，立即同步到调度器
     * （新增/修改/删除点位无需重启服务或重开关读取即生效）。
     * 设备级配置按 belongSn（=设备SN）查找设备；产品模板等非设备归属自动跳过（由 syncConfigToDevice 下发）。
     * @param belongSn 归属（设备SN；非设备归属自动跳过）
     * @param config 读取配置
     * @param removeOnly true=仅从调度器移除（删除场景），false=移除后重建（新增/修改场景）
     */
    private void syncConfigToScheduler(String belongSn, LabdatahubBrotherConfig config, boolean removeOnly)
    {
        if (StringUtils.isBlank(belongSn)) {
            return;
        }
        LabdatahubDevice device = labdatahubDeviceService.getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getDeviceSn, belongSn), false);
        if (device == null || device.getComponentId() == null) {
            return; // 非设备级配置或设备未绑定网络组件：不直接调度
        }
        if (!"1".equals(device.getModbusRead())) {
            return; // 读取开关未开启：无需同步调度
        }
        BrotherTcpMessageScheduler.removeReadConfig(device.getComponentId(), device.getDeviceSn(), config.getCode());
        if (removeOnly) {
            return;
        }
        BrotherTcpReadConfig readConfig = new BrotherTcpReadConfig();
        readConfig.setDeviceSn(config.getBelongSn());
        readConfig.setCode(config.getCode());
        readConfig.setDelayTime(config.getDelayTime() == null ? 0 : config.getDelayTime().intValue());
        readConfig.setIntervalTime(config.getIntervalTime() == null ? 1 : config.getIntervalTime().intValue());
        readConfig.setDataArea(config.getDataArea());
        readConfig.setRowNumber(config.getRowNumber());
        readConfig.setFieldIndex(config.getFieldIndex());
        ParseMetaUtils.applyTo(readConfig, device.getDeviceSn(), device.getProductSn(), config.getCode());
        BrotherTcpMessageScheduler.addReadConfig(device.getComponentId(), readConfig);
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
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
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
        if (device == null) {
            return AjaxResult.error("设备不存在：" + deviceSn);
        }
        List<LabdatahubBrotherConfig> list = labdatahubBrotherConfigService.list(new LambdaQueryWrapper<LabdatahubBrotherConfig>()
                .eq(LabdatahubBrotherConfig::getBelongSn,device.getDeviceSn()));
        if("1".equals(isOpen)){
            list.forEach(o->{
                BrotherTcpMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                BrotherTcpReadConfig config = new BrotherTcpReadConfig();
                config.setDeviceSn(o.getBelongSn());
                config.setCode(o.getCode());
                config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
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
            List<LabdatahubBrotherConfig> list = labdatahubBrotherConfigService.list(new LambdaQueryWrapper<LabdatahubBrotherConfig>()
                    .eq(LabdatahubBrotherConfig::getBelongSn,device.getDeviceSn()));
            if("1".equals(isOpen)){
                list.forEach(o->{
                    BrotherTcpMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    BrotherTcpReadConfig config = new BrotherTcpReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
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
            // 持久化读取开关状态，保证刷新页面后开关状态与实际读取一致
            device.setModbusRead(isOpen);
            labdatahubDeviceService.updateById(device);
        });
        return AjaxResult.success("操作成功");
    }
}
