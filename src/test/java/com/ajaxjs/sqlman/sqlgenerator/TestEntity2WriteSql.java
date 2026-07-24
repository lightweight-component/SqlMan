package com.ajaxjs.sqlman.sqlgenerator;

import com.ajaxjs.sqlman.annotation.Column;
import com.ajaxjs.sqlman.annotation.Table;
import com.ajaxjs.sqlman.annotation.Transient;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestEntity2WriteSql {
    private final static String TABLE_NAME = "test_table";

    @Table(TABLE_NAME)
    @Data
    public static class TestBean {
        private Long id;
        private String name;

        @Column(name = "real_age")
        private Integer age;

        @Transient
        private String temp;
    }

    /**
     * Test insert with Map input.
     */
    @Test
    void testGetInsertSqlWithMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("age", 25);

        Entity2WriteSql generator = new Entity2WriteSql(map);
        generator.setTableName(TABLE_NAME);
        generator.getInsertSql();

        assertEquals("INSERT INTO test_table (`name`, `age` ) VALUES (?, ?)", generator.getSql());
        assertArrayEquals(new Object[]{"Alice", 25}, generator.getParams());
    }

    /**
     * Test insert with Bean input.
     */
    @Test
    void testGetInsertSqlWithBean() {
        TestBean bean = new TestBean();
        bean.setName("Bob");
        bean.setAge(30);

        Entity2WriteSql generator = new Entity2WriteSql(bean);
        generator.getInsertSql();

        assertEquals("INSERT INTO test_table (`real_age`, `name` ) VALUES (?, ?)", generator.getSql());
        assertArrayEquals(new Object[]{30, "Bob"}, generator.getParams());
    }

    /**
     * Test update with Map input.
     */
    @Test
    void testGetUpdateSqlWithMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Charlie");
        map.put("age", 35);
        map.put("id", 1L);

        Entity2WriteSql generator = new Entity2WriteSql(map);
        generator.setTableName(TABLE_NAME);

        generator.getUpdateSqlWithId("id");
        assertEquals("UPDATE test_table SET `name` = ?, `age` = ? WHERE id = ?", generator.getSql());
        assertArrayEquals(new Object[]{"Charlie", 35, 1L}, generator.getParams());

        generator.getUpdateSqlWithId("id", 2L);
        assertEquals("UPDATE test_table SET `name` = ?, `age` = ? WHERE id = ?", generator.getSql());
        assertArrayEquals(new Object[]{"Charlie", 35, 2L}, generator.getParams());
    }

    /**
     * Test update with Bean input.
     */
    @Test
    void testGetUpdateSqlWithBean() {
        TestBean bean = new TestBean();
        bean.setId(1L);
        bean.setName("David");
        bean.setTemp("ignored");
        bean.setAge(40);

        Entity2WriteSql generator = new Entity2WriteSql(bean);
        generator.getUpdateSqlWithId("id");

        assertEquals("UPDATE test_table SET `real_age` = ?, `name` = ? WHERE id = ?", generator.getSql());
        assertArrayEquals(new Object[]{40, "David", 1L}, generator.getParams());

        generator.getUpdateSqlWithId("id", 2L);

        assertEquals("UPDATE test_table SET `real_age` = ?, `name` = ? WHERE id = ?", generator.getSql());
        assertArrayEquals(new Object[]{40, "David", 2L}, generator.getParams());
    }

    @Test
    void testGetTableNameByBean() {
        TestBean bean = new TestBean();
        assertEquals(TABLE_NAME, Entity2WriteSql.getTableNameByBean(bean));
    }

    @Test
    void skipsWriteOnlyProperties() {
        WriteOnlyBean bean = new WriteOnlyBean();
        bean.setSecret("ignored");

        Entity2WriteSql generator = new Entity2WriteSql(bean);
        generator.setTableName(TABLE_NAME);
        generator.getInsertSql();

        assertEquals("INSERT INTO test_table (`name` ) VALUES (?)", generator.getSql());
        assertArrayEquals(new Object[]{"readable"}, generator.getParams());
    }

    @Test
    void propagatesGetterFailureWithPropertyContext() {
        Entity2WriteSql generator = new Entity2WriteSql(new FailingGetterBean());
        generator.setTableName(TABLE_NAME);

        IllegalStateException error = assertThrows(IllegalStateException.class, generator::getInsertSql);

        assertTrue(error.getMessage().contains(FailingGetterBean.class.getName()));
        assertTrue(error.getMessage().contains("broken"));
        assertInstanceOf(IllegalArgumentException.class, error.getCause());
    }

    public static class WriteOnlyBean {
        private String secret;

        public String getName() {
            return "readable";
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    public static class FailingGetterBean {
        public String getBroken() {
            throw new IllegalArgumentException("broken getter");
        }

        public String getName() {
            return "must not be inserted";
        }
    }
}
