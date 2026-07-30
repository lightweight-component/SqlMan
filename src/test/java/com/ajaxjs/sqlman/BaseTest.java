package com.ajaxjs.sqlman;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseTest {
    protected Connection conn;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + getClass().getSimpleName() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("password");
        conn = dataSource.getConnection();

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS shop_address");
            stmt.execute("CREATE TABLE shop_address (\n" +
                    "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    name VARCHAR(255) NOT NULL,\n" +
                    "    address VARCHAR(255) NOT NULL,\n" +
                    "    phone VARCHAR(20),\n" +
                    "    receiver VARCHAR(255),\n" +
                    "    stat INT,\n" +
                    "    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\n" +
                    ");");

            stmt.execute("INSERT INTO shop_address (name, address, phone, receiver, stat)\n" +
                    "VALUES\n" +
                    "('Shop A', '123 Main St', '123-456-7890', 'John Doe', 0),\n" +
                    "('Shop B', '456 Elm St', '234-567-8901', 'Jane Smith',0),\n" +
                    "('Shop C', '789 Oak St', '345-678-9012', 'Alice Johnson', 0),\n" +
                    "('Shop D', '101 Maple St', '456-789-0123', 'Bob Brown', 1),\n" +
                    "('Shop E', '202 Birch St', '567-890-1234', 'Charlie Davis', 1);");
        }
    }

    @AfterEach
    void tearDown() {
        JdbcConnection.closeDb(conn);
    }
}
