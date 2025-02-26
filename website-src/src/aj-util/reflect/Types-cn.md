---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util-cn.njk
---

# Types
`Types` 类提供了检索泛型类型信息、转换类型以及处理参数化类型的方法。这些方法有助于在运行时高效、方便地操作 Java 类型。

## 方法

### 1. `getActualType(Type type)`

检索参数化类型的实际类型参数。

*   **参数说明：**
    *   `type`: 要从中检索实际类型参数的类型。
*   **返回值:** 表示实际类型参数的 `Type` 数组，如果输入类型不是参数化类型，则返回 `null`。

**示例:**

```java
Type type = new ParameterizedType() {
    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{String.class};
    }

    @Override
    public Type getRawType() {
        return List.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
};
Type[] actualType = Types.getActualType(type);
// actualType 将包含 String.class
```

### 2. `getGenericReturnType(Method method)`

检索方法返回类型的实际类型参数。

*   **参数说明：**
    *   `method`: 要从中检索返回类型的类型。
*   **返回值:** 表示返回类型实际类型参数的 `Type` 数组。

**示例:**

```java
Method method = TestTypes.class.getMethods()[0];
Type[] actualType = Types.getGenericReturnType(method);
// actualType 将包含方法的泛型返回类型
```

### 3. `getGenericFirstReturnType(Method method)`

检索方法返回类型的第一个实际类型参数并将其转换为 `Class`。

*   **参数说明：**
    *   `method`: 要从中检索返回类型的类型。
*   **返回值:** 返回类型的第一个实际类型参数作为 `Class`，如果返回类型不是参数化的，则返回 `null`。

**示例:**

```java
Method method = TestTypes.class.getMethods()[0];
Class<?> actualType = Types.getGenericFirstReturnType(method);
// actualType 将是方法的第一个泛型返回类型，作为 Class
```

### 4. `getActualType(Class<?> clz)`

检索类的超类的实际类型参数。

*   **参数说明：**
    *   `clz`: 要从中检索实际类型参数的类。
*   **返回值:** 表示类的超类的实际类型参数的 `Type` 数组。

**示例:**

```java
Type[] actualType = Types.getActualType(ArrayList.class);
// actualType 将包含 ArrayList 超类的泛型类型参数
```

### 5. `getActualClass(Class<?> clz)`

检索类的超类的第一个实际类型参数并将其转换为 `Class`。

*   **参数说明：**
    *   `clz`: 要从中检索实际类型参数的类。
*   **返回值:** 超类的第一个实际类型参数作为 `Class`。

**示例:**

```java
Class<?> actualClass = Types.getActualClass(ArrayList.class);
// actualClass 将是 ArrayList 超类的第一个泛型类型参数，作为 Class
```

### 6. `type2class(Type type)`

将 `Type` 转换为 `Class`。

*   **参数说明：**
    *   `type`: 要转换的 `Type`。
*   **返回值:** `Type` 的 `Class` 表示，如果 `Type` 不是 `Class`，则返回 `null`。

**示例:**

```java
Type type = String.class;
Class<?> actualClass = Types.type2class(type);
// actualClass 将是 String.class
```