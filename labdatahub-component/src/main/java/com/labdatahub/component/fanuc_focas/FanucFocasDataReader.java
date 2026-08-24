//由AI修改
package com.labdatahub.component.fanuc_focas;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.ShortByReference;

/**
 * 数据读取器，负责按点位地址（readType.param1.param2）从 FANUC FOCAS2 设备读取单项数据
 * FOCAS2 通过 fwlib32 库按函数调用读取，一次读一个点；读失败自动重连重试一次
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
                // 坐标：param1=轴号(1-6) param2=坐标类型(1机械 2绝对 3相对 4剩余)
                if (param1 == null || param2 == null || param2 < 1 || param2 > 4) {
                    return null;
                }
                Fwlib32.ODBDATA data = new Fwlib32.ODBDATA();
                short ret = lib.cnc_rdaxisdata(handle, param2.shortValue(), param1.shortValue(), data);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_rdaxisdata socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                switch (param2) {
                    case 1:
                        return String.valueOf(data.machine);
                    case 2:
                        return String.valueOf(data.absolute);
                    case 3:
                        return String.valueOf(data.relative);
                    default:
                        return String.valueOf(data.distance);
                }
            }
            case "spindle": {
                // 主轴：param1=1转速 2倍率 3负载 4报警（对应 fwlib type 0-3）
                if (param1 == null || param1 < 1 || param1 > 4) {
                    return null;
                }
                Fwlib32.ODBSPINDLE data = new Fwlib32.ODBSPINDLE();
                short ret = lib.cnc_rdspindle(handle, (short) (param1 - 1), data);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_rdspindle socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                switch (param1) {
                    case 1:
                        return String.valueOf(data.speed.longValue());
                    case 2:
                        return String.valueOf(data.override);
                    case 3:
                        return String.valueOf(data.load);
                    default:
                        return String.valueOf(data.alarm);
                }
            }
            case "feed": {
                // 进给：param1=1实际进给（每分钟进给）
                if (param1 == null) {
                    return null;
                }
                Fwlib32.ODBACT data = new Fwlib32.ODBACT();
                short ret = lib.cnc_rdact(handle, (short) 0, data);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_rdact socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                return String.valueOf(data.data);
            }
            case "mode": {
                // 操作模式：0=MDI 1=AUTO 2=EDIT 3=HANDLE 4=JOG 5=INC 6=RMT 7=REF 8=TAPE
                ShortByReference mode = new ShortByReference();
                short ret = lib.cnc_rdopmode(handle, mode);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_rdopmode socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                return String.valueOf(mode.getValue());
            }
            case "status": {
                // 运行状态：param1=1运行 2停止 3急停 4自动方式 5程序运行
                if (param1 == null) {
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
                        return String.valueOf(data.run);
                    case 2:
                        return String.valueOf(data.stop);
                    case 3:
                        return String.valueOf(data.emergency);
                    case 4:
                        return String.valueOf(data.automatic);
                    case 5:
                        return String.valueOf(data.prog_run);
                    default:
                        return null;
                }
            }
            case "prgnum": {
                // 程序：param1=1程序号 2顺序号
                if (param1 == null) {
                    return null;
                }
                Fwlib32.ODBPRGNUM data = new Fwlib32.ODBPRGNUM();
                short ret = lib.cnc_rdprgnum(handle, data);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_rdprgnum socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                switch (param1) {
                    case 1:
                        return String.valueOf(data.m_nProgramNo);
                    case 2:
                        return String.valueOf(data.m_nSequenceNo);
                    default:
                        return null;
                }
            }
            case "alarm": {
                // 报警：param1=1报警号 2报警文本（type=0 读全部，alm_no=报警数，alm_msg=最后一条报警文本）
                if (param1 == null) {
                    return null;
                }
                Fwlib32.ODBALM data = new Fwlib32.ODBALM();
                short ret = lib.cnc_rdalmmsg(handle, (short) 0, data);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_rdalmmsg socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                switch (param1) {
                    case 1:
                        return String.valueOf(data.alm_no);
                    case 2:
                        return new String(data.alm_msg).trim();
                    default:
                        return null;
                }
            }
            case "tcode": {
                // 刀具：param1=1当前刀具 2上一把刀具（对应 fwlib type 0-1）
                if (param1 == null || param1 < 1 || param1 > 2) {
                    return null;
                }
                Fwlib32.ODBTCODE data = new Fwlib32.ODBTCODE();
                short ret = lib.cnc_rdtcode(handle, (short) (param1 - 1), data);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_rdtcode socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                return String.valueOf(data.data);
            }
            case "macro": {
                // 宏变量：param1=宏变量号（1-999）；值 = mcr_val / 10^dec_val
                if (param1 == null || param1 < 1) {
                    return null;
                }
                Fwlib32.ODBMACRO data = new Fwlib32.ODBMACRO();
                short ret = lib.cnc_rdmacro(handle, param1.shortValue(), data);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_rdmacro socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                if (data.dec_val <= 0) {
                    return String.valueOf(data.mcr_val);
                }
                return String.valueOf(data.mcr_val / Math.pow(10, data.dec_val));
            }
            case "timer": {
                // 时间：param1=1运行 2切削 3循环 4上电（对应 fwlib type 1/2/3/0），单位分钟
                if (param1 == null) {
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
                    case 4:
                        timerType = 0;
                        break;
                    default:
                        return null;
                }
                Fwlib32.ODBTIME data = new Fwlib32.ODBTIME();
                short ret = lib.cnc_rdtimer(handle, timerType, data);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("cnc_rdtimer socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                return String.valueOf(data.value.longValue());
            }
            case "pmc": {
                // PMC 信号：param1=1=F 2=G，param2=字节地址号（如 F1 自动运行、G8 主轴）；读字节值，待真机验证
                if (param1 == null || param2 == null) {
                    return null;
                }
                // 先获取 PMC 数据范围（校验地址合法性）
                Fwlib32.ODBPMCRNG range = new Fwlib32.ODBPMCRNG();
                short rangeRet = lib.pmc_rdpmcrng(handle, range);
                if (rangeRet == Fwlib32.EW_SOCKET) {
                    throw new IOException("pmc_rdpmcrng socket错误(" + rangeRet + ")");
                }
                if (rangeRet != Fwlib32.EW_OK) {
                    return null;
                }
                if (param2 < 0 || param2 > range.pmcmaxadrs) {
                    return null;
                }
                Fwlib32.ODBPMC pmc = new Fwlib32.ODBPMC();
                pmc.c_lngth = 1;        // 读1个地址
                pmc.c_type = 0;         // 0=字节
                pmc.c_addr = param2.shortValue();
                short ret = lib.pmc_rdpmc(handle, pmc);
                if (ret == Fwlib32.EW_SOCKET) {
                    throw new IOException("pmc_rdpmc socket错误(" + ret + ")");
                }
                if (ret != Fwlib32.EW_OK) {
                    return null;
                }
                return String.valueOf(pmc.c_date[0]);
            }
            default:
                // 不支持的采集项类型
                return null;
        }
    }
}
