package com.labdatahub.component.fins_tcp;

import com.alibaba.fastjson2.JSONArray;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据读取器，负责从FINS设备读取/写入内存区数据
 */
public class FinsDataReader {
    // 服务ID，自增，用于区分请求
    private static byte sid = 0;
    
    /**
     * 从输入流中读取指定长度的数据，直到读满
     */
    private static void readFully(InputStream in, byte[] buffer) throws Exception {
        int totalRead = 0;
        int len;
        while (totalRead < buffer.length) {
            len = in.read(buffer, totalRead, buffer.length - totalRead);
            if (len == -1) {
                throw new Exception("流已结束，无法读取足够的数据，预期" + buffer.length + "字节，已读取" + totalRead + "字节");
            }
            totalRead += len;
        }
    }
    
    /**
     * 读取FINS内存区数据
     * @param componentId 组件ID，用于获取连接和配置
     * @param areaCode 存储区代码
     * @param startAddr 起始地址
     * @param count 读取数量
     * @return 读取到的整数列表
     * @throws Exception 通信异常
     */
    public static List<Integer> readMemoryArea(String componentId, int areaCode, int startAddr, int count) throws Exception {
        Socket socket = FinsConnectionManager.connections.get(componentId);
        FinsTcpConfig config = FinsConnectionManager.configMap.get(componentId);
        System.out.println("ClientNodeAddress："+config.getClientNodeAddress());
        System.out.println("plcNodeAddress："+config.getPlcNodeAddress());
     // ===================== 关键修复：连接状态检查 =====================
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            throw new Exception("连接已断开，请重新连接");
        }
        if (socket.isInputShutdown() || socket.isOutputShutdown()) {
            throw new Exception("Socket通道已关闭，无法通信");
        }
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        
        // 自增SID
        byte currentSid = sid++;
        
        // 1. 构建FINS帧
        int finsFrameLen = 10 + 2 + 6; // 头10 + 命令2 + 参数6
        byte[] finsFrame = new byte[finsFrameLen];
        finsFrame[0] = (byte) 0x80; // ICF
        finsFrame[1] = (byte) 0x00; // RSV
        finsFrame[2] = (byte) 0x02; // GCT
        finsFrame[3] = (byte) 0x00; // DNA 本地网络
        finsFrame[4] = config.getPlcNodeAddress().byteValue(); // DA1 PLC节点
        finsFrame[5] = (byte) 0x00; // DA2 CPU单元
        finsFrame[6] = (byte) 0x00; // SNA 源网络
        finsFrame[7] = config.getClientNodeAddress().byteValue(); // SA1 客户端节点
        finsFrame[8] = (byte) 0x00; // SA2 源单元
        finsFrame[9] = currentSid; // SID
        
        finsFrame[10] = (byte) 0x01; // MR 内存区访问
        finsFrame[11] = (byte) 0x01; // SR 读内存区
        
        finsFrame[12] = (byte) areaCode; // 存储区代码
        // 起始地址，3字节大端
        finsFrame[13] = (byte) ((startAddr >> 16) & 0xFF);
        finsFrame[14] = (byte) ((startAddr >> 8) & 0xFF);
        finsFrame[15] = (byte) (startAddr & 0xFF);
        // 数量，2字节大端
        finsFrame[16] = (byte) ((count >> 8) & 0xFF);
        finsFrame[17] = (byte) (count & 0xFF);
        
        // 2. 构建FINS/TCP头
        byte[] header = new byte[8];
        header[0] = 'F';
        header[1] = 'I';
        header[2] = 'N';
        header[3] = 'S';
        // 长度，4字节，大端，是command(4) + error(4) + finsFrame的长度
        int totalLen = 4 + 4 + finsFrameLen;
        header[4] = (byte) ((totalLen >> 24) & 0xFF);
        header[5] = (byte) ((totalLen >> 16) & 0xFF);
        header[6] = (byte) ((totalLen >> 8) & 0xFF);
        header[7] = (byte) (totalLen & 0xFF);
        
        // 3. 构建命令和错误码
        byte[] cmdError = new byte[8];
        // command=0x00000002，FinsFrame Send
        cmdError[0] = 0x00;
        cmdError[1] = 0x00;
        cmdError[2] = 0x00;
        cmdError[3] = 0x02;
        // error=0
        cmdError[4] = 0x00;
        cmdError[5] = 0x00;
        cmdError[6] = 0x00;
        cmdError[7] = 0x00;
        
