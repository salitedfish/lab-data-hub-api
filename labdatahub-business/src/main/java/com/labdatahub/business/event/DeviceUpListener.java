package com.labdatahub.business.event;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.labdatahub.business.domain.*;
import com.labdatahub.business.domain.enums.DeviceLogType;
import com.labdatahub.business.down.DeviceDownUtils;
import com.labdatahub.business.engine.utils.RuleEngineCache;
import com.labdatahub.business.process.log.LogProcessor;
import com.labdatahub.business.process.warn.WarnProcessor;
import com.labdatahub.business.service.ILabdatahubComponentService;
import com.labdatahub.business.service.ILabdatahubDeviceLogsService;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubWarnRecordService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.business.warn.WarnRule;
import com.labdatahub.business.warn.WarnRuleMatcher;
import com.labdatahub.business.warn.link.WarnLinkUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.event.EventBus;
import com.labdatahub.component.event.MessageUpEvent;
import com.labdatahub.component.message.*;
import com.labdatahub.component.rule_engine.mq.http.HttpManager;
import com.labdatahub.component.rule_engine.mq.kafka.KafkaProducerManager;
import com.labdatahub.component.rule_engine.mq.mqtt.MqttBrokerUtils;
import com.labdatahub.component.rule_engine.mq.rabbitmq.RabbitMQUtils;
import com.labdatahub.component.rule_engine.mq.rocketmq.RocketMQProducerManager;
import com.labdatahub.component.utils.PropertyToJson;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-09-25
 */
@Component
@Slf4j
public class DeviceUpListener {

    @Autowired
    private EventBus eventBus;
    @Autowired
    private ILabdatahubDeviceLogsService labdatahubDeviceLogsService;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private ILabdatahubWarnRecordService labdatahubWarnRecordService;

    @PostConstruct
    public void subscribeToEvents() {
        // 订阅所有设备消息
        eventBus.subscribe("device.up", this::dealDeviceUp);
        eventBus.subscribe("device.offline", this::directlyConnectedOffline);
        eventBus.subscribe("device.warn", this::warnEngine);
    }

    public void dealDeviceUp(Object event) {
        if (event instanceof MessageUpEvent) {
            MessageUpEvent message = (MessageUpEvent) event;
            if (message.getData() instanceof DecodeMessage) {
                DecodeMessage decodeMessage = (DecodeMessage) message.getData();
                deviceRegister(decodeMessage);
                ruleEngine(decodeMessage);
                saveMessage(decodeMessage);
                deviceStatus(decodeMessage);
                ruleWarn(message);
                functionDown(decodeMessage);
                linkageWarnProperty(decodeMessage);
            }
        }
    }

    public void ruleEngine(DecodeMessage decodeMessage) {
        Map<RuleEngineCache.TargetType, List<String>> map = RuleEngineCache.getDeviceForwardTargets(RuleEngineCache.DataType.DEVICE_LOG, decodeMessage.getDeviceSn());
        if (map.isEmpty()) {
            return;
        }
        List<String> httpList = map.get(RuleEngineCache.TargetType.HTTP);
        if (httpList != null && !httpList.isEmpty()) {
            threadPoolTaskExecutor.execute(() -> {
                httpList.forEach(o -> {
                    HttpManager.sendMessage(o, JSONObject.toJSONString(decodeMessage));
                });
            });
        }
        List<String> rabbitmqList = map.get(RuleEngineCache.TargetType.RABBITMQ);
        if (rabbitmqList != null && !rabbitmqList.isEmpty()) {
            threadPoolTaskExecutor.execute(() -> {
                rabbitmqList.forEach(o -> {
                    RabbitMQUtils.sendMessage(o, JSONObject.toJSONString(decodeMessage));
                });
            });
        }
        List<String> kafkaList = map.get(RuleEngineCache.TargetType.KAFKA);
        if (kafkaList != null && !kafkaList.isEmpty()) {
            threadPoolTaskExecutor.execute(() -> {
                kafkaList.forEach(o -> {
                    KafkaProducerManager.getInstance().sendLogAsync(o, JSONObject.toJSONString(decodeMessage));
                });
            });
        }
        List<String> rocketMqList = map.get(RuleEngineCache.TargetType.ROCKETMQ);
        if (rocketMqList != null && !rocketMqList.isEmpty()) {
            threadPoolTaskExecutor.execute(() -> {
                rocketMqList.forEach(o -> {
                    RocketMQProducerManager.getInstance().sendAsync(o, JSONObject.toJSONString(decodeMessage));
                });
            });
        }
        List<String> mqttList = map.get(RuleEngineCache.TargetType.MQTT);
        if (mqttList != null && !mqttList.isEmpty()) {
            // MQTT 规则转发：携带该设备全部点位最新数据（合并 MessageCache 累积缓存），而非仅当前点位
            String mqttPayload = buildFullDataPayload(decodeMessage);
            threadPoolTaskExecutor.execute(() -> {
                mqttList.forEach(o -> {
                    MqttBrokerUtils.publishMessage(o, mqttPayload);
                });
            });
        }
    }

