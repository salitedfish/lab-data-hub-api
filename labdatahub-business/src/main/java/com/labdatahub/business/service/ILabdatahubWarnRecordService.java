package com.labdatahub.business.service;

import java.util.List;
import com.labdatahub.business.domain.LabdatahubWarnRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 告警记录Service接口
 *
 * @author labdatahub
 * @date 2025-10-05
 */
public interface ILabdatahubWarnRecordService extends IService<LabdatahubWarnRecord>
{
    /**
     * 查询告警记录
     *
     * @param id 告警记录主键
     * @return 告警记录
     */
    public LabdatahubWarnRecord selectLabdatahubWarnRecordById(String id);

    /**
     * 查询告警记录列表
     *
     * @param labdatahubWarnRecord 告警记录
     * @return 告警记录集合
     */
    public List<LabdatahubWarnRecord> selectLabdatahubWarnRecordList(LabdatahubWarnRecord labdatahubWarnRecord);

    /**
     * 新增告警记录
     *
     * @param labdatahubWarnRecord 告警记录
     * @return 结果
     */
    public int insertLabdatahubWarnRecord(LabdatahubWarnRecord labdatahubWarnRecord);

    /**
     * 修改告警记录
     *
     * @param labdatahubWarnRecord 告警记录
     * @return 结果
     */
    public int updateLabdatahubWarnRecord(LabdatahubWarnRecord labdatahubWarnRecord);

    /**
     * 批量删除告警记录
     *
     * @param ids 需要删除的告警记录主键集合
     * @return 结果
     */
    public int deleteLabdatahubWarnRecordByIds(String[] ids);

    /**
     * 删除告警记录信息
     *
     * @param id 告警记录主键
     * @return 结果
     */
    public int deleteLabdatahubWarnRecordById(String id);
}
