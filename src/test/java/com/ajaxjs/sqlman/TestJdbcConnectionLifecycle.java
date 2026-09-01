package com.ajaxjs.sqlman;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class TestJdbcConnectionLifecycle {
    @AfterEach
    void clearThreadLocal() {
        JdbcConnection.setConnection(null);
    }

    @Test
    void removesThreadLocalEvenWhenCloseFails() {
        Connection connection = connectionThatFailsToClose();
        JdbcConnection.setConnection(connection);

        assertThrows(RuntimeException.class, JdbcConnection::closeDb);
        assertThrows(UnsupportedOperationException.class, JdbcConnection::getConnection);
    }

    @Test
    void removesThreadLocalAfterNormalClose() {
        JdbcConnection.setConnection(connectionThatCloses());

        JdbcConnection.closeDb();

        assertThrows(UnsupportedOperationException.class, JdbcConnection::getConnection);
    }

    @Test
    void keepsConnectionsIsolatedAcrossConcurrentThreads() throws Exception {
        Connection first = connectionThatCloses();
        Connection second = connectionThatCloses();
        JdbcConnection.setConnection(connectionThatCloses());
        CountDownLatch bothBound = new CountDownLatch(2);
        CountDownLatch verify = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Connection> firstSeen = executor.submit(() -> bindAndRead(first, bothBound, verify));
            Future<Connection> secondSeen = executor.submit(() -> bindAndRead(second, bothBound, verify));

            assertTrue(bothBound.await(3, TimeUnit.SECONDS));
            verify.countDown();

            assertSame(first, firstSeen.get(3, TimeUnit.SECONDS));
            assertSame(second, secondSeen.get(3, TimeUnit.SECONDS));
            assertNotSame(first, JdbcConnection.getConnection());
            assertNotSame(second, JdbcConnection.getConnection());
        } finally {
            executor.shutdownNow();
        }
    }

    private Connection bindAndRead(Connection expected, CountDownLatch bothBound, CountDownLatch verify) throws InterruptedException {
        assertThrows(UnsupportedOperationException.class, JdbcConnection::getConnection);
        JdbcConnection.setConnection(expected);
        bothBound.countDown();
        assertTrue(verify.await(3, TimeUnit.SECONDS));

        Connection actual = JdbcConnection.getConnection();
        JdbcConnection.closeDb();
        assertThrows(UnsupportedOperationException.class, JdbcConnection::getConnection);
        return actual;
    }

    private Connection connectionThatCloses() {
        return connection(false);
    }

    private Connection connectionThatFailsToClose() {
        return connection(true);
    }

    private Connection connection(boolean closeFails) {
        return (Connection) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("isClosed".equals(method.getName()))
                        return false;
                    if ("close".equals(method.getName()) && closeFails)
                        throw new SQLException("close failed");
                    return null;
                });
    }
}
