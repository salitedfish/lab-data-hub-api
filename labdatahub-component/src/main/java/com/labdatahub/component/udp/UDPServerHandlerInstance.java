package com.labdatahub.component.udp;

import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.tcp.ServerHandler;
import com.labdatahub.component.tcp.TCPServerConsumer;

import java.lang.reflect.InvocationTargetException;

/**
 * @Description: UDP服务处理类
 * @Author: labdatahub
 * @CreateTime: 2025-10-14
 */
public class UDPServerHandlerInstance implements UDPHandler {
    @Override
    public void onMessageReceived(String componentId,String clientSign, String message) {
        SpringUtils.getBean(UDPServerConsumer.class).message(componentId,clientSign,message);
    }
}
