//由AI修改
package com.labdatahub.component.s7_tcp;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.github.s7connector.api.DaveArea;
import com.github.s7connector.api.S7Connector;
import com.github.s7connector.api.S7Serializable;
import com.github.s7connector.impl.serializer.converter.BitConverter;
import com.github.s7connector.impl.serializer.converter.IntegerConverter;
import com.github.s7connector.impl.serializer.converter.RealConverter;
import com.github.s7connector.impl.serializer.converter.StringConverter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S7DataReader {
	// 维护一个静态的转换器映射，用于处理不同的数据类型
    private static final Map<Class<?>, S7Serializable> CONVERTER_MAP = new HashMap<>();

    static {
        CONVERTER_MAP.put(Integer.class, new IntegerConverter());
        CONVERTER_MAP.put(Float.class, new RealConverter());  // Real 即 32位浮点数
        CONVERTER_MAP.put(String.class, new StringConverter());
        CONVERTER_MAP.put(Boolean.class, new BitConverter());
        // 可根据需要添加 Short、Long 等类型的转换器
    }
	// 重试配置
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    /**
     * 读取 DB 块数据
     * @param connector S7连接
     * @param dbNumber DB号
     * @param startByteOffset 起始字节偏移
     * @param numberOfBytes 读取字节数
     * @return 字节数组
     */
//    public static byte[] readDB(S7Connector connector, int dbNumber, int startByteOffset, int numberOfBytes) throws Exception {
//        if (connector == null) {
//            throw new IllegalStateException("S7连接未建立或已断开");
//        }
//        // 读取 DB 数据
//        byte[] data = connector.read(DaveArea.DB, dbNumber, numberOfBytes, startByteOffset);
//        if (data == null || data.length != numberOfBytes) {
//            throw new Exception("读取 DB" + dbNumber + " 失败，返回数据长度不匹配");
//        }
//        return data;
//    }
    public static byte[] readDB(S7Connector connector, int dbNumber, int startByteOffset, int numberOfBytes) throws Exception {
        return executeWithRetry(() -> {
            if (connector == null) {
                throw new IllegalStateException("S7连接未建立或已断开");
            }
            byte[] data = connector.read(DaveArea.DB, dbNumber, numberOfBytes, startByteOffset);
            if (data == null || data.length != numberOfBytes) {
                throw new Exception("读取 DB" + dbNumber + " 失败，返回数据长度不匹配");
            }
            return data;
        }, "readDB", dbNumber, numberOfBytes, startByteOffset);
    }
    
    public static Object readDB(S7Connector connector, Integer dbNumber, String blockType, String areaType, String dataType, Integer startAddress, Integer length, Integer bitOffset, String isSigned) throws Exception {
    	if (connector == null) {
            throw new IllegalStateException("S7连接未建立或已断开");
        }
        DaveArea area = toDaveArea(areaType);
    	switch (blockType){
    		case "DBW":{//2字节
    			byte[] data = connector.read(area, dbNumber, 2, startAddress);
    	    	int value = new IntegerConverter().extract(Integer.class, data, 0, 0);
    	    	return value;
    		}
    		case "DBX":{//1字节
    			byte[] data = connector.read(area, dbNumber, 1, startAddress);
    	    	boolean value = new BitConverter().extract(Boolean.class, data, 0, bitOffset == null ? 0 : bitOffset);
    	    	return value;
    		}
    		case "DBD":{//4字节 32位浮点数或DINT整型
    			byte[] data = connector.read(area, dbNumber, 4, startAddress);
    			if (isIntType(dataType)) {// 物模型 dataType 为 int 系列：按 DINT 32位大端整型解析（S7大端）
    				int value = ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
    				// isSigned=1（或 0/false/无符号 之外）按有符号 32 位整型返回；否则按无符号返回（值范围 0~4294967295，用 Long 承载）
    				return isSignedInteger(isSigned) ? value : (Object) Integer.toUnsignedLong(value);
    			}
    			float value = new RealConverter().extract(Float.class, data, 0, 0);
    	    	return value;
    		}
    		case "DBB":{//length字节（西门子 STRING：前2字节为 最大长度+实际长度 头，字符内容从第3字节起，GBK编码兼容中文）
    			byte[] data = connector.read(area, dbNumber, length == null ? 1 : length, startAddress);
    	        // 西门子 STRING 结构：data[0]=最大长度，data[1]=实际长度，内容从 data[2] 起；跳过头部再按实际长度取内容
    	        int contentLen = data.length >= 2 ? Math.min(data[1] & 0xFF, data.length - 2) : 0;
    	        try {
    	            return new String(data, 2, contentLen, "GBK").trim();
    	        } catch (UnsupportedEncodingException e) {
    	            return new String(data, 2, contentLen, StandardCharsets.ISO_8859_1).trim();
    	        }
    		}
    		default:
    	}
    	return null;
    }
    
    /**
     * 区类型转 DaveArea：DB→DB数据块，M→FLAGS标志位，I→INPUTS输入区，Q→OUTPUTS输出区
     *
     * <p>包级可见：写值链路 {@link S7PointWriter} 要按同一套映射定位写入区，
     * 读写用两套映射迟早会写串区。
     */
    static DaveArea toDaveArea(String areaType) {
        if (areaType == null) {
            return DaveArea.DB;
        }
        switch (areaType.trim()) {
            case "M":
                return DaveArea.FLAGS;
            case "I":
                return DaveArea.INPUTS;
            case "Q":
                return DaveArea.OUTPUTS;
            case "DB":
            default:
                return DaveArea.DB;
        }
    }

    /**
     * 物模型 isSigned 是否按有符号处理："0"/"false"/"无符号" 视为无符号，其余（含空/1）默认有符号
     */
    private static boolean isSignedInteger(String isSigned) {
        if (isSigned == null || isSigned.trim().isEmpty()) {
            return true;
        }
        String s = isSigned.trim().toLowerCase();
        return !("0".equals(s) || "false".equals(s) || "无符号".equals(s));
    }

    /**
     * 是否 int 系列数据类型（DBD 按 DINT 32位整型解析；否则按 32位浮点 Real）
     */
    private static boolean isIntType(String dataType) {
        if (dataType == null) {
            return false;
        }
        String type = dataType.trim().toLowerCase();
        return "int".equals(type) || "integer".equals(type) || "short".equals(type) || "long".equals(type) || "byte".equals(type);
    }

    public static  <T> T readDB(S7Connector connector, int dbNumber, int startByteOffset, int numberOfBytes, Class<T> targetClass) throws Exception {
        if (connector == null) {
            throw new IllegalStateException("S7连接未建立或已断开");
        }
        // 读取 DB 数据
        byte[] data = connector.read(DaveArea.DB, dbNumber, numberOfBytes, startByteOffset);
        // 步骤2：将字节数组转换为目标 Java 类型
        if (targetClass == String.class) {
            // 字符串需要特殊处理，去除末尾的空字符
            return (T) new String(data).trim();
        }
        S7Serializable converter = CONVERTER_MAP.get(targetClass);
        if (converter == null) {
            throw new UnsupportedOperationException("不支持的数据类型: " + targetClass);
        }
        return converter.extract(targetClass, data, 0, 0);
    }
    
    

    /**
     * 写入 DB 块数据
     * @param connector S7连接
     * @param dbNumber DB号
     * @param startByteOffset 起始字节偏移
     * @param data 要写入的字节数组
     * @return 是否成功
     */
//    public static boolean writeDB(S7Connector connector, int dbNumber, int startByteOffset, byte[] data) throws Exception {
//        if (connector == null) {
//            throw new IllegalStateException("S7连接未建立或已断开");
//        }
//        connector.write(DaveArea.DB, dbNumber, startByteOffset, data);
//        return true; // 无异常即成功
//    }
    
    public static boolean writeDB(S7Connector connector, int dbNumber, int startByteOffset, byte[] data) throws Exception {
        return executeWithRetry(() -> {
            if (connector == null) {
                throw new IllegalStateException("S7连接未建立或已断开");
            }
            connector.write(DaveArea.DB, dbNumber, startByteOffset, data);
            return true;
        }, "writeDB", dbNumber, startByteOffset, data.length);
    }
    
    /**
     * 向 PLC 的数据块 (DB) 中写入数据
     * @param connector 已建立的连接对象
     * @param dbNumber DB 块编号
     * @param startByteOffset 起始字节偏移量
     * @param value 要写入的值，支持 Integer, Float, String 等
     */
    public static void writeDB(S7Connector connector, int dbNumber, int startByteOffset, Object value) {
        // 根据值的类型获取对应的转换器，将 Java 对象序列化为字节数组
        S7Serializable converter = CONVERTER_MAP.get(value.getClass());
        if (converter == null) {
            throw new UnsupportedOperationException("不支持的数据类型: " + value.getClass());
        }

        // 计算数据长度并准备缓冲区
        int dataLength = getDataLength(value);
        byte[] buffer = new byte[dataLength];
        converter.insert(value, buffer, 0, 0, dataLength);

        // 执行写入操作
        connector.write(DaveArea.DB, dbNumber, startByteOffset, buffer);
    }
    
    // 辅助方法：根据数据类型获取需要写入的字节长度
    private static int getDataLength(Object value) {
        if (value instanceof Integer) {
            return 4;  // 32位整数占4字节
        } else if (value instanceof Float) {
            return 4;  // 32位浮点数占4字节
        } else if (value instanceof String) {
            // 此处简化处理，实际开发中应确保字符串长度不超过 DB 块中定义的长度
            return ((String) value).length();
        } else if (value instanceof Boolean) {
            // 
            return 1;
        }
        return 0;
    }
    
    @FunctionalInterface
    private interface Operation<T> {
        T execute() throws Exception;
    }

    private static <T> T executeWithRetry(Operation<T> op, String operationName, Object... params) throws Exception {
        int attempt = 0;
        while (true) {
            try {
                return op.execute();
            } catch (Exception e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    log.error("[S7读写] {} 失败，重试次数已用完, params: {}", operationName, params, e);
                    throw e;
                }
                log.warn("[S7读写] {} 失败（第{}次），{}ms后重试, params: {}, error: {}",
                        operationName, attempt, RETRY_DELAY_MS, params, e.getMessage());
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("[S7读写] {} 重试等待被中断", operationName);
                    throw new InterruptedException("重试中断");
                }
            }
        }
    }
}