---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util-cn.njk
---

# EncodeTools 教程

`EncodeTools` 类提供了 URL 编码、URL 解码、Base64 编码和 Base64 解码的方法。这些方法可用于处理字符串编码和解码的各种用途，如准备 HTTP 请求的字符串或编码二进制数据。

## 方法

### 1. `urlEncode(String str)`

使用 URL 编码对字符串进行编码。

*   **参数说明：**
    *   `str`: 要编码的输入字符串。
*   **返回值:** URL 编码后的字符串。

**示例:**

```java
String encoded = EncodeTools.urlEncode("Hello World!");
// encoded 将是 "Hello%20World%21"
```

### 2. `urlDecode(String str)`

解码 URL 编码的字符串。

*   **参数说明：**
    *   `str`: 要解码的 URL 编码字符串。
*   **返回值:** 解码后的字符串。

**示例:**

```java
String decoded = EncodeTools.urlDecode("Hello%20World%21");
// decoded 将是 "Hello World!"
```

### 3. `base64Encode(byte[] bytes)`

使用 Base64 编码对字节数组进行编码。

*   **参数说明：**
    *   `bytes`: 要编码的字节数组。
*   **返回值:** Base64 编码后的字节数组。

**示例:**

```java
byte[] encodedBytes = EncodeTools.base64Encode("Hello World!".getBytes());
// encodedBytes 将是 Base64 编码后的字节数组
```

### 4. `base64EncodeToString(byte[] bytes)`

使用 Base64 编码对字节数组进行编码，并返回结果字符串。

*   **参数说明：**
    *   `bytes`: 要编码的字节数组。
*   **返回值:** Base64 编码后的字符串。

**示例:**

```java
String encodedString = EncodeTools.base64EncodeToString("Hello World!".getBytes());
// encodedString 将是 "SGVsbG8gV29ybGQh"
```

### 5. `base64Decode(String str)`

解码 Base64 编码的字符串。

*   **参数说明：**
    *   `str`: 要解码的 Base64 编码字符串。
*   **返回值:** 解码后的字节数组。

**示例:**

```java
byte[] decodedBytes = EncodeTools.base64Decode("SGVsbG8gV29ybGQh");
// decodedBytes 将是解码后的字节数组
```

### 6. `base64DecodeToString(String str)`

解码 Base64 编码的字符串，并返回结果字符串。

*   **参数说明：**
    *   `str`: 要解码的 Base64 编码字符串。
*   **返回值:** 解码后的字符串。

**示例:**

```java
String decodedString = EncodeTools.base64DecodeToString("SGVsbG8gV29ybGQh");
// decodedString 将是 "Hello World!"
```
