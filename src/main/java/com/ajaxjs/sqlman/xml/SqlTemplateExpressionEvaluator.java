package com.ajaxjs.sqlman.xml;

import java.util.Map;

/**
 * Evaluates a dynamic SQL condition against the current template parameters.
 * Implementations may use any expression language, or a lambda for a small
 * application-specific condition set.
 */
@FunctionalInterface
public interface SqlTemplateExpressionEvaluator {
    /**
     * @param expression the {@code test} attribute from an {@code <if>} element.
     * @param params     the current rendering scope, including forEach item values.
     * @return whether the {@code <if>} true branch should be rendered.
     */
    boolean evaluate(String expression, Map<String, Object> params);
}
