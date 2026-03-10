package com.labdatahub.business.utils;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.labdatahub.business.domain.*;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubFunctionService;
import com.labdatahub.business.service.ILabdatahubWarnConfigService;
import com.labdatahub.business.warn.WarnRule;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.spring.SpringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @Description: 缓存工具
 * @Author: labdatahub
 * @CreateTime: 2025-10-10
 */
public class CacheUtils {
    /**
     * 设备实例数据缓存
     */
    /**
     * 设备实例数据缓存
     */
    public final static ConcurrentMap<String, LabdatahubDevice> DEVICE_MAP = new ConcurrentHashMap<>();

    /**
     * 产品数据缓存
     */
    public final static ConcurrentMap<String, LabdatahubProduct> PRODUCT_MAP = new ConcurrentHashMap<>();

    /**
     * 设备在离线状态缓存
     */
    public final static ConcurrentMap<String,Boolean> DEVICE_STATUS = new ConcurrentHashMap<>();

    /**
     * 设备某个规则上一次告警时间
     */
    public final static ConcurrentMap<String,Long> DEVICE_WARN_TIME = new ConcurrentHashMap<>();

    /**
     * 设备告警规则
     */
    public final static ConcurrentMap<String,List<WarnRule>> DEVICE_WARN_RULE = new ConcurrentHashMap<>();

    /**
     * 设备联动告警规则
     */
    public final static ConcurrentMap<String,List<WarnRule>> DEVICE_LINK_WARN_RULE = new ConcurrentHashMap<>();

    /**
     * 设备自注册开关
     */
    public static String DEVICE_REGISTER_SWITCH = "false";

    /**
     * 设备指令缓存
     */
    public static ConcurrentMap<String,List<LabdatahubFunction>> DEVICE_FUNCTION = new ConcurrentHashMap<>();

    /**
     * 网络组件缓存
     */
    public static ConcurrentMap<String, LabdatahubComponent> COMPONENT_MAP = new ConcurrentHashMap<>();

    /**
     * 更新所有设备实例数据
     */
    public static void updateAllDeviceCache(){
        List<LabdatahubDevice> list = SpringUtils.getBean(ILabdatahubDeviceService.class).list();
        DEVICE_MAP.clear();
        list.forEach(device->{
            DEVICE_MAP.put(device.getDeviceSn(),device);
        });
    }

    /**
     * 更新设备实例数据根据产品
     */
    public static void updateDeviceCacheByProductSn(String productSn){
        List<LabdatahubDevice> list = SpringUtils.getBean(ILabdatahubDeviceService.class).list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        DEVICE_MAP.clear();
        list.forEach(device->{
            DEVICE_MAP.put(device.getDeviceSn(),device);
        });
    }

    /**
     * 更新所有设备实例数据
     */
    public static void updateAllDeviceCache(List<LabdatahubDevice> list){
        DEVICE_MAP.clear();
        list.forEach(device->{
            DEVICE_MAP.put(device.getDeviceSn(),device);
        });
    }

