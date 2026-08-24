//由AI修改
package com.labdatahub.business.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.labdatahub.business.domain.LabdatahubProperties;
import com.labdatahub.business.service.ILabdatahubPropertiesService;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.brother_tcp.BrotherTcpReadConfig;
import com.labdatahub.component.fanuc_focas.FanucFocasReadConfig;
import com.labdatahub.component.fins_tcp.FinsReadConfig;
import com.labdatahub.component.mitsubishi_tcp.MitsubishiReadConfig;
import com.labdatahub.component.modbus_tcp.ModbusReadConfig;
import com.labdatahub.component.s7_tcp.S7ReadConfig;

/**
 * 物模型属性解析元数据工具类
 * 构建各协议读取配置时，根据设备/产品的物模型属性（labdatahub_properties）
 * 把 dataType/字节序/缩放/偏移查出来塞进读取配置，随消息传给协议端做按类型解析
 */
public class ParseMetaUtils {

    private ParseMetaUtils() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 查询属性解析元数据
     * 优先查设备自定义属性（belongSn=设备SN），查不到回退产品属性（belongSn=产品SN，设备继承）
     *
     * @param deviceSn  设备SN
     * @param productSn 产品SN
     * @param code      属性标识符
     * @return 物模型属性，查不到返回null
     */
    public static LabdatahubProperties resolve(String deviceSn, String productSn, String code) {
        ILabdatahubPropertiesService service = SpringUtils.getBean(ILabdatahubPropertiesService.class);
        LabdatahubProperties property = null;
        if (StringUtils.isNotEmpty(deviceSn)) {
            property = service.getOne(new LambdaQueryWrapper<LabdatahubProperties>()
                    .eq(LabdatahubProperties::getBelongSn, deviceSn)
                    .eq(LabdatahubProperties::getIdentifier, code), false);
        }
        if (property == null && StringUtils.isNotEmpty(productSn)) {
            property = service.getOne(new LambdaQueryWrapper<LabdatahubProperties>()
                    .eq(LabdatahubProperties::getBelongSn, productSn)
                    .eq(LabdatahubProperties::getIdentifier, code), false);
        }
        return property;
    }

    /**
     * 把解析元数据应用到Modbus读取配置
     */
    public static void applyTo(ModbusReadConfig config, String deviceSn, String productSn, String code) {
        LabdatahubProperties property = resolve(deviceSn, productSn, code);
        if (property == null) {
            return;
        }
        config.setDataType(property.getDataType());
        config.setByteOrder(property.getByteOrder());
        config.setIsSigned(property.getIsSigned());
        config.setScale(property.getScale());
        config.setOffset(property.getOffset());
    }

    /**
     * 把解析元数据应用到S7读取配置
     */
    public static void applyTo(S7ReadConfig config, String deviceSn, String productSn, String code) {
        LabdatahubProperties property = resolve(deviceSn, productSn, code);
        if (property == null) {
            return;
        }
        config.setDataType(property.getDataType());
        config.setByteOrder(property.getByteOrder());
        config.setIsSigned(property.getIsSigned());
        config.setScale(property.getScale());
        config.setOffset(property.getOffset());
    }

    /**
     * 把解析元数据应用到FINS读取配置
     */
    public static void applyTo(FinsReadConfig config, String deviceSn, String productSn, String code) {
        LabdatahubProperties property = resolve(deviceSn, productSn, code);
        if (property == null) {
            return;
        }
        config.setDataType(property.getDataType());
        config.setByteOrder(property.getByteOrder());
        config.setIsSigned(property.getIsSigned());
        config.setScale(property.getScale());
        config.setOffset(property.getOffset());
    }

    /**
     * 把解析元数据应用到三菱MC读取配置
     */
    public static void applyTo(MitsubishiReadConfig config, String deviceSn, String productSn, String code) {
        LabdatahubProperties property = resolve(deviceSn, productSn, code);
        if (property == null) {
            return;
        }
        config.setDataType(property.getDataType());
        config.setByteOrder(property.getByteOrder());
        config.setIsSigned(property.getIsSigned());
        config.setScale(property.getScale());
        config.setOffset(property.getOffset());
    }

    /**
     * 把解析元数据应用到Brother读取配置
     */
    public static void applyTo(BrotherTcpReadConfig config, String deviceSn, String productSn, String code) {
        LabdatahubProperties property = resolve(deviceSn, productSn, code);
        if (property == null) {
            return;
        }
        config.setDataType(property.getDataType());
        config.setByteOrder(property.getByteOrder());
        config.setIsSigned(property.getIsSigned());
        config.setScale(property.getScale());
        config.setOffset(property.getOffset());
    }

    /**
     * 把解析元数据应用到FANUC FOCAS2读取配置
     */
    public static void applyTo(FanucFocasReadConfig config, String deviceSn, String productSn, String code) {
        LabdatahubProperties property = resolve(deviceSn, productSn, code);
        if (property == null) {
            return;
        }
        config.setDataType(property.getDataType());
        config.setByteOrder(property.getByteOrder());
        config.setIsSigned(property.getIsSigned());
        config.setScale(property.getScale());
        config.setOffset(property.getOffset());
    }
}
