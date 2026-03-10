package com.labdatahub.business.service;

import java.util.List;
import com.labdatahub.business.domain.LabdatahubLinkageWarnRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 设备联动告警记录Service接口
 *
 * @author ruoyi
 * @date 2025-11-20
 */
public interface ILabdatahubLinkageWarnRecordService extends IService<LabdatahubLinkageWarnRecord>
{
    /**
     * 查询设备联动告警记录
     *
     * @param id 设备联动告警记录主键
     * @return 设备联动告警记录
     */
    public LabdatahubLinkageWarnRecord selectLabdatahubLinkageWarnRecordById(String id);

    /**
     * 查询设备联动告警记录列表
     *
     * @param labdatahubLinkageWarnRecord 设备联动告警记录
     * @return 设备联动告警记录集合
     */
    public List<LabdatahubLinkageWarnRecord> selectLabdatahubLinkageWarnRecordList(LabdatahubLinkageWarnRecord labdatahubLinkageWarnRecord);

    /**
     * 新增设备联动告警记录
     *
     * @param labdatahubLinkageWarnRecord 设备联动告警记录
     * @return 结果
     */
    public int insertLabdatahubLinkageWarnRecord(LabdatahubLinkageWarnRecord labdatahubLinkageWarnRecord);

    /**
     * 修改设备联动告警记录
     *
     * @param labdatahubLinkageWarnRecord 设备联动告警记录
     * @return 结果
     */
    public int updateLabdatahubLinkageWarnRecord(LabdatahubLinkageWarnRecord labdatahubLinkageWarnRecord);

    /**
     * 批量删除设备联动告警记录
     *
     * @param ids 需要删除的设备联动告警记录主键集合
     * @return 结果
     */
    public int deleteLabdatahubLinkageWarnRecordByIds(String[] ids);

    /**
     * 删除设备联动告警记录信息
     *
     * @param id 设备联动告警记录主键
     * @return 结果
     */
    public int deleteLabdatahubLinkageWarnRecordById(String id);
}
