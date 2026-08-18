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
        labdatahubModbusConfig.setCreateTime(new Date());
        return toAjax(labdatahubModbusConfigService.save(labdatahubModbusConfig));
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
        return toAjax(labdatahubModbusConfigService.updateById(labdatahubModbusConfig));
    }

    /**
     * 删除modbus协议读取配置
     */
    @Log(title = "modbus协议读取配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(labdatahubModbusConfigService.removeBatchByIds(Arrays.asList(ids)));
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
                    config.setDelayTime(o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime().intValue());
                    config.setSlaveId(device.getSlaveId());
                    config.setRegisterRange(o.getRegisterRange());
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
        List<LabdatahubModbusConfig> list = labdatahubModbusConfigService.list(new LambdaQueryWrapper<LabdatahubModbusConfig>()
                .eq(LabdatahubModbusConfig::getBelongSn,device.getDeviceSn()));
        if("1".equals(isOpen)){
            list.forEach(o->{
                ModbusMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                ModbusReadConfig config = new ModbusReadConfig();
                config.setDeviceSn(o.getBelongSn());
                config.setCode(o.getCode());
                config.setDelayTime(o.getDelayTime().intValue());
                config.setIntervalTime(o.getIntervalTime().intValue());
                config.setSlaveId(device.getSlaveId());
                config.setRegisterRange(o.getRegisterRange());
                ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                ModbusMessageScheduler.addReadConfig(device.getComponentId(),config);
            });
        }else {
            list.forEach(o->{
                ModbusMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
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
            List<LabdatahubModbusConfig> list = labdatahubModbusConfigService.list(new LambdaQueryWrapper<LabdatahubModbusConfig>()
                    .eq(LabdatahubModbusConfig::getBelongSn,device.getDeviceSn()));
            if("1".equals(isOpen)){
                list.forEach(o->{
                    ModbusMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    ModbusReadConfig config = new ModbusReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime().intValue());
                    config.setSlaveId(device.getSlaveId());
                    config.setRegisterRange(o.getRegisterRange());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    ModbusMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                list.forEach(o->{
                    ModbusMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                });
            }
        });
        return AjaxResult.success("操作成功");
    }
}
