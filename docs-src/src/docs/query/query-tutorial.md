---
title: Query Tutorial
subTitle: Parameters and result mapping
description: Query a database with positional parameters, named SQL templates, Maps, and JavaBeans.
date: 2026-07-29
tags:
  - SqlMan
  - query tutorial
  - prepared statement
layout: layouts/docs.njk
---

# Query Tutorial

## Bind positional parameters

Use `?` for data values and pass parameters in the same order:

```java
Map<String, Object> row =
        new Action(conn, "SELECT * FROM shop_address WHERE id = ? AND stat = ?")
                .query(1, 0)
                .one();
```

The values are bound by `PreparedStatement`; do not quote the `?` placeholders.

## SQL template parameters

If the first argument is a `Map`, `Action` sends it to `SmallMyBatis` before binding the remaining positional parameters:

```java
Map<String, Object> template = new HashMap<>();
template.put("tableName", "shop_address");

Map<String, Object> row =
        new Action(conn, "SELECT * FROM ${tableName} WHERE id = ?")
                .query(template, 1)
                .one();
```

`${...}` and `#{...}` are text substitutions in the current implementation, not JDBC bind parameters. Only use `${...}` for trusted identifiers or trusted SQL fragments. Prefer `?` for all data values.

## Map column names

Map results use JDBC column labels. Give expressions an alias when you need a stable key:

```java
Map<String, Object> totals =
        new Action(conn, "SELECT COUNT(*) AS total FROM shop_address")
                .query()
                .one();

Object total = totals.get("total");
```

## Map to a JavaBean

The target class needs a no-argument constructor and writable properties:

```java
Address address =
        new Action(conn, "SELECT id, name, create_date FROM shop_address WHERE id = ?")
                .query(1)
                .one(Address.class);
```

SqlMan converts underscore column names such as `create_date` to Java property names such as `createDate`.
