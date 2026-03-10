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
 * 设备联动告警记录对象 labdatahub_linkage_warn_record
 *
 * @author ruoyi
 * @date 2025-11-20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_linkage_warn_record")
public class LabdatahubLinkageWarnRecord
{
private static final long serialVersionUID = 1L;

    /** id */
    private String id;
    /** 告警配置id */
    @Excel(name = "告警配置id")
    private String configId;
    /** 告警配置名称 */
    @Excel(name = "告警配置名称")
    private String configName;
    /** 告警内容 */
    @Excel(name = "告警内容")
    private String warnMessage;
    /** 告警时相关设备数据 */
    @Excel(name = "告警时相关设备数据")
    private String warnData;
    /** 告警设备SN列表 */
    @Excel(name = "告警设备SN列表")
    private String triggerSnList;
    /** 告警设备名称列表 */
    @Excel(name = "告警设备名称列表")
    private String triggerNameList;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /** 告警等级 1-紧急 2-严重 3-一般 4-警告 5-正常 */
    @Excel(name = "告警等级 1-紧急 2-严重 3-一般 4-警告 5-正常")
    private String warnLevel;
    /** 0-未处理 1-已处理 */
    @Excel(name = "0-未处理 1-已处理")
    private String status;
}
