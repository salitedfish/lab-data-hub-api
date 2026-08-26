//由AI修改
package com.labdatahub.component.fanuc_focas;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ShortByReference;

import java.util.Arrays;
import java.util.List;

/**
 * FANUC FOCAS2 库（fwlib）JNA 接口定义
 * 函数签名与结构体布局按官方 Fwlib64.h（0iD 型号头文件）逐字核对：
 *   - 结构体全部位于官方 `#pragma pack(push, 4)` 区域内，但成员类型最大为 4 字节 long，
 *     自然对齐即等于 pack(4)，JNA 默认对齐布局与 C 端一致，无需手动 setAlignType
 *   - `long` 型字段用 NativeLong（Windows x64 的 C long=4 字节，与 JNA NativeLong 一致）
 *   - 输出/输入输出句柄指针（unsigned short *）用 short[]（JNA 映射 short*），
 *     句柄用值传递（Java short 即 16 位）
 * 注意：结构体映射为 FOCAS2 对接最关键环节，第一次真机/NCGuide 验证后需核对修正
 */
public interface Fwlib32 extends Library {

    // FOCAS2 返回码：成功
    short EW_OK = 0;
    // FOCAS2 返回码：机床忙（请求与其他功能冲突，连接本身有效，稍后重试）
    short EW_BUSY = -8;
    // FOCAS2 返回码：socket 连接错误（连接断开/超时）
    short EW_SOCKET = -16;
    // 0i-D/F 最大主轴数（MAX_SPINDLE）
    int MAX_SPINDLE = 8;

    // ==================== 连接与断开 ====================
    /**
     * 建立 FOCAS2 连接（TCP 8193）
     * @param ipaddr 机床 IP
     * @param port 端口（固定 8193）
     * @param timeout 连接超时（毫秒）
     * @param flibhndl 返回的句柄（unsigned short 指针，必须用 ShortByReference 接收）
     * @return 返回码（EW_OK=成功）
     */
    short cnc_allclibhndl3(String ipaddr, short port, NativeLong timeout, ShortByReference flibhndl);

    /**
     * 释放 FOCAS2 连接句柄
     * 注意：FOCAS2 断开函数是 cnc_freelibhndl，fwlib 中没有 cnc_close
     * @param flibhndl 句柄
     * @return 返回码
     */
    short cnc_freelibhndl(short flibhndl);

    // ==================== 机床状态 ====================
    /**
     * 读取 CNC 系统状态（ODBST，9 个 short：hdck/tmmode/aut/run/motion/mstb/emergency/alarm/edit）
     * @param flibhndl 句柄
     * @param status 返回的状态
     * @return 返回码
     */
    short cnc_statinfo(short flibhndl, ODBST status);

    // ==================== 坐标 ====================
    /**
     * 读取轴数据（官方签名 6 参数：FlibHndl, cls, type[], num, len*, ODBAXDT[]）
     * cls=1（坐标值）时 type 数组：0=绝对 1=机械 2=相对 3=剩余移动量；
     * 从第 1 根轴开始按 (*len) 读取 (*len) 根轴，返回数组 axdata[] 按 type 顺序、每 type 连续 (*len) 个，
     * 即 type[0] 的数据在 axdata[0..len-1]，故读第 axis 根轴取 axdata[axis-1]
     * @param flibhndl 句柄
     * @param cls 数据类（1=坐标值）
     * @param type 数据类型数组（如 {1}=机械坐标；最多 4 种，超过返回 EW_ATTRIB）
     * @param num type 数组元素个数（≤4）
     * @param len in: 读取的轴数；out: 实际有效轴数（小于 in 说明后面轴不存在）
     * @param axdata 返回的轴数据数组，长度 ≥ num*len
     * @return 返回码
     */
    short cnc_rdaxisdata(short flibhndl, short cls, short[] type, short num, short[] len, ODBAXDT[] axdata);

