---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util.njk
---
 # RegExpUtils Tutorial

The `RegExpUtils` class provides methods for matching strings against regular expressions, extracting matched groups, and finding all matches in a string. These methods can be used for validating input, parsing text, and more.

## Methods

### 1. `isMatch(Pattern pattern, String str)`

Checks if a string matches a given regular expression pattern.

*   **Parameters:**
    *   `pattern`: The regular expression pattern.
    *   `str`: The string to match against the pattern.
*   **Returns:** `true` if the string matches the pattern, `false` otherwise.

**Example:**

```java
Pattern pattern = Pattern.compile("^a");
boolean matches = RegExpUtils.isMatch(pattern, "abc");
// matches will be true
```

### 2. `regMatch(String regexp, String str, int groupIndex)`

Finds a match in a string using a regular expression and returns the specified group.

*   **Parameters:**
    *   `regexp`: The regular expression.
    *   `str`: The string to match against.
    *   `groupIndex`: The group index to return. If -1, returns the last group.
*   **Returns:** The matched group, or `null` if no match is found.

**Example:**

```java
String match = RegExpUtils.regMatch("^a(b)", "abc", 1);
// match will be "b"
```

### 3. `regMatch(String regexp, String str)`

Finds a match in a string using a regular expression and returns the first group.

*   **Parameters:**
    *   `regexp`: The regular expression.
    *   `str`: The string to match against.
*   **Returns:** The matched group, or `null` if no match is found.

**Example:**

```java
String match = RegExpUtils.regMatch("^a", "abc");
// match will be "a"
```

### 4. `regMatchAll(String regexp, String str)`

Finds all matches in a string using a regular expression and returns them as an array.

*   **Parameters:**
    *   `regexp`: The regular expression.
    *   `str`: The string to match against.
*   **Returns:** An array of all matched groups.

**Example:**

```java
String[] matches = RegExpUtils.regMatchAll("\\d+", "abc123def456");
// matches will be ["123", "456"]
```