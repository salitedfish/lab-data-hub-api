//由AI修改
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
* @ClassName: LabdatahubS71200Config  
* @Description: s71200协议读取配置对象 labdatahub_s71200_config
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
    /** 点位名称（物模型属性名用，null 用标识 code） */
    @Excel(name = "点位名称")
    private String name;
    /** 创建时间 */
    private Date createTime;
    /** 多少毫秒读取一次 */
    @Excel(name = "多少毫秒读取一次")
    private Long intervalTime;
    /** 同一网络组件读取属性延迟时间 */
    @Excel(name = "同一网络组件读取属性延迟时间")
    private Long delayTime;

    @Excel(name = "DB块号")
    private Integer dbNumber;     // DB块号
    
    @Excel(name = "块类型（DBW，DBX，DBD，DBB）")
    private String blockType;

    @Excel(name = "区类型（DB，M，I，Q）")
    private String areaType;      // 区类型: DB数据块/M标志位/I输入区/Q输出区

    @Excel(name = "起始地址")
    private Integer startAddress; 
    
    @Excel(name = "偏移量（0-7）")
    private Integer bitOffset;   		// 偏移量
    
    @Excel(name = "读取长度（字节）")
    private Integer length;       // 读取长度（字节）
}
