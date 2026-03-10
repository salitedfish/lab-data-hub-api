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
 * 设备联动告警对象 labdatahub_warn_linkage
 *
 * @author ruoyi
 * @date 2025-11-03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_warn_linkage")
public class LabdatahubWarnLinkage
{
private static final long serialVersionUID = 1L;

    /** id */
    private String id;
    /** 规则名称 */
    @Excel(name = "规则名称")
    private String name;
    /** 规则json */
    @Excel(name = "规则json")
    private String ruleJson;
    /** 告警消息模板 */
    @Excel(name = "告警消息模板")
    private String warnMessage;
    /** 告警等级 1-紧急 2-严重 3-警告 4-正常 */
    @Excel(name = "告警等级 1-紧急 2-严重 3-警告 4-正常")
    private String warnLevel;
    /** 是否启用 0-否 1-是 */
    @Excel(name = "是否启用 0-否 1-是")
    private String isEnable;
    /** 触发设备列表 */
    @Excel(name = "触发设备列表")
    private String triggerSnList;
    /** 触发设备名称 */
    @Excel(name = "触发设备名称")
    private String triggerNameList;
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
    private String remark;
}
