package com.labdatahub.component.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 
* @ClassName: DatabaseReader  
* @Description: 数据库读写执行器
* @author xwb  
* @date 2026年4月1日
 */
@Slf4j
public class DatabaseReader {

    // 重试配置
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * 查询返回 List<Map<String, Object>>
     */
    public static List<Map<String, Object>> queryForList(String componentId, String sql, Object... params) {
        return executeWithRetry(() -> {
            DataSource ds = DatabaseConnectionManager.getDataSource(componentId);
            if (ds == null) {
                throw new IllegalStateException("数据源不存在: " + componentId);
            }
            JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
            return jdbcTemplate.queryForList(sql, params);
        }, componentId, sql, params);
    }

    /**
     * 查询返回单个对象
     */
    public static <T> T queryForObject(String componentId, String sql, Class<T> requiredType, Object... params) {
        return executeWithRetry(() -> {
            DataSource ds = DatabaseConnectionManager.getDataSource(componentId);
            if (ds == null) {
                throw new IllegalStateException("数据源不存在: " + componentId);
            }
            JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
            return jdbcTemplate.queryForObject(sql, requiredType, params);
        }, componentId, sql, params);
    }

    /**
     * 执行更新（INSERT/UPDATE/DELETE）
     */
    public static int update(String componentId, String sql, Object... params) {
        return executeWithRetry(() -> {
            DataSource ds = DatabaseConnectionManager.getDataSource(componentId);
            if (ds == null) {
                throw new IllegalStateException("数据源不存在: " + componentId);
            }
            JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
            return jdbcTemplate.update(sql, params);
        }, componentId, sql, params);
    }

