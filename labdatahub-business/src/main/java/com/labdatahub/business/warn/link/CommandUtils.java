package com.labdatahub.business.warn.link;

import java.util.List;

/**
 * @Description:
 * @Author: ruoyi
 * @CreateTime: 2025-11-24
 */
public class CommandUtils {
    public static void main(String[] args) {
        try {
            // 1. 解析JSON为指令树
            String json = "{\n" +
                    "    \"nodeList\": [\n" +
                    "        {\n" +
                    "            \"id\": \"d204d6rrd\",\n" +
                    "            \"name\": \"设备属性\",\n" +
                    "            \"type\": \"deviceProperty\",\n" +
                    "            \"left\": \"47px\",\n" +
                    "            \"top\": \"110px\",\n" +
                    "            \"ico\": \"el-icon-time\",\n" +
                    "            \"state\": \"success\",\n" +
                    "            \"configData\": {\n" +
                    "                \"productList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983736431073325058\",\n" +
                    "                        \"productSn\": \"product_001\",\n" +
                    "                        \"productName\": \"WS产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983736295886712834\",\n" +
                    "                        \"componentName\": \"WS网络组件_10883\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"protocolName\": \"WS协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983886502083457026\",\n" +
                    "                        \"productSn\": \"mqtt_broker_001\",\n" +
                    "                        \"productName\": \"MQTT服务端产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"2\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1987422834516848641\",\n" +
                    "                        \"productSn\": \"11\",\n" +
                    "                        \"productName\": \"11\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 0,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-11-09 15:31:43\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": \"11\",\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"0\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": null,\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"deviceList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983736532730671105\",\n" +
                    "                        \"deviceSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"deviceName\": \"WS设备001\",\n" +
                    "                        \"productId\": \"1983736431073325058\",\n" +
                    "                        \"productName\": \"WS产品\",\n" +
                    "                        \"productSn\": \"product_001\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983736295886712834\",\n" +
                    "                        \"componentName\": \"WS网络组件_10883\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"protocolName\": \"WS协议\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": \"admin\",\n" +
                    "                        \"createTime\": \"2025-10-30 11:23:40\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"week\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"propertyList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983737513321197570\",\n" +
                    "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"identifier\": \"voice\",\n" +
                    "                        \"name\": \"音量\",\n" +
                    "                        \"parentId\": \"0\",\n" +
                    "                        \"dataType\": \"float\",\n" +
                    "                        \"sortNum\": 0,\n" +
                    "                        \"fromType\": \"0\",\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"unit\": \"分贝\"\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983737513321197571\",\n" +
                    "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"identifier\": \"windSpeed\",\n" +
                    "                        \"name\": \"风速\",\n" +
                    "                        \"parentId\": \"0\",\n" +
                    "                        \"dataType\": \"float\",\n" +
                    "                        \"sortNum\": 0,\n" +
                    "                        \"fromType\": \"0\",\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"unit\": \"m/s\"\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983737513321197572\",\n" +
                    "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"identifier\": \"humidity\",\n" +
                    "                        \"name\": \"湿度\",\n" +
                    "                        \"parentId\": \"0\",\n" +
                    "                        \"dataType\": \"float\",\n" +
                    "                        \"sortNum\": 0,\n" +
                    "                        \"fromType\": \"0\",\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"unit\": \"%rh\"\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983737513321197573\",\n" +
                    "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"identifier\": \"outTemperature\",\n" +
                    "                        \"name\": \"室外温度\",\n" +
                    "                        \"parentId\": \"0\",\n" +
                    "                        \"dataType\": \"float\",\n" +
                    "                        \"sortNum\": 0,\n" +
                    "                        \"fromType\": \"0\",\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"unit\": \"℃\"\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983737513321197574\",\n" +
                    "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"identifier\": \"inTemperature\",\n" +
                    "                        \"name\": \"室内温度\",\n" +
                    "                        \"parentId\": \"0\",\n" +
                    "                        \"dataType\": \"float\",\n" +
                    "                        \"sortNum\": 0,\n" +
                    "                        \"fromType\": \"0\",\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"unit\": \"℃\"\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"deviceSn\": \"WS_DEVICE_001\",\n" +
                    "                \"productSn\": \"product_001\",\n" +
                    "                \"currentStatus\": null,\n" +
                    "                \"changeStatus\": null,\n" +
                    "                \"warnMessage\": null,\n" +
                    "                \"functionList\": [],\n" +
                    "                \"functionParams\": null,\n" +
                    "                \"attribute\": \"outTemperature\",\n" +
                    "                \"operator\": \"gt\",\n" +
                    "                \"value\": \"5\"\n" +
                    "            }\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"id\": \"iokz8t0ok\",\n" +
                    "            \"name\": \"设备属性1\",\n" +
                    "            \"type\": \"deviceProperty\",\n" +
                    "            \"left\": \"32px\",\n" +
                    "            \"top\": \"227px\",\n" +
                    "            \"ico\": \"el-icon-time\",\n" +
                    "            \"state\": \"success\",\n" +
                    "            \"configData\": {\n" +
                    "                \"productList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983736431073325058\",\n" +
                    "                        \"productSn\": \"product_001\",\n" +
                    "                        \"productName\": \"WS产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983736295886712834\",\n" +
                    "                        \"componentName\": \"WS网络组件_10883\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"protocolName\": \"WS协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983886502083457026\",\n" +
                    "                        \"productSn\": \"mqtt_broker_001\",\n" +
                    "                        \"productName\": \"MQTT服务端产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"2\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1987422834516848641\",\n" +
                    "                        \"productSn\": \"11\",\n" +
                    "                        \"productName\": \"11\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 0,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-11-09 15:31:43\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": \"11\",\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"0\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": null,\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"deviceList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983886611634483202\",\n" +
                    "                        \"deviceSn\": \"mqtt_001\",\n" +
                    "                        \"deviceName\": \"MQTT设备001\",\n" +
                    "                        \"productId\": \"1983886502083457026\",\n" +
                    "                        \"productName\": \"MQTT服务端产品\",\n" +
                    "                        \"productSn\": \"mqtt_broker_001\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": \"admin\",\n" +
                    "                        \"createTime\": \"2025-10-30 21:20:02\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"deviceType\": \"2\",\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"week\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"propertyList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983887612907122689\",\n" +
                    "                        \"belongSn\": \"mqtt_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"identifier\": \"windSpeed\",\n" +
                    "                        \"name\": \"风速\",\n" +
                    "                        \"parentId\": \"0\",\n" +
                    "                        \"dataType\": \"float\",\n" +
                    "                        \"sortNum\": 0,\n" +
                    "                        \"fromType\": \"0\",\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"unit\": \"m/s\"\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983887612907122690\",\n" +
                    "                        \"belongSn\": \"mqtt_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"identifier\": \"inTemperature\",\n" +
                    "                        \"name\": \"室内温度\",\n" +
                    "                        \"parentId\": \"0\",\n" +
                    "                        \"dataType\": \"float\",\n" +
                    "                        \"sortNum\": 0,\n" +
                    "                        \"fromType\": \"0\",\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"unit\": \"℃\"\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983887612923899906\",\n" +
                    "                        \"belongSn\": \"mqtt_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"identifier\": \"outTemperature\",\n" +
                    "                        \"name\": \"室外温度\",\n" +
                    "                        \"parentId\": \"0\",\n" +
                    "                        \"dataType\": \"float\",\n" +
                    "                        \"sortNum\": 0,\n" +
                    "                        \"fromType\": \"0\",\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"unit\": \"℃\"\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983887612923899907\",\n" +
                    "                        \"belongSn\": \"mqtt_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"identifier\": \"humidity\",\n" +
                    "                        \"name\": \"湿度\",\n" +
                    "                        \"parentId\": \"0\",\n" +
                    "                        \"dataType\": \"float\",\n" +
                    "                        \"sortNum\": 0,\n" +
                    "                        \"fromType\": \"0\",\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"unit\": \"%rh\"\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983887612936482818\",\n" +
                    "                        \"belongSn\": \"mqtt_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"identifier\": \"voice\",\n" +
                    "                        \"name\": \"音量\",\n" +
                    "                        \"parentId\": \"0\",\n" +
                    "                        \"dataType\": \"float\",\n" +
                    "                        \"sortNum\": 0,\n" +
                    "                        \"fromType\": \"0\",\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"unit\": \"分贝\"\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"deviceSn\": \"mqtt_001\",\n" +
                    "                \"productSn\": \"mqtt_broker_001\",\n" +
                    "                \"currentStatus\": null,\n" +
                    "                \"changeStatus\": null,\n" +
                    "                \"warnMessage\": null,\n" +
                    "                \"functionList\": [],\n" +
                    "                \"functionParams\": null,\n" +
                    "                \"attribute\": \"outTemperature\",\n" +
                    "                \"operator\": \"gt\",\n" +
                    "                \"value\": \"5\"\n" +
                    "            }\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"id\": \"em5xv0asah\",\n" +
                    "            \"name\": \"并且\",\n" +
                    "            \"type\": \"and\",\n" +
                    "            \"left\": \"194px\",\n" +
                    "            \"top\": \"153px\",\n" +
                    "            \"ico\": \"el-icon-caret-right\",\n" +
                    "            \"state\": \"success\",\n" +
                    "            \"configData\": {\n" +
                    "                \"productList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983736431073325058\",\n" +
                    "                        \"productSn\": \"product_001\",\n" +
                    "                        \"productName\": \"WS产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983736295886712834\",\n" +
                    "                        \"componentName\": \"WS网络组件_10883\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"protocolName\": \"WS协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983886502083457026\",\n" +
                    "                        \"productSn\": \"mqtt_broker_001\",\n" +
                    "                        \"productName\": \"MQTT服务端产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"2\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1987422834516848641\",\n" +
                    "                        \"productSn\": \"11\",\n" +
                    "                        \"productName\": \"11\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 0,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-11-09 15:31:43\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": \"11\",\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"0\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": null,\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"deviceList\": [],\n" +
                    "                \"propertyList\": [],\n" +
                    "                \"deviceSn\": null,\n" +
                    "                \"productSn\": null,\n" +
                    "                \"currentStatus\": null,\n" +
                    "                \"changeStatus\": null,\n" +
                    "                \"warnMessage\": \"123213\",\n" +
                    "                \"functionList\": [],\n" +
                    "                \"functionParams\": null,\n" +
                    "                \"warnLevel\": \"1\"\n" +
                    "            }\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"id\": \"f9ry0irzz\",\n" +
                    "            \"name\": \"指令下发\",\n" +
                    "            \"type\": \"function\",\n" +
                    "            \"left\": \"474px\",\n" +
                    "            \"top\": \"98px\",\n" +
                    "            \"ico\": \"el-icon-shopping-cart-full\",\n" +
                    "            \"state\": \"success\",\n" +
                    "            \"configData\": {\n" +
                    "                \"productList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983736431073325058\",\n" +
                    "                        \"productSn\": \"product_001\",\n" +
                    "                        \"productName\": \"WS产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983736295886712834\",\n" +
                    "                        \"componentName\": \"WS网络组件_10883\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"protocolName\": \"WS协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983886502083457026\",\n" +
                    "                        \"productSn\": \"mqtt_broker_001\",\n" +
                    "                        \"productName\": \"MQTT服务端产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"2\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1987422834516848641\",\n" +
                    "                        \"productSn\": \"11\",\n" +
                    "                        \"productName\": \"11\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 0,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-11-09 15:31:43\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": \"11\",\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"0\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": null,\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"deviceList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983736532730671105\",\n" +
                    "                        \"deviceSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"deviceName\": \"WS设备001\",\n" +
                    "                        \"productId\": \"1983736431073325058\",\n" +
                    "                        \"productName\": \"WS产品\",\n" +
                    "                        \"productSn\": \"product_001\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983736295886712834\",\n" +
                    "                        \"componentName\": \"WS网络组件_10883\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"protocolName\": \"WS协议\",\n" +
                    "                        \"status\": \"1\",\n" +
                    "                        \"createBy\": \"admin\",\n" +
                    "                        \"createTime\": \"2025-10-30 11:23:40\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"week\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"propertyList\": [],\n" +
                    "                \"deviceSn\": \"WS_DEVICE_001\",\n" +
                    "                \"productSn\": \"product_001\",\n" +
                    "                \"currentStatus\": null,\n" +
                    "                \"changeStatus\": null,\n" +
                    "                \"warnMessage\": null,\n" +
                    "                \"functionList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983737461873864705\",\n" +
                    "                        \"functionName\": \"测试指令\",\n" +
                    "                        \"functionCode\": \"TEST\",\n" +
                    "                        \"functionParams\": \"123456\",\n" +
                    "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"createTime\": \"2025-10-30 20:15:03\",\n" +
                    "                        \"createBy\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"functionParams\": \"123456\",\n" +
                    "                \"functionCode\": \"TEST\"\n" +
                    "            }\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"id\": \"nm868bt5p9\",\n" +
                    "            \"name\": \"指令下发1\",\n" +
                    "            \"type\": \"function\",\n" +
                    "            \"left\": \"373px\",\n" +
                    "            \"top\": \"345px\",\n" +
                    "            \"ico\": \"el-icon-shopping-cart-full\",\n" +
                    "            \"state\": \"success\",\n" +
                    "            \"configData\": {\n" +
                    "                \"productList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983736431073325058\",\n" +
                    "                        \"productSn\": \"product_001\",\n" +
                    "                        \"productName\": \"WS产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983736295886712834\",\n" +
                    "                        \"componentName\": \"WS网络组件_10883\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"protocolName\": \"WS协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983886502083457026\",\n" +
                    "                        \"productSn\": \"mqtt_broker_001\",\n" +
                    "                        \"productName\": \"MQTT服务端产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"2\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1987422834516848641\",\n" +
                    "                        \"productSn\": \"11\",\n" +
                    "                        \"productName\": \"11\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 0,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-11-09 15:31:43\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": \"11\",\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"0\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": null,\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"deviceList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983736532730671105\",\n" +
                    "                        \"deviceSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"deviceName\": \"WS设备001\",\n" +
                    "                        \"productId\": \"1983736431073325058\",\n" +
                    "                        \"productName\": \"WS产品\",\n" +
                    "                        \"productSn\": \"product_001\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983736295886712834\",\n" +
                    "                        \"componentName\": \"WS网络组件_10883\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"protocolName\": \"WS协议\",\n" +
                    "                        \"status\": \"1\",\n" +
                    "                        \"createBy\": \"admin\",\n" +
                    "                        \"createTime\": \"2025-10-30 11:23:40\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"week\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"propertyList\": [],\n" +
                    "                \"deviceSn\": \"WS_DEVICE_001\",\n" +
                    "                \"productSn\": \"product_001\",\n" +
                    "                \"currentStatus\": null,\n" +
                    "                \"changeStatus\": null,\n" +
                    "                \"warnMessage\": null,\n" +
                    "                \"functionList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983737461873864705\",\n" +
                    "                        \"functionName\": \"测试指令\",\n" +
                    "                        \"functionCode\": \"TEST\",\n" +
                    "                        \"functionParams\": \"123456\",\n" +
                    "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"createTime\": \"2025-10-30 20:15:03\",\n" +
                    "                        \"createBy\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"functionParams\": \"123456\",\n" +
                    "                \"functionCode\": \"TEST\"\n" +
                    "            }\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"id\": \"u8t7tu8yp8\",\n" +
                    "            \"name\": \"指令下发2\",\n" +
                    "            \"type\": \"function\",\n" +
                    "            \"left\": \"675px\",\n" +
                    "            \"top\": \"197px\",\n" +
                    "            \"ico\": \"el-icon-shopping-cart-full\",\n" +
                    "            \"state\": \"success\",\n" +
                    "            \"configData\": {\n" +
                    "                \"productList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983736431073325058\",\n" +
                    "                        \"productSn\": \"product_001\",\n" +
                    "                        \"productName\": \"WS产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983736295886712834\",\n" +
                    "                        \"componentName\": \"WS网络组件_10883\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"protocolName\": \"WS协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983886502083457026\",\n" +
                    "                        \"productSn\": \"mqtt_broker_001\",\n" +
                    "                        \"productName\": \"MQTT服务端产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"2\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1987422834516848641\",\n" +
                    "                        \"productSn\": \"11\",\n" +
                    "                        \"productName\": \"11\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 0,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-11-09 15:31:43\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": \"11\",\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"0\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": null,\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"deviceList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983736532730671105\",\n" +
                    "                        \"deviceSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"deviceName\": \"WS设备001\",\n" +
                    "                        \"productId\": \"1983736431073325058\",\n" +
                    "                        \"productName\": \"WS产品\",\n" +
                    "                        \"productSn\": \"product_001\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983736295886712834\",\n" +
                    "                        \"componentName\": \"WS网络组件_10883\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"protocolName\": \"WS协议\",\n" +
                    "                        \"status\": \"1\",\n" +
                    "                        \"createBy\": \"admin\",\n" +
                    "                        \"createTime\": \"2025-10-30 11:23:40\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"week\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"propertyList\": [],\n" +
                    "                \"deviceSn\": \"WS_DEVICE_001\",\n" +
                    "                \"productSn\": \"product_001\",\n" +
                    "                \"currentStatus\": null,\n" +
                    "                \"changeStatus\": null,\n" +
                    "                \"warnMessage\": null,\n" +
                    "                \"functionList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983737461873864705\",\n" +
                    "                        \"functionName\": \"测试指令\",\n" +
                    "                        \"functionCode\": \"TEST\",\n" +
                    "                        \"functionParams\": \"123456\",\n" +
                    "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"createTime\": \"2025-10-30 20:15:03\",\n" +
                    "                        \"createBy\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"functionParams\": \"123456\",\n" +
                    "                \"functionCode\": \"TEST\"\n" +
                    "            }\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"id\": \"enhb8btpjt\",\n" +
                    "            \"name\": \"指令下发3\",\n" +
                    "            \"type\": \"function\",\n" +
                    "            \"left\": \"641px\",\n" +
                    "            \"top\": \"280px\",\n" +
                    "            \"ico\": \"el-icon-shopping-cart-full\",\n" +
                    "            \"state\": \"success\",\n" +
                    "            \"configData\": {\n" +
                    "                \"productList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983736431073325058\",\n" +
                    "                        \"productSn\": \"product_001\",\n" +
                    "                        \"productName\": \"WS产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983736295886712834\",\n" +
                    "                        \"componentName\": \"WS网络组件_10883\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"protocolName\": \"WS协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983886502083457026\",\n" +
                    "                        \"productSn\": \"mqtt_broker_001\",\n" +
                    "                        \"productName\": \"MQTT服务端产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"2\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1987422834516848641\",\n" +
                    "                        \"productSn\": \"11\",\n" +
                    "                        \"productName\": \"11\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 0,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-11-09 15:31:43\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": \"11\",\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"0\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": null,\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"deviceList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983736532730671105\",\n" +
                    "                        \"deviceSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"deviceName\": \"WS设备001\",\n" +
                    "                        \"productId\": \"1983736431073325058\",\n" +
                    "                        \"productName\": \"WS产品\",\n" +
                    "                        \"productSn\": \"product_001\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983736295886712834\",\n" +
                    "                        \"componentName\": \"WS网络组件_10883\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"protocolName\": \"WS协议\",\n" +
                    "                        \"status\": \"1\",\n" +
                    "                        \"createBy\": \"admin\",\n" +
                    "                        \"createTime\": \"2025-10-30 11:23:40\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"week\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"propertyList\": [],\n" +
                    "                \"deviceSn\": \"WS_DEVICE_001\",\n" +
                    "                \"productSn\": \"product_001\",\n" +
                    "                \"currentStatus\": null,\n" +
                    "                \"changeStatus\": null,\n" +
                    "                \"warnMessage\": null,\n" +
                    "                \"functionList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983737461873864705\",\n" +
                    "                        \"functionName\": \"测试指令\",\n" +
                    "                        \"functionCode\": \"TEST\",\n" +
                    "                        \"functionParams\": \"123456\",\n" +
                    "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"createTime\": \"2025-10-30 20:15:03\",\n" +
                    "                        \"createBy\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"functionParams\": \"123456\",\n" +
                    "                \"functionCode\": \"TEST\"\n" +
                    "            }\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"id\": \"2q1za9diu\",\n" +
                    "            \"name\": \"指令下发4\",\n" +
                    "            \"type\": \"function\",\n" +
                    "            \"left\": \"509.667px\",\n" +
                    "            \"top\": \"452px\",\n" +
                    "            \"ico\": \"el-icon-shopping-cart-full\",\n" +
                    "            \"state\": \"success\",\n" +
                    "            \"configData\": {\n" +
                    "                \"productList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983736431073325058\",\n" +
                    "                        \"productSn\": \"product_001\",\n" +
                    "                        \"productName\": \"WS产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983736295886712834\",\n" +
                    "                        \"componentName\": \"WS网络组件_10883\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"protocolName\": \"WS协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983886502083457026\",\n" +
                    "                        \"productSn\": \"mqtt_broker_001\",\n" +
                    "                        \"productName\": \"MQTT服务端产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"2\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1987422834516848641\",\n" +
                    "                        \"productSn\": \"11\",\n" +
                    "                        \"productName\": \"11\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 0,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-11-09 15:31:43\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": \"11\",\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"0\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": null,\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"deviceList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983736532730671105\",\n" +
                    "                        \"deviceSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"deviceName\": \"WS设备001\",\n" +
                    "                        \"productId\": \"1983736431073325058\",\n" +
                    "                        \"productName\": \"WS产品\",\n" +
                    "                        \"productSn\": \"product_001\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983736295886712834\",\n" +
                    "                        \"componentName\": \"WS网络组件_10883\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"protocolName\": \"WS协议\",\n" +
                    "                        \"status\": \"1\",\n" +
                    "                        \"createBy\": \"admin\",\n" +
                    "                        \"createTime\": \"2025-10-30 11:23:40\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"week\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"propertyList\": [],\n" +
                    "                \"deviceSn\": \"WS_DEVICE_001\",\n" +
                    "                \"productSn\": \"product_001\",\n" +
                    "                \"currentStatus\": null,\n" +
                    "                \"changeStatus\": null,\n" +
                    "                \"warnMessage\": null,\n" +
                    "                \"functionList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983737461873864705\",\n" +
                    "                        \"functionName\": \"测试指令\",\n" +
                    "                        \"functionCode\": \"TEST\",\n" +
                    "                        \"functionParams\": \"123456\",\n" +
                    "                        \"belongSn\": \"WS_DEVICE_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"createTime\": \"2025-10-30 20:15:03\",\n" +
                    "                        \"createBy\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"functionParams\": \"123456\",\n" +
                    "                \"functionCode\": \"TEST\"\n" +
                    "            }\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"id\": \"50pkgl77np\",\n" +
                    "            \"name\": \"指令下发5\",\n" +
                    "            \"type\": \"function\",\n" +
                    "            \"left\": \"713.667px\",\n" +
                    "            \"top\": \"467px\",\n" +
                    "            \"ico\": \"el-icon-shopping-cart-full\",\n" +
                    "            \"state\": \"success\",\n" +
                    "            \"configData\": {\n" +
                    "                \"productList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983736431073325058\",\n" +
                    "                        \"productSn\": \"product_001\",\n" +
                    "                        \"productName\": \"WS产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983736295886712834\",\n" +
                    "                        \"componentName\": \"WS网络组件_10883\",\n" +
                    "                        \"protocolId\": \"1983735910950268930\",\n" +
                    "                        \"protocolName\": \"WS协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 11:23:16\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983886502083457026\",\n" +
                    "                        \"productSn\": \"mqtt_broker_001\",\n" +
                    "                        \"productName\": \"MQTT服务端产品\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 1,\n" +
                    "                        \"deviceType\": \"2\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-10-30 21:19:36\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"day\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"id\": \"1987422834516848641\",\n" +
                    "                        \"productSn\": \"11\",\n" +
                    "                        \"productName\": \"11\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"deviceCount\": 0,\n" +
                    "                        \"deviceType\": \"0\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": null,\n" +
                    "                        \"createTime\": \"2025-11-09 15:31:43\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": \"11\",\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"regularCleaning\": \"0\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": null,\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"deviceList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983886611634483202\",\n" +
                    "                        \"deviceSn\": \"mqtt_001\",\n" +
                    "                        \"deviceName\": \"MQTT设备001\",\n" +
                    "                        \"productId\": \"1983886502083457026\",\n" +
                    "                        \"productName\": \"MQTT服务端产品\",\n" +
                    "                        \"productSn\": \"mqtt_broker_001\",\n" +
                    "                        \"linkMethodId\": null,\n" +
                    "                        \"linkMethodName\": null,\n" +
                    "                        \"componentId\": \"1983886026080284674\",\n" +
                    "                        \"componentName\": \"MQTT服务端\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"protocolName\": \"MQTT服务端协议\",\n" +
                    "                        \"status\": \"0\",\n" +
                    "                        \"createBy\": \"admin\",\n" +
                    "                        \"createTime\": \"2025-10-30 21:20:02\",\n" +
                    "                        \"updateBy\": null,\n" +
                    "                        \"updateTime\": null,\n" +
                    "                        \"remark\": null,\n" +
                    "                        \"timeoutSeconds\": 60,\n" +
                    "                        \"deviceType\": \"2\",\n" +
                    "                        \"regularCleaning\": \"1\",\n" +
                    "                        \"retentionTime\": 1,\n" +
                    "                        \"retentionUnit\": \"week\",\n" +
                    "                        \"customConfig\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"propertyList\": [],\n" +
                    "                \"deviceSn\": \"mqtt_001\",\n" +
                    "                \"productSn\": \"mqtt_broker_001\",\n" +
                    "                \"currentStatus\": null,\n" +
                    "                \"changeStatus\": null,\n" +
                    "                \"warnMessage\": null,\n" +
                    "                \"functionList\": [\n" +
                    "                    {\n" +
                    "                        \"id\": \"1983887080943546370\",\n" +
                    "                        \"functionName\": \"测试指令\",\n" +
                    "                        \"functionCode\": \"TEST\",\n" +
                    "                        \"functionParams\": \"123213\",\n" +
                    "                        \"belongSn\": \"mqtt_001\",\n" +
                    "                        \"belongType\": \"1\",\n" +
                    "                        \"protocolId\": \"1983885892432982018\",\n" +
                    "                        \"createTime\": \"2025-10-30 21:21:35\",\n" +
                    "                        \"createBy\": null\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                \"functionParams\": \"123213\",\n" +
                    "                \"functionCode\": \"TEST\"\n" +
                    "            }\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"lineList\": [\n" +
                    "        {\n" +
                    "            \"from\": \"d204d6rrd\",\n" +
                    "            \"to\": \"em5xv0asah\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"from\": \"iokz8t0ok\",\n" +
                    "            \"to\": \"em5xv0asah\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"from\": \"em5xv0asah\",\n" +
                    "            \"to\": \"f9ry0irzz\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"from\": \"em5xv0asah\",\n" +
                    "            \"to\": \"nm868bt5p9\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"from\": \"f9ry0irzz\",\n" +
                    "            \"to\": \"u8t7tu8yp8\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"from\": \"f9ry0irzz\",\n" +
                    "            \"to\": \"enhb8btpjt\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"from\": \"nm868bt5p9\",\n" +
                    "            \"to\": \"2q1za9diu\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"from\": \"2q1za9diu\",\n" +
                    "            \"to\": \"50pkgl77np\"\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";
            List<CommandNode> commandTree = CommandTreeParser.parseCommandTree(json);

            // 2. 打印指令树结构
            printCommandTree(commandTree, 0);

            // 3. 执行指令树
            System.out.println("\n开始执行指令树...");
            CommandTreeExecutor.executeCommandTree(commandTree);
            System.out.println("所有指令执行完成");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CommandTreeExecutor.shutdown();
        }
    }

    /**
     * 打印指令树结构（兼容 Java 8）
     */
    private static void printCommandTree(List<CommandNode> tree, int depth) {
        for (CommandNode node : tree) {
            // 替代方案1：使用循环生成缩进
            StringBuilder indent = new StringBuilder();
            for (int i = 0; i < depth; i++) {
                indent.append("  ");
            }

            System.out.println(indent.toString() + "└─ " + node.getName() +
                    " (设备: " + node.getDeviceSn() + ")");

            if (!node.getChildren().isEmpty()) {
                printCommandTree(node.getChildren(), depth + 1);
            }
        }
    }
}
