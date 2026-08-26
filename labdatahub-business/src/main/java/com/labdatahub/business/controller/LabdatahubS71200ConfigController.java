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
import com.labdatahub.business.domain.LabdatahubS71200Config;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubS71200ConfigService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.business.utils.ParseMetaUtils;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.core.page.TableDataInfo;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.component.s7_tcp.S7MessageScheduler;
import com.labdatahub.component.s7_tcp.S7ReadConfig;

/**
 * 
* @ClassName: LabdatahubS71200ConfigController  
* @Description: s71200协议读取配置Controller
* @author xwb  
* @date 2026年3月24日
 */
@RestController
@RequestMapping("/business/s71200")
public class LabdatahubS71200ConfigController extends BaseController
{
    @Autowired
    private ILabdatahubS71200ConfigService labdatahubS71200ConfigService;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    /**
     * 查询s71200协议读取配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubS71200Config labdatahubS71200Config)
    {
        LambdaQueryWrapper<LabdatahubS71200Config> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(LabdatahubS71200Config::getCreateTime);
        if (StringUtils.isNotEmpty(labdatahubS71200Config.getBelongSn())) {
            queryWrapper.eq(LabdatahubS71200Config::getBelongSn, labdatahubS71200Config.getBelongSn());
        }
        if (StringUtils.isNotEmpty(labdatahubS71200Config.getCode())) {
            queryWrapper.like(LabdatahubS71200Config::getCode, labdatahubS71200Config.getCode());
        }
        Page<LabdatahubS71200Config> page = new Page<>(PageUtils.getPageNum(), PageUtils.getPageSize());
        Page<LabdatahubS71200Config> pageList = labdatahubS71200ConfigService.page(page, queryWrapper);
        return getDataTable(pageList);
    }

//    /**
//     * 导出s71200协议读取配置列表
//     */
//    @Log(title = "s71200协议读取配置", businessType = BusinessType.EXPORT)
//    @PostMapping("/export")
//    public void export(HttpServletResponse response, LabdatahubS71200Config labdatahubS71200Config)
//    {
//        List<LabdatahubS71200Config> list = labdatahubS71200ConfigService.selectLabdatahubS71200ConfigList(labdatahubS71200Config);
//        ExcelUtil<LabdatahubS71200Config> util = new ExcelUtil<LabdatahubS71200Config>(LabdatahubS71200Config.class);
//        util.exportExcel(response, list, "s71200协议读取配置数据");
//    }

    /**
     * 获取s71200协议读取配置详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubS71200ConfigService.getById(id));
    }

    /**
     * 校验 S7 读取配置字段
     *
     * @return null-校验通过，否则返回错误信息
     */
    private AjaxResult checkConfig(LabdatahubS71200Config config) {
        if (config.getDbNumber() == null || config.getDbNumber() <= 0) {
            return AjaxResult.error("DB块号 dbNumber 必须为正整数");
        }
        String blockType = config.getBlockType();
        if (blockType == null || StringUtils.isEmpty(blockType.trim())) {
            config.setBlockType("DBW");
            blockType = "DBW";
        }
        blockType = blockType.trim();
        if (!"DBW".equals(blockType) && !"DBX".equals(blockType)
                && !"DBD".equals(blockType) && !"DBB".equals(blockType)) {
            return AjaxResult.error("块类型 blockType 只能是 DBW(16位整型)/DBX(位)/DBD(32位浮点或整型)/DBB(字符串)");
        }
        config.setBlockType(blockType);
        if (config.getAreaType() == null || StringUtils.isEmpty(config.getAreaType().trim())) {
            config.setAreaType("DB");
        } else {
            String areaType = config.getAreaType().trim();
            if (!"DB".equals(areaType) && !"M".equals(areaType)
                    && !"I".equals(areaType) && !"Q".equals(areaType)) {
                return AjaxResult.error("区类型 areaType 只能是 DB数据块/M标志位/I输入区/Q输出区");
            }
            config.setAreaType(areaType);
        }
        if (config.getStartAddress() == null || config.getStartAddress() < 0) {
            return AjaxResult.error("起始地址 startAddress 不能为负数（字节偏移）");
        }
        if (config.getIntervalTime() == null || config.getIntervalTime() <= 0) {
            return AjaxResult.error("读取间隔 intervalTime 必须为正整数（单位：秒）");
        }
        if ("DBX".equals(blockType) && (config.getBitOffset() == null || config.getBitOffset() < 0 || config.getBitOffset() > 7)) {
            return AjaxResult.error("DBX 位读取的偏移量 bitOffset 必须在 0-7 之间");
        }
        if ("DBB".equals(blockType) && (config.getLength() == null || config.getLength() <= 0)) {
            return AjaxResult.error("DBB 字符串读取的长度 length 必须为正整数（字节）");
        }
        return null;
    }

