//由AI修改
package com.labdatahub.business.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.labdatahub.business.domain.OpenApiEncodeKeys;
import com.labdatahub.business.domain.PointWriteException;
import com.labdatahub.business.domain.PointWriteMeta;
import com.labdatahub.component.modbus_tcp.RangeParserUtil;
import com.labdatahub.component.fins_tcp.FinsPointWriter;
import com.labdatahub.component.protocol.EncodeMessage;
import com.labdatahub.component.protocol.ProtocolManager;
import com.labdatahub.component.s7_tcp.S7PointWriter;

/**
 * 反射调协议库 encode 的封装
 *
 * <p>与老的指令下发链路（{@code DeviceDownUtils#modbusTcpDown}）走的是同一套反射机制
 * （{@code ProtocolManager.ENCODE_METHOD} / {@code CLASS_INSTANCE}，key 都是 protocolId），
 * 区别只在传参：<b>新链路只读 otherConfig</b>。
 *
 * <table>
 *   <tr><th>参数</th><th>传入值</th><th>说明</th></tr>
 *   <tr><td>functionCode</td><td>{@code null}</td><td>旧链路是 "fun1" 这类业务串，与写值无关</td></tr>
 *   <tr><td>deviceSn</td><td>实际 deviceSn</td><td>原样透传，便于协议层日志定位</td></tr>
 *   <tr><td>properties</td><td>{@code Collections.emptyMap()}</td><td><b>不传 null</b>，避免协议实现直接对 Map 操作时 NPE</td></tr>
 *   <tr><td>params</td><td>{@code null}</td><td>旧链路的指令参数，新链路没有</td></tr>
 *   <tr><td>customConfig</td><td>{@code null}</td><td>同上</td></tr>
 *   <tr><td>otherConfig</td><td>见 {@link OpenApiEncodeKeys}</td><td>唯一有效载荷</td></tr>
 * </table>
 *
 * <p>⚠️ 协议 jar 与 api 是两个 classloader：返回的对象必须经 JSON 往返才能变回
 * api 侧的 {@link EncodeMessage}（直接强转是 ClassCastException）。
 */
public class ProtocolEncodeInvoker {

