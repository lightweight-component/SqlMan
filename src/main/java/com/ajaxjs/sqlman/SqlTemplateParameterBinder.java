package com.ajaxjs.sqlman;

import com.ajaxjs.sqlman.xml.RenderedSql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts rendered SQL named placeholders into JDBC SQL and ordered values.
 */
public final class SqlTemplateParameterBinder {
    private static final Pattern PLACEHOLDER = Pattern.compile("(#\\{|\\$\\{)(.*?)(})");
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*");

    private SqlTemplateParameterBinder() {
    }

    /**
     * Binds one dynamic XML rendering result.
     */
    public static SmallMyBatis.PreparedSql prepare(RenderedSql renderedSql, Object... positionalParams) {
        if (renderedSql == null)
            throw new IllegalArgumentException("Rendered SQL must not be null");

        return prepare(renderedSql.getSql(), renderedSql.getParams(), positionalParams);
    }

    /**
     * Binds {@code #{...}} values and substitutes validated {@code ${...}}
     * identifiers in already-rendered SQL.
     */
    public static SmallMyBatis.PreparedSql prepare(String sqlTemplate, Map<String, Object> params, Object[] positionalParams) {
        if (sqlTemplate == null)
            throw new IllegalArgumentException("SQL template must not be null");

        Map<String, Object> safeParams = params == null ? Collections.<String, Object>emptyMap() : params;
        Matcher matcher = PLACEHOLDER.matcher(sqlTemplate);
        StringBuffer markedSql = new StringBuffer();
        List<Object> namedValues = new ArrayList<>();

        while (matcher.find()) {
            String name = matcher.group(2);

            if ("#{".equals(matcher.group(1))) {
                String marker = "\u0001" + namedValues.size() + "\u0002";
                namedValues.add(safeParams.get(name));
                matcher.appendReplacement(markedSql, Matcher.quoteReplacement(marker));
            } else {
                Object value = safeParams.get(name);
                String identifier = value == null ? null : value.toString();

                if (identifier == null || !IDENTIFIER.matcher(identifier).matches())
                    throw new IllegalArgumentException("Template identifier " + name + " is not allowlisted: " + identifier);

                matcher.appendReplacement(markedSql, Matcher.quoteReplacement(identifier));
            }
        }

        matcher.appendTail(markedSql);
        return bindMarkers(markedSql.toString(), namedValues, positionalParams == null ? new Object[0] : positionalParams);
    }

    public static boolean hasPlaceholder(String sql) {
        return sql != null && PLACEHOLDER.matcher(sql).find();
    }

    private static SmallMyBatis.PreparedSql bindMarkers(String markedSql, List<Object> namedValues, Object[] positionalParams) {
        StringBuilder sql = new StringBuilder(markedSql.length());
        List<Object> bindings = new ArrayList<>();
        boolean singleQuote = false, doubleQuote = false, backtick = false, lineComment = false, blockComment = false;
        int positionalIndex = 0;

        for (int i = 0; i < markedSql.length(); i++) {
            char current = markedSql.charAt(i);
            char next = i + 1 < markedSql.length() ? markedSql.charAt(i + 1) : '\0';

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

            if (current == '\u0001') {
                int end = markedSql.indexOf('\u0002', i + 1);
                if (end < 0)
                    throw new IllegalArgumentException("Invalid named SQL parameter marker");

                int index = Integer.parseInt(markedSql.substring(i + 1, end));
                sql.append('?');
                bindings.add(namedValues.get(index));
                i = end;
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

        return new SmallMyBatis.PreparedSql(sql.toString(), bindings.toArray());
    }
}
