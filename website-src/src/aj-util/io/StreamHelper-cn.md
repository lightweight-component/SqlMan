---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util-cn.njk
---
# StreamHelper
 `StreamHelper` 类提供静态方法来对 Java 流执行常见操作。 它侧重于简化将输入流转换为字符串以及有效地在流之间复制数据。

## 方法

### 1. `copyToString(InputStream in)`

读取输入流并使用默认的 UTF-8 字符集将其转换为字符串。

*   **参数：**
    *   `in`：要从中读取的输入流。 此方法不会关闭流。
*   **返回值：** 输入流的内容，以字符串形式表示。

**示例：**

```java
InputStream inputStream = new ByteArrayInputStream("你好，世界！".getBytes(StandardCharsets.UTF_8));
String content = StreamHelper.copyToString(inputStream);
System.out.println(content); // Output: 你好，世界！\n
```

### 2. `copyToString(InputStream in, Charset encode)`

读取输入流并使用指定的字符集将其转换为字符串。

*   **参数：**
    *   `in`：要从中读取的输入流。 此方法不会关闭流。
    *   `encode`：用于解码输入流的字符集。
*   **返回值：** 输入流的内容，以字符串形式表示。

**示例：**

```java
InputStream inputStream = new ByteArrayInputStream("你好，世界！".getBytes(StandardCharsets.UTF_8));
String content = StreamHelper.copyToString(inputStream, StandardCharsets.UTF_8);
System.out.println(content); // Output: 你好，世界！\n
```

### 3. `read(InputStream in, Charset encode, Consumer<String> fn)`

逐行读取输入流，并将给定的 consumer 函数应用于每一行。

*   **参数：**
    *   `in`：要从中读取的输入流。 该方法内部会关闭流。
    *   `encode`：用于解码输入流的字符集。
    *   `fn`：一个 `Consumer`，它接受一个字符串（输入流中的一行）并执行一个动作。

**示例：**

```java
InputStream inputStream = new ByteArrayInputStream("第一行\n第二行\n第三行".getBytes(StandardCharsets.UTF_8));
StreamHelper.read(inputStream, StandardCharsets.UTF_8, line -> {
    System.out.println("行: " + line);
});
// Output:
// 行: 第一行
// 行: 第二行
// 行: 第三行
```

## 单元测试示例

```java
package com.ajaxjs.util.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestStreamHelper {
    private InputStream inputStream;
    private String testString;

    @BeforeEach
    public void setUp() {
        testString = "测试字符串";
        inputStream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void copyToString_WithDefaultCharset_ReturnsCorrectString() {
        String result = StreamHelper.copyToString(inputStream);
        assertEquals(testString + "\n", result);
    }

    @Test
    public void copyToString_WithSpecifiedCharset_ReturnsCorrectString() {
        String result = StreamHelper.copyToString(inputStream, StandardCharsets.UTF_8);
        assertEquals(testString + "\n", result);
    }
}
```

**注意：** `inputStream2Byte` 方法存在于测试代码中，但不存在于提供的 `StreamHelper` 类代码中。

## 结论

`StreamHelper` 类提供了一种方便的方式来在 Java 中执行常见的流操作。 通过使用这些方法，您可以简化代码并避免编写重复的流 I/O 逻辑。