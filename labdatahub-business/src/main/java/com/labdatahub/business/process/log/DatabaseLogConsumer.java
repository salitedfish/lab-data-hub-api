package com.labdatahub.business.process.log;

import com.labdatahub.business.domain.LabdatahubDeviceLogs;
import com.labdatahub.business.service.ILabdatahubDeviceLogsService;

import java.util.List;

/**
 * 数据库日志消费者
 */
public class DatabaseLogConsumer implements LogConsumer {

    // 你可以在这里注入你的数据库服务
    private final ILabdatahubDeviceLogsService logsService;

    public DatabaseLogConsumer(ILabdatahubDeviceLogsService logsService) {
        this.logsService = logsService;
    }

    @Override
    public void consume(List<LabdatahubDeviceLogs> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }

        try {
            // 这里实现你的数据库批量插入逻辑
            boolean success = logsService.saveBatch(logs);

            if (!success) {
                System.err.println("Failed to insert " + logs.size() + " logs");
                // 可以添加重试逻辑
                retryInsert(logs);
            }

        } catch (Exception e) {
            System.err.println("Error consuming logs: " + e.getMessage());
            // 异常处理：记录到文件或降级处理
            fallback(logs);
        }
    }

    private void retryInsert(List<LabdatahubDeviceLogs> logs) {
        // 简单重试逻辑
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(1000 * (i + 1)); // 指数退避
                if (logsService.saveBatch(logs)) {
                    return;
                }
            } catch (Exception e) {
                System.err.println("Retry " + (i + 1) + " failed: " + e.getMessage());
            }
        }
    }

    private void fallback(List<LabdatahubDeviceLogs> logs) {
        // 降级处理：单条插入或记录到文件
        for (LabdatahubDeviceLogs log : logs) {
            try {
                logsService.save(log);
            } catch (Exception e) {
                System.err.println("Fallback failed for log: " + log);
            }
        }
    }
}
