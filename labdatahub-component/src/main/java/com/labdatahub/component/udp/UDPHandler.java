package com.labdatahub.component.udp;

import java.lang.reflect.InvocationTargetException;

/**
 * UDP消息处理器接口
 */
public interface UDPHandler {
    /**
     * 当收到UDP消息时调用
     * @param clientSign 客户端地址 (IP:端口)
     * @param message 接收到的消息
     * @return 要发送的响应消息，返回null或空字符串则不响应
     */
    void onMessageReceived(String componentId,String clientSign, String message) throws InvocationTargetException, IllegalAccessException;

    /**
     * 当发生错误时调用
     */
    default void onError(String clientAddress, Exception e) {
        System.err.println("处理客户端 " + clientAddress + " 消息时出错: " + e.getMessage());
    }
}