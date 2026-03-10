package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubLinkageActionRecord;

/**
 * 设备联动告警动作执行记录Mapper接口
 *
 * @author ruoyi
 * @date 2025-11-20
 */
public interface LabdatahubLinkageActionRecordMapper extends BaseMapper<LabdatahubLinkageActionRecord>
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
     * 删除设备联动告警动作执行记录
     *
     * @param id 设备联动告警动作执行记录主键
     * @return 结果
     */
    public int deleteLabdatahubLinkageActionRecordById(String id);

    /**
     * 批量删除设备联动告警动作执行记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubLinkageActionRecordByIds(String[] ids);
}
