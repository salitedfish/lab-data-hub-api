package com.labdatahub.component.s7_tcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;

/**
 * S7 生命周期管理
 */
@Slf4j
@Component
public class S7Lifecycle {
    
    @PreDestroy
    public void destroy() {
    	log.info("=== 应用关闭，停止 S7 循环消费线程 ===");
        S7LoopConsumer.stopAllConsume();
        
        log.info("=== 应用关闭，停止 S7 消息调度器 ===");
        S7MessageScheduler.shutdown();
        
        log.info("=== 应用关闭，清理 S7 连接资源 ===");
        S7ConnectionManager.closeAllConnections();
        
        log.info("=== 所有 S7 资源已清理完成 ===");
    }
}
