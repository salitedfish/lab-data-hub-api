package com.labdatahub.business.service;

import java.util.List;
import com.labdatahub.business.domain.LabdatahubWarnConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import com.labdatahub.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 告警配置Service接口
 *
 * @author labdatahub
 * @date 2025-10-05
 */
public interface ILabdatahubWarnConfigService extends IService<LabdatahubWarnConfig>
{
    /**
     * 查询告警配置
     *
     * @param id 告警配置主键
     * @return 告警配置
     */
    public LabdatahubWarnConfig selectLabdatahubWarnConfigById(String id);

    /**
     * 查询告警配置列表
     *
     * @param labdatahubWarnConfig 告警配置
     * @return 告警配置集合
     */
    public List<LabdatahubWarnConfig> selectLabdatahubWarnConfigList(LabdatahubWarnConfig labdatahubWarnConfig);

    /**
     * 新增告警配置
     *
     * @param labdatahubWarnConfig 告警配置
     * @return 结果
     */
    public int insertLabdatahubWarnConfig(LabdatahubWarnConfig labdatahubWarnConfig);

    /**
     * 修改告警配置
     *
     * @param labdatahubWarnConfig 告警配置
     * @return 结果
     */
    public int updateLabdatahubWarnConfig(LabdatahubWarnConfig labdatahubWarnConfig);

    /**
     * 批量删除告警配置
     *
     * @param ids 需要删除的告警配置主键集合
     * @return 结果
     */
    public int deleteLabdatahubWarnConfigByIds(String[] ids);

    /**
     * 删除告警配置信息
     *
     * @param id 告警配置主键
     * @return 结果
     */
    public int deleteLabdatahubWarnConfigById(String id);

    /**
     * 从产品同步告警配置到指定设备
     * @param productSn
     * @return
     */
    public void syncWarnConfigToDevice(String productSn,String deviceSn);
}
