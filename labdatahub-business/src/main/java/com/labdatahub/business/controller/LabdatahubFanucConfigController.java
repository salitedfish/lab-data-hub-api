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
import com.labdatahub.business.utils.ProtocolPointModelSync;
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
        AjaxResult result = toAjax(labdatahubFanucConfigService.save(labdatahubFanucConfig));
        // 由AI修改：保存成功后立即同步调度器（新增点位无需重启服务或重开关读取即生效）
        syncConfigToScheduler(labdatahubFanucConfig.getBelongSn(), labdatahubFanucConfig, false);
        // 由AI修改：自动生成对应物模型属性（identifier=code），无需再手动去物模型 tab 建同名属性
        ProtocolPointModelSync.afterAdd(labdatahubFanucConfig.getBelongSn(), labdatahubFanucConfig.getBelongType(), labdatahubFanucConfig.getCode(), labdatahubFanucConfig.getName());
        return result;
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
        // 由AI修改：修改前先取旧配置，标识(code)或归属(belongSn)变更时先移除旧调度，
        // 避免旧 code 的调度以旧 key 残留在调度器，导致同一点位被新旧两套调度同时读取
        String oldCode = null;
        if (labdatahubFanucConfig.getId() != null) {
            LabdatahubFanucConfig oldConfig = labdatahubFanucConfigService.getById(labdatahubFanucConfig.getId());
            if (oldConfig != null && StringUtils.isNotBlank(oldConfig.getBelongSn())) {
                oldCode = oldConfig.getCode();
                LabdatahubDevice oldDevice = labdatahubDeviceService.getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                        .eq(LabdatahubDevice::getDeviceSn, oldConfig.getBelongSn()), false);
                if (oldDevice != null && oldDevice.getComponentId() != null) {
                    FanucFocasMessageScheduler.removeReadConfig(oldDevice.getComponentId(), oldDevice.getDeviceSn(), oldConfig.getCode());
                }
            }
        }
        AjaxResult result = toAjax(labdatahubFanucConfigService.updateById(labdatahubFanucConfig));
        // 由AI修改：修改成功后立即同步调度器（改地址/间隔无需重启服务即生效）
        syncConfigToScheduler(labdatahubFanucConfig.getBelongSn(), labdatahubFanucConfig, false);
        // 由AI修改：点位标识(code)变更时同步修改对应物模型 identifier/name
        ProtocolPointModelSync.afterEdit(labdatahubFanucConfig.getBelongSn(), labdatahubFanucConfig.getBelongType(), oldCode, labdatahubFanucConfig.getCode(), labdatahubFanucConfig.getName());
        return result;
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
        // 参数1：mode/exeprgname 无需参数（mode 读 ODBST.aut、主程序名读 cnc_exeprgname）；
        // count 的 param1 允许 0（0=总加工数 1=稼働程序加工数 2=特定加工数）；其余类型必须 >0
        if (!"mode".equals(readType) && !"exeprgname".equals(readType)) {
            Integer param1 = config.getParam1();
            if (param1 == null || ("count".equals(readType) ? param1 < 0 : param1 <= 0)) {
                return "count".equals(readType) ? "参数1必须大于等于0" : "参数1必须大于0";
            }
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
        // 由AI修改：删除前先取出配置，删除后逐个同步移除调度器，避免停服前残留调度
        List<LabdatahubFanucConfig> configList = labdatahubFanucConfigService.listByIds(Arrays.asList(ids));
        AjaxResult result = toAjax(labdatahubFanucConfigService.removeBatchByIds(Arrays.asList(ids)));
        for (LabdatahubFanucConfig config : configList) {
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
    private void syncConfigToScheduler(String belongSn, LabdatahubFanucConfig config, boolean removeOnly)
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
        FanucFocasMessageScheduler.removeReadConfig(device.getComponentId(), device.getDeviceSn(), config.getCode());
        if (removeOnly) {
            return;
        }
        FanucFocasReadConfig readConfig = new FanucFocasReadConfig();
        readConfig.setDeviceSn(config.getBelongSn());
        readConfig.setCode(config.getCode());
        readConfig.setDelayTime(config.getDelayTime() == null ? 0 : config.getDelayTime().intValue());
        readConfig.setIntervalTime(config.getIntervalTime() == null ? 1 : config.getIntervalTime().intValue());
        readConfig.setReadType(config.getReadType());
        readConfig.setParam1(config.getParam1());
        readConfig.setParam2(config.getParam2());
        ParseMetaUtils.applyTo(readConfig, device.getDeviceSn(), device.getProductSn(), config.getCode());
        FanucFocasMessageScheduler.addReadConfig(device.getComponentId(), readConfig);
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
                config.setName(config.getName());
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
        if (device == null) {
            return AjaxResult.error("设备不存在：" + deviceSn);
        }
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
