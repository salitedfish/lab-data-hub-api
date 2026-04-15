package com.labdatahub.component.fins_tcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;

/**
 * Fins 生命周期管理
 */
@Slf4j
@Component
public class FinsLifecycle {
    
    @PreDestroy
    public void destroy() {
    	log.info("=== 应用关闭，停止 Fins 循环消费线程 ===");
    	FinsLoopConsumer.stopAllConsume();
        
        log.info("=== 应用关闭，停止 Fins 消息调度器 ===");
        FinsMessageScheduler.shutdown();
        
        log.info("=== 应用关闭，清理 Fins 连接资源 ===");
        FinsConnectionManager.closeAllConnections();
        
        log.info("=== 所有 Fins 资源已清理完成 ===");
    }
}
