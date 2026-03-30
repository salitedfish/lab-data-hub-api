package com.labdatahub.business.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.labdatahub.common.utils.uuid.UUID;
import com.labdatahub.component.protocol.ProtocolManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labdatahub.business.mapper.LabdatahubProtocolMapper;
import com.labdatahub.business.domain.LabdatahubProtocol;
import com.labdatahub.business.service.ILabdatahubProtocolService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 协议管理Service业务层处理
 *
 * @author labdatahub
 * @date 2025-09-18
 */
@Service
public class LabdatahubProtocolServiceImpl extends ServiceImpl<LabdatahubProtocolMapper, LabdatahubProtocol> implements ILabdatahubProtocolService {
    @Autowired
    private LabdatahubProtocolMapper labdatahubProtocolMapper;
    public static final Map<String,String> MAIN_CLASS_PATH = new HashMap<>();
    public static final String PROTOCOL_PATH = System.getProperty("user.dir")+ File.separator+"protocol";
    static {
        File file = new File(PROTOCOL_PATH);
        if(!file.exists()){
            file.mkdir();
        }
        MAIN_CLASS_PATH.put("MQTT_BROKER","com.labdatahub.protocol.parent.MqttBrokerProtocol");
        MAIN_CLASS_PATH.put("MQTT_CLIENT","com.labdatahub.protocol.parent.MqttClientProtocol");
        MAIN_CLASS_PATH.put("TCP_SERVER","com.labdatahub.protocol.parent.TcpServerProtocol");
        MAIN_CLASS_PATH.put("UDP_SERVER","com.labdatahub.protocol.parent.UdpServerProtocol");
        MAIN_CLASS_PATH.put("COAP_SERVER","com.labdatahub.protocol.parent.CoapServerProtocol");
        MAIN_CLASS_PATH.put("HTTP_SERVER","com.labdatahub.protocol.parent.HttpServerProtocol");
        MAIN_CLASS_PATH.put("WEBSOCKET_SERVER","com.labdatahub.protocol.parent.WsServerProtocol");
        MAIN_CLASS_PATH.put("MODBUS_TCP","com.labdatahub.protocol.parent.ModbusTcpProtocol");
        MAIN_CLASS_PATH.put("S71200_TCP","com.labdatahub.protocol.parent.S71200TcpProtocol");
    }
    /**
     * 查询协议管理
     *
     * @param id 协议管理主键
     * @return 协议管理
     */
    @Override
    public LabdatahubProtocol selectLabdatahubProtocolById(String id) {
        return labdatahubProtocolMapper.selectLabdatahubProtocolById(id);
    }

    /**
     * 查询协议管理列表
     *
     * @param labdatahubProtocol 协议管理
     * @return 协议管理
     */
    @Override
    public List<LabdatahubProtocol> selectLabdatahubProtocolList(LabdatahubProtocol labdatahubProtocol) {
        return labdatahubProtocolMapper.selectLabdatahubProtocolList(labdatahubProtocol);
    }

    /**
     * 新增协议管理
     *
     * @param labdatahubProtocol 协议管理
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String insertLabdatahubProtocol(LabdatahubProtocol labdatahubProtocol, MultipartFile protocolFile) throws Exception {
        String result = "0";
        if(protocolFile!=null&&!protocolFile.isEmpty()){
            if(Objects.requireNonNull(protocolFile.getOriginalFilename()).endsWith(".jar")){
                String fileNewName = UUID.randomUUID().toString()+".jar";
                String filePath = PROTOCOL_PATH + File.separator + fileNewName;
                protocolFile.transferTo(new File(filePath));
                labdatahubProtocol.setCreateTime(new Date());
                labdatahubProtocol.setLocalUrl(filePath);
                labdatahubProtocol.setNewName(fileNewName);
                labdatahubProtocol.setType("jar");
                labdatahubProtocol.setOriginName(protocolFile.getOriginalFilename());
                labdatahubProtocol.setMainClassPath(MAIN_CLASS_PATH.getOrDefault(labdatahubProtocol.getProtocolType(),null));
                labdatahubProtocol.setId(IdWorker.getIdStr());
                labdatahubProtocolMapper.insert(labdatahubProtocol);
                ProtocolManager.addProtocol(labdatahubProtocol.getId(),labdatahubProtocol.getProtocolType(),labdatahubProtocol.getLocalUrl(),labdatahubProtocol.getMainClassPath());
                result = labdatahubProtocol.getId();
            }else {
                //其他类型
            }
        }
        return result;
    }

    /**
     * 修改协议管理
     *
     * @param labdatahubProtocol 协议管理
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateLabdatahubProtocol(LabdatahubProtocol labdatahubProtocol, MultipartFile protocolFile) throws Exception {
        if(protocolFile!=null&&!protocolFile.isEmpty()){
            if(Objects.requireNonNull(protocolFile.getOriginalFilename()).endsWith(".jar")){
                String fileNewName = UUID.randomUUID().toString()+".jar";
                String filePath = PROTOCOL_PATH + File.separator + fileNewName;
                protocolFile.transferTo(new File(filePath));
                labdatahubProtocol.setLocalUrl(filePath);
                labdatahubProtocol.setNewName(fileNewName);
                labdatahubProtocol.setType("jar");
                labdatahubProtocol.setOriginName(protocolFile.getOriginalFilename());
                labdatahubProtocol.setMainClassPath(MAIN_CLASS_PATH.getOrDefault(labdatahubProtocol.getProtocolType(),null));
                labdatahubProtocolMapper.updateById(labdatahubProtocol);
                ProtocolManager.removeProtocol(labdatahubProtocol.getId());
                ProtocolManager.addProtocol(labdatahubProtocol.getId(),labdatahubProtocol.getProtocolType(),labdatahubProtocol.getLocalUrl(),labdatahubProtocol.getMainClassPath());
            }else {
                //其他类型
            }
        }
        return 1;
    }

    /**
     * 批量删除协议管理
     *
     * @param ids 需要删除的协议管理主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubProtocolByIds(String[] ids) {
        return labdatahubProtocolMapper.deleteLabdatahubProtocolByIds(ids);
    }

    /**
     * 删除协议管理信息
     *
     * @param id 协议管理主键
     * @return 结果
     */
    @Override
    public int deleteLabdatahubProtocolById(String id) {
        return labdatahubProtocolMapper.deleteLabdatahubProtocolById(id);
    }
}
