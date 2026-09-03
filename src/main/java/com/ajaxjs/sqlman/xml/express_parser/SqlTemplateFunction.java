package com.ajaxjs.sqlman.xml.express_parser;

import java.util.List;
import java.util.Map;

/**
 * A function explicitly exposed to SQL template expressions.
 */
@FunctionalInterface
public interface SqlTemplateFunction {
    /**
     * @param arguments evaluated function arguments.
     * @param params    the current template parameter scope.
     * @return the function result, which can be used in a condition or another expression.
     */
    Object apply(List<Object> arguments, Map<String, Object> params);
}
