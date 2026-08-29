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
        // X/Y 输入/输出继电器地址为八进制：十进制写法含 8/9 即为非法八进制数字，配置期拦截（读取期 MitsubishiDataReader 也会抛）
        if ((config.getAreaCode() == 0x9C || config.getAreaCode() == 0x9D)
                && Integer.toString(config.getStartAddress()).matches(".*[89].*")) {
            return AjaxResult.error("X/Y 软元件地址为八进制（仅允许数字 0-7），当前起始地址含非法数字：" + config.getStartAddress());
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
        // 协议帧模式：为空默认 3E；非空仅允许 1E/3E
        if (config.getProtocolMode() != null && !"1E".equals(config.getProtocolMode()) && !"3E".equals(config.getProtocolMode())) {
            return AjaxResult.error("协议帧模式 protocolMode 只能是 1E(MC1E) / 3E(MC3E)");
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
        AjaxResult result = toAjax(labdatahubMitsubishiConfigService.save(labdatahubMitsubishiConfig));
        // 由AI修改：保存成功后立即同步调度器（新增点位无需重启服务或重开关读取即生效）
        syncConfigToScheduler(labdatahubMitsubishiConfig.getBelongSn(), labdatahubMitsubishiConfig, false);
        return result;
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
        // 由AI修改：修改前先取旧配置，标识(code)或归属(belongSn)变更时先移除旧调度，
        // 避免旧 code 的调度以旧 key 残留在调度器，导致同一点位被新旧两套调度同时读取
        if (labdatahubMitsubishiConfig.getId() != null) {
            LabdatahubMitsubishiConfig oldConfig = labdatahubMitsubishiConfigService.getById(labdatahubMitsubishiConfig.getId());
            if (oldConfig != null && StringUtils.isNotBlank(oldConfig.getBelongSn())) {
                LabdatahubDevice oldDevice = labdatahubDeviceService.getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                        .eq(LabdatahubDevice::getDeviceSn, oldConfig.getBelongSn()), false);
                if (oldDevice != null && oldDevice.getComponentId() != null) {
                    MitsubishiMessageScheduler.removeReadConfig(oldDevice.getComponentId(), oldDevice.getDeviceSn(), oldConfig.getCode());
                }
            }
        }
        AjaxResult result = toAjax(labdatahubMitsubishiConfigService.updateById(labdatahubMitsubishiConfig));
        // 由AI修改：修改成功后立即同步调度器（改地址/间隔无需重启服务即生效）
        syncConfigToScheduler(labdatahubMitsubishiConfig.getBelongSn(), labdatahubMitsubishiConfig, false);
        return result;
    }

    /**
     * 删除三菱MC协议读取配置
     */
    @Log(title = "三菱MC协议读取配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        // 由AI修改：删除前先取出配置，删除后逐个同步移除调度器，避免停服前残留调度
        List<LabdatahubMitsubishiConfig> configList = labdatahubMitsubishiConfigService.listByIds(Arrays.asList(ids));
        AjaxResult result = toAjax(labdatahubMitsubishiConfigService.removeBatchByIds(Arrays.asList(ids)));
        for (LabdatahubMitsubishiConfig config : configList) {
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
    private void syncConfigToScheduler(String belongSn, LabdatahubMitsubishiConfig config, boolean removeOnly)
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
        MitsubishiMessageScheduler.removeReadConfig(device.getComponentId(), device.getDeviceSn(), config.getCode());
        if (removeOnly) {
            return;
        }
        MitsubishiReadConfig readConfig = new MitsubishiReadConfig();
        readConfig.setDeviceSn(config.getBelongSn());
        readConfig.setCode(config.getCode());
        readConfig.setDelayTime(config.getDelayTime() == null ? 0 : config.getDelayTime().intValue());
        readConfig.setIntervalTime(config.getIntervalTime() == null ? 1 : config.getIntervalTime().intValue());
        readConfig.setAreaCode(config.getAreaCode());
        readConfig.setStartAddress(config.getStartAddress());
        readConfig.setLength(config.getLength());
        readConfig.setProtocolMode(config.getProtocolMode());
        ParseMetaUtils.applyTo(readConfig, device.getDeviceSn(), device.getProductSn(), config.getCode());
        MitsubishiMessageScheduler.addReadConfig(device.getComponentId(), readConfig);
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
                    config.setProtocolMode(o.getProtocolMode());
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
                    config.setProtocolMode(o.getProtocolMode());
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
