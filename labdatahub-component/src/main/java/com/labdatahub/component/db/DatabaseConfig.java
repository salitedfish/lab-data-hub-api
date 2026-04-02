package com.labdatahub.component.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
* @ClassName: DatabaseConfig  
* @Description: 数据库配置类
* @author xwb  
* @date 2026年4月1日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseConfig {
	/**
	 * 数据库类型// mysql / postgresql / oracle
	 */
    private String dbType; 
    /**
     * ip地址
     */
    private String ipAddr;
    /**
     * 端口
     */
    private Integer port;
    /**
     * 数据库名称
     */
    private String databaseName;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    private Integer maxPoolSize = 10;
    /**
     * 连接超时
     */
    private Integer timeout = 30000; // ms
    private Integer idleTimeout = 600000;      // ms
    private Integer maxLifetime = 1800000;     // ms
    /**
     * 表名
     */
    private String tableName;

    public String getJdbcUrl() {
        switch (dbType.toLowerCase()) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%d/%s?serverTimezone=Asia/Shanghai&useSSL=false&useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&autoReconnect=true&autoReconnectForPools=true&rewriteBatchedStatements=true",
                		ipAddr, port, databaseName);
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s",
                		ipAddr, port, databaseName);
            case "oracle":
                return String.format("jdbc:oracle:thin:@%s:%d:%s",
                		ipAddr, port, databaseName);
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }

    public String getDriverClassName() {
        switch (dbType.toLowerCase()) {
            case "mysql":
                return "com.mysql.cj.jdbc.Driver";
            case "postgresql":
                return "org.postgresql.Driver";
            case "oracle":
                return "oracle.jdbc.OracleDriver";
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }
}