    // ==================== 主轴 ====================
    /**
     * 读取实际主轴转速（rpm，ODBACT.data）
     * @param flibhndl 句柄
     * @param data 返回的转速数据
     * @return 返回码
     */
    short cnc_acts(short flibhndl, ODBACT data);

    /**
     * 读取主轴负载（%，负载值在 ODBSPN.data[0]）
     * @param flibhndl 句柄
     * @param spNo 主轴号（1..MAX_SPINDLE，0iD=8）
     * @param data 返回的负载数据
     * @return 返回码
     */
    short cnc_rdspload(short flibhndl, short spNo, ODBSPN data);

    // ==================== 进给 ====================
    /**
     * 读取实际进给速度（F，单位随 G94/G95：mm/min 或 mm/rev，ODBACT.data）
     * @param flibhndl 句柄
     * @param data 返回的进给数据
     * @return 返回码
     */
    short cnc_actf(short flibhndl, ODBACT data);

    // ==================== 程序 ====================
    /**
     * 读取当前执行中的程序号（ODBPRO.data / mdata 主程序号）
     * @param flibhndl 句柄
     * @param programNo 返回的程序信息
     * @return 返回码
     */
    short cnc_rdprgnum(short flibhndl, ODBPRO programNo);

    /**
     * 读取当前执行段顺序号（ODBSEQNUM.data；此前误以为无此函数，实际 FOCAS2 官方支持）
     * @param flibhndl 句柄
     * @param seqNum 返回的顺序号
     * @return 返回码
     */
    short cnc_rdseqnum(short flibhndl, ODBSEQNUM seqNum);

    /**
     * 读取当前执行中的程序名（ODBEXEPRGNAME.name，最长 32 字节）
     * @param flibhndl 句柄
     * @param exeprgname 返回的程序名
     * @return 返回码
     */
    short cnc_exeprgname(short flibhndl, ODBEXEPRGNAME exeprgname);

    // ==================== 加工数（产量） ====================
    /**
     * 读取加工数（ODBCOUNT.data/dec；比 cnc_rdparam 读系统参数更标准的官方函数）
     * CntDataNo：0=总加工数（加工数1） 1=稼働程序加工数（加工数2） 2=特定加工数（加工数3）
     * @param flibhndl 句柄
     * @param cntDataNo 加工数类别（0/1/2）
     * @param count 返回的加工数
     * @return 返回码
     */
    short cnc_rdcount(short flibhndl, short cntDataNo, ODBCOUNT count);

    // ==================== 诊断 ====================
    /**
     * 读取诊断信息（ODBDIAGNO.data；主轴温度等机床特有数据在诊断号中，号因机床而异）
     * @param flibhndl 句柄
     * @param number 诊断号
     * @param diagno 返回的诊断值
     * @return 返回码
     */
    short cnc_diagnoss(short flibhndl, short number, ODBDIAGNO diagno);

    // ==================== 报警 ====================
    /**
     * 读取报警信息（官方签名 4 参数：FlibHndl, type, num*, ODBALMMSG[]）
     * type=-1 读全部；type 指定具体报警类型；num in: 请求读的最大条数，out: 实际读到的条数
     * @param flibhndl 句柄
     * @param type 报警类型（-1=全部；其它见官方文档）
     * @param num in/out 报警条数
     * @param alms 返回的报警数组，长度 ≥ num 的初始值
     * @return 返回码
     */
    short cnc_rdalmmsg(short flibhndl, short type, short[] num, ODBALMMSG[] alms);

    // ==================== 宏变量 ====================
    /**
     * 读取自定义宏变量（可读 #3901 当前刀具号、#3902 下一刀具号、#1000..#9999 等）
     * 值 = mcr_val / 10^dec_val
     * @param flibhndl 句柄
     * @param number 宏变量号
     * @param length 数据长度（固定 10，官方示例）
     * @param macro 返回的宏变量值
     * @return 返回码
     */
    short cnc_rdmacro(short flibhndl, short number, short length, ODBM macro);

