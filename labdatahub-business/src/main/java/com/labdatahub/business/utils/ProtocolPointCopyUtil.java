//由AI修改
package com.labdatahub.business.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.IService;
import com.labdatahub.business.domain.LabdatahubFunction;
import com.labdatahub.business.domain.LabdatahubProperties;
import com.labdatahub.business.domain.LabdatahubWarnConfig;
import com.labdatahub.business.service.ILabdatahubBrotherConfigService;
import com.labdatahub.business.service.ILabdatahubDbConfigService;
import com.labdatahub.business.service.ILabdatahubFanucConfigService;
import com.labdatahub.business.service.ILabdatahubFunctionService;
import com.labdatahub.business.service.ILabdatahubMitsubishiCncConfigService;
import com.labdatahub.business.service.ILabdatahubMitsubishiMc3eConfigService;
import com.labdatahub.business.service.ILabdatahubModbusConfigService;
import com.labdatahub.business.service.ILabdatahubOmronFinsConfigService;
import com.labdatahub.business.service.ILabdatahubPropertiesService;
import com.labdatahub.business.service.ILabdatahubS71200ConfigService;
import com.labdatahub.business.service.ILabdatahubWarnConfigService;
import com.labdatahub.business.warn.WarnRule;
import com.labdatahub.common.utils.SecurityUtils;
import com.labdatahub.common.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 由AI修改：产品/设备复制时，把源归属下的点位数据（物模型、各协议点位、告警、功能）复制到新归属
 * 产品复制 belongType=0（产品级点位模板）；设备复制 belongType=1（设备级点位，物模型额外只带 fromType=1 设备自定义属性）
 * 供 LabdatahubDeviceServiceImpl.saveDevice / LabdatahubProductController.add 的 copyFromId 复用
 */
@Component
public class ProtocolPointCopyUtil {

    @Autowired
    private ILabdatahubPropertiesService labdatahubPropertiesService;
    @Autowired
    private ILabdatahubWarnConfigService labdatahubWarnConfigService;
    @Autowired
    private ILabdatahubFunctionService labdatahubFunctionService;
    @Autowired
    private ILabdatahubModbusConfigService labdatahubModbusConfigService;
    @Autowired
    private ILabdatahubFanucConfigService labdatahubFanucConfigService;
    @Autowired
    private ILabdatahubMitsubishiCncConfigService labdatahubMitsubishiCncConfigService;
    @Autowired
    private ILabdatahubMitsubishiMc3eConfigService labdatahubMitsubishiConfigService;
    @Autowired
    private ILabdatahubBrotherConfigService labdatahubBrotherConfigService;
    @Autowired
    private ILabdatahubOmronFinsConfigService labdatahubOmronFinsConfigService;
    @Autowired
    private ILabdatahubS71200ConfigService labdatahubS71200ConfigService;
    @Autowired
    private ILabdatahubDbConfigService labdatahubDbConfigService;

    /**
     * 复制设备级点位到新设备（物模型设备专属 belong_type=0 + 各协议点位 belong_type 0/1 全带 + 设备级告警/功能 belongType=1）
     * 设备专属物模型 = 设备详情页手动加/协议点位自动生成（belong_type=0）；产品继承到设备的（belong_type=1）不复制，由 saveDevice.syncPropertyToDevice 重新继承
     *
     * @param sourceSn 源设备SN
     * @param newSn    新设备SN
     */
    public void copyDevicePoints(String sourceSn, String newSn) {
        copyProperties(sourceSn, newSn, "0");
        copyConfigTables(sourceSn, newSn, null);
        copyWarnConfigs(sourceSn, newSn, "1");
        copyFunctions(sourceSn, newSn, "1");
    }

    /**
     * 复制产品级点位模板到新产品（物模型 + 各协议点位 + 告警 + 功能，belongType=0）
     *
     * @param sourceSn 源产品SN
     * @param newSn    新产品SN
     */
    public void copyProductPoints(String sourceSn, String newSn) {
        copyProperties(sourceSn, newSn, null);
        copyConfigTables(sourceSn, newSn, "0");
        copyWarnConfigs(sourceSn, newSn, "0");
        copyFunctions(sourceSn, newSn, "0");
    }

