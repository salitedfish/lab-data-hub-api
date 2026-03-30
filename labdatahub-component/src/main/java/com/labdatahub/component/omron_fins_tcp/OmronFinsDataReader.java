package com.labdatahub.component.omron_fins_tcp;

import com.alibaba.fastjson2.JSONArray;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据读取器，负责从Omron PLC读写寄存器数据
 */
public class OmronFinsDataReader {

    /**
     * 读取内存区域（字为单位）
     * @param client FINS客户端
     * @param areaCode 内存区域代码（如0x82代表D区）
     * @param startAddr 起始地址（字地址）
     * @param count 读取字数
     * @return 读取到的整数列表
     */
    public static List<Integer> readWords(OmronFinsClient client, int areaCode, int startAddr, int count) throws Exception {
        int[] data = client.readWords(areaCode, startAddr, count);
        if (data == null) {
            throw new Exception("读取FINS数据失败");
        }
        List<Integer> result = new ArrayList<>();
        for (int v : data) {
            result.add(v);
        }
        return result;
    }

    /**
     * 写入多个字
     */
    public static boolean writeWords(String componentId, Integer slaveId, String jsonArray) {
        OmronFinsClient client = OmronFinsConnectionManager.connections.get(componentId);
        if (client == null || !client.isConnected()) {
            return false;
        }
        try {
            List<RangeParserUtil.RangeItem> itemList = JSONArray.parseArray(jsonArray, RangeParserUtil.RangeItem.class);
            for (RangeParserUtil.RangeItem item : itemList) {
                // 假设区域代码从配置或解析中获取，此处简化：使用D区（0x82）
                int areaCode = 0x82; // 可根据需要扩展
                if (!writeWords(client, areaCode, item.getStart(), item.getRegisterList())) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 写入多个保持寄存器（此处对应FINS字写入）
     */
    public static boolean writeWords(OmronFinsClient client, int areaCode, int startAddr, List<Integer> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("写入值列表不能为空");
        }
        int[] arr = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            arr[i] = values.get(i);
        }
        return client.writeWords(areaCode, startAddr, arr);
    }
}