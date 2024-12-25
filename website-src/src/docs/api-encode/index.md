---
title: API 加密/解密
subTitle: 2024-12-05 by Frank Cheung
description: TODO
date: 2022-01-05
tags:
  - last one
layout: layouts/docs.njk
---
# API 加密/解密
为了安全性需要对接口的数据进行加密处理。一般采用 RSA 加密算法。虽然 RSA 没 AES 速度高，但胜在是非对称加密，AES 这种对称加密机制在这场合就不适用了（因为浏览器是不能放置任何密钥的，——除非非对称的公钥）。

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