---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util.njk
---
# BytesHelper Tutorial

This tutorial provides an overview of the `BytesHelper` class, which is part of the `lightweight-component/aj-util` library.  `BytesHelper` offers a collection of utility methods for working with byte arrays in Java. This guide will cover the purpose of each method and provide usage examples.

## Introduction

The `BytesHelper` class provides several useful methods for manipulating byte arrays, including:

*   Extracting subarrays
*   Finding the index of a byte array within another
*   Concatenating byte arrays
*   Converting byte arrays to hexadecimal strings and vice versa

These methods can be helpful in various scenarios, such as network programming, file I/O, and data processing.

## Methods

### 1. `subBytes(byte[] data, int off, int length)`

Extracts a subarray from a given byte array.

*   **Parameters:**
    *   `data`: The input byte array.
    *   `off`: The starting offset (index) for the subarray.
    *   `length`: The length of the subarray to extract.
*   **Returns:** A new byte array containing the extracted subarray.

**Example:**

```java
byte[] original = {0x01, 0x02, 0x03, 0x04, 0x05};
byte[] sub = BytesHelper.subBytes(original, 1, 3); // Extract from index 1, length 3
// sub will be {0x02, 0x03, 0x04}
```

### 2. `byteIndexOf(byte[] data, byte[] search, int start)`

Finds the index of a byte array (`search`) within another byte array (`data`), starting the search from a specified index (`start`).

*   **Parameters:**
    *   `data`: The byte array to search within.
    *   `search`: The byte array to search for.
    *   `start`: The index to start the search from.
*   **Returns:** The index of the first occurrence of `search` within `data`, or -1 if not found.

**Example:**

```java
byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05};
byte[] search = {0x03, 0x04};
int index = BytesHelper.byteIndexOf(data, search, 0); // Search from the beginning
// index will be 2
```

### 3. `byteIndexOf(byte[] data, byte[] search)`

Finds the index of a byte array (`search`) within another byte array (`data`), starting the search from the beginning (index 0). This is an overloaded version of the previous method.

*   **Parameters:**
    *   `data`: The byte array to search within.
    *   `search`: The byte array to search for.
*   **Returns:** The index of the first occurrence of `search` within `data`, or -1 if not found.

**Example:**

```java
byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05};
byte[] search = {0x03, 0x04};
int index = BytesHelper.byteIndexOf(data, search); // Search from the beginning
// index will be 2
```

### 4. `concat(byte[] a, byte[] b)`

Concatenates two byte arrays into a new byte array.

*   **Parameters:**
    *   `a`: The first byte array.
    *   `b`: The second byte array.
*   **Returns:** A new byte array containing the elements of `a` followed by the elements of `b`.

**Example:**

```java
byte[] a = {0x01, 0x02};
byte[] b = {0x03, 0x04};
byte[] combined = BytesHelper.concat(a, b);
// combined will be {0x01, 0x02, 0x03, 0x04}
```

### 5. `bytesToHexStr(byte[] bytes)`

Converts a byte array to its hexadecimal string representation.

*   **Parameters:**
    *   `bytes`: The byte array to convert.
*   **Returns:** A string representing the hexadecimal value of each byte in the array.

**Example:**

```java
byte[] bytes = {0x1A, 0x2B, 0x3C};
String hexString = BytesHelper.bytesToHexStr(bytes);
// hexString will be "1A2B3C"
```

### 6. `parseHexStr2Byte(String hex)` (Hypothetical - Not found in the provided code, but good to include if it existed or you implemented it)

Converts a hexadecimal string to a byte array.  This method appears in the test code, so it's likely part of the library, or intended to be.

*   **Parameters:**
    *   `hex`: The hexadecimal string to convert.
*   **Returns:** A byte array representing the converted hexadecimal string.

**Example:**

```java
String hexString = "1A2B3C";
byte[] bytes = BytesHelper.parseHexStr2Byte(hexString);
// bytes will be {0x1A, 0x2B, 0x3C}
```

**Unit Test Example (Based on the provided test code):**

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBytesHelper {
    @Test
    public void testParseHexStr2Byte() {
        byte[] bs = BytesHelper.parseHexStr2Byte("1A2B3C");
        assert bs != null;
        assertEquals(0x1A, bs[0]);
    }
}
```

## Conclusion

The `BytesHelper` class provides a convenient set of utilities for common byte array manipulations in Java.  By using these methods, you can simplify your code and avoid writing repetitive byte array handling logic.  Remember to consult the library's Javadoc for the most up-to-date information and additional methods.