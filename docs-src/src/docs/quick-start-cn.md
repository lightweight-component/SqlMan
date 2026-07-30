---
title: 快速开始
subTitle: SqlMan 2.0
description: 安装 SqlMan 2.0、连接数据库并执行第一个参数化 JDBC 查询。
date: 2026-07-29
tags:
  - SqlMan
  - 快速开始
  - JDBC
layout: layouts/docs-cn.njk
---

# 快速开始

## 安装 SqlMan

SqlMan 需要 Java 8 或更高版本。添加 SqlMan 和目标数据库对应的 JDBC 驱动：

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>sqlman</artifactId>
    <version>2.1</version>
</dependency>
```

## 执行查询

`Action` 是查询和数据写入的入口。传入 JDBC `Connection`、SQL，然后把位置参数交给 `query(...)`：

```java
try (Connection conn = dataSource.getConnection()) {
    String sql = "SELECT id, name FROM shop_address WHERE stat = ?";

    List<Map<String, Object>> rows =
            new Action(conn, sql).query(1).list();

    System.out.println(rows);
}
```

传给 `query(...)`、`create(...)` 或 `update(...)` 的参数会通过 `PreparedStatement` 绑定到 `?` 占位符。

如果使用不带 `Connection` 的构造方法，需要先为当前线程注册连接：

```java
JdbcConnection.setConnection(conn);
try {
    Map<String, Object> row =
            new Action("SELECT * FROM shop_address WHERE id = ?")
                    .query(1)
                    .one();
} finally {
    JdbcConnection.closeDb();
}
```

如果应用或连接池已经管理连接生命周期，建议显式传入 `Connection`。
