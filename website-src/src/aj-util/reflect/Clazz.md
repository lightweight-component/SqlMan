---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util.njk
---
# Clazz
The `Clazz` class provides methods for dynamically creating class instances, getting constructors, and checking if a class has constructors with parameters. These methods facilitate efficient and convenient handling of Java classes at runtime.

## Methods

### 1. `newInstance(Constructor<T> constructor, Object... args)`

Creates an instance of a class using the specified constructor and arguments.

*   **Parameters:**
    *   `constructor`: The constructor to use for creating the instance.
    *   `args`: The arguments to pass to the constructor.
*   **Returns:** The created instance of type `T`.

**Example:**

```java
Constructor<String> constructor = String.class.getConstructor(String.class);
String instance = Clazz.newInstance(constructor, "Hello");
// instance will be "Hello"
```

### 2. `hasArgsCon(Class<?> clz)`

Determines if a class has any constructors with parameters.

*   **Parameters:**
    *   `clz`: The class to check.
*   **Returns:** `true` if the class has constructors with parameters, `false` otherwise.

**Example:**

```java
boolean hasArgsCon = Clazz.hasArgsCon(String.class);
// hasArgsCon will be true
```

### 3. `newInstance(String className, Class<T> clazz)`

Creates an instance of a class by its name and casts it to the specified interface type.

*   **Parameters:**
    *   `className`: The full name of the class.
    *   `clazz`: The interface type to cast to.
*   **Returns:** The created instance of type `T`.

**Example:**

```java
String instance = Clazz.newInstance("java.lang.String", String.class);
// instance will be an empty string
```

### 4. `newInstance(String clzName, Object... args)`

Creates an instance of a class by its name and arguments.

*   **Parameters:**
    *   `clzName`: The full name of the class.
    *   `args`: The arguments to pass to the constructor.
*   **Returns:** The created instance as an `Object`.

**Example:**

```java
Object instance = Clazz.newInstance("java.lang.String", "Hello");
// instance will be "Hello"
```

## Unit Test Examples

Here are some examples from the unit tests:

```java name=src/test/java/com/ajaxjs/util/reflect/TestClazz.java
package com.ajaxjs.util.reflect;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.ajaxjs.util.reflect.Clazz.getConstructor;
import static com.ajaxjs.util.reflect.Clazz.newInstance;
import static org.junit.jupiter.api.Assertions.*;

public class TestClazz {
    @Test
    public void testGetClassByName() {
        Class<?> actual = Clazz.getClassByName("java.lang.String");
        assertEquals(String.class, actual);
    }

    @Test
    public void testGetClassByName_whenClassNotFound() {
        Class<?> actual = Clazz.getClassByName("com.example.NotFoundClass");
        assertNull(actual);
    }

    @Test
    public void testGetClassByName_whenClassFoundWithGenerics() {
        Class<?> actual = Clazz.getClassByName("java.util.ArrayList");
        assertEquals(ArrayList.class, actual);
    }

    @Test
    public void testGetClassByInterface() {
        Class<?> actual = Clazz.getClassByInterface(List.class);
        assertEquals(List.class, actual);
    }

    @Test
    public void testNewInstance() {
        assertNotNull(newInstance(TestReflectUtil.Foo.class));
        assertNotNull(newInstance(TestReflectUtil.Foo.class, "a", "b"));
        assertNotNull(newInstance(Objects.requireNonNull(getConstructor(TestReflectUtil.Foo.class))));
        assertNotNull(newInstance(Objects.requireNonNull(getConstructor(TestReflectUtil.Foo.class, String.class, String.class)), "a", "b"));
        assertNotNull(newInstance("com.ajaxjs.util.reflect.TestReflectUtil"));
        assertNotNull(Clazz.getClassByName("com.ajaxjs.util.reflect.TestReflectUtil"));

        Class<?>[] cs = Clazz.getDeclaredInterface(ArrayList.class);
        assertNotNull(cs);
    }
}
```

## Conclusion

The `Clazz` class provides a set of useful utilities for working with Java classes through reflection. By using these methods, you can simplify your code and handle class operations more efficiently. Remember to consult the library's Javadoc for the most up-to-date information and additional methods.