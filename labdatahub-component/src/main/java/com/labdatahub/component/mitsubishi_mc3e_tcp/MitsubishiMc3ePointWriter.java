//由AI修改
package com.labdatahub.component.mitsubishi_mc3e_tcp;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.fastjson2.JSONObject;
import com.labdatahub.component.protocol.exception.PointConnectionException;
import com.labdatahub.component.protocol.exception.PointDeviceException;

import lombok.extern.slf4j.Slf4j;

/**
 * 点位写值器（三菱 MC QnA-3E 二进制帧）
 *
 * <p>载荷由协议库 {@code MitsubishiMc3eTcpDeal.encode} 产出，是<b>单个 JSON 对象</b>（不是 Modbus 那种区间数组）：
 * <pre>{"areaCode":168,"startAddress":100,"count":1,"words":[8888],"dataType":"int"}  // D 区字写
 * {"areaCode":144,"startAddress":11, "count":1,"words":[1],   "dataType":"bool"} // M 区位写</pre>
 * 拿到的 {@code words} 就是最终要落到 PLC 上的字值（0-65535）/ 位值（0/1），这里<b>不再做任何值与类型的转换</b>
 * （那是协议库 encode 的职责，那边才看得到物模型的 dataType / 字节序 / 缩放）。
 *
 * <p>与 {@link com.labdatahub.component.fins_tcp.FinsPointWriter} 的异同：
 * <ul>
 *   <li><b>字写/位写走的是同一条命令</b>（批量写 {@code 0x1401}），只是子命令不同
 *       （字 {@code 0x0000} / 位 {@code 0x0001}）；位区地址不是「字地址 + 位号」，
 *       而是直接给位编号，所以载荷里<b>没有 bitAddress 这个键</b>（FINS 有）。</li>
 *   <li><b>位写不需要读-改-写</b>：MC 有原生位写命令，一次调用完成，与 FINS 一样干净。</li>
 *   <li><b>写响应带结束码</b>：{@code endCode != 0} 就是设备<b>明确拒绝、本次未写入</b>，
 *       直接决定 504 报哪种子情况（见 {@code PointDeviceException} 类注释）。</li>
 * </ul>
 *
 * <p>⚠️ 载荷键名与协议库侧 {@code MitsubishiMc3eTcpDeal} 的 {@code OUT_*} 常量是<b>跨 classloader 的字符串契约</b>，
 * 两侧无编译期检查，改一处必须同步另一处。其中 {@code startAddress} / {@code words} 两个键与 FINS 刻意取同名，
 * 好让 api 侧的回显解析（{@code ProtocolEncodeInvoker#parseWriteEcho}）不必为三菱再分一支 —— 载荷形态本来就一样。
 */
@Slf4j
public class MitsubishiMc3ePointWriter {

    /** 写锁等待上限（毫秒）。等不到就返回 503 让调用方重试，不把 HTTP 线程无限挂住 */
    private static final int LOCK_WAIT_MS = 5000;

    /** 起始软元件地址上限（帧里是 3 字节小端，X/Y 还有更严格的实际范围，由 PLC 用结束码把关） */
    private static final int MAX_START_ADDRESS = 0xFFFFFF;

    // ===== 载荷键名：与协议 jar 侧 com.labdatahub.protocol.deal.MitsubishiMc3eTcpDeal 的 OUT_* 常量一一对应 =====
    /** 软元件代码（字设备 D=168 / 位设备 M=144 等） */
    private static final String KEY_AREA_CODE = "areaCode";
    /** 起始软元件地址（X/Y 为八进制写法，与点位配置里填的一致） */
    public static final String KEY_START_ADDRESS = "startAddress";
    /** 写入点数 */
    private static final String KEY_COUNT = "count";
    /** 编码后的字值列表（字设备，每个 0-65535）或位值列表（位设备，每个 0/1） */
    public static final String KEY_WORDS = "words";

    private MitsubishiMc3ePointWriter() {
    }

    /**
     * 写值任务：一次写入所需的全部信息（即载荷解析后的样子）
     */
    private static class WriteTask {
        /** 软元件代码 */
        private int areaCode;
        /** 起始软元件地址（原始写法，用于回显与再次换算） */
        private int startAddress;
        /** 待写入的字值/位值 */
        private List<Integer> words;
    }

