package com.labdatahub.business.service;

import java.util.List;
import com.labdatahub.business.domain.LabdatahubWarnLinkage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.labdatahub.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 设备联动告警Service接口
 *
 * @author ruoyi
 * @date 2025-11-03
 */
public interface ILabdatahubWarnLinkageService extends IService<LabdatahubWarnLinkage>
{
    /**
     * 查询设备联动告警
     *
     * @param id 设备联动告警主键
     * @return 设备联动告警
     */
    public LabdatahubWarnLinkage selectLabdatahubWarnLinkageById(String id);

    /**
     * 查询设备联动告警列表
     *
     * @param labdatahubWarnLinkage 设备联动告警
     * @return 设备联动告警集合
     */
    public List<LabdatahubWarnLinkage> selectLabdatahubWarnLinkageList(LabdatahubWarnLinkage labdatahubWarnLinkage);

    /**
     * 新增设备联动告警
     *
     * @param labdatahubWarnLinkage 设备联动告警
     * @return 结果
     */
    public int insertLabdatahubWarnLinkage(LabdatahubWarnLinkage labdatahubWarnLinkage);

    /**
     * 修改设备联动告警
     *
     * @param labdatahubWarnLinkage 设备联动告警
     * @return 结果
     */
    public int updateLabdatahubWarnLinkage(LabdatahubWarnLinkage labdatahubWarnLinkage);

    /**
     * 批量删除设备联动告警
     *
     * @param ids 需要删除的设备联动告警主键集合
     * @return 结果
     */
    public int deleteLabdatahubWarnLinkageByIds(String[] ids);

    /**
     * 删除设备联动告警信息
     *
     * @param id 设备联动告警主键
     * @return 结果
     */
    public int deleteLabdatahubWarnLinkageById(String id);

    /**
     * 配置设备联动告警
     */
    public AjaxResult configLinkage(LabdatahubWarnLinkage labdatahubWarnLinkage) throws Exception;

    /**
     * 开关设备联动告警
     */
    public AjaxResult control(String id,String isEnable) throws Exception;
}
