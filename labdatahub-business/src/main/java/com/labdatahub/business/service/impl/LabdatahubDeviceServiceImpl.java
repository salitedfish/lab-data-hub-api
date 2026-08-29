package com.labdatahub.business.service.impl;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.labdatahub.business.domain.LabdatahubFunction;
import com.labdatahub.business.domain.LabdatahubProduct;
import com.labdatahub.business.domain.LabdatahubProperties;
import com.labdatahub.business.service.*;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.business.utils.PropertyConverter;
import com.labdatahub.business.utils.ProtocolPointCopyUtil;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.utils.SecurityUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.component.message.PropertyNode;
import com.labdatahub.component.utils.PropertyToJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubDeviceMapper;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 设备Service业务层处理
 *
 * @author labdatahub
 * @date 2025-09-18
 */
@Service
public class LabdatahubDeviceServiceImpl extends ServiceImpl<LabdatahubDeviceMapper, LabdatahubDevice> implements ILabdatahubDeviceService {
    @Autowired
    private LabdatahubDeviceMapper labdatahubDeviceMapper;
    @Autowired
    private ILabdatahubPropertiesService labdatahubPropertiesService;
    @Autowired
    private ILabdatahubProductService labdatahubProductService;
    @Autowired
    private ILabdatahubFunctionService labdatahubFunctionService;
    @Autowired
    private ILabdatahubWarnConfigService labdatahubWarnConfigService;
    @Autowired
    private ILabdatahubModbusConfigService labdatahubModbusConfigService;
    @Autowired
    private ILabdatahubFanucConfigService labdatahubFanucConfigService;
    @Autowired
    private ILabdatahubMitsubishiCncConfigService labdatahubMitsubishiCncConfigService;
    @Autowired
    private ILabdatahubMitsubishiConfigService labdatahubMitsubishiConfigService;
    @Autowired
    private ILabdatahubBrotherConfigService labdatahubBrotherConfigService;
    @Autowired
    private ILabdatahubOmronFinsConfigService labdatahubOmronFinsConfigService;
    @Autowired
    private ILabdatahubS71200ConfigService labdatahubS71200ConfigService;
    @Autowired
    private ProtocolPointCopyUtil protocolPointCopyUtil;
    /**
     * 查询设备
     *
     * @param id 设备主键
     * @return 设备
     */
    @Override
    public LabdatahubDevice selectLabdatahubDeviceById(String id) {
        return labdatahubDeviceMapper.selectLabdatahubDeviceById(id);
    }

    /**
     * 查询设备列表
     *
     * @param labdatahubDevice 设备
     * @return 设备
     */
    @Override
    public List<LabdatahubDevice> selectLabdatahubDeviceList(LabdatahubDevice labdatahubDevice) {
        return labdatahubDeviceMapper.selectLabdatahubDeviceList(labdatahubDevice);
    }

    /**
     * 新增设备
     *
     * @param labdatahubDevice 设备
     * @return 结果
     */
    @Override
    public int insertLabdatahubDevice(LabdatahubDevice labdatahubDevice) {
        return labdatahubDeviceMapper.insertLabdatahubDevice(labdatahubDevice);
    }

    /**
     * 修改设备
     *
     * @param labdatahubDevice 设备
     * @return 结果
     */
    @Override
    public int updateLabdatahubDevice(LabdatahubDevice labdatahubDevice) {
        return labdatahubDeviceMapper.updateLabdatahubDevice(labdatahubDevice);
    }

    /**
     * 批量删除设备
     *
     * @param ids 需要删除的设备主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubDeviceByIds(String[] ids) {
        return labdatahubDeviceMapper.deleteLabdatahubDeviceByIds(ids);
    }

    /**
     * 删除设备信息
     *
     * @param id 设备主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubDeviceById(String id) {
        return labdatahubDeviceMapper.deleteLabdatahubDeviceById(id);
    }

    /**
     * 缓存设备属性
     */
    @Override
    public boolean cacheDeviceProperties(String deviceSn) {
        //查询设备全部属性
        List<LabdatahubProperties> properties = labdatahubPropertiesService.list(new LambdaQueryWrapper<LabdatahubProperties>()
                .eq(LabdatahubProperties::getBelongSn,deviceSn));
        List<PropertyNode> nodeList = PropertyConverter.buildPropertyTree(properties);
        PropertyToJson.PROPERTY_TREE.put(deviceSn,nodeList);
        return true;
    }

