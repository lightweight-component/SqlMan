---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util-cn.njk
---
# RegExpUtils 教程 

`RegExpUtils` 类提供了用于匹配字符串、提取匹配组和查找字符串中所有匹配项的方法。这些方法可用于验证输入、解析文本等。

## 方法

### 1. `isMatch(Pattern pattern, String str)`

检查字符串是否匹配给定的正则表达式模式。

*   **参数说明：**
    *   `pattern`: 正则表达式模式。
    *   `str`: 要与模式匹配的字符串。
*   **返回值:** 如果字符串匹配模式则返回 `true`，否则返回 `false`。

**示例:**

```java
Pattern pattern = Pattern.compile("^a");
boolean matches = RegExpUtils.isMatch(pattern, "abc");
// matches 将会是 true
```

### 2. `regMatch(String regexp, String str, int groupIndex)`

使用正则表达式在字符串中查找匹配项并返回指定的组。

*   **参数说明：**
    *   `regexp`: 正则表达式。
    *   `str`: 要匹配的字符串。
    *   `groupIndex`: 要返回的组索引。如果为 -1，则返回最后一组。
*   **返回值:** 匹配的组，如果未找到匹配项则返回 `null`。

**示例:**

```java
String match = RegExpUtils.regMatch("^a(b)", "abc", 1);
// match 将会是 "b"
```

### 3. `regMatch(String regexp, String str)`

使用正则表达式在字符串中查找匹配项并返回第一个组。

*   **参数说明：**
    *   `regexp`: 正则表达式。
    *   `str`: 要匹配的字符串。
*   **返回值:** 匹配的组，如果未找到匹配项则返回 `null`。

**示例:**

```java
String match = RegExpUtils.regMatch("^a", "abc");
// match 将会是 "a"
```

### 4. `regMatchAll(String regexp, String str)`

使用正则表达式在字符串中查找所有匹配项并返回它们的数组。

*   **参数说明：**
    *   `regexp`: 正则表达式。
    *   `str`: 要匹配的字符串。
*   **返回值:** 所有匹配组的数组。

**示例:**

```java
String[] matches = RegExpUtils.regMatchAll("\\d+", "abc123def456");
// matches 将会是 ["123", "456"]
```