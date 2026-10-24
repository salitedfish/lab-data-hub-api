package com.labdatahub.component.collect;

/**
 * 采集协议族常量 + 统一采集引擎的固定参数
 *
 * <p>所有协议（含以后新增的协议）一律走统一采集引擎，不再有「旧链路/统一引擎」的开关，
 * 也不读取任何 {@code labdatahub.collect.*} 配置 —— 新增协议只要实现
 * {@link CollectionAdapter} 并注册到 {@link CollectionRegistry} 即可接入。
 */
public final class CollectionFamily {

    /** Modbus TCP（MODBUS_TCP） */
    public static final String MODBUS = "MODBUS";
    /** 西门子 S7（S71200_TCP） */
    public static final String S7 = "S7";
    /** 欧姆龙 FINS（OMRONFINS_TCP） */
    public static final String FINS = "FINS";
    /** 三菱 MC 3E（MITSUBISHI_MC3E_TCP / MITSUBISHI_MC3E_UDP，两个 netType 共用同一套读法） */
    public static final String MC3E = "MC3E";
    /** 兄弟 Brother NC（BROTHER_TCP） */
    public static final String BROTHER = "BROTHER";
    /** 发那科 FOCAS2（FANUC_TCP） */
    public static final String FANUC = "FANUC";
    /** 三菱 CNC（MITSUBISHI_CNC_TCP） */
    public static final String CNC = "CNC";
    /** 数据库采集（DATABASE_TCP） */
    public static final String DATABASE = "DATABASE";

    /** 组件任务队列容量：一条任务现在覆盖一批点位，容量不需要旧链路的 1000 */
    private static final int QUEUE_CAPACITY = 200;

    /** 合并读取允许的地址空档：0 = 只合并重叠/相邻地址，绝不补读设备未实现的地址 */
    private static final int MERGE_GAP = 0;

    private CollectionFamily() {
        throw new UnsupportedOperationException("该类为常量类，禁止实例化");
    }

    /**
     * 组件任务队列容量
     *
     * @return 队列容量
     */
    public static int queueCapacity() {
        return QUEUE_CAPACITY;
    }

    /**
     * 合并读取允许的地址空档（同一采集单元内两块地址之间最多跳过多少个地址仍合成一次读）
     *
     * <p>固定 0：只合并重叠/相邻的地址，与旧链路读到的地址范围一致。中间落在设备未实现的
     * 地址上会让整块读失败，所以不能默认放宽。
     *
     * @param family 协议族
     * @return 允许的空档大小
     */
    public static int mergeGap(String family) {
        return MERGE_GAP;
    }
}
