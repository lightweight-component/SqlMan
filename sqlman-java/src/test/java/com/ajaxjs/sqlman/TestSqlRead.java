//package com.ajaxjs.sqlman;
//
//import lombok.Data;
//import org.junit.jupiter.api.Test;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class TestSqlRead {
//
//    Connection conn = JdbcConn.getMySqlConnection();
//
//    @Test
//    public void testQueryOne() throws SQLException {
//        int result;
//        result = Sql.sql(conn,  "SELECT COUNT(*) AS total FROM shop_address").queryOne(int.class); // fetch the first one
//
//        System.out.println(result);
//        assertEquals(5, result);
//
//        result = new Sql(conn).input("SELECT COUNT(*) AS total FROM shop_address WHERE id = ?", 1).queryOne(int.class);
//        assertEquals(1, result);
//
//        result = new Sql(conn).input("SELECT id FROM ${tableName} WHERE id = #{stat}", Map.of("tableName", "shop_address", "stat", 1)).queryOne(int.class);
//        assertEquals(1, result);
//
//        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = ?", Map.of("tableName", "shop_address", "abc", 2), 1).queryOne(int.class);
//        System.out.println(result); // TODO, should be return 0
//
//        conn.close();
//    }
//
//    @Test
//    public void testQueryInfo() throws SQLException {
//        Map<String, Object> result;
//        result = new Sql(conn).input("SELECT * FROM shop_address").query(); // fetch the first one
//
//        System.out.println(result);
//        assertNotNull(result);
//
//        result = new Sql(conn).input("SELECT * FROM shop_address WHERE id = ?", 1).query();
//        assertNotNull(result);
//
//        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = #{stat}", Map.of("tableName", "shop_address", "stat", 1)).query();
//        assertNotNull(result);
//
//        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = ?", Map.of("tableName", "shop_address", "abc", 2), 1).query();
//        assertNotNull(result);
//
//        conn.close();
//    }
//
//    @Test
//    public void testQueryList() throws SQLException {
//        List<Map<String, Object>> result;
//        result = new Sql(conn).input("SELECT * FROM shop_address").queryList();
//
//        System.out.println(result);
//        assertTrue(result.size() > 1);
//
//        result = new Sql(conn).input("SELECT * FROM shop_address WHERE stat = ?", 1).queryList();
//
//        System.out.println(result.size());
//        assertEquals(2, result.size());
//
//        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE stat = #{stat}", Map.of("tableName", "shop_address", "stat", 1)).queryList();
//
//        System.out.println(result.size());
//        assertEquals(2, result.size());
//
//        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE stat = ?", Map.of("tableName", "shop_address", "abc", 2), 1).queryList();
//
//        System.out.println(result.size());
//        assertEquals(2, result.size());
//
//        conn.close();
//    }
//
//    @Data
//    public static class Address {
//        private Integer id;
//
//        private String name;
//
//        private String address;
//    }
//
//    @Test
//    public void testQueryInfoBean() throws SQLException {
//        Address result;
//        result = new Sql(conn).input("SELECT * FROM shop_address").query(Address.class); // fetch the first one
//
//        System.out.println(result);
//        assertNotNull(result);
//
//        result = new Sql(conn).input("SELECT * FROM shop_address WHERE id = ?", 1).query(Address.class);
//        assertNotNull(result);
//
//        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = #{stat}", Map.of("tableName", "shop_address", "stat", 1)).query(Address.class);
//        assertNotNull(result);
//
//        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = ?", Map.of("tableName", "shop_address", "abc", 2), 1).query(Address.class);
//        assertNotNull(result);
//
//        conn.close();
//    }
//
//    @Test
//    public void testQueryListBean() throws SQLException {
//        List<Address> result;
//        result = new Sql(conn).input("SELECT * FROM shop_address").queryList(Address.class);
//
//        System.out.println(result);
//        assertTrue(result.size() > 1);
//
//        result = new Sql(conn).input("SELECT * FROM shop_address WHERE stat = ?", 1).queryList(Address.class);
//
//        System.out.println(result.size());
//        assertEquals(2, result.size());
//
//        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE stat = #{stat}", Map.of("tableName", "shop_address", "stat", 1)).queryList(Address.class);
//
//        System.out.println(result.size());
//        assertEquals(2, result.size());
//
//        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE stat = ?", Map.of("tableName", "shop_address", "abc", 2), 1).queryList(Address.class);
//
//        System.out.println(result.size());
//        assertEquals(2, result.size());
//
//        conn.close();
//    }
//
//}
