package com.ajaxjs.sqlman.xml.express_parser;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Evaluates the restricted JSqlParser AST accepted by
 * {@link JSqlParserExpressionEvaluator}.
 */
class JSqlParserExpressionVisitor extends ExpressionVisitorAdapter {
    private static final Object UNSET = new Object();

    private final Map<String, Object> params;
    private final Map<String, SqlTemplateFunction> functions;
    private Object result = UNSET;

    JSqlParserExpressionVisitor(Map<String, Object> params, Map<String, SqlTemplateFunction> functions) {
        this.params = params;
        this.functions = functions;
    }

    Object evaluate(Expression expression) {
        Object previous = result;
        result = UNSET;

        try {
            expression.accept(this);

            if (result == UNSET)
                throw unsupported(expression);

            return result;
        } finally {
            result = previous;
        }
    }

    @Override
    public void visit(NullValue value) {
        result = null;
    }

    @Override
    public void visit(StringValue value) {
        result = value.getValue();
    }

    @Override
    public void visit(LongValue value) {
        result = BigDecimal.valueOf(value.getValue());
    }

    @Override
    public void visit(DoubleValue value) {
        result = new BigDecimal(value.toString());
    }

    @Override
    public void visit(Parenthesis value) {
        result = evaluate(value.getExpression());
    }

    @Override
    public void visit(SignedExpression value) {
        BigDecimal number = asNumber(evaluate(value.getExpression()), "signed expression");

        if (value.getSign() == '+')
            result = number;
        else if (value.getSign() == '-')
            result = number.negate();
        else
            throw unsupported(value);
    }

    @Override
    public void visit(Column column) {
        if (column.getTable() != null && column.getTable().getName() != null)
            throw new IllegalArgumentException("Qualified parameter names are not supported: " + column);

        String name = column.getColumnName();

        if ("TRUE".equalsIgnoreCase(name)) {
            result = Boolean.TRUE;
            return;
        }

        if ("FALSE".equalsIgnoreCase(name)) {
            result = Boolean.FALSE;
            return;
        }

        if (!params.containsKey(name))
            throw new IllegalArgumentException("Template parameter not found: " + name);

        result = params.get(name);
    }

    @Override
    public void visit(Addition value) {
        result = asNumber(evaluate(value.getLeftExpression()), "+").add(asNumber(evaluate(value.getRightExpression()), "+"));
    }

    @Override
    public void visit(Subtraction value) {
        result = asNumber(evaluate(value.getLeftExpression()), "-").subtract(asNumber(evaluate(value.getRightExpression()), "-"));
    }

    @Override
    public void visit(Multiplication value) {
        result = asNumber(evaluate(value.getLeftExpression()), "*").multiply(asNumber(evaluate(value.getRightExpression()), "*"));
    }

    @Override
    public void visit(Division value) {
        BigDecimal divisor = asNumber(evaluate(value.getRightExpression()), "/");

        if (BigDecimal.ZERO.compareTo(divisor) == 0)
            throw new IllegalArgumentException("Division by zero in template condition");

        result = asNumber(evaluate(value.getLeftExpression()), "/").divide(divisor, MathContext.DECIMAL128);
    }

    @Override
    public void visit(IntegerDivision value) {
        BigDecimal divisor = asNumber(evaluate(value.getRightExpression()), "DIV");

        if (BigDecimal.ZERO.compareTo(divisor) == 0)
            throw new IllegalArgumentException("Division by zero in template condition");

        result = asNumber(evaluate(value.getLeftExpression()), "DIV").divideToIntegralValue(divisor);
    }

    @Override
    public void visit(Modulo value) {
        BigDecimal divisor = asNumber(evaluate(value.getRightExpression()), "%");

        if (BigDecimal.ZERO.compareTo(divisor) == 0)
            throw new IllegalArgumentException("Division by zero in template condition");

        result = asNumber(evaluate(value.getLeftExpression()), "%").remainder(divisor);
    }

    @Override
    public void visit(AndExpression value) {
        boolean left = asBoolean(evaluate(value.getLeftExpression()), "AND");
        result = left && asBoolean(evaluate(value.getRightExpression()), "AND");
    }

    @Override
    public void visit(OrExpression value) {
        boolean left = asBoolean(evaluate(value.getLeftExpression()), "OR");
        result = left || asBoolean(evaluate(value.getRightExpression()), "OR");
    }

