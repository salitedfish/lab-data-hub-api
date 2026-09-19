//由AI修改
package com.labdatahub.business.domain;

/**
 * 写值链路 otherConfig 的键名常量（方向：api → 协议层）
 *
 * <p>⚠️ <b>这是跨 classloader 的字符串契约，两侧没有任何编译期检查。</b>
 * api 侧用这些常量拼 Map，协议 jar 侧的
 * {@code com.labdatahub.protocol.deal.ModbusTcpDeal#encode} 用自己的
 * {@code KEY_*} 常量取值——两边靠字符串相等对上。
 * <b>改这里必须同步改那边</b>（另一处契约是协议层回传的
 * {@code EncodeMessage.modbusWriteJson}，见方案 4.4.4）。
 *
 * <p>⚠️ Map 的 value <b>只能放 JDK 标量</b>（String / Number / Boolean）。
 * 协议 jar 与 api 是两个 classloader，塞进自定义对象（哪怕同名同类）在协议层
 * 取出来就是 ClassCastException。所以 {@link PointWriteMeta} 必须在这里拆成
 * 平铺的键值对，不能整个对象塞进去。
 */
public class OpenApiEncodeKeys {

    private OpenApiEncodeKeys() {
        throw new UnsupportedOperationException("该类为常量类，禁止实例化");
    }

    /** 物模型类型（dataType） */
    public static final String DATA_TYPE = "dataType";

    /** 字节序（big/little） */
    public static final String BYTE_ORDER = "byteOrder";

    /** 有无符号（1/0） */
    public static final String IS_SIGNED = "isSigned";

    /** 缩放系数 */
    public static final String SCALE = "scale";

    /** 偏移量 */
    public static final String OFFSET = "offset";

    /** 写入值，只能是 JDK 标量 */
    public static final String VALUE = "value";

    /** 起始地址：MODBUS=起始寄存器地址（由 register_range 解析）；S7=起始字节偏移（由 labdatahub_s71200_config.start_address 取） */
    public static final String ADDRESS = "address";

    /** 写入长度：MODBUS=寄存器个数（16 位字）；S7=字节数（DBX 1 / DBW 2 / DBD 4 / DBB=length） */
    public static final String COUNT = "count";

    /** 读功能码，协议层据此推写功能码（仅 MODBUS 有；S7 无功能码概念） */
    public static final String FUNCTION_CODE = "functionCode";

    // ===== 以下 5 个键只有 S7（S71200_TCP）用，MODBUS 侧不读，传了也无害 =====

    /** DB 块号（S7；M/I/Q 区不使用，但配置里仍带） */
    public static final String DB_NUMBER = "dbNumber";

    /** 区类型（S7）：DB 数据块 / M 标志位 / I 输入区 / Q 输出区 */
    public static final String AREA_TYPE = "areaType";

    /** 块类型（S7）：DBX 位 / DBW 2字节整型 / DBD 4字节整型或浮点 / DBB 西门子字符串 */
    public static final String BLOCK_TYPE = "blockType";

    /** 位偏移（S7 仅 DBX 用，0-7） */
    public static final String BIT_OFFSET = "bitOffset";

    /** 读取长度（S7 仅 DBB 用，字符串所占字节数，含 2 字节长度头） */
    public static final String LENGTH = "length";

    // ===== 以下 2 个键只有欧姆龙 FINS（OMRONFINS_TCP）用，其余协议不读，传了也无害 =====

    /** 存储区码（FINS）：字区如 DM=0x82 / CIO=0xB0，位区如 DM位=0x02 / CIO位=0x30 */
    public static final String AREA_CODE = "areaCode";

    /** 位号（FINS 仅位区用，0-15；字区为 null） */
    public static final String BIT_ADDRESS = "bitAddress";
}
