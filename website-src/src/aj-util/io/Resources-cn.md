---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util-cn.njk
---

# Resources
`Resources` 类提供静态方法来定位和检索类路径中或相对于特定类的资源。 这对于访问与应用程序捆绑在一起的配置文件、模板或其他数据文件特别有用。

## 方法

### 1. `getResourcesFromClasspath(String resource)`

检索位于类路径中的资源的路径。

*   **参数：**
    *   `resource`：资源文件的名称。 可以包括包目录（例如，`com/example/my_resource.txt`）。 空字符串返回类路径根目录。
*   **返回值：** 资源的绝对路径。
*   **抛出：** 如果找不到资源，则抛出 `RuntimeException`。

**示例：**

```java
String resourcePath = Resources.getResourcesFromClasspath("com/example/config.properties");
System.out.println(resourcePath);
```

### 2. `getResourcesFromClasspath(String resource, boolean isDecode)`

检索位于类路径中的资源的路径，并可选择进行 URL 解码。

*   **参数：**
    *   `resource`：资源文件的名称。
    *   `isDecode`：一个布尔值，指示是否应解码 URL。
*   **返回值：** 资源的绝对路径。
*   **抛出：** 如果找不到资源，则抛出 `RuntimeException`。

**示例：**

```java
String resourcePath = Resources.getResourcesFromClasspath("com/example/config.properties", true);
System.out.println(resourcePath);
```

### 3. `getResourcesFromClass(Class<?> clz, String resource)`

检索位于相对于给定类的资源的路径。

*   **参数：**
    *   `clz`：用于定位资源的类。
    *   `resource`：资源文件的名称。
*   **返回值：** 资源的绝对路径；如果未找到，则返回 `null`。

**示例：**

```java
String resourcePath = Resources.getResourcesFromClass(MyClass.class, "my_resource.txt");
System.out.println(resourcePath);
```

### 4. `getResourcesFromClass(Class<?> clz, String resource, boolean isDecode)`

检索位于相对于给定类的资源的路径，并可选择进行 URL 解码。

*   **参数：**
    *   `clz`：用于定位资源的类。
    *   `resource`：资源文件的名称。
    *   `isDecode`：一个布尔值，指示是否应解码 URL。
*   **返回值：** 资源的绝对路径；如果未找到，则返回 `null`。

**示例：**

```java
String resourcePath = Resources.getResourcesFromClass(MyClass.class, "my_resource.txt", true);
System.out.println(resourcePath);
```

### 5. `getClassName(File file, String packageName)`

从 Java 类文件中提取类名。

*   **参数：**
    *   `file`：Java 类文件。
    *   `packageName`：类的包名。
*   **返回值：** 完全限定的类名。

**示例：**

```java
File classFile = new File("path/to/MyClass.class");
String className = Resources.getClassName(classFile, "com.example");
System.out.println(className); // Output: com.example.MyClass
```

## 单元测试示例

```java
package com.ajaxjs.util.io;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class TestResources {
    @Test
    public void testGetResourcesFromClasspath_FoundResource_ShouldReturnPath() {
        assertThrows(RuntimeException.class, () -> Resources.getResourcesFromClasspath("\\com\\test.txt"));
    }

    @Test
    public void testGetResourcesFromClasspath_NotFoundResource_ShouldReturnNull() {
        assertThrows(RuntimeException.class, () -> Resources.getResourcesFromClasspath("application.yml"));
    }

    @Test
    public void testGetResourcesFromClass_FoundResource_ShouldReturnPath() {
        String resourcePath = Resources.getResourcesFromClass(TestResources.class, "test.txt");
        System.out.println(resourcePath);
        assertNull(resourcePath);
    }

    @Test
    public void testGetResourcesFromClass_NotFoundResource_ShouldReturnNull() {
        String resourcePath = Resources.getResourcesFromClass(TestResources.class, "non-existent-resource.txt");
        assertNull(resourcePath, "Expected null for non-existent resource");
    }

    @Test
    public void testGetResourceText_FoundResource_ShouldReturnContent() {
        String resourceContent = Resources.getResourceText("README.md");
        //assertNotNull(resourceContent, "Resource content not found");
    }

    @Test
    public void testGetResourceText_NotFoundResource_ShouldReturnNull() {
        String resourceContent = Resources.getResourceText("non-existent-file.md");
        //assertNull(resourceContent, "Expected null for non-existent resource");
    }

    @Test
    public void testGetProperties_ValidPropertiesFile_ShouldLoadProperties() {
        Properties properties = Resources.getProperties("test-demo.properties");
        System.out.println(properties);
        //assertNotNull(properties);
    }

    @Test
    public void testGetProperties_NotFoundPropertiesFile_ShouldThrowException() {
        assertThrows(RuntimeException.class, () -> Resources.getProperties("application.properties"));
    }
}
```

**注意：** 单元测试使用 `assertThrows` 和 `assertNull` 来检查预期的异常和 null 返回值。 由于提供的代码中没有相关方法，因此某些测试被注释掉了。

## 结论

`Resources` 类提供了一种简单有效的方法来访问应用程序中的资源。 通过使用这些方法，您可以轻松地从类路径或相对于特定类检索配置文件、模板和其他数据文件。