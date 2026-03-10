package com.labdatahub.component.tcp;

import com.labdatahub.common.utils.StringUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 客户端连接处理器
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final String clientId;
    private final ServerHandler handler;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean isConnected = true;
    private String componentId;
    // 文本模式分隔符相关
    private String delimiter;
    private String[] delimiters;
    private boolean USE_CUSTOM_PARSER = false;
    // 二进制模式相关
    private boolean binaryMode = false;
    private byte[][] binaryDelimiters;
    private static final String DEFAULT_CHARSET = "UTF-8";

    public ClientHandler(String componentId, Socket socket, String clientId,
                         ServerHandler handler, String delimiter) {
        this.clientSocket = socket;
        this.clientId = clientId;
        this.handler = handler;
        this.componentId = componentId;
        this.delimiter = delimiter;

        parseDelimiters(delimiter);
    }

    /**
     * 解析分隔符配置，支持普通字符串和十六进制（如 5EH）
     */
    private void parseDelimiters(String delimiter) {
        if (StringUtils.isEmpty(delimiter)) {
            this.USE_CUSTOM_PARSER = false;
            return;
        }

        String[] parts = delimiter.split(",");
        List<String> strDelims = new ArrayList<>();
        List<byte[]> binDelims = new ArrayList<>();

        for (String part : parts) {
            part = part.trim();
            // 判断是否为十六进制分隔符（不区分大小写，以 H 结尾）
            if (part.toUpperCase().endsWith("H")) {
                String hex = part.substring(0, part.length() - 1);
                try {
                    byte[] bytes = hexStringToByteArray(hex);
                    binDelims.add(bytes);
                    binaryMode = true; // 只要有一个十六进制分隔符，就启用二进制模式
                } catch (IllegalArgumentException e) {
                    // 如果十六进制解析失败，则作为普通字符串处理（保留原始内容）
                    strDelims.add(part);
                }
            } else {
                // 普通字符串，转义后加入文本分隔符列表
                String unescaped = unescapeDelimiter(part);
                strDelims.add(unescaped);
            }
        }

        if (binaryMode) {
            // 二进制模式：将二进制分隔符转换为数组
            this.binaryDelimiters = binDelims.toArray(new byte[0][]);
            this.USE_CUSTOM_PARSER = true; // 仍然使用自定义解析，但走二进制分支
        } else {
            // 纯文本模式
            this.delimiters = strDelims.toArray(new String[0]);
            this.USE_CUSTOM_PARSER = true;
        }
    }

    /**
     * 将十六进制字符串转换为字节数组
     */
    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("十六进制字符串长度必须为偶数");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 转义分隔符字符串（支持 \n, \r, \t, \\, \0, \r\n）
     */
    private String unescapeDelimiter(String delimiter) {
        if (delimiter == null) {
            return "";
        }
        if (delimiter.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int length = delimiter.length();

        for (int i = 0; i < length; i++) {
            char c = delimiter.charAt(i);
            if (c == '\\' && i + 1 < length) {
                char next = delimiter.charAt(i + 1);
                switch (next) {
                    case 'n':
                        result.append('\n');
                        i++;
                        break;
                    case 'r':
                        // 检查是否是 \r\n
                        if (i + 3 < length && delimiter.charAt(i + 2) == '\\' && delimiter.charAt(i + 3) == 'n') {
                            result.append("\r\n");
                            i += 3;
                        } else {
                            result.append('\r');
                            i++;
                        }
                        break;
                    case 't':
                        result.append('\t');
                        i++;
                        break;
                    case '0':
                        result.append('\0');
                        i++;
                        break;
                    case '\\':
                        result.append('\\');
                        i++;
                        break;
                    default:
                        // 未知转义，保留原样（包括反斜杠）
                        result.append(c).append(next);
                        i++;
                        break;
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    @Override
    public void run() {
        try {
            if (binaryMode) {
                // 二进制模式：直接使用原始输入流
                InputStream rawIn = clientSocket.getInputStream();
                // 输出流仍用 PrintWriter（兼容 sendMessage 方法）
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                handler.onClientConnected(clientId);
                processWithBinaryDelimiter(rawIn);
            } else {
                // 文本模式
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                handler.onClientConnected(clientId);

                if (USE_CUSTOM_PARSER) {
                    processWithCustomDelimiter();
                } else {
                    processWithReadLine();
                }
            }
        } catch (IOException e) {
            if (isConnected) {
                System.err.println("客户端处理异常: " + clientId + ", 错误: " + e.getMessage());
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            close();
            TCPServerInstance server = TCPServerManager.getServerInstance(componentId);
            if (server != null) {
                server.removeClient(clientId);
            }
            handler.onClientDisconnected(clientId);
        }
    }

    /**
     * 使用 readLine 方式处理（默认，无自定义分隔符）
     */
    private void processWithReadLine() throws IOException, InvocationTargetException, IllegalAccessException {
        String inputLine;
        while (isConnected && (inputLine = in.readLine()) != null) {
            handler.onMessageReceived(componentId, clientId, inputLine);
        }
    }

    /**
     * 使用自定义文本分隔符解析器（字符串分隔符）
     */
    private void processWithCustomDelimiter() throws IOException, InvocationTargetException, IllegalAccessException {
        StringBuilder buffer = new StringBuilder();
        char[] charBuffer = new char[4096];

        while (isConnected) {
            int charsRead;
            try {
                charsRead = in.read(charBuffer);
                if (charsRead == -1) {
                    break;
                }

                buffer.append(charBuffer, 0, charsRead);
                processTextBuffer(buffer); // 处理缓冲区中的完整消息

            } catch (IOException e) {
                if (isConnected) {
                    throw e;
                }
                break;
            }
        }

        // 处理剩余数据
        if (buffer.length() > 0) {
            handler.onMessageReceived(componentId, clientId, buffer.toString());
        }
    }

    /**
     * 处理文本缓冲区，根据字符串分隔符分割消息
     */
    private void processTextBuffer(StringBuilder buffer)
            throws InvocationTargetException, IllegalAccessException {
        String content = buffer.toString();
        List<String> messages = new ArrayList<>();
        int lastEnd = 0;

        while (true) {
            int delimiterIndex = -1;
            String foundDelimiter = null;

            // 查找第一个出现的分隔符
            for (String delim : delimiters) {
                int index = content.indexOf(delim, lastEnd);
                if (index != -1 && (delimiterIndex == -1 || index < delimiterIndex)) {
                    delimiterIndex = index;
                    foundDelimiter = delim;
                }
            }

            if (delimiterIndex == -1) {
                break;
            }

            String message = content.substring(lastEnd, delimiterIndex);
            messages.add(message);
            lastEnd = delimiterIndex + foundDelimiter.length();
        }

        for (String message : messages) {
            if (StringUtils.isNotEmpty(message)) {
                handler.onMessageReceived(componentId, clientId, message);
            }
        }

        // 保留未处理部分
        if (lastEnd < content.length()) {
            buffer.setLength(0);
            buffer.append(content.substring(lastEnd));
        } else {
            buffer.setLength(0);
        }
    }

    /**
     * 二进制模式处理
     */
    private void processWithBinaryDelimiter(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] readBuf = new byte[4096];
        int len;
        while (isConnected) {
            try {
                len = input.read(readBuf);
                if (len == -1) break;
                buffer.write(readBuf, 0, len);
                byte[] data = buffer.toByteArray();
                int processed = processBinaryBuffer(data);
                if (processed > 0) {
                    byte[] remaining = Arrays.copyOfRange(data, processed, data.length);
                    buffer.reset();
                    buffer.write(remaining);
                }
            } catch (IOException e) {
                if (isConnected) throw e;
                break;
            }
        }
        // 剩余数据转为十六进制上报
        if (buffer.size() > 0) {
            byte[] remaining = buffer.toByteArray();
            String hexMessage = bytesToHex(remaining);
            try {
                handler.onMessageReceived(componentId, clientId, hexMessage);
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 将字节数组转换为十六进制字符串（大写，无分隔符）
     * 例如：{0x01, 0x2F} -> "012F"
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }
    /**
     * 处理二进制缓冲区，返回已处理的字节数（包括分隔符）
     */
    private int processBinaryBuffer(byte[] data) {
        int processed = 0;
        int start = 0;
        while (start < data.length) {
            int earliestPos = Integer.MAX_VALUE;
            byte[] matchedDelim = null;
            for (byte[] delim : binaryDelimiters) {
                int pos = indexOf(data, delim, start);
                if (pos != -1 && pos < earliestPos) {
                    earliestPos = pos;
                    matchedDelim = delim;
                }
            }
            if (earliestPos == Integer.MAX_VALUE) {
                break;
            }
            int msgLen = earliestPos - start;
            if (msgLen > 0) {
                byte[] msgBytes = Arrays.copyOfRange(data, start, earliestPos);
                String hexMessage = bytesToHex(msgBytes);   // 直接转十六进制
                try {
                    handler.onMessageReceived(componentId, clientId, hexMessage);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            start = earliestPos + matchedDelim.length;
            processed = start;
        }
        return processed;
    }

    /**
     * 在字节数组中查找另一个字节数组第一次出现的位置
     */
    private int indexOf(byte[] array, byte[] target, int fromIndex) {
        if (target.length == 0) {
            return fromIndex;
        }
        outer:
        for (int i = fromIndex; i <= array.length - target.length; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    /**
     * 发送消息到客户端（文本模式）
     */
    public void sendMessage(String message) {
        if (out != null && isConnected) {
            out.print(message);
            out.flush();
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        isConnected = false;
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            System.err.println("关闭客户端连接时出错: " + clientId);
        }
    }

    public String getClientId() {
        return clientId;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String[] getDelimiters() {
        return delimiters != null ? delimiters.clone() : new String[0];
    }

    public byte[][] getBinaryDelimiters() {
        return binaryDelimiters != null ? binaryDelimiters.clone() : new byte[0][];
    }
}