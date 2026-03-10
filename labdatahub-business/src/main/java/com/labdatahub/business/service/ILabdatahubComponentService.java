package com.labdatahub.business.service;

import java.io.IOException;
import java.util.List;
import com.labdatahub.business.domain.LabdatahubComponent;
import com.baomidou.mybatisplus.extension.service.IService;
import com.labdatahub.common.exception.CommonWarnException;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * 网络组件Service接口
 *
 * @author labdatahub
 * @date 2025-09-18
 */
public interface ILabdatahubComponentService extends IService<LabdatahubComponent>
{
    /**
     * 查询网络组件
     *
     * @param id 网络组件主键
     * @return 网络组件
     */
    public LabdatahubComponent selectLabdatahubComponentById(String id);

    /**
     * 查询网络组件列表
     *
     * @param labdatahubComponent 网络组件
     * @return 网络组件集合
     */
    public List<LabdatahubComponent> selectLabdatahubComponentList(LabdatahubComponent labdatahubComponent);

    /**
     * 新增网络组件
     *
     * @param labdatahubComponent 网络组件
     * @return 结果
     */
    public int insertLabdatahubComponent(LabdatahubComponent labdatahubComponent);

    /**
     * 修改网络组件
     *
     * @param labdatahubComponent 网络组件
     * @return 结果
     */
    public int updateLabdatahubComponent(LabdatahubComponent labdatahubComponent);

    /**
     * 批量删除网络组件
     *
     * @param ids 需要删除的网络组件主键集合
     * @return 结果
     */
    public int deleteLabdatahubComponentByIds(String[] ids);

    /**
     * 删除网络组件信息
     *
     * @param id 网络组件主键
     * @return 结果
     */
    public int deleteLabdatahubComponentById(String id);

    /**
     * 启用网络组件
     */
    public boolean openComponent(String id) throws Exception;

    /**
     * 关闭网络组件
     */
    public boolean closeComponent(String id) throws IOException, MqttException;
}
