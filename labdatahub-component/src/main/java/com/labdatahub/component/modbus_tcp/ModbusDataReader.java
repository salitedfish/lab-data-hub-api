//由AI修改
package com.labdatahub.component.modbus_tcp;

import com.alibaba.fastjson2.JSONArray;

import lombok.extern.slf4j.Slf4j;
import net.wimpi.modbus.ModbusIOException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.BitVector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据读取器，负责从Modbus设备读取/写入寄存器数据
 */
@Slf4j
public class ModbusDataReader {
	
	public static List<Integer> readHoldingRegisters(String componentId, Integer slaveId, int startAddr, int count) throws Exception {
	    TCPMasterConnection connection = ModbusConnectionManager.getValidConnection(componentId);
	    if (connection == null) {
	        throw new Exception("componentId=" + componentId + " 无法获取有效连接");
	    }
	    // 原有读取逻辑，但使用传入的 connection
	    return readHoldingRegisters(componentId,connection, slaveId, startAddr, count, 1);
	}

	/**
	 * 按功能码读取：01线圈 / 02离散输入 / 03保持寄存器 / 04输入寄存器（读补全，功能码为空默认 03）
	 *
	 * @param componentId  组件ID
	 * @param slaveId      从站ID
	 * @param functionCode 功能码 1/2/3/4（null 按 3）
	 * @param startAddr    起始地址
	 * @param count        读取数量（03/04 ≤125，01/02 ≤2000）
	 * @return 读取到的整数列表（线圈/离散输入转 0/1）
	 */
	public static List<Integer> readByFunction(String componentId, Integer slaveId, Integer functionCode, int startAddr, int count) throws Exception {
		if (functionCode == null) {
			functionCode = 3;
		}
		if (functionCode == 3) {
			if (count > 125) {
				throw new Exception("保持寄存器读取数量超过协议上限125，当前：" + count);
			}
			return readHoldingRegisters(componentId, slaveId, startAddr, count);
		}
		TCPMasterConnection connection = ModbusConnectionManager.getValidConnection(componentId);
		if (connection == null) {
			throw new Exception("componentId=" + componentId + " 无法获取有效连接");
		}
		return readByFunctionWithConn(componentId, connection, slaveId, functionCode, startAddr, count, 1);
	}

	/**
	 * 带连接的按功能码读取（含连接异常自动重建重试一次，镜像 readHoldingRegisters 自愈）
	 */
	private static List<Integer> readByFunctionWithConn(String componentId, TCPMasterConnection connection, Integer slaveId, Integer functionCode, int startAddr, int count, int retryLeft) throws Exception {
		try {
			ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
			List<Integer> result = new ArrayList<>();
			switch (functionCode) {
				case 1: { // 读线圈
					if (count > 2000) {
						throw new Exception("线圈读取数量超过协议上限2000，当前：" + count);
					}
					ReadCoilsRequest request = new ReadCoilsRequest();
					request.setUnitID(slaveId);
					request.setReference(startAddr);
					request.setBitCount(count);
					transaction.setRequest(request);
					transaction.execute();
					ReadCoilsResponse response = (ReadCoilsResponse) transaction.getResponse();
					BitVector coils = response.getCoils();
					for (int i = 0; i < count; i++) {
						result.add(coils.getBit(i) ? 1 : 0);
					}
					return result;
				}
				case 2: { // 读离散输入
					if (count > 2000) {
						throw new Exception("离散输入读取数量超过协议上限2000，当前：" + count);
					}
					ReadInputDiscretesRequest request = new ReadInputDiscretesRequest();
					request.setUnitID(slaveId);
					request.setReference(startAddr);
					request.setBitCount(count);
					transaction.setRequest(request);
					transaction.execute();
					ReadInputDiscretesResponse response = (ReadInputDiscretesResponse) transaction.getResponse();
					BitVector bits = response.getDiscretes();
					for (int i = 0; i < count; i++) {
						result.add(bits.getBit(i) ? 1 : 0);
					}
					return result;
				}
				case 4: { // 读输入寄存器
					if (count > 125) {
						throw new Exception("输入寄存器读取数量超过协议上限125，当前：" + count);
					}
					ReadInputRegistersRequest request = new ReadInputRegistersRequest();
					request.setUnitID(slaveId);
					request.setReference(startAddr);
					request.setWordCount(count);
					transaction.setRequest(request);
					transaction.execute();
					ReadInputRegistersResponse response = (ReadInputRegistersResponse) transaction.getResponse();
					InputRegister[] registers = response.getRegisters();
					for (InputRegister reg : registers) {
						result.add(reg.getValue());
					}
					return result;
				}
				default:
					throw new Exception("不支持的功能码：" + functionCode + "，仅支持 01线圈/02离散输入/03保持寄存器/04输入寄存器");
			}
		} catch (Exception e) {
			//重试上限1次，防止"连接异常→重建→再异常→再重建"无限递归耗尽栈
			if ((e instanceof ModbusIOException || e instanceof IOException) && retryLeft > 0) {
				log.warn("[Modbus读取] 连接异常，自动重建：componentId={}", componentId);
				ModbusConnectionManager.connections.remove(componentId); // 清理旧连接
				TCPMasterConnection newConn = ModbusConnectionManager.getValidConnection(componentId);
				if (newConn != null) {
					return readByFunctionWithConn(componentId, newConn, slaveId, functionCode, startAddr, count, retryLeft - 1);
				}
			}
			throw e;
		}
	}
	
    /**
     * 读取保持寄存器
     */
    public static List<Integer> readHoldingRegisters(String componentId,TCPMasterConnection connection, Integer slaveId, int startAddr, int count, int retryLeft) throws Exception {
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
//            if (e instanceof ModbusIOException || e instanceof IOException) {
//                //log.warn("读取失败，尝试重新获取有效连接并重试");
//                TCPMasterConnection newConn = ModbusConnectionManager.renewConnection(componentId);
//                if (newConn != null) {
//                    return readHoldingRegisters(newConn, slaveId, startAddr, count);
//                }
//            }
//            e.printStackTrace();
//            throw e;
        	//重试上限1次，防止"连接异常→重建→再异常→再重建"无限递归耗尽栈
        	if ((e instanceof ModbusIOException || e instanceof IOException) && retryLeft > 0) {
                log.warn("[Modbus读取] 连接异常，自动重建：componentId={}", componentId);
                ModbusConnectionManager.connections.remove(componentId); // 清理旧连接
                TCPMasterConnection newConn = ModbusConnectionManager.getValidConnection(componentId);
                if (newConn != null) {
                    return readHoldingRegisters(componentId, newConn, slaveId, startAddr, count, retryLeft - 1);
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