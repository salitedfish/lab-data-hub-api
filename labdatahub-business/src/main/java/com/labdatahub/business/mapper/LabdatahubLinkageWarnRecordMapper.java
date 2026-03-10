package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubLinkageWarnRecord;

/**
 * 设备联动告警记录Mapper接口
 *
 * @author ruoyi
 * @date 2025-11-20
 */
public interface LabdatahubLinkageWarnRecordMapper extends BaseMapper<LabdatahubLinkageWarnRecord>
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
     * 删除设备联动告警记录
     *
     * @param id 设备联动告警记录主键
     * @return 结果
     */
    public int deleteLabdatahubLinkageWarnRecordById(String id);

    /**
     * 批量删除设备联动告警记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubLinkageWarnRecordByIds(String[] ids);
}
