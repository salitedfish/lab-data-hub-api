package com.labdatahub.component.http;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-10-18
 */
public class FileRestore {

    /**
     * 将字节数组还原为原始文件（推荐方法）
     * @param byteArray 从原始文件读取的字节数组
     * @param restorePath 还原的目标路径
     */
    public static boolean restoreFileFromBytes(byte[] byteArray, String restorePath) throws IOException {
        Path path = Paths.get(restorePath);

        // 确保目标目录存在
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        // 直接写入字节数组，保持原始内容
        Files.write(path, byteArray);
        return true;
    }
}
