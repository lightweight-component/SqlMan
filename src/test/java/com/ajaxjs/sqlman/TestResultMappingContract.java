package com.ajaxjs.sqlman;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Defines failure visibility when JDBC values cannot be mapped to a bean property.
 */
class TestResultMappingContract {
    @Test
    void rejectsRowsThatCannotBeConvertedToTheTargetPropertyType() throws Exception {
        try (Connection conn = createDatabase()) {
            assertThrows(RuntimeException.class, () -> new Action(conn,
                    "SELECT amount FROM mapping_items").query().one(MappedItem.class));
        }
    }

    private static Connection createDatabase() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:result_mapping_contract;DB_CLOSE_DELAY=-1");
        Connection conn = dataSource.getConnection();

        try (Statement statement = conn.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS mapping_items");
            statement.execute("CREATE TABLE mapping_items (amount VARCHAR(30))");
            statement.execute("INSERT INTO mapping_items VALUES ('not-a-number')");
        }

        return conn;
    }

    public static class MappedItem {
        private Integer amount;

        public Integer getAmount() {
            return amount;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }
    }
}
