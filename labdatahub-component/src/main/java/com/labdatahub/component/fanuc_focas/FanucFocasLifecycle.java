//由AI修改
package com.labdatahub.component.fanuc_focas;

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * FANUC FOCAS2 生命周期管理
 */
@Slf4j
@Component
public class FanucFocasLifecycle {

    @PreDestroy
    public void destroy() {
        log.info("=== 应用关闭，停止 FANUC FOCAS2 循环消费线程 ===");
        FanucFocasLoopConsumer.stopAllConsume();

        log.info("=== 应用关闭，停止 FANUC FOCAS2 消息调度器 ===");
        FanucFocasMessageScheduler.shutdown();

        log.info("=== 应用关闭，清理 FANUC FOCAS2 连接资源 ===");
        FanucFocasConnectionManager.closeAllConnections();

        log.info("=== 所有 FANUC FOCAS2 资源已清理完成 ===");
    }
}
