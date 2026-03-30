package com.labdatahub.component.omron_fins_tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * FINS TCP客户端
 * 负责与Omron PLC的底层通信，构建并解析FINS帧
 */
public class OmronFinsClient {
    private final OmronFinsTcpConfig config;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private int nextSid = 0; // 简单的发送序号（0~255循环）

    private static final int FINS_TCP_HEADER_LEN = 8; // FINS TCP帧头固定8字节
    private static final int FINS_FRAME_HEADER_LEN = 10; // FINS帧头（命令码等）固定10字节

    public OmronFinsClient(OmronFinsTcpConfig config) {
        this.config = config;
    }

    /**
     * 建立TCP连接
     */
    public boolean connect() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(config.getIpAddr(), config.getPort()), config.getTimeout());
            socket.setSoTimeout(config.getTimeout());
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否已连接
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * 读取指定内存区域的数据（字为单位）
     * @param areaCode 内存区域代码（如0x82代表D区）
     * @param startAddr 起始地址（字节地址，需转换为字地址？实际FINS中地址是字地址，但传入时需明确）
     * @param count 读取的字数
     * @return 读取到的字数组，失败返回null
     */
    public int[] readWords(int areaCode, int startAddr, int count) {
        if (!isConnected()) {
            return null;
        }

        // 构建FINS命令帧（读内存区域）
        // 命令码：0101（读取内存区域）
        byte[] command = new byte[FINS_FRAME_HEADER_LEN + 4]; // 命令码+区域+地址+数量
        // 命令码
        command[0] = 0x01;
        command[1] = 0x01;
        // 内存区域代码
        command[2] = (byte) (areaCode & 0xFF);
        // 起始地址（2字节，高字节在前）
        command[3] = (byte) ((startAddr >> 8) & 0xFF);
        command[4] = (byte) (startAddr & 0xFF);
        // 读取数量（2字节）
        command[5] = (byte) ((count >> 8) & 0xFF);
        command[6] = (byte) (count & 0xFF);
        // 剩余字节填充0
        Arrays.fill(command, 7, command.length, (byte) 0);

        byte[] response = sendFinsFrame(command);
        if (response == null || response.length < 2) {
            return null;
        }

        // 检查结束码（前两个字节是结束码，00 00表示成功）
        if (response[0] != 0 || response[1] != 0) {
            System.err.printf("FINS读取失败，结束码: %02X %02X%n", response[0], response[1]);
            return null;
        }

        // 解析返回的数据（每字2字节，高位在前）
        int dataLen = response.length - 2; // 除去结束码
        if (dataLen % 2 != 0) {
            return null;
        }
        int wordCount = dataLen / 2;
        int[] result = new int[wordCount];
        for (int i = 0; i < wordCount; i++) {
            int high = response[2 + i * 2] & 0xFF;
            int low = response[2 + i * 2 + 1] & 0xFF;
            result[i] = (high << 8) | low;
        }
        return result;
    }

    /**
     * 写入多个字到指定内存区域
     * @param areaCode 内存区域代码
     * @param startAddr 起始地址（字地址）
     * @param values 要写入的字数组
     * @return 是否成功
     */
    public boolean writeWords(int areaCode, int startAddr, int[] values) {
        if (!isConnected() || values == null || values.length == 0) {
            return false;
        }

        // 构建FINS命令帧（写内存区域）
        // 命令码：0102（写入内存区域）
        byte[] command = new byte[FINS_FRAME_HEADER_LEN + 4 + values.length * 2];
        command[0] = 0x01;
        command[1] = 0x02;
        command[2] = (byte) (areaCode & 0xFF);
        command[3] = (byte) ((startAddr >> 8) & 0xFF);
        command[4] = (byte) (startAddr & 0xFF);
        command[5] = (byte) ((values.length >> 8) & 0xFF);
        command[6] = (byte) (values.length & 0xFF);
        // 填充数据
        for (int i = 0; i < values.length; i++) {
            command[7 + i * 2] = (byte) ((values[i] >> 8) & 0xFF);
            command[7 + i * 2 + 1] = (byte) (values[i] & 0xFF);
        }

        byte[] response = sendFinsFrame(command);
        if (response == null || response.length < 2) {
            return false;
        }
        // 检查结束码
        return response[0] == 0 && response[1] == 0;
    }

    /**
     * 发送FINS帧并接收响应
     * @param command 命令数据（不含FINS TCP帧头）
     * @return 响应数据（不含FINS TCP帧头），失败返回null
     */
    private byte[] sendFinsFrame(byte[] command) {
        // FINS TCP帧头格式：
        // 字节0-3: 固定46 46 49 53 (ASCII "FINS")
        // 字节4: 固定0
        // 字节5-6: 固定00 00
        // 字节7: 发送序号(SID)
        // 字节8-11: 数据长度（4字节，大端）
        // 之后是FINS命令数据

        byte[] header = new byte[12];
        header[0] = 'F';
        header[1] = 'I';
        header[2] = 'N';
        header[3] = 'S';
        header[4] = 0x00; // 固定
        header[5] = 0x00; // 固定
        header[6] = 0x00; // 固定
        header[7] = (byte) (nextSid++ & 0xFF); // SID
        int dataLen = command.length;
        header[8] = (byte) ((dataLen >> 24) & 0xFF);
        header[9] = (byte) ((dataLen >> 16) & 0xFF);
        header[10] = (byte) ((dataLen >> 8) & 0xFF);
        header[11] = (byte) (dataLen & 0xFF);

        byte[] sendData = new byte[header.length + command.length];
        System.arraycopy(header, 0, sendData, 0, header.length);
        System.arraycopy(command, 0, sendData, header.length, command.length);

        try {
            outputStream.write(sendData);
            outputStream.flush();

            // 读取响应头
            byte[] recvHeader = new byte[12];
            int read = 0;
            while (read < 12) {
                int r = inputStream.read(recvHeader, read, 12 - read);
                if (r <= 0) throw new IOException("连接中断");
                read += r;
            }

            // 检查响应头标识
            if (!(recvHeader[0] == 'F' && recvHeader[1] == 'I' && recvHeader[2] == 'N' && recvHeader[3] == 'S')) {
                throw new IOException("无效的FINS响应头");
            }
            // 读取数据长度
            int respLen = ((recvHeader[8] & 0xFF) << 24) |
                    ((recvHeader[9] & 0xFF) << 16) |
                    ((recvHeader[10] & 0xFF) << 8) |
                    (recvHeader[11] & 0xFF);
            if (respLen <= 0) {
                return null;
            }
            byte[] respData = new byte[respLen];
            read = 0;
            while (read < respLen) {
                int r = inputStream.read(respData, read, respLen - read);
                if (r <= 0) throw new IOException("连接中断");
                read += r;
            }
            return respData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}