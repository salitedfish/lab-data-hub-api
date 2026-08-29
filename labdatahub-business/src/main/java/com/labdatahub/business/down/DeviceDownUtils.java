package com.labdatahub.business.down;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.labdatahub.business.domain.LabdatahubComponent;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.domain.LabdatahubDeviceLogs;
import com.labdatahub.business.domain.LabdatahubFunction;
import com.labdatahub.business.domain.LabdatahubFunctionRecord;
import com.labdatahub.business.process.function.FunctionProcessor;
import com.labdatahub.business.service.ILabdatahubComponentService;
import com.labdatahub.business.service.ILabdatahubDeviceLogsService;
import com.labdatahub.business.service.ILabdatahubFunctionRecordService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.coap.CoapCache;
import com.labdatahub.component.http.HttpClientManager;
import com.labdatahub.component.message.DecodeMessage;
import com.labdatahub.component.message.MessageCache;
import com.labdatahub.component.modbus_tcp.ModbusDataReader;
import com.labdatahub.component.mqtt.client.MqttClientManager;
import com.labdatahub.component.mqtt.server.MqttBrokerManager;
import com.labdatahub.component.protocol.EncodeMessage;
import com.labdatahub.component.protocol.ProtocolManager;
import com.labdatahub.component.tcp.TCPServerManager;
import com.labdatahub.component.udp.UDPServerManager;
import com.labdatahub.component.websocket.WebSocketFrameHandler;

/**
 * @Description: 设备功能下发工具
 * @Author: ruoyi
 * @CreateTime: 2025-10-25
 */
public class DeviceDownUtils {

