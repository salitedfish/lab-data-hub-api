//由AI修改
package com.labdatahub.component.s7_tcp;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.fastjson2.JSONObject;
import com.github.s7connector.api.DaveArea;
import com.github.s7connector.api.S7Connector;
import com.labdatahub.component.protocol.exception.PointConnectionException;
import com.labdatahub.component.protocol.exception.PointDeviceException;

import lombok.extern.slf4j.Slf4j;

/**
 * 点位写值器（S7-1200，ISO-TCP）
 *
 * <p>载荷由协议库 {@code S71200TcpDeal.encode} 产出，是<b>单个 JSON 对象</b>（不是 Modbus 那种区间数组）：
 * <pre>{"areaType":"DB","dbNumber":1,"address":4,"bitOffset":0,"blockType":"DBD","dataType":"float","bytes":[66,42,0,0]}</pre>
 * 拿到的 {@code bytes} 就是最终要落到 PLC 上的字节，这里<b>不再做任何值与类型的转换</b>
 * （那是协议库 encode 的职责，那边才看得到物模型的 dataType / 字节序 / 缩放）。
 *
 * <p>与 {@link com.labdatahub.component.modbus_tcp.ModbusPointWriter} 对齐的三件事：连接自愈、读写串行锁、区分
 * 「没写」和「不知道写没写」。S7 这边还有自己的第四件：
 * <ul>
 *   <li><b>位写要靠读-改-写</b>：s7connector 没有位写 API，只能读回该字节、改掉目标位、整字节写回。
 *       注意这个「读-改-写」对<b>我们自己的多线程</b>是原子的（在锁内），
 *       但对<b>PLC 里的用户程序</b>不是——两次调用之间 PLC 完全可能改掉同一个字节的别的位。
 *       这是无位写 API 的固有限制，不是这里能补的。</li>
 *   <li><b>写响应没有回显</b>：S7 的写应答里只有返回码，没有「地址 + 数量」可对账
 *       （Modbus 有）。库在返回码非 0 时抛 {@code S7Exception}，正常返回即设备已受理，
 *       所以这里只能做到「未抛异常 ≈ 写成功」，做不了 Modbus 那样的逐项校验。</li>
 *   <li><b>载荷超过 96 字节会被库拆成多条请求</b>（S7 PDU 上限，见 {@code S7BaseConnection.write}）。
 *       中途失败就是<b>部分写入</b>——DBB 字符串点位配置到接近 96 字节以上时要注意这一点。</li>
 * </ul>
 */
@Slf4j
public class S7PointWriter {

    /** 写锁等待上限（毫秒）。等不到就返回 503 让调用方重试，不把 HTTP 线程无限挂住 */
    private static final int LOCK_WAIT_MS = 5000;

    /** 位写入的块类型 */
    private static final String BLOCK_DBX = "DBX";

    // ===== 载荷键名：与协议 jar 侧 com.labdatahub.protocol.deal.S71200TcpDeal 的 OUT_* 常量一一对应 =====
    // ⚠️ 跨 classloader 的字符串契约，两侧无编译期检查，改一处必须同步另一处
    /** 区类型：DB / M / I / Q */
    private static final String KEY_AREA_TYPE = "areaType";
    /** DB 块号 */
    private static final String KEY_DB_NUMBER = "dbNumber";
    /** 起始字节偏移 */
    public static final String KEY_ADDRESS = "address";
    /** 位偏移（仅 DBX） */
    private static final String KEY_BIT_OFFSET = "bitOffset";
    /** 块类型 */
    private static final String KEY_BLOCK_TYPE = "blockType";
    /** 待写入的字节值（无符号 0-255 整数列表） */
    public static final String KEY_BYTES = "bytes";

    private S7PointWriter() {
    }

    /**
     * 写值任务：一次写入所需的全部信息（即载荷解析后的样子）
     */
    private static class WriteTask {
        /** 区类型 */
        private String areaType;
        /** DB 块号 */
        private int dbNumber;
        /** 起始字节偏移 */
        private int address;
        /** 位偏移（仅 DBX） */
        private int bitOffset;
        /** 块类型 */
        private String blockType;
        /** 待写入字节 */
        private byte[] bytes;
    }

