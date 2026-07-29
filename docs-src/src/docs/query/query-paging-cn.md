---
title: 分页查询
subTitle: Offset 与页码模式
description: 使用 start/limit 或 pageNo/pageSize 分页，并把分页记录映射成 JavaBean。
date: 2026-07-29
tags:
  - SqlMan
  - 分页
  - 查询
layout: layouts/docs-cn.njk
---

# 分页查询

分页会先执行统计查询，再执行当前页查询。传入的原始 SQL 不应预先包含数据库专用的分页语句。

## Start 和 Limit

`start` 是从 0 开始的记录偏移量，`limit` 是每页记录数：

```java
PageResult<Map<String, Object>> page =
        new Action(conn, "SELECT * FROM article ORDER BY id DESC")
                .query()
                .pageByStartLimit(0, 20);
```

## 页码和每页大小

页码从 1 开始：

```java
PageResult<Map<String, Object>> page =
        new Action(conn, "SELECT * FROM article ORDER BY id DESC")
                .query()
                .pageByPageNo(3, 20);
```

## 映射 JavaBean

```java
PageResult<Address> page =
        new Action(conn, "SELECT * FROM shop_address ORDER BY id")
                .query()
                .pageByPageNo(1, 20, Address.class);
```

`PageResult` 包含 `list`、`totalCount`、`totalPage`、`currentPage`、`start`、`pageSize` 和 `zero`。请求超过最后一页时，`list` 是空列表。

## 绑定查询参数

先在创建 `Query` 时传入参数，再调用分页方法：

```java
PageResult<Map<String, Object>> page =
        new Action(conn, "SELECT * FROM shop_address WHERE stat = ? ORDER BY id")
                .query(1)
                .pageByStartLimit(0, 20);
```

## 从 HTTP 请求读取分页参数

Servlet 重载支持以下参数名：

- Offset 模式：`start` 或 `offset`；每页大小：`pageSize`、`rows` 或 `limit`。
- 页码模式：`pageNo` 或 `page`；每页大小：`pageSize`、`rows` 或 `limit`。

```java
PageResult<Map<String, Object>> page =
        new Action(conn, sql).query(params).pageByPageNo(request);
```

基于请求的分页默认每页 12 条记录。
