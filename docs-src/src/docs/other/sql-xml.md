---
title: XML SQL
subTitle: SQL XML statement templates
description: Load SQL statements from classpath XML and safely render dynamic conditions and bindings.
date: 2026-10-05
tags:
  - SqlMan
  - XML SQL
  - dynamic SQL
layout: layouts/docs.njk
---

# XML SQL

`SqlXmlMgr` stores named SQL statements from classpath XML. It is a small dynamic-SQL facility, not a MyBatis mapper or an ORM. Its default expression evaluator is based on JSqlParser; Spring EL is not involved.

## Define statements

Put XML files under `src/main/resources/sql/` (subdirectories are scanned too). A file has one `<mapper>` root and direct `<sql id="...">` children:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<mapper>
    <sql id="address-by-status">
        SELECT * FROM ${tableName}
        <if test="stat IS NOT NULL">
            WHERE stat = #{stat}
            <else>
                WHERE stat IS NULL
            </else>
        </if>
    </sql>
    <sql id="address-by-ids">
        SELECT * FROM shop_address WHERE id IN
        <forEach collection="ids" item="id" open="(" separator="," close=")">#{id}</forEach>
    </sql>
</mapper>
```

Statement IDs are unique within one manager. Resources are sorted by logical path; if two files define the same ID, the later one deterministically replaces the former and a warning is logged.

## Load, prepare, and execute

```java
SqlXmlMgr sqlXml = new SqlXmlMgr();
sqlXml.init();                       // scans classpath sql/
// sqlXml.init("myapp/sql");         // scans a custom classpath directory

Map<String, Object> params = new HashMap<>();
params.put("tableName", "shop_address");
params.put("stat", 1);

PreparedSql prepared = sqlXml.prepareSql("address-by-status", params);
List<Map<String, Object>> rows = new Action(conn, prepared).query().list();
```

`renderSql(id, params)` is available when the rendered SQL and effective immutable parameter map are needed before binding. `prepareSql(id, params)` is the usual entry point and returns JDBC SQL plus its ordered values.

The scanner supports ordinary classpath directories and ordinary JAR files. It intentionally does not support Spring resource expressions such as `classpath*:` or nested/fat JAR layouts. Missing XML files and malformed mapper files fail during `init`, rather than creating an empty registry.

## Dynamic nodes and expressions

`<if>` may contain a direct `<else>` child, and either branch may contain nested dynamic nodes. `<forEach>` supports `Iterable`, arrays, and `Map`: for a map, `index` is the key and `item` is the value; for other collections, `index` is the zero-based position.

The expression language accepts a constrained SQL-like subset: boolean operators, comparisons, arithmetic, parentheses, `IS [NOT] NULL`, `BETWEEN`, `IN`, and explicitly registered functions. Use SQL null tests such as `stat IS NOT NULL`, rather than Java/Spring-EL syntax such as `stat != null`.

## Binding and safety

- `#{name}` always becomes JDBC `?`; values are collected in occurrence order.
- `${name}` is allowed only for identifiers, including qualified names such as `schema.table`. Arbitrary fragments are rejected.
- Ordinary `?` parameters can be supplied as trailing positional values where the API supports them.

Do not expose raw table/column selection to untrusted callers merely because `${...}` validates identifier syntax; authorization still belongs to the application.

## Inline templates

XML storage is optional. A dynamic SQL string can be compiled as an XML fragment:

```java
RenderedSql rendered = SqlXmlDomTemplate.compile(
        "SELECT * FROM ${tableName}<if test=\"stat IS NOT NULL\"> WHERE stat = #{stat}</if>")
        .render(params);
PreparedSql prepared = ParameterBinder.prepare(rendered);
```

When an `Action` receives a `Map` as its first parameter, it applies the same inline-template path automatically for SQL containing dynamic nodes or named placeholders.
