package com.ajaxjs.sqlman;

import com.ajaxjs.sqlman.annotation.Table;
import com.ajaxjs.sqlman.model.NullValue;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestLocalDateTimeNullValue {
    @Test
    void supportsBeanInsertAndUpdateWhilePreservingOrdinaryValues() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:ldt_null")) {
            conn.createStatement().execute("CREATE TABLE null_items (id BIGINT PRIMARY KEY, expires TIMESTAMP DEFAULT TIMESTAMP '2020-01-01 00:00:00')");
            Item item = new Item();
            item.setId(1L);
            item.setExpires(NullValue.NULL_LOCAL_DATE_TIME);
            new Action(conn, item).create().execute(false);
            assertNull(new Action(conn, "SELECT expires FROM null_items WHERE id=1").query().one().get("expires"));

            LocalDateTime real = LocalDateTime.of(1970, 1, 1, 0, 0);
            item.setExpires(real);
            new Action(conn, item).update().withId();
            assertEquals(real, readTime(conn));

            item.setExpires(null);
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("id", 2L);
            new Action(conn, values, "null_items").create().execute(false);
            item.setId(2L);
            assertThrows(IllegalArgumentException.class, () -> new Action(conn, item).update().withId());
            assertEquals(LocalDateTime.of(2020, 1, 1, 0, 0),
                    new Action(conn, "SELECT expires FROM null_items WHERE id=2").query().one(Item.class).getExpires());

            item.setId(1L);
            item.setExpires(LocalDateTime.parse(NullValue.NULL_LOCAL_DATE_TIME.toString()));
            new Action(conn, item).update().withId();
            assertNull(readTime(conn));
        }
    }

    @Test
    void supportsMapMarker() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:ldt_map")) {
            conn.createStatement().execute("CREATE TABLE null_items (id BIGINT PRIMARY KEY, expires TIMESTAMP)");
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("id", 1L);
            values.put("expires", NullValue.NULL_LOCAL_DATE_TIME);
            new Action(conn, values, "null_items").create().execute(false);
            assertNull(readTime(conn));
        }
    }

    private LocalDateTime readTime(Connection conn) {
        Item item = new Action(conn, "SELECT id, expires FROM null_items WHERE id=1").query().one(Item.class);
        return item.getExpires();
    }

    @Table("null_items")
    public static class Item {
        private Long id;
        private LocalDateTime expires;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public LocalDateTime getExpires() { return expires; }
        public void setExpires(LocalDateTime expires) { this.expires = expires; }
    }
}
