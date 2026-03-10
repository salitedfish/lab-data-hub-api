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

public class HttpServerManager {

    private static final Map<String, TomcatWebServer> servers = new ConcurrentHashMap<>();
    private static final Map<String, HttpServerConfig> serverConfigs = new ConcurrentHashMap<>();

    // 私有构造器，防止实例化
    private HttpServerManager() {}

    /**
     * 动态启动HTTP服务器
     */
    public static boolean startServer(String componentId,int port,boolean needReply) {
        return startServer(componentId,port,needReply, null);
    }

    /**
     * 动态启动HTTP服务器（带自定义处理器）
     */
    public static boolean startServer(String componentId,int port,boolean needReply, HttpRequestHandler customHandler) {
        try {
            if (servers.containsKey(componentId)) {
                System.err.println("端口 " + port + " 已在运行");
                return false;
            }

            if (port < 1 || port > 65535) {
                System.err.println("端口号必须在1-65535之间");
                return false;
            }

            TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
            factory.setPort(port);

            TomcatWebServer server = (TomcatWebServer) factory.getWebServer(new ServletContextInitializer() {
                @Override
                public void onStartup(ServletContext servletContext) {
                    ServletRegistration.Dynamic dynamic = servletContext.addServlet("default", new HttpServlet() {
                        @Override
                        protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
                            String path = req.getRequestURI();
                            // 过滤静态资源请求
                            if (path.endsWith(".ico") || path.endsWith(".css") || path.endsWith(".js")) {
                                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                return;
                            }
                            // 创建请求上下文
                            RequestContext context = new RequestContext(req, resp, port);

                            if (customHandler != null) {
                                customHandler.handle(componentId,context,needReply);
                            } else {
                                handleDefaultRequest(componentId,context,needReply);
                            }
                        }
                    });
                    dynamic.addMapping("/*");
                    dynamic.setLoadOnStartup(1);
                }
            });

            server.start();
            servers.put(componentId, server);
            serverConfigs.put(componentId, new HttpServerConfig(port, "运行中", new Date(), customHandler != null));

            System.out.println("HTTP服务器启动成功，端口: " + port);
            return true;

        } catch (Exception e) {
            System.err.println("启动HTTP服务器失败，端口: " + port + ", 错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 停止HTTP服务器
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
     * 重启HTTP服务器
     */
    public static boolean restartServer(String componentId,int port,boolean needReply) {
        HttpRequestHandler handler = getServerHandler(componentId,needReply);
        stopServer(componentId);
        return startServer(componentId,port,needReply, handler);
    }

    /**
     * 获取服务器处理器
     */
    private static HttpRequestHandler getServerHandler(String componentId,boolean needReply) {
        HttpServerConfig config = serverConfigs.get(componentId);
        return config != null && config.hasCustomHandler() ?
                (id,context,reply) -> handleDefaultRequest(componentId,context,needReply) : null;
    }

    /**
     * 停止所有服务器
     */
    public static void stopAllServers() {
        List<String> ids = new ArrayList<>(servers.keySet());
        for (String id : ids) {
            stopServer(id);
        }
        System.out.println("所有HTTP服务器已停止");
    }

    /**
     * 获取所有运行的服务器
     */
    public static List<HttpServerConfig> getRunningServers() {
        return new ArrayList<>(serverConfigs.values());
    }

    /**
     * 检查端口是否在运行
     */
    public static boolean isServerRunning(int port) {
        return servers.containsKey(port);
    }

    /**
     * 获取服务器数量
     */
    public static int getServerCount() {
        return servers.size();
    }

    /**
     * 默认请求处理器
     */
    private static void handleDefaultRequest(String componentId,RequestContext context,boolean needReply){
        HttpServletResponse resp = context.getResponse();
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(HttpServletResponse.SC_OK);
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", "success");
//        response.put("message", "HTTP服务器运行中");
//        response.put("port", context.getPort());
//        response.put("path", context.getRequest().getRequestURI());
//        response.put("method", context.getRequest().getMethod());
//        response.put("timestamp", new Date());
//        response.put("server", "HttpServerManager");
//
//        // 添加各种数据到响应中
//        response.put("query_parameters", context.getQueryParameters());
//        response.put("form_parameters", context.getFormParameters());
//        response.put("request_body", context.getRequestBody());
//        response.put("headers", context.getHeaders());
//        response.put("content_type", context.getContentType());
//        response.put("is_form_submit", context.isFormSubmit());
//        response.put("is_json_request", context.isJsonRequest());
//        response.put("is_multipart", context.isMultipartContent());
//        response.put("multipart_data",MultipartParser.parse(context.requestBody,context.getContentType()));
        if(needReply){
            try {
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
     * 从InputStream读取字符串（兼容Java 8）
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
     */
    public static class RequestContext {
        private final HttpServletRequest request;
        private final HttpServletResponse response;
        private final int port;
        private String requestBody;
        private Map<String, String> formParameters;
        private boolean dataParsed = false;

        public RequestContext(HttpServletRequest request, HttpServletResponse response, int port) {
            this.request = request;
            this.response = response;
            this.port = port;
        }

        /**
         * 获取查询参数（URL中的参数）
         */
        public Map<String, String> getQueryParameters() {
            Map<String, String> params = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) {
                    // 手动解码参数值
                    String recodedKey = new String(key.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    String recodedValue = new String(values[0].getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    params.put(recodedKey, recodedValue);
                }
            });
            return params;
        }

        /**
         * 获取表单参数（application/x-www-form-urlencoded）
         */
        public Map<String, String> getFormParameters() {
            if (!dataParsed) {
                parseRequestData();
            }
            return formParameters != null ? new HashMap<>(formParameters) : new HashMap<>();
        }

        /**
         * 获取请求体数据（raw body）
         */
        public String getRequestBody() {
            if (!dataParsed) {
                parseRequestData();
            }
            return requestBody;
        }

        /**
         * 获取JSON格式的请求体
         */
        public <T> T getJsonBody(Class<T> clazz) throws IOException {
            String body = getRequestBody();
            if (body != null && !body.trim().isEmpty()) {
                return new ObjectMapper().readValue(body, clazz);
            }
            return null;
        }

        /**
         * 获取多部分表单数据（multipart/form-data）- 兼容Java 8
         */
        public Map<String, String> getMultipartFormData() throws IOException, ServletException {
            if (!isMultipartContent()) {
                return new HashMap<>();
            }

            Map<String, String> formData = new HashMap<>();
            for (Part part : request.getParts()) {
                if (part.getContentType() == null) { // 表单字段
                    String value = readInputStreamToString(part.getInputStream());
                    formData.put(part.getName(), value);
                }
            }
            return formData;
        }

        /**
         * 获取上传的文件
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
         * 获取请求头
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
         * 获取Content-Type
         */
        public String getContentType() {
            return request.getContentType();
        }

        /**
         * 检查是否是表单提交
         */
        public boolean isFormSubmit() {
            String contentType = getContentType();
            return contentType != null &&
                    (contentType.contains("application/x-www-form-urlencoded") ||
                            contentType.contains("multipart/form-data"));
        }

        /**
         * 检查是否是JSON请求
         */
        public boolean isJsonRequest() {
            String contentType = getContentType();
            return contentType != null && contentType.contains("application/json");
        }

        /**
         * 检查是否是多部分内容
         */
        public boolean isMultipartContent() {
            String contentType = getContentType();
            return contentType != null && contentType.startsWith("multipart/");
        }

        /**
         * 解析请求数据
         */
        private void parseRequestData() {
            try {
                // 设置请求编码为UTF-8，这必须在getReader()之前调用
                request.setCharacterEncoding("UTF-8");
                if (isFormSubmit() && !isMultipartContent()) {
                    // 对于普通表单，读取body
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
                            // 手动解码参数值
                            String recodedKey = new String(key.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                            String recodedValue = new String(values[0].getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                            formParameters.put(recodedKey, recodedValue);
                        }
                    });
                } else {
                    // 对于其他类型，直接读取body
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
     * 自定义请求处理器接口
     */
    @FunctionalInterface
    public interface HttpRequestHandler {
        void handle(String componentId,RequestContext context,boolean needReply) throws IOException, ServletException;
    }

    /**
     * 服务器配置信息
     */
    public static class HttpServerConfig {
        private int port;
        private String status;
        private Date startTime;
        private boolean customHandler;

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
     * 简单的JSON序列化工具（避免依赖）
     */
    private static class ObjectMapper {
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

        public <T> T readValue(String content, Class<T> clazz) throws IOException {
            // 简化实现，实际使用时建议使用真实的Jackson ObjectMapper
            throw new IOException("请使用真实的JSON库来实现此方法");
        }

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

    public static void main(String[] args) {
//        startServer("123",9595);
    }
}