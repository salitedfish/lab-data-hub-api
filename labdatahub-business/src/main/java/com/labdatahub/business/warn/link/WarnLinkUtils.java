package com.labdatahub.business.warn.link;

import com.alibaba.fastjson.JSONObject;
import com.labdatahub.business.domain.LabdatahubLinkageActionRecord;
import com.labdatahub.business.domain.LabdatahubLinkageWarnRecord;
import com.labdatahub.business.process.linkage.LinkageProcessor;
import com.labdatahub.business.process.warn.WarnProcessor;
import com.labdatahub.business.service.ILabdatahubLinkageWarnRecordService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.message.DecodeMessage;
import com.labdatahub.component.message.MessageCache;

import java.util.*;
import java.util.stream.Collectors;

import static com.labdatahub.business.warn.link.LinkRuleCache.*;

/**
 * @Description: 联动告警工具类
 * @Author: ruoyi
 * @CreateTime: 2025-11-19
 */
public class WarnLinkUtils {

    /**
     * 传入设备SN触发告警和动作
     */
    public static void propertyWarn(String deviceSn){
        if(!PROPERTY_RULE_MAP.containsKey(deviceSn)){
            return;
        }
        List<String> ruleIds = PROPERTY_RULE_MAP.getOrDefault(deviceSn,new ArrayList<>());
        ruleIds.forEach(ruleId->{
            WarnLinkRule rule = WARN_LINK_RULE_MAP.getOrDefault(ruleId,null);
            if(rule!=null){
                List<String> propertySnList = getDeviceSnByPropertyRule(ruleId);
                //如果存在设备状态变化条件，直接退出
                List<String> changeStatusSnList = getDeviceSnByChangeStatusRule(ruleId);
                if(!changeStatusSnList.isEmpty()){
                    return;
                }
                Map<String,Map<String,Object>> data = new HashMap<>();
                propertySnList.forEach(sn->{
                    DecodeMessage decodeMessage = MessageCache.getDeviceLastData(sn);
                    if(decodeMessage!=null){
                        data.put(sn,decodeMessage.getProperties());
                    }
                });
                List<String> currentStatusSnList = getDeviceSnByCurrentStatusRule(ruleId);
                currentStatusSnList.forEach(sn->{
                    boolean status = CacheUtils.getDeviceStatusBySn(sn);
                    Map<String,Object> map = data.getOrDefault(sn,new HashMap<>());
                    map.put("currentStatus",status?"1":"0");
                    data.put(sn,map);
                });
                boolean isTrigger = RuleEngine.evaluate(rule.getRootNode(),data);
                if(isTrigger){
                    LabdatahubLinkageWarnRecord warnRecord = new LabdatahubLinkageWarnRecord();
                    warnRecord.setCreateTime(new Date());
                    warnRecord.setConfigId(ruleId);
                    warnRecord.setConfigName(rule.getName());
                    warnRecord.setWarnMessage(rule.getMessage());
                    warnRecord.setWarnLevel(rule.getLevel());
                    warnRecord.setStatus("0");
                    warnRecord.setWarnData(JSONObject.toJSONString(data));
                    warnRecord.setTriggerSnList(String.join(",",rule.getInvolvedDeviceSns()));
                    warnRecord.setTriggerNameList(String.join(",",rule.getInvolvedDeviceNames()));
                    if(LinkageProcessor.getInstance().isRunning()){
                        LinkageProcessor.getInstance().addLog(warnRecord);
                    }else {
                        SpringUtils.getBean(ILabdatahubLinkageWarnRecordService.class).save(warnRecord);
                    }
                    CommandTreeExecutor.executeCommandTree(rule.getActions());
                }
            }
        });
    }

