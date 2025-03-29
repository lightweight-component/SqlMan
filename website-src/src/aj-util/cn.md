---
title: Docs Home
subTitle: 2024-12-05 by Frank Cheung
description: TODO
date: 2022-01-05
tags:
  - last one
layout: layouts/aj-util-cn.njk
---

# AJ Utilities 简介

小型、干净、简单的 Java 工具库。JAR 包体积大小约 60kb。它包含下面的模块。

| Class/Package 模块    | Detail 说明                     | Memo 备注 |
|---------------------|-------------------------------|---------|
| BytesHelper         | 字节数组工具类                       |         |
| CollUtils           | 集合工具类                         |         |
| ConvertBasicValue   | 尝试转换目标类型，注意并不是所有的类型都可以进行转换    |         |
| DateHelper          | 日期工具类                         |         |
| EncodeTools         | 字符串 URL/Base64 编码、解码     |         |
| MessageDigestHelper | MD5/SHA1/SHA256/384/512 加密工具类 |         |
| ObjectHelper        | Java Object 工具类     |         |
| HTTP Request         | 小型的 HTTP 请求工具 Component                                                         |                                           |
| RandomTools         | 随机数或随机字符串工具类   |         |
| RegExpUtils         | 正则表达式工具类                      |         |
| StrUtil             | 字符串工具类                        |         |
| JsonUtil             | 对 Jackson JSON 库的封装，json、map、bean、list 之间的转换16种方法 | Jackson 是唯一的依赖|
| XmlHelper           | XML 处理工具类                     |         |
| Cryptography        | AES/RSA 加密解密包                 |         |
| IO                  | 文件、资源、流工具包                    |         |
| Reflection             | 反射工具包                         |         |

## 源代码

[Github](https://github.com/lightweight-component/aj-util) | [GitCode](https://gitcode.com/lightweight-component/aj-util) | [Java 文档](https://javadoc.io/doc/com.ajaxjs/ajaxjs-util) 


## 安装
需要 Java 8+ 或以上。Maven 坐标：

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>ajaxjs-util</artifactId>
    <version>1.2.2</version>
</dependency>
```

[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/ajaxjs-util?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/ajaxjs-util)