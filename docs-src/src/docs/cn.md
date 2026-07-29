---
title: SqlMan 文档首页
subTitle: Java 的 SQL 优先 JDBC 工具
description: SqlMan 中文文档，介绍 SQL 优先的 JDBC 查询、数据更新、实体映射、分页、批处理和 XML SQL。
date: 2026-07-29
tags:
  - SqlMan
  - JDBC
  - Java 数据库
layout: layouts/docs-cn.njk
---

# 欢迎来到 SqlMan 文档中心

SqlMan 是一个小型 Java 数据库工具，用于消除重复的 JDBC 代码，同时保留清晰可见的 SQL。2.0 版本以 `Action` 作为查询、插入、更新和删除的统一入口。

```java
List<Map<String, Object>> rows =
        new Action(conn, "SELECT * FROM shop_address WHERE stat = ?")
                .query(1)
                .list();
```

## SqlMan 提供的功能

- 通过 JDBC `PreparedStatement` 执行参数化 SQL。
- 把查询结果返回为单值、`Map`、列表或 JavaBean。
- 执行 INSERT，并可按指定类型返回数据库生成的主键。
- 根据 Map 或 JavaBean 生成实体 INSERT、UPDATE 和 DELETE。
- 针对已支持数据库提供 Offset 和页码两种分页方式。
- 参数化批量插入和批量删除。
- 使用 `SmallMyBatis` 在 XML 中保存 SQL，并处理轻量的动态 `<if>`。
- 输出包含绑定参数的 SQL 日志，方便诊断。

## 设计原则

- **SQL 优先：** SQL 由应用明确编写，可以使用数据库特有能力。
- **小巧：** 只封装常见 JDBC 操作，不引入持久化会话或实体管理器。
- **透明：** 连接和事务的生命周期仍由应用控制。

SqlMan 不是完整 ORM、JPA 实现或 MyBatis 替代品。实体功能主要用于数据写入，查询 SQL 仍需明确编写。

## 源代码

SqlMan 使用 GNU General Public License v3.0。

- [GitHub](https://github.com/lightweight-component/SqlMan)
- [GitCode](https://gitcode.com/lightweight-component/SqlMan)
- [Maven Central](https://central.sonatype.com/artifact/com.ajaxjs/sqlman)

## 链接

[网站](https://sqlman.ajaxjs.com) | [文档](https://sqlman.ajaxjs.com/docs) | [JavaDoc 与源码](https://github.com/lightweight-component/SqlMan)
