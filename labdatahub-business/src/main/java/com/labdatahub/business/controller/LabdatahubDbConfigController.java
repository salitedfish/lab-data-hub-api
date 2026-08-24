package com.labdatahub.business.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.labdatahub.business.domain.LabdatahubDbConfig;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.service.ILabdatahubDbConfigService;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.common.annotation.Log;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.core.page.TableDataInfo;
import com.labdatahub.common.enums.BusinessType;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.component.db.DatabaseConnectionManager;
import com.labdatahub.component.db.DatabaseMessageScheduler;
import com.labdatahub.component.db.DatabaseReadConfig;
import com.labdatahub.component.db.DatabaseReader;

/**
 * 
* @ClassName: LabdatahubDbConfigController  
* @Description: database协议读取配置Controller
* @author xwb  
* @date 2026年4月2日
 */
@RestController
@RequestMapping("/business/db")
public class LabdatahubDbConfigController extends BaseController
{
    @Autowired
    private ILabdatahubDbConfigService labdatahubDbConfigService;
    @Autowired
    private ILabdatahubDeviceService labdatahubDeviceService;
    /**
     * 查询database协议读取配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(LabdatahubDbConfig labdatahubDbConfig)
    {
        LambdaQueryWrapper<LabdatahubDbConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(LabdatahubDbConfig::getCreateTime);
        if (StringUtils.isNotEmpty(labdatahubDbConfig.getBelongSn())) {
            queryWrapper.eq(LabdatahubDbConfig::getBelongSn, labdatahubDbConfig.getBelongSn());
        }
        if (StringUtils.isNotEmpty(labdatahubDbConfig.getCode())) {
            queryWrapper.like(LabdatahubDbConfig::getCode, labdatahubDbConfig.getCode());
        }
        Page<LabdatahubDbConfig> page = new Page<>(PageUtils.getPageNum(), PageUtils.getPageSize());
        Page<LabdatahubDbConfig> pageList = labdatahubDbConfigService.page(page, queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 获取database协议读取配置详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubDbConfigService.getById(id));
    }

    /**
     * 新增database协议读取配置
     */
    @Log(title = "database协议读取配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabdatahubDbConfig labdatahubDbConfig)
    {	
    	//更新
    	LabdatahubDbConfig updateEntity = new LabdatahubDbConfig();
    	updateEntity.setIntervalTime(labdatahubDbConfig.getIntervalTime());
    	updateEntity.setDelayTime(labdatahubDbConfig.getDelayTime());
    	LambdaUpdateWrapper<LabdatahubDbConfig> updateWrapper = new LambdaUpdateWrapper<>();
    	updateWrapper.eq(LabdatahubDbConfig::getBelongSn, labdatahubDbConfig.getBelongSn());
    	labdatahubDbConfigService.update(updateEntity, updateWrapper);
    	
        labdatahubDbConfig.setCreateTime(new Date());
        return toAjax(labdatahubDbConfigService.save(labdatahubDbConfig));
    }

    /**
     * 修改database协议读取配置
     */
    @Log(title = "database协议读取配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabdatahubDbConfig labdatahubDbConfig)
    {	
    	//更新
    	LabdatahubDbConfig updateEntity = new LabdatahubDbConfig();
    	updateEntity.setIntervalTime(labdatahubDbConfig.getIntervalTime());
    	updateEntity.setDelayTime(labdatahubDbConfig.getDelayTime());
    	LambdaUpdateWrapper<LabdatahubDbConfig> updateWrapper = new LambdaUpdateWrapper<>();
    	updateWrapper.eq(LabdatahubDbConfig::getBelongSn, labdatahubDbConfig.getBelongSn());
    	labdatahubDbConfigService.update(updateEntity, updateWrapper);
        return toAjax(labdatahubDbConfigService.updateById(labdatahubDbConfig));
    }

    /**
     * 删除database协议读取配置
     */
    @Log(title = "database协议读取配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(labdatahubDbConfigService.removeBatchByIds(Arrays.asList(ids)));
    }

    /**
     * 同步到所有设备
     */
    @PostMapping("/syncConfigToDevice")
    public AjaxResult syncConfigToDevice(@RequestParam String productSn){
        List<LabdatahubDbConfig> databaseConfigList = labdatahubDbConfigService.list(new LambdaQueryWrapper<LabdatahubDbConfig>()
                .eq(LabdatahubDbConfig::getBelongSn,productSn));
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        deviceList.forEach(device->{
            //删除设备所有规则
            labdatahubDbConfigService.remove(new LambdaUpdateWrapper<LabdatahubDbConfig>()
                    .eq(LabdatahubDbConfig::getBelongSn,device.getDeviceSn())
                    .eq(LabdatahubDbConfig::getBelongType,"1"));
            //添加新的规则
            databaseConfigList.forEach(config->{
                config.setId(IdWorker.getIdStr());
                config.setBelongSn(device.getDeviceSn());
                config.setBelongType("1");
                config.setCreateTime(new Date());
                config.setCode(config.getCode());
                config.setDelayTime(config.getDelayTime());
                config.setIntervalTime(config.getIntervalTime());
            });
            labdatahubDbConfigService.saveBatch(databaseConfigList);
            if(CollectionUtils.isNotEmpty(databaseConfigList)) {
            	if("1".equals(device.getModbusRead())){
            		DatabaseMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),null);
                    DatabaseReadConfig config = new DatabaseReadConfig();
                    config.setDeviceSn(device.getDeviceSn());
                    config.setDelayTime(databaseConfigList.get(0).getDelayTime() == null ? 0 : databaseConfigList.get(0).getDelayTime().intValue());
                    config.setIntervalTime(databaseConfigList.get(0).getIntervalTime() == null ? 1 : databaseConfigList.get(0).getIntervalTime().intValue());
                    DatabaseMessageScheduler.addReadConfig(device.getComponentId(),config);
                }else {
                	DatabaseMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),null);
                }
            }           
//            if("1".equals(device.getModbusRead())){
//                databaseConfigList.forEach(o->{
//                    DatabaseMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
//                    DatabaseReadConfig config = new DatabaseReadConfig();
//                    config.setDeviceSn(o.getBelongSn());
//                    config.setCode(o.getCode());
//                    config.setDelayTime(o.getDelayTime().intValue());
//                    config.setIntervalTime(o.getIntervalTime().intValue());;
//                    DatabaseMessageScheduler.addReadConfig(device.getComponentId(),config);
//                });
//            }else {
//                databaseConfigList.forEach(o->{
//                    DatabaseMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
//                });
//            }
            CacheUtils.updateDeviceWarnRule(device.getDeviceSn());
        });
        return AjaxResult.success();
    }

    /**
     * 数据读取开关
     * @param deviceSn 设备SN
     * @param isOpen 0-关闭 1-开启
     */
    @PostMapping("/readSwitchByDevice")
    public AjaxResult readSwitchByDevice(@RequestParam String deviceSn,@RequestParam String isOpen){
        LabdatahubDevice device = labdatahubDeviceService.getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getDeviceSn,deviceSn));
        if (device == null) {
            return AjaxResult.error("设备不存在：" + deviceSn);
        }
        List<LabdatahubDbConfig> databaseConfigList = labdatahubDbConfigService.list(new LambdaQueryWrapper<LabdatahubDbConfig>()
                .eq(LabdatahubDbConfig::getBelongSn,device.getDeviceSn()));
        if(CollectionUtils.isNotEmpty(databaseConfigList)) {
        	if("1".equals(isOpen)){
        		DatabaseMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),null);
                DatabaseReadConfig config = new DatabaseReadConfig();
                config.setDeviceSn(device.getDeviceSn());
                config.setDelayTime(databaseConfigList.get(0).getDelayTime() == null ? 0 : databaseConfigList.get(0).getDelayTime().intValue());
                config.setIntervalTime(databaseConfigList.get(0).getIntervalTime() == null ? 1 : databaseConfigList.get(0).getIntervalTime().intValue());
                DatabaseMessageScheduler.addReadConfig(device.getComponentId(),config);
            }else {
            	DatabaseMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),null);
            }
        }
