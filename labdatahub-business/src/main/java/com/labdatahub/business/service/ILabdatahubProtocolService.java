package com.labdatahub.business.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import com.labdatahub.business.domain.LabdatahubProtocol;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 协议管理Service接口
 *
 * @author labdatahub
 * @date 2025-09-18
 */
public interface ILabdatahubProtocolService extends IService<LabdatahubProtocol>
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
    public String insertLabdatahubProtocol(LabdatahubProtocol labdatahubProtocol, MultipartFile protocolFile) throws Exception;

    /**
     * 修改协议管理
     *
     * @param labdatahubProtocol 协议管理
     * @return 结果
     */
    public int updateLabdatahubProtocol(LabdatahubProtocol labdatahubProtocol, MultipartFile protocolFil) throws Exception;

    /**
     * 批量删除协议管理
     *
     * @param ids 需要删除的协议管理主键集合
     * @return 结果
     */
    public int deleteLabdatahubProtocolByIds(String[] ids);

    /**
     * 删除协议管理信息
     *
     * @param id 协议管理主键
     * @return 结果
     */
    public int deleteLabdatahubProtocolById(String id);
}
