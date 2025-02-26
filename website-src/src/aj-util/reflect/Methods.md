---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util.njk
---
# Methods
The `Methods` class provides methods for dynamically retrieving and invoking methods, as well as handling method parameters. These methods facilitate efficient and convenient manipulation of Java methods at runtime.

## Methods

### 1. `getDeclaredMethod(Class<?> clz, String methodName)`

Retrieves a declared method of a class by its name.

*   **Parameters:**
    *   `clz`: The class to retrieve the method from.
    *   `methodName`: The name of the method.
*   **Returns:** The retrieved method, or `null` if the method does not exist.

**Example:**

```java
Method method = Methods.getDeclaredMethod(String.class, "length");
// method will be the "length" method of the String class
```

### 2. `getMethod(Object obj, String method, Class<?>... args)`

Retrieves a method of a class or an instance by its name and parameter types.

*   **Parameters:**
    *   `obj`: The instance or class to retrieve the method from.
    *   `method`: The name of the method.
    *   `args`: The parameter types of the method.
*   **Returns:** The retrieved method, or `null` if the method does not exist.

**Example:**

```java
Method method = Methods.getMethod(String.class, "substring", int.class, int.class);
// method will be the "substring" method of the String class that takes two int parameters
```

### 3. `getMethod(Object obj, String method, Object... args)`

Retrieves a method of a class or an instance by its name and parameters.

*   **Parameters:**
    *   `obj`: The instance or class to retrieve the method from.
    *   `method`: The name of the method.
    *   `args`: The parameters of the method.
*   **Returns:** The retrieved method, or `null` if the method does not exist.

**Example:**

```java
Method method = Methods.getMethod("example", "substring", 1, 3);
// method will be the "substring" method of the String class that takes two int parameters
```

### 4. `getMethodByUpCastingSearch(Class<?> clz, String method, Object arg)`

Searches for a method by its name and parameter, automatically upcasting the parameter type.

*   **Parameters:**
    *   `clz`: The class to search for the method in.
    *   `method`: The name of the method.
    *   `arg`: The parameter of the method.
*   **Returns:** The retrieved method, or `null` if the method does not exist.

**Example:**

```java
Method method = Methods.getMethodByUpCastingSearch(List.class, "add", "example");
// method will be the "add" method of the List class that takes an Object parameter
```

### 5. `getDeclaredMethodByInterface(Class<?> clz, String method, Object arg)`

Retrieves a declared method of a class by its name and parameter, automatically upcasting the parameter type.

*   **Parameters:**
    *   `clz`: The class to retrieve the method from.
    *   `method`: The name of the method.
    *   `arg`: The parameter of the method.
*   **Returns:** The retrieved method, or `null` if the method does not exist.

**Example:**

```java
Method method = Methods.getDeclaredMethodByInterface(List.class, "add", "example");
// method will be the "add" method of the List class that takes an Object parameter
```

## Unit Test Examples

Here are some examples from the unit tests:

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

## Conclusion

The `Methods` class provides a set of useful utilities for working with Java methods through reflection. By using these methods, you can simplify your code and handle method operations more efficiently. Remember to consult the library's Javadoc for the most up-to-date information and additional methods.