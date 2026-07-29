---
name: sql-man
description: Maintain, review, debug, test, and document the SqlMan Java JDBC library. Use for work in the SqlMan repository involving Action, JdbcConnection, Query/Create/Update/BatchUpdate, pagination, entity-to-SQL generation, result mapping, SmallMyBatis XML SQL, SQL logging, database-vendor compatibility, Maven tests, or the bilingual Eleventy documentation under docs-src.
---

# SqlMan

Work from the current repository state. Treat reference files as orientation, then verify behavior against source because the API is evolving.

## Start every task

1. Read `pom.xml` and inspect `git status --short`.
2. Locate the relevant source and tests with `rg`; do not infer an API from generated docs.
3. Read [references/architecture.md](references/architecture.md) when crossing packages or tracing execution.
4. Read [references/api.md](references/api.md) for public API conventions and compatibility constraints.
5. Read [references/workflow.md](references/workflow.md) before implementing, testing, or editing documentation.

Preserve unrelated working-tree changes. Do not modify generated `docs/` when the source of truth is `docs-src/`.

## Review and diagnose

Trace the full path:

```text
Action input
  -> SQL/template or entity SQL generation
  -> PreparedStatement parameter binding
  -> JDBC execution and resource cleanup
  -> result conversion
  -> SQL logging
```

Check both raw-SQL and Map/JavaBean entry points. Pay special attention to:

- empty/null input and reused mutable `Action` state;
- SQL identifiers versus bindable data values;
- quotes, comments, and literals when parsing SQL;
- numeric conversion and overflow;
- missing getters/setters and reflection exceptions;
- transaction ownership, auto-commit restoration, rollback, and suppressed exceptions;
- connection ownership and thread-local cleanup;
- empty results and offsets beyond the final page;
- database product names, identifier quoting, and pagination syntax;
- logging failures masking successful or failed database operations;
- API compatibility when changing public constructors or methods.

Report findings by severity with exact file and line references. Do not repeat issues already fixed in the current tree.

## Implement changes

Prefer narrow internal helpers and overloads over breaking public APIs. Retain a deprecated delegating overload when replacing a reasonable public method.

Use `PreparedStatement` placeholders for data. Treat table names, column names, ordering, and raw WHERE fragments as identifiers or SQL syntax; validate or constrain them rather than pretending they are bind parameters.

Keep transaction behavior explicit:

- join caller-managed transactions without committing or rolling back them;
- create, finish, and restore only transactions opened locally;
- restore connection state in `finally`;
- attach rollback/restoration failures as suppressed exceptions when a primary failure exists.

Close statements and result sets with try-with-resources. Close a connection only when the current API clearly owns it.

## Test proportionally

Add focused regression tests for the failure mode, including negative and boundary cases. Prefer H2-backed tests for JDBC behavior and proxies/mocks for driver-specific metadata or generated-key types.

Run focused tests first, then the relevant suite. Confirm from Maven output that tests actually ran; inherited Surefire configuration may skip them unless explicitly overridden. See [references/workflow.md](references/workflow.md).

## Maintain documentation

Treat every Markdown file under `docs-src` as Eleventy/Nunjucks source. Keep English and `-cn` Chinese pages aligned.

Every Markdown front matter must contain meaningful, non-placeholder:

```yaml
title:
description:
tags:
```

Document the current source API, including limitations that materially affect safe usage. Build with Eleventy after edits and scan for removed API names. Do not hand-edit generated site output.
