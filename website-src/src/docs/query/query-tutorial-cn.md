---
title: 查询教程
subTitle: 2024-12-05 by Frank Cheung
description: TODO
date: 2022-01-05
tags:
  - last one
layout: layouts/docs-cn.njk
---
# 查询教程
## 绑定参数

查询语句通常包含固定部分和参数化部分。这样做有几个优点：

- 安全性：通过避免字符串拼接，可以防止 SQL 注入
- 便利性：我们不需要记住复杂数据类型（如时间戳）的确切语法
- 性能：查询的静态部分只需解析一次并可以被缓存

SqlMan 同时支持位置参数和命名参数。

我们在查询或语句中使用问号来插入位置参数：

```java
Map<String, Object> result = new Sql(conn).input("SELECT * FROM shop_address WHERE id = ?", 1).query();
```
这与我们在经典 JDBC 查询中使用预处理语句的方式相同。参数可以多个。

命名参数则是以 ${ 开头，后面跟着参数名，以 } 结尾：

```java
Map<String, Object> result；
result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = #{stat}", mapOf("tableName", "shop_address", "stat", 1)).query();
assertNotNull(result);

result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = ?", mapOf("tableName", "shop_address", "abc", 2), 1).query();
assertNotNull(result);
```

这允许我们使用 `Map` 对象一次绑定多个命名参数。

## 返回 Java Bean
有时候，我们需要返回一个 Java Bean 而不是`Map`。SqlMan 提供了一个简单的方法来实现这一点，只需要将 Java Bean 类作为查询方法的参数传入：

```java
Address result = new Sql(conn).input("SELECT * FROM shop_address").query(Address.class); 
List<Address> results = new Sql(conn).input("SELECT * FROM shop_address").queryList(Address.class);
 ```
```

最后，我们可以将查询结果行映射到一个 bean 或其他自定义类。