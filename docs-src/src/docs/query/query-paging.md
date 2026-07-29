---
title: Pagination
subTitle: Offset and page-number APIs
description: Paginate SqlMan queries by offset/limit or page number/page size and map page rows to JavaBeans.
date: 2026-07-29
tags:
  - SqlMan
  - pagination
  - query
layout: layouts/docs.njk
---

# Pagination

Pagination executes a count query followed by a query for the requested page. Start with SQL that does not already contain database-specific pagination syntax.

## Offset and limit

`start` is a zero-based row offset and `limit` is the page size:

```java
PageResult<Map<String, Object>> page =
        new Action(conn, "SELECT * FROM article ORDER BY id DESC")
                .query()
                .pageByStartLimit(0, 20);
```

## Page number and page size

Page numbers start at 1:

```java
PageResult<Map<String, Object>> page =
        new Action(conn, "SELECT * FROM article ORDER BY id DESC")
                .query()
                .pageByPageNo(3, 20);
```

## JavaBean rows

```java
PageResult<Address> page =
        new Action(conn, "SELECT * FROM shop_address ORDER BY id")
                .query()
                .pageByPageNo(1, 20, Address.class);
```

`PageResult` contains `list`, `totalCount`, `totalPage`, `currentPage`, `start`, `pageSize`, and `zero`. A request beyond the last page returns an empty list.

## Bind query parameters

Bind parameters when creating the `Query`, before invoking the pagination method:

```java
PageResult<Map<String, Object>> page =
        new Action(conn, "SELECT * FROM shop_address WHERE stat = ? ORDER BY id")
                .query(1)
                .pageByStartLimit(0, 20);
```

## Read pagination from an HTTP request

The servlet overloads recognize:

- Offset mode: `start` or `offset`; page size: `pageSize`, `rows`, or `limit`.
- Page-number mode: `pageNo` or `page`; page size: `pageSize`, `rows`, or `limit`.

```java
PageResult<Map<String, Object>> page =
        new Action(conn, sql).query(params).pageByPageNo(request);
```

The default page size for request-based pagination is 12.
