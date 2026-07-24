package com.ajaxjs.sqlman.crud;

import com.ajaxjs.sqlman.JdbcConnection;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestBatchUpdate {
    private Connection conn;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:batch_test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        conn = dataSource.getConnection();
        JdbcConnection.setConnection(conn);

        try (Statement statement = conn.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS batch_items");
            statement.execute("CREATE TABLE batch_items ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(100) NOT NULL UNIQUE, "
                    + "note VARCHAR(200))");
        }
    }

    @AfterEach
    void tearDown() {
        JdbcConnection.closeDb();
    }

    @Test
    void insertsMapsWithNullAndDifferentIterationOrderSafely() throws SQLException {
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("name", "O'Reilly");
        first.put("note", null);

        Map<String, Object> second = new LinkedHashMap<>();
        second.put("note", "x'); DROP TABLE batch_items; --");
        second.put("name", "second");

        BatchUpdate batch = new BatchUpdate();
        batch.createBatchMap(Arrays.asList(first, second), "batch_items");

        assertTrue(conn.getAutoCommit());
        assertEquals(2, countRows());
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT note FROM batch_items WHERE name = 'second'")) {
            assertTrue(rs.next());
            assertEquals("x'); DROP TABLE batch_items; --", rs.getString(1));
        }
    }

    @Test
    void doesNotCommitAnExistingTransaction() throws SQLException {
        conn.setAutoCommit(false);
        BatchUpdate batch = new BatchUpdate();
        batch.createBatchMap(Arrays.asList(row("first", "note")), "batch_items");

        assertFalse(conn.getAutoCommit());
        assertEquals(1, countRows());
        conn.rollback();
        assertEquals(0, countRows());
    }

    @Test
    void rollsBackOwnedTransactionAndRestoresAutoCommit() throws SQLException {
        BatchUpdate batch = new BatchUpdate();

        assertThrows(RuntimeException.class, () ->
                batch.createBatchMap(Arrays.asList(row("duplicate", "one"), row("duplicate", "two")), "batch_items"));

        assertTrue(conn.getAutoCommit());
        assertEquals(0, countRows());
    }

    @Test
    void rejectsEmptyOrInconsistentInput() {
        BatchUpdate batch = new BatchUpdate();
        assertThrows(IllegalArgumentException.class, () -> batch.createBatchMap(Arrays.asList(), "batch_items"));

        Map<String, Object> first = row("first", "note");
        Map<String, Object> second = new LinkedHashMap<>();
        second.put("name", "second");
        assertThrows(IllegalArgumentException.class,
                () -> batch.createBatchMap(Arrays.asList(first, second), "batch_items"));
    }

    @Test
    void insertsBeansAndBindsLaterNullValues() throws SQLException {
        BatchItem first = new BatchItem();
        first.setName("first");
        first.setNote("note");
        BatchItem second = new BatchItem();
        second.setName("second");
        second.setNote(null);

        BatchUpdate batch = new BatchUpdate();
        batch.setTableName("batch_items");
        batch.createBatch(new BatchItem[]{first, second});

        assertEquals(2, countRows());
    }

    @Test
    void doesNotExecutePartialBatchWhenGetterFails() throws SQLException {
        BatchUpdate batch = new BatchUpdate();
        batch.setTableName("batch_items");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> batch.createBatch(new Object[]{new FailingBatchItem()}));

        assertTrue(error.getMessage().contains("name"));
        assertEquals(0, countRows());
    }

    @Test
    void rejectsEmptyDeleteList() {
        BatchUpdate batch = new BatchUpdate();
        batch.setTableName("batch_items");
        assertThrows(IllegalArgumentException.class, () -> batch.deleteBatch(Arrays.asList()));
    }

    @Test
    void deletesIdsWithBoundParameters() throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("INSERT INTO batch_items(name) VALUES ('first'), ('second'), ('third')");
        }

        BatchUpdate batch = new BatchUpdate();
        batch.setTableName("batch_items");
        batch.deleteBatch(Arrays.asList(1, 3));

        assertEquals(1, countRows());
    }

    private static Map<String, Object> row(String name, String note) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", name);
        row.put("note", note);
        return row;
    }

    private int countRows() throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM batch_items")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public static class BatchItem {
        private Integer id;
        private String name;
        private String note;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }

    public static class FailingBatchItem {
        public String getName() {
            throw new IllegalStateException("getter failed");
        }

        public String getNote() {
            return "must not be inserted";
        }
    }
}