    /**
     * 功能下发统一方法
     */
    public static boolean functionDown(String deviceSn,String functionCode,String params,String triggerType) throws InvocationTargetException, IllegalAccessException, MqttException {
        LabdatahubDevice device = CacheUtils.getDeviceBySn(deviceSn);
        LabdatahubComponent component = SpringUtils.getBean(ILabdatahubComponentService.class).getById(device.getComponentId());
        String componentId = component.getId();
        String componentType = component.getNetType();
        String protocolId = component.getProtocolId();
        String customConfig = device.getCustomConfig();
        DecodeMessage decodeMessage = MessageCache.getDeviceLastData(deviceSn);
        if(decodeMessage==null){
            LabdatahubDeviceLogs log = SpringUtils.getBean(ILabdatahubDeviceLogsService.class).getOne(new LambdaQueryWrapper<LabdatahubDeviceLogs>()
                    .eq(LabdatahubDeviceLogs::getDeviceSn,deviceSn)
                    .orderByDesc(LabdatahubDeviceLogs::getReportTime)
                    .eq(LabdatahubDeviceLogs::getLogType,"PROPERTY")
                    .last(" limit 1 "));
            if(log!=null){
                decodeMessage = JSONObject.parseObject(log.getProperties(),DecodeMessage.class);
                MessageCache.setDeviceLastData(deviceSn,decodeMessage);
            }else {
                decodeMessage = new DecodeMessage();
                MessageCache.setDeviceLastData(deviceSn,new DecodeMessage());
            }
        }
        boolean isOk = false;
        try {
            switch (componentType){
                case "MQTT_BROKER":{
                    isOk = mqttBrokerDown(deviceSn, functionCode,decodeMessage.getProperties(),params, componentId, protocolId,customConfig);
                    break;
                }
                case "MQTT_CLIENT":{
                    isOk = mqttClientDown(deviceSn, functionCode,decodeMessage.getProperties(),params, componentId, protocolId,customConfig);
                    break;
                }
                case "TCP_SERVER":{
                    isOk = tcpServerDown(deviceSn, functionCode,decodeMessage.getProperties(),params, componentId, protocolId,customConfig);
                    break;
                }
                case "UDP_SERVER":{
                    isOk = udpServerDown(deviceSn, functionCode,decodeMessage.getProperties(),params, componentId, protocolId,customConfig);
                    break;
                }
                case "HTTP_SERVER":{
                    isOk = httpServerDown(deviceSn, functionCode,decodeMessage.getProperties(),params, componentId, protocolId,customConfig);
                    break;
                }
                case "COAP_SERVER":{
                    isOk = coapServerDown(deviceSn, functionCode,decodeMessage.getProperties(),params, componentId, protocolId,customConfig);
                    break;
                }
                case "WEBSOCKET_SERVER":{
                    isOk = webSocketServerDown(deviceSn, functionCode,decodeMessage.getProperties(),params, componentId, protocolId,customConfig);
                    break;
                }
                case "MODBUS_TCP":{
                    isOk = modbusTcpDown(deviceSn,device.getSlaveId(), functionCode,decodeMessage.getProperties(),params, componentId, protocolId,customConfig);
                    break;
                }
                case "S71200_TCP":{
                    isOk = s71200TcpDown(deviceSn, functionCode,decodeMessage.getProperties(),params, componentId, protocolId,customConfig);
                    break;
                }
                case "OMRONFINS_TCP":{
                    isOk = omronFinsTcpDown(deviceSn, functionCode,decodeMessage.getProperties(),params, componentId, protocolId,customConfig);
                    break;
                }
                case "BROTHER_TCP":{
                    isOk = brotherTcpDown(deviceSn, functionCode,decodeMessage.getProperties(),params, componentId, protocolId,customConfig);
                    break;
                }
                case "FANUC_TCP":{
                    isOk = fanucFocasDown(deviceSn, functionCode,decodeMessage.getProperties(),params, componentId, protocolId,customConfig);
                    break;
                }
                case "MITSUBISHI_TCP":{
                    isOk = mitsubishiTcpDown(deviceSn, functionCode,decodeMessage.getProperties(),params, componentId, protocolId,customConfig);
                    break;
                }
                case "MITSUBISHI_CNC_TCP":{
                    isOk = mitsubishiTcpDown(deviceSn, functionCode,decodeMessage.getProperties(),params, componentId, protocolId,customConfig);
                    break;
                }
                default:
            }
        }catch (Exception ignore){}
        LabdatahubFunctionRecord record = new LabdatahubFunctionRecord();
        record.setDeviceSn(deviceSn);
        record.setFunctionCode(functionCode);
        record.setCreateTime(new Date());
        record.setFunctionParams(params);
        record.setIsSuccess(isOk?"1":"0");
        record.setTriggerType(triggerType);
        LabdatahubFunction function = CacheUtils.getDeviceFunctionCache(deviceSn,functionCode);
        if(function!=null){
            record.setFunctionId(function.getId());
            record.setFunctionName(function.getFunctionName());
        }
        if(FunctionProcessor.getInstance().isRunning()){
            FunctionProcessor.getInstance().addLog(record);
        }else {
            SpringUtils.getBean(ILabdatahubFunctionRecordService.class).save(record);
        }
        return isOk;
    }



