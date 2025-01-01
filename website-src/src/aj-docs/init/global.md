---
title: 统一返回、统一异常
subTitle: 2024-12-05 by Frank Cheung
description: TODO
date: 2022-01-05
tags:
  - last one
layout: layouts/aj-docs.njk
---
# 统一返回、统一异常
# 统一返回结果
开发 REST API 接口时往往需要和前端对接定义返回的数据结构，例如所有返回 JSON 格式数据的接口都需要返回下面格式的数据。
```json
{
  "success": true,
  "code": null,
  "msg": "操作成功!",
  "data": 具体的数据
}
```
但是咱们系统中所有的接口返回值都是下面这种格式的，难道咱们要一个个去手动改一下接口的返回值么？
```java
@PostMapping("/submit")
boolean jsonSubmit(@RequestBody User user);

@GetMapping("/user")
User User();

@GetMapping("/user_desensitize")
User UserDesensitize();
```
Spring 为我们提供了更简单的方法，此时我们可以使用`RequestBodyAdvice`来拦截所有接口，将接口的返回值包装一下，包装为统一的格式返回，下面来看具体代码如何实现。

> 这里我们不主张使用`ResponseResultWrapper<T>`来包装返回值，而是“隐式”使用`ResponseResultWrapper`，没有泛型，不用出现在控制器上。控制器直接使用业务对象或者`boolean`、`String`等返回即可，这样代码显示更清爽，减少心智。
>
> 另外有人采用 AOP 实现拦截，笔者认为大可不必，`RequestBodyAdvice`本身就可拦截


```java
@RestControllerAdvice
@Component
public class GlobalResponseResult implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
//        System.out.println("supports:" + returnType);
        return true;
    }

    private static final String OK = "操作成功";

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        Method method = returnType.getMethod();
        assert method != null;

        if (method.isAnnotationPresent(Desensitize.class))
            body=  DeSensitize.acquire(body);

        if (method.isAnnotationPresent(IgnoredGlobalReturn.class))
            return body;

        ResponseResultWrapper responseResult = new ResponseResultWrapper();
        responseResult.setStatus(1);

        JsonMessage annotation = Objects.requireNonNull(returnType.getMethod()).getAnnotation(JsonMessage.class);

        if (annotation != null)
            responseResult.setMessage(annotation.value());
        else
            responseResult.setMessage(OK);

        responseResult.setData(body);

        return responseResult;
    }
}
```

可见代码不过寥寥数行，简单说就是把控制器所返回的结果（业务结果）嵌入到`ResponseResultWrapper`，连 JSON 序列化都不用（序列化在另外的地方统一处理）。

值得注意的是几个注解：

- `Desensitize` 表示该方法返回的结果要进行脱敏
- `IgnoredGlobalReturn` 忽略全局返回，直接返回业务对象
- `JsonMessage` 返回 JSON 结构时候，自定义 message 内容



# 统一异常处理
## 使用全局异常处理器
有些小伙伴，经常喜欢在 Controller、Service 代码中捕获异常。不管是普通异常 Exception，还是运行时异常 RuntimeException，都使用`try/catch`把它们捕获。
反例：

```java
try {
    checkParam(param);
} catch (BusinessException e) {
    return ApiResultUtil.error(1,"参数错误");
}
```

显然这种做法会造成大量重复的代码。我们在 Controller、Service 等业务代码中，尽可能少捕获异常。这种业务异常处理，应该交给拦截器统一处理。有了这个全局的异常处理器，之前我们在 Controller 或者 Service 中的`try/catch`代码可以去掉。

如果在接口中出现异常，全局的异常处理器`HandlerExceptionResolver`会帮我们封装结果，返回给用户。具体实现请看<span class="external-link">
        <span>↗</span>
    </span>[`GlobalExceptionHandler`](https://gitcode.com/zhangxin09/aj-framework/blob/master/aj-framework/src/main/java/com/ajaxjs/springboot/GlobalExceptionHandler.java)，非常简单。所有异常到跑到这里来打印，根据具体的异常返回特定的 HTTP Status Code，比如 `SecurityException`、`IllegalAccessError`、`IllegalAccessException` 返回 403。


## 优先使用标准异常
在 Java 中已经定义了许多比较常用的标准异常，比如下面这张图中列出的这些异常。

- `IllegalArgumentException` 入参不合法
- `IllegalStateException` 入参的状态不合法
- `UnsupportedOperationException` 不支持的操作
- `SecurityException` 安全异常 返回 401/403
- `NullPointerException` 空指针异常 返回 500

反例：
```java
public void checkValue(int value) {
    if (value < 0) {
        throw new MyIllegalArgumentException("值不能为负");
    }
}
```
自定义了一个异常表示参数错误。其实我们可以直接复用已有的标准异常。

正例：
```java
public void checkValue(int value) {
    if (value < 0) {
        throw new IllegalArgumentException("值不能为负");
    }
}
```
## 尽可能捕获具体异常

在你的业务逻辑方法中，有可能需要去处理多种不同的异常。 你可能你会觉得比较麻烦，而直接捕获`Exception`。

反例：

```java
try {
    doSomething();
} catch(Exception e) {
    log.error("doSomething处理失败，原因：",e);
}
```

这样捕获异常太笼统了。 其实`doSomething`方法中，会抛出`FileNotFoundException`和`IOException`。 这种情况我们最好捕获具体的异常，然后分别做处理。

正例：
 
```java
try {
   doSomething();
} catch(FileNotFoundException e) {
  log.error("doSomething处理失败，文件找不到，原因：",e);
} catch(IOException e) {
  log.error("doSomething处理失败，IO出现了异常，原因：",e);
}
```
这样如果后面出现了上面的异常，我们就非常方便知道是什么原因了。