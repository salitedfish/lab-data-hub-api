package com.labdatahub.component.modbus_tcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;

/**
 * Modbus 生命周期管理
 */
@Slf4j
@Component
public class ModbusLifecycle {
    
    @PreDestroy
    public void destroy() {
    	log.info("=== 应用关闭，停止 Modbus 循环消费线程 ===");
        ModbusLoopConsumer.stopAllConsume();
        
        log.info("=== 应用关闭，停止 Modbus 消息调度器 ===");
        ModbusMessageScheduler.shutdown();
        
        log.info("=== 应用关闭，清理 Modbus 连接资源 ===");
        ModbusConnectionManager.closeAllConnections();
        
        log.info("=== 所有 Modbus 资源已清理完成 ===");
    }
}
