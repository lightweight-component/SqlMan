---
name: java-doc-maintenance
description: Maintain Java source documentation by adding missing JavaDoc for types, members, and non-test methods while preserving existing comments and implementation text.
---

# JavaDoc Maintenance

Use this skill when completing or normalizing Java API documentation in this repository.

- Inspect the declaration and its callers before writing a new comment. Document the behavior actually implemented; do not infer unsupported guarantees.
- Preserve every existing comment verbatim. Do not add method-body comments or alter ordinary implementation comments.
- For a declaration with a preceding `//` documentation comment, convert that comment to a `/** ... */` JavaDoc block without changing its language or wording.
- Add JavaDoc to undocumented classes, interfaces, enums, annotations, fields, constructors, and non-test methods, including private members. Do not add JavaDoc to unit-test methods; retain any documentation already present on them.
- New method and constructor JavaDoc describes the operation and includes `@param` for each parameter, `@return` for non-void methods, and `@throws` for declared or meaningfully propagated exceptions. Use the source's existing Chinese or English style where one is evident.
- Put a blank line between consecutive field JavaDoc/declaration groups. Do not reformat unrelated code.
- Treat `src/test` methods annotated with test lifecycle or test annotations, and methods in test classes whose purpose is a test case, as unit-test methods.
- Verify that new JavaDoc has balanced delimiters, that tags name real parameters, and that the Maven build still passes.
