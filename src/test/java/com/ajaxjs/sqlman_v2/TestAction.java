package com.ajaxjs.sqlman_v2;

import com.ajaxjs.sqlman.Address;
import com.ajaxjs.sqlman.BaseTest;
import com.ajaxjs.sqlman.model.CreateResult;
import com.ajaxjs.sqlman_v2.crud.Create;
import com.ajaxjs.sqlman_v2.sqlgenerator.Entity2WriteSql;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class TestAction extends BaseTest {
    @Test
    void testEntityCreate() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("address", "HK");
        map.put("phone", "110");

        Entity2WriteSql generator = new Entity2WriteSql(map);
        generator.setTableName("shop_address");
        generator.getInsertSql();

        String sql = generator.getSql();

        Action action = new Action();
        action.setConn(conn);
        action.setSql(sql);
        action.setParams(generator.getParams());

        Create create = new Create(action);
        CreateResult<Integer> result = create.create(true, Integer.class);
        System.out.println(result);
    }

    @Test
    void testEntityCreateShort() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("address", "HK");
        map.put("phone", "110");

        new Action(conn, map, "shop_address").create(true, Integer.class);

        Address address = new Address();
        address.setName("出差");
        address.setAddress("广州");
        address.setPhone("188");
        address.setPhone2("188");
        address.setRe("Tom");

        new Action(conn, address, "shop_address").create(true, Integer.class);

        new Action(conn, address).create(true, Integer.class);
    }
}
