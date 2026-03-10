package com.labdatahub.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labdatahub.business.domain.LabdatahubProtocol;

/**
 * 协议管理Mapper接口
 *
 * @author labdatahub
 * @date 2025-09-18
 */
public interface LabdatahubProtocolMapper extends BaseMapper<LabdatahubProtocol>
{
    /**
     * 查询协议管理
     *
     * @param id 协议管理主键
     * @return 协议管理
     */
    public LabdatahubProtocol selectLabdatahubProtocolById(String id);

    /**
     * 查询协议管理列表
     *
     * @param labdatahubProtocol 协议管理
     * @return 协议管理集合
     */
    public List<LabdatahubProtocol> selectLabdatahubProtocolList(LabdatahubProtocol labdatahubProtocol);

    /**
     * 新增协议管理
     *
     * @param labdatahubProtocol 协议管理
     * @return 结果
     */
    public int insertLabdatahubProtocol(LabdatahubProtocol labdatahubProtocol);

    /**
     * 修改协议管理
     *
     * @param labdatahubProtocol 协议管理
     * @return 结果
     */
    public int updateLabdatahubProtocol(LabdatahubProtocol labdatahubProtocol);

    /**
     * 删除协议管理
     *
     * @param id 协议管理主键
     * @return 结果
     */
    public int deleteLabdatahubProtocolById(String id);

    /**
     * 批量删除协议管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabdatahubProtocolByIds(String[] ids);
}