    /**
     * 把载荷写进 PLC
     *
     * <p>不返回写下去的字节：响应里回显的 {@code registers} 由 service 从载荷 JSON 取。
     * 必须这样，因为 503 时写根本没发生，但响应照样要给 {@code address}/{@code count}/{@code registers}
     * —— 回显的唯一来源只能是载荷，不能是「写的结果」。
     *
     * @param componentId 组件ID（一台设备一条连接，锁也按它分）
     * @param payload     协议库产出的载荷 JSON
     * @throws PointConnectionException 平台侧没连上 / 等锁超时（503，请求没发出去，重试安全）
     * @throws PointDeviceException     设备侧异常或结果不确定（504，<b>值可能已写入</b>）
     */
    public static void write(String componentId, String payload)
            throws PointConnectionException, PointDeviceException {

        if (componentId == null || payload == null || payload.trim().isEmpty()) {
            throw new PointConnectionException("写值参数缺失：componentId 或 写入载荷为空");
        }
        WriteTask task = parsePayload(payload);

        // 与读、重连共用同一把锁（见 S7ConnectionManager.lockMap 注释）。
        // 写侧用有界 tryLock：读是采集命脉不能等写，反过来写也不该把 HTTP 线程无限挂住。
        ReentrantLock lock = S7ConnectionManager.getLock(componentId);
        boolean locked;
        try {
            locked = lock.tryLock(LOCK_WAIT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PointConnectionException("平台繁忙：等待写锁被中断，请稍后重试", e);
        }
        if (!locked) {
            throw new PointConnectionException(
                    "平台繁忙：等待读周期结束超过 " + LOCK_WAIT_MS + "ms 仍未取得写锁，请稍后重试");
        }

        boolean needReconnect = false;
        try {
            S7Connector connector = S7ConnectionManager.getConnection(componentId);
            if (connector == null) {
                // 不在这里触发重连：健康检查每 10 秒会自查并重建（checkAndReconnect），
                // 而 forceReconnect 内部带退避 sleep（最长数秒），不该让 HTTP 线程陪着等
                throw new PointConnectionException(noConnectionMessage(componentId));
            }
            // 从这里往下：连接对象是有的，出任何岔子都说明这条 socket 的请求/响应已不可信，
            // 继续复用只会把后续的读也带乱 —— 必须重建
            needReconnect = true;

            DaveArea area = S7DataReader.toDaveArea(task.areaType);
            if (BLOCK_DBX.equals(task.blockType)) {
                writeBit(connector, area, task);
            } else {
                writeRaw(connector, area, task, task.bytes);
            }
        } finally {
            lock.unlock();
            if (needReconnect) {
                // ⚠️ 必须在 unlock 之后：forceReconnect 内部会真建 TCP 连接，
                // 持着锁做会把整条读链路卡住一个 connect 超时（方案 5.4）。
                S7ConnectionManager.forceReconnect(componentId);
            }
        }
    }

    /**
     * 位写入：读回该字节 → 改掉目标位 → 整字节写回
     *
     * <p>预读失败时<b>值还没有写出去</b>，报 503（重试安全）而不是 504 —— 这个区分对调用方很重要，
     * 不能笼统地按「结果不确定」报，否则一次根本没发生的写入会被当成「可能已写入」。
     */
    private static void writeBit(S7Connector connector, DaveArea area, WriteTask task)
            throws PointConnectionException, PointDeviceException {
        int bit = task.bitOffset;
        byte[] current;
        try {
            current = connector.read(area, task.dbNumber, 1, task.address);
        } catch (Exception e) {
            throw new PointConnectionException("位写入前的读取失败，本次未写入（可重试）：" + e.getMessage(), e);
        }
        if (current == null || current.length != 1) {
            throw new PointConnectionException("位写入前的读取返回长度异常，本次未写入（可重试）："
                    + (current == null ? "null" : current.length + " 字节"));
        }
        int cur = current[0] & 0xFF;
        int mask = 1 << bit;
        boolean on = task.bytes[0] != 0;
        int next = on ? (cur | mask) : (cur & ~mask);
        // 即使 next == cur 也照写一遍：位已经是目标值也可能是别人写的，
        // 「这次请求没下发」和「下发了且成功」在调用方看来必须一样
        writeRaw(connector, area, task, new byte[]{(byte) next});
        log.info("[S7写值] 位写入 DB{}.DBX{} 第{}位 -> {}（字节 {} -> {}）",
                task.dbNumber, task.address, bit, on ? 1 : 0, cur, next);
    }

    /**
     * 整段字节写入（DBW/DBD/DBB 唯一路径）
     *
     * <p>库在设备返回码非 0 时抛 {@code S7Exception}；正常返回即设备已受理。
     * S7 的写应答里没有回显，做不了 Modbus 那种「地址/数量逐项对账」。
     */
    private static void writeRaw(S7Connector connector, DaveArea area, WriteTask task, byte[] bytes)
            throws PointDeviceException {
        try {
            connector.write(area, task.dbNumber, task.address, bytes);
        } catch (Exception e) {
            // 请求已经发出去了：返回码错误、超时、断流，值都可能已经写进设备
            throw new PointDeviceException("写值失败，结果不确定（值可能已写入）：" + e.getMessage(), e);
        }
        log.info("[S7写值] {}区 DB{} {} 起始字节{} 写入{}字节：{}",
                task.areaType, task.dbNumber, task.blockType, task.address, bytes.length, toHex(bytes));
    }

    /**
     * 解析协议层载荷
     *
     * <p>载荷是协议库自己产出的，字段缺失属于平台侧问题（协议库与 api 版本不匹配），
     * 报 500 更合适 —— 但这里拿不到 PointWriteException 体系，只能抛连接类异常让 service 收口。
     * 实际上解析失败时 503 的文案「连接不可用」是误导的，所以消息里点明是载荷问题。
     */
    private static WriteTask parsePayload(String payload) {
        JSONObject json;
        try {
            json = JSONObject.parseObject(payload.trim());
        } catch (Exception e) {
            throw new PointConnectionException("写值载荷解析失败（协议层返回的格式不对）：" + e.getMessage(), e);
        }
        if (json == null) {
            throw new PointConnectionException("写值载荷解析失败：内容为空");
        }
        WriteTask task = new WriteTask();
        task.areaType = json.getString(KEY_AREA_TYPE);
        task.blockType = json.getString(KEY_BLOCK_TYPE);
        Integer dbNumber = json.getInteger(KEY_DB_NUMBER);
        Integer address = json.getInteger(KEY_ADDRESS);
        Integer bitOffset = json.getInteger(KEY_BIT_OFFSET);
        if (dbNumber == null || address == null) {
            throw new PointConnectionException("写值载荷缺少 " + KEY_DB_NUMBER + " 或 " + KEY_ADDRESS
                    + "（协议层返回不完整）");
        }
        task.dbNumber = dbNumber;
        task.address = address;
        task.bitOffset = bitOffset == null ? 0 : bitOffset;
        task.bytes = toBytes(json.getList(KEY_BYTES, Integer.class));
        if (task.bytes.length == 0) {
            throw new PointConnectionException("写值载荷里没有字节值（" + KEY_BYTES + " 为空）");
        }
        if (BLOCK_DBX.equals(task.blockType) && (task.bitOffset < 0 || task.bitOffset > 7)) {
            throw new PointConnectionException("写值载荷里的位偏移 " + task.bitOffset + " 超出 0-7 范围");
        }
        return task;
    }

    /**
     * 载荷里的无符号整数列表 → 字节数组
     *
     * <p>协议层回传的是 0-255 的无符号值（不是 Java 的 -128~127），这里按低 8 位还原。
     */
    private static byte[] toBytes(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return new byte[0];
        }
        byte[] bytes = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Integer v = list.get(i);
            bytes[i] = (byte) (v == null ? 0 : v.intValue() & 0xFF);
        }
        return bytes;
    }

    /** 字节数组转 16 进制串（日志用） */
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }

    /**
     * 无可用连接时的提示
     *
     * <p>⚠️ 不能写成「请去开启网络组件」：{@code addConnection} 失败时会把 connections 与
     * configMap 一起清掉，「组件没开」和「开了但连不上」在 connections 里长得一模一样。
     */
    private static String noConnectionMessage(String componentId) {
        S7TcpConfig config = S7ConnectionManager.getConfig(componentId);
        if (config == null) {
            return "无可用连接：该设备没有可用的连接配置（网络组件未开启，或开启时连接失败），请检查网络组件状态";
        }
        return "连接已断开：无法连接 " + config.getIpAddr() + ":" + config.getPort()
                + "，请检查设备网络与网络组件状态";
    }

}
