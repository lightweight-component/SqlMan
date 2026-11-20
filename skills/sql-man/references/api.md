# Public API guide

Verify signatures in source before use. This is a navigation aid, not a frozen contract.

## Raw SQL and prepared templates

```java
new Action(conn, sql).query(params).one();
new Action(conn, sql).query(params).oneValue(Integer.class);
new Action(conn, sql).query(params).list();
new Action(conn, sql).query(params).list(Bean.class);

new Action(conn, insertSql).create(params).execute(true, Long.class);
new Action(conn, updateSql).update(params).execute();
new Action(conn).delete(tableName, idField, id);

PreparedSql prepared = sqlXml.prepareSql("statement-id", namedParams);
new Action(conn, prepared).query().list();
```

`Action.query/create/update` accept bind parameters. When the first supplied parameter is a Map, SQL with dynamic nodes or named placeholders follows the inline template/binding path; remaining values bind ordinary `?` placeholders.

## Entity writes

```java
new Action(conn, map, tableName).create().execute(true, Long.class);
new Action(conn, bean, tableName).update().withId();
new Action(conn, bean).update().withId("id", id);
new Action(conn, entity, tableName).update().delete();
```

A Bean can obtain its table name from `@Table`. Entity Map writes and `createBatchMap` convert lower camel-case keys such as `tenantId` to `tenant_id` and reject keys that resolve to the same column. Explicit ID columns use database names. Check empty entities, null properties, ID exclusion, and annotation support when changing generation.

## SQL XML and inline templates

```java
SqlXmlMgr sqlXml = new SqlXmlMgr();
sqlXml.init(); // classpath sql/
PreparedSql prepared = sqlXml.prepareSql("address-by-status", params);

RenderedSql rendered = SqlXmlDomTemplate.compile(inlineSql).render(params);
PreparedSql inlinePrepared = ParameterBinder.prepare(rendered);
```

`#{name}` binds data; `${name}` accepts identifiers only. XML mapper files have a `<mapper>` root and direct `<sql id="...">` children.

## Pagination

```java
query.pageByStartLimit(start, limit);
query.pageByStartLimit(start, limit, Bean.class);
query.pageByPageNo(pageNo, pageSize);
query.pageByPageNo(pageNo, pageSize, Bean.class);
```

Servlet overloads read known request parameter aliases.

## Batch operations

```java
JdbcConnection.setConnection(conn);

BatchUpdate batch = new BatchUpdate();
batch.createBatchMap(rows, tableName);
batch.setTableName(tableName);
batch.createBatch(beans);
batch.setIdField(idField);
batch.deleteBatch(ids);
```

The raw-values `createBatch` overloads are deprecated and cannot safely bind their SQL value fragments.

## Compatibility policy

- Avoid changing public signatures or return semantics without strong justification.
- Add an overload or deprecated bridge when practical.
- Keep generic ID types truthful by performing real conversion.
- Make exceptions actionable: include operation, property/column, source type, target type, or SQL context as appropriate.
- Treat Lombok-generated accessors on public models as part of observable API usage.

## Explicit SQL NULL

Verified against model/NullValue.java, entity/BeanIterator.java, entity/BaseWriteSql.java,
entity/MapWriteSql.java and BaseAction.setParam2Ps().

- Bean-generated INSERT/UPDATE skips ordinary Java null properties. An omitted INSERT column can receive
  a database default. An omitted UPDATE column retains the existing value.
- NullValue.NULL_STRING is the String sentinel __NULL_STRING__. NULL_INT is Integer.MAX_VALUE,
  NULL_LONG is Long.MAX_VALUE, and NULL_DATE is java.util.Date at epoch milliseconds 0.
- BaseWriteSql.toSqlValue converts these markers to SQL NULL during Bean/Map write generation.
  Column(nullable=true) does not cause Java null properties to be included.
- Markers must fit the property type. NULL_DATE cannot be assigned to LocalDateTime, LocalDate, or
  java.sql.Date fields. NULL_LOCAL_DATE_TIME uses LocalDateTime.MIN and supports LocalDateTime properties.
  There are no Boolean, BigDecimal or LocalDate markers.
- For unsupported Bean types, use an allowlisted Map containing an explicit null entry, or parameterized
  SQL. MapWriteSql includes explicit null entries.
- Raw SQL parameter binding does not currently translate these markers. Supply actual null instead.
  For example, new Action("UPDATE firewall_blacklist SET expire_time = ? WHERE id = ?")
  .update((Object) null, id).execute(). For a single null parameter, cast it to Object so it is not a null
  varargs array.
- Marker comparison uses equals rather than identity. The sentinel values are reserved on generated
  write paths. Do not assume they can also be persisted as ordinary values through those paths.
- Preserve existing writable-column boundaries and server-owned fields when migrating entity CRUD.

For a compatible String property, row.setDescription(NullValue.NULL_STRING) followed by
new Action(row).update().withId() clears the description. For LocalDateTime properties, use NullValue.NULL_LOCAL_DATE_TIME. The reserved value is LocalDateTime.MIN. It is converted only by generated Bean/Map writes, not by raw SQL binding.
