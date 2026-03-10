package com.labdatahub.business.service.impl;

import java.io.IOException;
import java.util.*;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.engine.entity.ProductConfig;
import com.labdatahub.business.engine.entity.RuleEngineConfig;
import com.labdatahub.business.engine.entity.RuleNode;
import com.labdatahub.business.engine.utils.RuleEngineCache;
import com.labdatahub.business.engine.utils.RuleGroupExtractor;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.common.core.domain.R;
import com.labdatahub.common.exception.CommonWarnException;
import com.labdatahub.common.utils.DateUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.component.mqtt.server.MqttBrokerConfig;
import com.labdatahub.component.mqtt.server.MqttBrokerManager;
import com.labdatahub.component.rule_engine.mq.http.HttpConfig;
import com.labdatahub.component.rule_engine.mq.http.HttpManager;
import com.labdatahub.component.rule_engine.mq.kafka.KafkaConfig;
import com.labdatahub.component.rule_engine.mq.kafka.KafkaProducerManager;
import com.labdatahub.component.rule_engine.mq.mqtt.MqttBrokerUtils;
import com.labdatahub.component.rule_engine.mq.mqtt.MqttConfig;
import com.labdatahub.component.rule_engine.mq.rabbitmq.RabbitConfig;
import com.labdatahub.component.rule_engine.mq.rabbitmq.RabbitMQConfig;
import com.labdatahub.component.rule_engine.mq.rabbitmq.RabbitMQUtils;
import com.labdatahub.component.rule_engine.mq.rocketmq.RocketConfig;
import com.labdatahub.component.rule_engine.mq.rocketmq.RocketMQProducerManager;
import com.labdatahub.component.utils.PortChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubRuleEngineMapper;
import com.labdatahub.business.domain.LabdatahubRuleEngine;
import com.labdatahub.business.service.ILabdatahubRuleEngineService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 规则引擎配置Service业务层处理
 *
 * @author labdatahub
 * @date 2025-10-20
 */
@Service
public class LabdatahubRuleEngineServiceImpl extends ServiceImpl<LabdatahubRuleEngineMapper, LabdatahubRuleEngine> implements ILabdatahubRuleEngineService
{
    @Autowired
    private LabdatahubRuleEngineMapper labdatahubRuleEngineMapper;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;

    /**
     * 查询规则引擎配置
     *
     * @param id 规则引擎配置主键
     * @return 规则引擎配置
     */
    @Override
    public LabdatahubRuleEngine selectLabdatahubRuleEngineById(String id)
    {
        return labdatahubRuleEngineMapper.selectLabdatahubRuleEngineById(id);
    }

    /**
     * 查询规则引擎配置列表
     *
     * @param labdatahubRuleEngine 规则引擎配置
     * @return 规则引擎配置
     */
    @Override
    public List<LabdatahubRuleEngine> selectLabdatahubRuleEngineList(LabdatahubRuleEngine labdatahubRuleEngine)
    {
        return labdatahubRuleEngineMapper.selectLabdatahubRuleEngineList(labdatahubRuleEngine);
    }

    /**
     * 新增规则引擎配置
     *
     * @param labdatahubRuleEngine 规则引擎配置
     * @return 结果
     */
    @Override
    public int insertLabdatahubRuleEngine(LabdatahubRuleEngine labdatahubRuleEngine)
    {
        labdatahubRuleEngine.setCreateTime(DateUtils.getNowDate());
        return labdatahubRuleEngineMapper.insertLabdatahubRuleEngine(labdatahubRuleEngine);
    }

    /**
     * 修改规则引擎配置
     *
     * @param labdatahubRuleEngine 规则引擎配置
     * @return 结果
     */
    @Override
    public int updateLabdatahubRuleEngine(LabdatahubRuleEngine labdatahubRuleEngine)
    {
        return labdatahubRuleEngineMapper.updateLabdatahubRuleEngine(labdatahubRuleEngine);
    }

