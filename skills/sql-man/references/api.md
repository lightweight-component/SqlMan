# Public API guide

Verify signatures in source before use. This is a navigation aid, not a frozen contract.

## Raw SQL

```java
new Action(conn, sql).query(params).one();
new Action(conn, sql).query(params).oneValue(Integer.class);
new Action(conn, sql).query(params).list();
new Action(conn, sql).query(params).list(Bean.class);

new Action(conn, insertSql).create(params).execute(true, Long.class);
new Action(conn, updateSql).update(params).execute();
new Action(conn).delete(tableName, idField, id);
```

`Action.query/create/update` accept bind parameters. If the first parameter is a Map, current code processes SQL templates first and binds remaining arguments.

## Entity writes

```java
new Action(conn, map, tableName).create().execute(true, Long.class);
new Action(conn, bean, tableName).update().withId();
new Action(conn, bean).update().withId("id", id);
new Action(conn, entity, tableName).update().delete();
```

A Bean can obtain its table name from `@Table`. Map keys are treated as column names. Check empty entities, null properties, ID exclusion, and annotation support when changing generation.

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
