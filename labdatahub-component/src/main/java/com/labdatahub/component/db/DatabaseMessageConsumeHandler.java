package com.labdatahub.component.db;

/**
 * 
* @ClassName: DatabaseMessageConsumeHandler  
* @Description: 消费处理接口
* @author xwb  
* @date 2026年4月1日
 */
@FunctionalInterface
public interface DatabaseMessageConsumeHandler {
    void handle(String componentId, DatabaseMessage message) throws Exception;
}