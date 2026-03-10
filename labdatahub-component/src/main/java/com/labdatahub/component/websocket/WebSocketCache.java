package com.labdatahub.component.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Description: 缓存
 * @Author: ruoyi
 * @CreateTime: 2025-10-28
 */
public class WebSocketCache {
    //组件id和端口
    public static final Map<String,Integer> COMPONENT_PORT = new HashMap<>();
    //端口和组件id
    public static final Map<Integer,String> PORT_COMPONENT = new HashMap<>();
    //设备连接
    public static final Map<String,String> DEVICE_CLIENT = new HashMap<>();
    //设备连接
    public static final Map<String, Set<String>> CLIENT_DEVICE = new HashMap<>();

    public static void bindDevice(String channelId,String deviceSn){
        Set<String> set = CLIENT_DEVICE.getOrDefault(channelId,null);
        if(set!=null){
            set.add(deviceSn);
        }else {
            Set<String> deviceList = new HashSet<>();
            deviceList.add(deviceSn);
            CLIENT_DEVICE.put(channelId,deviceList);
        }
    }
}
