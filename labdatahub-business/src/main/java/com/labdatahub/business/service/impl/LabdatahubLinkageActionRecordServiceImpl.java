package com.labdatahub.business.service.impl;

import java.util.List;
        import com.labdatahub.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubLinkageActionRecordMapper;
import com.labdatahub.business.domain.LabdatahubLinkageActionRecord;
import com.labdatahub.business.service.ILabdatahubLinkageActionRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 设备联动告警动作执行记录Service业务层处理
 *
 * @author ruoyi
 * @date 2025-11-20
 */
@Service
public class LabdatahubLinkageActionRecordServiceImpl extends ServiceImpl<LabdatahubLinkageActionRecordMapper, LabdatahubLinkageActionRecord> implements ILabdatahubLinkageActionRecordService
{
    @Autowired
    private LabdatahubLinkageActionRecordMapper labdatahubLinkageActionRecordMapper;

    /**
     * 查询设备联动告警动作执行记录
     *
     * @param id 设备联动告警动作执行记录主键
     * @return 设备联动告警动作执行记录
     */
    @Override
    public LabdatahubLinkageActionRecord selectLabdatahubLinkageActionRecordById(String id)
    {
        return labdatahubLinkageActionRecordMapper.selectLabdatahubLinkageActionRecordById(id);
    }

    /**
     * 查询设备联动告警动作执行记录列表
     *
     * @param labdatahubLinkageActionRecord 设备联动告警动作执行记录
     * @return 设备联动告警动作执行记录
     */
    @Override
    public List<LabdatahubLinkageActionRecord> selectLabdatahubLinkageActionRecordList(LabdatahubLinkageActionRecord labdatahubLinkageActionRecord)
    {
        return labdatahubLinkageActionRecordMapper.selectLabdatahubLinkageActionRecordList(labdatahubLinkageActionRecord);
    }

    /**
     * 新增设备联动告警动作执行记录
     *
     * @param labdatahubLinkageActionRecord 设备联动告警动作执行记录
     * @return 结果
     */
    @Override
    public int insertLabdatahubLinkageActionRecord(LabdatahubLinkageActionRecord labdatahubLinkageActionRecord)
    {
        labdatahubLinkageActionRecord.setCreateTime(DateUtils.getNowDate());
        return labdatahubLinkageActionRecordMapper.insertLabdatahubLinkageActionRecord(labdatahubLinkageActionRecord);
    }

    /**
     * 修改设备联动告警动作执行记录
     *
     * @param labdatahubLinkageActionRecord 设备联动告警动作执行记录
     * @return 结果
     */
    @Override
    public int updateLabdatahubLinkageActionRecord(LabdatahubLinkageActionRecord labdatahubLinkageActionRecord)
    {
        return labdatahubLinkageActionRecordMapper.updateLabdatahubLinkageActionRecord(labdatahubLinkageActionRecord);
    }

    /**
     * 批量删除设备联动告警动作执行记录
     *
     * @param ids 需要删除的设备联动告警动作执行记录主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubLinkageActionRecordByIds(String[] ids)
    {
        return labdatahubLinkageActionRecordMapper.deleteLabdatahubLinkageActionRecordByIds(ids);
    }

    /**
     * 删除设备联动告警动作执行记录信息
     *
     * @param id 设备联动告警动作执行记录主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubLinkageActionRecordById(String id)
    {
        return labdatahubLinkageActionRecordMapper.deleteLabdatahubLinkageActionRecordById(id);
    }
}
