//由AI修改
package com.labdatahub.component.mitsubishi_cnc_tcp;

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 三菱 CNC TCP（MOCHA）生命周期管理
 */
@Slf4j
@Component
public class MitsubishiCncLifecycle {

    @PreDestroy
    public void destroy() {
        log.info("=== 应用关闭，停止 Mitsubishi CNC 循环消费线程 ===");
        MitsubishiCncLoopConsumer.stopAllConsume();

        log.info("=== 应用关闭，停止 Mitsubishi CNC 消息调度器 ===");
        MitsubishiCncMessageScheduler.shutdown();

        log.info("=== 应用关闭，清理 Mitsubishi CNC 连接资源 ===");
        MitsubishiCncConnectionManager.closeAllConnections();

        log.info("=== 所有 Mitsubishi CNC 资源已清理完成 ===");
    }
}
