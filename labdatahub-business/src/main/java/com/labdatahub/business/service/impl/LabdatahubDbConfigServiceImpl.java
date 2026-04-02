package com.labdatahub.business.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.labdatahub.business.domain.LabdatahubDbConfig;
import com.labdatahub.business.mapper.LabdatahubDbConfigMapper;
import com.labdatahub.business.service.ILabdatahubDbConfigService;

/**
 * 
* @ClassName: LabdatahubDbConfigServiceImpl  
* @Description: database协议读取配置Service业务层处理
* @author xwb  
* @date 2026年4月2日
 */
@Service
public class LabdatahubDbConfigServiceImpl extends ServiceImpl<LabdatahubDbConfigMapper, LabdatahubDbConfig> implements ILabdatahubDbConfigService
{

}
