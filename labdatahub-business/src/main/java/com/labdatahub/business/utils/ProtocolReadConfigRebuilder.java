//由AI修改
package com.labdatahub.business.utils;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.labdatahub.business.domain.LabdatahubBrotherConfig;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.domain.LabdatahubFanucConfig;
import com.labdatahub.business.domain.LabdatahubModbusConfig;
import com.labdatahub.business.domain.LabdatahubMitsubishiConfig;
import com.labdatahub.business.domain.LabdatahubMitsubishiCncConfig;
import com.labdatahub.business.domain.LabdatahubOmronFinsConfig;
import com.labdatahub.business.domain.LabdatahubS71200Config;
import com.labdatahub.business.service.ILabdatahubBrotherConfigService;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubFanucConfigService;
import com.labdatahub.business.service.ILabdatahubModbusConfigService;
import com.labdatahub.business.service.ILabdatahubMitsubishiConfigService;
import com.labdatahub.business.service.ILabdatahubMitsubishiCncConfigService;
import com.labdatahub.business.service.ILabdatahubOmronFinsConfigService;
import com.labdatahub.business.service.ILabdatahubS71200ConfigService;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.brother_tcp.BrotherTcpMessageScheduler;
import com.labdatahub.component.brother_tcp.BrotherTcpReadConfig;
import com.labdatahub.component.fanuc_focas.FanucFocasMessageScheduler;
import com.labdatahub.component.fanuc_focas.FanucFocasReadConfig;
import com.labdatahub.component.fins_tcp.FinsMessageScheduler;
import com.labdatahub.component.mitsubishi_cnc_tcp.MitsubishiCncMessageScheduler;
import com.labdatahub.component.mitsubishi_cnc_tcp.MitsubishiCncReadConfig;
import com.labdatahub.component.fins_tcp.FinsReadConfig;
import com.labdatahub.component.mitsubishi_tcp.MitsubishiMessageScheduler;
import com.labdatahub.component.mitsubishi_tcp.MitsubishiReadConfig;
import com.labdatahub.component.modbus_tcp.ModbusMessageScheduler;
import com.labdatahub.component.modbus_tcp.ModbusReadConfig;
import com.labdatahub.component.s7_tcp.S7MessageScheduler;
import com.labdatahub.component.s7_tcp.S7ReadConfig;

/**
 * 协议读取配置重建工具类
 * 物模型解析参数（dataType/字节序/缩放/偏移）变更后，重建设备的轮询读取配置：
 * 只重建当前正在轮询的配置（addReadConfig 内部会先取消旧任务再注册），已关闭的不重新拉起。
 */
public class ProtocolReadConfigRebuilder {

    private ProtocolReadConfigRebuilder() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 重建单个设备的轮询读取配置
     *
     * @param deviceSn 设备SN
     */
    public static void rebuildDevice(String deviceSn) {
        if (StringUtils.isEmpty(deviceSn)) {
            return;
        }
        LabdatahubDevice device = SpringUtils.getBean(ILabdatahubDeviceService.class).getOne(
                new LambdaQueryWrapper<LabdatahubDevice>().eq(LabdatahubDevice::getDeviceSn, deviceSn), false);
        if (device == null) {
            return;
        }
        rebuildForDevice(device);
    }

    /**
     * 重建产品下全部设备的轮询读取配置（产品物模型变更时调用）
     *
     * @param productSn 产品SN
     */
    public static void rebuildByProduct(String productSn) {
        if (StringUtils.isEmpty(productSn)) {
            return;
        }
        List<LabdatahubDevice> deviceList = SpringUtils.getBean(ILabdatahubDeviceService.class).list(
                new LambdaQueryWrapper<LabdatahubDevice>().eq(LabdatahubDevice::getProductSn, productSn));
        for (LabdatahubDevice device : deviceList) {
            rebuildForDevice(device);
        }
    }

    /**
     * 重建单个设备的四类协议轮询（Modbus/S7/Fins/Brother），仅重建当前正在轮询的配置
     */
    private static void rebuildForDevice(LabdatahubDevice device) {
        if (StringUtils.isEmpty(device.getComponentId())) {
            return;
        }
        rebuildModbus(device);
        rebuildS7(device);
        rebuildFins(device);
        rebuildBrother(device);
        rebuildFanuc(device);
        rebuildMitsubishi(device);
        rebuildMitsubishiCnc(device);
    }