    /**
     * 传入设备SN触发告警和动作
     */
    public static void changeStatusWarn(String deviceSn){
        if(!CHANGE_STATUS_RULE_MAP.containsKey(deviceSn)){
            return;
        }
        List<String> ruleIds = CHANGE_STATUS_RULE_MAP.getOrDefault(deviceSn,new ArrayList<>());
        ruleIds.forEach(ruleId->{
            WarnLinkRule rule = WARN_LINK_RULE_MAP.getOrDefault(ruleId,null);
            if(rule!=null){
                List<String> propertySnList = getDeviceSnByPropertyRule(ruleId);
                Map<String,Map<String,Object>> data = new HashMap<>();
                propertySnList.forEach(sn->{
                    DecodeMessage decodeMessage = MessageCache.getDeviceLastData(sn);
                    if(decodeMessage!=null){
                        data.put(sn,decodeMessage.getProperties());
                    }
                });
                List<String> currentStatusSnList = getDeviceSnByCurrentStatusRule(ruleId);
                currentStatusSnList.forEach(sn->{
                    boolean status = CacheUtils.getDeviceStatusBySn(sn);
                    Map<String,Object> map = data.getOrDefault(sn,new HashMap<>());
                    map.put("currentStatus",status?"1":"0");
                    data.put(sn,map);
                });
                List<String> changeStatusSnList = getDeviceSnByChangeStatusRule(ruleId);
                changeStatusSnList.forEach(sn->{
                    boolean status = CacheUtils.getDeviceStatusBySn(sn);
                    Map<String,Object> map = data.getOrDefault(sn,new HashMap<>());
                    map.put("changeStatus",status?"1":"0");
                    data.put(sn,map);
                });
                boolean isTrigger = RuleEngine.evaluate(rule.getRootNode(),data);
                if(isTrigger){
                    LabdatahubLinkageWarnRecord warnRecord = new LabdatahubLinkageWarnRecord();
                    warnRecord.setCreateTime(new Date());
                    warnRecord.setConfigId(ruleId);
                    warnRecord.setConfigName(rule.getName());
                    warnRecord.setWarnMessage(rule.getMessage());
                    warnRecord.setWarnLevel(rule.getLevel());
                    warnRecord.setStatus("0");
                    warnRecord.setWarnData(JSONObject.toJSONString(data));
                    warnRecord.setTriggerSnList(String.join(",",rule.getInvolvedDeviceSns()));
                    warnRecord.setTriggerNameList(String.join(",",rule.getInvolvedDeviceNames()));
                    if(LinkageProcessor.getInstance().isRunning()){
                        LinkageProcessor.getInstance().addLog(warnRecord);
                    }else {
                        SpringUtils.getBean(ILabdatahubLinkageWarnRecordService.class).save(warnRecord);
                    }
                    CommandTreeExecutor.executeCommandTree(rule.getActions());
                }
            }
        });
    }

    // ==================== 联动告警规则缓存操作方法 ====================

    /**
     * 新增规则到缓存
     */
    public static void addRuleToCache(String ruleId, WarnLinkRule rule) {
        if (ruleId != null && rule != null) {
            WARN_LINK_RULE_MAP.put(ruleId, rule);
        }
    }

    /**
     * 从缓存移除规则
     */
    public static void removeRuleFromCache(String ruleId) {
        if (ruleId != null) {
            WARN_LINK_RULE_MAP.remove(ruleId);
        }
    }

    /**
     * 获取缓存中的规则
     */
    public static WarnLinkRule getRuleFromCache(String ruleId) {
        return ruleId != null ? WARN_LINK_RULE_MAP.get(ruleId) : null;
    }

    // ==================== 联动告警设备列表缓存操作方法 ====================

    /**
     * 新增规则对应的设备列表
     */
    public static void addDeviceListToCache(String ruleId, List<String> deviceList) {
        if (ruleId != null && deviceList != null && !deviceList.isEmpty()) {
            LINK_DEVICE_LIST_MAP.put(ruleId, deviceList);
        }
    }

    /**
     * 从缓存移除规则对应的设备列表
     */
    public static void removeDeviceListFromCache(String ruleId) {
        if (ruleId != null) {
            LINK_DEVICE_LIST_MAP.remove(ruleId);
        }
    }

    /**
     * 获取规则对应的设备列表
     */
    public static List<String> getDeviceListFromCache(String ruleId) {
        return ruleId != null ? LINK_DEVICE_LIST_MAP.get(ruleId) : null;
    }

    // ==================== 设备联动告警缓存操作方法 ====================

    /**
     * 新增设备与规则的关联
     */
    public static void addDeviceRuleRelation(String deviceSn, String ruleId) {
        if (deviceSn != null && ruleId != null) {
            List<String> ruleIds = DEVICE_RULE_MAP.computeIfAbsent(deviceSn, k -> new ArrayList<>());
            if (!ruleIds.contains(ruleId)) {
                ruleIds.add(ruleId);
            }
        }
    }

    /**
     * 移除设备与规则的关联
     */
    public static void removeDeviceRuleRelation(String deviceSn, String ruleId) {
        if (deviceSn != null && ruleId != null && DEVICE_RULE_MAP.containsKey(deviceSn)) {
            List<String> ruleIds = DEVICE_RULE_MAP.get(deviceSn);
            if (ruleIds != null) {
                ruleIds.remove(ruleId);
                // 如果设备没有关联任何规则，移除该设备
                if (ruleIds.isEmpty()) {
                    DEVICE_RULE_MAP.remove(deviceSn);
                }
            }
        }
    }

    /**
     * 移除设备的所有规则关联
     */
    public static void removeAllDeviceRuleRelations(String deviceSn) {
        if (deviceSn != null) {
            DEVICE_RULE_MAP.remove(deviceSn);
        }
    }

