//由AI修改
package com.labdatahub.component.mitsubishi_cnc_tcp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 三菱 CNC TCP（MELDAS MOCHA）协议数据读取器，只做读
 * 帧结构取自树根 MitsubishiNcTcpPacket.dll（未混淆，IL 逐条提取，见 cnc_tcp_protocol.md）：
 *   请求 = [GIOPHead 12B][MessageHead 24B][操作名区块(含NUL,补零到4B)][GetDataBody 7×uint32=28B]，全小端
 *   响应 = [GIOPHead 12B][ResponseHead 16B][DataResponseHead 8B][数据]
 * 一次请求读一个点位（Section/SubSection/SystemNo/AxisFlag/DataType 寻址）。
 * 点位寻址表 = 树根点位表 17 个键里的 16 个（**缺 `cur`** —— 树根的程序内容是走单独操作
 * `mochaGetCurrentPrgBlockFirst` 读的，不是 GetDataBody）+ 轴循环（mechpos/currpos/remapos/cu/sp）× 轴号 1-6，
 * 另有 `axc`/`al`/`ct` 三个树根没有、语义未证实的键（见 ADDRESS_MAP 注释）；
 * 各键的**语义**按树根读点方法的错误串校准，不按名字猜。
 *
 * ⚠️ GIOPHead 的布局以树根 IL 为准，**不是标准 CORBA GIOP**（2026-10-15 用真机 192.168.151.103:683 实测校准）：
 *   Magic 4B ASCII("GIOP") + Version **int16** + LittenEnd u8(固定1) + ReplyType u8(请求0/应答1) + MessageSize u32。
 *   Version 是 **2 字节** 这点最容易写错 —— 少写 1 字节会让整帧左移，真机直接 RST（连接被拒、设备恒离线）。
 *   操作名区块 = 操作名 ASCII + 结尾 NUL，再补零到 4 字节边界（树根 CalcLen）；OperationLength = 字符数 + 1。
 * 数据区布局同样是实测结论，见 parseData 各分支注释。
 */
public class MitsubishiCncDataReader {

    // 请求序号（跨连接复用，requestId 取低 16 位，与树根 requestid = Random & 0xFFFF 对应）
    private static final AtomicInteger REQUEST_SEQ = new AtomicInteger(1);

    // 握手读取超时（毫秒），只作用于 mochaCancelModal2 那一次往返，用完即恢复原值
    private static final int HANDSHAKE_TIMEOUT_MS = 1000;

    // 响应体长度上限（字节），防应答头被写坏时按一个巨大 length 去 allocate
    private static final int MAX_RESPONSE_SIZE = 65536;
    // mochaCancelModal2 的参数体（GetDataBody 同形状的 7 个 u32 全 0）
    // ⚠️ 不能省成 null：真机实测无参数体/1~2 个 u32 都回 code=2 MARSHAL，取消不生效（见 sendCancelModal 注释）
    private static final int[] CANCEL_MODAL_BODY = new int[7];

    // 点位寻址结构（对应树根 PacketController.CreatePacketStructure 的 PacketStructure）
    private static class PacketStructure {
        // 分区号
        int section;
        // 子分区号
        int subSection;
        // 系统号
        int systemNo;
        // 轴标志（轴类点位为轴序 1-6，非轴点位固定）
        int axisFlag;
        // 数据类型（1/3/4/5/6/16）
        int dataType;
    }

