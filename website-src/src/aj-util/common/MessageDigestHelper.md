---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util.njk
---
 # MessageDigestHelper Tutorial

The `MessageDigestHelper` class provides methods for generating message digests using different algorithms like MD5, SHA-1, and SHA-256. These methods can be used to create hashes for verifying data integrity, storing passwords securely, and other cryptographic purposes.

## Methods

### 1. `getMd5(String str)`

Generates an MD5 hash for a given string.

*   **Parameters:**
    *   `str`: The input string to hash.
*   **Returns:** The MD5 hash of the input string.

**Example:**

```java
String md5Hash = MessageDigestHelper.getMd5("Hello World!");
// md5Hash will be the MD5 hash of "Hello World!"
```

### 2. `getSHA1(String str)`

Generates a SHA-1 hash for a given string.

*   **Parameters:**
    *   `str`: The input string to hash.
*   **Returns:** The SHA-1 hash of the input string.

**Example:**

```java
String sha1Hash = MessageDigestHelper.getSHA1("Hello World!");
// sha1Hash will be the SHA-1 hash of "Hello World!"
```

### 3. `getSHA256(String str)`

Generates a SHA-256 hash for a given string.

*   **Parameters:**
    *   `str`: The input string to hash.
*   **Returns:** The SHA-256 hash of the input string.

**Example:**

```java
String sha256Hash = MessageDigestHelper.getSHA256("Hello World!");
// sha256Hash will be the SHA-256 hash of "Hello World!"
```

### 4. `getMd5AsBase64(String str)`

Generates an MD5 hash for a given string and encodes it in Base64.

*   **Parameters:**
    *   `str`: The input string to hash.
*   **Returns:** The Base64-encoded MD5 hash of the input string.

**Example:**

```java
String md5Base64Hash = MessageDigestHelper.getMd5AsBase64("Hello World!");
// md5Base64Hash will be the Base64-encoded MD5 hash of "Hello World!"
```

### 5. `getResult()`

Returns the hash result for the configured algorithm, input string, and key (if provided).

*   **Returns:** The hash result as a string.

**Example:**

```java
MessageDigestHelper helper = new MessageDigestHelper()
    .setAlgorithmName("SHA-256")
    .setValue("Hello World!");
String hashResult = helper.getResult();
// hashResult will be the SHA-256 hash of "Hello World!"
```
