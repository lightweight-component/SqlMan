# 实体写操作

除了原生 SQL 可以执行写操作外，通过实体也可以进行写操作，而且更方便。这里的实体包含 Map 及 Java Bean 两种。

写操作最终目标是返回执行的 SQL 及其参数（params 数组），适合 JDBC prepareStatement 去执行。

写操作具体分为创建、更新及删除。下面逐一看看如何执行。

## 创建

创建的目标是返回执行的 INSERT 语句。遍历实体获取字段名及字段值组成 INSERT 语句及参数即可。

## 更新

更新的目标是返回执行的 UPDATE 语句。这里涉及哪一行记录进行更新的问题（或者不止一行，多行的）。可以支持下面几种的方式。

- 明确给出字段名及字段值，比如`String idField, Object idValue`，最终组成`WHERE idField = idValue`
- idValue 已经蕴含在实体中，只要告诉 API 知道在哪一个字段上即可返回。这时候只要传入 idField。除此之外，此 field 不参与实体遍历构成
  UPDATE 字段更新。
- 不传 idField、idValue，只传自定义 WHERE 语句。这种选择自定义度最高，也是批量更新的唯一方式

## 删除

删除的目标是返回执行的 DELETE 语句。当前不考虑逻辑删除，如果要实现逻辑删除则通过更新来实现。

删除的条件方式应与 UPDATE 的一致，即可以 idField，idValue（可选的）或 WHERE SQL。

## Map 写操作

Map 写操作入参：

- 表名
- Map 实体

约定 Map 的 key 为数据库字段，value 为数据库的值。新建数据时候比较简单，转化为 INSERT 语句及参数数组。

## Bean 写操作

Bean 写操作入参：

- 表名（可选的，如果没有则从 bean 的注解获取）
- Bean 实体

Bean 实体的方式，能够比 Map 带来更强大的功能，比如字段名映射、忽略字段。

# 扩展 SQL

增强 SQL 语句的功能，可以从两个方面来考虑。首先是 SQL 字符串本身，这个只能有限度的处理，毕竟字符串。其次就是使用 SqlParser
语法解析的方式。本文只考虑前者。

## 插值扩展

其实 JDBC preparedStatement 的`?`插值已经算是一种扩展了，但是有一定限制。现今我们讨论基于字符串的插值，类似于把 SQL
当作模板去处理，那样便比较自由。

参考 MyBatis，仍是一样的模板标签：

- `${}` 直接字符串替换
- `#{}` 自动处理类型转换

入参仍是 Map，未来可支持 Bean。

例子：

```
// 动态表名
SELECT * FROM ${tableName
```

## 动态 SQL

支持 if..else/forEach 逻辑判断和循环

```
SELECT * FROM ${tableName}
<if test="stat != null">
    WHERE stat = #{stat}
</if>
```

其中 test 中涉及表达式的运算。

# SQL 存储位置

- String，特别在 Java 多行文本中可以很好支持复杂的 SQL
- XML，适合复杂的 SQL
- 类似 JPA 通过注解写在接口上，暂不实现
