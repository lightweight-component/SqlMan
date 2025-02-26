---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util.njk
---
# RandomTools Tutorial

The `RandomTools` class provides methods for generating random integers and strings. These methods can be used when you need random values for testing, simulations, or other purposes.

## Methods

### 1. `generateRandomNumber(int numDigits)`

Generates a random integer with the specified number of digits.

*   **Parameters:**
    *   `numDigits`: The number of digits for the random number.
*   **Returns:** A random integer with the specified number of digits.

**Example:**

```java
int randomNumber = RandomTools.generateRandomNumber(6);
// randomNumber will be a 6-digit random number
```

### 2. `generateRandomNumber()`

Generates a random integer with 6 digits.

*   **Returns:** A random 6-digit integer.

**Example:**

```java
int randomNumber = RandomTools.generateRandomNumber();
// randomNumber will be a 6-digit random number
```

### 3. `generateRandomString(int length)`

Generates a random string with the specified length.

*   **Parameters:**
    *   `length`: The length of the random string.
*   **Returns:** A random string with the specified length.

**Example:**

```java
String randomString = RandomTools.generateRandomString(6);
// randomString will be a 6-character random string
```

### 4. `generateRandomString()`

Generates a random string with 6 characters.

*   **Returns:** A random 6-character string.

**Example:**

```java
String randomString = RandomTools.generateRandomString();
// randomString will be a 6-character random string
```