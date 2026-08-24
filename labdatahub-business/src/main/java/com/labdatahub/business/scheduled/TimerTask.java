package com.labdatahub.business.scheduled;

import static com.labdatahub.business.service.impl.LabdatahubProtocolServiceImpl.PROTOCOL_PATH;
import static com.labdatahub.business.utils.CacheUtils.updateAllDeviceCache;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.labdatahub.business.domain.LabdatahubComponent;
import com.labdatahub.business.domain.LabdatahubDbConfig;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.domain.LabdatahubDeviceLogs;
import com.labdatahub.business.domain.LabdatahubFunction;
import com.labdatahub.business.domain.LabdatahubBrotherConfig;
import com.labdatahub.business.domain.LabdatahubFanucConfig;
import com.labdatahub.business.domain.LabdatahubModbusConfig;
import com.labdatahub.business.domain.LabdatahubOmronFinsConfig;
import com.labdatahub.business.domain.LabdatahubProduct;
import com.labdatahub.business.domain.LabdatahubProtocol;
import com.labdatahub.business.domain.LabdatahubRuleEngine;
import com.labdatahub.business.domain.LabdatahubS71200Config;
import com.labdatahub.business.domain.LabdatahubScheduledTask;
import com.labdatahub.business.domain.LabdatahubWarnLinkage;
import com.labdatahub.business.event.DeviceHeartbeatManager;
import com.labdatahub.business.process.function.DatabaseFunctionConsumer;
import com.labdatahub.business.process.function.FunctionConsumer;
import com.labdatahub.business.process.function.FunctionProcessor;
import com.labdatahub.business.process.linkage.DatabaseLinkageConsumer;
import com.labdatahub.business.process.linkage.LinkageConsumer;
import com.labdatahub.business.process.linkage.LinkageProcessor;
import com.labdatahub.business.process.log.DatabaseLogConsumer;
import com.labdatahub.business.process.log.LogConsumer;
import com.labdatahub.business.process.log.LogProcessor;
import com.labdatahub.business.process.warn.DatabaseWarnConsumer;
import com.labdatahub.business.process.warn.WarnConsumer;
import com.labdatahub.business.process.warn.WarnProcessor;
import com.labdatahub.business.service.ILabdatahubComponentService;
import com.labdatahub.business.service.ILabdatahubDbConfigService;
import com.labdatahub.business.service.ILabdatahubDeviceLogsService;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubFunctionRecordService;
import com.labdatahub.business.service.ILabdatahubFunctionService;
import com.labdatahub.business.service.ILabdatahubLinkageWarnRecordService;
import com.labdatahub.business.service.ILabdatahubBrotherConfigService;
import com.labdatahub.business.service.ILabdatahubFanucConfigService;
import com.labdatahub.business.service.ILabdatahubModbusConfigService;
import com.labdatahub.business.service.ILabdatahubOmronFinsConfigService;
import com.labdatahub.business.service.ILabdatahubProductService;
import com.labdatahub.business.service.ILabdatahubProtocolService;
import com.labdatahub.business.service.ILabdatahubRuleEngineService;
import com.labdatahub.business.service.ILabdatahubS71200ConfigService;
import com.labdatahub.business.service.ILabdatahubScheduledTaskService;
import com.labdatahub.business.service.ILabdatahubWarnLinkageService;
import com.labdatahub.business.service.ILabdatahubWarnRecordService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.business.utils.ParseMetaUtils;
import com.labdatahub.common.core.redis.RedisCache;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.component.brother_tcp.BrotherTcpMessageScheduler;
import com.labdatahub.component.brother_tcp.BrotherTcpReadConfig;
import com.labdatahub.component.fanuc_focas.FanucFocasMessageScheduler;
import com.labdatahub.component.fanuc_focas.FanucFocasReadConfig;
import com.labdatahub.component.db.DatabaseMessageScheduler;
import com.labdatahub.component.db.DatabaseReadConfig;
import com.labdatahub.component.fins_tcp.FinsMessageScheduler;
import com.labdatahub.component.fins_tcp.FinsReadConfig;
import com.labdatahub.component.message.DecodeMessage;
import com.labdatahub.component.message.MessageCache;
import com.labdatahub.component.modbus_tcp.ModbusMessageScheduler;
import com.labdatahub.component.modbus_tcp.ModbusReadConfig;
import com.labdatahub.component.protocol.ProtocolManager;
import com.labdatahub.component.s7_tcp.S7MessageScheduler;
import com.labdatahub.component.s7_tcp.S7ReadConfig;

