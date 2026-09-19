//由AI修改
package com.labdatahub.business.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.labdatahub.business.domain.LabdatahubPointWriteRecord;
import com.labdatahub.business.service.ILabdatahubPointWriteRecordService;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.page.TableDataInfo;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.StringUtils;

/**
 * 点位写值记录Controller
 *
 * <p>只读接口：这张表是审计日志，写入由 {@code OpenApiPointValueServiceImpl} 落，
 * 外部只查不改不删，所以这里没有 add/edit/remove/export。
 *
 * <p><b>协议无关</b>：设备详情页各协议的物模型 tab 用的是同一个按钮、同一个弹窗、
 * 同一个接口（方案 4.8.3），不按 netType 分叉——{@code netType} 只作为数据列存在，
 * 将来多协议接入时不用改这里。
 *
 * <p>过滤口径与 {@code LabdatahubDeviceLogsController#list} 一致
 * （LambdaQueryWrapper + 时间范围 ge/le + create_time 倒序 + Page 分页），
 * 前端的查询/重置/分页交互也照「历史数据」弹窗抄，不另起一套。
 */
@RestController
@RequestMapping("/business/pointWriteRecord")
public class LabdatahubPointWriteRecordController extends BaseController {

    @Autowired
    private ILabdatahubPointWriteRecordService labdatahubPointWriteRecordService;

    /**
     * 查询写值记录列表
     *
     * <p>四个过滤条件都可以不传，不传即不过滤。设备维度由前端固定带上（弹窗是从设备详情页进的），
     * 所以 {@code deviceSn} 实际上必传，但仍按可选处理，免得接口只能被这一个页面用。
     *
     * @param labdatahubPointWriteRecord 载体对象，只取 deviceSn / code / source / isSuccess 四个字段做等值过滤
     * @param startTime 起始时间（yyyy-MM-dd HH:mm:ss），含
     * @param endTime   结束时间（yyyy-MM-dd HH:mm:ss），含
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubPointWriteRecord labdatahubPointWriteRecord, String startTime, String endTime) {
        LambdaQueryWrapper<LabdatahubPointWriteRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(LabdatahubPointWriteRecord::getCreateTime);
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubPointWriteRecord.getDeviceSn()),
                LabdatahubPointWriteRecord::getDeviceSn, labdatahubPointWriteRecord.getDeviceSn());
        // 点位用等值匹配：前端下拉选的是标识符，不会传模糊词
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubPointWriteRecord.getCode()),
                LabdatahubPointWriteRecord::getCode, labdatahubPointWriteRecord.getCode());
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubPointWriteRecord.getSource()),
                LabdatahubPointWriteRecord::getSource, labdatahubPointWriteRecord.getSource());
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubPointWriteRecord.getIsSuccess()),
                LabdatahubPointWriteRecord::getIsSuccess, labdatahubPointWriteRecord.getIsSuccess());
        // 处理时间范围查询
        // ⚠️ 必须写成显式 if，不能写成 ge(StringUtils.isNotEmpty(startTime), 字段, LocalDateTime.parse(startTime, ...))：
        // Java 会先把 LocalDateTime.parse(startTime) 求值再传进 ge，条件为 false 也照样解析，
        // 页面「重置」清空时间范围后 startTime 为 null → NPE。两个条件必须分开判。
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (StringUtils.isNotEmpty(startTime)) {
            queryWrapper.ge(LabdatahubPointWriteRecord::getCreateTime, LocalDateTime.parse(startTime, formatter));
        }
        if (StringUtils.isNotEmpty(endTime)) {
            queryWrapper.le(LabdatahubPointWriteRecord::getCreateTime, LocalDateTime.parse(endTime, formatter));
        }
        Page<LabdatahubPointWriteRecord> page = new Page<LabdatahubPointWriteRecord>(PageUtils.getPageNum(), PageUtils.getPageSize());
        Page<LabdatahubPointWriteRecord> pageList = labdatahubPointWriteRecordService.page(page, queryWrapper);
        return getDataTable(pageList);
    }
}
