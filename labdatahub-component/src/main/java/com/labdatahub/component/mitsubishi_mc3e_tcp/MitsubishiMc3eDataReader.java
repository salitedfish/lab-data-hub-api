//由AI修改
package com.labdatahub.component.mitsubishi_mc3e_tcp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 三菱 MC 协议数据读写器（MELSEC 通信协议，QnA 兼容 3E 二进制帧，TCP）
 *
 * <p>字设备按字访问、位设备按位访问（命令 0x0401 批量读 / 0x1401 批量写，子命令区分字/位；
 * 帧内多字节字段均为小端，已对照 pymcprotocol 与 xingshuangs iot-communication 两个真机验证实现）。
 *
 * <p><b>本类只发 3E 帧</b>：协议拆分后一个 netType 一种帧格式，不再有帧模式分支
 * （原先按 {@code protocolMode} 走 1E 的那段分支已一并删除；1E 帧将作为独立协议
 * {@code MITSUBISHI_MC1E_TCP} 另行实现，两者不共用代码）。
 *
 * <p>⚠️ <b>请求副头部是 {@code 50 00}，不是 {@code D0 00}</b>（2026-09-19 修正）。
 * SLMP 规范里两者分得很清楚：<b>请求</b>副头部 {@code 50 00}、<b>响应</b>副头部 {@code D0 00}。
 * 这里此前错发了 {@code D0 00}，拟真平台按规范校验后直接回错误码 {@code 0xC059}（请求副头部异常），
 * 连第一步都过不去。响应侧的 {@code D0 00} 校验本来就是对的，未改。
 *
 * <p>⚠️ 帧数据段的字段顺序同样是规范定死的：<b>首软元件 3 字节小端 → 软元件码 1 字节 → 点数 2 字节小端</b>。
 * 拟真平台把前两个字段写反了（见根 CLAUDE.md 的 ProtoForge 段落），不是我们错。
 */
public class MitsubishiMc3eDataReader {

    // 字设备软元件代码
    private static final int DEV_D = 0xA8;  // 数据寄存器
    private static final int DEV_W = 0xB4;  // 链接寄存器
    private static final int DEV_R = 0xAF;  // 文件寄存器
    private static final int DEV_ZR = 0xB0; // 文件寄存器(ZR)
    private static final int DEV_SD = 0xA9; // 特殊寄存器
    // 位设备软元件代码
    private static final int DEV_M = 0x90;  // 内部继电器
    private static final int DEV_L = 0x92;  // 锁存继电器
    private static final int DEV_B = 0xA0;  // 链接继电器
    private static final int DEV_X = 0x9C;  // 输入继电器（八进制）
    private static final int DEV_Y = 0x9D;  // 输出继电器（八进制）
    private static final int DEV_S = 0x98;  // 步进继电器
    private static final int DEV_SM = 0x91; // 特殊继电器
    private static final int DEV_F = 0x93;  // 报警器

    /** 字读/字写单次点数上限 */
    public static final int MAX_WORD_COUNT = 960;
    /** 位读/位写单次点数上限 */
    public static final int MAX_BIT_COUNT = 2000;

    /** 3E 帧固定表头长度（副头部 2 + 网络 1 + PC 1 + I/O 2 + 站 1 + 长度 2 = 9 字节） */
    private static final int HEADER_LEN = 9;
    /** 命令字之前的数据段长度：监视定时器 2 + 命令 2 + 子命令 2 + 首软元件 3 + 软元件码 1 + 点数 2 */
    private static final int REQUEST_DATA_BASE_LEN = 12;

