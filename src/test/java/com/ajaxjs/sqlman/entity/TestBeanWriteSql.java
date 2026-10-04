package com.ajaxjs.sqlman.entity;

import com.ajaxjs.sqlman.annotation.Column;
import com.ajaxjs.sqlman.annotation.Table;
import com.ajaxjs.sqlman.annotation.Transient;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestBeanWriteSql {
    @Table("test_table")
    public static class WriteBean {
        private Long id;

        private String name;

        @Column(insertable = false)
        private String updateOnly;

        @Column(updatable = false)
        private String insertOnly;

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getUpdateOnly() {
            return updateOnly;
        }

        public String getInsertOnly() {
            return insertOnly;
        }
    }

    static class IllegalColumnBean {
        @Column(name = "name; DELETE FROM users")
        private String name = "Alice";

        public String getName() {
            return name;
        }
    }

    @Test
    void respectsColumnInsertableAndUpdatableFlags() {
        WriteBean insertBean = new WriteBean();
        insertBean.name = "Alice";
        insertBean.insertOnly = "created";
        insertBean.updateOnly = "ignored";
        BeanWriteSql insertGenerator = new BeanWriteSql(insertBean);

        insertGenerator.getInsertSql();

        assertTrue(insertGenerator.getSql().contains("`name`"));
        assertTrue(insertGenerator.getSql().contains("`insert_only`"));
        assertFalse(insertGenerator.getSql().contains("`update_only`"));

        WriteBean updateBean = new WriteBean();
        updateBean.id = 7L;
        updateBean.name = "Bob";
        updateBean.insertOnly = "ignored";
        updateBean.updateOnly = "changed";
        BeanWriteSql updateGenerator = new BeanWriteSql(updateBean);

        updateGenerator.getUpdateSqlWithId("id");

        assertTrue(updateGenerator.getSql().contains("`name` = ?"));
        assertTrue(updateGenerator.getSql().contains("`update_only` = ?"));
        assertFalse(updateGenerator.getSql().contains("`insert_only` = ?"));
        assertArrayEquals(new Object[]{"Bob", "changed", 7L}, updateGenerator.getParams());
    }

    @Test
    void rejectsIllegalColumnName() {
        BeanWriteSql generator = new BeanWriteSql("test_table", new IllegalColumnBean());

        assertThrows(IllegalArgumentException.class, generator::getInsertSql);
    }

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

    @Test
    void testGetInsertSqlWithBean() {
        TestBean bean = new TestBean();
        bean.setName("Bob");
        bean.setAge(30);

        BeanWriteSql generator = new BeanWriteSql(bean);
        generator.getInsertSql();

        assertEquals("INSERT INTO test_table (`real_age`, `name` ) VALUES (?, ?)", generator.getSql());
        assertArrayEquals(new Object[]{30, "Bob"}, generator.getParams());
    }

    @Test
    void testGetUpdateSqlWithBean() {
        TestBean bean = new TestBean();
        bean.setId(1L);
        bean.setName("David");
        bean.setTemp("ignored");
        bean.setAge(40);

        BeanWriteSql generator = new BeanWriteSql(bean);
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
        assertEquals(TABLE_NAME, BeanWriteSql.getTableNameByBean(bean));
    }

    @Test
    void skipsWriteOnlyProperties() {
        WriteOnlyBean bean = new WriteOnlyBean();
        bean.setSecret("ignored");

        BeanWriteSql generator = new BeanWriteSql(TABLE_NAME, bean);
        generator.getInsertSql();

        assertEquals("INSERT INTO test_table (`name` ) VALUES (?)", generator.getSql());
        assertArrayEquals(new Object[]{"readable"}, generator.getParams());
    }

    @Test
    void propagatesGetterFailureWithPropertyContext() {
        BeanWriteSql generator = new BeanWriteSql(TABLE_NAME, new FailingGetterBean());

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
