package com.ajaxjs.sqlman.util;

import com.ajaxjs.sqlman.BaseAction;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestColumnNameMapping {
    @Test
    void convertsUppercaseAndLowercaseUnderscoreNames() {
        assertEquals("processlistId", Utils.changeColumnToFieldName("PROCESSLIST_ID"));
        assertEquals("lockStatus", Utils.changeColumnToFieldName("LOCK_STATUS"));
        assertEquals("processlistId", Utils.changeColumnToFieldName("processlist_id"));
        assertEquals("lockStatus", Utils.changeColumnToFieldName("lock_status"));
    }

    @Test
    void preservesNamesWithoutUnderscores() {
        assertEquals("processlistId", Utils.changeColumnToFieldName("processlistId"));
        assertEquals("lockStatus", Utils.changeColumnToFieldName("lockStatus"));
        assertEquals("ID", Utils.changeColumnToFieldName("ID"));
    }

    @Test
    void mapsJdbcColumnLabelsAndPreservesCamelCaseAliases() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:column_name_mapping");
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT 1 AS PROCESSLIST_ID, 'GRANTED' AS LOCK_STATUS, 2 AS \"existingId\"")) {
            result.next();
            Map<String, Object> row = BaseAction.getResultMap(result);

            assertEquals(3, row.size());
            assertEquals(1, row.get("processlistId"));
            assertEquals("GRANTED", row.get("lockStatus"));
            assertEquals(2, row.get("existingId"));
        }
    }
}
