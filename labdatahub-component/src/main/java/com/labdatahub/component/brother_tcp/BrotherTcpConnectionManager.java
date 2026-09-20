//由AI修改
package com.labdatahub.component.brother_tcp;

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
import com.labdatahub.component.utils.ParallelHealthCheck;

import lombok.extern.slf4j.Slf4j;

/**
 * Brother NC（兄弟数控）连接管理器，维护多个设备的连接（新增自动检查+定时重连）
 * Brother NC 协议无握手，建连即可通信
 */
@Slf4j
public class BrotherTcpConnectionManager {
    // 存储设备连接：key为componentId（线程安全）
    public static Map<String, Socket> connections = new ConcurrentHashMap<>();
    // 存储每个componentId对应的连接配置（用于重连）
    public static final Map<String, BrotherTcpConfig> configMap = new ConcurrentHashMap<>();
    // 连接健康检查调度器（全局单例，定时检查连接状态）
    private static final ScheduledExecutorService healthCheckScheduler = Executors.newScheduledThreadPool(
            1,
            r -> {
                Thread t = new Thread(r, "brother-connection-health-check");
                t.setDaemon(true); // 守护线程，应用退出时自动销毁
                return t;
            }
    );

    private static final int HEALTH_CHECK_INTERVAL = 10;
    // 静态初始化：启动全局连接健康检查（每10秒检查一次）
    static {
        healthCheckScheduler.scheduleAtFixedRate(
                BrotherTcpConnectionManager::checkAllConnections,
                0,
                HEALTH_CHECK_INTERVAL,
                TimeUnit.SECONDS
        );
        log.info("Brother 连接健康检查任务已启动，检查间隔={}秒", HEALTH_CHECK_INTERVAL);
    }

    /**
     * 创建连接（兼容原有逻辑，新增配置存储）
     * @param componentId 组件唯一标识
     * @param config 连接配置
     * @return 首次连接是否成功
     */
    public static boolean addConnection(String componentId, BrotherTcpConfig config) {
        // 1. 校验参数
        if (componentId == null || config == null || config.getIpAddr() == null) {
            return false;
        }
        // 先把消费线程拉起来，与「连接是否成功」解耦：
        //   调用方（LabdatahubComponentServiceImpl 的 BROTHER_TCP 分支）是「先 addConnection、再
        //   timerTask.initBrotherTcpRead()」，也就是无论连接成败，读配置的定时任务都已经开始产消息了；
        //   消费线程不起来，队列只会被灌满到上限、恢复后还要回放一堆过期快照。
        //   连接由健康检查在机床回来后自动重建（见 checkAndReconnect），届时数据即可正常被消费。
        //   （消费线程对「队列不存在」自带退避重试，见 BrotherTcpLoopConsumer#run 的 QUEUE_MISSING_RETRY_MS）
        try {
            if (!BrotherTcpLoopConsumer.isConsuming(componentId)) {
                BrotherTcpLoopConsumer.startConsume(componentId, SpringUtils.getBean(BrotherTcpMessageConsumeService.class));
            }
        } catch (IllegalStateException e) {
            System.out.printf("[Brother连接] componentId=%s 已存在消费线程，跳过重复启动%n", componentId);
        }
        Socket socket = null;
        try {
            configMap.put(componentId, config);
            // 先关闭旧连接（避免资源泄漏）
            Socket oldConn = connections.get(componentId);
            if (oldConn != null && !oldConn.isClosed()) {
                oldConn.close();
            }
            // 2. 执行连接逻辑（带连接超时，避免对不可达地址长时间阻塞；Brother NC 无握手，建连即可）
            InetAddress address = InetAddress.getByName(config.getIpAddr());
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, config.getPort()), config.getTimeout());
            socket.setSoTimeout(config.getTimeout());
            socket.setTcpNoDelay(true); // 禁用Nagle算法，降低延迟

