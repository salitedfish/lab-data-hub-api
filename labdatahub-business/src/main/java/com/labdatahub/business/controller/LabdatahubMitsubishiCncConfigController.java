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
import com.labdatahub.business.domain.LabdatahubMitsubishiCncConfig;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubMitsubishiCncConfigService;
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
import com.labdatahub.component.mitsubishi_cnc_tcp.MitsubishiCncMessageScheduler;
import com.labdatahub.component.mitsubishi_cnc_tcp.MitsubishiCncReadConfig;

/**
 *
* @ClassName: LabdatahubMitsubishiCncConfigController
* @Description: 三菱CNC TCP(MOCHA)协议读取配置Controller
* @author xwb
* @date 2026年8月29日
 */
@RestController
@RequestMapping("/business/mitsubishiCncTcp")
public class LabdatahubMitsubishiCncConfigController extends BaseController
{
    @Autowired
    private ILabdatahubMitsubishiCncConfigService labdatahubMitsubishiCncConfigService;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    /**
     * 查询三菱CNC TCP(MOCHA)协议读取配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubMitsubishiCncConfig labdatahubMitsubishiCncConfig)
    {
        LambdaQueryWrapper<LabdatahubMitsubishiCncConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(LabdatahubMitsubishiCncConfig::getCreateTime);
        if (StringUtils.isNotEmpty(labdatahubMitsubishiCncConfig.getBelongSn())) {
            queryWrapper.eq(LabdatahubMitsubishiCncConfig::getBelongSn, labdatahubMitsubishiCncConfig.getBelongSn());
        }
        if (StringUtils.isNotEmpty(labdatahubMitsubishiCncConfig.getCode())) {
            queryWrapper.like(LabdatahubMitsubishiCncConfig::getCode, labdatahubMitsubishiCncConfig.getCode());
        }
        Page<LabdatahubMitsubishiCncConfig> page = new Page<>(PageUtils.getPageNum(), PageUtils.getPageSize());
        Page<LabdatahubMitsubishiCncConfig> pageList = labdatahubMitsubishiCncConfigService.page(page, queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 获取三菱CNC TCP(MOCHA)协议读取配置详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubMitsubishiCncConfigService.getById(id));
    }

    /**
     * 新增三菱CNC TCP(MOCHA)协议读取配置
     */
    @Log(title = "三菱CNC协议读取配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubMitsubishiCncConfig labdatahubMitsubishiCncConfig)
    {
        String error = validateConfig(labdatahubMitsubishiCncConfig);
        if (StringUtils.isNotEmpty(error)) {
            return AjaxResult.error(error);
        }
        labdatahubMitsubishiCncConfig.setCreateTime(new Date());
        AjaxResult result = toAjax(labdatahubMitsubishiCncConfigService.save(labdatahubMitsubishiCncConfig));
        // 由AI修改：保存成功后立即同步调度器（新增点位无需重启服务或重开关读取即生效）
        syncConfigToScheduler(labdatahubMitsubishiCncConfig.getBelongSn(), labdatahubMitsubishiCncConfig, false);
        // 由AI修改：自动生成对应物模型属性（identifier=code），无需再手动去物模型 tab 建同名属性
        ProtocolPointModelSync.afterAdd(labdatahubMitsubishiCncConfig.getBelongSn(), labdatahubMitsubishiCncConfig.getBelongType(), labdatahubMitsubishiCncConfig.getCode(), labdatahubMitsubishiCncConfig.getName());
        return result;
    }

    /**
     * 修改三菱CNC TCP(MOCHA)协议读取配置
     */
    @Log(title = "三菱CNC协议读取配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubMitsubishiCncConfig labdatahubMitsubishiCncConfig)
    {
        String error = validateConfig(labdatahubMitsubishiCncConfig);
        if (StringUtils.isNotEmpty(error)) {
            return AjaxResult.error(error);
        }
        // 由AI修改：修改前先取旧配置，标识(code)或归属(belongSn)变更时先移除旧调度，
        // 避免旧 code 的调度以旧 key 残留在调度器，导致同一点位被新旧两套调度同时读取
        String oldCode = null;
        if (labdatahubMitsubishiCncConfig.getId() != null) {
            LabdatahubMitsubishiCncConfig oldConfig = labdatahubMitsubishiCncConfigService.getById(labdatahubMitsubishiCncConfig.getId());
            if (oldConfig != null && StringUtils.isNotBlank(oldConfig.getBelongSn())) {
                oldCode = oldConfig.getCode();
                LabdatahubDevice oldDevice = labdatahubDeviceService.getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                        .eq(LabdatahubDevice::getDeviceSn, oldConfig.getBelongSn()), false);
                if (oldDevice != null && oldDevice.getComponentId() != null) {
                    MitsubishiCncMessageScheduler.removeReadConfig(oldDevice.getComponentId(), oldDevice.getDeviceSn(), oldConfig.getCode());
                }
            }
        }
        AjaxResult result = toAjax(labdatahubMitsubishiCncConfigService.updateById(labdatahubMitsubishiCncConfig));
        // 由AI修改：修改成功后立即同步调度器（改地址/间隔无需重启服务即生效）
        syncConfigToScheduler(labdatahubMitsubishiCncConfig.getBelongSn(), labdatahubMitsubishiCncConfig, false);
        // 由AI修改：点位标识(code)变更时同步修改对应物模型 identifier/name
        ProtocolPointModelSync.afterEdit(labdatahubMitsubishiCncConfig.getBelongSn(), labdatahubMitsubishiCncConfig.getBelongType(), oldCode, labdatahubMitsubishiCncConfig.getCode(), labdatahubMitsubishiCncConfig.getName());
        return result;
    }

    /**
     * 校验三菱CNC配置必填项与地址合法性（避免脏数据导致调度空指针或静默采空）
     * 点位按采集项类型校验：轴类点位（mechpos/currpos/remapos/cu/sp）必须配置轴号（1-6）；
     * 其余非轴点位（al/fre/pn/spn/cc/sl1/ss1/tn/stn/po/opt/cut/ct/sv/fv/st/pst/opm/axc）无需轴号
     * @param config 待校验配置
     * @return 空串表示通过，否则为错误提示
     */
    private String validateConfig(LabdatahubMitsubishiCncConfig config)
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
        // 轴类点位必须配置轴号 1-6（树根轴循环 SystemNum=轴序）
        if (isAxisPoint(readType)) {
            if (config.getAxisNo() == null || config.getAxisNo() < 1 || config.getAxisNo() > 6) {
                return "轴类点位轴号必须在1-6之间";
            }
        }
        if (config.getIntervalTime() == null || config.getIntervalTime() <= 0) {
            return "读取间隔必须大于0";
        }
        return "";
    }

