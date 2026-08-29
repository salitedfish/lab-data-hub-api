package com.labdatahub.business.service.impl;

import static com.labdatahub.business.service.impl.LabdatahubProtocolServiceImpl.PROTOCOL_PATH;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.labdatahub.business.domain.LabdatahubComponent;
import com.labdatahub.business.domain.LabdatahubProtocol;
import com.labdatahub.business.mapper.LabdatahubComponentMapper;
import com.labdatahub.business.mapper.LabdatahubProtocolMapper;
import com.labdatahub.business.scheduled.TimerTask;
import com.labdatahub.business.service.ILabdatahubComponentService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.common.exception.CommonWarnException;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.coap.CoapServerConfig;
import com.labdatahub.component.coap.CoapServerManager;
import com.labdatahub.component.brother_tcp.BrotherTcpConfig;
import com.labdatahub.component.brother_tcp.BrotherTcpConnectionManager;
import com.labdatahub.component.fanuc_focas.FanucFocasConfig;
import com.labdatahub.component.fanuc_focas.FanucFocasConnectionManager;
import com.labdatahub.component.db.DatabaseConfig;
import com.labdatahub.component.db.DatabaseConnectionManager;
import com.labdatahub.component.event.ComponentOnlineNotifier;
import com.labdatahub.component.fins_tcp.FinsConnectionManager;
import com.labdatahub.component.fins_tcp.FinsTcpConfig;
import com.labdatahub.component.mitsubishi_tcp.MitsubishiConnectionManager;
import com.labdatahub.component.mitsubishi_tcp.MitsubishiTcpConfig;
import com.labdatahub.component.mitsubishi_cnc_tcp.MitsubishiCncConnectionManager;
import com.labdatahub.component.mitsubishi_cnc_tcp.MitsubishiCncTcpConfig;
import com.labdatahub.component.http.HttpServerConfig;
import com.labdatahub.component.http.HttpServerManager;
import com.labdatahub.component.modbus_tcp.ModbusConnectionManager;
import com.labdatahub.component.modbus_tcp.ModbusTcpConfig;
import com.labdatahub.component.mqtt.client.MqttClientConfig;
import com.labdatahub.component.mqtt.client.MqttClientManager;
import com.labdatahub.component.mqtt.server.MqttBrokerConfig;
import com.labdatahub.component.mqtt.server.MqttBrokerManager;
import com.labdatahub.component.protocol.ProtocolManager;
import com.labdatahub.component.s7_tcp.S7ConnectionManager;
import com.labdatahub.component.s7_tcp.S7TcpConfig;
import com.labdatahub.component.tcp.TCPServerConfig;
import com.labdatahub.component.tcp.TCPServerHandlerInstance;
import com.labdatahub.component.tcp.TCPServerManager;
import com.labdatahub.component.udp.UDPServerConfig;
import com.labdatahub.component.udp.UDPServerHandlerInstance;
import com.labdatahub.component.udp.UDPServerManager;
import com.labdatahub.component.utils.PortChecker;
import com.labdatahub.component.websocket.NettyWebSocketServer;
import com.labdatahub.component.websocket.WebsocketServerConfig;

/**
 * 网络组件Service业务层处理
 *
 * @author labdatahub
 * @date 2025-09-18
 */
@Service
public class LabdatahubComponentServiceImpl extends ServiceImpl<LabdatahubComponentMapper, LabdatahubComponent> implements ILabdatahubComponentService {
    @Autowired
    private LabdatahubComponentMapper labdatahubComponentMapper;
    @Autowired
    private LabdatahubProtocolMapper labdatahubProtocolMapper;
    /**
     * 定时任务调度（组件开启后重建该协议下已开读开关设备的读取调度，
     * 修复后配点位/后绑组件时调度缺失、设备不读取的问题）
     */
    @Autowired
    private TimerTask timerTask;
    /**
     * 查询网络组件
     *
     * @param id 网络组件主键
     * @return 网络组件
     */
    @Override
    public LabdatahubComponent selectLabdatahubComponentById(String id) {
        return labdatahubComponentMapper.selectLabdatahubComponentById(id);
    }

