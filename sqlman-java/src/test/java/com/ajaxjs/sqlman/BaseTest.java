package com.ajaxjs.sqlman;

import com.ajaxjs.util.io.Resources;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;


public abstract class BaseTest {
    static final Properties config = Resources.getProperties("test-demo.properties");

    protected static Connection conn;

    @BeforeAll
    static void setUpAll() throws SQLException {
        // 配置 H2 数据源
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("password");

        // 获取数据库连接
        conn = dataSource.getConnection();

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE shop_address (\n" +
                    "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    name VARCHAR(255) NOT NULL,\n" +
                    "    address VARCHAR(255) NOT NULL,\n" +
                    "    phone VARCHAR(20),\n" +
                    "    receiver VARCHAR(255),\n" +
                    "    stat INT,\n" +
                    "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\n" +
                    ");");

            stmt.execute("INSERT INTO shop_address (name, address, phone, receiver, stat)\n" +
                    "VALUES\n" +
                    "('Shop A', '123 Main St', '123-456-7890', 'John Doe', 0),\n" +
                    "('Shop B', '456 Elm St', '234-567-8901', 'Jane Smith',0),\n" +
                    "('Shop C', '789 Oak St', '345-678-9012', 'Alice Johnson', 0),\n" +
                    "('Shop D', '101 Maple St', '456-789-0123', 'Bob Brown', 1),\n" +
                    "('Shop E', '202 Birch St', '567-890-1234', 'Charlie Davis', 1);");
        }

        // 在这里放置一次性初始化代码
//        conn = JdbcConnection.getMySqlConnection(config.get("database.ipPort").toString(), "aj_base",
//                config.get("database.username").toString(), config.get("database.password").toString());
    }

    @AfterAll
    static void end() {
        JdbcConnection.closeDb(conn);
    }
}
