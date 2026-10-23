package com.labdatahub.business.domain.enums;

import java.util.Arrays;

/**
 * @Description: 网络组件枚举
 * @Author: labdatahub
 * @CreateTime: 2025-09-18
 */
public enum NetType {
    /**
     * MQTT客户端
     */
    MQTT_CLIENT,
    /**
     * MQTT服务端
     */
    MQTT_BROKER,
    /**
     * TCP客户端
     */
    TCP_CLIENT,
    /**
     * TCP服务端
     */
    TCP_SERVER,
    /**
     * UDP客户端
     */
    UDP_CLIENT,
    /**
     * UDP服务端
     */
    UDP_SERVER,
    /**
     * HTTP服务端
     */
    HTTP_SERVER,
    /**
     * MODBUS_TCP
     */
    MODBUS_TCP,
    /**
     * S71200_TCP
     */
    S71200_TCP,
    /**
     * MITSUBISHI_MC3E_TCP
     */
    MITSUBISHI_MC3E_TCP,
    /**
     * MITSUBISHI_MC3E_UDP（同一套 3E 帧，走数据报；供应商网关常用，如 192.168.3.15:5040）
     */
    MITSUBISHI_MC3E_UDP;
}
