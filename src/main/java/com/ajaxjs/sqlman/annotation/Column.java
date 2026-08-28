package com.ajaxjs.sqlman.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the mapping of a persistent property or field.
 *
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * (Optional) The name of the column. Defaults to the property
     * or field name of the perishable class.
     *
     * @return the configured column name, or an empty string for the default.
     */
    String name() default "";

    /**
     * (Optional) Whether the column is included in the generated
     * SELECT statement for find and query operations.
     *
     * @return whether the column may be inserted.
     */
    boolean insertable() default true;

    /**
     * (Optional) Whether the column is included in the generated
     * UPDATE statement for the entity.
     *
     * @return whether the column may be updated.
     */
    boolean updatable() default true;

    /**
     * (Optional) Whether the column is a unique key. This is a
     * shortcut for the UniqueConstraint annotation at the table level
     * and is useful for single-column primary keys.
     *
     * @return whether the column is unique.
     */
    boolean unique() default false;

    /**
     * (Optional) Whether the column may be null. Defaults to true.
     *
     * @return whether the column is nullable.
     */
    boolean nullable() default true;

    /**
     * (Optional) The column length. It should be set for string
     * columns (such as CHAR, VARCHAR) and tinyint columns that store
     * boolean values.
     *
     * @return the configured column length.
     */
    int length() default 255;

    /**
     * (Optional) For numeric or date columns, specifies the precision
     * to which the value is stored.
     *
     * @return the configured precision.
     */
    int precision() default 0;

    /**
     * (Optional) For floating-point or decimal columns, specifies
     * the number of digits after the decimal point.
     *
     * @return the configured scale.
     */
    int scale() default 0;

    /**
     * (Optional) The SQL fragment that is used when generating the
     * DDL for the column.
     *
     * @return the column-definition SQL fragment.
     */
    String columnDefinition() default "";

    /**
     * (Optional) The table in which the column is created in a
     * secondary table scenario.
     *
     * @return the target table name, or an empty string for the default.
     */
    String table() default "";
}
