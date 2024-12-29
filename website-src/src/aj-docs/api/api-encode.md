---
title: API 加密/解密
subTitle: 2024-12-05 by Frank Cheung
description: TODO
date: 2022-01-05
tags:
  - last one
layout: layouts/aj-docs.njk
---
# API 接口加密/解密
为了安全性需要对接口的数据进行加密处理，不能明文暴露数据。为此应该对接口进行加密/解密处理，对于接口的行为，分别有：

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

系统要求：只支持 Spring + Jackson 的方案。

## 加密算法

加密算法需要调用方（如浏览器）与 API 接口协商好。一般采用 RSA 加密算法。虽然 RSA 没 AES 速度高，但胜在是非对称加密，AES 这种对称加密机制在这场合就不适用了（因为浏览器是不能放置任何密钥的，——除非放置非对称的公钥）。

当然，如果你设计的 API 接口给其他第三方调用而不是浏览器，可以保证密钥安全的话，那么使用 AES 也可以，包括其他摘要算法同理亦可，大家商定好算法（md5/sha1/sha256……）和盐值（Slat）即可。

该组件当前仅支持 RSA（1024bit key）。下面更多的算法在路上。

- RSA（512/2048……）
- AES
- MD5/SHA1/SHA256…… with Slat

# 使用方式


## 初始化
在 YAML 配置中加入：
```yaml
api:
  EncryptedBody:
    enable: true
    publicKey: MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCmkKluNutOWGmAK2U……
    privateKey: MIICdgIBADANBgkqhkiG9w0BAQ……
```
主要是 RSA 的公钥/私钥。然后在 Spring 配置类`WebMvcConfigurer`中加入：
```java
@Value("${api.EncryptedBody.publicKey}")
private String apiPublicKey;

@Value("${api.EncryptedBody.privateKey}")
private String apiPrivateKey;

@Value("${api.EncryptedBody.enable}")
private boolean apiEncryptedBodyEnable;

@Override
public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    EncryptedBodyConverter converter = new EncryptedBodyConverter(apiPublicKey, apiPrivateKey);
    converter.setEnabled(apiEncryptedBodyEnable);

    converters.add(0, converter);
}
```
## 配置要加密的数据
使用方式很简单，其实就是添加一个 Java 注解`@EncryptedData`到你的 Java Bean 上即可。

不过我们还是按照正儿八经的循序渐进的方式去看看。首先是解密请求的数据，我们观察这个 Spring MVC 接口声明，与一般的 JSON 提交数据方式无异，添加了注解`@RequestBody`，其他无须修改：

```java
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

这个加密过的密文怎么来的？当然是你客户端加密后的结果。或者从下面小节说的方式，返回一段密文。

## 返回加密的数据

下面 Controller 方法返回一个 User 对象，没有任何修改。

```java
@GetMapping("/user")
User User();

……

@Override
public User User() {
    User user = new User();
    user.setAge(1);
    user.setName("tom");
    
    return user;
}
```

我们同样需要加一个注解`@EncryptedData`即可对其加密。当前版本中暂不支持字段级别的加密，只支持整个对象加密。

返回结果如下：
```json
{
    "status": 1,
    "errorCode": null,
    "message": "操作成功",
    "data": "ReSSPC34JE+O/SmLCxE5zVJb6D2tzp1f5pfQyKdjvOWkQQ+qDjcjw/2m/KPA+2+uc9kseqFryXNPIZCEfsaOCJAqzMtrXyZ0JPB1skeJxKOngS5USijsY0UZqN9hLS3O/7CBLlSGkEuyXZV//WcWDG9BpQ4TAKrlRfwM4bnCo+E="
}
```

## 添加依赖
哦~对了，别忘了添加依赖，——没单独搞 jar 包，直接 copy 代码吧~才三个类：[源码](https://gitcode.com/zhangxin09/aj-framework/tree/master/aj-framework/src/main/java/com/ajaxjs/api/encryptedbody)。

其中`ResponseResultWrapper`就是统一返回结果的类，你可以改为你项目的，——其他的没啥依赖了，——还有就是 RSA 依赖我的工具包：

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>ajaxjs-util</artifactId>
    <version>1.1.8</version>
</dependency>
```
很小巧的，才60kb 的 jar 包——请放心食用~

# 实现方式
这里说说实现原理，以及一些 API 设计风格的思考。

我们这种的用法，相当于接收了 A 对象（加密的，`DecodeDTO`），转换为 B 对象（解密的，供控制器使用）。最简单的方式就是这样的：