    // 非轴点位寻址表（键即 readType；语义按树根读点方法的错误串 + 真机读值校准，见前端 utils/cncTcpPoints.js 文件头）
    //
    // ⚠️ put 的第 6 个参数（dataType）**只用于组请求帧，机床并不看它** —— 真机实测（2026-10-15）回包的
    //    ResponseHead.DataType 是数据区自己的类型，与请求无关：`axc` 请求 1 回 2、`cc` 请求 4 回 3、
    //    `st` 请求 3 回 2、`fv` 请求 3 回 6。所以这些值**保持实测跑通时的原样**，别按"应该是什么类型"去改。
    //
    // ⚠️ `axc`/`al`/`ct` 三个键**树根点位表里没有**，语义未经证实 —— `axc` 与 `pst` 同址（35.10，仅 dataType
    //    不同而 dataType 被忽略），真机实测两者回包一模一样，是 `pst` 的重复项；保留只为让老配置仍读得到。
    private static final Map<String, PacketStructure> ADDRESS_MAP = new HashMap<>();

    static {
        put("axc", 35, 10, 1, 0, 1);     // 与 pst 同址的重复项（语义未证实，勿用于新配置）
        put("pst", 35, 10, 1, 0, 2);     // CNC 状态（回 u16）
        put("opm", 35, 11, 1, 0, 2);     // 运行模式（回 u16）
        put("al", 35, 203, 1, 0, 2);     // 报警（回 u8，本机恒 0；语义未证实）
        put("fre", 33, 1, 1, 0, 6);      // 进给速度（回 double）
        put("pn", 45, 101, 1, 0, 16);    // 主程序名（回字符串 GBK）
        put("spn", 45, 201, 1, 0, 16);   // 子程序名（回字符串 GBK；不是主轴号）
        put("cc", 126, 8002, 1, 0, 4);   // 工件总计数（回 u32）
        put("sl1", 63, 4, 0, 1, 3);      // 主轴负载（回 u32）
        put("ss1", 63, 3, 0, 1, 3);      // 主轴转速（回 u32）
        put("tn", 21, 1, 0, 0, 3);       // 刀具号（回 u32）
        put("stn", 45, 103, 1, 0, 3);    // 程序步序号（回 u32；不是主轴转速）
        put("po", 40, 1, 1, 0, 3);       // 上电时间（回 u32，毫秒）
        put("opt", 40, 2, 1, 0, 3);      // 运行时间（回 u32，毫秒）
        put("cut", 40, 3, 1, 0, 3);      // 切削时间（回 u32，毫秒）
        put("ct", 40, 8, 1, 0, 3);       // 实测每秒 +1 的计时量（语义未证实，不是计数）
        put("sv", 43, 1, 0, 0, 3);       // S 代码（回 u32）
        put("fv", 42, 1, 1, 0, 3);       // F 代码（回 double）
        put("st", 63, 221, 1, 1, 3);     // 主轴温度（回 u16）
    }

    private static void put(String key, int section, int subSection, int systemNo, int axisFlag, int dataType) {
        PacketStructure ps = new PacketStructure();
        ps.section = section;
        ps.subSection = subSection;
        ps.systemNo = systemNo;
        ps.axisFlag = axisFlag;
        ps.dataType = dataType;
        ADDRESS_MAP.put(key, ps);
    }

    private MitsubishiCncDataReader() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 从输入流中读取指定长度的数据，直到读满
     */
    private static void readFully(InputStream in, byte[] buffer) throws Exception {
        int totalRead = 0;
        int len;
        while (totalRead < buffer.length) {
            len = in.read(buffer, totalRead, buffer.length - totalRead);
            if (len == -1) {
                throw new Exception("流已结束，无法读取足够的数据，预期" + buffer.length + "字节，已读取" + totalRead + "字节");
            }
            totalRead += len;
        }
    }

    /**
     * 读取 4 字节小端 uint
     */
    private static int readUintLE(byte[] b, int offset) {
        return (b[offset] & 0xFF) | ((b[offset + 1] & 0xFF) << 8) | ((b[offset + 2] & 0xFF) << 16) | ((b[offset + 3] & 0xFF) << 24);
    }

