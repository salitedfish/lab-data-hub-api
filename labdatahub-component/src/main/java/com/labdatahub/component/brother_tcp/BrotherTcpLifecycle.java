//由AI修改
package com.labdatahub.component.brother_tcp;

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Brother NC 生命周期管理
 */
@Slf4j
@Component
public class BrotherTcpLifecycle {

    @PreDestroy
    public void destroy() {
        log.info("=== 应用关闭，停止 Brother 循环消费线程 ===");
        BrotherTcpLoopConsumer.stopAllConsume();

        log.info("=== 应用关闭，停止 Brother 消息调度器 ===");
        BrotherTcpMessageScheduler.shutdown();

        log.info("=== 应用关闭，清理 Brother 连接资源 ===");
        BrotherTcpConnectionManager.closeAllConnections();

        log.info("=== 所有 Brother 资源已清理完成 ===");
    }
}
