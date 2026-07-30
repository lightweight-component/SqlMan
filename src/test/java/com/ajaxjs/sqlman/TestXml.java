package com.ajaxjs.sqlman;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestXml {
    private SmallMyBatis mapper;

    @BeforeEach
    void loadStatements() {
        mapper = new SmallMyBatis();
        mapper.loadXML("sql/mysql.xml");
    }

    @Test
    void loadsSqlById() {
        assertEquals("SELECT COUNT(*) AS total FROM shop_address",
                mapper.getSqlById("foo").trim());
        assertThrows(IllegalArgumentException.class, () -> mapper.getSqlById("missing"));
    }

    @Test
    void evaluatesIfBlocksAndPlaceholders() {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "address");

        String addressSql = normalize(mapper.handleSql(params, "foo-5"));
        assertTrue(addressSql.contains("FROM shop_address"));
        assertFalse(addressSql.contains("<if"));

        params.put("type", "article");
        params.put("tableName", "article");
        String articleSql = normalize(mapper.handleSql(params, "foo-5"));
        assertTrue(articleSql.contains("FROM article"));
        assertFalse(articleSql.contains("shop_address"));
    }

    @Test
    void formatsHashAndDollarPlaceholdersAccordingToCurrentContract() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "shop_address");
        params.put("stat", 1);

        assertEquals("SELECT id FROM shop_address WHERE id = 1",
                normalize(mapper.handleSql(params, "foo-3")));
    }

    private static String normalize(String sql) {
        return sql.trim().replaceAll("\\s+", " ");
    }
}
