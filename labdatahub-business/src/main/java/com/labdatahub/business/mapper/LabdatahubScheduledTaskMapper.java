package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubScheduledTask;

/**
 * 定时引擎配置Mapper接口
 *
 * @author ruoyi
 * @date 2025-12-02
 */
public interface LabdatahubScheduledTaskMapper extends BaseMapper<LabdatahubScheduledTask>
{
    /**
     * 查询定时引擎配置
     *
     * @param id 定时引擎配置主键
     * @return 定时引擎配置
     */
    public LabdatahubScheduledTask selectLabdatahubScheduledTaskById(String id);

    /**
     * 查询定时引擎配置列表
     *
     * @param labdatahubScheduledTask 定时引擎配置
     * @return 定时引擎配置集合
     */
    public List<LabdatahubScheduledTask> selectLabdatahubScheduledTaskList(LabdatahubScheduledTask labdatahubScheduledTask);

    /**
     * 新增定时引擎配置
     *
     * @param labdatahubScheduledTask 定时引擎配置
     * @return 结果
     */
    public int insertLabdatahubScheduledTask(LabdatahubScheduledTask labdatahubScheduledTask);

    /**
     * 修改定时引擎配置
     *
     * @param labdatahubScheduledTask 定时引擎配置
     * @return 结果
     */
    public int updateLabdatahubScheduledTask(LabdatahubScheduledTask labdatahubScheduledTask);

    /**
     * 删除定时引擎配置
     *
     * @param id 定时引擎配置主键
     * @return 结果
     */
    public int deleteLabdatahubScheduledTaskById(String id);

    /**
     * 批量删除定时引擎配置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubScheduledTaskByIds(String[] ids);
}