    @Override
    public void visit(NotExpression value) {
        result = !asBoolean(evaluate(value.getExpression()), "NOT");
    }

    @Override
    public void visit(EqualsTo value) {
        result = equalsValue(evaluate(value.getLeftExpression()), evaluate(value.getRightExpression()));
    }

    @Override
    public void visit(NotEqualsTo value) {
        Object left = evaluate(value.getLeftExpression());
        Object right = evaluate(value.getRightExpression());
        result = left != null && right != null && !equalsValue(left, right);
    }

    @Override
    public void visit(GreaterThan value) {
        Integer comparison = compare(value);
        result = comparison != null && comparison > 0;
    }

    @Override
    public void visit(GreaterThanEquals value) {
        Integer comparison = compare(value);
        result = comparison != null && comparison >= 0;
    }

    @Override
    public void visit(MinorThan value) {
        Integer comparison = compare(value);
        result = comparison != null && comparison < 0;
    }

    @Override
    public void visit(MinorThanEquals value) {
        Integer comparison = compare(value);
        result = comparison != null && comparison <= 0;
    }

    @Override
    public void visit(IsNullExpression value) {
        boolean isNull = evaluate(value.getLeftExpression()) == null;
        result = value.isNot() ? !isNull : isNull;
    }

    @Override
    public void visit(Between value) {
        Object target = evaluate(value.getLeftExpression());
        Object start = evaluate(value.getBetweenExpressionStart());
        Object end = evaluate(value.getBetweenExpressionEnd());
        boolean matches = target != null && start != null && end != null && compare(target, start) >= 0 && compare(target, end) <= 0;
        result = target != null && start != null && end != null && (value.isNot() ? !matches : matches);
    }

    @Override
    public void visit(InExpression value) {
        Object target = evaluate(value.getLeftExpression());
        Expression right = value.getRightExpression();

        if (!(right instanceof ExpressionList<?>))
            throw unsupported(value);

        boolean matches = false;

        if (target != null) {
            for (Expression candidate : ((ExpressionList<?>) right).getExpressions()) {
                if (equalsValue(target, evaluate(candidate))) {
                    matches = true;
                    break;
                }
            }
        }

        result = target != null && (value.isNot() ? !matches : matches);
    }

    @Override
    public void visit(Function value) {
        if (value.isAllColumns() || value.isDistinct() || value.getNamedParameters() != null)
            throw unsupported(value);

        SqlTemplateFunction function = functions.get(JSqlParserExpressionEvaluator.normalizeName(value.getName()));

        if (function == null)
            throw new IllegalArgumentException("Template function is not registered: " + value.getName());

        List<Object> arguments = new ArrayList<>();
        ExpressionList<?> parameters = value.getParameters();

        if (parameters != null) {
            for (Expression parameter : parameters.getExpressions())
                arguments.add(evaluate(parameter));
        }

        result = function.apply(Collections.unmodifiableList(arguments), params);
    }

    private Integer compare(BinaryExpression expression) {
        Object left = evaluate(expression.getLeftExpression());
        Object right = evaluate(expression.getRightExpression());

        if (left == null || right == null)
            return null;

        return compare(left, right);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int compare(Object left, Object right) {
        if (left instanceof Number && right instanceof Number)
            return asNumber(left, "comparison").compareTo(asNumber(right, "comparison"));

        if (left.getClass().isInstance(right) && left instanceof Comparable)
            return ((Comparable) left).compareTo(right);

        throw new IllegalArgumentException("Values cannot be compared: " + left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
    }

    private static boolean equalsValue(Object left, Object right) {
        if (left == null || right == null)
            return false;

        if (left instanceof Number && right instanceof Number)
            return asNumber(left, "comparison").compareTo(asNumber(right, "comparison")) == 0;

        return left.equals(right);
    }

    private static boolean asBoolean(Object value, String operator) {
        if (!(value instanceof Boolean))
            throw new IllegalArgumentException(operator + " requires a boolean value");

        return (Boolean) value;
    }

    private static BigDecimal asNumber(Object value, String operator) {
        if (!(value instanceof Number))
            throw new IllegalArgumentException(operator + " requires a numeric value");

        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value in template condition: " + value, e);
        }
    }

    private static IllegalArgumentException unsupported(Expression expression) {
        return new IllegalArgumentException("Unsupported template expression: " + expression.getClass().getSimpleName());
    }
}
