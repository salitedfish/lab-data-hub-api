package com.labdatahub.business.service;

import java.util.List;
import com.labdatahub.business.domain.LabdatahubLinkageActionRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 设备联动告警动作执行记录Service接口
 *
 * @author ruoyi
 * @date 2025-11-20
 */
public interface ILabdatahubLinkageActionRecordService extends IService<LabdatahubLinkageActionRecord>
{
    /**
     * 查询设备联动告警动作执行记录
     *
     * @param id 设备联动告警动作执行记录主键
     * @return 设备联动告警动作执行记录
     */
    public LabdatahubLinkageActionRecord selectLabdatahubLinkageActionRecordById(String id);

    /**
     * 查询设备联动告警动作执行记录列表
     *
     * @param labdatahubLinkageActionRecord 设备联动告警动作执行记录
     * @return 设备联动告警动作执行记录集合
     */
    public List<LabdatahubLinkageActionRecord> selectLabdatahubLinkageActionRecordList(LabdatahubLinkageActionRecord labdatahubLinkageActionRecord);

    /**
     * 新增设备联动告警动作执行记录
     *
     * @param labdatahubLinkageActionRecord 设备联动告警动作执行记录
     * @return 结果
     */
    public int insertLabdatahubLinkageActionRecord(LabdatahubLinkageActionRecord labdatahubLinkageActionRecord);

    /**
     * 修改设备联动告警动作执行记录
     *
     * @param labdatahubLinkageActionRecord 设备联动告警动作执行记录
     * @return 结果
     */
    public int updateLabdatahubLinkageActionRecord(LabdatahubLinkageActionRecord labdatahubLinkageActionRecord);

    /**
     * 批量删除设备联动告警动作执行记录
     *
     * @param ids 需要删除的设备联动告警动作执行记录主键集合
     * @return 结果
     */
    public int deleteLabdatahubLinkageActionRecordByIds(String[] ids);

    /**
     * 删除设备联动告警动作执行记录信息
     *
     * @param id 设备联动告警动作执行记录主键
     * @return 结果
     */
    public int deleteLabdatahubLinkageActionRecordById(String id);
}
