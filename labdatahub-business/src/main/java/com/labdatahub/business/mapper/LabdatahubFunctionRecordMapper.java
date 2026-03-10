package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubFunctionRecord;

/**
 * 指令下发记录Mapper接口
 *
 * @author ruoyi
 * @date 2025-10-29
 */
public interface LabdatahubFunctionRecordMapper extends BaseMapper<LabdatahubFunctionRecord>
{
    /**
     * 查询指令下发记录
     *
     * @param id 指令下发记录主键
     * @return 指令下发记录
     */
    public LabdatahubFunctionRecord selectLabdatahubFunctionRecordById(String id);

    /**
     * 查询指令下发记录列表
     *
     * @param labdatahubFunctionRecord 指令下发记录
     * @return 指令下发记录集合
     */
    public List<LabdatahubFunctionRecord> selectLabdatahubFunctionRecordList(LabdatahubFunctionRecord labdatahubFunctionRecord);

    /**
     * 新增指令下发记录
     *
     * @param labdatahubFunctionRecord 指令下发记录
     * @return 结果
     */
    public int insertLabdatahubFunctionRecord(LabdatahubFunctionRecord labdatahubFunctionRecord);

    /**
     * 修改指令下发记录
     *
     * @param labdatahubFunctionRecord 指令下发记录
     * @return 结果
     */
    public int updateLabdatahubFunctionRecord(LabdatahubFunctionRecord labdatahubFunctionRecord);

    /**
     * 删除指令下发记录
     *
     * @param id 指令下发记录主键
     * @return 结果
     */
    public int deleteLabdatahubFunctionRecordById(String id);

    /**
     * 批量删除指令下发记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubFunctionRecordByIds(String[] ids);
}
