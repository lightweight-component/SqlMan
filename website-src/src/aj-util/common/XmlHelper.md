---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util.njk
---
# XmlHelper
The `XmlHelper` class provides methods for parsing XML documents, retrieving elements, and converting nodes to maps. These methods can be used for processing and manipulating XML data.

## Methods

### 1. `initBuilder()`

Initializes a `DocumentBuilder` for XML parsing.

*   **Returns:** A `DocumentBuilder` instance.

**Example:**

```java
DocumentBuilder builder = XmlHelper.initBuilder();
// builder will be a new DocumentBuilder instance
```

### 2. `xPath(String xml, String xpath, Consumer<Node> fn)`

Retrieves nodes from an XML document using an XPath expression and processes them with a given function.

*   **Parameters:**
    *   `xml`: The path to the XML file.
    *   `xpath`: The XPath expression to locate nodes.
    *   `fn`: A function to process the nodes.
*   **Returns:** None.

**Example:**

```java
XmlHelper.xPath("path/to/xml/file.xml", "/root/element", node -> {
    // Process the node
    System.out.println(node.getTextContent());
});
```

### 3. `parseXML(String xml, BiConsumer<Node, NodeList> fn)`

Parses an XML string and processes the root element and its children with a given function.

*   **Parameters:**
    *   `xml`: The XML content as a string.
    *   `fn`: A function to process the root element and its children.
*   **Returns:** None.

**Example:**

```java
String xmlContent = "<root><child>Content</child></root>";
XmlHelper.parseXML(xmlContent, (node, nodeList) -> {
    // Process the root element and its children
    System.out.println(node.getNodeName());
    for (int i = 0; i < nodeList.getLength(); i++) {
        System.out.println(nodeList.item(i).getTextContent());
    }
});
```

### 4. `getRoot(String xml)`

Retrieves the root element of an XML string.

*   **Parameters:**
    *   `xml`: The XML content as a string.
*   **Returns:** The root element.

**Example:**

```java
String xmlContent = "<root><child>Content</child></root>";
Element root = XmlHelper.getRoot(xmlContent);
// root will be the <root> element
```

### 5. `nodeAsMap(String xml, String xpath)`

Converts the attributes of a node to a map.

*   **Parameters:**
    *   `xml`: The path to the XML file.
    *   `xpath`: The XPath expression to locate the node.
*   **Returns:** A map of the node's attributes.

**Example:**

```java
Map<String, String> attributes = XmlHelper.nodeAsMap("path/to/xml/file.xml", "/root/element");
// attributes will contain the attributes of the <element> node
```