package com.ajaxjs.sqlman.entity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Generates write SQL statements from map-backed entity values.
 * <p>
 * Each map key represents a database column name and must be a simple SQL identifier.
 * Map values are converted to JDBC-compatible parameter values and bound through
 * placeholders; they are never concatenated directly into SQL text.
 * </p>
 * <p>
 * The target table name may be a one-, two-, or three-part unquoted name, such as
 * {@code orders}, {@code app.orders}, or {@code catalog.app.orders}.
 * </p>
 */
public class MapWriteSql extends BaseWriteSql {
    /**
     * Source values keyed by database column name.
     * <p>
     * The map is retained by reference and is read when SQL is generated. Callers
     * requiring a stable iteration order should provide an ordered map, such as
     * {@link java.util.LinkedHashMap}.
     * </p>
     */
    final Map<String, Object> entityMap;

    /**
     * Creates a write SQL generator for the supplied table and map-backed values.
     *
     * @param tableName the target table name, optionally qualified by schema or catalog
     * @param entityMap source values keyed by database column name
     * @throws NullPointerException     if {@code tableName} or {@code entityMap} is {@code null}
     * @throws IllegalArgumentException if {@code tableName} is not a valid qualified table name
     */
    public MapWriteSql(String tableName, Map<String, Object> entityMap) {
        Objects.requireNonNull(tableName, "MapWriteSql.tableName");
        Objects.requireNonNull(entityMap, "MapWriteSql.entityMap");

        this.entityMap = entityMap;
        setTableName(tableName);
    }

    /**
     * Appends every map entry as an INSERT column and a JDBC parameter placeholder.
     *
     * @param sb           the SQL builder
     * @param values       the collected parameter values
     * @param valuesHolder the collected value placeholders
     * @throws IllegalArgumentException if a map key is not a valid SQL column identifier
     */
    @Override
    void everyFieldInsert(StringBuilder sb, List<Object> values, List<String> valuesHolder) {
        entityMap.forEach((field, value) -> {
            sb.append("`").append(SqlIdentifier.check(field)).append("`, ");
            valuesHolder.add("?");
            values.add(toSqlValue(value));
        });
    }

    /**
     * Appends every map entry except the identifier field as an UPDATE assignment.
     *
     * @param sb      the SQL builder
     * @param values  the collected parameter values
     * @param idField the identifier field to exclude from the SET clause, or {@code null}
     * @throws IllegalArgumentException if a map key is not a valid SQL column identifier
     */
    @Override
    void everyFieldUpdate(StringBuilder sb, List<Object> values, String idField) {
        entityMap.forEach((field, value) -> {
            String column = SqlIdentifier.check(field);

            if (column.equals(idField)) // 跳过 id
                return;

            sb.append(" `").append(column).append("` = ?,");
            values.add(toSqlValue(value));
        });
    }

    /**
     * Obtains an identifier value from the source map.
     *
     * @param idField the identifier column name
     * @return the mapped identifier value, or {@code null} when the key is absent or maps to {@code null}
     */
    @Override
    Object getIdValueByIdField(String idField) {
        return entityMap.get(idField);
    }
}
