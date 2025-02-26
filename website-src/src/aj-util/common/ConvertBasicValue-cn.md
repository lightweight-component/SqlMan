---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util-cn.njk
---
# ConvertBasicValue 教程 

本教程旨在介绍 `lightweight-component/aj-util` 库中的 `ConvertBasicValue` 类。`ConvertBasicValue` 提供了一组实用工具方法，用于将对象安全地转换为基本的 Java 数据类型。本指南将详细介绍每个方法的目的，并提供相应的用法示例。

## 引言

`ConvertBasicValue` 类提供了一系列方法，可以将对象安全地转换为各种基本 Java 类型，例如 `String`、`boolean`、`int`、`long`、`float`、`double`、`Date` 和 `BigDecimal`。 这些方法能够处理空值（null），并在适当的情况下尝试解析字符串表示形式，从而为直接类型转换提供了一种更强大的替代方案。

## 方法详解

### 1. `basicCast(Object value, Class<T> clz)`

安全地将对象强制转换为指定的类类型。 此方法利用 `basicConvert` 执行初始转换，然后将结果强制转换为所需的类型。

*   **参数：**
    *   `value`: 要转换的对象。
    *   `clz`: 目标类类型。
*   **返回值：** 转换为 `T` 类型的对象。

**示例：**

```java
Integer intValue = ConvertBasicValue.basicCast("123", Integer.class);
// intValue 的值为 123
```

### 2. `basicConvert(Object value, Class<?> clz)`

将对象转换为指定的类类型。 此方法处理 `null` 值并执行特定于类型的转换。

*   **参数：**
    *   `value`: 要转换的对象。
    *   `clz`: 目标类类型。
*   **返回值：** 转换后的对象；如果输入值为 `null`，则返回 `null`。

此方法包含不同类型转换的核心逻辑。 让我们看看它处理的一些特定转换：

*   **String：** 使用 `value.toString()` 将对象转换为字符串。
*   **boolean/Boolean：** 使用 `toBoolean()` 方法（如下所述）将对象转换为布尔值。
*   **int/Integer：** 使用 `object2int()` 方法（如下所述）将对象转换为整数。
*   **long/Long：** 使用 `object2long()` 方法（如下所述）将对象转换为长整数。
*   **float/Float：** 使用 `object2float()` 方法将对象转换为浮点数。
*   **double/Double：** 使用 `object2double()` 方法将对象转换为双精度浮点数。
*   **Date：** 使用 `DateHelper.object2Date()` 方法将对象转换为 Date 对象。
*   **BigDecimal：** 如果值为 String 或 Number，则将对象转换为 BigDecimal。
*   **Array：** 使用 `toArray()` 方法将对象转换为数组。
*   **Enum：** 将对象转换为枚举。

### 3. `toBoolean(Object value)`

将对象转换为布尔值。 此方法处理各种输入类型，包括字符串、数字和布尔值。

*   **参数：**
    *   `value`: 要转换的对象。
*   **返回值：** 对象的布尔值。

**示例：**

```java
assertTrue(ConvertBasicValue.toBoolean(true));
assertTrue(ConvertBasicValue.toBoolean("true"));
assertTrue(ConvertBasicValue.toBoolean("1"));
assertFalse(ConvertBasicValue.toBoolean("false"));
assertFalse(ConvertBasicValue.toBoolean(0));
```

### 4. `object2int(Object value)`

将对象转换为整数。 此方法处理 `null` 值并尝试解析字符串表示形式。

*   **参数：**
    *   `value`: 要转换的对象。
*   **返回值：** 对象的整数值；如果输入值为 `null`，则返回 0。
*   **抛出：** 如果对象无法转换为整数，则抛出 `IllegalArgumentException`。

**示例：**

```java
assertEquals(0, ConvertBasicValue.object2int(null));
assertEquals(123, ConvertBasicValue.object2int("123"));
assertEquals(-456, ConvertBasicValue.object2int("-456"));
```

### 5. `object2long(Object value)`

将对象转换为长整数。 此方法处理 `null` 值并尝试解析字符串表示形式。

*   **参数：**
    *   `value`: 要转换的对象。
*   **返回值：** 对象的长整数值；如果输入值为 `null`，则返回 0L。
*   **抛出：** 如果对象无法转换为长整数，则抛出 `IllegalArgumentException`。

**示例：**

```java
assertEquals(0L, ConvertBasicValue.object2long(null));
assertEquals(123L, ConvertBasicValue.object2long("123"));
assertEquals(-456L, ConvertBasicValue.object2long("-456"));
```

### 6. `object2double(Object value)`

将对象转换为双精度浮点数。 此方法处理 `null` 值并尝试解析字符串表示形式。

*   **参数：**
    *   `value`: 要转换的对象。
*   **返回值：** 对象的双精度浮点数值；如果输入值为 `null`，则返回 0.0。

**示例：**

```java
assertEquals(0.0, ConvertBasicValue.object2double(null), 0.0001);
assertEquals(3.14, ConvertBasicValue.object2double("3.14"), 0.0001);
```

## 单元测试示例

以下是单元测试中的一些示例：

```java
import org.junit.jupiter.api.Test;
import static com.ajaxjs.util.ConvertBasicValue.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestConvertBasicValue {
    @Test
    public void testToBoolean() {
        assertTrue(toBoolean(true));
        assertTrue(toBoolean("true"));
        assertTrue(toBoolean("True"));

        assertFalse(toBoolean("fAlse"));
        assertFalse(toBoolean("null"));

        assertTrue(toBoolean("on"));
        assertTrue(toBoolean("yes"));
        assertTrue(toBoolean(1));
        assertTrue(toBoolean("1"));
    }

    @Test
    public void testObject2Int() {
        assertEquals(0, object2int(null));
        assertEquals(100, object2int(100));
        assertEquals(123, object2int("123"));
    }

    @Test
    public void testObject2Long() {
        assertEquals(0L, object2long(null));
        assertEquals(100L, object2long(100L));
        assertEquals(123L, object2long("123"));
    }

    @Test
    public void testObject2Double() {
        assertEquals(0.0, object2double(null), 0.0001);
        assertEquals(3.14, object2double(3.14), 0.0001);
    }
}
```

## 总结

`ConvertBasicValue` 类提供了一组有用的实用工具，用于将对象安全地转换为基本的 Java 类型。 通过使用这些方法，您可以简化代码并更优雅地处理潜在的 `null` 值和解析错误。 请记住查阅库的 Javadoc 以获取最新信息和其他方法。