package com.ajaxjs.sqlman;

import com.ajaxjs.sqlman.model.PageResult;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.ajaxjs.util.ObjectHelper.mapOf;
import static org.junit.jupiter.api.Assertions.*;

public class TestSqlRead extends BaseTest {
    @Test
    public void testQueryOne() {
        int result;
        result = new Sql(conn).input("SELECT COUNT(*) AS total FROM shop_address").queryOne(int.class); // fetch the first one
        assertTrue(result > 0);

        result = new Sql(conn).input("SELECT COUNT(*) AS total FROM shop_address WHERE id = ?", 1).queryOne(int.class);
        assertEquals(1, result);

        result = new Sql(conn).input("SELECT id FROM ${tableName} WHERE id = #{stat}", mapOf("tableName", "shop_address", "stat", 1)).queryOne(int.class);
        assertEquals(1, result);

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = ?", mapOf("tableName", "shop_address", "abc", 2), 1).queryOne(int.class);
        System.out.println(result); // TODO, should be return 0
    }

    @Test
    public void testQueryBlob() throws SQLException {
        try (Connection conn = DriverManager.getConnection(String.format(JdbcConnection.MYSQL_CONN, "", "user_interaction"), "root", "")) {
            Map<String, Object> result;
            result = new Sql(conn).input("SELECT * FROM user WHERE id = 8").query();
            System.out.println(result);
        }
    }

    @Test
    public void testQueryInfo() {
        Map<String, Object> result;
        result = new Sql(conn).input("SELECT * FROM shop_address").query(); // fetch the first one
        assertNotNull(result);

        result = new Sql(conn).input("SELECT * FROM shop_address WHERE id = ?", 1).query();
        assertNotNull(result);

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = #{stat}", mapOf("tableName", "shop_address", "stat", 1)).query();
        assertNotNull(result);

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = ?", mapOf("tableName", "shop_address", "abc", 2), 1).query();
        assertNotNull(result);
    }

    @Test
    public void testQueryList() {
        List<Map<String, Object>> result;
        result = new Sql(conn).input("SELECT * FROM shop_address").queryList();
        assertTrue(result.size() > 1);

        result = new Sql(conn).input("SELECT * FROM shop_address WHERE stat = ?", 1).queryList();
        assertEquals(2, result.size());

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE stat = #{stat}", mapOf("tableName", "shop_address", "stat", 1)).queryList();
        assertEquals(2, result.size());

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE stat = ?", mapOf("tableName", "shop_address", "abc", 2), 1).queryList();
        assertEquals(2, result.size());
    }

    @Test
    public void testQueryInfoBean() {
        Address result;

        result = new Sql(conn).input("SELECT * FROM shop_address").query(Address.class); // fetch the first one
        assertNotNull(result);

        result = new Sql(conn).input("SELECT * FROM shop_address WHERE id = ?", 1).query(Address.class);
        assertNotNull(result);

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = #{stat}", mapOf("tableName", "shop_address", "stat", 1)).query(Address.class);
        assertNotNull(result);

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE id = ?", mapOf("tableName", "shop_address", "abc", 2), 1).query(Address.class);
        assertNotNull(result);
    }

    @Test
    public void testQueryListBean() {
        List<Address> result;
        result = new Sql(conn).input("SELECT * FROM shop_address").queryList(Address.class);
        assertTrue(result.size() > 1);

        result = new Sql(conn).input("SELECT * FROM shop_address WHERE stat = ?", 1).queryList(Address.class);
        assertEquals(2, result.size());

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE stat = #{stat}", mapOf("tableName", "shop_address", "stat", 1)).queryList(Address.class);
        assertEquals(2, result.size());

        result = new Sql(conn).input("SELECT * FROM ${tableName} WHERE stat = ?", mapOf("tableName", "shop_address", "abc", 2), 1).queryList(Address.class);
        assertEquals(2, result.size());
    }

    @Test
    public void testPage() {
        PageResult<Map<String, Object>> result;
        result = new Sql(conn).input("SELECT * FROM shop_address").page();
        assertNotNull(result);

        result = new Sql(conn).input("SELECT * FROM shop_address where stat = ?", 1).page();
        assertNotNull(result);

        result = new Sql(conn).input("SELECT * FROM shop_address").page(3, 5);
        assertNotNull(result);

        PageResult<Address> result2;
        result2 = new Sql(conn).input("SELECT * FROM shop_address").page(Address.class, 1, 2);
        assertNotNull(result2);

        result2 = new Sql(conn).input("SELECT * FROM shop_address").page(Address.class, 100, 2);
        assertEquals(0, result2.size());

//Sql sqlServer = new Sql(conn);
//sqlServer.setDatabaseVendor(JdbcConstants.DatabaseVendor.SQL_SERVER);
//sqlServer.input("SELECT * FROM article").page();
    }
}