    // ==================== 时间 ====================
    /**
     * 读取 CNC 计时器（type：0=上电 1=运行 2=切削 3=循环，分钟；msec 为不足一分钟的毫秒）
     * @param flibhndl 句柄
     * @param type 计时器类型
     * @param time 返回的时间数据
     * @return 返回码
     */
    short cnc_rdtimer(short flibhndl, short type, IODBTIME time);

    // ==================== PMC 信号 ====================
    /**
     * 读取 PMC 数据（官方签名 7 参数：FlibHndl, adr_type, data_type, s_number, e_number, length, IODBPMC*）
     * adr_type：0=G 1=F 2=Y 3=X；data_type：0=字节 1=字 2=长字；
     * 数据块长度 length = 8 + N（字节）/ 8+N*2（字）/ 8+N*4（长字），N 为数据个数
     * @param flibhndl 句柄
     * @param adrType PMC 地址类型（0=G 1=F 2=Y 3=X）
     * @param dataType 数据类型（0=字节 1=字 2=长字）
     * @param sNumber 起始地址
     * @param eNumber 结束地址
     * @param length 数据块长度（读 1 个字节地址 = 9）
     * @param buf 返回的 PMC 数据（IODBPMC.u 为联合体，字节数据在第 i 个长字的低字节）
     * @return 返回码
     */
    short pmc_rdpmcrng(short flibhndl, short adrType, short dataType, short sNumber, short eNumber, short length, IODBPMC buf);

