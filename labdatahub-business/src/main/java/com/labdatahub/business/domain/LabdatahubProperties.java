//由AI修改
package com.labdatahub.business.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.labdatahub.common.annotation.Excel;

import java.util.Date;

/**
 * 物模型属性定义对象 labdatahub_properties
 *
 * @author labdatahub
 * @date 2025-09-18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "labdatahub_properties")
public class LabdatahubProperties {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 归属Sn 产品/设备
     */
    @Excel(name = "归属Sn 产品/设备")
    private String belongSn;

    /**
     * 归属类型 0-产品 1-设备
     */
    @Excel(name = "归属类型 0-产品 1-设备")
    private String belongType;

    /**
     * 属性标识符，如 temperature, status
     */
    @Excel(name = "属性标识符，如 temperature, status")
    private String identifier;

    /**
     * 属性名称
     */
    @Excel(name = "属性名称")
    private String name;

    /**
     * 父属性ID，用于构建嵌套结构。0表示根级属性
     */
    @Excel(name = "父属性ID，用于构建嵌套结构。0表示根级属性")
    private String parentId;

    /**
     * 数据类型: int, double, bool, string, struct, array...
     */
    @Excel(name = "数据类型: int, double, bool, string, struct, array...")
    private String dataType;

    /**
     * 排序
     */
    @Excel(name = "排序")
    private Long sortNum;

    /**
     * 来源 0-产品继承 1-设备自定义(继承不可修改)
     */
    @Excel(name = "来源 0-产品继承 1-设备自定义(继承不可修改)")
    private String fromType;

    /** 备注 */
    private String remark;

    /**单位*/
    private String unit;

    /**字节序: big-大端 little-小端（多寄存器/多字节数值解析用）*/
    private String byteOrder;

    /**是否有符号: 1-有符号 0-无符号（整型解析用）*/
    private String isSigned;

    /**缩放系数: 数值乘此系数得到工程值*/
    private Double scale;

    /**偏移量: 数值乘缩放后再加此偏移（列名避开PG/MySQL保留字offset，用offset_value）*/
    @TableField("offset_value")
    private Double offset;

}
