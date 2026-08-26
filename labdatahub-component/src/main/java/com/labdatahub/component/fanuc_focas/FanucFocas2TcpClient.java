//由AI修改
package com.labdatahub.component.fanuc_focas;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自研 FOCAS2 报文客户端（FOCAS2 二进制 RPC over TCP，端口 8193）
 * 直接构造 FOCAS2 报文读系统参数（read-param 功能码 0x008D），绕过 fwlib 的 cnc_rdparam/cnc_rdcount。
 * 背景：台丽 0i-MF Plus 上 fwlib 的 cnc_rdcount 返回错误码3、cnc_rdparam 返回错误码4，读不到产量；
 * 树根平台用自研 FOCAS2 报文客户端能读到（读参数 6712/6711/6713），本类复现其报文协议（抓包确认）。
 *
 * 报文格式（focas_proxy 抓包逆向确认）：
 *   统一帧    = 魔数 a0a0a0a0 + 类型(0001请求/0005响应) + 处理码(2101请求/2102响应) + 数据长度(2字节) + 数据
 *   握手      = a0a0a0a0 0001 0101 0002 000N（会话号从 0001 起，需双连接：第1个建会话、第2个传数据）
 *   read-param 请求数据 = 0001 001c 0001 0001 008D + 负载(20字节)
 *   read-param 负载    = <参数号4> <参数号4> 0000 0000 0000 0000（20字节：双参数号 + 长度/计数字段 + 8字节0）
 *     注意：长度/计数字段必须为 0！fwlib 用 0000 0001 被机床拒绝（错误码4），改 0 即成功（错误码0）
 *     （2026-08-26 真机修正：参数号必须在负载开头连写两份，此前误写成 0000 开头被机床拒错误码4）
 *   read-param 响应数据 = 0001 <子长度2> 0001 0001 008D + 错误码(2) + 负载
 *
 * 连接说明：FOCAS2 走机床内嵌以太网，共享 CNC CPU，官方限制并发客户端≤5、轮询≤10次/秒。
 * 本类按 ip:port 缓存最近连接复用（30秒空闲重建），避免每次读都握手触发机床连接限制。
 * 纯 Java socket 实现，不受 fwlib "连接绑定创建线程" 限制，任意线程可读。
 */
public class FanucFocas2TcpClient {

    // FOCAS2 报文魔数（固定 4 字节帧头）
    private static final byte[] MAGIC = {(byte) 0xA0, (byte) 0xA0, (byte) 0xA0, (byte) 0xA0};
    // 报文类型：0001=请求
    private static final byte[] TYPE_REQ = {0x00, 0x01};
    // 报文类型：0005=响应
    private static final byte[] TYPE_RSP = {0x00, 0x05};
    // 处理码：2101=数据请求
    private static final byte[] PROC_DATA_REQ = {0x21, 0x01};
    // 处理码：2102=数据响应
    private static final byte[] PROC_DATA_RSP = {0x21, 0x02};
    // read-param（读参数）功能码 0x008D
    private static final byte[] FUNC_RD_PARAM = {0x00, (byte) 0x8D};
    // 子块头部：0001 001c 0001 0001（read-param 请求数据的前 8 字节）
    private static final byte[] SUB_HEAD = {0x00, 0x01, 0x00, 0x1C, 0x00, 0x01, 0x00, 0x01};

    // 连接缓存：key=ip:port，value=复用连接（串行访问由调用方 ReentrantLock 保证，无需内部加锁）
    private static final Map<String, CachedConnection> connCache = new ConcurrentHashMap<>();
    // 连接空闲超时（毫秒）：超过后复用前重建
    private static final long IDLE_TIMEOUT_MS = 30_000L;
    // Socket 读写超时（毫秒）
    private static final int SOCKET_TIMEOUT_MS = 5_000;

    /**
     * 读系统参数（read-param 0x008D）
     * @param ip 机床 IP
     * @param port 机床端口（默认 8193）
     * @param paramNo 参数号（如 6711/6712/6713）
     * @return 参数值；读取失败或错误码非0返回 null
     * @throws IOException 连接级异常（读失败，由调用方触发重连自愈）
     */
    public static Long readParam(String ip, Integer port, int paramNo) throws IOException {
        if (ip == null || ip.isEmpty()) {
            return null;
        }
        int portValue = port == null ? 8193 : port;
        byte[] resp = readParamRaw(ip, portValue, paramNo);
        if (resp == null) {
            return null;
        }
        return parseParamValue(resp, paramNo);
    }

