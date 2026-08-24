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
import com.labdatahub.business.domain.LabdatahubFanucConfig;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubFanucConfigService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.business.utils.ParseMetaUtils;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.core.page.TableDataInfo;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.component.fanuc_focas.FanucFocasMessageScheduler;
import com.labdatahub.component.fanuc_focas.FanucFocasReadConfig;

/**
 *
* @ClassName: LabdatahubFanucConfigController
* @Description: FANUC FOCAS2协议读取配置Controller
* @author xwb
* @date 2026年8月24日
 */
@RestController
@RequestMapping("/business/fanucTcp")
public class LabdatahubFanucConfigController extends BaseController
{
    @Autowired
    private ILabdatahubFanucConfigService labdatahubFanucConfigService;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    /**
     * 查询FANUC FOCAS2协议读取配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubFanucConfig labdatahubFanucConfig)
    {
        LambdaQueryWrapper<LabdatahubFanucConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(LabdatahubFanucConfig::getCreateTime);
        if (StringUtils.isNotEmpty(labdatahubFanucConfig.getBelongSn())) {
            queryWrapper.eq(LabdatahubFanucConfig::getBelongSn, labdatahubFanucConfig.getBelongSn());
        }
        if (StringUtils.isNotEmpty(labdatahubFanucConfig.getCode())) {
            queryWrapper.like(LabdatahubFanucConfig::getCode, labdatahubFanucConfig.getCode());
        }
        Page<LabdatahubFanucConfig> page = new Page<>(PageUtils.getPageNum(), PageUtils.getPageSize());
        Page<LabdatahubFanucConfig> pageList = labdatahubFanucConfigService.page(page, queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 获取FANUC FOCAS2协议读取配置详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubFanucConfigService.getById(id));
    }

    /**
     * 新增FANUC FOCAS2协议读取配置
     */
    @Log(title = "FANUC协议读取配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubFanucConfig labdatahubFanucConfig)
    {
        String error = validateConfig(labdatahubFanucConfig);
        if (StringUtils.isNotEmpty(error)) {
            return AjaxResult.error(error);
        }
        labdatahubFanucConfig.setCreateTime(new Date());
        return toAjax(labdatahubFanucConfigService.save(labdatahubFanucConfig));
    }

    /**
     * 修改FANUC FOCAS2协议读取配置
     */
    @Log(title = "FANUC协议读取配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubFanucConfig labdatahubFanucConfig)
    {
        String error = validateConfig(labdatahubFanucConfig);
        if (StringUtils.isNotEmpty(error)) {
            return AjaxResult.error(error);
        }
        return toAjax(labdatahubFanucConfigService.updateById(labdatahubFanucConfig));
    }

    /**
     * 校验FANUC配置必填项与地址合法性（避免脏数据导致调度空指针或静默采空）
     * 参数按采集项类型校验：参数1除 mode（操作模式）外均必填；
     * 参数2仅 axis/pmc 需要（坐标类型/PMC地址号），其余采集项非必填
     * @param config 待校验配置
     * @return 空串表示通过，否则为错误提示
     */
    private String validateConfig(LabdatahubFanucConfig config)
    {
        if (config == null) {
            return "配置不能为空";
        }
        if (StringUtils.isBlank(config.getCode())) {
            return "标识不能为空";
        }
        if (StringUtils.isBlank(config.getReadType())) {
            return "采集项类型不能为空";
        }
        String readType = config.getReadType().trim().toLowerCase();
        // 参数1：除 mode 外均必填（mode 无参数概念，cnc_rdopmode 直接读取）
        if (!"mode".equals(readType) && (config.getParam1() == null || config.getParam1() <= 0)) {
            return "参数1必须大于0";
        }
        // 参数2：axis=坐标类型(1机械 2绝对 3相对 4剩余)、pmc=PMC字节地址号，缺则无法定位数据
        if (("axis".equals(readType) || "pmc".equals(readType)) && config.getParam2() == null) {
            return "参数2不能为空";
        }
        if (config.getIntervalTime() == null || config.getIntervalTime() <= 0) {
            return "读取间隔必须大于0";
        }
        return "";
    }

    /**
     * 删除FANUC FOCAS2协议读取配置
     */
    @Log(title = "FANUC协议读取配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(labdatahubFanucConfigService.removeBatchByIds(Arrays.asList(ids)));
    }

    /**
     * 同步到所有设备
     */
    @PostMapping("/syncConfigToDevice")
    public AjaxResult syncConfigToDevice(@RequestParam String productSn){
        List<LabdatahubFanucConfig> configList = labdatahubFanucConfigService.list(new LambdaQueryWrapper<LabdatahubFanucConfig>()
                .eq(LabdatahubFanucConfig::getBelongSn,productSn));
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        deviceList.forEach(device->{
            //删除设备所有规则
            labdatahubFanucConfigService.remove(new LambdaUpdateWrapper<LabdatahubFanucConfig>()
                    .eq(LabdatahubFanucConfig::getBelongSn,device.getDeviceSn())
                    .eq(LabdatahubFanucConfig::getBelongType,"1"));
            //添加新的规则
            configList.forEach(config->{
                config.setId(IdWorker.getIdStr());
                config.setBelongSn(device.getDeviceSn());
                config.setBelongType("1");
                config.setCreateTime(new Date());
                config.setCode(config.getCode());
                config.setDelayTime(config.getDelayTime());
                config.setIntervalTime(config.getIntervalTime());
                config.setReadType(config.getReadType());
                config.setParam1(config.getParam1());
                config.setParam2(config.getParam2());
            });
            labdatahubFanucConfigService.saveBatch(configList);
            if("1".equals(device.getModbusRead())){
                configList.forEach(o->{
                    FanucFocasMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    FanucFocasReadConfig config = new FanucFocasReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                    config.setReadType(o.getReadType());
                    config.setParam1(o.getParam1());
                    config.setParam2(o.getParam2());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    FanucFocasMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                configList.forEach(o->{
                    FanucFocasMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
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
        List<LabdatahubFanucConfig> list = labdatahubFanucConfigService.list(new LambdaQueryWrapper<LabdatahubFanucConfig>()
                .eq(LabdatahubFanucConfig::getBelongSn,device.getDeviceSn()));
        if("1".equals(isOpen)){
            list.forEach(o->{
                FanucFocasMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                FanucFocasReadConfig config = new FanucFocasReadConfig();
                config.setDeviceSn(o.getBelongSn());
                config.setCode(o.getCode());
                config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                config.setReadType(o.getReadType());
                config.setParam1(o.getParam1());
                config.setParam2(o.getParam2());
                ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                FanucFocasMessageScheduler.addReadConfig(device.getComponentId(),config);
            });
        }else {
            list.forEach(o->{
                FanucFocasMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
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
            List<LabdatahubFanucConfig> list = labdatahubFanucConfigService.list(new LambdaQueryWrapper<LabdatahubFanucConfig>()
                    .eq(LabdatahubFanucConfig::getBelongSn,device.getDeviceSn()));
            if("1".equals(isOpen)){
                list.forEach(o->{
                    FanucFocasMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    FanucFocasReadConfig config = new FanucFocasReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                    config.setReadType(o.getReadType());
                    config.setParam1(o.getParam1());
                    config.setParam2(o.getParam2());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    FanucFocasMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                list.forEach(o->{
                    FanucFocasMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                });
            }
            // 持久化读取开关状态，保证刷新页面后开关状态与实际读取一致
            device.setModbusRead(isOpen);
            labdatahubDeviceService.updateById(device);
        });
        return AjaxResult.success("操作成功");
    }
}