    /**
     * 功能下发统一方法
     */
    public static boolean functionDown(String deviceSn,String functionCode,String params,String componentId,String componentType,String protocolId,String customConfig,String triggerType) throws InvocationTargetException, IllegalAccessException, MqttException {
        DecodeMessage decodeMessage = MessageCache.getDeviceLastData(deviceSn);
        if(decodeMessage==null){
            LabdatahubDeviceLogs log = SpringUtils.getBean(ILabdatahubDeviceLogsService.class).getOne(new LambdaQueryWrapper<LabdatahubDeviceLogs>()
                    .eq(LabdatahubDeviceLogs::getDeviceSn,deviceSn)
                    .orderByDesc(LabdatahubDeviceLogs::getReportTime)
                    .eq(LabdatahubDeviceLogs::getLogType,"PROPERTY")
                    .last(" limit 1 "));
            if(log!=null){
                decodeMessage = JSONObject.parseObject(log.getProperties(),DecodeMessage.class);
                MessageCache.setDeviceLastData(deviceSn,decodeMessage);
            }else {
                decodeMessage = new DecodeMessage();
                MessageCache.setDeviceLastData(deviceSn,new DecodeMessage());
            }
        }
        LabdatahubDevice device = CacheUtils.getDeviceBySn(deviceSn);
        boolean isOk = false;
        try {
            switch (componentType) {
                case "MQTT_BROKER": {
                    isOk = mqttBrokerDown(deviceSn, functionCode, decodeMessage.getProperties(), params, componentId, protocolId, customConfig);
                    break;
                }
                case "MQTT_CLIENT": {
                    isOk = mqttClientDown(deviceSn, functionCode, decodeMessage.getProperties(), params, componentId, protocolId, customConfig);
                    break;
                }
                case "TCP_SERVER": {
                    isOk = tcpServerDown(deviceSn, functionCode, decodeMessage.getProperties(), params, componentId, protocolId, customConfig);
                    break;
                }
                case "UDP_SERVER": {
                    isOk = udpServerDown(deviceSn, functionCode, decodeMessage.getProperties(), params, componentId, protocolId, customConfig);
                    break;
                }
                case "HTTP_SERVER": {
                    isOk = httpServerDown(deviceSn, functionCode, decodeMessage.getProperties(), params, componentId, protocolId, customConfig);
                    break;
                }
                case "COAP_SERVER": {
                    isOk = coapServerDown(deviceSn, functionCode, decodeMessage.getProperties(), params, componentId, protocolId, customConfig);
                    break;
                }
                case "WEBSOCKET_SERVER": {
                    isOk = webSocketServerDown(deviceSn, functionCode, decodeMessage.getProperties(), params, componentId, protocolId, customConfig);
                    break;
                }
                case "MODBUS_TCP": {
                    isOk = modbusTcpDown(deviceSn, device.getSlaveId(), functionCode, decodeMessage.getProperties(), params, componentId, protocolId, customConfig);
                    break;
                }
                case "S71200_TCP": {
                    isOk = s71200TcpDown(deviceSn,  functionCode, decodeMessage.getProperties(), params, componentId, protocolId, customConfig);
                    break;
                }
                case "OMRONFINS_TCP": {
                    isOk = omronFinsTcpDown(deviceSn,  functionCode, decodeMessage.getProperties(), params, componentId, protocolId, customConfig);
                    break;
                }
                case "BROTHER_TCP": {
                    isOk = brotherTcpDown(deviceSn,  functionCode, decodeMessage.getProperties(), params, componentId, protocolId, customConfig);
                    break;
                }
                case "FANUC_TCP": {
                    isOk = fanucFocasDown(deviceSn,  functionCode, decodeMessage.getProperties(), params, componentId, protocolId, customConfig);
                    break;
                }
                case "MITSUBISHI_TCP": {
                    isOk = mitsubishiTcpDown(deviceSn,  functionCode, decodeMessage.getProperties(), params, componentId, protocolId, customConfig);
                    break;
                }
                case "MITSUBISHI_CNC_TCP": {
                    isOk = mitsubishiTcpDown(deviceSn, functionCode, decodeMessage.getProperties(), params, componentId, protocolId, customConfig);
                    break;
                }
                default:
            }
        }catch (Exception ignore){}
        LabdatahubFunctionRecord record = new LabdatahubFunctionRecord();
        record.setDeviceSn(deviceSn);
        record.setFunctionCode(functionCode);
        record.setCreateTime(new Date());
        record.setFunctionParams(params);
        record.setIsSuccess(isOk?"1":"0");
        record.setTriggerType(triggerType);
        LabdatahubFunction function = CacheUtils.getDeviceFunctionCache(deviceSn,functionCode);
        if(function!=null){
            record.setFunctionId(function.getId());
            record.setFunctionName(function.getFunctionName());
        }
        if(FunctionProcessor.getInstance().isRunning()){
            FunctionProcessor.getInstance().addLog(record);
        }else {
            SpringUtils.getBean(ILabdatahubFunctionRecordService.class).save(record);
        }
        return isOk;
    }

