package com.labdatahub.component.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * 
* @ClassName: DatabaseLifecycle  
* @Description: 生命周期管理
* @author xwb  
* @date 2026年4月1日
 */
@Slf4j
@Component
public class DatabaseLifecycle {

    @PreDestroy
    public void destroy() {
        log.info("=== 应用关闭，停止数据库循环消费线程 ===");
        DatabaseLoopConsumer.stopAllConsume();

        log.info("=== 应用关闭，停止数据库消息调度器 ===");
        DatabaseMessageScheduler.shutdown();

        log.info("=== 应用关闭，清理数据库连接资源 ===");
        DatabaseConnectionManager.closeAllDataSources();

        log.info("=== 所有数据库资源已清理完成 ===");
    }
}