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
import com.labdatahub.business.utils.ProtocolPointModelSync;
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
     * 校验 FINS 读取配置字段（存储区白名单 + 地址/长度/间隔）
     *
     * @return null-校验通过，否则返回错误信息
     */
    private AjaxResult checkConfig(LabdatahubOmronFinsConfig config) {
        if (config.getAreaCode() == null) {
            return AjaxResult.error("存储区 areaCode 不能为空");
        }
        // 存储区白名单：DM区/CIO区/WR区/H区/IR区/LR区/EM区
        // FINS 标准区码：DM=0x82 / CIO=0x30 / WR=0xB1 / H(HR)=0x32 / IR=0x88 / LR=0x98 / EM=0xA0
        // 注意：H 区是 0x32（0x31 是 WR 的位码，旧配置误用会静默读到 WR 区）
        if (!Arrays.asList(0x82, 0x30, 0xB1, 0x32, 0x88, 0x98, 0xA0).contains(config.getAreaCode())) {
            return AjaxResult.error("存储区 areaCode 只能是 DM区(0x82)/CIO区(0x30)/WR区(0xB1)/H区(0x32)/IR区(0x88)/LR区(0x98)/EM区(0xA0)");
        }
        if (config.getStartAddress() == null || config.getStartAddress() < 0) {
            return AjaxResult.error("起始地址 startAddress 不能为负数");
        }
        if (config.getLength() == null || config.getLength() < 1 || config.getLength() > 1000) {
            return AjaxResult.error("读取长度 length 必须在 1-1000 之间");
        }
        if (config.getIntervalTime() == null || config.getIntervalTime() <= 0) {
            return AjaxResult.error("读取间隔 intervalTime 必须为正整数（单位：秒）");
        }
        return null;
    }

    /**
     * 新增modbus协议读取配置
     */
    @Log(title = "modbus协议读取配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubOmronFinsConfig labdatahubOmronFinsConfig)
    {
        AjaxResult check = checkConfig(labdatahubOmronFinsConfig);
        if (check != null) {
            return check;
        }
        labdatahubOmronFinsConfig.setCreateTime(new Date());
        AjaxResult result = toAjax(labdatahubOmronFinsConfigService.save(labdatahubOmronFinsConfig));
        // 由AI修改：保存成功后立即同步调度器（新增点位无需重启服务或重开关读取即生效）
        syncConfigToScheduler(labdatahubOmronFinsConfig.getBelongSn(), labdatahubOmronFinsConfig, false);
        // 由AI修改：自动生成对应物模型属性（identifier=code），无需再手动去物模型 tab 建同名属性
        ProtocolPointModelSync.afterAdd(labdatahubOmronFinsConfig.getBelongSn(), labdatahubOmronFinsConfig.getBelongType(), labdatahubOmronFinsConfig.getCode(), labdatahubOmronFinsConfig.getName());
        return result;
    }

    /**
     * 修改modbus协议读取配置
     */
    @Log(title = "modbus协议读取配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubOmronFinsConfig labdatahubOmronFinsConfig)
    {
        AjaxResult check = checkConfig(labdatahubOmronFinsConfig);
        if (check != null) {
            return check;
        }
        // 由AI修改：修改前先取旧配置，标识(code)或归属(belongSn)变更时先移除旧调度，
        // 避免旧 code 的调度以旧 key 残留在调度器，导致同一点位被新旧两套调度同时读取
        String oldCode = null;
        if (labdatahubOmronFinsConfig.getId() != null) {
            LabdatahubOmronFinsConfig oldConfig = labdatahubOmronFinsConfigService.getById(labdatahubOmronFinsConfig.getId());
            if (oldConfig != null && StringUtils.isNotBlank(oldConfig.getBelongSn())) {
                oldCode = oldConfig.getCode();
                LabdatahubDevice oldDevice = labdatahubDeviceService.getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                        .eq(LabdatahubDevice::getDeviceSn, oldConfig.getBelongSn()), false);
                if (oldDevice != null && oldDevice.getComponentId() != null) {
                    FinsMessageScheduler.removeReadConfig(oldDevice.getComponentId(), oldDevice.getDeviceSn(), oldConfig.getCode());
                }
            }
        }
        AjaxResult result = toAjax(labdatahubOmronFinsConfigService.updateById(labdatahubOmronFinsConfig));
        // 由AI修改：修改成功后立即同步调度器（改地址/间隔无需重启服务即生效）
        syncConfigToScheduler(labdatahubOmronFinsConfig.getBelongSn(), labdatahubOmronFinsConfig, false);
        // 由AI修改：点位标识(code)变更时同步修改对应物模型 identifier/name
        ProtocolPointModelSync.afterEdit(labdatahubOmronFinsConfig.getBelongSn(), labdatahubOmronFinsConfig.getBelongType(), oldCode, labdatahubOmronFinsConfig.getCode(), labdatahubOmronFinsConfig.getName());
        return result;
    }

    /**
     * 删除modbus协议读取配置
     */
    @Log(title = "modbus协议读取配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        // 由AI修改：删除前先取出配置，删除后逐个同步移除调度器，避免停服前残留调度
        List<LabdatahubOmronFinsConfig> configList = labdatahubOmronFinsConfigService.listByIds(Arrays.asList(ids));
        AjaxResult result = toAjax(labdatahubOmronFinsConfigService.removeBatchByIds(Arrays.asList(ids)));
        for (LabdatahubOmronFinsConfig config : configList) {
            syncConfigToScheduler(config.getBelongSn(), config, true);
            // 由AI修改：删除点位同时删除自动生成的对应物模型属性（不误伤手动配置的同名属性）
            ProtocolPointModelSync.afterRemove(config.getBelongSn(), config.getBelongType(), config.getCode());
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
    private void syncConfigToScheduler(String belongSn, LabdatahubOmronFinsConfig config, boolean removeOnly)
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
        FinsMessageScheduler.removeReadConfig(device.getComponentId(), device.getDeviceSn(), config.getCode());
        if (removeOnly) {
            return;
        }
        FinsReadConfig readConfig = new FinsReadConfig();
        readConfig.setDeviceSn(config.getBelongSn());
        readConfig.setCode(config.getCode());
        readConfig.setDelayTime(config.getDelayTime() == null ? 0 : config.getDelayTime().intValue());
        readConfig.setIntervalTime(config.getIntervalTime() == null ? 1 : config.getIntervalTime().intValue());
        readConfig.setAreaCode(config.getAreaCode());
        readConfig.setStartAddress(config.getStartAddress());
        readConfig.setLength(config.getLength());
        ParseMetaUtils.applyTo(readConfig, device.getDeviceSn(), device.getProductSn(), config.getCode());
        FinsMessageScheduler.addReadConfig(device.getComponentId(), readConfig);
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
                config.setName(config.getName());
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
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
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
        if (device == null) {
            return AjaxResult.error("设备不存在：" + deviceSn);
        }
        List<LabdatahubOmronFinsConfig> list = labdatahubOmronFinsConfigService.list(new LambdaQueryWrapper<LabdatahubOmronFinsConfig>()
                .eq(LabdatahubOmronFinsConfig::getBelongSn,device.getDeviceSn()));
        if("1".equals(isOpen)){
            list.forEach(o->{
                FinsMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                FinsReadConfig config = new FinsReadConfig();
                config.setDeviceSn(o.getBelongSn());
                config.setCode(o.getCode());
                config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
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
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
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
