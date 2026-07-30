---
title: Quick Start
subTitle: SqlMan 2.0
description: Install SqlMan 2.0, connect to a database, and run the first parameterized JDBC query.
date: 2026-07-29
tags:
  - SqlMan
  - quick start
  - JDBC
layout: layouts/docs.njk
---

# Quick Start

## Install SqlMan

SqlMan requires Java 8 or later. Add the library and the JDBC driver for your database:

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>sqlman</artifactId>
    <version>2.1</version>
</dependency>
```

## Run a query

`Action` is the entry point for queries and data modifications. Supply a JDBC `Connection`, SQL text, and then the positional parameters:

```java
try (Connection conn = dataSource.getConnection()) {
    String sql = "SELECT id, name FROM shop_address WHERE stat = ?";

    List<Map<String, Object>> rows =
            new Action(conn, sql).query(1).list();

    System.out.println(rows);
}
```

Parameters passed to `query(...)`, `create(...)`, or `update(...)` are bound to `?` placeholders through `PreparedStatement`.

To use the constructors that do not take a `Connection`, register one for the current thread:

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

Prefer an explicit `Connection` when the application or connection pool already manages its lifecycle.
