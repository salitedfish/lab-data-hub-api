package com.labdatahub.business.process.warn;

import com.labdatahub.business.domain.LabdatahubDeviceLogs;
import com.labdatahub.business.domain.LabdatahubWarnRecord;

import java.util.List;

/**
 * 日志消费处理器接口
 */
public interface WarnConsumer {

    /**
     * 消费一批日志
     * @param logs 日志列表
     */
    void consume(List<LabdatahubWarnRecord> logs);
}
