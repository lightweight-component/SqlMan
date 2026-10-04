/**
 * Generates parameterized INSERT, UPDATE, and DELETE SQL from entity values.
 * <p>
 * {@link com.ajaxjs.sqlman.entity.BeanWriteSql} reads writable JavaBean properties,
 * while {@link com.ajaxjs.sqlman.entity.MapWriteSql} reads values from a map. Both
 * implementations use {@link com.ajaxjs.sqlman.entity.BaseWriteSql} to expose the
 * generated SQL statement and its ordered JDBC parameters.
 * </p>
 * <p>
 * Data values are represented by JDBC parameter placeholders. Table names, column
 * names, and identifier fields are SQL syntax rather than bindable values, so this
 * package validates them with {@link com.ajaxjs.sqlman.entity.SqlIdentifier}. Such
 * validation prevents malformed identifiers but does not authorize access to a
 * table; callers must still restrict dynamically selected tables to trusted
 * application configuration.
 * </p>
 */
package com.ajaxjs.sqlman.entity;
