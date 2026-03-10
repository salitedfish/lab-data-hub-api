package com.labdatahub.business.process.log;

import com.labdatahub.business.domain.LabdatahubDeviceLogs;

import java.util.List;

/**
 * 日志消费处理器接口
 */
public interface LogConsumer {

    /**
     * 消费一批日志
     * @param logs 日志列表
     */
    void consume(List<LabdatahubDeviceLogs> logs);
}