```java
@PostMapping("/submit")
boolean jsonSubmit(@RequestBody DecodeDTO dto) {
    User user = 转换函数(dto.getData());
}
```

但是这种方法，方法数量一多则遍地`DecodeDTO`，API 文档也没法写了（破坏了代码清晰度，不能反映原来代码的意图）。为此我们应该尽量采用“非入侵”的方法，所谓非入侵，就是不修改原有的代码，只做额外的“装饰”。这种手段有很多，典型如 AOP，其他同类的开源库 [rsa-encrypt-body-spring-boot](https://github.com/ishuibo/rsa-encrypt-body-spring-boot)、[encrypt-body-spring-boot-starter](https://github.com/Licoy/encrypt-body-spring-boot-starter) 也是不约而同地使用 AOP。

然而笔者个人来说不太喜欢 AOP，可能也是不够熟悉吧——反正能不用则不用。如果不用 AOP 那应该如何做呢？笔者思考了几种方式例如 Filter、拦截器等，但最终把这个问题定位于 JSON 序列化/反序列化层面上，在执行这一步骤之前就可以做加密/解密操作了。开始以为可以修改 Jackson 全局序列化方式，但碍于全局的话感觉不太合理，更合适的是在介乎于 Spring 与 Jackson 结合的地方做修改。于是有了在的`MappingJackson2HttpMessageConverter`基础上扩展的 `EncryptedBodyConverter`，重写了`read`方法，在反序列化之前先做解密操作，`writeInternal`方法亦然。


核心方法就一个类，不足一百行代码：

```java
import com.ajaxjs.springboot.ResponseResultWrapper;
import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.cryptography.RsaCrypto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;

public class EncryptedBodyConverter extends MappingJackson2HttpMessageConverter {
    public EncryptedBodyConverter(String publicKey, String privateKey) {
        super();
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    private final String publicKey;

    private final String privateKey;

    /**
     * 使用私钥解密字符串
     *
     * @param encryptBody 经过 Base64 编码的加密字符串
     * @param privateKey  私钥字符串，用于解密
     * @return 解密后的字符串
     */
    static String decrypt(String encryptBody, String privateKey) {
        byte[] data = EncodeTools.base64Decode(encryptBody);

        return new String(RsaCrypto.decryptByPrivateKey(data, privateKey));
    }

    /**
     * 使用公钥加密字符串
     * <p>
     * 该方法采用RSA加密算法，使用给定的公钥对一段字符串进行加密
     * 加密后的字节数组被转换为 Base64 编码的字符串，以便于传输和存储
     *
     * @param body      需要加密的原始字符串
     * @param publicKey 用于加密的公钥字符串
     * @return 加密后的 Base64 编码字符串
     */
    static String encrypt(String body, String publicKey) {
        byte[] encWord = RsaCrypto.encryptByPublicKey(body.getBytes(), publicKey);
        return EncodeTools.base64EncodeToString(encWord);
    }

    /**
     * 重写 read 方法以支持加密数据的读取
     *
     * @param type         数据类型，用于确定返回对象的类型
     * @param contextClass 上下文类，未在本方法中使用
     * @param inputMessage 包含加密数据的 HTTP 输入消息
     * @return 根据类型参数反序列化后的对象实例
     * @throws IOException                     如果读取或解析过程中发生 I/O 错误
     * @throws HttpMessageNotReadableException 如果消息无法解析为对象实例
     */
    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        Class<?> clz = (Class<?>) type;

        if (clz.getAnnotation(EncryptedData.class) != null) {
            ObjectMapper objectMapper = getObjectMapper();
            DecodeDTO decodeDTO = objectMapper.readValue(inputMessage.getBody(), DecodeDTO.class);
            String encryptBody = decodeDTO.getData();

            String decodeJson = decrypt(encryptBody, privateKey);

            return objectMapper.readValue(decodeJson, clz);
        }

        return super.read(type, contextClass, inputMessage);
    }

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        Class<?> clz = (Class<?>) type;

        if (object instanceof ResponseResultWrapper && clz.getAnnotation(EncryptedData.class) != null) {
            ResponseResultWrapper response = (ResponseResultWrapper) object;
            Object data = response.getData();
            String json = getObjectMapper().writeValueAsString(data);
            String encryptBody = encrypt(json, publicKey);

            response.setData(encryptBody);
        }

        super.writeInternal(object, type, outputMessage);
    }
}
```

TODO
- 增加加密解密算法
- 增加一个加密选项，说明使用公钥还是私钥。当前是公钥
- 针对字段单独的解密


哈哈 最后发现也有人用`HttpMessageConverter`来做：https://blog.allbs.cn/posts/49043/。