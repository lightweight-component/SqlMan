package com.ajaxjs.sqlman;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Defines the desired trust boundary for SQL templates.
 */
class TestSqlTemplateSafety {
    @Test
    void mapValueMustNotTurnIntoExecutableSql() throws Exception {
        try (Connection conn = createDatabase()) {
            Map<String, Object> values = new HashMap<>();
            values.put("name", "safe' OR 1=1 --");

            Integer count = new Action(conn,
                    "SELECT COUNT(*) FROM template_items WHERE name = #{name}")
                    .query(values).oneValue(Integer.class);

            assertEquals(Integer.valueOf(0), count,
                    "Map values must be bound as data instead of interpolated into SQL text.");
        }
    }

    @Test
    void positionalValuesStayBoundAfterTrustedTemplateExpansion() throws Exception {
        try (Connection conn = createDatabase()) {
            Map<String, Object> values = new HashMap<>();
            values.put("table", "template_items");

            Integer count = new Action(conn,
                    "SELECT COUNT(*) FROM ${table} WHERE name = ?")
                    .query(values, "safe' OR 1=1 --").oneValue(Integer.class);

            assertEquals(Integer.valueOf(0), count);
        }
    }

    @Test
    void rejectsUntrustedIdentifierTemplateValues() throws Exception {
        try (Connection conn = createDatabase()) {
            Map<String, Object> values = new HashMap<>();
            values.put("table", "template_items; DROP TABLE template_items");

            assertThrows(IllegalArgumentException.class, () -> new Action(conn,
                    "SELECT COUNT(*) FROM ${table}").query(values));
        }
    }

    private static Connection createDatabase() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:template_safety;DB_CLOSE_DELAY=-1");
        Connection conn = dataSource.getConnection();

        try (Statement statement = conn.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS template_items");
            statement.execute("CREATE TABLE template_items (name VARCHAR(100))");
            statement.execute("INSERT INTO template_items VALUES ('safe'), ('other')");
        }

        return conn;
    }
}
