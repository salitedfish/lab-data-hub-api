package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubWarnLinkage;

/**
 * 设备联动告警Mapper接口
 *
 * @author ruoyi
 * @date 2025-11-03
 */
public interface LabdatahubWarnLinkageMapper extends BaseMapper<LabdatahubWarnLinkage>
{
    /**
     * 查询设备联动告警
     *
     * @param id 设备联动告警主键
     * @return 设备联动告警
     */
    public LabdatahubWarnLinkage selectLabdatahubWarnLinkageById(String id);

    /**
     * 查询设备联动告警列表
     *
     * @param labdatahubWarnLinkage 设备联动告警
     * @return 设备联动告警集合
     */
    public List<LabdatahubWarnLinkage> selectLabdatahubWarnLinkageList(LabdatahubWarnLinkage labdatahubWarnLinkage);

    /**
     * 新增设备联动告警
     *
     * @param labdatahubWarnLinkage 设备联动告警
     * @return 结果
     */
    public int insertLabdatahubWarnLinkage(LabdatahubWarnLinkage labdatahubWarnLinkage);

    /**
     * 修改设备联动告警
     *
     * @param labdatahubWarnLinkage 设备联动告警
     * @return 结果
     */
    public int updateLabdatahubWarnLinkage(LabdatahubWarnLinkage labdatahubWarnLinkage);

    /**
     * 删除设备联动告警
     *
     * @param id 设备联动告警主键
     * @return 结果
     */
    public int deleteLabdatahubWarnLinkageById(String id);

    /**
     * 批量删除设备联动告警
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubWarnLinkageByIds(String[] ids);
}
