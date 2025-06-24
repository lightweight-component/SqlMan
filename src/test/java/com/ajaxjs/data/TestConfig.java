package com.ajaxjs.data;


import com.ajaxjs.data.crud.CRUD_Service;
import com.ajaxjs.data.jdbc_helper.DatabaseVendor;
import com.ajaxjs.data.jdbc_helper.JdbcConn;
import com.ajaxjs.data.jdbc_helper.JdbcReader;
import com.ajaxjs.data.jdbc_helper.JdbcWriter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Configuration
@ComponentScan("com.ajaxjs.data")
public class TestConfig {
    static String mkdir() {
        File newDirectory = BaseTest.getTestDir();

//        if (newDirectory.mkdir())
//            System.out.println("新目录创建成功：" + newDirectory.getAbsolutePath());
//        else
//            System.out.println("新目录创建失败，可能目录已存在或没有权限。");

        // 先删除
        BaseTest.deleteDirectory(newDirectory);

        return newDirectory.getAbsolutePath().replace("\\.", "");
    }

    static String CREATE_TABLE = "CREATE TABLE Employees (\n" +
            "    id INT PRIMARY KEY,\n" +
            "    name VARCHAR(50),\n" +
            "    birthday DATE,\n" +
            "    hire_date DATE,\n" +
            "    department VARCHAR(50)\n" +
            ")";

    @Bean(value = "dataSource", destroyMethod = "close")
    public DataSource dataSource() throws SQLException {
        System.out.println(mkdir());
        String jdbcStr = "jdbc:derby:" + mkdir() + ";create=true";
        DataSource dataSource = JdbcConn.setupJdbcPool("org.apache.derby.jdbc.EmbeddedDriver", jdbcStr, "", "");

        // 创建测试数据
        try (Connection conn = dataSource.getConnection()) {
            JdbcWriter writer = new JdbcWriter();
            writer.setConn(conn);
            writer.insert(CREATE_TABLE);
            writer.insert("INSERT INTO Employees (id, name, birthday, hire_date, department) VALUES (1, 'John', '1980-01-01', '2005-06-15', 'Finance')");
            writer.insert("INSERT INTO Employees (id, name, hire_date, department) VALUES (2, 'Jane Smith', '2010-08-23', 'Sales')");
            writer.insert("INSERT INTO Employees (id, name, birthday, hire_date, department) VALUES (3, 'Alice Johnson', '1990-05-20', '2015-01-10', 'IT')," +
                    "(4, 'Mike Brown', '1985-11-12', '2018-04-01', 'HR')");

            JdbcReader reader = new JdbcReader();
            reader.setConn(conn);
            List<Map<String, Object>> list = reader.queryAsMapList("SELECT * FROM Employees");
            System.out.println(list);
        }

        return dataSource;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CRUD_Service getCRUD_Service() throws SQLException {
        CRUD_Service crud = new CRUD_Service();

        Connection connection = dataSource().getConnection();

        JdbcWriter writer = new JdbcWriter();
        writer.setConn(connection);
        writer.setDatabaseVendor(DatabaseVendor.DERBY);

        JdbcReader reader = new JdbcReader();
        reader.setConn(connection);
        reader.setDatabaseVendor(DatabaseVendor.DERBY);

        crud.setWriter(writer);
        crud.setReader(reader);

        return crud;
    }
}