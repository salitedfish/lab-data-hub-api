package com.labdatahub.business.process.function;

import com.labdatahub.business.domain.LabdatahubFunctionRecord;

import java.util.List;

/**
 * 日志消费处理器接口
 */
public interface FunctionConsumer {

    /**
     * 消费一批日志
     * @param logs 日志列表
     */
    void consume(List<LabdatahubFunctionRecord> logs);
}
