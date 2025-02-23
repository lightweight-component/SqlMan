---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util.njk
---
# FileHelper
The `FileHelper` class simplifies common file system operations, such as reading and writing file content, deleting files or directories, listing directory contents, creating directories, and checking for the existence of files or directories. It leverages the `java.nio.file` package for efficient and modern file I/O.

## Methods

### 1. `readFileContent(String filePath)`

Reads the entire content of a file into a string.

*   **Parameters:**
    *   `filePath`: The path to the file.
*   **Returns:** The content of the file as a string.
*   **Throws:** `UncheckedIOException` if an error occurs during file reading.

**Example:**

```java
String content = FileHelper.readFileContent("path/to/my/file.txt");
System.out.println(content);
```

### 2. `writeFileContent(String filePath, String content)`

Writes a string to a file, overwriting any existing content.

*   **Parameters:**
    *   `filePath`: The path to the file.
    *   `content`: The string to write to the file.
*   **Throws:** `UncheckedIOException` if an error occurs during file writing.

**Example:**

```java
FileHelper.writeFileContent("path/to/my/file.txt", "Hello, World!");
```

### 3. `deleteFileOrDirectory(String filePath)`

Deletes a file or directory. If the path is a directory, it will recursively delete all files and subdirectories within it.

*   **Parameters:**
    *   `filePath`: The path to the file or directory.
*   **Throws:** `UncheckedIOException` if an error occurs during deletion.

**Example:**

```java
FileHelper.deleteFileOrDirectory("path/to/my/file.txt");
FileHelper.deleteFileOrDirectory("path/to/my/directory");
```

### 4. `listDirectoryContents(String directoryPath)`

Lists the names of the files and subdirectories within a directory.

*   **Parameters:**
    *   `directoryPath`: The path to the directory.
*   **Returns:** A list of strings, where each string is the name of a file or subdirectory.
*   **Throws:** `UncheckedIOException` if an error occurs during directory listing.

**Example:**

```java
List<String> contents = FileHelper.listDirectoryContents("path/to/my/directory");
for (String item : contents) {
    System.out.println(item);
}
```

### 5. `createDirectory(String directoryPath)`

Creates a directory, including any necessary parent directories.

*   **Parameters:**
    *   `directoryPath`: The path to the directory to create.
*   **Throws:** `UncheckedIOException` if an error occurs during directory creation.

**Example:**

```java
FileHelper.createDirectory("path/to/my/new/directory");
```

### 6. `exists(String filePath)`

Checks if a file or directory exists.

*   **Parameters:**
    *   `filePath`: The path to the file or directory.
*   **Returns:** `true` if the file or directory exists, `false` otherwise.

**Example:**

```java
boolean exists = FileHelper.exists("path/to/my/file.txt");
if (exists) {
    System.out.println("File exists!");
} else {
    System.out.println("File does not exist.");
}
```

## Unit Test Example

Here's an example of how `FileHelper` might be used in a unit test:

```java
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

        // 检查文件或目录是否存在
        boolean exists = FileHelper.exists(fullPath);
        System.out.println("File exists: " + exists);

        // 删除文件或目录 - commented out to prevent accidental deletion during testing
        // FileHelper.deleteFileOrDirectory("output.txt");
    }
}
```

**Note:** The `getFileSize()`, `copyFileOrDirectory()`, and `moveFileOrDirectory()` methods are present in the test code but not in the provided `FileHelper` class code.

## Conclusion

The `FileHelper` class provides a convenient set of utilities for performing common file system operations in Java. By using these methods, you can simplify your code and avoid writing repetitive file I/O logic.