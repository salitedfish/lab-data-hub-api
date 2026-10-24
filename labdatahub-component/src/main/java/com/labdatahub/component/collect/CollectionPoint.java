package com.labdatahub.component.collect;

/**
 * 统一采集引擎里的一个点位（协议无关）
 *
 * <p>{@link #source} 保存协议自己的读取配置对象（ModbusReadConfig / S7ReadConfig ...），
 * 适配器按自己的协议读取它；引擎只关心拆任务、调度、失败处理这些通用部分。
 */
public class CollectionPoint {

    /** 所属网络组件ID */
    private final String componentId;

    /** 设备SN */
    private final String deviceSn;

    /** 点位编码 */
    private final String code;

    /** 采集间隔（秒） */
    private final int intervalSeconds;

    /** 物模型数据类型 */
    private final String dataType;

    /** 字节序 */
    private final String byteOrder;

    /** 是否有符号 */
    private final String isSigned;

    /** 缩放系数 */
    private final Double scale;

    /** 偏移量 */
    private final Double offset;

    /** 协议原始读取配置对象 */
    private final Object source;

    public CollectionPoint(String componentId, String deviceSn, String code, int intervalSeconds,
                           String dataType, String byteOrder, String isSigned, Double scale, Double offset,
                           Object source) {
        this.componentId = componentId;
        this.deviceSn = deviceSn;
        this.code = code;
        this.intervalSeconds = intervalSeconds;
        this.dataType = dataType;
        this.byteOrder = byteOrder;
        this.isSigned = isSigned;
        this.scale = scale;
        this.offset = offset;
        this.source = source;
    }

    /**
     * 点位唯一键（同一组件内）：设备SN#点位编码
     *
     * @return 唯一键
     */
    public String key() {
        return deviceSn + "#" + code;
    }

    public String getComponentId() {
        return componentId;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public String getCode() {
        return code;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public String getDataType() {
        return dataType;
    }

    public String getByteOrder() {
        return byteOrder;
    }

    public String getIsSigned() {
        return isSigned;
    }

    public Double getScale() {
        return scale;
    }

    public Double getOffset() {
        return offset;
    }

    public Object getSource() {
        return source;
    }
}
