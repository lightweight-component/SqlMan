package com.ajaxjs.sqlman.expression;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class Evaluator implements Visitor<Object> {
    private final Map<String, Object> context; // 简化的上下文

    Evaluator(Map<String, Object> context) {
        this.context = context;
    }

    Object evaluate(Expr expression) {
        return expression.accept(this);
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        // 在上下文中查找变量
        Object value = context.get(expr.name);

        if (value == null && !context.containsKey(expr.name))
            System.err.println("Warning: Variable '" + expr.name + "' not found in context.");
        // 根据需求，可以选择返回 null, 抛出异常等

        return value;
    }

    @Override
    public Object visitGetPropertyExpr(Expr.GetProperty expr) {
        Object object = evaluate(expr.object);
        if (object == null)
            return null; // 对空对象访问属性返回null

        String property = expr.property;

        try {
            // 使用反射获取属性值 (简化处理，仅 getter)
            // 注意：实际EL实现会更复杂，考虑公共字段、Map、List等
            String getterName = "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
            Method getter = object.getClass().getMethod(getterName);

            return getter.invoke(object);
        } catch (Exception e) {
            // 如果标准getter失败，检查是否实现了特定接口（如 Map）
            if (object instanceof Map)
                return ((Map<?, ?>) object).get(property);

            System.err.println("Error accessing property '" + property + "' on object of type " + object.getClass().getName() + ": " + e.getMessage());

            return null; // 或抛出运行时异常
        }
    }

    @Override
    public Object visitGetIndexExpr(Expr.GetIndex expr) {
        Object object = evaluate(expr.object);

        if (object == null)
            return null;

        Object indexObj = evaluate(expr.index);

        if (object instanceof List && indexObj instanceof Integer) {
            int index = (Integer) indexObj;
            List<?> list = (List<?>) object;

            if (index >= 0 && index < list.size())
                return list.get(index);
            else {
                System.err.println("Index out of bounds: " + index + " for list size " + list.size());
                return null;
            }
        } else if (object instanceof Map) {
            // Map 可以接受任何类型的键
            return ((Map<?, ?>) object).get(indexObj);
        } else {
            System.err.println("Cannot apply index operation on object of type " + (object != null ? object.getClass().getName() : "null") + " with index " + indexObj);
            return null;
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }
}
