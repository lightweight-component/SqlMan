package com.ajaxjs.sqlman.sql;

import com.ajaxjs.util.io.Resources;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.sql.Connection;
import java.util.Properties;

public abstract class BaseTest {
    static final Properties config = Resources.getProperties("test.properties");

    protected static Connection conn;

    @BeforeAll
    static void setUpAll() {
        // 在这里放置一次性初始化代码
        conn = JdbcConn.getMySqlConnection(config.get("database.ipPort").toString(), "aj_base",
                config.get("database.username").toString(), config.get("database.password").toString());
    }

    @AfterAll
    static void end() {
        JdbcConn.closeDb(conn);
    }
}
