package com.labdatahub.component.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketManagerService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketManagerService.class);
    private final NettyWebSocketServer nettyWebSocketServer;

    // 存储端点配置 {port: WebSocketEndpointConfig}
    private final Map<Integer, WebSocketEndpointConfig> endpointConfigs = new ConcurrentHashMap<>();

    public static final Map<String,Integer> COMPONENT_PORT = new HashMap<>();

    public WebSocketManagerService(NettyWebSocketServer nettyWebSocketServer) {
        this.nettyWebSocketServer = nettyWebSocketServer;
    }

    @PostConstruct
    public void init() {
        logger.info("WebSocket管理服务初始化完成");
    }

    /**
     * 动态开启WebSocket端口
     */
    public boolean createWebSocketEndpoint(String componentId,int port, String path, String description) {
        try {
            boolean success = nettyWebSocketServer.startWebSocketServer(componentId,port, path);
            if (success) {
                WebSocketEndpointConfig config = new WebSocketEndpointConfig(port, path, description);
                endpointConfigs.put(port, config);
                logger.info("WebSocket端点创建成功 - 端口: {}, 路径: {}", port, path);
            }
            return success;
        } catch (Exception e) {
            logger.error("创建WebSocket端点失败 - 端口: {}, 路径: {}", port, path, e);
            return false;
        }
    }

    /**
     * 关闭WebSocket端口
     */
    public boolean removeWebSocketEndpoint(int port) {
        try {
            boolean success = nettyWebSocketServer.stopWebSocketServer(port);
            if (success) {
                endpointConfigs.remove(port);
                logger.info("WebSocket端点移除成功 - 端口: {}", port);
            }
            return success;
        } catch (Exception e) {
            logger.error("移除WebSocket端点失败 - 端口: {}", port, e);
            return false;
        }
    }

    /**
     * 向指定端口广播消息
     */
    public void broadcastToPort(int port, String message) {
        WebSocketFrameHandler.broadcastToPort(port, message);
    }

    /**
     * 获取所有运行的WebSocket端点
     */
    public Map<Integer, WebSocketEndpointConfig> getActiveEndpoints() {
        return new ConcurrentHashMap<>(endpointConfigs);
    }

    /**
     * 获取端口连接数
     */
    public int getConnectionCount(int port) {
        return WebSocketFrameHandler.getConnectionCount(port);
    }

    /**
     * 检查端口是否在运行
     */
    public boolean isPortRunning(int port) {
        return nettyWebSocketServer.isPortRunning(port);
    }

    /**
     * 端点配置类
     */
    public static class WebSocketEndpointConfig {
        private int port;
        private String path;
        private String description;
        private long createTime;

        public WebSocketEndpointConfig(int port, String path, String description) {
            this.port = port;
            this.path = path;
            this.description = description;
            this.createTime = System.currentTimeMillis();
        }

        // getter and setter
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public long getCreateTime() { return createTime; }
        public void setCreateTime(long createTime) { this.createTime = createTime; }
    }
}