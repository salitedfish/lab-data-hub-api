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
import com.labdatahub.business.domain.LabdatahubMitsubishiConfig;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubMitsubishiConfigService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.business.utils.ParseMetaUtils;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.core.page.TableDataInfo;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.component.mitsubishi_tcp.MitsubishiMessageScheduler;
import com.labdatahub.component.mitsubishi_tcp.MitsubishiReadConfig;

/**
 *
* @ClassName: LabdatahubMitsubishiConfigController
* @Description: 三菱MC协议读取配置Controller
* @author xwb
* @date 2026年8月24日
 */
@RestController
@RequestMapping("/business/mitsubishiTcp")
public class LabdatahubMitsubishiConfigController extends BaseController
{
    @Autowired
    private ILabdatahubMitsubishiConfigService labdatahubMitsubishiConfigService;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;

    // 字设备软元件代码（按字读取）
    private static final List<Integer> WORD_DEVICE_CODES = Arrays.asList(0xA8, 0xB4, 0xAF, 0xB0, 0xA9);
    // 位设备软元件代码（按位读取）
    private static final List<Integer> BIT_DEVICE_CODES = Arrays.asList(0x90, 0x92, 0xA0, 0x9C, 0x9D, 0x98, 0x91, 0x93);

    /**
     * 判断软元件是否为字设备
     */
    private boolean isWordDevice(Integer areaCode) {
        return WORD_DEVICE_CODES.contains(areaCode);
    }

    /**
     * 查询三菱MC协议读取配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubMitsubishiConfig labdatahubMitsubishiConfig)
    {
        LambdaQueryWrapper<LabdatahubMitsubishiConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(LabdatahubMitsubishiConfig::getCreateTime);
        if (StringUtils.isNotEmpty(labdatahubMitsubishiConfig.getBelongSn())) {
            queryWrapper.eq(LabdatahubMitsubishiConfig::getBelongSn, labdatahubMitsubishiConfig.getBelongSn());
        }
        if (StringUtils.isNotEmpty(labdatahubMitsubishiConfig.getCode())) {
            queryWrapper.like(LabdatahubMitsubishiConfig::getCode, labdatahubMitsubishiConfig.getCode());
        }
        Page<LabdatahubMitsubishiConfig> page = new Page<>(PageUtils.getPageNum(), PageUtils.getPageSize());
        Page<LabdatahubMitsubishiConfig> pageList = labdatahubMitsubishiConfigService.page(page, queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 获取三菱MC协议读取配置详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubMitsubishiConfigService.getById(id));
    }

    /**
     * 校验三菱MC读取配置字段（软元件白名单 + 字/位长度上限 + 地址/间隔）
     *
     * @return null-校验通过，否则返回错误信息
     */
    private AjaxResult checkConfig(LabdatahubMitsubishiConfig config) {
        if (config.getAreaCode() == null) {
            return AjaxResult.error("软元件代码 areaCode 不能为空");
        }
        // 软元件白名单：字设备 D/W/R/ZR/SD；位设备 M/L/B/X/Y/S/SM/F
        if (!WORD_DEVICE_CODES.contains(config.getAreaCode()) && !BIT_DEVICE_CODES.contains(config.getAreaCode())) {
            return AjaxResult.error("软元件代码 areaCode 只能是 D(0xA8)/W(0xB4)/R(0xAF)/ZR(0xB0)/SD(0xA9)/M(0x90)/L(0x92)/B(0xA0)/X(0x9C)/Y(0x9D)/S(0x98)/SM(0x91)/F(0x93)");
        }
        if (config.getStartAddress() == null || config.getStartAddress() < 0) {
            return AjaxResult.error("起始地址 startAddress 不能为负数");
        }
        if (config.getLength() == null || config.getLength() < 1) {
            return AjaxResult.error("读取数量 length 不能小于1");
        }
        if (isWordDevice(config.getAreaCode())) {
            if (config.getLength() > 960) {
                return AjaxResult.error("字设备单次读取数量 length 不能超过 960");
            }
        } else {
            if (config.getLength() > 2000) {
                return AjaxResult.error("位设备单次读取数量 length 不能超过 2000");
            }
        }
        if (config.getIntervalTime() == null || config.getIntervalTime() <= 0) {
            return AjaxResult.error("读取间隔 intervalTime 必须为正整数（单位：秒）");
        }
        return null;
    }

