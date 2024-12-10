package com.ajaxjs.sqlman.sql.crud;

import com.ajaxjs.sqlman.crud.Crud;
import com.ajaxjs.sqlman.crud.TableModel;
import com.ajaxjs.sqlman.sql.Address;
import com.ajaxjs.sqlman.sql.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class TestCrud extends BaseTest {
    @Test
    void testInfo() {
        TableModel tableModel = new TableModel();
        tableModel.setTableName("shop_address");

        Crud crud = new Crud(conn);
        Map<String, Object> result = crud.setTableModel(tableModel).info(1L).query();
        System.out.println(result);

        Address bean = crud.query(Address.class);
        System.out.println(bean);
    }

    @Test
    void testList() {
        TableModel tableModel = new TableModel();
        tableModel.setTableName("shop_address");
        tableModel.setHasIsDeleted(false);

        Crud crud = new Crud(conn);
        List<Map<String, Object>> result = crud.setTableModel(tableModel).list().queryList();
        System.out.println(result);

        List<Address> addresses = crud.queryList(Address.class);
        System.out.println(addresses);
    }
}
