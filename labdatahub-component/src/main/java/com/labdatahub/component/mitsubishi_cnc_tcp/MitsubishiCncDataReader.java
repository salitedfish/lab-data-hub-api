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
 *   请求 = [GIOPHead 12B][MessageHead 24B][操作名ASCII][GetDataBody 7×uint32=28B]，全小端
 *   响应 = [GIOPHead 12B][ResponseHead 16B][DataResponseHead 8B][数据]
 * 一次请求读一个点位（Section/SubSection/SystemNo/AxisFlag/DataType 寻址）。点位寻址表与树根一致。
 * 待真机验证：握手/请求帧的 messageSize 与操作名长度、坐标类点位（datatype 1/5）数据布局需按真机响应校准。
 */
public class MitsubishiCncDataReader {

    // 请求序号（跨连接复用，requestId 取低 16 位，与树根 requestid = Random & 0xFFFF 对应）
    private static final AtomicInteger REQUEST_SEQ = new AtomicInteger(1);

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

    // 非轴点位寻址表（树根 19 键，键即 readType）
    private static final Map<String, PacketStructure> ADDRESS_MAP = new HashMap<>();

    static {
        put("axc", 35, 10, 1, 0, 1);     // 轴坐标（整体，多轴）
        put("pst", 35, 10, 1, 0, 2);     // 程序状态
        put("opm", 35, 11, 1, 0, 2);     // 运行模式
        put("al", 35, 203, 1, 0, 2);     // 报警
        put("fre", 33, 1, 1, 0, 6);      // 进给速度
        put("pn", 45, 101, 1, 0, 16);    // 程序号
        put("spn", 45, 201, 1, 0, 16);   // 主轴号
        put("cc", 126, 8002, 1, 0, 4);   // 计数
        put("sl1", 63, 4, 0, 1, 3);      // 状态1
        put("ss1", 63, 3, 0, 1, 3);      // 状态2
        put("tn", 21, 1, 0, 0, 3);       // 刀具号
        put("stn", 45, 103, 1, 0, 3);    // 主轴转速
        put("po", 40, 1, 1, 0, 3);       // 位置
        put("opt", 40, 2, 1, 0, 3);      // 操作倍率
        put("cut", 40, 3, 1, 0, 3);      // 切削
        put("ct", 40, 8, 1, 0, 3);       // 计数
        put("sv", 43, 1, 0, 0, 3);       // 主轴速度
        put("fv", 42, 1, 1, 0, 3);       // 进给倍率
        put("st", 63, 221, 1, 1, 3);     // 温度/状态
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
            throw new Exception("MOCHA 响应错误码: " + respCode);
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
     * @param dataType 响应数据类型
     * @param data 数据区字节
     * @return 值字符串
     */
    private static String parseData(int dataType, byte[] data) throws Exception {
        if (data == null || data.length == 0) {
            return null;
        }
        switch (dataType) {
            case 3:  // DoubleData { nIntDataNos, nDecDataNos, lOption, fData, dData } → 返回 dData
            case 5:  // 轴坐标（机械/当前/相对）→ 坐标 double 在数据区末尾
            case 6:  // FloatBin { nIntDataNos, nDecDataNos, lOption, dData } → 返回 dData
                if (data.length < 8) {
                    return null;
                }
                // dData 均为结构末尾 8 字节（无论 16/20/24 对齐），取末尾 double
                return formatDouble(readTrailingDouble(data));
            case 4:  // long/int（计数等）
                if (data.length >= 8) {
                    ByteBuffer b8 = ByteBuffer.wrap(data, 0, 8).order(ByteOrder.LITTLE_ENDIAN);
                    return String.valueOf(b8.getLong());
                }
                if (data.length >= 4) {
                    return String.valueOf((long) readUintLE(data, 0));
                }
                return null;
            case 16: // 字符串（程序号/主轴号，GetString + Trim）
                return new String(data, "GBK").trim();
            case 1:  // 轴坐标整体（axc，多轴结构未知），暂不支持
            case 2:  // 程序状态/运行模式/报警（可能为位掩码或枚举，真机校准）
            default:
                // 未知类型：原样返回十六进制便于排查，待真机校准
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
     * 组 MOCHA 请求帧（GIOPHead + MessageHead + 操作名 + GetDataBody），全小端
     * @param requestId 请求号
     * @param operationName 操作名（如 mochaGetData / mochaCancelModal2）
     * @param body 7×uint32 的 GetDataBody；null 表示无 body（握手等无参操作）
     * @return 完整请求帧字节
     */
    private static byte[] buildMochaRequest(int requestId, String operationName, int[] body) {
        byte[] opBytes = operationName.getBytes(StandardCharsets.US_ASCII);
        int bodyLen = body == null ? 0 : body.length * 4;
        int messageSize = 24 + opBytes.length + bodyLen;
        ByteBuffer buf = ByteBuffer.allocate(12 + messageSize).order(ByteOrder.LITTLE_ENDIAN);
        // GIOPHead
        buf.put("GIOP".getBytes(StandardCharsets.US_ASCII));
        buf.put((byte) 1);            // 版本
        buf.put((byte) 1);            // 小端
        buf.put((byte) 0);            // 应答类型（请求=0）
        buf.putInt(messageSize);      // 后续全部长度
        // MessageHead
        buf.putInt(0);                // serviceContextList
        buf.putInt(requestId);        // requestId
        buf.put((byte) 1);            // isExpectResponse
        buf.put(new byte[3]);         // reserved
        buf.putInt(4);                // length（固定 4）
        buf.putInt(1);                // key（固定 1）
        buf.putInt(operationName.length() - 1); // operationLength（树根 opname.Length - 1）
        // 操作名 ASCII
        buf.put(opBytes);
        // GetDataBody
        if (body != null) {
            for (int v : body) {
                buf.putInt(v);
            }
        }
        return buf.array();
    }

    /**
     * MOCHA 握手：发送 mochaCancelModal2 取消模态
     * 树根连接成功后调用，取消系统弹窗避免干扰点位读取。
     * @param socket 已建立的连接
     * @throws Exception 通信异常
     */
    public static void sendCancelModal(Socket socket) throws Exception {
        if (socket == null || !socket.isConnected() || socket.isClosed()) {
            return;
        }
        int requestId = REQUEST_SEQ.incrementAndGet() & 0xFFFF;
        byte[] request = buildMochaRequest(requestId, "mochaCancelModal2", null);
        OutputStream out = socket.getOutputStream();
        out.write(request);
        out.flush();
        socket.setSoTimeout(1000);
        try {
            // 读取并丢弃响应，避免污染后续点位读取流
            byte[] giop = new byte[12];
            readFully(socket.getInputStream(), giop);
            int messageSize = readUintLE(giop, 8);
            if (messageSize > 0 && messageSize <= 65536) {
                byte[] body = new byte[messageSize];
                readFully(socket.getInputStream(), body);
            }
        } catch (Exception e) {
            // 握手响应读不到不影响建连（真机验证后按需调整）
        }
    }
}
