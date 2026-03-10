package com.labdatahub.business.service;

import java.util.List;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.labdatahub.common.core.domain.AjaxResult;

/**
 * 设备Service接口
 *
 * @author labdatahub
 * @date 2025-09-18
 */
public interface ILabdatahubDeviceService extends IService<LabdatahubDevice>
{
    /**
     * 查询设备
     *
     * @param id 设备主键
     * @return 设备
     */
    public LabdatahubDevice selectLabdatahubDeviceById(String id);

    /**
     * 查询设备列表
     *
     * @param labdatahubDevice 设备
     * @return 设备集合
     */
    public List<LabdatahubDevice> selectLabdatahubDeviceList(LabdatahubDevice labdatahubDevice);

    /**
     * 新增设备
     *
     * @param labdatahubDevice 设备
     * @return 结果
     */
    public int insertLabdatahubDevice(LabdatahubDevice labdatahubDevice);

    /**
     * 修改设备
     *
     * @param labdatahubDevice 设备
     * @return 结果
     */
    public int updateLabdatahubDevice(LabdatahubDevice labdatahubDevice);

    /**
     * 批量删除设备
     *
     * @param ids 需要删除的设备主键集合
     * @return 结果
     */
    public int deleteLabdatahubDeviceByIds(String[] ids);

    /**
     * 删除设备信息
     *
     * @param id 设备主键
     * @return 结果
     */
    public int deleteLabdatahubDeviceById(String id);

    /**
     * 缓存设备属性
     */
    public boolean cacheDeviceProperties(String deviceSn);

    /**
     * 同步产品物模型属性到设备
     */
    public boolean syncPropertyToDevice(String deviceSn);

    /**
     * 同步指令到设备
     * @return
     */
    public boolean syncProductToDevice(String deviceSn);

    /**
     * 新增设备
     */
    public AjaxResult saveDevice(LabdatahubDevice device);

    public AjaxResult saveDeviceByProductSn(LabdatahubDevice device);
}
