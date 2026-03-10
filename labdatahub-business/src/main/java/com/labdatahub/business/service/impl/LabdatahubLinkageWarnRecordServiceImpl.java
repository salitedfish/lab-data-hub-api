package com.labdatahub.business.service.impl;

import java.util.List;
        import com.labdatahub.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubLinkageWarnRecordMapper;
import com.labdatahub.business.domain.LabdatahubLinkageWarnRecord;
import com.labdatahub.business.service.ILabdatahubLinkageWarnRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 设备联动告警记录Service业务层处理
 *
 * @author ruoyi
 * @date 2025-11-20
 */
@Service
public class LabdatahubLinkageWarnRecordServiceImpl extends ServiceImpl<LabdatahubLinkageWarnRecordMapper, LabdatahubLinkageWarnRecord> implements ILabdatahubLinkageWarnRecordService
{
    @Autowired
    private LabdatahubLinkageWarnRecordMapper labdatahubLinkageWarnRecordMapper;

    /**
     * 查询设备联动告警记录
     *
     * @param id 设备联动告警记录主键
     * @return 设备联动告警记录
     */
    @Override
    public LabdatahubLinkageWarnRecord selectLabdatahubLinkageWarnRecordById(String id)
    {
        return labdatahubLinkageWarnRecordMapper.selectLabdatahubLinkageWarnRecordById(id);
    }

    /**
     * 查询设备联动告警记录列表
     *
     * @param labdatahubLinkageWarnRecord 设备联动告警记录
     * @return 设备联动告警记录
     */
    @Override
    public List<LabdatahubLinkageWarnRecord> selectLabdatahubLinkageWarnRecordList(LabdatahubLinkageWarnRecord labdatahubLinkageWarnRecord)
    {
        return labdatahubLinkageWarnRecordMapper.selectLabdatahubLinkageWarnRecordList(labdatahubLinkageWarnRecord);
    }

    /**
     * 新增设备联动告警记录
     *
     * @param labdatahubLinkageWarnRecord 设备联动告警记录
     * @return 结果
     */
    @Override
    public int insertLabdatahubLinkageWarnRecord(LabdatahubLinkageWarnRecord labdatahubLinkageWarnRecord)
    {
        labdatahubLinkageWarnRecord.setCreateTime(DateUtils.getNowDate());
        return labdatahubLinkageWarnRecordMapper.insertLabdatahubLinkageWarnRecord(labdatahubLinkageWarnRecord);
    }

    /**
     * 修改设备联动告警记录
     *
     * @param labdatahubLinkageWarnRecord 设备联动告警记录
     * @return 结果
     */
    @Override
    public int updateLabdatahubLinkageWarnRecord(LabdatahubLinkageWarnRecord labdatahubLinkageWarnRecord)
    {
        return labdatahubLinkageWarnRecordMapper.updateLabdatahubLinkageWarnRecord(labdatahubLinkageWarnRecord);
    }

    /**
     * 批量删除设备联动告警记录
     *
     * @param ids 需要删除的设备联动告警记录主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubLinkageWarnRecordByIds(String[] ids)
    {
        return labdatahubLinkageWarnRecordMapper.deleteLabdatahubLinkageWarnRecordByIds(ids);
    }

    /**
     * 删除设备联动告警记录信息
     *
     * @param id 设备联动告警记录主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubLinkageWarnRecordById(String id)
    {
        return labdatahubLinkageWarnRecordMapper.deleteLabdatahubLinkageWarnRecordById(id);
    }
}