    /**
     * 判断采集项是否为轴类点位（需要轴号）
     * @param readType 采集项类型（小写）
     * @return true-轴类点位
     */
    private boolean isAxisPoint(String readType)
    {
        return "mechpos".equals(readType) || "currpos".equals(readType) || "remapos".equals(readType)
                || "cu".equals(readType) || "sp".equals(readType);
    }

    /**
     * 删除三菱CNC TCP(MOCHA)协议读取配置
     */
    @Log(title = "三菱CNC协议读取配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        // 由AI修改：删除前先取出配置，删除后逐个同步移除调度器，避免停服前残留调度
        List<LabdatahubMitsubishiCncConfig> configList = labdatahubMitsubishiCncConfigService.listByIds(Arrays.asList(ids));
        AjaxResult result = toAjax(labdatahubMitsubishiCncConfigService.removeBatchByIds(Arrays.asList(ids)));
        for (LabdatahubMitsubishiCncConfig config : configList) {
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
    private void syncConfigToScheduler(String belongSn, LabdatahubMitsubishiCncConfig config, boolean removeOnly)
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
        MitsubishiCncMessageScheduler.removeReadConfig(device.getComponentId(), device.getDeviceSn(), config.getCode());
        if (removeOnly) {
            return;
        }
        MitsubishiCncReadConfig readConfig = new MitsubishiCncReadConfig();
        readConfig.setDeviceSn(config.getBelongSn());
        readConfig.setCode(config.getCode());
        readConfig.setDelayTime(config.getDelayTime() == null ? 0 : config.getDelayTime().intValue());
        readConfig.setIntervalTime(config.getIntervalTime() == null ? 1 : config.getIntervalTime().intValue());
        readConfig.setReadType(config.getReadType());
        readConfig.setAxisNo(config.getAxisNo());
        ParseMetaUtils.applyTo(readConfig, device.getDeviceSn(), device.getProductSn(), config.getCode());
        MitsubishiCncMessageScheduler.addReadConfig(device.getComponentId(), readConfig);
    }

    /**
     * 同步到所有设备
     */
    @PostMapping("/syncConfigToDevice")
    public AjaxResult syncConfigToDevice(@RequestParam String productSn){
        List<LabdatahubMitsubishiCncConfig> configList = labdatahubMitsubishiCncConfigService.list(new LambdaQueryWrapper<LabdatahubMitsubishiCncConfig>()
                .eq(LabdatahubMitsubishiCncConfig::getBelongSn,productSn));
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        deviceList.forEach(device->{
            //删除设备所有规则
            labdatahubMitsubishiCncConfigService.remove(new LambdaUpdateWrapper<LabdatahubMitsubishiCncConfig>()
                    .eq(LabdatahubMitsubishiCncConfig::getBelongSn,device.getDeviceSn())
                    .eq(LabdatahubMitsubishiCncConfig::getBelongType,"1"));
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
                config.setAxisNo(config.getAxisNo());
            });
            labdatahubMitsubishiCncConfigService.saveBatch(configList);
            if("1".equals(device.getModbusRead())){
                configList.forEach(o->{
                    MitsubishiCncMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    MitsubishiCncReadConfig config = new MitsubishiCncReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                    config.setReadType(o.getReadType());
                    config.setAxisNo(o.getAxisNo());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    MitsubishiCncMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                configList.forEach(o->{
                    MitsubishiCncMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
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
        List<LabdatahubMitsubishiCncConfig> list = labdatahubMitsubishiCncConfigService.list(new LambdaQueryWrapper<LabdatahubMitsubishiCncConfig>()
                .eq(LabdatahubMitsubishiCncConfig::getBelongSn,device.getDeviceSn()));
        if("1".equals(isOpen)){
            list.forEach(o->{
                MitsubishiCncMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                MitsubishiCncReadConfig config = new MitsubishiCncReadConfig();
                config.setDeviceSn(o.getBelongSn());
                config.setCode(o.getCode());
                config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                config.setReadType(o.getReadType());
                config.setAxisNo(o.getAxisNo());
                ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                MitsubishiCncMessageScheduler.addReadConfig(device.getComponentId(),config);
            });
        }else {
            list.forEach(o->{
                MitsubishiCncMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
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
            List<LabdatahubMitsubishiCncConfig> list = labdatahubMitsubishiCncConfigService.list(new LambdaQueryWrapper<LabdatahubMitsubishiCncConfig>()
                    .eq(LabdatahubMitsubishiCncConfig::getBelongSn,device.getDeviceSn()));
            if("1".equals(isOpen)){
                list.forEach(o->{
                    MitsubishiCncMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    MitsubishiCncReadConfig config = new MitsubishiCncReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                    config.setReadType(o.getReadType());
                    config.setAxisNo(o.getAxisNo());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    MitsubishiCncMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                list.forEach(o->{
                    MitsubishiCncMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                });
            }
            // 持久化读取开关状态，保证刷新页面后开关状态与实际读取一致
            device.setModbusRead(isOpen);
            labdatahubDeviceService.updateById(device);
        });
        return AjaxResult.success("操作成功");
    }
}
