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
* @ClassName: LabdatahubOmronFinsConfig  
* @Description: omronfins协议读取配置对象 labdatahub_omronfins_config
* @author xwb  
* @date 2026年4月1日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_omronfins_config")
public class LabdatahubOmronFinsConfig implements Serializable
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

    /** 存储区代码：字区如 DM=0x82 / CIO=0xB0，位区如 DM位=0x02 / CIO位=0x30 */
    @Excel(name = "存储区代码")
    private Integer areaCode;

    /** 起始字地址（位区下这是「字地址」，位号另看 bitAddress） */
    @Excel(name = "起始地址")
    private Integer startAddress;

    /** 位号（仅位区用，0-15；字区为 null 表示按字访问） */
    @Excel(name = "位号")
    private Integer bitAddress;

    /** 读取长度：字区=字个数，位区=位个数（位点位恒为 1） */
    @Excel(name = "读取长度")
    private Integer length;
}
