package com.labdatahub.component.tcp;

import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.event.EventBus;

import static com.labdatahub.component.tcp.TCPClientManager.CLIENT_DEVICE;


/**
 * @Description: TCP服务处理类
 * @Author: labdatahub
 * @CreateTime: 2025-10-14
 */
public class TCPServerHandlerInstance implements ServerHandler{
    @Override
    public void onMessageReceived(String componentId,String clientId, String message) {
        SpringUtils.getBean(TCPServerConsumer.class).message(componentId,clientId,message);
    }

    @Override
    public void onClientConnected(String clientId) {
        System.out.println("连接");
    }

    @Override
    public void onClientDisconnected(String clientId) {
        if(CLIENT_DEVICE.getOrDefault(clientId,null)!=null){
            SpringUtils.getBean(EventBus.class).publish("device.offline",CLIENT_DEVICE.get(clientId));
        }
    }
}
