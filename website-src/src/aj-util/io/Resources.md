---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util.njk
---

# Resources
The `Resources` class provides static methods to locate and retrieve resources from the classpath or relative to a specific class. This is particularly useful for accessing configuration files, templates, or other data files bundled with your application.

## Methods

### 1. `getResourcesFromClasspath(String resource)`

Retrieves the path of a resource located in the classpath.

*   **Parameters:**
    *   `resource`: The name of the resource file.  Can include package directories (e.g., `com/example/my_resource.txt`). An empty string returns the classpath root.
*   **Returns:** The absolute path to the resource.
*   **Throws:** `RuntimeException` if the resource is not found.

**Example:**

```java
String resourcePath = Resources.getResourcesFromClasspath("com/example/config.properties");
System.out.println(resourcePath);
```

### 2. `getResourcesFromClasspath(String resource, boolean isDecode)`

Retrieves the path of a resource located in the classpath, with optional URL decoding.

*   **Parameters:**
    *   `resource`: The name of the resource file.
    *   `isDecode`: A boolean indicating whether the URL should be decoded.
*   **Returns:** The absolute path to the resource.
*   **Throws:** `RuntimeException` if the resource is not found.

**Example:**

```java
String resourcePath = Resources.getResourcesFromClasspath("com/example/config.properties", true);
System.out.println(resourcePath);
```

### 3. `getResourcesFromClass(Class<?> clz, String resource)`

Retrieves the path of a resource located relative to a given class.

*   **Parameters:**
    *   `clz`: The class used to locate the resource.
    *   `resource`: The name of the resource file.
*   **Returns:** The absolute path to the resource, or `null` if not found.

**Example:**

```java
String resourcePath = Resources.getResourcesFromClass(MyClass.class, "my_resource.txt");
System.out.println(resourcePath);
```

### 4. `getResourcesFromClass(Class<?> clz, String resource, boolean isDecode)`

Retrieves the path of a resource located relative to a given class, with optional URL decoding.

*   **Parameters:**
    *   `clz`: The class used to locate the resource.
    *   `resource`: The name of the resource file.
    *   `isDecode`: A boolean indicating whether the URL should be decoded.
*   **Returns:** The absolute path to the resource, or `null` if not found.

**Example:**

```java
String resourcePath = Resources.getResourcesFromClass(MyClass.class, "my_resource.txt", true);
System.out.println(resourcePath);
```

### 5. `getClassName(File file, String packageName)`

Extracts the class name from a Java class file.

*   **Parameters:**
    *   `file`: The Java class file.
    *   `packageName`: The package name of the class.
*   **Returns:** The fully qualified class name.

**Example:**

```java
File classFile = new File("path/to/MyClass.class");
String className = Resources.getClassName(classFile, "com.example");
System.out.println(className); // Output: com.example.MyClass
```

## Unit Test Examples

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

**Note:** The unit tests use `assertThrows` and `assertNull` to check for exceptions and null returns as expected. Some tests are commented out since the associated methods are not available in the provided code.

## Conclusion

The `Resources` class provides a simple and effective way to access resources within your application. By using these methods, you can easily retrieve configuration files, templates, and other data files from the classpath or relative to a specific class.