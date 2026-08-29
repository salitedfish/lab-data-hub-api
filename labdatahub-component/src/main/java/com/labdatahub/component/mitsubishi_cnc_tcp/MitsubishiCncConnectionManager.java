//由AI修改
package com.labdatahub.component.mitsubishi_cnc_tcp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.event.ComponentOnlineNotifier;

import lombok.extern.slf4j.Slf4j;

/**
 * 三菱 CNC TCP（MELDAS MOCHA）连接管理器，维护多个设备的连接
 * MOCHA 协议 TCP 683，建连后需发 mochaCancelModal2 取消模态（取消系统弹窗，避免干扰后续点位读取）；
 * 新增自动检查+定时重连。待真机验证：握手帧结构（操作名长度/是否带 GetDataBody）以树根 IL 为准，若机床拒绝需按抓包校准。
 */
@Slf4j
public class MitsubishiCncConnectionManager {
    // 存储设备连接：key为componentId（线程安全）
    public static Map<String, Socket> connections = new ConcurrentHashMap<>();
    // 存储每个componentId对应的连接配置（用于重连）
    public static final Map<String, MitsubishiCncTcpConfig> configMap = new ConcurrentHashMap<>();
    // 连接健康检查调度器（全局单例，定时检查连接状态）
    private static final ScheduledExecutorService healthCheckScheduler = Executors.newScheduledThreadPool(
            1,
            r -> {
                Thread t = new Thread(r, "mitsubishi-cnc-connection-health-check");
                t.setDaemon(true); // 守护线程，应用退出时自动销毁
                return t;
            }
    );

    private static final int HEALTH_CHECK_INTERVAL = 10;
    // 静态初始化：启动全局连接健康检查（每10秒检查一次）
    static {
        healthCheckScheduler.scheduleAtFixedRate(
                MitsubishiCncConnectionManager::checkAllConnections,
                0,
                HEALTH_CHECK_INTERVAL,
                TimeUnit.SECONDS
        );
        log.info("MITSUBISHI-CNC 连接健康检查任务已启动，检查间隔={}秒", HEALTH_CHECK_INTERVAL);
    }

    /**
     * 创建连接（MOCHA 建连后发 mochaCancelModal2 取消模态）
     * @param componentId 组件唯一标识
     * @param config 连接配置
     * @return 首次连接是否成功
     */
    public static boolean addConnection(String componentId, MitsubishiCncTcpConfig config) {
        Socket socket = null;
        try {
            // 1. 校验参数
            if (componentId == null || config == null || config.getIpAddr() == null) {
                return false;
            }
            configMap.put(componentId, config);
            // 先关闭旧连接（避免资源泄漏）
            Socket oldConn = connections.get(componentId);
            if (oldConn != null && !oldConn.isClosed()) {
                oldConn.close();
            }
            // 2. 执行连接逻辑（带连接超时，避免对不可达地址长时间阻塞）
            InetAddress address = InetAddress.getByName(config.getIpAddr());
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, config.getPort()), config.getTimeout());
            socket.setSoTimeout(config.getTimeout());
            socket.setTcpNoDelay(true); // 禁用Nagle算法，降低延迟
            // MOCHA 握手：取消模态（失败不阻断建连，真机验证后按需调整）
            try {
                MitsubishiCncDataReader.sendCancelModal(socket);
            } catch (Exception he) {
                System.err.printf("[MITSUBISHI-CNC握手] componentId=%s 发送mochaCancelModal2异常：%s%n", componentId, he.getMessage());
            }

