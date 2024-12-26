---
title: Quick Start
subTitle: 2024-12-05 by Frank Cheung
description: TODO
date: 2022-01-05
tags:
  - last one
layout: layouts/docs.njk
---
# Quick Start
## 🔧 Install SqlMan
To get started, we just have to include the one SqlMan module in our dependencies:

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>sqlman</artifactId>
    <version>1.0</version>
</dependency>
```

Over the course of this article, we’ll show examples using the HSQL database:
```xml
<dependency>
    <groupId>org.hsqldb</groupId>
    <artifactId>hsqldb</artifactId>
    <version>2.4.0</version>
    <scope>test</scope>
</dependency>
```
We can find the latest version of SqlMan on [Maven Central]().

> About Java Version
>
> Currently, SqlMan only supports **Java 11** and above. However, we are aware that there is a significant user base still using JDK 8. Should the need arise, we are committed to maintaining compatibility with Java 8 by making a few modifications to the code.