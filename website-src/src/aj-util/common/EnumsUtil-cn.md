---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util-cn.njk
---

# EnumsUtil 

当枚举实现 `IEnum` 接口时，`EnumsUtil` 类提供了一种基于代码检索枚举实例及其关联消息（或其他值）的方法。 当您拥有每个常量都有一个代码和相应描述性消息的枚举，并且需要通过其代码查找枚举时，这尤其有用。

## `IEnum` 接口

在深入了解 `EnumsUtil` 之前，让我们了解 `IEnum` 接口。 与 `EnumsUtil` 一起使用的枚举应实现此接口：

```java
package com.ajaxjs.util.enums;

public interface IEnum<E, V> {
    E getCode();
    V getMsg();
}
```

其中：

*   `E` 是代码的类型（例如，`String`，`Integer`）。
*   `V` 是消息（或关联值）的类型。

## 方法

### 1. `of(E code, Class<T> clz)`

检索包含与给定代码匹配的枚举实例的 `Optional<T>`。

*   **参数：**
    *   `code`：要搜索的代码。
    *   `clz`：表示枚举类型的 `Class` 对象。
*   **返回值：** 如果找到，则返回包含枚举实例的 `Optional<T>`；如果未找到匹配的枚举，则返回 `Optional.empty()`。

**示例：**

```java
Optional<TypeEnum> type = EnumsUtil.of("YES", TypeEnum.class);
if (type.isPresent()) {
    TypeEnum enumValue = type.get();
    System.out.println("找到枚举： " + enumValue);
} else {
    System.out.println("未找到代码 YES 的枚举");
}
```

### 2. `ofMsg(E code, Class<T> cla)`

检索与给定代码匹配的枚举实例的消息（或关联值）。

*   **参数：**
    *   `code`：要搜索的代码。
    *   `cla`：表示枚举类型的 `Class` 对象。
*   **返回值：** 枚举实例的消息（或关联值）；如果未找到匹配的枚举，则返回 `null`。

**示例：**

```java
String message = EnumsUtil.ofMsg("YES", TypeEnum.class);
System.out.println("YES 的消息： " + message); // 输出：YES 的消息： 是
```

## 使用示例

这是一个完整的示例，包括枚举定义：

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
            System.out.println("找到枚举： " + enumValue);
            System.out.println("代码： " + enumValue.getCode());
            System.out.println("消息： " + enumValue.getMsg());
        } else {
            System.out.println("未找到代码 YES 的枚举");
        }

        String message = EnumsUtil.ofMsg("NO", TypeEnum.class);
        System.out.println("NO 的消息： " + message);
    }
}
```

## 单元测试示例

这是单元测试代码：

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

## 结论

`EnumsUtil` 类提供了一种方便的方式来处理实现 `IEnum` 接口的枚举，从而允许您基于代码轻松检索枚举实例及其关联值。 这在需要将代码映射到枚举值及其相应消息的应用程序中尤其有用。

**重要提示：** 搜索结果可能不完整。 要查看更多结果，您可以使用此链接：[https://github.com/lightweight-component/aj-util/search?q=symbol%3AEnumsUtil](https://github.com/lightweight-component/aj-util/search?q=symbol%3AEnumsUtil) 在 GitHub UI 中。