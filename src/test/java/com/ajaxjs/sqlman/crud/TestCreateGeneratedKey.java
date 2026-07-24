package com.ajaxjs.sqlman.crud;

import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.model.CreateResult;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestCreateGeneratedKey {
    @Test
    void convertsKeyReturnedByJdbcDriver() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:create_key_test;DB_CLOSE_DELAY=-1");

        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS generated_key_test");
            statement.execute("CREATE TABLE generated_key_test (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(20))");

            CreateResult<Integer> integerResult = new Action(conn,
                    "INSERT INTO generated_key_test(name) VALUES (?)").create("first").execute(true, Integer.class);
            CreateResult<String> stringResult = new Action(conn,
                    "INSERT INTO generated_key_test(name) VALUES (?)").create("second").execute(true, String.class);

            assertEquals(Integer.valueOf(1), integerResult.getNewlyId());
            assertEquals("2", stringResult.getNewlyId());
        }
    }

    @Test
    void convertsGeneratedKeysToRequestedType() {
        assertEquals(Integer.valueOf(42), Create.convertGeneratedKey(42L, Integer.class));
        assertEquals(Long.valueOf(42), Create.convertGeneratedKey(BigInteger.valueOf(42), Long.class));
        assertEquals("42", Create.convertGeneratedKey(42L, String.class));
        assertEquals(Short.valueOf((short) 42), Create.convertGeneratedKey(42L, Short.class));
        assertEquals(new BigInteger("42"), Create.convertGeneratedKey(new BigDecimal("42"), BigInteger.class));

        Serializable value = 42L;
        assertSame(value, Create.convertGeneratedKey(value, Serializable.class));
    }

    @Test
    void rejectsOverflowAndLossyConversions() {
        assertThrows(IllegalArgumentException.class,
                () -> Create.convertGeneratedKey((long) Integer.MAX_VALUE + 1, Integer.class));
        assertThrows(IllegalArgumentException.class,
                () -> Create.convertGeneratedKey(new BigInteger("9223372036854775808"), Long.class));
        assertThrows(IllegalArgumentException.class,
                () -> Create.convertGeneratedKey(new BigDecimal("1.5"), Integer.class));
        assertThrows(IllegalArgumentException.class,
                () -> Create.convertGeneratedKey("not-a-number", Long.class));
    }
}
