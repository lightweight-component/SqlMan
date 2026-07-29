---
title: 查询基础概念
subTitle: Action、Query 与返回类型
description: 了解 SqlMan 的 Action、Query 对象，以及 Map、标量和 JavaBean 查询结果。
date: 2026-07-29
tags:
  - SqlMan
  - 查询
  - JDBC
layout: layouts/docs-cn.njk
---

# 查询基础概念

SqlMan 始终保留可见的 SQL。`Action` 保存连接、SQL、参数和数据库类型；调用 `query(...)` 后得到 `Query`，由它执行语句并映射结果。

```java
Query query = new Action(
        conn,
        "SELECT id, name FROM shop_address WHERE stat = ?"
).query(1);
```

## 单行结果

把第一行返回为 `Map`：

```java
Map<String, Object> row = query.one();
```

把第一行返回为 JavaBean：

```java
Address address = query.one(Address.class);
```

查询不到记录时，这两个方法都返回 `null`。

## 单值结果

只有一列的查询可以使用 `oneValue(...)`：

```java
Integer total = new Action(
        conn,
        "SELECT COUNT(*) FROM shop_address WHERE stat = ?"
).query(1).oneValue(Integer.class);
```

## 多行结果

```java
List<Map<String, Object>> rows =
        new Action(conn, "SELECT * FROM shop_address ORDER BY id")
                .query()
                .list();

List<Address> addresses =
        new Action(conn, "SELECT * FROM shop_address ORDER BY id")
                .query()
                .list(Address.class);
```

当前多行查询没有记录时返回 `null`；需要集合语义的调用方可以统一转换为 `Collections.emptyList()`。

## 资源管理

`Query` 会关闭 `PreparedStatement` 和 `ResultSet`。`Connection` 仍由调用方负责关闭或归还连接池。
