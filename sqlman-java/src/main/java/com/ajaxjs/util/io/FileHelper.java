package com.ajaxjs.util.io;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FileHelper {
    /**
     * 读取文件内容并返回为字符串。
     *
     * @param filePath 文件路径
     * @return 文件内容
     * @throws UncheckedIOException 如果读取文件时发生错误
     */
    public static String readFileContent(String filePath) {
        try {
            return Files.readString(Paths.get(filePath));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 将字符串内容写入文件。
     *
     * @param filePath 文件路径
     * @param content  要写入的内容
     * @throws UncheckedIOException 如果写入文件时发生错误
     */
    public static void writeFileContent(String filePath, String content) {
        try {
            Files.write(Paths.get(filePath), content.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 删除文件或目录。
     *
     * @param filePath 文件或目录路径
     * @throws UncheckedIOException 如果删除文件时发生错误
     */
    public static void deleteFileOrDirectory(String filePath) {
        Path path = Paths.get(filePath);

        try {
            if (Files.isDirectory(path)) {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } else
                Files.delete(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 列出目录内容。
     *
     * @param directoryPath 目录路径
     * @return 目录内容列表
     * @throws UncheckedIOException 如果列出目录内容时发生错误
     */
    public static List<String> listDirectoryContents(String directoryPath) {
        try (Stream<Path> stream = Files.list(Paths.get(directoryPath))) {
            return stream.map(p -> p.getFileName().toString()).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 创建目录。
     *
     * @param directoryPath 目录路径
     * @throws UncheckedIOException 如果创建目录时发生错误
     */
    public static void createDirectory(String directoryPath) {
        try {
            Files.createDirectories(Paths.get(directoryPath));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 检查文件或目录是否存在。
     *
     * @param filePath 文件或目录路径
     * @return 如果文件或目录存在则返回 true，否则返回 false
     */
    public static boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 获取文件或目录的大小。
     *
     * @param filePath 文件或目录路径
     * @return 文件或目录的大小（以字节为单位）
     * @throws UncheckedIOException 如果获取大小时发生错误
     */
    public static long getFileSize(String filePath) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(Paths.get(filePath), BasicFileAttributes.class);

            return attrs.size();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 复制文件或目录。
     *
     * @param source 源文件或目录路径
     * @param target 目标文件或目录路径
     * @throws UncheckedIOException 如果复制文件时发生错误
     */
    public static void copyFileOrDirectory(String source, String target) {
        Path sourcePath = Paths.get(source);
        Path targetPath = Paths.get(target);

        try {
            if (Files.isDirectory(sourcePath)) {
                Files.walk(sourcePath).forEach(sourceFilePath -> {
                    Path targetFilePath = targetPath.resolve(sourcePath.relativize(sourceFilePath));

                    try {
                        Files.copy(sourceFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            } else
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 移动文件或目录。
     *
     * @param source 源文件或目录路径
     * @param target 目标文件或目录路径
     * @throws UncheckedIOException 如果移动文件时发生错误
     */
    public static void moveFileOrDirectory(String source, String target) {
        Path sourcePath = Paths.get(source);
        Path targetPath = Paths.get(target);

        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}