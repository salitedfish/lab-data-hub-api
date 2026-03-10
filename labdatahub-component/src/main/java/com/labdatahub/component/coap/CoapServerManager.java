package com.labdatahub.component.coap;

import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.message.DecodeMessage;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.server.MessageDeliverer;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.elements.config.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.labdatahub.component.coap.CoapCache.DEVICE_SERVER;

public class CoapServerManager {
    static {
        // 关键：注册配置模块
        CoapConfig.register();
    }
    public static final Map<String,CoapServer> COAP_SERVER_MAP = new HashMap<>();

    /**
     * 新增COAP服务端
     * @param id
     * @param port
     * @param tokenConfig 配置的token
     */
    public static boolean addServer(String id,int port,boolean isAuth,String tokenConfig){
        try {
            // 手动创建配置，避免文件加载警告
            Configuration config = new Configuration()
                    .set(CoapConfig.COAP_PORT, port);
            CoapServer coapServer = new CoapServer(config);
            coapServer.setMessageDeliverer(new MessageDeliverer() {
                @Override
                public void deliverRequest(Exchange exchange) {
                    Request request = exchange.getRequest();
                    String messageToken = new String(request.getTokenBytes());
                    //如果开启了鉴权并且鉴权不通过则不处理
                    if (isAuth) {
                        if (!tokenConfig.equals(messageToken)) {
                            return;
                        }
                    }
                    String path = request.getOptions().getUriPathString();
                    CoAP.Code method = request.getCode();
                    String payload = request.getPayloadString();
                    List<String> pathParams = request.getOptions().getUriQuery();
                    String address = request.getSourceContext().getPeerAddress().getHostString();
                    int port = request.getSourceContext().getPeerAddress().getPort();
                    try {
                        DecodeMessage decodeMessage = SpringUtils.getBean(CoapServerConsumer.class).message(id, method.name(), path, payload, pathParams);
                        if (decodeMessage != null && decodeMessage.getCoapIsRecover()) {
                            DEVICE_SERVER.put(decodeMessage.getDeviceSn(), address + "_" + port);
                            // 创建并发送响应
                            Response response = new Response(CoAP.ResponseCode.CONTENT);
                            response.setPayload(decodeMessage.getCoapRecoverContent());
                            exchange.sendResponse(response);
                        }
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void deliverResponse(Exchange exchange, Response response) {
                }
            });
            coapServer.start();
            COAP_SERVER_MAP.put(id, coapServer);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 停用组件
     */
    public static void stopServer(String id){
        CoapServer coapServer = COAP_SERVER_MAP.getOrDefault(id,null);
        if(coapServer!=null){
            coapServer.stop();
            COAP_SERVER_MAP.remove(id);
        }
    }
}

