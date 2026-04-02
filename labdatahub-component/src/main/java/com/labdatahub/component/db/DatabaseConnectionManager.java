package com.labdatahub.component.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import com.labdatahub.common.utils.spring.SpringUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

/**
 * 
* @ClassName: DatabaseConnectionManager  
* @Description: 数据库连接管理器
* @author xwb  
* @date 2026年4月1日
 */
@Slf4j
public class DatabaseConnectionManager {

    // 数据源池：key = componentId（例如 componentId）
    private static final Map<String, HikariDataSource> connections = new ConcurrentHashMap<>();
    // 配置缓存（用于重建）
    private static final Map<String, DatabaseConfig> configMap = new ConcurrentHashMap<>();

    // 健康检查调度器
    private static final ScheduledExecutorService healthCheckScheduler = Executors.newScheduledThreadPool(1,
            r -> {
                Thread t = new Thread(r, "db-connection-health-check");
                t.setDaemon(true);
                return t;
            });

    // 健康检查间隔（秒）
    private static final int HEALTH_CHECK_INTERVAL = 10;
    // 重连配置
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000;
    private static final long MAX_RETRY_DELAY_MS = 30000;

    static {
        healthCheckScheduler.scheduleAtFixedRate(
                DatabaseConnectionManager::checkAllDataSources,
                0,
                HEALTH_CHECK_INTERVAL,
                TimeUnit.SECONDS
        );
        log.info("数据库连接健康检查任务已启动，间隔={}秒", HEALTH_CHECK_INTERVAL);
    }

    /**
     * 注册数据源（若已存在则先关闭旧连接）
     */
    public static boolean addConnection(String componentId, DatabaseConfig config) {
        if (componentId == null || config == null) {
            log.warn("[DB] componentId或config为空");
            return false;
        }
        try {
        	closeConnection(componentId);
            configMap.put(componentId, config);
            HikariDataSource dataSource = createDataSource(config);
            connections.put(componentId, dataSource);
            log.info("[DB] 数据源注册成功: {} -> {}", componentId, config.getJdbcUrl());
            // 启动消费线程
            DatabaseLoopConsumer.startConsume(componentId, SpringUtils.getBean(DatabaseMessageConsumeService.class));
            return true;
        } catch (Exception e) {
            log.error("[DB] 数据源注册失败: {}", componentId, e);
            closeConnection(componentId);
            configMap.remove(componentId);
            return false;
        }
    }

    /**
     * 获取数据源
     */
    public static DataSource getDataSource(String componentId) {
        return connections.get(componentId);
    }

    /**
     * 获取连接（建议使用 DataSource 获取，此方法仅作兼容）
     */
    public static Connection getConnection(String componentId) throws SQLException {
        HikariDataSource ds = connections.get(componentId);
        if (ds == null) {
            throw new SQLException("数据源不存在: " + componentId);
        }
        return ds.getConnection();
    }

    /**
     * 关闭并移除指定数据源
     */
    public static void closeConnection(String componentId) {
        HikariDataSource ds = connections.remove(componentId);
        if (ds != null && !ds.isClosed()) {
            ds.close();
            log.info("[DB] 数据源已关闭: {}", componentId);
        }
        configMap.remove(componentId);
        DatabaseMessageScheduler.removeMessageQueue(componentId);
        DatabaseLoopConsumer.stopConsume(componentId);
    }

    /**
     * 关闭所有数据源
     */
    public static void closeAllDataSources() {
        log.info("[DB] 开始关闭所有数据源，当前数量={}", connections.size());
        for (String id : connections.keySet()) {
        	closeConnection(id);
        }
        connections.clear();
        configMap.clear();
        healthCheckScheduler.shutdown();
        try {
            if (!healthCheckScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("[DB] 健康检查调度器未能在5秒内终止，强制关闭");
                healthCheckScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("[DB] 等待健康检查调度器终止时被中断", e);
            healthCheckScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("[DB] 所有数据源已关闭，健康检查调度器已停止");
    }

    // ------------------ 健康检查与重连 ------------------
    private static void checkAllDataSources() {
        for (String componentId : configMap.keySet()) {
            checkAndReconnect(componentId);
        }
    }

    private static void checkAndReconnect(String componentId) {
        DatabaseConfig config = configMap.get(componentId);
        if (config == null) return;

        HikariDataSource ds = connections.get(componentId);
        boolean isValid = (ds != null && !ds.isClosed() && isConnectionValid(ds));
        if (!isValid) {
            log.warn("[DB重连] 数据源 {} 连接失效，启动重连流程", componentId);
            reconnectWithRetry(componentId, config);
        }
    }

    private static boolean isConnectionValid(HikariDataSource ds) {
        try (Connection conn = ds.getConnection()) {
            return conn.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }

    private static void reconnectWithRetry(String componentId, DatabaseConfig config) {
        int attempt = 0;
        long delayMs = INITIAL_RETRY_DELAY_MS;
        while (attempt < MAX_RECONNECT_ATTEMPTS) {
            attempt++;
            log.info("[DB重连] 数据源 {} 第{}次尝试重连", componentId, attempt);
            if (doReconnect(componentId, config)) {
                log.info("[DB重连] 数据源 {} 重连成功", componentId);
                return;
            }
            if (attempt < MAX_RECONNECT_ATTEMPTS) {
                log.warn("[DB重连] 数据源 {} 第{}次重连失败，{}ms后重试", componentId, attempt, delayMs);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("[DB重连] 数据源 {} 重连等待被中断", componentId);
                    break;
                }
                delayMs = Math.min(delayMs * 2, MAX_RETRY_DELAY_MS);
            }
        }
        log.error("[DB重连] 数据源 {} 重连失败，已达到最大重试次数 {}", componentId, MAX_RECONNECT_ATTEMPTS);
        closeConnection(componentId);
    }

    private static boolean doReconnect(String componentId, DatabaseConfig config) {
        try {
        	closeConnection(componentId);
            HikariDataSource newDs = createDataSource(config);
            connections.put(componentId, newDs);
            configMap.put(componentId, config);
            log.info("[DB重连] 数据源 {} 重建成功", componentId);
            return true;
        } catch (Exception e) {
            log.error("[DB重连] 数据源 {} 重连尝试失败: {}", componentId, e.getMessage());
            return false;
        }
    }

    private static HikariDataSource createDataSource(DatabaseConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName(config.getDriverClassName());
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        hikariConfig.setConnectionTimeout(config.getTimeout());
        hikariConfig.setIdleTimeout(config.getIdleTimeout());
        hikariConfig.setMaxLifetime(config.getMaxLifetime());
        hikariConfig.setPoolName("HikariPool-" + config.getDbType() + "-" + System.identityHashCode(config));
        // 验证连接有效性
        hikariConfig.setConnectionTestQuery(getTestQuery(config.getDbType()));
        return new HikariDataSource(hikariConfig);
    }

    private static String getTestQuery(String dbType) {
        switch (dbType.toLowerCase()) {
            case "mysql":
                return "SELECT 1";
            case "postgresql":
                return "SELECT 1";
            case "oracle":
                return "SELECT 1 FROM DUAL";
            default:
                return "SELECT 1";
        }
    }
    
    public static DatabaseConfig getConfig(String componentId) {
        return configMap.get(componentId);
    }
}