//        if("1".equals(isOpen)){
//            list.forEach(o->{
//                DatabaseMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
//                DatabaseReadConfig config = new DatabaseReadConfig();
//                config.setDeviceSn(o.getBelongSn());
//                config.setCode(o.getCode());
//                config.setDelayTime(o.getDelayTime().intValue());
//                config.setIntervalTime(o.getIntervalTime().intValue());
//                DatabaseMessageScheduler.addReadConfig(device.getComponentId(),config);
//            });
//        }else {
//            list.forEach(o->{
//                DatabaseMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
//            });
//        }
        // 持久化读取开关状态，保证刷新页面后开关状态与实际读取一致
        device.setModbusRead(isOpen);
        labdatahubDeviceService.updateById(device);
        return AjaxResult.success("操作成功");
    }

    /**
     * 产品全部设备数据读取开关
     * @param productSn 产品SN
     * @param isOpen 0-关闭 1-开启
     */
    @PostMapping("/readSwitchByProduct")
    public AjaxResult readSwitchByProduct(@RequestParam String productSn,@RequestParam String isOpen){
        List<LabdatahubDevice> deviceList = labdatahubDeviceService.list(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getProductSn,productSn));
        deviceList.forEach(device->{
            List<LabdatahubDbConfig> databaseConfigList = labdatahubDbConfigService.list(new LambdaQueryWrapper<LabdatahubDbConfig>()
                    .eq(LabdatahubDbConfig::getBelongSn,device.getDeviceSn()));
            if(CollectionUtils.isNotEmpty(databaseConfigList)) {
            	if("1".equals(isOpen)){
            		DatabaseMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),null);
                    DatabaseReadConfig config = new DatabaseReadConfig();
                    config.setDeviceSn(device.getDeviceSn());
                    config.setDelayTime(databaseConfigList.get(0).getDelayTime() == null ? 0 : databaseConfigList.get(0).getDelayTime().intValue());
                    config.setIntervalTime(databaseConfigList.get(0).getIntervalTime() == null ? 1 : databaseConfigList.get(0).getIntervalTime().intValue());
                    DatabaseMessageScheduler.addReadConfig(device.getComponentId(),config);
                }else {
                	DatabaseMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),null);
                }
            }
