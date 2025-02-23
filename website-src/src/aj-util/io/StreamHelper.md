---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util.njk
---
# StreamHelper
 The `StreamHelper` class provides static methods to perform common operations on Java streams.  It focuses on simplifying the conversion of input streams to strings and efficiently copying data between streams.

## Methods

### 1. `copyToString(InputStream in)`

Reads an input stream and converts it to a string using the default UTF-8 charset.

*   **Parameters:**
    *   `in`: The input stream to read from. The stream is not closed by this method.
*   **Returns:** The content of the input stream as a string.

**Example:**

```java
InputStream inputStream = new ByteArrayInputStream("Hello, World!".getBytes(StandardCharsets.UTF_8));
String content = StreamHelper.copyToString(inputStream);
System.out.println(content); // Output: Hello, World!\n
```

### 2. `copyToString(InputStream in, Charset encode)`

Reads an input stream and converts it to a string using the specified charset.

*   **Parameters:**
    *   `in`: The input stream to read from.  The stream is not closed by this method.
    *   `encode`: The charset to use for decoding the input stream.
*   **Returns:** The content of the input stream as a string.

**Example:**

```java
InputStream inputStream = new ByteArrayInputStream("你好，世界！".getBytes(StandardCharsets.UTF_8));
String content = StreamHelper.copyToString(inputStream, StandardCharsets.UTF_8);
System.out.println(content); // Output: 你好，世界！\n
```

### 3. `read(InputStream in, Charset encode, Consumer<String> fn)`

Reads the input stream line by line and applies the given consumer function to each line.

*   **Parameters:**
    *   `in`: The input stream to read from. The stream is closed within the method.
    *   `encode`: The charset to use for decoding the input stream.
    *   `fn`: A `Consumer` that accepts a string (a line from the input stream) and performs an action.

**Example:**

```java
InputStream inputStream = new ByteArrayInputStream("Line 1\nLine 2\nLine 3".getBytes(StandardCharsets.UTF_8));
StreamHelper.read(inputStream, StandardCharsets.UTF_8, line -> {
    System.out.println("Line: " + line);
});
// Output:
// Line: Line 1
// Line: Line 2
// Line: Line 3
```

## Unit Test Examples

```java
package com.ajaxjs.util.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestStreamHelper {
    private InputStream inputStream;
    private String testString;

    @BeforeEach
    public void setUp() {
        testString = "测试字符串";
        inputStream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void copyToString_WithDefaultCharset_ReturnsCorrectString() {
        String result = StreamHelper.copyToString(inputStream);
        assertEquals(testString + "\n", result);
    }

    @Test
    public void copyToString_WithSpecifiedCharset_ReturnsCorrectString() {
        String result = StreamHelper.copyToString(inputStream, StandardCharsets.UTF_8);
        assertEquals(testString + "\n", result);
    }
}
```

**Note:** The `inputStream2Byte` method is present in the test code but not in the provided `StreamHelper` class code.

## Conclusion

The `StreamHelper` class provides a convenient way to perform common stream operations in Java. By using these methods, you can simplify your code and avoid writing repetitive stream I/O logic.