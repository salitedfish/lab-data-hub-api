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
    
    public static Object readDB(S7Connector connector, Integer dbNumber, String blockType,Integer startAddress, Integer length, Integer bitOffset) throws Exception {
    	if (connector == null) {
            throw new IllegalStateException("S7连接未建立或已断开");
        }
    	switch (blockType){
    		case "DBW":{//2字节
    			byte[] data = connector.read(DaveArea.DB, dbNumber, 2, startAddress);
    	    	int value = new IntegerConverter().extract(Integer.class, data, 0, 0);
    	    	return value;
    		}
    		case "DBX":{//1字节
    			byte[] data = connector.read(DaveArea.DB, dbNumber, 1, startAddress);
    	    	boolean value = new BitConverter().extract(Boolean.class, data, 0, bitOffset == null ? 0 : bitOffset);
    	    	return value;
    		}
    		case "DBD":{//4字节
    			byte[] data = connector.read(DaveArea.DB, dbNumber, 4, startAddress);
    			float value = new RealConverter().extract(Float.class, data, 0, 0);
    	    	return value;
    		}
    		case "DBB":{//length字节
    			byte[] data = connector.read(DaveArea.DB, dbNumber, length == null ? 1 : length, startAddress);
//    			String value = new StringConverter().extract(String.class, data, 0, 0);
//    			// 读取前两个字节（最大长度和实际长度）
//    	        byte[] header = connector.read(DaveArea.DB, dbNumber, 2, startAddress);
//    	        int maxLength = header[0] & 0xFF;
//    	        int actualLength = header[1] & 0xFF;
//    	        // 实际读取字符内容（跳过头部2字节）
//    	        byte[] charData = connector.read(DaveArea.DB, dbNumber, actualLength, startAddress + 2);
    	        // 西门子 String 类型的中文使用 GBK 编码
    	        try {
    	            return new String(data, "GBK").trim();
    	        } catch (UnsupportedEncodingException e) {
    	            return new String(data, StandardCharsets.ISO_8859_1).trim();
    	        }
    		}
    		default:
    	}
    	return null;
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