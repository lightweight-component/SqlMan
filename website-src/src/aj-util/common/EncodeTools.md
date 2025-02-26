---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util.njk
---
# EncodeTools Tutorial

The `EncodeTools` class provides methods for URL encoding, URL decoding, Base64 encoding, and Base64 decoding. These methods can be used to handle string encoding and decoding for various purposes, such as preparing strings for HTTP requests or encoding binary data.

## Methods

### 1. `urlEncode(String str)`

Encodes a string using URL encoding.

*   **Parameters:**
    *   `str`: The input string to encode.
*   **Returns:** The URL-encoded string.

**Example:**

```java
String encoded = EncodeTools.urlEncode("Hello World!");
// encoded will be "Hello%20World%21"
```

### 2. `urlDecode(String str)`

Decodes a URL-encoded string.

*   **Parameters:**
    *   `str`: The URL-encoded string to decode.
*   **Returns:** The decoded string.

**Example:**

```java
String decoded = EncodeTools.urlDecode("Hello%20World%21");
// decoded will be "Hello World!"
```

### 3. `base64Encode(byte[] bytes)`

Encodes a byte array using Base64 encoding.

*   **Parameters:**
    *   `bytes`: The byte array to encode.
*   **Returns:** The Base64-encoded byte array.

**Example:**

```java
byte[] encodedBytes = EncodeTools.base64Encode("Hello World!".getBytes());
// encodedBytes will be the Base64-encoded byte array
```

### 4. `base64EncodeToString(byte[] bytes)`

Encodes a byte array using Base64 encoding and returns the result as a string.

*   **Parameters:**
    *   `bytes`: The byte array to encode.
*   **Returns:** The Base64-encoded string.

**Example:**

```java
String encodedString = EncodeTools.base64EncodeToString("Hello World!".getBytes());
// encodedString will be "SGVsbG8gV29ybGQh"
```

### 5. `base64Decode(String str)`

Decodes a Base64-encoded string.

*   **Parameters:**
    *   `str`: The Base64-encoded string to decode.
*   **Returns:** The decoded byte array.

**Example:**

```java
byte[] decodedBytes = EncodeTools.base64Decode("SGVsbG8gV29ybGQh");
// decodedBytes will be the decoded byte array
```

### 6. `base64DecodeToString(String str)`

Decodes a Base64-encoded string and returns the result as a string.

*   **Parameters:**
    *   `str`: The Base64-encoded string to decode.
*   **Returns:** The decoded string.

**Example:**

```java
String decodedString = EncodeTools.base64DecodeToString("SGVsbG8gV29ybGQh");
// decodedString will be "Hello World!"
```
