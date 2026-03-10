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
 * 设备分组对象 labdatahub_device_group
 *
 * @author ruoyi
 * @date 2026-01-18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_device_group")
public class LabdatahubDeviceGroup
{
private static final long serialVersionUID = 1L;

    /** id */
    private String id;
    /** 分组名称 */
    @Excel(name = "分组名称")
    private String groupName;
    /** 唯一标识 */
    @Excel(name = "唯一标识")
    private String groupCode;
    /** 0-产品 1-设备 */
    @Excel(name = "0-产品 1-设备")
    private String type;
    /** 排序 */
    @Excel(name = "排序")
    private Long sortNum;
    /** 备注 */
    @Excel(name = "备注")
    private String remark;
    /** 创建时间 */
    private Date createTime;
}
