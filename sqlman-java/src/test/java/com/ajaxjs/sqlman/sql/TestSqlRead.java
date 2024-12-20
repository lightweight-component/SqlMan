package com.ajaxjs.sqlman.sql;

import com.ajaxjs.sqlman.model.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestSqlRead extends BaseTest {
    @Test
    public void testQueryOne() {
        int result;
        result = new Sql(conn).input("SELECT COUNT(*) AS total FROM shop_address").queryOne(int.class); // fetch the first one
        assertTrue( result>0);

        result = new Sql(conn).input("SELECT COUNT(*) AS total FROM shop_address WHERE id = ?", 1).queryOne(int.class);
        assertEquals(1, result);

        result = new Sql(conn).input("SELECT id FROM ${tableName} WHERE id = #{stat}", Map.of("tableName", "shop_address", "stat", 1)).queryOne(int.class);
        assertEquals(1, result);

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = ?", Map.of("tableName", "shop_address", "abc", 2), 1).queryOne(int.class);
        System.out.println(result); // TODO, should be return 0
    }

    @Test
    public void testQueryInfo() {
        Map<String, Object> result;
        result = new Sql(conn).input("SELECT * FROM shop_address").query(); // fetch the first one
        assertNotNull(result);

        result = new Sql(conn).input("SELECT * FROM shop_address WHERE id = ?", 1).query();
        assertNotNull(result);

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = #{stat}", Map.of("tableName", "shop_address", "stat", 1)).query();
        assertNotNull(result);

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = ?", Map.of("tableName", "shop_address", "abc", 2), 1).query();
        assertNotNull(result);

    }

    @Test
    public void testQueryList() {
        List<Map<String, Object>> result;
        result = new Sql(conn).input("SELECT * FROM shop_address").queryList();
        assertTrue(result.size() > 1);

        result = new Sql(conn).input("SELECT * FROM shop_address WHERE stat = ?", 1).queryList();
        assertEquals(2, result.size());

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE stat = #{stat}", Map.of("tableName", "shop_address", "stat", 1)).queryList();
        assertEquals(2, result.size());

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE stat = ?", Map.of("tableName", "shop_address", "abc", 2), 1).queryList();
        assertEquals(2, result.size());
    }

    @Test
    public void testQueryInfoBean() {
        Address result;

        result = new Sql(conn).input("SELECT * FROM shop_address").query(Address.class); // fetch the first one
        assertNotNull(result);

        result = new Sql(conn).input("SELECT * FROM shop_address WHERE id = ?", 1).query(Address.class);
        assertNotNull(result);

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = #{stat}", Map.of("tableName", "shop_address", "stat", 1)).query(Address.class);
        assertNotNull(result);

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = ?", Map.of("tableName", "shop_address", "abc", 2), 1).query(Address.class);
        assertNotNull(result);
    }

    @Test
    public void testQueryListBean() {
        List<Address> result;
        result = new Sql(conn).input("SELECT * FROM shop_address").queryList(Address.class);
        assertTrue(result.size() > 1);

        result = new Sql(conn).input("SELECT * FROM shop_address WHERE stat = ?", 1).queryList(Address.class);
        assertEquals(2, result.size());

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE stat = #{stat}", Map.of("tableName", "shop_address", "stat", 1)).queryList(Address.class);
        assertEquals(2, result.size());

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE stat = ?", Map.of("tableName", "shop_address", "abc", 2), 1).queryList(Address.class);
        assertEquals(2, result.size());
    }

    @Test
    void testPage() {
        PageResult<Map<String, Object>> result;
        result = new Sql(conn).input("SELECT * FROM article").page();
        assertNotNull(result);

        result = new Sql(conn).input("SELECT * FROM article").page(3, 5);
        assertNotNull(result);

        PageResult<Address> result2;
        result2 = new Sql(conn).input("SELECT * FROM shop_address").page(Address.class, 1, 2);
        assertNotNull(result2);

        result2 = new Sql(conn).input("SELECT * FROM shop_address").page(Address.class, 100, 2);
        assertEquals(0, result2.size());
    }
}
