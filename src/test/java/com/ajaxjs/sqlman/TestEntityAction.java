package com.ajaxjs.sqlman;

import com.ajaxjs.sqlman.crud.Create;
import com.ajaxjs.sqlman.model.CreateResult;
import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman.sqlgenerator.Entity2WriteSql;
import com.ajaxjs.sqlman.testcase.Address;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestEntityAction extends BaseTest {
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

        Action action = new Action(conn);
        action.setSql(sql);
        action.setParams(generator.getParams());

        Create create = new Create(action);
        CreateResult<Integer> result = create.execute(true, Integer.class);

        assertTrue(result.isOk());
    }

    @Test
    void testEntityCreateShort() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("address", "HK");
        map.put("phone", "110");

        CreateResult<Integer> result = new Action(conn, map, "shop_address").create().execute(true, Integer.class);
        assertNotNull(result.getNewlyId());
        assertTrue(result.isOk());

        Address address = new Address();
        address.setName("出差");
        address.setAddress("广州");
        address.setPhone("188");
        address.setPhone2("188");
        address.setRe("Tom");

        result = new Action(conn, address, "shop_address").create().execute(true, Integer.class);
        assertTrue(result.isOk());
        assertNotNull(result.getNewlyId());

        result = new Action(conn, address).create().execute(true, Integer.class);
        assertTrue(result.isOk());
        assertNotNull(result.getNewlyId());
    }

    @Test
    void testEntityUpdate() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("address", "HK");
        map.put("phone", "110");

        UpdateResult updateResult = new Action(conn, map, "shop_address").update().withId("id", 1L);
        assertTrue(updateResult.isOk());

        Address address = new Address();
        address.setName("出差");
        address.setAddress("广州");
        address.setPhone("188");
        address.setPhone2("188");
        address.setRe("Tom");

        updateResult = new Action(conn, address, "shop_address").update().withId("id", 1L);
        assertTrue(updateResult.isOk());

        updateResult = new Action(conn, address).update().withId("id", 1L);
        assertTrue(updateResult.isOk());

        // auto id obtaining
        map.put("id", 2);
        updateResult = new Action(conn, map, "shop_address").update().withId("id");
        assertTrue(updateResult.isOk());

        address.setId(2);
        updateResult = new Action(conn, address).update().withId("id");
        assertTrue(updateResult.isOk());
    }

    @Test
    void testEntityUpdateWhere() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("address", "HK");
        map.put("phone", "110");

        UpdateResult updateResult = new Action(conn, map, "shop_address").update().execute("`id` = 1");
        assertTrue(updateResult.isOk());

        Address address = new Address();
        address.setName("出差");
        address.setAddress("广州");
        address.setPhone("188");
        address.setPhone2("188");
        address.setRe("Tom");

        updateResult = new Action(conn, address).update().execute("id = 1");
        assertTrue(updateResult.isOk());
    }

    @Test
    void testEntityDelete() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("address", "HK");
        map.put("phone", "110");

        UpdateResult updateResult = new Action(conn, map, "shop_address").update().delete("id", 1);
        assertTrue(updateResult.isOk());

        updateResult = new Action(conn, map, "shop_address").update().delete(2);
        assertTrue(updateResult.isOk());

        map.put("id", 3);
        updateResult = new Action(conn, map, "shop_address").update().delete();
        assertTrue(updateResult.isOk());

//        updateResult = new Action(conn, map, "shop_address").update().delete("id", 1);
//        assertTrue(updateResult.isOk());
//
//        Address address = new Address();
//        address.setName("出差");
//        address.setAddress("广州");
//        address.setPhone("188");
//        address.setPhone2("188");
//        address.setRe("Tom");
//
//        updateResult = new Action(conn, address).update().execute("id = 1");
//        assertTrue(updateResult.isOk());
    }
}
