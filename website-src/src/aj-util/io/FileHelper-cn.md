---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util-cn.njk
---

# FileHelper
`FileHelper` 类简化了常见的文件系统操作，例如读取和写入文件内容、删除文件或目录、列出目录内容、创建目录以及检查文件或目录是否存在。 它利用 `java.nio.file` 包来实现高效且现代的文件 I/O。

## 方法

### 1. `readFileContent(String filePath)`

将文件的全部内容读取到字符串中。

*   **参数：**
    *   `filePath`：文件的路径。
*   **返回值：** 文件的内容，以字符串形式表示。
*   **抛出：** 如果在文件读取期间发生错误，则抛出 `UncheckedIOException`。

**示例：**

```java
String content = FileHelper.readFileContent("path/to/my/file.txt");
System.out.println(content);
```

### 2. `writeFileContent(String filePath, String content)`

将字符串写入文件，覆盖任何现有内容。

*   **参数：**
    *   `filePath`：文件的路径。
    *   `content`：要写入文件的字符串。
*   **抛出：** 如果在文件写入期间发生错误，则抛出 `UncheckedIOException`。

**示例：**

```java
FileHelper.writeFileContent("path/to/my/file.txt", "你好，世界！");
```

### 3. `deleteFileOrDirectory(String filePath)`

删除文件或目录。 如果路径是目录，它将递归删除其中的所有文件和子目录。

*   **参数：**
    *   `filePath`：文件或目录的路径。
*   **抛出：** 如果在删除期间发生错误，则抛出 `UncheckedIOException`。

**示例：**

```java
FileHelper.deleteFileOrDirectory("path/to/my/file.txt");
FileHelper.deleteFileOrDirectory("path/to/my/directory");
```

### 4. `listDirectoryContents(String directoryPath)`

列出目录中文件和子目录的名称。

*   **参数：**
    *   `directoryPath`：目录的路径。
*   **返回值：** 字符串列表，其中每个字符串是文件或子目录的名称。
*   **抛出：** 如果在目录列表期间发生错误，则抛出 `UncheckedIOException`。

**示例：**

```java
List<String> contents = FileHelper.listDirectoryContents("path/to/my/directory");
for (String item : contents) {
    System.out.println(item);
}
```

### 5. `createDirectory(String directoryPath)`

创建目录，包括任何必要的父目录。

*   **参数：**
    *   `directoryPath`：要创建的目录的路径。
*   **抛出：** 如果在目录创建期间发生错误，则抛出 `UncheckedIOException`。

**示例：**

```java
FileHelper.createDirectory("path/to/my/new/directory");
```

### 6. `exists(String filePath)`

检查文件或目录是否存在。

*   **参数：**
    *   `filePath`：文件或目录的路径。
*   **返回值：** 如果文件或目录存在，则返回 `true`；否则返回 `false`。

**示例：**

```java
boolean exists = FileHelper.exists("path/to/my/file.txt");
if (exists) {
    System.out.println("文件存在！");
} else {
    System.out.println("文件不存在。");
}
```

## 单元测试示例

以下是如何在单元测试中使用 `FileHelper` 的示例：

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
        System.out.println("文件内容: " + content);

        // 写入文件内容
        FileHelper.writeFileContent(fullPath, "你好，世界！Hello, World!");

        // 列出目录内容
        List<String> directoryContents = FileHelper.listDirectoryContents(dir);
        System.out.println("目录内容: " + directoryContents);

        // 创建目录
        FileHelper.createDirectory(dir + File.separator + "newdirectory");

        // 检查文件或目录是否存在
        boolean exists = FileHelper.exists(fullPath);
        System.out.println("文件是否存在: " + exists);

        // 删除文件或目录 - 已注释掉，以防止在测试期间意外删除
        // FileHelper.deleteFileOrDirectory("output.txt");
    }
}
```

**注意：** `getFileSize()`、`copyFileOrDirectory()` 和 `moveFileOrDirectory()` 方法存在于测试代码中，但不存在于提供的 `FileHelper` 类代码中。

## 结论

`FileHelper` 类提供了一组方便的实用程序，用于在 Java 中执行常见的文件系统操作。 通过使用这些方法，您可以简化代码并避免编写重复的文件 I/O 逻辑。