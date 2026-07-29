---
title: Setup SqlMan
subTitle: Connections and database support
description: Runtime requirements, supported databases, and connection management options for SqlMan.
date: 2026-07-29
tags:
  - SqlMan
  - configuration
  - database connection
layout: layouts/docs.njk
---

# Setup SqlMan

## Requirements

SqlMan runs on Java 8 or later. Its database access is based on standard JDBC.

Database vendor detection currently recognizes MySQL, MariaDB, PostgreSQL, Oracle, SQL Server, SQLite, H2, HSQLDB, Derby, and DB2. Pagination syntax varies by vendor; test generated SQL against the database and driver used by your application.

## Use an existing connection

Applications using a connection pool should obtain and close the connection according to the pool's rules:

```java
try (Connection conn = dataSource.getConnection()) {
    Map<String, Object> row =
            new Action(conn, "SELECT * FROM shop_address WHERE id = ?")
                    .query(1)
                    .one();
}
```

`new Action(dataSource)` also obtains a connection, but the caller remains responsible for closing that connection.

## Open a direct JDBC connection

Direct connections are convenient for tests and command-line tools:

```java
try (Connection conn = JdbcConnection.getConnection(
        "jdbc:mysql://localhost:3306/test", "root", "password")) {
    // use conn
}
```

For long-running applications, use a connection pool instead.

## Thread-local connection

Entity batch APIs and `Action` constructors without a connection use the connection registered for the current thread:

```java
Connection conn = dataSource.getConnection();
JdbcConnection.setConnection(conn);
try {
    // new Action(sql), new Action(entity), BatchUpdate, ...
} finally {
    JdbcConnection.closeDb();
}
```

Do not share one JDBC connection concurrently between threads.
