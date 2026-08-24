//由AI修改
package com.labdatahub.component.message;

import com.alibaba.fastjson2.JSONObject;
import com.labdatahub.common.utils.StringUtils;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 设备最新数据缓存
 */
public class MessageCache {
    /**
     * key：设备sn
     * value：数据json
     */
    // 多协议消费线程（Modbus/S7/Fins/Brother/Mitsubishi/Fanuc...）并发写不同设备，用 ConcurrentHashMap 防并发读写 HashMap 导致死循环/数据错乱
    public final static Map<String, DecodeMessage> DEVICE_LAST_DATA = new ConcurrentHashMap<>();

    /**
     * 获取设备属性最新状态数据（全部属性,可能不同属性上传时间不同）
     */
    public static DecodeMessage getDeviceLastData(String deviceSn){
        return DEVICE_LAST_DATA.getOrDefault(deviceSn,null);
    }
    /**
     * 设置设备属性（可以只设置部分属性）
     */
    public static void setDeviceLastData(String deviceSn,DecodeMessage decodeMessage){
        DecodeMessage oldData = DEVICE_LAST_DATA.getOrDefault(deviceSn,null);
        if(oldData==null|| StringUtils.isEmpty(oldData.getDeviceSn())||oldData.getProperties()==null){
            decodeMessage.setReportTime(decodeMessage.getReportTime()==null?new Date():decodeMessage.getReportTime());
            DEVICE_LAST_DATA.put(deviceSn,decodeMessage);
        }else {
            decodeMessage.getProperties().keySet().forEach(key->{
                oldData.getProperties().put(key,decodeMessage.getProperties().get(key));
            });
            oldData.setDeviceSn(decodeMessage.getDeviceSn());
            oldData.setReportTime(decodeMessage.getReportTime()==null?new Date():decodeMessage.getReportTime());
            oldData.setDeviceName(decodeMessage.getDeviceName());
            oldData.setIsRegister(decodeMessage.getIsRegister());
            oldData.setProductSn(decodeMessage.getProductSn());
            oldData.setCoapIsRecover(decodeMessage.getCoapIsRecover());
            oldData.setIsStore(decodeMessage.getIsStore());
            oldData.setIsOnline(decodeMessage.getIsOnline());
            oldData.setIsRegister(decodeMessage.getIsRegister());
            oldData.setCoapRecoverContent(decodeMessage.getCoapRecoverContent());
            oldData.setHttpReply(decodeMessage.getHttpReply());
        }
    }
}
