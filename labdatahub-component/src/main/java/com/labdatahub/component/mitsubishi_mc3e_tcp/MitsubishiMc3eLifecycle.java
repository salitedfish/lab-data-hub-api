package com.labdatahub.component.mitsubishi_mc3e_tcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;

/**
 * 三菱 MC 生命周期管理
 */
@Slf4j
@Component
public class MitsubishiMc3eLifecycle {

    @PreDestroy
    public void destroy() {
    	log.info("=== 应用关闭，停止 Mitsubishi 循环消费线程 ===");
    	MitsubishiMc3eLoopConsumer.stopAllConsume();

        log.info("=== 应用关闭，停止 Mitsubishi 消息调度器 ===");
        MitsubishiMc3eMessageScheduler.shutdown();

        log.info("=== 应用关闭，清理 Mitsubishi 连接资源 ===");
        MitsubishiMc3eConnectionManager.closeAllConnections();

        log.info("=== 所有 Mitsubishi 资源已清理完成 ===");
    }
}