    /**
     * 读系统参数并返回原始响应（供解析/调试）
     * 连接失败/超时抛 IOException（连接级，触发重连）；机床返回错误码非0 返回 null
     */
    private static byte[] readParamRaw(String ip, int port, int paramNo) throws IOException {
        CachedConnection conn = getConnection(ip, port);
        try {
            return sendAndReceive(conn, buildReadParamRequest(paramNo));
        } catch (IOException e) {
            // 连接异常：关闭缓存连接，下次重建（自愈）
            closeCache(ip, port);
            throw e;
        }
    }

    /**
     * 获取复用连接：缓存未失效则复用，否则双连接握手新建
     */
    private static CachedConnection getConnection(String ip, int port) throws IOException {
        String key = ip + ":" + port;
        CachedConnection conn = connCache.get(key);
        long now = System.currentTimeMillis();
        if (conn != null && conn.socket != null && conn.socket.isConnected() && !conn.socket.isClosed()
                && (now - conn.lastUsed) < IDLE_TIMEOUT_MS) {
            return conn;
        }
        // 缓存失效：关闭旧连接并新建
        closeCache(ip, port);
        CachedConnection fresh = connect(ip, port);
        connCache.put(key, fresh);
        return fresh;
    }

    /**
     * 双连接握手建会话（复现 fwlib 行为）：
     * 第1个连接握手(seq=1)后立即关闭（建立会话上下文），第2个连接握手(seq=2)用于后续数据传输。
     */
    private static CachedConnection connect(String ip, int port) throws IOException {
        // 第1个连接：握手 seq=1，读响应后关闭
        try (Socket c1 = new Socket()) {
            c1.connect(new InetSocketAddress(ip, port), SOCKET_TIMEOUT_MS);
            c1.setSoTimeout(SOCKET_TIMEOUT_MS);
            DataOutputStream out1 = new DataOutputStream(c1.getOutputStream());
            out1.write(buildHandshake(1));
            out1.flush();
            readFrame(new DataInputStream(c1.getInputStream()));
        }
        // 第2个连接：握手 seq=2，保持用于数据传输
        Socket c2 = new Socket();
        c2.connect(new InetSocketAddress(ip, port), SOCKET_TIMEOUT_MS);
        c2.setSoTimeout(SOCKET_TIMEOUT_MS);
        DataInputStream in = new DataInputStream(c2.getInputStream());
        DataOutputStream out = new DataOutputStream(c2.getOutputStream());
        out.write(buildHandshake(2));
        out.flush();
        readFrame(in);
        return new CachedConnection(c2, in, out, System.currentTimeMillis());
    }

    /**
     * 构造握手报文：a0a0a0a0 0001 0101 0002 000N
     */
    private static byte[] buildHandshake(int seq) {
        byte[] frame = new byte[12];
        System.arraycopy(MAGIC, 0, frame, 0, 4);
        System.arraycopy(TYPE_REQ, 0, frame, 4, 2);
        frame[6] = 0x01;
        frame[7] = 0x01;
        frame[8] = 0x00;
        frame[9] = 0x02;
        frame[10] = 0x00;
        frame[11] = (byte) seq;
        return frame;
    }

    /**
     * 构造 read-param 请求帧：
     * a0a0a0a0 0001 2101 <len> 0001 001c 0001 0001 008D + 负载(20字节)
     * 负载 = <参数号4> <参数号4> 0000 0000 + 8字节0（参数号连写两份，长度/计数字段为 0）
     */
    private static byte[] buildReadParamRequest(int paramNo) {
        // 负载：20 字节
        byte[] body = new byte[20];
        // 参数号连写两份（大端 4 字节）
        body[0] = (byte) (paramNo >> 24);
        body[1] = (byte) (paramNo >> 16);
        body[2] = (byte) (paramNo >> 8);
        body[3] = (byte) paramNo;
        body[4] = (byte) (paramNo >> 24);
        body[5] = (byte) (paramNo >> 16);
        body[6] = (byte) (paramNo >> 8);
        body[7] = (byte) paramNo;
        // 长度/计数字段保持 0（fwlib 用 0000 0001 被拒，改 0 即成功，真机验证错误码0）
        // body[8..19] 全 0
        byte[] data = new byte[8 + 2 + 20];
        System.arraycopy(SUB_HEAD, 0, data, 0, 8);
        System.arraycopy(FUNC_RD_PARAM, 0, data, 8, 2);
        System.arraycopy(body, 0, data, 10, 20);
        return buildFrame(TYPE_REQ, PROC_DATA_REQ, data);
    }

    /**
     * 组装统一帧：魔数 + 类型 + 处理码 + 数据长度 + 数据
     */
    private static byte[] buildFrame(byte[] type, byte[] proc, byte[] data) {
        byte[] frame = new byte[10 + data.length];
        System.arraycopy(MAGIC, 0, frame, 0, 4);
        System.arraycopy(type, 0, frame, 4, 2);
        System.arraycopy(proc, 0, frame, 6, 2);
        frame[8] = (byte) (data.length >> 8);
        frame[9] = (byte) data.length;
        System.arraycopy(data, 0, frame, 10, data.length);
        return frame;
    }

