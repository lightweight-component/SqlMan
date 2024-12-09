package com.ajaxjs.sqlman;

import org.junit.jupiter.api.Test;

public class TestSql {
    @Test
    public void testQuery() {
        Sql sql = new Sql();
        sql.sql("SELECT * FROM user WHERE ID = ?");
    }

    @Test
    public void testCreate() {
        Sql sql = new Sql();
//        Create create = sql.sql("SELECT * FROM user WHERE ID = ?").setIdType(int.class).create();
//
//        assertTrue(create.isOk());
//        System.out.println(create.getNewlyId());
    }
}
