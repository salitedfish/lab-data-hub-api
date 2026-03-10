package com.labdatahub.component.udp;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * UDP服务器实例
 */
class UDPServerInstance implements Runnable {
    private final int port;
    private final String id;
    private final UDPHandler handler;
    private DatagramSocket socket;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final byte[] buffer = new byte[1024]; // 接收缓冲区

    public UDPServerInstance(String id,int port, UDPHandler handler) throws SocketException {
        this.port = port;
        this.id = id;
        this.handler = handler;
        this.socket = new DatagramSocket(port);
        this.socket.setSoTimeout(1000); // 设置超时，便于优雅退出
    }

    @Override
    public void run() {

        try {
            while (isRunning.get()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    // 处理接收到的数据
                    String message = new String(packet.getData(), 0, packet.getLength());

                    try {
                        // 调用处理器
                        handler.onMessageReceived(id,packet.getAddress().getHostAddress()+"_"+packet.getPort(), message);
                    }catch (Exception e){
                        System.err.println("消息解析失败");
                    }

                } catch (java.net.SocketTimeoutException e) {
                    // 超时是正常的，用于检查是否应该停止
                } catch (IOException e) {
                    if (isRunning.get()) {
                        System.err.println("接收数据错误，端口: " + port + ", 错误: " + e.getMessage());
                    }
                }
            }
        } finally {
//            closeServer();
        }
    }

    /**
     * 发送响应
     */
    private void sendResponse(String response, InetAddress address, int port) {
        try {
            byte[] responseData = response.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(
                    responseData, responseData.length, address, port);
            socket.send(responsePacket);
        } catch (IOException e) {
            System.err.println("发送响应失败: " + e.getMessage());
        }
    }

    /**
     * 发送消息到指定地址
     */
    public boolean sendMessage(String message, String targetHost, int targetPort) {
        try {
            InetAddress address = InetAddress.getByName(targetHost);
            byte[] messageData = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                    messageData, messageData.length, address, targetPort);
            socket.send(packet);

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 广播消息
     */
    public void broadcast(String message, int broadcastPort) {
        try {
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            byte[] messageData = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                    messageData, messageData.length, broadcastAddress, broadcastPort);
            socket.send(packet);

            System.out.println("广播消息已发送到端口: " + broadcastPort);
        } catch (IOException e) {
            System.err.println("广播消息失败: " + e.getMessage());
        }
    }

    /**
     * 停止服务器
     */
    public void stop() {
        isRunning.set(false);
        closeServer();
    }

    /**
     * 关闭服务器
     */
    private void closeServer() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public int getPort() {
        return port;
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}
