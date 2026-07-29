---
title: Entity-based Writes
subTitle: JavaBean and Map CRUD
description: Insert, update, and delete rows from JavaBeans or Maps with SqlMan entity actions and mapping annotations.
date: 2026-07-29
tags:
  - SqlMan
  - entity
  - CRUD
layout: layouts/docs.njk
---

# Entity-based Writes

Entity actions generate parameterized INSERT, UPDATE, or DELETE SQL from a JavaBean or `Map`. Queries still use explicit SQL and can map their results to a JavaBean.

## Insert a Map

Maps require an explicit table name:

```java
Map<String, Object> address = new LinkedHashMap<>();
address.put("name", "Office");
address.put("address", "Tree Road");
address.put("phone", "3412");

CreateResult<Long> result =
        new Action(conn, address, "shop_address")
                .create()
                .execute(true, Long.class);
```

Map keys are treated as database column names.

## Insert a JavaBean

Supply the table explicitly:

```java
CreateResult<Long> result =
        new Action(conn, addressBean, "shop_address")
                .create()
                .execute(true, Long.class);
```

Or annotate the bean:

```java
@Table("shop_address")
public class Address {
    private Long id;
    private String name;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Transient
    private String displayText;

    // getters and setters
}
```

```java
CreateResult<Long> result =
        new Action(conn, addressBean)
                .create()
                .execute(true, Long.class);
```

Null-valued bean properties are omitted from a single-row INSERT. A field annotated with `@Transient`, or a getter annotated with `@Transient`, is omitted. `@Column(name = "...")` changes the database column name.

## Update by ID

The ID may be read from the entity:

```java
addressBean.setId(12L);
addressBean.setName("New name");

UpdateResult result =
        new Action(conn, addressBean, "shop_address")
                .update()
                .withId(); // default ID field: id
```

Or pass the ID value explicitly:

```java
UpdateResult result =
        new Action(conn, addressBean, "shop_address")
                .update()
                .withId("id", 12L);
```

The ID field is excluded from the SET clause and bound in the WHERE clause.

## Update with a condition

```java
UpdateResult result =
        new Action(conn, values, "shop_address")
                .update()
                .execute("stat = 0");
```

The condition is appended as raw SQL. It must be a trusted application-defined fragment; it does not bind additional values.

## Delete an entity

```java
UpdateResult result =
        new Action(conn, addressBean, "shop_address")
                .update()
                .delete();
```

The default ID field is `id`. This performs a physical delete.

## Value conversion

Enums are stored with `toString()`. `List` and `Map` property values are serialized as JSON. `NullValue` constants can explicitly write SQL `NULL`; ordinary null bean properties are skipped.