    /**
     * 复制物模型属性到新归属（belongTypeFilter 传 "0" 只复制该归属专属/手动物模型，传 null 复制该归属全部；belongType/fromType 保持源值）
     *
     * @param sourceSn        源归属SN
     * @param newSn           新归属SN
     * @param belongTypeFilter 源 belongType 过滤条件（可为 null）
     */
    private void copyProperties(String sourceSn, String newSn, String belongTypeFilter) {
        LambdaQueryWrapper<LabdatahubProperties> queryWrapper = new LambdaQueryWrapper<LabdatahubProperties>()
                .eq(LabdatahubProperties::getBelongSn, sourceSn);
        if (StringUtils.isNotEmpty(belongTypeFilter)) {
            queryWrapper.eq(LabdatahubProperties::getBelongType, belongTypeFilter);
        }
        List<LabdatahubProperties> sourceList = labdatahubPropertiesService.list(queryWrapper);
        if (StringUtils.isEmpty(sourceList)) {
            return;
        }
        // 由AI修改：目标归属已有的 identifier 不再复制。设备创建时 syncPropertyToDevice 已经把产品属性继承过来，
        // 源设备的设备级副本若同 identifier 再插一遍就是重复行（物模型重复、点位不重复的来源）。
        Set<String> targetIdentifiers = labdatahubPropertiesService.list(new LambdaQueryWrapper<LabdatahubProperties>()
                        .eq(LabdatahubProperties::getBelongSn, newSn))
                .stream()
                .map(LabdatahubProperties::getIdentifier)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        List<LabdatahubProperties> copyList = new ArrayList<>();
        for (LabdatahubProperties source : sourceList) {
            String identifier = source.getIdentifier();
            if (StringUtils.isNotBlank(identifier) && !targetIdentifiers.add(identifier)) {
                continue;
            }
            LabdatahubProperties copy = new LabdatahubProperties();
            BeanUtils.copyProperties(source, copy);
            copy.setId(null);
            copy.setBelongSn(newSn);
            // belongType / fromType 保持源值（产品继承到设备的 belong_type=1 不在此列）
            copyList.add(copy);
        }
        if (copyList.isEmpty()) {
            return;
        }
        labdatahubPropertiesService.saveBatch(copyList);
    }

    /**
     * 复制各协议点位表（belongTypeFilter 传 "0" 只复制产品/设备手动点位，传 null 复制该归属全部点位；belongType 保持源值）
     * 设备点位 belong_type 有 0（设备详情页手动配）和 1（syncConfigToDevice 产品下发）两种来源，复制设备时必须都带
     *
     * @param sourceSn        源归属SN
     * @param newSn           新归属SN
     * @param belongTypeFilter 源 belongType 过滤条件（可为 null = 不过滤）
     */
    private void copyConfigTables(String sourceSn, String newSn, String belongTypeFilter) {
        copyConfigTable(labdatahubModbusConfigService, sourceSn, newSn, belongTypeFilter);
        copyConfigTable(labdatahubFanucConfigService, sourceSn, newSn, belongTypeFilter);
        copyConfigTable(labdatahubMitsubishiCncConfigService, sourceSn, newSn, belongTypeFilter);
        copyConfigTable(labdatahubMitsubishiConfigService, sourceSn, newSn, belongTypeFilter);
        copyConfigTable(labdatahubBrotherConfigService, sourceSn, newSn, belongTypeFilter);
        copyConfigTable(labdatahubOmronFinsConfigService, sourceSn, newSn, belongTypeFilter);
        copyConfigTable(labdatahubS71200ConfigService, sourceSn, newSn, belongTypeFilter);
        copyConfigTable(labdatahubDbConfigService, sourceSn, newSn, belongTypeFilter);
    }

