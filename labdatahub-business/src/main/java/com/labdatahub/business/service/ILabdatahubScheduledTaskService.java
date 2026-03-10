package com.labdatahub.business.service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.labdatahub.business.domain.LabdatahubScheduledTask;
import com.baomidou.mybatisplus.extension.service.IService;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.exception.CommonWarnException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 定时引擎配置Service接口
 *
 * @author ruoyi
 * @date 2025-12-02
 */
public interface ILabdatahubScheduledTaskService extends IService<LabdatahubScheduledTask>
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
     * 批量删除定时引擎配置
     *
     * @param ids 需要删除的定时引擎配置主键集合
     * @return 结果
     */
    public int deleteLabdatahubScheduledTaskByIds(String[] ids);

    /**
     * 删除定时引擎配置信息
     *
     * @param id 定时引擎配置主键
     * @return 结果
     */
    public int deleteLabdatahubScheduledTaskById(String id);

    public AjaxResult configTask(LabdatahubScheduledTask labdatahubScheduledTask) throws Exception;

    /**
     * 开关定时任务
     */
    public AjaxResult control(String id,String isEnable) throws Exception;
}
