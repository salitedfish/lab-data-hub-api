//由AI修改
package com.labdatahub.component.modbus_tcp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.fastjson2.JSONArray;
import com.labdatahub.component.protocol.exception.PointConnectionException;
import com.labdatahub.component.protocol.exception.PointDeviceException;

import lombok.extern.slf4j.Slf4j;
import net.wimpi.modbus.ModbusSlaveException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ExceptionResponse;
import net.wimpi.modbus.msg.ModbusResponse;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.msg.WriteMultipleRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;

/**
 * 点位写值器（MODBUS 保持寄存器，功能码 16）
 *
 * <p>与老的 {@link ModbusDataReader#writeMultipleHoldingRegisters(String, Integer, String)} 的区别，
 * 也是这个类存在的全部理由：
 * <ol>
 *   <li><b>连接自愈</b>：老的用 {@code connections.get()}，连接没了就直接返回 false，
 *       永远等不到重连；这里用 {@link ModbusConnectionManager#getValidConnection}，无效会同步重建。</li>
 *   <li><b>真验证响应</b>：老的写死 {@code return true}，写没写进去根本不知道；
 *       这里逐项验证空响应 / 设备异常响应 / 回显地址 / 回显数量。</li>
 *   <li><b>区分「没写」和「不知道写没写」</b>：老的一律返 false，调用方只能猜。
 *       这里平台侧没连上抛 {@link PointConnectionException}（503，重试安全），
 *       设备侧异常或结果不确定抛 {@link PointDeviceException}（504，值可能已写入）。</li>
 * </ol>
 *
 * <p>老方法<b>保持原样不动</b>，仍在给老的指令下发链路用；新老两条链路并存。
 */
@Slf4j
public class ModbusPointWriter {

    /** 写锁等待上限（毫秒）。等不到就返回 503 让调用方重试，不把 HTTP 线程无限挂住 */
    private static final int LOCK_WAIT_MS = 5000;

    /** Modbus 功能码 16（写多个保持寄存器）单次可写的寄存器数上限 */
    private static final int MAX_WRITE_REGISTERS = 123;

    private ModbusPointWriter() {
    }

    /**
     * 写值任务：一次写入所需的全部信息
     *
     * <p>不含「读什么类型/怎么编码」——那是协议库 encode 的事，进到这里时值已经编码成寄存器字了。
     */
    public static class PointWriteTask {

        /** 从站ID（Slave ID），来自网络组件 other_config */
        private Integer slaveId;

        /** 写值报文：{@code RangeParserUtil.RangeItem} 数组的 JSON，由协议库 encode 产出 */
        private String rangeItemsJson;

        public PointWriteTask() {
        }

        public PointWriteTask(Integer slaveId, String rangeItemsJson) {
            this.slaveId = slaveId;
            this.rangeItemsJson = rangeItemsJson;
        }

        public Integer getSlaveId() {
            return slaveId;
        }

        public void setSlaveId(Integer slaveId) {
            this.slaveId = slaveId;
        }

        public String getRangeItemsJson() {
            return rangeItemsJson;
        }

        public void setRangeItemsJson(String rangeItemsJson) {
            this.rangeItemsJson = rangeItemsJson;
        }
    }

