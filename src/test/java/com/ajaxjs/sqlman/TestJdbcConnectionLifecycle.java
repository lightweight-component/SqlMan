package com.ajaxjs.sqlman;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TestJdbcConnectionLifecycle {
    @AfterEach
    void clearThreadLocal() {
        JdbcConnection.setConnection(null);
    }

    @Test
    void removesThreadLocalEvenWhenCloseFails() {
        Connection connection = (Connection) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("isClosed".equals(method.getName()))
                        return false;
                    if ("close".equals(method.getName()))
                        throw new SQLException("close failed");
                    return null;
                });
        JdbcConnection.setConnection(connection);

        assertThrows(RuntimeException.class, JdbcConnection::closeDb);
        assertThrows(UnsupportedOperationException.class, JdbcConnection::getConnection);
    }
}
