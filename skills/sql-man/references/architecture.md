# Architecture

## Execution path

- `com.ajaxjs.sqlman.Action`: mutable input/configuration object and public entry point for raw SQL, entity writes, and `PreparedSql`.
- `JdbcConnection`: direct `DriverManager`, `DataSource`, and thread-local connection access; detects `DatabaseVendor`.
- `BaseAction`, `Query`, `Create`, `Update`, and `BatchUpdate`: JDBC execution and result conversion. `BatchUpdate` obtains its connection from `JdbcConnection`.
- `com.ajaxjs.sqlman.model.PreparedSql`: JDBC SQL plus ordered values; pass it to `Action` for prepared XML/template statements.

## Entity write SQL

- `entity.BaseWriteSql`: common INSERT/UPDATE/DELETE assembly and value normalization.
- `entity.MapWriteSql` and `entity.BeanWriteSql`: traverse Map or Bean values.
- `entity.BeanIterator`: resolves readable Bean properties and mapping annotations.
- `entity.SqlIdentifier`: validates simple and qualified SQL identifiers.
- `annotation.Table`, `Column`, `Transient`, and `Id`: write mapping metadata; `Column.insertable` and `Column.updatable` affect Bean writes.

## SQL templates

- `sqltemplate.SqlXmlDomTemplate`: immutable DOM-backed template compiled from an inline SQL fragment or XML statement.
- `sqltemplate.xml.SqlXmlMgr`: scans and caches classpath XML mapper statements by ID. Default directory: `sql/`.
- `sqltemplate.ParameterBinder`: transforms `#{name}` into JDBC `?`, validates `${name}` identifiers, and combines named and positional values.
- `sqltemplate.ExpressionEvaluator`: template-condition SPI.
- `sqltemplate.express_parser.JSqlParserExpressionEvaluator`: constrained SQL-expression evaluator supporting logical/comparison/arithmetic expressions, parentheses, null tests, `BETWEEN`, `IN`, and explicitly registered functions.

XML uses `<mapper>` with direct `<sql id="...">` children. Dynamic nodes are `<if>`, an optional direct `<else>`, and `<forEach>`. `SqlXmlMgr` supports ordinary directory/JAR classpaths, not Spring resource patterns or nested/fat JARs.

## Pagination

- `page.PageQuery` executes count and page queries and fills `PageResult`.
- `page.PageControl` rewrites count and database-specific page SQL with JSqlParser.

## Results and diagnostics

- `model.CreateResult`, `UpdateResult`, and `Result` hold write outcomes.
- `BaseAction` maps JDBC values into Map/Bean/scalar results.
- `util.PrintRealSql` renders parameters for logging and throttles repeated business-action logs.

Logging is secondary behavior: it must not change the database operation's outcome.

## Documentation

- Source: `docs-src/src/**/*.md`, layouts, styles, and Eleventy config.
- Generated site: `docs/` or local `docs-src/dist/`; do not use generated pages as the editing source.
- Files ending in `-cn.md` are Chinese counterparts; files without `-cn` are English.
