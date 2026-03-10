package com.labdatahub.component.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Component
public class NettyWebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyWebSocketServer.class);

    // 存储所有运行的服务器 {port: channelFuture}
    private final Map<Integer, ChannelFuture> servers = new ConcurrentHashMap<>();
    // 存储服务器状态 {port: isRunning}
    private final Map<Integer, Boolean> serverStatus = new ConcurrentHashMap<>();

    // 存储端口对应的路径模式 {port: pathPattern}
    private final Map<Integer, String> serverPaths = new ConcurrentHashMap<>();

    /**
     * 动态开启WebSocket端口
     * @param componentId 组件ID
     * @param port 端口号
     * @param pathPattern WebSocket路径模式（支持通配符）
     * @return 是否开启成功
     */
    public boolean startWebSocketServer(String componentId, int port, String pathPattern) {
        if (servers.containsKey(port)) {
            logger.warn("端口 {} 的WebSocket服务器已在运行", port);
            return false;
        }

        if (port < 1 || port > 65535) {
            logger.error("端口号不合法: {}", port);
            return false;
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // HTTP编解码器
                            pipeline.addLast(new HttpServerCodec());
                            // HTTP消息聚合器
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            // 自定义HTTP请求处理器，用于路径匹配
                            pipeline.addLast(new HttpRequestHandler(pathPattern));
                            // 自定义WebSocket处理器
                            pipeline.addLast(new WebSocketFrameHandler(componentId,port, pathPattern));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口，启动服务器
            ChannelFuture future = bootstrap.bind(port).sync();
            servers.put(port, future);
            serverStatus.put(port, true);
            serverPaths.put(port, pathPattern);
            WebSocketCache.COMPONENT_PORT.put(componentId,port);
            WebSocketCache.PORT_COMPONENT.put(port,componentId);
            logger.info("WebSocket服务器启动成功 - 端口: {}, 路径模式: {}", port, pathPattern);

            // 添加关闭监听
            future.channel().closeFuture().addListener(f -> {
                servers.remove(port);
                serverStatus.remove(port);
                serverPaths.remove(port);
                WebSocketCache.PORT_COMPONENT.remove(port);
                WebSocketCache.COMPONENT_PORT.remove(componentId);
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                logger.info("WebSocket服务器已关闭 - 端口: {}", port);
            });

            return true;

        } catch (Exception e) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            logger.error("启动WebSocket服务器失败 - 端口: {}", port, e);
            return false;
        }
    }

    /**
     * 自定义HTTP请求处理器，支持通配符路径匹配
     */
    private static class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        private final String pathPattern;
        private final Pattern compiledPattern;

        public HttpRequestHandler(String pathPattern) {
            this.pathPattern = pathPattern;
            // 将通配符路径转换为正则表达式
            this.compiledPattern = compilePathPattern(pathPattern);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
            // 只处理WebSocket升级请求
            if (isWebSocketUpgrade(request)) {
                String requestPath = request.uri();

                // 检查路径是否匹配模式
                if (matchesPathPattern(requestPath)) {
                    // 路径匹配，进行WebSocket握手
                    handleWebSocketHandshake(ctx, request);
                } else {
                    // 路径不匹配，返回404
                    sendHttpResponse(ctx, request, new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
                }
            } else {
                // 非WebSocket请求，传递给下一个处理器
                ctx.fireChannelRead(request.retain());
            }
        }

        private boolean isWebSocketUpgrade(FullHttpRequest request) {
            HttpHeaders headers = request.headers();
            return "Upgrade".equalsIgnoreCase(headers.get(HttpHeaderNames.CONNECTION)) &&
                    "WebSocket".equalsIgnoreCase(headers.get(HttpHeaderNames.UPGRADE));
        }

        private boolean matchesPathPattern(String requestPath) {
            // 处理查询参数
            int queryParamIndex = requestPath.indexOf('?');
            if (queryParamIndex > 0) {
                requestPath = requestPath.substring(0, queryParamIndex);
            }

            return compiledPattern.matcher(requestPath).matches();
        }

        private void handleWebSocketHandshake(ChannelHandlerContext ctx, FullHttpRequest request) {
            try {
                // 创建WebSocket握手工厂
                WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                        getWebSocketLocation(request), null, true, 65536);

                WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
                if (handshaker == null) {
                    WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                } else {
                    // 在握手前保存路径信息到 Channel
                    String requestPath = request.uri();
                    ctx.channel().attr(WebSocketFrameHandler.WEBSOCKET_PATH).set(requestPath);

                    handshaker.handshake(ctx.channel(), request).addListener(future -> {
                        if (future.isSuccess()) {
                            logger.debug("WebSocket握手成功 - 路径: {}", requestPath);
                        } else {
                            logger.error("WebSocket握手失败", future.cause());
                        }
                    });
                }
            } catch (Exception e) {
                logger.error("WebSocket握手处理异常", e);
                sendHttpResponse(ctx, request, new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR));
            }
        }

        private String getWebSocketLocation(FullHttpRequest request) {
            String protocol = "ws";
            if (request.headers().contains(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL)) {
                protocol = "wss";
            }
            return protocol + "://" + request.headers().get(HttpHeaderNames.HOST) + request.uri();
        }

        private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
            // 生成错误响应
            if (response.status().code() != 200) {
                HttpUtil.setContentLength(response, response.content().readableBytes());
            }

            // 发送响应
            ChannelFuture f = ctx.channel().writeAndFlush(response);
            if (!HttpUtil.isKeepAlive(request) || response.status().code() != 200) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }

        /**
         * 将通配符路径模式编译为正则表达式
         */
        private Pattern compilePathPattern(String pattern) {
            if (pattern == null || pattern.isEmpty()) {
                pattern = "/*";
            }

            // 转义正则表达式特殊字符
            String regex = pattern
                    .replace("\\", "\\\\")
                    .replace(".", "\\.")
                    .replace("+", "\\+")
                    .replace("$", "\\$")
                    .replace("^", "\\^")
                    .replace("[", "\\[")
                    .replace("]", "\\]")
                    .replace("(", "\\(")
                    .replace(")", "\\)")
                    .replace("|", "\\|")
                    .replace("{", "\\{")
                    .replace("}", "\\}");

            // 将通配符转换为正则表达式
            regex = regex
                    .replace("*", ".*")   // * 匹配任意字符
                    .replace("?", ".");   // ? 匹配单个字符

            // 确保以 ^ 开头和 $ 结尾，进行完整匹配
            if (!regex.startsWith("^")) {
                regex = "^" + regex;
            }
            if (!regex.endsWith("$")) {
                regex = regex + "$";
            }

            return Pattern.compile(regex);
        }
    }

    /**
     * 关闭指定端口的WebSocket服务器
     * @param port 端口号
     * @return 是否关闭成功
     */
    public boolean stopWebSocketServer(int port) {
        ChannelFuture server = servers.get(port);
        if (server == null) {
            logger.warn("端口 {} 的WebSocket服务器未运行", port);
            return false;
        }

        try {
            server.channel().close().sync();
            servers.remove(port);
            serverStatus.remove(port);
            serverPaths.remove(port);
            String componentId = WebSocketCache.PORT_COMPONENT.remove(port);
            WebSocketCache.COMPONENT_PORT.remove(componentId);
            logger.info("WebSocket服务器关闭成功 - 端口: {}", port);
            return true;
        } catch (Exception e) {
            logger.error("关闭WebSocket服务器失败 - 端口: {}", port, e);
            return false;
        }
    }

    /**
     * 关闭网络组件
     * @param componentId
     * @return
     */
    public boolean stopWebSocketServer(String componentId) {
        Integer port = WebSocketCache.COMPONENT_PORT.get(componentId);
        if(port!=null){
            return stopWebSocketServer(port);
        }
        return false;
    }

    /**
     * 获取所有运行的WebSocket服务器信息
     */
    public Map<Integer, Boolean> getServerStatus() {
        return new ConcurrentHashMap<>(serverStatus);
    }

    /**
     * 检查端口是否在运行WebSocket服务器
     */
    public boolean isPortRunning(int port) {
        return serverStatus.containsKey(port) && serverStatus.get(port);
    }

    /**
     * 检查组件是否正在运行ws
     */
    public boolean isComponentRunning(String componentId) {
        return WebSocketCache.COMPONENT_PORT.containsKey(componentId);
    }

    /**
     * 获取端口对应的路径模式
     */
    public String getServerPath(int port) {
        return serverPaths.get(port);
    }

}