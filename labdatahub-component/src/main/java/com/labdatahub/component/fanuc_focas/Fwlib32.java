//由AI修改
package com.labdatahub.component.fanuc_focas;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ShortByReference;

import java.util.Arrays;
import java.util.List;

/**
 * FANUC FOCAS2 库（fwlib32）JNA 接口定义
 * 结构体布局对齐 FANUC fwlib.h（ODBST/ODBDATA 等），
 * long 型字段用 NativeLong（随 OS 位数匹配：Windows long=4字节，Linux long=8字节）
 * 注意：结构体映射为 FOCAS2 对接最关键环节，第一次真机/NCGuide 验证后需核对修正
 */
public interface Fwlib32 extends Library {

	// FOCAS2 返回码：成功
	short EW_OK = 0;
	// FOCAS2 返回码：socket 连接错误（连接断开/超时）
	short EW_SOCKET = -16;

	// ==================== 连接与关闭 ====================
	/**
	 * 建立 FOCAS2 连接（TCP 8193）
	 * @param ipaddr 机床 IP
	 * @param port 端口（固定 8193）
	 * @param timeout 连接超时（毫秒）
	 * @param flibhndl 返回的句柄（unsigned short 指针，必须用 ShortByReference 接收，直接传 short 会空指针崩溃）
	 * @return 返回码（EW_OK=成功）
	 */
	short cnc_allclibhndl3(String ipaddr, short port, NativeLong timeout, ShortByReference flibhndl);

	/**
	 * 关闭 FOCAS2 连接
	 * @param flibhndl 句柄
	 * @return 返回码
	 */
	short cnc_close(short flibhndl);

	// ==================== 操作模式与状态 ====================
	/**
	 * 读取操作模式（0=MDI 1=AUTO 2=EDIT 3=HANDLE 4=JOG 5=INC 6=RMT 7=REF 8=TAPE）
	 * @param flibhndl 句柄
	 * @param mode 返回的操作模式
	 * @return 返回码
	 */
	short cnc_rdopmode(short flibhndl, ShortByReference mode);

	/**
	 * 读取系统状态（ODBST 结构体，含运行/急停/停止等位标志）
	 * @param flibhndl 句柄
	 * @param status 返回的状态
	 * @return 返回码
	 */
	short cnc_statinfo(short flibhndl, ODBST status);

	// ==================== 坐标 ====================
	/**
	 * 读取坐标（ODBDATA 一次返回机械/绝对/相对/剩余 4 个坐标）
	 * @param flibhndl 句柄
	 * @param type 坐标类型：1=机械 2=绝对 3=相对 4=剩余
	 * @param axis 轴号（1起：1=X 2=Y 3=Z 4=A 5=B 6=C）
	 * @param data 返回的坐标数据
	 * @return 返回码
	 */
	short cnc_rdaxisdata(short flibhndl, short type, short axis, ODBDATA data);

	// ==================== 主轴 ====================
	/**
	 * 读取主轴数据
	 * @param flibhndl 句柄
	 * @param type 数据类型：0=转速 1=倍率 2=负载 3=报警
	 * @param data 返回的主轴数据
	 * @return 返回码
	 */
	short cnc_rdspindle(short flibhndl, short type, ODBSPINDLE data);

	// ==================== 进给 ====================
	/**
	 * 读取实际进给速度
	 * @param flibhndl 句柄
	 * @param type 数据类型（0=每分钟进给 1=每转进给）
	 * @param data 返回的进给数据
	 * @return 返回码
	 */
	short cnc_rdact(short flibhndl, short type, ODBACT data);

	// ==================== 程序 ====================
	/**
	 * 读取当前程序号/顺序号
	 * @param flibhndl 句柄
	 * @param programNo 返回的程序信息
	 * @return 返回码
	 */
	short cnc_rdprgnum(short flibhndl, ODBPRGNUM programNo);

	// ==================== 报警 ====================
	/**
	 * 读取报警信息
	 * @param flibhndl 句柄
	 * @param type 读取类型（0=读全部）
	 * @param alarm 返回的报警信息
	 * @return 返回码
	 */
	short cnc_rdalmmsg(short flibhndl, short type, ODBALM alarm);