    /**
     * 更新单个设备实例数据
     */
    public static void updateDeviceCache(String deviceSn){
        LabdatahubDevice device = SpringUtils.getBean(ILabdatahubDeviceService.class).getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getDeviceSn,deviceSn));
        DEVICE_MAP.put(device.getDeviceSn(),device);
    }

    /**
     * 更新单个设备自定义配置
     */
    public static void updateDeviceCustomConfig(String deviceSn,String customConfig){
        LabdatahubDevice device = DEVICE_MAP.get(deviceSn);
        device.setCustomConfig(customConfig);
    }

    /**
     * 更新产品下所有设备自定义配置
     */
    public static void updateDeviceCustomConfigByProductSn(String productSn,String customConfig){
        List<LabdatahubDevice> list = SpringUtils.getBean(ILabdatahubDeviceService.class).list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        list.forEach(device -> {
            LabdatahubDevice o = DEVICE_MAP.get(device.getDeviceSn());
            o.setCustomConfig(customConfig);
        });
    }

    /**
     * 更新单个设备实例数据
     */
    public static void updateDeviceCache(LabdatahubDevice device){
        DEVICE_MAP.put(device.getDeviceSn(),device);
    }

    /**
     * 获取设备实例数据
     */
    public static LabdatahubDevice getDeviceBySn(String deviceSn){
        return DEVICE_MAP.getOrDefault(deviceSn,null);
    }

    /**
     * 获取设备实例数据
     */
    public static List<LabdatahubDevice> getDeviceByProductSn(String productSn){
        List<LabdatahubDevice> list = new ArrayList<>();
        DEVICE_MAP.values().forEach(device->{
            if(productSn.equals(device.getProductSn())){
                list.add(device);
            }
        });
        return list;
    }

    /**
     * 更新所有设备状态
     */
    public static void updateAllDeviceStatusCache(){
        List<LabdatahubDevice> list = SpringUtils.getBean(ILabdatahubDeviceService.class).list();
        list.forEach(device->{
            DEVICE_STATUS.put(device.getDeviceSn(),"1".equals(device.getStatus()));
        });
    }

    /**
     * 更新单个设备状态
     */
    public static void updateDeviceStatusCache(LabdatahubDevice device){
        DEVICE_STATUS.put(device.getDeviceSn(), "1".equals(device.getStatus()));
    }

    /**
     * 更新单个设备状态
     */
    public static void updateDeviceStatusCache(String deviceSn){
        LabdatahubDevice device = SpringUtils.getBean(ILabdatahubDeviceService.class).getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getDeviceSn,deviceSn));
        DEVICE_STATUS.put(device.getDeviceSn(), "1".equals(device.getStatus()));
    }

    /**
     * 更新单个设备状态
     */
    public static void updateDeviceStatusCache(String deviceSn,boolean status){
        DEVICE_STATUS.put(deviceSn, status);
    }

    /**
     * 更新单个设备状态
     */
    public static boolean getDeviceStatusBySn(String deviceSn){
        return DEVICE_STATUS.getOrDefault(deviceSn, false);
    }

    /**
     * 更新设备规则上一次告警时间
     */
    public static void updateDeviceRuleWarnTime(String deviceSn,String warnKey,long time){
        DEVICE_WARN_TIME.put(deviceSn+"_"+warnKey,time);
    }

    /**
     * 获取设备规则上一次告警时间
     */
    public static long getDeviceRuleWarnTime(String deviceSn,String warnKey){
        return DEVICE_WARN_TIME.getOrDefault(deviceSn+"_"+warnKey,0L);
    }

    /**
     * 更新所有设备告警规则
     */
    public static void updateAllDeviceWarnRule(){
        List<LabdatahubWarnConfig> allList = SpringUtils.getBean(ILabdatahubWarnConfigService.class).list();
        // 按 deviceSn 分组
        Map<String, List<LabdatahubWarnConfig>> groupedByDeviceSn = allList.stream()
                .collect(Collectors.groupingBy(LabdatahubWarnConfig::getBelongSn));
        // 清空原有缓存
        DEVICE_WARN_RULE.clear();
        // 逐个放入缓存
        for (Map.Entry<String, List<LabdatahubWarnConfig>> entry : groupedByDeviceSn.entrySet()) {
            String deviceSn = entry.getKey();
            List<LabdatahubWarnConfig> configList = entry.getValue();
            List<WarnRule> list = new ArrayList<>();
            configList.forEach(config->{
                WarnRule rule =  JSONObject.parseObject(config.getRuleJson(),WarnRule.class);
                rule.setId(config.getId());
                rule.setWarnType("0");
                if(rule.getConditions()!=null&&rule.getConditions().size()>0){
                    rule.getConditions().forEach(condition->{
                        if("device_online".equals(condition.getType())){
                            condition.setAttribute("device_online");
                            condition.setOperator("eq");
                            condition.setValue("1");
                            rule.setWarnType("1");
                        }
                        if("device_offline".equals(condition.getType())){
                            condition.setAttribute("device_offline");
                            condition.setOperator("eq");
                            condition.setValue("1");
                            rule.setWarnType("2");
                        }
                    });
                    if(!"0".equals(rule.getWarnType())){
                        SpringUtils.getBean(ILabdatahubWarnConfigService.class).update(new LambdaUpdateWrapper<LabdatahubWarnConfig>()
                                .eq(LabdatahubWarnConfig::getId,config.getId())
                                .set(LabdatahubWarnConfig::getWarnType,rule.getWarnType()));
                    }
                }
                list.add(rule);
            });
            DEVICE_WARN_RULE.put(deviceSn, list);
        }
    }

    /**
     * 更新设备告警规则
     */
    public static void updateDeviceWarnRule(String deviceSn){
        List<LabdatahubWarnConfig> configList = SpringUtils.getBean(ILabdatahubWarnConfigService.class).list(new LambdaQueryWrapper<LabdatahubWarnConfig>()
                .eq(LabdatahubWarnConfig::getBelongSn,deviceSn));
        List<WarnRule> list = new ArrayList<>();
        configList.forEach(config->{
            WarnRule rule =  JSONObject.parseObject(config.getRuleJson(),WarnRule.class);
            rule.setId(config.getId());
            rule.setWarnType("0");
            if(rule.getConditions()!=null&&rule.getConditions().size()>0){
                rule.getConditions().forEach(condition->{
                    if("device_online".equals(condition.getType())){
                        condition.setAttribute("device_online");
                        condition.setOperator("eq");
                        condition.setValue("1");
                        rule.setWarnType("1");
                    }
                    if("device_offline".equals(condition.getType())){
                        condition.setAttribute("device_offline");
                        condition.setOperator("eq");
                        condition.setValue("1");
                        rule.setWarnType("2");
                    }
                });
                if(!"0".equals(rule.getWarnType())){
                    SpringUtils.getBean(ILabdatahubWarnConfigService.class).update(new LambdaUpdateWrapper<LabdatahubWarnConfig>()
                            .eq(LabdatahubWarnConfig::getId,config.getId())
                            .set(LabdatahubWarnConfig::getWarnType,rule.getWarnType()));
                }
            }
            list.add(rule);
        });
        DEVICE_WARN_RULE.put(deviceSn,list);
    }

    /**
     * 更新设备告警规则
     */
    public static void updateDeviceWarnRule(String deviceSn,List<LabdatahubWarnConfig> configList){
        List<WarnRule> list = new ArrayList<>();
        configList.forEach(config->{
            WarnRule rule =  JSONObject.parseObject(config.getRuleJson(),WarnRule.class);
            rule.setId(config.getId());
            list.add(rule);
        });
        DEVICE_WARN_RULE.put(deviceSn,list);
    }

    /**
     * 获取设备告警规则
     * @param deviceSn
     */
    public static List<WarnRule> getDeviceWarnRule(String deviceSn){
        return DEVICE_WARN_RULE.getOrDefault(deviceSn,new ArrayList<>());
    }

    /**
     * 更新设备指令缓存
     */
    public static void updateDeviceFunctionCache(String deviceSn){
        List<LabdatahubFunction> list = SpringUtils.getBean(ILabdatahubFunctionService.class).list(new LambdaQueryWrapper<LabdatahubFunction>()
                .eq(LabdatahubFunction::getBelongSn,deviceSn));
        if(list.isEmpty()){
            return;
        }
        DEVICE_FUNCTION.put(deviceSn,list);
    }

    /**
     * 设置设备指令缓存
     */
    public static void setDeviceFunctionCache(String deviceSn,List<LabdatahubFunction> list){
        if(StringUtils.isEmpty(deviceSn)||list.isEmpty()){
            return;
        }
        DEVICE_FUNCTION.put(deviceSn,list);
    }

    /**
     * 获取设备指令缓存
     */
    public static LabdatahubFunction getDeviceFunctionCache(String deviceSn,String functionCode){
        if(StringUtils.isEmpty(functionCode)||StringUtils.isEmpty(deviceSn)){
            return null;
        }
        List<LabdatahubFunction> functions = DEVICE_FUNCTION.getOrDefault(deviceSn,null);
        if(functions==null||functions.isEmpty()){
            return null;
        }
        for (int i = 0; i < functions.size(); i++) {
            if(functionCode.equals(functions.get(i).getFunctionCode())){
                return functions.get(i);
            }
        }
        return null;
    }

    /**
     * 更新网络组件缓存
     */
    public static void setComponentCache(String componentId,LabdatahubComponent labdatahubComponent){
        COMPONENT_MAP.put(componentId,labdatahubComponent);
    }

    /**
     * 获取网络组件缓存
     */
    public static LabdatahubComponent getComponentCache(String componentId){
        return COMPONENT_MAP.getOrDefault(componentId,null);
    }

    /**
     * 删除网络组件缓存
     */
    public static void removeComponentCache(String componentId){
        COMPONENT_MAP.remove(componentId);
    }

}
