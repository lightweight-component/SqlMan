---
title: Batch Operations
subTitle: Parameterized inserts and deletes
description: Perform parameterized batch inserts and batch deletes from Maps or JavaBeans with transaction handling.
date: 2026-07-29
tags:
  - SqlMan
  - batch operations
  - JDBC
layout: layouts/docs.njk
---

# Batch Operations

`BatchUpdate` uses the connection registered in `JdbcConnection` for the current thread:

```java
JdbcConnection.setConnection(conn);
try {
    BatchUpdate batch = new BatchUpdate();
    // execute batch operations
} finally {
    JdbcConnection.closeDb();
}
```

If the connection is in auto-commit mode, a parameterized batch insert opens a local transaction, commits on success, rolls back on failure, and restores auto-commit. If the caller has already disabled auto-commit, transaction completion remains the caller's responsibility.

## Insert Maps

All rows must contain exactly the same keys as the first row. A `LinkedHashMap` makes the column order explicit:

```java
List<Map<String, Object>> users = new ArrayList<>();

Map<String, Object> first = new LinkedHashMap<>();
first.put("name", "John");
first.put("email", "john@example.com");
users.add(first);

Map<String, Object> second = new LinkedHashMap<>();
second.put("name", "Jane");
second.put("email", "jane@example.com");
users.add(second);

new BatchUpdate().createBatchMap(users, "users");
```

Values are bound with `PreparedStatement`. `byte[]` and `InputStream` are supported; enums are stored as strings, and `Map` or `List` values are serialized as JSON.

## Insert JavaBeans

Set the table name on the batch object:

```java
BatchUpdate batch = new BatchUpdate();
batch.setTableName("users");
batch.createBatch(Arrays.asList(user1, user2));
```

The non-null properties of the first bean select the INSERT columns. Later beans may contain `null` for those columns, but may not introduce an additional non-null property.

`@Column` changes a property-to-column mapping and `@Transient` excludes a property.

## Delete by IDs

```java
BatchUpdate batch = new BatchUpdate();
batch.setTableName("users");
batch.setIdField("id");

UpdateResult result = batch.deleteBatch(Arrays.asList(1, 2, 3));
```

IDs are bound as parameters. Empty lists and lists containing `null` are rejected.

## Legacy raw-values API

`createBatch(String fields, List<String> values)` and its string overload are deprecated. They accept complete SQL value fragments and cannot bind values safely. Keep them only for trusted legacy input; new code should use the Map or JavaBean APIs.
