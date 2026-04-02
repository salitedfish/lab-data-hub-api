package com.labdatahub.component.db;

import lombok.Data;

/**
 * 
* @ClassName: DatabaseReadConfig  
* @Description: 定时读取配置
* @author xwb  
* @date 2026年4月1日
 */
@Data
public class DatabaseReadConfig {
    private String deviceSn;        // 设备SN（业务标识）
    private String code;            // 指令编码
    private String sql;             // 查询 SQL（支持 ? 占位符）
    private Object[] parameters;    // SQL 参数（可选）
    private Integer intervalTime; // 调度间隔（秒）
    private Integer delayTime;      // 读取完成后暂停时间（毫秒）
}