    /**
     * 构建规则转发载荷：携带该设备全部点位的最新数据
     * 各协议 consume 在发布 device.up 前已调 MessageCache.setDeviceLastData 将本点位累积合并到设备缓存，
     * 此处直接取缓存即得全量属性（含本点位）；缓存为空（如新设备首点）时退化为仅当前点位
     * @param decodeMessage 当前点位解码消息
     * @return 转发 JSON 字符串
     */
    private String buildFullDataPayload(DecodeMessage decodeMessage) {
        DecodeMessage lastData = MessageCache.getDeviceLastData(decodeMessage.getDeviceSn());
        if (lastData == null || lastData.getProperties() == null || lastData.getProperties().isEmpty()) {
            return JSONObject.toJSONString(decodeMessage);
        }
        return JSONObject.toJSONString(lastData);
    }

    /**
     * 设备属性联动告警
     */
    public void linkageWarnProperty(DecodeMessage decodeMessage) {
        WarnLinkUtils.propertyWarn(decodeMessage.getDeviceSn());
    }


    /**
     * 告警消息
     */
    public void warnEngine(Object event) {
        if (event instanceof LabdatahubWarnRecord) {
            LabdatahubWarnRecord warnRecord = (LabdatahubWarnRecord) event;
            Map<RuleEngineCache.TargetType, List<String>> map = RuleEngineCache.getDeviceForwardTargets(RuleEngineCache.DataType.DEVICE_ALARM, warnRecord.getBelongSn());
            if (map.isEmpty()) {
                return;
            }
            // HTTP处理
            List<String> httpTargets = map.get(RuleEngineCache.TargetType.HTTP);
            if (httpTargets != null && !httpTargets.isEmpty()) {
                threadPoolTaskExecutor.execute(() -> {
                    httpTargets.forEach(target -> {
                        HttpManager.sendMessage(target, JSONObject.toJSONString(warnRecord));
                    });
                });
            }

            // RabbitMQ处理
            List<String> rabbitmqTargets = map.get(RuleEngineCache.TargetType.RABBITMQ);
            if (rabbitmqTargets != null && !rabbitmqTargets.isEmpty()) {
                threadPoolTaskExecutor.execute(() -> {
                    rabbitmqTargets.forEach(target -> {
                        RabbitMQUtils.sendMessage(target, JSONObject.toJSONString(warnRecord));
                    });
                });
            }

            // Kafka处理
            List<String> kafkaTargets = map.get(RuleEngineCache.TargetType.KAFKA);
            if (kafkaTargets != null && !kafkaTargets.isEmpty()) {
                threadPoolTaskExecutor.execute(() -> {
                    kafkaTargets.forEach(target -> {
                        KafkaProducerManager.getInstance().sendLogAsync(target, JSONObject.toJSONString(warnRecord));
                    });
                });
            }

            // RocketMQ处理
            List<String> rocketmqTargets = map.get(RuleEngineCache.TargetType.ROCKETMQ);
            if (rocketmqTargets != null && !rocketmqTargets.isEmpty()) {
                threadPoolTaskExecutor.execute(() -> {
                    rocketmqTargets.forEach(target -> {
                        RocketMQProducerManager.getInstance().sendAsync(target, JSONObject.toJSONString(warnRecord));
                    });
                });
            }

            // MQTT处理
            List<String> mqttList = map.get(RuleEngineCache.TargetType.MQTT);
            if (mqttList != null && !mqttList.isEmpty()) {
                threadPoolTaskExecutor.execute(() -> {
                    mqttList.forEach(o -> {
                        MqttBrokerUtils.publishMessage(o, JSONObject.toJSONString(warnRecord));
                    });
                });
            }
        }
    }

