//由AI修改
package com.labdatahub.business.utils;

import java.util.List;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.labdatahub.business.domain.LabdatahubComponent;
import com.labdatahub.business.domain.LabdatahubDevice;
import com.labdatahub.business.domain.LabdatahubMitsubishiMc3eConfig;
import com.labdatahub.business.domain.LabdatahubModbusConfig;
import com.labdatahub.business.domain.LabdatahubOmronFinsConfig;
import com.labdatahub.business.domain.LabdatahubProperties;
import com.labdatahub.business.domain.LabdatahubS71200Config;
import com.labdatahub.business.domain.PointWriteException;
import com.labdatahub.business.domain.PointWriteMeta;
import com.labdatahub.business.service.ILabdatahubComponentService;
import com.labdatahub.business.service.ILabdatahubDeviceService;
import com.labdatahub.business.service.ILabdatahubMitsubishiMc3eConfigService;
import com.labdatahub.business.service.ILabdatahubModbusConfigService;
import com.labdatahub.business.service.ILabdatahubOmronFinsConfigService;
import com.labdatahub.business.service.ILabdatahubS71200ConfigService;
import com.labdatahub.common.utils.StringUtils;
import com.labdatahub.common.utils.spring.SpringUtils;
import com.labdatahub.component.fins_tcp.FinsDataReader;
import com.labdatahub.component.fins_tcp.FinsTcpConfig;
import com.labdatahub.component.mitsubishi_mc3e_tcp.MitsubishiMc3eDataReader;
import com.labdatahub.component.mitsubishi_mc3e_tcp.MitsubishiMc3eTcpConfig;
import com.labdatahub.component.modbus_tcp.ModbusTcpConfig;
import com.labdatahub.component.modbus_tcp.RangeParserUtil;
import com.labdatahub.component.s7_tcp.S7TcpConfig;

/**
 * 写值匹配链路（方案 4.3）
 *
 * <p>deviceSn → 设备 → 组件 → 地址解析 → 从站号 → 防写串 → 协议点位 → 物模型类型，
 * 每一步都把算出来的东西填进 {@link PointWriteMeta}；中途失败时抛出
 * {@link PointWriteException} 并<b>带上当时那一份 meta</b>，让上层能拼出带地址的失败响应。
 *
 * <p>链路里只有三处「协议相关」：① 准入判断（哪些网络类型能写）；② other_config 解析成哪种
 * 连接配置；③ 取地址那张表（MODBUS 看 {@code labdatahub_modbus_config}，
 * S7 看 {@code labdatahub_s71200_config}，FINS 看 {@code labdatahub_omronfins_config}，
 * MC 3E 看 {@code labdatahub_mitsubishi_mc3e_config}）。第 ③ 项目前是 if/else 分派，
 * 四个协议还够用；再多就该抽成策略接口了 —— 但别为了「将来可能有七个协议」
 * 现在就把这段拆成七个类，读的人得跳七次才能拼出一条链路。
 *
 * <p>⚠️ 这个类<b>只服务写方向</b>，不去动读方向。读方向的地址解析散在
 * {@code TimerTask} 与 {@code ModbusDataReader} / {@code S7DataReader} 里，顺手重构过来很诱人，
 * 但那会把风险摊到整条采集链路上——而采集链路是现在唯一在正常工作的东西。
 */
public class PointWriteMetaResolver {

    /** MODBUS_TCP */
    private static final String NET_TYPE_MODBUS_TCP = "MODBUS_TCP";

    /** S7-1200（西门子 ISO-TCP，端口 102） */
    private static final String NET_TYPE_S71200_TCP = "S71200_TCP";

    /** 欧姆龙 FINS/TCP（端口 9600） */
    private static final String NET_TYPE_OMRONFINS_TCP = "OMRONFINS_TCP";

    /** 三菱 MC QnA 兼容 3E 帧（TCP，默认 5007） */
    private static final String NET_TYPE_MITSUBISHI_MC3E_TCP = "MITSUBISHI_MC3E_TCP";

    /** FINS 单条读/写命令的项数上限（读侧 length 与写侧 count 同一个上限） */
    private static final int FINS_MAX_ITEMS = 1000;
    /** FINS 位号上限（地址字段第 3 字节只有 0-15 有效） */
    private static final int FINS_MAX_BIT_ADDRESS = 15;