    /**
     * 新增s71200协议读取配置
     */
    @Log(title = "s71200协议读取配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubS71200Config labdatahubS71200Config)
    {
        AjaxResult check = checkConfig(labdatahubS71200Config);
        if (check != null) {
            return check;
        }
        labdatahubS71200Config.setCreateTime(new Date());
        AjaxResult result = toAjax(labdatahubS71200ConfigService.save(labdatahubS71200Config));
        // 由AI修改：保存成功后立即同步调度器（新增点位无需重启服务或重开关读取即生效）
        syncConfigToScheduler(labdatahubS71200Config.getBelongSn(), labdatahubS71200Config, false);
        return result;
    }

    /**
     * 修改s71200协议读取配置
     */
    @Log(title = "s71200协议读取配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubS71200Config labdatahubS71200Config)
    {
        AjaxResult check = checkConfig(labdatahubS71200Config);
        if (check != null) {
            return check;
        }
        // 由AI修改：修改前先取旧配置，标识(code)或归属(belongSn)变更时先移除旧调度，
        // 避免旧 code 的调度以旧 key 残留在调度器，导致同一点位被新旧两套调度同时读取
        if (labdatahubS71200Config.getId() != null) {
            LabdatahubS71200Config oldConfig = labdatahubS71200ConfigService.getById(labdatahubS71200Config.getId());
            if (oldConfig != null && StringUtils.isNotBlank(oldConfig.getBelongSn())) {
                LabdatahubDevice oldDevice = labdatahubDeviceService.getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                        .eq(LabdatahubDevice::getDeviceSn, oldConfig.getBelongSn()), false);
                if (oldDevice != null && oldDevice.getComponentId() != null) {
                    S7MessageScheduler.removeReadConfig(oldDevice.getComponentId(), oldDevice.getDeviceSn(), oldConfig.getCode());
                }
            }
        }
        AjaxResult result = toAjax(labdatahubS71200ConfigService.updateById(labdatahubS71200Config));
        // 由AI修改：修改成功后立即同步调度器（改地址/间隔无需重启服务即生效）
        syncConfigToScheduler(labdatahubS71200Config.getBelongSn(), labdatahubS71200Config, false);
        return result;
    }

    /**
     * 删除s71200协议读取配置
     */
    @Log(title = "s71200协议读取配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        // 由AI修改：删除前先取出配置，删除后逐个同步移除调度器，避免停服前残留调度
        List<LabdatahubS71200Config> configList = labdatahubS71200ConfigService.listByIds(Arrays.asList(ids));
        AjaxResult result = toAjax(labdatahubS71200ConfigService.removeBatchByIds(Arrays.asList(ids)));
        for (LabdatahubS71200Config config : configList) {
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
    private void syncConfigToScheduler(String belongSn, LabdatahubS71200Config config, boolean removeOnly)
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
        S7MessageScheduler.removeReadConfig(device.getComponentId(), device.getDeviceSn(), config.getCode());
        if (removeOnly) {
            return;
        }
        S7ReadConfig readConfig = new S7ReadConfig();
        readConfig.setDeviceSn(config.getBelongSn());
        readConfig.setCode(config.getCode());
        readConfig.setDelayTime(config.getDelayTime() == null ? 0 : config.getDelayTime().intValue());
        readConfig.setIntervalTime(config.getIntervalTime() == null ? 1 : config.getIntervalTime().intValue());
        readConfig.setDbNumber(config.getDbNumber());
        readConfig.setBlockType(config.getBlockType());
        readConfig.setAreaType(config.getAreaType());
        readConfig.setBitOffset(config.getBitOffset());
        readConfig.setStartAddress(config.getStartAddress());
        readConfig.setLength(config.getLength());
        ParseMetaUtils.applyTo(readConfig, device.getDeviceSn(), device.getProductSn(), config.getCode());
        S7MessageScheduler.addReadConfig(device.getComponentId(), readConfig);
    }

    /**
     * 同步到所有设备
     */
    @PostMapping("/syncConfigToDevice")
    public AjaxResult syncConfigToDevice(@RequestParam String productSn){
        List<LabdatahubS71200Config> s71200ConfigList = labdatahubS71200ConfigService.list(new LambdaQueryWrapper<LabdatahubS71200Config>()
                .eq(LabdatahubS71200Config::getBelongSn,productSn));
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        deviceList.forEach(device->{
            //删除设备所有规则
            labdatahubS71200ConfigService.remove(new LambdaUpdateWrapper<LabdatahubS71200Config>()
                    .eq(LabdatahubS71200Config::getBelongSn,device.getDeviceSn())
                    .eq(LabdatahubS71200Config::getBelongType,"1"));
            //添加新的规则
            s71200ConfigList.forEach(config->{
                config.setId(IdWorker.getIdStr());
                config.setBelongSn(device.getDeviceSn());
                config.setBelongType("1");
                config.setCreateTime(new Date());
                config.setCode(config.getCode());
                config.setDelayTime(config.getDelayTime());
                config.setIntervalTime(config.getIntervalTime());
                config.setDbNumber(config.getDbNumber());
                config.setBlockType(config.getBlockType());
                config.setBitOffset(config.getBitOffset());
                config.setStartAddress(config.getStartAddress());
                config.setLength(config.getLength());
            });
            labdatahubS71200ConfigService.saveBatch(s71200ConfigList);
            if("1".equals(device.getModbusRead())){
                s71200ConfigList.forEach(o->{
                    S7MessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    S7ReadConfig config = new S7ReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                    config.setDbNumber(o.getDbNumber());
                    config.setBlockType(o.getBlockType());
                    config.setAreaType(o.getAreaType());
                    config.setBitOffset(o.getBitOffset());
                    config.setStartAddress(o.getStartAddress());
                    config.setLength(o.getLength());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    S7MessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                s71200ConfigList.forEach(o->{
                    S7MessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
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
        List<LabdatahubS71200Config> list = labdatahubS71200ConfigService.list(new LambdaQueryWrapper<LabdatahubS71200Config>()
                .eq(LabdatahubS71200Config::getBelongSn,device.getDeviceSn()));
        if("1".equals(isOpen)){
            list.forEach(o->{
                S7MessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                S7ReadConfig config = new S7ReadConfig();
                config.setDeviceSn(o.getBelongSn());
                config.setCode(o.getCode());
                config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                config.setDbNumber(o.getDbNumber());
                config.setBlockType(o.getBlockType());
                config.setAreaType(o.getAreaType());
                config.setBitOffset(o.getBitOffset());
                config.setStartAddress(o.getStartAddress());
                config.setLength(o.getLength());
                ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                S7MessageScheduler.addReadConfig(device.getComponentId(),config);
            });
        }else {
            list.forEach(o->{
                S7MessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
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
            List<LabdatahubS71200Config> list = labdatahubS71200ConfigService.list(new LambdaQueryWrapper<LabdatahubS71200Config>()
                    .eq(LabdatahubS71200Config::getBelongSn,device.getDeviceSn()));
            if("1".equals(isOpen)){
                list.forEach(o->{
                    S7MessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    S7ReadConfig config = new S7ReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                    config.setDbNumber(o.getDbNumber());
                    config.setBlockType(o.getBlockType());
                    config.setAreaType(o.getAreaType());
                    config.setBitOffset(o.getBitOffset());
                    config.setStartAddress(o.getStartAddress());
                    config.setLength(o.getLength());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    S7MessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            }else {
                list.forEach(o->{
                    S7MessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                });
            }
            // 持久化读取开关状态，保证刷新页面后开关状态与实际读取一致
            device.setModbusRead(isOpen);
            labdatahubDeviceService.updateById(device);
        });
        return AjaxResult.success("操作成功");
    }
}
