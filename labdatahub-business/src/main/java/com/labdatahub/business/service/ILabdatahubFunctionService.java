package com.labdatahub.business.service;

import java.util.List;
import com.labdatahub.business.domain.LabdatahubFunction;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 设备指令下发Service接口
 *
 * @author ruoyi
 * @date 2025-10-24
 */
public interface ILabdatahubFunctionService extends IService<LabdatahubFunction>
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
     * 批量删除设备指令下发
     *
     * @param ids 需要删除的设备指令下发主键集合
     * @return 结果
     */
    public int deleteLabdatahubFunctionByIds(String[] ids);

    /**
     * 删除设备指令下发信息
     *
     * @param id 设备指令下发主键
     * @return 结果
     */
    public int deleteLabdatahubFunctionById(String id);

    /**
     * 同步指令
     */
    public void syscFunction(String productSn,String deviceSn);
}
