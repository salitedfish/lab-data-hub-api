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
* @ClassName: LabdatahubFanucConfig
* @Description: FANUC FOCAS2协议读取配置对象 labdatahub_fanuc_config
* @author xwb
* @date 2026年8月24日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_fanuc_config")
public class LabdatahubFanucConfig implements Serializable
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

    /** 采集项类型（axis/spindle/feed/mode/status/prgnum/exeprgname/alarm/tcode/macro/timer/count/diag/override/pmc） */
    @Excel(name = "采集项类型")
    private String readType;

    /** 参数1（轴号/子项/宏变量号等，各readType含义不同） */
    @Excel(name = "参数1")
    private Integer param1;

    /** 参数2（坐标类型/地址号等，各readType含义不同） */
    @Excel(name = "参数2")
    private Integer param2;
}