    /**
     * 存储消息日志
     */
    public void saveMessage(DecodeMessage decodeMessage) {
        if (decodeMessage.getIsStore()) {
            // 保存设备消息
            LabdatahubDeviceLogs logs = new LabdatahubDeviceLogs();
            logs.setDeviceSn(decodeMessage.getDeviceSn());
            logs.setLogType(DeviceLogType.PROPERTY.name());
            logs.setCreateTime(new Date());
            logs.setReportTime(decodeMessage.getReportTime());
            logs.setProperties(JSONObject.toJSONString(decodeMessage));
            if (LogProcessor.getInstance().isRunning()) {
                LogProcessor.getInstance().addLog(logs);
            } else {
                threadPoolTaskExecutor.execute(() -> {
                    labdatahubDeviceLogsService.save(logs);
                });
            }
        }
    }

    /**
     * 设备状态处理
     */
    public void deviceStatus(DecodeMessage decodeMessage) {
        LabdatahubDevice device = CacheUtils.getDeviceBySn(decodeMessage.getDeviceSn());
        //心跳设备
        if (device != null && "2".equals(device.getDeviceType())) {
            boolean status = CacheUtils.getDeviceStatusBySn(decodeMessage.getDeviceSn());
            if (!status) {
                threadPoolTaskExecutor.execute(() -> {
                    CacheUtils.updateDeviceStatusCache(decodeMessage.getDeviceSn(), true);
                    labdatahubDeviceService.update(new LambdaUpdateWrapper<LabdatahubDevice>()
                            .eq(LabdatahubDevice::getDeviceSn, decodeMessage.getDeviceSn())
                            .set(LabdatahubDevice::getStatus, "1"));
                    //保存上线消息
                    LabdatahubDeviceLogs logs = new LabdatahubDeviceLogs();
                    logs.setDeviceSn(decodeMessage.getDeviceSn());
                    logs.setLogType(DeviceLogType.ONLINE.name());
                    logs.setCreateTime(new Date());
                    logs.setReportTime(decodeMessage.getReportTime());
                    if (LogProcessor.getInstance().isRunning()) {
                        LogProcessor.getInstance().addLog(logs);
                    } else {
                        labdatahubDeviceLogsService.save(logs);
                    }
                    WarnLinkUtils.changeStatusWarn(decodeMessage.getDeviceSn());
                    decodeMessage.getProperties().put("device_online", "1");
                    ruleWarn(new MessageUpEvent("deviceStatusWarn", null, decodeMessage.getDeviceSn(), decodeMessage));
                });
            }
            //自动离线计时
            DeviceHeartbeatManager.updateHeartbeat(device.getDeviceSn(), "1", device.getTimeoutSeconds());
        } else {
            //网关设备和直连设备
            boolean status = CacheUtils.getDeviceStatusBySn(decodeMessage.getDeviceSn());
            if (status != decodeMessage.getIsOnline()) {
                threadPoolTaskExecutor.execute(() -> {
                    //更新状态
                    labdatahubDeviceService.update(new LambdaUpdateWrapper<LabdatahubDevice>()
                            .eq(LabdatahubDevice::getDeviceSn, decodeMessage.getDeviceSn())
                            .set(LabdatahubDevice::getStatus, decodeMessage.getIsOnline() ? "1" : "0"));
                    CacheUtils.updateDeviceStatusCache(decodeMessage.getDeviceSn(), decodeMessage.getIsOnline());
                    //保存上下线消息
                    LabdatahubDeviceLogs logs = new LabdatahubDeviceLogs();
                    logs.setDeviceSn(decodeMessage.getDeviceSn());
                    if (decodeMessage.getIsOnline()) {
                        logs.setLogType(DeviceLogType.ONLINE.name());
                        decodeMessage.getProperties().put("device_online", "1");
                    } else {
                        logs.setLogType(DeviceLogType.OFFLINE.name());
                        decodeMessage.getProperties().put("device_offline", "1");
                    }
                    logs.setCreateTime(new Date());
                    logs.setReportTime(decodeMessage.getReportTime());
                    if (LogProcessor.getInstance().isRunning()) {
                        LogProcessor.getInstance().addLog(logs);
                    } else {
                        labdatahubDeviceLogsService.save(logs);
                    }
                    WarnLinkUtils.changeStatusWarn(decodeMessage.getDeviceSn());
                    ruleWarn(new MessageUpEvent("deviceStatusWarn", null, decodeMessage.getDeviceSn(), decodeMessage));
                });
            }
        }
    }