    /**
     * 获取设备关联的所有规则ID
     */
    public static List<String> getRuleIdsByDevice(String deviceSn) {
        return deviceSn != null ? DEVICE_RULE_MAP.get(deviceSn) : null;
    }

    // ==================== 状态变化规则缓存操作方法 ====================

    /**
     * 新增状态变化规则关联
     */
    public static void addChangeStatusRule(String deviceSn, String ruleId) {
        if (deviceSn != null && ruleId != null) {
            List<String> ruleIds = CHANGE_STATUS_RULE_MAP.computeIfAbsent(deviceSn, k -> new ArrayList<>());
            if (!ruleIds.contains(ruleId)) {
                ruleIds.add(ruleId);
            }
        }
    }

    /**
     * 移除状态变化规则关联
     */
    public static void removeChangeStatusRule(String deviceSn, String ruleId) {
        if (deviceSn != null && ruleId != null && CHANGE_STATUS_RULE_MAP.containsKey(deviceSn)) {
            List<String> ruleIds = CHANGE_STATUS_RULE_MAP.get(deviceSn);
            if (ruleIds != null) {
                ruleIds.remove(ruleId);
                // 如果设备没有状态变化规则，移除该设备
                if (ruleIds.isEmpty()) {
                    CHANGE_STATUS_RULE_MAP.remove(deviceSn);
                }
            }
        }
    }

    /**
     * 移除设备的所有状态变化规则
     */
    public static void removeAllChangeStatusRules(String deviceSn) {
        if (deviceSn != null) {
            CHANGE_STATUS_RULE_MAP.remove(deviceSn);
        }
    }

    /**
     * 获取设备的状态变化规则ID列表
     */
    public static List<String> getChangeStatusRuleIds(String deviceSn) {
        return deviceSn != null ? CHANGE_STATUS_RULE_MAP.get(deviceSn) : null;
    }

    // ==================== 当前状态规则缓存操作方法 ====================

    /**
     * 新增当前状态规则关联
     */
    public static void addCurrentStatusRule(String deviceSn, String ruleId) {
        if (deviceSn != null && ruleId != null) {
            List<String> ruleIds = CURRENT_STATUS_RULE_MAP.computeIfAbsent(deviceSn, k -> new ArrayList<>());
            if (!ruleIds.contains(ruleId)) {
                ruleIds.add(ruleId);
            }
        }
    }

    /**
     * 移除当前状态规则关联
     */
    public static void removeCurrentStatusRule(String deviceSn, String ruleId) {
        if (deviceSn != null && ruleId != null && CURRENT_STATUS_RULE_MAP.containsKey(deviceSn)) {
            List<String> ruleIds = CURRENT_STATUS_RULE_MAP.get(deviceSn);
            if (ruleIds != null) {
                ruleIds.remove(ruleId);
                // 如果设备没有当前状态规则，移除该设备
                if (ruleIds.isEmpty()) {
                    CURRENT_STATUS_RULE_MAP.remove(deviceSn);
                }
            }
        }
    }

    /**
     * 移除设备的所有当前状态规则
     */
    public static void removeAllCurrentStatusRules(String deviceSn) {
        if (deviceSn != null) {
            CURRENT_STATUS_RULE_MAP.remove(deviceSn);
        }
    }

    /**
     * 获取设备的当前状态规则ID列表
     */
    public static List<String> getCurrentStatusRuleIds(String deviceSn) {
        return deviceSn != null ? CURRENT_STATUS_RULE_MAP.get(deviceSn) : null;
    }

    // ==================== 属性比较规则缓存操作方法 ====================

    /**
     * 新增属性比较规则关联
     */
    public static void addPropertyRule(String deviceSn, String ruleId) {
        if (deviceSn != null && ruleId != null) {
            List<String> ruleIds = PROPERTY_RULE_MAP.computeIfAbsent(deviceSn, k -> new ArrayList<>());
            if (!ruleIds.contains(ruleId)) {
                ruleIds.add(ruleId);
            }
        }
    }

    /**
     * 移除属性比较规则关联
     */
    public static void removePropertyRule(String deviceSn, String ruleId) {
        if (deviceSn != null && ruleId != null && PROPERTY_RULE_MAP.containsKey(deviceSn)) {
            List<String> ruleIds = PROPERTY_RULE_MAP.get(deviceSn);
            if (ruleIds != null) {
                ruleIds.remove(ruleId);
                // 如果设备没有属性比较规则，移除该设备
                if (ruleIds.isEmpty()) {
                    PROPERTY_RULE_MAP.remove(deviceSn);
                }
            }
        }
    }

    /**
     * 移除设备的所有属性比较规则
     */
    public static void removeAllPropertyRules(String deviceSn) {
        if (deviceSn != null) {
            PROPERTY_RULE_MAP.remove(deviceSn);
        }
    }

