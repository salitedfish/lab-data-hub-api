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
* @ClassName: LabdatahubMitsubishiCncConfig
* @Description: 三菱CNC TCP(MOCHA)协议读取配置对象 labdatahub_mitsubishi_cnc_config
* @author xwb
* @date 2026年8月29日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_mitsubishi_cnc_config")
public class LabdatahubMitsubishiCncConfig implements Serializable
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

    /** 采集项类型（树根点位键：al/fre/pn/spn/cc/sl1/ss1/tn/stn/po/opt/cut/ct/sv/fv/st/pst/opm/axc；轴点 mechpos/currpos/remapos/cu/sp） */
    @Excel(name = "采集项类型")
    private String readType;

    /** 轴号（1-6，仅轴类点位有效） */
    @Excel(name = "轴号")
    private Integer axisNo;
}
