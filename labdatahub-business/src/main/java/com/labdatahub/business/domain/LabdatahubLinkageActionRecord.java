package com.labdatahub.business.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.labdatahub.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 设备联动告警动作执行记录对象 labdatahub_linkage_action_record
 *
 * @author ruoyi
 * @date 2025-11-20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_linkage_action_record")
public class LabdatahubLinkageActionRecord
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
    /** 执行动作设备SN */
    @Excel(name = "执行动作设备SN")
    private String executeSn;
    /** 执行动作设备名称 */
    @Excel(name = "执行动作设备名称")
    private String executeName;
    /** 动作CODE */
    @Excel(name = "动作CODE")
    private String functionCode;
    /** 动作名称 */
    @Excel(name = "动作名称")
    private String functionName;
    /** 参数 */
    @Excel(name = "参数")
    private String functionParam;
    /** 创建时间 */
    private Date createTime;
}