    /**
     * 获取设备的属性比较规则ID列表
     */
    public static List<String> getPropertyRuleIds(String deviceSn) {
        return deviceSn != null ? PROPERTY_RULE_MAP.get(deviceSn) : null;
    }

    // ==================== 批量操作方法 ====================

    /**
     * 批量添加设备与规则的关联
     */
    public static void batchAddDeviceRuleRelations(List<String> deviceSns, String ruleId) {
        if (deviceSns != null && ruleId != null) {
            for (String deviceSn : deviceSns) {
                addDeviceRuleRelation(deviceSn, ruleId);
            }
        }
    }

    /**
     * 批量移除设备与规则的关联
     */
    public static void batchRemoveDeviceRuleRelations(List<String> deviceSns, String ruleId) {
        if (deviceSns != null && ruleId != null) {
            for (String deviceSn : deviceSns) {
                removeDeviceRuleRelation(deviceSn, ruleId);
            }
        }
    }

    /**
     * 根据规则ID移除所有相关缓存
     */
    public static void removeAllCacheByRuleId(String ruleId) {
        if (ruleId == null) {
            return;
        }

        // 移除规则缓存
        removeRuleFromCache(ruleId);

        // 移除设备列表缓存
        removeDeviceListFromCache(ruleId);

        // 从所有设备关联中移除该规则
        removeRuleFromAllDevices(ruleId);
    }

    /**
     * 从所有设备关联中移除指定规则
     */
    private static void removeRuleFromAllDevices(String ruleId) {
        // 从 DEVICE_RULE_MAP 中移除
        removeRuleFromMap(DEVICE_RULE_MAP, ruleId);

        // 从 CHANGE_STATUS_RULE_MAP 中移除
        removeRuleFromMap(CHANGE_STATUS_RULE_MAP, ruleId);

        // 从 CURRENT_STATUS_RULE_MAP 中移除
        removeRuleFromMap(CURRENT_STATUS_RULE_MAP, ruleId);

        // 从 PROPERTY_RULE_MAP 中移除
        removeRuleFromMap(PROPERTY_RULE_MAP, ruleId);
    }

    /**
     * 从指定Map中移除规则ID
     */
    private static void removeRuleFromMap(Map<String, List<String>> map, String ruleId) {
        map.entrySet().removeIf(entry -> {
            List<String> ruleIds = entry.getValue();
            if (ruleIds != null) {
                ruleIds.remove(ruleId);
                // 如果列表为空，移除该条目
                return ruleIds.isEmpty();
            }
            return true;
        });
    }

    /**
     * 获取所有涉及指定设备的规则ID（去重）
     */
    public static List<String> getAllRuleIdsByDevice(String deviceSn) {
        if (deviceSn == null) {
            return new ArrayList<>();
        }

        List<String> allRuleIds = new ArrayList<>();

        List<String> deviceRules = getRuleIdsByDevice(deviceSn);
        if (deviceRules != null) {
            allRuleIds.addAll(deviceRules);
        }

        List<String> changeStatusRules = getChangeStatusRuleIds(deviceSn);
        if (changeStatusRules != null) {
            allRuleIds.addAll(changeStatusRules);
        }

        List<String> currentStatusRules = getCurrentStatusRuleIds(deviceSn);
        if (currentStatusRules != null) {
            allRuleIds.addAll(currentStatusRules);
        }

        List<String> propertyRules = getPropertyRuleIds(deviceSn);
        if (propertyRules != null) {
            allRuleIds.addAll(propertyRules);
        }

        // 去重返回
        return allRuleIds.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 检查设备是否存在于任何规则缓存中
     */
    public static boolean isDeviceInAnyCache(String deviceSn) {
        if (deviceSn == null) {
            return false;
        }

        return DEVICE_RULE_MAP.containsKey(deviceSn) ||
                CHANGE_STATUS_RULE_MAP.containsKey(deviceSn) ||
                CURRENT_STATUS_RULE_MAP.containsKey(deviceSn) ||
                PROPERTY_RULE_MAP.containsKey(deviceSn);
    }

    /**
     * 根据规则ID查找设备SN列表 - 从状态变化规则表
     */
    public static List<String> getDeviceSnByChangeStatusRule(String ruleId) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : CHANGE_STATUS_RULE_MAP.entrySet()) {
            if (entry.getValue().contains(ruleId)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * 根据规则ID查找设备SN列表 - 从当前状态规则表
     */
    public static List<String> getDeviceSnByCurrentStatusRule(String ruleId) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : CURRENT_STATUS_RULE_MAP.entrySet()) {
            if (entry.getValue().contains(ruleId)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * 根据规则ID查找设备SN列表 - 从属性比较规则表
     */
    public static List<String> getDeviceSnByPropertyRule(String ruleId) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : PROPERTY_RULE_MAP.entrySet()) {
            if (entry.getValue().contains(ruleId)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

}