---
title: Docs Home
subTitle: 2024-12-05 by Frank Cheung
description: TODO
date: 2022-01-05
tags:
  - last one
layout: layouts/aj-util.njk
---

# AJ Utilities

Small Java Utils Library with many powerful components. The size of jar is around 90kb. It contains the following modules:

| Class/Package Module | Detail Description                                                                     | Memo                                      |
|----------------------|----------------------------------------------------------------------------------------|-------------------------------------------|
| BytesHelper          | Byte array utility class                                                               |                                           |
| CollUtils            | Collection utility class                                                               |                                           |
| ConvertBasicValue    | Attempts to convert target type, note that not all types can be converted              |                                           |
| DateHelper           | Date utility class                                                                     |                                           |
| EncodeTools          | String URL/Base64 encoder                                                              |                                           |
| MessageDigestHelper  | MD5/SHA1/SHA256/384/512 encryption utility class                                       |                                           |
| ObjectHelper         | A helper for Java Object                                                               |                                           |
| HTTP Request         | A Small HTTP Request Component                                                         |                                           |
| RandomTools          | Random Numbers and Strings                                                             |                                           |
| RegExpUtils          | Regular expression utility class                                                       |                                           |
| StrUtil              | String utility class                                                                   |                                           |
| JsonUtil             | Encapsulation of Jackson Library: Conversion Methods Between JSON, Map, Bean, and List | Jackson is only library that dependencies |
| XmlHelper            | XML processing utility class                                                           |                                           |
| Cryptography         | AES/RSA encryption and decryption package                                              |                                           |
| IO                   | File, resource, stream utility package                                                 |                                           |
| Reflection           | Reflection utility package                                                             |                                           |

## Source code

[Github](https://github.com/lightweight-component/aj-util) | [GitCode](https://gitcode.com/lightweight-component/aj-util) | [Java Documents](https://javadoc.io/doc/com.ajaxjs/ajaxjs-util) 

 
## Install

Runs on Java 8+. Maven:

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>ajaxjs-util</artifactId>
    <version>1.2.2</version>
</dependency>
```

[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/ajaxjs-util?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/ajaxjs-util)