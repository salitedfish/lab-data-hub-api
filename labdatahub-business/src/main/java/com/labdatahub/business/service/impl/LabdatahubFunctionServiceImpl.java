package com.labdatahub.business.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.utils.DateUtils;
import com.labdatahub.common.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubFunctionMapper;
import com.labdatahub.business.domain.LabdatahubFunction;
import com.labdatahub.business.service.ILabdatahubFunctionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 设备指令下发Service业务层处理
 *
 * @author ruoyi
 * @date 2025-10-24
 */
@Service
public class LabdatahubFunctionServiceImpl extends ServiceImpl<LabdatahubFunctionMapper, LabdatahubFunction> implements ILabdatahubFunctionService
{
    @Autowired
    private LabdatahubFunctionMapper labdatahubFunctionMapper;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    /**
     * 查询设备指令下发
     *
     * @param id 设备指令下发主键
     * @return 设备指令下发
     */
    @Override
    public LabdatahubFunction selectLabdatahubFunctionById(String id)
    {
        return labdatahubFunctionMapper.selectLabdatahubFunctionById(id);
    }

    /**
     * 查询设备指令下发列表
     *
     * @param labdatahubFunction 设备指令下发
     * @return 设备指令下发
     */
    @Override
    public List<LabdatahubFunction> selectLabdatahubFunctionList(LabdatahubFunction labdatahubFunction)
    {
        return labdatahubFunctionMapper.selectLabdatahubFunctionList(labdatahubFunction);
    }

    /**
     * 新增设备指令下发
     *
     * @param labdatahubFunction 设备指令下发
     * @return 结果
     */
    @Override
    public int insertLabdatahubFunction(LabdatahubFunction labdatahubFunction)
    {
        labdatahubFunction.setCreateTime(DateUtils.getNowDate());
        return labdatahubFunctionMapper.insertLabdatahubFunction(labdatahubFunction);
    }

    /**
     * 修改设备指令下发
     *
     * @param labdatahubFunction 设备指令下发
     * @return 结果
     */
    @Override
    public int updateLabdatahubFunction(LabdatahubFunction labdatahubFunction)
    {
        return labdatahubFunctionMapper.updateLabdatahubFunction(labdatahubFunction);
    }

    /**
     * 批量删除设备指令下发
     *
     * @param ids 需要删除的设备指令下发主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubFunctionByIds(String[] ids)
    {
        return labdatahubFunctionMapper.deleteLabdatahubFunctionByIds(ids);
    }

    /**
     * 删除设备指令下发信息
     *
     * @param id 设备指令下发主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubFunctionById(String id)
    {
        return labdatahubFunctionMapper.deleteLabdatahubFunctionById(id);
    }

    @Override
    public void syscFunction(String productSn, String deviceSn) {
        if(StringUtils.isEmpty(productSn)){
            return;
        }
        List<LabdatahubFunction> functionList = labdatahubFunctionMapper.selectList(new LambdaQueryWrapper<LabdatahubFunction>()
                .eq(LabdatahubFunction::getBelongSn,productSn));
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        if(deviceList.size()==0){
            return;
        }
        labdatahubFunctionMapper.delete(new LambdaUpdateWrapper<LabdatahubFunction>()
                .in(LabdatahubFunction::getBelongSn, deviceList.stream().map(LabdatahubDevice::getDeviceSn).collect(Collectors.toList())));
        List<LabdatahubFunction> list = new ArrayList<>();
        deviceList.forEach(device->{
            functionList.forEach(function ->{
                LabdatahubFunction func = new LabdatahubFunction();
                BeanUtils.copyProperties(function,func);
                func.setBelongSn(device.getDeviceSn());
                func.setBelongType("1");
                func.setId(null);
                list.add(func);
            });
        });
        list.forEach(function -> {
            labdatahubFunctionMapper.insert(function);
        });
    }
}
