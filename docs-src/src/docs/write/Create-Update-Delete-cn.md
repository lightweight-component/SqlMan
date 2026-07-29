---
title: 数据的创建、更新和删除
subTitle: 参数化数据写入
description: 使用 SqlMan 执行参数化 INSERT、UPDATE、DELETE，并获取数据库生成的主键。
date: 2026-07-29
tags:
  - SqlMan
  - 数据写入
  - CRUD
layout: layouts/docs-cn.njk
---

# 数据的创建、更新和删除

## 插入记录

执行 `INSERT` 时使用 `create(params).execute(...)`：

```java
String sql = "INSERT INTO shop_address (name, address, phone) VALUES (?, ?, ?)";

CreateResult<Serializable> result =
        new Action(conn, sql)
                .create("公司", "Tree Road", "3412")
                .execute(false);
```

`isOk()` 表示是否插入成功。

## 获取数据库生成的主键

数据库生成主键时传入 `true`，同时指定期望的 Java 类型：

```java
CreateResult<Long> result =
        new Action(conn,
                "INSERT INTO shop_address (name, address) VALUES (?, ?)")
                .create("家", "Lake Road")
                .execute(true, Long.class);

Long id = result.getNewlyId();
```

SqlMan 会把 JDBC 返回的主键转换为指定数值类型，并拒绝溢出或丢失小数的转换。

## 更新记录

```java
UpdateResult result =
        new Action(conn,
                "UPDATE shop_address SET name = ? WHERE id = ?")
                .update("总公司", 8)
                .execute();

int affected = result.getEffectedRows();
```

只要 JDBC 执行过程没有异常，即使影响行数为 0，也会被视为执行成功。

## 按 ID 删除

```java
UpdateResult result =
        new Action(conn).delete("shop_address", "id", 8);
```

这是物理删除。逻辑删除应执行 `UPDATE`，修改相应的状态字段。

表名、列名和原始条件不能作为 JDBC 参数绑定。标识符和 SQL 片段必须由应用代码控制，不要直接拼接不可信的请求内容。
