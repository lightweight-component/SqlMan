---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util-cn.njk
---

# BytesHelper 教程

本教程概述了 `lightweight-component/aj-util` 库中的 `BytesHelper` 类。`BytesHelper` 提供了一系列用于在 Java 中处理字节数组的实用方法。本指南将介绍每个方法的用途并提供使用示例。

## 简介

`BytesHelper` 类提供了一些有用的方法来操作字节数组，包括：

*   提取子数组
*   查找字节数组在另一个字节数组中的索引
*   连接字节数组
*   将字节数组转换为十六进制字符串，反之亦然

这些方法在各种场景中都非常有用，例如网络编程、文件 I/O 和数据处理。

## 方法

### 1. `subBytes(byte[] data, int off, int length)`

从给定的字节数组中提取子数组。

*   **参数：**
    *   `data`: 输入字节数组。
    *   `off`: 子数组的起始偏移量（索引）。
    *   `length`: 要提取的子数组的长度。
*   **返回：** 包含提取的子数组的新字节数组。

**示例：**

```java
byte[] original = {0x01, 0x02, 0x03, 0x04, 0x05};
byte[] sub = BytesHelper.subBytes(original, 1, 3); // 从索引 1 提取，长度为 3
// sub 将为 {0x02, 0x03, 0x04}
```

### 2. `byteIndexOf(byte[] data, byte[] search, int start)`

在字节数组 (`data`) 中查找字节数组 (`search`) 的索引，从指定的索引 (`start`) 开始搜索。

*   **参数：**
    *   `data`: 要在其中搜索的字节数组。
    *   `search`: 要搜索的字节数组。
    *   `start`: 开始搜索的索引。
*   **返回：** `search` 在 `data` 中首次出现的索引，如果未找到则返回 -1。

**示例：**

```java
byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05};
byte[] search = {0x03, 0x04};
int index = BytesHelper.byteIndexOf(data, search, 0); // 从头开始搜索
// index 将为 2
```

### 3. `byteIndexOf(byte[] data, byte[] search)`

在字节数组 (`data`) 中查找字节数组 (`search`) 的索引，从头开始（索引 0）搜索。 这是前一个方法的重载版本。

*   **参数：**
    *   `data`: 要在其中搜索的字节数组。
    *   `search`: 要搜索的字节数组。
*   **返回：** `search` 在 `data` 中首次出现的索引，如果未找到则返回 -1。

**示例：**

```java
byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05};
byte[] search = {0x03, 0x04};
int index = BytesHelper.byteIndexOf(data, search); // 从头开始搜索
// index 将为 2
```

### 4. `concat(byte[] a, byte[] b)`

将两个字节数组连接成一个新的字节数组。

*   **参数：**
    *   `a`: 第一个字节数组。
    *   `b`: 第二个字节数组。
*   **返回：** 包含 `a` 的元素，后跟 `b` 的元素的新字节数组。

**示例：**

```java
byte[] a = {0x01, 0x02};
byte[] b = {0x03, 0x04};
byte[] combined = BytesHelper.concat(a, b);
// combined 将为 {0x01, 0x02, 0x03, 0x04}
```

### 5. `bytesToHexStr(byte[] bytes)`

将字节数组转换为其十六进制字符串表示形式。

*   **参数：**
    *   `bytes`: 要转换的字节数组。
*   **返回：** 一个字符串，表示数组中每个字节的十六进制值。

**示例：**

```java
byte[] bytes = {0x1A, 0x2B, 0x3C};
String hexString = BytesHelper.bytesToHexStr(bytes);
// hexString 将为 "1A2B3C"
```

### 6. `parseHexStr2Byte(String hex)` (假设 - 在提供的代码中未找到，但如果它存在或您实现了它，则包含它会很好)

将十六进制字符串转换为字节数组。 此方法出现在测试代码中，因此它可能是库的一部分，或者打算成为库的一部分。

*   **参数：**
    *   `hex`: 要转换的十六进制字符串。
*   **返回：** 表示转换后的十六进制字符串的字节数组。

**示例：**

```java
String hexString = "1A2B3C";
byte[] bytes = BytesHelper.parseHexStr2Byte(hexString);
// bytes 将为 {0x1A, 0x2B, 0x3C}
```

**单元测试示例（基于提供的测试代码）：**

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBytesHelper {
    @Test
    public void testParseHexStr2Byte() {
        byte[] bs = BytesHelper.parseHexStr2Byte("1A2B3C");
        assert bs != null;
        assertEquals(0x1A, bs[0]);
    }
}
```

## 结论

`BytesHelper` 类提供了一组方便的实用程序，用于 Java 中常见的字节数组操作。 通过使用这些方法，您可以简化代码并避免编写重复的字节数组处理逻辑。 请记住查阅库的 Javadoc 以获取最新信息和其他方法。