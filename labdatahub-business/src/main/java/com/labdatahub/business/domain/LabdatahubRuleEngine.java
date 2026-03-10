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
 * 规则引擎配置对象 labdatahub_rule_engine
 *
 * @author labdatahub
 * @date 2025-10-20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_rule_engine")
public class LabdatahubRuleEngine
{
private static final long serialVersionUID = 1L;

    /** id */
    private String id;
    /** 引擎名称 */
    @Excel(name = "引擎名称")
    private String engineName;
    /** json配置 */
    @Excel(name = "json配置")
    private String configJson;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /** 备注 */
    @Excel(name = "备注")
    private String remark;
    /** 状态 0-停止 1-启用 */
    @Excel(name = "状态")
    private String isEnable;
}
