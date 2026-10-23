//由AI修改
package com.labdatahub.business.utils;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.labdatahub.business.domain.LabdatahubBrotherConfig;
import com.labdatahub.business.domain.LabdatahubDbConfig;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.domain.LabdatahubFanucConfig;
import com.labdatahub.business.domain.LabdatahubMitsubishiCncConfig;
import com.labdatahub.business.domain.LabdatahubMitsubishiMc3eConfig;
import com.labdatahub.business.domain.LabdatahubModbusConfig;
import com.labdatahub.business.domain.LabdatahubOmronFinsConfig;
import com.labdatahub.business.domain.LabdatahubProperties;
import com.labdatahub.business.domain.LabdatahubS71200Config;
import com.labdatahub.business.service.ILabdatahubBrotherConfigService;
import com.labdatahub.business.service.ILabdatahubDbConfigService;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubFanucConfigService;
import com.labdatahub.business.service.ILabdatahubMitsubishiCncConfigService;
import com.labdatahub.business.service.ILabdatahubMitsubishiMc3eConfigService;
import com.labdatahub.business.service.ILabdatahubModbusConfigService;
import com.labdatahub.business.service.ILabdatahubOmronFinsConfigService;
import com.labdatahub.business.service.ILabdatahubPropertiesService;
import com.labdatahub.business.service.ILabdatahubS71200ConfigService;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.brother_tcp.BrotherTcpMessageScheduler;
import com.labdatahub.component.db.DatabaseMessageScheduler;
import com.labdatahub.component.db.DatabaseReadConfig;
import com.labdatahub.component.fanuc_focas.FanucFocasMessageScheduler;
import com.labdatahub.component.fins_tcp.FinsMessageScheduler;
import com.labdatahub.component.message.MessageCache;
import com.labdatahub.component.mitsubishi_cnc_tcp.MitsubishiCncMessageScheduler;
import com.labdatahub.component.mitsubishi_mc3e_tcp.MitsubishiMc3eMessageScheduler;
import com.labdatahub.component.modbus_tcp.ModbusMessageScheduler;
import com.labdatahub.component.s7_tcp.S7MessageScheduler;

/**
 * 协议点位 ↔ 物模型自动同步工具类（单向：协议点位增删改 → 自动同步对应物模型属性）
 * 关联键：协议点位 code = 物模型 identifier（ParseMetaUtils 用 code 查物模型解析元数据）
 * 供各协议 ConfigController 的 add/edit/remove 复用
 */
public class ProtocolPointModelSync {

    private ProtocolPointModelSync() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 新增协议点位后：该归属下无 identifier=code 属性时，自动创建默认物模型属性（dataType=int，可微调）
     *
     * @param belongSn   归属（产品SN或设备SN）
     * @param belongType 归属类型 0-产品 1-设备
     * @param code       协议点位标识（= 物模型 identifier）
     * @param name       点位名称（物模型属性名用，null/空则用 code 标识）
     */
    public static void afterAdd(String belongSn, String belongType, String code, String name) {
        afterAdd(belongSn, belongType, code, name, "int");
    }

    /**
     * 新增协议点位后自动建物模型属性，并指定默认数据类型
     *
     * <p>字内位点位（如三菱 D150.0）读回来是 0/1，属性应建成 bool；
     * 不传类型的旧调用方仍按 int 建，行为不变。
     */
    public static void afterAdd(String belongSn, String belongType, String code, String name, String dataType) {
        if (StringUtils.isBlank(belongSn) || StringUtils.isBlank(code)) {
            return;
        }
        ILabdatahubPropertiesService service = SpringUtils.getBean(ILabdatahubPropertiesService.class);
        long exist = service.count(new LambdaQueryWrapper<LabdatahubProperties>()
                .eq(LabdatahubProperties::getBelongSn, belongSn)
                .eq(LabdatahubProperties::getIdentifier, code));
        if (exist > 0) {
            return;
        }
        LabdatahubProperties property = new LabdatahubProperties();
        property.setBelongSn(belongSn);
        property.setBelongType(belongType);
        property.setIdentifier(code);
        property.setName(StringUtils.isBlank(name) ? code : name);
        property.setDataType(StringUtils.isBlank(dataType) ? "int" : dataType);
        property.setParentId("0");
        property.setSortNum(0L);
        // 设备级属性标设备自定义（可改），产品级属性默认值
        property.setFromType("1".equals(belongType) ? "1" : "0");
        service.save(property);
        refreshCache(belongSn, belongType);
    }

