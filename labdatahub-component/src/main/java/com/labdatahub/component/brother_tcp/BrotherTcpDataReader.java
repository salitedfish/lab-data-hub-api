//由AI修改
package com.labdatahub.component.brother_tcp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据读取器，负责从 Brother NC（兄弟数控）设备读取数据区
 * Brother NC 协议：TCP 10000 只读，发送 LOD 命令加载整块数据区，
 * 响应为 % 包裹的 ASCII 帧，按 \r\n 分行、每行 , 拆分（参考开源 BrotherAdapter 的 Request.cs/Program.cs）
 */
public class BrotherTcpDataReader {
    // 数据区名左对齐补到8位的宽度
    private static final int AREA_WIDTH = 8;
    // 命令左对齐补到7位的宽度（不含前缀C，命令整体占8位：C+7）
    private static final int COMMAND_WIDTH = 7;
    // 最大响应大小保护（1MB），防止异常报文撑爆内存
    private static final int MAX_RESPONSE_SIZE = 1024 * 1024;
    // 总读取响应上限（10秒），防止机器无响应时死等
    private static final long RESPONSE_TOTAL_TIMEOUT_MS = 10000L;

    private BrotherTcpDataReader() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 读取整个数据区
     * @param componentId 组件ID，用于获取连接
     * @param dataArea 数据区名（如 PDSP/ALARM/PRD3）
     * @return 按 \r\n 分行、按 , 拆分的行数组列表；rows.get(0) 为 % 帧头，rows.get(rowNumber) 为第 rowNumber 行
     * @throws Exception 通信异常
     */
    public static List<String[]> readDataArea(String componentId, String dataArea) throws Exception {
        Socket socket = BrotherTcpConnectionManager.connections.get(componentId);
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            throw new Exception("连接已断开，请重新连接");
        }
        if (socket.isInputShutdown() || socket.isOutputShutdown()) {
            throw new Exception("Socket通道已关闭，无法通信");
        }
        // 1. 构建 LOD 请求帧并发送
        String request = buildRequest(dataArea);
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        out.write(request.getBytes(StandardCharsets.US_ASCII));
        out.flush();
        // 2. 读取响应（%开头，%结尾，带超时保护）
        String response = readResponse(in);
        // 3. 按行解析
        List<String[]> rows = new ArrayList<>();
        String[] lines = response.split("\r\n", -1);
        for (String line : lines) {
            rows.add(line.split(",", -1));
        }
        return rows;
    }

    /**
     * 按点位地址（行号.字段序号）从整块数据中取出字段原始值
     * @param rows 整块数据行列表
     * @param rowNumber 行号（1起，对应数据区点表的行顺序，如 PDSP 的 P01=4）
     * @param fieldIndex 字段序号（1起，第1个字段=响应行 Symbol 后的第一个值）
     * @return 字段原始字符串；行或字段越界返回null
     */
    public static String extractValue(List<String[]> rows, Integer rowNumber, Integer fieldIndex) {
        if (rows == null || rowNumber == null || fieldIndex == null) {
            return null;
        }
        if (rowNumber < 0 || rowNumber >= rows.size()) {
            return null;
        }
        String[] tokens = rows.get(rowNumber);
        if (tokens == null || fieldIndex < 0 || fieldIndex >= tokens.length) {
            return null;
        }
        return tokens[fieldIndex].trim();
    }

    /**
     * 构建 Brother NC LOD 请求帧（照开源 BrotherAdapter 的 Request.cs）
     * 帧格式：%C<命令左对齐7位><数据区名左对齐8位>  \r\n<校验和2位>%\r\n
     * 校验和 = 中间段全部字符 ASCII 值求和 % 16，十进制两位
     */
    public static String buildRequest(String dataArea) {
        String command = "C" + rightPad("LOD", COMMAND_WIDTH) + rightPad(dataArea, AREA_WIDTH) + "  \r\n";
        int checksum = 0;
        for (int i = 0; i < command.length(); i++) {
            checksum += command.charAt(i);
        }
        checksum = checksum % 16;
        return "%" + command + "\r\n" + String.format("%02d", checksum) + "%\r\n";
    }

    /**
     * 字符串右补空格到指定宽度（不足补空格，超过截断）
     */
    private static String rightPad(String str, int width) {
        if (str == null) {
            str = "";
        }
        if (str.length() >= width) {
            return str.substring(0, width);
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < width) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * 读取响应直到以 % 开头、以 % 结尾（兼容结尾带 \r\n 的情况），超时保护
     */
    private static String readResponse(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[2048];
        long deadline = System.currentTimeMillis() + RESPONSE_TOTAL_TIMEOUT_MS;
        while (true) {
            int n;
            try {
                n = in.read(buf);
            } catch (SocketTimeoutException e) {
                // 读超时：若已积累到完整响应（末尾是%），按完整响应处理；否则抛异常
                if (isResponseComplete(sb)) {
                    break;
                }
                throw new Exception("读取Brother响应超时：" + e.getMessage());
            }
            if (n == -1) {
                throw new Exception("连接已关闭，读取Brother响应失败");
            }
            sb.append(new String(buf, 0, n, StandardCharsets.US_ASCII));
            if (sb.length() > MAX_RESPONSE_SIZE) {
                throw new Exception("Brother响应超过大小上限(" + MAX_RESPONSE_SIZE + "字节)");
            }
            if (isResponseComplete(sb)) {
                break;
            }
            if (System.currentTimeMillis() > deadline) {
                throw new Exception("读取Brother响应超时（累计超过" + RESPONSE_TOTAL_TIMEOUT_MS / 1000 + "秒）");
            }
        }
        return sb.toString();
    }

    /**
     * 判断积累的响应是否完整：以 % 开头且去掉末尾 \r\n 后以 % 结尾
     */
    private static boolean isResponseComplete(StringBuilder sb) {
        if (sb.length() == 0 || sb.charAt(0) != '%') {
            return false;
        }
        int end = sb.length();
        while (end > 0 && (sb.charAt(end - 1) == '\r' || sb.charAt(end - 1) == '\n')) {
            end--;
        }
        return end > 0 && sb.charAt(end - 1) == '%';
    }
}