    /** MODBUS 读功能码 → 写功能码：03 保持寄存器 → 16 写多寄存器 */
    private static final String READ_FC_HOLDING = "03";
    private static final String READ_FC_COIL = "01";
    private static final String READ_FC_DISCRETE = "02";
    private static final String READ_FC_INPUT = "04";

    private PointWriteMetaResolver() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 走一遍匹配链路
     *
     * @param deviceSn  设备编码
     * @param code      点位标识
     * @param requestId 服务端生成的追踪标识（先于匹配生成，失败响应里也要给）
     * @return 填好的写值元数据
     * @throws PointWriteException 匹配失败（400/404/409/422/500），异常内带已匹配到的部分 meta
     */
    public static PointWriteMeta resolve(String deviceSn, String code, String requestId) {
        PointWriteMeta meta = new PointWriteMeta();
        meta.setRequestId(requestId);

        // 1. 参数
        if (StringUtils.isEmpty(deviceSn)) {
            throw new PointWriteException(400, "参数缺失：deviceSn 不能为空", meta);
        }
        if (StringUtils.isEmpty(code)) {
            throw new PointWriteException(400, "参数缺失：code 不能为空", meta);
        }
        meta.setDeviceSn(deviceSn);
        meta.setCode(code);

        // 2. 设备
        ILabdatahubDeviceService deviceService = SpringUtils.getBean(ILabdatahubDeviceService.class);
        LabdatahubDevice device = deviceService.getOne(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getDeviceSn, deviceSn), false);
        if (device == null) {
            throw new PointWriteException(404, "设备不存在：" + deviceSn, meta);
        }
        meta.setDeviceName(device.getDeviceName());

        // 3. 组件
        if (StringUtils.isEmpty(device.getComponentId())) {
            throw new PointWriteException(409, "设备未绑定网络组件：" + deviceSn, meta);
        }
        meta.setComponentId(device.getComponentId());

        ILabdatahubComponentService componentService = SpringUtils.getBean(ILabdatahubComponentService.class);
        LabdatahubComponent component = componentService.getById(device.getComponentId());
        if (component == null) {
            throw new PointWriteException(409, "设备绑定的网络组件不存在：" + device.getComponentId(), meta);
        }
        meta.setNetType(component.getNetType());

        // 4. 网络类型准入：只放行已实现写值的四个协议，其余一律 409
        String netType = component.getNetType() == null ? "" : component.getNetType().trim();
        boolean isModbus = NET_TYPE_MODBUS_TCP.equals(netType);
        boolean isS7 = NET_TYPE_S71200_TCP.equals(netType);
        boolean isFins = NET_TYPE_OMRONFINS_TCP.equals(netType);
        boolean isMc3e = NET_TYPE_MITSUBISHI_MC3E_TCP.equals(netType);
        if (!isModbus && !isS7 && !isFins && !isMc3e) {
            throw new PointWriteException(409, "该接口暂只支持 " + NET_TYPE_MODBUS_TCP + " / "
                    + NET_TYPE_S71200_TCP + " / " + NET_TYPE_OMRONFINS_TCP + " / "
                    + NET_TYPE_MITSUBISHI_MC3E_TCP
                    + " 协议，当前设备所在组件的网络类型为：" + component.getNetType(), meta);
        }

        // 5. 地址真源是 other_config，不是 ip_addr / port 两列
        //    （那两列 7 个组件里 5 个是 NULL，MODBUS 组件正是其中之一；有值的 BROTHER 组件
        //     两处地址还互相矛盾。拿列做校验会把唯一具备写能力的设备直接挡死。）
        if (StringUtils.isEmpty(component.getOtherConfig())) {
            throw new PointWriteException(409, "网络组件未配置地址：other_config 为空", meta);
        }
        fillEndpoint(meta, component.getOtherConfig(), netType);

