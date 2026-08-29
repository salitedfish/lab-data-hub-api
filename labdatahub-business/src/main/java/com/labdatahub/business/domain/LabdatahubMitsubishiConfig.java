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
* @ClassName: LabdatahubMitsubishiConfig
* @Description: 三菱MC协议读取配置对象 labdatahub_mitsubishi_config
* @author xwb
* @date 2026年8月24日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_mitsubishi_config")
public class LabdatahubMitsubishiConfig implements Serializable
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
    /** 多少秒读取一次 */
    @Excel(name = "多少秒读取一次")
    private Long intervalTime;
    /** 同一网络组件读取属性延迟时间（毫秒） */
    @Excel(name = "同一网络组件读取属性延迟时间")
    private Long delayTime;

    /** 软元件代码：字设备 D=0xA8 W=0xB4 R=0xAF ZR=0xB0 SD=0xA9；位设备 M=0x90 L=0x92 B=0xA0 X=0x9C Y=0x9D S=0x98 SM=0x91 F=0x93 */
    @Excel(name = "软元件代码")
    private Integer areaCode;

    /** 起始地址（X/Y为八进制地址） */
    @Excel(name = "起始地址")
    private Integer startAddress;

    /** 读取数量（字设备为字数，位设备为点数） */
    @Excel(name = "读取数量")
    private Integer length;

    /** 协议帧模式：3E-QnA兼容3E帧（默认） 1E-MC1E标准二进制帧 */
    @Excel(name = "协议帧模式")
    private String protocolMode;
}