	// ==================== 刀具 ====================
	/**
	 * 读取刀具号（T 代码）
	 * @param flibhndl 句柄
	 * @param type 0=当前刀具 1=上一把刀具
	 * @param tcode 返回的刀具信息
	 * @return 返回码
	 */
	short cnc_rdtcode(short flibhndl, short type, ODBTCODE tcode);

	// ==================== 宏变量 ====================
	/**
	 * 读取宏变量
	 * @param flibhndl 句柄
	 * @param num 宏变量号（如 500、501...）
	 * @param macro 返回的宏变量值
	 * @return 返回码
	 */
	short cnc_rdmacro(short flibhndl, short num, ODBMACRO macro);

	// ==================== 时间 ====================
	/**
	 * 读取时间数据
	 * @param flibhndl 句柄
	 * @param type 0=上电时间 1=运行时间 2=切削时间 3=循环时间（单位：分钟）
	 * @param time 返回的时间数据
	 * @return 返回码
	 */
	short cnc_rdtimer(short flibhndl, short type, ODBTIME time);

	// ==================== PMC 信号（待真机验证） ====================
	/**
	 * 读取 PMC 数据范围
	 * @param flibhndl 句柄
	 * @param range 返回的 PMC 范围
	 * @return 返回码
	 */
	short pmc_rdpmcrng(short flibhndl, ODBPMCRNG range);

	/**
	 * 读取 PMC 数据（F/G/X/Y 信号地址）
	 * @param flibhndl 句柄
	 * @param pmc 待读取的 PMC 数据（c_lngth/c_type/c_addr 为输入，c_date 为输出）
	 * @return 返回码
	 */
	short pmc_rdpmc(short flibhndl, ODBPMC pmc);

