package com.ajaxjs.sqlman.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestPrintRealSql {
    @Test
    void ignoresQuestionMarksInLiteralsAndComments() {
        String sql = "SELECT '?', \"?\", `?` FROM test "
                + "WHERE text = 'It''s ?' AND escaped = 'a\\?b' AND id = ? "
                + "-- line comment ?\n"
                + "AND state = ? /* block comment ? */ # MySQL comment ?";

        assertEquals("SELECT '?', \"?\", `?` FROM test "
                        + "WHERE text = 'It''s ?' AND escaped = 'a\\?b' AND id = 7 "
                        + "-- line comment ?\n"
                        + "AND state = 'OK' /* block comment ? */ # MySQL comment ?",
                PrintRealSql.printRealSql(sql, new Object[]{7, "OK"}));
    }

    @Test
    void formatsStringsDatesAndMissingParameters() {
        String sql1 = "SELECT * FROM users WHERE name = ? AND age > ?";
        Object[] params1 = {"O'Reilly", 25};
        assertEquals("SELECT * FROM users WHERE name = 'O''Reilly' AND age > 25",
                PrintRealSql.printRealSql(sql1, params1));

        String sql2 = "INSERT INTO logs (msg, time) VALUES (?, ?)";
        Object[] params2 = {"Error occurred", new Date(0)};
        String renderedDateSql = PrintRealSql.printRealSql(sql2, params2);
        assertTrue(renderedDateSql.startsWith("INSERT INTO logs (msg, time) VALUES ('Error occurred', '"));
        assertTrue(renderedDateSql.endsWith("')"));

        String sql3 = "DELETE FROM temp WHERE id = ? AND status = ?";
        Object[] params3 = {123};
        assertEquals("DELETE FROM temp WHERE id = 123 AND status = ?",
                PrintRealSql.printRealSql(sql3, params3));
    }

    @Test
    void test2() {
        String sql = "SELECT \n" +
                "    u.id AS user_id,\n" +
                "    u.name,\n" +
                "    u.email,\n" +
                "    COALESCE(up.phone, 'N/A') AS phone,\n" +
                "    CASE \n" +
                "        WHEN u.status = 1 THEN 'Active'\n" +
                "        WHEN u.status = 0 THEN 'Inactive'\n" +
                "        ELSE 'Unknown'\n" +
                "    END AS status_label,\n" +
                "    DATE_FORMAT(u.created_time, '%Y-%m-%d') AS signup_date,\n" +
                "    COUNT(o.id) AS order_count,\n" +
                "    SUM(COALESCE(o.total_amount, 0)) AS total_spent,\n" +
                "    AVG(o.total_amount) AS avg_order_value,\n" +
                "    RANK() OVER (PARTITION BY u.region ORDER BY SUM(o.total_amount) DESC) AS spending_rank,\n" +
                "    ROW_NUMBER() OVER (PARTITION BY u.region ORDER BY u.created_time) AS registration_order,\n" +
                "    GROUP_CONCAT(DISTINCT p.category ORDER BY p.category SEPARATOR ' | ') AS favorite_categories,\n" +
                "    JSON_OBJECT('max', MAX(o.total_amount), 'min', MIN(o.total_amount)) AS order_stats\n" +
                "FROM \n" +
                "    users u\n" +
                "    LEFT JOIN user_profiles up ON u.id = up.user_id\n" +
                "    LEFT JOIN orders o ON u.id = o.user_id \n" +
                "        AND o.order_date >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)\n" +
                "    LEFT JOIN order_items oi ON o.id = oi.order_id\n" +
                "    LEFT JOIN products p ON oi.product_id = p.id\n" +
                "WHERE \n" +
                "    u.created_time >= ?\n" +
                "    AND u.created_time < ?\n" +
                "    AND u.region IN (?, ?, ?)\n" +
                "    AND (u.status = ? OR ? IS NULL)\n" +
                "    AND (up.city = ? OR ? = '')\n" +
                "    AND EXISTS (\n" +
                "        SELECT 1 \n" +
                "        FROM user_login_log l \n" +
                "        WHERE l.user_id = u.id \n" +
                "          AND l.login_time >= DATE_SUB(?, INTERVAL 30 DAY)\n" +
                "    )\n" +
                "GROUP BY \n" +
                "    u.id, u.name, u.email, up.phone, u.status, u.region, u.created_time\n" +
                "HAVING \n" +
                "    order_count >= ?\n" +
                "    AND total_spent > ?\n" +
                "ORDER BY \n" +
                "    total_spent DESC,\n" +
                "    order_count DESC\n" +
                "LIMIT ?;";

        // 模拟参数
        Object[] params = {
                Date.from(LocalDateTime.of(2024, 1, 1, 0, 0).atZone(ZoneId.systemDefault()).toInstant()), // ?
                Date.from(LocalDateTime.of(2025, 1, 1, 0, 0).atZone(ZoneId.systemDefault()).toInstant()), // ?
                "North", "South", "East",                                                 // ?, ?, ?
                1, null,                                                                  // ?, ?
                "Beijing", "",                                                            // ?, ?
                Date.from(LocalDateTime.now().minusDays(30).atZone(ZoneId.systemDefault()).toInstant()), // ?
                1,                                                                        // ?
                100.0,                                                                    // ?
                50                                                                        // ?
        };

        String rendered = PrintRealSql.printRealSql(sql, params);
        assertFalse(rendered.contains("?"));
        assertTrue(rendered.endsWith("LIMIT 50;"));
    }
}
