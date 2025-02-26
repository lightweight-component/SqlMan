---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util-cn.njk
---

# Clazz
`Clazz` 类提供了动态创建类实例、获取构造函数以及检查类是否具有带参数的构造函数的方法。这些方法有助于在运行时高效、方便地处理 Java 类。

## 方法

### 1. `newInstance(Constructor<T> constructor, Object... args)`

使用指定的构造函数和参数创建类的实例。

*   **参数说明：**
    *   `constructor`: 用于创建实例的构造函数。
    *   `args`: 传递给构造函数的参数。
*   **返回值:** 类型为 `T` 的创建的实例。

**示例:**

```java
Constructor<String> constructor = String.class.getConstructor(String.class);
String instance = Clazz.newInstance(constructor, "Hello");
// instance 将会是 "Hello"
```

### 2. `hasArgsCon(Class<?> clz)`

确定一个类是否具有带参数的构造函数。

*   **参数说明：**
    *   `clz`: 要检查的类。
*   **返回值:** 如果类具有带参数的构造函数，则返回 `true`，否则返回 `false`。

**示例:**

```java
boolean hasArgsCon = Clazz.hasArgsCon(String.class);
// hasArgsCon 将会是 true
```

### 3. `newInstance(String className, Class<T> clazz)`

通过类名创建类的实例并将其转换为指定的接口类型。

*   **参数说明：**
    *   `className`: 类的全名。
    *   `clazz`: 要转换为的接口类型。
*   **返回值:** 类型为 `T` 的创建的实例。

**示例:**

```java
String instance = Clazz.newInstance("java.lang.String", String.class);
// instance 将会是一个空字符串
```

### 4. `newInstance(String clzName, Object... args)`

通过类名和参数创建类的实例。

*   **参数说明：**
    *   `clzName`: 类的全名。
    *   `args`: 传递给构造函数的参数。
*   **返回值:** 创建的实例，类型为 `Object`。

**示例:**

```java
Object instance = Clazz.newInstance("java.lang.String", "Hello");
// instance 将会是 "Hello"
```