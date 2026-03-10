package com.labdatahub.business.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.labdatahub.business.task.TaskBuilder;
import com.labdatahub.business.task.TaskExecutor;
import com.labdatahub.business.task.entity.FlowConfig;
import com.labdatahub.business.task.entity.TaskGroup;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.exception.CommonWarnException;
import com.labdatahub.common.utils.DateUtils;
import com.labdatahub.common.utils.StringUtils;
import org.aspectj.weaver.loadtime.Aj;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubScheduledTaskMapper;
import com.labdatahub.business.domain.LabdatahubScheduledTask;
import com.labdatahub.business.service.ILabdatahubScheduledTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import static com.labdatahub.business.task.TaskExecutor.TASK_GROUP;

/**
 * 定时引擎配置Service业务层处理
 *
 * @author ruoyi
 * @date 2025-12-02
 */
@Service
public class LabdatahubScheduledTaskServiceImpl extends ServiceImpl<LabdatahubScheduledTaskMapper, LabdatahubScheduledTask> implements ILabdatahubScheduledTaskService
{
    @Autowired
    private LabdatahubScheduledTaskMapper labdatahubScheduledTaskMapper;
    @Autowired
    private TaskExecutor taskExecutor;
    /**
     * 查询定时引擎配置
     *
     * @param id 定时引擎配置主键
     * @return 定时引擎配置
     */
    @Override
    public LabdatahubScheduledTask selectLabdatahubScheduledTaskById(String id)
    {
        return labdatahubScheduledTaskMapper.selectLabdatahubScheduledTaskById(id);
    }

    /**
     * 查询定时引擎配置列表
     *
     * @param labdatahubScheduledTask 定时引擎配置
     * @return 定时引擎配置
     */
    @Override
    public List<LabdatahubScheduledTask> selectLabdatahubScheduledTaskList(LabdatahubScheduledTask labdatahubScheduledTask)
    {
        return labdatahubScheduledTaskMapper.selectLabdatahubScheduledTaskList(labdatahubScheduledTask);
    }

    /**
     * 新增定时引擎配置
     *
     * @param labdatahubScheduledTask 定时引擎配置
     * @return 结果
     */
    @Override
    public int insertLabdatahubScheduledTask(LabdatahubScheduledTask labdatahubScheduledTask)
    {
        labdatahubScheduledTask.setCreateTime(DateUtils.getNowDate());
        return labdatahubScheduledTaskMapper.insertLabdatahubScheduledTask(labdatahubScheduledTask);
    }

    /**
     * 修改定时引擎配置
     *
     * @param labdatahubScheduledTask 定时引擎配置
     * @return 结果
     */
    @Override
    public int updateLabdatahubScheduledTask(LabdatahubScheduledTask labdatahubScheduledTask)
    {
        return labdatahubScheduledTaskMapper.updateLabdatahubScheduledTask(labdatahubScheduledTask);
    }

    /**
     * 批量删除定时引擎配置
     *
     * @param ids 需要删除的定时引擎配置主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubScheduledTaskByIds(String[] ids)
    {
        return labdatahubScheduledTaskMapper.deleteLabdatahubScheduledTaskByIds(ids);
    }

    /**
     * 删除定时引擎配置信息
     *
     * @param id 定时引擎配置主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubScheduledTaskById(String id)
    {
        return labdatahubScheduledTaskMapper.deleteLabdatahubScheduledTaskById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult configTask(LabdatahubScheduledTask labdatahubScheduledTask) throws Exception {
        if(StringUtils.isEmpty(labdatahubScheduledTask.getId())||StringUtils.isEmpty(labdatahubScheduledTask.getRuleJson())){
            return AjaxResult.warn("请配置节点");
        }
        labdatahubScheduledTaskMapper.updateById(labdatahubScheduledTask);
        LabdatahubScheduledTask task = labdatahubScheduledTaskMapper.selectById(labdatahubScheduledTask.getId());
        control(task.getId(),task.getIsEnable());
        return AjaxResult.success();
    }

    @Override
    public AjaxResult control(String id, String isEnable) throws Exception {
        LabdatahubScheduledTask labdatahubScheduledTask = labdatahubScheduledTaskMapper.selectById(id);
        //无论开关都先清除
        taskExecutor.stopTaskGroup(id);
        if("1".equals(isEnable)){
            JSONObject ruleJson = JSONObject.parseObject(labdatahubScheduledTask.getRuleJson());
            FlowConfig config = ruleJson.toJavaObject(FlowConfig.class);
            List<TaskGroup> list = TaskBuilder.buildTaskGroups(config);
            list.forEach(task->{
                taskExecutor.executeTaskGroup(labdatahubScheduledTask.getId(),task);
            });
        }
        labdatahubScheduledTask.setIsEnable(isEnable);
        labdatahubScheduledTaskMapper.updateById(labdatahubScheduledTask);
        return AjaxResult.success();
    }
}
