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
* @ClassName: LabdatahubBrotherConfig
* @Description: Brother NC协议读取配置对象 labdatahub_brother_config
* @author xwb
* @date 2026年8月20日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_brother_config")
public class LabdatahubBrotherConfig implements Serializable
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
    /** 同一网络组件读取属性延迟时间 */
    @Excel(name = "同一网络组件读取属性延迟时间")
    private Long delayTime;

    /** 数据区名（如 PDSP/ALARM/PRD3） */
    @Excel(name = "数据区名")
    private String dataArea;

    /** 行号（1起，对应数据区点表的行顺序） */
    @Excel(name = "行号")
    private Integer rowNumber;

    /** 字段序号（1起，第1个字段=响应行 Symbol 后的第一个值） */
    @Excel(name = "字段序号")
    private Integer fieldIndex;
}
