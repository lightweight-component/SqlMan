---
title: XML SQL
subTitle: SmallMyBatis statement templates
description: Store SQL in XML, load statements by ID, and apply lightweight dynamic SQL with SmallMyBatis.
date: 2026-07-29
tags:
  - SqlMan
  - XML SQL
  - dynamic SQL
layout: layouts/docs.njk
---

# XML SQL

`SmallMyBatis` stores SQL statements in classpath XML resources. It is a lightweight template helper, not a complete MyBatis mapper or ORM.

## Define statements

```xml
<?xml version="1.0" encoding="UTF-8"?>
<root>
    <sql id="address-count">
        SELECT COUNT(*) AS total FROM shop_address
    </sql>

    <sql id="address-by-id">
        SELECT * FROM shop_address WHERE id = ?
    </sql>

    <sql id="address-by-status">
        SELECT * FROM ${tableName}
        <if test="stat != null">
            WHERE stat = #{stat}
        </if>
    </sql>
</root>
```

Statement IDs share one map inside a `SmallMyBatis` instance. Loading a duplicate ID replaces the previous SQL and writes a warning.

## Load and execute

```java
SmallMyBatis mapper = new SmallMyBatis();
mapper.loadXML("sql/mysql.xml");

String sql = mapper.getSqlById("address-by-id");
Address address = new Action(conn, sql).query(12L).one(Address.class);
```

Multiple resources can be loaded at once. `loadBySqlLocations(...)` also accepts a Spring resource pattern:

```java
mapper.loadBySqlLocations("classpath*:sql/**/*.xml");
```

## Dynamic conditions

`handleSql(params, sqlId)` evaluates `<if test="...">` blocks with Spring Expression Language and then performs placeholders substitution:

```java
Map<String, Object> params = new HashMap<>();
params.put("tableName", "shop_address");
params.put("stat", 1);

String sql = mapper.handleSql(params, "address-by-status");
List<Map<String, Object>> rows = new Action(conn, sql).query().list();
```

XML comparison operators can be written as `&lt;` and `&gt;`; the generated SQL converts them back.

## Placeholder safety

- `${name}` inserts text without quoting. Use it only for trusted identifiers or SQL fragments.
- `#{name}` currently inserts a formatted value directly into SQL. Despite its MyBatis-like spelling, it does not create a JDBC `?` parameter.
- JDBC `?` remains the recommended form for data values.

Do not pass request parameters or other untrusted input to `${...}` or `#{...}`. The `<forEach>` parser is not active in `handleSql(...)` and should not be relied upon.
