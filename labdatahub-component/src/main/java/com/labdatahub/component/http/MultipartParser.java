package com.labdatahub.component.http;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-10-17
 */
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 改进的Multipart表单数据解析器
 */
public class MultipartParser {

    /**
     * 解析multipart表单数据（处理无换行符的情况）
     */
    public static Map<String, String> parse(String requestBody, String contentType) {
        Map<String, String> result = new HashMap<>();

        if (requestBody == null || requestBody.isEmpty()) {
            return result;
        }

        // 提取boundary
        String boundary = extractBoundary(contentType);
        if (boundary == null) {
            System.err.println("无法从Content-Type中提取boundary");
            return result;
        }

        System.out.println("提取到boundary: " + boundary);
        System.out.println("原始请求体长度: " + requestBody.length());

        try {
            // 完整的boundary标记
            String fullBoundary = "--" + boundary;
            String endBoundary = fullBoundary + "--";

            // 移除结束标记
            if (requestBody.endsWith(endBoundary)) {
                requestBody = requestBody.substring(0, requestBody.length() - endBoundary.length());
            }

            // 使用boundary分割parts
            String[] parts = requestBody.split(Pattern.quote(fullBoundary));
            System.out.println("分割出 " + parts.length + " 个part");

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                if (part == null || part.trim().isEmpty()) {
                    continue;
                }

                System.out.println("=== 解析第 " + (i + 1) + " 个part ===");
                System.out.println("Part前100字符: " + part.substring(0, Math.min(100, part.length())));

                // 解析单个part
                PartData partData = parsePartImproved(part);
                if (partData != null && partData.fieldName != null) {
                    result.put(partData.fieldName, partData.fieldValue);
                    System.out.println("✅ 解析到字段: " + partData.fieldName + " = " +
                            (partData.fieldValue.length() > 50 ?
                                    partData.fieldValue.substring(0, 50) + "..." :
                                    partData.fieldValue));
                }
            }

        } catch (Exception e) {
            System.err.println("解析multipart数据失败: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 改进的part解析方法（处理无换行符情况）
     */
    private static PartData parsePartImproved(String partContent) {
        PartData partData = new PartData();

        try {
            // 查找Content-Disposition的位置
            int dispositionStart = partContent.indexOf("Content-Disposition:");
            if (dispositionStart == -1) {
                System.err.println("未找到Content-Disposition");
                return partData;
            }

            // 提取Content-Disposition行
            String remaining = partContent.substring(dispositionStart);

            // 解析Content-Disposition获取字段名
            parseContentDispositionImproved(remaining, partData);

            if (partData.fieldName == null) {
                System.err.println("无法解析字段名");
                return partData;
            }

            // 查找字段值的开始位置
            // 在Content-Disposition之后查找字段值的开始
            int valueStart = findValueStart(remaining, partData.fieldName);
            if (valueStart == -1) {
                System.err.println("无法找到字段值开始位置");
                return partData;
            }

            // 提取字段值
            String fieldValue = remaining.substring(valueStart).trim();

            // 清理可能的后续boundary标记
            fieldValue = cleanFieldValue(fieldValue);

            partData.fieldValue = fieldValue;

            System.out.println("字段名: " + partData.fieldName);
            System.out.println("字段值长度: " + fieldValue.length());
            System.out.println("字段值前100字符: " +
                    (fieldValue.length() > 100 ? fieldValue.substring(0, 100) + "..." : fieldValue));

        } catch (Exception e) {
            System.err.println("解析part失败: " + e.getMessage());
            e.printStackTrace();
        }

        return partData;
    }

    /**
     * 改进的Content-Disposition解析
     */
    private static void parseContentDispositionImproved(String content, PartData partData) {
        // 查找name字段
        Pattern namePattern = Pattern.compile("name=\"([^\"]+)\"");
        Matcher nameMatcher = namePattern.matcher(content);

        if (nameMatcher.find()) {
            partData.fieldName = nameMatcher.group(1);
            System.out.println("从引号中解析到字段名: " + partData.fieldName);
            return;
        }

        // 如果没有引号，尝试其他格式
        Pattern namePattern2 = Pattern.compile("name=([^;\\s]+)");
        Matcher nameMatcher2 = namePattern2.matcher(content);
        if (nameMatcher2.find()) {
            partData.fieldName = nameMatcher2.group(1);
            System.out.println("从无引号中解析到字段名: " + partData.fieldName);
            return;
        }

        // 最后尝试：查找name=后面的内容直到下一个boundary或结束
        int nameIndex = content.indexOf("name=");
        if (nameIndex != -1) {
            int nameStart = nameIndex + 5; // "name=".length()
            int nameEnd = content.length();

            // 查找可能的结束位置
            for (int i = nameStart; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == ';' || c == ' ' || c == '\r' || c == '\n') {
                    nameEnd = i;
                    break;
                }
            }

            if (nameEnd > nameStart) {
                partData.fieldName = content.substring(nameStart, nameEnd).replace("\"", "").trim();
                System.out.println("从手动解析得到字段名: " + partData.fieldName);
            }
        }
    }

    /**
     * 查找字段值的开始位置
     */
    private static int findValueStart(String content, String fieldName) {
        // 查找字段名之后的位置
        String namePattern = "name=\"" + Pattern.quote(fieldName) + "\"";
        Pattern pattern = Pattern.compile(namePattern);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            int nameEnd = matcher.end();

            // 在字段名之后查找值的开始
            // 跳过可能的空格、分号、引号等
            for (int i = nameEnd; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '"') {
                    // 跳过引号
                    continue;
                }
                if (Character.isLetterOrDigit(c) || c == '-' || c == '_') {
                    // 找到值的开始
                    return i;
                }
            }
        }

