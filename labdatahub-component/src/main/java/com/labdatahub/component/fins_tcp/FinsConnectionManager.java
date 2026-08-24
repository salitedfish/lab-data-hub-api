//由AI修改
package com.labdatahub.component.fins_tcp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.labdatahub.common.utils.spring.SpringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * FINS连接管理器，维护多个设备的连接（新增自动检查+定时重连）
 */
@Slf4j
public class FinsConnectionManager {
    // 存储设备连接：key为componentId（线程安全）
    public static Map<String, Socket> connections = new ConcurrentHashMap<>();
    // 存储每个componentId对应的连接配置（用于重连）
    public static final Map<String, FinsTcpConfig> configMap = new ConcurrentHashMap<>();
    // 连接健康检查调度器（全局单例，定时检查连接状态）
    private static final ScheduledExecutorService healthCheckScheduler = Executors.newScheduledThreadPool(
            1,
            r -> {
                Thread t = new Thread(r, "fins-connection-health-check");
                t.setDaemon(true); // 守护线程，应用退出时自动销毁
                return t;
            }
    );
    
    private static final int HEALTH_CHECK_INTERVAL = 10;
    // 静态初始化：启动全局连接健康检查（每10秒检查一次）
    static {
        healthCheckScheduler.scheduleAtFixedRate(
                FinsConnectionManager::checkAllConnections,
                0,
                HEALTH_CHECK_INTERVAL,
                TimeUnit.SECONDS
        );
        log.info("FINS 连接健康检查任务已启动，检查间隔={}秒", HEALTH_CHECK_INTERVAL);
    }
    
    /**
     * 从输入流中读取指定长度的数据，直到读满
     */
    private static void readFully(InputStream in, byte[] buffer) throws Exception {
        int totalRead = 0;
        int len;
        while (totalRead < buffer.length) {
            len = in.read(buffer, totalRead, buffer.length - totalRead);
            if (len == -1) {
                throw new Exception("流已结束，无法读取足够的数据，预期" + buffer.length + "字节，已读取" + totalRead + "字节");
            }
            totalRead += len;
        }
    }
    
    /**
     * FINS/TCP握手，获取PLC节点地址
     */
    private static int doHandshake(Socket socket, int clientNodeAddr) throws Exception {
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        
        // 构建握手请求报文
        byte[] handshakeReq = new byte[] {
            'F', 'I', 'N', 'S', // FINS头
            0x00, 0x00, 0x00, 0x0C, // 后续数据长度：12字节
            0x00, 0x00, 0x00, 0x00, // 命令：节点地址发送
            0x00, 0x00, 0x00, 0x00, // 错误码
            0x00, 0x00, 0x00, (byte) clientNodeAddr // 客户端节点地址
        };
        
        out.write(handshakeReq);
        out.flush();
        
        // 读取握手响应，共24字节（使用readFully确保读满）
        byte[] handshakeResp = new byte[24];
        readFully(in, handshakeResp);
        
        // 解析PLC节点地址，最后4字节，大端
        int plcNodeAddr = ((handshakeResp[20] & 0xFF) << 24) |
                          ((handshakeResp[21] & 0xFF) << 16) |
                          ((handshakeResp[22] & 0xFF) << 8) |
                          (handshakeResp[23] & 0xFF);
        
        return plcNodeAddr;
    }

