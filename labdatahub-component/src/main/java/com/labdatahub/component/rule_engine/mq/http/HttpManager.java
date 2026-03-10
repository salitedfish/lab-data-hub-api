package com.labdatahub.component.rule_engine.mq.http;

import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.spring.SpringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: ruoyi
 * @CreateTime: 2025-10-31
 */
public class HttpManager {

    public static final Map<String,HttpConfig> HTTP_CONFIG = new HashMap<>();

    public static boolean addConfig(String id,HttpConfig httpConfig){
        HTTP_CONFIG.put(id,httpConfig);
        return true;
    }

    public static boolean removeConfig(String id){
        HTTP_CONFIG.remove(id);
        return true;
    }

    public static boolean sendMessage(String id, String message) {
        HttpConfig config = HTTP_CONFIG.get(id);
        if (config == null) {
            return false;
        }
        try {
            RestTemplate restTemplate = SpringUtils.getBean(RestTemplate.class);
            HttpHeaders headers = new HttpHeaders();
            // 设置请求头 Content-Type
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 添加认证请求头（如果存在）
            if (StringUtils.hasText(config.getAuthHeaderSign())&&StringUtils.hasText(config.getAuthToken())) {
                headers.set(config.getAuthHeaderSign(), config.getAuthToken());
            }
            HttpEntity<String> requestEntity = new HttpEntity<>(message, headers);
            ResponseEntity<String> response;
            if ("POST".equals(config.getMethod())) {
                response = restTemplate.postForEntity(config.getUrl(), requestEntity, String.class);
            } else if ("PUT".equals(config.getMethod())) {
                restTemplate.put(config.getUrl(), requestEntity);
                return true; // PUT请求没有返回值，默认认为成功
            } else {
                // 如果不是POST或PUT，返回false
                return false;
            }
            // 检查HTTP状态码，2xx表示成功
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            // 记录日志
            // log.error("发送HTTP请求失败, id: {}, url: {}", id, config.getUrl(), e);
            return false;
        }
    }
}
