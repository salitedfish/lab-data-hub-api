package com.labdatahub.component.db;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
* @ClassName: DatabaseLoopConsumer  
* @Description: 循环消费者   
* @author xwb  
* @date 2026年4月1日
 */
@Slf4j
public class DatabaseLoopConsumer {

    // 存储已启动的消费线程：key = datasourceId
    private static final Map<String, ConsumeThread> consumeThreadMap = new ConcurrentHashMap<>();
    private static final String THREAD_NAME_PREFIX = "db-loop-consumer-";
    private static final long STOP_TIMEOUT_MS = 5000L;

    private static class ConsumeThread extends Thread {
        private final String datasourceId;
        private final DatabaseMessageConsumeHandler handler;
        private final AtomicBoolean isRunning = new AtomicBoolean(true);

        public ConsumeThread(String datasourceId, DatabaseMessageConsumeHandler handler) {
            super(THREAD_NAME_PREFIX + datasourceId);
            this.datasourceId = datasourceId;
            this.handler = handler;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            log.info("启动数据库消费线程 datasourceId={}", datasourceId);
            while (isRunning.get()) {
                try {
                    DatabaseMessage message = DatabaseMessageScheduler.takeMessage(datasourceId);
                    handler.handle(datasourceId, message);
                } catch (InterruptedException e) {
                    log.info("datasourceId={} 消费线程被中断，准备停止", datasourceId);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("datasourceId={} 消费消息异常", datasourceId, e);
                }
            }
            consumeThreadMap.remove(datasourceId);
            log.info("数据库消费线程已停止 datasourceId={}", datasourceId);
        }

        public void stopConsume() {
            isRunning.set(false);
            this.interrupt();
        }

        public boolean isConsuming() {
            return isRunning.get() && this.isAlive();
        }
    }

    /**
     * 启动指定数据源的循环消费
     */
    public static void startConsume(String datasourceId, DatabaseMessageConsumeHandler handler) {
        if (StringUtils.isBlank(datasourceId) || handler == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (consumeThreadMap.containsKey(datasourceId)) {
            ConsumeThread exist = consumeThreadMap.get(datasourceId);
            if (exist.isConsuming()) {
                throw new IllegalStateException("datasourceId=" + datasourceId + " 已启动消费");
            } else {
                consumeThreadMap.remove(datasourceId);
            }
        }
        ConsumeThread thread = new ConsumeThread(datasourceId, handler);
        consumeThreadMap.put(datasourceId, thread);
        thread.start();
    }

    /**
     * 停止指定数据源的循环消费
     */
    public static void stopConsume(String datasourceId) {
        if (StringUtils.isBlank(datasourceId)) {
            return;
        }
        ConsumeThread thread = consumeThreadMap.get(datasourceId);
        if (thread != null && thread.isConsuming()) {
            thread.stopConsume();
            try {
                thread.join(STOP_TIMEOUT_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("等待 datasourceId={} 消费线程停止时被中断", datasourceId, e);
            }
        }
    }

    /**
     * 检查是否正在消费
     */
    public static boolean isConsuming(String datasourceId) {
        if (StringUtils.isBlank(datasourceId)) {
            return false;
        }
        ConsumeThread thread = consumeThreadMap.get(datasourceId);
        return thread != null && thread.isConsuming();
    }

    /**
     * 停止所有消费线程
     */
    public static void stopAllConsume() {
        List<String> ids = new ArrayList<>(consumeThreadMap.keySet());
        for (String id : ids) {
            stopConsume(id);
        }
        consumeThreadMap.clear();
        log.info("所有数据库消费线程已停止");
    }
}