    /**
     * MQTT_BROKER功能下发
     */
    public static boolean mqttBrokerDown(String deviceSn, String functionCode, Map<String,Object> properties,String params, String componentId, String protocolId,String customConfig) throws InvocationTargetException, IllegalAccessException {
        Method encodeMethod = ProtocolManager.ENCODE_METHOD.getOrDefault(protocolId,null);
        Object instance = ProtocolManager.CLASS_INSTANCE.getOrDefault(protocolId,null);
//        Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig);
        Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig,null);
        EncodeMessage encodeMessage = JSONObject.parseObject(JSONObject.toJSONString(result), EncodeMessage.class);
        if(!encodeMessage.getIsSend()){
            return true;
        }
        return MqttBrokerManager.publishMessage(componentId,encodeMessage.getTopic(),new String(encodeMessage.getContent()), encodeMessage.getQos(), encodeMessage.getRetain());
    }

    /**
     * MQTT_CLIENT功能下发
     */
    public static boolean mqttClientDown(String deviceSn, String functionCode, Map<String,Object> properties,String params, String componentId, String protocolId,String customConfig) throws InvocationTargetException, IllegalAccessException, MqttException {
        Method encodeMethod = ProtocolManager.ENCODE_METHOD.getOrDefault(protocolId,null);
        Object instance = ProtocolManager.CLASS_INSTANCE.getOrDefault(protocolId,null);
        //Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig);
        Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig,null);
        EncodeMessage encodeMessage = JSONObject.parseObject(JSONObject.toJSONString(result), EncodeMessage.class);
        if(!encodeMessage.getIsSend()){
            return true;
        }
        return MqttClientManager.publishMessage(componentId,encodeMessage.getTopic(),encodeMessage.getContent(), encodeMessage.getQos(), encodeMessage.getRetain());
    }

    /**
     * TCP_SERVER功能下发
     */
    public static boolean tcpServerDown(String deviceSn, String functionCode, Map<String,Object> properties,String params, String componentId, String protocolId,String customConfig) throws InvocationTargetException, IllegalAccessException, MqttException {
        Method encodeMethod = ProtocolManager.ENCODE_METHOD.getOrDefault(protocolId,null);
        Object instance = ProtocolManager.CLASS_INSTANCE.getOrDefault(protocolId,null);
        //Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig);
        Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig,null);
        EncodeMessage encodeMessage = JSONObject.parseObject(JSONObject.toJSONString(result), EncodeMessage.class);
        if(!encodeMessage.getIsSend()){
            return true;
        }
        return TCPServerManager.sendMessageToDevice(deviceSn,new String(encodeMessage.getContent()));
    }

    /**
     * UDP_SERVER功能下发
     */
    public static boolean udpServerDown(String deviceSn, String functionCode, Map<String,Object> properties,String params, String componentId, String protocolId,String customConfig) throws InvocationTargetException, IllegalAccessException, MqttException {
        Method encodeMethod = ProtocolManager.ENCODE_METHOD.getOrDefault(protocolId,null);
        Object instance = ProtocolManager.CLASS_INSTANCE.getOrDefault(protocolId,null);
        //Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig);
        Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig,null);
        EncodeMessage encodeMessage = JSONObject.parseObject(JSONObject.toJSONString(result), EncodeMessage.class);
        if(!encodeMessage.getIsSend()){
            return true;
        }
        return UDPServerManager.sendMessageToDevice(deviceSn,new String(encodeMessage.getContent()));
    }

    /**
     * HTTP_SERVER功能下发
     */
    public static boolean httpServerDown(String deviceSn, String functionCode, Map<String,Object> properties,String params, String componentId, String protocolId,String customConfig) throws InvocationTargetException, IllegalAccessException, MqttException {
        Method encodeMethod = ProtocolManager.ENCODE_METHOD.getOrDefault(protocolId,null);
        Object instance = ProtocolManager.CLASS_INSTANCE.getOrDefault(protocolId,null);
        String clientInfo = HttpClientManager.DEVICE_CLIENT.get(deviceSn);      
//        if(StringUtils.isNotEmpty(clientInfo)){
//            String[] ipPort = clientInfo.split("_");
//            encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,ipPort[0],ipPort[1],customConfig);
//        }else {
//            encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,null,null,customConfig);
//        }
        Map<String,Object> otherConfig = new HashMap<>();
        if(StringUtils.isNotEmpty(clientInfo)){
        	String[] ipPort = clientInfo.split("_");
        	otherConfig.put("clientIp", ipPort[0]);
        	otherConfig.put("clientPort", ipPort[1]);
        }
        encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig,otherConfig);
        return true;
    }

    /**
     * COAP_SERVER功能下发
     */
    public static boolean coapServerDown(String deviceSn, String functionCode, Map<String,Object> properties,String params, String componentId, String protocolId,String customConfig) throws InvocationTargetException, IllegalAccessException, MqttException {
        Method encodeMethod = ProtocolManager.ENCODE_METHOD.getOrDefault(protocolId,null);
        Object instance = ProtocolManager.CLASS_INSTANCE.getOrDefault(protocolId,null);
        String address = CoapCache.DEVICE_SERVER.get(deviceSn);
//        if(StringUtils.isNotEmpty(address)){
//            String[] ipPort = address.split("_");
//            encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,ipPort[0],ipPort[1],customConfig);
//        }else {
//            encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,null,null,customConfig);
//        }
        Map<String,Object> otherConfig = new HashMap<>();
        if(StringUtils.isNotEmpty(address)){
        	String[] ipPort = address.split("_");
        	otherConfig.put("clientIp", ipPort[0]);
        	otherConfig.put("clientPort", ipPort[1]);
        }
        encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig,otherConfig);
        return true;
    }

    /**
     * WEBSOCKET_SERVER功能下发
     */
    public static boolean webSocketServerDown(String deviceSn, String functionCode, Map<String,Object> properties,String params, String componentId, String protocolId,String customConfig) throws InvocationTargetException, IllegalAccessException, MqttException {
        Method encodeMethod = ProtocolManager.ENCODE_METHOD.getOrDefault(protocolId,null);
        Object instance = ProtocolManager.CLASS_INSTANCE.getOrDefault(protocolId,null);
        //Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig);
        Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig,null);
        EncodeMessage encodeMessage = JSONObject.parseObject(JSONObject.toJSONString(result), EncodeMessage.class);
        if(!encodeMessage.getIsSend()){
            return true;
        }
        return WebSocketFrameHandler.sendToDevice(componentId,deviceSn,new String(encodeMessage.getContent()));
    }


    /**
     * MODBUS_TCP功能下发
     */
    public static boolean modbusTcpDown(String deviceSn,Integer slaveId, String functionCode, Map<String,Object> properties,String params, String componentId, String protocolId,String customConfig) throws InvocationTargetException, IllegalAccessException, MqttException {
        Method encodeMethod = ProtocolManager.ENCODE_METHOD.getOrDefault(protocolId,null);
        Object instance = ProtocolManager.CLASS_INSTANCE.getOrDefault(protocolId,null);
        //Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig);
        Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig,null);
        EncodeMessage encodeMessage = JSONObject.parseObject(JSONObject.toJSONString(result), EncodeMessage.class);
        if(!encodeMessage.getIsSend()){
            return true;
        }
        return ModbusDataReader.writeMultipleHoldingRegisters(componentId,slaveId,encodeMessage.getModbusWriteJson());
    }
    
    /**
     * S71200_TCP功能下发
     */
    public static boolean s71200TcpDown(String deviceSn,String functionCode, Map<String,Object> properties,String params, String componentId, String protocolId,String customConfig) throws InvocationTargetException, IllegalAccessException, MqttException {
        Method encodeMethod = ProtocolManager.ENCODE_METHOD.getOrDefault(protocolId,null);
        Object instance = ProtocolManager.CLASS_INSTANCE.getOrDefault(protocolId,null);
        //Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig);
        Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig,null);
        EncodeMessage encodeMessage = JSONObject.parseObject(JSONObject.toJSONString(result), EncodeMessage.class);
        if(!encodeMessage.getIsSend()){
            return true;
        }
        return true;
        //return S7DataReader.writeMultipleHoldingRegisters(componentId,slaveId,encodeMessage.getModbusWriteJson());
    }
    
    /**
     * OMRONFINS_TCP功能下发
     */
    public static boolean omronFinsTcpDown(String deviceSn,String functionCode, Map<String,Object> properties,String params, String componentId, String protocolId,String customConfig) throws InvocationTargetException, IllegalAccessException, MqttException {
        Method encodeMethod = ProtocolManager.ENCODE_METHOD.getOrDefault(protocolId,null);
        Object instance = ProtocolManager.CLASS_INSTANCE.getOrDefault(protocolId,null);
        //Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig);
        Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig,null);
        EncodeMessage encodeMessage = JSONObject.parseObject(JSONObject.toJSONString(result), EncodeMessage.class);
        if(!encodeMessage.getIsSend()){
            return true;
        }
        return true;
        //return FinsDataReader.writeMultipleHoldingRegisters(componentId,slaveId,encodeMessage.getModbusWriteJson());
    }

    /**
     * MITSUBISHI_TCP功能下发（三菱 MC 协议只读采集，encode 返回 isSend=false，直接返回成功）
     */
    public static boolean mitsubishiTcpDown(String deviceSn,String functionCode, Map<String,Object> properties,String params, String componentId, String protocolId,String customConfig) throws InvocationTargetException, IllegalAccessException, MqttException {
        Method encodeMethod = ProtocolManager.ENCODE_METHOD.getOrDefault(protocolId,null);
        Object instance = ProtocolManager.CLASS_INSTANCE.getOrDefault(protocolId,null);
        Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig,null);
        EncodeMessage encodeMessage = JSONObject.parseObject(JSONObject.toJSONString(result), EncodeMessage.class);
        if(!encodeMessage.getIsSend()){
            return true;
        }
        return true;
    }

    /**
     * BROTHER_TCP功能下发（Brother NC 协议只读采集，encode 返回 isSend=false，直接返回成功）
     */
    public static boolean brotherTcpDown(String deviceSn,String functionCode, Map<String,Object> properties,String params, String componentId, String protocolId,String customConfig) throws InvocationTargetException, IllegalAccessException, MqttException {
        Method encodeMethod = ProtocolManager.ENCODE_METHOD.getOrDefault(protocolId,null);
        Object instance = ProtocolManager.CLASS_INSTANCE.getOrDefault(protocolId,null);
        Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig,null);
        EncodeMessage encodeMessage = JSONObject.parseObject(JSONObject.toJSONString(result), EncodeMessage.class);
        if(!encodeMessage.getIsSend()){
            return true;
        }
        return true;
    }

    /**
     * FANUC_TCP功能下发（FANUC FOCAS2 协议只读采集，encode 返回 isSend=false，直接返回成功）
     */
    public static boolean fanucFocasDown(String deviceSn,String functionCode, Map<String,Object> properties,String params, String componentId, String protocolId,String customConfig) throws InvocationTargetException, IllegalAccessException, MqttException {
        Method encodeMethod = ProtocolManager.ENCODE_METHOD.getOrDefault(protocolId,null);
        Object instance = ProtocolManager.CLASS_INSTANCE.getOrDefault(protocolId,null);
        Object result = encodeMethod.invoke(instance,functionCode,deviceSn,properties,params,customConfig,null);
        EncodeMessage encodeMessage = JSONObject.parseObject(JSONObject.toJSONString(result), EncodeMessage.class);
        if(!encodeMessage.getIsSend()){
            return true;
        }
        return true;
    }

}
