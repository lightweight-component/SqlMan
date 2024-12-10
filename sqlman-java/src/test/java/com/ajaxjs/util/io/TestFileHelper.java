package com.ajaxjs.util.io;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

public class TestFileHelper {
    final String dir = Resources.getResourcesFromClass(TestFileHelper.class, "");
    final String fullPath = dir + File.separator + "test.txt";

    @Test
    public void test() {
        // 读取文件内容
        String content = FileHelper.readFileContent(fullPath);
        System.out.println("File content: " + content);

        // 写入文件内容
        FileHelper.writeFileContent(fullPath, "Hello, World! 你好世界");

        // 列出目录内容
        List<String> directoryContents = FileHelper.listDirectoryContents(dir);
        System.out.println("Directory contents: " + directoryContents);

        // 创建目录
        FileHelper.createDirectory(dir + File.separator + "newdirectory");

        // 删除文件或目录
//            FileHelper.deleteFileOrDirectory("output.txt");

        // 检查文件或目录是否存在
        boolean exists = FileHelper.exists(fullPath);
        System.out.println("File exists: " + exists);

        // 获取文件或目录的大小
        long size = FileHelper.getFileSize(fullPath);
        System.out.println("File size: " + size + " bytes");

        // 复制文件或目录
        FileHelper.copyFileOrDirectory(fullPath, "example_copy.txt");

        // 移动文件或目录
        FileHelper.moveFileOrDirectory(fullPath, "moved_example.txt");
    }
}
