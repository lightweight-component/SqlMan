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

如果第一个参数是 `Map`，且 SQL 中包含动态节点或命名占位符，`Action` 会先渲染内联 SQL 模板，再绑定剩余的位置参数：

### 命名数据参数 `#{}`

`#{name}` 用于 Map 中的普通数据值。它会生成 JDBC `?` 占位符，参数值按其在 SQL 中的出现顺序绑定；因此可安全用于查询字符串、表单或 JSON 请求体中的字段：

```java
Map<String, Object> params = new HashMap<>();
params.put("id", 1);
params.put("name", "Alice");

Map<String, Object> row =
        new Action(conn, "SELECT * FROM shop_address WHERE id = #{id} AND name = #{name}")
                .query(params)
                .one();
```

上例执行的 JDBC SQL 等价于 `SELECT * FROM shop_address WHERE id = ? AND name = ?`。`#{}` 可用于 `SELECT`、`INSERT`、`UPDATE` 和 `DELETE` 中的值；不要给占位符手动加引号。

### 标识符参数 `${}`

```java
Map<String, Object> template = new HashMap<>();
template.put("tableName", "shop_address");

Map<String, Object> row =
        new Action(conn, "SELECT * FROM ${tableName} WHERE id = ?")
                .query(template, 1)
                .one();
```

`${...}` 仅允许 SQL 标识符，包括 `schema.table` 这样的限定名称；任意 SQL 片段会被拒绝。它不能用于普通请求值，例如 `WHERE id = ${id}` 中的 `id=1` 会被拒绝，应写为 `WHERE id = #{id}`。表名、列名、排序字段和排序方向属于 SQL 结构，不能使用 JDBC 参数绑定；即使标识符语法通过校验，也必须由服务端固定或通过白名单和授权控制。

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