    /**
     * 把载荷写进 PLC
     *
     * <p>不返回写下去的值：响应里回显的 {@code registers} 由 service 从载荷 JSON 取。
     * 必须这样，因为 503 时写根本没发生，但响应照样要给 {@code address}/{@code count}/{@code registers}
     * —— 回显的唯一来源只能是载荷，不能是「写的结果」。
     *
     * @param componentId 组件ID（一台设备一条连接，锁也按它分）
     * @param payload     协议库产出的载荷 JSON
     * @throws PointConnectionException 平台侧没连上 / 等锁超时（503，请求没发出去，重试安全）
     * @throws PointDeviceException     设备侧异常（504）。文案区分两种子情况：
     *                                  「设备拒绝、<b>本次未写入</b>」（结束码非 0）与
     *                                  「<b>结果不确定</b>（值可能已写入）」（超时/断流）
     */
    public static void write(String componentId, String payload)
            throws PointConnectionException, PointDeviceException {

        if (componentId == null || payload == null || payload.trim().isEmpty()) {
            throw new PointConnectionException("写值参数缺失：componentId 或 写入载荷为空");
        }
        WriteTask task = parsePayload(payload);

        // 与读、重连共用同一把锁（见 MitsubishiMc3eConnectionManager.lockMap 注释）。
        // 写侧用有界 tryLock：读是采集命脉不能等写，反过来写也不该把 HTTP 线程无限挂住。
        ReentrantLock lock = MitsubishiMc3eConnectionManager.getLock(componentId);
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
            if (!hasUsableConnection(componentId)) {
                // 不在这里触发重连：健康检查每 10 秒会自查并重建（checkAndReconnect），
                // 而 forceReconnect 内部带退避 sleep（最长数秒），不该让 HTTP 线程陪着等
                throw new PointConnectionException(noConnectionMessage(componentId));
            }
            // 从这里往下：连接是好的，但 socket 上出任何岔子都说明帧可能错位，
            // 继续复用只会把后续的读也带乱 —— 必须重建
            needReconnect = true;

            try {
                MitsubishiMc3eDataReader.writeMemoryArea(componentId, task.areaCode, task.startAddress,
                        task.words.size(), task.words);
            } catch (MitsubishiMc3eResponseException e) {
                // PLC 正常响应了，只是结束码非 0（软元件码/地址/点数越界、写保护…）：本次未写入。
                // 连接是好的，不能重连 —— 重连解决不了配置错误，只会白抖一次连接
                needReconnect = false;
                throw new PointDeviceException("设备拒绝写入，本次未写入：" + e.getMessage(), e);
            } catch (Exception e) {
                // 传输层异常（超时/断流/EOF）：请求已经发出去了，值可能已写入，结果不确定
                throw new PointDeviceException("写值失败，结果不确定（值可能已写入）：" + e.getMessage(), e);
            }
            log.info("[MC3E写值] componentId={} 软元件码0x{} 起始地址{} {} 写入{}点：{}",
                    componentId, Integer.toHexString(task.areaCode), task.startAddress,
                    MitsubishiMc3eDataReader.isWordDevice(task.areaCode) ? "（字访问）" : "（位访问）",
                    task.words.size(), task.words);
        } finally {
            lock.unlock();
            if (needReconnect) {
                // ⚠️ 必须在 unlock 之后：forceReconnect 内部会真建 TCP 连接 + 退避 sleep，
                // 持着锁做会把整条读链路卡住一个 connect 超时（方案 5.4）。
                MitsubishiMc3eConnectionManager.forceReconnect(componentId);
            }
        }
    }

    /**
     * 连接是否可用（本地状态检查，不发探测帧 —— 见 {@code MitsubishiMc3eConnectionManager#isConnectionValid} 注释）
     */
    private static boolean hasUsableConnection(String componentId) {
        Socket socket = MitsubishiMc3eConnectionManager.connections.get(componentId);
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * 解析协议层载荷
     *
     * <p>载荷是协议库自己产出的，字段缺失属于平台侧问题（协议库与 api 版本不匹配），
     * 报 500 更合适 —— 但这里拿不到 PointWriteException 体系，只能抛连接类异常让 service 收口。
     * 实际上解析失败时 503 的文案「连接不可用」是误导的，所以消息里点明是载荷问题。
     *
     * <p>这里把 {@code MitsubishiMc3eDataReader} 会校验的东西<b>先查一遍</b>：一来报错更早更准，
     * 二来避免它抛的 {@link Exception} 落进下面的传输层分支被误报成
     * 「结果不确定（值可能已写入）」——那种情况下请求压根没发出去。
     * X/Y 的八进制换算尤其要在这里先做一次：配置写错时它抛的是普通 Exception，会被误判。
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
        Integer areaCode = json.getInteger(KEY_AREA_CODE);
        Integer startAddress = json.getInteger(KEY_START_ADDRESS);
        Integer count = json.getInteger(KEY_COUNT);
        if (areaCode == null || startAddress == null) {
            throw new PointConnectionException("写值载荷缺少 " + KEY_AREA_CODE + " 或 " + KEY_START_ADDRESS
                    + "（协议层返回不完整）");
        }
        task.areaCode = areaCode;
        task.startAddress = startAddress;
        task.words = json.getList(KEY_WORDS, Integer.class);

        if (startAddress < 0 || startAddress > MAX_START_ADDRESS) {
            throw new PointConnectionException("写值载荷里的起始地址 " + startAddress
                    + " 超出 MC 帧 3 字节地址的 0-" + MAX_START_ADDRESS + " 范围");
        }
        // X/Y 是八进制软元件：这里先换算一次，把「配置了非法八进制」挡在发帧之前
        try {
            MitsubishiMc3eDataReader.toRealAddress(task.areaCode, task.startAddress);
        } catch (Exception e) {
            throw new PointConnectionException("写值载荷里的地址不合法：" + e.getMessage(), e);
        }
        if (task.words == null || task.words.isEmpty()) {
            throw new PointConnectionException("写值载荷里没有待写入的值（" + KEY_WORDS + " 为空）");
        }
        if (count != null && count.intValue() != task.words.size()) {
            throw new PointConnectionException("写值载荷里的点数与值个数不一致：" + KEY_COUNT + "=" + count
                    + "，" + KEY_WORDS + " 有 " + task.words.size() + " 项");
        }

        boolean wordDevice = MitsubishiMc3eDataReader.isWordDevice(task.areaCode);
        int maxCount = wordDevice ? MitsubishiMc3eDataReader.MAX_WORD_COUNT
                : MitsubishiMc3eDataReader.MAX_BIT_COUNT;
        if (task.words.size() > maxCount) {
            throw new PointConnectionException("写值载荷点数 " + task.words.size()
                    + " 超出 MC 单条写命令上限 " + maxCount);
        }
        // 字写每点 2 字节无符号、位写每点 0/1 —— 值域不对说明协议层编码错了，不能发出去
        for (Integer word : task.words) {
            if (word == null || (wordDevice ? (word < 0 || word > 0xFFFF) : (word != 0 && word != 1))) {
                throw new PointConnectionException("写值载荷里的值 " + word + " 超出"
                        + (wordDevice ? "字设备 0-65535" : "位设备 0/1") + " 范围（协议层编码有误）");
            }
        }
        return task;
    }

    /**
     * 无可用连接时的提示
     *
     * <p>⚠️ 不能写成「请去开启网络组件」：{@code addConnection} 失败时会把 connections 与
     * configMap 一起清掉，「组件没开」和「开了但连不上」在 connections 里长得一模一样。
     */
    private static String noConnectionMessage(String componentId) {
        MitsubishiMc3eTcpConfig config = MitsubishiMc3eConnectionManager.configMap.get(componentId);
        if (config == null) {
            return "无可用连接：该设备没有可用的连接配置（网络组件未开启，或开启时连接失败），请检查网络组件状态";
        }
        return "连接已断开：无法连接 " + config.getIpAddr() + ":" + config.getPort()
                + "，请检查设备网络与网络组件状态";
    }

}
