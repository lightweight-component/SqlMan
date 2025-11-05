package com.ajaxjs.sqlman.page;

import com.ajaxjs.sqlman.crud.page.PageControl;
import com.ajaxjs.sqlman.model.DatabaseVendor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestPage {
    @Test
    void testPageControl() {
        String sql = "SELECT * FROM user WHERE state = 2 ORDER BY id";
        PageControl pc = new PageControl(DatabaseVendor.MYSQL, sql, 0, 10);
        pc.getCount();

        assertEquals("SELECT * FROM user WHERE state = 2 ORDER BY id LIMIT 0, 10", pc.getPagedSql());
        assertEquals("SELECT COUNT(*) FROM user WHERE state = 2", pc.getCountSql());

        pc = new PageControl(DatabaseVendor.SQL_SERVER, sql, 0, 10);
        pc.getCount();

        assertEquals("SELECT * FROM user WHERE state = 2 ORDER BY id OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY", pc.getPagedSql());
        assertEquals("SELECT COUNT(*) FROM user WHERE state = 2", pc.getCountSql());
    }
}
