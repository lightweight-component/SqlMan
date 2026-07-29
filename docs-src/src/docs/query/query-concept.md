---
title: Query Concepts
subTitle: Action, Query, and result formats
description: Understand SqlMan Action and Query objects and choose Map, scalar, or JavaBean query results.
date: 2026-07-29
tags:
  - SqlMan
  - query
  - JDBC
layout: layouts/docs.njk
---

# Query Concepts

SqlMan keeps SQL visible. An `Action` contains the connection, SQL text, parameters, and database vendor. Calling `query(...)` creates a `Query`, which executes the statement and maps its result.

```java
Query query = new Action(
        conn,
        "SELECT id, name FROM shop_address WHERE stat = ?"
).query(1);
```

## One row

Return the first row as a `Map`:

```java
Map<String, Object> row = query.one();
```

Return the first row as a JavaBean:

```java
Address address = query.one(Address.class);
```

If the query has no row, both methods return `null`.

## One value

For a single-column result, use `oneValue(...)`:

```java
Integer total = new Action(
        conn,
        "SELECT COUNT(*) FROM shop_address WHERE stat = ?"
).query(1).oneValue(Integer.class);
```

## Multiple rows

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

An empty multi-row query currently returns `null`; callers that require a collection can normalize it with `Collections.emptyList()`.

## Resource handling

`Query` closes its `PreparedStatement` and `ResultSet`. The caller owns the `Connection` and must close or return it to the pool.
