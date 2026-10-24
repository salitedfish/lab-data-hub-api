//由AI修改
package com.labdatahub.business.service.impl;

import java.io.IOException;
import java.util.*;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
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
import com.labdatahub.business.mapper.LabdatahubRuleEngineControlLogMapper;
import com.labdatahub.business.domain.LabdatahubRuleEngine;
import com.labdatahub.business.domain.LabdatahubRuleEngineControlLog;
import com.labdatahub.common.utils.SecurityUtils;
import com.labdatahub.common.utils.uuid.IdUtils;
import com.labdatahub.business.service.ILabdatahubRuleEngineService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 规则引擎配置Service业务层处理
 *
 * @author labdatahub
 * @date 2025-10-20
 */
@Service
@Slf4j
public class LabdatahubRuleEngineServiceImpl extends ServiceImpl<LabdatahubRuleEngineMapper, LabdatahubRuleEngine> implements ILabdatahubRuleEngineService
{
    @Autowired
    private LabdatahubRuleEngineMapper labdatahubRuleEngineMapper;
    @Autowired
    private LabdatahubRuleEngineControlLogMapper labdatahubRuleEngineControlLogMapper;
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
        try {
            boolean result = doStartRuleEngine(id);
            if (result) {
                saveControlLog(id, "start", "0", "启用成功");
            }
            return result;
        } catch (RuntimeException e) {
            //启动失败同样要留痕：页面只弹错误提示，事后查不到是谁、在什么时候启的、为什么失败
            saveControlLog(id, "start", "1", e.getMessage() == null ? e.toString() : e.getMessage());
            throw e;
        }
    }

    /**
     * 规则引擎启用（不含启停日志记录）
     *
     * @param id 规则引擎id
     * @return 结果
     */
    private boolean doStartRuleEngine(String id) {
        LabdatahubRuleEngine labdatahubRuleEngine = labdatahubRuleEngineMapper.selectById(id);
        if(labdatahubRuleEngine == null){
            log.warn("启动规则引擎失败，规则不存在：id={}", id);
            return false;
        }
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
            // 输入侧设备范围只决定「转发哪些设备的消息」，不决定「输出服务起不起」：
            // 范围缺失/为空时 deviceList 保持为空，下面的输出服务（MQTT/Kafka/...）照常启动，只是没有消息进来
            ProductConfig productConfig = null;
            if(input.getConfigData() == null){
                log.warn("规则引擎{} 的输入节点[{}]未配置数据源，仅启动输出服务、不绑定设备", id, input.getName());
            }else {
                productConfig = input.getConfigData().toJavaObject(ProductConfig.class);
            }
            List<LabdatahubDevice> deviceList = new ArrayList<>();
            if(productConfig != null && "all".equals(productConfig.getDeviceScope())){
                if(productConfig.getProductScope()==null||productConfig.getProductScope().isEmpty()){
                    log.warn("规则引擎{} 的输入节点[{}]未选择产品范围，仅启动输出服务、不绑定设备", id, input.getName());
                }else {
                    deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                            .in(LabdatahubDevice::getProductSn,productConfig.getProductScope()));
                }
            }else if(productConfig != null && "part".equals(productConfig.getDeviceScope())){
                if(productConfig.getDeviceSnList()==null||productConfig.getDeviceSnList().isEmpty()){
                    log.warn("规则引擎{} 的输入节点[{}]未选择设备，仅启动输出服务、不绑定设备", id, input.getName());
                }else {
                    deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                            .in(LabdatahubDevice::getDeviceSn,productConfig.getDeviceSnList()));
                }
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
        try {
            boolean result = doStopRuleEngine(id);
            if (result) {
                saveControlLog(id, "stop", "0", "停用成功");
            }
            return result;
        } catch (RuntimeException e) {
            saveControlLog(id, "stop", "1", e.getMessage() == null ? e.toString() : e.getMessage());
            throw e;
        }
    }

    /**
     * 规则引擎停用（不含启停日志记录）
     *
     * @param id 规则引擎id
     * @return 结果
     */
    private boolean doStopRuleEngine(String id) {
        LabdatahubRuleEngine labdatahubRuleEngine = labdatahubRuleEngineMapper.selectById(id);
        if(labdatahubRuleEngine == null){
            log.warn("关闭规则引擎失败，规则不存在：id={}", id);
            return false;
        }
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

    /**
     * 记录一次规则引擎启停（失败原因一并落库）
     *
     * @param engineId 规则引擎id
     * @param action   操作 start-启用 stop-停用
     * @param status   结果 0-成功 1-失败
     * @param message  说明（失败原因）
     */
    private void saveControlLog(String engineId, String action, String status, String message) {
        try {
            LabdatahubRuleEngine engine = labdatahubRuleEngineMapper.selectById(engineId);
            LabdatahubRuleEngineControlLog controlLog = new LabdatahubRuleEngineControlLog();
            controlLog.setId(IdUtils.fastSimpleUUID());
            controlLog.setEngineId(engineId);
            controlLog.setEngineName(engine == null ? null : engine.getEngineName());
            controlLog.setAction(action);
            controlLog.setOperName(getCurrentOperName());
            controlLog.setOperTime(new Date());
            controlLog.setStatus(status);
            if (message != null && message.length() > 500) {
                message = message.substring(0, 500);
            }
            controlLog.setMessage(message);
            labdatahubRuleEngineControlLogMapper.insert(controlLog);
        } catch (Exception e) {
            //日志表写失败不能反过来影响启停本身
            log.error("记录规则引擎启停日志失败：id={}，action={}", engineId, action, e);
        }
    }

    /**
     * 当前操作人，无登录态（启动时自动恢复规则引擎）记为 system
     *
     * @return 操作人
     */
    private String getCurrentOperName() {
        try {
            return SecurityUtils.getUsername();
        } catch (Exception e) {
            return "system";
        }
    }
}
