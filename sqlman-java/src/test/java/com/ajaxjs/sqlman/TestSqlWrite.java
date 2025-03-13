package com.ajaxjs.sqlman;

import com.ajaxjs.sqlman.model.Create;
import com.ajaxjs.sqlman.model.Update;
import org.junit.jupiter.api.Test;

import static com.ajaxjs.util.ObjectHelper.mapOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSqlWrite extends BaseTest {
    @Test
    void testCreate() {
        String sql = "INSERT INTO shop_address (name, address, phone, receiver) " +
                "VALUES ('家', '北京路', '3412', 'Jack')";
        Create<Integer> result;
        result = new Sql(conn).input(sql).create(true, Integer.class);
        assertTrue(result.isOk());

        sql = "INSERT INTO shop_address (name, address, phone, receiver) " +
                "VALUES ('家', ?, '3412', ?)";
        result = new Sql(conn).input(sql, "南京路", "Tom").create(true, Integer.class);
        assertTrue(result.isOk());
    }

    @Test
    void testUpdate() {
        String sql = "UPDATE shop_address SET name= '公司' WHERE id = ?";

        Update result;
        result = new Sql(conn).input(sql, 8).update();
        assertTrue(result.isOk());

        String sql2 = "UPDATE ${tableName} SET name= '公司' WHERE id = ?";
        result = new Sql(conn).input(sql2, mapOf("tableName", "shop_address"), 9).update();
        assertTrue(result.isOk());

        String sql3 = "DELETE FROM ${tableName} WHERE  id = 10"; // Delete 也是 update
        result = new Sql(conn).input(sql3, mapOf("tableName", "shop_address")).update();
        assertTrue(result.isOk());
    }
}