	// ==================== 结构体定义 ====================
	/**
	 * 系统状态（odbst），34 个 short
	 * 待真机/NCGuide 验证字段偏移
	 */
	class ODBST extends Structure {
		public short dummy;
		public short run;           // 运行中
		public short emergency;     // 急停
		public short stop;          // 停止
		public short reset;         // 复位
		public short automatic;     // 自动方式
		public short memory;        // 内存方式
		public short mdi;           // MDI 方式
		public short tape;          // 纸带方式
		public short editor;        // 编辑方式
		public short manual_abs;    // 手动绝对
		public short manual_mix;    // 手动混合
		public short manual_int;    // 手动插入
		public short manual_seq;    // 手动顺序
		public short pswd_locked;   // 密码锁定
		public short block_hld;     // 程序段暂停
		public short cuff_block;    // 程序段忽略
		public short prog_run;      // 程序运行
		public short unused1;
		public short unused2;
		public short unused3;
		public short unused4;
		public short unused5;
		public short unused6;
		public short unused7;
		public short unused8;
		public short unused9;
		public short unused10;
		public short unused11;
		public short unused12;
		public short unused13;
		public short unused14;
		public short unused15;
		public short unused16;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("dummy", "run", "emergency", "stop", "reset", "automatic", "memory", "mdi",
					"tape", "editor", "manual_abs", "manual_mix", "manual_int", "manual_seq", "pswd_locked",
					"block_hld", "cuff_block", "prog_run",
					"unused1", "unused2", "unused3", "unused4", "unused5", "unused6", "unused7", "unused8",
					"unused9", "unused10", "unused11", "unused12", "unused13", "unused14", "unused15", "unused16");
		}
	}

	/**
	 * 坐标数据（odbdata），2 short + 4 double，一次读出机械/绝对/相对/剩余 4 坐标
	 * 待真机/NCGuide 验证字段偏移
	 */
	class ODBDATA extends Structure {
		public short dummy;
		public short type;          // 坐标类型
		public double machine;      // 机械坐标
		public double absolute;     // 绝对坐标
		public double relative;     // 相对坐标
		public double distance;     // 剩余移动量

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("dummy", "type", "machine", "absolute", "relative", "distance");
		}
	}

	/**
	 * 主轴数据（odbspdl），3 short + 1 long(NativeLong) + 5 short
	 * 待真机/NCGuide 验证字段偏移
	 */
	class ODBSPINDLE extends Structure {
		public short dummy;
		public short type;          // 数据类型
		public short function;      // 主轴功能
		public NativeLong speed;    // 主轴转速（rpm）
		public short load;          // 负载（%）
		public short override;      // 倍率（%）
		public short alarm;         // 主轴报警
		public short prog;          // 程序转速
		public short tmp;           // 内部使用

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("dummy", "type", "function", "speed", "load", "override", "alarm", "prog", "tmp");
		}
	}

	/**
	 * 进给数据（odbact），3 short
	 * 待真机/NCGuide 验证字段偏移
	 */
	class ODBACT extends Structure {
		public short dummy;
		public short type;          // 数据类型
		public short data;          // 进给速度值

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("dummy", "type", "data");
		}
	}

	/**
	 * 程序信息（odbprgnum），2 short
	 * 待真机/NCGuide 验证字段偏移
	 */
	class ODBPRGNUM extends Structure {
		public short m_nProgramNo;   // 程序号
		public short m_nSequenceNo;  // 顺序号

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("m_nProgramNo", "m_nSequenceNo");
		}
	}

	/**
	 * 报警信息（odbalm），2 short + 1 long + 32 char
	 * 待真机/NCGuide 验证字段偏移
	 */
	class ODBALM extends Structure {
		public short alm_no;        // 报警号
		public short alm_type;      // 报警类型
		public NativeLong msg_no;   // 报警文本号
		public byte[] alm_msg = new byte[32]; // 报警文本

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("alm_no", "alm_type", "msg_no", "alm_msg");
		}
	}

	/**
	 * 刀具信息（odbtcode），3 short
	 * 待真机/NCGuide 验证字段偏移
	 */
	class ODBTCODE extends Structure {
		public short dummy;
		public short type;          // 数据类型
		public short data;          // 刀具号

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("dummy", "type", "data");
		}
	}

	/**
	 * 宏变量数据（odbmacro），2 short + 1 long
	 * 值 = mcr_val / 10^dec_val
	 * 待真机/NCGuide 验证字段偏移
	 */
	class ODBMACRO extends Structure {
		public short mcr_val;       // 宏变量值（整数部分）
		public short dec_val;       // 小数位数（值 = mcr_val / 10^dec_val）
		public NativeLong mcr_type; // 宏变量类型（0=数值 1=字符串）

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("mcr_val", "dec_val", "mcr_type");
		}
	}

	/**
	 * 时间数据（odbtime），2 short + 1 long
	 * 待真机/NCGuide 验证字段偏移
	 */
	class ODBTIME extends Structure {
		public short dummy;
		public short type;          // 时间类型
		public NativeLong value;    // 时间值（分钟）

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("dummy", "type", "value");
		}
	}

	/**
	 * PMC 数据范围（odbpmcrng），7 short
	 * 待真机/NCGuide 验证字段偏移
	 */
	class ODBPMCRNG extends Structure {
		public short pmctype;       // 内存类型（1=F 2=G 3=X 4=Y）
		public short mtn_type;      // 系统类型
		public short psize;         // 内存尺寸
		public short pscreen_type;  // 屏幕类型
		public short pmcmaxadrs;    // 最大地址
		public short pmcsignal;     // 信号起始地址
		public short pmcoffset;     // 偏移

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("pmctype", "mtn_type", "psize", "pscreen_type", "pmcmaxadrs", "pmcsignal", "pmcoffset");
		}
	}

	/**
	 * PMC 数据（odbpmc），200+ short
	 * 待真机/NCGuide 验证字段偏移
	 */
	class ODBPMC extends Structure {
		public short c_lngth;            // 读取/写入长度
		public short[] c_date = new short[200]; // 数据区
		public short c_type;             // 数据类型（0=字节 1=字 2=双字）
		public short c_addr;             // 数据地址
		public short c_no;               // 连续读取的块数
		public short c_neck;             // 系统内部使用
		public short c_width;            // 系统内部使用
		public short c_signal;           // 系统内部使用
		public short c_mode;             // 系统内部使用
		public short c_lngth_max;        // 系统内部使用

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("c_lngth", "c_date", "c_type", "c_addr", "c_no",
					"c_neck", "c_width", "c_signal", "c_mode", "c_lngth_max");
		}
	}
}
