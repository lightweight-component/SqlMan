# SqlMan contributor guide

## Baseline

- SqlMan 2.3 targets Java 8. Keep public production code compatible with Java 8.
- Read `pom.xml`, the affected source, direct callers, and tests before editing. Preserve unrelated working-tree changes.
- Run focused Maven tests first and `mvn -DskipTests=false test` before handing off a behavior change.

## Code layout

- `com.ajaxjs.sqlman`: `Action`, connection handling, and raw JDBC actions.
- `com.ajaxjs.sqlman.entity`: Map/Bean INSERT, UPDATE, and DELETE SQL generation.
- `com.ajaxjs.sqlman.page`: pagination models and JSqlParser page rewriting.
- `com.ajaxjs.sqlman.sqltemplate`: inline and XML dynamic SQL, parameter binding, and expression evaluation.
- `com.ajaxjs.sqlman.sqltemplate.xml.SqlXmlMgr`: classpath XML statement registry.

## SQL safety

- Bind data using JDBC `?` or template `#{name}`. Do not concatenate values into SQL.
- `${name}` is for validated identifiers only; it is not a raw SQL-fragment escape hatch.
- Validate table/column identifiers with `SqlIdentifier`; preserve the explicit APIs for raw `where` fragments and document their trust boundary.
- Do not let logging or cleanup failures hide the database operation failure. Thread-local connections must always be unbound after close.

## Templates

- XML statement files use a `<mapper>` root and direct `<sql id="...">` children. `SqlXmlMgr.init()` scans classpath `sql/`; `init("my/sql")` scans a custom logical directory.
- Dynamic nodes are `<if>`, optional child `<else>`, and `<forEach>`. The default evaluator is the restricted JSqlParser evaluator, not Spring EL.
- For a one-off SQL string, use `SqlXmlDomTemplate.compile(sql).render(params)` or let `Action` process a Map first parameter.

## Documentation

- Edit documentation source under `docs-src/`, never generated `docs/` output.
- Keep English and Chinese pages aligned when public behavior changes.
- Update `README.md`, `README.zh-CN.md`, JavaDoc, and `skills/sql-man/` when a public API or workflow changes.
