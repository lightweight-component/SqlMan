---
title: 查询教程
subTitle: 参数绑定与结果映射
description: 使用位置参数、命名 SQL 模板、Map 和 JavaBean 查询数据库。
date: 2026-07-29
tags:
  - SqlMan
  - 查询教程
  - 预编译参数
layout: layouts/docs-cn.njk
---

# 查询教程

## 绑定位置参数

数据值使用 `?`，并按照占位符顺序传入参数：

```java
Map<String, Object> row =
        new Action(conn, "SELECT * FROM shop_address WHERE id = ? AND stat = ?")
                .query(1, 0)
                .one();
```

这些值由 `PreparedStatement` 绑定，不要给 `?` 添加引号。

## SQL 模板参数

如果第一个参数是 `Map`，`Action` 会先交给 `SmallMyBatis` 处理，再绑定剩余的位置参数：

```java
Map<String, Object> template = new HashMap<>();
template.put("tableName", "shop_address");

Map<String, Object> row =
        new Action(conn, "SELECT * FROM ${tableName} WHERE id = ?")
                .query(template, 1)
                .one();
```

当前实现中的 `${...}` 和 `#{...}` 都属于文本替换，并不是 JDBC 参数绑定。`${...}` 只能用于可信的表名、列名或 SQL 片段；数据值应优先使用 `?`。

## Map 的列名

Map 使用 JDBC 返回的列标签。表达式应设置别名，以获得稳定的键名：

```java
Map<String, Object> totals =
        new Action(conn, "SELECT COUNT(*) AS total FROM shop_address")
                .query()
                .one();

Object total = totals.get("total");
```

## 映射 JavaBean

目标类型需要无参构造方法和可写属性：

```java
Address address =
        new Action(conn, "SELECT id, name, create_date FROM shop_address WHERE id = ?")
                .query(1)
                .one(Address.class);
```

SqlMan 会把 `create_date` 这样的下划线列名转换为 `createDate` 属性名。
