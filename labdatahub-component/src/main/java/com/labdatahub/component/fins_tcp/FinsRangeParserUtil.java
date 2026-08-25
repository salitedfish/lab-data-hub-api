package com.labdatahub.component.fins_tcp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FINS地址范围解析工具类
 * 功能：解析"D100,W20-30,CIO100"格式字符串，转换为「存储区+开始位置+数量」的结构化数据
 */
public class FinsRangeParserUtil {
    // 匹配FINS地址片段的正则，支持Dxxx、Wxxx、CIOxxx，以及范围xxx-xxx
    private static final Pattern FINS_RANGE_PATTERN = Pattern.compile("^([DWC]|CIO)(\\d+)(?:-(\\d+))?$");
    
    /**
     * 范围项实体：封装存储区、开始位置和数量
     */
    public static class RangeItem {
        private int areaCode;  // FINS存储区代码
        private int start;    // 开始地址
        private int count;    // 读取数量
        private List<Integer> registerList; // 读取结果
        
        public RangeItem(int areaCode, int start, int count) {
            this.areaCode = areaCode;
            this.start = start;
            this.count = count;
        }
        
        // getter/setter
        public int getAreaCode() { return areaCode; }
        public void setAreaCode(int areaCode) { this.areaCode = areaCode; }
        public int getStart() { return start; }
        public void setStart(int start) { this.start = start; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public List<Integer> getRegisterList() { return registerList; }
        public void setRegisterList(List<Integer> registerList) { this.registerList = registerList; }
    }
    
    /**
     * 根据存储区前缀获取对应的FINS存储区代码
     */
    private static int getAreaCode(String prefix) {
        switch (prefix.toUpperCase()) {
            case "D": return 0x82; // DM区字访问
            case "W": return 0xB1; // WR区字访问
            case "C":
            case "CIO": return 0x30; // CIO区字访问
            default: throw new IllegalArgumentException("不支持的存储区类型: " + prefix + "，当前支持D/W/CIO");
        }
    }
    
    /**
     * 解析FINS地址范围字符串
     * @param rangeStr 格式示例："D100,W20-30,CIO100-200"（空/空白字符串返回空列表）
     * @return 解析后的RangeItem列表
     * @throws IllegalArgumentException 非法格式时抛出
     */
    public static List<RangeItem> parse(String rangeStr) {
        List<RangeItem> result = new ArrayList<>();
        // 空值/空白处理
        if (rangeStr == null || rangeStr.trim().isEmpty()) {
            return result;
        }
        // 按逗号分割片段
        String[] segments = rangeStr.trim().split(",");
        for (String segment : segments) {
            String trimSegment = segment.trim();
            if (trimSegment.isEmpty()) {
                continue; // 跳过空片段
            }
            Matcher matcher = FINS_RANGE_PATTERN.matcher(trimSegment);
            if (matcher.matches()) {
                String prefix = matcher.group(1);
                int startAddr = Integer.parseInt(matcher.group(2));
                String endStr = matcher.group(3);
                
                int areaCode = getAreaCode(prefix);
                
                if (endStr == null) {
                    // 单地址
                    result.add(new RangeItem(areaCode, startAddr, 1));
                } else {
                    // 范围地址
                    int endAddr = Integer.parseInt(endStr);
                    if (endAddr < startAddr) {
                        throw new IllegalArgumentException("范围格式非法：结束值(" + endAddr + ") < 开始值(" + startAddr + ")，片段：" + trimSegment);
                    }
                    int count = endAddr - startAddr + 1;
                    result.add(new RangeItem(areaCode, startAddr, count));
                }
            } else {
                throw new IllegalArgumentException("格式非法，不支持的FINS地址片段：" + trimSegment + "，示例格式：D100,W20-30,CIO100");
            }
        }
        return result;
    }
}
