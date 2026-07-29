---
title: XML SQL
subTitle: SmallMyBatis SQL 模板
description: 在 XML 中保存 SQL，按 ID 加载语句，并使用 SmallMyBatis 处理轻量动态 SQL。
date: 2026-07-29
tags:
  - SqlMan
  - XML SQL
  - 动态 SQL
layout: layouts/docs-cn.njk
---

# XML SQL

## 为什么要在 XML 中使用 SQL？

像著名的 MyBatis 框架一样，SQL 语句可以存储在 XML 文件中。这种方式有其特定的原因。以下是在 XML 中存储 SQL 的一些优点：

1. **关注点分离**：将 SQL 语句保存在 XML 文件中有助于将 SQL 逻辑与 Java 代码分离。这使代码库更加整洁，更易于管理。
1. **可读性**：当存储在 XML 中时，长 SQL 语句可以更具可读性。XML 文件的结构可以帮助使 SQL 查询更有条理，更易于理解。
1. **可维护性**：当 SQL 语句存储在 XML 中时，无需更改 Java 代码就可以更轻松地更新和维护它们。这对于具有大量 SQL 查询的大型项目特别有用。

SqlMan 在处理 SQL 语句方面采用了类似 MyBatis 的方法。

`SmallMyBatis` 可以把 SQL 保存在 classpath 下的 XML 资源中。它是轻量 SQL 模板工具，并不是完整的 MyBatis Mapper 或 ORM。

## 定义 SQL

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

同一个 `SmallMyBatis` 实例中的 SQL ID 共用一张映射表。加载重复 ID 时，后加载的 SQL 会覆盖原内容并记录警告。

## 加载和执行

```java
SmallMyBatis mapper = new SmallMyBatis();
mapper.loadXML("sql/mysql.xml");

String sql = mapper.getSqlById("address-by-id");
Address address = new Action(conn, sql).query(12L).one(Address.class);
```

`loadXML(...)` 可以一次加载多个资源。`loadBySqlLocations(...)` 支持 Spring 资源匹配模式：

```java
mapper.loadBySqlLocations("classpath*:sql/**/*.xml");
```

## 动态条件

`handleSql(params, sqlId)` 使用 Spring 表达式执行 `<if test="...">`，然后处理占位符：

```java
Map<String, Object> params = new HashMap<>();
params.put("tableName", "shop_address");
params.put("stat", 1);

String sql = mapper.handleSql(params, "address-by-status");
List<Map<String, Object>> rows = new Action(conn, sql).query().list();
```

XML 中的比较符可以写成 `&lt;` 和 `&gt;`，生成 SQL 时会恢复为原符号。

## 占位符安全

- `${name}` 不加引号地插入文本，只能用于可信的标识符或 SQL 片段。
- `#{name}` 当前会把格式化后的值直接插入 SQL；虽然写法类似 MyBatis，但不会生成 JDBC `?` 参数。
- 数据值仍推荐使用 JDBC `?`。

不要把请求参数或其他不可信输入传给 `${...}` 或 `#{...}`。`handleSql(...)` 当前没有启用 `<forEach>` 解析，文档和业务代码不应依赖该标签。