    /**
     * 写入点位值
     *
     * <p>不返回写下去的寄存器：响应里要回显的 {@code registers} 由 service 从载荷 JSON
     * （{@code modbusWriteJson}）里取。必须这样，因为 503（连接不可用）时写根本没发生，
     * 但响应照样要给出 {@code address}/{@code count}/{@code registers} —— 回显的唯一来源
     * 只能是载荷，不能是「写的结果」。
     *
     * @param componentId 组件ID（一台设备一条连接，锁也按它分）
     * @param task        写值任务（从站ID + 报文）
     * @throws PointConnectionException 平台侧没连上 / 等锁超时（503，请求没发出去，重试安全）
     * @throws PointDeviceException     设备侧异常或结果不确定（504，<b>值可能已写入</b>）
     */
    public static void write(String componentId, PointWriteTask task)
            throws PointConnectionException, PointDeviceException {

        if (componentId == null || task == null) {
            throw new PointConnectionException("写值参数缺失：componentId 或 写入任务为空");
        }
        if (task.getSlaveId() == null) {
            throw new PointConnectionException("写值参数缺失：从站ID 为空，请检查网络组件配置");
        }

        List<RangeParserUtil.RangeItem> itemList = parseRangeItems(task.getRangeItemsJson());
        if (itemList.isEmpty()) {
            throw new PointConnectionException("写值报文为空，没有可写入的寄存器");
        }

        // 与读、重连共用同一把锁（方案 5.4）。写侧用有界 tryLock：
        // 读是采集命脉不能等写，反过来写也不该把 HTTP 线程无限挂住 —— 等不到就 503 让外面重试。
        ReentrantLock lock = ModbusConnectionManager.getLock(componentId);
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
            TCPMasterConnection connection = ModbusConnectionManager.getValidConnection(componentId);
            if (connection == null || !connection.isConnected()) {
                throw new PointConnectionException(noConnectionMessage(componentId));
            }

            for (RangeParserUtil.RangeItem item : itemList) {
                writeOne(componentId, connection, task.getSlaveId(), item);
            }

        } catch (PointDeviceException e) {
            // 回显对不上 / 超时：这条 socket 上的请求响应已错位，而 isConnectionValid 只看
            // 本地 isConnected()，错位没断的 socket 会被判「有效」继续复用 —— 必须重建。
            needReconnect = true;
            throw e;
        } finally {
            lock.unlock();
            if (needReconnect) {
                // ⚠️ 必须在 unlock 之后：forceReconnect 内部会真建 TCP 连接，
                // 持着锁做会把整条读链路卡住一个 connect 超时（方案 5.4）。
                ModbusConnectionManager.forceReconnect(componentId);
            }
        }
    }

    /**
     * 解析写值报文
     *
     * <p>⚠️ {@code RangeItem} 没有无参构造（2 参 + setter），fastjson2 走 Unsafe 反序列化。
     * 这条路径在老的指令下发里从没真正跑通过，第一次实测要重点确认。
     */
    private static List<RangeParserUtil.RangeItem> parseRangeItems(String rangeItemsJson) {
        if (rangeItemsJson == null || rangeItemsJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            List<RangeParserUtil.RangeItem> itemList =
                    JSONArray.parseArray(rangeItemsJson, RangeParserUtil.RangeItem.class);
            return itemList == null ? new ArrayList<>() : itemList;
        } catch (Exception e) {
            throw new PointConnectionException("写值报文解析失败：" + e.getMessage(), e);
        }
    }

    /**
     * 写一个连续区间
     *
     * @return 实际写下去的寄存器字列表
     */
    private static List<Integer> writeOne(String componentId, TCPMasterConnection connection,
                                          Integer slaveId, RangeParserUtil.RangeItem item)
            throws PointDeviceException {

        int startAddr = item.getStart();
        List<Integer> values = item.getRegisterList();
        if (values == null || values.isEmpty()) {
            throw new PointDeviceException("写值报文里地址 " + startAddr + " 没有寄存器值");
        }
        if (values.size() > MAX_WRITE_REGISTERS) {
            throw new PointDeviceException("写入寄存器数量 " + values.size()
                    + " 超过功能码 16 的协议上限 " + MAX_WRITE_REGISTERS);
        }

        WriteMultipleRegistersRequest request;
        try {
            Register[] registers = new Register[values.size()];
            for (int i = 0; i < values.size(); i++) {
                registers[i] = new SimpleRegister(values.get(i));
            }
            request = new WriteMultipleRegistersRequest();
            request.setUnitID(slaveId);
            request.setReference(startAddr);
            request.setRegisters(registers);
        } catch (Exception e) {
            throw new PointDeviceException("写值请求构造失败：" + e.getMessage(), e);
        }

        ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
        transaction.setRequest(request);
        // 不做传输层重试：写是赋值，重发同一个值虽然幂等，但超时后再重试会把这个请求
        // 「结果不确定」的窗口拉长，而这里要的恰恰是一次干净、可判定的执行。
        transaction.setRetries(0);

        try {
            transaction.execute();
        } catch (ModbusSlaveException e) {
            // 设备明确拒绝（异常响应，功能码最高位为 1）—— 值为未写入
            throw new PointDeviceException(
                    "设备拒绝写入：地址 " + startAddr + " 异常码 " + e.getType() + "（"
                            + exceptionCodeText(e.getType()) + "）", e);
        } catch (Exception e) {
            // 超时 / 断流 / 响应格式非法：请求已经发出去了，值可能已经写进设备
            throw new PointDeviceException(
                    "写值超时或链路中断，结果不确定（值可能已写入）：" + e.getMessage(), e);
        }

        ModbusResponse response;
        try {
            response = transaction.getResponse();
        } catch (Exception e) {
            throw new PointDeviceException(
                    "写值响应读取失败，结果不确定（值可能已写入）：" + e.getMessage(), e);
        }
        if (response == null) {
            throw new PointDeviceException("写值未收到响应，结果不确定（值可能已写入）");
        }
        // jamod 的 execute() 遇到异常响应会直接抛 ModbusSlaveException，正常返回时不会是
        // ExceptionResponse；这里兜一道底，防 jamod 行为在别的版本上不一样。
        if (response instanceof ExceptionResponse) {
            int code = ((ExceptionResponse) response).getExceptionCode();
            throw new PointDeviceException(
                    "设备拒绝写入：地址 " + startAddr + " 异常码 " + code + "（" + exceptionCodeText(code) + "）");
        }
        if (!(response instanceof WriteMultipleRegistersResponse)) {
            throw new PointDeviceException("写值响应类型异常，结果不确定（值可能已写入）："
                    + response.getClass().getSimpleName());
        }

        WriteMultipleRegistersResponse writeResponse = (WriteMultipleRegistersResponse) response;
        int respReference = writeResponse.getReference();
        int respWordCount = writeResponse.getWordCount();

        // 下面这两项对不上，说明设备确实处理了这次请求、但回显跟请求不符：
        // 值大概率已经写进去了，按「结果不确定」报 504，绝不能当成「没写」。
        if (respReference != startAddr) {
            throw new PointDeviceException("写值回显地址不符，结果不确定（值可能已写入）：请求 "
                    + startAddr + "，设备回显 " + respReference);
        }
        if (respWordCount != values.size()) {
            throw new PointDeviceException("写值回显数量不符，结果不确定（值可能已写入）：请求 "
                    + values.size() + " 个寄存器，设备回显 " + respWordCount);
        }

        log.info("[Modbus写值] componentId={} slaveId={} 地址={} 写入{}个寄存器：{}",
                componentId, slaveId, startAddr, values.size(), values);
        return values;
    }

    /**
     * 无可用连接时的提示
     *
     * <p>⚠️ 不能写成「请去开启网络组件」：{@code addConnection} 失败时会把 connections 与
     * configMap 一起清掉，「组件没开」和「开了但连不上」在 connections 里长得一模一样，
     * 一口咬定是前者会把排查方向带偏。
     */
    private static String noConnectionMessage(String componentId) {
        ModbusTcpConfig config = ModbusConnectionManager.getConfig(componentId);
        if (config == null) {
            return "无可用连接：该设备没有可用的连接配置（网络组件未开启，或开启时连接失败），请检查网络组件状态";
        }
        return "连接已断开：无法连接 " + config.getIpAddr() + ":" + config.getPort()
                + "，请检查设备网络与网络组件状态";
    }

    /** Modbus 异常码转中文 */
    private static String exceptionCodeText(int code) {
        switch (code) {
            case 1: return "非法功能码";
            case 2: return "非法数据地址";
            case 3: return "非法数据值";
            case 4: return "从站设备故障";
            case 5: return "确认";
            case 6: return "从站设备忙";
            case 8: return "存储奇偶校验错";
            case 10: return "网关路径不可用";
            case 11: return "网关目标设备无响应";
            default: return "未知异常码";
        }
    }
}
