package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubFunction;

/**
 * 设备指令下发Mapper接口
 *
 * @author ruoyi
 * @date 2025-10-24
 */
public interface LabdatahubFunctionMapper extends BaseMapper<LabdatahubFunction>
{
    /**
     * 查询设备指令下发
     *
     * @param id 设备指令下发主键
     * @return 设备指令下发
     */
    public LabdatahubFunction selectLabdatahubFunctionById(String id);

    /**
     * 查询设备指令下发列表
     *
     * @param labdatahubFunction 设备指令下发
     * @return 设备指令下发集合
     */
    public List<LabdatahubFunction> selectLabdatahubFunctionList(LabdatahubFunction labdatahubFunction);

    /**
     * 新增设备指令下发
     *
     * @param labdatahubFunction 设备指令下发
     * @return 结果
     */
    public int insertLabdatahubFunction(LabdatahubFunction labdatahubFunction);

    /**
     * 修改设备指令下发
     *
     * @param labdatahubFunction 设备指令下发
     * @return 结果
     */
    public int updateLabdatahubFunction(LabdatahubFunction labdatahubFunction);

    /**
     * 删除设备指令下发
     *
     * @param id 设备指令下发主键
     * @return 结果
     */
    public int deleteLabdatahubFunctionById(String id);

    /**
     * 批量删除设备指令下发
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubFunctionByIds(String[] ids);
}
