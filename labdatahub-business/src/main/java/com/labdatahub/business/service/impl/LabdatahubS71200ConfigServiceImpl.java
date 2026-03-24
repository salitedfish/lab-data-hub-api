package com.labdatahub.business.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.labdatahub.business.domain.LabdatahubS71200Config;
import com.labdatahub.business.mapper.LabdatahubModbusConfigMapper;
import com.labdatahub.business.mapper.LabdatahubS71200ConfigMapper;
import com.labdatahub.business.service.ILabdatahubS71200ConfigService;

/**
 * 
* @ClassName: LabdatahubS71200ConfigServiceImpl  
* @Description: S71200协议读取配置Service业务层处理
* @author xwb  
* @date 2026年3月24日
 */
@Service
public class LabdatahubS71200ConfigServiceImpl extends ServiceImpl<LabdatahubS71200ConfigMapper, LabdatahubS71200Config> implements ILabdatahubS71200ConfigService
{
    @Autowired
    private LabdatahubModbusConfigMapper labdatahubModbusConfigMapper;

    
}
