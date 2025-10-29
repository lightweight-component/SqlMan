package com.ajaxjs.sqlman_v2;

import com.ajaxjs.sqlman.JdbcConnection;
import com.ajaxjs.sqlman.JdbcConstants;
import com.ajaxjs.sqlman.SmallMyBatis;
import lombok.Data;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

@Data
public class Action {
    /**
     * The connection to be used to execute the sql
     */
    Connection conn;

    public void setDataSource(DataSource dataSource) {
        conn = JdbcConnection.getConnection(dataSource);
    }

    public void setConn() {
        conn = JdbcConnection.getConnection();
    }

    /**
     * The sql to be executed
     */
    String sql;

    public void setSql(String sql, Map<String, Object> keyParams) {
        this.sql = sql; // TODO 模板替换
    }

    public void setDynamicSql(String sql, Map<String, Object> keyParams) {
        this.sql = SmallMyBatis.handleSql(sql, keyParams);
    }

    /**
     * The parameters to be bound to the sql
     */
    Object[] params;

    /**
     * 当前数据库厂商，默认 MySQL
     */
    JdbcConstants.DatabaseVendor databaseVendor = JdbcConstants.DatabaseVendor.MYSQL;
}
