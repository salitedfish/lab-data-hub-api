//由AI修改
package com.labdatahub.business.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.domain.LabdatahubProperties;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubPropertiesService;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.spring.SpringUtils;

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
        property.setDataType("int");
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
