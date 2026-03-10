package com.labdatahub.component.udp;

import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.component.tcp.ServerHandler;
import com.labdatahub.component.tcp.TCPServerInstance;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UDP服务器管理器 - 支持动态添加和管理多个UDP服务器
 */
public class UDPServerManager {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private static UDPServerManager instance;

    public static Map<String, UDPServerInstance> udpServerInstanceMap = new HashMap<>();

    private UDPServerManager() {}

    public static synchronized UDPServerManager getInstance() {
        if (instance == null) {
            instance = new UDPServerManager();
        }
        return instance;
    }

    /**
     * 动态添加UDP服务器 - 静态方法
     */
    public static boolean addServer(String id,int port, UDPHandler handler) {
        return getInstance().addServerInstance(id,port, handler);
    }
    /**
     * 实例方法 - 供静态方法内部调用
     */
    private boolean addServerInstance(String id,int port, UDPHandler handler) {

        try {
            UDPServerInstance serverInstance = new UDPServerInstance(id,port, handler);
            udpServerInstanceMap.put(id, serverInstance);
            executor.execute(serverInstance);
            System.out.println("UDP服务器启动成功，端口: " + port);
            return true;
        } catch (SocketException e) {
            System.err.println("启动UDP服务器失败，端口: " + port + ", 错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 移除UDP服务器
     */
    public static boolean removeServer(String id) {
        return getInstance().removeServerInstance(id);
    }

    private boolean removeServerInstance(String id) {
        UDPServerInstance server = udpServerInstanceMap.get(id);;
        if (server != null) {
            server.stop();
            udpServerInstanceMap.remove(id);
            return true;
        }
        return false;
    }
    /**
     * 获取所有运行的服务器端口
     */
    public java.util.Set<String> getRunningServers() {
        return udpServerInstanceMap.keySet();
    }

    /**
     * 停止所有服务器
     */
    public void stopAllServers() {
        getInstance().stopAllServersInstance();
    }
    private void stopAllServersInstance() {
        udpServerInstanceMap.forEach((port, server) -> server.stop());
        udpServerInstanceMap.clear();
        executor.shutdown();
        System.out.println("所有TCP服务器已停止");
    }
    /**
     * 获取服务器实例
     */
    public static UDPServerInstance getServerInstance(String id) {
        return udpServerInstanceMap.get(id);
    }

    /**
     * 发送消息到客户端
     */
    public static boolean sendMessageToDevice(String deviceSn,String message){
        String address = UDPClientManager.DEVICE_CLIENT.get(deviceSn);
        String componentId = UDPClientManager.CLIENT_SERVER.get(address);
        if(StringUtils.isEmpty(address)||StringUtils.isEmpty(componentId)){
            return false;
        }
        String[] ipPort = address.split("_");
        UDPServerInstance serverInstance = getServerInstance(componentId);
        return serverInstance.sendMessage(message,ipPort[0],Integer.parseInt(ipPort[1]));
    }

}