        // 4. 发送请求
        out.write(header);
        out.write(cmdError);
        out.write(finsFrame);
        out.flush();
        
     // ===================== 关键修复：等待PLC响应 =====================
        Thread.sleep(10);
        
        // 5. 读取响应
        // 先读FINS/TCP头（8字节：FINS+Length）
        byte[] respHeader = new byte[8];
        readFully(in, respHeader);
        
        // 验证FINS头
        if (respHeader[0] != 'F' || respHeader[1] != 'I' || respHeader[2] != 'N' || respHeader[3] != 'S') {
            throw new FinsResponseException("无效的FINS/TCP响应头");
        }
        
        // 解析Length字段（4字节无符号大端，用long避免溢出）
        long respLenLong = ((respHeader[4] & 0xFFL) << 24) |
                           ((respHeader[5] & 0xFFL) << 16) |
                           ((respHeader[6] & 0xFFL) << 8) |
                           (respHeader[7] & 0xFFL);
        
        // 检查长度是否合理（0 < 长度 <= 1MB，防止恶意报文或错误）
        if (respLenLong <= 0 || respLenLong > 1024 * 1024) {
            throw new FinsResponseException("无效的FINS/TCP响应长度: " + respLenLong);
        }
        
        int respLen = (int) respLenLong;
        
        // 读取后续数据（Command+Error+FINS Frame）
        byte[] respBody = new byte[respLen];
        readFully(in, respBody);
        
        // 6. 解析响应
        // 跳过command和error，前8字节
        byte[] respFinsFrame = new byte[respLen - 8];
        System.arraycopy(respBody, 8, respFinsFrame, 0, respLen - 8);
        
        // 检查FINS帧长度是否足够（头10 + 命令2 + 结束码2 = 14字节）
        if (respFinsFrame.length < 14) {
            throw new FinsResponseException("响应FINS帧长度不足，预期至少14字节，实际: " + respFinsFrame.length);
        }
        
        // 跳过Fins头10字节，命令2字节，读取结束码
        int endCode = ((respFinsFrame[12] & 0xFF) << 8) | (respFinsFrame[13] & 0xFF);
        if (endCode != 0) {
            throw new FinsResponseException(String.format("FINS读命令错误，结束码: 0x%04X", endCode));
        }

        // 检查数据部分长度是否足够
        int expectedDataLen = 14 + count * 2;
        if (respFinsFrame.length < expectedDataLen) {
            throw new FinsResponseException("响应FINS帧数据部分长度不足，预期: " + expectedDataLen + "，实际: " + respFinsFrame.length);
        }
        
