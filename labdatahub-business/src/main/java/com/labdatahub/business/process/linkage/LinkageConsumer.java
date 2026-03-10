package com.labdatahub.business.process.linkage;

import com.labdatahub.business.domain.LabdatahubFunctionRecord;
import com.labdatahub.business.domain.LabdatahubLinkageWarnRecord;

import java.util.List;

/**
 * 日志消费处理器接口
 */
public interface LinkageConsumer {

    /**
     * 消费一批日志
     * @param logs 日志列表
     */
    void consume(List<LabdatahubLinkageWarnRecord> logs);
}
