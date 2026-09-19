//由AI修改
package com.labdatahub.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.labdatahub.business.domain.LabdatahubPointWriteRecord;

/**
 * 点位外部写入审计Service接口
 *
 * <p>只写不查——本接口的读侧留给将来的审计查询页，本期不建。
 */
public interface ILabdatahubPointWriteRecordService extends IService<LabdatahubPointWriteRecord>
{
    /**
     * 落一条审计记录
     *
     * <p>⚠️ <b>失败不得影响接口返回结果。</b> 审计写在设备写入之后（要记 isSuccess
     * 与 costMs），若与外层包在同一事务里，审计失败会连带回滚——但设备已经写进去了，
     * 数据库回滚改变不了这个事实，只会让调用方收到 500 然后重试。
     * 正确语义是：设备写成功就是成功，审计尽力而为。
     *
     * @param record 审计记录
     */
    void saveRecord(LabdatahubPointWriteRecord record);
}