    /**
     * 重建 Modbus 读取配置（构建逻辑与 LabdatahubModbusConfigController 保持一致）
     */
    private static void rebuildModbus(LabdatahubDevice device) {
        String componentId = device.getComponentId();
        String deviceSn = device.getDeviceSn();
        List<LabdatahubModbusConfig> list = SpringUtils.getBean(ILabdatahubModbusConfigService.class).list(
                new LambdaQueryWrapper<LabdatahubModbusConfig>().eq(LabdatahubModbusConfig::getBelongSn, deviceSn));
        for (LabdatahubModbusConfig o : list) {
            if (o.getDelayTime() == null || o.getIntervalTime() == null) {
                continue;
            }
            if (!ModbusMessageScheduler.isReadConfigRunning(componentId, deviceSn, o.getCode())) {
                continue;
            }
            ModbusReadConfig config = new ModbusReadConfig();
            config.setDeviceSn(o.getBelongSn());
            config.setCode(o.getCode());
            config.setDelayTime(o.getDelayTime().intValue());
            config.setIntervalTime(o.getIntervalTime().intValue());
            config.setSlaveId(device.getSlaveId());
            config.setRegisterRange(o.getRegisterRange());
            config.setFunctionCode(o.getFunctionCode());
            ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
            ModbusMessageScheduler.addReadConfig(componentId, config);
        }
    }

    /**
     * 重建 S7 读取配置（构建逻辑与 LabdatahubS71200ConfigController 保持一致）
     */
    private static void rebuildS7(LabdatahubDevice device) {
        String componentId = device.getComponentId();
        String deviceSn = device.getDeviceSn();
        List<LabdatahubS71200Config> list = SpringUtils.getBean(ILabdatahubS71200ConfigService.class).list(
                new LambdaQueryWrapper<LabdatahubS71200Config>().eq(LabdatahubS71200Config::getBelongSn, deviceSn));
        for (LabdatahubS71200Config o : list) {
            if (o.getDelayTime() == null || o.getIntervalTime() == null) {
                continue;
            }
            if (!S7MessageScheduler.isReadConfigRunning(componentId, deviceSn, o.getCode())) {
                continue;
            }
            S7ReadConfig config = new S7ReadConfig();
            config.setDeviceSn(o.getBelongSn());
            config.setCode(o.getCode());
            config.setDelayTime(o.getDelayTime().intValue());
            config.setIntervalTime(o.getIntervalTime().intValue());
            config.setDbNumber(o.getDbNumber());
            config.setBlockType(o.getBlockType());
            config.setAreaType(o.getAreaType());
            config.setBitOffset(o.getBitOffset());
            config.setStartAddress(o.getStartAddress());
            config.setLength(o.getLength());
            ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
            S7MessageScheduler.addReadConfig(componentId, config);
        }
    }

    /**
     * 重建 FINS 读取配置（构建逻辑与 LabdatahubOmronFinsConfigController 保持一致）
     */
    private static void rebuildFins(LabdatahubDevice device) {
        String componentId = device.getComponentId();
        String deviceSn = device.getDeviceSn();
        List<LabdatahubOmronFinsConfig> list = SpringUtils.getBean(ILabdatahubOmronFinsConfigService.class).list(
                new LambdaQueryWrapper<LabdatahubOmronFinsConfig>().eq(LabdatahubOmronFinsConfig::getBelongSn, deviceSn));
        for (LabdatahubOmronFinsConfig o : list) {
            if (o.getDelayTime() == null || o.getIntervalTime() == null) {
                continue;
            }
            if (!FinsMessageScheduler.isReadConfigRunning(componentId, deviceSn, o.getCode())) {
                continue;
            }
            FinsReadConfig config = new FinsReadConfig();
            config.setDeviceSn(o.getBelongSn());
            config.setCode(o.getCode());
            config.setDelayTime(o.getDelayTime().intValue());
            config.setIntervalTime(o.getIntervalTime().intValue());
            config.setAreaCode(o.getAreaCode());
            config.setStartAddress(o.getStartAddress());
            config.setLength(o.getLength());
            ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
            FinsMessageScheduler.addReadConfig(componentId, config);
        }
    }

    /**
     * 重建三菱MC读取配置（构建逻辑与 LabdatahubMitsubishiConfigController 保持一致）
     */
    private static void rebuildMitsubishi(LabdatahubDevice device) {
        String componentId = device.getComponentId();
        String deviceSn = device.getDeviceSn();
        List<LabdatahubMitsubishiConfig> list = SpringUtils.getBean(ILabdatahubMitsubishiConfigService.class).list(
                new LambdaQueryWrapper<LabdatahubMitsubishiConfig>().eq(LabdatahubMitsubishiConfig::getBelongSn, deviceSn));
        for (LabdatahubMitsubishiConfig o : list) {
            if (o.getDelayTime() == null || o.getIntervalTime() == null) {
                continue;
            }
            if (!MitsubishiMessageScheduler.isReadConfigRunning(componentId, deviceSn, o.getCode())) {
                continue;
            }
            MitsubishiReadConfig config = new MitsubishiReadConfig();
            config.setDeviceSn(o.getBelongSn());
            config.setCode(o.getCode());
            config.setDelayTime(o.getDelayTime().intValue());
            config.setIntervalTime(o.getIntervalTime().intValue());
            config.setAreaCode(o.getAreaCode());
            config.setStartAddress(o.getStartAddress());
            config.setLength(o.getLength());
            config.setProtocolMode(o.getProtocolMode());
            ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
            MitsubishiMessageScheduler.addReadConfig(componentId, config);
        }
    }

