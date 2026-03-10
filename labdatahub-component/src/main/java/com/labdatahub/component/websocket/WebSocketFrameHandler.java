package com.labdatahub.component.websocket;

import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.event.EventBus;
import com.labdatahub.component.event.MessageUpEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.labdatahub.component.websocket.WebSocketCache.CLIENT_DEVICE;
import static com.labdatahub.component.websocket.WebSocketCache.DEVICE_CLIENT;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketFrameHandler.class);
    private final String componentId;
    private final int port;
    private final String pathPattern;

    // 定义AttributeKey用于存储路径信息
    public static final AttributeKey<String> WEBSOCKET_PATH = AttributeKey.valueOf("websocketPath");
    public static final AttributeKey<Map<String, String>> QUERY_PARAMS = AttributeKey.valueOf("queryParams");

    // 存储连接会话 {port: {channelId: ctx}}
    private static final Map<Integer, Map<String, ChannelHandlerContext>> portConnections = new ConcurrentHashMap<>();

    public WebSocketFrameHandler(String componentId,int port, String pathPattern) {
        this.componentId = componentId;
        this.port = port;
        this.pathPattern = pathPattern;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asShortText();

        // 获取当前连接的完整路径
        String currentPath = ctx.channel().attr(WEBSOCKET_PATH).get();
        if (currentPath == null) {
            currentPath = "未知路径";
        }

        // 按端口分组存储连接
        portConnections.computeIfAbsent(port, k -> new ConcurrentHashMap<>())
                .put(channelId, ctx);

        logger.info("WebSocket连接建立 - 端口: {}, 路径模式: {}, 实际路径: {}, 通道: {}",
                port, pathPattern, currentPath, channelId);

        // 发送连接成功消息（包含路径信息）
        String welcomeMsg = String.format(
                "{\"type\":\"connection\",\"status\":\"connected\",\"port\":%d,\"pathPattern\":\"%s\",\"actualPath\":\"%s\"}",
                port, pathPattern, currentPath
        );
        ctx.channel().writeAndFlush(new TextWebSocketFrame(welcomeMsg));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        String channelId = ctx.channel().id().asShortText();
        // 获取当前连接的完整路径
        String currentPath = ctx.channel().attr(WEBSOCKET_PATH).get();
        if (currentPath == null) {
            currentPath = "未知路径";
        }

        if (frame instanceof TextWebSocketFrame) {
            // 处理文本消息
            String request = ((TextWebSocketFrame) frame).text();
//            logger.info("收到WebSocket消息 - 端口: {}, 路径: {}, 消息: {}", port, currentPath, request);
            SpringUtils.getBean(WebSocketServerConsumer.class).message(componentId,port,currentPath,request,channelId);
//            // 回复消息（包含路径信息）
//            String response = String.format(
//                    "{\"type\":\"message\",\"status\":\"received\",\"port\":%d,\"path\":\"%s\",\"timestamp\":%d,\"yourMessage\":\"%s\"}",
//                    port, currentPath, System.currentTimeMillis(), request
//            );
//            ctx.channel().writeAndFlush(new TextWebSocketFrame(response));

        } else if (frame instanceof PingWebSocketFrame) {
            logger.debug("收到Ping消息 - 端口: {}, 路径: {}", port, currentPath);
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));

        } else if (frame instanceof CloseWebSocketFrame) {
            logger.info("收到关闭帧 - 端口: {}, 路径: {}", port, currentPath);
            ctx.channel().close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asShortText();

        // 获取路径信息用于日志
        String currentPath = ctx.channel().attr(WEBSOCKET_PATH).get();
        if (currentPath == null) {
            currentPath = "未知路径";
        }

        Map<String, ChannelHandlerContext> connections = portConnections.get(port);
        if (connections != null) {
            connections.remove(channelId);
            if (connections.isEmpty()) {
                portConnections.remove(port);
            }
        }
        if(CLIENT_DEVICE.getOrDefault(channelId,null)!=null){
            SpringUtils.getBean(EventBus.class).publish("device.offline",CLIENT_DEVICE.get(channelId));
        }
        logger.info("WebSocket连接关闭 - 端口: {}, 路径: {}, 通道: {}", port, currentPath, channelId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 获取路径信息用于日志
        String currentPath = ctx.channel().attr(WEBSOCKET_PATH).get();
        if (currentPath == null) {
            currentPath = "未知路径";
        }

        logger.error("WebSocket处理异常 - 端口: {}, 路径: {}", port, currentPath, cause);
        ctx.close();
    }

    /**
     * 向指定端口的所有连接广播消息
     */
    public static void broadcastToPort(int port, String message) {
        Map<String, ChannelHandlerContext> connections = portConnections.get(port);
        if (connections != null) {
            TextWebSocketFrame frame = new TextWebSocketFrame(message);
            connections.values().forEach(ctx -> {
                if (ctx.channel().isActive()) {
                    ctx.channel().writeAndFlush(frame);
                }
            });
        }
    }

    /**
     * 向指定设备发送消息
     */
    public static boolean sendToDevice(String componentId,String deviceSn, String message) {
        Integer port = WebSocketCache.COMPONENT_PORT.get(componentId);
        String channelId = DEVICE_CLIENT.get(deviceSn);
        AtomicBoolean result = new AtomicBoolean(false);
        if(port!=null&&channelId!=null) {
            Map<String, ChannelHandlerContext> connections = portConnections.get(port);
            if (connections != null) {
                TextWebSocketFrame frame = new TextWebSocketFrame(message);
                connections.values().forEach(ctx -> {
                    String id = ctx.channel().id().asShortText();
                    if (channelId.equals(id)&&ctx.channel().isActive()) {
                        ctx.channel().writeAndFlush(frame);
                        result.set(true);
                    }
                });
            }
        }
        return result.get();
    }

    /**
     * 向指定端口的特定路径连接发送消息
     */
    public static void sendToPath(int port, String path, String message) {
        Map<String, ChannelHandlerContext> connections = portConnections.get(port);
        if (connections != null) {
            TextWebSocketFrame frame = new TextWebSocketFrame(message);
            connections.values().forEach(ctx -> {
                String currentPath = ctx.channel().attr(WEBSOCKET_PATH).get();
                if (ctx.channel().isActive() && path.equals(currentPath)) {
                    ctx.channel().writeAndFlush(frame);
                }
            });
        }
    }

    /**
     * 获取指定端口的连接数
     */
    public static int getConnectionCount(int port) {
        Map<String, ChannelHandlerContext> connections = portConnections.get(port);
        return connections != null ? connections.size() : 0;
    }

    /**
     * 获取指定端口的路径统计
     */
    public static Map<String, Integer> getPathStatistics(int port) {
        Map<String, Integer> pathStats = new ConcurrentHashMap<>();
        Map<String, ChannelHandlerContext> connections = portConnections.get(port);

        if (connections != null) {
            connections.values().forEach(ctx -> {
                String path = ctx.channel().attr(WEBSOCKET_PATH).get();
                if (path != null) {
                    pathStats.put(path, pathStats.getOrDefault(path, 0) + 1);
                }
            });
        }

        return pathStats;
    }
}