            // 3. 存储配置（用于后续重连）
            configMap.put(componentId, config);

            // 4. 更新连接映射
            connections.put(componentId, socket);
            System.out.printf("[Brother连接] componentId=%s 首次连接成功（%s:%d）%n",
                    componentId, config.getIpAddr(), config.getPort());
            // 连接成功，清除离线节流标记（允许后续断连再次通知离线）
            ComponentOnlineNotifier.markOnline(componentId);
            return true;
        } catch (Exception e) {
            System.err.printf("[Brother连接] componentId=%s 首次连接失败：%s%n", componentId, e.getMessage());
            // 连接失败时关闭刚创建的 socket，避免 socket 泄漏
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception closeEx) {
                    System.err.printf("[Brother连接] componentId=%s 关闭失败连接异常：%s%n", componentId, closeEx.getMessage());
                }
            }
            // 连接失败时移除无效连接，避免空转
            // ⚠️ 但 configMap 不能删：它是健康检查的监控名单（checkAllConnections 遍历的就是它的 keySet），
            //    删掉等于判定「不再重连」。而「首次连接失败」只说明机床此刻不在线（平台启动时机床正好没开），
            //    不代表组件不该被监控 —— 删掉之后机床开机了也永远接不回去，只能由管理员在页面上重新点开启。
            //    配置留在 map 里，健康检查每 10 秒会自动重试（组件被真正关闭时走 closeConnection，那里才清 configMap）
            connections.remove(componentId);
            // 首次连接失败视为组件离线，通知设备下线（已离线设备幂等跳过）
            ComponentOnlineNotifier.markOfflineAndNotify(componentId);
            return false;
        }
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
                System.err.printf("[Brother连接] componentId=%s 关闭失败：%s%n", componentId, e.getMessage());
            }
        }
        // 2. 清理配置（停止该组件的重连检查）
        configMap.remove(componentId);
        // 3. 清理消息队列和消费线程（原有逻辑保留）
        BrotherTcpMessageScheduler.removeMessageQueue(componentId);
        BrotherTcpLoopConsumer.stopConsume(componentId);
        System.out.printf("[Brother连接] componentId=%s 连接已关闭，配置已清理%n", componentId);
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
                System.err.printf("[Brother连接] componentId=%s 关闭失败：%s%n", id, e.getMessage());
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
        System.out.println("[Brother连接] 所有连接已关闭，健康检查调度器已停止");
    }

    // ========== 核心新增：连接健康检查与重连逻辑 ==========
    /**
     * 检查所有连接状态，失效则自动重连
     */
    private static void checkAllConnections() {
        // 并发检查：原先单线程串行遍历，设备离线时每台都要等满一次建连超时，
        // 100 台同协议设备同时离线要几分钟才轮完一遍，断线/恢复感知随之失效
        // （详见 ParallelHealthCheck 的类注释）
        ParallelHealthCheck.run("Brother", configMap.keySet(), BrotherTcpConnectionManager::checkAndReconnect, HEALTH_CHECK_INTERVAL);
    }

    /**
     * 检查单个componentId的连接状态，失效则重连
     * @param componentId 组件唯一标识
     */
    private static void checkAndReconnect(String componentId) {
        BrotherTcpConfig config = configMap.get(componentId);
        if (config == null) {
            return;
        }
        Socket socket = connections.get(componentId);
        // 校验连接是否有效
        boolean isConnectionValid = isConnectionValid(socket);
        if (!isConnectionValid) {
            System.out.printf("[Brother重连] componentId=%s 连接失效，开始重连（%s:%d）%n",
                    componentId, config.getIpAddr(), config.getPort());
            // 执行重连逻辑
            reconnect(componentId, config);
        } else {
            // 连接有效，清除离线节流标记（连接已恢复，允许再次断连时通知离线）
            ComponentOnlineNotifier.markOnline(componentId);
        }
    }

    /**
     * 校验连接是否有效
     * @param socket Brother TCP连接
     * @return true=有效，false=失效
     */
    private static boolean isConnectionValid(Socket socket) {
        if (socket == null) {
            return false;
        }
        try {
            // 仅检查Socket本地状态即可；不再发送OOB紧急字节（sendUrgentData 0xFF）探测半开连接——
            // 机床是严格的ASCII文本协议，0xFF 会被协议栈当垃圾数据，反而可能触发断连
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
    private static boolean reconnect(String componentId, BrotherTcpConfig config) {
        // 先关闭旧连接（机床只服务一个活动数据连接，新旧并存会把正在使用的旧连接踢掉）
        Socket oldConn = connections.remove(componentId);
        if (oldConn != null) {
            try {
                oldConn.close();
            } catch (Exception e) {
                // 旧连接关闭失败不影响新连接创建
                System.err.printf("[Brother重连] componentId=%s 旧连接关闭失败：%s%n", componentId, e.getMessage());
            }
        }
        Socket newSocket = null;
        try {
            // 1. 创建新连接
            // ⚠️ connect 必须带超时：`new Socket(addr, port)` 走的是操作系统默认 connect 超时
            //    （Windows 约 21 秒、Linux 约 127 秒）。设备断电/拔网线时，健康检查串行遍历到这台
            //    就会卡这么久 —— 100 台同时离线时一轮要 35 分钟，离线/恢复感知彻底失效。
            //    首次连接（addConnection）本就带超时，重连也必须一致。
            InetAddress address = InetAddress.getByName(config.getIpAddr());
            newSocket = new Socket();
            newSocket.connect(new InetSocketAddress(address, config.getPort()), config.getTimeout());
            newSocket.setSoTimeout(config.getTimeout());
            newSocket.setTcpNoDelay(true);

            // 2. 更新连接映射
            connections.put(componentId, newSocket);
            // 已交给 connections 托管，后面的异常不再由本地引用负责关闭
            newSocket = null;
            System.out.printf("[Brother重连] componentId=%s 重连成功（%s:%d）%n",
                    componentId, config.getIpAddr(), config.getPort());
            // 重连成功，清除离线节流标记
            ComponentOnlineNotifier.markOnline(componentId);
            return true;
        } catch (Exception e) {
            // ⚠️ 半成品 socket（还没进 connections 的）必须由本地引用显式关闭：
            //    new Socket 成功、后续 setSoTimeout/setTcpNoDelay 抛异常时它就没主了 ——
            //    不关就永远停在 CLOSE_WAIT（FINS 同一位置实测漏了 12 条）
            if (newSocket != null) {
                try {
                    newSocket.close();
                } catch (Exception closeEx) {
                    System.err.printf("[Brother重连] componentId=%s 关闭半成品连接失败：%s%n", componentId, closeEx.getMessage());
                }
            }
            System.err.printf("[Brother重连] componentId=%s 重连失败：%s%n", componentId, e.getMessage());
            // 重连失败时移除无效连接（避免下次检查重复处理）；旧连接在上面的步骤里已摘已关，这里只会摘到空
            connections.remove(componentId);
            // 重连失败视为组件离线，通知设备下线（节流，仅在在线→离线转变时发一次）
            ComponentOnlineNotifier.markOfflineAndNotify(componentId);
            return false;
        }
    }

    /**
     * 主动强制重连（读取失败时由读取器触发）：关闭死连接并建立新连接
     * @param componentId 组件唯一标识
     */
    public static void forceReconnect(String componentId) {
        BrotherTcpConfig config = configMap.get(componentId);
        if (config == null) {
            // 无连接配置时仅清理死连接，避免残留悬挂
            Socket dead = connections.remove(componentId);
            if (dead != null) {
                try {
                    dead.close();
                } catch (Exception e) {
                    // 忽略关闭异常
                }
            }
            return;
        }
        reconnect(componentId, config);
    }
}
