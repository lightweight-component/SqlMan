package com.ajaxjs.sqlman.entity;

import com.ajaxjs.sqlman.annotation.Column;
import com.ajaxjs.sqlman.annotation.Table;
import com.ajaxjs.sqlman.annotation.Transient;
import com.ajaxjs.sqlman.model.meta.DbMetaInfoUpdate;

import java.util.List;
import java.util.Objects;

/**
 * Generates write SQL statements from JavaBean properties.
 * <p>
 * Readable, non-{@link Transient transient} properties with non-null values are
 * included by default. A {@link Column} annotation on the getter takes precedence
 * over one on the backing field. Its {@link Column#name() name},
 * {@link Column#insertable() insertable}, and {@link Column#updatable() updatable}
 * settings control the generated column and write operations.
 * </p>
 */
public class BeanWriteSql extends BaseWriteSql {
    /**
     * Source entity whose readable properties provide column values.
     */
    final Object entityBean;

    /**
     * Creates a generator using the table name declared by the entity's {@link Table} annotation.
     *
     * @param entityBean the source entity
     * @throws NullPointerException     if {@code entityBean} is {@code null} or has no {@link Table} annotation
     * @throws IllegalArgumentException if the annotated table name is not a valid qualified table name
     */
    public BeanWriteSql(Object entityBean) {
        this(getTableNameByBean(Objects.requireNonNull(entityBean, "BeanWriteSql.entityBean")), entityBean);
    }

    /**
     * Creates a generator for the supplied table and source entity.
     *
     * @param tableName  the target table name, optionally qualified by schema or catalog
     * @param entityBean the source entity
     * @throws NullPointerException     if {@code tableName} or {@code entityBean} is {@code null}
     * @throws IllegalArgumentException if {@code tableName} is not a valid qualified table name
     */
    public BeanWriteSql(String tableName, Object entityBean) {
        Objects.requireNonNull(tableName, "BeanWriteSql.tableName");
        Objects.requireNonNull(entityBean, "BeanWriteSql.entityBean");

        this.entityBean = entityBean;
        setTableName(tableName);
    }

    /**
     * Appends insertable Bean properties as INSERT columns and JDBC parameter placeholders.
     *
     * @param sb           the SQL builder
     * @param values       the collected parameter values
     * @param valuesHolder the collected value placeholders
     * @throws IllegalArgumentException if a resolved column name is not a valid SQL identifier
     */
    @Override
    void everyFieldInsert(StringBuilder sb, List<Object> values, List<String> valuesHolder) {
        BeanIterator iterator = new BeanIterator(entityBean);
        iterator.setInsert(true);
        iterator.each((field, value) -> {
            sb.append("`").append(field).append("`, ");
            valuesHolder.add("?");
            values.add(toSqlValue(value));
        });
    }

    /**
     * Appends updatable Bean properties, except the identifier field, as UPDATE assignments.
     *
     * @param sb      the SQL builder
     * @param values  the collected parameter values
     * @param idField the identifier field to exclude from the SET clause, or {@code null}
     * @throws IllegalArgumentException if a resolved column name is not a valid SQL identifier
     */
    @Override
    void everyFieldUpdate(StringBuilder sb, List<Object> values, String idField) {
        BeanIterator iterator = new BeanIterator(entityBean);
        iterator.setInsert(false);
        iterator.each((field, value) -> {
            if (field.equals(idField)) // 忽略 id
                return;

            sb.append(" `").append(field).append("` = ?,");
            values.add(toSqlValue(value));
        });
    }

    /**
     * Obtains an identifier value from the source entity.
     *
     * @param idField the identifier column name
     * @return the identifier value, or {@code null} when it is unavailable
     */
    @Override
    Object getIdValueByIdField(String idField) {
        return new DbMetaInfoUpdate(entityBean, idField).getIdValue();
    }

    /**
     * Resolves an entity table name from its {@link Table} annotation.
     *
     * @param javaBean the entity bean
     * @return the annotated table name, or {@code null} when absent.
     * @throws NullPointerException if {@code javaBean} is {@code null}
     */
    public static String getTableNameByBean(Object javaBean) {
        Table annotation = javaBean.getClass().getAnnotation(Table.class);

        return annotation == null ? null : annotation.value();
    }
}
