//由AI修改
package com.labdatahub.component.mitsubishi_tcp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 三菱 MC 协议数据读取器（MELSEC 通信协议，QnA 兼容 3E 二进制帧，TCP）
 * 只做读：字设备按字读、位设备按位读（批量读命令统一 0x0401，子命令 0x0000 字 / 0x0001 位；
 * 帧内多字节字段均为小端，已对照 pymcprotocol 与 xingshuangs iot-communication 两个真机验证实现）
 */
public class MitsubishiDataReader {

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

    // 字读/位读点数上限
    private static final int MAX_WORD_COUNT = 960;
    private static final int MAX_BIT_COUNT = 2000;

    private MitsubishiDataReader() {
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
     * 是否为字设备（按字读 0401）；否则按位读 0402
     */
    private static boolean isWordDevice(int areaCode) {
        return areaCode == DEV_D || areaCode == DEV_W || areaCode == DEV_R || areaCode == DEV_ZR || areaCode == DEV_SD;
    }

    /**
     * X/Y 输入/输出继电器地址为八进制
     */
    private static boolean isOctalDevice(int areaCode) {
        return areaCode == DEV_X || areaCode == DEV_Y;
    }

    /**
     * 读取三菱 MC 软元件数据
     * @param componentId 组件ID，用于获取连接
     * @param areaCode 软元件代码（D/W/R/ZR/SD 字设备；M/L/B/X/Y/S/SM/F 位设备）
     * @param startAddr 起始地址（X/Y 为八进制写法，内部按八进制解析）
     * @param count 读取点数（字读上限960，位读上限2000）
     * @return 字设备返回各字值；位设备返回 0/1 列表
     * @throws Exception 通信异常
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
        // X/Y 八进制地址转实际地址编号
        if (isOctalDevice(areaCode)) {
            try {
                startAddr = Integer.parseInt(Integer.toString(startAddr), 8);
            } catch (NumberFormatException e) {
                throw new Exception("X/Y 软元件地址为八进制，配置的起始地址含非法八进制数字：" + startAddr);
            }
        }
        if (startAddr < 0) {
            throw new Exception("起始地址不能为负数，当前：" + startAddr);
        }

        // 2. 取连接
        Socket socket = MitsubishiConnectionManager.connections.get(componentId);
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            throw new Exception("连接已断开，请重新连接");
        }
        if (socket.isInputShutdown() || socket.isOutputShutdown()) {
            throw new Exception("Socket通道已关闭，无法通信");
        }
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();

        // 3. 构建 MC QnA 兼容 3E 帧请求（7头 + 2长度 + 12请求数据 = 21 字节，帧内多字节字段均为小端）
        //    对照 pymcprotocol 与 xingshuangs iot-communication：命令统一 0x0401，子命令区分字/位
        byte[] request = new byte[21];
        request[0] = (byte) 0xD0; // 副头部高字节（QnA 兼容 3E 二进制帧，响应副头部同为 D0 00）
        request[1] = 0x00;        // 副头部低字节
        request[2] = 0x00;        // 网络号
        request[3] = (byte) 0xFF; // PC号（QnA）
        request[4] = (byte) 0xFF; // 请求目标模块 I/O 号 0x03FF（低字节在前）
        request[5] = 0x03;        // 请求目标模块 I/O 号（高字节）
        request[6] = 0x00;        // 请求目标模块站号
        // 请求数据长度：从监视定时器到末尾 = 2 + 10 = 12 字节（小端 0x0C 00）
        request[7] = 0x0C;
        request[8] = 0x00;
        // 监视定时器 0x0010（16 × 250ms = 4 秒，小端 10 00）
        request[9] = 0x10;
        request[10] = 0x00;
        // 命令：批量读 0x0401（字/位统一，小端 01 04）
        request[11] = 0x01;
        request[12] = 0x04;
        // 子命令：字读 0x0000 / 位读 0x0001（小端 00 00 / 01 00）
        request[13] = (byte) (wordDevice ? 0x00 : 0x01);
        request[14] = 0x00;
        // 起始地址 3 字节小端（X/Y 已按八进制解析为实际编号）
        request[15] = (byte) (startAddr & 0xFF);
        request[16] = (byte) ((startAddr >> 8) & 0xFF);
        request[17] = (byte) ((startAddr >> 16) & 0xFF);
        // 软元件代码 1 字节（D=0xA8 等）
        request[18] = (byte) areaCode;
        // 点数 2 字节小端
        request[19] = (byte) (count & 0xFF);
        request[20] = (byte) ((count >> 8) & 0xFF);

        // 4. 发送
        out.write(request);
        out.flush();
        // 等待 PLC 响应
        Thread.sleep(10);

        // 5. 读取响应头（7 字节固定头 + 2 字节长度）
        byte[] respHeader = new byte[9];
        readFully(in, respHeader);
        // 校验副头部
        if ((respHeader[0] & 0xFF) != 0xD0 || (respHeader[1] & 0xFF) != 0x00) {
            throw new Exception("无效的 MC 响应头，期望 D0 00，实际: " + String.format("%02X %02X", respHeader[0], respHeader[1]));
        }
        // 响应长度字段小端（如 0x0006 → 06 00）
        int respLen = ((respHeader[8] & 0xFF) << 8) | (respHeader[7] & 0xFF);
        if (respLen < 2) {
            throw new Exception("无效的 MC 响应长度: " + respLen);
        }

        // 6. 读取响应体（结束码 2 字节 + 数据）
        byte[] respBody = new byte[respLen];
        readFully(in, respBody);
        int endCode = ((respBody[1] & 0xFF) << 8) | (respBody[0] & 0xFF); // 结束码小端
        if (endCode != 0) {
            throw new Exception(String.format("MC读命令错误，结束码: 0x%04X", endCode));
        }

        // 7. 解析数据
        List<Integer> result = new ArrayList<>();
        if (wordDevice) {
            int expectedDataLen = 2 + count * 2;
            if (respBody.length < expectedDataLen) {
                throw new Exception("响应数据长度不足，预期: " + expectedDataLen + "，实际: " + respBody.length);
            }
            // 字数据每点 2 字节小端
            for (int i = 0; i < count; i++) {
                int offset = 2 + i * 2;
                int value = ((respBody[offset + 1] & 0xFF) << 8) | (respBody[offset] & 0xFF);
                result.add(value);
            }
        } else {
            // MC 位读响应为 2 位/字节：第 i 点 = 字节[i/2] 的 bit4（i 为偶数）或 bit0（i 为奇数）
            int byteCount = (count + 1) / 2; // ceil(count/2)
            int expectedDataLen = 2 + byteCount;
            if (respBody.length < expectedDataLen) {
                throw new Exception("响应数据长度不足，预期: " + expectedDataLen + "，实际: " + respBody.length);
            }
            for (int i = 0; i < count; i++) {
                int bitShift = (i % 2 == 0) ? 4 : 0;
                result.add(((respBody[2 + i / 2] >> bitShift) & 0x01));
            }
        }
        return result;
    }
}
