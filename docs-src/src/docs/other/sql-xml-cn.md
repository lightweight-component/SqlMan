---
title: XML SQL
subTitle: SQL XML 语句模板
description: 从 classpath XML 加载 SQL，并安全地渲染动态条件与 JDBC 参数。
date: 2026-10-05
tags:
  - SqlMan
  - XML SQL
  - 动态 SQL
layout: layouts/docs-cn.njk
---

# XML SQL

`SqlXmlMgr` 用于从 classpath XML 保存和加载具名 SQL。它是轻量动态 SQL 功能，不是 MyBatis Mapper 或 ORM；默认条件表达式求值器基于 JSqlParser，不再依赖 Spring EL。

## 定义 SQL

将 XML 放入 `src/main/resources/sql/`，子目录也会递归扫描。每个文件只能有一个 `<mapper>` 根节点，直属子节点为 `<sql id="...">`：

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

同一管理器内 SQL ID 必须唯一。资源按逻辑路径排序；若重复 ID，排序靠后的资源会稳定地覆盖前者，并输出警告日志。

## 加载、预编译与执行

```java
SqlXmlMgr sqlXml = new SqlXmlMgr();
sqlXml.init();                       // 扫描 classpath sql/
// sqlXml.init("myapp/sql");         // 扫描自定义 classpath 目录

Map<String, Object> params = new HashMap<>();
params.put("tableName", "shop_address");
params.put("stat", 1);

PreparedSql prepared = sqlXml.prepareSql("address-by-status", params);
List<Map<String, Object>> rows = new Action(conn, prepared).query().list();
```

`renderSql(id, params)` 可获得渲染后的 SQL 和不可变的有效参数快照；通常直接使用 `prepareSql(id, params)`，它返回 JDBC SQL 与有序参数值。

扫描器支持普通 classpath 目录和普通 JAR，不支持 `classpath*:` 等 Spring 资源表达式，也不支持嵌套/fat JAR。找不到 XML 或 Mapper 格式不正确时，`init` 会明确失败，不会静默创建空注册表。

## 动态节点与表达式

`<if>` 可以拥有直属 `<else>` 子节点，两个分支都可继续嵌套动态节点。`<forEach>` 支持 `Iterable`、数组和 `Map`：遍历 Map 时 `index` 为 key、`item` 为 value；其他集合的 `index` 为从零开始的序号。

表达式使用受限的 SQL 风格子集：布尔逻辑、比较、算术、括号、`IS [NOT] NULL`、`BETWEEN`、`IN` 和显式注册的函数。空值判断请使用 `stat IS NOT NULL`，而不是 `stat != null` 这类 Java/Spring-EL 写法。

## 参数绑定与安全性

- `#{name}` 必定转换为 JDBC `?`，值按出现顺序收集。
- `${name}` 仅允许标识符（包括 `schema.table` 等限定名称），任意 SQL 片段都会被拒绝。
- API 支持的位置参数可继续以普通 `?` 并配合尾随位置参数使用。

`${...}` 的标识符校验不能替代业务授权；即使语法合法，也不应让不可信调用方任意选择表或列。

## 内联模板

并非所有动态 SQL 都必须存入 XML。SQL 字符串也可作为 XML 片段编译：

```java
RenderedSql rendered = SqlXmlDomTemplate.compile(
        "SELECT * FROM ${tableName}<if test=\"stat IS NOT NULL\"> WHERE stat = #{stat}</if>")
        .render(params);
PreparedSql prepared = ParameterBinder.prepare(rendered);
```

当 `Action` 的第一个参数为 `Map` 时，只要 SQL 含动态节点或命名占位符，它会自动走同一套内联模板流程。
