package com.labdatahub.component.modbus_tcp;

import com.labdatahub.common.utils.spring.SpringUtils;
import net.wimpi.modbus.net.TCPMasterConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 连接管理器，维护多个设备的连接（新增自动检查+定时重连）
 */
public class ModbusConnectionManager {
    // 存储串口服务器连接：key为componentId（线程安全）
    public static Map<String, TCPMasterConnection> connections = new ConcurrentHashMap<>();
    // 存储每个componentId对应的连接配置（用于重连）
    private static final Map<String, ModbusTcpConfig> configMap = new ConcurrentHashMap<>();
    // 连接健康检查调度器（全局单例，定时检查连接状态）
    private static final ScheduledExecutorService healthCheckScheduler = Executors.newScheduledThreadPool(
            1,
            r -> {
                Thread t = new Thread(r, "modbus-connection-health-check");
                t.setDaemon(true); // 守护线程，应用退出时自动销毁
                return t;
            }
    );

    // 静态初始化：启动全局连接健康检查（每10秒检查一次，可根据业务调整）
    static {
        // 首次延迟0秒执行，之后每10秒执行一次健康检查
        healthCheckScheduler.scheduleAtFixedRate(
                ModbusConnectionManager::checkAllConnections,
                0,
                10,
                TimeUnit.SECONDS
        );
    }

    /**
     * 创建连接（兼容原有逻辑，新增配置存储）
     * @param componentId 组件唯一标识
     * @param config 连接配置
     * @return 首次连接是否成功
     */
    public static boolean addConnection(String componentId, ModbusTcpConfig config) {
        try {
            // 1. 校验参数
            if (componentId == null || config == null || config.getIpAddr() == null) {
                return false;
            }

            // 2. 存储配置（用于后续重连）
            configMap.put(componentId, config);

            // 3. 执行连接逻辑
            InetAddress address = InetAddress.getByName(config.getIpAddr());
            TCPMasterConnection connection = new TCPMasterConnection(address);
            connection.setPort(config.getPort());
            connection.setTimeout(config.getTimeout());

            // 先关闭旧连接（避免资源泄漏）
            TCPMasterConnection oldConn = connections.get(componentId);
            if (oldConn != null && oldConn.isConnected()) {
                oldConn.close();
            }

            // 建立新连接
            if (!connection.isConnected()) {
                connection.connect();
            }

            // 4. 更新连接映射
            connections.put(componentId, connection);
            System.out.printf("[Modbus连接] componentId=%s 首次连接成功（%s:%d）%n",
                    componentId, config.getIpAddr(), config.getPort());
            ModbusLoopConsumer.startConsume(componentId, SpringUtils.getBean(ModbusMessageConsumeService.class));
            return true;
        } catch (Exception e) {
            System.err.printf("[Modbus连接] componentId=%s 首次连接失败：%s%n", componentId, e.getMessage());
            // 连接失败时移除无效配置/连接，避免空转
            connections.remove(componentId);
            configMap.remove(componentId);
            return false;
        }
    }

    /**
     * 关闭单个设备连接（兼容原有逻辑，新增配置清理）
     */
    public static void closeConnection(String componentId) {
        // 1. 关闭连接
        TCPMasterConnection connection = connections.remove(componentId);
        if (connection != null) {
            try {
                if (connection.isConnected()) {
                    connection.close();
                }
            } catch (Exception e) {
                System.err.printf("[Modbus连接] componentId=%s 关闭失败：%s%n", componentId, e.getMessage());
            }
        }

        // 2. 清理配置（停止该组件的重连检查）
        configMap.remove(componentId);

        // 3. 清理消息队列和消费线程（原有逻辑保留）
        ModbusMessageScheduler.removeMessageQueue(componentId);
        ModbusLoopConsumer.stopConsume(componentId);

        System.out.printf("[Modbus连接] componentId=%s 连接已关闭，配置已清理%n", componentId);
    }

    /**
     * 关闭所有连接（兼容原有逻辑，新增调度器停止+配置清理）
     */
    public static void closeAllConnections() {
        // 1. 关闭所有连接
        connections.forEach((id, conn) -> {
            try {
                if (conn.isConnected()) {
                    conn.close();
                }
            } catch (Exception e) {
                System.err.printf("[Modbus连接] componentId=%s 关闭失败：%s%n", id, e.getMessage());
            }
        });

        // 2. 清理所有映射
        connections.clear();
        configMap.clear();

        // 3. 停止健康检查调度器（避免资源泄漏）
        healthCheckScheduler.shutdown();
        try {
            if (!healthCheckScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                healthCheckScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            healthCheckScheduler.shutdownNow();
        }

        System.out.println("[Modbus连接] 所有连接已关闭，健康检查调度器已停止");
    }

    // ========== 核心新增：连接健康检查与重连逻辑 ==========

    /**
     * 检查所有连接状态，失效则自动重连
     */
    private static void checkAllConnections() {
        // 遍历所有已配置的componentId（避免遗漏待重连的组件）
        for (String componentId : configMap.keySet()) {
            checkAndReconnect(componentId);
        }
    }

    /**
     * 检查单个componentId的连接状态，失效则重连
     * @param componentId 组件唯一标识
     */
    private static void checkAndReconnect(String componentId) {
        ModbusTcpConfig config = configMap.get(componentId);
        if (config == null) {
            return;
        }

        TCPMasterConnection connection = connections.get(componentId);
        // 校验连接是否有效（结合底层Socket状态，避免本地标记误判）
        boolean isConnectionValid = isConnectionValid(connection);

        if (!isConnectionValid) {
            System.out.printf("[Modbus重连] componentId=%s 连接失效，开始重连（%s:%d）%n",
                    componentId, config.getIpAddr(), config.getPort());
            // 执行重连逻辑
            reconnect(componentId, config);
        }
    }

    /**
     * 精准校验连接是否有效（核心：避免本地isConnected()误判）
     * @param connection Modbus TCP连接
     * @return true=有效，false=失效
     */
    private static boolean isConnectionValid(TCPMasterConnection connection) {
        if (connection == null||!connection.isConnected()) {
            return false;
        }else {
            return true;
        }
    }

    /**
     * 执行重连逻辑
     * @param componentId 组件唯一标识
     * @param config 连接配置
     * @return 重连是否成功
     */
    private static boolean reconnect(String componentId, ModbusTcpConfig config) {
        try {
            // 1. 创建新连接
            InetAddress address = InetAddress.getByName(config.getIpAddr());
            TCPMasterConnection newConn = new TCPMasterConnection(address);
            newConn.setPort(config.getPort());
            newConn.setTimeout(config.getTimeout());

            // 2. 关闭旧连接（释放资源）
            TCPMasterConnection oldConn = connections.get(componentId);
            if (oldConn != null) {
                try {
                    oldConn.close();
                } catch (Exception e) {
                    // 旧连接关闭失败不影响新连接创建
                    System.err.printf("[Modbus重连] componentId=%s 旧连接关闭失败：%s%n", componentId, e.getMessage());
                }
            }

            // 3. 建立新连接
            newConn.connect();
            // 4. 更新连接映射
            connections.put(componentId, newConn);
            System.out.printf("[Modbus重连] componentId=%s 重连成功（%s:%d）%n",
                    componentId, config.getIpAddr(), config.getPort());
            return true;
        } catch (Exception e) {
            System.err.printf("[Modbus重连] componentId=%s 重连失败：%s%n", componentId, e.getMessage());
            // 重连失败时移除无效连接（避免下次检查重复处理）
            connections.remove(componentId);
            return false;
        }
    }
}