    private ProtocolEncodeInvoker() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 编码写入值
     *
     * @param meta  写值元数据
     * @param value 写入值，只能是 JDK 标量
     * @return 协议层产出的载荷 JSON（{@code EncodeMessage.modbusWriteJson}）。形态由协议决定：
     *         MODBUS 是 {@code RangeItem} 数组（单位 16 位字），S7 是单个对象（单位字节），
     *         见 {@link #parseWriteEcho}
     * @throws PointWriteException 编码失败（400 值非法 / 422 值与类型不兼容 / 500 协议层异常）
     */
    public static String encode(PointWriteMeta meta, Object value) {
        Map<String, Object> otherConfig = buildOtherConfig(meta, value);

        String protocolId = meta.getProtocolId();
        if (protocolId == null || protocolId.trim().isEmpty()) {
            throw new PointWriteException(500, "网络组件未注册协议，无法编码写入值", meta);
        }
        Method encodeMethod = ProtocolManager.ENCODE_METHOD.get(protocolId);
        Object instance = ProtocolManager.CLASS_INSTANCE.get(protocolId);
        if (encodeMethod == null || instance == null) {
            throw new PointWriteException(500, "协议未注册或加载失败（protocolId=" + protocolId
                    + "），请到「协议管理」页面重新上传协议包", meta);
        }

        Object result;
        try {
            result = encodeMethod.invoke(instance, null, meta.getDeviceSn(),
                    Collections.emptyMap(), null, null, otherConfig);
        } catch (InvocationTargetException e) {
            throw mapInvocationError(meta, e);
        } catch (Exception e) {
            throw new PointWriteException(500, "调用协议编码方法失败：" + e.getMessage(), meta);
        }

        if (result == null) {
            throw new PointWriteException(500, "协议层 encode 返回空结果", meta);
        }

        EncodeMessage encodeMessage;
        try {
            encodeMessage = JSONObject.parseObject(JSONObject.toJSONString(result), EncodeMessage.class);
        } catch (Exception e) {
            throw new PointWriteException(500, "协议层返回结果无法解析：" + e.getMessage(), meta);
        }
        if (encodeMessage == null) {
            throw new PointWriteException(500, "协议层返回结果无法解析为 EncodeMessage", meta);
        }
        if (encodeMessage.getIsSend() == null || !encodeMessage.getIsSend()) {
            throw new PointWriteException(500, "协议层判定该请求不需下发（isSend=false）", meta);
        }
        String payload = encodeMessage.getModbusWriteJson();
        if (payload == null || payload.trim().isEmpty()) {
            throw new PointWriteException(500, "协议层未产出写入载荷（modbusWriteJson 为空）", meta);
        }
        return payload;
    }

    /**
     * 组装 otherConfig
     *
     * <p>⚠️ 这是<b>跨 classloader 的字符串契约</b>，键名见 {@link OpenApiEncodeKeys}，
     * 与协议 jar 侧 {@code ModbusTcpDeal} 的 {@code KEY_*} 常量一一对应，两侧无编译期检查。
     * value 只能放 JDK 标量。
     */
    private static Map<String, Object> buildOtherConfig(PointWriteMeta meta, Object value) {
        if (value != null && !(value instanceof String) && !(value instanceof Number)
                && !(value instanceof Boolean)) {
            throw new PointWriteException(400, "value 只能是字符串、数字或布尔值，当前类型："
                    + value.getClass().getSimpleName(), meta);
        }
        Map<String, Object> otherConfig = new HashMap<>();
        otherConfig.put(OpenApiEncodeKeys.DATA_TYPE, meta.getDataType());
        otherConfig.put(OpenApiEncodeKeys.BYTE_ORDER, meta.getByteOrder());
        otherConfig.put(OpenApiEncodeKeys.IS_SIGNED, meta.getIsSigned());
        otherConfig.put(OpenApiEncodeKeys.SCALE, meta.getScale());
        otherConfig.put(OpenApiEncodeKeys.OFFSET, meta.getOffset());
        otherConfig.put(OpenApiEncodeKeys.VALUE, value);
        otherConfig.put(OpenApiEncodeKeys.ADDRESS, meta.getAddress());
        otherConfig.put(OpenApiEncodeKeys.COUNT, meta.getCount());
        otherConfig.put(OpenApiEncodeKeys.FUNCTION_CODE, meta.getReadFunctionCode());
        // S7 专用键。MODBUS 协议实现不读这几个键，多传无副作用；
        // 但 value 不能是 null 以外的复杂对象 —— Map 里只能放 JDK 标量（跨 classloader）。
        otherConfig.put(OpenApiEncodeKeys.DB_NUMBER, meta.getDbNumber());
        otherConfig.put(OpenApiEncodeKeys.AREA_TYPE, meta.getAreaType());
        otherConfig.put(OpenApiEncodeKeys.BLOCK_TYPE, meta.getBlockType());
        otherConfig.put(OpenApiEncodeKeys.BIT_OFFSET, meta.getBitOffset());
        otherConfig.put(OpenApiEncodeKeys.LENGTH, meta.getLength());
        // FINS 专用键。同上，其余协议不读，多传无副作用。
        otherConfig.put(OpenApiEncodeKeys.AREA_CODE, meta.getAreaCode());
        otherConfig.put(OpenApiEncodeKeys.BIT_ADDRESS, meta.getBitAddress());
        return otherConfig;
    }

    /**
     * 把反射里包着的业务异常还原出来
     *
     * <p>{@code RegisterValueUtils.encode} 的前置校验（长度超上限、值与类型不兼容、
     * 字符串超长、scale=0 等）抛的都是 {@code IllegalArgumentException}，经协议层
     * {@code ModbusTcpDeal.encode} 透出后被反射包成 {@code InvocationTargetException}。
     * 这些都是<b>调用方能改的输入问题</b>，要报 422；其余才是平台侧的 500。
     */
    private static PointWriteException mapInvocationError(PointWriteMeta meta, InvocationTargetException e) {
        Throwable cause = e.getCause() == null ? e : e.getCause();
        if (cause instanceof IllegalArgumentException) {
            return new PointWriteException(422, cause.getMessage(), meta);
        }
        return new PointWriteException(500, "协议层编码异常：" + cause.getMessage(), meta);
    }

    /**
     * 从载荷 JSON 里取出「平台实际会写到哪儿」的回显（起始地址 + 写入单元列表）
     *
     * <p>响应里的 {@code address} / {@code count} / {@code registers} 都从这里来，
     * <b>不能从「写入的结果」来</b>：503（连接不可用）时写根本没发生，
     * 但响应照样要给出这三个字段，调用方据此区分「平台把地址算错了」和「地址对但连不上」。
     *
     * <p>三种载荷形态，按「首字符 + 键名」区分（都不用靠猜）：
     * <ul>
     *   <li><b>MODBUS</b>：首字符是 {@code [}（{@code RangeItem} 数组）→ 取第一项，
     *       {@code count}=寄存器个数（16 位字）</li>
     *   <li><b>S7</b>：单个对象且带 {@code bytes} 键 → 转成 {@code RangeItem(address, bytes.size(), bytes)}，
     *       即 <b>address=起始字节、count=字节数、registers=字节值</b></li>
     *   <li><b>FINS / 三菱 MC</b>：单个对象且带 {@code words} 键 → 转成
     *       {@code RangeItem(startAddress, words.size(), words)}，
     *       即 <b>address=起始字/软元件地址、count=项数、registers=字值（位区/位设备则是 0/1 列表）</b></li>
     * </ul>
     * 两种对象型载荷靠 {@code bytes} / {@code words} 两个键互斥区分，<b>不需要改签名或加参数</b>：
     * 协议层各自产出自己的键，谁也带不出对方的键。统一成 {@code RangeItem} 返回，是因为响应结构与
     * service 的拼装逻辑完全共用，没必要为了「单位是字节还是字」再造一个平行结构 ——
     * 单位差异写在字段注释里，由调用方按 netType 读懂。MODBUS 侧用的就是
     * {@code ModbusPointWriter} 消费载荷时的同一个类（component 侧 {@code RangeParserUtil.RangeItem}），
     * 保证两条路对载荷的理解一致。
     *
     * <p>⚠️ FINS 与三菱 MC <b>共用同一个解析分支</b>（不用 netType 分派，因为这里拿不到）——
     * 两个协议层的载荷键名是刻意取一致的（{@code startAddress} + {@code count} + {@code words}），
     * 协议库侧 {@code OmronFinsTcpDeal} / {@code MitsubishiMc3eTcpDeal} 的 {@code OUT_*} 常量
     * 各有一份注释说明这层契约。谁改了三菱的键名而没同步这里，回显会静默变成 null
     * （不影响写入本身，只是响应少三个字段）。
     *
     * @param payload 协议层产出的载荷 JSON
     * @return 回显区间；解析不出来时返回 null（不影响主流程，只是响应少几个字段）
     */
    public static RangeParserUtil.RangeItem parseWriteEcho(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            return null;
        }
        String trimmed = payload.trim();
        try {
            if (!trimmed.startsWith("[")) {
                // 对象型载荷：有 words 是「起始地址 + 字/位值列表」形态（FINS 与三菱 MC 共用），
                // 有 bytes 是 S7（两者互斥）
                if (trimmed.contains("\"" + FinsPointWriter.KEY_WORDS + "\"")) {
                    return parseWordsEcho(trimmed);
                }
                return parseS7Echo(trimmed);
            }
            // 与 ModbusDataReader 消费载荷用的是同一个调用，保证两条路对载荷的理解一致
            List<RangeParserUtil.RangeItem> itemList =
                    JSONArray.parseArray(trimmed, RangeParserUtil.RangeItem.class);
            if (itemList == null || itemList.isEmpty()) {
                return null;
            }
            // 写方向要求单个连续区间（多区间在匹配阶段就被 422 拒了），取第一项即可
            return itemList.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * S7 载荷（单个对象）→ 回显区间
     *
     * <p>键名取自 {@code S7PointWriter} 的公开常量 —— 那是 api 侧解析载荷的唯一出处，
     * 在这里另抄一份字符串迟早对不上。
     */
    private static RangeParserUtil.RangeItem parseS7Echo(String payload) {
        JSONObject json = JSONObject.parseObject(payload);
        if (json == null) {
            return null;
        }
        Integer address = json.getInteger(S7PointWriter.KEY_ADDRESS);
        List<Integer> bytes = json.getList(S7PointWriter.KEY_BYTES, Integer.class);
        if (address == null || bytes == null || bytes.isEmpty()) {
            return null;
        }
        RangeParserUtil.RangeItem item = new RangeParserUtil.RangeItem(address, bytes.size());
        item.setRegisterList(bytes);
        return item;
    }

    /**
     * 「起始地址 + 字/位值列表」型载荷（FINS 与三菱 MC 共用）→ 回显区间
     *
     * <p>键名取自 {@code FinsPointWriter} 的公开常量 —— 那是 api 侧解析载荷的唯一出处，
     * 在这里另抄一份字符串迟早对不上。三菱 MC 侧（{@code MitsubishiMc3ePointWriter}）
     * 刻意沿用同一组键名，所以这里不需要为它单开一支。
     */
    private static RangeParserUtil.RangeItem parseWordsEcho(String payload) {
        JSONObject json = JSONObject.parseObject(payload);
        if (json == null) {
            return null;
        }
        Integer address = json.getInteger(FinsPointWriter.KEY_START_ADDRESS);
        List<Integer> words = json.getList(FinsPointWriter.KEY_WORDS, Integer.class);
        if (address == null || words == null || words.isEmpty()) {
            return null;
        }
        RangeParserUtil.RangeItem item = new RangeParserUtil.RangeItem(address, words.size());
        item.setRegisterList(words);
        return item;
    }
}
