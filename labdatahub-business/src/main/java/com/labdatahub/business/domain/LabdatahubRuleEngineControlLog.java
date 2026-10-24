package com.labdatahub.business.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规则引擎启停日志对象 labdatahub_rule_engine_control_log
 *
 * @author labdatahub
 * @date 2026-10-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_rule_engine_control_log")
public class LabdatahubRuleEngineControlLog
{
    private static final long serialVersionUID = 1L;

    /** id */
    private String id;
    /** 规则引擎id */
    private String engineId;
    /** 引擎名称 */
    private String engineName;
    /** 操作 start-启用 stop-停用 */
    private String action;
    /** 操作人（无登录态时为 system） */
    private String operName;
    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operTime;
    /** 结果 0-成功 1-失败 */
    private String status;
    /** 说明（失败原因） */
    private String message;
}
