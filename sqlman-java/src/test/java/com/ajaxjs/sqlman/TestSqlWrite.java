package com.ajaxjs.sqlman;

import com.ajaxjs.sqlman.model.CreateResult;
import com.ajaxjs.sqlman.model.UpdateResult;
import org.junit.jupiter.api.Test;

import static com.ajaxjs.util.ObjectHelper.mapOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSqlWrite extends BaseTest {
    @Test
    public   void testCreate() {
        String sql = "INSERT INTO shop_address (name, address, phone, receiver) " +
                "VALUES ('家', '北京路', '3412', 'Jack')";
        CreateResult<Integer> result;
        result = new Sql(conn).input(sql).create(true, Integer.class);
        assertTrue(result.isOk());

        sql = "INSERT INTO shop_address (name, address, phone, receiver) " +
                "VALUES (${name}, ?, '3412', ?)";

        // mixing parameters with Map and Array
        result = new Sql(conn).input(sql, mapOf("name", "'office'"),"Kid Place", "Tom").create(true, Integer.class);
        assertTrue(result.isOk());

        Address address = new Sql(conn).input("SELECT * FROM shop_address WHERE id = ?", result.getNewlyId()).query(Address.class);
        System.out.println(address);
    }

    @Test
   public void testUpdate() {
        String sql = "UPDATE shop_address SET name= '公司' WHERE id = ?";

        UpdateResult result;
        result = new Sql(conn).input(sql, 8).update();
        assertTrue(result.isOk());

        String sql2 = "UPDATE ${tableName} SET name= '公司' WHERE id = ?";
        result = new Sql(conn).input(sql2, mapOf("tableName", "shop_address"), 9).update();
        assertTrue(result.isOk());

        String sql3 = "DELETE FROM ${tableName} WHERE  id = 10"; // Delete 也是 update
        result = new Sql(conn).input(sql3, mapOf("tableName", "shop_address")).update();
        assertTrue(result.isOk());
        System.out.println(result.getEffectedRows());
    }

    @Test
    public void testDelete() {
        UpdateResult result = new Sql(conn).delete("shop_address", "id", 1);
        assertTrue(result.isOk());
    }
}
