package com.ajaxjs.data.jdbc_helper;

import com.ajaxjs.data.BaseTest;
import com.ajaxjs.data.data_service.DataService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class TestJdbcHelper extends BaseTest {
    @Autowired
    DataSource ds;

    @Test
    public void test() throws SQLException {
        JdbcReader reader = new JdbcReader();
        reader.setConn(ds.getConnection());
        List<Map<String, Object>> list = reader.queryAsMapList("SELECT * FROM Employees");
        System.out.println(list);

        assertNotNull(list);
    }
}