    /**
     * 创建连接（兼容原有逻辑，新增配置存储）
     * @param componentId 组件唯一标识
     * @param config 连接配置
     * @return 首次连接是否成功
     */
    public static boolean addConnection(String componentId, FinsTcpConfig config) {
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

            // 建立连接后执行握手
            int plcNodeAddr = doHandshake(socket, config.getClientNodeAddress());
            config.setPlcNodeAddress(plcNodeAddr);
            System.out.printf("[FINS握手] componentId=%s 获取PLC节点地址：%d%n", componentId, plcNodeAddr);

            // 3. 存储配置（用于后续重连）
            configMap.put(componentId, config);

            // 4. 更新连接映射
            connections.put(componentId, socket);
            System.out.printf("[FINS连接] componentId=%s 首次连接成功（%s:%d）%n",
                    componentId, config.getIpAddr(), config.getPort());
        } catch (Exception e) {
            System.err.printf("[FINS连接] componentId=%s 首次连接失败：%s%n", componentId, e.getMessage());
            // 连接失败时关闭刚创建的 socket（含握手失败场景），避免 socket 泄漏
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception closeEx) {
                    System.err.printf("[FINS连接] componentId=%s 关闭失败连接异常：%s%n", componentId, closeEx.getMessage());
                }
            }
            // 连接失败时移除无效配置/连接，避免空转
            connections.remove(componentId);
            configMap.remove(componentId);
            return false;
        }
        // 5. 启动消费线程（放 try 外：重复开启组件时 startConsume 抛 IllegalStateException，
        //    应复用已有消费线程，而不是回滚刚建立的有效连接）
        try {
            FinsLoopConsumer.startConsume(componentId, SpringUtils.getBean(FinsMessageConsumeService.class));
        } catch (IllegalStateException e) {
            System.out.printf("[FINS连接] componentId=%s 已存在消费线程，跳过重复启动%n", componentId);
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
                System.err.printf("[FINS连接] componentId=%s 关闭失败：%s%n", componentId, e.getMessage());
            }
        }
        // 2. 清理配置（停止该组件的重连检查）
        configMap.remove(componentId);
        // 3. 清理消息队列和消费线程（原有逻辑保留）
        FinsMessageScheduler.removeMessageQueue(componentId);
        FinsLoopConsumer.stopConsume(componentId);
        System.out.printf("[FINS连接] componentId=%s 连接已关闭，配置已清理%n", componentId);
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
                System.err.printf("[FINS连接] componentId=%s 关闭失败：%s%n", id, e.getMessage());
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
        System.out.println("[FINS连接] 所有连接已关闭，健康检查调度器已停止");
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
        FinsTcpConfig config = configMap.get(componentId);
        if (config == null) {
            return;
        }
        Socket socket = connections.get(componentId);
        // 校验连接是否有效
        boolean isConnectionValid = isConnectionValid(socket);
        if (!isConnectionValid) {
            System.out.printf("[FINS重连] componentId=%s 连接失效，开始重连（%s:%d）%n",
                    componentId, config.getIpAddr(), config.getPort());
            // 执行重连逻辑
            reconnect(componentId, config);
        }
    }

    /**
     * 精准校验连接是否有效
     * @param socket FINS TCP连接
     * @return true=有效，false=失效
     */
    private static boolean isConnectionValid(Socket socket) {
        if (socket == null) {
            return false;
        }
        try {
            // 仅检查Socket本地状态；不再发送OOB紧急字节（sendUrgentData 0xFF）探测半开连接——
            // 半开连接内核缓冲仍可写入，探测无意义；且0xFF可能被PLC协议栈当垃圾数据打乱帧对齐（与Brother一致）。
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
    private static boolean reconnect(String componentId, FinsTcpConfig config) {
        try {
            // 1. 创建新连接
            InetAddress address = InetAddress.getByName(config.getIpAddr());
            Socket newSocket = new Socket(address, config.getPort());
            newSocket.setSoTimeout(config.getTimeout());
            newSocket.setTcpNoDelay(true);
            
            // 2. 关闭旧连接（释放资源）
            Socket oldConn = connections.get(componentId);
            if (oldConn != null) {
                try {
                    oldConn.close();
                } catch (Exception e) {
                    // 旧连接关闭失败不影响新连接创建
                    System.err.printf("[FINS重连] componentId=%s 旧连接关闭失败：%s%n", componentId, e.getMessage());
                }
            }
            
            // 3. 执行握手
            int plcNodeAddr = doHandshake(newSocket, config.getClientNodeAddress());
            config.setPlcNodeAddress(plcNodeAddr);
            System.out.printf("[FINS握手] componentId=%s 重连后获取PLC节点地址：%d%n", componentId, plcNodeAddr);
            
            // 4. 更新连接映射
            connections.put(componentId, newSocket);
            System.out.printf("[FINS重连] componentId=%s 重连成功（%s:%d）%n",
                    componentId, config.getIpAddr(), config.getPort());
            return true;
        } catch (Exception e) {
            System.err.printf("[FINS重连] componentId=%s 重连失败：%s%n", componentId, e.getMessage());
            // 重连失败时移除无效连接（避免下次检查重复处理）
            connections.remove(componentId);
            return false;
        }
    }

    /**
     * 强制重连（读失败时调用：半开连接本地状态检测不出来，靠读超时触发重连，与S7的forceReconnect对齐）
     * @param componentId 组件唯一标识
     */
    public static void forceReconnect(String componentId) {
        FinsTcpConfig config = configMap.get(componentId);
        if (config == null) {
            System.err.printf("[FINS重连] componentId=%s 无连接配置，无法重连%n", componentId);
            return;
        }
        reconnect(componentId, config);
    }
}