    /**
     * 查询网络组件列表
     *
     * @param labdatahubComponent 网络组件
     * @return 网络组件
     */
    @Override
    public List<LabdatahubComponent> selectLabdatahubComponentList(LabdatahubComponent labdatahubComponent) {
        return labdatahubComponentMapper.selectLabdatahubComponentList(labdatahubComponent);
    }

    /**
     * 新增网络组件
     *
     * @param labdatahubComponent 网络组件
     * @return 结果
     */
    @Override
    public int insertLabdatahubComponent(LabdatahubComponent labdatahubComponent) {
        return labdatahubComponentMapper.insertLabdatahubComponent(labdatahubComponent);
    }

    /**
     * 修改网络组件
     *
     * @param labdatahubComponent 网络组件
     * @return 结果
     */
    @Override
    public int updateLabdatahubComponent(LabdatahubComponent labdatahubComponent) {
        return labdatahubComponentMapper.updateLabdatahubComponent(labdatahubComponent);
    }

    /**
     * 批量删除网络组件
     *
     * @param ids 需要删除的网络组件主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubComponentByIds(String[] ids) {
        return labdatahubComponentMapper.deleteLabdatahubComponentByIds(ids);
    }

    /**
     * 删除网络组件信息
     *
     * @param id 网络组件主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubComponentById(String id) {
        return labdatahubComponentMapper.deleteLabdatahubComponentById(id);
    }

    @Override
    public boolean openComponent(String id) throws Exception {
        LabdatahubComponent component = labdatahubComponentMapper.selectById(id);
        if(component==null) {
            return false;
        }
        switch (component.getNetType()){
            //MQTT服务端
            case "MQTT_BROKER" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                    MqttBrokerConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(MqttBrokerConfig.class);
                    if(StringUtils.isEmpty(config.getTcpPort())||!PortChecker.isLocalPortAvailable(Integer.parseInt(config.getTcpPort()))){
                        throw new CommonWarnException("TCP端口被占用或未输入");
                    }
                    if(StringUtils.isNotEmpty(config.getWsPort())&&!PortChecker.isLocalPortAvailable(Integer.parseInt(config.getWsPort()))){
                        throw new CommonWarnException("WS端口被占用");
                    }
                    MqttBrokerManager.addBroker(component.getId(),config.getTcpPort(),config.getWsPort(),config.getAllowAnonymous(),config.getUsername(),config.getPassword());
                    component.setStatus("1");
                    List<String> portList = new ArrayList<>();
                    if(StringUtils.isNotEmpty(config.getTcpPort())){
                        portList.add(config.getTcpPort());
                    }
                    if(StringUtils.isNotEmpty(config.getWsPort())){
                        portList.add(config.getWsPort());
                    }
                    if(StringUtils.isNotEmpty(component.getProtocolId())){
                        initProtocol(component.getProtocolId());
                        ProtocolManager.PROTOCOL_MAP.put(component.getId(),component.getProtocolId());
                    }
                    component.setPort(String.join(",",portList));
                    component.setIpAddr("0.0.0.0");
                    labdatahubComponentMapper.updateById(component);
                    CacheUtils.setComponentCache(component.getId(),component);
                    return true;
                }else {
                    return false;
                }
            }
            //MQTT客户端
            case "MQTT_CLIENT" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                    MqttClientConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(MqttClientConfig.class);
                    config.setClientId(id);
                    boolean isOk = MqttClientManager.addConnection(config);
                    if(!isOk){
                        throw new CommonWarnException("连接失败，请检查配置信息是否正确");
                    }
                    if(StringUtils.isNotEmpty(component.getProtocolId())){
                        initProtocol(component.getProtocolId());
                        ProtocolManager.PROTOCOL_MAP.put(component.getId(),component.getProtocolId());
                    }
                    component.setStatus("1");
                    component.setIpAddr(config.getBrokerUrl());
                    labdatahubComponentMapper.updateById(component);
                    CacheUtils.setComponentCache(component.getId(),component);
                    return true;
                }else {
                    return false;
                }
            }
            //Websocket服务端
            case "WEBSOCKET_SERVER" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                    WebsocketServerConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(WebsocketServerConfig.class);
                    boolean isOk = SpringUtils.getBean(NettyWebSocketServer.class).startWebSocketServer(component.getId(),config.getPort(),config.getPath());
                    if(!isOk){
                        throw new CommonWarnException("开启失败，请检查配置信息是否正确");
                    }
                    component.setStatus("1");
                    if(StringUtils.isNotEmpty(component.getProtocolId())){
                        initProtocol(component.getProtocolId());
                        ProtocolManager.PROTOCOL_MAP.put(component.getId(),component.getProtocolId());
                    }
                    component.setIpAddr("0.0.0.0");
                    component.setPort(String.valueOf(config.getPort()));
                    labdatahubComponentMapper.updateById(component);
                    CacheUtils.setComponentCache(component.getId(),component);
                    return true;
                }else {
                    return false;
                }
            }
            //TCP服务端
            case "TCP_SERVER" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                    TCPServerConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(TCPServerConfig.class);
                    if(!PortChecker.isLocalPortAvailable(config.getServerPort())){
                        throw new CommonWarnException("端口被占用");
                    }
                    boolean isOk = TCPServerManager.addServer(component.getId(), config.getServerPort(),config.getDelimiter(),config.getCacheSize(), new TCPServerHandlerInstance());
                    if(!isOk){
                        throw new CommonWarnException("开启失败，请检查配置信息是否正确");
                    }
                    component.setStatus("1");
                    if(StringUtils.isNotEmpty(component.getProtocolId())){
                        initProtocol(component.getProtocolId());
                        ProtocolManager.PROTOCOL_MAP.put(component.getId(),component.getProtocolId());
                    }
                    component.setIpAddr("0.0.0.0");
                    component.setPort(String.valueOf(config.getServerPort()));
                    labdatahubComponentMapper.updateById(component);
                    CacheUtils.setComponentCache(component.getId(),component);
                    return true;
                }else {
                    return false;
                }
            }
            //UDP服务端
            case "UDP_SERVER" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                    UDPServerConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(UDPServerConfig.class);
                    if(!PortChecker.isLocalPortAvailable(config.getServerPort())){
                        throw new CommonWarnException("端口被占用");
                    }
                    boolean isOk = UDPServerManager.addServer(component.getId(), config.getServerPort(), new UDPServerHandlerInstance());
                    if(!isOk){
                        throw new CommonWarnException("开启失败，请检查配置信息是否正确");
                    }
                    component.setStatus("1");
                    if(StringUtils.isNotEmpty(component.getProtocolId())){
                        initProtocol(component.getProtocolId());
                        ProtocolManager.PROTOCOL_MAP.put(component.getId(),component.getProtocolId());
                    }
                    component.setIpAddr("0.0.0.0");
                    component.setPort(String.valueOf(config.getServerPort()));
                    labdatahubComponentMapper.updateById(component);
                    CacheUtils.setComponentCache(component.getId(),component);
                    return true;
                }else {
                    return false;
                }
            }
            //COAP服务端
            case "COAP_SERVER" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                    CoapServerConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(CoapServerConfig.class);
                    if(!PortChecker.isLocalPortAvailable(config.getPort())){
                        throw new CommonWarnException("端口被占用");
                    }
                    boolean isOk = CoapServerManager.addServer(component.getId(), config.getPort(),config.getIsAuth(),config.getTokenConfig());
                    if(!isOk){
                        throw new CommonWarnException("开启失败，请检查配置信息是否正确");
                    }
                    component.setStatus("1");
                    if(StringUtils.isNotEmpty(component.getProtocolId())){
                        initProtocol(component.getProtocolId());
                        ProtocolManager.PROTOCOL_MAP.put(component.getId(),component.getProtocolId());
                    }
                    component.setIpAddr("0.0.0.0");
                    component.setPort(String.valueOf(config.getPort()));
                    labdatahubComponentMapper.updateById(component);
                    CacheUtils.setComponentCache(component.getId(),component);
                    return true;
                }else {
                    return false;
                }
            }
            //HTTP服务端
            case "HTTP_SERVER" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                    HttpServerConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(HttpServerConfig.class);
                    if(!PortChecker.isLocalPortAvailable(config.getPort())){
                        throw new CommonWarnException("端口被占用");
                    }
                    boolean isOk = HttpServerManager.startServer(component.getId(), config.getPort(),config.getNeedReply());
                    if(!isOk){
                        throw new CommonWarnException("开启失败，请检查配置信息是否正确");
                    }
                    component.setStatus("1");
                    if(StringUtils.isNotEmpty(component.getProtocolId())){
                        initProtocol(component.getProtocolId());
                        ProtocolManager.PROTOCOL_MAP.put(component.getId(),component.getProtocolId());
                    }
                    component.setIpAddr("0.0.0.0");
                    component.setPort(String.valueOf(config.getPort()));
                    labdatahubComponentMapper.updateById(component);
                    CacheUtils.setComponentCache(component.getId(),component);
                    return true;
                }else {
                    return false;
                }
            }
            //modbus_tcp
            case "MODBUS_TCP" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                    ModbusTcpConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(ModbusTcpConfig.class);
                    boolean isOk = ModbusConnectionManager.addConnection(component.getId(), config);
                    // 组件开启后重建该协议下已开读开关设备的定时调度（修复后配点位/后绑组件时调度缺失）
                    timerTask.initModbusTcpRead();
                    if(!isOk){
                        throw new CommonWarnException("开启失败，请检查配置信息是否正确");
                    }
                    component.setStatus("1");
                    if(StringUtils.isNotEmpty(component.getProtocolId())){
                        initProtocol(component.getProtocolId());
                        ProtocolManager.PROTOCOL_MAP.put(component.getId(),component.getProtocolId());
                    }
                    component.setIpAddr(config.getIpAddr());
                    component.setPort(String.valueOf(config.getPort()));
                    labdatahubComponentMapper.updateById(component);
                    CacheUtils.setComponentCache(component.getId(),component);
                    return true;
                }else {
                    return false;
                }
            }
            case "S71200_TCP" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                	S7TcpConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(S7TcpConfig.class);
                    boolean isOk = S7ConnectionManager.addConnection(component.getId(), config);
                    // 组件开启后重建该协议下已开读开关设备的定时调度（修复后配点位/后绑组件时调度缺失）
                    timerTask.initS71200TcpRead();
                    if(!isOk){
                        throw new CommonWarnException("开启失败，请检查配置信息是否正确");
                    }
                    component.setStatus("1");
                    if(StringUtils.isNotEmpty(component.getProtocolId())){
                        initProtocol(component.getProtocolId());
                        ProtocolManager.PROTOCOL_MAP.put(component.getId(),component.getProtocolId());
                    }
                    component.setIpAddr(config.getIpAddr());
                    component.setPort(String.valueOf(config.getPort()));
                    labdatahubComponentMapper.updateById(component);
                    CacheUtils.setComponentCache(component.getId(),component);
                    return true;
                }else {
                    return false;
                }
            }
            case "OMRONFINS_TCP" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                	FinsTcpConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(FinsTcpConfig.class);
                    boolean isOk = FinsConnectionManager.addConnection(component.getId(), config);
                    // 组件开启后重建该协议下已开读开关设备的定时调度（修复后配点位/后绑组件时调度缺失）
                    timerTask.initOmronFinsTcpRead();
                    if(!isOk){
                        throw new CommonWarnException("开启失败，请检查配置信息是否正确");
                    }
                    component.setStatus("1");
                    if(StringUtils.isNotEmpty(component.getProtocolId())){
                        initProtocol(component.getProtocolId());
                        ProtocolManager.PROTOCOL_MAP.put(component.getId(),component.getProtocolId());
                    }
                    component.setIpAddr(config.getIpAddr());
                    component.setPort(String.valueOf(config.getPort()));
                    labdatahubComponentMapper.updateById(component);
                    CacheUtils.setComponentCache(component.getId(),component);
                    return true;
                }else {
                    return false;
                }
            }
            case "BROTHER_TCP" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                	BrotherTcpConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(BrotherTcpConfig.class);
                    boolean isOk = BrotherTcpConnectionManager.addConnection(component.getId(), config);
                    // 组件开启后重建该协议下已开读开关设备的定时调度（修复后配点位/后绑组件时调度缺失）
                    timerTask.initBrotherTcpRead();
                    if(!isOk){
                        throw new CommonWarnException("开启失败，请检查配置信息是否正确");
                    }
                    component.setStatus("1");
                    if(StringUtils.isNotEmpty(component.getProtocolId())){
                        initProtocol(component.getProtocolId());
                        ProtocolManager.PROTOCOL_MAP.put(component.getId(),component.getProtocolId());
                    }
                    component.setIpAddr(config.getIpAddr());
                    component.setPort(String.valueOf(config.getPort()));
                    labdatahubComponentMapper.updateById(component);
                    CacheUtils.setComponentCache(component.getId(),component);
                    return true;
                }else {
                    return false;
                }
            }
            case "FANUC_TCP" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                	FanucFocasConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(FanucFocasConfig.class);
                    boolean isOk = FanucFocasConnectionManager.addConnection(component.getId(), config);
                    // 组件开启后重建该协议下已开读开关设备的定时调度（修复后配点位/后绑组件时调度缺失）
                    timerTask.initFanucTcpRead();
                    if(!isOk){
                        throw new CommonWarnException("开启失败，请检查配置信息是否正确");
                    }
                    component.setStatus("1");
                    if(StringUtils.isNotEmpty(component.getProtocolId())){
                        initProtocol(component.getProtocolId());
                        ProtocolManager.PROTOCOL_MAP.put(component.getId(),component.getProtocolId());
                    }
                    component.setIpAddr(config.getIpAddr());
                    component.setPort(String.valueOf(config.getPort()));
                    labdatahubComponentMapper.updateById(component);
                    CacheUtils.setComponentCache(component.getId(),component);
                    return true;
                }else {
                    return false;
                }
            }
            case "MITSUBISHI_TCP" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                	MitsubishiTcpConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(MitsubishiTcpConfig.class);
                    boolean isOk = MitsubishiConnectionManager.addConnection(component.getId(), config);
                    // 组件开启后重建该协议下已开读开关设备的定时调度（修复后配点位/后绑组件时调度缺失）
                    timerTask.initMitsubishiTcpRead();
                    if(!isOk){
                        throw new CommonWarnException("开启失败，请检查配置信息是否正确");
                    }
                    component.setStatus("1");
                    if(StringUtils.isNotEmpty(component.getProtocolId())){
                        initProtocol(component.getProtocolId());
                        ProtocolManager.PROTOCOL_MAP.put(component.getId(),component.getProtocolId());
                    }
                    component.setIpAddr(config.getIpAddr());
                    component.setPort(String.valueOf(config.getPort()));
                    labdatahubComponentMapper.updateById(component);
                    CacheUtils.setComponentCache(component.getId(),component);
                    return true;
                }else {
                    return false;
                }
            }
            case "MITSUBISHI_CNC_TCP" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                	MitsubishiCncTcpConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(MitsubishiCncTcpConfig.class);
                    boolean isOk = MitsubishiCncConnectionManager.addConnection(component.getId(), config);
                    // 组件开启后重建该协议下已开读开关设备的定时调度（修复后配点位/后绑组件时调度缺失）
                    timerTask.initMitsubishiCncTcpRead();
                    if(!isOk){
                        throw new CommonWarnException("开启失败，请检查配置信息是否正确");
                    }
                    component.setStatus("1");
                    if(StringUtils.isNotEmpty(component.getProtocolId())){
                        initProtocol(component.getProtocolId());
                        ProtocolManager.PROTOCOL_MAP.put(component.getId(),component.getProtocolId());
                    }
                    component.setIpAddr(config.getIpAddr());
                    component.setPort(String.valueOf(config.getPort()));
                    labdatahubComponentMapper.updateById(component);
                    CacheUtils.setComponentCache(component.getId(),component);
                    return true;
                }else {
                    return false;
                }
            }
            case "DATABASE_TCP" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                	DatabaseConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(DatabaseConfig.class);
                    boolean isOk = DatabaseConnectionManager.addConnection(component.getId(), config);
                    // 组件开启后重建该协议下已开读开关设备的定时调度（修复后配点位/后绑组件时调度缺失）
                    timerTask.initDatabaseTcpRead();
                    if(!isOk){
                        throw new CommonWarnException("开启失败，请检查配置信息是否正确");
                    }
                    component.setStatus("1");
                    if(StringUtils.isNotEmpty(component.getProtocolId())){
                        initProtocol(component.getProtocolId());
                        ProtocolManager.PROTOCOL_MAP.put(component.getId(),component.getProtocolId());
                    }
                    component.setIpAddr(config.getIpAddr());
                    component.setPort(String.valueOf(config.getPort()));
                    labdatahubComponentMapper.updateById(component);
                    CacheUtils.setComponentCache(component.getId(),component);
                    return true;
                }else {
                    return false;
                }
            }
            default: return false;
        }
    }

    @Override
    public boolean closeComponent(String id) throws IOException, MqttException {
        LabdatahubComponent component = labdatahubComponentMapper.selectById(id);
        if(component==null) {
            return false;
        }
        switch (component.getNetType()){
            //MQTT服务端
            case "MQTT_BROKER" : {
                MqttBrokerManager.stopBroker(component.getId());
                MqttBrokerManager.serverMap.remove(component.getId());
                component.setStatus("0");
                labdatahubComponentMapper.updateById(component);
                CacheUtils.setComponentCache(component.getId(),component);
                return true;
            }
            //MQTT客户端
            case "MQTT_CLIENT" : {
                MqttClientManager.deleteConnection(component.getId());
                MqttClientManager.clientMap.remove(component.getId());
                component.setStatus("0");
                labdatahubComponentMapper.updateById(component);
                CacheUtils.setComponentCache(component.getId(),component);
                return true;
            }
            //TCP服务端
            case "TCP_SERVER" : {
                TCPServerManager.removeServer(component.getId());
                component.setStatus("0");
                labdatahubComponentMapper.updateById(component);
                CacheUtils.setComponentCache(component.getId(),component);
                return true;
            }
            //UDP服务端
            case "UDP_SERVER" : {
                UDPServerManager.removeServer(component.getId());
                component.setStatus("0");
                labdatahubComponentMapper.updateById(component);
                CacheUtils.setComponentCache(component.getId(),component);
                return true;
            }
            //COAP服务端
            case "COAP_SERVER" : {
                CoapServerManager.stopServer(component.getId());
                component.setStatus("0");
                labdatahubComponentMapper.updateById(component);
                CacheUtils.setComponentCache(component.getId(),component);
                return true;
            }
            //HTTP服务端
            case "HTTP_SERVER" : {
                HttpServerManager.stopServer(component.getId());
                component.setStatus("0");
                labdatahubComponentMapper.updateById(component);
                CacheUtils.setComponentCache(component.getId(),component);
                return true;
            }
            //WEBSOCKET服务端
            case "WEBSOCKET_SERVER" : {
                SpringUtils.getBean(NettyWebSocketServer.class).stopWebSocketServer(component.getId());
                component.setStatus("0");
                labdatahubComponentMapper.updateById(component);
                CacheUtils.setComponentCache(component.getId(),component);
                return true;
            }
            //MODBUS连接
            case "MODBUS_TCP" : {
                ModbusConnectionManager.closeConnection(component.getId());
                // 组件关闭视为离线，通知该组件下设备下线（节流，仅在线→离线转变时发一次）
                ComponentOnlineNotifier.markOfflineAndNotify(component.getId());
                component.setStatus("0");
                labdatahubComponentMapper.updateById(component);
                CacheUtils.setComponentCache(component.getId(),component);
                return true;
            }
            case "S71200_TCP" : {
            	S7ConnectionManager.closeConnection(component.getId());
                // 组件关闭视为离线，通知该组件下设备下线（节流，仅在线→离线转变时发一次）
                ComponentOnlineNotifier.markOfflineAndNotify(component.getId());
                component.setStatus("0");
                labdatahubComponentMapper.updateById(component);
                CacheUtils.setComponentCache(component.getId(),component);
                return true;
            }
            case "OMRONFINS_TCP" : {
            	FinsConnectionManager.closeConnection(component.getId());
                // 组件关闭视为离线，通知该组件下设备下线（节流，仅在线→离线转变时发一次）
                ComponentOnlineNotifier.markOfflineAndNotify(component.getId());
                component.setStatus("0");
                labdatahubComponentMapper.updateById(component);
                CacheUtils.setComponentCache(component.getId(),component);
                return true;
            }
            case "BROTHER_TCP" : {
            	BrotherTcpConnectionManager.closeConnection(component.getId());
                // 组件关闭视为离线，通知该组件下设备下线（节流，仅在线→离线转变时发一次）
                ComponentOnlineNotifier.markOfflineAndNotify(component.getId());
                component.setStatus("0");
                labdatahubComponentMapper.updateById(component);
                CacheUtils.setComponentCache(component.getId(),component);
                return true;
            }
            case "FANUC_TCP" : {
            	FanucFocasConnectionManager.closeConnection(component.getId());
                // 组件关闭视为离线，通知该组件下设备下线（节流，仅在线→离线转变时发一次）
                ComponentOnlineNotifier.markOfflineAndNotify(component.getId());
                component.setStatus("0");
                labdatahubComponentMapper.updateById(component);
                CacheUtils.setComponentCache(component.getId(),component);
                return true;
            }
            case "MITSUBISHI_TCP" : {
            	MitsubishiConnectionManager.closeConnection(component.getId());
                // 组件关闭视为离线，通知该组件下设备下线（节流，仅在线→离线转变时发一次）
                ComponentOnlineNotifier.markOfflineAndNotify(component.getId());
                component.setStatus("0");
                labdatahubComponentMapper.updateById(component);
                CacheUtils.setComponentCache(component.getId(),component);
                return true;
            }
            case "MITSUBISHI_CNC_TCP" : {
            	MitsubishiCncConnectionManager.closeConnection(component.getId());
                // 组件关闭视为离线，通知该组件下设备下线（节流，仅在线→离线转变时发一次）
                ComponentOnlineNotifier.markOfflineAndNotify(component.getId());
                component.setStatus("0");
                labdatahubComponentMapper.updateById(component);
                CacheUtils.setComponentCache(component.getId(),component);
                return true;
            }
            case "DATABASE_TCP" : {
            	DatabaseConnectionManager.closeConnection(component.getId());
                // 组件关闭视为离线，通知该组件下设备下线（节流，仅在线→离线转变时发一次）
                ComponentOnlineNotifier.markOfflineAndNotify(component.getId());
                component.setStatus("0");
                labdatahubComponentMapper.updateById(component);
                CacheUtils.setComponentCache(component.getId(),component);
                return true;
            }
            default: return false;
        }
    }

    public void initProtocol(String protocolId){
        //先加载相关协议
        if (StringUtils.isNotEmpty(protocolId)) {
            LabdatahubProtocol protocol = labdatahubProtocolMapper.selectById(protocolId);
            try {
                ProtocolManager.addProtocol(protocol.getId(),protocol.getProtocolType(), PROTOCOL_PATH + File.separator + protocol.getNewName(), protocol.getMainClassPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