    /**
     * 发送请求帧并读一个响应帧（完整按帧读取，处理 TCP 粘包/拆包）
     */
    private static byte[] sendAndReceive(CachedConnection conn, byte[] request) throws IOException {
        conn.out.write(request);
        conn.out.flush();
        conn.lastUsed = System.currentTimeMillis();
        byte[] resp = readFrame(conn.in);
        if (resp == null || resp.length < 12) {
            return null;
        }
        return resp;
    }

    /**
     * 读一个完整帧：10 字节头（魔数4 + 类型2 + 处理2 + 长度2）+ 长度字节数据
     */
    private static byte[] readFrame(DataInputStream in) throws IOException {
        byte[] head = new byte[10];
        in.readFully(head);
        int len = ((head[8] & 0xFF) << 8) | (head[9] & 0xFF);
        byte[] data = new byte[len];
        in.readFully(data);
        byte[] frame = new byte[10 + len];
        System.arraycopy(head, 0, frame, 0, 10);
        System.arraycopy(data, 0, frame, 10, len);
        return frame;
    }

    /**
     * 解析 read-param 响应，提取参数值。
     * 响应数据 = 0001 <子长度2> 0001 0001 008D + 错误码(2) + 负载
     * 错误码 0 成功，非0 失败（机床拒绝）。
     * 参数值从负载中"参数号回显"后提取，具体偏移待真机校准（V2 格式实测：参数号后 0000 0003 0000 <值>）。
     * @return 参数值；错误码非0或未找到返回 null
     */
    private static Long parseParamValue(byte[] frame, int paramNo) throws IOException {
        // 帧数据区 = 帧体（10字节头之后）
        if (frame.length < 22) {
            return null;
        }
        byte[] data = new byte[frame.length - 10];
        System.arraycopy(frame, 10, data, 0, data.length);
        // 找功能码 0x008D：SUB_HEAD 前 8 字节后第 8 字节（data[8:10]）
        if (data.length < 12) {
            return null;
        }
        if ((data[8] & 0xFF) != (FUNC_RD_PARAM[0] & 0xFF) || (data[9] & 0xFF) != (FUNC_RD_PARAM[1] & 0xFF)) {
            return null;
        }
        // 错误码：功能码后 2 字节
        int err = ((data[10] & 0xFF) << 8) | (data[11] & 0xFF);
        if (err != 0) {
            return null;
        }
        // 负载：错误码之后
        byte[] load = new byte[data.length - 12];
        System.arraycopy(data, 12, load, 0, load.length);
        // 在负载中搜索参数号（4字节大端）
        byte[] pno = {(byte) (paramNo >> 24), (byte) (paramNo >> 16), (byte) (paramNo >> 8), (byte) paramNo};
        int idx = indexOf(load, pno);
        if (idx < 0 || idx + 12 > load.length) {
            return null;
        }
        // 参数号回显后：0000 0003 0000 <值4>（V2 格式，偏移待真机校准）
        long value = ((load[idx + 8] & 0xFFL) << 24) | ((load[idx + 9] & 0xFFL) << 16)
                | ((load[idx + 10] & 0xFFL) << 8) | (load[idx + 11] & 0xFFL);
        return value;
    }

    /**
     * 在 src 中查找 pattern 首次出现位置，找不到返回 -1
     */
    private static int indexOf(byte[] src, byte[] pattern) {
        if (src == null || pattern == null || src.length < pattern.length) {
            return -1;
        }
        int max = src.length - pattern.length;
        for (int i = 0; i <= max; i++) {
            boolean match = true;
            for (int j = 0; j < pattern.length; j++) {
                if (src[i + j] != pattern[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 关闭并移除指定 ip:port 的缓存连接
     */
    private static void closeCache(String ip, int port) {
        String key = ip + ":" + port;
        CachedConnection conn = connCache.remove(key);
        if (conn != null && conn.socket != null) {
            try {
                conn.socket.close();
            } catch (IOException e) {
                // 关闭失败不影响后续
            }
        }
    }

    /**
     * 复用连接：socket + 输入输出流 + 最后使用时间
     */
    private static class CachedConnection {
        // 数据连接 socket
        private final Socket socket;
        // 输入流
        private final DataInputStream in;
        // 输出流
        private final DataOutputStream out;
        // 最后使用时间（毫秒），用于空闲重建判断
        private volatile long lastUsed;

        private CachedConnection(Socket socket, DataInputStream in, DataOutputStream out, long lastUsed) {
            this.socket = socket;
            this.in = in;
            this.out = out;
            this.lastUsed = lastUsed;
        }
    }
}
