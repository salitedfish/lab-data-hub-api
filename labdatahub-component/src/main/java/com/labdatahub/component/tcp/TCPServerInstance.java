package com.labdatahub.component.tcp;

import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.common.utils.uuid.UUID;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP服务器实例
 */
public class TCPServerInstance implements Runnable {
    private final int port;
    private final String id;
    private final ServerHandler handler;
    private ServerSocket serverSocket;
    private String delimiter;
    private Integer cacheSize;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final AtomicBoolean isStopped = new AtomicBoolean(false);
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private Thread serverThread;

    public TCPServerInstance(String id, int port, String delimiter, Integer cacheSize, ServerHandler handler) throws IOException {
        this.id = id;
        this.port = port;
        this.handler = handler;
        this.delimiter = delimiter;
        this.cacheSize = cacheSize;
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        serverThread = Thread.currentThread();
        System.out.println("TCP服务器开始监听端口: " + port);

        try {
            while (isRunning.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    // 检查是否正在停止
                    if (!isRunning.get()) {
                        clientSocket.close();
                        break;
                    }

                    String clientId = UUID.randomUUID().toString();
                    if (cacheSize != null) {
                        clientSocket.setReceiveBufferSize(cacheSize);
                    }
                    ClientHandler clientHandler = new ClientHandler(id, clientSocket, clientId, handler, delimiter);
                    clients.put(clientId, clientHandler);

                    // 在线程池中处理客户端连接
                    ThreadPoolTaskExecutor executor = SpringUtils.getBean(ThreadPoolTaskExecutor.class);
                    if (executor != null) {
                        executor.execute(clientHandler);
                    } else {
                        // 如果获取不到线程池，创建新线程处理
                        new Thread(clientHandler).start();
                    }

                } catch (SocketException e) {
                    // 如果服务器正在停止，忽略此异常
                    if (isRunning.get()) {
                        System.err.println("服务器Socket异常，端口: " + port + ", 错误: " + e.getMessage());
                    }
                    break;
                }
            }
        } catch (IOException e) {
            if (isRunning.get()) {
                System.err.println("服务器异常，端口: " + port + ", 错误: " + e.getMessage());
            }
        } finally {
            closeServer();
            isStopped.set(true);
            System.out.println("TCP服务器端口 " + port + " 线程已退出");
        }
    }

    /**
     * 停止服务器
     */
    public void stop() {
        if (!isRunning.get()) {
            return; // 已经停止
        }

        System.out.println("正在停止TCP服务器，端口: " + port);
        isRunning.set(false);

        // 关闭ServerSocket，这会导致accept()抛出异常，从而退出循环
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("关闭ServerSocket时出错: " + e.getMessage());
            }
        }

        // 关闭所有客户端连接
        closeAllClients();

        // 如果需要等待线程结束，可以调用waitForStop()
    }

    /**
     * 等待服务器完全停止
     */
    public void waitForStop(long timeoutMillis) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (!isStopped.get() && (System.currentTimeMillis() - startTime) < timeoutMillis) {
            Thread.sleep(100);
        }
    }

    /**
     * 关闭服务器和所有客户端连接
     */
    private void closeServer() {
        // 关闭所有客户端连接
        closeAllClients();

        // 关闭服务器Socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("关闭服务器Socket时出错，端口: " + port + ", 错误: " + e.getMessage());
            }
        }
    }

    /**
     * 向指定客户端发送消息
     */
    public void sendToClient(String clientId, String message) {
        ClientHandler client = clients.get(clientId);
        if (client != null) {
            client.sendMessage(message);
        }
    }

    /**
     * 广播消息给所有客户端
     */
    public void broadcast(String message) {
        clients.forEach((clientId, client) -> client.sendMessage(message));
    }

    /**
     * 通过clientId获取客户端
     */
    public ClientHandler getClientByClientId(String clientId) {
        return clients.get(clientId);
    }

    /**
     * 关闭所有客户端连接
     */
    public void closeAllClients() {
        clients.forEach((id, client) -> {
            try {
                client.close();
            } catch (Exception e) {
                System.err.println("关闭客户端 " + id + " 时出错: " + e.getMessage());
            }
        });
        clients.clear();
    }

    /**
     * 移除客户端（由ClientHandler在关闭时调用）
     */
    public void removeClient(String clientId) {
        clients.remove(clientId);
    }

    /**
     * 获取连接客户端数量
     */
    public int getClientCount() {
        return clients.size();
    }

    public int getPort() {
        return port;
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public boolean isStopped() {
        return isStopped.get();
    }

    public String getId() {
        return id;
    }
}