    /**
     * 修改协议点位后：同步对应物模型 identifier/name
     * 标识(code)变化时 identifier/name 一起改；标识未变但点位有名称时仅同步 name（编辑保存后物模型名跟随点位名称）
     *
     * @param belongSn   归属（产品SN或设备SN，取变更后的值）
     * @param belongType 归属类型 0-产品 1-设备
     * @param oldCode    变更前的点位标识
     * @param newCode    变更后的点位标识
     * @param name       点位名称（物模型属性名用，null/空则用 code 标识）
     */
    public static void afterEdit(String belongSn, String belongType, String oldCode, String newCode, String name) {
        if (StringUtils.isBlank(belongSn) || StringUtils.isBlank(newCode)) {
            return;
        }
        ILabdatahubPropertiesService service = SpringUtils.getBean(ILabdatahubPropertiesService.class);
        LambdaUpdateWrapper<LabdatahubProperties> wrapper = new LambdaUpdateWrapper<LabdatahubProperties>()
                .eq(LabdatahubProperties::getBelongSn, belongSn);
        if (oldCode != null && !oldCode.equals(newCode)) {
            // 标识变更：identifier/name 一起改（name 用点位名称，空用新标识）
            String finalName = StringUtils.isBlank(name) ? newCode : name;
            wrapper.eq(LabdatahubProperties::getIdentifier, oldCode)
                    .set(LabdatahubProperties::getIdentifier, newCode)
                    .set(LabdatahubProperties::getName, finalName);
        } else if (StringUtils.isNotBlank(name)) {
            // 标识未变且点位有名称：仅同步 name（不误伤点位无名称时用户手动改的物模型名）
            wrapper.eq(LabdatahubProperties::getIdentifier, newCode)
                    .set(LabdatahubProperties::getName, name);
        } else {
            // 标识未变且无点位名称：无需更新
            return;
        }
        service.update(wrapper);
        refreshCache(belongSn, belongType);
    }

    /**
     * 删除协议点位后：删除该归属下 identifier=code 的对应物模型属性（点位与属性一一对应）
     *
     * @param belongSn   归属（产品SN或设备SN）
     * @param belongType 归属类型 0-产品 1-设备
     * @param code       被删除的点位标识
     */
    public static void afterRemove(String belongSn, String belongType, String code) {
        if (StringUtils.isBlank(belongSn) || StringUtils.isBlank(code)) {
            return;
        }
        ILabdatahubPropertiesService service = SpringUtils.getBean(ILabdatahubPropertiesService.class);
        service.remove(new LambdaUpdateWrapper<LabdatahubProperties>()
                .eq(LabdatahubProperties::getBelongSn, belongSn)
                .eq(LabdatahubProperties::getIdentifier, code));
        refreshCache(belongSn, belongType);
    }

