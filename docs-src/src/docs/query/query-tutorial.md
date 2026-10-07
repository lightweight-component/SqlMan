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

If the first argument is a `Map`, `Action` renders an inline SQL template when the SQL contains a dynamic node or a
named placeholder, then binds any remaining positional parameters:

### Named data parameters: `#{}`

Use `#{name}` for ordinary values in the Map. It generates a JDBC `?` placeholder, and values are bound in SQL
occurrence order. It is therefore safe for fields originating from a query string, form, or JSON request body:

```java
Map<String, Object> params = new HashMap<>();
params.put("id", 1);
params.put("name", "Alice");

Map<String, Object> row =
        new Action(conn, "SELECT * FROM shop_address WHERE id = #{id} AND name = #{name}")
                .query(params)
                .one();
```

The JDBC SQL for the example is equivalent to `SELECT * FROM shop_address WHERE id = ? AND name = ?`. Use `#{}` for
values in `SELECT`, `INSERT`, `UPDATE`, and `DELETE`; do not add quotes around the placeholder yourself.

### Identifier parameters: `${}`

```java
Map<String, Object> template = new HashMap<>();
template.put("tableName", "shop_address");

Map<String, Object> row =
        new Action(conn, "SELECT * FROM ${tableName} WHERE id = ?")
                .query(template, 1)
                .one();
```

`${...}` is restricted to SQL identifiers, including qualified names such as `schema.table`; arbitrary SQL fragments
are rejected. It is not for ordinary request values: `WHERE id = ${id}` rejects an `id=1` value, so write
`WHERE id = #{id}` instead. Table names, column names, sort fields, and sort direction are SQL structure and cannot be
JDBC-bound; even when an identifier passes syntax validation, the application must constrain it through server-side
configuration, an allowlist, and authorization.

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