    /**
     * 复制一张协议点位表（各 Config 表结构同构，用反射设公共字段，避免 7 份重复代码）
     *
     * @param configService   协议点位 Service
     * @param sourceSn        源归属SN
     * @param newSn           新归属SN
     * @param belongTypeFilter 源 belongType 过滤条件（可为 null = 不过滤，belongType 保持源值）
     */
    private <T> void copyConfigTable(IService<T> configService, String sourceSn, String newSn, String belongTypeFilter) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<T>()
                .eq("belong_sn", sourceSn);
        if (StringUtils.isNotEmpty(belongTypeFilter)) {
            queryWrapper.eq("belong_type", belongTypeFilter);
        }
        List<T> sourceList = configService.list(queryWrapper);
        if (StringUtils.isEmpty(sourceList)) {
            return;
        }
        List<T> copyList = new ArrayList<>();
        // 由AI修改：源归属自身若有重复 code（旧版本设备删除不清点位、重复复制叠加而来），每个 code+归属类型只带一条，
        // 否则重复会原样传播到新设备（gx-341 就是这样从源设备带出两份的）
        Set<String> copiedKeys = new HashSet<>();
        for (T source : sourceList) {
            String key = readStringField(source, "getCode") + "|" + readStringField(source, "getBelongType");
            if (!copiedKeys.add(key)) {
                continue;
            }
            T copy = (T) BeanUtils.instantiateClass(source.getClass());
            BeanUtils.copyProperties(source, copy);
            setConfigBaseFields(copy, newSn);
            copyList.add(copy);
        }
        configService.saveBatch(copyList);
    }

    /**
     * 由AI修改：清掉该归属 SN 下遗留的物模型与全部协议点位行。
     * 设备删除不清这两类表，同 SN 重建时旧行还在，复制时再整份插入就会叠成重复行
     * （gx-209 每个点位三条、gx-265 两条即由此而来）。只在「确认该 SN 没有设备行」的创建入口调用，
     * 不能用于普通保存，否则会删掉在用的配置。
     *
     * @param belongSn 设备/产品SN
     */
    public void clearBelongPoints(String belongSn) {
        labdatahubPropertiesService.remove(new LambdaQueryWrapper<LabdatahubProperties>()
                .eq(LabdatahubProperties::getBelongSn, belongSn));
        clearConfigTable(labdatahubModbusConfigService, belongSn);
        clearConfigTable(labdatahubFanucConfigService, belongSn);
        clearConfigTable(labdatahubMitsubishiCncConfigService, belongSn);
        clearConfigTable(labdatahubMitsubishiConfigService, belongSn);
        clearConfigTable(labdatahubBrotherConfigService, belongSn);
        clearConfigTable(labdatahubOmronFinsConfigService, belongSn);
        clearConfigTable(labdatahubS71200ConfigService, belongSn);
        clearConfigTable(labdatahubDbConfigService, belongSn);
    }

    /**
     * 清空一张协议点位表里属于该归属的行
     */
    private <T> void clearConfigTable(IService<T> configService, String belongSn) {
        configService.remove(new QueryWrapper<T>().eq("belong_sn", belongSn));
    }

    /**
     * 通过反射读取协议点位公共字段（各 Config 表结构同构，用 getter 取 code/belongType）
     */
    private String readStringField(Object config, String getter) {
        try {
            Object value = config.getClass().getMethod(getter).invoke(config);
            return value == null ? "" : value.toString();
        } catch (Exception e) {
            throw new RuntimeException("读取协议点位公共字段失败：" + getter, e);
        }
    }

    /**
     * 通过反射设置协议点位公共字段（id 置空让 MyBatis-Plus 自动生成、belongSn 换新归属、createTime；belongType 保持源值不动）
     */
    private void setConfigBaseFields(Object config, String newSn) {
        try {
            Class<?> clazz = config.getClass();
            clazz.getMethod("setId", String.class).invoke(config, new Object[]{null});
            clazz.getMethod("setBelongSn", String.class).invoke(config, newSn);
            clazz.getMethod("setCreateTime", Date.class).invoke(config, new Date());
        } catch (Exception e) {
            throw new RuntimeException("复制协议点位设置公共字段失败", e);
        }
    }

    /**
     * 复制告警到新归属（ruleJson 内嵌的 belongSn/ruleId 一并改掉，参照 syncWarnConfigToDevice）
     *
     * @param sourceSn   源归属SN
     * @param newSn      新归属SN
     * @param belongType 归属类型 0-产品 1-设备
     */
    private void copyWarnConfigs(String sourceSn, String newSn, String belongType) {
        List<LabdatahubWarnConfig> sourceList = labdatahubWarnConfigService.list(new LambdaQueryWrapper<LabdatahubWarnConfig>()
                .eq(LabdatahubWarnConfig::getBelongSn, sourceSn)
                .eq(LabdatahubWarnConfig::getBelongType, belongType));
        if (StringUtils.isEmpty(sourceList)) {
            return;
        }
        List<LabdatahubWarnConfig> copyList = new ArrayList<>();
        for (LabdatahubWarnConfig config : sourceList) {
            LabdatahubWarnConfig copy = new LabdatahubWarnConfig();
            BeanUtils.copyProperties(config, copy);
            copy.setId(IdWorker.getIdStr());
            copy.setBelongSn(newSn);
            copy.setBelongType(belongType);
            copy.setCreateTime(new Date());
            try {
                copy.setCreateBy(SecurityUtils.getUsername());
            } catch (Exception e) {
                copy.setCreateBy("自注册");
            }
            WarnRule rule = JSONObject.parseObject(config.getRuleJson(), WarnRule.class);
            rule.setBelongSn(copy.getBelongSn());
            rule.setBelongType(copy.getBelongType());
            rule.setId(copy.getId());
            copy.setRuleJson(JSONObject.toJSONString(rule));
            copyList.add(copy);
        }
        labdatahubWarnConfigService.saveBatch(copyList);
        CacheUtils.updateDeviceWarnRule(newSn);
    }

    /**
     * 复制功能到新归属
     *
     * @param sourceSn   源归属SN
     * @param newSn      新归属SN
     * @param belongType 归属类型 0-产品 1-设备
     */
    private void copyFunctions(String sourceSn, String newSn, String belongType) {
        List<LabdatahubFunction> sourceList = labdatahubFunctionService.list(new LambdaQueryWrapper<LabdatahubFunction>()
                .eq(LabdatahubFunction::getBelongSn, sourceSn)
                .eq(LabdatahubFunction::getBelongType, belongType));
        if (StringUtils.isEmpty(sourceList)) {
            return;
        }
        List<LabdatahubFunction> copyList = new ArrayList<>();
        for (LabdatahubFunction source : sourceList) {
            LabdatahubFunction copy = new LabdatahubFunction();
            BeanUtils.copyProperties(source, copy);
            copy.setId(null);
            copy.setBelongSn(newSn);
            copy.setBelongType(belongType);
            copy.setCreateTime(new Date());
            copyList.add(copy);
        }
        labdatahubFunctionService.saveBatch(copyList);
    }
}
