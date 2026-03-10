package com.labdatahub.business.warn.link;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 设备联动告警缓存
 * @Author: ruoyi
 * @CreateTime: 2025-11-19
 */
public class LinkRuleCache {

    /**
     * 联动告警规则缓存
     * KEY：id
     * VALUE：规则
     */
    public static final Map<String,WarnLinkRule> WARN_LINK_RULE_MAP = new HashMap<>();

    /**
     * 联动告警设备列表缓存
     * KEY：id
     * VALUE：设备列表
     */
    public static final Map<String, List<String>> LINK_DEVICE_LIST_MAP = new HashMap<>();

    /**
     * 设备联动告警缓存
     * KEY：设备sn
     * VALUE：规则id
     */
    public static final Map<String,List<String>> DEVICE_RULE_MAP = new HashMap<>();

    /**
     * 存在状态变化的设备告警规则
     * KEY：设备sn
     * VALUE：规则id
     */
    public static final Map<String,List<String>> CHANGE_STATUS_RULE_MAP = new HashMap<>();

    /**
     * 存在当前状态的设备告警规则
     * KEY：设备sn
     * VALUE：规则id
     */
    public static final Map<String,List<String>> CURRENT_STATUS_RULE_MAP = new HashMap<>();

    /**
     * 存在属性比较的设备告警规则
     * KEY：设备sn
     * VALUE：规则id
     */
    public static final Map<String,List<String>> PROPERTY_RULE_MAP = new HashMap<>();


}
