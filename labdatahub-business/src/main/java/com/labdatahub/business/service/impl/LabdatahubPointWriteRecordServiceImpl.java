//由AI修改
package com.labdatahub.business.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.labdatahub.business.domain.LabdatahubPointWriteRecord;
import com.labdatahub.business.mapper.LabdatahubPointWriteRecordMapper;
import com.labdatahub.business.service.ILabdatahubPointWriteRecordService;

import lombok.extern.slf4j.Slf4j;

/**
 * 点位外部写入审计Service业务层处理
 */
@Slf4j
@Service
public class LabdatahubPointWriteRecordServiceImpl
        extends ServiceImpl<LabdatahubPointWriteRecordMapper, LabdatahubPointWriteRecord>
        implements ILabdatahubPointWriteRecordService {

    @Autowired
    private LabdatahubPointWriteRecordMapper labdatahubPointWriteRecordMapper;

    /**
     * 落一条审计记录
     *
     * <p>⚠️ <b>这里刻意不加 {@code @Transactional}。</b> 审计写在设备写入之后，
     * 若与外层包在同一事务里，审计失败会连带把设备写入「回滚」——但设备已经写进去了，
     * 数据库回滚改变不了这个事实，只会让调用方收到 500 然后重试。
     * 不加事务时每条 insert 各自自动提交，异常在这里被完全吞掉，不影响接口结果。
     *
     * <p>（也不加 {@code REQUIRES_NEW}：单条 insert 用不上，而且在外层已开事务时，
     * 若这里 catch 住异常再返回，Spring 仍会去 commit 一个已被 PG 标记 aborted 的事务，
     * 那个 commit 异常会绕过这里的 catch 抛给调用方。不包事务最干净。）
     */
    @Override
    public void saveRecord(LabdatahubPointWriteRecord record) {
        if (record == null) {
            return;
        }
        try {
            labdatahubPointWriteRecordMapper.insert(record);
        } catch (Exception e) {
            // 审计失败只记日志，绝不影响接口返回结果
            log.error("[写值审计] 落库失败，deviceSn={} code={} requestId={}：{}",
                    record.getDeviceSn(), record.getCode(), record.getRequestId(), e.getMessage(), e);
        }
    }
}