    /**
     * 物模型属性被删除后的级联处理（2026-10-19）：
     * 物模型 tab 删除一行再保存，本质是删 properties 表；协议点位表（以及调度器）原来完全无感知，
     * 于是被删的点位仍在轮询、仍写 MessageCache，MQTT 全量转发里就永远带着它。
     * 这里按 identifier 反查各协议点位表并一并删除，再取消调度、清最新数据缓存、刷新物模型树。
     * <p>
     * 只处理设备归属：productSn 不是设备时仅删除产品模板上的点位行（设备侧副本由「同步到所有设备」覆盖）。
     *
     * @param belongSn     归属SN（设备SN或产品SN）
     * @param removedCodes 本次从物模型中删掉的标识
     */
    public static void afterPropertiesRemoved(String belongSn, Collection<String> removedCodes) {
        if (StringUtils.isBlank(belongSn) || removedCodes == null || removedCodes.isEmpty()) {
            return;
        }
        ILabdatahubDeviceService deviceService = SpringUtils.getBean(ILabdatahubDeviceService.class);
        LabdatahubDevice device = deviceService.getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getDeviceSn, belongSn), false);
        if (device != null) {
            removeDevicePointConfigs(device, removedCodes);
            // 最新数据缓存里的旧值必须一起清掉：调度取消后队列里可能还有排队消息会把它写回来，
            // 转发侧（DeviceUpListener#buildFullDataPayload）还会按物模型再过滤一道
            MessageCache.removeProperties(device.getDeviceSn(), removedCodes);
            deviceService.cacheDeviceProperties(device.getDeviceSn());
            return;
        }
        // 产品归属：只删产品模板的点位行，不直接动设备
        removeConfigRows(belongSn, removedCodes);
    }

    /**
     * 删除设备在各协议点位表里的对应行，并取消对应轮询调度
     *
     * @param device       设备
     * @param codes        被删除的点位标识
     */
    private static void removeDevicePointConfigs(LabdatahubDevice device, Collection<String> codes) {
        String deviceSn = device.getDeviceSn();
        String componentId = device.getComponentId();
        // db 协议一个设备只有一个调度（code=null，取第一条配置的间隔），删除点位后需整体重建
        ILabdatahubDbConfigService dbConfigService = SpringUtils.getBean(ILabdatahubDbConfigService.class);
        boolean dbPointRemoved = dbConfigService.count(new LambdaQueryWrapper<LabdatahubDbConfig>()
                .eq(LabdatahubDbConfig::getBelongSn, deviceSn)
                .in(LabdatahubDbConfig::getCode, codes)) > 0;

        removeConfigRows(deviceSn, codes);

        if (StringUtils.isEmpty(componentId)) {
            return;
        }
        for (String code : codes) {
            MitsubishiCncMessageScheduler.removeReadConfig(componentId, deviceSn, code);
            MitsubishiMc3eMessageScheduler.removeReadConfig(componentId, deviceSn, code);
            ModbusMessageScheduler.removeReadConfig(componentId, deviceSn, code);
            S7MessageScheduler.removeReadConfig(componentId, deviceSn, code);
            FinsMessageScheduler.removeReadConfig(componentId, deviceSn, code);
            BrotherTcpMessageScheduler.removeReadConfig(componentId, deviceSn, code);
            FanucFocasMessageScheduler.removeReadConfig(componentId, deviceSn, code);
        }
        if (dbPointRemoved) {
            rebuildDatabaseScheduler(device);
        }
    }

    /**
     * 删除各协议点位表中 belongSn + code 命中的行（7 个轮询协议 + db 协议）
     *
     * @param belongSn 归属SN
     * @param codes    点位标识
     */
    private static void removeConfigRows(String belongSn, Collection<String> codes) {
        SpringUtils.getBean(ILabdatahubModbusConfigService.class).remove(new LambdaQueryWrapper<LabdatahubModbusConfig>()
                .eq(LabdatahubModbusConfig::getBelongSn, belongSn).in(LabdatahubModbusConfig::getCode, codes));
        SpringUtils.getBean(ILabdatahubS71200ConfigService.class).remove(new LambdaQueryWrapper<LabdatahubS71200Config>()
                .eq(LabdatahubS71200Config::getBelongSn, belongSn).in(LabdatahubS71200Config::getCode, codes));
        SpringUtils.getBean(ILabdatahubOmronFinsConfigService.class).remove(new LambdaQueryWrapper<LabdatahubOmronFinsConfig>()
                .eq(LabdatahubOmronFinsConfig::getBelongSn, belongSn).in(LabdatahubOmronFinsConfig::getCode, codes));
        SpringUtils.getBean(ILabdatahubBrotherConfigService.class).remove(new LambdaQueryWrapper<LabdatahubBrotherConfig>()
                .eq(LabdatahubBrotherConfig::getBelongSn, belongSn).in(LabdatahubBrotherConfig::getCode, codes));
        SpringUtils.getBean(ILabdatahubFanucConfigService.class).remove(new LambdaQueryWrapper<LabdatahubFanucConfig>()
                .eq(LabdatahubFanucConfig::getBelongSn, belongSn).in(LabdatahubFanucConfig::getCode, codes));
        SpringUtils.getBean(ILabdatahubMitsubishiMc3eConfigService.class).remove(new LambdaQueryWrapper<LabdatahubMitsubishiMc3eConfig>()
                .eq(LabdatahubMitsubishiMc3eConfig::getBelongSn, belongSn).in(LabdatahubMitsubishiMc3eConfig::getCode, codes));
        SpringUtils.getBean(ILabdatahubMitsubishiCncConfigService.class).remove(new LambdaQueryWrapper<LabdatahubMitsubishiCncConfig>()
                .eq(LabdatahubMitsubishiCncConfig::getBelongSn, belongSn).in(LabdatahubMitsubishiCncConfig::getCode, codes));
        SpringUtils.getBean(ILabdatahubDbConfigService.class).remove(new LambdaQueryWrapper<LabdatahubDbConfig>()
                .eq(LabdatahubDbConfig::getBelongSn, belongSn).in(LabdatahubDbConfig::getCode, codes));
    }

    /**
     * db 协议按设备整体重建调度：先移除设备调度，剩余配置非空且读取开关开启时按第一条配置的间隔重新注册
     * （与 LabdatahubDbConfigController#syncConfigToScheduler 的口径一致）
     *
     * @param device 设备
     */
    private static void rebuildDatabaseScheduler(LabdatahubDevice device) {
        String componentId = device.getComponentId();
        String deviceSn = device.getDeviceSn();
        DatabaseMessageScheduler.removeReadConfig(componentId, deviceSn, null);
        if (!"1".equals(device.getModbusRead())) {
            return;
        }
        List<LabdatahubDbConfig> remain = SpringUtils.getBean(ILabdatahubDbConfigService.class).list(
                new LambdaQueryWrapper<LabdatahubDbConfig>()
                        .eq(LabdatahubDbConfig::getBelongSn, deviceSn)
                        .orderByAsc(LabdatahubDbConfig::getCreateTime));
        if (remain.isEmpty()) {
            return;
        }
        LabdatahubDbConfig first = remain.get(0);
        DatabaseReadConfig readConfig = new DatabaseReadConfig();
        readConfig.setDeviceSn(deviceSn);
        readConfig.setDelayTime(first.getDelayTime() == null ? 0 : first.getDelayTime().intValue());
        readConfig.setIntervalTime(first.getIntervalTime() == null ? 1 : first.getIntervalTime().intValue());
        DatabaseMessageScheduler.addReadConfig(componentId, readConfig);
    }

    /**
     * 刷新物模型属性树缓存（belongSn 是设备才刷新；产品点位 belongSn 是产品SN，不需要刷设备缓存）
     * 设备详情页手动配点位 belongType 实际为 "0"（前端 Config 组件硬编码），故不能按 belongType 判断，须查设备表确认
     */
    private static void refreshCache(String belongSn, String belongType) {
        if (StringUtils.isBlank(belongSn)) {
            return;
        }
        try {
            ILabdatahubDeviceService deviceService = SpringUtils.getBean(ILabdatahubDeviceService.class);
            long deviceCount = deviceService.count(new LambdaQueryWrapper<LabdatahubDevice>()
                    .eq(LabdatahubDevice::getDeviceSn, belongSn));
            if (deviceCount == 0) {
                // 产品级点位，不刷新设备缓存
                return;
            }
            deviceService.cacheDeviceProperties(belongSn);
        } catch (Exception e) {
            // 缓存刷新失败不影响点位增删改结果，忽略
        }
    }
}
