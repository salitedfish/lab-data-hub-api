package com.labdatahub.component.modbus_tcp;

import com.alibaba.fastjson2.JSONArray;

import net.wimpi.modbus.ModbusIOException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据读取器，负责从Modbus设备读取/写入寄存器数据
 */
public class ModbusDataReader {
	
	public static List<Integer> readHoldingRegisters(String componentId, Integer slaveId, int startAddr, int count) throws Exception {
	    TCPMasterConnection connection = ModbusConnectionManager.getValidConnection(componentId);
	    // 原有读取逻辑，但使用传入的 connection
	    return readHoldingRegisters(componentId,connection, slaveId, startAddr, count);
	}
	
    /**
     * 读取保持寄存器
     */
    public static List<Integer> readHoldingRegisters(String componentId,TCPMasterConnection connection, Integer slaveId, int startAddr, int count) throws Exception {
        ModbusTCPTransaction transaction = null;

        try {
            // 创建请求
            ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest();
            request.setUnitID(slaveId);
            request.setReference(startAddr);
            request.setWordCount(count);

            // 创建并执行事务
            transaction = new ModbusTCPTransaction(connection);
            transaction.setRequest(request);
            transaction.execute();

            // 处理响应
            ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
            Register[] registers = response.getRegisters();

            List<Integer> result = new ArrayList<>();
            for (Register reg : registers) {
                result.add(reg.getValue());
            }
            return result;

        } catch (Exception e) {
//        	// 如果是连接问题，尝试重新获取有效连接并重试一次
            if (e instanceof ModbusIOException || e instanceof IOException) {
                //log.warn("读取失败，尝试重新获取有效连接并重试");
                TCPMasterConnection newConn = ModbusConnectionManager.renewConnection(componentId);
                if (newConn != null) {
                    return readHoldingRegisters(newConn, slaveId, startAddr, count);
                }
            }
            e.printStackTrace();
            throw e;
        }
    }
    
    public static List<Integer> readHoldingRegisters(TCPMasterConnection connection, Integer slaveId, int startAddr, int count) throws Exception {
        ModbusTCPTransaction transaction = null;

        try {
            // 创建请求
            ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest();
            request.setUnitID(slaveId);
            request.setReference(startAddr);
            request.setWordCount(count);

            // 创建并执行事务
            transaction = new ModbusTCPTransaction(connection);
            transaction.setRequest(request);
            transaction.execute();

            // 处理响应
            ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
            Register[] registers = response.getRegisters();

            List<Integer> result = new ArrayList<>();
            for (Register reg : registers) {
                result.add(reg.getValue());
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 写入多个保持寄存器
     */
    public static boolean writeMultipleHoldingRegisters(String componentId, Integer slaveId, String jsonArray){
        TCPMasterConnection connection = ModbusConnectionManager.connections.get(componentId);
        if(connection==null||!connection.isConnected()){
            return false;
        }
        try {
            List<RangeParserUtil.RangeItem> itemList = JSONArray.parseArray(jsonArray,RangeParserUtil.RangeItem.class);
            itemList.forEach(item->{
                writeMultipleHoldingRegisters(connection,slaveId,item.getStart(),item.getRegisterList());
            });
            return true;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 写入多个保持寄存器
     *
     * @param connection Modbus TCP连接（已建立）
     * @param slaveId    从站ID（Slave ID）
     * @param startAddr  寄存器起始地址
     * @param values     要写入的16位整数列表（数量需≤123，Modbus协议限制）
     * @return 是否写入成功
     */
    public static boolean writeMultipleHoldingRegisters(TCPMasterConnection connection, Integer slaveId, int startAddr, List<Integer> values) {
        // 校验参数：Modbus协议规定单次最多写入123个寄存器
        if (values == null || values.isEmpty() || values.size() > 123) {
            throw new IllegalArgumentException("写入寄存器数量需在1-123之间");
        }

        ModbusTCPTransaction transaction = null;

        try {
            // 1. 将整数列表转换为Register数组
            Register[] registers = new Register[values.size()];
            for (int i = 0; i < values.size(); i++) {
                registers[i] = new SimpleRegister(values.get(i));
            }

            // 2. 创建写入多个寄存器的请求
            WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest();
            request.setUnitID(slaveId);        // 设置从站ID
            request.setReference(startAddr);   // 设置起始地址
            request.setRegisters(registers);   // 设置要写入的寄存器数组

            // 3. 创建并配置事务
            transaction = new ModbusTCPTransaction(connection);
            transaction.setRequest(request);
            transaction.setRetries(3); // 设置重试次数（可选）
            transaction.execute();     // 执行写入

            // 4. 验证响应
            WriteMultipleRegistersResponse response = (WriteMultipleRegistersResponse) transaction.getResponse();
            return response != null
                    && response.getReference() == startAddr
                    && response.getWordCount() == values.size();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw e; // 参数异常直接抛出
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}