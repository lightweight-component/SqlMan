package com.ajaxjs.sqlman;

import com.ajaxjs.sqlman.crud.page.PageResult;
import com.ajaxjs.sqlman.model.CreateResult;
import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman.testcase.Address;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.ajaxjs.util.ObjectHelper.mapOf;
import static org.junit.jupiter.api.Assertions.*;

class TestSqlAction extends BaseTest {
    @Test
    void testQuery() {
        int result;
        result = new Action(conn, "SELECT COUNT(*) AS total FROM shop_address").query().oneValue(int.class);
        assertTrue(result > 0);

        result = new Action(conn, "SELECT COUNT(*) AS total FROM shop_address WHERE id = ?").query(1).oneValue(int.class);
        assertEquals(1, result);

        result = new Action(conn, "SELECT id FROM ${tableName} WHERE id = #{stat}").query(mapOf("tableName", "shop_address", "stat", 1)).oneValue(int.class);
        assertEquals(1, result);

        result = new Action(conn, "SELECT * FROM ${tableName} WHERE id = ?").query(mapOf("tableName", "shop_address", "abc", 2), 1).oneValue(int.class);
        System.out.println(result); // TODO, should be return 0
    }

    @Test
    void testQueryInfo() {
        Map<String, Object> result;
        result = new Action(conn, "SELECT * FROM shop_address").query().one(); // fetch the first one
        assertNotNull(result);

        result = new Action(conn, "SELECT * FROM shop_address WHERE id = ?").query(1).one();
        assertNotNull(result);

        result = new Action(conn, "SELECT * FROM ${tableName} WHERE id = #{stat}").query(mapOf("tableName", "shop_address", "stat", 1)).one();
        assertNotNull(result);

        result = new Action(conn, "SELECT * FROM ${tableName} WHERE id = ?").query(mapOf("tableName", "shop_address", "abc", 2), 1).one();
        assertNotNull(result);
    }

    @Test
    void testQueryList() {
        List<Map<String, Object>> result;
        result = new Action(conn, "SELECT * FROM shop_address").query().list();
        assertTrue(result.size() > 1);

        result = new Action(conn, "SELECT * FROM shop_address WHERE stat = ?").query(1).list();
        assertEquals(2, result.size());

        result = new Action(conn, "SELECT * FROM ${tableName} WHERE stat = #{stat}").query(mapOf("tableName", "shop_address", "stat", 1)).list();
        assertEquals(2, result.size());

        result = new Action(conn, "SELECT * FROM ${tableName} WHERE stat = ?").query(mapOf("tableName", "shop_address", "abc", 2), 1).list();
        assertEquals(2, result.size());
    }

    @Test
    void testQueryInfoBean() {
        Address result;

        result = new Action(conn, "SELECT * FROM shop_address").query().one(Address.class); // fetch the first one
        assertNotNull(result);

        result = new Action(conn, "SELECT * FROM shop_address WHERE id = ?").query(1).one(Address.class);
        assertNotNull(result);

        result = new Action(conn, "SELECT * FROM ${tableName} WHERE id = #{stat}").query(mapOf("tableName", "shop_address", "stat", 1)).one(Address.class);
        assertNotNull(result);

        result = new Action(conn, "SELECT * FROM ${tableName} WHERE id = ?").query(mapOf("tableName", "shop_address", "abc", 2), 1).one(Address.class);
        assertNotNull(result);
    }

    @Test
    void testQueryListBean() {
        List<Address> result;
        result = new Action(conn, "SELECT * FROM shop_address").query().list(Address.class);
        assertTrue(result.size() > 1);

        result = new Action(conn, "SELECT * FROM shop_address WHERE stat = ?").query(1).list(Address.class);
        assertEquals(2, result.size());

        result = new Action(conn, "SELECT * FROM ${tableName} WHERE stat = #{stat}").query(mapOf("tableName", "shop_address", "stat", 1)).list(Address.class);
        assertEquals(2, result.size());

        result = new Action(conn, "SELECT * FROM ${tableName} WHERE stat = ?").query(mapOf("tableName", "shop_address", "abc", 2), 1).list(Address.class);
        assertEquals(2, result.size());
    }

    @Test
    void testPage() {
        PageResult<Map<String, Object>> result;
        result = new Action(conn, "SELECT * FROM shop_address").query().pageByPageNo(1, 3);
        assertEquals(3, result.getList().size());
        assertNotNull(result);

        result = new Action(conn, "SELECT * FROM shop_address where stat = ?").query(1).pageByPageNo(1, 3);
        assertNotNull(result);
        assertEquals(2, result.getList().size());

        result = new Action(conn, "SELECT * FROM shop_address").query().pageByStartLimit(3, 5);
        assertNotNull(result);

        PageResult<Address> result2;
        result2 = new Action(conn, "SELECT * FROM shop_address").query().pageByStartLimit(1, 2, Address.class);
        assertNotNull(result2);

        result2 = new Action(conn, "SELECT * FROM shop_address").query().pageByStartLimit(100, 2, Address.class);
        assertEquals(0, result2.getTotalCount());
    }

    @Test
    public void testCreate() {
        String sql = "INSERT INTO shop_address (name, address, phone, receiver) " +
                "VALUES ('家', '北京路', '3412', 'Jack')";
        CreateResult<Integer> result;
        result = new Action(conn, sql).create().execute(true, Integer.class);
        assertTrue(result.isOk());

        sql = "INSERT INTO shop_address (name, address, phone, receiver) " +
                "VALUES (${name}, ?, '3412', ?)";

        // mixing parameters with Map and Array
        result = new Action(conn, sql).setParams(mapOf("name", "'office'"), "Kid Place", "Tom").create().execute(true, Integer.class);
        assertTrue(result.isOk());

        Address address = new Action(conn, "SELECT * FROM shop_address WHERE id = ?").query(result.getNewlyId()).one(Address.class);
        System.out.println(address);
        assertNotNull(address);
    }

    @Test
    public void testUpdate() {
        String sql = "UPDATE shop_address SET name= '公司' WHERE id = ?";

        UpdateResult result;
        result = new Action(conn, sql).setParams(2).update().execute();
        assertTrue(result.isOk());

        String sql2 = "UPDATE ${tableName} SET name= '公司' WHERE id = ?";
        result = new Action(conn, sql2).setParams(mapOf("tableName", "shop_address"), 2).update().execute();
        assertTrue(result.isOk());

        String sql3 = "DELETE FROM ${tableName} WHERE id = 3"; // Delete 也是 update
        result = new Action(conn, sql3).setParams(mapOf("tableName", "shop_address")).update().execute();
        assertTrue(result.isOk());
        System.out.println(result.getEffectedRows());
    }

    @Test
    public void testDelete() {
        UpdateResult result = new Action(conn).update().delete("shop_address", "id", 1);
        assertTrue(result.isOk());
    }
}