    /**
     * 重建 Brother 读取配置（构建逻辑与 LabdatahubBrotherConfigController 保持一致）
     */
    private static void rebuildBrother(LabdatahubDevice device) {
        String componentId = device.getComponentId();
        String deviceSn = device.getDeviceSn();
        List<LabdatahubBrotherConfig> list = SpringUtils.getBean(ILabdatahubBrotherConfigService.class).list(
                new LambdaQueryWrapper<LabdatahubBrotherConfig>().eq(LabdatahubBrotherConfig::getBelongSn, deviceSn));
        for (LabdatahubBrotherConfig o : list) {
            if (o.getDelayTime() == null || o.getIntervalTime() == null) {
                continue;
            }
            if (!BrotherTcpMessageScheduler.isReadConfigRunning(componentId, deviceSn, o.getCode())) {
                continue;
            }
            BrotherTcpReadConfig config = new BrotherTcpReadConfig();
            config.setDeviceSn(o.getBelongSn());
            config.setCode(o.getCode());
            config.setDelayTime(o.getDelayTime().intValue());
            config.setIntervalTime(o.getIntervalTime().intValue());
            config.setDataArea(o.getDataArea());
            config.setRowNumber(o.getRowNumber());
            config.setFieldIndex(o.getFieldIndex());
            ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
            BrotherTcpMessageScheduler.addReadConfig(componentId, config);
        }
    }

    /**
     * 重建 FANUC FOCAS2 读取配置（构建逻辑与 LabdatahubFanucConfigController 保持一致）
     */
    private static void rebuildFanuc(LabdatahubDevice device) {
        String componentId = device.getComponentId();
        String deviceSn = device.getDeviceSn();
        List<LabdatahubFanucConfig> list = SpringUtils.getBean(ILabdatahubFanucConfigService.class).list(
                new LambdaQueryWrapper<LabdatahubFanucConfig>().eq(LabdatahubFanucConfig::getBelongSn, deviceSn));
        for (LabdatahubFanucConfig o : list) {
            if (o.getDelayTime() == null || o.getIntervalTime() == null) {
                continue;
            }
            if (!FanucFocasMessageScheduler.isReadConfigRunning(componentId, deviceSn, o.getCode())) {
                continue;
            }
            FanucFocasReadConfig config = new FanucFocasReadConfig();
            config.setDeviceSn(o.getBelongSn());
            config.setCode(o.getCode());
            config.setDelayTime(o.getDelayTime().intValue());
            config.setIntervalTime(o.getIntervalTime().intValue());
            config.setReadType(o.getReadType());
            config.setParam1(o.getParam1());
            config.setParam2(o.getParam2());
            ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
            FanucFocasMessageScheduler.addReadConfig(componentId, config);
        }
    }

    /**
     * 重建三菱CNC TCP(MOCHA)读取配置（构建逻辑与 LabdatahubMitsubishiCncConfigController 保持一致）
     */
    private static void rebuildMitsubishiCnc(LabdatahubDevice device) {
        String componentId = device.getComponentId();
        String deviceSn = device.getDeviceSn();
        List<LabdatahubMitsubishiCncConfig> list = SpringUtils.getBean(ILabdatahubMitsubishiCncConfigService.class).list(
                new LambdaQueryWrapper<LabdatahubMitsubishiCncConfig>().eq(LabdatahubMitsubishiCncConfig::getBelongSn, deviceSn));
        for (LabdatahubMitsubishiCncConfig o : list) {
            if (o.getDelayTime() == null || o.getIntervalTime() == null) {
                continue;
            }
            if (!MitsubishiCncMessageScheduler.isReadConfigRunning(componentId, deviceSn, o.getCode())) {
                continue;
            }
            MitsubishiCncReadConfig config = new MitsubishiCncReadConfig();
            config.setDeviceSn(o.getBelongSn());
            config.setCode(o.getCode());
            config.setDelayTime(o.getDelayTime().intValue());
            config.setIntervalTime(o.getIntervalTime().intValue());
            config.setReadType(o.getReadType());
            config.setAxisNo(o.getAxisNo());
            ParseMetaUtils.applyTo(config, device.getDeviceSn(), device.getProductSn(), o.getCode());
            MitsubishiCncMessageScheduler.addReadConfig(componentId, config);
        }
    }
}
