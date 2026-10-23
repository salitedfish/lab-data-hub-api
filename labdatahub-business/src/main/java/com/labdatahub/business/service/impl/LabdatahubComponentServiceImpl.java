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
import com.labdatahub.business.scheduled.ComponentStartupRetryer;
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
import com.labdatahub.component.mitsubishi_mc3e_tcp.MitsubishiMc3eConnectionManager;
import com.labdatahub.component.mitsubishi_mc3e_tcp.MitsubishiMc3eTcpConfig;
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
     * 协议加载互斥锁（由AI修改）：见 {@link #initProtocol(String)}。
     * 锁对象独立于本服务实例，不与别处对本 bean 的同步相互牵连；静态是因为协议表
     * （{@link ProtocolManager} 的几个容器）本身就是静态的，锁要跟着数据走。
     */
    private static final Object PROTOCOL_LOAD_LOCK = new Object();
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

    /**
     * 该网络类型是否必须绑定协议包才能开启。
     *
     * <p>判定依据是消费端是否依赖 {@code ProtocolManager.DECODE_METHOD} 解码并发布 {@code device.up}：
     * 以下 8 个采集类协议全部走「调度 → 消费 → 反射 decode → device.up」链路，缺协议包时
     * 设备永远不会被置在线；MQTT / TCP / UDP / HTTP / Websocket / CoAP 服务端与数据库采集
     * 不依赖该链路，protocolId 为空是正常配置。
     *
     * @param netType 网络组件类型
     * @return true=必须绑定协议包
     */
    private static boolean requiresProtocol(String netType) {
        if (StringUtils.isEmpty(netType)) {
            return false;
        }
        switch (netType) {
            case "MODBUS_TCP":
            case "S71200_TCP":
            case "OMRONFINS_TCP":
            case "BROTHER_TCP":
            case "FANUC_TCP":
            case "MITSUBISHI_MC3E_TCP":
            case "MITSUBISHI_MC3E_UDP":
            case "MITSUBISHI_CNC_TCP":
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean openComponent(String id) throws Exception {
        LabdatahubComponent component = labdatahubComponentMapper.selectById(id);
        if(component==null) {
            return false;
        }
        // 协议包前置校验：必须放在建连接、起消费线程、注册调度之前。
        // 各 netType 分支里原本是「先 addConnection → 再 initProtocol」，协议加载不了也照样把连接和
        // 消费线程拉起来，只有解码那步永远失败 —— 组件停在「页面显示已开启、设备永不上线」的半成品状态，
        // 下次点开启还得先清掉这堆残留。放到最前面，协议缺了就直接失败、什么都不起。
        // 关闭（closeComponent）不加这道校验：协议缺包时组件更要能关得掉。
        // 上面这条「缺协议就不起」原写法有个漏洞：protocolId 为空时 initProtocol 直接 return，
        // 连接、调度、消费线程照样全起来，只有消费端的 decode → MessageCache → device.up 整段被
        // protocolId 闸门跳过 —— 表现是「组件开启成功、设备永远离线、库里一条数据都没有」。
        // 2026-10-22 用户新建的 MC UDP 组件正是这个状态（组件 14:52 先建、协议包 16:08 才上传，
        // 创建时「协议绑定」下拉里没有可选项，保存下来 protocol_id 为空）。采集类组件必须显式绑定，
        // 在开启入口拒绝，让问题在页面上直接暴露。
        if (StringUtils.isEmpty(component.getProtocolId()) && requiresProtocol(component.getNetType())) {
            throw new CommonWarnException("开启失败：该网络组件还没有绑定协议包（网络类型 " + component.getNetType() + "）\n"
                    + "可能原因：组件创建时「协议管理」里还没有上传该网络类型的协议包，保存时协议绑定为空\n"
                    + "处理办法：先到「协议管理」上传对应网络类型的协议包，再回到本组件点「修改」，"
                    + "在「协议绑定」里选中该协议并保存");
        }
        initProtocol(component.getProtocolId());
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
            case "MITSUBISHI_MC3E_TCP" :
            case "MITSUBISHI_MC3E_UDP" : {
                if(StringUtils.isNotEmpty(component.getOtherConfig())){
                	MitsubishiMc3eTcpConfig config = JSONObject.parseObject(component.getOtherConfig()).toJavaObject(MitsubishiMc3eTcpConfig.class);
                	// 传输方式由组件 netType 决定，不靠 other_config 里的 transport 字段（用户无需、也不该手填）
                	config.setTransport("MITSUBISHI_MC3E_UDP".equals(component.getNetType()) ? "udp" : "tcp");
                    boolean isOk = MitsubishiMc3eConnectionManager.addConnection(component.getId(), config);
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
        // 手动关闭即取消启动自愈对它的重试（静态调用，避免自愈器与本服务互相注入）。
        // 不在启动快照里、或已开启不在重试集合里的组件，调用无副作用。
        ComponentStartupRetryer.onManualClose(id);
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
            case "MITSUBISHI_MC3E_TCP" :
            case "MITSUBISHI_MC3E_UDP" : {
            	MitsubishiMc3eConnectionManager.closeConnection(component.getId());
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

    /**
     * 加载组件绑定的协议包；<b>失败即抛出带排查指引的告警，绝不静默</b>。
     *
     * <p>原实现把异常 {@code e.printStackTrace()} 吞掉（协议行不存在时还会在 {@code protocol.getId()}
     * 直接 NPE），于是「协议包缺失」——换机部署时漏拷 {@code protocol/} 下的 jar 最常见——会演变成
     * 最难查的一种故障：组件照样置 {@code status=1}、连接照样建、消费线程照样起、调度照样注册，
     * 只有解码那一步永远取不到 {@code DECODE_METHOD}（消费服务反射调用直接 NPE，再被分钟节流压成
     * 一分钟一条）—— 页面显示「组件已开启」、设备却永远不上线，日志里只剩一行堆栈。
     * 协议包是采集链路的第一环，缺了它整个组件都不该算开启成功。
     *
     * <p>「可能原因 / 处理办法」直接写进提示里：这类故障的关键现场（哪个文件、哪个目录）只有后端知道，
     * 让人拿着页面上的提示就能自查，比翻服务器日志快得多。
     *
     * @param protocolId 组件绑定的协议ID；为空表示该组件本就不需要协议包（如数据库采集），直接跳过
     * @throws CommonWarnException 协议记录不存在 / 协议包文件缺失 / 协议包解析失败时抛出（含原因与处理办法）
     */
    public void initProtocol(String protocolId) throws Exception {
        if (StringUtils.isEmpty(protocolId)) {
            return;
        }
        // 已加载过就不再重复加载：启动时 initComponentAndProtocol 已为 status=1 的组件注册过协议，
        // 这里随组件开启再走一遍会重复建 URLClassLoader + 重复实例化（旧实例无人回收）
        if (ProtocolManager.CLASS_INSTANCE.containsKey(protocolId)) {
            return;
        }
        // 由AI修改：加锁 + 锁内二次检查，把上面这个「检查-再执行」变成原子操作。
        // 原写法在并发启动组件时会重复加载同一个协议包：两个线程都通过了上面的 containsKey，
        // 就都会走完 addProtocol —— 各建一个 URLClassLoader、各实例化一个协议对象，后一次 put
        // 覆盖前一次，前一个实例连同它的 ClassLoader 再无人引用（类加载器泄漏；且同时存在两个
        // 活的协议实例，各持一份解码状态）。触发条件很现实：同一个协议被多个组件绑定，用户在
        // 页面上连着点几个组件的「启动」；或 TimerTask 启动时批量注册 status=1 的组件撞上手动开启。
        // 锁内只放「确实需要加载」的那一次，上面的快路径不抢锁 —— 绝大多数调用（协议已加载）
        // 直接返回。锁内的操作是冷路径上的毫秒级动作（一次主键查询 + 文件存在性判断 + 反射装载），
        // 相对启动组件本身（建连探针最长 3 秒）可以忽略。
        // ⚠️ 本锁只管 initProtocol 这一条入口：TimerTask 启动时的注册、
        //    LabdatahubProtocolServiceImpl 上传/删除协议包都是直调 ProtocolManager，
        //    不经过这里 —— 那几处与这里的并发属于另一回事，本次未处理。
        synchronized (PROTOCOL_LOAD_LOCK) {
            // 二次检查：等锁期间可能已被别的线程加载完，此时直接返回，不做重复装载
            if (ProtocolManager.CLASS_INSTANCE.containsKey(protocolId)) {
                return;
            }
            loadProtocol(protocolId);
        }
    }

    /**
     * 真正执行协议包加载（由AI修改：从 {@link #initProtocol(String)} 里拆出来的纯执行部分）。
     * 拆分的唯一目的是让互斥逻辑集中在 initProtocol 一处：本方法必须在持锁状态下调用，
     * 不要绕过 initProtocol 直接调它。
     *
     * @param protocolId 组件绑定的协议ID，调用方已保证非空
     * @throws CommonWarnException 协议记录不存在 / 协议包文件缺失 / 协议包解析失败时抛出
     */
    private void loadProtocol(String protocolId) throws Exception {
        LabdatahubProtocol protocol = labdatahubProtocolMapper.selectById(protocolId);
        if (protocol == null) {
            throw new CommonWarnException("协议加载失败：组件绑定的协议记录不存在（protocolId=" + protocolId + "）\n"
                    + "可能原因：1) 该协议记录已被删除；2) 组件是从别的环境导过来的，协议没跟着一起导过来\n"
                    + "处理办法：到「协议管理」页重新上传协议包，回到本组件重新选择协议并保存，然后重新开启");
        }
        String jarName = protocol.getNewName();
        // 协议包固定按「后端启动目录/protocol」查找（PROTOCOL_PATH = user.dir/protocol）；
        // 库里的 local_url 是上传时的原始路径（可能是另一台机器的），换机后必然失效，不能拿它找文件
        String jarPath = PROTOCOL_PATH + File.separator + jarName;
        if (!new File(jarPath).exists()) {
            throw new CommonWarnException("协议加载失败：「" + protocol.getProtocolName() + "」的协议包文件不存在\n"
                    + "文件名：" + jarName + "\n"
                    + "查找目录：" + PROTOCOL_PATH + "\n"
                    + "可能原因：1) 协议包没上传到本机 —— 库里协议记录在、文件却不在，换机部署或迁移时最容易漏；"
                    + "2) 后端不是从 labdatahub-admin 目录启动的 —— 协议包按「启动目录/protocol」查找；"
                    + "3) 协议包被清理或移动过\n"
                    + "处理办法：到「协议管理」页重新上传该协议包（网络类型选 " + protocol.getProtocolType() + "），"
                    + "或把文件放到上述目录、文件名保持为 " + jarName + "，然后重新开启组件");
        }
        try {
            ProtocolManager.addProtocol(protocol.getId(), protocol.getProtocolType(), jarPath, protocol.getMainClassPath());
        } catch (Exception e) {
            throw new CommonWarnException("协议加载失败：「" + protocol.getProtocolName() + "」的协议包解析不出来\n"
                    + "文件名：" + jarName + "\n"
                    + "主类：" + protocol.getMainClassPath() + "\n"
                    + "错误：" + e + "\n"
                    + "可能原因：1) 上传时网络类型选错了，包与协议类型不匹配；"
                    + "2) 包内没有该主类（不是本平台 scprotocol 编译出来的）；"
                    + "3) 包内依赖与后端版本冲突\n"
                    + "处理办法：用 scprotocol 重新编译出 quickly_create.jar，到「协议管理」页按正确的网络类型（"
                    + protocol.getProtocolType() + "）重新上传");
        }
    }
}
