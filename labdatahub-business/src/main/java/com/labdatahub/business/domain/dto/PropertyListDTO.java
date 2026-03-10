package com.labdatahub.business.domain.dto;

import com.labdatahub.business.domain.LabdatahubProperties;
import com.labdatahub.common.annotation.Excel;
import lombok.Data;

import java.util.List;

/**
 * @Description: 属性列表
 * @Author: labdatahub
 * @CreateTime: 2025-09-24
 */
@Data
public class PropertyListDTO {
    private List<LabdatahubProperties> propertyList;
    private String belongSn;
    private String belongType;
    private String fromType;
}
