package com.labdatahub.business.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import com.labdatahub.common.annotation.Excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * modbus协议读取配置对象 labdatahub_modbus_config
 *
 * @author ruoyi
 * @date 2025-12-16
 */
/**
 * 
* @ClassName: LabdatahubS71200Config  
* @Description: s71200协议读取配置对象 labdatahub_modbus_config
* @author xwb  
* @date 2026年3月24日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_s71200_config")
public class LabdatahubS71200Config implements Serializable
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
    @Excel(name = "读取编码")
    private String code;
    /** 创建时间 */
    private Date createTime;
    /** 多少毫秒读取一次 */
    @Excel(name = "多少毫秒读取一次")
    private Long intervalTime;
    /** 同一网络组件读取属性延迟时间 */
    @Excel(name = "同一网络组件读取属性延迟时间")
    private Long delayTime;
    /** 范围,逗号分隔如（1,2-5,7） */
    @Excel(name = "DB块号")
    private Integer dbNumber;     // DB块号
    @Excel(name = "起始字节偏移")
    private Integer startAddress; // 起始字节偏移
    @Excel(name = "读取长度（字节）")
    private Integer length;       // 读取长度（字节）
}
