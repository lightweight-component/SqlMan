package com.ajaxjs.sqlman;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.assertNull;

class TestActionState {
    @Test
    void emptyParametersClearParametersFromPreviousUse() {
        DatabaseMetaData metadata = (DatabaseMetaData) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{DatabaseMetaData.class},
                (proxy, method, args) -> "getDatabaseProductName".equals(method.getName()) ? "H2" : null);
        Connection connection = (Connection) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> "getMetaData".equals(method.getName()) ? metadata : null);
        Action action = new Action(connection);
        action.setParams(1, "old");
        action.setParams();

        assertNull(action.getParams());
    }
}
