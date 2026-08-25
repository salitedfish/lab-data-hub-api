//由AI修改
package com.labdatahub.component.fanuc_focas;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 数据读取器，负责按点位地址（readType.param1.param2）从 FANUC FOCAS2 设备读取单项数据
 * FOCAS2 通过 fwlib32 库按函数调用读取，一次读一个点；读失败自动重连重试一次
 * 所有读取函数均为官方 FOCAS2 函数族（cnc_rdaxisdata/cnc_acts/cnc_rdspload/cnc_actf/
 * cnc_statinfo/cnc_rdprgnum/cnc_rdalmmsg/cnc_rdmacro/cnc_rdtimer/pmc_rdpmcrng）：
 *   - FOCAS2 无 cnc_rdspindle/cnc_rdact/cnc_rdtcode/pmc_rdpmc 等函数（旧代码误用）
 *   - 主轴倍率、顺序号无直接读取函数，返回 null（前端点表已标注）
 *   - 刀具号通过系统宏变量读取：#3901=当前刀具、#3902=下一把（程序指定）
 */
public class FanucFocasDataReader {

    private FanucFocasDataReader() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 读取单个点位（带自动重连重试）
     * 连接被机床掐断或句柄失效时，自动清理死句柄、重连一次并重发，自愈后正常返回
     * @param componentId 组件ID，用于获取连接句柄
     * @param readType 采集项类型：axis/spindle/feed/mode/status/prgnum/alarm/tcode/macro/timer/pmc
     * @param param1 参数1（轴号/子项/宏变量号等）
     * @param param2 参数2（坐标类型/地址号等）
     * @return 原始值字符串；点位不支持或读取失败返回null
     * @throws Exception 通信异常（重连重试后仍失败时抛出）
     */
    public static String readPoint(String componentId, String readType, Integer param1, Integer param2) throws Exception {
        ReentrantLock lock = FanucFocasConnectionManager.getReadLock(componentId);
        lock.lock();
        try {
            Short handle = FanucFocasConnectionManager.handleMap.get(componentId);
            if (handle == null) {
                throw new IOException("FOCAS2连接已断开，请重新连接");
            }
            try {
                return doReadPoint(handle, readType, param1, param2);
            } catch (IOException e) {
                // 连接级异常（socket错误/句柄失效）：重连一次后重发（ReentrantLock 可重入，forceReconnect 内部会再次加锁）
                System.err.printf("[FOCAS2读取] componentId=%s 连接异常（%s），自动重连后重试%n", componentId, e.getMessage());
                FanucFocasConnectionManager.forceReconnect(componentId);
                Short newHandle = FanucFocasConnectionManager.handleMap.get(componentId);
                if (newHandle == null) {
                    throw e;
                }
                return doReadPoint(newHandle, readType, param1, param2);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 单次读取点位（不含重试）
     * @param handle FOCAS2 连接句柄
     * @param readType 采集项类型
     * @param param1 参数1
     * @param param2 参数2
     * @return 原始值字符串；点位不支持或读取失败返回null
     * @throws IOException socket级错误（可重试）
     */
    private static String doReadPoint(Short handle, String readType, Integer param1, Integer param2) throws IOException {
        if (handle == null || readType == null) {
            return null;
        }
        Fwlib32 lib = Fwlib32Loader.get();
        String type = readType.trim().toLowerCase();
        switch (type) {
            case "axis": {
                // 坐标：param1=轴号(1起) param2=坐标类型(1机械 2绝对 3相对 4剩余)
                if (param1 == null || param1 < 1 || param2 == null || param2 < 1 || param2 > 4) {
                    return null;
                }
                // fwlib cnc_rdaxisdata 的 type（cls=1 坐标值）：0=绝对 1=机械 2=相对 3=剩余移动量；
                // 前端 param2：1=机械 2=绝对 3=相对 4=剩余，需映射到 fwlib type
                short axisType;
                switch (param2) {
                    case 1:
                        axisType = 1; // 机械坐标 → fwlib type 1
                        break;
                    case 2:
                        axisType = 0; // 绝对坐标 → fwlib type 0
                        break;
                    case 3:
                        axisType = 2; // 相对坐标 → fwlib type 2
                        break;
                    default:
                        axisType = 3; // 剩余移动量 → fwlib type 3
                        break;
                }
                // 官方 cnc_rdaxisdata 从第 1 根轴开始按 (*len) 读取，读第 param1 根轴需 len=param1、取 axdata[param1-1]
                short[] len = new short[]{param1.shortValue()};
                Fwlib32.ODBAXDT[] axdata = new Fwlib32.ODBAXDT[param1];
                short ret = lib.cnc_rdaxisdata(handle, (short) 1, new short[]{axisType}, (short) 1, len, axdata);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_rdaxisdata socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                // 实际有效轴数少于请求轴数：该轴不存在
                if (len[0] < param1) {
                    return null;
                }
                Fwlib32.ODBAXDT data = axdata[param1 - 1];
                // 位置值 = data / 10^dec
                return String.valueOf(data.data.intValue() / Math.pow(10, data.dec));
            }
            case "spindle": {
                // 主轴：param1=1转速 2倍率 3负载 4报警
                if (param1 == null || param1 < 1 || param1 > 4) {
                    return null;
                }
                switch (param1) {
                    case 1: {
                        // 实际主轴转速：cnc_acts（FOCAS2 无 cnc_rdspindle）
                        Fwlib32.ODBACT data = new Fwlib32.ODBACT();
                        short ret = lib.cnc_acts(handle, data);
                        if (ret == Fwlib32.EW_SOCKET) {
                            throw new IOException("cnc_acts socket错误(" + ret + ")");
                        }
                        if (ret != Fwlib32.EW_OK) {
                            return null;
                        }
                        return String.valueOf(data.data.longValue());
                    }
                    case 2:
                        // 主轴倍率：FOCAS2 无直接读取函数，需走 PMC 读取（G 区倍率地址），前端点表已标注
                        return null;
                    case 3: {
                        // 主轴负载：cnc_rdspload，负载在 ODBSPN.data[0]
                        Fwlib32.ODBSPN data = new Fwlib32.ODBSPN();
                        short ret = lib.cnc_rdspload(handle, (short) 1, data);
                        if (ret == Fwlib32.EW_SOCKET) {
                            throw new IOException("cnc_rdspload socket错误(" + ret + ")");
                        }
                        if (ret != Fwlib32.EW_OK) {
                            return null;
                        }
                        return String.valueOf(data.data[0]);
                    }
                    default: {
                        // 主轴报警：cnc_rdalmmsg type=9（0i-D/F 主轴报警 SP）
                        short[] num = new short[]{4};
                        Fwlib32.ODBALMMSG[] alms = new Fwlib32.ODBALMMSG[4];
                        short ret = lib.cnc_rdalmmsg(handle, (short) 9, num, alms);
                        if (ret == Fwlib32.EW_SOCKET) {
                            throw new IOException("cnc_rdalmmsg socket错误(" + ret + ")");
                        }
                        if (ret != Fwlib32.EW_OK) {
                            return null;
                        }
                        return num[0] > 0 ? "1" : "0";
                    }
                }
            }
            case "feed": {
                // 进给：param1=1 实际进给（cnc_actf，单位随 G94/G95：mm/min 或 mm/rev）
                if (param1 == null || param1 != 1) {
                    return null;
                }
                Fwlib32.ODBACT data = new Fwlib32.ODBACT();
                short ret = lib.cnc_actf(handle, data);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_actf socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                return String.valueOf(data.data.longValue());
            }
            case "mode": {
                // 操作模式：cnc_statinfo → ODBST.aut（0i-D/F）
                // 0=MDI 1=MEM 2=*** 3=EDIT 4=HANDLE 5=JOG 6=Teach JOG 7=Teach HANDLE 8=INC 9=REF 10=RMT 11=TEST
                // 由AI修改：mode 点位固定读 aut，不使用 param1（DB 配置 operation_mode 的 param1 为空）。
                // 此前要求 param1 非空，导致 param1 留空的操作模式点位静默无数据。
                Fwlib32.ODBST data = new Fwlib32.ODBST();
                short ret = lib.cnc_statinfo(handle, data);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_statinfo socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                return String.valueOf(data.aut);
            }
            case "status": {
                // 运行状态（cnc_statinfo → ODBST.run/emergency/aut）：
                //   param1=1 运行/加工中（run==3 START 时为1）
                //   param1=2 停止（run==1 STOP 时为1）
                //   param1=3 急停（emergency 非0为1）
                //   param1=4 自动方式（aut==1 MEM 时为1）
                if (param1 == null || param1 < 1 || param1 > 4) {
                    return null;
                }
                Fwlib32.ODBST data = new Fwlib32.ODBST();
                short ret = lib.cnc_statinfo(handle, data);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_statinfo socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                switch (param1) {
                    case 1:
                        // 运行/加工中：自动运行启动中（run==3）
                        return data.run == 3 ? "1" : "0";
                    case 2:
                        // 停止：run==1（STOP）
                        return data.run == 1 ? "1" : "0";
                    case 3:
                        // 急停：emergency 非 0（1=急停 2=复位中）
                        return data.emergency == 0 ? "0" : "1";
                    default:
                        // 自动方式：aut==1（MEM/自动）
                        return data.aut == 1 ? "1" : "0";
                }
            }
            case "prgnum": {
                // 程序：param1=1 程序号；param1=2 顺序号（FOCAS2 无直接读取函数，返回null）
                if (param1 == null) {
                    return null;
                }
                if (param1 == 2) {
                    return null;
                }
                Fwlib32.ODBPRO data = new Fwlib32.ODBPRO();
                short ret = lib.cnc_rdprgnum(handle, data);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_rdprgnum socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                return String.valueOf(data.data);
            }
            case "alarm": {
                // 报警：param1=1 报警数量；param1=2 报警文本（多条取最后一条）
                // cnc_rdalmmsg：type=-1 读全部，num in=请求条数，out=实际条数
                if (param1 == null || (param1 != 1 && param1 != 2)) {
                    return null;
                }
                short[] num = new short[]{16};
                Fwlib32.ODBALMMSG[] alms = new Fwlib32.ODBALMMSG[16];
                short ret = lib.cnc_rdalmmsg(handle, (short) -1, num, alms);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_rdalmmsg socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                int count = num[0];
                if (param1 == 1) {
                    return String.valueOf(count);
                }
                // 报警文本：取最后一条（GBK 解码，去末尾空字节）
                if (count <= 0) {
                    return "";
                }
                return decodeCncString(alms[count - 1].alm_msg);
            }
            case "tcode": {
                // 刀具：param1=1 当前刀具（#3901）；param1=2 下一把/程序指定刀具（#3902）
                // FOCAS2 无 cnc_rdtcode，通过系统宏变量读取刀具号（宏值即整数刀具号）
                if (param1 == null || (param1 != 1 && param1 != 2)) {
                    return null;
                }
                short macroNo = param1 == 1 ? (short) 3901 : (short) 3902;
                Fwlib32.ODBM data = new Fwlib32.ODBM();
                short ret = lib.cnc_rdmacro(handle, macroNo, (short) 10, data);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_rdmacro socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                return String.valueOf(data.mcr_val.longValue());
            }
            case "macro": {
                // 宏变量：param1=宏变量号（如 500、3901）；值 = mcr_val / 10^dec_val
                if (param1 == null || param1 < 1) {
                    return null;
                }
                Fwlib32.ODBM data = new Fwlib32.ODBM();
                short ret = lib.cnc_rdmacro(handle, param1.shortValue(), (short) 10, data);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_rdmacro socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                if (data.dec_val <= 0) {
                    return String.valueOf(data.mcr_val.longValue());
                }
                return String.valueOf(data.mcr_val.longValue() / Math.pow(10, data.dec_val));
            }
            case "timer": {
                // 时间：param1=1 运行 2 切削 3 循环 4 上电（cnc_rdtimer type 1/2/3/0），单位分钟
                if (param1 == null || param1 < 1 || param1 > 4) {
                    return null;
                }
                short timerType;
                switch (param1) {
                    case 1:
                        timerType = 1;
                        break;
                    case 2:
                        timerType = 2;
                        break;
                    case 3:
                        timerType = 3;
                        break;
                    default:
                        timerType = 0;
                        break;
                }
                Fwlib32.IODBTIME data = new Fwlib32.IODBTIME();
                short ret = lib.cnc_rdtimer(handle, timerType, data);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_rdtimer socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                return String.valueOf(data.minute.longValue());
            }
            case "pmc": {
                // PMC 信号：param1=1(F) 2(G) 3(X) 4(Y)，param2=字节地址号；读整字节，位需自行按位解析
                // pmc_rdpmcrng：adr_type 0=G 1=F 2=Y 3=X；data_type 0=字节；单字节读 length=9
                if (param1 == null || param2 == null || param2 < 0) {
                    return null;
                }
                short adrType;
                switch (param1) {
                    case 1:
                        adrType = 1; // F（信号 CNC->PMC）
                        break;
                    case 2:
                        adrType = 0; // G（信号 PMC->CNC）
                        break;
                    case 3:
                        adrType = 3; // X（信号 机床->PMC）
                        break;
                    case 4:
                        adrType = 2; // Y（信号 PMC->机床）
                        break;
                    default:
                        return null;
                }
                Fwlib32.IODBPMC pmc = new Fwlib32.IODBPMC();
                short ret = lib.pmc_rdpmcrng(handle, adrType, (short) 0,
                        param2.shortValue(), param2.shortValue(), (short) 9, pmc);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("pmc_rdpmcrng socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                return String.valueOf(pmc.byteAt(0));
            }
            default:
                // 不支持的采集项类型
                return null;
        }
    }

    /**
     * 解码 CNC 报警文本等字节串：GBK 编码（中文字符），遇末尾空字节截断
     * @param bytes 原始字节数组
     * @return 解码后的字符串
     */
    private static String decodeCncString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        int len = 0;
        while (len < bytes.length && bytes[len] != 0) {
            len++;
        }
        try {
            return new String(bytes, 0, len, Charset.forName("GBK"));
        } catch (Exception e) {
            return new String(bytes, 0, len);
        }
    }
}
