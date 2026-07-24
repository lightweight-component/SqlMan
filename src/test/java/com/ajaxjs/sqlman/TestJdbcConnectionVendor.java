package com.ajaxjs.sqlman;

import com.ajaxjs.sqlman.model.DatabaseVendor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestJdbcConnectionVendor {
    @Test
    void recognizesAdditionalDatabaseVendors() {
        assertVendor("MariaDB", DatabaseVendor.MARIADB);
        assertVendor("SQLite", DatabaseVendor.SQL_LITE);
        assertVendor("HSQL Database Engine", DatabaseVendor.HSQLDB);
        assertVendor("Microsoft SQL Server", DatabaseVendor.SQL_SERVER);
    }

    private static void assertVendor(String productName, DatabaseVendor expected) {
        DatabaseMetaData metadata = (DatabaseMetaData) Proxy.newProxyInstance(
                TestJdbcConnectionVendor.class.getClassLoader(),
                new Class[]{DatabaseMetaData.class},
                (proxy, method, args) -> {
                    if ("getDatabaseProductName".equals(method.getName()))
                        return productName;
                    throw new UnsupportedOperationException(method.getName());
                });

        Connection connection = (Connection) Proxy.newProxyInstance(
                TestJdbcConnectionVendor.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("getMetaData".equals(method.getName()))
                        return metadata;
                    throw new UnsupportedOperationException(method.getName());
                });

        assertEquals(expected, JdbcConnection.initDatabaseVendor(connection));
    }
}
