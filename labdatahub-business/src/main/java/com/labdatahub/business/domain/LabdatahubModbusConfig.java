//由AI修改
package com.labdatahub.business.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.labdatahub.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * modbus协议读取配置对象 labdatahub_modbus_config
 *
 * @author ruoyi
 * @date 2025-12-16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_modbus_config")
public class LabdatahubModbusConfig
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
    /** 范围,逗号分隔如（1,2-5,7） */
    @Excel(name = "范围,逗号分隔如", readConverterExp = "1=,2-5,7")
    private String registerRange;
    /** 功能码: 01线圈 02离散输入 03保持寄存器 04输入寄存器，默认03 */
    @Excel(name = "功能码")
    private String functionCode;
    /** 多少毫秒读取一次 */
    @Excel(name = "多少毫秒读取一次")
    private Long intervalTime;
    /** 同一网络组件读取属性延迟时间 */
    @Excel(name = "同一网络组件读取属性延迟时间")
    private Long delayTime;
}