    /**
     * 解析点位地址：非轴点直接查寻址表；轴点（mechpos/currpos/remapos/cu/sp）按轴序动态组
     * @param readType 采集项类型
     * @param axisNo 轴号（仅轴点有效）
     * @return 寻址结构；不支持的键返回 null
     */
    private static PacketStructure resolve(String readType, Integer axisNo) {
        if (readType == null) {
            return null;
        }
        String type = readType.trim().toLowerCase();
        PacketStructure ps = ADDRESS_MAP.get(type);
        if (ps != null) {
            return ps;
        }
        // 轴类点位（树根轴循环：SystemNum=轴序 1..n，AxisNo 字典 {1:X,2:Y,3:Z,4:A,5:B,6:C}）
        if (axisNo == null || axisNo < 1 || axisNo > 6) {
            return null;
        }
        ps = new PacketStructure();
        ps.systemNo = 1;
        ps.axisFlag = axisNo;
        switch (type) {
            case "mechpos": // 机械坐标
                ps.section = 37;
                ps.subSection = 1;
                ps.dataType = 5;
                break;
            case "currpos": // 当前坐标
                ps.section = 37;
                ps.subSection = 2;
                ps.dataType = 5;
                break;
            case "remapos": // 相对坐标
                ps.section = 37;
                ps.subSection = 3;
                ps.dataType = 5;
                break;
            case "cu": // 轴电流
                ps.section = 59;
                ps.subSection = 4;
                ps.dataType = 3;
                break;
            case "sp": // 轴速度
                ps.section = 59;
                ps.subSection = 3;
                ps.dataType = 3;
                break;
            default:
                return null;
        }
        return ps;
    }

    /**
     * 读取三菱 CNC 单点位数据
     * @param componentId 组件ID，用于获取连接
     * @param readType 采集项类型（树根点位键；轴点 mechpos/currpos/remapos/cu/sp）
     * @param axisNo 轴号（仅轴类点位有效）
     * @return 原始值字符串；点位不支持返回 null
     * @throws Exception 通信异常
     */
    public static String readPoint(String componentId, String readType, Integer axisNo) throws Exception {
        Socket socket = MitsubishiCncConnectionManager.connections.get(componentId);
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            throw new Exception("连接已断开，请重新连接");
        }
        if (socket.isInputShutdown() || socket.isOutputShutdown()) {
            throw new Exception("Socket通道已关闭，无法通信");
        }
        PacketStructure ps = resolve(readType, axisNo);
        if (ps == null) {
            // 不支持的采集项，跳过本次（消费端 value 为 null 时不发送）
            return null;
        }
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();

        // 组 mochaGetData 请求帧（GetDataBody 7×uint32：RequstPrincipal/Section/SubSection/SystemNo/AxisFlag/Reserved/DataType）
        int requestId = REQUEST_SEQ.incrementAndGet() & 0xFFFF;
        byte[] request = buildMochaRequest(requestId, "mochaGetData",
                new int[]{0, ps.section, ps.subSection, ps.systemNo, ps.axisFlag, 0, ps.dataType});

        out.write(request);
        out.flush();