import cn.hutool.cron.CronUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 定时任务和初始化数据
 * @Author: labdatahub
 * @CreateTime: 2025-09-22
 */
@Slf4j
@Component
public class TimerTask {
    @Autowired
    private ILabdatahubComponentService labdatahubComponentService;
    @Autowired
    private ILabdatahubProtocolService labdatahubProtocolService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    @Autowired
    private ILabdatahubDeviceLogsService labdatahubDeviceLogsService;
    @Autowired
    private ILabdatahubRuleEngineService labdatahubRuleEngineService;
    @Autowired
    private ILabdatahubWarnLinkageService labdatahubWarnLinkageService;
    @Autowired
    private ILabdatahubScheduledTaskService labdatahubScheduledTaskService;
    @Autowired
    private ILabdatahubModbusConfigService labdatahubModbusConfigService;
    @Autowired
    private ILabdatahubProductService labdatahubProductService;
    @Autowired
    private ILabdatahubFunctionService labdatahubFunctionService;
    @Autowired
    private ILabdatahubFunctionRecordService labdatahubFunctionRecordService;
    @Autowired
    private ILabdatahubWarnRecordService labdatahubWarnRecordService;
    @Autowired
    private ILabdatahubLinkageWarnRecordService labdatahubLinkageWarnRecordService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ILabdatahubS71200ConfigService labdatahubS71200ConfigService;
    @Autowired
    private ILabdatahubOmronFinsConfigService labdatahubOmronFinsConfigService;
    @Autowired
    private ILabdatahubBrotherConfigService labdatahubBrotherConfigService;
    @Autowired
    private ILabdatahubFanucConfigService labdatahubFanucConfigService;
    @Autowired
    private ILabdatahubDbConfigService labdatahubDbConfigService;
    /**
     * 初始化组件和协议
     */
    @PostConstruct
    public void initComponentAndProtocol() {
        threadPoolTaskExecutor.execute(() -> {
            List<LabdatahubComponent> components = labdatahubComponentService.list(new LambdaQueryWrapper<LabdatahubComponent>()
                    .eq(LabdatahubComponent::getStatus, "1"));
            components.forEach(component -> {
                CacheUtils.setComponentCache(component.getId(),component);
                //先加载相关协议
                if (StringUtils.isNotEmpty(component.getProtocolId())) {
                    LabdatahubProtocol protocol = labdatahubProtocolService.getById(component.getProtocolId());
                    try {
                        ProtocolManager.addProtocol(protocol.getId(),protocol.getProtocolType(), PROTOCOL_PATH + File.separator + protocol.getNewName(), protocol.getMainClassPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    labdatahubComponentService.openComponent(component.getId());
                } catch (Exception e) {
                    //开启失败则状态置为关闭
                    LabdatahubComponent closeComponent = new LabdatahubComponent();
                    closeComponent.setId(component.getId());
                    closeComponent.setStatus("0");
                    labdatahubComponentService.updateById(closeComponent);
                    throw new RuntimeException(e);
                }
            });
        });
    }

    /**
     * 初始化设备属性缓存和状态缓存、和产品缓存
     */
    @PostConstruct
    public void initDeviceProperty() {
        List<LabdatahubProduct> labdatahubProduct = labdatahubProductService.list();
        labdatahubProduct.forEach(product->{
            CacheUtils.PRODUCT_MAP.put(product.getProductSn(),product);
        });
        List<LabdatahubDevice> labdatahubDevice = labdatahubDeviceService.list();
        labdatahubDevice.forEach(device -> {
            threadPoolTaskExecutor.execute(() -> {
                CacheUtils.updateDeviceCache(device);
                labdatahubDeviceService.cacheDeviceProperties(device.getDeviceSn());
            });
        });
        labdatahubDevice.forEach(device -> {
            threadPoolTaskExecutor.execute(() -> {
                CacheUtils.updateDeviceStatusCache(device);
                //无状态设备状态管理
                if("2".equals(device.getDeviceType())&&"1".equals(device.getStatus())) {
                    DeviceHeartbeatManager.updateHeartbeat(device.getDeviceSn(), device.getStatus(), device.getTimeoutSeconds());
                }
            });
        });
        updateAllDeviceCache(labdatahubDevice);
        labdatahubDevice.forEach(device -> {
            if("2".equals(device.getDeviceType())&&"1".equals(device.getStatus())){
                DeviceHeartbeatManager.updateHeartbeat(device.getDeviceSn(),"1",device.getTimeoutSeconds());
            }
        });
        long functionCount = labdatahubFunctionService.count();
        int batchSize = 1000;
        int totalPages = (int) Math.ceil((double) functionCount / batchSize);
        List<LabdatahubFunction> totalFunction = new ArrayList<>();
        for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
            // 每次查询1000条数据
            Page<LabdatahubFunction> page = labdatahubFunctionService.page(
                    new Page<>(pageNum, batchSize)
            );
            List<LabdatahubFunction> batchList = page.getRecords();
            if (!batchList.isEmpty()) {
                totalFunction.addAll(batchList);
            }
        }
        CacheUtils.DEVICE_FUNCTION = new ConcurrentHashMap<>(
                totalFunction.stream()
                        .filter(Objects::nonNull)
                        .filter(func -> StringUtils.isNotBlank(func.getBelongSn()))
                        .collect(Collectors.groupingBy(
                                LabdatahubFunction::getBelongSn,
                                Collectors.toList()
                        ))
        );
    }

    /**
     * 初始化设备告警规则缓存
     */
    @PostConstruct
    public void initDeviceWarnRule(){
        threadPoolTaskExecutor.execute(CacheUtils::updateAllDeviceWarnRule);
    }

    /**
     * 定时清理设备数据,间隔10分钟一次
     */
    @Scheduled(fixedDelay = 1000 * 10 * 60)
    public void cleanDeviceData(){
        List<LabdatahubDevice> deviceList = new ArrayList<>(CacheUtils.DEVICE_MAP.values());
        deviceList.forEach(device->{
            if("0".equals(device.getRegularCleaning())){
                return;
            }
            try {
                LocalDateTime startTime = LocalDateTime.now();
                if("hour".equals(device.getRetentionUnit())){
                    startTime = startTime.minusHours(device.getRetentionTime());
                }
                if("day".equals(device.getRetentionUnit())){
                    startTime = startTime.minusDays(device.getRetentionTime());
                }
                if("week".equals(device.getRetentionUnit())){
                    startTime = startTime.minusWeeks(device.getRetentionTime());
                }
                if("month".equals(device.getRetentionUnit())){
                    startTime = startTime.minusMonths(device.getRetentionTime());
                }
                if("year".equals(device.getRetentionUnit())){
                    startTime = startTime.minusYears(device.getRetentionTime());
                }
                DecodeMessage decodeMessage = MessageCache.getDeviceLastData(device.getDeviceSn());
                //无数据设备不执行删除
                if(decodeMessage==null){
                    return;
                }
                labdatahubDeviceLogsService.remove(new LambdaUpdateWrapper<LabdatahubDeviceLogs>()
                        .eq(LabdatahubDeviceLogs::getDeviceSn,device.getDeviceSn())
                        .lt(LabdatahubDeviceLogs::getCreateTime,startTime));
            }catch (Exception e){
                log.error(device.getDeviceName()+"：定时清理数据失败");
            }
        });
    }

    /**
     * 初始化规则引擎
     */
    @PostConstruct
    public void initRuleEngine(){
        threadPoolTaskExecutor.execute(()->{
            List<LabdatahubRuleEngine> ruleEngines = labdatahubRuleEngineService.list(new LambdaQueryWrapper<LabdatahubRuleEngine>()
                    .eq(LabdatahubRuleEngine::getIsEnable,"1"));
            ruleEngines.forEach(ruleEngine->{
                try {
                    labdatahubRuleEngineService.startRuleEngine(ruleEngine.getId());
                }catch (Exception ignored){}
            });
        });
    }

    /**
     * 初始化设备联动告警
     */
    @PostConstruct
    public void initLinkageWarn(){
        threadPoolTaskExecutor.execute(()->{
            List<LabdatahubWarnLinkage> ruleEngines = labdatahubWarnLinkageService.list(new LambdaQueryWrapper<LabdatahubWarnLinkage>()
                    .eq(LabdatahubWarnLinkage::getIsEnable,"1"));
            ruleEngines.forEach(ruleEngine->{
                if("1".equals(ruleEngine.getIsEnable())){
                    try {
                        labdatahubWarnLinkageService.control(ruleEngine.getId(),"1");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
    }

    /**
     * 初始化定时任务
     */
    @PostConstruct
    public void initScheduledTask(){
        List<LabdatahubScheduledTask> list = labdatahubScheduledTaskService.list(new LambdaQueryWrapper<LabdatahubScheduledTask>()
                .eq(LabdatahubScheduledTask::getIsEnable,"1"));
        list.forEach(o->{
            try {
                labdatahubScheduledTaskService.control(o.getId(),"1");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    /**
     * 初始化modbus定时读取
     */
    @PostConstruct
    public void initModbusTcpRead(){
        threadPoolTaskExecutor.execute(()->{
            List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                    .eq(LabdatahubDevice::getModbusRead,"1"));
            deviceList.forEach(device->{
                // 只拉起 Modbus 网络组件下设备的轮询
                if(StringUtils.isEmpty(device.getComponentId())){
                    return;
                }
                LabdatahubComponent component = labdatahubComponentService.getById(device.getComponentId());
                if(component == null || !"MODBUS_TCP".equals(component.getNetType())){
                    return;
                }
                List<LabdatahubModbusConfig> list = labdatahubModbusConfigService.list(new LambdaQueryWrapper<LabdatahubModbusConfig>()
                        .eq(LabdatahubModbusConfig::getBelongSn,device.getDeviceSn()));
                list.forEach(o->{
                    ModbusMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    ModbusReadConfig config = new ModbusReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                    config.setSlaveId(device.getSlaveId());
                    config.setRegisterRange(o.getRegisterRange());
                    config.setFunctionCode(o.getFunctionCode());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    ModbusMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            });
        });
    }

    /**
     * 初始化s71200定时读取
     */
    @PostConstruct
    public void initS71200TcpRead(){
        threadPoolTaskExecutor.execute(()->{
            List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                    .eq(LabdatahubDevice::getModbusRead,"1"));
            deviceList.forEach(device->{
                // 只拉起 S71200 网络组件下设备的轮询
                if(StringUtils.isEmpty(device.getComponentId())){
                    return;
                }
                LabdatahubComponent component = labdatahubComponentService.getById(device.getComponentId());
                if(component == null || !"S71200_TCP".equals(component.getNetType())){
                    return;
                }
                List<LabdatahubS71200Config> list = labdatahubS71200ConfigService.list(new LambdaQueryWrapper<LabdatahubS71200Config>()
                        .eq(LabdatahubS71200Config::getBelongSn,device.getDeviceSn()));
                list.forEach(o->{
                	S7MessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                	S7ReadConfig config = new S7ReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                    config.setDbNumber(o.getDbNumber());
                    config.setBlockType(o.getBlockType());
                    config.setBitOffset(o.getBitOffset());
                    config.setStartAddress(o.getStartAddress());
                    config.setLength(o.getLength());
                    config.setAreaType(o.getAreaType());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    S7MessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            });
        });
    }

    /**
     * 初始化OmronFins定时读取
     */
    @PostConstruct
    public void initOmronFinsTcpRead(){
        threadPoolTaskExecutor.execute(()->{
            List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                    .eq(LabdatahubDevice::getModbusRead,"1"));
            deviceList.forEach(device->{
                // 只拉起 OMRONFINS 网络组件下设备的轮询
                if(StringUtils.isEmpty(device.getComponentId())){
                    return;
                }
                LabdatahubComponent component = labdatahubComponentService.getById(device.getComponentId());
                if(component == null || !"OMRONFINS_TCP".equals(component.getNetType())){
                    return;
                }
                List<LabdatahubOmronFinsConfig> list = labdatahubOmronFinsConfigService.list(new LambdaQueryWrapper<LabdatahubOmronFinsConfig>()
                        .eq(LabdatahubOmronFinsConfig::getBelongSn,device.getDeviceSn()));
                list.forEach(o->{
                	FinsMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                	FinsReadConfig config = new FinsReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                    config.setAreaCode(o.getAreaCode());
                    config.setStartAddress(o.getStartAddress());
                    config.setLength(o.getLength());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    FinsMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            });
        });
    }

    /**
     * 初始化Brother定时读取（仅拉起 netType=BROTHER_TCP 组件的设备，避免对其它协议组件误调度）
     */
    @PostConstruct
    public void initBrotherTcpRead(){
        threadPoolTaskExecutor.execute(()->{
            List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                    .eq(LabdatahubDevice::getModbusRead,"1"));
            deviceList.forEach(device->{
                // 只拉起 Brother 网络组件下设备的轮询
                if(StringUtils.isEmpty(device.getComponentId())){
                    return;
                }
                LabdatahubComponent component = labdatahubComponentService.getById(device.getComponentId());
                if(component == null || !"BROTHER_TCP".equals(component.getNetType())){
                    return;
                }
                List<LabdatahubBrotherConfig> list = labdatahubBrotherConfigService.list(new LambdaQueryWrapper<LabdatahubBrotherConfig>()
                        .eq(LabdatahubBrotherConfig::getBelongSn,device.getDeviceSn()));
                list.forEach(o->{
                    BrotherTcpMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    BrotherTcpReadConfig config = new BrotherTcpReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime().intValue());
                    config.setDataArea(o.getDataArea());
                    config.setRowNumber(o.getRowNumber());
                    config.setFieldIndex(o.getFieldIndex());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    BrotherTcpMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            });
        });
    }

    /**
     * 初始化FANUC定时读取（仅拉起 netType=FANUC_TCP 组件的设备，避免对其它协议组件误调度）
     */
    @PostConstruct
    public void initFanucTcpRead(){
        threadPoolTaskExecutor.execute(()->{
            List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                    .eq(LabdatahubDevice::getModbusRead,"1"));
            deviceList.forEach(device->{
                // 只拉起 FANUC 网络组件下设备的轮询
                if(StringUtils.isEmpty(device.getComponentId())){
                    return;
                }
                LabdatahubComponent component = labdatahubComponentService.getById(device.getComponentId());
                if(component == null || !"FANUC_TCP".equals(component.getNetType())){
                    return;
                }
                List<LabdatahubFanucConfig> list = labdatahubFanucConfigService.list(new LambdaQueryWrapper<LabdatahubFanucConfig>()
                        .eq(LabdatahubFanucConfig::getBelongSn,device.getDeviceSn()));
                list.forEach(o->{
                    FanucFocasMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
                    FanucFocasReadConfig config = new FanucFocasReadConfig();
                    config.setDeviceSn(o.getBelongSn());
                    config.setCode(o.getCode());
                    config.setDelayTime(o.getDelayTime() == null ? 0 : o.getDelayTime().intValue());
                    config.setIntervalTime(o.getIntervalTime() == null ? 1 : o.getIntervalTime().intValue());
                    config.setReadType(o.getReadType());
                    config.setParam1(o.getParam1());
                    config.setParam2(o.getParam2());
                    ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
                    FanucFocasMessageScheduler.addReadConfig(device.getComponentId(),config);
                });
            });
        });
    }

    /**
     * 初始化Database定时读取
     */
    @PostConstruct
    public void initDatabaseTcpRead(){
        threadPoolTaskExecutor.execute(()->{
            List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                    .eq(LabdatahubDevice::getModbusRead,"1"));
            deviceList.forEach(device->{
                List<LabdatahubDbConfig> databaseConfigList = labdatahubDbConfigService.list(new LambdaQueryWrapper<LabdatahubDbConfig>()
                        .eq(LabdatahubDbConfig::getBelongSn,device.getDeviceSn()));
                if("1".equals(device.getModbusRead())){
                	if(CollectionUtils.isNotEmpty(databaseConfigList)) {
                		DatabaseMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),null);
                        DatabaseReadConfig config = new DatabaseReadConfig();
                        config.setDeviceSn(device.getDeviceSn());
                        config.setDelayTime(databaseConfigList.get(0).getDelayTime().intValue());
                        config.setIntervalTime(databaseConfigList.get(0).getIntervalTime().intValue());;
                        DatabaseMessageScheduler.addReadConfig(device.getComponentId(),config);
                    }
//                    list.forEach(o->{
//                    	FinsMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
//                    	FinsReadConfig config = new FinsReadConfig();
//                        config.setDeviceSn(o.getBelongSn());
//                        config.setCode(o.getCode());
//                        config.setDelayTime(o.getDelayTime().intValue());
//                        config.setIntervalTime(o.getIntervalTime().intValue());
//                        config.setAreaCode(o.getAreaCode());
//                        config.setStartAddress(o.getStartAddress());
//                        config.setLength(o.getLength());
//                        FinsMessageScheduler.addReadConfig(device.getComponentId(),config);
//                    });
                }
            });
        });
    }



    /**
     * 设备自注册开关更新,批量插入开关更新
     */
    @PostConstruct
    @Scheduled(fixedDelay = 10000)
    public void updateRegisterSwitch(){
        String value = redisCache.getCacheObject("sys_config:device.register.switch");
        if("true".equals(value)||"false".equals(value)){
            CacheUtils.DEVICE_REGISTER_SWITCH = value;
        }
        String batchConfig = redisCache.getCacheObject("sys_config:device.log.batch");
        if(!StringUtils.isEmpty(batchConfig)){
            String[] configs = batchConfig.split(",");
            if(configs.length==3){
                try {
                    String isOpen = configs[0];
                    int batchSize = Integer.parseInt(configs[1]);
                    int maxSize = Integer.parseInt(configs[2]);
                    if("true".equals(isOpen) && batchSize>0){
                        if(LogProcessor.getInstance().isRunning() &&
                                (LogProcessor.getInstance().getBatchSize()!=batchSize || LogProcessor.getInstance().getMaxQueueSize()!=maxSize)){
                            LogProcessor.getInstance().setBatchSize(batchSize);
                            LogProcessor.getInstance().resizeQueueAndDiscard(maxSize);
                            log.info("批量插入设备日志开启成功");
                        }
                        if(!LogProcessor.getInstance().isRunning()){
                            LogProcessor processor = LogProcessor.getInstance();
                            LogConsumer consumer = new DatabaseLogConsumer(labdatahubDeviceLogsService);
                            processor.start(batchSize, maxSize, consumer);
                            log.info("批量插入设备日志开启成功");
                        }
                    }else if("false".equals(isOpen)){
                        LogProcessor.getInstance().stop();
                        log.info("批量插入设备日志已关闭");
                    }
                }catch (Exception ignore){}
            }
        }
        String functionConfig = redisCache.getCacheObject("sys_config:device.function.batch");
        if(!StringUtils.isEmpty(functionConfig)){
            String[] configs = functionConfig.split(",");
            if(configs.length==3){
                try {
                    String isOpen = configs[0];
                    int batchSize = Integer.parseInt(configs[1]);
                    int maxSize = Integer.parseInt(configs[2]);
                    if("true".equals(isOpen) && batchSize>0){
                        if(FunctionProcessor.getInstance().isRunning() &&
                                (FunctionProcessor.getInstance().getBatchSize()!=batchSize || FunctionProcessor.getInstance().getMaxQueueSize()!=maxSize)){
                            FunctionProcessor.getInstance().setBatchSize(batchSize);
                            FunctionProcessor.getInstance().resizeQueueAndDiscard(maxSize);
                            log.info("批量插入指令下发日志更新成功");
                        }
                        if(!FunctionProcessor.getInstance().isRunning()){
                            FunctionConsumer consumer = new DatabaseFunctionConsumer(labdatahubFunctionRecordService);
                            FunctionProcessor functionProcessor = FunctionProcessor.getInstance();
                            functionProcessor.start(batchSize, maxSize, consumer);
                            log.info("批量插入指令下发日志开启成功");
                        }
                    }else if("false".equals(isOpen)){
                        LogProcessor.getInstance().stop();
                        log.info("批量插入指令下发日志已关闭");
                    }
                }catch (Exception ignore){}
            }
        }
        String warnRecord = redisCache.getCacheObject("sys_config:device.warn.batch");
        if(!StringUtils.isEmpty(warnRecord)){
            String[] configs = warnRecord.split(",");
            if(configs.length==3){
                try {
                    String isOpen = configs[0];
                    int batchSize = Integer.parseInt(configs[1]);
                    int maxSize = Integer.parseInt(configs[2]);
                    if("true".equals(isOpen) && batchSize>0){
                        if(WarnProcessor.getInstance().isRunning() &&
                                (WarnProcessor.getInstance().getBatchSize()!=batchSize || WarnProcessor.getInstance().getMaxQueueSize()!=maxSize)){
                            WarnProcessor.getInstance().setBatchSize(batchSize);
                            WarnProcessor.getInstance().resizeQueueAndDiscard(maxSize);
                            log.info("批量插入告警日志更新成功");
                        }
                        if(!WarnProcessor.getInstance().isRunning()){
                            WarnConsumer consumer = new DatabaseWarnConsumer(labdatahubWarnRecordService);
                            WarnProcessor warnProcessor = WarnProcessor.getInstance();
                            warnProcessor.start(batchSize, maxSize, consumer);
                            log.info("批量插入告警日志开启成功");
                        }
                    }else if("false".equals(isOpen)){
                        LogProcessor.getInstance().stop();
                        log.info("批量插入告警日志已关闭");
                    }
                }catch (Exception ignore){}
            }
        }
        String linkageRecord = redisCache.getCacheObject("sys_config:device.linkage.batch");
        if(!StringUtils.isEmpty(linkageRecord)){
            String[] configs = linkageRecord.split(",");
            if(configs.length==3){
                try {
                    String isOpen = configs[0];
                    int batchSize = Integer.parseInt(configs[1]);
                    int maxSize = Integer.parseInt(configs[2]);
                    if("true".equals(isOpen) && batchSize>0){
                        if(LinkageProcessor.getInstance().isRunning() &&
                                (LinkageProcessor.getInstance().getBatchSize()!=batchSize || LinkageProcessor.getInstance().getMaxQueueSize()!=maxSize)){
                            LinkageProcessor.getInstance().setBatchSize(batchSize);
                            LinkageProcessor.getInstance().resizeQueueAndDiscard(maxSize);
                            log.info("批量插入联动告警日志更新成功");
                        }
                        if(!LinkageProcessor.getInstance().isRunning()){
                            LinkageConsumer consumer = new DatabaseLinkageConsumer(labdatahubLinkageWarnRecordService);
                            LinkageProcessor linkageProcessor = LinkageProcessor.getInstance();
                            linkageProcessor.start(batchSize, maxSize, consumer);
                            log.info("批量插入联动告警日志开启成功");
                        }
                    }else if("false".equals(isOpen)){
                        LogProcessor.getInstance().stop();
                        log.info("批量插入联动告警日志已关闭");
                    }
                }catch (Exception ignore){}
            }
        }
    }

}
