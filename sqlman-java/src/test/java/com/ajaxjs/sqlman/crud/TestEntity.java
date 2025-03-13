package com.ajaxjs.sqlman.crud;

import com.ajaxjs.sqlman.model.Create;
import com.ajaxjs.sqlman.model.PageResult;
import com.ajaxjs.sqlman.model.TableModel;
import com.ajaxjs.sqlman.model.Update;
import com.ajaxjs.sqlman.Address;
import com.ajaxjs.sqlman.BaseTest;
import org.junit.jupiter.api.Test;

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
        assertFalse(result.isEmpty());

        Address bean = entity.query(Address.class);
        assertNotNull(bean);
    }

    @Test
    void testList() {
        TableModel tableModel = new TableModel();
        tableModel.setTableName("shop_address");
        tableModel.setHasIsDeleted(false);

        Entity entity = new Entity(conn);
        List<Map<String, Object>> result = entity.setTableModel(tableModel).list().queryList();
        assertFalse(result.isEmpty());

        List<Address> addresses = entity.queryList(Address.class);
        assertNotNull(addresses);
    }

    @Test
    void testPage() {
        TableModel tableModel = new TableModel();
        tableModel.setTableName("shop_address");
        tableModel.setHasIsDeleted(false);

        Entity entity = new Entity(conn);
        PageResult<Address> result = entity.setTableModel(tableModel).list().page(Address.class);
        assertFalse(result.isEmpty());

        PageResult<Object> article = new Entity(conn).setTableName("article").list().page();
        assertFalse(article.isEmpty());
    }

    @Test
    void testCreate() {
        Address address = new Address();
        address.setName("出差");
        address.setAddress("广州");
        address.setPhone("188");
        address.setPhone2("188");
        address.setRe("Tom");

        TableModel tableModel = new TableModel();
        tableModel.setTableName("shop_address");
        tableModel.setAutoIns(true);
        tableModel.setIdTypeClz(Integer.class);

        Create<Integer> result = new Entity(conn).setTableModel(tableModel).input(address).create(Integer.class);
        assertNotNull(result.getNewlyId());
        assertTrue(result.isOk());
    }

    @Test
    void testUpdate() {
        Address address = new Address();
        address.setId(1);
        address.setName("出差");
        address.setAddress("广州");
        address.setPhone("188");
        address.setPhone2("188");
        address.setRe("Tom");

        TableModel tableModel = new TableModel();
        tableModel.setTableName("shop_address");
        tableModel.setAutoIns(true);
        tableModel.setIdTypeClz(Integer.class);

        Update result;
        result = new Entity(conn).setTableModel(tableModel).input(address).update();

        assertTrue(result.isOk());
    }
}