        // 读 GIOP 响应头 12B（Magic+版本+小端+应答类型+MessageSize）
        byte[] giop = new byte[12];
        readFully(in, giop);
        if (giop[0] != 'G' || giop[1] != 'I' || giop[2] != 'O' || giop[3] != 'P') {
            throw new Exception("无效的 MOCHA 响应头，期望 GIOP，实际: " + new String(giop, 0, 4, StandardCharsets.US_ASCII));
        }
        int messageSize = readUintLE(giop, 8);
        if (messageSize < 24) {
            throw new Exception("无效的 MOCHA 响应长度: " + messageSize);
        }
        // 读响应主体（ResponseHead 16B + DataResponseHead 8B + 数据）
        byte[] body = new byte[messageSize];
        readFully(in, body);
        // ResponseHead: serviceContextList(4) requestId(4) code(4) stubData(4)
        int respCode = readUintLE(body, 8);
        if (respCode != 0) {
            // 真机区号/子区号非法时回 code=1 + CORBA 异常 ncMagicException，把描述一并带出来便于定位点位表
            throw new Exception("MOCHA 响应错误码: " + respCode + errorDetail(body));
        }
        // DataResponseHead: datatype(4) dataLength(4)
        int respDataType = readUintLE(body, 16);
        int dataLength = readUintLE(body, 20);
        if (body.length < 24 + dataLength) {
            throw new Exception("MOCHA 响应数据长度不足，预期: " + dataLength + "，实际: " + (body.length - 24));
        }
        byte[] data = new byte[dataLength];
        System.arraycopy(body, 24, data, 0, dataLength);
        return parseData(respDataType, data);
    }

    /**
     * 按 DataType 解析响应数据（树根 UnpackMessageData 对应分支）
     *
     * ⚠️ 这里分的是**响应**的 DataType，不是请求里填的 —— 真机实测：请求 DataType 基本被忽略，
     *    同一个地址请求 1/2/3/99 都返回 CNC 自己的类型标记。各分支布局均为真机实测结论：
     *      1   → 1 字节无符号（35.203 报警）
     *      2   → 2 字节小端无符号（35.10 程序状态、35.11 运行模式、63.221 温度）
     *      3   → 4 字节小端无符号（40.x 时间/计数、45.103、42.1、43.1 等）
     *      6   → 16 字节 [u32 nIntDataNos][u32 nDecDataNos][double]，double 在**末尾 8 字节**（33.1、37.x）
     *      16  → [u32 长度][内容 GBK]（45.101 程序号 "O0613"、45.201 主轴号）
     *      >255→ 数组：真机 37.1 传 axisFlag=3 时返回 260(0x104) = [u32 元素类型][u32 元素个数][元素…]
     *
     * @param dataType 响应数据类型
     * @param data 数据区字节
     * @return 值字符串
     */
    private static String parseData(int dataType, byte[] data) throws Exception {
        if (data == null || data.length == 0) {
            return null;
        }
        // 数组应答：取第一条记录递归解析（轴点多条时先只取首条，够上位机显示当前值）
        if (dataType > 0xFF) {
            if (data.length < 8) {
                return null;
            }
            int elemType = readUintLE(data, 0);
            int count = readUintLE(data, 4);
            if (count < 1 || data.length <= 8) {
                return null;
            }
            int elemLen = (data.length - 8) / count;
            if (elemLen <= 0) {
                return null;
            }
            byte[] first = new byte[elemLen];
            System.arraycopy(data, 8, first, 0, elemLen);
            return parseData(elemType, first);
        }
        switch (dataType) {
            case 1:  // 1 字节无符号
                return String.valueOf(data[0] & 0xFF);
            case 2:  // 2 字节小端无符号
                if (data.length < 2) {
                    return null;
                }
                return String.valueOf((data[0] & 0xFF) | ((data[1] & 0xFF) << 8));
            case 3:  // 4 字节小端无符号（按无符号输出，避免负值）
                if (data.length < 4) {
                    return null;
                }
                return String.valueOf(readUintLE(data, 0) & 0xFFFFFFFFL);
            case 4:  // 8 字节长整型（真机未见，保留）
                if (data.length >= 8) {
                    ByteBuffer b8 = ByteBuffer.wrap(data, 0, 8).order(ByteOrder.LITTLE_ENDIAN);
                    return String.valueOf(b8.getLong());
                }
                if (data.length >= 4) {
                    return String.valueOf(readUintLE(data, 0) & 0xFFFFFFFFL);
                }
                return null;
            case 5:  // 浮点（真机未见独立应答，按 6 处理）
            case 6:  // DoubleData { nIntDataNos, nDecDataNos, dData } → dData 在末尾 8 字节
                if (data.length < 8) {
                    return null;
                }
                return formatDouble(readTrailingDouble(data));
            case 16: // 字符串 { 长度 u32, 内容 GBK }
                int strLen = readUintLE(data, 0);
                int start = 4;
                int avail = data.length - start;
                // 长度不可信时（非标准布局）退化成整段按字符串解，保持老行为兜底
                if (strLen <= 0 || strLen > avail) {
                    return new String(data, "GBK").trim();
                }
                return new String(data, start, strLen, "GBK").trim();
            default:
                // 未知类型：无法解析，跳过本次（消费端 value 为 null 时不发送）
                return null;
        }
    }

    /**
     * 读取数据区末尾 8 字节为小端 double（dData 在结构末尾，兼容 16/20/24 字节对齐）
     */
    private static double readTrailingDouble(byte[] data) {
        ByteBuffer b = ByteBuffer.wrap(data, data.length - 8, 8).order(ByteOrder.LITTLE_ENDIAN);
        return b.getDouble();
    }

    /**
     * double 值格式化：整数值去掉小数尾（如 1500.0 → 1500），避免物模型 int 转换失败
     */
    private static String formatDouble(double v) {
        if (v == Math.rint(v) && !Double.isInfinite(v) && Math.abs(v) < 1e15) {
            return String.valueOf((long) v);
        }
        return String.valueOf(v);
    }

    /**
     * 操作名区块长度（对应树根 CalcLen）：字符数 +1（结尾 NUL）后向上取整到 4 的倍数。
     * 真机实测：mochaGetData(12) → 16；mochaCancelModal2(16) → 20。算错会让 MessageSize 与实发字节数不符。
     * @param nameLength 操作名字符数
     * @return 区块字节数
     */
    private static int calcLen(int nameLength) {
        int n = (nameLength + 1) / 4;
        if ((nameLength + 1) % 4 != 0) {
            n++;
        }
        return n * 4;
    }

    /**
     * 组 MOCHA 请求帧（GIOPHead + MessageHead + 操作名区块 + GetDataBody），全小端
     *
     * ⚠️ 帧头布局是**真机实测校准**的结果（2026-10-15，见类注释）：
     *   Magic 4B + Version **int16** + LittenEnd u8 + ReplyType u8 + MessageSize u32 = 12 字节。
     *   Version 写成单字节会让整帧左移、真机直接 RST —— 这是设备恒离线的根因，改动前先看类注释。
     * 操作名区块 = 操作名 ASCII + NUL + 补零到 4 字节边界；OperationLength = 字符数 + 1（不是 -1）。
     *
     * @param requestId 请求号
     * @param operationName 操作名（如 mochaGetData / mochaCancelModal2）
     * @param body 7×uint32 的 GetDataBody；null 表示无 body（握手等无参操作）
     * @return 完整请求帧字节
     */
    private static byte[] buildMochaRequest(int requestId, String operationName, int[] body) {
        byte[] opBytes = operationName.getBytes(StandardCharsets.US_ASCII);
        int opBlockLen = calcLen(operationName.length());
        int bodyLen = body == null ? 0 : body.length * 4;
        int messageSize = 24 + opBlockLen + bodyLen;
        ByteBuffer buf = ByteBuffer.allocate(12 + messageSize).order(ByteOrder.LITTLE_ENDIAN);
        // GIOPHead 12B
        buf.put("GIOP".getBytes(StandardCharsets.US_ASCII));
        buf.putShort((short) 1);      // 版本（int16，真机实测 01 00）
        buf.put((byte) 1);            // 小端标志（固定 1）
        buf.put((byte) 0);            // 应答类型（请求=0，应答=1）
        buf.putInt(messageSize);      // 后续全部长度
        // MessageHead 24B
        buf.putInt(0);                // serviceContextList
        buf.putInt(requestId);        // requestId
        buf.put((byte) 1);            // isExpectResponse
        buf.put(new byte[3]);         // reserved
        buf.putInt(4);                // length（固定 4）
        buf.putInt(1);                // key（固定 1）
        buf.putInt(operationName.length() + 1); // operationLength（含结尾 NUL）
        // 操作名区块：ASCII + NUL，其余补零（allocate 已清零，跳过即可）
        buf.put(opBytes);
        buf.position(buf.position() + (opBlockLen - opBytes.length));
        // GetDataBody
        if (body != null) {
            for (int v : body) {
                buf.putInt(v);
            }
        }
        return buf.array();
    }

    /**
     * 从错误应答体里抽出可读的异常描述（真机错误包是 CORBA 异常：长度 + "IDL:ncMagicException:1.0"）
     * 用于把「响应错误码: 1」变成能直接看出是「区号/子区号不合法」的提示。
     * @param body 响应体
     * @return 括号包裹的描述；抽不出来时返回空串
     */
    private static String errorDetail(byte[] body) {
        StringBuilder sb = new StringBuilder();
        for (int i = 12; i < body.length && sb.length() < 80; i++) {
            int c = body[i] & 0xFF;
            if (c >= 0x20 && c < 0x7F) {
                sb.append((char) c);
            } else if (sb.length() > 0) {
                break;
            }
        }
        return sb.length() == 0 ? "" : "（" + sb + "）";
    }

    /**
     * MOCHA 握手：发送 mochaCancelModal2 取消模态
     * 树根连接成功后调用，取消系统弹窗避免干扰点位读取。
     *
     * ⚠️ soTimeout 只在这一段临时收紧，**必须在 finally 恢复**：原实现改成 1000ms 后就不管了，
     *    之后所有点位读取都跟着用 1000ms（比组件配置的读取超时短），真机上容易误判读超时。
     * ⚠️ 握手失败**不阻断建连**（与树根一致：树根的 Connect 也只把异常写进 out 参数、不据此判失败），
     *    由调用方记日志；但读不到响应会在这里抛出，别再静默吞掉 —— 那是「连上了却读不到数据」最难查的一环。
     * ⚠️ **参数体不能省**（2026-10-15 真机 192.168.151.103:683 实测）：不带参数体、或只带 1~2 个 u32 时，
     *    机床回 `code=2` + `IDL:omg.org/CORBA/MARSHAL:1.0`（编组失败，等于这次取消没生效）；
     *    带 **≥5 个 u32**（这里用 GetDataBody 同形状的 7 个 0）才回 `code=0`(ms=20)，模态才真被取消。
     *
     * @param socket 已建立的连接
     * @throws Exception 通信异常
     */
    public static void sendCancelModal(Socket socket) throws Exception {
        if (socket == null || !socket.isConnected() || socket.isClosed()) {
            return;
        }
        int requestId = REQUEST_SEQ.incrementAndGet() & 0xFFFF;
        byte[] request = buildMochaRequest(requestId, "mochaCancelModal2", CANCEL_MODAL_BODY);
        OutputStream out = socket.getOutputStream();
        out.write(request);
        out.flush();
        int oldSoTimeout = socket.getSoTimeout();
        socket.setSoTimeout(HANDSHAKE_TIMEOUT_MS);
        try {
            // 读取并丢弃响应，避免污染后续点位读取流
            byte[] giop = new byte[12];
            readFully(socket.getInputStream(), giop);
            int messageSize = readUintLE(giop, 8);
            if (messageSize <= 0 || messageSize > MAX_RESPONSE_SIZE) {
                throw new Exception("mochaCancelModal2 应答长度异常: " + messageSize);
            }
            byte[] body = new byte[messageSize];
            readFully(socket.getInputStream(), body);
            if (body.length >= 12) {
                int code = readUintLE(body, 8);
                if (code != 0) {
                    throw new Exception("mochaCancelModal2 应答错误码: " + code + errorDetail(body));
                }
            }
        } finally {
            socket.setSoTimeout(oldSoTimeout);
        }
    }
}
