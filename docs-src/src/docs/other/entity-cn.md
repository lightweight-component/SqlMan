---
title: 实体写入
subTitle: JavaBean 与 Map CRUD
description: 使用 JavaBean 或 Map 及映射注解完成实体插入、更新和删除。
date: 2026-07-29
tags:
  - SqlMan
  - 实体
  - CRUD
layout: layouts/docs-cn.njk
---

# 实体写入

实体操作会根据 JavaBean 或 `Map` 生成带参数的 INSERT、UPDATE 或 DELETE SQL。查询仍然使用明确的 SQL，并可以把结果映射成 JavaBean。

## 插入 Map

Map 必须显式指定表名：

```java
Map<String, Object> address = new LinkedHashMap<>();
address.put("name", "公司");
address.put("address", "Tree Road");
address.put("phone", "3412");

CreateResult<Long> result =
        new Action(conn, address, "shop_address")
                .create()
                .execute(true, Long.class);
```

Map 的键会被直接当作数据库列名。

## 插入 JavaBean

可以显式传入表名：

```java
CreateResult<Long> result =
        new Action(conn, addressBean, "shop_address")
                .create()
                .execute(true, Long.class);
```

也可以给 Bean 添加注解：

```java
@Table("shop_address")
public class Address {
    private Long id;
    private String name;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Transient
    private String displayText;

    // getter 和 setter
}
```

```java
CreateResult<Long> result =
        new Action(conn, addressBean)
                .create()
                .execute(true, Long.class);
```

单条 INSERT 会忽略值为 null 的 Bean 属性。字段或 getter 上的 `@Transient` 表示不持久化，`@Column(name = "...")` 用于修改数据库列名。

## 按 ID 更新

ID 可以从实体中取得：

```java
addressBean.setId(12L);
addressBean.setName("新名称");

UpdateResult result =
        new Action(conn, addressBean, "shop_address")
                .update()
                .withId(); // 默认 ID 字段是 id
```

也可以显式传入 ID：

```java
UpdateResult result =
        new Action(conn, addressBean, "shop_address")
                .update()
                .withId("id", 12L);
```

ID 字段不会进入 SET 子句，而是作为 WHERE 参数绑定。

## 按条件更新

```java
UpdateResult result =
        new Action(conn, values, "shop_address")
                .update()
                .execute("stat = 0");
```

该条件会作为原始 SQL 追加，不能继续绑定参数，因此只能传入由应用代码控制的可信片段。

## 删除实体

```java
UpdateResult result =
        new Action(conn, addressBean, "shop_address")
                .update()
                .delete();
```

默认 ID 字段为 `id`，执行的是物理删除。

## 属性值转换

枚举通过 `toString()` 保存；`List` 和 `Map` 属性会序列化为 JSON。可以使用 `NullValue` 常量显式写入 SQL `NULL`；普通的 null Bean 属性会被忽略。
