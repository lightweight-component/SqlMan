package com.ajaxjs.sqlman.sqltemplate;

import com.ajaxjs.sqlman.model.PreparedSql;
import com.ajaxjs.sqlman.sqltemplate.model.RenderedSql;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestParameterBinder {
    @Test
    void preservesLiteralPlaceholderInReportedQuery() {
        assertPrepared("SELECT '#{name}' AS label FROM users WHERE id = #{id}",
                "SELECT '#{name}' AS label FROM users WHERE id = ?", values(), null, 42);
    }

    @Test
    void bindsRepeatedNamedParametersAndNulls() {
        assertPrepared("SELECT #{name}, #{id}, #{name}, #{absent}", "SELECT ?, ?, ?, ?", values(), null,
                "safe' OR 1=1 --", 42, "safe' OR 1=1 --", null);
    }

    @Test
    void preservesStringLiteralsAndQuotedIdentifiers() {
        String prefix = "SELECT '#{name}', '${missing}', 'it''s #{name} ?', 'it\\'s ${missing}', \"#{name}\", `#{name}`, \"a\"\"${missing}\", `a``${missing}`";
        assertPrepared(prefix + " WHERE id = #{id}", prefix + " WHERE id = ?", values(), new Object[0], 42);
    }

    @Test
    void preservesEverySupportedCommentWithoutResolvingItsPlaceholders() {
        String prefix = "SELECT 1 -- #{name} ${missing} ?\r\n# #{name} ${missing} ?\n/* #{name} ${missing} ? ' */\n";
        assertPrepared(prefix + "WHERE id = #{id}", prefix + "WHERE id = ?", values(), null, 42);
    }

    @Test
    void bindsNamedAndPositionalValuesInSqlOrder() {
        assertPrepared("SELECT ? FROM ${table} WHERE id = #{id} AND name = ? OR id = #{id} AND other = #{absent}",
                "SELECT ? FROM app.users WHERE id = ? AND name = ? OR id = ? AND other = ?", values(),
                new Object[]{"first", "safe' OR 1=1 --"}, "first", 42, "safe' OR 1=1 --", 42, null);
    }

    @Test
    void ignoresQuestionMarksInQuotesAndComments() {
        assertPrepared("SELECT '?', \"?\", `?` /* ? */ WHERE id = ? -- ?\n", "SELECT '?', \"?\", `?` /* ? */ WHERE id = ? -- ?\n",
                null, new Object[]{42}, 42);
    }

    @Test
    void validatesOnlyExecutableIdentifierSubstitutions() {
        for (String identifier : new String[]{"users; DROP TABLE users", "users --", "users/*x*/", "users?", "users.\"name\"", "#{id}", ""}) {
            Map<String, Object> params = values();
            params.put("table", identifier);
            assertThrows(IllegalArgumentException.class, () -> ParameterBinder.prepare("SELECT * FROM ${table}", params, null));
        }
        assertThrows(IllegalArgumentException.class, () -> ParameterBinder.prepare("SELECT * FROM ${missing}", values(), null));
        assertPrepared("SELECT '${table}' /* ${table} */", "SELECT '${table}' /* ${table} */", Collections.emptyMap(), null);
    }

    @Test
    void enforcesPositionalParameterCounts() {
        assertThrows(IllegalArgumentException.class, () -> ParameterBinder.prepare("SELECT #{id}, ?", values(), null));
        assertThrows(IllegalArgumentException.class, () -> ParameterBinder.prepare("SELECT #{id}, '?'", values(), new Object[]{1}));
    }

    @Test
    void renderedSqlUsesTheSameBindingRules() {
        PreparedSql result = ParameterBinder.prepare(new RenderedSql("SELECT '#{name}' WHERE id = #{id} AND name = ?", values()), "safe");
        assertEquals("SELECT '#{name}' WHERE id = ? AND name = ?", result.getSql());
        assertArrayEquals(new Object[]{42, "safe"}, result.getParams());
    }

    private static Map<String, Object> values() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 42);
        params.put("name", "safe' OR 1=1 --");
        params.put("table", "app.users");
        return params;
    }

    private static void assertPrepared(String source, String expected, Map<String, Object> params, Object[] positional, Object... bindings) {
        PreparedSql result = ParameterBinder.prepare(source, params, positional);
        assertEquals(expected, result.getSql());
        assertArrayEquals(bindings, result.getParams());
    }
}
