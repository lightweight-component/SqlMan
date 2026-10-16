package com.ajaxjs.sqlman.sqltemplate;

import com.ajaxjs.sqlman.model.PreparedSql;
import com.ajaxjs.sqlman.sqltemplate.model.RenderedSql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts rendered SQL named placeholders into JDBC SQL and ordered values.
 */
public final class ParameterBinder {
    private static final Pattern PLACEHOLDER = Pattern.compile("(#\\{|\\$\\{)(.*?)(})");
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*");

    private ParameterBinder() {
    }

    /**
     * Binds one dynamic XML rendering result.
     *
     * @param renderedSql      rendered SQL and named parameter values
     * @param positionalParams values for ordinary JDBC {@code ?} placeholders
     * @return JDBC SQL with its ordered parameter values
     */
    public static PreparedSql prepare(RenderedSql renderedSql, Object... positionalParams) {
        if (renderedSql == null)
            throw new IllegalArgumentException("Rendered SQL must not be null");

        return prepare(renderedSql.getSql(), renderedSql.getParams(), positionalParams);
    }

    /**
     * Binds {@code #{...}} values and substitutes validated {@code ${...}}
     * identifiers in already-rendered SQL. Placeholders inside string literals,
     * quoted identifiers and SQL comments are preserved verbatim.
     *
     * @param sqlTemplate      rendered SQL containing named and/or JDBC placeholders
     * @param params           values used by named placeholders; {@code null} means no named values
     * @param positionalParams values for ordinary JDBC {@code ?} placeholders; may be {@code null}
     * @return JDBC SQL with its ordered parameter values
     */
    public static PreparedSql prepare(String sqlTemplate, Map<String, Object> params, Object[] positionalParams) {
        if (sqlTemplate == null)
            throw new IllegalArgumentException("SQL template must not be null");

        Map<String, Object> safeParams = params == null ? Collections.emptyMap() : params;
        return bindParameters(sqlTemplate, safeParams, positionalParams == null ? new Object[0] : positionalParams);
    }

    /**
     * Determines whether SQL contains a named template placeholder.
     *
     * @param sql SQL text to inspect
     * @return {@code true} if {@code #{...}} or {@code ${...}} occurs
     */
    public static boolean hasPlaceholder(String sql) {
        return sql != null && PLACEHOLDER.matcher(sql).find();
    }

    private static PreparedSql bindParameters(String template, Map<String, Object> params, Object[] positionalParams) {
        StringBuilder sql = new StringBuilder(template.length());
        List<Object> bindings = new ArrayList<>();
        boolean singleQuote = false, doubleQuote = false, backtick = false, lineComment = false, blockComment = false;
        int positionalIndex = 0;
        Matcher placeholder = PLACEHOLDER.matcher(template);

        for (int i = 0; i < template.length(); i++) {
            char current = template.charAt(i);
            char next = i + 1 < template.length() ? template.charAt(i + 1) : '\0';

            if (lineComment) {
                sql.append(current);

                if (current == '\n' || current == '\r')
                    lineComment = false;

                continue;
            }

            if (blockComment) {
                sql.append(current);

                if (current == '*' && next == '/') {
                    sql.append(next);
                    i++;
                    blockComment = false;
                }

                continue;
            }

            if (singleQuote) {
                sql.append(current);

                if (current == '\'' && next == '\'') {
                    sql.append(next);
                    i++;
                } else if (current == '\\' && next != '\0') {
                    sql.append(next);
                    i++;
                } else if (current == '\'')
                    singleQuote = false;

                continue;
            }

            if (doubleQuote) {
                sql.append(current);

                if (current == '"' && next == '"') {
                    sql.append(next);
                    i++;
                } else if (current == '"')
                    doubleQuote = false;

                continue;
            }

            if (backtick) {
                sql.append(current);

                if (current == '`' && next == '`') {
                    sql.append(next);
                    i++;
                } else if (current == '`')
                    backtick = false;

                continue;
            }

            // Recognize placeholders only in executable SQL, before '#' can start a comment.
            if ((current == '#' || current == '$') && next == '{' && placeholder.region(i, template.length()).lookingAt()) {
                String name = placeholder.group(2);

                if (current == '#') {
                    sql.append('?');
                    bindings.add(params.get(name));
                } else {
                    Object value = params.get(name);
                    String identifier = value == null ? null : value.toString();

                    if (identifier == null || !IDENTIFIER.matcher(identifier).matches())
                        throw new IllegalArgumentException("Template identifier " + name + " is not allowlisted: " + identifier);

                    sql.append(identifier);
                }

                i = placeholder.end() - 1;
            } else if ((current == '-' && next == '-') || current == '#') {
                sql.append(current);

                if (current == '-') {
                    sql.append(next);
                    i++;
                }

                lineComment = true;
            } else if (current == '/' && next == '*') {
                sql.append(current).append(next);
                i++;
                blockComment = true;
            } else if (current == '\'') {
                sql.append(current);
                singleQuote = true;
            } else if (current == '"') {
                sql.append(current);
                doubleQuote = true;
            } else if (current == '`') {
                sql.append(current);
                backtick = true;
            } else if (current == '?') {
                if (positionalIndex >= positionalParams.length)
                    throw new IllegalArgumentException("Not enough JDBC parameters for SQL template");

                sql.append(current);
                bindings.add(positionalParams[positionalIndex++]);
            } else
                sql.append(current);
        }

        if (positionalIndex != positionalParams.length)
            throw new IllegalArgumentException("Too many JDBC parameters for SQL template");

        return new PreparedSql(sql.toString(), bindings.toArray());
    }
}
