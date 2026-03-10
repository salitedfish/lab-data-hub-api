package com.labdatahub.business.service.impl;

import java.util.List;
        import com.labdatahub.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubWarnRecordMapper;
import com.labdatahub.business.domain.LabdatahubWarnRecord;
import com.labdatahub.business.service.ILabdatahubWarnRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 告警记录Service业务层处理
 *
 * @author labdatahub
 * @date 2025-10-05
 */
@Service
public class LabdatahubWarnRecordServiceImpl extends ServiceImpl<LabdatahubWarnRecordMapper, LabdatahubWarnRecord> implements ILabdatahubWarnRecordService
{
    @Autowired
    private LabdatahubWarnRecordMapper labdatahubWarnRecordMapper;

    /**
     * 查询告警记录
     *
     * @param id 告警记录主键
     * @return 告警记录
     */
    @Override
    public LabdatahubWarnRecord selectLabdatahubWarnRecordById(String id)
    {
        return labdatahubWarnRecordMapper.selectLabdatahubWarnRecordById(id);
    }

    /**
     * 查询告警记录列表
     *
     * @param labdatahubWarnRecord 告警记录
     * @return 告警记录
     */
    @Override
    public List<LabdatahubWarnRecord> selectLabdatahubWarnRecordList(LabdatahubWarnRecord labdatahubWarnRecord)
    {
        return labdatahubWarnRecordMapper.selectLabdatahubWarnRecordList(labdatahubWarnRecord);
    }

    /**
     * 新增告警记录
     *
     * @param labdatahubWarnRecord 告警记录
     * @return 结果
     */
    @Override
    public int insertLabdatahubWarnRecord(LabdatahubWarnRecord labdatahubWarnRecord)
    {
        labdatahubWarnRecord.setCreateTime(DateUtils.getNowDate());
        return labdatahubWarnRecordMapper.insertLabdatahubWarnRecord(labdatahubWarnRecord);
    }

    /**
     * 修改告警记录
     *
     * @param labdatahubWarnRecord 告警记录
     * @return 结果
     */
    @Override
    public int updateLabdatahubWarnRecord(LabdatahubWarnRecord labdatahubWarnRecord)
    {
        return labdatahubWarnRecordMapper.updateLabdatahubWarnRecord(labdatahubWarnRecord);
    }

    /**
     * 批量删除告警记录
     *
     * @param ids 需要删除的告警记录主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubWarnRecordByIds(String[] ids)
    {
        return labdatahubWarnRecordMapper.deleteLabdatahubWarnRecordByIds(ids);
    }

    /**
     * 删除告警记录信息
     *
     * @param id 告警记录主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubWarnRecordById(String id)
    {
        return labdatahubWarnRecordMapper.deleteLabdatahubWarnRecordById(id);
    }
}
