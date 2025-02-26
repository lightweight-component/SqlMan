---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util-cn.njk
---

# RandomTools 教程

`RandomTools` 类提供了生成随机整数和字符串的方法。当您需要随机值进行测试、模拟或其他用途时，可以使用这些方法。

## 方法

### 1. `generateRandomNumber(int numDigits)`

生成一个具有指定位数的随机整数。

*   **参数说明：**
    *   `numDigits`: 随机数的位数。
*   **返回值:** 具有指定位数的随机整数。

**示例:**

```java
int randomNumber = RandomTools.generateRandomNumber(6);
// randomNumber 将是一个6位的随机数
```

### 2. `generateRandomNumber()`

生成一个6位的随机整数。

*   **返回值:** 一个6位的随机整数。

**示例:**

```java
int randomNumber = RandomTools.generateRandomNumber();
// randomNumber 将是一个6位的随机数
```

### 3. `generateRandomString(int length)`

生成一个具有指定长度的随机字符串。

*   **参数说明：**
    *   `length`: 随机字符串的长度。
*   **返回值:** 具有指定长度的随机字符串。

**示例:**

```java
String randomString = RandomTools.generateRandomString(6);
// randomString 将是一个6字符的随机字符串
```

### 4. `generateRandomString()`

生成一个6字符的随机字符串。

*   **返回值:** 一个6字符的随机字符串。

**示例:**

```java
String randomString = RandomTools.generateRandomString();
// randomString 将是一个6字符的随机字符串
```