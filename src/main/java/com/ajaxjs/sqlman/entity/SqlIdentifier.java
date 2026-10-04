package com.ajaxjs.sqlman.entity;

import com.ajaxjs.util.ObjectHelper;

import java.util.regex.Pattern;

/**
 * Validates unquoted SQL identifiers used by generated write statements.
 * <p>
 * A simple identifier must start with an ASCII letter or underscore and may then
 * contain only ASCII letters, digits, and underscores. Quoted identifiers,
 * whitespace, SQL expressions, and punctuation are rejected.
 * </p>
 */
public class SqlIdentifier {
    private static final Pattern PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    /**
     * Validates a single unquoted SQL identifier, such as a column name.
     *
     * @param name the identifier to validate
     * @return the original validated identifier
     * @throws IllegalArgumentException if {@code name} is blank or not a simple SQL identifier
     */
    public static String check(String name) {
        if (ObjectHelper.isEmptyText(name))
            throw new IllegalArgumentException("SQL identifier must not be blank.");

        if (!PATTERN.matcher(name).matches())
            throw new IllegalArgumentException("Illegal SQL identifier: " + name);

        return name;
    }

    /**
     * Validates an unquoted table name with one to three identifier parts.
     *
     * @param tableName the table name, optionally qualified by schema or catalog
     * @return the validated table name
     * @throws IllegalArgumentException if {@code tableName} is blank, has more than three parts,
     *                                  or contains an invalid identifier part
     */
    public static String checkTableName(String tableName) {
        if (ObjectHelper.isEmptyText(tableName))
            throw new IllegalArgumentException("SQL table name must not be blank.");

        String[] parts = tableName.split("\\.", -1);

        if (parts.length > 3)
            throw new IllegalArgumentException("Illegal qualified SQL table name: " + tableName);

        for (String part : parts)
            check(part);

        return tableName;
    }
}