    @Override
    public boolean syncPropertyToDevice(String deviceSn) {
        LabdatahubDevice device = labdatahubDeviceMapper.selectOne(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getDeviceSn,deviceSn));
        List<LabdatahubProperties> properties = labdatahubPropertiesService.list(new LambdaQueryWrapper<LabdatahubProperties>()
                .eq(LabdatahubProperties::getBelongSn,device.getProductSn()));
        properties.forEach(property->{
            property.setId(null);
            property.setBelongType("1");
            property.setBelongSn(deviceSn);
            property.setFromType("0");
        });
        labdatahubPropertiesService.saveBatch(properties);
        cacheDeviceProperties(deviceSn);
        return true;
    }

    @Override
    public boolean syncProductToDevice(String deviceSn) {
        LabdatahubDevice device = labdatahubDeviceMapper.selectOne(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getDeviceSn,deviceSn));
        List<LabdatahubFunction> functionList = labdatahubFunctionService.list(new LambdaQueryWrapper<LabdatahubFunction>()
                .eq(LabdatahubFunction::getBelongSn,device.getProductSn()));
        functionList.forEach(property->{
            property.setId(null);
            property.setBelongType("1");
            property.setBelongSn(deviceSn);
            property.setCreateTime(new Date());
        });
        labdatahubFunctionService.saveBatch(functionList);
        CacheUtils.setDeviceFunctionCache(deviceSn,functionList);
        return true;
    }

    @Override
    public AjaxResult saveDevice(LabdatahubDevice labdatahubDevice) {
        if(StringUtils.isEmpty(labdatahubDevice.getProductId())){
            return AjaxResult.error("请选择产品");
        }
        //不可与产品sn重复
        long count = labdatahubProductService.count(new LambdaQueryWrapper<LabdatahubProduct>()
                .eq(LabdatahubProduct::getProductSn,labdatahubDevice.getDeviceSn()));
        if(count>0){
            return AjaxResult.warn("设备sn已经被产品使用，请更换设备sn");
        }
        LabdatahubProduct labdatahubProduct = labdatahubProductService.getById(labdatahubDevice.getProductId());
        labdatahubDevice.setProductName(labdatahubProduct.getProductName());
        labdatahubDevice.setProductSn(labdatahubProduct.getProductSn());
        labdatahubDevice.setCreateTime(new Date());
        labdatahubDevice.setCreateBy(SecurityUtils.getUsername());
        labdatahubDevice.setId(null);
        labdatahubDevice.setDeviceType(labdatahubProduct.getDeviceType());
        labdatahubDevice.setComponentId(labdatahubProduct.getComponentId());
        labdatahubDevice.setComponentName(labdatahubProduct.getComponentName());
        labdatahubDevice.setProtocolId(labdatahubProduct.getProtocolId());
        labdatahubDevice.setProtocolName(labdatahubProduct.getProtocolName());
        baseMapper.insert(labdatahubDevice);
        syncPropertyToDevice(labdatahubDevice.getDeviceSn());
        labdatahubWarnConfigService.syncWarnConfigToDevice(labdatahubProduct.getProductSn(),labdatahubDevice.getDeviceSn());
        labdatahubProductService.syncDeviceCount(labdatahubProduct.getProductSn());
        syncProductToDevice(labdatahubDevice.getDeviceSn());
        // 由AI修改：复制设备时连设备级点位一起复制（物模型设备自定义 + 各协议点位 + 设备级告警/功能），换产品即换组件（不改框架）
        if (StringUtils.isNotEmpty(labdatahubDevice.getCopyFromId())) {
            LabdatahubDevice sourceDevice = getById(labdatahubDevice.getCopyFromId());
            if (sourceDevice != null) {
                protocolPointCopyUtil.copyDevicePoints(sourceDevice.getDeviceSn(), labdatahubDevice.getDeviceSn());
                cacheDeviceProperties(labdatahubDevice.getDeviceSn());
            }
        }
        CacheUtils.updateDeviceCache(labdatahubDevice.getDeviceSn());
        return AjaxResult.success();
    }


    @Override
    public AjaxResult saveDeviceByProductSn(LabdatahubDevice labdatahubDevice) {
        if(StringUtils.isEmpty(labdatahubDevice.getProductSn())){
            return AjaxResult.error("请选择产品");
        }
        //不可与产品sn重复
        long count = labdatahubProductService.count(new LambdaQueryWrapper<LabdatahubProduct>()
                .eq(LabdatahubProduct::getProductSn,labdatahubDevice.getDeviceSn()));
        if(count>0){
            return AjaxResult.warn("设备sn已经被产品使用，请更换设备sn");
        }
        LabdatahubProduct labdatahubProduct = labdatahubProductService.getOne(new LambdaQueryWrapper<LabdatahubProduct>()
                .eq(LabdatahubProduct::getProductSn,labdatahubDevice.getProductSn()),false);
        labdatahubDevice.setProductName(labdatahubProduct.getProductName());
        labdatahubDevice.setProductSn(labdatahubProduct.getProductSn());
        labdatahubDevice.setCreateTime(new Date());
        labdatahubDevice.setCreateBy("自注册");
        labdatahubDevice.setId(null);
        labdatahubDevice.setDeviceType(labdatahubProduct.getDeviceType());
        labdatahubDevice.setComponentId(labdatahubProduct.getComponentId());
        labdatahubDevice.setComponentName(labdatahubProduct.getComponentName());
        labdatahubDevice.setProtocolId(labdatahubProduct.getProtocolId());
        labdatahubDevice.setProtocolName(labdatahubProduct.getProtocolName());
        baseMapper.insert(labdatahubDevice);
        syncPropertyToDevice(labdatahubDevice.getDeviceSn());
        labdatahubWarnConfigService.syncWarnConfigToDevice(labdatahubProduct.getProductSn(),labdatahubDevice.getDeviceSn());
        labdatahubProductService.syncDeviceCount(labdatahubProduct.getProductSn());
        syncProductToDevice(labdatahubDevice.getDeviceSn());
        CacheUtils.updateDeviceCache(labdatahubDevice.getDeviceSn());
        return AjaxResult.success();
    }

}
