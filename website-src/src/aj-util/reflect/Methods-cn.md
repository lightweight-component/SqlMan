---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util-cn.njk
---
# Methods
`Methods` 类提供了动态检索和调用方法以及处理方法参数的方法。这些方法有助于在运行时高效、方便地操作 Java 方法。

## 方法

### 1. `getDeclaredMethod(Class<?> clz, String methodName)`

通过方法名检索类的声明方法。

*   **参数说明：**
    *   `clz`: 要检索方法的类。
    *   `methodName`: 方法的名称。
*   **返回值:** 检索到的方法，如果方法不存在则返回 `null`。

**示例:**

```java
Method method = Methods.getDeclaredMethod(String.class, "length");
// method 将会是 String 类的 "length" 方法
```

### 2. `getMethod(Object obj, String method, Class<?>... args)`

通过方法名和参数类型检索类或实例的方法。

*   **参数说明：**
    *   `obj`: 要检索方法的实例或类。
    *   `method`: 方法的名称。
    *   `args`: 方法的参数类型。
*   **返回值:** 检索到的方法，如果方法不存在则返回 `null`。

**示例:**

```java
Method method = Methods.getMethod(String.class, "substring", int.class, int.class);
// method 将会是 String 类的 "substring" 方法，带有两个 int 参数
```

### 3. `getMethod(Object obj, String method, Object... args)`

通过方法名和参数检索类或实例的方法。

*   **参数说明：**
    *   `obj`: 要检索方法的实例或类。
    *   `method`: 方法的名称。
    *   `args`: 方法的参数。
*   **返回值:** 检索到的方法，如果方法不存在则返回 `null`。

**示例:**

```java
Method method = Methods.getMethod("example", "substring", 1, 3);
// method 将会是 String 类的 "substring" 方法，带有两个 int 参数
```

### 4. `getMethodByUpCastingSearch(Class<?> clz, String method, Object arg)`

通过方法名和参数搜索方法，自动向上转型参数类型。

*   **参数说明：**
    *   `clz`: 要搜索方法的类。
    *   `method`: 方法的名称。
    *   `arg`: 方法的参数。
*   **返回值:** 检索到的方法，如果方法不存在则返回 `null`。

**示例:**

```java
Method method = Methods.getMethodByUpCastingSearch(List.class, "add", "example");
// method 将会是 List 类的 "add" 方法，带有一个 Object 参数
```

### 5. `getDeclaredMethodByInterface(Class<?> clz, String method, Object arg)`

通过方法名和参数检索类的声明方法，自动向上转型参数类型。

*   **参数说明：**
    *   `clz`: 要检索方法的类。
    *   `method`: 方法的名称。
    *   `arg`: 方法的参数。
*   **返回值:** 检索到的方法，如果方法不存在则返回 `null`。

**示例:**

```java
Method method = Methods.getDeclaredMethodByInterface(List.class, "add", "example");
// method 将会是 List 类的 "add" 方法，带有一个 Object 参数
```

## 单元测试示例

以下是单元测试中的一些示例：

```java name=src/test/java/com/ajaxjs/util/reflect/TestReflectUtil.java
package com.ajaxjs.util.reflect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestReflectUtil {
    public static class Foo {
        public Foo() {
        }

        public Foo(String str, String str2) {
        }

        public void Bar() {

        }

        public void CC(String cc) {

        }

        public String Bar2() {
            return "bar2";
        }

        public String Bar3(String arg) {
            return arg;
        }
    }

    static class Foo2 {
        public void m1() {
        }

        public void m1(String arg) {
        }
    }

    static class Bar extends Foo {
        public void m2() {
        }
    }

    @Test
    public void testGetMethod() {
        assertNotNull(Methods.getMethod(new Foo(), "m1"));// 按实际对象
        assertNotNull(Methods.getMethod(Foo2.class, "m1"));// 按类引用
        assertNotNull(Methods.getMethod(Foo2.class, "m1", String.class)); // 按参数类型
        assertNotNull(Methods.getMethod(Foo2.class, "m1", "foo"));// 按实际参数
        assertNotNull(Methods.getMethod(Bar.class, "m1"));
        assertNotNull(Methods.getMethod(Bar.class, "m1", String.class));
        assertNotNull(Methods.getMethod(Bar.class, "m2"));
    }

    static class Foo1 {
        public void foo(Foo1 a) {

        }
    }

    static class Bar2 extends Foo1 {

    }

    @Test
    public void testGetMethodByUpCastingSearch() {
        assertNull(Methods.getMethod(Foo1.class, "foo", new Bar2())); // 找不到
        assertNotNull(Methods.getMethodByUpCastingSearch(Foo1.class, "foo", new Bar2())); // 找到了
    }

    public static class A {
        public String foo(A a) {
            return "A.foo";
        }

        public String bar(C c) {
            return "A.bar";
        }
    }

    public static class B extends A {
    }

    public interface C {
    }

    public static class D implements C {
    }

    @Test
    public void testDeclaredMethod() {
        assertNull(Methods.getMethodByUpCastingSearch(A.class, "bar", new D())); // 找不到
        assertNotNull(Methods.getDeclaredMethodByInterface(A.class, "bar", new D()));// 找到了
    }

    public static class Foo3 {
        public void m1() {
        }

        public String m1(String arg) {
            return arg;
        }
    }

    static class Bar3 extends Foo3 {
        public void m2() {
        }
    }

    @Test
    public void testExecuteMethod() {
        assertNull(Methods.executeMethod(new Foo3(), "m1"));
        assertEquals(Methods.executeMethod(new Foo3(), "m1", "foo"), "foo");
        assertNull(Methods.executeMethod(new Bar2(), "m1"));
        assertEquals(Methods.executeMethod(new Bar3(), "m1", "bar"), "bar");
        assertEquals(Methods.executeMethod(new Bar3(), "m1", String.class, "foo"), "foo");
    }
}
```

 