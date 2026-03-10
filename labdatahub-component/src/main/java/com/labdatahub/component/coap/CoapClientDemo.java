package com.labdatahub.component.coap;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.elements.config.Configuration;

import java.net.URI;
import java.nio.charset.StandardCharsets;

public class CoapClientDemo {
    static {
        // 关键：注册配置模块
        CoapConfig.register();
    }
    public static void main(String[] args) throws Exception {
        hello();
    }

    public static void hello() throws Exception {
        URI uri = new URI("localhost:5683/hello?id=123");
        CoapClient coapClient = new CoapClient(uri);
        // 使用Request builder
        Request postRequest = new Request(CoAP.Code.POST);
        postRequest.setPayload("{\n" +
                "  \"temperature\":11.5,\n" +
                "  \"windSpeed\":123,\n" +
                "  \"deviceSn\": \"IOT-device01\"\n" +
                "}");
        postRequest.setOptions(new OptionSet()
                .setContentFormat(MediaTypeRegistry.TEXT_PLAIN)
                .addUriQuery("id=123"));
        coapClient.advanced(postRequest);
        coapClient.shutdown();
    }

    public static void time() throws Exception {
        URI uri = new URI("localhost:5683/time");
        CoapClient coapClient = new CoapClient(uri);
        CoapResponse response = coapClient.get();
        if(response !=null){
            // 打印格式良好的输出
            System.out.println(response.getResponseText());
        }
    }

}

