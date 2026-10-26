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
import com.labdatahub.common.exception.ServiceException;
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
        // id 精确过滤：设备列表点组件名跳转过来时用（组件名称可能重名，名称模糊匹配不够）
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubComponent.getId()),
                LabdatahubComponent::getId, labdatahubComponent.getId());
        // 组件名称搜索（此前忽略所有查询参数，列表页的搜索框一直无效）
        queryWrapper.like(StringUtils.isNotEmpty(labdatahubComponent.getName()),
                LabdatahubComponent::getName, labdatahubComponent.getName());
        // 状态筛选：启用 / 停用
        queryWrapper.eq(StringUtils.isNotEmpty(labdatahubComponent.getStatus()),
                LabdatahubComponent::getStatus, labdatahubComponent.getStatus());
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
        checkEndpointNotUsed(labdatahubComponent);
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
        checkEndpointNotUsed(labdatahubComponent);
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
     * 校验「同一个端点」有没有被别的网络组件占用 —— 同一网络类型下，IP + 端口不允许重复。
     *
     * <p>为什么必须拦：组件是「一条到设备/服务的连接」。两个组件配同一个 IP:端口，就等于对同一台设备
     * 开了两份客户端，两个消费线程各自排队去抢同一个连接，数据会被切成两半、队列堆积丢消息
     * （2026-10-24 银申卡特 gx-115/gx-122 就是这么来的：产品上选错了组件，两台设备挤进同一个 FANUC 组件，
     * 队列长期满载、数据延迟 9 分钟）。这种配置在页面上没有任何提示，只能靠人事后发现。
     *
     * <p>判定口径（与 {@link #syncDisplayAddress} 取同一套键名）：
     * <ul>
     *   <li>只比 <b>同 netType</b> 的组件 —— 同一端口号上跑不同协议是合法的，例如三菱 3E 的
     *       {@code MITSUBISHI_MC3E_TCP} 与 {@code MITSUBISHI_MC3E_UDP} 可以用同一个端口号。</li>
     *   <li>数据库类组件额外比 {@code databaseName} —— 同一台数据库服务器、同一个端口下可以配多个库。</li>
     *   <li>MQTT 客户端 / 各类服务端组件不参与：它们没有 {@code ipAddr}（服务端绑 0.0.0.0，
     *       MQTT 客户端配的是 {@code brokerUrl}，同一 broker 上多个客户端订阅不同主题是正常用法）。</li>
     * </ul>
     *
     * @param target 待新增/修改的组件
     */
    private void checkEndpointNotUsed(LabdatahubComponent target) {
        if (target == null) {
            return;
        }
        String netType = StringUtils.trim(target.getNetType());
        String[] endpoint = resolveEndpoint(target.getOtherConfig());
        if (StringUtils.isEmpty(netType) || endpoint == null) {
            return;
        }
        String ip = endpoint[0];
        String port = endpoint[1];
        String databaseName = endpoint[2];
        for (LabdatahubComponent other : labdatahubComponentService.list()) {
            // 编辑自己时不能被自己挡住
            if (target.getId() != null && target.getId().equals(other.getId())) {
                continue;
            }
            if (!netType.equals(StringUtils.trim(other.getNetType()))) {
                continue;
            }
            String[] otherEndpoint = resolveEndpoint(other.getOtherConfig());
            if (otherEndpoint == null) {
                continue;
            }
            if (!ip.equalsIgnoreCase(otherEndpoint[0]) || !port.equals(otherEndpoint[1])) {
                continue;
            }
            // 数据库组件：同一台服务器上的不同库是两个端点，不算重复；库名为空则按端口算重复
            if (StringUtils.isNotEmpty(databaseName) && !databaseName.equalsIgnoreCase(otherEndpoint[2])) {
                continue;
            }
            throw new ServiceException(String.format(
                    "IP %s 端口 %s 已被网络组件「%s」（%s，%s）使用，同一个端点不能重复配置；"
                            + "如果就是要连这台设备，请直接在产品上引用那个组件，不要再建一个",
                    ip, port,
                    StringUtils.isNotEmpty(other.getName()) ? other.getName() : other.getId(),
                    other.getNetType(),
                    "1".equals(other.getStatus()) ? "已启用" : "已停用"));
        }
    }

    /**
     * 从 other_config 里取「目标端点」：{IP, 端口, 数据库名}。
     *
     * <p>键名与 {@link #syncDisplayAddress} 一致：客户端类组件用 {@code ipAddr} + {@code port}，
     * 少数用 {@code serverPort}；数据库类组件再取 {@code databaseName}。
     * 解析不了、没有 IP 或没有端口的一律返回 null —— 这类组件不参与重复校验，交给 openComponent 报错。
     */
    private static String[] resolveEndpoint(String otherConfig) {
        if (StringUtils.isEmpty(otherConfig)) {
            return null;
        }
        JSONObject config;
        try {
            config = JSONObject.parseObject(otherConfig);
        } catch (Exception e) {
            return null;
        }
        if (config == null) {
            return null;
        }
        String ip = StringUtils.trim(config.getString("ipAddr"));
        String port = StringUtils.trim(config.getString("port"));
        if (StringUtils.isEmpty(port)) {
            port = StringUtils.trim(config.getString("serverPort"));
        }
        if (StringUtils.isEmpty(ip) || StringUtils.isEmpty(port)) {
            return null;
        }
        return new String[] { ip, port, StringUtils.trim(config.getString("databaseName")) };
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
