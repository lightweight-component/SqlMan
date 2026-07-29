---
title: Create, Update, and Delete
subTitle: Parameterized data modification
description: Execute parameterized INSERT, UPDATE, and DELETE statements and retrieve generated keys with SqlMan.
date: 2026-07-29
tags:
  - SqlMan
  - data modification
  - CRUD
layout: layouts/docs.njk
---

# Create, Update, and Delete

## Insert a row

Use `create(params).execute(...)` for an `INSERT` statement:

```java
String sql = "INSERT INTO shop_address (name, address, phone) VALUES (?, ?, ?)";

CreateResult<Serializable> result =
        new Action(conn, sql)
                .create("Office", "Tree Road", "3412")
                .execute(false);
```

`isOk()` reports whether a row was inserted.

## Retrieve a generated key

Pass `true` when the database generates the key, and specify the requested Java type:

```java
CreateResult<Long> result =
        new Action(conn,
                "INSERT INTO shop_address (name, address) VALUES (?, ?)")
                .create("Home", "Lake Road")
                .execute(true, Long.class);

Long id = result.getNewlyId();
```

SqlMan converts the JDBC-generated value to the requested numeric type and rejects overflow or fractional conversion.

## Update rows

```java
UpdateResult result =
        new Action(conn,
                "UPDATE shop_address SET name = ? WHERE id = ?")
                .update("Head Office", 8)
                .execute();

int affected = result.getEffectedRows();
```

An execution without a JDBC exception is considered successful even when `affected` is zero.

## Delete by ID

```java
UpdateResult result =
        new Action(conn).delete("shop_address", "id", 8);
```

This is a physical delete. For logical deletion, execute an `UPDATE` that changes the relevant status column.

Table names, column names, and raw clauses are not bind parameters. Keep identifiers and SQL fragments controlled by application code; never copy untrusted request text into them.
