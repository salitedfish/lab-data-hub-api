//由AI修改
package com.labdatahub.component.fins_tcp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据读取器，负责从FINS设备读取/写入内存区数据
 *
 * <p>读与写共用 {@link #exchange} 一套收发：帧格式、地址打包、SID 校验只有一份实现，
 * 两条路不可能各自跑偏。
 */
public class FinsDataReader {

    // ===== FINS/TCP 头相关 =====
    /** FINS/TCP 命令字：FINS 帧发送 */
    private static final int TCP_COMMAND_FINS_FRAME_SEND = 0x00000002;
    /** FINS/TCP 头长度（'FINS' + 4 字节大端长度） */
    private static final int TCP_HEADER_LEN = 8;
    /** FINS/TCP 头之后的前缀长度（Command 4 + Error 4） */
    private static final int TCP_PREFIX_LEN = 8;

    // ===== FINS 帧相关 =====
    /** FINS 帧头长度（ICF/RSV/GCT/DNA/DA1/DA2/SNA/SA1/SA2/SID） */
    private static final int FINS_HEADER_LEN = 10;
    /** FINS 命令长度（MRC + SRC） */
    private static final int FINS_CMD_LEN = 2;
    /** 内存区访问命令的参数长度（区码 1 + 地址 3 + 数量 2） */
    private static final int FINS_AREA_PARAM_LEN = 6;
    /** 结束码在响应 FINS 帧里的偏移（头 10 + MRC/SRC 2） */
    private static final int END_CODE_OFFSET = FINS_HEADER_LEN + FINS_CMD_LEN;
    /** 响应数据在响应 FINS 帧里的起始偏移（结束码 2 字节之后） */
    private static final int DATA_OFFSET = END_CODE_OFFSET + 2;

    /** 内存区访问的 MRC */
    private static final byte MRC_MEMORY_AREA = 0x01;
    /** 读内存区 SRC */
    private static final byte SRC_READ = 0x01;
    /** 写内存区 SRC */
    private static final byte SRC_WRITE = 0x02;

    /** 服务ID，自增，用于区分请求；响应必须回同一个 SID，错位时能被立刻发现 */
    private static final AtomicInteger SID = new AtomicInteger(0);

    /** 单次读/写的项数上限（OMRON CS/CJ/CP 系列） */
    public static final int MAX_ITEMS = 1000;

    /**
     * 位区码：这些区码下地址第 3 字节是<b>位号</b>，每点占 1 位
     * （请求/响应数据每点 1 字节 0/1，而不是字区的每点 2 字节大端）
     */
    private static final Set<Integer> BIT_AREA_CODES = new HashSet<>(Arrays.asList(
            0x30, 0x31, 0x32, 0x33,          // CIO位 / WR位 / HR位 / AR位
            0x02, 0x18,                      // DM位 / EM当前库位
            0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27,  // EM库0-7位
            0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F   // EM库8-15位
    ));

    /** 常见结束码含义（非全集，认不出的码原样打出十六进制） */
    private static final Map<Integer, String> END_CODE_MEANING = new HashMap<>();

    static {
        END_CODE_MEANING.put(0x0000, "正常");
        END_CODE_MEANING.put(0x0101, "本地节点不在网络中");
        END_CODE_MEANING.put(0x0104, "溢出");
        END_CODE_MEANING.put(0x0105, "节点地址超出范围");
        END_CODE_MEANING.put(0x0204, "地址超出范围");
        END_CODE_MEANING.put(0x0205, "已注册");
        END_CODE_MEANING.put(0x0301, "校验和错误");
        END_CODE_MEANING.put(0x0302, "I/O设置错误");
        END_CODE_MEANING.put(0x0303, "不支持的命令");
        END_CODE_MEANING.put(0x0304, "命令太长");
        END_CODE_MEANING.put(0x0401, "命令不支持");
        END_CODE_MEANING.put(0x0402, "命令中指定的区域不存在");
        END_CODE_MEANING.put(0x1001, "命令太长");
        END_CODE_MEANING.put(0x1002, "命令中指定的区域不存在");
        END_CODE_MEANING.put(0x1003, "数据长度不一致");
        END_CODE_MEANING.put(0x1004, "访问权错误");
        END_CODE_MEANING.put(0x1101, "无法对当前模式执行");
        END_CODE_MEANING.put(0x1102, "无法对当前模式执行");
        END_CODE_MEANING.put(0x1106, "数据超出范围");
        END_CODE_MEANING.put(0x2001, "读保护");
        END_CODE_MEANING.put(0x2002, "写保护");
        END_CODE_MEANING.put(0x2005, "处于禁止编辑状态");
        END_CODE_MEANING.put(0x2101, "指定区域已满");
    }

    /**
     * 是否位区码
     *
     * <p>⚠️ 与协议库侧 {@code OmronFinsTcpDeal.isBitArea} 是同一套判据的两份实现 ——
     * 协议 jar 与 api 是两个 classloader，共享不了。改一边必须同步另一边。
     */
    public static boolean isBitArea(int areaCode) {
        return BIT_AREA_CODES.contains(areaCode);
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
     * 读取FINS内存区数据
     *
     * @param componentId 组件ID，用于获取连接和配置
     * @param areaCode 存储区代码（字区或位区，决定每点 2 字节还是 1 字节）
     * @param startAddr 起始字地址（0-65535）
     * @param bitAddr 位号（仅位区用，0-15；字区传 null）
     * @param count 读取数量（字区=字个数，位区=位个数）
     * @return 读取到的整数列表（字区每个 0-65535，位区每个 0/1）
     * @throws Exception 通信异常
     */
    public static List<Integer> readMemoryArea(String componentId, int areaCode, int startAddr,
                                               Integer bitAddr, int count) throws Exception {
        byte[] respFinsFrame = exchange(componentId, MRC_MEMORY_AREA, SRC_READ, areaCode,
                startAddr, bitAddr, count, null);

        int endCode = ((respFinsFrame[END_CODE_OFFSET] & 0xFF) << 8) | (respFinsFrame[END_CODE_OFFSET + 1] & 0xFF);
        if (endCode != 0) {
            throw new FinsResponseException(String.format("FINS读命令错误，结束码: 0x%04X（%s）",
                    endCode, endCodeMeaning(endCode)));
        }

        // 位区每点 1 字节（0/1），字区每点 2 字节大端 —— 长度校验必须跟着区码走，
        // 用固定 count*2 去校验位区响应会必然失败
        boolean bitArea = isBitArea(areaCode);
        int unit = bitArea ? 1 : 2;
        int expectedDataLen = DATA_OFFSET + count * unit;
        if (respFinsFrame.length < expectedDataLen) {
            throw new FinsResponseException("响应FINS帧数据部分长度不足，预期: " + expectedDataLen
                    + "，实际: " + respFinsFrame.length);
        }

        List<Integer> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int offset = DATA_OFFSET + i * unit;
            if (bitArea) {
                result.add(respFinsFrame[offset] & 0x01);
            } else {
                result.add(((respFinsFrame[offset] & 0xFF) << 8) | (respFinsFrame[offset + 1] & 0xFF));
            }
        }
        return result;
    }

    /**
     * 写入内存区
     *
     * <p>结束码非 0 时抛 {@link FinsResponseException}：设备<b>明确拒绝且本次没有写入</b>，
     * 与「读超时/断流（结果不确定）」是两回事，调用方要能分辨。
     *
     * @param componentId 组件ID
     * @param areaCode 存储区代码（字区或位区）
     * @param startAddr 起始字地址（0-65535）
     * @param bitAddr 位号（仅位区用，0-15；字区传 null）
     * @param values 要写入的值（字区每个 0-65535，位区每个 0/1）
     * @throws FinsResponseException 设备拒绝写入（结束码非 0，本次未写入）
     * @throws Exception 传输层异常（结果不确定）
     */
    public static void writeMemoryArea(String componentId, int areaCode, int startAddr,
                                       Integer bitAddr, List<Integer> values) throws Exception {
        if (values == null || values.isEmpty() || values.size() > MAX_ITEMS) {
            throw new FinsResponseException("写入项数需在 1-" + MAX_ITEMS + " 之间，实际："
                    + (values == null ? "null" : String.valueOf(values.size())));
        }
        byte[] respFinsFrame = exchange(componentId, MRC_MEMORY_AREA, SRC_WRITE, areaCode,
                startAddr, bitAddr, values.size(), values);

        int endCode = ((respFinsFrame[END_CODE_OFFSET] & 0xFF) << 8) | (respFinsFrame[END_CODE_OFFSET + 1] & 0xFF);
        if (endCode != 0) {
            throw new FinsResponseException(String.format("设备拒绝写入，FINS结束码 0x%04X（%s），本次未写入",
                    endCode, endCodeMeaning(endCode)));
        }
    }

    /**
     * 发一条 FINS 内存区访问命令并取回响应 FINS 帧（已剥掉 FINS/TCP 的 Command+Error 前缀）
     *
     * <p><b>调用方必须持有该组件的锁</b>（见 {@code FinsConnectionManager#getLock}）：
     * 本方法在同一 socket 上「发一帧收一帧」，与轮询读或重连交错就是帧错位。
     *
     * @param values 写命令的数据（读命令传 null）
     * @return 响应 FINS 帧，长度 ≥ 14（头 2 + 命令 2 + 结束码 2）
     */
    private static byte[] exchange(String componentId, byte mrc, byte src, int areaCode,
                                   int startAddr, Integer bitAddr, int count, List<Integer> values) throws Exception {
        Socket socket = FinsConnectionManager.getConnection(componentId);
        FinsTcpConfig config = FinsConnectionManager.getConfig(componentId);
        if (config == null) {
            throw new Exception("无可用连接：该设备没有可用的连接配置（网络组件未开启，或开启时连接失败）");
        }
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            throw new Exception("连接已断开，请重新连接");
        }
        if (socket.isInputShutdown() || socket.isOutputShutdown()) {
            throw new Exception("Socket通道已关闭，无法通信");
        }
        if (startAddr < 0 || startAddr > 0xFFFF) {
            throw new IllegalArgumentException("起始字地址 " + startAddr + " 超出 FINS 的 0-65535 范围");
        }
        int bit = bitAddr == null ? 0 : bitAddr;
        if (bit < 0 || bit > 15) {
            throw new IllegalArgumentException("位号 " + bit + " 超出 0-15 范围");
        }
        boolean bitArea = isBitArea(areaCode);
        int unit = bitArea ? 1 : 2;

        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();

        byte sid = (byte) (SID.getAndIncrement() & 0xFF);

        // 1. 构建FINS帧
        int dataLen = values == null ? 0 : values.size() * unit;
        byte[] finsFrame = new byte[FINS_HEADER_LEN + FINS_CMD_LEN + FINS_AREA_PARAM_LEN + dataLen];
        finsFrame[0] = (byte) 0x80; // ICF
        finsFrame[1] = (byte) 0x00; // RSV
        finsFrame[2] = (byte) 0x02; // GCT
        finsFrame[3] = (byte) 0x00; // DNA 本地网络
        finsFrame[4] = (byte) nodeAddress(config.getPlcNodeAddress(), 0); // DA1 PLC节点
        finsFrame[5] = (byte) 0x00; // DA2 CPU单元
        finsFrame[6] = (byte) 0x00; // SNA 源网络
        finsFrame[7] = (byte) nodeAddress(config.getClientNodeAddress(), 1); // SA1 客户端节点
        finsFrame[8] = (byte) 0x00; // SA2 源单元
        finsFrame[9] = sid; // SID

        finsFrame[10] = mrc; // MR 内存区访问
        finsFrame[11] = src; // SR 读/写内存区

        finsFrame[12] = (byte) areaCode; // 存储区代码
        // 地址 3 字节：前 2 字节 = 起始字地址（大端），第 3 字节 = 位号（字访问恒为 0）
        finsFrame[13] = (byte) ((startAddr >> 8) & 0xFF);
        finsFrame[14] = (byte) (startAddr & 0xFF);
        finsFrame[15] = (byte) bit;
        // 数量，2字节大端
        finsFrame[16] = (byte) ((count >> 8) & 0xFF);
        finsFrame[17] = (byte) (count & 0xFF);

        // 写入数据（位区每点 1 字节 0/1，字区每点 2 字节大端）
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                Integer v = values.get(i);
                int val = v == null ? 0 : v;
                if (bitArea) {
                    finsFrame[18 + i] = (byte) (val & 0x01);
                } else {
                    finsFrame[18 + i * 2] = (byte) ((val >> 8) & 0xFF);
                    finsFrame[19 + i * 2] = (byte) (val & 0xFF);
                }
            }
        }

        // 2. 构建FINS/TCP头
        byte[] header = new byte[TCP_HEADER_LEN];
        header[0] = 'F';
        header[1] = 'I';
        header[2] = 'N';
        header[3] = 'S';
        // 长度，4字节，大端，是command(4) + error(4) + finsFrame的长度
        int totalLen = TCP_PREFIX_LEN + finsFrame.length;
        header[4] = (byte) ((totalLen >> 24) & 0xFF);
        header[5] = (byte) ((totalLen >> 16) & 0xFF);
        header[6] = (byte) ((totalLen >> 8) & 0xFF);
        header[7] = (byte) (totalLen & 0xFF);

        // 3. 构建命令和错误码
        byte[] cmdError = new byte[TCP_PREFIX_LEN];
        // command=0x00000002，FinsFrame Send
        cmdError[0] = 0x00;
        cmdError[1] = 0x00;
        cmdError[2] = 0x00;
        cmdError[3] = (byte) TCP_COMMAND_FINS_FRAME_SEND;
        // error=0
        cmdError[4] = 0x00;
        cmdError[5] = 0x00;
        cmdError[6] = 0x00;
        cmdError[7] = 0x00;

        // 4. 发送请求并读响应
        // 不再 sleep(10)：readFully 本来就阻塞到读满，响应慢由 soTimeout 兜底，
        // 固定 sleep 只是白等 10ms
        out.write(header);
        out.write(cmdError);
        out.write(finsFrame);
        out.flush();

        // 5. 先读FINS/TCP头（8字节：FINS+Length）
        byte[] respHeader = new byte[TCP_HEADER_LEN];
        readFully(in, respHeader);

        // 验证FINS头
        if (respHeader[0] != 'F' || respHeader[1] != 'I' || respHeader[2] != 'N' || respHeader[3] != 'S') {
            throw new FinsResponseException("无效的FINS/TCP响应头");
        }

        // 解析Length字段（4字节无符号大端，用long避免溢出）
        long respLenLong = ((respHeader[4] & 0xFFL) << 24) |
                           ((respHeader[5] & 0xFFL) << 16) |
                           ((respHeader[6] & 0xFFL) << 8) |
                           (respHeader[7] & 0xFFL);

        // 长度必须能覆盖 Command+Error（8字节），否则下面 arraycopy 会算出一个负长度
        if (respLenLong < TCP_PREFIX_LEN || respLenLong > 1024 * 1024) {
            throw new FinsResponseException("无效的FINS/TCP响应长度: " + respLenLong);
        }

        int respLen = (int) respLenLong;
        byte[] respBody = new byte[respLen];
        readFully(in, respBody);

        // 6. 剥掉 Command(4) + Error(4)，只留 FINS 帧
        byte[] respFinsFrame = new byte[respLen - TCP_PREFIX_LEN];
        System.arraycopy(respBody, TCP_PREFIX_LEN, respFinsFrame, 0, respFinsFrame.length);

        // 检查FINS帧长度是否足够（头10 + 命令2 + 结束码2 = 14字节）
        if (respFinsFrame.length < DATA_OFFSET) {
            throw new FinsResponseException("响应FINS帧长度不足，预期至少" + DATA_OFFSET
                    + "字节，实际: " + respFinsFrame.length);
        }
        // SID 必须回同一个：正常情况下串行访问不会错位，错位说明有并发没串住或上一帧残留，
        // 此时这条 socket 上的响应已不可信，宁可报错也不要拿错帧当数据
        if (respFinsFrame[9] != sid) {
            throw new FinsResponseException(String.format("响应SID不匹配：请求 0x%02X，响应 0x%02X（帧错位）",
                    sid & 0xFF, respFinsFrame[9] & 0xFF));
        }
        return respFinsFrame;
    }

    /** 节点地址兜底：配置里显式为 null 时给默认值，避免拆箱 NPE */
    private static int nodeAddress(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    /** 结束码含义（认不出的码返回「未知结束码」） */
    private static String endCodeMeaning(int endCode) {
        String meaning = END_CODE_MEANING.get(endCode);
        return meaning == null ? "未知结束码" : meaning;
    }
}
