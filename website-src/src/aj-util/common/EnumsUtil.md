---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util.njk
---
# EnumsUtil

This tutorial provides an overview of the `EnumsUtil` class, which is part of the `lightweight-component/aj-util` library. The `EnumsUtil` class provides a way to retrieve enum instances and their associated messages (or other values) based on a code, when the enums implement the `IEnum` interface. This is particularly useful when you have enums where each constant has a code and a corresponding descriptive message, and you need to look up the enum by its code.

## The `IEnum` Interface

Before diving into `EnumsUtil`, let's understand the `IEnum` interface. Enums that work with `EnumsUtil` are expected to implement this interface:

```java
package com.ajaxjs.util.enums;

public interface IEnum<E, V> {
    E getCode();
    V getMsg();
}
```

Where:

*   `E` is the type of the code (e.g., `String`, `Integer`).
*   `V` is the type of the message (or associated value).

## Methods

### 1. `of(E code, Class<T> clz)`

Retrieves an `Optional<T>` containing the enum instance that matches the given code.

*   **Parameters:**
    *   `code`: The code to search for.
    *   `clz`: The `Class` object representing the enum type.
*   **Returns:** An `Optional<T>` containing the enum instance if found, or `Optional.empty()` if no matching enum is found.

**Example:**

```java
Optional<TypeEnum> type = EnumsUtil.of("YES", TypeEnum.class);
if (type.isPresent()) {
    TypeEnum enumValue = type.get();
    System.out.println("Found enum: " + enumValue);
} else {
    System.out.println("Enum not found for code YES");
}
```

### 2. `ofMsg(E code, Class<T> cla)`

Retrieves the message (or associated value) of the enum instance that matches the given code.

*   **Parameters:**
    *   `code`: The code to search for.
    *   `cla`: The `Class` object representing the enum type.
*   **Returns:** The message (or associated value) of the enum instance, or `null` if no matching enum is found.

**Example:**

```java
String message = EnumsUtil.ofMsg("YES", TypeEnum.class);
System.out.println("Message for YES: " + message); // Output: Message for YES: 是
```

## Usage Example

Here's a complete example, including the enum definition:

```java
import com.ajaxjs.util.enums.EnumsUtil;
import com.ajaxjs.util.enums.IEnum;
import java.util.Optional;

public class Example {

    public enum TypeEnum implements IEnum<String, String> {
        YES("YES", "是"),
        NO("NO", "否");

        private final String code;
        private final String msg;

        TypeEnum(String code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        @Override
        public String getCode() {
            return this.code;
        }

        @Override
        public String getMsg() {
            return this.msg;
        }
    }

    public static void main(String[] args) {
        Optional<TypeEnum> type = EnumsUtil.of("YES", TypeEnum.class);
        if (type.isPresent()) {
            TypeEnum enumValue = type.get();
            System.out.println("Found enum: " + enumValue);
            System.out.println("Code: " + enumValue.getCode());
            System.out.println("Message: " + enumValue.getMsg());
        } else {
            System.out.println("Enum not found for code YES");
        }

        String message = EnumsUtil.ofMsg("NO", TypeEnum.class);
        System.out.println("Message for NO: " + message);
    }
}
```

## Unit Test Example

Here's the unit test code:

```java
package com.ajaxjs.util.enums;

import org.junit.jupiter.api.Test;

public class TestEnumsUtil {
    public enum TypeEnum implements IEnum<String, String> {
        YES("YES", "是"),
        NO("NO", "否");

        private final String code;
        private final String msg;

        TypeEnum(String code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        @Override
        public String getCode() {
            return this.code;
        }

        @Override
        public String getMsg() {
            return this.msg;
        }
    }

    @Test
    public void test() {
        System.out.println(EnumsUtil.ofMsg("YES", TypeEnum.class));
    }
}
```

## Conclusion

The `EnumsUtil` class provides a convenient way to work with enums that implement the `IEnum` interface, allowing you to easily retrieve enum instances and their associated values based on a code. This can be particularly useful in applications where you need to map codes to enum values and their corresponding messages.

**Important Note:** The search results may be incomplete. To view more results, you can use this link: [https://github.com/lightweight-component/aj-util/search?q=symbol%3AEnumsUtil](https://github.com/lightweight-component/aj-util/search?q=symbol%3AEnumsUtil) in the GitHub UI.