        // 6. 从站号：jamod 的 setUnitID 收 int，传 null 拆箱 NPE，必须在这里拦住。
        //    ⚠️ 仅 MODBUS 需要 —— S7 报文里没有从站号，S7 设备的 slave_id 实测就是 NULL，
        //    照搬这条校验会把所有西门子设备挡死在 409。
        if (isModbus) {
            if (device.getSlaveId() == null) {
                throw new PointWriteException(409, "设备未配置从站ID（slave_id 为空），请在设备详情页补全", meta);
            }
            meta.setSlaveId(device.getSlaveId());
        }

        // 7. 协议id：反射调 encode 的 key，取自组件（与指令下发链路一致）
        meta.setProtocolId(component.getProtocolId());

        // 8. 防写串设备。产品:设备:组件 = 1:1 是业务约定不是数据库约束，表结构不阻止
        //    两台设备指向同一个组件；真出现这种数据，按 deviceSn 匹配出的地址会落到另一台机器上。
        //    ⚠️ 定位是「配置错误的保险」，不是安全控制——本接口不做鉴权，它拦不住有意的调用方。
        long otherCount = deviceService.count(new LambdaQueryWrapper<LabdatahubDevice>()
                .eq(LabdatahubDevice::getComponentId, device.getComponentId())
                .ne(LabdatahubDevice::getId, device.getId()));
        if (otherCount > 0) {
            throw new PointWriteException(409, "网络组件被多台设备共用（另有 " + otherCount
                    + " 台），无法确定写入目标设备，请检查设备配置", meta);
        }

        // 9-11. 【协议相关】地址与写入长度：查点位表 → 定地址 → 定字节/字个数
        if (isModbus) {
            fillModbusPoint(meta, deviceSn, code);
        } else if (isS7) {
            fillS7Point(meta, deviceSn, code);
        } else if (isFins) {
            fillFinsPoint(meta, deviceSn, code);
        } else {
            fillMitsubishiMc3ePoint(meta, deviceSn, code);
        }

        // 12. 【通用】类型：与读方向同源，不重复实现
        LabdatahubProperties property = ParseMetaUtils.resolve(deviceSn, device.getProductSn(), code);
        if (property == null) {
            throw new PointWriteException(404, "点位类型未定义：物模型里没有 identifier=" + code
                    + " 的属性（设备 " + deviceSn + " 与其产品下都没有）", meta);
        }
        meta.setDataType(property.getDataType());
        meta.setByteOrder(property.getByteOrder());
        meta.setIsSigned(property.getIsSigned());
        meta.setScale(property.getScale());
        meta.setOffset(property.getOffset());

