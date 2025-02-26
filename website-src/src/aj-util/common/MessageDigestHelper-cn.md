---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util-cn.njk
---
# MessageDigestHelper 教程 

`MessageDigestHelper` 类提供了使用不同算法生成消息摘要的方法，如 MD5、SHA-1 和 SHA-256。这些方法可用于创建哈希以验证数据完整性、安全存储密码等加密用途。

## 方法

### 1. `getMd5(String str)`

生成给定字符串的 MD5 哈希。

*   **参数说明：**
    *   `str`: 要哈希的输入字符串。
*   **返回值:** 输入字符串的 MD5 哈希。

**示例:**

```java
String md5Hash = MessageDigestHelper.getMd5("Hello World!");
// md5Hash 将是 "Hello World!" 的 MD5 哈希
```

### 2. `getSHA1(String str)`

生成给定字符串的 SHA-1 哈希。

*   **参数说明：**
    *   `str`: 要哈希的输入字符串。
*   **返回值:** 输入字符串的 SHA-1 哈希。

**示例:**

```java
String sha1Hash = MessageDigestHelper.getSHA1("Hello World!");
// sha1Hash 将是 "Hello World!" 的 SHA-1 哈希
```

### 3. `getSHA256(String str)`

生成给定字符串的 SHA-256 哈希。

*   **参数说明：**
    *   `str`: 要哈希的输入字符串。
*   **返回值:** 输入字符串的 SHA-256 哈希。

**示例:**

```java
String sha256Hash = MessageDigestHelper.getSHA256("Hello World!");
// sha256Hash 将是 "Hello World!" 的 SHA-256 哈希
```

### 4. `getMd5AsBase64(String str)`

生成给定字符串的 MD5 哈希，并使用 Base64 编码。

*   **参数说明：**
    *   `str`: 要哈希的输入字符串。
*   **返回值:** 输入字符串的 Base64 编码的 MD5 哈希。

**示例:**

```java
String md5Base64Hash = MessageDigestHelper.getMd5AsBase64("Hello World!");
// md5Base64Hash 将是 "Hello World!" 的 Base64 编码的 MD5 哈希
```

### 5. `getResult()`

返回配置的算法、输入字符串和密钥（如果提供）的哈希结果。

*   **返回值:** 哈希结果字符串。

**示例:**

```java
MessageDigestHelper helper = new MessageDigestHelper()
    .setAlgorithmName("SHA-256")
    .setValue("Hello World!");
String hashResult = helper.getResult();
// hashResult 将是 "Hello World!" 的 SHA-256 哈希
```