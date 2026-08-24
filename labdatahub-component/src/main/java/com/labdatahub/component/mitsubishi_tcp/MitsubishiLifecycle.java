package com.labdatahub.component.mitsubishi_tcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;

/**
 * 三菱 MC 生命周期管理
 */
@Slf4j
@Component
public class MitsubishiLifecycle {

    @PreDestroy
    public void destroy() {
    	log.info("=== 应用关闭，停止 Mitsubishi 循环消费线程 ===");
    	MitsubishiLoopConsumer.stopAllConsume();

        log.info("=== 应用关闭，停止 Mitsubishi 消息调度器 ===");
        MitsubishiMessageScheduler.shutdown();

        log.info("=== 应用关闭，清理 Mitsubishi 连接资源 ===");
        MitsubishiConnectionManager.closeAllConnections();

        log.info("=== 所有 Mitsubishi 资源已清理完成 ===");
    }
}
