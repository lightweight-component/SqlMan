package com.ajaxjs.sqlman.xml.express_parser;

import com.ajaxjs.sqlman.xml.SqlTemplateExpressionEvaluator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A restricted dynamic-SQL condition evaluator backed by JSqlParser.
 *
 * <p>This evaluator parses expressions only; it never executes SQL. It accepts
 * a small, explicit subset of the JSqlParser AST: boolean logic, comparisons,
 * arithmetic, parentheses, {@code IS NULL}, {@code BETWEEN}, {@code IN}, and
 * functions registered by the caller. All other AST nodes are rejected.</p>
 */
public class JSqlParserExpressionEvaluator implements SqlTemplateExpressionEvaluator {
    private final Map<String, SqlTemplateFunction> functions;
    private final ConcurrentMap<String, Expression> expressions = new ConcurrentHashMap<>();

    /**
     * Creates an evaluator without custom functions.
     */
    public JSqlParserExpressionEvaluator() {
        this(Collections.<String, SqlTemplateFunction>emptyMap());
    }

    /**
     * @param functions functions explicitly available to template expressions,
     *                  indexed by case-insensitive name.
     */
    public JSqlParserExpressionEvaluator(Map<String, SqlTemplateFunction> functions) {
        if (functions == null)
            throw new IllegalArgumentException("Functions must not be null");

        Map<String, SqlTemplateFunction> copied = new HashMap<>();

        for (Map.Entry<String, SqlTemplateFunction> entry : functions.entrySet()) {
            String name = entry.getKey();

            if (name == null || name.trim().isEmpty() || entry.getValue() == null)
                throw new IllegalArgumentException("Function name and implementation must not be empty");

            copied.put(normalizeName(name), entry.getValue());
        }

        this.functions = Collections.unmodifiableMap(copied);
    }

    @Override
    public boolean evaluate(String expression, Map<String, Object> params) {
        if (expression == null || expression.trim().isEmpty())
            throw new IllegalArgumentException("Template condition must not be empty");

        Map<String, Object> scope = params == null ? Collections.<String, Object>emptyMap() : params;
        Object value = new JSqlParserExpressionVisitor(scope, functions).evaluate(parse(expression));

        if (!(value instanceof Boolean))
            throw new IllegalArgumentException("Template condition must evaluate to boolean: " + expression);

        return (Boolean) value;
    }

    private Expression parse(String expression) {
        Expression parsed = expressions.get(expression);

        if (parsed != null)
            return parsed;

        try {
            parsed = CCJSqlParserUtil.parseCondExpression(expression);
        } catch (JSQLParserException e) {
            throw new IllegalArgumentException("Invalid template condition: " + expression, e);
        }

        Expression previous = expressions.putIfAbsent(expression, parsed);

        return previous == null ? parsed : previous;
    }

    static String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
