---
title: API 安全
subTitle: 2024-12-05 by Frank Cheung
description: TODO
date: 2022-01-05
tags:
  - last one
layout: layouts/aj-docs.njk
---
# API 安全
为了安全性需要对接口的数据进行加密处理，不能明文暴露数据。对于接口的行为，分别有：

- 入参，对传过来的加密参数解密。接口处理客户端提交的参数时候，这里统一约定对 HTTP Raw Body 提交的数据（已加密的密文），转换为 JSON 处理，这是最常见的提交方式。其他 QueryString、标准 Form、HTTP Header 的入参则不支持。
- 出参，对返回值进行加密。接口统一返回加密后的 JSON 结果。

有人把加密结果原文输出，如下图所示：

![](/asset/aj-docs/api-encode.png)
但笔者觉得那是一种反模式，而保留原有 JSON 结构更好，如下提交的 JSON。

```json
{
    "errCode": "0",
    "data": "BQduoGH4PI+6jxgu+6S2FWu5c/vHd+041ITnCH9JulUKpPX8BvRTvBNYfP7……"
}
```

另外也符合既有的统一返回结果，即把`data`数据加密，其他`code`、`msg`等的正常显示。

## 加密算法

加密算法需要调用方（如浏览器）与 API 接口协商好。一般采用 RSA 加密算法。虽然 RSA 没 AES 速度高，但胜在是非对称加密，AES 这种对称加密机制在这场合就不适用了（因为浏览器是不能放置任何密钥的，——除非放置非对称的公钥）。

当然，如果你设计的 API 接口给其他第三方调用而不是浏览器，可以保证密钥安全的话，那么使用 AES 也可以，包括其他摘要算法同理亦可，大家商定好算法（md5/sha1/sha256……）和盐值（Slat）即可。

该组件当前仅支持 RSA（1024bit key）。下面更多的算法在路上。

- RSA（512/2048……）
- AES
- MD5/SHA1/SHA256…… with Slat

# 使用方式
首先观察这个 Spring MVC 接口声明，与一般的 JSON 提交数据方式无异，仍然需要注解`@RequestBody`：

```
@PostMapping("/submit")
boolean jsonSubmit(@RequestBody User user);
```

重点是 User 这个 DTO，为了标明是加密数据，需要在这个 Bean 上声明我们自定义的注解`@EncryptedData`：

```java
package com.ajaxjs.api.encryptedbody;

@EncryptedData
public class User {
    private String name;
    private int age;

    // Getters and Setters
}
```

同时我们提交的对象不再是 User 的 JSON，而是`DecodeDTO`（虽然最终转换为`User`，成功解密的话），即:

```java
package com.ajaxjs.api.encryptedbody;

import lombok.Data;

@Data
public class DecodeDTO {
    /**
     * Encrypted data
     */
    private String data;
}
```
当然你可以修改这个 DTO 为你符合的结构。提交的样子就是像:

```json
{
    "data": "BQduoGH4PI+6jxgu+6S2FWu5c/vHd+041ITnCH9JulUKpPX8BvRTvBNYfP7……"
}
```

这个加密过的密文怎么来的？当然是你客户端加密后的结果。

## 初始化
在 YAML 配置中加入：
```yaml
api:
  EncryptedBody:
    privateKey: MIICdgIBADANBgkqhkiG9w0BAQ……
```
主要是 RSA 的私钥。然后在 Spring 配置类中加入：
```java
@Value("${api.EncryptedBody.privateKey}")
private String apiPrivateKey;

@Override
public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(0, new EncryptedBodyConverter(apiPrivateKey));
}
```