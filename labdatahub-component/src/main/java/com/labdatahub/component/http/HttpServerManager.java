package com.labdatahub.component.http;

import com.labdatahub.common.utils.spring.SpringUtils;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP 服务器管理器
 * 支持动态启动、停止多个 HTTP 服务器实例
 * 每个服务器实例可独立配置端口和请求处理器
 */
public class HttpServerManager {

    // 存储所有运行的服务器实例，key 为组件 ID
    private static final Map<String, TomcatWebServer> servers = new ConcurrentHashMap<>();
    // 存储所有服务器的配置信息
    private static final Map<String, HttpServerConfig> serverConfigs = new ConcurrentHashMap<>();

    // 私有构造器，防止实例化
    private HttpServerManager() {}

    /**
     * 动态启动 HTTP 服务器（基础版本）
     * @param componentId 组件唯一标识
     * @param port 服务器端口号
     * @param needReply 是否需要响应数据
     * @return 启动成功返回 true，否则返回 false
     */
    public static boolean startServer(String componentId,int port,boolean needReply) {
        return startServer(componentId,port,needReply, null);
    }

    /**
     * 动态启动 HTTP 服务器（带自定义处理器）
     * @param componentId 组件唯一标识
     * @param port 服务器端口号 (1-65535)
     * @param needReply 是否需要响应数据
     * @param customHandler 自定义请求处理器，可为 null
     * @return 启动成功返回 true，否则返回 false
     */
    public static boolean startServer(String componentId,int port,boolean needReply, HttpRequestHandler customHandler) {
        try {
            // 检查组件是否已存在服务器
            if (servers.containsKey(componentId)) {
                System.err.println("端口 " + port + " 已在运行");
                return false;
            }

            // 验证端口号范围
            if (port < 1 || port > 65535) {
                System.err.println("端口号必须在 1-65535 之间");
                return false;
            }

            // 创建 Tomcat 服务器工厂
            TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
            factory.setPort(port);

            // 初始化 Servlet 上下文
            TomcatWebServer server = (TomcatWebServer) factory.getWebServer(new ServletContextInitializer() {
                @Override
                public void onStartup(ServletContext servletContext) {
                    // 注册默认 Servlet 处理器
                    ServletRegistration.Dynamic dynamic = servletContext.addServlet("default", new HttpServlet() {
                        @Override
                        protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
                            String path = req.getRequestURI();
                            // 过滤静态资源请求（.ico、.css、.js）
                            if (path.endsWith(".ico") || path.endsWith(".css") || path.endsWith(".js")) {
                                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                return;
                            }
                            // 创建请求上下文，封装请求和响应对象
                            RequestContext context = new RequestContext(req, resp, port);

                            // 使用自定义处理器或默认处理器处理请求
                            if (customHandler != null) {
                                customHandler.handle(componentId,context,needReply);
                            } else {
                                handleDefaultRequest(componentId,context,needReply);
                            }
                        }
                    });
                    dynamic.addMapping("/*");  // 拦截所有请求路径
                    dynamic.setLoadOnStartup(1);  // 容器启动时加载 Servlet
                }
            });

            // 启动服务器并保存引用
            server.start();
            servers.put(componentId, server);
            serverConfigs.put(componentId, new HttpServerConfig(port, "运行中", new Date(), customHandler != null));

            System.out.println("HTTP 服务器启动成功，端口：" + port);
            return true;

        } catch (Exception e) {
            System.err.println("启动 HTTP 服务器失败，端口：" + port + ", 错误：" + e.getMessage());
            return false;
        }
    }

    /**
     * 停止指定组件的 HTTP 服务器
     * @param componentId 组件唯一标识
     * @return 停止成功返回 true，否则返回 false
     */
    public static boolean stopServer(String componentId) {
        try {
            TomcatWebServer server = servers.get(componentId);
            if (server != null) {
                server.stop();
                servers.remove(componentId);
                serverConfigs.remove(componentId);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 重启 HTTP 服务器（使用新端口）
     * @param componentId 组件唯一标识
     * @param port 新端口号
     * @param needReply 是否需要响应数据
     * @return 重启成功返回 true，否则返回 false
     */
    public static boolean restartServer(String componentId,int port,boolean needReply) {
        HttpRequestHandler handler = getServerHandler(componentId,needReply);
        stopServer(componentId);
        return startServer(componentId,port,needReply, handler);
    }

    /**
     * 获取服务器的请求处理器
     * @param componentId 组件唯一标识
     * @param needReply 是否需要响应数据
     * @return 返回处理器函数，无自定义处理器时返回 null
     */
    private static HttpRequestHandler getServerHandler(String componentId,boolean needReply) {
        HttpServerConfig config = serverConfigs.get(componentId);
        return config != null && config.hasCustomHandler() ?
                (id,context,reply) -> handleDefaultRequest(componentId,context,needReply) : null;
    }

    /**
     * 停止所有正在运行的 HTTP 服务器
     */
    public static void stopAllServers() {
        List<String> ids = new ArrayList<>(servers.keySet());
        for (String id : ids) {
            stopServer(id);
        }
        System.out.println("所有 HTTP 服务器已停止");
    }

    /**
     * 获取所有运行中的服务器配置列表
     * @return 服务器配置列表
     */
    public static List<HttpServerConfig> getRunningServers() {
        return new ArrayList<>(serverConfigs.values());
    }

    /**
     * 检查指定端口的服务器是否在运行
     * @param port 端口号
     * @return 运行中返回 true，否则返回 false
     */
    public static boolean isServerRunning(int port) {
        return servers.containsKey(port);
    }

    /**
     * 获取当前运行的服务器数量
     * @return 服务器数量
     */
    public static int getServerCount() {
        return servers.size();
    }

    /**
     * 默认请求处理器
     * 处理 HTTP 请求并调用消费者服务
     * @param componentId 组件唯一标识
     * @param context 请求上下文
     * @param needReply 是否需要响应数据
     */
    private static void handleDefaultRequest(String componentId,RequestContext context,boolean needReply){
        HttpServletResponse resp = context.getResponse();
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        
        // 根据是否需要响应，选择同步或异步处理方式
        if(needReply){
            try {
                // 同步处理：获取消费者 Bean 处理消息并返回结果
                HttpResData result = SpringUtils.getBean(HttpServerConsumer.class).handleMessage(
                        componentId,
                        context.getRequest().getMethod(),
                        context.getRequest().getRequestURI(),
                        context.getContentType(),
                        context.getHeaders(),
                        context.getQueryParameters(),
                        context.getRequestBody(),
                        MultipartParser.parse(context.requestBody, context.getContentType()),
                        context.getRequest().getRemoteAddr(),
                        context.getRequest().getRemotePort());
                if(result.getHttNeedReply()){
                    resp.getWriter().write(new ObjectMapper().writeValueAsString(result.getData()));
                }
            }catch (Exception ignore){}
        }else {
            // 异步处理：仅发送消息，不等待响应
            SpringUtils.getBean(HttpServerConsumer.class).message(
                    componentId,
                    context.getRequest().getMethod(),
                    context.getRequest().getRequestURI(),
                    context.getContentType(),
                    context.getHeaders(),
                    context.getQueryParameters(),
                    context.getRequestBody(),
                    MultipartParser.parse(context.requestBody,context.getContentType()),
                    context.getRequest().getRemoteAddr(),
                    context.getRequest().getRemotePort());
        }
    }

    /**
     * 从 InputStream 读取字符串（兼容 Java 8）
     * @param inputStream 输入流
     * @return 读取的字符串
     */
    private static String readInputStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    /**
     * 请求上下文类（封装所有数据获取方法）
     * 提供便捷的请求数据访问接口
     */
    public static class RequestContext {
        private final HttpServletRequest request;
        private final HttpServletResponse response;
        private final int port;
        private String requestBody;  // 缓存的请求体
        private Map<String, String> formParameters;  // 缓存的表单参数
        private boolean dataParsed = false;  // 数据是否已解析标记

        public RequestContext(HttpServletRequest request, HttpServletResponse response, int port) {
            this.request = request;
            this.response = response;
            this.port = port;
        }

        /**
         * 获取查询参数（URL 中的参数）
         * 自动处理字符编码转换（ISO_8859_1 -> UTF-8）
         * @return 查询参数 Map
         */
        public Map<String, String> getQueryParameters() {
            Map<String, String> params = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) {
                    // 手动解码参数值，解决中文乱码问题
                    String recodedKey = new String(key.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    String recodedValue = new String(values[0].getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    params.put(recodedKey, recodedValue);
                }
            });
            return params;
        }

        /**
         * 获取表单参数（application/x-www-form-urlencoded）
         * 延迟加载：首次调用时解析请求数据
         * @return 表单参数 Map
         */
        public Map<String, String> getFormParameters() {
            if (!dataParsed) {
                parseRequestData();
            }
            return formParameters != null ? new HashMap<>(formParameters) : new HashMap<>();
        }

        /**
         * 获取请求体数据（raw body）
         * 延迟加载：首次调用时解析请求数据
         * @return 请求体字符串
         */
        public String getRequestBody() {
            if (!dataParsed) {
                parseRequestData();
            }
            return requestBody;
        }

        /**
         * 获取 JSON 格式的请求体
         * @param clazz 目标类型
         * @return 反序列化后的对象
         */
        public <T> T getJsonBody(Class<T> clazz) throws IOException {
            String body = getRequestBody();
            if (body != null && !body.trim().isEmpty()) {
                return new ObjectMapper().readValue(body, clazz);
            }
            return null;
        }

        /**
         * 获取多部分表单数据（multipart/form-data）- 兼容 Java 8
         * 仅获取普通表单字段，不包含文件
         * @return 表单字段 Map
         */
        public Map<String, String> getMultipartFormData() throws IOException, ServletException {
            if (!isMultipartContent()) {
                return new HashMap<>();
            }

            Map<String, String> formData = new HashMap<>();
            for (Part part : request.getParts()) {
                if (part.getContentType() == null) { // 表单字段（非文件）
                    String value = readInputStreamToString(part.getInputStream());
                    formData.put(part.getName(), value);
                }
            }
            return formData;
        }

        /**
         * 获取上传的文件
         * @return 文件 Map，key 为字段名，value 为 Part 对象
         */
        public Map<String, Part> getUploadedFiles() throws IOException, ServletException {
            if (!isMultipartContent()) {
                return new HashMap<>();
            }

            Map<String, Part> files = new HashMap<>();
            for (Part part : request.getParts()) {
                if (part.getContentType() != null && part.getSize() > 0) { // 文件类型
                    files.put(part.getName(), part);
                }
            }
            return files;
        }

        /**
         * 获取所有请求头
         * @return 请求头 Map
         */
        public Map<String, String> getHeaders() {
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }
            return headers;
        }

        /**
         * 获取 Content-Type 请求头
         * @return Content-Type 字符串
         */
        public String getContentType() {
            return request.getContentType();
        }

        /**
         * 检查是否是表单提交
         * 包括 application/x-www-form-urlencoded 和 multipart/form-data
         * @return 是表单提交返回 true
         */
        public boolean isFormSubmit() {
            String contentType = getContentType();
            return contentType != null &&
                    (contentType.contains("application/x-www-form-urlencoded") ||
                            contentType.contains("multipart/form-data"));
        }

        /**
         * 检查是否是 JSON 请求
         * @return 是 JSON 请求返回 true
         */
        public boolean isJsonRequest() {
            String contentType = getContentType();
            return contentType != null && contentType.contains("application/json");
        }

        /**
         * 检查是否是多部分内容（multipart/*）
         * @return 是多部分内容返回 true
         */
        public boolean isMultipartContent() {
            String contentType = getContentType();
            return contentType != null && contentType.startsWith("multipart/");
        }

        /**
         * 解析请求数据（延迟加载）
         * 根据 Content-Type 选择不同的解析方式
         */
        private void parseRequestData() {
            try {
                // 设置请求编码为 UTF-8，这必须在 getReader() 之前调用
                request.setCharacterEncoding("UTF-8");
                if (isFormSubmit() && !isMultipartContent()) {
                    // 对于普通表单，读取 body
                    StringBuilder bodyBuilder = new StringBuilder();
                    BufferedReader reader = request.getReader();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        bodyBuilder.append(line);
                    }
                    requestBody = bodyBuilder.toString();

                    // 获取表单参数 - 手动处理编码
                    formParameters = new HashMap<>();
                    request.setCharacterEncoding("UTF-8");
                    request.getParameterMap().forEach((key, values) -> {
                        if (values.length > 0) {
                            // 手动解码参数值，解决中文乱码
                            String recodedKey = new String(key.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                            String recodedValue = new String(values[0].getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                            formParameters.put(recodedKey, recodedValue);
                        }
                    });
                } else {
                    // 对于其他类型（JSON、XML 等），直接读取 body
                    StringBuilder bodyBuilder = new StringBuilder();
                    BufferedReader reader = request.getReader();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        bodyBuilder.append(line);
                    }
                    requestBody = bodyBuilder.toString();
                }
            } catch (IOException e) {
                requestBody = "";
                formParameters = new HashMap<>();
            }
            dataParsed = true;
        }

        // Getters
        public HttpServletRequest getRequest() { return request; }
        public HttpServletResponse getResponse() { return response; }
        public int getPort() { return port; }
    }

    /**
     * 自定义请求处理器接口（函数式接口）
     * 允许用户自定义 HTTP 请求处理逻辑
     */
    @FunctionalInterface
    public interface HttpRequestHandler {
        /**
         * 处理 HTTP 请求
         * @param componentId 组件 ID
         * @param context 请求上下文
         * @param needReply 是否需要响应
         */
        void handle(String componentId,RequestContext context,boolean needReply) throws IOException, ServletException;
    }

    /**
     * 服务器配置信息类
     * 用于记录和展示服务器运行状态
     */
    public static class HttpServerConfig {
        private int port;  // 服务器端口
        private String status;  // 运行状态
        private Date startTime;  // 启动时间
        private boolean customHandler;  // 是否使用自定义处理器

        public HttpServerConfig(int port, String status, Date startTime, boolean customHandler) {
            this.port = port;
            this.status = status;
            this.startTime = startTime;
            this.customHandler = customHandler;
        }

        // Getters and Setters
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Date getStartTime() { return startTime; }
        public void setStartTime(Date startTime) { this.startTime = startTime; }
        public boolean hasCustomHandler() { return customHandler; }
        public void setCustomHandler(boolean customHandler) { this.customHandler = customHandler; }
    }

    /**
     * 简单的 JSON 序列化工具类（避免外部依赖）
     * 支持基本的 JSON 序列化功能
     */
    private static class ObjectMapper {
        /**
         * 将 Java 对象序列化为 JSON 字符串
         * @param value 要序列化的对象
         * @return JSON 字符串
         */
        public String writeValueAsString(Object value) throws IOException {
            if (value == null) {
                return "null";
            }
            if (value instanceof String) {
                return "\"" + escapeJsonString((String) value) + "\"";
            }
            if (value instanceof Number || value instanceof Boolean) {
                return value.toString();
            }
            if (value instanceof Map) {
                return mapToJson((Map<?, ?>) value);
            }
            if (value instanceof Collection) {
                return collectionToJson((Collection<?>) value);
            }
            if (value instanceof Object[]) {
                return arrayToJson((Object[]) value);
            }
            if (value instanceof Date) {
                return "\"" + value.toString() + "\"";
            }
            return "\"" + value.toString() + "\"";
        }

        /**
         * 将 JSON 字符串反序列化为 Java 对象
         * @param content JSON 字符串
         * @param clazz 目标类型
         * @return 反序列化后的对象
         */
        public <T> T readValue(String content, Class<T> clazz) throws IOException {
            // 简化实现，实际使用时建议使用真实的 Jackson ObjectMapper
            throw new IOException("请使用真实的 JSON 库来实现此方法");
        }

        /**
         * Map 转 JSON 对象
         */
        private String mapToJson(Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    sb.append(",");
                }
                sb.append("\"").append(escapeJsonString(entry.getKey().toString())).append("\":");
                try {
                    sb.append(writeValueAsString(entry.getValue()));
                } catch (IOException e) {
                    sb.append("\"error\"");
                }
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }

        /**
         * Collection 转 JSON 数组
         */
        private String collectionToJson(Collection<?> collection) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : collection) {
                if (!first) {
                    sb.append(",");
                }
                try {
                    sb.append(writeValueAsString(item));
                } catch (IOException e) {
                    sb.append("\"error\"");
                }
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }

        /**
         * 数组转 JSON 数组
         */
        private String arrayToJson(Object[] array) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                try {
                    sb.append(writeValueAsString(array[i]));
                } catch (IOException e) {
                    sb.append("\"error\"");
                }
            }
            sb.append("]");
            return sb.toString();
        }

        /**
         * 转义 JSON 字符串中的特殊字符
         */
        private String escapeJsonString(String str) {
            if (str == null) {
                return "";
            }
            return str.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }

    /**
     * 主方法（用于测试）
     */
    public static void main(String[] args) {
//        startServer("123",9595);
    }
}
