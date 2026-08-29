package com.labdatahub.business.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.labdatahub.business.domain.*;
import com.labdatahub.business.service.*;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.business.utils.ProtocolPointCopyUtil;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.core.page.TableDataInfo;

/**
 * 产品Controller
 *
 * @author labdatahub
 * @date 2025-09-18
 */
@RestController
@RequestMapping("/business/product")
public class LabdatahubProductController extends BaseController {
    @Autowired
    private ILabdatahubProductService labdatahubProductService;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    @Autowired
    private ILabdatahubPropertiesService labdatahubPropertiesService;
    @Autowired
    private ILabdatahubComponentService labdatahubComponentService;
    @Autowired
    private ILabdatahubProtocolService labdatahubProtocolService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private ILabdatahubDeviceLogsService labdatahubDeviceLogsService;
    @Autowired
    private ProtocolPointCopyUtil protocolPointCopyUtil;


    /**
     * 查询产品列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubProduct labdatahubProduct) {
        QueryWrapper<LabdatahubProduct> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("create_time");
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubProduct.getGroupCode()),"group_code",labdatahubProduct.getGroupCode());
        queryWrapper.like(StringUtils.isNotEmpty(labdatahubProduct.getProductName()),"product_name",labdatahubProduct.getProductName());
        queryWrapper.like(StringUtils.isNotEmpty(labdatahubProduct.getProductSn()),"product_sn",labdatahubProduct.getProductSn());
        Page<LabdatahubProduct> page = new Page<LabdatahubProduct>(PageUtils.getPageNum(), PageUtils.getPageSize());
        Page<LabdatahubProduct> pageList = labdatahubProductService.page(page, queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出产品列表
     */
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubProduct labdatahubProduct) {
        List<LabdatahubProduct> list = labdatahubProductService.selectLabdatahubProductList(labdatahubProduct);
        ExcelUtil<LabdatahubProduct> util = new ExcelUtil<LabdatahubProduct>(LabdatahubProduct.class);
        util.exportExcel(response, list, "产品数据");
    }

    /**
     * 获取产品详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return success(labdatahubProductService.getById(id));
    }

    /**
     * 新增产品
     */
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubProduct labdatahubProduct) {
        //不可与产品sn重复
        long count = labdatahubDeviceService.count(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getDeviceSn, labdatahubProduct.getProductSn()));
        if (count > 0) {
            return AjaxResult.warn("产品sn已经被设备使用，请更换产品sn");
        }
        labdatahubProduct.setCreateTime(new Date());
        if(StringUtils.isNotEmpty(labdatahubProduct.getComponentId())){
            LabdatahubComponent component = labdatahubComponentService.getById(labdatahubProduct.getComponentId());
            labdatahubProduct.setProtocolId(component.getProtocolId());
            labdatahubProduct.setProtocolName(component.getProtocolName());
        }
        labdatahubProductService.save(labdatahubProduct);
        CacheUtils.PRODUCT_MAP.put(labdatahubProduct.getProductSn(),labdatahubProduct);
        // 由AI修改：复制产品时把产品级点位模板一起复制（物模型 + 各协议点位 + 告警 + 功能），换组件即换产品（不改框架）
        if (StringUtils.isNotEmpty(labdatahubProduct.getCopyFromId())) {
            LabdatahubProduct sourceProduct = labdatahubProductService.getById(labdatahubProduct.getCopyFromId());
            if (sourceProduct != null) {
                protocolPointCopyUtil.copyProductPoints(sourceProduct.getProductSn(), labdatahubProduct.getProductSn());
            }
        }
        return AjaxResult.success();
    }

    /**
     * 修改产品
     */
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubProduct labdatahubProduct) {
        if(StringUtils.isNotEmpty(labdatahubProduct.getComponentId())){
            LabdatahubComponent component = labdatahubComponentService.getById(labdatahubProduct.getComponentId());
            labdatahubProduct.setProtocolId(component.getProtocolId());
            labdatahubProduct.setProtocolName(component.getProtocolName());
            labdatahubDeviceService.update(new LambdaUpdateWrapper<LabdatahubDevice>()
                    .eq(LabdatahubDevice::getProductSn,labdatahubProduct.getProductSn())
                    .set(LabdatahubDevice::getComponentId,component.getId())
                    .set(LabdatahubDevice::getComponentName,component.getName())
                    .set(LabdatahubDevice::getProtocolId,component.getProtocolId())
                    .set(LabdatahubDevice::getProtocolName,component.getProtocolName()));
            CacheUtils.updateDeviceCacheByProductSn(labdatahubProduct.getProductSn());
        }
        labdatahubProductService.updateById(labdatahubProduct);
        LabdatahubProduct product = labdatahubProductService.getById(labdatahubProduct.getId());
        CacheUtils.PRODUCT_MAP.put(product.getProductSn(),product);
        return AjaxResult.success();
    }

    /**
     * 删除产品
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        for (String id : ids) {
            LabdatahubProduct product = labdatahubProductService.getById(id);
            if(product.getDeviceCount()>0){
                return AjaxResult.warn("请先删除产品关联的设备");
            }
            labdatahubProductService.removeById(id);
            CacheUtils.PRODUCT_MAP.remove(product.getProductSn());
        }
        return AjaxResult.success();
    }

    /**
     * 同步物模型到设备
     */
    @PostMapping("/syncToDevice")
    public AjaxResult syncToDevice(@RequestBody LabdatahubProduct labdatahubProduct) {
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn, labdatahubProduct.getProductSn()));
        List<LabdatahubProperties> propertiesList = labdatahubPropertiesService.list(new LambdaQueryWrapper<LabdatahubProperties>()
                .eq(LabdatahubProperties::getBelongSn, labdatahubProduct.getProductSn()));
        List<LabdatahubProperties> saveList = new ArrayList<>();
        deviceList.forEach(o -> {
            labdatahubPropertiesService.remove(new LambdaUpdateWrapper<LabdatahubProperties>()
                    .eq(LabdatahubProperties::getBelongSn, o.getDeviceSn()));
            propertiesList.forEach(vo -> {
                LabdatahubProperties properties = new LabdatahubProperties();
                BeanUtils.copyProperties(vo, properties);
                properties.setBelongSn(o.getDeviceSn());
                properties.setBelongType("1");
                properties.setFromType("0");
                properties.setId(null);
                saveList.add(properties);
            });
        });
        labdatahubPropertiesService.saveBatch(saveList);
        //缓存物模型
        deviceList.forEach(o -> {
            labdatahubDeviceService.cacheDeviceProperties(o.getDeviceSn());
        });
        return AjaxResult.success();
    }

    /**
     * 同步数据保存时间到设备
     */
    @PostMapping("/syncRetentionTimeToDevice")
    public AjaxResult syncRetentionTimeToDevice(@RequestBody LabdatahubProduct labdatahubProduct) {
        if (StringUtils.isEmpty(labdatahubProduct.getProductSn())
                || StringUtils.isEmpty(labdatahubProduct.getRegularCleaning())
                || labdatahubProduct.getRetentionTime() == null) {
            return AjaxResult.warn("配置信息填写不完整！");
        }
        labdatahubProductService.update(new LambdaUpdateWrapper<LabdatahubProduct>()
                .eq(LabdatahubProduct::getProductSn,labdatahubProduct.getProductSn())
                .set(LabdatahubProduct::getRegularCleaning,labdatahubProduct.getRegularCleaning())
                .set(LabdatahubProduct::getRetentionTime,labdatahubProduct.getRetentionTime())
                .set(LabdatahubProduct::getRetentionUnit,labdatahubProduct.getRetentionUnit()));
        labdatahubDeviceService.update(new LambdaUpdateWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,labdatahubProduct.getProductSn())
                .set(LabdatahubDevice::getRegularCleaning,labdatahubProduct.getRegularCleaning())
                .set(LabdatahubDevice::getRetentionTime,labdatahubProduct.getRetentionTime())
                .set(LabdatahubDevice::getRetentionUnit,labdatahubProduct.getRetentionUnit()));
        CacheUtils.DEVICE_MAP.keySet().forEach(key->{
            LabdatahubDevice device = CacheUtils.DEVICE_MAP.get(key);
            if(device!=null&&labdatahubProduct.getProductSn().equals(device.getProductSn())){
                device.setRetentionTime(labdatahubProduct.getRetentionTime());
                device.setRetentionUnit(labdatahubProduct.getRetentionUnit());
                device.setRegularCleaning(labdatahubProduct.getRegularCleaning());
            }
        });
        return AjaxResult.success("同步成功");
    }

    /**
     * 修改自定义配置
     */
    @PostMapping("/syncCustomConfigToDevice")
    public AjaxResult syncCustomConfigToDevice(@RequestBody LabdatahubProduct labdatahubProduct){
        if (StringUtils.isEmpty(labdatahubProduct.getProductSn())
                || StringUtils.isEmpty(labdatahubProduct.getCustomConfig())) {
            return AjaxResult.warn("配置信息填写不完整！");
        }
        labdatahubProductService.update(new LambdaUpdateWrapper<LabdatahubProduct>()
                .eq(LabdatahubProduct::getProductSn,labdatahubProduct.getProductSn())
                .set(LabdatahubProduct::getCustomConfig,labdatahubProduct.getCustomConfig()));
        labdatahubDeviceService.update(new LambdaUpdateWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,labdatahubProduct.getProductSn())
                .set(LabdatahubDevice::getCustomConfig,labdatahubProduct.getCustomConfig()));
        CacheUtils.DEVICE_MAP.keySet().forEach(key->{
            LabdatahubDevice device = CacheUtils.DEVICE_MAP.get(key);
            if(device!=null&&labdatahubProduct.getProductSn().equals(device.getProductSn())){
                device.setCustomConfig(labdatahubProduct.getCustomConfig());
            }
        });
        LabdatahubProduct product = labdatahubProductService.getOne(new LambdaUpdateWrapper<LabdatahubProduct>()
                .eq(LabdatahubProduct::getProductSn,labdatahubProduct.getProductSn()));
        CacheUtils.PRODUCT_MAP.put(labdatahubProduct.getProductSn(),product);
        return AjaxResult.success("同步成功");
    }

    /**
     * 统计
     */
    @GetMapping("/indexStatics")
    public AjaxResult indexStatics() {
        // 创建计数器
        CountDownLatch latch = new CountDownLatch(5);

        // 使用原子类保证线程安全
        AtomicLong productCount = new AtomicLong(0);
        AtomicLong deviceOnlineCount = new AtomicLong(0);
        AtomicLong deviceCount = new AtomicLong(0);
        AtomicLong componentCount = new AtomicLong(0);
        AtomicLong protocolCount = new AtomicLong(0);
        AtomicLong todayMessageCount = new AtomicLong(0);
        // 执行任务
        threadPoolTaskExecutor.execute(() -> {
            try {
                productCount.set(labdatahubProductService.count());
            } finally {
                latch.countDown();
            }
        });
        threadPoolTaskExecutor.execute(() -> {
            try {
                deviceOnlineCount.set(labdatahubDeviceService.count(new LambdaQueryWrapper<LabdatahubDevice>()
                        .eq(LabdatahubDevice::getStatus, "1")));
                deviceCount.set(labdatahubDeviceService.count());
            } finally {
                latch.countDown();
            }
        });
        threadPoolTaskExecutor.execute(() -> {
            try {
                componentCount.set(labdatahubComponentService.count());
            } finally {
                latch.countDown();
            }
        });
        threadPoolTaskExecutor.execute(() -> {
            try {
                protocolCount.set(labdatahubProtocolService.count());
            } finally {
                latch.countDown();
            }
        });
        threadPoolTaskExecutor.execute(() -> {
            try {
                todayMessageCount.set(labdatahubDeviceLogsService.count(new LambdaQueryWrapper<LabdatahubDeviceLogs>()
                        .ge(LabdatahubDeviceLogs::getCreateTime, LocalDate.now().atStartOfDay())));
            } finally {
                latch.countDown();
            }
        });
        try {
            // 等待所有任务完成，设置超时时间避免无限等待
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            if (!completed) {
                return AjaxResult.error("查询超时");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return AjaxResult.error("查询被中断");
        }
        JSONObject result = new JSONObject();
        result.put("productCount", productCount.get());
        result.put("deviceOnlineCount", deviceOnlineCount.get());
        result.put("deviceCount", deviceCount.get());
        result.put("deviceOfflineCount", deviceCount.get() - deviceOnlineCount.get());
        result.put("componentCount", componentCount.get());
        result.put("protocolCount", protocolCount.get());
        result.put("todayMessageCount", todayMessageCount.get());
        return AjaxResult.success(result);
    }

    /**
     * 在线设备
     */
    @GetMapping("/onlineDeviceCount")
    public AjaxResult onlineDeviceCount(@RequestParam String productSn){
        long onlineCount = labdatahubDeviceService.count(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn)
                .eq(LabdatahubDevice::getStatus,"1"));
        return AjaxResult.success(onlineCount);
    }
}
