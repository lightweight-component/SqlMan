---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util-cn.njk
---

# XmlHelper

`XmlHelper` 类提供了用于解析 XML 文档、检索元素以及将节点转换为映射的方法。这些方法可用于处理和操作 XML 数据。

## 方法

### 1. `initBuilder()`

初始化一个用于 XML 解析的 `DocumentBuilder`。

*   **返回值:** 一个 `DocumentBuilder` 实例。

**示例:**

```java
DocumentBuilder builder = XmlHelper.initBuilder();
// builder 将是一个新的 DocumentBuilder 实例
```

### 2. `xPath(String xml, String xpath, Consumer<Node> fn)`

使用 XPath 表达式从 XML 文档中检索节点，并使用给定的函数处理它们。

*   **参数说明：**
    *   `xml`: XML 文件的路径。
    *   `xpath`: 用于定位节点的 XPath 表达式。
    *   `fn`: 用于处理节点的函数。
*   **返回值:** 无。

**示例:**

```java
XmlHelper.xPath("path/to/xml/file.xml", "/root/element", node -> {
    // 处理节点
    System.out.println(node.getTextContent());
});
```

### 3. `parseXML(String xml, BiConsumer<Node, NodeList> fn)`

解析 XML 字符串，并使用给定的函数处理根元素及其子元素。

*   **参数说明：**
    *   `xml`: XML 内容的字符串。
    *   `fn`: 用于处理根元素及其子元素的函数。
*   **返回值:** 无。

**示例:**

```java
String xmlContent = "<root><child>Content</child></root>";
XmlHelper.parseXML(xmlContent, (node, nodeList) -> {
    // 处理根元素及其子元素
    System.out.println(node.getNodeName());
    for (int i = 0; i < nodeList.getLength(); i++) {
        System.out.println(nodeList.item(i).getTextContent());
    }
});
```

### 4. `getRoot(String xml)`

检索 XML 字符串的根元素。

*   **参数说明：**
    *   `xml`: XML 内容的字符串。
*   **返回值:** 根元素。

**示例:**

```java
String xmlContent = "<root><child>Content</child></root>";
Element root = XmlHelper.getRoot(xmlContent);
// root 将是 <root> 元素
```

### 5. `nodeAsMap(String xml, String xpath)`

将节点的属性转换为映射。

*   **参数说明：**
    *   `xml`: XML 文件的路径。
    *   `xpath`: 用于定位节点的 XPath 表达式。
*   **返回值:** 节点属性的映射。

**示例:**

```java
Map<String, String> attributes = XmlHelper.nodeAsMap("path/to/xml/file.xml", "/root/element");
// attributes 将包含 <element> 节点的属性
```