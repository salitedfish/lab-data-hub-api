//由AI修改
package com.labdatahub.business.utils;

import com.labdatahub.business.domain.PointWriteMeta;
import com.labdatahub.component.fins_tcp.FinsPointWriter;
import com.labdatahub.component.mitsubishi_mc3e_tcp.MitsubishiMc3ePointWriter;
import com.labdatahub.component.modbus_tcp.ModbusPointWriter;
import com.labdatahub.component.protocol.exception.PointConnectionException;
import com.labdatahub.component.protocol.exception.PointDeviceException;
import com.labdatahub.component.s7_tcp.S7PointWriter;

/**
 * 把编码好的载荷下发到设备
 *
 * <p>这一层看着薄，但它是<b>链路里唯一的「协议相关」出口</b>（另一个是
 * {@code PointWriteMetaResolver} 里取地址那段）。加第二个可写协议时，
 * 分派就长在这里，上面 service 的编排一行不用动。
 *
 * <p>目前四个实现：{@code MODBUS_TCP} → {@link ModbusPointWriter}（保持寄存器，功能码 16）、
 * {@code S71200_TCP} → {@link S7PointWriter}（按块类型写字节，DBX 走位读-改-写）、
 * {@code OMRONFINS_TCP} → {@link FinsPointWriter}（内存区访问，字区/位区由区码决定）、
 * {@code MITSUBISHI_MC3E_TCP} → {@link MitsubishiMc3ePointWriter}（批量写 0x1401，
 * 字/位由软元件码决定）。四者都负责连接自愈、读写串行锁、结果判定，并抛出具名的两种异常
 * （503 平台侧没连上 / 504 设备侧含「结果不确定」）。
 *
 * <p><b>这里不做 try/catch</b>：异常一路透到 service，由 service 做唯一一次码值映射。
 * 分派用 netType 而不是「组件有没有 slaveId」之类的外观特征 —— 那是碰运气，
 * netType 才是 {@code PointWriteMetaResolver} 已经准入校验过的那一项。
 */
public class PointWriter {

    /** MODBUS_TCP */
    private static final String NET_TYPE_MODBUS_TCP = "MODBUS_TCP";

    /** S7-1200（西门子 ISO-TCP） */
    private static final String NET_TYPE_S71200_TCP = "S71200_TCP";

    /** 欧姆龙 FINS/TCP */
    private static final String NET_TYPE_OMRONFINS_TCP = "OMRONFINS_TCP";

    /** 三菱 MC QnA 兼容 3E 帧 */
    private static final String NET_TYPE_MITSUBISHI_MC3E_TCP = "MITSUBISHI_MC3E_TCP";

    private PointWriter() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 下发写入
     *
     * @param meta    写值元数据（取 componentId / slaveId / netType）
     * @param payload 协议层产出的载荷 JSON
     * @throws PointConnectionException 平台侧没连上 / 等锁超时（503，重试安全）
     * @throws PointDeviceException     设备侧异常或结果不确定（504，值可能已写入）
     */
    public static void write(PointWriteMeta meta, String payload)
            throws PointConnectionException, PointDeviceException {
        String netType = meta.getNetType() == null ? "" : meta.getNetType().trim();
        if (NET_TYPE_S71200_TCP.equals(netType)) {
            S7PointWriter.write(meta.getComponentId(), payload);
            return;
        }
        if (NET_TYPE_OMRONFINS_TCP.equals(netType)) {
            FinsPointWriter.write(meta.getComponentId(), payload);
            return;
        }
        if (NET_TYPE_MITSUBISHI_MC3E_TCP.equals(netType)) {
            MitsubishiMc3ePointWriter.write(meta.getComponentId(), payload);
            return;
        }
        if (!NET_TYPE_MODBUS_TCP.equals(netType)) {
            // 理论到不了这里：PointWriteMetaResolver 的准入判断已经拦过一遍。
            // 真到了说明准入与分派两边对不上，报出来比默默按 MODBUS 写上去强
            throw new PointConnectionException("不支持的写入协议：" + meta.getNetType()
                    + "（准入校验与下发分派不一致，请检查代码）");
        }
        ModbusPointWriter.write(meta.getComponentId(),
                new ModbusPointWriter.PointWriteTask(meta.getSlaveId(), payload));
    }
}
