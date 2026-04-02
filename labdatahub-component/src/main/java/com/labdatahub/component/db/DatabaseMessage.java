package com.labdatahub.component.db;

import lombok.Data;

/**
 * 
* @ClassName: DatabaseMessage  
* @Description: 消息体
* @author xwb  
* @date 2026年4月1日
 */
@Data
public class DatabaseMessage {
    private String deviceSn;        // 设备SN
    private String code;            // 指令编码
    private String sql;             // 待执行的 SQL
    private Object[] parameters;    // SQL 参数
    private Integer delayTime;      // 执行后延迟（毫秒）
}