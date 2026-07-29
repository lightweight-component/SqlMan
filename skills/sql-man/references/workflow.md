# Workflow

## Inspect

```bash
git status --short
rg --files src/main/java src/test/java docs-src
rg -n "symbol-or-api" src/main/java src/test/java docs-src
```

Read the current implementation, direct callers, tests, and documentation before deciding whether behavior is intentional.

## Edit

- Use focused patches.
- Preserve Java 8 compatibility unless `pom.xml` changes the baseline.
- Preserve user edits and avoid formatting unrelated files.
- Update JavaDoc and bilingual docs when public behavior changes.

## Test Java

Run a focused regression first:

```bash
mvn -DskipTests=false -Dtest=TestClassName test
```

Then run the suite:

```bash
mvn -DskipTests=false test
```

Inspect the Surefire summary. A successful Maven exit is insufficient if inherited configuration reports tests were skipped.

Useful test dimensions:

- H2 for real prepared statements, generated keys, paging, and transactions.
- JDBC proxies for vendor product names, identifier quote strings, failures, and unusual return types.
- boundary cases: empty inputs, nulls, overflow, comments/literals containing `?`, page-size zero, offset past end;
- failure preservation: primary exception plus rollback, close, restoration, or logging failures.

## Test documentation

From `docs-src`:

```bash
npm install
npx @11ty/eleventy
```

Then scan source and generated output:

```bash
rg -n "new Sql\\b|new Entity\\b|queryList\\(|queryOne\\(|description: TODO|last one" . --glob "*.md"
```

Verify every Markdown file contains non-empty `title`, `description`, and `tags`, and ensure English/Chinese examples use the same current API.

## Hand off

State:

- files and behavior changed;
- compatibility decisions;
- focused and full test commands with actual counts;
- any test skipping, dependency audit result, or unresolved limitation.
