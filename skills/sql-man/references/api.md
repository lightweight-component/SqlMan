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

A Bean can obtain its table name from `@Table`. Map keys and resolved Bean columns are validated identifiers. Check empty entities, null properties, ID exclusion, and annotation support when changing generation.

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
