package com.ajaxjs.sqlman.entity;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestMapWriteSql {
    private final static String TABLE_NAME = "test_table";

    @Test
    void testGetInsertSqlWithMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("age", 25);

        MapWriteSql generator = new MapWriteSql(TABLE_NAME, map);
        generator.getInsertSql();

        assertEquals("INSERT INTO test_table (`name`, `age` ) VALUES (?, ?)", generator.getSql());
        assertArrayEquals(new Object[]{"Alice", 25}, generator.getParams());
    }

    @Test
    void testGetUpdateSqlWithMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Charlie");
        map.put("age", 35);
        map.put("id", 1L);

        MapWriteSql generator = new MapWriteSql(TABLE_NAME, map);

        generator.getUpdateSqlWithId("id");
        assertEquals("UPDATE test_table SET `name` = ?, `age` = ? WHERE id = ?", generator.getSql());
        assertArrayEquals(new Object[]{"Charlie", 35, 1L}, generator.getParams());

        generator.getUpdateSqlWithId("id", 2L);
        assertEquals("UPDATE test_table SET `name` = ?, `age` = ? WHERE id = ?", generator.getSql());
        assertArrayEquals(new Object[]{"Charlie", 35, 2L}, generator.getParams());
    }

    @Test
    void testGetDeleteSqlWithMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", 1L);
        MapWriteSql generator = new MapWriteSql(TABLE_NAME, map);

        generator.getDeleteSql("id", null);
        assertEquals("DELETE FROM test_table WHERE id = ?", generator.getSql());
        assertArrayEquals(new Object[]{1L}, generator.getParams());

        generator.getDeleteSql("id", 2L);
        assertEquals("DELETE FROM test_table WHERE id = ?", generator.getSql());
        assertArrayEquals(new Object[]{2L}, generator.getParams());
    }

    @Test
    void supportsSchemaQualifiedTableName() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "Alice");
        MapWriteSql generator = new MapWriteSql("app.test_table", map);

        generator.getInsertSql();

        assertEquals("INSERT INTO app.test_table (`name` ) VALUES (?)", generator.getSql());
    }

    @Test
    void rejectsIllegalTableColumnAndIdIdentifiers() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("name", "Alice");
        values.put("id", 1L);

        assertThrows(IllegalArgumentException.class, () -> new MapWriteSql("app..test_table", values));

        Map<String, Object> illegalColumn = new LinkedHashMap<>();
        illegalColumn.put("name; DELETE FROM users", "Alice");
        MapWriteSql illegalColumnGenerator = new MapWriteSql(TABLE_NAME, illegalColumn);
        assertThrows(IllegalArgumentException.class, illegalColumnGenerator::getInsertSql);

        MapWriteSql generator = new MapWriteSql(TABLE_NAME, values);
        assertThrows(IllegalArgumentException.class, () -> generator.getUpdateSqlWithId("id; DELETE FROM users", 1L));
        assertThrows(IllegalArgumentException.class, () -> generator.getDeleteSql("id; DELETE FROM users", 1L));
    }

    @Test
    void rejectsUpdateWithoutValuesOtherThanId() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", 1L);
        MapWriteSql generator = new MapWriteSql(TABLE_NAME, values);

        assertThrows(IllegalArgumentException.class, () -> generator.getUpdateSqlWithId("id"));
    }

    @Test
    void rejectsInsertWithoutWritableValues() {
        MapWriteSql generator = new MapWriteSql(TABLE_NAME, new LinkedHashMap<>());

        assertThrows(IllegalArgumentException.class, generator::getInsertSql);
    }
}
