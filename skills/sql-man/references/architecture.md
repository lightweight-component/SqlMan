# Architecture

## Execution path

- `com.ajaxjs.sqlman.Action`: mutable input and configuration object; creates query, create, and update actions.
- `JdbcConnection`: direct, `DataSource`, and thread-local connection access; detects `DatabaseVendor`.
- `crud.BaseAction`: common parameter binding and `ResultSet` conversion.
- `crud.Query`: scalar, Map, JavaBean, list, and pagination queries.
- `crud.Create`: INSERT execution and generated-key conversion.
- `crud.Update`: raw SQL update plus entity update/delete entry points.
- `crud.BatchUpdate`: Map/Bean batch INSERT and ID-list DELETE; currently obtains its connection from `JdbcConnection`.

## SQL generation

- `sqlgenerator.Entity2WriteSql`: creates INSERT/UPDATE/DELETE SQL and ordered parameters from a Map or JavaBean.
- `meta.*`: reads table and ID metadata.
- `annotation.Table`, `Column`, `Transient`, `Id`: mapping metadata; verify which annotation members are actually honored before documenting them.
- `SmallMyBatis`: loads `<sql id="...">` nodes and applies lightweight dynamic/template processing.
- `sqlgenerator.XmlSql`: separate placeholder implementation; check callers before changing it.

## Pagination

- `Query` exposes start/limit and pageNo/pageSize overloads.
- `PageQuery` executes count and page queries and fills `PageResult`.
- `PageControl` rewrites count and database-specific page SQL with JSQLParser.

Review SQL state restoration, parameter preservation, complex COUNT queries, input validation, required ordering, and out-of-range behavior together.

## Results and diagnostics

- `model.CreateResult`, `UpdateResult`, and `Result` hold write outcomes.
- `BaseAction` maps JDBC values into Map/bean/scalar results.
- `util.PrintRealSql` renders parameters for logging and throttles repeated business-action logs.

Logging is secondary behavior: it must not change the database operation's outcome.

## Documentation

- Source: `docs-src/src/**/*.md`, layouts, styles, and Eleventy config.
- Generated site: `docs/` or local `docs-src/dist/`; do not use generated pages as the editing source.
- Files ending in `-cn.md` are Chinese counterparts; files without `-cn` are English.
