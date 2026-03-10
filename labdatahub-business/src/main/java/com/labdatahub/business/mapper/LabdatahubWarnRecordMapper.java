package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubWarnRecord;

/**
 * 告警记录Mapper接口
 *
 * @author labdatahub
 * @date 2025-10-05
 */
public interface LabdatahubWarnRecordMapper extends BaseMapper<LabdatahubWarnRecord>
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
     * 删除告警记录
     *
     * @param id 告警记录主键
     * @return 结果
     */
    public int deleteLabdatahubWarnRecordById(String id);

    /**
     * 批量删除告警记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubWarnRecordByIds(String[] ids);
}