//            if("1".equals(isOpen)){
//                list.forEach(o->{
//                    DatabaseMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
//                    DatabaseReadConfig config = new DatabaseReadConfig();
//                    config.setDeviceSn(o.getBelongSn());
//                    config.setCode(o.getCode());
//                    config.setDelayTime(o.getDelayTime().intValue());
//                    config.setIntervalTime(o.getIntervalTime().intValue());
//                    DatabaseMessageScheduler.addReadConfig(device.getComponentId(),config);
//                });
//            }else {
//                list.forEach(o->{
//                    DatabaseMessageScheduler.removeReadConfig(device.getComponentId(),device.getDeviceSn(),o.getCode());
//                });
//            }
            // 持久化读取开关状态，保证刷新页面后开关状态与实际读取一致
            device.setModbusRead(isOpen);
            labdatahubDeviceService.updateById(device);
        });
        return AjaxResult.success("操作成功");
    }
    
    /**
     * 获取表字段
     */
    @GetMapping("/listTableColumns")
    public AjaxResult listAllTables(String componentId) throws Exception {
    	DataSource ds = DatabaseConnectionManager.getDataSource(componentId);
        if (ds == null) {
            return AjaxResult.error("请先启动当前网络组件");
        }
    	List<Map<String, String>> columns = DatabaseReader.getTableColumns(componentId);
        return AjaxResult.success(columns);
    }
}
