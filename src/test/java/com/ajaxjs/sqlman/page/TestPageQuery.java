package com.ajaxjs.sqlman.page;

import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.crud.page.PageResult;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestPageQuery {
    @Test
    void returnsEmptyMapListWhenOffsetExceedsLastPage() throws Exception {
        try (Connection conn = createDatabase()) {
            PageResult<Map<String, Object>> result = new Action(conn,
                    "SELECT id, name FROM page_items ORDER BY id")
                    .query().pageByStartLimit(100, 10);

            assertOutOfRangePage(result);
        }
    }

    @Test
    void returnsEmptyBeanListWhenOffsetExceedsLastPage() throws Exception {
        try (Connection conn = createDatabase()) {
            PageResult<PageItem> result = new Action(conn,
                    "SELECT id, name FROM page_items ORDER BY id")
                    .query().pageByStartLimit(100, 10, PageItem.class);

            assertOutOfRangePage(result);
        }
    }

    private static Connection createDatabase() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:page_out_of_range;DB_CLOSE_DELAY=-1");
        Connection conn = dataSource.getConnection();

        try (Statement statement = conn.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS page_items");
            statement.execute("CREATE TABLE page_items (id INT PRIMARY KEY, name VARCHAR(20))");
            statement.execute("INSERT INTO page_items VALUES (1, 'first'), (2, 'second')");
        }

        return conn;
    }

    private static void assertOutOfRangePage(PageResult<?> result) {
        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getTotalPage());
        assertEquals(11, result.getCurrentPage());
        assertFalse(result.isZero());
        assertNotNull(result.getList());
        assertTrue(result.getList().isEmpty());
    }

    public static class PageItem {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