    /**
     * 原生 JDBC 方式（可处理大结果集）
     */
    public static List<Map<String, Object>> queryNative(String componentId, String sql, Object... params) throws Exception {
        return executeWithRetry(() -> {
            DataSource ds = DatabaseConnectionManager.getDataSource(componentId);
            if (ds == null) {
                throw new IllegalStateException("数据源不存在: " + componentId);
            }
            List<Map<String, Object>> results = new ArrayList<>();
            try (Connection conn = DataSourceUtils.getConnection(ds);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnName(i), rs.getObject(i));
                        }
                        results.add(row);
                    }
                }
            }
            return results;
        }, componentId, sql, params);
    }

    // ------------------ 重试机制 ------------------
    @FunctionalInterface
    private interface DbOperation<T> {
        T execute() throws Exception;
    }

    private static <T> T executeWithRetry(DbOperation<T> op, String componentId, String sql, Object... params) {
        int attempt = 0;
        while (true) {
            try {
                return op.execute();
            } catch (Exception e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    log.error("[DB读写] 数据源 {} 执行失败，已达最大重试次数, sql: {}", componentId, sql, e);
                    throw new RuntimeException("数据库操作失败", e);
                }
                log.warn("[DB读写] 数据源 {} 执行失败（第{}次），{}ms后重试, sql: {}, error: {}",
                        componentId, attempt, RETRY_DELAY_MS, sql, e.getMessage());
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }
    }
    
    /**
     * 获取指定数据源中当前数据库/模式下的所有用户表名称
     * @param componentId 数据源标识
     * @return 表名列表（不含 schema 前缀）
     * @throws SQLException 数据库连接或元数据获取异常
     */
    public static List<String> getAllTables(String componentId) throws SQLException {
        DataSource ds = DatabaseConnectionManager.getDataSource(componentId);
        if (ds == null) {
            throw new SQLException("数据源不存在: " + componentId);
        }
        List<String> tables = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String dbType = getDbType(componentId); // 从配置获取数据库类型
            String catalog = null;
            String schema = null;

            if ("mysql".equalsIgnoreCase(dbType)) {
                // MySQL 使用 catalog 对应数据库名
                catalog = conn.getCatalog();
                // 如果 catalog 为 null，尝试从连接 URL 解析，此处直接使用 conn.getCatalog()
            } else if ("postgresql".equalsIgnoreCase(dbType)) {
                // PostgreSQL 使用 schema
                schema = conn.getSchema(); // JDBC 4.1+
                if (schema == null) {
                    // 兼容旧驱动，手动查询当前 schema
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT current_schema()")) {
                        if (rs.next()) {
                            schema = rs.getString(1);
                        }
                    }
                }
            } else if ("oracle".equalsIgnoreCase(dbType)) {
                // Oracle 使用 schema（即用户名）
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT USER FROM DUAL")) {
                    if (rs.next()) {
                        schema = rs.getString(1);
                    }
                }
                if (schema != null) {
                    schema = schema.toUpperCase(); // Oracle 元数据中 schema 为大写
                }
            }

            // 获取表元数据
            try (ResultSet rs = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    tables.add(tableName);
                }
            }
        }
        return tables;
    }
    
    /**
     * 获取指定表的所有字段信息（字段名、类型、注释）
     * 支持 MySQL、PostgreSQL、Oracle
     * @param componentId 数据源标识
     * @return 字段信息列表，每个元素包含 fieldName, fieldType, fieldComment
     * @throws SQLException 数据库操作异常
     */
    public static List<Map<String, String>> getTableColumns(String componentId) throws SQLException {
        DataSource ds = DatabaseConnectionManager.getDataSource(componentId);
        if (ds == null) {
            throw new SQLException("数据源不存在: " + componentId);
        }
        List<Map<String, String>> columns = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String dbType = getDbType(componentId); // 从配置中获取数据库类型
            String catalog = conn.getCatalog();
            String schema = null;
            String actualTableName = getDbTableName(componentId);

            // 解析表名中的 schema
            if (actualTableName.contains(".")) {
                String[] parts = actualTableName.split("\\.");
                if (parts.length == 2) {
                    schema = parts[0];
                    actualTableName = parts[1];
                }
            }

            // 未显式指定 schema，尝试从连接获取当前 schema
            if (schema == null) {
                try {
                    schema = conn.getSchema(); // JDBC 4.1+
                } catch (Exception ignored) {}
            }

            // Oracle 特殊处理：获取当前用户作为 schema，并将表名和 schema 转为大写
            if ("oracle".equalsIgnoreCase(dbType)) {
                if (schema == null) {
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT USER FROM DUAL")) {
                        if (rs.next()) {
                            schema = rs.getString(1);
                        }
                    }
                }
                if (actualTableName != null && !actualTableName.equals(actualTableName.toUpperCase())) {
                    actualTableName = actualTableName.toUpperCase();
                }
                if (schema != null && !schema.equals(schema.toUpperCase())) {
                    schema = schema.toUpperCase();
                }
            }

            // 获取列元数据
            try (ResultSet rs = metaData.getColumns(catalog, schema, actualTableName, "%")) {
                while (rs.next()) {
                    Map<String, String> colInfo = new HashMap<>();
                    colInfo.put("code", rs.getString("COLUMN_NAME"));
                    colInfo.put("fieldType", rs.getString("TYPE_NAME"));
                    String remarks = rs.getString("REMARKS");
                    colInfo.put("fieldComment", remarks != null ? remarks : "");
                    int columnSize = rs.getInt("COLUMN_SIZE");
                    colInfo.put("fieldLength", String.valueOf(columnSize));
                    columns.add(colInfo);
                }
            }
        }
        return columns;
    }

    /**
     * 获取数据源对应的数据库类型（mysql / postgresql / oracle）
     */
    private static String getDbType(String componentId) {
        DatabaseConfig config = DatabaseConnectionManager.getConfig(componentId);
        return config != null ? config.getDbType() : null;
    }
    
    /**
     * 获取数据源对应的表名（mysql / postgresql / oracle）
     */
    private static String getDbTableName(String componentId) {
        DatabaseConfig config = DatabaseConnectionManager.getConfig(componentId);
        return config != null ? config.getTableName() : null;
    }
    
    /**
     * 获取指定表的最新一条记录（按指定排序字段降序）
     * @param componentId 数据源标识
     * @param tableName 表名（仅允许字母、数字、下划线，防注入）
     * @param orderByColumn 排序字段（如 "id", "create_time"），必须为表中真实字段
     * @return 最新一条记录的 Map（字段名 -> 值），若无记录返回空 Map
     * @throws Exception 数据库操作异常
     */
    public static Map<String, Object> getLatestRecord(String componentId, String tableName, String orderByColumn) throws Exception {
        // 简单防注入：表名和字段名只允许字母、数字、下划线，且不能以数字开头
        if (!tableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }
        if (!orderByColumn.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("Invalid column name: " + orderByColumn);
        }

        String dbType = getDbType(componentId);
        String sql = buildLatestRecordSql(dbType, tableName, orderByColumn);
        
        List<Map<String, Object>> resultList = queryForList(componentId, sql);
        return resultList.isEmpty() ? new HashMap<>() : resultList.get(0);
    }
    
    /**
     * 获取指定表的最新一条记录（自动尝试常见排序字段：create_time, update_time, id）
     * @param componentId 数据源标识
     * @return 最新一条记录的 Map，若无记录返回空 Map
     * @throws Exception 数据库操作异常
     */
    public static Map<String, Object> getLatestRecord(String componentId) throws Exception {
    	String tableName = getDbTableName(componentId);
        // 先获取表的所有列名，用于判断哪些排序字段存在
        List<Map<String, String>> columns = getTableColumns(componentId);
        Set<String> columnNames = columns.stream()
                .map(col -> col.get("code"))
                .collect(Collectors.toSet());
        
        // 按优先级尝试排序字段
        List<String> candidateOrderColumns = Arrays.asList("create_time", "update_time", "id");
        for (String col : candidateOrderColumns) {
            if (columnNames.contains(col)) {
                return getLatestRecord(componentId, tableName, col);
            }
        }
        // 如果都没有，使用第一个字段作为排序字段（不推荐，但兜底）
        if (!columnNames.isEmpty()) {
            String firstColumn = columnNames.iterator().next();
            return getLatestRecord(componentId, tableName, firstColumn);
        }
        throw new IllegalArgumentException("表 " + tableName + " 无任何字段，无法获取最新记录");
    }

    /**
     * 根据数据库类型构建“取最新一条”的 SQL
     */
    private static String buildLatestRecordSql(String dbType, String tableName, String orderByColumn) {
        String baseSql = "SELECT * FROM " + tableName + " ORDER BY " + orderByColumn + " DESC";
        if ("mysql".equalsIgnoreCase(dbType) || "postgresql".equalsIgnoreCase(dbType)) {
            return baseSql + " LIMIT 1";
        } else if ("oracle".equalsIgnoreCase(dbType)) {
            // Oracle 12c 以上支持 FETCH FIRST 1 ROW ONLY，更推荐；若版本较低可用 ROWNUM
            return "SELECT * FROM (" + baseSql + ") WHERE ROWNUM <= 1";
        } else {
            throw new IllegalArgumentException("Unsupported database type for latest record: " + dbType);
        }
    }
}