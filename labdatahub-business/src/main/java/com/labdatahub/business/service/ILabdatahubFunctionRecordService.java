package com.labdatahub.business.service;

import java.util.List;
import com.labdatahub.business.domain.LabdatahubFunctionRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 指令下发记录Service接口
 *
 * @author ruoyi
 * @date 2025-10-29
 */
public interface ILabdatahubFunctionRecordService extends IService<LabdatahubFunctionRecord>
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
     * @param LabdatahubFunctionRecord 指令下发记录
     * @return 指令下发记录集合
     */
    public List<LabdatahubFunctionRecord> selectLabdatahubFunctionRecordList(LabdatahubFunctionRecord LabdatahubFunctionRecord);

    /**
     * 新增指令下发记录
     *
     * @param LabdatahubFunctionRecord 指令下发记录
     * @return 结果
     */
    public int insertLabdatahubFunctionRecord(LabdatahubFunctionRecord LabdatahubFunctionRecord);

    /**
     * 修改指令下发记录
     *
     * @param LabdatahubFunctionRecord 指令下发记录
     * @return 结果
     */
    public int updateLabdatahubFunctionRecord(LabdatahubFunctionRecord LabdatahubFunctionRecord);

    /**
     * 批量删除指令下发记录
     *
     * @param ids 需要删除的指令下发记录主键集合
     * @return 结果
     */
    public int deleteLabdatahubFunctionRecordByIds(String[] ids);

    /**
     * 删除指令下发记录信息
     *
     * @param id 指令下发记录主键
     * @return 结果
     */
    public int deleteLabdatahubFunctionRecordById(String id);
}
