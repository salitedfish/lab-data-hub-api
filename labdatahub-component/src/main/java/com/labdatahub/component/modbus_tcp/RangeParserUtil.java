package com.labdatahub.component.modbus_tcp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 范围解析工具类
 * 功能：解析"1,2-5,6,7-9"格式字符串，转换为「开始位置+数量」的结构化数据
 */
public class RangeParserUtil {
    // 数字正则（仅匹配正整数）
    private static final Pattern NUM_PATTERN = Pattern.compile("^\\d+$");
    // 范围正则（匹配 x-y 格式，x和y均为正整数）
    private static final Pattern RANGE_PATTERN = Pattern.compile("^(\\d+)-(\\d+)$");

    /**
     * 范围项实体：封装开始位置和数量
     */
    public static class RangeItem {
        private int start;  // 开始位置
        private int count;  // 数量

        private List<Integer> registerList;

        public RangeItem(int start, int count) {
            this.start = start;
            this.count = count;
        }

        // getter/setter
        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<Integer> getRegisterList() {
            return registerList;
        }

        public void setRegisterList(List<Integer> registerList) {
            this.registerList = registerList;
        }
    }

    /**
     * 解析范围字符串
     * @param rangeStr 格式示例："1,2-5,6,7-9"（空/空白字符串返回空列表）
     * @return 解析后的RangeItem列表
     * @throws IllegalArgumentException 非法格式时抛出（如2-1、a-5、2-等）
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
                continue; // 跳过空片段（如",,1-3,"中的空值）
            }

            // 1. 匹配单数字格式（如1、6）
            if (NUM_PATTERN.matcher(trimSegment).matches()) {
                int start = Integer.parseInt(trimSegment);
                result.add(new RangeItem(start, 1));
            }
            // 2. 匹配范围格式（如2-5、7-9）
            else if (RANGE_PATTERN.matcher(trimSegment).matches()) {
                String[] rangeParts = trimSegment.split("-");
                int start = Integer.parseInt(rangeParts[0]);
                int end = Integer.parseInt(rangeParts[1]);

                // 校验结束值 ≥ 开始值
                if (end < start) {
                    throw new IllegalArgumentException("范围格式非法：结束值(" + end + ") < 开始值(" + start + ")，片段：" + trimSegment);
                }

                // 计算数量：结束值 - 开始值 + 1
                int count = end - start + 1;
                result.add(new RangeItem(start, count));
            }
            // 3. 非法格式
            else {
                throw new IllegalArgumentException("格式非法，不支持的片段：" + trimSegment);
            }
        }

        return result;
    }

    // 测试示例
    public static void main(String[] args) {
        // 测试用例1：正常格式
        String testStr1 = "1,2-5,6,7-9";
        List<RangeItem> result1 = parse(testStr1);
        System.out.println("解析结果1：");
        result1.forEach(System.out::println);
        // 输出：
        // RangeItem{start=1, count=1}
        // RangeItem{start=2, count=4}
        // RangeItem{start=6, count=1}
        // RangeItem{start=7, count=3}

        // 测试用例2：单数字
        String testStr2 = "8";
        List<RangeItem> result2 = parse(testStr2);
        System.out.println("\n解析结果2：");
        result2.forEach(System.out::println);
        // 输出：RangeItem{start=8, count=1}

        // 测试用例3：非法格式（2-1）
        try {
            parse("2-1");
        } catch (IllegalArgumentException e) {
            System.out.println("\n解析异常3：" + e.getMessage());
            // 输出：范围格式非法：结束值(1) < 开始值(2)，片段：2-1
        }

        // 测试用例4：非法格式（a-5）
        try {
            parse("a-5");
        } catch (IllegalArgumentException e) {
            System.out.println("\n解析异常4：" + e.getMessage());
            // 输出：格式非法，不支持的片段：a-5
        }
    }
}