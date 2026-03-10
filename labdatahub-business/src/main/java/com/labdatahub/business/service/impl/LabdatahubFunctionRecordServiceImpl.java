package com.labdatahub.business.service.impl;

import java.util.List;
        import com.labdatahub.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubFunctionRecordMapper;
import com.labdatahub.business.domain.LabdatahubFunctionRecord;
import com.labdatahub.business.service.ILabdatahubFunctionRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 指令下发记录Service业务层处理
 *
 * @author ruoyi
 * @date 2025-10-29
 */
@Service
public class LabdatahubFunctionRecordServiceImpl extends ServiceImpl<LabdatahubFunctionRecordMapper, LabdatahubFunctionRecord> implements ILabdatahubFunctionRecordService
{
    @Autowired
    private LabdatahubFunctionRecordMapper LabdatahubFunctionRecordMapper;

    /**
     * 查询指令下发记录
     *
     * @param id 指令下发记录主键
     * @return 指令下发记录
     */
    @Override
    public LabdatahubFunctionRecord selectLabdatahubFunctionRecordById(String id)
    {
        return LabdatahubFunctionRecordMapper.selectLabdatahubFunctionRecordById(id);
    }

    /**
     * 查询指令下发记录列表
     *
     * @param labdatahubFunctionRecord 指令下发记录
     * @return 指令下发记录
     */
    @Override
    public List<LabdatahubFunctionRecord> selectLabdatahubFunctionRecordList(LabdatahubFunctionRecord labdatahubFunctionRecord)
    {
        return LabdatahubFunctionRecordMapper.selectLabdatahubFunctionRecordList(labdatahubFunctionRecord);
    }

    /**
     * 新增指令下发记录
     *
     * @param labdatahubFunctionRecord 指令下发记录
     * @return 结果
     */
    @Override
    public int insertLabdatahubFunctionRecord(LabdatahubFunctionRecord labdatahubFunctionRecord)
    {
        labdatahubFunctionRecord.setCreateTime(DateUtils.getNowDate());
        return LabdatahubFunctionRecordMapper.insertLabdatahubFunctionRecord(labdatahubFunctionRecord);
    }

    /**
     * 修改指令下发记录
     *
     * @param labdatahubFunctionRecord 指令下发记录
     * @return 结果
     */
    @Override
    public int updateLabdatahubFunctionRecord(LabdatahubFunctionRecord labdatahubFunctionRecord)
    {
        return LabdatahubFunctionRecordMapper.updateLabdatahubFunctionRecord(labdatahubFunctionRecord);
    }

    /**
     * 批量删除指令下发记录
     *
     * @param ids 需要删除的指令下发记录主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubFunctionRecordByIds(String[] ids)
    {
        return LabdatahubFunctionRecordMapper.deleteLabdatahubFunctionRecordByIds(ids);
    }

    /**
     * 删除指令下发记录信息
     *
     * @param id 指令下发记录主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubFunctionRecordById(String id)
    {
        return LabdatahubFunctionRecordMapper.deleteLabdatahubFunctionRecordById(id);
    }
}