        return meta;
    }

    /**
     * 解析 other_config 里的连接地址，填 ipAddr / port / endpoint
     *
     * <p>四种协议的地址字段名恰好一样（ipAddr / port），差别只在连接参数（S7 多 rack / slot，
     * FINS 多 clientNodeAddress）。这里只取地址做回显，真正的连接由组件层的连接管理器
     * 按自己的配置类建。
     *
     * <p>用各自的 {@code XxxTcpConfig} 反序列化而不是直接读 JSON 键：字段名一旦在组件层
     * 改过（如 FINS 的 clientNodeAddress），这里跟着类走就不会漏。
     */
    private static void fillEndpoint(PointWriteMeta meta, String otherConfig, String netType) {
        String ipAddr;
        Integer port;
        try {
            JSONObject json = JSONObject.parseObject(otherConfig);
            if (NET_TYPE_MODBUS_TCP.equals(netType)) {
                ModbusTcpConfig tcpConfig = json.toJavaObject(ModbusTcpConfig.class);
                ipAddr = tcpConfig == null ? null : tcpConfig.getIpAddr();
                port = tcpConfig == null ? null : tcpConfig.getPort();
            } else if (NET_TYPE_S71200_TCP.equals(netType)) {
                S7TcpConfig tcpConfig = json.toJavaObject(S7TcpConfig.class);
                ipAddr = tcpConfig == null ? null : tcpConfig.getIpAddr();
                port = tcpConfig == null ? null : tcpConfig.getPort();
            } else if (NET_TYPE_OMRONFINS_TCP.equals(netType)) {
                FinsTcpConfig tcpConfig = json.toJavaObject(FinsTcpConfig.class);
                ipAddr = tcpConfig == null ? null : tcpConfig.getIpAddr();
                port = tcpConfig == null ? null : tcpConfig.getPort();
            } else {
                MitsubishiMc3eTcpConfig tcpConfig = json.toJavaObject(MitsubishiMc3eTcpConfig.class);
                ipAddr = tcpConfig == null ? null : tcpConfig.getIpAddr();
                port = tcpConfig == null ? null : tcpConfig.getPort();
            }
        } catch (Exception e) {
            throw new PointWriteException(409, "网络组件地址配置无法解析：" + e.getMessage(), meta);
        }
        if (StringUtils.isEmpty(ipAddr) || port == null) {
            throw new PointWriteException(409, "网络组件未配置地址：other_config 里 ipAddr / port 缺失", meta);
        }
        meta.setIpAddr(ipAddr);
        meta.setPort(port);
        meta.setEndpoint(ipAddr + ":" + port);
    }

    /**
     * MODBUS 点位：地址真源 {@code labdatahub_modbus_config}（belong_sn + code），
     * 由 register_range 解出起始寄存器与寄存器个数
     */
    private static void fillModbusPoint(PointWriteMeta meta, String deviceSn, String code) {
        ILabdatahubModbusConfigService modbusConfigService = SpringUtils.getBean(ILabdatahubModbusConfigService.class);
        LabdatahubModbusConfig modbusConfig = modbusConfigService.getOne(new LambdaQueryWrapper<LabdatahubModbusConfig>()
                .eq(LabdatahubModbusConfig::getBelongSn, deviceSn)
                .eq(LabdatahubModbusConfig::getCode, code), false);
        if (modbusConfig == null) {
            // 注意：产品级点位（belong_sn = 产品SN）在这里查不到，会走到这个 404 —— 本期不支持，
            // 报错信息里点明 code，便于调用方区分「写错了」和「这是个产品级点位」。
            throw new PointWriteException(404, "点位不存在：设备 " + deviceSn
                    + " 下没有 code=" + code + " 的协议点位配置（产品级点位本期不支持写入）", meta);
        }
        meta.setPointName(modbusConfig.getName());
        meta.setRegisterRange(modbusConfig.getRegisterRange());

        // 可写性推导：由读功能码推写功能码，不新增「可写标志」列
        String readFunctionCode = modbusConfig.getFunctionCode();
        // 与读方向保持一致：ModbusDataReader.readByFunction 里就有 if (functionCode == null) functionCode = 3;
        if (StringUtils.isEmpty(readFunctionCode)) {
            readFunctionCode = READ_FC_HOLDING;
        }
        readFunctionCode = readFunctionCode.trim();
        meta.setReadFunctionCode(readFunctionCode);

        if (READ_FC_COIL.equals(readFunctionCode)) {
            throw new PointWriteException(422, "点位 " + code + " 的读功能码是 01线圈，"
                    + "对应的写功能码 15（写多线圈）本期暂不支持", meta);
        }
        if (READ_FC_DISCRETE.equals(readFunctionCode) || READ_FC_INPUT.equals(readFunctionCode)) {
            // 02/04 不是业务判断，是 Modbus 规范里根本没有对应的写操作——物理上不可写
            throw new PointWriteException(422, "点位 " + code + " 的读功能码是 " + readFunctionCode
                    + "（" + (READ_FC_DISCRETE.equals(readFunctionCode) ? "离散输入" : "输入寄存器")
                    + "），Modbus 规范中没有对应的写操作，该点位不可写", meta);
        }
        if (!READ_FC_HOLDING.equals(readFunctionCode)) {
            throw new PointWriteException(422, "点位 " + code + " 的读功能码 " + readFunctionCode
                    + " 不支持写入，仅支持 03保持寄存器", meta);
        }
        meta.setWriteFunctionCode(16);

        if (StringUtils.isEmpty(modbusConfig.getRegisterRange())) {
            throw new PointWriteException(422, "点位 " + code + " 未配置寄存器范围", meta);
        }
        List<RangeParserUtil.RangeItem> itemList;
        try {
            itemList = RangeParserUtil.parse(modbusConfig.getRegisterRange());
        } catch (IllegalArgumentException e) {
            throw new PointWriteException(422, "点位 " + code + " 的寄存器范围格式非法："
                    + modbusConfig.getRegisterRange() + "（" + e.getMessage() + "）", meta);
        }
        // 多区间写方向的语义不明确（写哪个区间的值？全写？），直接拒绝
        if (itemList.size() != 1) {
            throw new PointWriteException(422, "点位 " + code + " 的寄存器范围 \"" + modbusConfig.getRegisterRange()
                    + "\" 含 " + itemList.size() + " 个子区间，写入要求单个连续区间", meta);
        }
        RangeParserUtil.RangeItem rangeItem = itemList.get(0);
        meta.setAddress(rangeItem.getStart());
        meta.setCount(rangeItem.getCount());
    }

    /**
     * S7 点位：地址真源 {@code labdatahub_s71200_config}（belong_sn + code），
     * 地址 = startAddress（起始<b>字节</b>偏移，不是寄存器），长度由块类型决定
     *
     * <p>不校验「块类型 vs 物模型类型」是否搭配 —— 那是协议层 {@code S71200TcpDeal.encode}
     * 的活，它拿得到块类型也拿得到 dataType，在这里重复一遍只会两边不一致。
     * 这里只取配置、定长度，配置本身缺项（块类型为空等）才是 422。
     */
    private static void fillS7Point(PointWriteMeta meta, String deviceSn, String code) {
        ILabdatahubS71200ConfigService configService = SpringUtils.getBean(ILabdatahubS71200ConfigService.class);
        LabdatahubS71200Config config = configService.getOne(new LambdaQueryWrapper<LabdatahubS71200Config>()
                .eq(LabdatahubS71200Config::getBelongSn, deviceSn)
                .eq(LabdatahubS71200Config::getCode, code), false);
        if (config == null) {
            throw new PointWriteException(404, "点位不存在：设备 " + deviceSn
                    + " 下没有 code=" + code + " 的协议点位配置（S71200，产品级点位本期不支持写入）", meta);
        }
        meta.setPointName(config.getName());
        meta.setAreaType(config.getAreaType());
        meta.setDbNumber(config.getDbNumber());
        meta.setBlockType(config.getBlockType());
        meta.setBitOffset(config.getBitOffset());
        meta.setLength(config.getLength());
        // S7 没有功能码，置 null：响应里这一项自然不输出（PointWriteResult 是 NON_NULL）
        meta.setWriteFunctionCode(null);

        if (config.getStartAddress() == null) {
            throw new PointWriteException(422, "点位 " + code + " 未配置起始地址（start_address 为空）", meta);
        }
        meta.setAddress(config.getStartAddress());

        String blockType = config.getBlockType() == null ? "" : config.getBlockType().trim().toUpperCase();
        if (blockType.isEmpty()) {
            throw new PointWriteException(422, "点位 " + code + " 未配置块类型（block_type 为空），无法确定写入长度", meta);
        }
        // count 在 S7 下语义是「字节数」，不是寄存器个数
        switch (blockType) {
            case "DBX":
                meta.setCount(1);
                break;
            case "DBW":
                meta.setCount(2);
                break;
            case "DBD":
                meta.setCount(4);
                break;
            case "DBB":
                if (config.getLength() == null) {
                    throw new PointWriteException(422, "点位 " + code + " 是字符串块（DBB），但未配置读取长度（length）", meta);
                }
                meta.setCount(config.getLength());
                break;
            default:
                throw new PointWriteException(422, "点位 " + code + " 的块类型 " + config.getBlockType()
                        + " 不支持写入（只支持 DBX/DBW/DBD/DBB）", meta);
        }
    }

    /**
     * FINS 点位：地址真源 {@code labdatahub_omronfins_config}（belong_sn + code），
     * 地址 = startAddress（起始<b>字</b>地址，不是字节），长度 = length（字区=字个数，位区恒为 1）
     *
     * <p><b>字区/位区的判据是区码本身</b>（{@code FinsDataReader.isBitArea}，与协议库侧的
     * {@code OmronFinsTcpDeal.isBitArea} 同源），位号是区码之外必须同时对上的一项：
     * 位区没位号、字区带了位号，都是配置矛盾，一律 422。这两种情况设备<b>不会报错</b> ——
     * FINS 的地址第三字节对字访问本该恒为 0，配错了只是把值静默写到别的位上，
     * 比报错难查得多，所以要在写入之前拦住。
     *
     * <p>不校验「区码 vs 物模型类型」是否搭配（如 bool 配字区）——那是协议层
     * {@code OmronFinsTcpDeal.encode} 的活，它拿得到区码也拿得到 dataType，
     * 在这里重复一遍只会两边不一致。这里只取配置、定地址与长度。
     */
    private static void fillFinsPoint(PointWriteMeta meta, String deviceSn, String code) {
        ILabdatahubOmronFinsConfigService configService =
                SpringUtils.getBean(ILabdatahubOmronFinsConfigService.class);
        LabdatahubOmronFinsConfig config = configService.getOne(new LambdaQueryWrapper<LabdatahubOmronFinsConfig>()
                .eq(LabdatahubOmronFinsConfig::getBelongSn, deviceSn)
                .eq(LabdatahubOmronFinsConfig::getCode, code), false);
        if (config == null) {
            throw new PointWriteException(404, "点位不存在：设备 " + deviceSn
                    + " 下没有 code=" + code + " 的协议点位配置（OMRONFINS，产品级点位本期不支持写入）", meta);
        }
        meta.setPointName(config.getName());
        meta.setAreaCode(config.getAreaCode());
        meta.setBitAddress(config.getBitAddress());
        // FINS 无功能码，置 null：响应里这一项自然不输出（PointWriteResult 是 NON_NULL）
        meta.setWriteFunctionCode(null);

        if (config.getAreaCode() == null) {
            throw new PointWriteException(422, "点位 " + code + " 未配置存储区码（area_code 为空）", meta);
        }
        if (config.getStartAddress() == null) {
            throw new PointWriteException(422, "点位 " + code + " 未配置起始地址（start_address 为空）", meta);
        }
        meta.setAddress(config.getStartAddress());

        boolean bitArea = FinsDataReader.isBitArea(config.getAreaCode());
        if (bitArea) {
            // 位区：一个点位就是一个位，count 恒 1（协议层 encode 也按这个约定拒绝 count != 1）
            if (config.getBitAddress() == null) {
                throw new PointWriteException(422, "点位 " + code + " 的存储区码 0x"
                        + Integer.toHexString(config.getAreaCode())
                        + " 是位区，但未配置位号（bit_address 为空）", meta);
            }
            if (config.getBitAddress() < 0 || config.getBitAddress() > FINS_MAX_BIT_ADDRESS) {
                throw new PointWriteException(422, "点位 " + code + " 的位号 " + config.getBitAddress()
                        + " 超出 0-" + FINS_MAX_BIT_ADDRESS + " 范围", meta);
            }
            if (config.getLength() != null && config.getLength() != 1) {
                throw new PointWriteException(422, "点位 " + code + " 是位区点位，读取长度必须为 1，当前为 "
                        + config.getLength(), meta);
            }
            meta.setCount(1);
        } else {
            if (config.getBitAddress() != null) {
                throw new PointWriteException(422, "点位 " + code + " 的存储区码 0x"
                        + Integer.toHexString(config.getAreaCode())
                        + " 是字区，却配置了位号 " + config.getBitAddress() + "（配置矛盾，拒绝写入）", meta);
            }
            if (config.getLength() == null || config.getLength() < 1) {
                throw new PointWriteException(422, "点位 " + code + " 未配置读取长度（length 为空或非正），无法确定写入字数", meta);
            }
            if (config.getLength() > FINS_MAX_ITEMS) {
                throw new PointWriteException(422, "点位 " + code + " 的读取长度 " + config.getLength()
                        + " 超出 FINS 单条命令上限 " + FINS_MAX_ITEMS, meta);
            }
            // 字区的 count 就是读取的字个数：读几个字就写几个字，与读方向严格对称
            meta.setCount(config.getLength());
        }
    }

    /**
     * 三菱 MC 3E 点位：地址真源 {@code labdatahub_mitsubishi_mc3e_config}（belong_sn + code），
     * 地址 = startAddress（X/Y 是八进制写法，与配置里填的一致，换算在组件层
     * {@code MitsubishiMc3eDataReader.toRealAddress} 做），长度 = length
     * （字设备=字数，位设备恒为 1）
     *
     * <p><b>字设备/位设备的判据是软元件码本身</b>（{@code MitsubishiMc3eDataReader.isWordDevice}，
     * 与协议库侧 {@code MitsubishiMc3eTcpDeal} 的码表同源）。这里<b>只用它决定 count 规则</b>，
     * 不当「码表是否合法」的裁判 —— 认不出来的码由协议库直接抛
     * {@code IllegalArgumentException}（经反射还原为 422）。两边判据万一不一致，
     * 结果也是「协议库拒掉」，不会出现「两边都收下但编码方式不同」的静默写错。
     *
     * <p>不校验「软元件码 vs 物模型类型」是否搭配（如 bool 配 D 区）——那是协议层
     * {@code MitsubishiMc3eTcpDeal.encode} 的活，它拿得到软元件码也拿得到 dataType，
     * 在这里重复一遍只会两边不一致。这里只取配置、定地址与点数。
     */
    private static void fillMitsubishiMc3ePoint(PointWriteMeta meta, String deviceSn, String code) {
        ILabdatahubMitsubishiMc3eConfigService configService =
                SpringUtils.getBean(ILabdatahubMitsubishiMc3eConfigService.class);
        LabdatahubMitsubishiMc3eConfig config = configService.getOne(
                new LambdaQueryWrapper<LabdatahubMitsubishiMc3eConfig>()
                        .eq(LabdatahubMitsubishiMc3eConfig::getBelongSn, deviceSn)
                        .eq(LabdatahubMitsubishiMc3eConfig::getCode, code), false);
        if (config == null) {
            throw new PointWriteException(404, "点位不存在：设备 " + deviceSn
                    + " 下没有 code=" + code + " 的协议点位配置（MITSUBISHI_MC3E_TCP，产品级点位本期不支持写入）", meta);
        }
        meta.setPointName(config.getName());
        // MC 3E 没有 Modbus 那种功能码：字写/位写都是批量写命令 0x1401，只有子命令不同，
        // 拿它当「功能码」回显只会误导。置 null，响应里这一项自然不输出（PointWriteResult 是 NON_NULL）
        meta.setWriteFunctionCode(null);

        if (config.getAreaCode() == null) {
            throw new PointWriteException(422, "点位 " + code + " 未配置软元件码（area_code 为空）", meta);
        }
        meta.setAreaCode(config.getAreaCode());

        if (config.getStartAddress() == null) {
            throw new PointWriteException(422, "点位 " + code + " 未配置起始地址（start_address 为空）", meta);
        }
        if (config.getStartAddress() < 0) {
            throw new PointWriteException(422, "点位 " + code + " 的起始地址不能为负数："
                    + config.getStartAddress(), meta);
        }
        meta.setAddress(config.getStartAddress());

        if (MitsubishiMc3eDataReader.isWordDevice(config.getAreaCode())) {
            if (config.getLength() == null || config.getLength() < 1) {
                throw new PointWriteException(422, "点位 " + code + " 未配置读取数量（length 为空或非正），无法确定写入字数", meta);
            }
            if (config.getLength() > MitsubishiMc3eDataReader.MAX_WORD_COUNT) {
                throw new PointWriteException(422, "点位 " + code + " 的读取数量 " + config.getLength()
                        + " 超出 MC 字设备单条命令上限 " + MitsubishiMc3eDataReader.MAX_WORD_COUNT, meta);
            }
            // 字设备的 count 就是读取的字个数：读几个字就写几个字，与读方向严格对称
            meta.setCount(config.getLength());
        } else {
            // 位设备：一个点位就是一个（或一段）位，而写方向一次只写一个位
            // （协议层 encode 也按这个约定拒绝 count != 1）。
            // 与其「按 length 写 N 个位都置同值」这种要靠猜的语义，不如把配置矛盾在写入前挑明。
            if (config.getLength() != null && config.getLength() != 1) {
                throw new PointWriteException(422, "点位 " + code + " 是位设备（软元件码 0x"
                        + Integer.toHexString(config.getAreaCode()) + "），写入要求读取数量为 1，当前为 "
                        + config.getLength() + "（请把点位配置的读取数量改为 1）", meta);
            }
            meta.setCount(1);
        }
    }
}