        // 备选方案：直接查找字段名后的第一个非空字符
        int fieldNameIndex = content.indexOf(fieldName);
        if (fieldNameIndex != -1) {
            int searchStart = fieldNameIndex + fieldName.length();
            for (int i = searchStart; i < content.length(); i++) {
                char c = content.charAt(i);
                if (!Character.isWhitespace(c) && c != '"' && c != ';' && c != ':') {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * 清理字段值
     */
    private static String cleanFieldValue(String fieldValue) {
        if (fieldValue == null) {
            return "";
        }

        // 移除末尾可能存在的boundary标记
        String cleaned = fieldValue;

        // 如果包含Content-Type，需要特殊处理文件字段
        int contentTypeIndex = cleaned.indexOf("Content-Type:");
        if (contentTypeIndex != -1) {
            // 对于文件字段，Content-Type后面的内容才是真正的文件数据
            int dataStart = cleaned.indexOf("Content-Type:") + "Content-Type:".length();
            // 跳过Content-Type行
            for (int i = dataStart; i < cleaned.length(); i++) {
                if (cleaned.charAt(i) == '\n' || cleaned.charAt(i) == '\r') {
                    dataStart = i + 1;
                    break;
                }
            }
            cleaned = cleaned.substring(dataStart).trim();
        }

        return cleaned.trim();
    }

    /**
     * 从Content-Type中提取boundary
     */
    private static String extractBoundary(String contentType) {
        if (contentType == null) {
            return null;
        }

        // 匹配 boundary= 后面的值
        Pattern pattern = Pattern.compile("boundary=([^;\\s]+)");
        Matcher matcher = pattern.matcher(contentType);

        if (matcher.find()) {
            String boundary = matcher.group(1).trim();
            // 清理可能的引号
            if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
                boundary = boundary.substring(1, boundary.length() - 1);
            }
            return boundary;
        }

        return null;
    }

    /**
     * 专门针对您的数据格式的解析方法
     */
    public static Map<String, String> parseYourData(String requestBody, String contentType) {
        Map<String, String> result = new HashMap<>();

        String boundary = extractBoundary(contentType);
        if (boundary == null) {
            return result;
        }

        String fullBoundary = "--" + boundary;

        // 移除开始和结束的boundary
        String cleanBody = requestBody;
        if (cleanBody.startsWith(fullBoundary)) {
            cleanBody = cleanBody.substring(fullBoundary.length());
        }
        if (cleanBody.endsWith(fullBoundary + "--")) {
            cleanBody = cleanBody.substring(0, cleanBody.length() - (fullBoundary + "--").length());
        }

        // 按boundary分割
        String[] parts = cleanBody.split(Pattern.quote(fullBoundary));

        for (String part : parts) {
            if (part.trim().isEmpty()) {
                continue;
            }

            // 直接解析字段名和值
            parseSimplePart(part, result);
        }

        return result;
    }

    /**
     * 简单解析part（针对您的特定格式）
     */
    private static void parseSimplePart(String part, Map<String, String> result) {
        // 查找name字段
        Pattern namePattern = Pattern.compile("name=\"([^\"]+)\"");
        Matcher nameMatcher = namePattern.matcher(part);

        if (!nameMatcher.find()) {
            return;
        }

        String fieldName = nameMatcher.group(1);
        int valueStart = nameMatcher.end();

        // 提取字段值
        String fieldValue = part.substring(valueStart).trim();

        // 如果包含filename，说明是文件字段
        if (part.contains("filename=")) {
            // 文件字段需要特殊处理
            int filenameIndex = part.indexOf("filename=");
            int contentTypeIndex = part.indexOf("Content-Type:");

            if (contentTypeIndex > filenameIndex) {
                // 文件数据在Content-Type之后
                int dataStart = contentTypeIndex + "Content-Type:".length();
                // 跳过Content-Type行
                for (int i = dataStart; i < part.length(); i++) {
                    if (part.charAt(i) == '\n' || part.charAt(i) == '\r') {
                        dataStart = i + 1;
                        break;
                    }
                }
                fieldValue = part.substring(dataStart).trim();
            }
        }

        result.put(fieldName, fieldValue);
        System.out.println("✅ 解析字段: " + fieldName + " (值长度: " + fieldValue.length() + ")");
    }

    /**
     * Part数据容器
     */
    private static class PartData {
        String fieldName;
        String fieldValue;
        String filename;
    }
}