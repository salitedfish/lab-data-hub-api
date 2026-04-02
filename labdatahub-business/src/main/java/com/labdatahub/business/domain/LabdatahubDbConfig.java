package com.labdatahub.business.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import com.labdatahub.common.annotation.Excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
* @ClassName: LabdatahubDbConfig  
* @Description: database协议读取配置对象 labdatahub_db_config
* @author xwb  
* @date 2026年4月2日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_db_config")
public class LabdatahubDbConfig implements Serializable
{
private static final long serialVersionUID = 1L;

    /** id */
    private String id;
    /** 归属sn */
    @Excel(name = "归属sn")
    private String belongSn;
    /** 归属类型 0-产品 1-设备 */
    @Excel(name = "归属类型 0-产品 1-设备")
    private String belongType;
    /** 读取编码 */
    @Excel(name = "读取编码（字段名称）")
    private String code;
    /** 创建时间 */
    private Date createTime;
    /** 多少毫秒读取一次 */
    @Excel(name = "多少毫秒读取一次")
    private Long intervalTime;
    /** 同一网络组件读取属性延迟时间 */
    @Excel(name = "同一网络组件读取属性延迟时间")
    private Long delayTime;

    @Excel(name = "字段类型")
    private String fieldType;     
    
    @Excel(name = "字段备注")
    private String fieldComment;    
    
    @Excel(name = "字段长度")
    private String fieldLength; 
}
