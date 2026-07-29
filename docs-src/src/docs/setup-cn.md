---
title: 配置 SqlMan
subTitle: 数据库支持与连接管理
description: SqlMan 的运行环境、支持的数据库以及连接管理方式。
date: 2026-07-29
tags:
  - SqlMan
  - 配置
  - 数据库连接
layout: layouts/docs-cn.njk
---

# 配置 SqlMan

## 运行要求

SqlMan 支持 Java 8 及以上版本，数据库访问基于标准 JDBC。

当前可以识别 MySQL、MariaDB、PostgreSQL、Oracle、SQL Server、SQLite、H2、HSQLDB、Derby 和 DB2。不同数据库的分页语法不同，请使用项目实际采用的数据库和驱动验证生成的 SQL。

## 使用已有连接

使用连接池的应用应按照连接池规则获取和关闭连接：

```java
try (Connection conn = dataSource.getConnection()) {
    Map<String, Object> row =
            new Action(conn, "SELECT * FROM shop_address WHERE id = ?")
                    .query(1)
                    .one();
}
```

`new Action(dataSource)` 也会取得连接，但连接仍由调用方负责关闭。

## 直接创建 JDBC 连接

直接连接适合测试程序和命令行工具：

```java
try (Connection conn = JdbcConnection.getConnection(
        "jdbc:mysql://localhost:3306/test", "root", "password")) {
    // 使用 conn
}
```

长期运行的服务应使用连接池。

## 当前线程连接

实体批处理 API 以及不传连接的 `Action` 构造方法，会读取当前线程注册的连接：

```java
Connection conn = dataSource.getConnection();
JdbcConnection.setConnection(conn);
try {
    // new Action(sql)、new Action(entity)、BatchUpdate 等
} finally {
    JdbcConnection.closeDb();
}
```

不要在多个线程之间并发共享同一个 JDBC 连接。