    /**
     * 新增三菱MC协议读取配置
     */
    @Log(title = "三菱MC协议读取配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubMitsubishiConfig labdatahubMitsubishiConfig)
    {
        AjaxResult check = checkConfig(labdatahubMitsubishiConfig);
        if (check != null) {
            return check;
        }
        labdatahubMitsubishiConfig.setCreateTime(new Date());
        return toAjax(labdatahubMitsubishiConfigService.save(labdatahubMitsubishiConfig));
    }

    /**
     * 修改三菱MC协议读取配置
     */
    @Log(title = "三菱MC协议读取配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubMitsubishiConfig labdatahubMitsubishiConfig)
    {
        AjaxResult check = checkConfig(labdatahubMitsubishiConfig);
        if (check != null) {
            return check;
        }
        return toAjax(labdatahubMitsubishiConfigService.updateById(labdatahubMitsubishiConfig));
    }

    /**
     * 删除三菱MC协议读取配置
     */
    @Log(title = "三菱MC协议读取配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(labdatahubMitsubishiConfigService.removeBatchByIds(Arrays.asList(ids)));
    }

    /**
     * 同步到所有设备
     */
    @PostMapping("/syncConfigToDevice")
    public AjaxResult syncConfigToDevice(@RequestParam String productSn){
        List<LabdatahubMitsubishiConfig> configList = labdatahubMitsubishiConfigService.list(new LambdaQueryWrapper<LabdatahubMitsubishiConfig>()
                .eq(LabdatahubMitsubishiConfig::getBelongSn,productSn));
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        deviceList.forEach(device->{
            //删除设备所有规则
            labdatahubMitsubishiConfigService.remove(new LambdaUpdateWrapper<LabdatahubMitsubishiConfig>()
                    .eq(LabdatahubMitsubishiConfig::getBelongSn,device.getDeviceSn())
                    .eq(LabdatahubMitsubishiConfig::getBelongType,"1"));
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
            labdatahubMitsubishiConfigService.saveBatch(configList);
            if("1".equals(device.getModbusRead())){
                configList.forEach(o->{
                    MitsubishiMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    MitsubishiReadConfig config = new MitsubishiReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                    config.setAreaCode(o.getAreaCode());
                    config.setStartAddress(o.getStartAddress());
                    config.setLength(o.getLength());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    MitsubishiMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                configList.forEach(o->{
                    MitsubishiMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
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
        List<LabdatahubMitsubishiConfig> list = labdatahubMitsubishiConfigService.list(new LambdaQueryWrapper<LabdatahubMitsubishiConfig>()
                .eq(LabdatahubMitsubishiConfig::getBelongSn,device.getDeviceSn()));
        if("1".equals(isOpen)){
            list.forEach(o->{
                MitsubishiMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                MitsubishiReadConfig config = new MitsubishiReadConfig();
                config.setDeviceSn(o.getBelongSn());
                config.setCode(o.getCode());
                config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                config.setAreaCode(o.getAreaCode());
                config.setStartAddress(o.getStartAddress());
                config.setLength(o.getLength());
                ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                MitsubishiMessageScheduler.addReadConfig(device.getComponentId(),config);
            });
        }else {
            list.forEach(o->{
                MitsubishiMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
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
            List<LabdatahubMitsubishiConfig> list = labdatahubMitsubishiConfigService.list(new LambdaQueryWrapper<LabdatahubMitsubishiConfig>()
                    .eq(LabdatahubMitsubishiConfig::getBelongSn,device.getDeviceSn()));
            if("1".equals(isOpen)){
                list.forEach(o->{
                    MitsubishiMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    MitsubishiReadConfig config = new MitsubishiReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                    config.setAreaCode(o.getAreaCode());
                    config.setStartAddress(o.getStartAddress());
                    config.setLength(o.getLength());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    MitsubishiMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                list.forEach(o->{
                    MitsubishiMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                });
            }
            // 持久化读取开关状态，保证刷新页面后开关状态与实际读取一致
            device.setModbusRead(isOpen);
            labdatahubDeviceService.updateById(device);
        });
        return AjaxResult.success("操作成功");
    }
}