    private MitsubishiMc3eDataReader() {
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
     * 是否为字设备（按字访问 0x0401 / 0x1401 + 子命令 0x0000）
     */
    public static boolean isWordDevice(int areaCode) {
        return areaCode == DEV_D || areaCode == DEV_W || areaCode == DEV_R || areaCode == DEV_ZR || areaCode == DEV_SD;
    }

    /**
     * X/Y 输入/输出继电器地址为八进制
     */
    public static boolean isOctalDevice(int areaCode) {
        return areaCode == DEV_X || areaCode == DEV_Y;
    }

    /**
     * X/Y 的八进制写法转实际地址编号（其它软元件原样返回）
     *
     * <p>⚠️ 配置里存的是<b>八进制写法</b>（如 X20 存 20），报文字段要的是换算后的编号。
     * 拟真平台按<b>十六进制</b>解析同一串数字，两边算法不同 —— 联调时避开 X/Y 即可，不改代码。
     */
    public static int toRealAddress(int areaCode, int startAddr) throws Exception {
        if (!isOctalDevice(areaCode)) {
            return startAddr;
        }
        try {
            return Integer.parseInt(Integer.toString(startAddr), 8);
        } catch (NumberFormatException e) {
            throw new Exception("X/Y 软元件地址为八进制，配置的起始地址含非法八进制数字：" + startAddr);
        }
    }

    /**
     * 读取三菱 MC 软元件数据
     *
     * <p><b>调用方必须持有该组件的锁</b>（见 {@code MitsubishiMc3eConnectionManager#getLock}）：
     * MC 3E 是「发一帧收一帧」的同步问答，读、写、重连三方共用一条 socket，不串行化就会互相取错帧。
     *
     * @param componentId 组件ID，用于获取连接
     * @param areaCode 软元件代码（D/W/R/ZR/SD 字设备；M/L/B/X/Y/S/SM/F 位设备）
     * @param startAddr 起始地址（X/Y 为八进制写法，内部按八进制解析）
     * @param count 读取点数（字读上限960，位读上限2000）
     * @return 字设备返回各字值；位设备返回 0/1 列表
     * @throws MitsubishiMc3eResponseException PLC 已响应但结束码非 0（命令被拒，连接是好的）
     * @throws Exception 传输层异常（超时/断流/连接不可用）
     */
    public static List<Integer> readMemoryArea(String componentId, int areaCode, int startAddr, int count) throws Exception {
        // 1. 参数校验
        if (count <= 0) {
            throw new Exception("读取点数必须为正整数，当前：" + count);
        }
        boolean wordDevice = isWordDevice(areaCode);
        if (wordDevice && count > MAX_WORD_COUNT) {
            throw new Exception("字设备读取点数超过协议上限" + MAX_WORD_COUNT + "，当前：" + count);
        }
        if (!wordDevice && count > MAX_BIT_COUNT) {
            throw new Exception("位设备读取点数超过协议上限" + MAX_BIT_COUNT + "，当前：" + count);
        }
        int realAddr = toRealAddress(areaCode, startAddr);
        if (realAddr < 0) {
            throw new Exception("起始地址不能为负数，当前：" + startAddr);
        }

        // 2. 组帧并收发
        byte[] request = buildRequest(0x0401, wordDevice, realAddr, areaCode, count, null);
        byte[] respBody = exchange(componentId, request);

        // 3. 解析数据（响应体前 2 字节是结束码，exchange 已校验为 0）
        List<Integer> result = new ArrayList<>();
        if (wordDevice) {
            int expectedDataLen = 2 + count * 2;
            if (respBody.length < expectedDataLen) {
                throw new MitsubishiMc3eResponseException("响应数据长度不足，预期: " + expectedDataLen + "，实际: " + respBody.length);
            }
            // 字数据每点 2 字节小端
            for (int i = 0; i < count; i++) {
                int offset = 2 + i * 2;
                int value = ((respBody[offset + 1] & 0xFF) << 8) | (respBody[offset] & 0xFF);
                result.add(value);
            }
        } else {
            // 位读响应为 2 位/字节：第 i 点 = 字节[i/2] 的 bit4（i 为偶数）或 bit0（i 为奇数）
            int byteCount = (count + 1) / 2; // ceil(count/2)
            int expectedDataLen = 2 + byteCount;
            if (respBody.length < expectedDataLen) {
                throw new MitsubishiMc3eResponseException("响应数据长度不足，预期: " + expectedDataLen + "，实际: " + respBody.length);
            }
            for (int i = 0; i < count; i++) {
                int bitShift = (i % 2 == 0) ? 4 : 0;
                result.add(((respBody[2 + i / 2] >> bitShift) & 0x01));
            }
        }
        return result;
    }

    /**
     * 写入三菱 MC 软元件数据（批量写命令 0x1401）
     *
     * <p>⚠️ 位写的打包与位读对称：<b>每字节 2 点，第 1 点在 bit4、第 2 点在 bit0</b>。
     * 单点位（count=1）落成 1 字节，ON 即 {@code 0x10}、OFF 即 {@code 0x00}。
     *
     * <p><b>调用方必须持有该组件的锁</b>（见 {@code MitsubishiMc3eConnectionManager#getLock}）——
     * 写与读共用一条 socket，锁要一直持到响应读完，不能只包住 out.write。
     *
     * @param componentId 组件ID，用于获取连接
     * @param areaCode 软元件代码
     * @param startAddr 起始地址（X/Y 为八进制写法，内部按八进制解析）
     * @param count 写入点数（必须与 values 个数一致）
     * @param values 字设备为各字值（0-65535）；位设备为 0/1
     * @throws MitsubishiMc3eResponseException PLC 已响应但结束码非 0 —— <b>设备明确拒绝、本次未写入</b>
     * @throws Exception 传输层异常（超时/断流/连接不可用）—— <b>请求已发出，结果不确定</b>
     */
    public static void writeMemoryArea(String componentId, int areaCode, int startAddr, int count, List<Integer> values)
            throws Exception {
        if (values == null || values.isEmpty()) {
            throw new Exception("写入值不能为空");
        }
        if (values.size() != count) {
            throw new Exception("写入点数与值个数不一致：count=" + count + "，值个数=" + values.size());
        }
        boolean wordDevice = isWordDevice(areaCode);
        if (wordDevice && count > MAX_WORD_COUNT) {
            throw new Exception("字设备写入点数超过协议上限" + MAX_WORD_COUNT + "，当前：" + count);
        }
        if (!wordDevice && count > MAX_BIT_COUNT) {
            throw new Exception("位设备写入点数超过协议上限" + MAX_BIT_COUNT + "，当前：" + count);
        }
        int realAddr = toRealAddress(areaCode, startAddr);
        if (realAddr < 0) {
            throw new Exception("起始地址不能为负数，当前：" + startAddr);
        }

        // 数据段：字设备每点 2 字节小端；位设备每字节 2 点（第 1 点 bit4、第 2 点 bit0）
        byte[] data;
        if (wordDevice) {
            data = new byte[count * 2];
            for (int i = 0; i < count; i++) {
                int v = values.get(i) & 0xFFFF;
                data[i * 2] = (byte) (v & 0xFF);
                data[i * 2 + 1] = (byte) ((v >> 8) & 0xFF);
            }
        } else {
            data = new byte[(count + 1) / 2];
            for (int i = 0; i < count; i++) {
                if (values.get(i) != null && values.get(i) != 0) {
                    data[i / 2] |= (byte) (i % 2 == 0 ? 0x10 : 0x01);
                }
            }
        }

        byte[] request = buildRequest(0x1401, wordDevice, realAddr, areaCode, count, data);
        // 写响应体只有 2 字节结束码（无数据），读满即可
        exchange(componentId, request);
    }

    /**
     * 组 3E 二进制帧
     *
     * @param command 0x0401 批量读 / 0x1401 批量写
     * @param wordDevice true=字访问（子命令 0x0000）false=位访问（子命令 0x0001）
     * @param realAddr 已换算的起始地址（X/Y 已按八进制换算）
     * @param data 请求数据段（读为 null，写为编码后的数据）
     */
    private static byte[] buildRequest(int command, boolean wordDevice, int realAddr, int areaCode, int count, byte[] data) {
        int dataLen = data == null ? 0 : data.length;
        byte[] request = new byte[HEADER_LEN + REQUEST_DATA_BASE_LEN + dataLen];
        // 副头部：请求是 50 00（⚠️ 不是 D0 00 —— 那是响应副头部）
        request[0] = 0x50;
        request[1] = 0x00;
        request[2] = 0x00;        // 网络号
        request[3] = (byte) 0xFF; // PC号（QnA）
        request[4] = (byte) 0xFF; // 请求目标模块 I/O 号 0x03FF（低字节在前）
        request[5] = 0x03;        // 请求目标模块 I/O 号（高字节）
        request[6] = 0x00;        // 请求目标模块站号
        // 请求数据长度：从监视定时器到帧尾（小端）
        int reqDataLen = REQUEST_DATA_BASE_LEN + dataLen;
        request[7] = (byte) (reqDataLen & 0xFF);
        request[8] = (byte) ((reqDataLen >> 8) & 0xFF);
        // 监视定时器 0x0010（16 × 250ms = 4 秒，小端 10 00）
        request[9] = 0x10;
        request[10] = 0x00;
        // 命令（小端）
        request[11] = (byte) (command & 0xFF);
        request[12] = (byte) ((command >> 8) & 0xFF);
        // 子命令：字 0x0000 / 位 0x0001（小端）
        request[13] = (byte) (wordDevice ? 0x00 : 0x01);
        request[14] = 0x00;
        // 首软元件 3 字节小端
        request[15] = (byte) (realAddr & 0xFF);
        request[16] = (byte) ((realAddr >> 8) & 0xFF);
        request[17] = (byte) ((realAddr >> 16) & 0xFF);
        // 软元件代码 1 字节（D=0xA8 等）
        request[18] = (byte) areaCode;
        // 点数 2 字节小端
        request[19] = (byte) (count & 0xFF);
        request[20] = (byte) ((count >> 8) & 0xFF);
        if (dataLen > 0) {
            System.arraycopy(data, 0, request, HEADER_LEN + REQUEST_DATA_BASE_LEN, dataLen);
        }
        return request;
    }

    /**
     * 发送请求帧并读回响应体（结束码 2 字节 + 数据）
     *
     * <p>读与写共用的收发骨架：都校验响应副头部 {@code D0 00}、按长度字段读满、校验结束码为 0。
     * 写响应没有数据段，所以不按期望数据长度断言 —— 长度字段说了算。
     *
     * @return 响应体（含前 2 字节结束码）
     * @throws MitsubishiMc3eResponseException 结束码非 0（PLC 明确拒绝）
     */
    private static byte[] exchange(String componentId, byte[] request) throws Exception {
        Socket socket = MitsubishiMc3eConnectionManager.connections.get(componentId);
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            throw new Exception("连接已断开，请重新连接");
        }
        if (socket.isInputShutdown() || socket.isOutputShutdown()) {
            throw new Exception("Socket通道已关闭，无法通信");
        }
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();

        out.write(request);
        out.flush();
        // 等待 PLC 响应（保留原有节奏，避免部分机型对连续帧敏感）
        Thread.sleep(10);

        // 响应头 9 字节：副头部 2 + 网络 1 + PC 1 + I/O 2 + 站 1 + 长度(小端) 2
        byte[] respHeader = new byte[HEADER_LEN];
        readFully(in, respHeader);
        if ((respHeader[0] & 0xFF) != 0xD0 || (respHeader[1] & 0xFF) != 0x00) {
            throw new Exception("无效的 MC 响应头，期望 D0 00，实际: "
                    + String.format("%02X %02X", respHeader[0], respHeader[1]));
        }
        int respLen = ((respHeader[8] & 0xFF) << 8) | (respHeader[7] & 0xFF);
        if (respLen < 2) {
            throw new Exception("无效的 MC 响应长度: " + respLen);
        }

        byte[] respBody = new byte[respLen];
        readFully(in, respBody);
        int endCode = ((respBody[1] & 0xFF) << 8) | (respBody[0] & 0xFF); // 结束码小端
        if (endCode != 0) {
            throw new MitsubishiMc3eResponseException(String.format("MC命令错误，结束码: 0x%04X", endCode));
        }
        return respBody;
    }
}
