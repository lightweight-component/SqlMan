package com.ajaxjs.sqlman.xml.express_parser;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestJSqlParserExpressionEvaluator {
    @Test
    void evaluatesNestedBooleanArithmeticAndParentheses() {
        JSqlParserExpressionEvaluator evaluator = new JSqlParserExpressionEvaluator();
        Map<String, Object> params = new HashMap<>();
        params.put("enabled", true);
        params.put("tier", 1);
        params.put("bonus", 1);
        params.put("admin", false);

        assertTrue(evaluator.evaluate("(enabled = TRUE AND (tier + bonus) >= 2) OR admin = TRUE", params));

        params.put("enabled", false);
        assertFalse(evaluator.evaluate("(enabled = TRUE AND (tier + bonus) >= 2) OR admin = TRUE", params));
    }

    @Test
    void supportsNullBetweenAndIn() {
        JSqlParserExpressionEvaluator evaluator = new JSqlParserExpressionEvaluator();
        Map<String, Object> params = new HashMap<>();
        params.put("status", "OPEN");
        params.put("score", 8);
        params.put("optional", null);

        assertTrue(evaluator.evaluate("status IN ('OPEN', 'PENDING') AND score BETWEEN 1 AND 10 AND optional IS NULL", params));
        assertFalse(evaluator.evaluate("optional <> 1", params));
    }

    @Test
    void callsOnlyExplicitlyRegisteredFunctions() {
        Map<String, SqlTemplateFunction> functions = new HashMap<>();
        functions.put("isBlank", (arguments, params) -> {
            String value = (String) arguments.get(0);
            return value == null || value.trim().isEmpty();
        });
        JSqlParserExpressionEvaluator evaluator = new JSqlParserExpressionEvaluator(functions);
        Map<String, Object> params = new HashMap<>();
        params.put("name", "  ");
        params.put("enabled", false);

        assertTrue(evaluator.evaluate("ISBLANK(name) OR enabled = TRUE", params));
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate("unknown(name)", params));
    }

    @Test
    void rejectsMissingParametersAndUnsupportedAstNodes() {
        JSqlParserExpressionEvaluator evaluator = new JSqlParserExpressionEvaluator();

        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate("missing = 1", new HashMap<String, Object>()));
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate("EXISTS (SELECT 1)", new HashMap<String, Object>()));
    }
}