        // 解析数据
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int offset = 14 + i * 2;
            int value = ((respFinsFrame[offset] & 0xFF) << 8) | (respFinsFrame[offset + 1] & 0xFF);
            result.add(value);
        }
        
        return result;
    }
    
    /**
     * 写入多个内存区字
     */
    public static boolean writeMultipleMemoryAreas(String componentId, Integer finsNodeAddress, String jsonArray){
        Socket socket = FinsConnectionManager.connections.get(componentId);
        if(socket==null||!socket.isConnected()||socket.isClosed()){
            return false;
        }
        try {
            List<FinsRangeParserUtil.RangeItem> itemList = JSONArray.parseArray(jsonArray,FinsRangeParserUtil.RangeItem.class);
            itemList.forEach(item->{
                try {
                    writeMultipleMemoryArea(componentId, item.getAreaCode(), item.getStart(), item.getRegisterList());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 写入多个内存区字
     *
     * @param componentId 组件ID
     * @param areaCode    存储区代码
     * @param startAddr   起始地址
     * @param values     要写入的16位整数列表
     * @return 是否写入成功
     */
    public static boolean writeMultipleMemoryArea(String componentId, int areaCode, int startAddr, List<Integer> values) throws Exception {
        Socket socket = FinsConnectionManager.connections.get(componentId);
        FinsTcpConfig config = FinsConnectionManager.configMap.get(componentId);
        if(socket==null||!socket.isConnected()||socket.isClosed()){
            return false;
        }
        // 校验参数：FINS协议单次写入数量限制
        if (values == null || values.isEmpty() || values.size() > 1000) {
            throw new IllegalArgumentException("写入寄存器数量需在1-1000之间");
        }
        
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        
        byte currentSid = sid++;
        
        // 1. 构建FINS帧
        int dataLen = values.size() * 2;
        int finsFrameLen = 10 + 2 + 6 + dataLen; // 头10 + 命令2 + 参数6 + 数据
        byte[] finsFrame = new byte[finsFrameLen];
        finsFrame[0] = (byte) 0x80; // ICF
        finsFrame[1] = (byte) 0x00; // RSV
        finsFrame[2] = (byte) 0x02; // GCT
        finsFrame[3] = (byte) 0x00; // DNA
        finsFrame[4] = config.getPlcNodeAddress().byteValue(); // DA1
        finsFrame[5] = (byte) 0x00; // DA2
        finsFrame[6] = (byte) 0x00; // SNA
        finsFrame[7] = config.getClientNodeAddress().byteValue(); // SA1
        finsFrame[8] = (byte) 0x00; // SA2
        finsFrame[9] = currentSid; // SID
        
        finsFrame[10] = (byte) 0x01; // MR
        finsFrame[11] = (byte) 0x02; // SR 写内存区
        
        finsFrame[12] = (byte) areaCode;
        // 起始地址
        finsFrame[13] = (byte) ((startAddr >> 16) & 0xFF);
        finsFrame[14] = (byte) ((startAddr >> 8) & 0xFF);
        finsFrame[15] = (byte) (startAddr & 0xFF);
        // 数量
        finsFrame[16] = (byte) ((values.size() >> 8) & 0xFF);
        finsFrame[17] = (byte) (values.size() & 0xFF);
        
        // 写入数据
        for (int i = 0; i < values.size(); i++) {
            int val = values.get(i);
            finsFrame[18 + i*2] = (byte) ((val >> 8) & 0xFF);
            finsFrame[19 + i*2] = (byte) (val & 0xFF);
        }
        
        // 2. 构建FINS/TCP头
        byte[] header = new byte[8];
        header[0] = 'F'; header[1] = 'I'; header[2] = 'N'; header[3] = 'S';
        int totalLen = 4 + 4 + finsFrameLen;
        header[4] = (byte) ((totalLen >> 24) & 0xFF);
        header[5] = (byte) ((totalLen >> 16) & 0xFF);
        header[6] = (byte) ((totalLen >> 8) & 0xFF);
        header[7] = (byte) (totalLen & 0xFF);
        
        // 3. 命令和错误
        byte[] cmdError = new byte[8];
        cmdError[0] = 0x00; cmdError[1] = 0x00; cmdError[2] = 0x00; cmdError[3] = 0x02;
        cmdError[4] = 0x00; cmdError[5] = 0x00; cmdError[6] = 0x00; cmdError[7] = 0x00;
        
        // 4. 发送
        out.write(header);
        out.write(cmdError);
        out.write(finsFrame);
        out.flush();
        
        // 5. 读取响应
        // 先读FINS/TCP头
        byte[] respHeader = new byte[8];
        readFully(in, respHeader);
        
        // 验证FINS头
        if (respHeader[0] != 'F' || respHeader[1] != 'I' || respHeader[2] != 'N' || respHeader[3] != 'S') {
            throw new FinsResponseException("无效的FINS/TCP响应头");
        }
        
        // 解析Length
        long respLenLong = ((respHeader[4] & 0xFFL) << 24) |
                           ((respHeader[5] & 0xFFL) << 16) |
                           ((respHeader[6] & 0xFFL) << 8) |
                           (respHeader[7] & 0xFFL);
        
        if (respLenLong <= 0 || respLenLong > 1024 * 1024) {
            throw new Exception("无效的FINS/TCP响应长度: " + respLenLong);
        }
        
        int respLen = (int) respLenLong;
        
        // 读取后续数据
        byte[] respBody = new byte[respLen];
        readFully(in, respBody);
        
        // 解析响应
        byte[] respFinsFrame = new byte[respLen - 8];
        System.arraycopy(respBody, 8, respFinsFrame, 0, respLen - 8);
        
        if (respFinsFrame.length < 14) {
            throw new Exception("响应FINS帧长度不足，预期至少14字节，实际: " + respFinsFrame.length);
        }
        
        int endCode = ((respFinsFrame[12] & 0xFF) << 8) | (respFinsFrame[13] & 0xFF);
        return endCode == 0;
    }
}