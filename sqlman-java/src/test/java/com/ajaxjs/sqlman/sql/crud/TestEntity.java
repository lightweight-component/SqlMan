package com.ajaxjs.sqlman.sql.crud;

import com.ajaxjs.sqlman.crud.Entity;
import com.ajaxjs.sqlman.model.Create;
import com.ajaxjs.sqlman.model.JdbcConstants;
import com.ajaxjs.sqlman.model.PageResult;
import com.ajaxjs.sqlman.model.TableModel;
import com.ajaxjs.sqlman.sql.Address;
import com.ajaxjs.sqlman.sql.BaseTest;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestEntity extends BaseTest {
    @Test
    void testInfo() {
        TableModel tableModel = new TableModel();
        tableModel.setTableName("shop_address");

        Entity entity = new Entity(conn);
        Map<String, Object> result = entity.setTableModel(tableModel).info(1L).query();
        System.out.println(result);

        Address bean = entity.query(Address.class);
        System.out.println(bean);
        assertFalse(result.isEmpty());
    }

    @Test
    void testList() {
        TableModel tableModel = new TableModel();
        tableModel.setTableName("shop_address");
        tableModel.setHasIsDeleted(false);

        Entity entity = new Entity(conn);
        List<Map<String, Object>> result = entity.setTableModel(tableModel).list().queryList();
        System.out.println(result);

        List<Address> addresses = entity.queryList(Address.class);
        System.out.println(addresses);
        assertFalse(result.isEmpty());
    }

    @Test
    void testPage() {
        TableModel tableModel = new TableModel();
        tableModel.setTableName("shop_address");
        tableModel.setHasIsDeleted(false);

        Entity entity = new Entity(conn);
        PageResult<Address> result = entity.setTableModel(tableModel).list().page(Address.class);
        System.out.println(result);
        assertFalse(result.isEmpty());

        PageResult<Object> article = new Entity(conn).setTableName("article").list().page();
        System.out.println(article);
        assertFalse(article.isEmpty());
    }

    @Test
    void testCreate() {
        Address address = new Address();
        address.setName("出差");
        address.setAddress("广州");
        address.setPhone("188");

        TableModel tableModel = new TableModel();
        tableModel.setTableName("shop_address");
        tableModel.setAutoIns(true);
        tableModel.setIdTypeClz(Integer.class);

        Create<Integer> result = new Entity(conn).setTableModel(tableModel).input(address).create(Integer.class);
        System.out.println(result);
        assertNotNull(result.getNewlyId());
        assertTrue(result.isOk());
    }
}