    // ==================== 结构体定义（按官方 Fwlib64.h） ====================
    /**
     * CNC 系统状态（odbst，9 个 short，默认分支/FS16WD 均为 9 个字段）
     * 默认分支：hdck/tmmode/aut/run/motion/mstb/emergency/alarm/edit
     */
    class ODBST extends Structure {
        public short hdck;      // 手轮回退状态
        public short tmmode;    // T/M 方式
        public short aut;       // 自动方式（0=MDI 1=MEM 3=EDIT 4=HANDLE 5=JOG 8=INC 9=REF 10=RMT）
        public short run;       // 运行状态（0=复位 1=停止 2=保持 3=启动 4=等待 MSTR）
        public short motion;    // 轴/暂停状态
        public short mstb;      // M/S/T/B 状态
        public short emergency; // 急停状态
        public short alarm;     // 报警状态
        public short edit;      // 编辑状态

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("hdck", "tmmode", "aut", "run", "motion", "mstb", "emergency", "alarm", "edit");
        }
    }

    /**
     * 轴数据（odbaxdt）：轴名[4] + long 位置值 + 4 个 short
     * 值 = data / 10^dec
     */
    class ODBAXDT extends Structure {
        public byte[] name = new byte[4]; // 轴名（如 "X"、"Y"）
        public NativeLong data;           // 位置数据（值 = data / 10^dec）
        public short dec;                 // 小数位数
        public short unit;                // 数据单位
        public short flag;                // 标志
        public short reserve;             // 保留

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("name", "data", "dec", "unit", "flag", "reserve");
        }
    }

    /**
     * 实际进给/实际转速数据（odbact）：2 个 short + 1 个 long
     */
    class ODBACT extends Structure {
        public short[] dummy = new short[2]; // 保留
        public NativeLong data;              // 实际进给（cnc_actf）/ 实际主轴转速（cnc_acts）

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dummy", "data");
        }
    }

    /**
     * 串行主轴数据（odbspn）：主轴号 + 未用 + data[MAX_SPINDLE]
     * 负载信息在 data[0]
     */
    class ODBSPN extends Structure {
        public short datano;                  // 主轴号
        public short type;                    // 未使用
        public short[] data = new short[MAX_SPINDLE]; // 主轴数据（负载在 data[0]）

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("datano", "type", "data");
        }
    }

    /**
     * 执行中程序信息（odbpro）：2 个 short 保留 + 程序号 + 主程序号
     */
    class ODBPRO extends Structure {
        public short[] dummy = new short[2]; // 保留
        public short data;                   // 执行中程序号
        public short mdata;                  // 主程序号

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dummy", "data", "mdata");
        }
    }

    /**
     * 当前执行段顺序号（odbseqnum）：1 个 long
     */
    class ODBSEQNUM extends Structure {
        public NativeLong data; // 顺序号（N 号）

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("data");
        }
    }

    /**
     * 执行中程序名（odbexeprgname）：char[33]（MAX_EXEPRGNAME_LEN=33）
     */
    class ODBEXEPRGNAME extends Structure {
        public byte[] name = new byte[33]; // 执行中程序名（ASCII/编码随程序文件名）

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("name");
        }
    }

    /**
     * 加工数（odbcount）：long 加工数 + short 小数位数
     * 值 = data / 10^dec
     */
    class ODBCOUNT extends Structure {
        public NativeLong data; // 加工数
        public short dec;       // 小数点以下位数

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("data", "dec");
        }
    }

    /**
     * 诊断信息（odbdiagno）：1 个 long
     */
    class ODBDIAGNO extends Structure {
        public NativeLong data; // 诊断值

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("data");
        }
    }

    /**
     * 报警信息（odbalmmsg）：报警号 + 类型/轴/保留/文本长度 + 报警文本[32]
     */
    class ODBALMMSG extends Structure {
        public NativeLong alm_no;               // 报警号
        public short type;                      // 报警类型
        public short axis;                      // 报警轴
        public short dummy;                     // 保留
        public short msg_len;                   // 报警文本长度
        public byte[] alm_msg = new byte[32];   // 报警文本（GBK 编码）

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("alm_no", "type", "axis", "dummy", "msg_len", "alm_msg");
        }
    }

    /**
     * 宏变量数据（odbm）：变量号 + 保留 + 长整数值 + 小数位数
     * 值 = mcr_val / 10^dec_val
     */
    class ODBM extends Structure {
        public short datano;         // 变量号
        public short dummy;          // 保留
        public NativeLong mcr_val;   // 宏变量值（=M，9 位整数）
        public short dec_val;        // 小数位数（=E，值 = mcr_val / 10^dec_val）

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("datano", "dummy", "mcr_val", "dec_val");
        }
    }

    /**
     * 计时器数据（iodbtime）：分钟 + 余数毫秒
     */
    class IODBTIME extends Structure {
        public NativeLong minute; // 时间（分钟）
        public NativeLong msec;   // 余数（毫秒）

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("minute", "msec");
        }
    }

    /**
     * PMC 数据（iodbpmc）：4 个 short 头 + 联合体（char[5]/short[5]/long[5]，取最大 20 字节）
     * 联合体用 NativeLong[5] 占位（对齐 4，覆盖最大成员 long[5]），
     * 字节型数据在第 i 个长字的低字节（little-endian），用 byteAt(index) 取
     */
    class IODBPMC extends Structure {
        public short type_a;                 // PMC 地址类型（0=G 1=F 2=Y 3=X）
        public short type_d;                 // PMC 数据类型（0=字节 1=字 2=长字）
        public short datano_s;               // 起始地址
        public short datano_e;               // 结束地址
        public NativeLong[] u = new NativeLong[5]; // 联合体数据区（20 字节）

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("type_a", "type_d", "datano_s", "datano_e", "u");
        }

        /**
         * 取第 index 个字节型数据（联合体 cdata[index]，即对应长字的低字节）
         * @param index 数据序号（0 起）
         * @return 无符号字节值（0-255）
         */
        public short byteAt(int index) {
            if (index < 0 || index >= u.length) {
                return 0;
            }
            return (short) (u[index].intValue() & 0xFF);
        }
    }
}
