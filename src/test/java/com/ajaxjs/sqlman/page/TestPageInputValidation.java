package com.ajaxjs.sqlman.page;

import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.crud.page.PageQuery;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Defines the paging input and Action-state contract.
 */
class TestPageInputValidation {
    @Test
    void rejectsInvalidPagingArguments() throws Exception {
        try (Connection conn = createDatabase()) {
            Action action = new Action(conn, "SELECT id FROM page_input_items ORDER BY id");

            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> action.query().pageByStartLimit(-1, 10)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> action.query().pageByStartLimit(0, 0)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> PageQuery.pageNo2start(0, 10)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> PageQuery.pageNo2start(1, 0)));
        }
    }

    @Test
    void pagingDoesNotReplaceTheActionSql() throws Exception {
        try (Connection conn = createDatabase()) {
            String sql = "SELECT id FROM page_input_items ORDER BY id";
            Action action = new Action(conn, sql);

            action.query().pageByStartLimit(0, 10);

            assertEquals(sql, action.getSql(), "Paging must not leave a count or paged SQL statement on Action.");
        }
    }

    private static Connection createDatabase() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:page_input_validation;DB_CLOSE_DELAY=-1");
        Connection conn = dataSource.getConnection();

        try (Statement statement = conn.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS page_input_items");
            statement.execute("CREATE TABLE page_input_items (id INT PRIMARY KEY)");
            statement.execute("INSERT INTO page_input_items VALUES (1)");
        }

        return conn;
    }
}
