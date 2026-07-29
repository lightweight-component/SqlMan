---
title: SqlMan Documentation
subTitle: SQL-first JDBC utilities for Java
description: SqlMan documentation for SQL-first JDBC queries, data updates, entity mapping, pagination, batch operations, and XML SQL.
date: 2026-07-29
tags:
  - SqlMan
  - JDBC
  - Java database
layout: layouts/docs.njk
---

# Welcome to the SqlMan Documentation

SqlMan is a small Java library that removes repetitive JDBC work while keeping SQL visible. Version 2.0 uses `Action` as the common entry point for queries, inserts, updates, and deletes.

```java
List<Map<String, Object>> rows =
        new Action(conn, "SELECT * FROM shop_address WHERE stat = ?")
                .query(1)
                .list();
```

## What SqlMan provides

- Parameterized SQL execution through JDBC `PreparedStatement`.
- Query results as a single value, a `Map`, a list, or JavaBeans.
- INSERT results with optional generated-key conversion.
- Entity-based INSERT, UPDATE, and DELETE generation for Maps and JavaBeans.
- Offset-based and page-number pagination for supported database vendors.
- Parameterized batch inserts and batch deletes.
- Lightweight XML statement storage and dynamic `<if>` processing with `SmallMyBatis`.
- SQL logging with bound-parameter rendering for diagnostics.

## Design principles

- **SQL first:** application SQL remains explicit and can use database-specific features.
- **Small:** the API wraps JDBC operations instead of introducing a persistence session or entity manager.
- **Transparent:** connections and transactions remain visible and under application control.

SqlMan is not a full ORM, a JPA implementation, or a complete MyBatis replacement. Entity support focuses on data modification; query SQL is still written explicitly.

## Source code

SqlMan is licensed under the GNU General Public License v3.0.

- [GitHub](https://github.com/lightweight-component/SqlMan)
- [GitCode](https://gitcode.com/lightweight-component/SqlMan)
- [Maven Central](https://central.sonatype.com/artifact/com.ajaxjs/sqlman)

## Links

[Website](https://sqlman.ajaxjs.com) | [Documentation](https://sqlman.ajaxjs.com/docs) | [JavaDoc and source](https://github.com/lightweight-component/SqlMan)