    /**
     * 规则告警
     */
    public void ruleWarn(MessageUpEvent message) {
        if (message.getData() instanceof DecodeMessage) {
            DecodeMessage decodeMessage = (DecodeMessage) message.getData();
            //用全属性缓存来计算 TODO
//                    DecodeMessage lastData = MessageCache.getDeviceLastData(decodeMessage.getDeviceSn());
            DecodeMessage lastData = decodeMessage;
            List<PropertyNode> propertyNodes = PropertyToJson.PROPERTY_TREE.get(decodeMessage.getDeviceSn());
            if (propertyNodes == null) {
                return;
            }
            String propertyJson = PropertyToJson.convertToJson(propertyNodes, lastData.getProperties());
            List<WarnRule> originRules = CacheUtils.getDeviceWarnRule(decodeMessage.getDeviceSn());
            List<WarnRule> rules = new ArrayList<>();
            if ("deviceStatusWarn".equals(message.getSource())) {
                rules.addAll(originRules.stream().filter(o -> !"0".equals(o.getWarnType())).collect(Collectors.toList()));
            } else {
                rules.addAll(originRules.stream().filter(o -> "0".equals(o.getWarnType())).collect(Collectors.toList()));
            }
            rules.removeIf(rule -> !rule.getEnable());
            if (!rules.isEmpty()) {
                threadPoolTaskExecutor.execute(() -> {
                    rules.forEach(rule -> {
                        long lastWarnTime = CacheUtils.getDeviceRuleWarnTime(rule.getBelongSn(), rule.getId());
                        //规定时间内不再重复告警
                        if (System.currentTimeMillis() - lastWarnTime < rule.getDelayTime() * 1000) {
                            return;
                        }
                        JSONObject propertyData = JSONObject.parseObject(propertyJson);
                        Map<String, Object> msgProperty = decodeMessage.getProperties();
                        if (msgProperty != null) {
                            if (msgProperty.containsKey("device_online")) {
                                propertyData.put("device_online", msgProperty.get("device_online"));
                            }
                            if (msgProperty.containsKey("device_offline")) {
                                propertyData.put("device_offline", msgProperty.get("device_offline"));
                            }
                        }
                        boolean isWarn = WarnRuleMatcher.matchesRule(propertyData, rule);
                        if (isWarn) {
                            String warnMessage = WarnRuleMatcher.generateAlertMessage(JSONObject.parseObject(propertyJson), rule);
                            LabdatahubWarnRecord warnRecord = new LabdatahubWarnRecord();
                            warnRecord.setWarnMessage(warnMessage);
                            warnRecord.setCreateTime(new Date());
                            warnRecord.setBelongSn(rule.getBelongSn());
                            propertyData.remove("device_online");
                            propertyData.remove("device_offline");
                            warnRecord.setWarnData(propertyData.toJSONString());
                            warnRecord.setWarnLevel(rule.getLevel());
                            warnRecord.setConfigId(rule.getId());
                            warnRecord.setConfigName(rule.getName());
                            warnRecord.setStatus("0");
                            warnRecord.setWarnType(rule.getWarnType());
                            if(WarnProcessor.getInstance().isRunning()){
                                WarnProcessor.getInstance().addLog(warnRecord);
                            }else {
                                labdatahubWarnRecordService.save(warnRecord);
                            }
                            SpringUtils.getBean(EventBus.class).publish("device.warn", warnRecord);
                            CacheUtils.updateDeviceRuleWarnTime(rule.getBelongSn(), rule.getId(), System.currentTimeMillis());
                            //动作执行
                            List<LabdatahubFunction> functionList = rule.getActions();
                            LabdatahubDevice device = CacheUtils.getDeviceBySn(decodeMessage.getDeviceSn());
                            if (device == null) {
                                return;
                            }
                            LabdatahubComponent component = SpringUtils.getBean(ILabdatahubComponentService.class).getById(device.getComponentId());
                            if (functionList != null && functionList.size() > 0) {
                                functionList.forEach(function -> {
                                    try {
                                        DeviceDownUtils.functionDown(decodeMessage.getDeviceSn(),
                                                function.getFunctionCode(),
                                                function.getFunctionParams(),
                                                component.getId(),
                                                component.getNetType(),
                                                device.getProtocolId(),
                                                device.getCustomConfig(), "1");
                                    } catch (InvocationTargetException | IllegalAccessException |
                                             MqttException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }
                        }
                    });
                });
            }
        }
    }

    /**
     * 指令下发
     */
    public void functionDown(DecodeMessage decodeMessage) {
        List<FunctionDown> functionList = decodeMessage.getFunctionList();
        LabdatahubDevice device = CacheUtils.getDeviceBySn(decodeMessage.getDeviceSn());
        LabdatahubComponent component = CacheUtils.getComponentCache(device.getComponentId());
        if (functionList != null && functionList.size() > 0) {
            threadPoolTaskExecutor.execute(() -> {
                functionList.forEach(function -> {
                    try {
                        if (StringUtils.isEmpty(function.getFunctionCode())) {
                            return;
                        }
                        if (StringUtils.isEmpty(function.getDeviceSn())) {
                            function.setDeviceSn(device.getDeviceSn());
                        }
                        DeviceDownUtils.functionDown(function.getDeviceSn(),
                                function.getFunctionCode(),
                                function.getFunctionParams(),
                                component.getId(),
                                component.getNetType(),
                                device.getProtocolId(),
                                device.getCustomConfig(), "0");
                    } catch (InvocationTargetException | IllegalAccessException | MqttException e) {
                        log.error("指令下发失败：" + e.getMessage());
                    }
                });
            });
        }
    }


    /**
     * 直连设备离线处理
     */
    public void directlyConnectedOffline(Object event) {
        threadPoolTaskExecutor.execute(() -> {
            if (event instanceof String) {
                String deviceSn = (String) event;
                LabdatahubDevice labdatahubDevice = CacheUtils.getDeviceBySn(deviceSn);
                if("2".equals(labdatahubDevice.getDeviceType())){
                    return;
                }
                labdatahubDeviceService.update(new LambdaUpdateWrapper<LabdatahubDevice>()
                        .eq(LabdatahubDevice::getDeviceSn, deviceSn)
                        .set(LabdatahubDevice::getStatus, "0"));
                CacheUtils.updateDeviceStatusCache(deviceSn, false);
                //保存上下线消息
                LabdatahubDeviceLogs logs = new LabdatahubDeviceLogs();
                logs.setDeviceSn(deviceSn);
                logs.setLogType(DeviceLogType.OFFLINE.name());
                logs.setCreateTime(new Date());
                logs.setReportTime(new Date());
                if (LogProcessor.getInstance().isRunning()) {
                    LogProcessor.getInstance().addLog(logs);
                } else {
                    labdatahubDeviceLogsService.save(logs);
                }
                WarnLinkUtils.changeStatusWarn(deviceSn);
                DecodeMessage decodeMessage = new DecodeMessage();
                decodeMessage.setIsStore(true);
                decodeMessage.setDeviceSn(deviceSn);
                decodeMessage.setProperties(new HashMap<>());
                decodeMessage.getProperties().put("device_offline", "1");
                MessageUpEvent event1 = new MessageUpEvent("deviceStatusWarn", null, deviceSn, decodeMessage);
                ruleWarn(event1);
            }
            if (event instanceof Set) {
                Set<String> deviceSnList = (Set<String>) event;
                labdatahubDeviceService.update(new LambdaUpdateWrapper<LabdatahubDevice>()
                        .in(LabdatahubDevice::getDeviceSn, deviceSnList)
                        .set(LabdatahubDevice::getStatus, "0"));
                deviceSnList.forEach(deviceSn -> {
                    CacheUtils.updateDeviceStatusCache(deviceSn, false);
                    //保存上下线消息
                    LabdatahubDeviceLogs logs = new LabdatahubDeviceLogs();
                    logs.setDeviceSn(deviceSn);
                    logs.setLogType(DeviceLogType.OFFLINE.name());
                    logs.setCreateTime(new Date());
                    logs.setReportTime(new Date());
                    if (LogProcessor.getInstance().isRunning()) {
                        LogProcessor.getInstance().addLog(logs);
                    } else {
                        labdatahubDeviceLogsService.save(logs);
                    }
                    WarnLinkUtils.changeStatusWarn(deviceSn);
                    DecodeMessage decodeMessage = new DecodeMessage();
                    decodeMessage.setIsStore(true);
                    decodeMessage.setDeviceSn(deviceSn);
                    decodeMessage.setProperties(new HashMap<>());
                    decodeMessage.getProperties().put("device_offline", "1");
                    MessageUpEvent event1 = new MessageUpEvent("deviceStatusWarn", null, deviceSn, decodeMessage);
                    ruleWarn(event1);
                });
            }
        });
    }

    /**
     * 设备自注册
     */
    public void deviceRegister(DecodeMessage decodeMessage) {
        if (!"true".equals(CacheUtils.DEVICE_REGISTER_SWITCH)) {
            return;
        }
        threadPoolTaskExecutor.execute(() -> {
            if (decodeMessage.getIsRegister()) {
                if (StringUtils.isNotEmpty(decodeMessage.getDeviceSn()) && StringUtils.isNotEmpty(decodeMessage.getProductSn()) && StringUtils.isNotEmpty(decodeMessage.getDeviceName())) {
                    if (CacheUtils.getDeviceBySn(decodeMessage.getDeviceSn()) == null) {
                        LabdatahubProduct product = CacheUtils.PRODUCT_MAP.getOrDefault(decodeMessage.getProductSn(), null);
                        if (product == null) {
                            return;
                        }
                        LabdatahubDevice device = new LabdatahubDevice();
                        device.setDeviceSn(decodeMessage.getDeviceSn());
                        device.setDeviceType(product.getDeviceType());
                        device.setDeviceName(decodeMessage.getDeviceName());
                        device.setProductSn(product.getProductSn());
                        device.setProductName(product.getProductName());
                        device.setProductId(product.getId());
                        device.setProtocolId(product.getProtocolId());
                        device.setProtocolName(product.getProtocolName());
                        device.setComponentId(product.getComponentId());
                        device.setComponentName(product.getComponentName());
                        device.setCustomConfig(product.getCustomConfig());
                        device.setLinkMethodId(product.getLinkMethodId());
                        device.setLinkMethodName(product.getLinkMethodName());
                        device.setModbusRead("0");
                        device.setStatus("1");
                        device.setTimeoutSeconds(product.getTimeoutSeconds());
                        device.setRegularCleaning(product.getRegularCleaning());
                        device.setRetentionTime(product.getRetentionTime());
                        device.setRetentionUnit(product.getRetentionUnit());
                        device.setCreateBy(product.getCreateBy());
                        device.setCreateTime(new Date());
                        labdatahubDeviceService.saveDeviceByProductSn(device);
                    }
                }
            }
        });
    }
}
