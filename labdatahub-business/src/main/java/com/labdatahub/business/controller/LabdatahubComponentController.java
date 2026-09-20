package com.labdatahub.business.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.labdatahub.business.domain.LabdatahubComponent;
import com.labdatahub.business.domain.LabdatahubProduct;
import com.labdatahub.business.domain.LabdatahubProtocol;
import com.labdatahub.business.service.ILabdatahubComponentService;
import com.labdatahub.business.service.ILabdatahubProductService;
import com.labdatahub.business.service.ILabdatahubProtocolService;
import com.labdatahub.business.utils.CacheUtils;
import com.labdatahub.common.core.controller.BaseController;
import com.labdatahub.common.core.domain.AjaxResult;
import com.labdatahub.common.core.page.TableDataInfo;
import com.labdatahub.common.utils.PageUtils;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.poi.ExcelUtil;
import com.labdatahub.common.utils.uuid.IdUtils;
import com.labdatahub.component.db.DatabaseConfig;
import com.labdatahub.component.db.DatabaseConnectionManager;
import com.labdatahub.component.db.DatabaseReader;
import com.labdatahub.component.utils.PortChecker;

/**
 * 网络组件Controller
 *
 * @author labdatahub
 * @date 2025-09-18
 */
@RestController
@RequestMapping("/business/component")
public class LabdatahubComponentController extends BaseController
{
    @Autowired
    private ILabdatahubComponentService labdatahubComponentService;
    @Autowired
    private ILabdatahubProtocolService labdatahubProtocolService;
    @Autowired
    private ILabdatahubProductService labdatahubProductService;
    /**
     * 查询网络组件列表
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('business:component:list')")
    public TableDataInfo list(LabdatahubComponent labdatahubComponent)
    {
    	LambdaQueryWrapper<LabdatahubComponent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(LabdatahubComponent::getCreateTime);
        Page<LabdatahubComponent> page = new Page<LabdatahubComponent>(PageUtils.getPageNum(),PageUtils.getPageSize());
        Page<LabdatahubComponent> pageList = labdatahubComponentService.page(page,queryWrapper);
        return getDataTable(pageList);
    }

    /**
     * 导出网络组件列表
     */
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabdatahubComponent labdatahubComponent)
    {
        List<LabdatahubComponent> list = labdatahubComponentService.selectLabdatahubComponentList(labdatahubComponent);
        ExcelUtil<LabdatahubComponent> util = new ExcelUtil<LabdatahubComponent>(LabdatahubComponent.class);
        util.exportExcel(response, list, "网络组件数据");
    }

    /**
     * 获取网络组件详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(labdatahubComponentService.getById(id));
    }

    /**
     * 新增网络组件
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult add(@RequestBody LabdatahubComponent labdatahubComponent) throws Exception {
    	labdatahubComponent.setCreateTime(new Date());
        syncDisplayAddress(labdatahubComponent);
        labdatahubComponentService.save(labdatahubComponent);
        CacheUtils.setComponentCache(labdatahubComponent.getId(),labdatahubComponent);
        if("1".equals(labdatahubComponent.getStatus())) {
            boolean isOk = labdatahubComponentService.openComponent(labdatahubComponent.getId());
            if (!isOk) {
                return AjaxResult.error("开启失败，请检查参数是否错误，端口是否占用");
            }
            if (StringUtils.isNotEmpty(labdatahubComponent.getProtocolId())) {
                bindProtocol(labdatahubComponent.getId(), labdatahubComponent.getProtocolId());
            }
        }
        return success();
    }

    /**
     * 修改网络组件
     */
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult edit(@RequestBody LabdatahubComponent labdatahubComponent) throws Exception {
        syncDisplayAddress(labdatahubComponent);
        labdatahubComponentService.updateById(labdatahubComponent);
        if("1".equals(labdatahubComponent.getStatus())) {
            labdatahubComponentService.closeComponent(labdatahubComponent.getId());
            boolean isOk = labdatahubComponentService.openComponent(labdatahubComponent.getId());
            if (!isOk) {
                return AjaxResult.error("修改失败，请检查参数是否错误，端口是否占用");
            }
            if(StringUtils.isNotEmpty(labdatahubComponent.getProtocolId())){
                bindProtocol(labdatahubComponent.getId(),labdatahubComponent.getProtocolId());
            }
        }else {
            labdatahubComponentService.closeComponent(labdatahubComponent.getId());
        }
        return AjaxResult.success();
    }

    /**
     * 删除网络组件
     */
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        //先检查是否已经关闭
        List<String> idList = Arrays.asList(ids);
        long openCount = labdatahubComponentService.count(new LambdaQueryWrapper<LabdatahubComponent>()
                .in(LabdatahubComponent::getId,idList)
                .eq(LabdatahubComponent::getStatus,"1"));
        if(openCount>0){
            return AjaxResult.error("请先停用网络组件再删除");
        }
        for (int i = 0; i < idList.size(); i++) {
            long count = labdatahubProductService.count(new LambdaQueryWrapper<LabdatahubProduct>()
                    .eq(LabdatahubProduct::getComponentId,idList.get(i)));
            if(count>0){
                return AjaxResult.error("网络组件已经被使用");
            }
        }
        labdatahubComponentService.removeBatchByIds(idList);
        idList.forEach(CacheUtils::removeComponentCache);
        return AjaxResult.success("删除成功");
    }

    /**
     * 开关网络组件
     * @param status 0-关 1-开
     */
    @PutMapping("/control")
    public AjaxResult controlComponent(@RequestParam String id,
                                       @RequestParam String status) throws Exception {
        if("0".equals(status)){
            labdatahubComponentService.closeComponent(id);
        }else {
            // 开启结果必须回传：原先无论成败都回 success()，页面弹「操作成功」而组件其实没开起来
            // （openComponent 返回 false 时既没抛异常也没置 status=1，前端只能靠这句话判断）
            boolean isOk = labdatahubComponentService.openComponent(id);
            if(!isOk){
                return AjaxResult.error("开启失败，请检查配置信息是否正确");
            }
        }
        return AjaxResult.success();
    }

    /**
     * 把动态配置里的地址回写到 ip_addr / port 两个展示列。
     *
     * <p>这两列是<b>派生展示值</b>（列表卡片上的「地址」「端口」直接读它们），原先只在
     * {@code openComponent} 连接<b>成功</b>之后才回写 —— 于是「改了 IP 之后组件没启动、或启动失败」
     * 时列表永远显示旧地址：库里 {@code other_config.ipAddr} 已是新值、{@code ip_addr} 还是老值，
     * 页面上看着像"改了没生效"，而真正生效的偏偏是不显示的那个。
     * 保存时就同步，展示的地址始终等于下次启动会用的地址。
     *
     * <p>键名按各 netType 的动态配置取（与前端表单、各 XxxConfig 的字段名一致）：
     * TCP/PLC 类用 {@code ipAddr}+{@code port}，数据库用 {@code ipAddr}+{@code port}，
     * MQTT 服务端用 {@code tcpPort}/{@code wsPort}（无 IP，绑定 0.0.0.0，交给 openComponent 回写），
     * HTTP/COAP/WS/TCP/UDP 服务端只有 {@code port}/{@code serverPort}。
     * 取不到的值不动原列，不凭空造地址。
     */
    private void syncDisplayAddress(LabdatahubComponent component) {
        if (component == null || StringUtils.isEmpty(component.getOtherConfig())) {
            return;
        }
        JSONObject config;
        try {
            config = JSONObject.parseObject(component.getOtherConfig());
        } catch (Exception e) {
            // 动态配置不是合法 JSON：保持原列不动，交给 openComponent 解析时报错
            return;
        }
        if (config == null) {
            return;
        }
        String ipAddr = config.getString("ipAddr");
        if (StringUtils.isEmpty(ipAddr)) {
            ipAddr = config.getString("brokerUrl");
        }
        if (StringUtils.isNotEmpty(ipAddr)) {
            component.setIpAddr(ipAddr);
        }
        String port = config.getString("port");
        if (StringUtils.isEmpty(port)) {
            port = config.getString("serverPort");
        }
        if (StringUtils.isEmpty(port)) {
            // MQTT 服务端：端口拆在 tcpPort / wsPort 两个键里，合并写法与 openComponent 一致
            String tcpPort = config.getString("tcpPort");
            if (StringUtils.isNotEmpty(tcpPort)) {
                String wsPort = config.getString("wsPort");
                port = StringUtils.isNotEmpty(wsPort) ? tcpPort + "," + wsPort : tcpPort;
            }
        }
        if (StringUtils.isNotEmpty(port)) {
            component.setPort(port);
        }
    }

    /**
     * 绑定协议
     * @param componentId 组件id
     * @param protocolId 协议id
     */
    @PutMapping("/bindProtocol")
    public AjaxResult bindProtocol(String componentId,String protocolId){
        if(StringUtils.isNotEmpty(componentId)&&StringUtils.isNotEmpty(protocolId)){
            LabdatahubComponent component = labdatahubComponentService.getById(componentId);
            if(component==null){
                return AjaxResult.error("网络组件不存在");
            }
            labdatahubComponentService.update(new LambdaUpdateWrapper<LabdatahubComponent>()
                    .eq(LabdatahubComponent::getId,componentId)
                    .set(LabdatahubComponent::getProtocolId,protocolId));
            component.setProtocolId(protocolId);
            CacheUtils.setComponentCache(component.getId(),component);
            labdatahubProtocolService.update(new LambdaUpdateWrapper<LabdatahubProtocol>()
                    .eq(LabdatahubProtocol::getId,protocolId)
                    .set(LabdatahubProtocol::getComponentId,componentId)
                    .set(LabdatahubProtocol::getComponentName,component.getName()));
            return AjaxResult.success("绑定成功");
        }else {
            return AjaxResult.error("请选择正确的网络组件和协议");
        }
    }

    /**
     * 检查端口占用
     */
    @PostMapping("/checkPort")
    public AjaxResult checkPort(@RequestParam Integer port){
        if(PortChecker.isLocalPortAvailable(port)){
            return AjaxResult.success("端口处于空闲状态");
        }else {
            return AjaxResult.error("端口已被占用，请选择其他端口");
        }
    }
    
    /**
     * 获取表名
     */
    @PostMapping("/listAllTables")
    public AjaxResult listAllTables(@RequestBody String otherConfig) throws Exception {
    	DatabaseConfig config = JSONObject.parseObject(otherConfig).toJavaObject(DatabaseConfig.class);
    	String tempId = IdUtils.simpleUUID();
    	boolean isOk = DatabaseConnectionManager.addConnection(tempId, config);
        if(!isOk){
            return AjaxResult.error("数据库连接失败");
        }
        List<String> tables = DatabaseReader.getAllTables(tempId);
        DatabaseConnectionManager.closeConnection(tempId);
        return AjaxResult.success(tables);
    }

}
