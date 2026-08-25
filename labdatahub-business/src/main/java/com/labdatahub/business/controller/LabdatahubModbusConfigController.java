//由AI修改
package com.labdatahub.business.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

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
import com.labdatahub.business.domain.LabdatahubModbusConfig;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubModbusConfigService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.business.utils.ParseMetaUtils;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.core.page.TableDataInfo;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.component.modbus_tcp.ModbusMessageScheduler;
import com.labdatahub.component.modbus_tcp.ModbusReadConfig;

/**
 * modbus协议读取配置Controller
 * 
 * @author ruoyi
 * @date 2025-12-16
 */
@RestController
@RequestMapping("/business/modbus")
public class LabdatahubModbusConfigController extends BaseController
{
    @Autowired
    private ILabdatahubModbusConfigService labdatahubModbusConfigService;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    /**
     * 查询modbus协议读取配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubModbusConfig labdatahubModbusConfig)
    {
//        QueryWrapper<LabdatahubModbusConfig> queryWrapper = new QueryWrapper<>();
//        queryWrapper.orderByAsc("create_time");
//        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubModbusConfig.getBelongSn()),"belong_sn",labdatahubModbusConfig.getBelongSn());
//        Page<LabdatahubModbusConfig> page = new Page<LabdatahubModbusConfig>(PageUtils.getPageNum(),PageUtils.getPageSize());
//        Page<LabdatahubModbusConfig> pageList = labdatahubModbusConfigService.page(page,queryWrapper);
        LambdaQueryWrapper<LabdatahubModbusConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(LabdatahubModbusConfig::getCreateTime);
        if (StringUtils.isNotEmpty(labdatahubModbusConfig.getBelongSn())) {
            queryWrapper.eq(LabdatahubModbusConfig::getBelongSn, labdatahubModbusConfig.getBelongSn());
        }
        if (StringUtils.isNotEmpty(labdatahubModbusConfig.getCode())) {
            queryWrapper.like(LabdatahubModbusConfig::getCode, labdatahubModbusConfig.getCode());
        }
        Page<LabdatahubModbusConfig> page = new Page<>(PageUtils.getPageNum(), PageUtils.getPageSize());
        Page<LabdatahubModbusConfig> pageList = labdatahubModbusConfigService.page(page, queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出modbus协议读取配置列表
     */
    @Log(title = "modbus协议读取配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubModbusConfig labdatahubModbusConfig)
    {
        List<LabdatahubModbusConfig> list = labdatahubModbusConfigService.selectLabdatahubModbusConfigList(labdatahubModbusConfig);
        ExcelUtil<LabdatahubModbusConfig> util = new ExcelUtil<LabdatahubModbusConfig>(LabdatahubModbusConfig.class);
        util.exportExcel(response, list, "modbus协议读取配置数据");
    }

    /**
     * 获取modbus协议读取配置详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubModbusConfigService.getById(id));
    }

    /**
     * 校验寄存器范围：一个标识只对应一个读取点位，只允许单段区间（如 0 或 0-3）
     * 多段区间会因协议端按 code 折叠丢数据，这里直接拒绝
     *
     * @return null-校验通过，否则返回错误信息
     */
    private AjaxResult checkRegisterRange(LabdatahubModbusConfig config) {
        String registerRange = config.getRegisterRange();
        if (StringUtils.isEmpty(registerRange)) {
            return AjaxResult.error("寄存器范围不能为空");
        }
        String range = registerRange.trim();
        String[] parts = range.split("-");
        boolean valid = parts.length <= 2
                && parts[0].matches("\\d+")
                && (parts.length == 1 || parts[1].matches("\\d+"))
                && (parts.length == 1 || Integer.parseInt(parts[1]) >= Integer.parseInt(parts[0]));
        if (!valid) {
            return AjaxResult.error("寄存器范围格式不正确，请输入单个区间，如：0 或 0-3");
        }
        return null;
    }

    /**
     * 校验功能码与读取间隔
     *
     * @return null-校验通过，否则返回错误信息
     */
    private AjaxResult checkReadConfig(LabdatahubModbusConfig config) {
        if (config.getFunctionCode() == null || StringUtils.isEmpty(config.getFunctionCode().trim())) {
            config.setFunctionCode("03");
        } else {
            String functionCode = config.getFunctionCode().trim();
            if (!"01".equals(functionCode) && !"02".equals(functionCode)
                    && !"03".equals(functionCode) && !"04".equals(functionCode)) {
                return AjaxResult.error("功能码只能是 01线圈/02离散输入/03保持寄存器/04输入寄存器");
            }
            config.setFunctionCode(functionCode);
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
    public AjaxResult add(@RequestBody LabdatahubModbusConfig labdatahubModbusConfig)
    {
        AjaxResult check = checkRegisterRange(labdatahubModbusConfig);
        if (check != null) {
            return check;
        }
        AjaxResult checkConfig = checkReadConfig(labdatahubModbusConfig);
        if (checkConfig != null) {
            return checkConfig;
        }
        labdatahubModbusConfig.setCreateTime(new Date());
        AjaxResult result = toAjax(labdatahubModbusConfigService.save(labdatahubModbusConfig));
        // 由AI修改：保存成功后立即同步调度器（新增点位无需重启服务或重开关读取即生效）
        syncConfigToScheduler(labdatahubModbusConfig.getBelongSn(), labdatahubModbusConfig, false);
        return result;
    }

    /**
     * 修改modbus协议读取配置
     */
    @Log(title = "modbus协议读取配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubModbusConfig labdatahubModbusConfig)
    {
        AjaxResult check = checkRegisterRange(labdatahubModbusConfig);
        if (check != null) {
            return check;
        }
        AjaxResult checkConfig = checkReadConfig(labdatahubModbusConfig);
        if (checkConfig != null) {
            return checkConfig;
        }
        AjaxResult result = toAjax(labdatahubModbusConfigService.updateById(labdatahubModbusConfig));
        // 由AI修改：修改成功后立即同步调度器（改地址/间隔无需重启服务即生效）
        syncConfigToScheduler(labdatahubModbusConfig.getBelongSn(), labdatahubModbusConfig, false);
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
        List<LabdatahubModbusConfig> configList = labdatahubModbusConfigService.listByIds(Arrays.asList(ids));
        AjaxResult result = toAjax(labdatahubModbusConfigService.removeBatchByIds(Arrays.asList(ids)));
        for (LabdatahubModbusConfig config : configList) {
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
    private void syncConfigToScheduler(String belongSn, LabdatahubModbusConfig config, boolean removeOnly)
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
        ModbusMessageScheduler.removeReadConfig(device.getComponentId(), device.getDeviceSn(), config.getCode());
        if (removeOnly) {
            return;
        }
        ModbusReadConfig readConfig = new ModbusReadConfig();
        readConfig.setDeviceSn(config.getBelongSn());
        readConfig.setCode(config.getCode());
        readConfig.setDelayTime(config.getDelayTime() == null ? 0 : config.getDelayTime().intValue());
        readConfig.setIntervalTime(config.getIntervalTime() == null ? 1 : config.getIntervalTime().intValue());
        readConfig.setSlaveId(device.getSlaveId());
        readConfig.setRegisterRange(config.getRegisterRange());
        readConfig.setFunctionCode(config.getFunctionCode());
        ParseMetaUtils.applyTo(readConfig, device.getDeviceSn(), device.getProductSn(), config.getCode());
        ModbusMessageScheduler.addReadConfig(device.getComponentId(), readConfig);
    }

    /**
     * 同步到所有设备
     */
    @PostMapping("/syncConfigToDevice")
    public AjaxResult syncConfigToDevice(@RequestParam String productSn){
        List<LabdatahubModbusConfig> modbusConfigList = labdatahubModbusConfigService.list(new LambdaQueryWrapper<LabdatahubModbusConfig>()
                .eq(LabdatahubModbusConfig::getBelongSn,productSn));
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        deviceList.forEach(device->{
            //删除设备所有规则
            labdatahubModbusConfigService.remove(new LambdaUpdateWrapper<LabdatahubModbusConfig>()
                    .eq(LabdatahubModbusConfig::getBelongSn,device.getDeviceSn())
                    .eq(LabdatahubModbusConfig::getBelongType,"1"));
            //添加新的规则
            modbusConfigList.forEach(config->{
                config.setId(IdWorker.getIdStr());
                config.setBelongSn(device.getDeviceSn());
                config.setBelongType("1");
                config.setCreateTime(new Date());
                config.setCode(config.getCode());
                config.setDelayTime(config.getDelayTime());
                config.setIntervalTime(config.getIntervalTime());
                config.setRegisterRange(config.getRegisterRange());
            });
            labdatahubModbusConfigService.saveBatch(modbusConfigList);
            if("1".equals(device.getModbusRead())){
                modbusConfigList.forEach(o->{
                    ModbusMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    ModbusReadConfig config = new ModbusReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                    config.setSlaveId(device.getSlaveId());
                    config.setRegisterRange(o.getRegisterRange());
                    config.setFunctionCode(o.getFunctionCode());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    ModbusMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                modbusConfigList.forEach(o->{
                    ModbusMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
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
        List<LabdatahubModbusConfig> list = labdatahubModbusConfigService.list(new LambdaQueryWrapper<LabdatahubModbusConfig>()
                .eq(LabdatahubModbusConfig::getBelongSn,device.getDeviceSn()));
        if("1".equals(isOpen)){
            list.forEach(o->{
                ModbusMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                ModbusReadConfig config = new ModbusReadConfig();
                config.setDeviceSn(o.getBelongSn());
                config.setCode(o.getCode());
                config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                config.setSlaveId(device.getSlaveId());
                config.setRegisterRange(o.getRegisterRange());
                config.setFunctionCode(o.getFunctionCode());
                ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                ModbusMessageScheduler.addReadConfig(device.getComponentId(),config);
            });
        }else {
            list.forEach(o->{
                ModbusMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
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
            List<LabdatahubModbusConfig> list = labdatahubModbusConfigService.list(new LambdaQueryWrapper<LabdatahubModbusConfig>()
                    .eq(LabdatahubModbusConfig::getBelongSn,device.getDeviceSn()));
            if("1".equals(isOpen)){
                list.forEach(o->{
                    ModbusMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    ModbusReadConfig config = new ModbusReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                    config.setSlaveId(device.getSlaveId());
                    config.setRegisterRange(o.getRegisterRange());
                    config.setFunctionCode(o.getFunctionCode());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    ModbusMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                list.forEach(o->{
                    ModbusMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                });
            }
            // 持久化读取开关状态，保证刷新页面后开关状态与实际读取一致
            device.setModbusRead(isOpen);
            labdatahubDeviceService.updateById(device);
        });
        return AjaxResult.success("操作成功");
    }
}