            // 3. 更新连接映射
            connections.put(componentId, socket);
            System.out.printf("[MITSUBISHI-CNC连接] componentId=%s 首次连接成功（%s:%d）%n",
                    componentId, config.getIpAddr(), config.getPort());
            // 连接成功，清除离线节流标记（允许后续断连再次通知离线）
            ComponentOnlineNotifier.markOnline(componentId);
        } catch (Exception e) {
            System.err.printf("[MITSUBISHI-CNC连接] componentId=%s 首次连接失败：%s%n", componentId, e.getMessage());
            // 连接失败时关闭刚创建的 socket，避免 socket 泄漏
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception closeEx) {
                    System.err.printf("[MITSUBISHI-CNC连接] componentId=%s 关闭失败连接异常：%s%n", componentId, closeEx.getMessage());
                }
            }
            // 连接失败时移除无效配置/连接，避免空转
            connections.remove(componentId);
            configMap.remove(componentId);
            // 首次连接失败视为组件离线，通知设备下线（已离线设备幂等跳过）
            ComponentOnlineNotifier.markOfflineAndNotify(componentId);
            return false;
        }
        // 4. 启动消费线程（放 try 外：重复开启组件时 startConsume 抛 IllegalStateException，
        //    应复用已有消费线程，而不是回滚刚建立的有效连接）
        try {
            MitsubishiCncLoopConsumer.startConsume(componentId, SpringUtils.getBean(MitsubishiCncMessageConsumeService.class));
        } catch (IllegalStateException e) {
            System.out.printf("[MITSUBISHI-CNC连接] componentId=%s 已存在消费线程，跳过重复启动%n", componentId);
        }
        return true;
    }

    /**
     * 关闭单个设备连接（兼容原有逻辑，新增配置清理）
     */
    public static void closeConnection(String componentId) {
        // 1. 关闭连接
        Socket socket = connections.remove(componentId);
        if (socket != null) {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception e) {
                System.err.printf("[MITSUBISHI-CNC连接] componentId=%s 关闭失败：%s%n", componentId, e.getMessage());
            }
        }
        // 2. 清理配置（停止该组件的重连检查）
        configMap.remove(componentId);
        // 3. 清理消息队列和消费线程（原有逻辑保留）
        MitsubishiCncMessageScheduler.removeMessageQueue(componentId);
        MitsubishiCncLoopConsumer.stopConsume(componentId);
        System.out.printf("[MITSUBISHI-CNC连接] componentId=%s 连接已关闭，配置已清理%n", componentId);
    }

    /**
     * 关闭所有连接（兼容原有逻辑，新增调度器停止+配置清理）
     */
    public static void closeAllConnections() {
        // 1. 关闭所有连接
        connections.forEach((id, conn) -> {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (Exception e) {
                System.err.printf("[MITSUBISHI-CNC连接] componentId=%s 关闭失败：%s%n", id, e.getMessage());
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
        System.out.println("[MITSUBISHI-CNC连接] 所有连接已关闭，健康检查调度器已停止");
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
        MitsubishiCncTcpConfig config = configMap.get(componentId);
        if (config == null) {
            return;
        }
        Socket socket = connections.get(componentId);
        // 校验连接是否有效
        boolean isConnectionValid = isConnectionValid(socket);
        if (!isConnectionValid) {
            System.out.printf("[MITSUBISHI-CNC重连] componentId=%s 连接失效，开始重连（%s:%d）%n",
                    componentId, config.getIpAddr(), config.getPort());
            // 执行重连逻辑
            reconnect(componentId, config);
        } else {
            // 连接有效，清除离线节流标记（连接已恢复，允许再次断连时通知离线）
            ComponentOnlineNotifier.markOnline(componentId);
        }
    }

    /**
     * 精准校验连接是否有效
     * @param socket MOCHA TCP连接
     * @return true=有效，false=失效
     */
    private static boolean isConnectionValid(Socket socket) {
        if (socket == null) {
            return false;
        }
        try {
            // 仅检查Socket本地状态；不发送OOB紧急字节探测（0xFF可能被机床协议栈当垃圾数据打乱帧对齐，与Brother/MC一致）。
            // 半开连接由读超时触发 forceReconnect 自愈
            if (!socket.isConnected() || socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 执行重连逻辑
     * @param componentId 组件唯一标识
     * @param config 连接配置
     * @return 重连是否成功
     */
    private static boolean reconnect(String componentId, MitsubishiCncTcpConfig config) {
        try {
            // 1. 创建新连接
            InetAddress address = InetAddress.getByName(config.getIpAddr());
            Socket newSocket = new Socket(address, config.getPort());
            newSocket.setSoTimeout(config.getTimeout());
            newSocket.setTcpNoDelay(true);
            // MOCHA 握手：取消模态（失败不阻断重连）
            try {
                MitsubishiCncDataReader.sendCancelModal(newSocket);
            } catch (Exception he) {
                System.err.printf("[MITSUBISHI-CNC握手] componentId=%s 重连后发送mochaCancelModal2异常：%s%n", componentId, he.getMessage());
            }

            // 2. 关闭旧连接（释放资源）
            Socket oldConn = connections.get(componentId);
            if (oldConn != null) {
                try {
                    oldConn.close();
                } catch (Exception e) {
                    // 旧连接关闭失败不影响新连接创建
                    System.err.printf("[MITSUBISHI-CNC重连] componentId=%s 旧连接关闭失败：%s%n", componentId, e.getMessage());
                }
            }

            // 3. 更新连接映射
            connections.put(componentId, newSocket);
            System.out.printf("[MITSUBISHI-CNC重连] componentId=%s 重连成功（%s:%d）%n",
                    componentId, config.getIpAddr(), config.getPort());
            // 重连成功，清除离线节流标记
            ComponentOnlineNotifier.markOnline(componentId);
            return true;
        } catch (Exception e) {
            System.err.printf("[MITSUBISHI-CNC重连] componentId=%s 重连失败：%s%n", componentId, e.getMessage());
            // 重连失败时移除无效连接（避免下次检查重复处理）
            connections.remove(componentId);
            // 重连失败视为组件离线，通知设备下线（节流，仅在在线→离线转变时发一次）
            ComponentOnlineNotifier.markOfflineAndNotify(componentId);
            return false;
        }
    }

    /**
     * 强制重连（读失败时调用：半开连接本地状态检测不出来，靠读超时触发重连，与S7/MC的forceReconnect对齐）
     * @param componentId 组件唯一标识
     */
    public static void forceReconnect(String componentId) {
        MitsubishiCncTcpConfig config = configMap.get(componentId);
        if (config == null) {
            System.err.printf("[MITSUBISHI-CNC重连] componentId=%s 无连接配置，无法重连%n", componentId);
            return;
        }
        reconnect(componentId, config);
    }
}
