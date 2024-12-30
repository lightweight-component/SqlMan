---
title: 实体类字段脱敏
subTitle: 2024-12-05 by Frank Cheung
description: TODO
date: 2022-01-05
tags:
  - last one
layout: layouts/aj-docs.njk
---
# 实体类字段脱敏

脱敏就是现实某些敏感的字段完全暴露数据，但又不能完全消去，保留一部分信息即可判断，常见如姓名、手机、邮箱、用户名、密码等字段。

考虑到穿梭于 Java 的实体要么是 Java Bean 要么就是 Map，针对实体数据处理即可。接着把控好在哪里调用这个脱敏组件，比如 REST API 的在返回实体之前处理就好；而 RPC 的又不一样。

脱敏实现方式也不难，本质上只是一个简单的字符串替换函数即可。但围绕实体字段各种的情况，考虑得就比较多了。

# 使用方式
## 源码
从源码空间直接 copy 代码接口，没有其他依赖。

[https://gitcode.com/zhangxin09/aj-framework/tree/master/aj-framework/src/main/java/com/ajaxjs/desensitize](https://gitcode.com/zhangxin09/aj-framework/tree/master/aj-framework/src/main/java/com/ajaxjs/desensitize)

很简单的几个类，轻量级为目标。


## 定义实体注解

```java
import com.ajaxjs.desensitize.DesensitizeType;
import com.ajaxjs.desensitize.annotation.DesensitizeModel;
import com.ajaxjs.desensitize.annotation.DesensitizeProperty;
import lombok.Data;

@Data
@DesensitizeModel
public class User {
    private String name;

    @DesensitizeProperty(DesensitizeType.PHONE)
    private String phone;

    private int age;
}
```
上例使用了 `@DesensitizeModel` 表示该 POJO 要脱敏；`@DesensitizeProperty(DesensitizeType.PHONE)`说明要脱敏的字段，以及是“手机”的类型。其他更多的类型参见枚举：


```java
/**
 * 脱敏类型
 */
public enum DesensitizeType {
    DEFAULT(v -> DataMask.PLACE_HOLDER),
    // 手机号
    PHONE(DataMask::maskPhoneNumber),
    // 银行卡号
    BANK_CARD(DataMask::maskBankCard),
    // 身份证号
    ID_CARD(DataMask::maskIdCard),
    // 姓名
    USERNAME(DataMask::maskChineseName),
    // email
    EMAIL(DataMask::maskEmail),
    //地址
    ADDRESS(v -> DataMask.maskAddress(v, 0));

    public final Function<String, String> handler;

    DesensitizeType(Function<String, String> handler) {
        this.handler = handler;
    }
}

```
手动执行脱敏：`DeSensitize.acquire(body);`。
## 定义控制器的注解
使用`@Desensitize`定义在控制器方法上。
```java
@GetMapping("/user_desensitize")
@Desensitize
public User UserDesensitize() {
    User user = new User();
    user.setAge(1);
    user.setName("tom");
    user.setPhone("13711118120");

    return user;
}
```

在统一返回的`ResponseBodyAdvice`上，增加一行判断。
```java
@RestControllerAdvice
@Component
public class GlobalResponseResult implements ResponseBodyAdvice<Object> {

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        Method method = returnType.getMethod();
        assert method != null;

        if (method.isAnnotationPresent(Desensitize.class)) // 判断要执行脱敏
            body = DeSensitize.acquire(body);

        ResponseResultWrapper responseResult = new ResponseResultWrapper();
        responseResult.setStatus(1);
        responseResult.setData(body);

        return responseResult;
    }
}
```
返回结果如：
```json
{
    "status": 1,
    "errorCode": null,
    "message": "操作成功",
    "data": {
        "phone": "137*****8120",
        "name": "tom",
        "age": 1
    }
}
```

# 类说明
- DeSensitizeUtils：这个类对实体进行脱敏后返回的是原来的实体对象，它直接在原始对象上进行操作，并对其进行修改。
- SensitizeUtils：而这个类则创建了一个新的对象实例（或集合），并在这个新对象上应用脱敏规则。这意味着原对象保持不变，而返回的是一个结构相同但值被脱敏处理过的新对象。

# 同类开源

- https://gitee.com/strong_sea/sensitive-plus https://www.cnblogs.com/nuccch/p/18148298 使用了`MappingJackson2HttpMessageConverter`这点不错，同时也比较全面，还支持日志脱敏，可是代码组织太分散了
- https://github.com/chenqi92/alltobs-desensitization-all
- https://github.com/mingyang66/spring-parent/tree/master/emily-project/oceansky-desensitize 代码简洁清晰
- https://gitee.com/l0km/beanfilter 大神作品，功能全面，包括 RPC 的

更复杂的参考这个[《大数据隐私保护关键技术解析：数据脱敏、匿名化、差分隐私和同态加密》](https://www.secrss.com/articles/13856)。