package com.labdatahub.business.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.labdatahub.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 定时引擎配置对象 labdatahub_scheduled_task
 *
 * @author ruoyi
 * @date 2025-12-02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_scheduled_task")
public class LabdatahubScheduledTask
{
private static final long serialVersionUID = 1L;

    /** id */
    private String id;
    /** 配置名称 */
    @Excel(name = "配置名称")
    private String name;
    /** 规则json */
    @Excel(name = "规则json")
    private String ruleJson;
    /** 是否启用 0-否 1-是 */
    @Excel(name = "是否启用 0-否 1-是")
    private String isEnable;
    /** 执行动作设备列表 */
    @Excel(name = "执行动作设备列表")
    private String executeSnList;
    /** 执行动作设备名称 */
    @Excel(name = "执行动作设备名称")
    private String executeNameList;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /** 备注 */
    @Excel(name = "备注")
    private String remark;
}
