---
title: 批量操作
subTitle: 参数化批量插入和删除
description: 使用 Map 或 JavaBean 执行参数化批量插入、批量删除及事务处理。
date: 2026-07-29
tags:
  - SqlMan
  - 批量操作
  - JDBC
layout: layouts/docs-cn.njk
---

# 批量操作

`BatchUpdate` 使用 `JdbcConnection` 为当前线程保存的连接：

```java
JdbcConnection.setConnection(conn);
try {
    BatchUpdate batch = new BatchUpdate();
    // 执行批量操作
} finally {
    JdbcConnection.closeDb();
}
```

如果连接处于自动提交模式，参数化批量插入会开启局部事务：成功时提交，失败时回滚，最后恢复自动提交。如果调用方已经关闭自动提交，则事务的提交和回滚仍由调用方负责。

## 批量插入 Map

所有记录必须和第一条记录包含完全相同的键。使用 `LinkedHashMap` 可以明确控制列顺序：

```java
List<Map<String, Object>> users = new ArrayList<>();

Map<String, Object> first = new LinkedHashMap<>();
first.put("name", "张三");
first.put("email", "zhangsan@example.com");
users.add(first);

Map<String, Object> second = new LinkedHashMap<>();
second.put("name", "李四");
second.put("email", "lisi@example.com");
users.add(second);

new BatchUpdate().createBatchMap(users, "users");
```

值通过 `PreparedStatement` 绑定。支持 `byte[]` 和 `InputStream`；枚举保存为字符串，`Map` 或 `List` 会序列化为 JSON。

## 批量插入 JavaBean

需要在批处理对象上设置表名：

```java
BatchUpdate batch = new BatchUpdate();
batch.setTableName("users");
batch.createBatch(Arrays.asList(user1, user2));
```

第一条 Bean 的非 null 属性决定 INSERT 列。后续 Bean 的这些属性可以为 `null`，但不能出现第一条 Bean 没有选择的其他非 null 属性。

`@Column` 用于修改属性到列的映射，`@Transient` 用于排除属性。

## 按 ID 批量删除

```java
BatchUpdate batch = new BatchUpdate();
batch.setTableName("users");
batch.setIdField("id");

UpdateResult result = batch.deleteBatch(Arrays.asList(1, 2, 3));
```

ID 使用参数绑定。空 ID 列表以及包含 `null` 的列表会被拒绝。

## 旧的原始值接口

`createBatch(String fields, List<String> values)` 及其字符串重载已弃用。它们接收完整 SQL 值片段，无法安全绑定参数，只应兼容可信的旧代码；新代码应使用 Map 或 JavaBean 接口。