    /**
     * 批量删除规则引擎配置
     *
     * @param ids 需要删除的规则引擎配置主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubRuleEngineByIds(String[] ids)
    {
        return labdatahubRuleEngineMapper.deleteLabdatahubRuleEngineByIds(ids);
    }

    /**
     * 删除规则引擎配置信息
     *
     * @param id 规则引擎配置主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubRuleEngineById(String id)
    {
        return labdatahubRuleEngineMapper.deleteLabdatahubRuleEngineById(id);
    }

    @Override
    public boolean startRuleEngine(String id) {
        LabdatahubRuleEngine labdatahubRuleEngine = labdatahubRuleEngineMapper.selectById(id);
        RuleEngineConfig config = JSONObject.parseObject(labdatahubRuleEngine.getConfigJson(),RuleEngineConfig.class);
        List<List<RuleNode>> ruleList = RuleGroupExtractor.extractValidGroups(config);
        String oldEngineConfig = RuleEngineCache.getJsonCache(labdatahubRuleEngine.getId());
        if(StringUtils.isNotEmpty(oldEngineConfig)){
            //清除旧规则缓存
            RuleEngineConfig oldConfig = JSONObject.parseObject(oldEngineConfig,RuleEngineConfig.class);
            List<List<RuleNode>> oldRuleList = RuleGroupExtractor.extractValidGroups(oldConfig);
            oldRuleList.forEach(ruleNodes->{
                RuleNode nodeType = ruleNodes.get(1);
                RuleNode output = ruleNodes.get(2);
                if(!"realTimePush".equals(nodeType.getType())){
                    return;
                }
                String componentId = id+"_"+output.getId();
                switch (output.getType()){
                    case "RabbitMQ":{
                        RabbitMQUtils.removeConnection(componentId);
                        RuleEngineCache.removeRabbitMq(componentId);
                        break;
                    }
                    case "Kafka":{
                        KafkaProducerManager.getInstance().closeProducer(componentId);
                        RuleEngineCache.removeKafka(componentId);
                        break;
                    }
                    case "RocketMQ":{
                        RocketMQProducerManager.getInstance().closeProducer(componentId);
                        RuleEngineCache.removeRocket(componentId);
                        break;
                    }
                    case "HTTP":{
                        HttpManager.removeConfig(componentId);
                        RuleEngineCache.removeHttp(componentId);
                        break;
                    }
                    case "MQTT":{
                        MqttBrokerUtils.stopBroker(componentId);
                        RuleEngineCache.removeMqtt(componentId);
                        break;
                    }
                    default:
                }
            });
        }
        RuleEngineCache.updateJsonCache(labdatahubRuleEngine.getId(), labdatahubRuleEngine.getConfigJson());
        ruleList.forEach(ruleNodes -> {
            RuleNode input = ruleNodes.get(0);
            RuleNode nodeType = ruleNodes.get(1);
            RuleNode output = ruleNodes.get(2);
            if(!"realTimePush".equals(nodeType.getType())){
                return;
            }
            ProductConfig productConfig = input.getConfigData().toJavaObject(ProductConfig.class);
            List<LabdatahubDevice> deviceList = new ArrayList<>();
            if("all".equals(productConfig.getDeviceScope())){
                if(productConfig.getProductScope()==null||productConfig.getProductScope().isEmpty()){
                    return;
                }
                deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                        .in(LabdatahubDevice::getProductSn,productConfig.getProductScope()));
            }else if("part".equals(productConfig.getDeviceScope())){
                if(productConfig.getDeviceSnList()==null||productConfig.getDeviceSnList().isEmpty()){
                    return;
                }
                deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                        .in(LabdatahubDevice::getDeviceSn,productConfig.getDeviceSnList()));
            }
            String componentId = id+"_"+output.getId();
            switch (output.getType()){
                case "RabbitMQ":{
                    RabbitConfig rabbitConfig = output.getConfigData().toJavaObject(RabbitConfig.class);
                    RabbitMQConfig mqConfig = new RabbitMQConfig();
                    mqConfig.setHost(rabbitConfig.getHost());
                    mqConfig.setPort(rabbitConfig.getPort());
                    mqConfig.setUsername(rabbitConfig.getUsername());
                    mqConfig.setPassword(rabbitConfig.getPassword());
                    RabbitMQUtils.createConnection(componentId,mqConfig,rabbitConfig.getExchange(),rabbitConfig.getRoutingKey());
                    deviceList.forEach(device->{
                        RuleEngineCache.addRabbitMq(device.getDeviceSn(),input.getType(),componentId);
                    });
                    break;
                }
                case "Kafka":{
                    KafkaConfig kafkaConfig = output.getConfigData().toJavaObject(KafkaConfig.class);
                    KafkaProducerManager.getInstance().createProducer(componentId,kafkaConfig.getIpAddress()+":"+kafkaConfig.getPort(),null,kafkaConfig.getTopic());
                    deviceList.forEach(device->{
                        RuleEngineCache.addKafkaMq(device.getDeviceSn(),input.getType(),componentId);
                    });
                    break;
                }
                case "RocketMQ":{
                    RocketConfig rocketConfig = output.getConfigData().toJavaObject(RocketConfig.class);
                    RocketMQProducerManager.getInstance().createProducer(componentId,rocketConfig);
                    deviceList.forEach(device->{
                        RuleEngineCache.addRocketMq(device.getDeviceSn(),input.getType(),componentId);
                    });
                    break;
                }
                case "HTTP":{
                    HttpConfig httpConfig = output.getConfigData().toJavaObject(HttpConfig.class);
                    HttpManager.addConfig(componentId,httpConfig);
                    deviceList.forEach(device->{
                        RuleEngineCache.addHttp(device.getDeviceSn(),input.getType(),componentId);
                    });
                    break;
                }
                case "MQTT":{
                    MqttConfig mqttConfig = output.getConfigData().toJavaObject(MqttConfig.class);
                    if(!PortChecker.isLocalPortAvailable(Integer.parseInt(mqttConfig.getPort()))){
                        throw new RuntimeException("端口被占用");
                    }
                    try {
                        MqttBrokerUtils.addBroker(componentId,mqttConfig.getPort(),mqttConfig.getTopic(),mqttConfig.getAllowAnonymous(),mqttConfig.getUsername(),mqttConfig.getPassword());
                    } catch (IOException e) {
                        throw new RuntimeException("启动MQTT服务失败");
                    }
                    deviceList.forEach(device->{
                        RuleEngineCache.addMqtt(device.getDeviceSn(),input.getType(),componentId);
                    });
                    break;
                }
                default:
            }
        });
        LabdatahubRuleEngine engine = new LabdatahubRuleEngine();
        engine.setId(id);
        engine.setIsEnable("1");
        baseMapper.updateById(engine);
        return true;
    }

    @Override
    public boolean stopRuleEngine(String id) {
        LabdatahubRuleEngine labdatahubRuleEngine = labdatahubRuleEngineMapper.selectById(id);
        if(StringUtils.isNotEmpty(labdatahubRuleEngine.getConfigJson())){
            RuleEngineConfig config = JSONObject.parseObject(labdatahubRuleEngine.getConfigJson(),RuleEngineConfig.class);
            List<List<RuleNode>> ruleList = RuleGroupExtractor.extractValidGroups(config);
            ruleList.forEach(ruleNodes->{
                RuleNode nodeType = ruleNodes.get(1);
                RuleNode output = ruleNodes.get(2);
                if(!"realTimePush".equals(nodeType.getType())){
                    return;
                }
                String componentId = id+"_"+output.getId();
                switch (output.getType()){
                    case "RabbitMQ":{
                        RabbitMQUtils.removeConnection(componentId);
                        RuleEngineCache.removeRabbitMq(componentId);
                        break;
                    }
                    case "Kafka":{
                        KafkaProducerManager.getInstance().closeProducer(componentId);
                        RuleEngineCache.removeKafka(componentId);
                        break;
                    }
                    case "RocketMQ":{
                        RocketMQProducerManager.getInstance().closeProducer(componentId);
                        RuleEngineCache.removeRocket(componentId);
                        break;
                    }
                    case "HTTP":{
                        HttpManager.removeConfig(componentId);
                        RuleEngineCache.removeHttp(componentId);
                        break;
                    }
                    case "MQTT":{
                        MqttBrokerUtils.stopBroker(componentId);
                        RuleEngineCache.removeMqtt(componentId);
                        break;
                    }
                    default:
                }
            });
        }
        RuleEngineCache.deleteJsonCache(id);
        LabdatahubRuleEngine engine = new LabdatahubRuleEngine();
        engine.setId(id);
        engine.setIsEnable("0");
        baseMapper.updateById(engine);
        return true;
    }
}
